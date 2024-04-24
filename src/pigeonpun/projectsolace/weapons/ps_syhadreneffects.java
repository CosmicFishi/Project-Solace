package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ps_syhadreneffects implements EveryFrameWeaponEffectPlugin{
    private boolean runOnce = false;
    Logger log = Global.getLogger(ps_syhadreneffects.class);
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (engine.isPaused()) return;
        if(!runOnce){
            if(!weapon.getSlot().isBuiltIn()) {
                ps_boundlesseffect.checkIfNeedAdding(ship.getVariant());
            }
            runOnce=true;
        }
    }
}
