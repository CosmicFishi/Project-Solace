package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class ps_swiftrushstats extends BaseShipSystemScript {

    public static final float MAX_SPEED_BONUS = 1.5f;
    public static final float MAX_ACCELERATION_BONUS = 1.5f;
    public static final float MAX_FLUX_LEVEL = 0.5f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            float speed_bonus = (ship.getFluxLevel() / MAX_FLUX_LEVEL) * MAX_SPEED_BONUS;
            if(speed_bonus > MAX_SPEED_BONUS) {
                speed_bonus = MAX_SPEED_BONUS;
            }
            float acceleration_bonus = (ship.getFluxLevel() / MAX_FLUX_LEVEL) * MAX_ACCELERATION_BONUS;
            if(acceleration_bonus > MAX_ACCELERATION_BONUS) {
                acceleration_bonus = MAX_ACCELERATION_BONUS;
            }
            stats.getMaxSpeed().modifyPercent(id, speed_bonus);
            stats.getAcceleration().modifyPercent(id, acceleration_bonus);
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased engine power", false);
        }
        return null;
    }
}
