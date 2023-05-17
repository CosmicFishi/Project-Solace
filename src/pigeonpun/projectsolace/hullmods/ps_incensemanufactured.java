package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.DoNotBackOff;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
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

public class ps_incensemanufactured extends BaseHullMod {

    private static final String INCENSE_TEXT = "Incense";
    private static final float INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION = 1f;
    //private static final float INCENSE_LEVEL_BONUS_FROM_DISSIPATION = 0.05f;
    private static final float INCENSE_BONUS_FROM_CAPACITOR = 0.01f;
    private static final float
            INCENSE_REGENERATION_BASE_FRIGATE = 5f,
            INCENSE_REGENERATION_BASE_DESTROYER = 10f,
            INCENSE_REGENERATION_BASE_CRUISER = 20f,
            INCENSE_REGENERATION_BASE_CAPITAL = 40f;

    private static final float
            INCENSE_REGENERATION_CAP_FRIGATE = 30f,
            INCENSE_REGENERATION_CAP_DESTROYER = 60f,
            INCENSE_REGENERATION_CAP_CRUISER = 120f,
            INCENSE_REGENERATION_CAP_CAPITAL = 240f;

    public float TIME_DAL_BONUS = 10f;
    public float MAX_INCENSE_LEVEL_TIME_DAL_BONUS = 1f;
    private static final float ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_HULL_ARMOR = 0.4f;
    private static final float ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_SHIELD = 0.2f;
    private final float EMP_RANGE = 500f;
    private final float EMP_DAMAGE = 200f;
    private final Color EMP_COLOR = new Color(241, 245, 40, 255);
    private final IntervalUtil spawnFlaresInterval = new IntervalUtil(0.5f, 1f);
    private final IntervalUtil spawnEMPInterval = new IntervalUtil(0.1f, 0.2f);
    private final IntervalUtil spawnEMPStartUP = new IntervalUtil(0.2f, 0.8f);
    private final float spawnJitterTimerFrom = 0.2f;
    private final float spawnJitterTimerTo = 0.5f;
    private final float spawnJitterTimerWait = 0.1f;
    //UP = Unsolidified Procedure
    private final float UP_HITPOINTS_START = 0.3f;
    private final float UP_ROF_BONUS = 3f;
    private final float UP_WEAPON_FLUX_BONUS = 0.6f;
    private final float UP_EMP_NEGATE_BONUS = 0.5f;
    private final float UP_ARMOR_REPAIR = 0.7f;
    private static final float
            UP_BONUS_DAMAGE_FRIGATE = 0.5f,
            UP_BONUS_DAMAGE_DESTROYER = 0.4f,
            UP_BONUS_DAMAGE_CRUISER = 0.3f,
            UP_BONUS_DAMAGE_CAPITAL = 0.2f;
    private final float UP_STANDSTILL_DURATION = 5f; //x second

    //Credit to PureTilt cuz I took reference from VIC
    //todo: description + hullmod sprite (may be some particle with a fire like object in the center)
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
        float incenseCap = getIncenseCap(ship);

        boolean up_activate_bonus = false;
        boolean up_standstill_activated = false;
        boolean up_standstill_bonus_removed = false;
        float ps_spawnjitter_timer = 0;
//        boolean up_standstill_popup = false;

        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();

        MutableShipStatsAPI stats = ship.getMutableStats();

        if (customCombatData.get("ps_incenselevel" + id) instanceof Float)
            incenseLevel = (float) customCombatData.get("ps_incenselevel" + id);
        if (customCombatData.get("up_activate_bonus" + id) instanceof Boolean)
            up_activate_bonus = (boolean) customCombatData.get("up_activate_bonus" + id);
        if (customCombatData.get("up_standstill_activated" + id) instanceof Boolean)
            up_standstill_activated = (boolean) customCombatData.get("up_standstill_activated" + id);
        if (customCombatData.get("up_standstill_bonus_removed" + id) instanceof Boolean)
            up_standstill_bonus_removed = (boolean) customCombatData.get("up_standstill_bonus_removed" + id);
        if (customCombatData.get("ps_spawnjitter_timer" + id) instanceof Float)
            ps_spawnjitter_timer = (float) customCombatData.get("ps_spawnjitter_timer" + id);
//        if (customCombatData.get("up_standstill_popup" + id) instanceof Float)
//            up_standstill_popup = (boolean) customCombatData.get("up_standstill_popup" + id);

