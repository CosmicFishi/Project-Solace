package pigeonpun.projectsolace.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class ps_cuttingsalvostats_ai implements ShipSystemAIScript {

    private final IntervalUtil
            timer = new IntervalUtil(0.75f, 1f);
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (!ship.isAlive() || engine.isPaused())
            return;
        timer.advance(amount);
        if (!timer.intervalElapsed()) return;

        boolean useSystem = false;
        boolean isCloseToTarget = ship.getShipTarget() != null && MathUtils.isWithinRange(ship.getShipTarget(), ship.getLocation(), 1200);

        //if target flux level high or venting
        if(isCloseToTarget && ship.getShipTarget() != null &&
                (ship.getShipTarget().getFluxLevel() > 0.9f ||
                ship.getShipTarget().getFluxTracker().isOverloadedOrVenting())
        ) {
            useSystem = true;
        }
        //if target is shield off
        if(isCloseToTarget && ship.getShipTarget().getShield() != null && ship.getShipTarget().getShield().isOff()) {
            useSystem = true;
        }
        //if attacking target && have missiles nearby
        if(isCloseToTarget && flags.hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) ||
                flags.hasFlag(ShipwideAIFlags.AIFlags.PURSUING)
        ) {
            useSystem = true;
        }
        if(useSystem) {
            ship.useSystem();
        }

    }
}
