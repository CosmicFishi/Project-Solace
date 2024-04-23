package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_vsguidedprojectilescript;
import pigeonpun.projectsolace.world.ps_salvagesplacer;

import java.awt.*;
import java.util.Objects;

public class ps_muizoneffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin{
    public static final Logger log = Global.getLogger(ps_muizoneffects.class);
    private static final String MUIZON_ID = "ps_muizon";
    private static final IntervalUtil empTimer = new IntervalUtil(0.05f, 0.15f);
    private float currentChargeLevel = 0;
    private boolean isChargingUp = false;
    private boolean isChargingDown = false;
    private DamagingProjectileAPI projectile;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        WeightedRandomPicker<Vector2f> empEndPointPicker = new WeightedRandomPicker<>();
        if(Global.getCombatEngine().isPaused()) return;
        //guided

        empTimer.advance(amount);
        if(weapon.isFiring()) {
            if(currentChargeLevel < weapon.getChargeLevel()) {
                isChargingUp = true;
                isChargingDown = false;
            } else if(currentChargeLevel > weapon.getChargeLevel()) {
                isChargingUp = false;
                isChargingDown = true;
            }
            currentChargeLevel = weapon.getChargeLevel();
        }
        if(isChargingUp && empTimer.intervalElapsed()) {
            for(WeaponAPI w: ship.getAllWeapons()) {
                if(!Objects.equals(w.getId(), MUIZON_ID)) {
                    empEndPointPicker.add(w.getLocation(), 4);
                }
            }
            ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
            for (BoundsAPI.SegmentAPI s: ship.getExactBounds().getSegments()) {
                //Global.getCombatEngine().addFloatingText( s.getP1() ,  ".", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);
                if(!empEndPointPicker.getItems().contains(s.getP1())) {
                    empEndPointPicker.add(s.getP1(), 1);
                }
            }
            Vector2f selectedPoint = empEndPointPicker.pick();
            engine.spawnEmpArcVisual(
                    weapon.getLocation(),
                    ship,
                    selectedPoint,
                    ship,
                    2f,
                    new Color(245, 153, 39, 255),
                    new Color(255, 255, 255, 255)
            );
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        engine.addPlugin(new ps_vsguidedprojectilescript(projectile, ship.getShipTarget()));
    }
}
