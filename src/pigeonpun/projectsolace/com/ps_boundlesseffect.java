package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import static pigeonpun.projectsolace.hullmods.ps_boundlessbarrier.*;
//credits to Tartiflette for the add hullmod to ship script
public class ps_boundlesseffect {

    public static int calcBoundlessWeaponPoint(ShipVariantAPI variant) {
        if(variant != null) {
            int totalPoint = 0;
            for (String weaponSlot : variant.getFittedWeaponSlots()) {
                WeaponSpecAPI w = variant.getWeaponSpec(weaponSlot);
                if(ps_misc.ENMITY_SPECIAL_WEAPONS_LIST.contains(w.getWeaponId())) {
                    if(w.getSize() == WeaponAPI.WeaponSize.SMALL) {
                        totalPoint += BOUNDLESS_SMALL_POINT;
                    } else if (w.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
                        totalPoint += BOUNDLESS_MEDIUM_POINT;
                    } else if (w.getSize() == WeaponAPI.WeaponSize.LARGE) {
                        totalPoint += BOUNDLESS_LARGE_POINT;
                    }
                }
            }
            return totalPoint;
        }
        return 0;
    }

    public static void checkIfNeedAdding(ShipVariantAPI variant) {
        if(calcBoundlessWeaponPoint(variant) > 0) {
            if(!variant.getHullMods().contains(BOUNDLESSBARRIER_ID)) {
                variant.addMod(BOUNDLESSBARRIER_ID);
            }
        } else {
            if(variant.getHullMods().contains(BOUNDLESSBARRIER_ID)) {
                variant.removeMod(BOUNDLESSBARRIER_ID);
            }
        }
    }
}
