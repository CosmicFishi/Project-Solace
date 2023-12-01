package pigeonpun.projectsolace.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicCampaign;
import pigeonpun.projectsolace.scripts.projectsolaceplugin;

import java.util.HashSet;
import java.util.Set;

//modified from ApexRelicPlacer, credit to theDragn
public class ps_salvagesplacer implements SectorGeneratorPlugin {
    private static final Set<String> VAGRANT_STAR_TYPES = new HashSet<>();
    private static final Set<String> VAGRANT_HULLS_SALVAGE = new HashSet<>();
    private static final Set<String> BYGONEDAME_STAR_TYPES = new HashSet<>();
    private static final Set<String> BYGONEDAME_HULLS_SALVAGE = new HashSet<>();
    public static final Logger log = Global.getLogger(ps_salvagesplacer.class);
    static {
        VAGRANT_STAR_TYPES.add("star_neutron");
        VAGRANT_HULLS_SALVAGE.add("ps_alitha_relic");

        BYGONEDAME_STAR_TYPES.add("star_red_giant");
        BYGONEDAME_HULLS_SALVAGE.add("ps_oculatus_attack");
    }
    @Override
    public void generate(SectorAPI sector) {
        if(projectsolaceplugin.ps_vagrantseerGenerateSalvage && !Global.getSector().getMemoryWithoutUpdate().contains(projectsolaceplugin.ps_vagrantseerSalvage_Generated)) {
            Global.getSector().getMemoryWithoutUpdate().set(projectsolaceplugin.ps_vagrantseerSalvage_Generated, true);
            placeHulls(sector, VAGRANT_HULLS_SALVAGE, ShipRecoverySpecial.ShipCondition.BATTERED, VAGRANT_STAR_TYPES, "Vagrant Seer");
        };
        if(projectsolaceplugin.ps_bygonedameGenerateSalvage && !Global.getSector().getMemoryWithoutUpdate().contains(projectsolaceplugin.ps_bygonedameSalvage_Generated)) {
            Global.getSector().getMemoryWithoutUpdate().set(projectsolaceplugin.ps_bygonedameSalvage_Generated, true);
            placeHulls(sector, BYGONEDAME_HULLS_SALVAGE, ShipRecoverySpecial.ShipCondition.BATTERED, BYGONEDAME_STAR_TYPES, "Bygone Dame");
        }
    }

    public void placeHulls(SectorAPI sector, Set<String> hulls, ShipRecoverySpecial.ShipCondition shipCondition, Set<String> startTypes, String msg) {
        WeightedRandomPicker<String> hullPicker = new WeightedRandomPicker<>();
        hullPicker.addAll(hulls);
        WeightedRandomPicker<StarSystemAPI> systemPicker = getSpawnSystems(sector, startTypes);

        for (int i = 0; i < hulls.toArray().length; i++) {
            StarSystemAPI system = systemPicker.pick();
            if (system == null)
            {
                log.log(Level.WARN, "Project Solace: Failed to find a valid system to spawn " + msg +" hull, aborting.");
                return;
            }
            // pick the hull and remove it from the list
            if (hullPicker.isEmpty()) break;
            String hull = hullPicker.pickAndRemove();
            if (hull == null || hull.equals(""))
            {
                log.log(Level.WARN, "somehow picked null for the ship hull, skipping");
                continue;
            }
            MagicCampaign.createDerelict(
                    hull,
                    shipCondition,
                    true,
                    -1,
                    true,
                    system.getStar(), Misc.random.nextFloat() * 360, system.getStar().getRadius() * 3f, 70f);
            log.log(Level.INFO, "Project Solace: spawned " + hull + " in " + system.getName());
        }
    }

    public static WeightedRandomPicker<StarSystemAPI> getSpawnSystems(SectorAPI sector, Set<String> starTypes)
    {
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>();
        for (StarSystemAPI system : sector.getStarSystems())
        {
            // system has star, system only has one star, system has the right star, system isn't inhabited
            if (system.getType() == StarSystemGenerator.StarSystemType.SINGLE
                    && system.getStar() != null
                    && starTypes.contains(system.getStar().getTypeId())
                    && system.isProcgen())
            {
                picker.add(system);
            }
        }
        return picker;
    }
}
