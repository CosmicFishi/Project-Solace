package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_vsguidedprojectilescript;

import java.awt.*;
import java.util.Objects;

public class ps_roseaeeffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin, OnHitEffectPlugin {
    public static final Logger log = Global.getLogger(ps_roseaeeffects.class);
    private static final String ROSEAE_ID = "ps_roseae";
    private static final float EMP_COUNT = 2f;
    private static final float FIXED_SLOW = 0.15f;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();

    }
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        engine.addPlugin(new ps_vsguidedprojectilescript(projectile, ship.getShipTarget()));
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        float emp = projectile.getEmpAmount();
        float dam = 0;
        float count = 0;
        if(shieldHit) {
            while(count < EMP_COUNT) {
                count++;
                engine.spawnEmpArcPierceShields(
                        projectile.getSource(), point, target, target,
                        DamageType.ENERGY,
                        dam, // damage
                        emp, // emp
                        100000f, // max range
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        //new Color(25,100,155,255),
                        //new Color(255,255,255,255)
                        new Color(125,125,100,255),
                        new Color(255,255,255,255)
                );
            }
        }
        float mult=1;
        if(shieldHit)mult=0.5f;
        target.getVelocity().scale(FIXED_SLOW*mult);
    }
}
