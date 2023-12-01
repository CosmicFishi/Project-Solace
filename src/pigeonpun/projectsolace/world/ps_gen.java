package pigeonpun.projectsolace.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import exerelin.campaign.AllianceManager;
import static pigeonpun.projectsolace.scripts.projectsolaceplugin.*;

import exerelin.campaign.alliances.Alliance;
import pigeonpun.projectsolace.world.systems.ps_ayubia;
import pigeonpun.projectsolace.world.systems.ps_chilka;

import java.util.ArrayList;

public class ps_gen implements SectorGeneratorPlugin {


    //Shorthand function for adding a market
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
                                           int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, ArrayList<String> industries, float tarrif,
                                           boolean freePort, boolean withJunkAndChatter) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tarrif);

        //Adds submarkets
        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        //Adds market conditions
        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        //Add market industries
        for (String industry : industries) {
            newMarket.addIndustry(industry);
        }

        //Sets us to a free port, if we should
        newMarket.setFreePort(freePort);

        //Adds our connected entities, if any
        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, withJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        //Finally, return the newly-generated market
        return newMarket;
    }

    @Override
    public void generate(SectorAPI sector) {

        FactionAPI ps = sector.getFaction("projectsolace");
        FactionAPI enmity = sector.getFaction("enmity");

        //Generate your system
        new ps_chilka().generate(sector);
        new ps_ayubia().generate(sector);

        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("projectsolace");

        if(nexerelinEnabled) {
            createSolaceEnmityAlliance(sector);
        }

        //vanilla factions
        ps.setRelationship(Factions.LUDDIC_CHURCH, -0.1f);
        ps.setRelationship(Factions.LUDDIC_PATH, -0.5f);
        ps.setRelationship(Factions.TRITACHYON, -1f);
        ps.setRelationship(Factions.PERSEAN, -0.2f);
        ps.setRelationship(Factions.PIRATES, -0.5f);
        ps.setRelationship(Factions.INDEPENDENT, 0.5f);
        ps.setRelationship(Factions.DIKTAT, -0.1f);
        ps.setRelationship(Factions.LIONS_GUARD, -0.1f);
        ps.setRelationship(Factions.HEGEMONY, 0.1f);
        ps.setRelationship(Factions.REMNANTS, -0.5f);
        ps.setRelationship("enmity", 1f);
        enmity.setRelationship(Factions.LUDDIC_CHURCH, -0.1f);
        enmity.setRelationship(Factions.LUDDIC_PATH, -0.5f);
        enmity.setRelationship(Factions.TRITACHYON, -1f);
        enmity.setRelationship(Factions.PERSEAN, -0.2f);
        enmity.setRelationship(Factions.PIRATES, -0.5f);
        enmity.setRelationship(Factions.INDEPENDENT, 0.5f);
        enmity.setRelationship(Factions.DIKTAT, -0.1f);
        enmity.setRelationship(Factions.LIONS_GUARD, -0.1f);
        enmity.setRelationship(Factions.HEGEMONY, 0.1f);
        enmity.setRelationship(Factions.REMNANTS, -0.5f);
        //modded factions
        ps.setRelationship("orks", 1.0f);
//        ps.setRelationship("scalartech", 0.4f);
    }

    public void createSolaceEnmityAlliance(SectorAPI sector) {
        FactionAPI solace = sector.getFaction(solace_ID);
        FactionAPI enmity = sector.getFaction(enmity_ID);

        if(Global.getSettings().getBoolean("ps_solaceEnmityAlliance")) {
            Alliance alliance = AllianceManager.createAlliance(enmity_ID, solace_ID, AllianceManager.getBestAlignment(enmity_ID, solace_ID));
            alliance.setName(Global.getSettings().getString("ps_projectsolace", "ps_enmitysolacealliance"));
            alliance.addPermaMember(solace_ID);
            alliance.addPermaMember(enmity_ID);
        }
        enmity.setRelationship(solace_ID, 1f);
        enmity.setRelationship(Factions.PLAYER, Global.getSector().getPlayerFaction().getRelationship(solace_ID));
    }
}
