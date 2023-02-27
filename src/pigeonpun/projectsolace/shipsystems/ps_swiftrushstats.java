package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;

public class ps_swiftrushstats extends BaseShipSystemScript {

    public static  final float SPEED_BONUS = 400f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
        } else {
            if(ship != null) {
                stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
                stats.getAcceleration().modifyPercent(id, SPEED_BONUS * 10f);
                stats.getDeceleration().modifyPercent(id, SPEED_BONUS * 10f);
                stats.getTurnAcceleration().modifyFlat(id, 0);
                stats.getTurnAcceleration().modifyPercent(id, 0);
                //fx
                ship.getEngineController().fadeToOtherColor(this, ps_misc.PROJECT_SOLACE_LIGHT, null, effectLevel, 0.7f);
                ship.getEngineController().extendFlame(this, 2f * effectLevel, 0.2f, 0.2f);
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased engine power", false);
        }
        return null;
    }
}
