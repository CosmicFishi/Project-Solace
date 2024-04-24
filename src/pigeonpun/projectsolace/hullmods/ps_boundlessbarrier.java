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
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pigeonpun.projectsolace.com.ps_boundlesseffect.calcTotalBoundlessWeaponPoint;
import static pigeonpun.projectsolace.com.ps_boundlesseffect.getBoundlessEffectData;

public class ps_boundlessbarrier extends BaseHullMod {

    //Each ship class have a point total
    //Effects will be applied with point total
    public static final String BOUNDLESSBARRIER_ID = "ps_boundlessbarrier";
    public static final float
            BOUNDLESS_LARGE_POINT = 4,
            BOUNDLESS_MEDIUM_POINT = 2,
            BOUNDLESS_SMALL_POINT = 1;
    private static final float BOUNDLESS_MAIN_PERCENTAGE_PER_POINT = 4F; //percentage
    private static final float BOUNDLESS_CR_PERCENTAGE_PER_POINT = 0.5f; //percentage
    private static final float BOUNDLESS_DP_MULT_PER_POINT = 1f;
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
            if(calcTotalBoundlessWeaponPoint(variant) < 1) {
                variant.getHullMods().remove(BOUNDLESSBARRIER_ID);
                return;
            }
            stats.getSuppliesPerMonth().modifyPercent(id, getMaintenanceIncreasePercentage(variant));
//            stats.getPeakCRDuration().modifyMult(id, ((100f - getCRReductionPercentage(variant)) / 100));
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, getDPIncrease(variant));
        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }
    public float getTooltipWidth() {
        return 500f;
    }
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.MOUNT_UNIVERSAL;
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getHighlightColor();
        Color neutral = Misc.getDarkHighlightColor();
        int totalWeaponPoints = calcTotalBoundlessWeaponPoint(ship.getVariant());
        float totalMaintCost = getMaintenanceIncreasePercentage(ship.getVariant());
//        float CRCost = getCRReductionPercentage(ship.getVariant());
        float totalDPCost = getDPIncrease(ship.getVariant());
        HashMap<String, ps_boundlesseffect.boundlessEffectData> listEffectData = getBoundlessEffectData(ship.getVariant());
        float totalWeapons = 0;
        float col1W = 220;
        float col2W = 65;
        float col3W = 50;
        float coi4W = 50;
        float col5W = 50;
        float lastW = 50;

        //. Discovery of these boundless entities have given scientists quite the bewilderment as the material it is made from is far more technology advanced than anything currently available in the Sector. Despite the limit of our understanding of such tech, scientists and engineers still managed to equip and operate said entities safely onto our current confined hull. But it comes with hefty prices.
        LabelAPI label = tooltip.addPara("With great power comes great responsibility", opad, h, "");

        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        label = tooltip.addPara("+ Boundless weapons point per size: %s / %s / %s.", opad, h,
                "" + Math.round(BOUNDLESS_SMALL_POINT), "" + Math.round(BOUNDLESS_MEDIUM_POINT), "" + Math.round(BOUNDLESS_LARGE_POINT));
        label.setHighlightColors(good, good, good);

        label = tooltip.addPara("- Increase maintenance cost by %s per point.", opad, h,"" + Math.round(BOUNDLESS_MAIN_PERCENTAGE_PER_POINT) + "%", "" + Math.round(totalMaintCost) + "%");
        label.setHighlightColors(bad, bad);

//        label = tooltip.addPara("- Reduce CR by %s per point", opad, h,"" + (BOUNDLESS_CR_PERCENTAGE_PER_POINT) + "%", "" + Math.round(CRCost) + "%");
//        label.setHighlightColors(bad, bad);

        label = tooltip.addPara("- Increase DP by %s per point.", opad, h,"" + Math.round(BOUNDLESS_DP_MULT_PER_POINT) + "", "" + Math.round(totalDPCost));
        label.setHighlightColors(bad, bad);

        tooltip.addSectionHeading("Details", Alignment.MID, opad);
        tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                20f, true, true,
                new Object [] {"Name", col1W, "Size", col2W, "Count", col3W, "Point", coi4W, "Maint", col5W, "DP", lastW});
        for(Map.Entry<String, ps_boundlesseffect.boundlessEffectData> data: listEffectData.entrySet()) {
            totalWeapons += data.getValue().getWeaponCount();
            tooltip.addRow(
                    Alignment.MID, h.darker(), data.getValue().weapon.getWeaponName(),
                    Alignment.MID, h.darker(), data.getValue().weapon.getSize().getDisplayName(),
                    Alignment.MID, h.darker(), data.getValue().getWeaponCount() + "",
                    Alignment.MID, h.darker(), data.getValue().getWeaponPoint() + "",
                    Alignment.MID, bad.darker(), Math.round(data.getValue().getWeaponPoint() * BOUNDLESS_MAIN_PERCENTAGE_PER_POINT) + "%",
                    Alignment.MID, bad.darker(), Math.round(data.getValue().getWeaponPoint() * BOUNDLESS_DP_MULT_PER_POINT) + ""
            );
        }
        tooltip.addRow(
                Alignment.MID, good, "Total",
                Alignment.MID, h, "",
                Alignment.MID, h, Math.round(totalWeapons) + "",
                Alignment.MID, h, Math.round(totalWeaponPoints) + "",
                Alignment.MID, bad, Math.round(totalMaintCost) + "%",
                Alignment.MID, bad, Math.round(totalDPCost) + ""
        );
        tooltip.addTable("", 0, opad);

    }
    private float getMaintenanceIncreasePercentage(ShipVariantAPI variant) {
        if(variant != null) {
            float totaltotalMaintCost = 0;
            float totalWeaponPoint = calcTotalBoundlessWeaponPoint(variant);
            totaltotalMaintCost = totalWeaponPoint * BOUNDLESS_MAIN_PERCENTAGE_PER_POINT;
            return totaltotalMaintCost;
        }
        return 0;
    }
//    private float getCRReductionPercentage(ShipVariantAPI variant) {
//        if(variant != null) {
//            float totalCRReduction = 0;
//            float totalWeaponPoint = calcTotalBoundlessWeaponPoint(variant);
//            totalCRReduction = totalWeaponPoint * BOUNDLESS_CR_PERCENTAGE_PER_POINT;
//            return totalCRReduction;
//        }
//        return 0;
//    }
    private float getDPIncrease(ShipVariantAPI variant) {
        if(variant != null) {
            float totalDPIncrease = 0;
            float totalWeaponPoint = calcTotalBoundlessWeaponPoint(variant);
            totalDPIncrease = totalWeaponPoint * BOUNDLESS_DP_MULT_PER_POINT;
            return totalDPIncrease;
        }
        return 0;
    }
}
