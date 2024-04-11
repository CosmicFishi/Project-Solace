package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_ringrenderer;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ps_lastkin extends BaseHullMod {
    Logger log = Global.getLogger(ps_lastkin.class);
    public static final Color RING_COLOR_SHIP = new Color(15, 187, 110, 155);
    public static final Color MISSILE_SLOWDOWN_COLOR = new Color(255,187,95,155);
    public static final float MISSILE_SLOW_RADIUS = 600f;
    public static final String SPEED_BOOST_ACTIVE_KEY = "ps_lastkinzeroflux";
    public static final float SPEED_BOOST_MAX_SPEED_MULT = 3.5f;
    public static final float SPEED_BOOST_ACCELERATION_MULT = 3f;
    public static final float SPEED_BOOST_DECELERATION_MULT = 3f;
    public static final float SPEED_BOOST_MAX_DURATION = 3f; //in seconds
    public static final float VELOCITY_REDUCED = 20f;
    IntervalUtil spawnEMPInterval = new IntervalUtil(1.5f,3f);
    IntervalUtil spawnAfterImageInterval = new IntervalUtil(0.1f, 0.1f);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();
        float speedBoostCurrDuration = 0;

        if (customCombatData.get(SPEED_BOOST_ACTIVE_KEY + id) instanceof Float)
            speedBoostCurrDuration = (Float) customCombatData.get(SPEED_BOOST_ACTIVE_KEY + id);
        //speed boost when enter phase
        if(ship.isPhased()) {
            if(speedBoostCurrDuration < SPEED_BOOST_MAX_DURATION) {
                speedBoostCurrDuration += amount;
                ship.getMutableStats().getMaxSpeed().modifyMult(SPEED_BOOST_ACTIVE_KEY, SPEED_BOOST_MAX_SPEED_MULT);
                ship.getMutableStats().getAcceleration().modifyMult(SPEED_BOOST_ACTIVE_KEY, SPEED_BOOST_ACCELERATION_MULT);
                ship.getMutableStats().getDeceleration().modifyMult(SPEED_BOOST_ACTIVE_KEY, SPEED_BOOST_DECELERATION_MULT);

                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    Global.getCombatEngine().maintainStatusForPlayerShip("ps_lastkin_maxspeed", "graphics/icons/hullsys/phase_cloak.png", "Max speed bonus", "" + String.valueOf(Math.round(SPEED_BOOST_MAX_SPEED_MULT * 100)) + "%", false);
                    Global.getCombatEngine().maintainStatusForPlayerShip("ps_lastkin_acceleration", "graphics/icons/hullsys/phase_cloak.png", "Acceleration bonus", "" + String.valueOf(Math.round(SPEED_BOOST_ACCELERATION_MULT * 100)) + "%", false);
                    Global.getCombatEngine().maintainStatusForPlayerShip("ps_lastkin_deceleration", "graphics/icons/hullsys/phase_cloak.png", "Deceleration bonus", "" + String.valueOf(Math.round(SPEED_BOOST_DECELERATION_MULT * 100)) + "%", false);
                }
            } else {
                ship.getMutableStats().getMaxSpeed().unmodify(SPEED_BOOST_ACTIVE_KEY);
                ship.getMutableStats().getAcceleration().unmodify(SPEED_BOOST_ACTIVE_KEY);
                ship.getMutableStats().getDeceleration().unmodify(SPEED_BOOST_ACTIVE_KEY);
            }

            //FX
            spawnAfterImageInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(spawnAfterImageInterval.intervalElapsed()) {
                ship.addAfterimage(
                        new Color(255, 255, 255,55),
                        0,
                        0,
                        ship.getVelocity().x * -1.1f,
                        ship.getVelocity().y * -1.1f,
                        1f,
                        0f,
                        0.2f,
                        0.2f,
                        false,
                        true,
                        false
                );
            }
        } else {
            speedBoostCurrDuration = 0;
            ship.getMutableStats().getMaxSpeed().unmodify(SPEED_BOOST_ACTIVE_KEY);
            ship.getMutableStats().getAcceleration().unmodify(SPEED_BOOST_ACTIVE_KEY);
            ship.getMutableStats().getDeceleration().unmodify(SPEED_BOOST_ACTIVE_KEY);
        }
        //slowdown missiles
        spawnEMPInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        List<MissileAPI> missilesNearby = CombatUtils.getMissilesWithinRange(ship.getLocation(), MISSILE_SLOW_RADIUS);
        //FX calculation
        WeightedRandomPicker<Vector2f> randomPointPicker = new WeightedRandomPicker<>();
        WeightedRandomPicker<MissileAPI> randomMissilePicker = new WeightedRandomPicker<>();
        ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
        for (BoundsAPI.SegmentAPI s: ship.getExactBounds().getSegments()) {
            //Global.getCombatEngine().addFloatingText( s.getP1() ,  ".", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);
            if(!randomPointPicker.getItems().contains(s.getP1())) {
                randomPointPicker.add(s.getP1());
            }
        }
        for (MissileAPI missile: missilesNearby) {
            if(missile != null && missile.getOwner() != ship.getOwner()) {
                randomMissilePicker.add(missile);
                missile.getVelocity().scale((float) (100 - VELOCITY_REDUCED) / 100);
                missile.setJitter(this, MISSILE_SLOWDOWN_COLOR, 1, 15, 3f, 10f);
            }
        }
        //FX
        if(!randomPointPicker.isEmpty() && !randomMissilePicker.isEmpty()) {
            if(spawnEMPInterval.intervalElapsed()) {
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 5); i++) {
                    MissileAPI pickedMissile = randomMissilePicker.pick();
                    Global.getCombatEngine().spawnEmpArcVisual(
                            randomPointPicker.pick(),
                            ship,
                            pickedMissile.getLocation(),
                            pickedMissile,
                            MathUtils.getRandomNumberInRange(5f,10f),
                            new Color(43,255,160,255),
                            new Color(255, 255,255, 255)
                    );
                }

            }
        }
//        log.info(speedBoostCurrDuration);
        customCombatData.put(SPEED_BOOST_ACTIVE_KEY + id, speedBoostCurrDuration);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//        log.warn("Applying renderer");
        Global.getCombatEngine().addLayeredRenderingPlugin(new ps_ringrenderer(ship, MISSILE_SLOW_RADIUS, RING_COLOR_SHIP, 8f, 40));
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.MOUNT_UNIVERSAL;
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        LabelAPI label = tooltip.addPara("May your will mend you back into this existence as the last extant Dame", opad, h, "");

        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        label = tooltip.addPara("When ship is phased, have a brief duration of boosted manoeuvrability and max speed", opad, h,"");

        label = tooltip.addPara("An aura circles the ship, any missiles enter the zone will have its velocity reduced by %s. The effect is stackable with its ship system", opad, h,"" + Math.round(VELOCITY_REDUCED) + "%");
        label.setHighlight("" + Math.round(VELOCITY_REDUCED) + "%");
        label.setHighlightColors(good);
    }
}
