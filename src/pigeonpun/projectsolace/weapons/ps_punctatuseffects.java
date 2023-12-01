package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.combat.CombatEngine;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicCampaignTrailPlugin;
import org.magiclib.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicRender;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.com.ps_util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//When hit, spawn a hex mark on the hit location, constantly applying damage for whatever in its range for a certain duration
public class ps_punctatuseffects implements EveryFrameWeaponEffectPlugin, OnHitEffectPlugin {
    Logger log = Global.getLogger(ps_punctatuseffects.class);
    boolean runOnce = false;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!runOnce){
            runOnce=true;
            if(!weapon.getSlot().isBuiltIn()){
                ps_boundlesseffect.checkIfNeedAdding(weapon.getShip().getVariant());
            }
        }
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        engine.addLayeredRenderingPlugin(new ps_punctatusQuakeCraterPlugin(projectile, point));
    }

    public class ps_punctatusQuakeCraterPlugin extends BaseCombatLayeredRenderingPlugin{
        protected DamagingProjectileAPI projectile;
        public static final float CRATER_DAMAGE = 200f;
        public static final float CRATER_RADIUS = 250f;
        public final IntervalUtil craterExplosionTimer = new IntervalUtil(2f, 2f);
        public static final float CRATER_DESPAWN_TIMER = 6f;
        protected float craterCurrentTimer = 0;
        protected Vector2f hitLocation;
        protected boolean didExplosionFirstTime = false;
        public ps_punctatusQuakeCraterPlugin(DamagingProjectileAPI projectile, Vector2f hitLocation) {
            this.projectile = projectile;
            this.hitLocation = hitLocation;
        }
        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine.isPaused()) return;
            craterCurrentTimer += Global.getCombatEngine().getElapsedInLastFrame();
            craterExplosionTimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(!didExplosionFirstTime) {
                doExplosionThings();
                didExplosionFirstTime = true;
            }
            if(craterExplosionTimer.intervalElapsed()) {
                doExplosionThings();
            }
        }
        public void doExplosionThings() {
            CombatEngineAPI engine = Global.getCombatEngine();
            SpriteAPI ringSprite = Global.getSettings().getSprite("fx", "ps_ring_smooth");
            MagicRender.battlespace(
                    ringSprite,
                    this.hitLocation,
                    new Vector2f(0, 0),
                    new Vector2f( 160f, 160f), new Vector2f(600,600),
                    MathUtils.getRandomNumberInRange(0,360),
                    0.2f,
                    new Color(122,94,124,255),
                    true,
                    0f,
                    0f,
                    0.7f
                    );
            engine.addNebulaSmoothParticle(
                    this.hitLocation,
                    new Vector2f(0,0),
                    15f,
                    30f,
                    0.2f,
                    0.8f,
                    0.4f,
                    new Color(179, 138, 159, 255)
            );
            engine.addNebulaSmoothParticle(
                    this.hitLocation,
                    new Vector2f(0,0),
                    15f,
                    30f,
                    0.2f,
                    0.8f,
                    0.7f,
                    new Color(105, 18, 114, 255)
            );
            DamagingExplosionSpec explosion = new DamagingExplosionSpec(
                    0.3f,
                    CRATER_RADIUS,
                    125,
                    CRATER_DAMAGE,
                    CRATER_DAMAGE * 0.5f,
                    CollisionClass.PROJECTILE_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    2,
                    5,
                    0.5f,
                    10,
                    new Color(219, 0, 11, 255),
                    new Color(255, 115,137, 255)
            );
            explosion.setDamageType(DamageType.HIGH_EXPLOSIVE);
            engine.spawnDamagingExplosion(explosion, projectile.getSource(), this.hitLocation);
            Global.getSoundPlayer().playSound(
                    "ps_punctatus_quake",
                    MathUtils.getRandomNumberInRange(0.9f, 1f),
                    MathUtils.getRandomNumberInRange(0.9f, 1f),
                    hitLocation,
                    new Vector2f()
                    );
        }
        @Override
        public float getRenderRadius() {
            return 100000f;
        }
        @Override
        public boolean isExpired() {
            return craterCurrentTimer > CRATER_DESPAWN_TIMER;
        }
    }
}
