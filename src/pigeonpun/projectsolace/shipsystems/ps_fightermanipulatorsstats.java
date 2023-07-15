package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ps_fightermanipulatorsstats extends BaseShipSystemScript {
    private static final float AOE = 2000f;
    private static final int NUM_OF_MARK_POINTS = 3;
    private static float SPIN_OFFSET = 0f;
    private static final float SPIN_STEP = 0.02f;
    private final IntervalUtil SPAWN_PARTICLES_INTERVAL = new IntervalUtil(0.03f, 0.03f);
    private final IntervalUtil spawnAfterImageInterval = new IntervalUtil(0.1f, 0.1f);
    private final float MAX_SPEED_BONUS = 30f;
    private final float ROF_BONUS = 1.2f;
    private ArrayList<ShipAPI> buffedShip = new ArrayList<>();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        //fx
        SPAWN_PARTICLES_INTERVAL.advance(Global.getCombatEngine().getElapsedInLastFrame());
        spawnAfterImageInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        for (ShipAPI friendly: CombatUtils.getShipsWithinRange(ship.getLocation(), AOE)) {
            if(friendly.isFighter() && friendly.isAlive() && friendly.getOwner() == 0) {
                if(!buffedShip.contains(friendly)) {
                    buffedShip.add(friendly);
                    friendly.getMutableStats().getMaxSpeed().modifyPercent(id, MAX_SPEED_BONUS);
                    friendly.getMutableStats().getBallisticRoFMult().modifyPercent(id, ROF_BONUS);
                    friendly.getMutableStats().getEnergyRoFMult().modifyPercent(id, ROF_BONUS);
                    friendly.getMutableStats().getMissileRoFMult().modifyPercent(id, ROF_BONUS);
                }
                if(spawnAfterImageInterval.intervalElapsed()) {
                    //Global.getCombatEngine().addFloatingText(friendly.getLocation(), "Charging...", 60, ps_misc.PROJECT_SOLACE_LIGHT, friendly, 0.25f, 0.25f);
                    friendly.addAfterimage(
                            ps_misc.ENMITY_MAIN_AFTER_IMG,
                            0f,
                            0f,
                            friendly.getVelocity().x * -1.1f,
                            friendly.getVelocity().y * -1.1f,
                            1,
                            0f,
                            0f,
                            0.4f,
                            true,
                            true,
                            false);
                }
            }
        }

        if(SPAWN_PARTICLES_INTERVAL.intervalElapsed()) {
            markFighters(ship, engine);
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        for (ShipAPI ship: buffedShip) {
            ship.getMutableStats().getMaxSpeed().unmodify(id);
            ship.getMutableStats().getBallisticRoFMult().unmodify(id);
            ship.getMutableStats().getEnergyRoFMult().unmodify(id);
            ship.getMutableStats().getMissileRoFMult().unmodify(id);
        }
        buffedShip.clear();
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Increasing fighters speed", false);
        }
        return null;
    }

    private void markFighters(ShipAPI ship, CombatEngineAPI engine) {
        int i = 0;
        while(i < NUM_OF_MARK_POINTS) {
            i++;
            SPIN_OFFSET += SPIN_STEP;
            Float angle = (float) ((360 / NUM_OF_MARK_POINTS * i) + SPIN_OFFSET);
            Vector2f spawnNebulaPoint = MathUtils.getPointOnCircumference(ship.getLocation(), AOE, angle);
            engine.addSmoothParticle(
                    spawnNebulaPoint,
                    new Vector2f(0, 0),
                    25f,
                    1f,
                    0.7f,
                    ps_misc.ENMITY_MAIN
            );
        }
    }
}
