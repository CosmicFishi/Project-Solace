package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_vsguidedprojectilescript;

import java.awt.*;
import java.util.Objects;

public class ps_fowleeffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin{
    public static final Logger log = Global.getLogger(ps_fowleeffects.class);
    protected CombatEntityAPI projectileEntity;
    protected ps_fowleprojectileeffects fowleProjectilePlugin;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if(Global.getCombatEngine().isPaused()) return;

        boolean charging = weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
        if (charging && projectileEntity == null) {
            fowleProjectilePlugin = new ps_fowleprojectileeffects(weapon);
            projectileEntity = Global.getCombatEngine().addLayeredRenderingPlugin(fowleProjectilePlugin);
        } else if (!charging && projectileEntity != null) {
            projectileEntity = null;
            fowleProjectilePlugin = null;
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        if (fowleProjectilePlugin != null) {
            fowleProjectilePlugin.attachToProjectile(projectile);
            fowleProjectilePlugin = null;
            projectileEntity = null;

            MissileAPI missile = (MissileAPI) projectile;
            missile.setMine(true);
            missile.setNoMineFFConcerns(true);
//            missile.setMineExplosionRange(RealityDisruptorChargeGlow.MAX_ARC_RANGE + 50f);
            missile.setMinePrimed(true);
            missile.setUntilMineExplosion(0f);
        }
    }
}
