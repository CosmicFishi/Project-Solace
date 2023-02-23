package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

public class ps_hyperframestats extends BaseShipSystemScript {

    public static final float SPEED_BONUS = 100f;
    public static final float ACCELERATION_BONUS = 100f;
    public static final float AMMO_REGEN_BONUS = 2f;
    public static final float FLUX_DISSIPATION_BONUS = 150f;
    private final IntervalUtil spawnAfterImageInterval = new IntervalUtil(0.1f, 0.1f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            if(ship != null) {
                stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS);
                stats.getAcceleration().modifyPercent(id, ACCELERATION_BONUS);
                stats.getEnergyAmmoRegenMult().modifyMult(id, AMMO_REGEN_BONUS);
                stats.getBallisticAmmoRegenMult().modifyMult(id, AMMO_REGEN_BONUS);
                stats.getFluxDissipation().modifyPercent(id, FLUX_DISSIPATION_BONUS);
                //fx
                spawnAfterImageInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if(spawnAfterImageInterval.intervalElapsed()) {
                    ship.addAfterimage(
                            ps_misc.PROJECT_SOLACE_LIGHT,
                            0f,
                            0f,
                            ship.getVelocity().x * -1.1f,
                            ship.getVelocity().y * -1.1f,
                            1,
                            0f,
                            0f,
                            0.3f,
                            true,
                            true,
                            false);
                }
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getEnergyAmmoRegenMult().unmodify(id);
        stats.getBallisticAmmoRegenMult().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Increased engine power", false);
        }
        if (index == 1) {
            return new StatusData("Increased Ammo regeneration", false);
        }
        if (index == 2) {
            return new StatusData("Increased flux dissipation", false);
        }
        return null;
    }
}
