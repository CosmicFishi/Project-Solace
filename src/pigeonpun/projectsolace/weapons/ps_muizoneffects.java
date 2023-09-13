package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Objects;

public class ps_muizoneffects implements EveryFrameWeaponEffectPlugin {
    private static final String MUIZON_ID = "ps_muizon";
    private static final IntervalUtil empTimer = new IntervalUtil(0.05f, 0.15f);
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        WeightedRandomPicker<Vector2f> empEndPointPicker = new WeightedRandomPicker<>();
        empTimer.advance(amount);
        if(weapon.getChargeLevel() < 1 && weapon.isFiring() && empTimer.intervalElapsed()) {
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
}
