package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicAnim;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

//Some parts taken from Scy Siren weapon - Tartiflette
public class ps_destruenslaunchereffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private SpriteAPI ARMOR_LEFT;
    private SpriteAPI ARMOR_RIGHT;
    private ShipAPI SHIP;

    private float ALheight, ALwidth;
    private float systemChargeLevel = 0;
    private final IntervalUtil timer = new IntervalUtil(0.0333f,0.0333f);
    private boolean runOnce=false;
    private final String armorLeftID="BTM_ARMOR_LEFT";
    private final String armorRightID="BTM_ARMOR_RIGHT";
    private final float EMP_RANGE = 100f;
    private final IntervalUtil EMP_TIMER = new IntervalUtil(0.3f, 2f);
    private final float EMP_DAMAGE = 200f;
    private final HashSet<DamagingProjectileAPI> projList = new HashSet<>();
    private final Color EMP_COLOR = new Color(120,255,250,255);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused()) return;

        if (!runOnce || SHIP == null) {
            runOnce = true;
            SHIP = weapon.getShip();

            //get the weapon, all the sprites and sizes
            for (WeaponAPI w : SHIP.getAllWeapons()) {
                switch (w.getSlot().getId()) {

                    case armorLeftID:
                        ARMOR_LEFT = w.getSprite();
                        ALheight = ARMOR_LEFT.getHeight();
                        ALwidth = ARMOR_LEFT.getWidth();
                        break;

                    case armorRightID:
                        ARMOR_RIGHT = w.getSprite();
                        break;
                }
            }
            return;
        }

        //Pink armor animation
        timer.advance(amount);
        if (timer.intervalElapsed()) {
            systemChargeLevel = SHIP.getSystem().getEffectLevel();
            float AR_y = ALheight / 2 - MagicAnim.smoothNormalizeRange(systemChargeLevel, 0f, 0.5f) * -11;
            float AR_x_L = ALwidth / 2 + MagicAnim.smoothNormalizeRange(systemChargeLevel, 0.3f, 0.7f) * -3;
            float AR_x_R = ALwidth / 2 - MagicAnim.smoothNormalizeRange(systemChargeLevel, 0.3f, 0.7f) * -3;
            ARMOR_LEFT.setCenter(AR_x_L, AR_y);
            ARMOR_RIGHT.setCenter(AR_x_R, AR_y);
        }

        ////////////////////
        //EMP
        ////////////////////
        HashSet<DamagingProjectileAPI> removeList = new HashSet<>();
        for (DamagingProjectileAPI proj : projList) {
            if (proj.isExpired() || proj.didDamage() || !engine.isInPlay(proj)) {
                removeList.add(proj);
            } else {
                //get list target
                List<CombatEntityAPI> targetNearby = CombatUtils.getEntitiesWithinRange(proj.getLocation(), EMP_RANGE);
                HashSet<CombatEntityAPI> listTargets = new HashSet<>();
                for (CombatEntityAPI entity: targetNearby) {
                    if(entity instanceof MissileAPI || entity instanceof FighterWingAPI) {
                        //projectile is on player team => damage enemy
                        //projectile is from enemy => damage player + allies ships
                        if(proj.getOwner() != entity.getOwner()) {
                            listTargets.add(entity);
                        }
                    }
                }
                //spawn EMP arc
                EMP_TIMER.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if(EMP_TIMER.intervalElapsed()) {
                    if(!listTargets.isEmpty()) {
                        for (CombatEntityAPI entity: listTargets) {
                            engine.spawnEmpArc(
                                    proj.getSource(),
                                    proj.getLocation(),
                                    proj,
                                    entity,
                                    DamageType.ENERGY,
                                    EMP_DAMAGE,
                                    0,
                                    10000,
                                    null,
                                    MathUtils.getRandomNumberInRange(5f,10f),
                                    EMP_COLOR,
                                    new Color(255, 255,255, 255)
                            );
                        }
                    } else {
                        float angle = (float) (Math.random() * 360);
                        Vector2f endArcPoint = MathUtils.getPointOnCircumference(proj.getLocation(), 50f, angle);

                        engine.spawnEmpArcVisual(
                                proj.getLocation(),
                                proj,
                                endArcPoint,
                                new SimpleEntity(endArcPoint),
                                MathUtils.getRandomNumberInRange(5f,10f),
                                EMP_COLOR,
                                new Color(255, 255,255, 255)
                        );
                    };
                }
            }

        }
        for (DamagingProjectileAPI toRemoveProj : removeList) {
            projList.remove(toRemoveProj);
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        projList.add(projectile);
        Global.getSoundPlayer().playSound("ps_destruenlaunch", 1, 1f, weapon.getShip().getLocation(), new Vector2f(20, 20));
    }
}
