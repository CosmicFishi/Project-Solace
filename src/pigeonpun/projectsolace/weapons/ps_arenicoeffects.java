package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;
import pigeonpun.projectsolace.com.ps_boundlesseffect;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;

public class ps_arenicoeffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    public final Logger log = Global.getLogger(ps_arenicoeffects.class);
    public static final Color CHANGE_TO_COLOR = new Color(161, 165,25, 255);
    public static final Color OG_COLOR = new Color(161,255,25,255);
    public static final Color CHANGE_TO_COLOR_2 = new Color(161, 65,25, 255);
    public static final Color OG_COLOR_2 = new Color(161,255,25,255);
    public final IntervalUtil succProjectilesTimer = new IntervalUtil(1.8f,3.8f);
    public final IntervalUtil succDelayFxTimer = new IntervalUtil(0.2f,0.2f);
    public boolean succStartFx = false;
    public final float SUCC_PROJ_COUNT = 3;
    public final float SUCC_CONVERT_PER_PROJ = 1;
    public final float SUCC_CONVERT_PER_HITPOINT = 100f;
    public final float SUCC_RADIUS = 500f;
    private boolean runOnce = false;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(Global.getCombatEngine().isPaused()) return;

        if(!runOnce){
            runOnce=true;
            if(!weapon.getSlot().isBuiltIn()){
                ps_boundlesseffect.checkIfNeedAdding(weapon.getShip().getVariant());
            }
        }

        if(!succStartFx) {
            succProjectilesTimer.advance(amount);
            if(succProjectilesTimer.intervalElapsed()) {
                succStartFx = true;
            }
        }
        if(succStartFx) {
            //spam fx
            doFx(weapon);
            //do funny succ
            succDelayFxTimer.advance(amount);
            if(succDelayFxTimer.intervalElapsed()) {
                succStartFx = false;
                doTheFunny(weapon);
            }
        }
    }
    public void doFx(WeaponAPI weapon) {
        Vector2f spawnHitParticlePoint = MathUtils.getPointOnCircumference(
                weapon.getLocation(),
                MathUtils.getRandomNumberInRange(10f, 50f),
                MathUtils.getRandomNumberInRange(0, 360)
        );
        Global.getCombatEngine().addSmoothParticle(spawnHitParticlePoint, (Vector2f) VectorUtils.getDirectionalVector(spawnHitParticlePoint, weapon.getLocation()).scale(45f), 10f, 1f, 1f, new Color(255,255,89,255));
    }
    public void doTheFunny(WeaponAPI weapon) {
        CombatEngineAPI engine = Global.getCombatEngine();
        List<CombatEntityAPI> list = CombatUtils.getEntitiesWithinRange(weapon.getLocation(), SUCC_RADIUS);
        WeightedRandomPicker<CombatEntityAPI> succableList = new WeightedRandomPicker<>();
        float totalAmmoSucc = 0;
        for (CombatEntityAPI proj: list) {
            if(proj.getOwner() == 1) {
                if(!proj.isExpired() && (proj instanceof DamagingProjectileAPI || proj instanceof MissileAPI)) {
                    succableList.add(proj);
                }
            }
        }
        if(!succableList.isEmpty()) {
            for (int i =0; i < SUCC_PROJ_COUNT; i++) {
                CombatEntityAPI proj = succableList.pick();
                //count the ammo
                totalAmmoSucc += SUCC_CONVERT_PER_PROJ;
                if(proj.getMaxHitpoints() > 0) {
                    totalAmmoSucc += Math.round(proj.getMaxHitpoints() / SUCC_CONVERT_PER_HITPOINT);
                }
                //remove proj
                Global.getCombatEngine().removeObject(proj);
                //do fx :D
                engine.spawnEmpArcVisual(
                        weapon.getLocation(),
                        weapon.getShip(),
                        proj.getLocation(),
                        null,
                        4f,
                        Color.red,
                        new Color(255,255,255,255)
                );
            }
            //add ammo for weapon
            if(weapon.getAmmo() < weapon.getMaxAmmo()) {
                int ammoToAdd = (int) (weapon.getAmmo() + totalAmmoSucc);
                if(ammoToAdd > weapon.getMaxAmmo()) {
                    ammoToAdd = weapon.getMaxAmmo();
                }
                weapon.setAmmo(ammoToAdd);
                Global.getSoundPlayer().playSound("ps_arenico_succ", MathUtils.getRandomNumberInRange(0.9f,1f), 0.9f, weapon.getLocation(), new Vector2f());
            }
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ps_arenicoProjectileRenderPlugin plugin = new ps_arenicoProjectileRenderPlugin(projectile);
        engine.addLayeredRenderingPlugin(plugin);
    }
    public class ps_arenicoProjectileRenderPlugin extends BaseCombatLayeredRenderingPlugin {
        public DamagingProjectileAPI proj;
        public Color colorToChange;
        public Color colorToChange2;
        public float uniqueId;
        public ps_arenicoProjectileRenderPlugin(DamagingProjectileAPI projectile) {
            this.proj = projectile;
            float currentAmmo = this.proj.getWeapon().getAmmo();
            float maxAmmo = this.proj.getWeapon().getMaxAmmo();
            float progression = 1 - currentAmmo / maxAmmo;
            this.colorToChange = Misc.interpolateColor(
                    OG_COLOR,
                    CHANGE_TO_COLOR,
                    progression
            );
            this.colorToChange2 = Misc.interpolateColor(
                    OG_COLOR_2,
                    CHANGE_TO_COLOR_2,
                    progression
            );
            this.uniqueId = MagicTrailPlugin.getUniqueID();
        }
        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (Global.getCombatEngine().isPaused()) return;
            SpriteAPI sprite = Global.getSettings().getSprite("fx", "base_trail_smooth");
            SpriteAPI sprite2 = Global.getSettings().getSprite("fx", "base_trail_fuzzy");
            MagicTrailPlugin.addTrailMemberSimple(
                    this.proj,
                    this.uniqueId,
                    sprite,
                    this.proj.getLocation(),
                    0f,
                    Misc.getAngleInDegrees(new Vector2f(proj.getVelocity())),
                    this.proj.getProjectileSpec().getWidth(),
                    1f,
                    colorToChange,
                    0.8f,
                    0f,
                    0f,
                    0.2f,
                    true
                );
            MagicTrailPlugin.addTrailMemberSimple(
                    this.proj,
                    this.uniqueId,
                    sprite2,
                    this.proj.getLocation(),
                    0f,
                    Misc.getAngleInDegrees(new Vector2f(proj.getVelocity())),
                    this.proj.getProjectileSpec().getWidth(),
                    2f,
                    colorToChange2,
                    0.8f,
                    0.2f,
                    0f,
                    0.5f,
                    true
            );
        }

        @Override
        public boolean isExpired() {
            return !Global.getCombatEngine().isEntityInPlay(this.proj);
        }

        @Override
        public float getRenderRadius() {
            return 10000f;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        }
    }
}
