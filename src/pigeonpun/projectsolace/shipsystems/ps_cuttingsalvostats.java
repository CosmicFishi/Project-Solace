package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.LazyLib;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class ps_cuttingsalvostats extends BaseShipSystemScript {
    private boolean fireOnce = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if(ship != null) {
            //active -> buff -> fire
            if(state.equals(State.ACTIVE)) {
                if(!fireOnce) {
                    for (WeaponAPI w : ship.getAllWeapons()) {
                        if (w.getSlot().isSystemSlot()) continue;
                        if (w.getType() == WeaponAPI.WeaponType.MISSILE) {

                            //w.setRefireDelay(0);
                            w.ensureClonedSpec();
                            if (w.getAmmo() < w.getMaxAmmo()) {
                                w.setAmmo(w.getAmmo() + 1);
                            }
                            w.setForceFireOneFrame(true);
                            fireOnce = true;
                        }
                    }
                }
            }
            //jitter
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
            effectLevel *= effectLevel;

            ship.setJitterUnder(this, new Color(52, 226, 79, 255), jitterLevel, 5, 0f, 7f + jitterRangeBonus);
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        fireOnce = false;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Overclocking weapons", false);
        }
        return null;
    }
}
