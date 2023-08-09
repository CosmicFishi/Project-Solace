package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

import static pigeonpun.projectsolace.com.ps_boundlesseffect.calcBoundlessWeaponPoint;

public class ps_boundlessbarrier extends BaseHullMod {

    //Each ship class have a limit of how many weapon points it can handle
    //increase maintenance by 1% per point
    //If overcap, increase maintenance by 2% per point and reduce CR by 0.5% per point
    public static final String BOUNDLESSBARRIER_ID = "ps_boundlessbarrier";
    public static final float
            BOUNDLESS_LARGE_POINT = 4,
            BOUNDLESS_MEDIUM_POINT = 2,
            BOUNDLESS_SMALL_POINT = 1;
    public static final float
            BOUNDLESS_CAPITAL_SOFT_CAP = 12,
            BOUNDLESS_CRUISER_SOFT_CAP = 9,
            BOUNDLESS_DESTROYER_SOFT_CAP = 6,
            BOUNDLESS_FRIGATE_SOFT_CAP = 3;
    private static final float BOUNDLESS_MAIN_PERCENTAGE_PER_POINT = 1F; //percentage
    private static final float BOUNDLESS_MAIN_PERCENTAGE_PER_POINT_OVERCAP = 2f; //percentage
    private static final float BOUNDLESS_CR_PERCENTAGE_PER_POINT_OVERCAP = 0.5f; //percentage

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;

        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipVariantAPI variant = stats.getVariant();

