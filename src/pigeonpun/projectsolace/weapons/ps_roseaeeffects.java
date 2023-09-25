package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_vsguidedprojectilescript;

import java.awt.*;
import java.util.Objects;

public class ps_roseaeeffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    public static final Logger log = Global.getLogger(ps_roseaeeffects.class);
    private static final String ROSEAE_ID = "ps_roseae";
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();

    }
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        engine.addPlugin(new ps_vsguidedprojectilescript(projectile, ship.getShipTarget()));
    }
}
