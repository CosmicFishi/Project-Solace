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
import com.fs.starfarer.api.util.Misc;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

public class ps_solacecore extends BaseHullMod {

    public float EMP_DAMAGE = 15f;
    public static float
            ENERGY_RANGE_FRIGATE = 100f,
            ENERGY_RANGE_DESTROYER = 150f,
            ENERGY_RANGE_CRUISER = 200f;
    public float ENERGY_FLUX_COST = 30f;
    public float BALLISTIC_ROF = 20f;
    //Hybrid slot bonus
    public float BONUS_BALLISTIC = 5f;
    public float BONUS_ENERGY = 5f;
    public float TOTAL_BONUS_ENERGY_CAPACITOR = 0f;
    public float TOTAL_BONUS_BALLISTIC_HULL = 0f;
    public float TIME_DAL_BONUS = 20f;
    public float MAX_FLUX_LEVEL_TIME_DAL_BONUS = 0.8f;
    public float SUPPLIES_REQUIRED = 25f;

    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }
        MutableShipStatsAPI stats = ship.getMutableStats();
        float speed_bonus_apply = ship.getFluxLevel() / MAX_FLUX_LEVEL_TIME_DAL_BONUS * TIME_DAL_BONUS;
        if(speed_bonus_apply > TIME_DAL_BONUS) {
            speed_bonus_apply = TIME_DAL_BONUS;
        }
        stats.getTimeMult().modifyPercent(ship.getId(), speed_bonus_apply);


        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("ps_solacecore_timedal", "graphics/icons/hullsys/temporal_shell.png", "Time Dilation", Math.round(speed_bonus_apply) + "%", false);
        }
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

        //bonus
        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        //Decrease EMP damage taken by
        label = tooltip.addPara("Decrease %s by %s", opad, h,
                "EMP damage taken" ,"" + Math.round(EMP_DAMAGE) + "%");
        label.setHighlight("EMP damage taken","" + Math.round(EMP_DAMAGE) + "%");
        label.setHighlightColors(h, good);

        //Increase none-beam energy weapon's base range by
        label = tooltip.addPara("Increase %s's %s by %s/%s/%s for frigate, destroyer and cruiser.", opad, h,
                "projectile energy weapon", "base range" ,"" + Math.round(ENERGY_RANGE_FRIGATE) + "u",
                "" + Math.round(ENERGY_RANGE_DESTROYER) + "u", "" + Math.round(ENERGY_RANGE_CRUISER) + "u");
        label.setHighlight("projectile energy weapon", "base range" ,"" + Math.round(ENERGY_RANGE_FRIGATE) + "u",
                "" + Math.round(ENERGY_RANGE_DESTROYER) + "u", "" + Math.round(ENERGY_RANGE_CRUISER) + "u");
        label.setHighlightColors(Misc.MOUNT_ENERGY, h, good, good, good);

        //Time dilation
        label = tooltip.addPara("Increase %s up to %s, proportion to the ship flux, effect max out at %s flux", opad, h,
                "time flow", "" + Math.round(TIME_DAL_BONUS) + "%" ,"" + Math.round(MAX_FLUX_LEVEL_TIME_DAL_BONUS* 100f) + "%");
        label.setHighlight("time flow", "" + Math.round(TIME_DAL_BONUS) + "%" ,"" + Math.round(MAX_FLUX_LEVEL_TIME_DAL_BONUS * 100f) + "%");
        label.setHighlightColors(h, good, h);

        //Increase flux cost for none-beam energy weapons
        label = tooltip.addPara("Increase %s for %s by %s.", opad, h,
                "flux cost", "energy weapons" ,"" + Math.round(ENERGY_FLUX_COST) + "%");
        label.setHighlight("flux cost", "energy weapons" ,"" + Math.round(ENERGY_FLUX_COST) + "%");
        label.setHighlightColors(h, Misc.MOUNT_ENERGY, bad);

        //Decrease fire rate for basllistic weapon by %s.d
        label = tooltip.addPara("Decrease %s for %s by %s", opad, h,
                "fire rate", "basllistic weapon" ,"" + Math.round(BALLISTIC_ROF) + "%");
        label.setHighlight("fire rate", "basllistic weapon" ,"" + Math.round(BALLISTIC_ROF) + "%");
        label.setHighlightColors(h, Misc.MOUNT_BALLISTIC, bad);

        //Maintain
        label = tooltip.addPara("Increase %s by %s", opad, h,
                "supplies per month", "" + Math.round(SUPPLIES_REQUIRED) + "%");
        label.setHighlight("supplies per month", "" + Math.round(SUPPLIES_REQUIRED) + "%");
        label.setHighlightColors(h, bad);

        //Hybrid
        tooltip.addSectionHeading("Hybrid slots", Alignment.MID, opad);

        //Increase shield efficiency by %s for each ballistic weapon on hybrid mount
        label = tooltip.addPara("Increase %s by %s for each %s on hybrid mount", opad, h,
                "ship hull" ,"" + Math.round(BONUS_BALLISTIC) + "%", "ballistic weapon");
        label.setHighlight("ship hull" ,"" + Math.round(BONUS_BALLISTIC) + "%", "ballistic weapon");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, good, Misc.MOUNT_BALLISTIC);

        //Increase flux capacitor by %s for each energy weapon on hybrid mount
        label = tooltip.addPara("Increase %s by %s for each %s on hybrid mount", opad, h,
                "flux capacitor" ,"" + Math.round(BONUS_ENERGY) + "%", "energy weapon");
        label.setHighlight("flux capacitor" ,"" + Math.round(BONUS_ENERGY) + "%", "energy weapon");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, good, Misc.MOUNT_ENERGY);

        //bonus
        tooltip.addSectionHeading("Current Hybrid slots Bonuses", Alignment.MID, opad);

        float total_bonus_capacitor = 0;
        float total_bonus_hull = 0;
        for (WeaponAPI weapon: ship.getAllWeapons()) {
            if (weapon.isDecorative() ) continue;
            if(weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.HYBRID) {
                if(weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
                    total_bonus_capacitor += BONUS_ENERGY;
                }
                if(weapon.getType() == WeaponAPI.WeaponType.BALLISTIC) {
                    total_bonus_hull += BONUS_BALLISTIC;
                }
            }
        }

        label = tooltip.addPara("Ship hull: %s", opad, h,
                "" + Math.round(total_bonus_hull) + "%");
        label.setHighlight("" + Math.round(total_bonus_hull) + "%");
        label.setHighlightColors(h);

        label = tooltip.addPara("Flux Capacitor: %s", opad, h,
                "" + Math.round(total_bonus_capacitor) + "%");
        label.setHighlight("" + Math.round(total_bonus_capacitor) + "%");
        label.setHighlightColors(h);

        tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
        tooltip.addPara("Since the base range is increased, this modifier"
                + "is affected by percentage modifiers from other hullmods and skills.", opad);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEmpDamageTakenMult().modifyPercent(id, EMP_DAMAGE);
        stats.getBallisticRoFMult().modifyPercent(id, -BALLISTIC_ROF);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_FLUX_COST);
        stats.getSuppliesPerMonth().modifyPercent(id, SUPPLIES_REQUIRED);

        TOTAL_BONUS_ENERGY_CAPACITOR = 0;
        TOTAL_BONUS_BALLISTIC_HULL = 0;
        for (String weaponSlot : stats.getVariant().getFittedWeaponSlots()) {
            WeaponSpecAPI weapon = stats.getVariant().getWeaponSpec(weaponSlot);
            if(stats.getVariant().getSlot(weaponSlot).getWeaponType() == WeaponAPI.WeaponType.HYBRID) {
                if(weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
                    TOTAL_BONUS_ENERGY_CAPACITOR += BONUS_ENERGY;
                }
                if(weapon.getType() == WeaponAPI.WeaponType.BALLISTIC) {
                    TOTAL_BONUS_BALLISTIC_HULL += BONUS_BALLISTIC;
                }
            }
        }
        stats.getHullBonus().modifyPercent(id, TOTAL_BONUS_BALLISTIC_HULL);
        stats.getFluxCapacity().modifyPercent(id, TOTAL_BONUS_ENERGY_CAPACITOR);

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats = ship.getMutableStats();

        if(ship.getHullSize() != HullSize.CAPITAL_SHIP) {
            ship.addListener(new SolaceCoreRangeMod());
        }
//        TOTAL_BONUS_ENERGY_CAPACITOR = 0;
//        TOTAL_BONUS_BALLISTIC_HULL = 0;
//        for (WeaponAPI weapon: ship.getAllWeapons()) {
//            if (weapon.isDecorative() ) continue;
//            if(weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.HYBRID) {
//                if(weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
//                    TOTAL_BONUS_ENERGY_CAPACITOR += BONUS_ENERGY;
//                }
//                if(weapon.getType() == WeaponAPI.WeaponType.BALLISTIC) {
//                    TOTAL_BONUS_BALLISTIC_HULL += BONUS_BALLISTIC;
//                }
//            }
//        }
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

            if (!weapon.isBeam() && weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
                float range = weapon.getSpec().getMaxRange();
                switch (ship.getHullSize()) {
                    case FRIGATE:
                        return ENERGY_RANGE_FRIGATE;
                    case DESTROYER:
                        return ENERGY_RANGE_DESTROYER;
                    case CRUISER:
                        return ENERGY_RANGE_CRUISER;
                    case DEFAULT:
                        return 0;
                }
            }
            return 0f;
        }
    }

}
