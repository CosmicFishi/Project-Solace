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
        //Generate your system
        new ps_chilka().generate(sector);

        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("projectsolace");

        //vanilla factions
        ps.setRelationship(Factions.LUDDIC_CHURCH, 0f);
        ps.setRelationship(Factions.LUDDIC_PATH, -0.2f);
        ps.setRelationship(Factions.TRITACHYON, -1f);
        ps.setRelationship(Factions.PERSEAN, -1f);
        ps.setRelationship(Factions.PIRATES, -1f);
        ps.setRelationship(Factions.INDEPENDENT, 1f);
        ps.setRelationship(Factions.DIKTAT, -0.5f);
        ps.setRelationship(Factions.LIONS_GUARD, -0.5f);
        ps.setRelationship(Factions.HEGEMONY, 0.2f);
        ps.setRelationship(Factions.REMNANTS, -0.5f);
        //modded factions
        ps.setRelationship("orks", 1.0f);
        ps.setRelationship("scalartech", 0.4f);
    }
}
