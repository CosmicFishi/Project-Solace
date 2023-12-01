package pigeonpun.projectsolace.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.*;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicCampaign;

import java.util.ArrayList;
import java.util.List;

public class ps_vagrantseergen implements SectorGeneratorPlugin {

    public static final Logger log = Global.getLogger(ps_vagrantseergen.class);
    @Override
    public void generate(SectorAPI sector) {
        addExplorationContent();
    }
    public static void addExplorationContent() {
        spawnSarpedon();
    }

    private static void spawnSarpedon() {
        List<String> themesLookingFor = new ArrayList<>();
        themesLookingFor.add(Tags.THEME_REMNANT_RESURGENT);
        themesLookingFor.add(Tags.THEME_REMNANT_SECONDARY);
        themesLookingFor.add(Tags.THEME_REMNANT);

        List<String> themesAvoid = new ArrayList<>();
        themesAvoid.add("theme_already_colonized");
        themesAvoid.add("theme_hidden");

//        List<String> entitiesLookingFor = new ArrayList<>();
//        entitiesLookingFor.add(Tags.STATION);
//        entitiesLookingFor.add(Tags.DEBRIS_FIELD);
//        entitiesLookingFor.add(Tags.WRECK);
//        entitiesLookingFor.add(Tags.SALVAGEABLE);

        SectorEntityToken targetLocation = MagicCampaign.findSuitableTarget(
                null,
                null,
                null,
                themesLookingFor,
                themesAvoid,
                null,
                false,
                true,
                true);
        if(targetLocation == null) {
            log.info("No suitable system found to spawn Sarpedon.");
        } else {
            log.info("Found " + targetLocation.getStarSystem().getName() +" system, spawning Sarpedon.");;

            PersonAPI sarpedonCommander = MagicCampaign.createCaptainBuilder(Factions.REMNANTS)
                    .setAICoreType(Commodities.ALPHA_CORE)
                    .setPersonality(Personalities.AGGRESSIVE)
                    .setSkillPreference(OfficerManagerEvent.SkillPickPreference.YES_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE)
                    .setLevel(10)
                    .setIsAI(true)
                    .setRankId(Ranks.SPACE_COMMANDER)
                    .setPostId(Ranks.POST_FLEET_COMMANDER)
                    .create();
            CampaignFleetAPI sarpedonFleet = MagicCampaign.createFleetBuilder()
                    .setCaptain(sarpedonCommander)
                    .setFleetFaction(Factions.REMNANTS)
                    .setAssignment(FleetAssignment.PATROL_SYSTEM)
                    .setIsImportant(false)
                    .setTransponderOn(false)
                    .setMinFP(300)
                    .setFlagshipAlwaysRecoverable(true)
                    .setAssignmentTarget(targetLocation)
                    .setFlagshipVariant("ps_sarpedon_assault")
                    .setFleetName("Vagrant Legacy")
                    .create();

            sarpedonFleet.setDiscoverable(true);
            sarpedonFleet.addTag(Tags.NEUTRINO);
            sarpedonFleet.addTag(Tags.NEUTRINO_HIGH);
            //sarpedonFleet.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
            sarpedonFleet.getFlagship().getVariant().addTag(Tags.SHIP_UNIQUE_SIGNATURE);
            sarpedonFleet.getMemoryWithoutUpdate().set("$ps_sarpedon", true);
            sarpedonFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
            sarpedonFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
            sarpedonFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
        }
    }
}
