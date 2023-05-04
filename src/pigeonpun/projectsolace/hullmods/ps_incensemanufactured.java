package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.DoNotBackOff;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicLensFlare;
import data.scripts.util.MagicUI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
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
    private static final float INCENSE_LEVEL_BONUS_FROM_DISSIPATION = 0.01f;
    private static final float INCENSE_CAP_BONUS_FROM_CAPACITOR = 0.01f;
    private static final float
            INCENSE_REGENERATION_BASE_FRIGATE = 5f,
            INCENSE_REGENERATION_BASE_DESTROYER = 10f,
            INCENSE_REGENERATION_BASE_CRUISER = 15f,
            INCENSE_REGENERATION_BASE_CAPITAL = 20f;

    public float TIME_DAL_BONUS = 10f;
    public float MAX_INCENSE_LEVEL_TIME_DAL_BONUS = 1f;
    private static final float ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_HULL = 0.1f;
    private static final float ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_SHIELD = 0.15f;
    private final float EMP_RANGE = 500f;
    private final float EMP_DAMAGE = 50f;
    private final Color EMP_COLOR = new Color(241, 245, 40, 255);
    private final IntervalUtil spawnFlaresInterval = new IntervalUtil(0.5f, 1f);
    private final IntervalUtil spawnEMPInterval = new IntervalUtil(0.1f, 0.2f);
    private final IntervalUtil spawnEMPStartUP = new IntervalUtil(0.2f, 0.8f);
    //UP = Unsolidified Procedure
    private final float UP_HITPOINTS_START = 0.6f;
    private final float UP_ROF_BONUS = 3f;
    private final float UP_WEAPON_FLUX_BONUS = 0.5f;
    private final float UP_EMP_NEGATE_BONUS = 0.5f;
    private final float UP_ARMOR_REPAIR = 0.5f;
    private static final float
            UP_BONUS_DAMAGE_FRIGATE = 0.5f,
            UP_BONUS_DAMAGE_DESTROYER = 0.4f,
            UP_BONUS_DAMAGE_CRUISER = 0.3f,
            UP_BONUS_DAMAGE_CAPITAL = 0.2f;
    private final float UP_STANDSTILL_DURATION = 5f; //x second

    //Credit to PureTilt cuz I took reference from VIC
    //todo: test out to see if its balance or not, thinking may be add another bonus
    //todo: description + hullmod sprite (may be some particle with a fire like object in the center)
    //todo: sound for after charge
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }
        /////////////////
        //INCENSE
        /////////////////
        float incenseLevel = 0f;
        float incenseCap = (ship.getMutableStats().getFluxDissipation().modified * INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION) + (ship.getMutableStats().getFluxDissipation().modified * INCENSE_CAP_BONUS_FROM_CAPACITOR);

        boolean up_activate_bonus = false;
        boolean up_standstill_activated = false;
//        boolean up_standstill_popup = false;

        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();

        MutableShipStatsAPI stats = ship.getMutableStats();

        if (customCombatData.get("ps_incenselevel" + id) instanceof Float)
            incenseLevel = (float) customCombatData.get("ps_incenselevel" + id);
        if (customCombatData.get("up_activate_bonus" + id) instanceof Float)
            up_activate_bonus = (boolean) customCombatData.get("up_activate_bonus" + id);
        if (customCombatData.get("up_standstill_activated" + id) instanceof Float)
            up_standstill_activated = (boolean) customCombatData.get("up_standstill_activated" + id);
