package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

import static pigeonpun.projectsolace.com.ps_boundlesseffect.calcBoundlessWeaponPoint;

public class ps_boundlessbarrier extends BaseHullMod {

    //Each ship class have a point total
    //Effects will be applied with point total
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
    private static final float BOUNDLESS_MAIN_PERCENTAGE_PER_POINT = 2F; //percentage
    private static final float BOUNDLESS_CR_PERCENTAGE_PER_POINT = 0.5f; //percentage
    private static final float BOUNDLESS_DP_MULT_PER_POINT = 1f; //percentage
//    private static final float BOUNDLESS_MAIN_PERCENTAGE_PER_POINT_OVERCAP = 2f; //percentage
//    private static final float BOUNDLESS_CR_PERCENTAGE_PER_POINT_OVERCAP = 0.5f; //percentage
    private static final IntervalUtil EMP_SPARK_TIMER = new IntervalUtil(2.5f, 3.5f);
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }
        WeightedRandomPicker<Vector2f> randomPointPicker = new WeightedRandomPicker<>();
        //Important: it updates the ship location for the bounds
        ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
        for (BoundsAPI.SegmentAPI s: ship.getExactBounds().getSegments()) {
            //Global.getCombatEngine().addFloatingText( s.getP1() ,  ".", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);
            if(!randomPointPicker.getItems().contains(s.getP1())) {
                randomPointPicker.add(s.getP1());
            }
        }
        if(!randomPointPicker.isEmpty()) {
            EMP_SPARK_TIMER.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(EMP_SPARK_TIMER.intervalElapsed()) {
                Global.getCombatEngine().spawnEmpArcVisual(
                        randomPointPicker.pick(),
                        ship,
                        randomPointPicker.pick(),
                        ship,
                        MathUtils.getRandomNumberInRange(5f,10f),
                        new Color(141,53,121,255),
                        new Color(255, 255,255, 255)
                );
                Global.getCombatEngine().spawnEmpArcVisual(
                        randomPointPicker.pick(),
                        ship,
                        randomPointPicker.pick(),
                        ship,
                        MathUtils.getRandomNumberInRange(5f,10f),
                        new Color(141,53,121,255),
                        new Color(255, 255,255, 255)
                );
                Global.getCombatEngine().spawnEmpArcVisual(
                        randomPointPicker.pick(),
                        ship,
                        randomPointPicker.pick(),
                        ship,
                        MathUtils.getRandomNumberInRange(5f,10f),
                        new Color(141,53,121,255),
                        new Color(255, 255,255, 255)
                );
            }
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
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, getDPIncrease(variant));
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
        float DPCost = getDPIncrease(ship.getVariant());

        LabelAPI label = tooltip.addPara("With great power comes great responsibility. Discovery of these boundless entities have given scientists quite the bewilderment as the material it is made from is far more technology advanced than anything currently available in the Sector. Despite the limit of our understanding of such tech, scientists and engineers still managed to equip and operate said entities safely onto our current confined hull. But it comes with hefty prices.", opad, h, "");

        tooltip.addSectionHeading("Details", Alignment.MID, opad);

        label = tooltip.addPara("+ Boundless weapons point per size: %s/%s/%s.", opad, h,
                "" + Math.round(BOUNDLESS_SMALL_POINT), "" + Math.round(BOUNDLESS_MEDIUM_POINT), "" + Math.round(BOUNDLESS_LARGE_POINT));
        label.setHighlight("" + Math.round(BOUNDLESS_SMALL_POINT), "" + Math.round(BOUNDLESS_MEDIUM_POINT), "" + Math.round(BOUNDLESS_LARGE_POINT));
        label.setHighlightColors(good, good, good);

