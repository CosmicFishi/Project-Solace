package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;
import pigeonpun.projectsolace.com.ps_misc;
import pigeonpun.projectsolace.hullmods.ps_incensemanufactured;

import java.awt.*;

public class ps_proteolyticbeameffects implements BeamEffectPlugin {
    private static final float EMP_DAMAGE_TO_ORIGINAL = 20f;
    private static final float MAX_EMP_DAMAGE_FLUX= 20f;
    private static final float EMP_DAMAGE_FLUX_MAX_OUT = 0.8f;
    private final IntervalUtil timer = new IntervalUtil(0.85f, 1.85f);
    private float chargeUpTimer = 0;
    private float chargeDownTimer = 0;
    private final float BEAM_WIDTH_SHIRNK = 6f;
    private float originalWeaponWidth = 0;
    private Color originalBeamColorFringe;
    private Color originalBeamColorCore;
    private Color originalBeamColorGlow;
    private boolean runOnce = true;
    private final Color BEAM_FRINGE_COLOR_CHANGE = new Color(255, 255, 255, 255);

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused()) {return;}

        ShipAPI ship = beam.getSource();
        if(runOnce) {
            originalWeaponWidth = beam.getWidth();
            originalBeamColorFringe = beam.getFringeColor();
            originalBeamColorCore = beam.getCoreColor();
            runOnce = false;
        }

        timer.advance(amount);
        if(timer.intervalElapsed()) {
            Vector2f beamLocation = beam.getWeapon().getLocation();
            if(beam.getDamageTarget() != null && beam.getDamageTarget() instanceof ShipAPI) {
                float fluxLevel = ((ShipAPI) beam.getDamageTarget()).getFluxLevel();
                float damage = (beam.getDamage().getDamage() * EMP_DAMAGE_TO_ORIGINAL / 100) +
                        (beam.getDamage().getDamage() * (fluxLevel / EMP_DAMAGE_FLUX_MAX_OUT * (MAX_EMP_DAMAGE_FLUX / 100)));
                engine.spawnEmpArc(beam.getSource(), beamLocation, null, beam.getDamageTarget(), DamageType.ENERGY, damage, 0, 2000f, null, 2f, Color.red, Color.white);
            }
        }

        //Erraticboost weapon change
        float chargeUpTime = ship.getSystem().getChargeUpDur();

        if(ship.isAlive()) {
            if(ship.getSystem().isActive()) {
                chargeUpTimer += amount;
                float chargeUpLevel = chargeUpTimer / chargeUpTime;
                beam.setWidth(Misc.interpolate(originalWeaponWidth, BEAM_WIDTH_SHIRNK, MagicAnim.smoothNormalizeRange(chargeUpLevel, 0.1f, 0.8f)));
                beam.setFringeColor(Misc.interpolateColor(beam.getFringeColor(), BEAM_FRINGE_COLOR_CHANGE, MagicAnim.smoothNormalizeRange(chargeUpLevel, 0.1f, 0.8f)));
            }

        }

    }
}