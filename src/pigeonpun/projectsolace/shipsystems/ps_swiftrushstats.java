package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import data.scripts.util.MagicAnim;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

public class ps_swiftrushstats extends BaseShipSystemScript {

    public static  final float SPEED_BONUS = 300f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            if(ship != null) {
                stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
                stats.getAcceleration().modifyFlat(id, SPEED_BONUS * effectLevel);
                stats.getDeceleration().modifyFlat(id, SPEED_BONUS * effectLevel);
                //fx
                float fxEffect = MagicAnim.smoothNormalizeRange(effectLevel, 0, 1);
                ship.setJitterUnder(
                        ship,
                        Color.yellow,
                        0.5f*fxEffect,
                        5,
                        10f*fxEffect,
                        15f*fxEffect
                );
                ship.getEngineController().fadeToOtherColor(this, ps_misc.PROJECT_SOLACE_LIGHT, null, effectLevel, 0.7f);
                ship.getEngineController().extendFlame(this, 2f * fxEffect, 0.2f, 0.2f);
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased engine power", false);
        }
        return null;
    }
}
