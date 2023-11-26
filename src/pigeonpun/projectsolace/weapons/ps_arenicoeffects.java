package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;

public class ps_arenicoeffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    public final Logger log = Global.getLogger(ps_arenicoeffects.class);
    public static final Color CHANGE_TO_COLOR = new Color(161, 25,25, 255);
    public static final Color OG_COLOR = new Color(161,255,25,255);
    public final IntervalUtil succProjectilesTimer = new IntervalUtil(2f,4f);
    public final float SUCC_PROJ_COUNT = 3;
    public final float SUCC_CONVERT_PER_PROJ = 1;
    public final float SUCC_CONVERT_PER_HITPOINT = 100f;
    public final float SUCC_RADIUS = 300f;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        succProjectilesTimer.advance(amount);
        if(succProjectilesTimer.intervalElapsed()) {
            List<DamagingProjectileAPI> listMissiles = CombatUtils.getProjectilesWithinRange(weapon.getLocation(), SUCC_RADIUS);
            WeightedRandomPicker<DamagingProjectileAPI> succableList = new WeightedRandomPicker<>();
            float totalAmmoSucc = 0;
            for (DamagingProjectileAPI proj: listMissiles) {
                if(!proj.isFading() && !proj.isExpired() && proj.getOwner() == 1) {
                    succableList.add(proj);
                }
            }
            if(!succableList.isEmpty()) {
                for (int i =0; i < SUCC_PROJ_COUNT; i++) {
                    DamagingProjectileAPI proj = succableList.pick();
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
                }
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
            this.uniqueId = MagicTrailPlugin.getUniqueID();
        }
        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (Global.getCombatEngine().isPaused()) return;
            SpriteAPI sprite = Global.getSettings().getSprite("fx", "base_trail_smooth");
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
                    0.15f,
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
