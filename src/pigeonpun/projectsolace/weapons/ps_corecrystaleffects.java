package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;

import data.scripts.util.MagicLensFlare;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

public class ps_corecrystaleffects implements EveryFrameWeaponEffectPlugin {
    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.45f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        
        if (engine.isPaused()) {return;}

        timer.advance(amount);
        if (timer.intervalElapsed()){
            Color nebulaColor = ps_misc.PROJECT_SOLACE_NEBULA_COLORs.get((int) (MathUtils.getRandomNumberInRange(0, ps_misc.PROJECT_SOLACE_NEBULA_COLORs.size() -1)));
            Vector2f nebulaLocation = MathUtils.getRandomPointInCircle(weapon.getLocation(), 50f);
            engine.addSmoothParticle(
                    nebulaLocation,
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(10f,15f),
                    2f,
                    0.4f,
                    nebulaColor
            );
        }
    }    
}