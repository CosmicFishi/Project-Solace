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
import java.util.Map;

//briefly overload fighter and flame out missiles in a certain range
//Overload radius reaches max after projectile have travel certain distance
//Overload radius scale with weapon max range and have a hard cap
//todo: custom hullmod which displace informations about Boundless weapons equiped on the ship
public class ps_daemelieffects extends BaseCombatLayeredRenderingPlugin implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    Logger log = Global.getLogger(ps_daemelieffects.class);
    private boolean runOnce = false;
    protected static final float BASE_RADIUS = 20f;
    protected static final float SOFT_MAX_RADIUS_BONUS_MULT = 3f;
    protected static final float HARD_CAP_RADIUS = BASE_RADIUS + 200f;
    protected static final float BASE_SECONDS_BEFORE_MAX_RADIUS = 0.5f; // in seconds
    protected static final Color RING_COLOR = new Color(190,200,255,200);
    protected static final Color FX_COLOR = new Color(71, 237, 223, 200);
    protected List<DamagingProjectileAPI> listProjectiles = new ArrayList<>();
    private static final String PROJ_CUSTOM_KEY = "ps_daemeli_overload_data";
    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if(listProjectiles.isEmpty()) return;
        for (DamagingProjectileAPI proj: new ArrayList<>(listProjectiles)) {
            if(proj != null && !proj.isExpired() && !proj.isFading()) {
                //render ring
                drawCircle(proj);
            }
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
        if(listProjectiles.isEmpty()) return;
        List<DamagingProjectileAPI> removeList = new ArrayList<>();
        for (DamagingProjectileAPI proj: new ArrayList<>(listProjectiles)) {
            if(!proj.isExpired() && !proj.isFading()) {
                calcCurrTimeTravel(proj, amount);
                calcCurrRadius(proj, weapon);
                //start flame out fighter / flame out projectiles
                startTheFun(proj);
            } else {
                removeList.add(proj);
            }
        }
        for (DamagingProjectileAPI proj: removeList) {
            listProjectiles.remove(proj);
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        projectile.setCustomData(PROJ_CUSTOM_KEY, new ps_daemeliProjectileOverloadData());
        listProjectiles.add(projectile);
    }
    protected void calcCurrTimeTravel(DamagingProjectileAPI proj, float amount) {
        Map<String, Object> map = proj.getCustomData();
        if(map.get(PROJ_CUSTOM_KEY) != null) {
            ps_daemeliProjectileOverloadData projData = (ps_daemeliProjectileOverloadData) map.get(PROJ_CUSTOM_KEY);
            projData.setTimeTravel(projData.getTimeTravel() + amount);
            //set data
            proj.setCustomData(PROJ_CUSTOM_KEY, projData);
        }
    }
    protected float calcCurrRadius(DamagingProjectileAPI proj, WeaponAPI weapon) {
        float currRadius = BASE_RADIUS;
        float maxModifiedRadius = BASE_RADIUS + (BASE_RADIUS * SOFT_MAX_RADIUS_BONUS_MULT);
        //calculate weapon range effect the overload radius
        maxModifiedRadius *= calcWeaponRangeBonusRadiusMult(weapon);
        float currsecondsBeforeMaxRadius = BASE_SECONDS_BEFORE_MAX_RADIUS;
        Map<String, Object> map = proj.getCustomData();
        if(map.get(PROJ_CUSTOM_KEY) != null) {
            ps_daemeliProjectileOverloadData projData = (ps_daemeliProjectileOverloadData) map.get(PROJ_CUSTOM_KEY);
            if(projData.getTimeTravel() > 0) {
                currRadius = Misc.interpolate(currRadius, maxModifiedRadius, (projData.getTimeTravel()/currsecondsBeforeMaxRadius));
            }
            if(currRadius > HARD_CAP_RADIUS) {
                currRadius = HARD_CAP_RADIUS;
            }
            projData.setEffectiveRadius(currRadius);
            //set data
            proj.setCustomData(PROJ_CUSTOM_KEY, projData);
        }
        return currRadius;
    }
    protected float calcWeaponRangeBonusRadiusMult(WeaponAPI weapon) {
        float currentMult = 1;
        currentMult = weapon.getRange() / weapon.getSpec().getMaxRange();
        return currentMult;
    }
    protected void drawCircle(DamagingProjectileAPI proj) {
        Map<String, Object> map = proj.getCustomData();
        if(map.get(PROJ_CUSTOM_KEY) != null) {
            ps_daemeliProjectileOverloadData projData = (ps_daemeliProjectileOverloadData) map.get(PROJ_CUSTOM_KEY);
            SpriteAPI sprite = Global.getSettings().getSprite("fx", "ps_ring_core");
//        log.info(effectiveRange);
            ps_util.drawTexturedRing(
                    proj.getLocation(),
                    projData.getEffectiveRadius(),
                    8f,
                    30,
                    1,
                    0.2f,
                    sprite,
                    RING_COLOR
            );
        }
    }
    protected void startTheFun(DamagingProjectileAPI proj) {
        List<MissileAPI> missileToFlamedOut;
        List<ShipAPI> fighterToOverloaded;
        Map<String, Object> map = proj.getCustomData();
        if(map.get(PROJ_CUSTOM_KEY) != null) {
            ps_daemeliProjectileOverloadData projData = (ps_daemeliProjectileOverloadData) map.get(PROJ_CUSTOM_KEY);
            missileToFlamedOut = CombatUtils.getMissilesWithinRange(proj.getLocation(), projData.getEffectiveRadius());
            for (MissileAPI missile: missileToFlamedOut) {
                if(missile.getOwner() != 1) continue;
                if(!missile.isFizzling() && !missile.isFading() && !missile.isExpired() && !missile.getEngineController().isFlamedOut()) {
                    missile.flameOut();
                    doFxOnOverload(missile);
                    Global.getSoundPlayer().playSound("ps_daemeli_disruption", MathUtils.getRandomNumberInRange(0.8f, 1.1f), 1, entity.getLocation(), new Vector2f(0,0));
                }
            }

            fighterToOverloaded = CombatUtils.getShipsWithinRange(proj.getLocation(), projData.getEffectiveRadius());
            for(ShipAPI ship: fighterToOverloaded) {
                if (ship.getOwner() != 1) continue;
                if(ship.isAlive() && !ship.isExpired() && ship.isFighter() && !ship.getEngineController().isFlamedOut()) {
                    ship.getEngineController().forceFlameout();
                    doFxOnOverload(ship);
                    Global.getSoundPlayer().playSound("ps_daemeli_disruption", MathUtils.getRandomNumberInRange(0.8f, 1.1f), 1, entity.getLocation(), new Vector2f(0,0));
                }
            }
        }
    }
    protected void doFxOnOverload(CombatEntityAPI entity) {
        Global.getCombatEngine().addNebulaParticle(entity.getLocation(), new Vector2f(0, 0), 20f, 1.2f, 1f, 1, 0.1f, FX_COLOR);
        if(entity instanceof MissileAPI) {
            ((MissileAPI) entity).setJitter(this, FX_COLOR, 0.7f, 5, 30f);
        }
        if(entity instanceof ShipAPI) {
            ((ShipAPI) entity).setJitterUnder(this, FX_COLOR, 0.7f, 5, 30f);
        }
    }

    protected class ps_daemeliProjectileOverloadData {
        private float effectiveRadius = 0;
        private float timeTravel = 0;
        public void setEffectiveRadius(float effectiveRange) {
            this.effectiveRadius = effectiveRange;
        }
        public float getEffectiveRadius() {
            return effectiveRadius;
        }
        public void setTimeTravel(float timeTravel) {
            this.timeTravel = timeTravel;
        }
        public float getTimeTravel() {
            return timeTravel;
        }
    }
}