        float incenseRegen = getIncenseRegen(ship);
        if(incenseLevel < incenseCap) {
            if((incenseLevel + incenseRegen * amount) > incenseCap) {
                incenseLevel = incenseCap;
            } else {
                incenseLevel += incenseRegen * amount;
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
        // repair armor to certain amount of original
        // deal 50/40/30/20% more damage to cruiser and capital depend on ship class.
        if(ship.getHitpoints() < ship.getMaxHitpoints() * UP_HITPOINTS_START) {
            //DISABLE Shield
            if(ship.getShield() != null) ship.getShield().toggleOff();
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
//                    if (ship == Global.getCombatEngine().getPlayerShip()) {
//                        Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_standstill", "graphics/icons/hullsys/temporal_shell.png", "Unsolidified Procedure charging...", Math.round(UP_STANDSTILL_DURATION - standstillTimer) + "s", false);
//                        Global.getCombatEngine().getTimeMult().modifyMult(id, 1f/(1f+standstillTimer));
//                    }
                    Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_standstill", "graphics/icons/hullsys/temporal_shell.png", "Unsolidified Procedure charging...", Math.round(UP_STANDSTILL_DURATION - standstillTimer) + "s", false);
                    for(WeaponAPI weapon: ship.getAllWeapons()) {
                        weapon.disable();
                    }
                    //FX
                    ship.setJitterUnder(this, ps_misc.PROJECT_SOLACE_UP_STANDSTILL, standstillTimer/UP_STANDSTILL_DURATION, 25, 0f, 7f + (standstillTimer/UP_STANDSTILL_DURATION * 10f));
                    ship.setCircularJitter(true);
                    Global.getSoundPlayer().playLoop("ps_up_charging", ship, 1f,1f, ship.getLocation(), new Vector2f(0, 0));

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
                    if(!up_standstill_bonus_removed) {
                        stats.getAcceleration().unmodify(id);
                        stats.getDeceleration().unmodify(id);
                        stats.getHullDamageTakenMult().unmodify(id);
                        stats.getArmorDamageTakenMult().unmodify(id);
                        Global.getCombatEngine().getTimeMult().unmodify(id);
                        up_standstill_activated = true;
                        for(WeaponAPI weapon: ship.getAllWeapons()) {
                            weapon.repair();
                        }
//                        Global.getCombatEngine().addFloatingText(ship.getLocation(), up_standstill_bonus_removed + "...", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);

                        Global.getSoundPlayer().playSound("ps_up_activate", 1, 1f, ship.getLocation(), new Vector2f(0, 0));
                        up_standstill_bonus_removed = true;
                    }
                }
                customCombatData.put("ps_standstilltimer" + id, standstillTimer);
            }
            if(up_standstill_activated) {
                //////////////////////////
                //Finish standstill
                //Move to berserk state
                //////////////////////////
                ps_spawnjitter_timer += amount;
                if(ps_spawnjitter_timer > spawnJitterTimerFrom && ps_spawnjitter_timer < spawnJitterTimerTo) {
                    ship.setJitter(this, ps_misc.PROJECT_SOLACE_UP_ACTIVATION, 2f, 2, 7, 15f);
                } else {
                    if(ps_spawnjitter_timer > (spawnJitterTimerTo + spawnJitterTimerWait)) {
                        ps_spawnjitter_timer = 0;
                    }
                }
//                ship.setJitter(this, ps_misc.PROJECT_SOLACE_UP_ACTIVATION, 1f, 1, 7, 15f);
                ship.getEngineController().getFlameColorShifter().shift(this, ps_misc.PROJECT_SOLACE_UP_ACTIVATION, 0.2f, 1f, 1f);

                //damage boost
                float damageToCruiser = 0f;
                float damageToCapital = 0f;
                switch (ship.getHullSize()) {
                    case FRIGATE:
                        damageToCruiser = UP_BONUS_DAMAGE_FRIGATE;
                        damageToCapital = UP_BONUS_DAMAGE_FRIGATE;
                        break;
                    case DESTROYER:
                        damageToCruiser = UP_BONUS_DAMAGE_DESTROYER;
                        damageToCapital = UP_BONUS_DAMAGE_DESTROYER;
                        break;
                    case CRUISER:
                        damageToCruiser = UP_BONUS_DAMAGE_CRUISER;
                        damageToCapital = UP_BONUS_DAMAGE_CRUISER;
                        break;
                    case CAPITAL_SHIP:
                        damageToCruiser = UP_BONUS_DAMAGE_CAPITAL;
                        damageToCapital = UP_BONUS_DAMAGE_CAPITAL;
                        break;
                }
                Global.getCombatEngine().maintainStatusForPlayerShip("ps_up_shield_down", "", "Shield", "Disabled", true);
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
        customCombatData.put("up_standstill_bonus_removed" + id, up_standstill_bonus_removed);
        customCombatData.put("ps_spawnjitter_timer" + id, ps_spawnjitter_timer);
//        customCombatData.put("up_standstill_popup" + id, up_standstill_popup);
    }

    private float getIncenseCap(ShipAPI ship) {
        if(ship != null) {
            float incenseCap = (ship.getMutableStats().getFluxDissipation().modified * INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION);
            return incenseCap;
        }
        return 0f;
    }
    private float getIncenseRegen(ShipAPI ship) {
        if(ship != null) {
            float actualRegenerationBase = 0;
            float regen = 0f;
            float regenCap = 0f;
            switch (ship.getHullSize()) {
                case FRIGATE:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_FRIGATE;
                    regenCap = INCENSE_REGENERATION_CAP_FRIGATE;
                    break;
                case DESTROYER:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_DESTROYER;
                    regenCap = INCENSE_REGENERATION_CAP_DESTROYER;
                    break;
                case CRUISER:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_CRUISER;
                    regenCap = INCENSE_REGENERATION_CAP_CRUISER;
                    break;
                case CAPITAL_SHIP:
                    actualRegenerationBase = INCENSE_REGENERATION_BASE_CAPITAL;
                    regenCap = INCENSE_REGENERATION_CAP_CAPITAL;
                    break;
            }
            regen = actualRegenerationBase + (ship.getMutableStats().getFluxCapacity().modified * INCENSE_BONUS_FROM_CAPACITOR);
            if(regen >regenCap) {
                return regenCap;
            }
            return regen;
        }
        return 0f;
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

        //Incense
        LabelAPI label = tooltip.addPara("A dust-like matter called Incense found by accident when a small piece of Solace crystal slipped off one of our scientist's hands and shattered on the ground. However, unlike other crystal dust, the area where the crystal shatters seems to be spreading out to fill up certain areas and within those areas, time seems to be moving differently. Further inspection and testing after this incident shows that the matter has some extraordinary quirks", opad, h, "");

        //3 effects
        //label = tooltip.addPara("First is the tendency to spread out to a large area, if the area is damaged by a moving projectile, the dust will disburse out then slowly form back to fill it up", opad, h, "");
        //label = tooltip.addPara("The second unique feature is the ability to release a huge amount of energy when Incense’s covering surface has been damaged to a certain point, after the initial impact, the matter seems to be bonding the surface back to a certain stage, this feature alone is a breakthrough for the Solace ship composition.", opad, h, "");
        //label = tooltip.addPara("The final specialty is the time manipulation, it seems that the object that Incense covers, depending on the density, can move slower or faster in time, this is also another key factor to Solace ship lineup.", opad, h, "");

        //bonus
        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        //incense
        //Time dal
        label = tooltip.addPara("Incense can be recharged automatically. The higher the Incense level, the higher the ship's time dilation is, max out at %s time dilation at max Incense", opad, h,
                "" + Math.round(TIME_DAL_BONUS) + "%");
        label.setHighlight("" + Math.round(TIME_DAL_BONUS) + "%");
        label.setHighlightColors(good);

        label = tooltip.addPara("Incense capacitor is %s of the ship flux dissipation. Incense regeneration rate per second is %s for each ship class plus %s of the ship's flux capacitor. Regeneration caps out at %s for each ship class", opad, h,
                "" + Math.round(INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION * 100) + "%",
                Math.round(INCENSE_REGENERATION_BASE_FRIGATE) + "/" + Math.round(INCENSE_REGENERATION_BASE_DESTROYER) + "/" + Math.round(INCENSE_REGENERATION_BASE_CRUISER) + "/" + Math.round(INCENSE_REGENERATION_BASE_CAPITAL) + "",
                "" + Math.round(INCENSE_BONUS_FROM_CAPACITOR * 100) + "%",
                Math.round(INCENSE_REGENERATION_CAP_FRIGATE) + "/" + Math.round(INCENSE_REGENERATION_CAP_DESTROYER) + "/" + Math.round(INCENSE_REGENERATION_CAP_CRUISER) + "/" + Math.round(INCENSE_REGENERATION_CAP_CAPITAL) + ""
        );
        label.setHighlight("" + Math.round(INCENSE_LEVEL_PERCENTAGE_FROM_DISSIPATION * 100) + "%",
                Math.round(INCENSE_REGENERATION_BASE_FRIGATE) + "/" + Math.round(INCENSE_REGENERATION_BASE_DESTROYER) + "/" + Math.round(INCENSE_REGENERATION_BASE_CRUISER) + "/" + Math.round(INCENSE_REGENERATION_BASE_CAPITAL) + "",
                "" + Math.round(INCENSE_BONUS_FROM_CAPACITOR * 100) + "%",
                Math.round(INCENSE_REGENERATION_CAP_FRIGATE) + "/" + Math.round(INCENSE_REGENERATION_CAP_DESTROYER) + "/" + Math.round(INCENSE_REGENERATION_CAP_CRUISER) + "/" + Math.round(INCENSE_REGENERATION_CAP_CAPITAL) + ""
        );
        label.setHighlightColors(good, good, ps_misc.PROJECT_SOLACE_LIGHT, good);

        label = tooltip.addPara("+ Current Incense capacitor: %s.", opad, h,
                "" + Math.round(getIncenseCap(ship)));
        label.setHighlight("" + Math.round(getIncenseCap(ship)));
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT);

        label = tooltip.addPara("+ Current Incense regeneration: %s.", opad, h,
                "" + Math.round(getIncenseRegen(ship)) + "/s");
        label.setHighlight("" + Math.round(getIncenseRegen(ship)) + "/s");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT);

