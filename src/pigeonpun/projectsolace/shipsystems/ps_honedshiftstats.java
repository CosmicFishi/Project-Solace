package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.Objects;

public class ps_honedshiftstats extends BaseShipSystemScript {
    private static final Color fromColor = new Color(175, 245, 253, 255);
    private static final Color toColor = new Color(251, 32, 32, 255);
    private static final float SHIP_TIME_DAL_BONUS_MULT = 2f;
    private static final float FLUX_REDUCTION_PERCENTAGE = 40f;
    private static final float ROF_BONUS_MULT = 4f;
    private static final String BUILT_IN_W_ID = "ps_muizon";
    private static IntervalUtil spanwAfterImgs = new IntervalUtil(0.5f, 0.5f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if(ship != null) {
            stats.getTimeMult().modifyMult(id, SHIP_TIME_DAL_BONUS_MULT);
            stats.getEnergyRoFMult().modifyMult(id, ROF_BONUS_MULT);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, (100f - FLUX_REDUCTION_PERCENTAGE) /100);
            spanwAfterImgs.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(spanwAfterImgs.intervalElapsed()) {
                ship.addAfterimage(
                        Misc.interpolateColor(fromColor, toColor, effectLevel), 0 , 0, -ship.getVelocity().x * 0.8f, -ship.getVelocity().y * 0.8f,
                        1,
                        0,
                        0f,
                        1.8f,
                        true,
                        false,
                        false
                );
            }

            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getSlot().isSystemSlot()) continue;
                if (state == State.ACTIVE) {
                    if (Objects.equals(w.getId(), BUILT_IN_W_ID)) {
                        w.setForceFireOneFrame(true);
                    } else {
                        w.setForceNoFireOneFrame(true);
                    }
                }
            }

            if (ship == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / SHIP_TIME_DAL_BONUS_MULT);
            } else {
                Global.getCombatEngine().getTimeMult().unmodify(id);
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship != null) {
            stats.getTimeMult().unmodify(id);
            stats.getEnergyRoFMult().unmodify(id);
            stats.getEnergyWeaponFluxCostMod().unmodify(id);
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Overclocking weapon", false);
        }
        if (index == 1) {
            return new StatusData("Reducing weapon flux cost by " + FLUX_REDUCTION_PERCENTAGE + "%", false);
        }
        if (index == 2) {
            return new StatusData("Increasing ROF by " + ROF_BONUS_MULT * 100 + "%", false);
        }
        if (index == 3) {
            return new StatusData("Increasing time dilation", false);
        }
        return null;
    }
}
