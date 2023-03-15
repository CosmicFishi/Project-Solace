package pigeonpun.projectsolace.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import pigeonpun.projectsolace.com.ps_misc;
import pigeonpun.projectsolace.world.ps_gen;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ps_chilka {

    public void generate(SectorAPI sector) {
        //Elia Vouvon
        //Stara Maslina
        //jardine juniper
        //Methuselah
        //Tjikko

        StarSystemAPI system = sector.createStarSystem("Chilka");
        system.getLocation().set(-10000, -30000);
        system.setLightColor(ps_misc.PROJECT_SOLACE_LIGHT); // light color in entire system, affects all entities
        system.setEnteredByPlayer(true);
        Misc.setAllPlanetsSurveyed(system, true);
        system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");

        PlanetAPI ChilkaStar = system.initStar("ps_star_chilka", // unique id for this star
                "star_white", // id in planets.json
                800f,        // radius (in pixels at default zoom)
                800, // corona radius, from star edge
                10f, // solar wind burn level
                0.2f, // flare probability
                5f); // cr loss mult

        PlanetAPI Elia_Vouvon = system.addPlanet("ps_planet_elia_vouvon",
                ChilkaStar,
                "Elia Vouvon",
                "jungle",
                40f,
                180f,
                3500f,
                100f);
        //Elia Vouvon.setCustomDescriptionId("vic_phlegethon"); //reference descriptions.csv

        system.addRingBand(Elia_Vouvon, "misc", "rings_dust0", 256f, 1, Color.gray, 600f, 300, 200);

        MarketAPI Elia_vouvon_market = ps_gen.addMarketplace(
                "projectsolace",
                Elia_Vouvon,
                null,
                "Elia Vouvon",
                6,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_6,
                                Conditions.ORGANICS_PLENTIFUL,
                                Conditions.FARMLAND_RICH,
                                Conditions.HABITABLE,
                                Conditions.MILD_CLIMATE,
                                Conditions.LARGE_REFUGEE_POPULATION,
                                Conditions.LOW_GRAVITY,
                                Conditions.REGIONAL_CAPITAL
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.HEAVYBATTERIES,
                                Industries.FARMING,
                                Industries.STARFORTRESS_HIGH,
                                Industries.LIGHTINDUSTRY,
                                Industries.WAYSTATION,
                                Industries.MILITARYBASE,
                                Industries.MEGAPORT,
                                Industries.REFINING
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                true,
                //junk and chatter
                true);
        Elia_vouvon_market.getIndustry(Industries.FARMING).setSpecialItem(new SpecialItemData(Items.SOIL_NANITES, null));
        Elia_vouvon_market.getIndustry(Industries.LIGHTINDUSTRY).setSpecialItem(new SpecialItemData(Items.BIOFACTORY_EMBRYO, null));
        Elia_vouvon_market.setImmigrationIncentivesOn(true);

        PlanetAPI Methuselah = system.addPlanet("ps_planet_methuselah",
                ChilkaStar,
                "Methuselah",
                "terran-eccentric",
                80f,
                180f,
                8000f,
                400f);
        //Methuselah.setCustomDescriptionId("vic_phlegethon"); //reference descriptions.csv

        system.addRingBand(Methuselah, "misc", "rings_asteroids0", 256f, 2, Color.gray, 256f, 500, 200f);

        MarketAPI Methuselah_market = ps_gen.addMarketplace(
                "projectsolace",
                Methuselah,
                null,
                "Methuselah",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.VOLATILES_PLENTIFUL,
                                Conditions.RARE_ORE_RICH,
                                Conditions.ORGANICS_ABUNDANT,
                                Conditions.HABITABLE,
                                Conditions.RUINS_SCATTERED,
                                Conditions.HOT
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MINING,
                                Industries.MEGAPORT,
                                Industries.BATTLESTATION,
                                Industries.WAYSTATION,
                                Industries.MILITARYBASE,
                                Industries.FUELPROD,
                                Industries.HEAVYBATTERIES
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                true,
                //junk and chatter
                true);

        Methuselah_market.getIndustry(Industries.HEAVYBATTERIES).setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR, null));

        PlanetAPI Tjikko = system.addPlanet("ps_planet_tjikko",
                ChilkaStar,
                "Tjikko",
                "lava_minor",
                10f,
                320f,
                5000f,
                300f);
        //Tjikko.setCustomDescriptionId("vic_phlegethon"); //reference descriptions.csv
        system.addRingBand(Tjikko, "misc", "rings_asteroids0", 256f, 2, Color.gray, 256f, 500, 200f);
        system.addRingBand(Tjikko, "misc", "rings_asteroids0", 256f, 2, Color.gray, 256f, 800, 200f);
        system.addAsteroidBelt(Tjikko, 200, 1000, 100, 250, 400, Terrain.ASTEROID_BELT, "Inner Band");

        MarketAPI Tjikko_market = ps_gen.addMarketplace(
                "projectsolace",
                Tjikko,
                null,
                "Tjikko",
                6,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_6,
                                Conditions.ORE_ULTRARICH,
                                Conditions.RARE_ORE_SPARSE,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.VERY_HOT,
                                Conditions.TECTONIC_ACTIVITY
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MINING,
                                Industries.MEGAPORT,
                                Industries.STARFORTRESS_MID,
                                Industries.HIGHCOMMAND,
                                Industries.ORBITALWORKS,
                                Industries.HEAVYBATTERIES,
                                Industries.REFINING
                                )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);
        Tjikko_market.getIndustry(Industries.HIGHCOMMAND).setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE, null));
        Tjikko_market.getIndustry(Industries.ORBITALWORKS).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));


        PlanetAPI Stara_Maslina = system.addPlanet("ps_planet_stara_maslina",
                ChilkaStar,
                "Stara Maslina",
                "barren_venuslike",
                220f,
                300f,
                3800f,
                120f);
        //Tjikko.setCustomDescriptionId("vic_phlegethon"); //reference descriptions.csv

        MarketAPI Stara_Maslina_market = ps_gen.addMarketplace(
                "projectsolace",
                Stara_Maslina,
                null,
                "Stara Maslina",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.LOW_GRAVITY,
                                Conditions.ORE_ABUNDANT,
                                Conditions.THIN_ATMOSPHERE
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.HEAVYINDUSTRY,
                                Industries.HIGHCOMMAND,
                                Industries.REFINING,
                                Industries.BATTLESTATION_HIGH,
                                Industries.GROUNDDEFENSES
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);
        Tjikko_market.getIndustry(Industries.HIGHCOMMAND).setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE, null));
        Tjikko_market.getIndustry(Industries.HEAVYBATTERIES).setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR, null));

        //Asteroid belt
        system.addAsteroidBelt(ChilkaStar, 1000, 11000, 800, 250, 400, Terrain.ASTEROID_BELT, "Inner Band");
        system.addRingBand(ChilkaStar, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 11000 - 200, 400f);
        system.addRingBand(ChilkaStar, "misc", "rings_asteroids0", 256f, 0, Color.gray, 256f, 11000, 450f);
        system.addRingBand(ChilkaStar, "misc", "rings_asteroids0", 256f, 2, Color.gray, 256f, 11000 + 400, 480f);

        system.addAsteroidBelt(ChilkaStar, 800, 7000, 200, 250, 400, Terrain.ASTEROID_BELT, "Inner Band");
        system.addRingBand(ChilkaStar, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 7000, 200f);

        //Ring
        system.addRingBand(ChilkaStar, "misc", "rings_dust0", 256f, 1, Color.gray, 600f, 9000, 200, Terrain.RING, "Outer ring");
        system.addRingBand(ChilkaStar, "misc", "rings_dust0", 256f, 0, Color.gray, 600f, 9500, 220, Terrain.RING, "Outer ring");

        system.addRingBand(ChilkaStar, "misc", "rings_dust0", 256f, 0, Color.gray, 600f, 2000, 220, Terrain.RING, "Outer ring");
        system.addRingBand(ChilkaStar, "misc", "rings_dust0", 256f, 0, Color.gray, 600f, 2500, 220, Terrain.RING, "Outer ring");

        //Asteroid field big
        SectorEntityToken ChilkaAF3 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                        800f, // min radius
                        900f, // max radius
                        24, // min asteroid count
                        48, // max asteroid count
                        8f, // min asteroid radius
                        16f, // max asteroid radius
                        "Big Asteroids Field")); // null for default name
        ChilkaAF3.setCircularOrbit(ChilkaStar, MathUtils.getRandomNumberInRange(50,180), 4000, 200);

        SectorEntityToken ChilkaAF1 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                        800f, // min radius
                        200f, // max radius
                        24, // min asteroid count
                        48, // max asteroid count
                        8f, // min asteroid radius
                        16f, // max asteroid radius
                        "Asteroids Field")); // null for default name
        ChilkaAF1.setCircularOrbit(ChilkaStar, MathUtils.getRandomNumberInRange(240,360), 3000, 200);

        SectorEntityToken ChilkaAF2 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                        800f, // min radius
                        200f, // max radius
                        24, // min asteroid count
                        48, // max asteroid count
                        8f, // min asteroid radius
                        16f, // max asteroid radius
                        "Asteroids Field")); // null for default name
        ChilkaAF2.setCircularOrbit(ChilkaStar, MathUtils.getRandomNumberInRange(240,300), 3000, 200);

        //add Comm relay
        SectorEntityToken MakeshiftRelay = system.addCustomEntity("ps_comm_relay_makeshift", // unique id
                "Outer Chilka Comm Relay", // name - if null, defaultName from custom_entities.json will be used
                "comm_relay_makeshift", // type of object, defined in custom_entities.json
                "projectsolace"); // faction
        MakeshiftRelay.setCircularOrbitPointingDown(ChilkaStar, 180f, 8700f, 265);

        // Nav beacon
        SectorEntityToken NavBeacon = system.addCustomEntity("ps_nav_buoy_makeshift", // unique id
                "Outer Chilka Nav Beacon", // name - if null, defaultName from custom_entities.json will be used
                "nav_buoy_makeshift", // type of object, defined in custom_entities.json
                "projectsolace"); // faction
        NavBeacon.setCircularOrbitPointingDown(ChilkaStar, -90f, 6000f, 105);

        // Sensor relay
        SectorEntityToken SensorRelay = system.addCustomEntity("ps_sensor_array", // unique id
                "Chilka Sensor Relay", // name - if null, defaultName from custom_entities.json will be used
                "sensor_array", // type of object, defined in custom_entities.json
                "projectsolace"); // faction
        SensorRelay.setCircularOrbitPointingDown(ChilkaStar, 100f, 8000f, 280f);

        //Jump point
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint(
                "ps_center_jump",
                "Center System Jump");

        jumpPoint1.setCircularOrbit(system.getEntityById("ps_star_chilka"), 280, 2400, 100f);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();

        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint(
                "ps_fringe_jump",
                "Fringe System Jump");

        jumpPoint2.setCircularOrbit(system.getEntityById("ps_star_chilka"), 100, 10000, 400f);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();

        //
        float radiusAfter2 = StarSystemGenerator.addOrbitingEntities(system, ChilkaStar, StarAge.YOUNG,
                1, 2, // min/max entities to add
                11000, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

        system.addEntity(jumpPoint1);
        system.addEntity(jumpPoint2);

        system.autogenerateHyperspaceJumpPoints(true, false);
        cleanup(system);
    }

    void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}
