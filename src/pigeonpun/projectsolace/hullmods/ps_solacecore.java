package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

public class ps_solacecore extends BaseHullMod {

    public float EMP_DAMAGE = 20f;
    public static float
            ENERGY_RANGE_FRIGATE = 100f,
            ENERGY_RANGE_DESTROYER = 150f,
            ENERGY_RANGE_CRUISER = 200f;
    public float ENERGY_FLUX_COST = 20f;
    public float BALLISTIC_ROF = 20f;
    //Hybrid slot bonus
    public float BONUS_BALLISTIC = 5f;
    public float BONUS_ENERGY = 5f;
    public float TOTAL_BONUS_ENERGY_CAPACITOR = 0f;
    public float TOTAL_BONUS_BALLISTIC_HULL = 0f;

    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        //The Solace Core.
        LabelAPI label = tooltip.addPara("%s", opad, h,
                "The Solace Core.");
        label.setHighlight("The Solace Core.");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE);

        //Decrease EMP damage taken by
        label = tooltip.addPara("Decrease %s by %s", opad, h,
                "EMP damage taken" ,"" + Math.round(EMP_DAMAGE) + "%");
        label.setHighlight("EMP damage taken","" + Math.round(EMP_DAMAGE) + "%");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, h);

        //Increase none-beam energy weapon's base range by
        label = tooltip.addPara("Increase %s by %s/%s/%s for frigate, destroyer and cruiser.", opad, h,
                "projectile energy weapon's base range" ,"" + Math.round(ENERGY_RANGE_FRIGATE) + "u",
                "" + Math.round(ENERGY_RANGE_DESTROYER) + "u", "" + Math.round(ENERGY_RANGE_CRUISER) + "u");
        label.setHighlight("projectile energy weapon's base range" ,"" + Math.round(ENERGY_RANGE_FRIGATE) + "u",
                "" + Math.round(ENERGY_RANGE_DESTROYER) + "u", "" + Math.round(ENERGY_RANGE_CRUISER) + "u");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, good, good, good);

        //Increase flux cost for none-beam energy weapons
        label = tooltip.addPara("Increase %s for %s by %s.", opad, h,
                "flux cost", "energy weapons" ,"" + Math.round(ENERGY_FLUX_COST) + "%");
        label.setHighlight("flux cost", "energy weapons" ,"" + Math.round(ENERGY_FLUX_COST) + "%");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, Misc.MOUNT_ENERGY, bad);

        //Decrease fire rate for basllistic weapon by %s.
        label = tooltip.addPara("Decrease %s for %s by %s", opad, h,
                "fire rate", "basllistic weapon" ,"" + Math.round(BALLISTIC_ROF) + "%");
        label.setHighlight("fire rate", "basllistic weapon" ,"" + Math.round(BALLISTIC_ROF) + "%");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, Misc.MOUNT_BALLISTIC, bad);

        //Hybrid
        tooltip.addSectionHeading("Hybrid slots: ", Alignment.MID, opad);

        //Increase shield efficiency by %s for each ballistic weapon on hybrid mount
        label = tooltip.addPara("Increase %s by %s for each %s on hybrid mount", opad, h,
                "ship hull" ,"" + Math.round(BONUS_BALLISTIC) + "%", "ballistic weapon");
        label.setHighlight("ship hull" ,"" + Math.round(BONUS_BALLISTIC) + "%", "ballistic weapon");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, good, Misc.MOUNT_BALLISTIC);

        //Increase flux capacitor by %s for each energy weapon on hybrid mount
        label = tooltip.addPara("Increase %s by %s for each %s on hybrid mount", opad, h,
                "flux capacitor" ,"" + Math.round(BONUS_ENERGY) + "%", "energy weapon");
        label.setHighlight("shield efficiency" ,"" + Math.round(BONUS_ENERGY) + "%", "energy weapon");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE, good, Misc.MOUNT_ENERGY);

        //bonus
        tooltip.addSectionHeading("Current Hybrid slots Bonuses: ", Alignment.MID, opad);

        label = tooltip.addPara("Ship hull: %s", opad, h,
                "" + Math.round(TOTAL_BONUS_BALLISTIC_HULL) + "%");
        label.setHighlight("" + Math.round(TOTAL_BONUS_BALLISTIC_HULL) + "%");
        label.setHighlightColors(h);

        label = tooltip.addPara("Flux Capacitor: %s", opad, h,
                "" + Math.round(TOTAL_BONUS_ENERGY_CAPACITOR) + "%");
        label.setHighlight("" + Math.round(TOTAL_BONUS_ENERGY_CAPACITOR) + "%");
        label.setHighlightColors(h);

        tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
        tooltip.addPara("Since the base range is increased, this modifier"
                + " - unlike most other flat modifiers in the game - "
                + "is affected by percentage modifiers from other hullmods and skills.", opad);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEmpDamageTakenMult().modifyPercent(id, EMP_DAMAGE);
        stats.getBallisticRoFMult().modifyPercent(id, -BALLISTIC_ROF);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_FLUX_COST);
        stats.getHullBonus().modifyPercent(id, TOTAL_BONUS_BALLISTIC_HULL);
        stats.getFluxCapacity().modifyPercent(id, TOTAL_BONUS_ENERGY_CAPACITOR);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats = ship.getMutableStats();

        if(ship.getHullSize() != HullSize.CAPITAL_SHIP) {
            ship.addListener(new SolaceCoreRangeMod());
        }
        TOTAL_BONUS_ENERGY_CAPACITOR = 0;
        TOTAL_BONUS_BALLISTIC_HULL = 0;
        for (WeaponAPI weapon: ship.getAllWeapons()) {
            if (weapon.isDecorative() ) continue;
            if(weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.HYBRID) {
                if(weapon.getType() == WeaponAPI.WeaponType.ENERGY) {
                    TOTAL_BONUS_ENERGY_CAPACITOR += BONUS_ENERGY;
                }
                if(weapon.getType() == WeaponAPI.WeaponType.BALLISTIC) {
                    TOTAL_BONUS_BALLISTIC_HULL += BONUS_BALLISTIC;
                }
            }
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
