package pigeonpun.projectsolace.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.input.InputEventAPI;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;
import pigeonpun.projectsolace.com.ps_boundlesseffect;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.List;

public class ps_anaxyreffects implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    private boolean runOnce = false;
    public static final float ANAXYR_FLUX_LEVEL_REQUIRE = 0.5f;
    public final Color AGGRESSIVE_COLOR = new Color(243, 94, 0, 255);
    public final Color AGGRESSIVE_TRAIL_COLOR = new Color(141, 54, 0, 155);
//    protected final float AGGRESSIVE_MISSILE_SPEED_MULT = 1.1f;
//    public static final String MODIFY_ID = "ps_anaxyr_AGGRESSIVE_modify";
    private MissileAPI missile;
    Logger log = Global.getLogger(ps_anaxyreffects.class);
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (engine.isPaused()) return;
        if(!runOnce){
            if(!weapon.getSlot().isBuiltIn()){
                ps_boundlesseffect.checkIfNeedAdding(ship.getVariant());
            }
            if(!ship.hasListenerOfClass(ps_anaxyrDamageDealListener.class)) {
                ship.addListener(new ps_anaxyrDamageDealListener(ship));
            }
            runOnce=true;
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        MissileAPI missile = (MissileAPI) projectile;
        this.missile = missile;
        if(ship.getFluxLevel() > ANAXYR_FLUX_LEVEL_REQUIRE) {
            ps_anaxyrMissileVisualPlugin missileVisualPlugin = new ps_anaxyrMissileVisualPlugin(missile);
            engine.addPlugin(missileVisualPlugin);
        }
    }

    protected class ps_anaxyrDamageDealListener implements DamageDealtModifier {
        ShipAPI ship;
        ps_anaxyrDamageDealListener(ShipAPI ship) {
            this.ship = ship;
        }
        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            //check if missile
            if(ship.getFluxLevel() < ANAXYR_FLUX_LEVEL_REQUIRE) {
                return null;
            }
            if(param instanceof MissileAPI && ((MissileAPI) param).getProjectileSpecId() != null) {
                if(((MissileAPI) param).getProjectileSpecId().equals(ps_misc.ANAXYR_MSL_ID)) {
                    damage.setType(DamageType.HIGH_EXPLOSIVE);
                    log.info(damage.getType().toString() + " " + damage.getDamage());
                }
            }
            return null;
        }
    }
    protected class ps_anaxyrMissileVisualPlugin extends BaseEveryFrameCombatPlugin {
        MissileAPI missile;
        ps_anaxyrMissileVisualPlugin(MissileAPI missile) {
            this.missile = missile;
        }
        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            missile.getEngineController().fadeToOtherColor(this, AGGRESSIVE_COLOR, AGGRESSIVE_TRAIL_COLOR, 1,1);
        }
    }
}
