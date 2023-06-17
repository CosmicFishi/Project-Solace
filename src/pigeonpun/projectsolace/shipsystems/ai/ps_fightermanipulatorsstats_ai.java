package pigeonpun.projectsolace.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ps_fightermanipulatorsstats_ai implements ShipSystemAIScript {

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
        boolean hasEnemiesNearby = !AIUtils.getNearbyEnemies(ship, 2500f).isEmpty();
        boolean isFighterEngaging = flags.hasFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET);

        //facing the enemy
        if(hasEnemiesNearby && isFighterEngaging) useSystem = true;
        if(isFighterEngaging) useSystem = true;

        if(useSystem) {
            ship.useSystem();
        }

    }
}
