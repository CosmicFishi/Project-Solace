package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.Map;

public class ps_missilefiend extends BaseHullMod {

    public static String MR_DATA_KEY = "ps_reload_data_key";
    public final float MISSILE_SPEED_BONUS = 5f;
    public final float MISSILE_RELOAD_BASE_TIME = 40f;
    public float MISSILE_RELOAD_BASE_ACTUAL = 0;
    public final float MISSILE_HEALTH_PER_MOUNT_BONUS = 5f;
    public final float RELOAD_SIZE = 20f;
    public final float MISSILE_AMMO_DECREASE = 40f;
    private final IntervalUtil MISSILE_RELOAD_TIMER = new IntervalUtil(40f, 40f);

    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }

//        float total_bonus_health = 0;
//        for (WeaponAPI weapon: ship.getAllWeapons()) {
//            if (weapon.isDecorative() ) continue;
//            if(weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.MISSILE) {
//                if(weapon.getAmmoPerSecond() == 0f) {
//                    total_bonus_health += MISSILE_HEALTH_PER_MOUNT_BONUS;
//                }
//            }
//        }
//        MISSILE_RELOAD_BASE_ACTUAL = MISSILE_RELOAD_BASE_TIME - total_bonus_health;

        //Bless you PureTilt
//        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
//        String id = ship.getId();
//        IntervalUtil timer = new IntervalUtil(MISSILE_RELOAD_BASE_ACTUAL, MISSILE_RELOAD_BASE_ACTUAL);
//        if (customCombatData.get("ps_missilefiend_reload_timer" + id) instanceof IntervalUtil) {
//            timer = (IntervalUtil) customCombatData.get("ps_missilefiend_reload_timer" + id);
//        }

//        timer.advance(amount);
//        if (timer.intervalElapsed()) {

        MISSILE_RELOAD_TIMER.advance(amount);
        if (MISSILE_RELOAD_TIMER.intervalElapsed()) {
            //engine.addFloatingText(ship.getLocation(), "reloading", 12f, ps_misc.PROJECT_SOLACE_LIGHT, ship, 1f, 1f);
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getType() != WeaponAPI.WeaponType.MISSILE) continue;

                if (w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {

                    int reload_amount = (int) Math.round(w.getAmmo() * RELOAD_SIZE / 100);
                    if(reload_amount < 1) {
                        reload_amount = 1;
                    }
                    w.setAmmo(reload_amount);
                }
            }
        }
//        customCombatData.put("ps_missilefiend_reload_timer" + id, timer);

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("ps_solacecore_debug_time", "graphics/icons/hullsys/temporal_shell.png", "MISSILE BASE TIME DEBUG", Math.round(MISSILE_RELOAD_BASE_ACTUAL) + "s", false);
        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.MOUNT_UNIVERSAL;
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        //The Solace Core.
        LabelAPI label = tooltip.addPara("With the overflowing amount of love for missiles, the Barking class ship has received a blessing from one of our brilliant scientists to further improve its combat ability by granting the power to regenerate missiles ammunition as well as increasing missiles speed or health, based on the type of missiles equipped.", opad, h);

        label = tooltip.addPara("However, due to the limit of current technology, the blessing comes with a cost, all the ammo count will be reduced by a large amount in order to not overheat the system. ", opad, h);

        //bonus
        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        //Reload all missile weapon ammo by %s or 1, which ever is higher
        label = tooltip.addPara("Reload all %s by %s or 1, which ever is higher.", opad, h,
                "missiles ammo" ,"" + Math.round(RELOAD_SIZE) + "%");
        label.setHighlight("missiles ammo" ,"" + Math.round(RELOAD_SIZE) + "%");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, good);

        //reduce all missile ammo
        label = tooltip.addPara("Reduce all %s by %s unless the ammo count is 1.", opad, h,
                "missiles ammo" ,"" + Math.round(MISSILE_AMMO_DECREASE) + "%");
        label.setHighlight("missiles ammo" ,"" + Math.round(MISSILE_AMMO_DECREASE) + "%");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, bad);

        tooltip.addSectionHeading("Missile slots", Alignment.MID, opad);

        //Increase missiles speed by %s for each missile mount with reload time.
        label = tooltip.addPara("Increase %s by %s for each %s.", opad, h,
                "missiles speed" ,"" + Math.round(MISSILE_SPEED_BONUS) + "%", "missile with reload time");
        label.setHighlight("missiles speed" ,"" + Math.round(MISSILE_SPEED_BONUS) + "%", "missile with reload time");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, good, Misc.MOUNT_MISSILE);

        //Increase missiles health time by %s for each missile mount without reload time.
        label = tooltip.addPara("Increase %s by %s for each %s.", opad, h,
                "missiles health" ,"" + Math.round(MISSILE_HEALTH_PER_MOUNT_BONUS) + "%", "missile without reload time");
        label.setHighlight("missiles health" ,"" + Math.round(MISSILE_HEALTH_PER_MOUNT_BONUS) + "%", "missile without reload time");
        label.setHighlightColors(ps_misc.PROJECT_SOLACE_LIGHT, good, Misc.MOUNT_MISSILE);

        //bonus
        tooltip.addSectionHeading("Current Missile slots Bonuses", Alignment.MID, opad);

        float total_bonus_speed = 0;
        float total_bonus_health = 0;
        for (WeaponAPI weapon: ship.getAllWeapons()) {
            if (weapon.isDecorative() ) continue;
            if(weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.MISSILE) {
                if(weapon.getAmmoPerSecond() != 0f) {
                    total_bonus_speed += MISSILE_SPEED_BONUS;
                } else {
                    total_bonus_health += MISSILE_HEALTH_PER_MOUNT_BONUS;
                }
            }
        }
        label = tooltip.addPara("Reload time: %s", opad, h,
                "" + Math.round(MISSILE_RELOAD_BASE_TIME) + "s");
        label.setHighlight("" + Math.round(MISSILE_RELOAD_BASE_TIME) + "s");
        label.setHighlightColors(h);
        
        label = tooltip.addPara("Missiles speed: %s", opad, h,
                "" + Math.round(total_bonus_speed) + "%");
        label.setHighlight("" + Math.round(total_bonus_speed) + "%");
        label.setHighlightColors(h);
        
        label = tooltip.addPara("Missiles health: %s", opad, h,
                "" + Math.round(total_bonus_health) + "%");
        label.setHighlight("" + Math.round(total_bonus_health) + "%");
        label.setHighlightColors(h);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float total_bonus_speed = 0;
        float total_bonus_health = 0;
        for (String weaponSlot : stats.getVariant().getFittedWeaponSlots()) {
            WeaponSpecAPI weapon = stats.getVariant().getWeaponSpec(weaponSlot);
            if(stats.getVariant().getSlot(weaponSlot).getWeaponType() == WeaponAPI.WeaponType.MISSILE) {
                if(weapon.getAmmoPerSecond() != 0f) {
                    total_bonus_speed += MISSILE_SPEED_BONUS;
                } else {
                    total_bonus_health += MISSILE_HEALTH_PER_MOUNT_BONUS;
                }
            }
        }
        stats.getMissileMaxSpeedBonus().modifyPercent(id, total_bonus_speed);
        stats.getMissileHealthBonus().modifyPercent(id, total_bonus_health);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.getType() != WeaponAPI.WeaponType.MISSILE) continue;

            if (w.getMaxAmmo() > 1) {
                w.setMaxAmmo((int) (w.getAmmo() * (100 - MISSILE_AMMO_DECREASE) / 100));
            }
        }
    }

}
