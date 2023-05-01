package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicLensFlare;
import data.scripts.util.MagicUI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//- deal damage to reduce the Incense level,
// + 10% of projectile damage will be applied when hit hull or 1, whichever is higher
// + 8% of the damage will be applied when hit shield or 1, whichever is higher.
//- Incense cap will be 100% ship flux dissipation stats
//- Incense regeneration stat will scale with ship flux capacitor, base will be 5/10/15/20 for each class plus
//1% of ship's flux capacitor stats
public class ps_incensemanufactured extends BaseHullMod {

    private static final String INCENSE_TEXT = "Incense";
    private static final float INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION = 1f;
    private static final float INCENSE_REGENERATION_PERCENTAGE_FROM_CAPACITOR = 0.01f;
    private static final float
            INCENSE_REGENERATION_BASE_FRIGATE = 5f,
            INCENSE_REGENERATION_BASE_DESTROYER = 10f,
            INCENSE_REGENERATION_BASE_CRUISER = 15f,
            INCENSE_REGENERATION_BASE_CAPITAL = 20f;
    public float TIME_DAL_BONUS = 10f;
    public float MAX_INCENSE_LEVEL_TIME_DAL_BONUS = 0.95f;
    private static final float ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_HULL = 0.1f;
    private static final float ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_SHIELD = 0.08f;

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
    private final IntervalUtil spawnFlaresInterval = new IntervalUtil(1f, 3f);
    //UP = Unsolidified Procedure
    private final float UP_HITPOINTS_START = 0.6f;
    private final float UP_ROF_BONUS = 3f;
    private final float UP_WEAPON_FLUX_BONUS = 0.5f;
    private final float UP_EMP_NEGATE_BONUS = 0.5f;
    private boolean UP_ACTIVATE_BONUS = false;
    private final float UP_STANDSTILL_DURATION = 2f; //3 second
    private boolean UP_STANDSTILL_ACTIVATED = false;
    private boolean UP_STANDSTILL_POPUP = false;
    private boolean UP_ACTIVATED = false;

    //Credit to PureTilt cuz I took reference from VIC
    //todo: test out to see if its balance or not, thinking may be add another bonus
    //todo: description + hullmod sprite (may be some particle with a fire like object in the center)

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }
        //INCENSE
        float incenseLevel = 0f;
        float incenseCap = ship.getMutableStats().getFluxDissipation().modified * INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION;

        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();

        MutableShipStatsAPI stats = ship.getMutableStats();

        if (customCombatData.get("ps_incenselevel" + id) instanceof Float)
            incenseLevel = (float) customCombatData.get("ps_incenselevel" + id);

        float actualRegenerationBase = 0;
        if(incenseLevel < incenseCap) {
            switch (ship.getHullSize()) {
                case FRIGATE:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_FRIGATE;
                case DESTROYER:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_DESTROYER;
                case CRUISER:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_CRUISER;
                case CAPITAL_SHIP:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_CAPITAL;
            }
            if((incenseLevel + actualRegenerationBase) > incenseCap) {
                incenseLevel = incenseCap;
            } else {
                incenseLevel += (actualRegenerationBase + (ship.getMutableStats().getFluxCapacity().modified * INCENSE_REGENERATION_PERCENTAGE_FROM_CAPACITOR))* amount;
            }
        }
        MagicUI.drawInterfaceStatusBar(
                ship,  incenseLevel/incenseCap,
                ps_misc.PROJECT_SOLACE,
                ps_misc.PROJECT_SOLACE,
                incenseLevel/incenseCap,
                this.INCENSE_TEXT,
                (int) incenseLevel
        );

        //stats incense
        float time_bonus_apply = (incenseLevel/incenseCap) / MAX_INCENSE_LEVEL_TIME_DAL_BONUS * TIME_DAL_BONUS;
        if(time_bonus_apply > TIME_DAL_BONUS) {
            time_bonus_apply = TIME_DAL_BONUS;
        }
        stats.getTimeMult().modifyPercent(ship.getId(), time_bonus_apply);

        //FX INCENSE
        float jitterLevel = incenseLevel/incenseCap;
        float jitterRangeBonus = 0;

        ship.setJitter(this, ps_misc.PROJECT_SOLACE_JITTER, jitterLevel, 3, 0, 0 + jitterRangeBonus);
        ship.setJitterUnder(this, ps_misc.PROJECT_SOLACE_JITTER_UNDER, jitterLevel, 25, 0f, 7f + jitterRangeBonus);

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("ps_solacecore_timedal", "graphics/icons/hullsys/temporal_shell.png", "Time Dilation", Math.round(time_bonus_apply) + "%", false);
        }
        if(ship.getShield() != null) {
            ship.getShield().setInnerColor(new Color(255,(Math.round((incenseLevel/incenseCap * 50) + 170)),(Math.round((incenseLevel/incenseCap * 55) + 170)),(Math.round((incenseLevel/incenseCap * 100) + 35))));
        }

