package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicIncompatibleHullmods;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

public class ps_solacecore extends BaseHullMod {

    public static float
            ENERGY_RANGE_FRIGATE_DESTROYER = 100f,
            ENERGY_RANGE_CRUISER_CAPITAL = 200f;
    public float ENERGY_FLUX_COST = 20f;
//    public float BALLISTIC_ROF = 40f;
    //Hybrid slot bonus
//    public float BONUS_BALLISTIC = 5f;
//    public float BONUS_ENERGY = 5f;
//    public float TOTAL_BONUS_ENERGY_CAPACITOR = 0f;
//    public float TOTAL_BONUS_BALLISTIC_HULL = 0f;
    public float TIME_DAL_BONUS = 10f;
    public float MAX_FLUX_LEVEL_TIME_DAL_BONUS = 0.8f;
//    public float SUPPLIES_REQUIRED = 25f;
    public static final float MAX_RANGE_WEAPON = 750f;
//    private final IntervalUtil spawnNebulaInterval = new IntervalUtil(0.8f, 0.8f);
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
//        super.advanceInCombat(ship, amount);
//        CombatEngineAPI engine = Global.getCombatEngine();
//        if (engine.isPaused()) {
//            return;
//        }
//        if (!ship.isAlive()) {
//            return;
//        }
//        MutableShipStatsAPI stats = ship.getMutableStats();
//        float time_bonus_apply = ship.getFluxLevel() / MAX_FLUX_LEVEL_TIME_DAL_BONUS * TIME_DAL_BONUS;
//        if(time_bonus_apply > TIME_DAL_BONUS) {
//            time_bonus_apply = TIME_DAL_BONUS;
//        }
//        stats.getTimeMult().modifyPercent(ship.getId(), time_bonus_apply);
//
//        //FX
//        float spawnRadius = ship.getCollisionRadius() * 1.2f;
//        float spawnAngle = (float) (Math.random() * 360f);
//        Vector2f spawnLocation = MathUtils.getPointOnCircumference(ship.getLocation(), spawnRadius, spawnAngle);
//        if(ship.getFluxLevel() > 0.5f) {
//            spawnNebulaInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
//            if(spawnNebulaInterval.intervalElapsed()) {
//                engine.addSwirlyNebulaParticle(
//                        spawnLocation,
//                        new Vector2f(0, 0),
//                        ship.getFluxLevel() * 70f,
//                        1f,0.1f,0.2f,
//                        0.9f,
//                        ps_misc.PROJECT_SOLACE_LIGHT,
//                        true
//                );
//                engine.addNebulaParticle(
//                        spawnLocation,
//                        new Vector2f(0, 0),
//                        ship.getFluxLevel() * 30f,
//                        1f,0.1f,0.2f,
//                        0.7f,
//                        ps_misc.PROJECT_SOLACE,
//                        true
//                );
//            }
//        }
//        float jitterLevel = ship.getFluxLevel();
//        float jitterRangeBonus = 0;
//        float maxRangeBonus = 10f * ship.getFluxLevel();
//
//        ship.setJitter(this, ps_misc.PROJECT_SOLACE_JITTER, jitterLevel, 3, 0, 0 + jitterRangeBonus);
//        ship.setJitterUnder(this, ps_misc.PROJECT_SOLACE_JITTER_UNDER, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
//
//
//        if (ship == Global.getCombatEngine().getPlayerShip()) {
//            Global.getCombatEngine().maintainStatusForPlayerShip("ps_solacecore_timedal", "graphics/icons/hullsys/temporal_shell.png", "Time Dilation", Math.round(time_bonus_apply) + "%", false);
//        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.MOUNT_UNIVERSAL;
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        //The Solace Core.
        LabelAPI label = tooltip.addPara("The %s core, a crystal-like object which draw flux from the ship to further improve it's performance.", opad, h,
                "Solace");
        label.setHighlight("Solace");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE);
        label = tooltip.addPara("However, due to the enormous amount of damage which occurred from an incident involving an experiment with the core, the Solace Association deemed that any act or movement to modify or to tamper with the core will be considered unlawful and therefore, prohibited.", opad);

        //bonus
        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        //Increase none-beam energy weapon's base range by
        label = tooltip.addPara("Increase %s's %s by %s/%s for frigate and destroyer/cruiser and capital.", opad, h,
                "projectile energy weapon", "base range" ,"" + Math.round(ENERGY_RANGE_FRIGATE_DESTROYER) + "u",
                "" + Math.round(ENERGY_RANGE_CRUISER_CAPITAL) + "u");
        label.setHighlight("projectile energy weapon", "base range" ,"" + Math.round(ENERGY_RANGE_FRIGATE_DESTROYER) + "u",
                "" + Math.round(ENERGY_RANGE_CRUISER_CAPITAL) + "u");
        label.setHighlightColors(Misc.MOUNT_ENERGY, ps_misc.PROJECT_SOLACE_LIGHT, good, good);

        //Time dilation
        label = tooltip.addPara("Increase %s up to %s, proportion to the ship flux, effect max out at %s flux", opad, h,
                "time flow", "" + Math.round(TIME_DAL_BONUS) + "%" ,"" + Math.round(MAX_FLUX_LEVEL_TIME_DAL_BONUS* 100f) + "%");
        label.setHighlight("time flow", "" + Math.round(TIME_DAL_BONUS) + "%" ,"" + Math.round(MAX_FLUX_LEVEL_TIME_DAL_BONUS * 100f) + "%");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, good, ps_misc.PROJECT_SOLACE_LIGHT);

        //Increase flux cost for none-beam energy weapons
        label = tooltip.addPara("Increase %s for %s by %s.", opad, h,
                "flux cost", "energy weapons" ,"" + Math.round(ENERGY_FLUX_COST) + "%");
        label.setHighlight("flux cost", "energy weapons" ,"" + Math.round(ENERGY_FLUX_COST) + "%");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, Misc.MOUNT_ENERGY, bad);

        //Decrease fire rate for basllistic weapon by %s
