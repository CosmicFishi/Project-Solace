package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import java.util.ArrayList;

//Credits to Ruddygreat for the script!
public class ps_specialitycontroller extends BaseHullMod {

    //this goes on the hidden hullmod
    //a little janky because I'm foing everything inside the script instead of using a deco or w/e to store the state on the ship itself
    String prefix = "some identifier";
    String lasthmod = null;
    private static ArrayList<String> hmods = new ArrayList<>(); //put the hmods in this list in the order you want them to cycle
    {
        hmods.add("ps_specialitydire");
        hmods.add("ps_specialitywatchful");
        hmods.add("ps_specialityzippy");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        ShipVariantAPI variant = stats.getVariant();

        for (String hmodid : variant.getHullMods()) {
            if (!hmodid.contains(prefix)) return;
            lasthmod = hmodid;
        }

        if (!variant.hasHullMod(lasthmod)) {
            if (lasthmod == null) {
                variant.addMod(hmods.get(0));
                variant.addPermaMod(hmods.get(0));
                lasthmod = hmods.get(0);
            }
            int nextIndex = hmods.indexOf(lasthmod) + 1;
            if (nextIndex > hmods.size() - 1) nextIndex = 0;
            variant.addMod(hmods.get(nextIndex));
            lasthmod = hmods.get(nextIndex);
        }
    }
}
