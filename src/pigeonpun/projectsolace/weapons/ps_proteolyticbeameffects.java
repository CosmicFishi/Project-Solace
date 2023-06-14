package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
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
    private final float BEAM_OG_DAMAGE_PERCENTAGE = 30f;
    private final IntervalUtil spawnEMPSystemTimer = new IntervalUtil(0.25f, 0.5f);
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused()) {return;}

        ShipAPI ship = beam.getSource();

        timer.advance(amount);
        spawnEMPSystemTimer.advance(amount);
        if(beam.getDamageTarget() != null && beam.getDamageTarget() instanceof ShipAPI) {
            if(timer.intervalElapsed()) {
                Vector2f beamLocation = beam.getWeapon().getLocation();
                float fluxLevel = ((ShipAPI) beam.getDamageTarget()).getFluxLevel();
                float damage = (beam.getDamage().getDamage() * EMP_DAMAGE_TO_ORIGINAL / 100) +
                        (beam.getDamage().getDamage() * (fluxLevel / EMP_DAMAGE_FLUX_MAX_OUT * (MAX_EMP_DAMAGE_FLUX / 100)));
                engine.spawnEmpArc(beam.getSource(), beamLocation, null, beam.getDamageTarget(), DamageType.ENERGY, damage, 0, 2000f, null, 2f, Color.red, Color.white);
            }
            //Erraticboost weapon change
            if(ship.getSystem().isActive()) {
                Vector2f hitLocation = beam.getRayEndPrevFrame();
                if(spawnEMPSystemTimer.intervalElapsed() && hitLocation != null) {
                    float spawnEMPCenterAngle = VectorUtils.getAngle(hitLocation, beam.getWeapon().getLocation());
                    Vector2f spawnEMPStart = MathUtils.getPointOnCircumference(hitLocation, 300f, MathUtils.getRandomNumberInRange(spawnEMPCenterAngle - 60, spawnEMPCenterAngle + 60));
//                Global.getCombatEngine().addFloatingText(spawnEMPStart, "...", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);
                    engine.spawnEmpArc(beam.getSource(), spawnEMPStart, null, beam.getDamageTarget(), DamageType.ENERGY, beam.getDamage().getDamage() * BEAM_OG_DAMAGE_PERCENTAGE / 100, 0, 2000f, null, 10f, new Color(140, 58, 255, 255), Color.white);
                }
            }
        }

    }
}