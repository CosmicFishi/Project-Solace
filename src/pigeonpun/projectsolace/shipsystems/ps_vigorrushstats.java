package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.MagicAnim;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

public class ps_vigorrushstats extends BaseShipSystemScript {
    //Increase ship's time dilation by 50%, fighter damage increase by 50%
    //jitter, EMP arc between mount
    private static final float MAX_TIME_DAL_BONUS = 50f;
    private static final float FIGHTER_DAMAGE_BONUS = 50f;
    private static final IntervalUtil EMP_SPARK_TIMER = new IntervalUtil(0.3f, 1.5f);
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        //FX jit
        float jitterLevel = effectLevel;
        float jitterRangeBonus = 0;
        float maxRangeBonus = 10f;
        if (state == State.IN) {
            jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
            if (jitterLevel > 1) {
                jitterLevel = 1f;
            }
            jitterRangeBonus = jitterLevel * maxRangeBonus;
        } else if (state == State.ACTIVE) {
            jitterLevel = 1f;
            jitterRangeBonus = maxRangeBonus;
        } else if (state == State.OUT) {
            jitterRangeBonus = jitterLevel * maxRangeBonus;
        }
        jitterLevel = (float) Math.sqrt(jitterLevel);

        ship.setJitter(this, Color.red, jitterLevel, 1, 0f, 7f + jitterRangeBonus);
        ship.setJitterUnder(this, Color.red, jitterLevel, 25, 0f, 7f + jitterRangeBonus);

        //stats
        float shipTime = MAX_TIME_DAL_BONUS * effectLevel;
        stats.getTimeMult().modifyPercent(id, shipTime);
        if(ship.getAllWings() != null) {
            for (FighterWingAPI fighterWing: ship.getAllWings()) {
                for(ShipAPI fighter: fighterWing.getWingMembers()) {
                    fighter.setJitterUnder(fighter, Color.red, jitterLevel, 15, 0f, 7f);
                    stats.getDamageToFrigates().modifyPercent(fighter.getId(), FIGHTER_DAMAGE_BONUS);
                    stats.getDamageToDestroyers().modifyPercent(fighter.getId(), FIGHTER_DAMAGE_BONUS);
                    stats.getDamageToCruisers().modifyPercent(fighter.getId(), FIGHTER_DAMAGE_BONUS);
                    stats.getDamageToCapital().modifyPercent(fighter.getId(), FIGHTER_DAMAGE_BONUS);
                }
            }
        }
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyPercent(id, shipTime);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
        ////EMP
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
                        Color.red,
                        new Color(255, 255,255, 255)
                );
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        stats.getTimeMult().unmodify(id);
        if(ship != null && ship.getAllWings() != null) {
            for (FighterWingAPI fighterWing: ship.getAllWings()) {
                for(ShipAPI fighter: fighterWing.getWingMembers()) {
                    stats.getDamageToFrigates().unmodify(fighter.getId());
                    stats.getDamageToDestroyers().unmodify(fighter.getId());
                    stats.getDamageToCruisers().unmodify(fighter.getId());
                    stats.getDamageToCapital().unmodify(fighter.getId());
                }
            }
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Time flow altered", false);
        }
        if (index == 1) {
            return new StatusData("Weapon range reduced", false);
        }
        return null;
    }
}
