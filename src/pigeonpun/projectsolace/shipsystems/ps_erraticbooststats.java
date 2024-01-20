package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import pigeonpun.projectsolace.com.ps_misc;

public class ps_erraticbooststats extends BaseShipSystemScript {

    private final float FIGHTER_TIME_DAL_BONUS = 20f;
    private final float SHIP_TIME_DAL_BONUS = 10f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if(ship != null) {
            stats.getTimeMult().modifyPercent(id, SHIP_TIME_DAL_BONUS);
            ship.setJitterUnder(this, ps_misc.ENMITY_JITTER, effectLevel * 0.8f, 1, 2f, 8f);
            if(ship.getAllWings() != null) {
                for (FighterWingAPI fighterWing: ship.getAllWings()) {
                    for(ShipAPI fighter: fighterWing.getWingMembers()) {
                        fighter.setJitterUnder(fighter, ps_misc.ENMITY_JITTER, effectLevel * 0.8f, 2, 4f, 12f);
                        fighter.getMutableStats().getTimeMult().modifyPercent(id, FIGHTER_TIME_DAL_BONUS);
                    }
                }
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship != null) {
            stats.getTimeMult().unmodifyPercent(id);
            if(ship.getAllWings() != null) {
                for (FighterWingAPI fighterWing: ship.getAllWings()) {
                    for(ShipAPI fighter: fighterWing.getWingMembers()) {
                        fighter.getMutableStats().getTimeMult().unmodifyPercent(id);
                    }
                }
            }
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Overclocking weapon", false);
        }
        if (index == 1) {
            return new StatusData("Increased ship and fighter time dilation", false);
        }
        return null;
    }
}
