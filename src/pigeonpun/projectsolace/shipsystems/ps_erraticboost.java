package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.Objects;

public class ps_erraticboost extends BaseShipSystemScript {

    private final float FIGHTER_ROF_BONUS = 20f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if(ship != null) {
            ship.setJitterUnder(this, ps_misc.ENMITY_JITTER, effectLevel * 0.8f, 2, 2f, 8f);
            if(ship.getAllWings() != null) {
                for (FighterWingAPI fighterWing: ship.getAllWings()) {
                    for(ShipAPI fighter: fighterWing.getWingMembers()) {
                        fighter.setJitterUnder(fighter, ps_misc.ENMITY_JITTER, effectLevel * 0.5f, 2, 4f, 12f);
                        stats.getEnergyRoFMult().modifyPercent(fighter.getId(), FIGHTER_ROF_BONUS);
                        stats.getBallisticRoFMult().modifyPercent(fighter.getId(), FIGHTER_ROF_BONUS);
                    }
                }
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship != null && ship.getAllWings() != null) {
            for (FighterWingAPI fighterWing: ship.getAllWings()) {
                for(ShipAPI fighter: fighterWing.getWingMembers()) {
                    stats.getEnergyRoFMult().unmodify(fighter.getId());
                    stats.getBallisticRoFMult().unmodify(fighter.getId());
                }
            }
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Overclocking weapon", false);
        }
        if (index == 1) {
            return new StatusData("Increased fighter damage", false);
        }
        return null;
    }
}
