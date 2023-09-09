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
import pigeonpun.projectsolace.scripts.projectsolaceplugin;

import java.util.HashSet;
import java.util.Set;

//modified from ApexRelicPlacer, credit to theDragn
public class ps_salvagesplacer implements SectorGeneratorPlugin {
    private static final Set<String> STAR_TYPES = new HashSet<>();
    private static final Set<String> VAGRANT_HULLS_SALVAGE = new HashSet<>();
    public static final Logger log = Global.getLogger(ps_salvagesplacer.class);
    private WeightedRandomPicker<String> hullPicker = new WeightedRandomPicker<>();
    static {
        STAR_TYPES.add("star_neutron");
        VAGRANT_HULLS_SALVAGE.add("");
    }
    @Override
    public void generate(SectorAPI sector) {
        hullPicker.addAll(VAGRANT_HULLS_SALVAGE);
        if(!projectsolaceplugin.ps_vagrantseerGenerateSalvage) return;

        Global.getSector().getMemoryWithoutUpdate().set("$ps_vagrantseerSalvage_Generated", true);
        WeightedRandomPicker<StarSystemAPI> systemPicker = getSpawnSystems(sector);

        for (int i = 0; i < VAGRANT_HULLS_SALVAGE.toArray().length; i++) {
            StarSystemAPI system = systemPicker.pick();
            if (system == null)
            {
                log.log(Level.WARN, "Project Solace: Failed to find a valid system to spawn Vagrant Seer hull, aborting.");
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
            // place the hull
            DerelictShipEntityPlugin.DerelictShipData derelictData = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(hull, ShipRecoverySpecial.ShipCondition.PRISTINE), false);
            SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.REMNANTS, derelictData);
            ship.setFaction("neutral");
            ship.setDiscoverable(true);
            // always put these pretty close to the star rather than possibly scattered way out in the system
            ship.setCircularOrbit(system.getStar(), Misc.random.nextFloat() * 360, system.getStar().getRadius() * 3f, 70);
            log.log(Level.INFO, "Project Solace: spawned " + hull + " in " + system.getName());
        }
    }

    public static WeightedRandomPicker<StarSystemAPI> getSpawnSystems(SectorAPI sector)
    {
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>();
        for (StarSystemAPI system : sector.getStarSystems())
        {
            // system has star, system only has one star, system has the right star, system isn't inhabited
            if (system.getType() == StarSystemGenerator.StarSystemType.SINGLE
                    && system.getStar() != null
                    && STAR_TYPES.contains(system.getStar().getTypeId())
                    && system.isProcgen())
            {
                picker.add(system);
            }
        }
        return picker;
    }
}
