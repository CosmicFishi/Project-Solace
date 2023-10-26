package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.util.D;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.opengl.DrawUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.com.ps_util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//briefly overload fighter and flame out missiles in a certain range
//Overload radius reaches max after projectile have travel certain distance
//Overload radius scale with weapon max range and have a hard cap
//todo: custom hullmod which displace informations about Boundless weapons equiped on the ship
//todo: make fx
public class ps_daemelieffects extends BaseCombatLayeredRenderingPlugin implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    Logger log = Global.getLogger(ps_daemelieffects.class);
    private boolean runOnce = false;
    private DamagingProjectileAPI projectile;
    private float effectiveRange = 0;
    private float timeTravel = 0;
    protected static final float BASE_RADIUS = 20f;
    protected static final float SOFT_MAX_RADIUS_BONUS_MULT = 5f;
    protected static final float HARD_CAP_RADIUS = BASE_RADIUS + 300f;
    protected static final float BASE_SECONDS_BEFORE_MAX_RADIUS = 0.5f; // in seconds
    protected static final Color RING_COLOR = new Color(190,200,255,200);
    protected static final Color FX_COLOR = new Color(71, 237, 223, 200);
    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if(projectile != null && !projectile.isExpired() && !projectile.isFading()) {
            //render ring
            drawCircle(effectiveRange);
        }
    }

    @Override
    public float getRenderRadius() {
        return 5000f;
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!runOnce){
            runOnce=true;
            if(!weapon.getSlot().isBuiltIn()){
                ps_boundlesseffect.checkIfNeedAdding(weapon.getShip().getVariant());
            }
            engine.addLayeredRenderingPlugin(this);
        }

        if(projectile != null) {
            timeTravel += amount;
            if(!projectile.isExpired() && !projectile.isFading()) {
                float effectiveRange = Math.round(calcCurrRadius(weapon));
                this.effectiveRange = effectiveRange;
//                log.info(effectiveRange + " " + this.effectiveRange);
                //start overloading fighter / flame out projectiles
                startTheFun(effectiveRange, projectile);
            }
            if(projectile.isFading() || projectile.isExpired()) {
                timeTravel = 0;
                effectiveRange = 0;
            }
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        this.projectile = projectile;
    }
    protected float calcCurrRadius(WeaponAPI weapon) {
        float currRadius = BASE_RADIUS;
        float maxModifiedRadius = BASE_RADIUS + (BASE_RADIUS * SOFT_MAX_RADIUS_BONUS_MULT);
        //calculate weapon range effect the overload radius
        maxModifiedRadius *= calcWeaponRangeBonusRadiusMult(weapon);
        float currsecondsBeforeMaxRadius = BASE_SECONDS_BEFORE_MAX_RADIUS;
        if(timeTravel > 0) {
            currRadius = Misc.interpolate(currRadius, maxModifiedRadius, (timeTravel/currsecondsBeforeMaxRadius));
        }
        if(currRadius > HARD_CAP_RADIUS) {
            currRadius = HARD_CAP_RADIUS;
        }
        return currRadius;
    }
    protected float calcWeaponRangeBonusRadiusMult(WeaponAPI weapon) {
        float currentMult = 1;
        currentMult = weapon.getRange() / weapon.getSpec().getMaxRange();
        return currentMult;
    }
    protected void drawCircle(float effectiveRange) {
        SpriteAPI sprite = Global.getSettings().getSprite("fx", "ps_ring_core");
//        log.info(effectiveRange);
        ps_util.drawTexturedRing(
                projectile.getLocation(),
                effectiveRange,
                8f,
                30,
                1,
                0.2f,
                sprite,
                RING_COLOR
                );
    }
    protected void startTheFun(float effectiveRange, DamagingProjectileAPI projectile) {
        List<MissileAPI> missileToFlamedOut;
        List<ShipAPI> fighterToOverloaded;

        missileToFlamedOut = CombatUtils.getMissilesWithinRange(projectile.getLocation(), effectiveRange);
        for (MissileAPI missile: missileToFlamedOut) {
            if(missile.getOwner() != 1) continue;
            if(!missile.isFizzling() && !missile.isFading() && !missile.isExpired() && !missile.getEngineController().isFlamedOut()) {
//                missile.getEngineController().forceFlameout();
                missile.flameOut();
                doFxOnOverload(missile);
            }
        }

        fighterToOverloaded = CombatUtils.getShipsWithinRange(projectile.getLocation(), effectiveRange);
        for(ShipAPI ship: fighterToOverloaded) {
            if (ship.getOwner() != 1) continue;
            if(ship.isAlive() && !ship.isExpired() && ship.isFighter() && !ship.getEngineController().isFlamedOut()) {
                ship.getEngineController().forceFlameout();
                doFxOnOverload(ship);
            }
        }
    }
    protected void doFxOnOverload(CombatEntityAPI entity) {
        Global.getCombatEngine().addNebulaParticle(entity.getLocation(), new Vector2f(0, 0), 20f, 1.2f, 1f, 1, 0.1f, FX_COLOR);
    }
}
