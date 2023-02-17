package data.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

public interface PS_FleetGenerator {

    public final static class GeneratorFleetTypes {

        static PS_FleetGenerator RAIDERS = new RaidersGen();
        static PS_FleetGenerator PATROL = new PatrolGen();
        static PS_FleetGenerator HUNTERS = new HuntersGen();
        static PS_FleetGenerator WAR = new WarGen();
        static PS_FleetGenerator DEFENSE = new DefenseGen();
        static PS_FleetGenerator CONVOY = new ConvoyGen();
        static PS_FleetGenerator BLOCKADE = new BlockadeGen();
        static PS_FleetGenerator INVASION = new InvasionGen();

        private final PS_FleetGenerator gen;

        private GeneratorFleetTypes(PS_FleetGenerator gen) {
            this.gen = gen;
        }

        FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            return gen.generate(api, side, faction, qf, opBonus, avgSMods, maxPts, seed, autoshit);
        }
    }

//    enum GeneratorFleetTypes {
//        RAIDERS(new RaidersGen()),
//        PATROL(new PatrolGen()),
//        HUNTERS(new HuntersGen()),
//        WAR(new WarGen()),
//        DEFENSE(new DefenseGen()),
//        CONVOY(new ConvoyGen()),
//        BLOCKADE(new BlockadeGen()),
//        INVASION(new InvasionGen());
//
//        private final PS_FleetGenerator gen;
//
//        private GeneratorFleetTypes(PS_FleetGenerator gen) {
//            this.gen = gen;
//        }
//
//        FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
//            return gen.generate(api, side, faction, qf, opBonus, avgSMods, maxPts, seed, autoshit);
//        }
//    }

    FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit);

    static class RaidersGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 4);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.75f, // combatPts
                    maxPts * 0.25f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class PatrolGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 4);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class HuntersGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 5);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class WarGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 6);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class DefenseGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 6);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class ConvoyGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 4);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.6f, // combatPts
                    maxPts * 0.1f, // freighterPts
                    maxPts * 0.1f, // tankerPts
                    maxPts * 0.1f, // transportPts
                    maxPts * 0.1f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class BlockadeGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 5);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.8f, // combatPts
                    maxPts * 0.1f, // freighterPts
                    0f, // tankerPts
                    maxPts * 0.1f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class InvasionGen implements PS_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 6);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.7f, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    maxPts * 0.3f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return data.missions.PS_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }
}
