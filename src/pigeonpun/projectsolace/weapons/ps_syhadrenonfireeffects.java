package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_boundlesseffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ps_syhadrenonfireeffects implements OnFireEffectPlugin {
    private boolean runOnce = false;
    private MissileAPI missile;
    protected Vector2f mslPoint1 = new Vector2f(0, 2.5f); //offset compared to the center of the big missile sprite
    protected Vector2f mslPoint2 = new Vector2f(0, -3.5f); //offset compared to the center of the big missile sprite
    protected final List<Vector2f> listSpawnSplitMslPoints = new ArrayList<>(Arrays.asList(mslPoint1, mslPoint2));

    Logger log = Global.getLogger(ps_syhadrenonfireeffects.class);

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        MissileAPI missile = (MissileAPI) projectile;
        this.missile = missile;
        ps_syhadrenMissilePlugin missileVisualPlugin = new ps_syhadrenMissilePlugin(missile);
        engine.addPlugin(missileVisualPlugin);
    }
    protected class ps_syhadrenMissilePlugin extends BaseEveryFrameCombatPlugin {
        MissileAPI missile;
        private IntervalUtil timer = new IntervalUtil(1.5f,1.5f);
        ps_syhadrenMissilePlugin(MissileAPI missile) {
            this.missile = missile;
        }
        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (Global.getCombatEngine().isPaused()) return;
            if(Global.getCombatEngine().isEntityInPlay(this.missile) && !this.missile.isFading() && !this.missile.isFizzling() ) {
               timer.advance(amount);
               if(timer.intervalElapsed()) {
                   log.info("firing");
                   for (Vector2f offset: listSpawnSplitMslPoints) {
                       Vector2f spawnLocation = Vector2f.add(offset, this.missile.getLocation(), new Vector2f());
                       float angle = MathUtils.getRandomNumberInRange(0, 360);
                       log.info(spawnLocation);
                       Global.getCombatEngine().spawnProjectile(this.missile.getWeapon().getShip(), this.missile.getWeapon(), "ps_syhadren_split", spawnLocation, angle, null);
                   }
               }
            }
        }
    }
}
