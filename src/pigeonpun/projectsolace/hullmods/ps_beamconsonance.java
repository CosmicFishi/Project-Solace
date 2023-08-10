package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.HighScatterAmp;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicUI;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ps_beamconsonance extends BaseHullMod {

    //increase damage by certain amount, reduce range
    //convert all energy or ballistic projectile weapons to pd, reducing base range that is above 500 units by 50%
    //Shield on enable EMP sparking to random 3 missiles
    private static final float DAMAGE_BEAM_BONUS = 20f;
    private static final float RANGE_BEAM_REDUCE = 30f;
    private static final float RANGE_BASE_PROJECTILE_START_REDUCE = 500f;
    private static final float RANGE_BASE_PROJECTILE_REDUCE_BY_MULT = 0.5f;
    private static final float EMP_SPARK_COUNT = 3;
    private static final float
            EMP_DAMAGE_FRIGATE = 100f,
            EMP_DAMAGE_DESTROYER = 150f,
            EMP_DAMAGE_CRUISER = 200f,
            EMP_DAMAGE_CAPITAL = 250f;
    private static final IntervalUtil EMP_TIMER = new IntervalUtil(1f, 1.5f);
    private static final float EMP_RANGE = 1500f;
    private static final IntervalUtil HIT_PARTICLE_TIMER = new IntervalUtil(0.05f, 0.2f);
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;

        }
        if(ship.getShield() != null && ship.getShield().isOn()) {
            EMP_TIMER.advance(amount);
            HIT_PARTICLE_TIMER.advance(amount);
            float startShieldAngle = ship.getShield().getFacing() - ship.getShield().getActiveArc() / 2;
            float endShieldAngle = ship.getShield().getFacing() + ship.getShield().getActiveArc() / 2;
            if(HIT_PARTICLE_TIMER.intervalElapsed()) {
                Vector2f spawnHitParticlePoint = MathUtils.getPointOnCircumference(
                        ship.getLocation(),
                        MathUtils.getRandomNumberInRange(ship.getShield().getRadius() + 10f, ship.getShield().getRadius() + 50f),
                        MathUtils.getRandomNumberInRange(startShieldAngle, endShieldAngle)
                );
                engine.addSmoothParticle(spawnHitParticlePoint, (Vector2f) VectorUtils.getDirectionalVector(spawnHitParticlePoint, ship.getLocation()).scale(15f), 7f, 1f, 2f, ps_misc.ENMITY_SHIELD_PARTICLE);
            }
            if(EMP_TIMER.intervalElapsed()) {
                List<MissileAPI> listMissiles = CombatUtils.getMissilesWithinRange(ship.getLocation(), EMP_RANGE);
                WeightedRandomPicker<MissileAPI> missilesToFire = new WeightedRandomPicker<>();
                for (MissileAPI missile: listMissiles) {
                    if(!missile.isFading() && !missile.isDecoyFlare() && (ship.getOwner() != missile.getOwner())) {
                        missilesToFire.add(missile, MathUtils.getDistance(ship.getLocation(), missile.getLocation()));
                    }
                }
                int i = 0;
                while(!missilesToFire.isEmpty() && i < EMP_SPARK_COUNT) {
                    MissileAPI missile = missilesToFire.pick();
                    Vector2f spawnEMPPoint = MathUtils.getPointOnCircumference(
                            ship.getLocation(),
                            ship.getShield().getRadius(),
                            MathUtils.getRandomNumberInRange(startShieldAngle, endShieldAngle)
                    );
                    float enemyAngle = VectorUtils.getAngle(ship.getLocation(), missile.getLocation());
                    if(enemyAngle > startShieldAngle || enemyAngle < endShieldAngle) {
                        float EMPdamage = 0;
                        switch (ship.getHullSize()) {
                            case FRIGATE:
                                EMPdamage = EMP_DAMAGE_FRIGATE;
                                break;
                            case DESTROYER:
                                EMPdamage = EMP_DAMAGE_DESTROYER;
                                break;
                            case CRUISER:
                                EMPdamage = EMP_DAMAGE_CRUISER;
                                break;
                            case CAPITAL_SHIP:
                                EMPdamage = EMP_DAMAGE_CAPITAL;
                                break;
                        }
                        Global.getCombatEngine().spawnEmpArcPierceShields(ship,
                                spawnEMPPoint,
                                null,
                                missile,
                                DamageType.FRAGMENTATION,
                                EMPdamage,
                                0,
                                3000,
                                null,
                                1,
                                ps_misc.ENMITY_SHIELD_EMP_FRINGE,
                                ps_misc.ENMITY_SHIELD_EMP_CORE);
                        Global.getSoundPlayer().playSound("ps_emp_shout", 1f, 1f, spawnEMPPoint, new Vector2f(0, 0));
                    }
                    i++;
                }
            }
        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.MOUNT_UNIVERSAL;
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getHighlightColor();

        LabelAPI label = tooltip.addPara("Hardened and refined to the peak of innovation. By the technology found only at the Ayubia system, the Enmity have gained new knowledge about beam weapons that have never been seen before. Fueling the beam with minerals deep under the caves in Kearsarge resulted in the increment in the intensity of one can fire, making its piercing power stronger than ever before.", opad, h, "");
        label = tooltip.addPara("But as with every alteration, it comes with a price. Due to the amount of flux required to push those beam weapons to full capacity, any energy or ballistic projectile weapon will be converted into point defense instead of the usual strike role and have their base range reduced.", opad, h);
        label = tooltip.addPara("A side effect or some may say blessing from this upgrade is that the ship’s shield has the ability to knock down enemies’s missiles with its EMP arc, as a way to discharge some of the overwhelming power that the innovation provided. But it only appears whenever the shield is enabled.", opad, h, "");

        //bonus
        tooltip.addSectionHeading("Effects", Alignment.MID, opad);
        label = tooltip.addPara("- Increase beam weapon damage by: %s.", opad, h,
                "" + Math.round(DAMAGE_BEAM_BONUS) + "%");
        label.setHighlight("" + Math.round(DAMAGE_BEAM_BONUS) + "%");
        label.setHighlightColors(good);

        label = tooltip.addPara("- Reduce beam weapon range by: %s.", opad, h,
                "" + Math.round(RANGE_BEAM_REDUCE) + "%");
        label.setHighlight("" + Math.round(RANGE_BEAM_REDUCE) + "%");
        label.setHighlightColors(bad);

        label = tooltip.addPara("- Convert projectile energy or ballistic weapon to point defend, if they are above %s. reduce base range by %s", opad, h,
                "" + Math.round(RANGE_BASE_PROJECTILE_START_REDUCE) + "u", "" + Math.round(RANGE_BASE_PROJECTILE_REDUCE_BY_MULT * 100) + "%");
        label.setHighlight("" + Math.round(RANGE_BASE_PROJECTILE_START_REDUCE) + "u", "" + Math.round(RANGE_BASE_PROJECTILE_REDUCE_BY_MULT * 100) + "%");
        label.setHighlightColors(bad, bad);

        label = tooltip.addPara("- EMP will periodically discharge when shield is up, dealing %s fragmentation damage to missiles", opad, h,
                "" + Math.round(EMP_DAMAGE_FRIGATE) + "/" + Math.round(EMP_DAMAGE_DESTROYER) + "/" + Math.round(EMP_DAMAGE_CRUISER) + "/" + Math.round(EMP_DAMAGE_CAPITAL) + ""
            );
        label.setHighlight("" + Math.round(EMP_DAMAGE_FRIGATE) + "/" + Math.round(EMP_DAMAGE_DESTROYER) + "/" + Math.round(EMP_DAMAGE_CRUISER) + "/" + Math.round(EMP_DAMAGE_CAPITAL) + "");
        label.setHighlightColors(good);

        tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
        tooltip.addPara("Since the base range is increased, this modifier"
                + "is affected by percentage modifiers from other hullmods and skills.", opad);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBeamWeaponDamageMult().modifyPercent(id, DAMAGE_BEAM_BONUS);
        stats.getBeamWeaponRangeBonus().modifyMult(id, (100 - RANGE_BEAM_REDUCE) / 100);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new EnmityRangeMod());

        for(WeaponAPI weapon: ship.getAllWeapons()) {
            if (!weapon.isBeam() && (weapon.getType() == WeaponAPI.WeaponType.ENERGY || weapon.getType() == WeaponAPI.WeaponType.BALLISTIC)) {
                weapon.ensureClonedSpec();
                weapon.setPD(true);
            }
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
    public static class EnmityRangeMod implements WeaponBaseRangeModifier {
        public EnmityRangeMod() {
        }
        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0;
        }
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (!weapon.isBeam() && (weapon.getType() == WeaponAPI.WeaponType.ENERGY || weapon.getType() == WeaponAPI.WeaponType.BALLISTIC)) {
                float range = weapon.getSpec().getMaxRange();
                if (range < RANGE_BASE_PROJECTILE_START_REDUCE) return 0;

                float past = range - RANGE_BASE_PROJECTILE_START_REDUCE;
                float penalty = past * (1f - RANGE_BASE_PROJECTILE_REDUCE_BY_MULT);
                return -penalty;
            }
            return 0f;
        }
    }
}
