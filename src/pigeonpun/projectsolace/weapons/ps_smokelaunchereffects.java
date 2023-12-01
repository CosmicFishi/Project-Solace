package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class ps_smokelaunchereffects implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if(ship != null && (ship.getSystem().isChargedown() || ship.getSystem().isCoolingDown())) {
            Vector2f smokeVel = MathUtils.getRandomPointInCone(new Vector2f(), 140f, weapon.getCurrAngle() - 20f, weapon.getCurrAngle() + 20f);
            engine.addSmokeParticle(
                    weapon.getLocation(),
                    smokeVel,
                    MathUtils.getRandomNumberInRange(2, 10),
                    MathUtils.getRandomNumberInRange(0, 1),
                    MathUtils.getRandomNumberInRange(0.4f, 1.2f),
                    new Color(153, 140, 149, MathUtils.getRandomNumberInRange(150, 255))
            );
            engine.addSmokeParticle(
                    weapon.getLocation(),
                    smokeVel,
                    MathUtils.getRandomNumberInRange(2, 10),
                    MathUtils.getRandomNumberInRange(0, 1),
                    MathUtils.getRandomNumberInRange(0.4f, 1.2f),
                    new Color(93, 86, 90, MathUtils.getRandomNumberInRange(150, 255))
            );
            engine.addSmokeParticle(
                    weapon.getLocation(),
                    smokeVel,
                    MathUtils.getRandomNumberInRange(2, 10),
                    MathUtils.getRandomNumberInRange(0, 1),
                    MathUtils.getRandomNumberInRange(0.4f, 1.2f),
                    new Color(63, 56, 60, MathUtils.getRandomNumberInRange(150, 255))
            );
        }
    }
}