//        Global.getCombatEngine().addFloatingText(ship.getLocation(), String.valueOf((Math.round((incenseLevel/incenseCap * 15) + 240))), 60, Color.WHITE, ship, 0.25f, 0.25f);

        //flares hull hitpoints
        spawnFlaresInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if(spawnFlaresInterval.intervalElapsed() && ship.getHitpoints() < ship.getMaxHitpoints() * 0.6f) {
            Vector2f spawnLocation = MathUtils.getRandomPointInCircle(
                    ship.getLocation(),
                    ship.getCollisionRadius()
            );
            MagicLensFlare.createSharpFlare(
                Global.getCombatEngine(),
                ship,
                spawnLocation,
                incenseLevel/incenseCap * 4,
                300,
                0,
                ps_misc.PROJECT_SOLACE_LIGHT,
                new Color(200,200,255)
            );
            Global.getCombatEngine().addSwirlyNebulaParticle(
                spawnLocation,
                new Vector2f(0, 0),
                incenseLevel/incenseCap * 70f,
                1f,0.1f,0.2f,
                0.9f,
                ps_misc.PROJECT_SOLACE,
                true
            );
        }

        MathUtils.clamp(incenseLevel, 0, incenseCap);
        customCombatData.put("ps_incenselevel" + id, incenseLevel);

        //UNSOLIDIFIED PROCEDURE
        //lower shield, reduce EMP damage by 50%
        // triple fire rate, lower weapon flux cost, refill all missile for a certain amount of time,
        // repair all weapon after the charge
        //Stand still for a couple of second for the activation
        if(ship.getHitpoints() < ship.getMaxHitpoints() * UP_HITPOINTS_START) {
            //standstill
            if(!UP_STANDSTILL_ACTIVATED) {
                float standstillTimer = 0f;
                if (customCombatData.get("ps_standstilltimer" + id) instanceof Float)
                    standstillTimer = (float) customCombatData.get("ps_standstilltimer" + id);
                standstillTimer += amount;
                if(!UP_STANDSTILL_POPUP) {
                    Global.getCombatEngine().addFloatingText(ship.getLocation(), "Charging...", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);
                    UP_STANDSTILL_POPUP = true;
                }

                if(standstillTimer < UP_STANDSTILL_DURATION) {
                    stats.getAcceleration().modifyPercent(id, 0.1f);
                    stats.getDeceleration().modifyPercent(id, 0.1f);
                    stats.getHullDamageTakenMult().modifyMult(id, 0.1f);
                    stats.getArmorDamageTakenMult().modifyMult(id, 0.4f);
                    if (ship == Global.getCombatEngine().getPlayerShip()) {
                        Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_standstill", "graphics/icons/hullsys/temporal_shell.png", "Unsolidified Procedure charging...", Math.round(UP_STANDSTILL_DURATION - standstillTimer) + "s", false);
                        Global.getCombatEngine().getTimeMult().modifyMult(id, 1f/(1f+standstillTimer));
                    }
                    //FX
                    ship.setJitterUnder(this, ps_misc.PROJECT_SOLACE_UP_STANDSTILL, standstillTimer/UP_STANDSTILL_DURATION, 25, 0f, 7f + (standstillTimer/UP_STANDSTILL_DURATION * 10f));
                    ship.setCircularJitter(true);
                } else {
                    stats.getAcceleration().unmodify(id);
                    stats.getDeceleration().unmodify(id);
                    stats.getHullDamageTakenMult().unmodify(id);
                    stats.getArmorDamageTakenMult().unmodify(id);
                    Global.getCombatEngine().getTimeMult().unmodify(id);
                    UP_STANDSTILL_ACTIVATED = true;
                    for(WeaponAPI weapon: ship.getAllWeapons()) {
                        weapon.repair();
                    }

                    //FX when go out of berserk
                    for(int i = 0; i < 10; i++) {
                        SimpleEntity fromEntity = new SimpleEntity(MathUtils.getRandomPointInCircle(
                                ship.getLocation(),
                                ship.getCollisionRadius()
                        ));
                        SimpleEntity toEntity = new SimpleEntity(MathUtils.getRandomPointInCircle(
                                ship.getLocation(),
                                ship.getCollisionRadius()
                        ));
                        Global.getCombatEngine().spawnEmpArcVisual(fromEntity.getLocation(),
                                fromEntity,
                                toEntity.getLocation(),
                                toEntity,
                                2,
                                new Color(255, 102, 0, 29),
                                new Color(255, 101, 21, 255)
                        );
                    }
                }
                customCombatData.put("ps_standstilltimer" + id, standstillTimer);

            }
            if(UP_STANDSTILL_ACTIVATED) {
                //Finish standstill
                //Move to berserk state
                if(ship.getShield() != null) ship.getShield().toggleOff();
                ship.setJitter(this, ps_misc.PROJECT_SOLACE_UP_ACTIVATION, 1.2f, 1, 3, 10f);
                ship.getEngineController().getFlameColorShifter().shift(this, ps_misc.PROJECT_SOLACE_UP_ACTIVATION, 0.2f, 1f, 1f);
                if(!UP_ACTIVATE_BONUS) {
                    stats.getBallisticRoFMult().modifyMult(id, UP_ROF_BONUS);
                    stats.getBallisticWeaponFluxCostMod().modifyMult(id, UP_WEAPON_FLUX_BONUS);
                    stats.getEnergyRoFMult().modifyMult(id, UP_ROF_BONUS);
                    stats.getEnergyWeaponFluxCostMod().modifyMult(id, UP_WEAPON_FLUX_BONUS);
                    stats.getEmpDamageTakenMult().modifyMult(id, UP_EMP_NEGATE_BONUS);
                    for (WeaponAPI weapon : ship.getAllWeapons()) {
                        if(weapon.getDamage().isMissile()) {
                            weapon.setAmmo(weapon.getMaxAmmo());
                        }
                    }
                    ship.getCaptain().setPersonality(Personalities.RECKLESS     );
                    UP_ACTIVATE_BONUS = true;
                }
            }
        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

    }

