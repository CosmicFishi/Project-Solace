package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.hullmods.ps_boundlessbarrier;

import java.awt.*;
import java.util.List;

//Hit on armor/hull instantly make the enemy engine goes out for a short duration. Modifications to beam range will increase flame out duration.
public class ps_acriseffects implements BeamEffectPlugin, EveryFrameWeaponEffectPlugin {
    private boolean runOnce = false;
    private float FLAMEOUT_DURATION = 2f; //2 seconds
    private Logger log = Global.getLogger(ps_acriseffects.class);

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused()) return;

        CombatEntityAPI target = beam.getDamageTarget();

        if (target instanceof ShipAPI && !((ShipAPI) target).isStation() && beam.getBrightness() >= 1f) {
            ShipAPI ship = (ShipAPI) target;
            float dur = FLAMEOUT_DURATION;
            Vector2f point = beam.getRayEndPrevFrame();

            float modifiedBeamRange = beam.getWeapon().getRange();
            float specBeamRange = beam.getWeapon().getSpec().getMaxRange();
            //check if hit armor or hull
            if(CollisionUtils.isPointWithinBounds(point, ship) && ship.getEngineController() != null) {
                if(modifiedBeamRange > specBeamRange) {
                    dur = dur * (modifiedBeamRange / specBeamRange);
                }
                engine.addLayeredRenderingPlugin(new acrisFlameOutPlugin(ship, dur));
            }
        }
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
    //Credits to theDragn since i copy and modify APexDartGunPlugin
    public class acrisFlameOutPlugin extends BaseCombatLayeredRenderingPlugin {
        ShipAPI target;
        float flameOutDuration = 1;
        float currentDuration = 0;
        final static String BUFF_ID = "ps_acrisFlameOut";
        final static float repairMult = 1f;
        boolean removePlugin = false;
        /**
         * @param target Not null
         * @param duration If below 0, will default to 1
         */
        public acrisFlameOutPlugin(ShipAPI target, float duration) {
            if(duration < 0) {
                duration = flameOutDuration;
            }
            this.target = target;
            target.getEngineController().forceFlameout();
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine() == null)
                return;
            if (!target.isAlive())
                return;
            currentDuration += amount;
            target.setJitter("ps_acrisFlameOut", new Color(207, 208, 30, 55), 1f, 4, 1f, 15);
            if(currentDuration > flameOutDuration) {
                target.getMutableStats().getCombatEngineRepairTimeMult().modifyMult(BUFF_ID, repairMult);
                if(!target.getEngineController().isFlamedOut()) {
                    //Thanks Tart for the flame out jumps start
                    target.getMutableStats().getCombatEngineRepairTimeMult().unmodify(BUFF_ID);
                    removePlugin = true;
                }
            }
        }

        @Override
        public float getRenderRadius() {
            return 1000;
        }

        @Override
        public boolean isExpired() {
            return target.isExpired() || removePlugin;
        }
    }
}
