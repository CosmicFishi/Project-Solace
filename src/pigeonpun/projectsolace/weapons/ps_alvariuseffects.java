package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.combat.*;
import pigeonpun.projectsolace.com.ps_boundlesseffect;

public class ps_alvariuseffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    private boolean runOnce = false;
    public static final float FLARE_SPAWN_COUNTER = 50f; //shots take to spawn flare
    public float shotCounter = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (engine.isPaused()) return;
        if(!runOnce){
            if(!weapon.getSlot().isBuiltIn()){
                ps_boundlesseffect.checkIfNeedAdding(ship.getVariant());
            }
            runOnce=true;
        }
        if(this.shotCounter > FLARE_SPAWN_COUNTER) {
            this.shotCounter =0;
            //do the funny
            engine.spawnProjectile(weapon.getShip(), weapon, "flarelauncher3", weapon.getLocation(), weapon.getCurrAngle(), weapon.getShip().getVelocity());
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        this.shotCounter += 1;
    }
}