        //taking damage
        label = tooltip.addPara("Hits on shield reduce Incense by %s of original damage. Hits on hull or armor reduce Incense by %s of the original damage. If the calculated damage on Incense is smaller than 1, 1 will be applied.", opad, h,
                "" + Math.round(ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_SHIELD * 100) + "%", "" + Math.round(ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_HULL_ARMOR * 100) + "%");
        label.setHighlight("" + Math.round(ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_SHIELD * 100) + "%", "" + Math.round(ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_HULL_ARMOR * 100) + "%");
        label.setHighlightColors(bad, bad);

        //Unsolidified Procedure
        tooltip.addSectionHeading("Special", Alignment.MID, opad);

        label = tooltip.addPara("If the ship hull fall below %s, activate %s - \"let the killing begins\"", opad, h,
                "" + Math.round(UP_HITPOINTS_START * 100) + "%", "Unsolidified Procedure (UP)");
        label.setHighlight("" + Math.round(UP_HITPOINTS_START * 100) + "%", "Unsolidified Procedure (UP)");
        label.setHighlightColors(bad, ps_misc.PROJECT_SOLACE_LIGHT);

        label = tooltip.addPara("%s: %s, %s fire rate, reduce weapon flux by %s, repair armor to %s of the original amount instantly, deal %s more damage to cruiser and capital, refill all missile and EMP sparking around the ship hitting any missiles or fighters", opad, h,
                "UP",
                "Disable shield",
                "" + Math.round(UP_ROF_BONUS * 100) + "%",
                "" + Math.round(UP_WEAPON_FLUX_BONUS * 100) + "%",
                "" + Math.round(UP_ARMOR_REPAIR * 100) + "%",
                "" + Math.round(UP_BONUS_DAMAGE_FRIGATE * 100) + "%/" + Math.round(UP_BONUS_DAMAGE_DESTROYER * 100) + "%/" + Math.round(UP_BONUS_DAMAGE_CRUISER * 100) + "%/" + Math.round(UP_BONUS_DAMAGE_CAPITAL * 100) + "%"
        );
        label.setHighlight("UP", "Disable shield",
                "" + Math.round(UP_ROF_BONUS * 100) + "%",
                "" + Math.round(UP_WEAPON_FLUX_BONUS * 100) + "%",
                "" + Math.round(UP_ARMOR_REPAIR * 100) + "%",
                "" + Math.round(UP_BONUS_DAMAGE_FRIGATE * 100) + "%/" + Math.round(UP_BONUS_DAMAGE_DESTROYER * 100) + "%/" + Math.round(UP_BONUS_DAMAGE_CRUISER * 100) + "%/" + Math.round(UP_BONUS_DAMAGE_CAPITAL * 100) + "%"
        );
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, bad, good, good, good, good);

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

            if (incenseLevel > 0) {
                float projectileDamage = 0;
                if(shieldHit) {
                    projectileDamage = damage.getDamage() * ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_SHIELD;
                } else {
                    projectileDamage = damage.getDamage() * ENEMY_PROJECTILE_DAMAGE_PERCENTAGE_HULL_ARMOR;
                }
                if (projectileDamage < 1) projectileDamage = 1;
                if(incenseLevel - projectileDamage > 0) {
                    incenseLevel -= projectileDamage;
                } else {
                    incenseLevel = 0;
                }
            }

            MathUtils.clamp(incenseLevel, 0, incenseCap);
            customCombatData.put("ps_incenselevel" + id, incenseLevel);
            return null;
        }
    }
}
