package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class ps_bulwarkbooststats extends BaseShipSystemScript {
    //Increase ship's max speed by 60%, disable shield, armor damage reduce by 80%
    //purple jitter,
    private static final float MAX_SPEED_BONUS = 60f;
    private static final float ARMOR_DAMAGE_REDUCE_MULT = 0.2f;
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyPercent(id, MAX_SPEED_BONUS);
            stats.getArmorDamageTakenMult().modifyMult(id, ARMOR_DAMAGE_REDUCE_MULT);
        }
        if (ship.getShield() != null) {
            ship.getShield().toggleOff();
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

        ship.setJitter(this, new Color(211, 110, 255, 200) , jitterLevel, 1, 0f, 7f + jitterRangeBonus);
        ship.setJitterUnder(this, new Color(211, 110, 255, 255) , jitterLevel, 20, 0f, 7f + jitterRangeBonus);
        ship.getEngineController().extendFlame(this, 2f, 1f, 1f);

        if (stats.getEntity() instanceof ShipAPI) {
            if (state == State.IN) {
                if (effectLevel > 0.2f) {
                    ship.getEngineController().getExtendLengthFraction().advance(1f);
                    for (ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                        if (engine.isSystemActivated()) {
                            ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
                        }
                    }
                }
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        stats.getMaxSpeed().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Time flow altered", false);
        }
        return null;
    }
}