        if(variant != null) {
            if(calcBoundlessWeaponPoint(variant) < 1) {
                variant.getHullMods().remove(BOUNDLESSBARRIER_ID);
                return;
            }
            stats.getSuppliesPerMonth().modifyPercent(id, getMaintenanceIncreasePercentage(variant));
            stats.getPeakCRDuration().modifyMult(id, ((100f - getCRReductionPercentage(variant)) / 100));
        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.MOUNT_UNIVERSAL;
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getHighlightColor();
        Color neutral = Misc.getDarkHighlightColor();
        int boundlessWeaponPoints = calcBoundlessWeaponPoint(ship.getVariant());
        float maintCost = getMaintenanceIncreasePercentage(ship.getVariant());
        float CRCost = getCRReductionPercentage(ship.getVariant());

        LabelAPI label = tooltip.addPara("With great power comes great responsibility. Discovery of these boundless entities have given scientists quite the bewilderment as the material it is made from is far more technology advanced than anything currently available in the Sector. Despite the limit of our understanding of such tech, scientists and engineers still managed to equip and operate said entities safely onto our current confined hull. But it comes with hefty prices.", opad, h, "");

        tooltip.addSectionHeading("Details", Alignment.MID, opad);
        label = tooltip.addPara("+ Boundless weapons max point: %s/%s/%s/%s.", opad, h,
                "" + Math.round(BOUNDLESS_FRIGATE_SOFT_CAP), Math.round(BOUNDLESS_DESTROYER_SOFT_CAP) + "", Math.round(BOUNDLESS_CRUISER_SOFT_CAP) + "", Math.round(BOUNDLESS_CAPITAL_SOFT_CAP) + "");
        label.setHighlight("" + Math.round(BOUNDLESS_FRIGATE_SOFT_CAP), Math.round(BOUNDLESS_DESTROYER_SOFT_CAP) + "", Math.round(BOUNDLESS_CRUISER_SOFT_CAP) + "", Math.round(BOUNDLESS_CAPITAL_SOFT_CAP) + "");
        label.setHighlightColors((ship.getHullSize() == ShipAPI.HullSize.FRIGATE) ? good : neutral,
                (ship.getHullSize() == ShipAPI.HullSize.DESTROYER) ? good : neutral,
                (ship.getHullSize() == ShipAPI.HullSize.CRUISER) ? good : neutral,
                (ship.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) ? good : neutral);

        label = tooltip.addPara("+ Current Boundless weapons point: %s.", opad, h,
                "" + Math.round(boundlessWeaponPoints));
        label.setHighlight("" + Math.round(boundlessWeaponPoints));
        label.setHighlightColors(boundlessWeaponPoints > getBoundlessWeaponCap(ship.getVariant())? bad: good);

        tooltip.addSectionHeading("Effects", Alignment.MID, opad);
        label = tooltip.addPara("Increase maintenance cost by %s per point", opad, h,"" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT) + "%");
        label.setHighlight("" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT) + "%");
        label.setHighlightColors(bad);

        label = tooltip.addPara("If Boundless weapon point overcap, increase maintenance cost by %s and reduce CR by %s per point", opad, h,"" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT_OVERCAP) + "%", "" + Math.round(BOUNDLESS_CR_PERCENTAGE_PER_POINT_OVERCAP) + "%");
        label.setHighlight("" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT_OVERCAP) + "%", "" + Math.round(BOUNDLESS_CR_PERCENTAGE_PER_POINT_OVERCAP) + "%");
        label.setHighlightColors(bad, bad);

        label = tooltip.addPara("+ Current maintenance increase: %s", opad, h,"" + Math.round(maintCost) + "%");
        label.setHighlight("" + Math.round(maintCost) + "%");
        label.setHighlightColors(good);

        label = tooltip.addPara("+ Current CR reduction: %s", opad, h,"" + Math.round(CRCost) + "%");
        label.setHighlight("" + Math.round(CRCost) + "%");
        label.setHighlightColors(good);

    }
    private float getMaintenanceIncreasePercentage(ShipVariantAPI variant) {
        if(variant != null) {
            float totalMaintCost = 0;
            float totalWeaponPoint = calcBoundlessWeaponPoint(variant);
            if(totalWeaponPoint < getBoundlessWeaponCap(variant)) {
                totalMaintCost = totalWeaponPoint * BOUNDLESS_MAIN_PERCENTAGE_PER_POINT;
            } else {
                totalMaintCost = totalWeaponPoint * BOUNDLESS_MAIN_PERCENTAGE_PER_POINT_OVERCAP;
            }
            return totalMaintCost;
        }
        return 0;
    }
    private float getCRReductionPercentage(ShipVariantAPI variant) {
        if(variant != null) {
            float totalCRReduction = 0;
            float totalWeaponPoint = calcBoundlessWeaponPoint(variant);
            if(totalWeaponPoint > getBoundlessWeaponCap(variant)) {
                totalCRReduction = totalWeaponPoint * BOUNDLESS_CR_PERCENTAGE_PER_POINT_OVERCAP;
            }
            return totalCRReduction;
        }
        return 0;
    }
    private float getBoundlessWeaponCap(ShipVariantAPI variant) {
        if(variant.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
            return BOUNDLESS_CAPITAL_SOFT_CAP;
        }
        if(variant.getHullSize() == ShipAPI.HullSize.CRUISER) {
            return BOUNDLESS_CRUISER_SOFT_CAP;
        }
        if(variant.getHullSize() == ShipAPI.HullSize.DESTROYER) {
            return BOUNDLESS_DESTROYER_SOFT_CAP;
        }
        if(variant.getHullSize() == ShipAPI.HullSize.FRIGATE) {
            return BOUNDLESS_FRIGATE_SOFT_CAP;
        }
        return 0;
    }
//    private int calcBoundlessWeaponPoint(ShipAPI ship) {
//        if(ship != null) {
//            int totalPoint = 0;
//            for(WeaponAPI w: ship.getAllWeapons()) {
//                if(ps_misc.ENMITY_SPECIAL_WEAPONS_LIST.contains(w.getId())) {
//                    if(w.getSize() == WeaponAPI.WeaponSize.SMALL) {
//                        totalPoint += BOUNDLESS_SMALL_POINT;
//                    } else if (w.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
//                        totalPoint += BOUNDLESS_MEDIUM_POINT;
//                    } else if (w.getSize() == WeaponAPI.WeaponSize.LARGE) {
//                        totalPoint += BOUNDLESS_LARGE_POINT;
//                    }
//                }
//            }
//            return totalPoint;
//        }
//        return 0;
//    }

}
