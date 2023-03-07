package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.Objects;

public class ps_exaltedshellstats extends BaseShipSystemScript {

    public static final float SHIELD_EFF_FLUX_BONUS = 10f;
    public static final float SHIELD_EFF_BASE_BONUS = 10f;
    public static final float ROF_BASE_BONUS = 1f;
    public static final float ROF_FLUX_BONUS = 1f;
    public static final float FLUX_LEVEL_MAX_BONUS = 0.8f;
    private static final float FLUX_CONSUME = 0.2f;
    public float ACTUAL_SHIELD_EFF_FLUX_BONUS = 0;
    public float ACTUAL_ROF_FLUX_BONUS = 0;
    private float SHIP_FLUX_LEVEL_BEFORE_ACTIVATED = 0;
    private boolean activateOnce = false;
    private IntervalUtil spawnEMPInterval = new IntervalUtil(0.2f, 0.5f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if(ship != null) {
            if(!activateOnce) {
                SHIP_FLUX_LEVEL_BEFORE_ACTIVATED = ship.getFluxLevel();
                ship.getFluxTracker().decreaseFlux(ship.getFluxTracker().getCurrFlux() * FLUX_CONSUME);
                activateOnce = true;
            }
            //shield eff
            ACTUAL_SHIELD_EFF_FLUX_BONUS = (SHIP_FLUX_LEVEL_BEFORE_ACTIVATED / FLUX_LEVEL_MAX_BONUS) * SHIELD_EFF_FLUX_BONUS + SHIELD_EFF_BASE_BONUS;
            if(ACTUAL_SHIELD_EFF_FLUX_BONUS > SHIELD_EFF_FLUX_BONUS + SHIELD_EFF_BASE_BONUS) {
                ACTUAL_SHIELD_EFF_FLUX_BONUS = SHIELD_EFF_FLUX_BONUS + SHIELD_EFF_BASE_BONUS;
            }
            stats.getShieldDamageTakenMult().modifyMult(id, 1 - ((ACTUAL_SHIELD_EFF_FLUX_BONUS * 0.01f)));
            //ROF
            ACTUAL_ROF_FLUX_BONUS = (SHIP_FLUX_LEVEL_BEFORE_ACTIVATED / FLUX_LEVEL_MAX_BONUS) * ROF_FLUX_BONUS + ROF_BASE_BONUS;
            if(ACTUAL_ROF_FLUX_BONUS > ROF_FLUX_BONUS + ROF_BASE_BONUS) {
                ACTUAL_ROF_FLUX_BONUS = ROF_FLUX_BONUS + ROF_BASE_BONUS;
            }
            stats.getEnergyRoFMult().modifyMult(id, ACTUAL_ROF_FLUX_BONUS);
            stats.getBallisticRoFMult().modifyMult(id, ACTUAL_ROF_FLUX_BONUS);

            //fx
            Vector2f crystalLocation = null;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.isDecorative() && Objects.equals(weapon.getId(), ps_misc.SOLACE_CORE_DECORATIVE_MOUNT_ID)) {
                    crystalLocation = weapon.getLocation();
                }
            }
            spawnEMPInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(crystalLocation != null && spawnEMPInterval.intervalElapsed()) {
                float startArcArea = (float) (ship.getCollisionRadius() + Math.random() * MathUtils.getRandomNumberInRange(10f, 60f));
                float angle = (float) (Math.random() * 360);
                Vector2f endArcPoint = MathUtils.getPointOnCircumference(crystalLocation, startArcArea * 0.5f , angle);
                engine.spawnEmpArc(
                        ship,
                        crystalLocation,
                        new SimpleEntity(crystalLocation),
                        new SimpleEntity(endArcPoint),
                        DamageType.ENERGY,
                        0,
                        0,
                        10000,
                        null,
                        MathUtils.getRandomNumberInRange(1f, 2f),
                        new Color(0,255,200,155),
                        new Color(255, 255,255, 255)
                );
            }

            ship.getEngineController().fadeToOtherColor(this, ps_misc.PROJECT_SOLACE_LIGHT, new Color(0,0,0,0), 1f, 0.67f);
            ship.getShield().setRingColor(ps_misc.PROJECT_SOLACE_LIGHT);
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        activateOnce = false;
        ACTUAL_SHIELD_EFF_FLUX_BONUS = 0;
        ACTUAL_ROF_FLUX_BONUS = 0;
        SHIP_FLUX_LEVEL_BEFORE_ACTIVATED = 0;
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getHardFluxDissipationFraction().unmodify(id);

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Increased shield effeciency by " + Math.round(ACTUAL_SHIELD_EFF_FLUX_BONUS) + "%", false);
        }
        if (index == 1) {
            return new StatusData("Increased rate of fire by " + Math.round(ACTUAL_ROF_FLUX_BONUS * 100) + "%", false);
        }
        return null;
    }
}
