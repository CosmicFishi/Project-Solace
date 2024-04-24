package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.util.HashMap;

import static pigeonpun.projectsolace.hullmods.ps_boundlessbarrier.*;
//credits to Tartiflette for the add hullmod to ship script
public class ps_boundlesseffect {
    public static int calcTotalBoundlessWeaponPoint(ShipVariantAPI variant) {
        if(variant != null) {
            int totalPoint = 0;
            for (String weaponSlot : variant.getFittedWeaponSlots()) {
                WeaponSpecAPI w = variant.getWeaponSpec(weaponSlot);
                totalPoint += getWeaponSizePoint(w.getWeaponId());
            }
            return totalPoint;
        }
        return 0;
    }
    public static int getWeaponSizePoint(String weaponId) {
        WeaponSpecAPI w = Global.getSettings().getWeaponSpec(weaponId);
        if(w != null) {
            if(ps_misc.ENMITY_SPECIAL_WEAPONS_LIST.contains(w.getWeaponId())) {
                if(w.getSize() == WeaponAPI.WeaponSize.SMALL) {
                    return (int) BOUNDLESS_SMALL_POINT;
                } else if (w.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
                    return (int) BOUNDLESS_MEDIUM_POINT;
                } else if (w.getSize() == WeaponAPI.WeaponSize.LARGE) {
                    return (int) BOUNDLESS_LARGE_POINT;
                }
            }
        }
        return 0;
    }

    public static void checkIfNeedAdding(ShipVariantAPI variant) {
        if(calcTotalBoundlessWeaponPoint(variant) > 0) {
            if(!variant.getHullMods().contains(BOUNDLESSBARRIER_ID)) {
                variant.addMod(BOUNDLESSBARRIER_ID);
            }
        } else {
            if(variant.getHullMods().contains(BOUNDLESSBARRIER_ID)) {
                variant.removeMod(BOUNDLESSBARRIER_ID);
            }
        }
    }
    public static HashMap<String, boundlessEffectData> getBoundlessEffectData(ShipVariantAPI variant) {
        HashMap<String, boundlessEffectData> list = new HashMap<>();
        if(variant != null) {
            for (String weaponSlot : variant.getFittedWeaponSlots()) {
                WeaponSpecAPI w = variant.getWeaponSpec(weaponSlot);
                if(ps_misc.ENMITY_SPECIAL_WEAPONS_LIST.contains(w.getWeaponId())) {
                    if(list.get(w.getWeaponId()) != null) {
                        list.get(w.getWeaponId()).setWeaponCount(list.get(w.getWeaponId()).getWeaponCount() + 1);
                    } else {
                        list.put(w.getWeaponId(), new boundlessEffectData(w, 1));
                    }
                }
            }
        }
        return list;
    }
    public static class boundlessEffectData {
        public WeaponSpecAPI weapon;
        private int weaponCount;
        private int weaponPoint = 0;
        public boundlessEffectData(WeaponSpecAPI weapon, int weaponCount) {
            this.weapon = weapon;
            setWeaponCount(weaponCount);
        }

        public int getWeaponCount() {
            return weaponCount;
        }

        public void setWeaponCount(int weaponCount) {
            this.weaponCount = weaponCount;
            this.weaponPoint = ps_boundlesseffect.getWeaponSizePoint(this.weapon.getWeaponId()) * this.weaponCount;
        }

        public int getWeaponPoint() {
            return weaponPoint;
        }
    }
}