//        if (customCombatData.get("up_standstill_popup" + id) instanceof Float)
//            up_standstill_popup = (boolean) customCombatData.get("up_standstill_popup" + id);

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
                incenseLevel += (actualRegenerationBase + (ship.getMutableStats().getFluxCapacity().modified * INCENSE_LEVEL_BONUS_FROM_DISSIPATION))* amount;
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

        ///////////////////
        //stats incense
        ///////////////////
        float time_bonus_apply = (incenseLevel/incenseCap) / MAX_INCENSE_LEVEL_TIME_DAL_BONUS * TIME_DAL_BONUS;
        if(time_bonus_apply > TIME_DAL_BONUS) {
            time_bonus_apply = TIME_DAL_BONUS;
        }
        stats.getTimeMult().modifyPercent(ship.getId(), time_bonus_apply);

        ////////////////
        //FX INCENSE
        ////////////////
        float jitterLevel = incenseLevel/incenseCap;
        float jitterRangeBonus = 0;

        ship.setJitter(this, ps_misc.PROJECT_SOLACE_JITTER, jitterLevel, 3, 0, 0 + jitterRangeBonus);
        ship.setJitterUnder(this, ps_misc.PROJECT_SOLACE_JITTER_UNDER, jitterLevel, 25, 0f, 7f + jitterRangeBonus);

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("ps_solacecore_timedal", "graphics/icons/hullsys/temporal_shell.png", "Time Dilation", Math.round(time_bonus_apply) + "%", false);
        }
        if(ship.getShield() != null) {
            ship.getShield().setInnerColor(
                new Color(255,
                    MathUtils.clamp(Math.round((incenseLevel/incenseCap * 50) + 170), 0, 255),
                    MathUtils.clamp(Math.round((incenseLevel/incenseCap * 55) + 170), 0, 255),
                    MathUtils.clamp(Math.round((incenseLevel/incenseCap * 100) + 35), 0, 255))
            );
        }

