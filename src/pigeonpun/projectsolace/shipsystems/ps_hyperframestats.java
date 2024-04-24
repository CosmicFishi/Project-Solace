package pigeonpun.projectsolace.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ps_hyperframestats extends BaseShipSystemScript {

//    public static final float SPEED_BONUS = 100f;
//    public static final float ACCELERATION_BONUS = 100f;
//    public static final float AMMO_REGEN_BONUS = 2f;
//    public static final float FLUX_DISSIPATION_BONUS = 150f;
    public static final float TIME_DAL = 5f;
    public static final float EMP_RANGE = 200f;
    public static final float EMP_DAMAGE = 500f;
    private boolean ps_hyperframeEMP_activated = false;
    private static final float EMP_COUNT = 4;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (state == State.OUT) {
//            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            if(ship != null) {
                Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
//                stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS);
//                stats.getAcceleration().modifyPercent(id, ACCELERATION_BONUS);
//                stats.getEnergyAmmoRegenMult().modifyMult(id, AMMO_REGEN_BONUS);
//                stats.getBallisticAmmoRegenMult().modifyMult(id, AMMO_REGEN_BONUS);
//                stats.getFluxDissipation().modifyPercent(id, FLUX_DISSIPATION_BONUS);
                stats.getTimeMult().modifyMult(id, TIME_DAL);
                //fx
                ship.addAfterimage(
                        new Color(255,0,255,75),
                        0f,
                        0f,
                        ship.getVelocity().x * -1.1f,
                        ship.getVelocity().y * -1.1f,
                        1,
                        0f,
                        0f,
                        0.7f,
                        false,
                        false,
                        false
                );

                if(!ps_hyperframeEMP_activated) {
                    List<CombatEntityAPI> targetNearby = CombatUtils.getEntitiesWithinRange(ship.getLocation(), EMP_RANGE);
                    WeightedRandomPicker<CombatEntityAPI> listTargets = new WeightedRandomPicker<>();
                    for (CombatEntityAPI entity: targetNearby) {
                        if(entity instanceof MissileAPI || entity instanceof FighterWingAPI) {
                            if(ship.getOwner() != entity.getOwner()) {
                                listTargets.add(entity);
                            }
                        }
                    }
                    WeightedRandomPicker<Vector2f> randomPointPicker = new WeightedRandomPicker<>();
                    ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
                    for (BoundsAPI.SegmentAPI s: ship.getExactBounds().getSegments()) {
                        //Global.getCombatEngine().addFloatingText( s.getP1() ,  ".", 60, ps_misc.PROJECT_SOLACE_LIGHT, ship, 0.25f, 0.25f);
                        if(!randomPointPicker.getItems().contains(s.getP1())) {
                            randomPointPicker.add(s.getP1());
                        }
                    }
                    for (int i = 0; i < EMP_COUNT; i++) {
                        if(!listTargets.isEmpty()) {
                            CombatEntityAPI entity = listTargets.pick();
                            if(entity.isExpired() || entity.getHitpoints() < 0) {
                                listTargets.remove(entity);
                            } else {
                                engine.spawnEmpArc(
                                        ship,
                                        randomPointPicker.pick(),
                                        ship,
                                        entity,
                                        DamageType.ENERGY,
                                        EMP_DAMAGE,
                                        0,
                                        10000,
                                        null,
                                        MathUtils.getRandomNumberInRange(5f,10f),
                                        ps_misc.PROJECT_SOLACE,
                                        new Color(255, 255,255, 255)
                                );
                            }
                        } else {
                            SimpleEntity toEntity = new SimpleEntity(MathUtils.getRandomPointInCircle(
                                    ship.getLocation(),
                                    ship.getCollisionRadius()
                            ));
                            Global.getCombatEngine().spawnEmpArcVisual(randomPointPicker.pick(),
                                    ship,
                                    toEntity.getLocation(),
                                    toEntity,
                                    2,
                                    ps_misc.PROJECT_SOLACE,
                                    new Color(255, 255,255, 255)
                            );
                        }
                    }
                    ps_hyperframeEMP_activated = true;
                }

//                customCombatData.put("ps_hyperframeEMP_activated" + id, ps_hyperframeEMP_activated);
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        ps_hyperframeEMP_activated = false;
        stats.getTimeMult().unmodify(id);
//        stats.getMaxSpeed().unmodify(id);
//        stats.getAcceleration().unmodify(id);
//        stats.getEnergyAmmoRegenMult().unmodify(id);
//        stats.getBallisticAmmoRegenMult().unmodify(id);
//        stats.getFluxDissipation().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Time dilation " + TIME_DAL + "x", false);
        }
        return null;
    }
}