//        label = tooltip.addPara("Decrease %s for %s by %s", opad, h,
//                "fire rate", "basllistic weapon" ,"" + Math.round(BALLISTIC_ROF) + "%");
//        label.setHighlight("fire rate", "basllistic weapon" ,"" + Math.round(BALLISTIC_ROF) + "%");
//        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, Misc.MOUNT_BALLISTIC, bad);

        //Maintain
//        label = tooltip.addPara("Increase %s by %s", opad, h,
//                "supplies per month", "" + Math.round(SUPPLIES_REQUIRED) + "%");
//        label.setHighlight("supplies per month", "" + Math.round(SUPPLIES_REQUIRED) + "%");
//        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, bad);

        //Hybrid
//        tooltip.addSectionHeading("Hybrid slots", Alignment.MID, opad);

        //Increase shield efficiency by %s for each ballistic weapon on hybrid mount
//        label = tooltip.addPara("Increase %s by %s for each %s on hybrid mount", opad, h,
//                "ship hull" ,"" + Math.round(BONUS_BALLISTIC) + "%", "ballistic weapon");
//        label.setHighlight("ship hull" ,"" + Math.round(BONUS_BALLISTIC) + "%", "ballistic weapon");
//        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, good, Misc.MOUNT_BALLISTIC);
//
//        //Increase flux capacitor by %s for each energy weapon on hybrid mount
//        label = tooltip.addPara("Increase %s by %s for each %s on hybrid mount", opad, h,
//                "flux capacitor" ,"" + Math.round(BONUS_ENERGY) + "%", "energy weapon");
//        label.setHighlight("flux capacitor" ,"" + Math.round(BONUS_ENERGY) + "%", "energy weapon");
//        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, good, Misc.MOUNT_ENERGY);

        //bonus
//        tooltip.addSectionHeading("Current Hybrid slots Bonuses", Alignment.MID, opad);
//
//        float total_bonus_capacitor = 0;
//        float total_bonus_hull = 0;
//        for (WeaponAPI weapon: ship.getAllWeapons()) {
//            if (weapon.isDecorative() ) continue;
//            if(weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.HYBRID) {
//                if(weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
//                    total_bonus_capacitor += BONUS_ENERGY;
//                }
//                if(weapon.getType() == WeaponAPI.WeaponType.BALLISTIC) {
//                    total_bonus_hull += BONUS_BALLISTIC;
//                }
//            }
//        }

//        label = tooltip.addPara("Ship hull: %s", opad, h,
//                "" + Math.round(total_bonus_hull) + "%");
//        label.setHighlight("" + Math.round(total_bonus_hull) + "%");
//        label.setHighlightColors(h);
//
//        label = tooltip.addPara("Flux Capacitor: %s", opad, h,
//                "" + Math.round(total_bonus_capacitor) + "%");
//        label.setHighlight("" + Math.round(total_bonus_capacitor) + "%");
//        label.setHighlightColors(h);
//
        tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
        tooltip.addPara("Since the base range is increased, this modifier"
                + "is affected by percentage modifiers from other hullmods and skills.", opad);

        //incompatible
        tooltip.addSectionHeading("Incompatible hullmods", Alignment.MID, opad);
        label = tooltip.addPara("- %s", opad, h,
                "Safety Overrides");
        label.setHighlight("Safety Overrides");
        label.setHighlightColors(bad);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        stats.getBallisticRoFMult().modifyPercent(id, -BALLISTIC_ROF);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_FLUX_COST);
//        stats.getSuppliesPerMonth().modifyPercent(id, SUPPLIES_REQUIRED);

//        TOTAL_BONUS_ENERGY_CAPACITOR = 0;
//        TOTAL_BONUS_BALLISTIC_HULL = 0;
//        for (String weaponSlot : stats.getVariant().getFittedWeaponSlots()) {
//            WeaponSpecAPI weapon = stats.getVariant().getWeaponSpec(weaponSlot);
//            if(stats.getVariant().getSlot(weaponSlot).getWeaponType() == WeaponAPI.WeaponType.HYBRID) {
//                if(weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
//                    TOTAL_BONUS_ENERGY_CAPACITOR += BONUS_ENERGY;
//                }
//                if(weapon.getType() == WeaponAPI.WeaponType.BALLISTIC) {
//                    TOTAL_BONUS_BALLISTIC_HULL += BONUS_BALLISTIC;
//                }
//            }
//        }
//        stats.getHullBonus().modifyPercent(id, TOTAL_BONUS_BALLISTIC_HULL);
//        stats.getFluxCapacity().modifyPercent(id, TOTAL_BONUS_ENERGY_CAPACITOR);

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        ship.addListener(new SolaceCoreRangeMod());
        if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(),"safetyoverrides","ps_solacecore");
        }
    }

    public static class SolaceCoreRangeMod implements WeaponBaseRangeModifier {
        public SolaceCoreRangeMod() {
        }
        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0;
        }
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if(weapon.getSpec().getMaxRange() < MAX_RANGE_WEAPON) return 0f;
            if (!weapon.isBeam() && weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
                switch (ship.getHullSize()) {
                    case DESTROYER:
                    case FRIGATE:
                        return ENERGY_RANGE_FRIGATE_DESTROYER;
                    case CRUISER:
                    case CAPITAL_SHIP:
                        return ENERGY_RANGE_CRUISER_CAPITAL;
                    case DEFAULT:
                        return 0;
                }
            }
            return 0f;
        }
    }

}
