package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.List;

public class ps_oncaeffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    private boolean runOnce = false;
    private static final float FLUX_LEVEL = 0.6f;
    private static final int AMMO_PLUS = 16;
    private IntervalUtil timer = new IntervalUtil(10f, 10f);
    private static final int FLUX_DECREASE = 15;
    Logger log = Global.getLogger(ps_oncaeffects.class);
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (engine.isPaused()) return;
        if (!runOnce) {
            if (!weapon.getSlot().isBuiltIn()) {
                ps_boundlesseffect.checkIfNeedAdding(ship.getVariant());
            }
            runOnce = true;
        }
        timer.advance(amount);
        if(timer.intervalElapsed()) {
            if(ship.isAlive() && !ship.isExpired() && ship.getFluxTracker().getFluxLevel() > FLUX_LEVEL) {
                if(weapon.getAmmo() < weapon.getMaxAmmo()) {
                    if(weapon.getAmmo() + AMMO_PLUS > weapon.getMaxAmmo()) {
                        weapon.setAmmo(weapon.getMaxAmmo());
                    } else {
                        weapon.setAmmo(weapon.getAmmo() + AMMO_PLUS);
                    }
                }
            }
        }
        if(weapon.isInBurst()) {
            weapon.getShip().setJitter(this, new Color(100,100,100,100), 1f, 5, 20f);
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if(!weapon.getShip().isAlive() || weapon.getShip().isExpired()) return;
        ShipAPI ship = weapon.getShip();
        if(ship.getFluxTracker().getHardFlux() > 0) {
            ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getHardFlux() - FLUX_DECREASE);
        } else {
            ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux() - FLUX_DECREASE);
        }
    }
}
