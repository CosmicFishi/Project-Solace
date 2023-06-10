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

    //increase damage by certain amount
    //convert all energy or ballistic projectile weapons to pd, reducing base range that is above 500 units by 50%
    //Shield on enable EMP sparking, the higher the hard flux, the more damage the EMP do
    //Speciality: increase flux cost - increase damage, increase flux eff - reduce max speed, Increase maneuver - reduce armor
    //Dire - Watchful - Zippy
    private static final float DAMAGE_BEAM_BONUS = 40f;
    private static final float RANGE_BASE_PROJECTILE_START_REDUCE = 500f;
    private static final float RANGE_BASE_PROJECTILE_REDUCE_BY_MULT = 0.5f;
    private static final float
            EMP_DAMAGE_FRIGATE = 100f,
            EMP_DAMAGE_DESTROYER = 150f,
            EMP_DAMAGE_CRUISER = 200f,
            EMP_DAMAGE_CAPITAL = 250f;
    private static final float EMP_DAMAGE_BONUS_HARD_FLUX_CAP_AT = 0.8f;
    private static final float EMP_DAMAGE_MAX_BONUS_HARD_FLUX = 50f;
    private static final IntervalUtil EMP_TIMER = new IntervalUtil(1f, 1.5f);
    private static final float EMP_RANGE = 1500f;
    private static final IntervalUtil HIT_PARTICLE_TIMER = new IntervalUtil(0.05f, 0.2f);

    //todo: change it to do real high damage but long cool down
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
                for (ShipAPI enemyShip: CombatUtils.getShipsWithinRange(ship.getLocation(), EMP_RANGE)) {
                    if(enemyShip.isAlive() && ship.getOwner() != enemyShip.getOwner()) {
                        Vector2f spawnEMPPoint = MathUtils.getPointOnCircumference(
                                ship.getLocation(),
                                ship.getShield().getRadius(),
                                MathUtils.getRandomNumberInRange(startShieldAngle, endShieldAngle)
                        );
                        float enemyAngle = VectorUtils.getAngle(ship.getLocation(), enemyShip.getLocation());
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
                                    enemyShip,
                                    DamageType.FRAGMENTATION,
                                    EMPdamage,
                                    EMPdamage,
                                    3000,
                                    null,
                                    1,
                                    ps_misc.ENMITY_SHIELD_EMP_FRINGE,
                                    ps_misc.ENMITY_SHIELD_EMP_CORE);
                            Global.getSoundPlayer().playSound("ps_emp_shout", 1f, 1f, spawnEMPPoint, new Vector2f(0, 0));
                        }
                    }
                };
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
        Color good = Misc.getPositiveHighlightColor();

        //Incense
        LabelAPI label = tooltip.addPara("A dust-like matter called Incense found by accident when a small piece of Solace crystal slipped off one of our scientist's hands and shattered on the ground. However, unlike other crystal dust, the area where the crystal shatters seems to be spreading out to fill up certain areas and within those areas, time seems to be moving differently. Further inspection and testing after this incident shows that the matter has some extraordinary quirks", opad, h, "");

        //3 effects
        //label = tooltip.addPara("First is the tendency to spread out to a large area, if the area is damaged by a moving projectile, the dust will disburse out then slowly form back to fill it up", opad, h, "");
        //label = tooltip.addPara("The second unique feature is the ability to release a huge amount of energy when Incense???s covering surface has been damaged to a certain point, after the initial impact, the matter seems to be bonding the surface back to a certain stage, this feature alone is a breakthrough for the Solace ship composition.", opad, h, "");
        //label = tooltip.addPara("The final specialty is the time manipulation, it seems that the object that Incense covers, depending on the density, can move slower or faster in time, this is also another key factor to Solace ship lineup.", opad, h, "");

        //bonus
        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        //incense
        //Time dal
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBeamWeaponDamageMult().modifyPercent(id, DAMAGE_BEAM_BONUS);
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
