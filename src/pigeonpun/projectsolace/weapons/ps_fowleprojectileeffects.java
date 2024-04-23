package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.CombatEntityPluginWithParticles;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class ps_fowleprojectileeffects extends CombatEntityPluginWithParticles {
    public static final Logger log = Global.getLogger(ps_fowleprojectileeffects.class);
    private DamagingProjectileAPI projectile;
    protected WeaponAPI weapon;
    protected DamagingProjectileAPI proj;
    protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
    public static final Color RIFT_COLOR = new Color(255,55,55,255);
    protected float delay = 1f;

    public ps_fowleprojectileeffects(WeaponAPI weapon) {
        super();
        this.weapon = weapon;
        delay = 0.5f;
        setSpriteSheetKey("fx_particles2");
    }

    public void attachToProjectile(DamagingProjectileAPI proj) {
        this.proj = proj;
    }
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;
        if (proj != null) {
            entity.getLocation().set(proj.getLocation());
        } else {
            entity.getLocation().set(weapon.getFirePoint(0));
        }
        super.advance(amount);

        boolean keepSpawningParticles = isWeaponCharging(weapon) ||
                (proj != null && !isProjectileExpired(proj) && !proj.isFading());
        if (keepSpawningParticles) {
            interval.advance(amount);
            if (interval.intervalElapsed()) {
                addChargingParticles(weapon);
            }
        }
        if (proj != null) {
            Global.getSoundPlayer().playLoop("realitydisruptor_loop", proj, 1f, 1f * proj.getBrightness(),
                    proj.getLocation(), proj.getVelocity());
            spawnArc();
        }
    }
    public void addChargingParticles(WeaponAPI weapon) {
        //CombatEngineAPI engine = Global.getCombatEngine();
        Color color = RiftLanceEffect.getColorForDarkening(RIFT_COLOR);

//		float b = 1f;
//		color = Misc.scaleAlpha(color, b);
        //undercolor = Misc.scaleAlpha(undercolor, b);

        float size = 50f;
        float underSize = 75f;
        //underSize = 100f;

        float in = 0.25f;
        float out = 0.75f;

        out *= 3f;

        float velMult = 0.2f;

        if (isWeaponCharging(weapon)) {
            size *= 0.25f + weapon.getChargeLevel() * 0.75f;
        }

        addDarkParticle(size, in, out, 1f, size * 0.5f * velMult, 0f, color);
        randomizePrevParticleLocation(size * 0.33f);

        if (proj != null) {
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
            //size = 40f;
            if (proj.getElapsed() > 0.2f) {
                addDarkParticle(size, in, out, 1.5f, size * 0.5f * velMult, 0f, color);
                Vector2f offset = new Vector2f(dir);
                offset.scale(size * 0.6f + (float) Math.random() * 0.2f);
                Vector2f.add(prev.offset, offset, prev.offset);
            }
            if (proj.getElapsed() > 0.4f) {
                addDarkParticle(size * 1f, in, out, 1.3f, size * 0.5f * velMult, 0f, color);
                Vector2f offset = new Vector2f(dir);
                offset.scale(size * 1.2f + (float) Math.random() * 0.2f);
                Vector2f.add(prev.offset, offset, prev.offset);
            }
            if (proj.getElapsed() > 0.6f) {
                addDarkParticle(size * .8f, in, out, 1.1f, size * 0.5f * velMult, 0f, color);
                Vector2f offset = new Vector2f(dir);
                offset.scale(size * 1.6f + (float) Math.random() * 0.2f);
                Vector2f.add(prev.offset, offset, prev.offset);
            }

            if (proj.getElapsed() > 0.8f) {
                addDarkParticle(size * .8f, in, out, 1.1f, size * 0.5f * velMult, 0f, color);
                Vector2f offset = new Vector2f(dir);
                offset.scale(size * 2.0f + (float) Math.random() * 0.2f);
                Vector2f.add(prev.offset, offset, prev.offset);
            }

            addParticle(underSize * 0.5f, in, out, 1.5f * 3f, 0f, 0f, RIFT_COLOR);
            randomizePrevParticleLocation(underSize * 0.67f);
            addParticle(underSize * 0.5f, in, out, 1.5f * 3f, 0f, 0f, RIFT_COLOR);
            randomizePrevParticleLocation(underSize * 0.67f);
        }
    }
    public void spawnArc() {
        spawnEMPParticles(RealityDisruptorChargeGlow.EMPArcHitType.SOURCE, proj.getLocation(), null);
    }
    public void spawnEMPParticles(RealityDisruptorChargeGlow.EMPArcHitType type, Vector2f point, CombatEntityAPI target) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Color color = RiftLanceEffect.getColorForDarkening(RIFT_COLOR);

        float size = 30f;
        float baseDuration = 1.5f;
        Vector2f vel = new Vector2f();
        int numNegative = 5;

        Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
        for (int i = 0; i < numNegative; i++) {
            float dur = baseDuration + baseDuration * (float) Math.random();
            float nSize = size;
            if (type == RealityDisruptorChargeGlow.EMPArcHitType.SOURCE) {
                nSize *= 1.5f;
            }
            Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
            Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
            v.scale(nSize + nSize * (float) Math.random() * 0.5f);
            v.scale(0.2f);

            float endSizeMult = 2f;
            if (type == RealityDisruptorChargeGlow.EMPArcHitType.SOURCE) {
                pt = Misc.getPointWithinRadius(point, nSize * 0f);
                Vector2f offset = new Vector2f(dir);
                offset.scale(size * 0.2f * i);
                Vector2f.add(pt, offset, pt);
                endSizeMult = 1.5f;
                v.scale(0.5f);
            }
            Vector2f.add(vel, v, v);

            float maxSpeed = nSize * 1.5f * 0.2f;
            float minSpeed = nSize * 1f * 0.2f;
            float overMin = v.length() - minSpeed;
            if (overMin > 0) {
                float durMult = 1f - overMin / (maxSpeed - minSpeed);
                if (durMult < 0.1f) durMult = 0.1f;
                dur *= 0.5f + 0.5f * durMult;
            }

            engine.addNegativeNebulaParticle(pt, v, nSize * 1f, endSizeMult,
                    //engine.addNegativeSwirlyNebulaParticle(pt, v, nSize * 1f, endSizeMult,
                    0.25f / dur, 0f, dur, color);
        }

        float dur = baseDuration;
        float rampUp = 0.5f / dur;
        color = RIFT_COLOR;
        for (int i = 0; i < 7; i++) {
            Vector2f loc = new Vector2f(point);
            loc = Misc.getPointWithinRadius(loc, size * 1f);
            float s = size * 4f * (0.5f + (float) Math.random() * 0.5f);
            engine.addSwirlyNebulaParticle(loc, vel, s, 1.5f, rampUp, 0f, dur, color, false);
        }
    }

    public float getRenderRadius() {
        return 500f;
    }
    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        // pass in proj as last argument to have particles rotate
        super.render(layer, viewport, null);
    }
    @Override
    protected float getGlobalAlphaMult() {
        if (proj != null && proj.isFading()) {
            return proj.getBrightness();
        }
        return super.getGlobalAlphaMult();
    }

    public boolean isExpired() {
        boolean keepSpawningParticles = isWeaponCharging(weapon) ||
                (proj != null && !isProjectileExpired(proj) && !proj.isFading());
        return super.isExpired() && (!keepSpawningParticles || (!weapon.getShip().isAlive() && proj == null));
    }
    public static boolean isProjectileExpired(DamagingProjectileAPI proj) {
        return proj.isExpired() || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj);
    }
    public static boolean isWeaponCharging(WeaponAPI weapon) {
        return weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
    }
}