//        label = tooltip.addPara("+ Boundless weapons max point: %s/%s/%s/%s.", opad, h,
//                "" + Math.round(BOUNDLESS_FRIGATE_SOFT_CAP), Math.round(BOUNDLESS_DESTROYER_SOFT_CAP) + "", Math.round(BOUNDLESS_CRUISER_SOFT_CAP) + "", Math.round(BOUNDLESS_CAPITAL_SOFT_CAP) + "");
//        label.setHighlight("" + Math.round(BOUNDLESS_FRIGATE_SOFT_CAP), Math.round(BOUNDLESS_DESTROYER_SOFT_CAP) + "", Math.round(BOUNDLESS_CRUISER_SOFT_CAP) + "", Math.round(BOUNDLESS_CAPITAL_SOFT_CAP) + "");
//        label.setHighlightColors((ship.getHullSize() == ShipAPI.HullSize.FRIGATE) ? good : neutral,
//                (ship.getHullSize() == ShipAPI.HullSize.DESTROYER) ? good : neutral,
//                (ship.getHullSize() == ShipAPI.HullSize.CRUISER) ? good : neutral,
//                (ship.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) ? good : neutral);

        label = tooltip.addPara("+ Current Boundless weapons point: %s.", opad, h,
                "" + Math.round(boundlessWeaponPoints));
        label.setHighlight("" + Math.round(boundlessWeaponPoints));
        label.setHighlightColors(boundlessWeaponPoints > getBoundlessWeaponCap(ship.getVariant())? bad: good);

        tooltip.addSectionHeading("Effects", Alignment.MID, opad);
        label = tooltip.addPara("Increase maintenance cost by %s per point, current increase: %s", opad, h,"" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT) + "%", "" + Math.round(maintCost) + "%");
        label.setHighlight("" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT) + "%", "" + Math.round(maintCost) + "%");
        label.setHighlightColors(bad, bad);

//        label = tooltip.addPara("If Boundless weapon point overcap, increase maintenance cost by %s and reduce CR by %s per point", opad, h,"" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT_OVERCAP) + "%", "" + Math.round(BOUNDLESS_CR_PERCENTAGE_PER_POINT_OVERCAP) + "%");
//        label.setHighlight("" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT_OVERCAP) + "%", "" + Math.round(BOUNDLESS_CR_PERCENTAGE_PER_POINT_OVERCAP) + "%");
//        label.setHighlightColors(bad, bad);

        label = tooltip.addPara("Reduce CR by %s per point, current reduction: %s", opad, h,"" + Math.round(BOUNDLESS_CR_PERCENTAGE_PER_POINT) + "%", "" + Math.round(CRCost) + "%");
        label.setHighlight("" + Math.round(BOUNDLESS_CR_PERCENTAGE_PER_POINT) + "%", "" + Math.round(CRCost) + "%");
        label.setHighlightColors(bad, bad);

        label = tooltip.addPara("Increase DP by %s per point, current increase: %s", opad, h,"" + Math.round(BOUNDLESS_DP_MULT_PER_POINT) + "", "" + Math.round(DPCost));
        label.setHighlight("" + Math.round(BOUNDLESS_DP_MULT_PER_POINT), "" + Math.round(DPCost));
        label.setHighlightColors(bad, bad);

//        label = tooltip.addPara("+ Current maintenance increase: %s", opad, h,"" + Math.round(maintCost) + "%");
//        label.setHighlight("" + Math.round(maintCost) + "%");
//        label.setHighlightColors(good);
//
//        label = tooltip.addPara("+ Current CR reduction: %s", opad, h,"" + Math.round(CRCost) + "%");
//        label.setHighlight("" + Math.round(CRCost) + "%");
//        label.setHighlightColors(good);

    }
    private float getMaintenanceIncreasePercentage(ShipVariantAPI variant) {
        if(variant != null) {
            float totalMaintCost = 0;
            float totalWeaponPoint = calcBoundlessWeaponPoint(variant);
            totalMaintCost = totalWeaponPoint * BOUNDLESS_MAIN_PERCENTAGE_PER_POINT;
            return totalMaintCost;
        }
        return 0;
    }
    private float getCRReductionPercentage(ShipVariantAPI variant) {
        if(variant != null) {
            float totalCRReduction = 0;
            float totalWeaponPoint = calcBoundlessWeaponPoint(variant);
            totalCRReduction = totalWeaponPoint * BOUNDLESS_CR_PERCENTAGE_PER_POINT;
            return totalCRReduction;
        }
        return 0;
    }
    private float getDPIncrease(ShipVariantAPI variant) {
        if(variant != null) {
            float totalDPIncrease = 0;
            float totalWeaponPoint = calcBoundlessWeaponPoint(variant);
            totalDPIncrease = totalWeaponPoint * BOUNDLESS_DP_MULT_PER_POINT;
            return totalDPIncrease;
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