//        Global.getCombatEngine().addFloatingText(ship.getLocation(), String.valueOf((Math.round((incenseLevel/incenseCap * 15) + 240))), 60, Color.WHITE, ship, 0.25f, 0.25f);

        MathUtils.clamp(incenseLevel, 0, incenseCap);
        customCombatData.put("ps_incenselevel" + id, incenseLevel);

        if(ship.getVariant().hasHullMod("do_not_back_off") && !up_activate_bonus) {
            ship.getVariant().removeMod("do_not_back_off");
        }

        //UNSOLIDIFIED PROCEDURE (UP)
        // lower shield,
        // triple fire rate,
        // refill all missile,
        // emp will start sparking around the ship hitting any missile or fighter,
        // repair armor to the original amount
        // deal 50/40/30/20% more damage to cruiser and capital depend on ship class.
        if(ship.getHitpoints() < ship.getMaxHitpoints() * UP_HITPOINTS_START) {
            ////////////////
            //standstill
            ////////////////
            if(!up_standstill_activated) {
                float standstillTimer = 0f;
                if (customCombatData.get("ps_standstilltimer" + id) instanceof Float)
                    standstillTimer = (float) customCombatData.get("ps_standstilltimer" + id);
                standstillTimer += amount;
//                if(!up_standstill_popup) {
//                    Global.getCombatEngine().addFloatingText(ship.getLocation(), "Charging...", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);
//                    up_standstill_popup = true;
//                }

                if(standstillTimer < UP_STANDSTILL_DURATION) {
                    stats.getAcceleration().modifyPercent(id, 0.0f);
                    stats.getDeceleration().modifyPercent(id, 0.0f);
                    stats.getHullDamageTakenMult().modifyMult(id, 0.0f);
                    stats.getArmorDamageTakenMult().modifyMult(id, 0.0f);
                    if (ship == Global.getCombatEngine().getPlayerShip()) {
                        Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_standstill", "graphics/icons/hullsys/temporal_shell.png", "Unsolidified Procedure charging...", Math.round(UP_STANDSTILL_DURATION - standstillTimer) + "s", false);
                        Global.getCombatEngine().getTimeMult().modifyMult(id, 1f/(1f+standstillTimer));
                    }
                    for(WeaponAPI weapon: ship.getAllWeapons()) {
                        weapon.disable();
                    }
                    //FX
                    ship.setJitterUnder(this, ps_misc.PROJECT_SOLACE_UP_STANDSTILL, standstillTimer/UP_STANDSTILL_DURATION, 25, 0f, 7f + (standstillTimer/UP_STANDSTILL_DURATION * 10f));
                    ship.setCircularJitter(true);

                    //repair armor
                    ArmorGridAPI grid = ship.getArmorGrid();
                    int gridHeight = grid.getGrid()[0].length;
                    int gridWidth = grid.getGrid().length;

                    for (int x = 0; x < gridWidth; x++) {
                        for (int y = 0; y < gridHeight; y++) {
                            float armorhp = grid.getArmorValue(x,y);
                            float maxHPRepair = grid.getMaxArmorInCell() * UP_ARMOR_REPAIR;

                            //float toadd = 5; // amount you want to add per-cell
                            if(armorhp < maxHPRepair) {
                                grid.setArmorValue(x,y, armorhp + (maxHPRepair * amount));
                            }
                            //grid.setArmorValue(x,y, Math.min(maxHPRepair, armorhp + toadd));
                            //grid.setArmorValue(x,y, maxHP * UP_ARMOR_REPAIR);
                        }
                    }
                } else {
                    stats.getAcceleration().unmodify(id);
                    stats.getDeceleration().unmodify(id);
                    stats.getHullDamageTakenMult().unmodify(id);
                    stats.getArmorDamageTakenMult().unmodify(id);
                    Global.getCombatEngine().getTimeMult().unmodify(id);
                    up_standstill_activated = true;
                    for(WeaponAPI weapon: ship.getAllWeapons()) {
                        weapon.repair();
                    }
                }
                customCombatData.put("ps_standstilltimer" + id, standstillTimer);

            }
            if(up_standstill_activated) {
                //////////////////////////
                //Finish standstill
                //Move to berserk state
                //////////////////////////
                if(ship.getShield() != null) ship.getShield().toggleOff();
                ship.setJitter(this, ps_misc.PROJECT_SOLACE_UP_ACTIVATION, 1.5f, 1, 3, 10f);
                ship.getEngineController().getFlameColorShifter().shift(this, ps_misc.PROJECT_SOLACE_UP_ACTIVATION, 0.2f, 1f, 1f);

                //damage boost
                float damageToCruiser = 0f;
                float damageToCapital = 0f;
                switch (ship.getHullSize()) {
                    case FRIGATE:
                        damageToCruiser = UP_BONUS_DAMAGE_FRIGATE;
                        damageToCapital = UP_BONUS_DAMAGE_FRIGATE;
                    case DESTROYER:
                        damageToCruiser = UP_BONUS_DAMAGE_DESTROYER;
                        damageToCapital = UP_BONUS_DAMAGE_DESTROYER;
                    case CRUISER:
                        damageToCruiser = UP_BONUS_DAMAGE_CRUISER;
                        damageToCapital = UP_BONUS_DAMAGE_CRUISER;
                    case CAPITAL_SHIP:
                        damageToCruiser = UP_BONUS_DAMAGE_CAPITAL;
                        damageToCapital = UP_BONUS_DAMAGE_CAPITAL;
                }
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_shield_down", "", "Shield down", "", true);
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_emp_emit", "graphics/icons/hullsys/emp_emitter", "Discharging EMP", "", false);
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_rof", "graphics/icons/hullsys/ammo_feeder.png", "RoF bonus", "+" + String.valueOf(UP_ROF_BONUS * 100) + "%", false);
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_weapon_flux", "", "Weapon flux reduction", String.valueOf(UP_WEAPON_FLUX_BONUS * 100) + "%", false);
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_emp", "", "EMP damage taken reduction", String.valueOf(UP_EMP_NEGATE_BONUS * 100) + "%", false);
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_cruiser_dmg", "", "Cruiser damage bonus", "+" + String.valueOf(damageToCruiser * 100) + "%", false);
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_cap_dmg", "", "Capital damage bonus", "+" + String.valueOf(damageToCapital * 100) + "%", false);

                //Ship FX + EMP when go out of stand still
                spawnEMPStartUP.advance(amount);
                float spawnEMPCountPerTime = 2;
                for(int i = 0; i < spawnEMPCountPerTime; i++) {
                    if(spawnEMPStartUP.intervalElapsed()) {
                        SimpleEntity fromEntity = new SimpleEntity(MathUtils.getRandomPointInCircle(
                                ship.getLocation(),
                                ship.getCollisionRadius()
                        ));
                        SimpleEntity toEntity = new SimpleEntity(MathUtils.getRandomPointInCircle(
                                ship.getLocation(),
                                ship.getCollisionRadius()
                        ));
                        List<CombatEntityAPI> targetNearby = CombatUtils.getEntitiesWithinRange(ship.getLocation(), EMP_RANGE);
                        HashSet<CombatEntityAPI> listTargets = new HashSet<>();
                        for (CombatEntityAPI entity: targetNearby) {
                            if(entity instanceof MissileAPI || entity instanceof FighterWingAPI) {
                                //projectile is on player team => damage enemy
                                //projectile is from enemy => damage player + allies ships
                                if(ship.getOwner() != entity.getOwner()) {
                                    listTargets.add(entity);
                                }
                            }
                        }
                        if(!listTargets.isEmpty()) {
                            for (CombatEntityAPI entity: listTargets) {
                                engine.spawnEmpArc(
                                        ship,
                                        fromEntity.getLocation(),
                                        fromEntity,
                                        entity,
                                        DamageType.ENERGY,
                                        EMP_DAMAGE,
                                        0,
                                        10000,
                                        null,
                                        MathUtils.getRandomNumberInRange(5f,10f),
                                        EMP_COLOR,
                                        new Color(255, 255,255, 255)
                                );
                                if(entity.isExpired() || entity.getHitpoints() < 0) {
                                    listTargets.remove(entity);
                                }
                            }
                        } else {
                            Global.getCombatEngine().spawnEmpArcVisual(fromEntity.getLocation(),
                                    fromEntity,
                                    toEntity.getLocation(),
                                    toEntity,
                                    2,
                                    new Color(255, 102, 0, 100),
                                    new Color(255, 101, 21, 255)
                            );
                        }
                    }
                }

                ////////////////////
                //FX EMP + flares
                ////////////////////
                spawnFlaresInterval.advance(amount);
                if(spawnFlaresInterval.intervalElapsed()) {
                    Vector2f spawnLocation = MathUtils.getRandomPointInCircle(
                            ship.getLocation(),
                            ship.getCollisionRadius() * 1.2f
                    );
                    SimpleEntity simpleE = new SimpleEntity(spawnLocation);
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
                    spawnEMPInterval.advance(amount);
                    if(spawnEMPInterval.intervalElapsed()) {
                        float angle = (float) (Math.random() * 360);
                        Vector2f endArcPoint = MathUtils.getPointOnCircumference(spawnLocation, 50f, angle);

                        engine.spawnEmpArcVisual(
                                spawnLocation,
                                ship,
                                endArcPoint,
                                new SimpleEntity(endArcPoint),
                                MathUtils.getRandomNumberInRange(5f,10f),
                                EMP_COLOR,
                                new Color(255, 255,255, 255)
                        );
                    }
                }

                if(!up_activate_bonus) {
                    stats.getBallisticRoFMult().modifyMult(id, UP_ROF_BONUS);
                    stats.getBallisticWeaponFluxCostMod().modifyMult(id, UP_WEAPON_FLUX_BONUS);
                    stats.getEnergyRoFMult().modifyMult(id, UP_ROF_BONUS);
                    stats.getEnergyWeaponFluxCostMod().modifyMult(id, UP_WEAPON_FLUX_BONUS);
                    stats.getEmpDamageTakenMult().modifyMult(id, UP_EMP_NEGATE_BONUS);

                    //damage boost
                    stats.getDamageToCruisers().modifyMult(id, 1f+ damageToCruiser);
                    stats.getDamageToCapital().modifyMult(id, 1f + damageToCapital);
                    //refill missiles
                    for (WeaponAPI weapon : ship.getAllWeapons()) {
                        if(weapon.getDamage().isMissile()) {
                            weapon.setAmmo(weapon.getMaxAmmo());
                        }
                    }
                    stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f);
                    ship.getCaptain().setPersonality(Personalities.RECKLESS);
                    ship.getVariant().addMod("do_not_back_off");
                    up_activate_bonus = true;
                }
            }
        }
        customCombatData.put("up_activate_bonus" + id, up_activate_bonus);
        customCombatData.put("up_standstill_activated" + id, up_standstill_activated);
//        customCombatData.put("up_standstill_popup" + id, up_standstill_popup);
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
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
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
