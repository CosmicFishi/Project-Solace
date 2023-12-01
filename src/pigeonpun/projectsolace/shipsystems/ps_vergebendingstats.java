package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Objects;

public class ps_vergebendingstats extends BaseShipSystemScript {
//    private static final float WEAPON_DAMAGE_BONUS_MULT = 2f;
    private static final float FLUX_INCREASE_MULT = 1.2f;
    private static final String BUILT_IN_W_ID = "ps_roseae";
    private static final String BUILT_IN_SMOKE_ID = "ps_smoke_launcher";
    private boolean fireOnce = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if(ship != null) {
            //active -> buff -> fire
            if(state.equals(State.ACTIVE)) {
//                if(ship.getShield() != null) {
//                    ship.getShield().toggleOff();
//                }
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_INCREASE_MULT);
                stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_INCREASE_MULT);
//                stats.getEnergyWeaponDamageMult().modifyMult(id, WEAPON_DAMAGE_BONUS_MULT);
//                stats.getBallisticWeaponDamageMult().modifyMult(id, WEAPON_DAMAGE_BONUS_MULT);

                for (WeaponAPI w : ship.getAllWeapons()) {
                    if (w.getSlot().isSystemSlot()) continue;
                    if(state.equals(State.OUT)) {
                        if (w.getSlot().isDecorative() && w.getId().equals(BUILT_IN_SMOKE_ID)) {
                            w.setForceFireOneFrame(true);
                        }
                    }
                    if (state.equals(State.ACTIVE)) {
                        if(!Objects.equals(w.getId(), BUILT_IN_W_ID)) continue;
                        //refire built in
                        if(!fireOnce) {
                            //w.setRefireDelay(0);
                            w.ensureClonedSpec();
                            w.setRemainingCooldownTo(0);
                            fireOnce = true;
                        }
                        w.setForceFireOneFrame(true);
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
//            //unmodify when out of active state
//            if(state.equals(State.OUT)) {
//                stats.getEnergyWeaponFluxCostMod().unmodify(id);
//                stats.getEnergyWeaponDamageMult().unmodify(id);
//                stats.getBallisticWeaponDamageMult().unmodify(id);
//            }
//            if(state.equals(State.COOLDOWN) || state.equals(State.IDLE)) {
//                fireOnce = false;
//            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
//        stats.getEnergyWeaponDamageMult().unmodify(id);
//        stats.getBallisticWeaponDamageMult().unmodify(id);
        fireOnce = false;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Overclocking weapon", false);
        }
//        if (index == 1) {
//            return new StatusData("Increasing ballistic/energy damage by " + Math.round(WEAPON_DAMAGE_BONUS_MULT * 100) + "%", false);
//        }
        if (index == 1) {
            return new StatusData("Increasing ballistic/energy flux by " + Math.round(FLUX_INCREASE_MULT * 100) + "%", true);
        }
        return null;
    }
}
