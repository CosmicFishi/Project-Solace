package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.combat.CombatUtils;
import pigeonpun.projectsolace.com.ps_ringrenderer;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ps_lastkin extends BaseHullMod {
    Logger log = Global.getLogger(ps_lastkin.class);
    public static final Color RING_COLOR_SHIP = new Color(15, 187, 110, 155);
    public static final Color MISSILE_SLOWDOWN_COLOR = new Color(255,187,95,155);
    public static final float MISSILE_SLOW_RADIUS = 800f;
    public static final String SPEED_BOOST_ACTIVE_KEY = "ps_lastkinzeroflux";
    public static final float SPEED_BOOST_MAX_SPEED_MULT = 3.5f;
    public static final float SPEED_BOOST_ACCELERATION_MULT = 3f;
    public static final float SPEED_BOOST_DECELERATION_MULT = 3f;
    public static final float SPEED_BOOST_MAX_DURATION = 3f; //in seconds
    public static final float VELOCITY_REDUCED = 20f;

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
        if(ship.isPhased() && speedBoostCurrDuration < SPEED_BOOST_MAX_DURATION) {
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
            speedBoostCurrDuration = 0;
            ship.getMutableStats().getMaxSpeed().unmodify(SPEED_BOOST_ACTIVE_KEY);
            ship.getMutableStats().getAcceleration().unmodify(SPEED_BOOST_ACTIVE_KEY);
            ship.getMutableStats().getDeceleration().unmodify(SPEED_BOOST_ACTIVE_KEY);
        }
        //slowdown missiles
        List<MissileAPI> missilesNearby = CombatUtils.getMissilesWithinRange(ship.getLocation(), MISSILE_SLOW_RADIUS);
        for (MissileAPI missile: missilesNearby) {
            if(missile != null && missile.getOwner() == 1) {
                //todo: fx
                //bind a EMP arc with the missile and the ship bound, empowered the ship in someway ?
                missile.getVelocity().scale((float) (100 - 20) / 100);
                missile.setJitter(this, MISSILE_SLOWDOWN_COLOR, 1, 15, 2f, 5f);
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

        LabelAPI label = tooltip.addPara("Description here", opad, h, "");

        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        label = tooltip.addPara("When ship is phased, have a brief duration of boosted manoeuvrability and max speed", opad, h,"");

        label = tooltip.addPara("An aura circles the ship, any missiles enter the zone will have its velocity reduced by %s", opad, h,"" + VELOCITY_REDUCED + "%");
        label.setHighlight("" + VELOCITY_REDUCED + "%");
        label.setHighlightColors(good);
    }
}
