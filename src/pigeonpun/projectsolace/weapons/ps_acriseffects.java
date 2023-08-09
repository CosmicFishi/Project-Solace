package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.combat.*;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.hullmods.ps_boundlessbarrier;

public class ps_acriseffects implements BeamEffectPlugin, EveryFrameWeaponEffectPlugin {
    private boolean runOnce = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!runOnce){
            runOnce=true;
            if(!weapon.getSlot().isBuiltIn()){
                ps_boundlesseffect.checkIfNeedAdding(weapon.getShip().getVariant());
            }
        }
    }
}