//    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
//
//    }
//
//    @Override
//    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//
//    }

    public static class ps_damagetakenlistener implements DamageTakenModifier {

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            float incenseLevel = 0f;
            float incenseCap = ((ShipAPI)target).getMutableStats().getFluxDissipation().modified * INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION;
            Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
            String id = ((ShipAPI) target).getId();

            if (customCombatData.get("ps_incenselevel" + id) instanceof Float)
                incenseLevel = (float) customCombatData.get("ps_incenselevel" + id);
//            Global.getCombatEngine().addFloatingText(target.getLocation(), String.valueOf(incenseLevel), 60, Color.WHITE, target, 0.25f, 0.25f);

            if (param instanceof DamagingProjectileAPI && incenseLevel > 0) {
                float projectileDamage = 0;
                if(shieldHit) {
                    projectileDamage = (((DamagingProjectileAPI) param).getDamage().getDamage()) * ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_SHIELD;
                } else {
                    projectileDamage = (((DamagingProjectileAPI) param).getDamage().getDamage()) * ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_HULL;
                }
                if (projectileDamage < 1) projectileDamage = 1;
                incenseLevel -= projectileDamage;
            }

            MathUtils.clamp(incenseLevel, 0, incenseCap);
            customCombatData.put("ps_incenselevel" + id, incenseLevel);
            return null;
        }
    }
}
