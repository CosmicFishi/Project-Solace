package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.input.InputEventAPI;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.List;

public class ps_hemiophryseffects implements BeamEffectPlugin, EveryFrameWeaponEffectPlugin {
    private boolean runOnce = false;
    private Logger log = Global.getLogger(ps_hemiophryseffects.class);
    public static final String WEAPON_ID = "ps_hemiophrys";
    public static final float REGEN_TO_DAMAGE_MULT = 0.01f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused()) return;

        CombatEntityAPI target = beam.getDamageTarget();

        if (target instanceof ShipAPI && !((ShipAPI) target).isStation() && beam.getBrightness() >= 1f) {
            ShipAPI ship = (ShipAPI) target;
            Vector2f randomPointOnLine = MathUtils.getRandomPointOnLine(beam.getRayEndPrevFrame(), beam.getWeapon().getLocation());

            Vector2f spawnHitParticlePoint = MathUtils.getPointOnCircumference(
                    randomPointOnLine,
                    MathUtils.getRandomNumberInRange(10f, 50f),
                    MathUtils.getRandomNumberInRange(0, 360)
            );
            engine.addSmoothParticle(spawnHitParticlePoint, (Vector2f) VectorUtils.getDirectionalVector(spawnHitParticlePoint, randomPointOnLine).scale(15f), 10f, 1f, 1f, new Color(255,0,87,255));
        }
    }
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!runOnce){
            runOnce=true;
            ShipAPI ship = weapon.getShip();
            if(!weapon.getSlot().isBuiltIn()){
                ps_boundlesseffect.checkIfNeedAdding(weapon.getShip().getVariant());
            }
            if(ship != null && !ship.hasListenerOfClass(ps_hemiophyrysDamageDealListener.class)) {
                ship.addListener(new ps_hemiophyrysDamageDealListener(ship));
            }
        }
    }
    public class ps_hemiophyrysDamageDealListener implements DamageDealtModifier, AdvanceableListener {
        private ShipAPI ship;
        private float totalRecoverAmount = 0;
        ps_hemiophyrysDamageDealListener(ShipAPI ship) {
            this.ship = ship;
        }
        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {

            if (param instanceof BeamAPI) {
                BeamAPI beam = (BeamAPI) param;
                if (beam.getWeapon().getId().equals(WEAPON_ID)) {
                    float regenReceived = Math.round(beam.getDamage().getDamage() * REGEN_TO_DAMAGE_MULT);
                    if(ship.getHitpoints() < ship.getMaxHitpoints()) {
                        totalRecoverAmount += regenReceived;
//                        log.info("Adding up recover" + totalRecoverAmount);
                    }
                }
            }
            return null;
        }

        @Override
        public void advance(float amount) {
            if(ship.isAlive() && totalRecoverAmount > 0) {
                if(ship.getHitpoints() < ship.getMaxHitpoints()) {
                    float recoverAmount =  totalRecoverAmount * amount;
                    ship.setHitpoints(ship.getHitpoints() + recoverAmount);
//                    log.info("recovering " + recoverAmount);
                    //clamp
                    if(ship.getHitpoints() > ship.getMaxHitpoints()) {
                        ship.setHitpoints(ship.getMaxHitpoints());
                    }
                    //remove the recovered amount from total
                    totalRecoverAmount -= recoverAmount;
                    if(totalRecoverAmount < 0) {
                        totalRecoverAmount = 0;
                    }
                }
            }
        }
    }
}
