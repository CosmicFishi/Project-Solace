package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import pigeonpun.projectsolace.com.ps_misc;

public class ps_intenseflowstats extends BaseShipSystemScript {

    public static  final float SPEED_BONUS = 50f;
    public static  final float ACCELERATION_BONUS = 50f;
    public static final float MAX_SPEED_BONUS = 70f;
    public static final float MAX_ACCELERATION_BONUS = 70f;
    public static final float MAX_FLUX_LEVEL = 70f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
        } else {
            if(ship != null) {
                float speed_bonus = (ship.getFluxLevel() / MAX_FLUX_LEVEL) * MAX_SPEED_BONUS + SPEED_BONUS;
                if(speed_bonus > MAX_SPEED_BONUS + SPEED_BONUS) {
                    speed_bonus = MAX_SPEED_BONUS + SPEED_BONUS;
                }
                float acceleration_bonus = (ship.getFluxLevel() / MAX_FLUX_LEVEL) * MAX_ACCELERATION_BONUS + ACCELERATION_BONUS;
                if(acceleration_bonus > MAX_ACCELERATION_BONUS + ACCELERATION_BONUS) {
                    acceleration_bonus = MAX_ACCELERATION_BONUS + ACCELERATION_BONUS;
                }
                stats.getMaxSpeed().modifyPercent(id, speed_bonus);
                stats.getAcceleration().modifyPercent(id, acceleration_bonus);
                //fx
                ship.getEngineController().fadeToOtherColor(this, ps_misc.PROJECT_SOLACE_LIGHT, null, 1f, 1f);
                ship.getEngineController().extendFlame(this, 0.2f, 0.2f, 0.2f);

            }
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
