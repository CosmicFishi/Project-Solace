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
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import pigeonpun.projectsolace.com.ps_misc;
import pigeonpun.projectsolace.world.ps_gen;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ps_ayubia {

    //todo: Missing Heg planet
    public void generate(SectorAPI sector) {
        //Bowditch
        //Kearsarge

        StarSystemAPI system = sector.createStarSystem("Ayubia");
        system.getLocation().set(-14500, -42500);
//        system.setLightColor(ps_misc.PROJECT_SOLACE_LIGHT); // light color in entire system, affects all entities
        system.setEnteredByPlayer(true);
        Misc.setAllPlanetsSurveyed(system, true);
        system.setBackgroundTextureFilename("graphics/backgrounds/background1.jpg");

        PlanetAPI AyubiaStar = system.initStar("ps_star_ayubia", // unique id for this star
                "star_red_giant", // id in planets.json
                1000f,        // radius (in pixels at default zoom)
                500, // corona radius, from star edge
                10f, // solar wind burn level
                0.8f, // flare probability
                5f); // cr loss mult

        PlanetAPI Kearsarge = system.addPlanet("ps_planet_kearsarge",
                AyubiaStar,
                "Kearsarge",
                "terran-eccentric",
                40f,
                180f,
                2000f,
                100f);
        //Elia Vouvon.setCustomDescriptionId("vic_phlegethon"); //reference descriptions.csv

        MarketAPI Kearsarge_market = ps_gen.addMarketplace(
                "enmity",
                Kearsarge,
                null,
                "Kearsarge",
                7,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_7,
                                Conditions.ORGANICS_COMMON,
                                Conditions.FARMLAND_BOUNTIFUL,
                                Conditions.HABITABLE,
                                Conditions.MILD_CLIMATE,
                                Conditions.REGIONAL_CAPITAL,
                                Conditions.LOW_GRAVITY,
                                Conditions.RUINS_VAST
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
                                Industries.HIGHCOMMAND,
                                Industries.MEGAPORT,
                                Industries.COMMERCE
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                true,
                //junk and chatter
                true);

        PlanetAPI Bowditch = system.addPlanet("ps_planet_Bowditch",
                AyubiaStar,
                "Bowditch",
                "desert",
                80f,
                100f,
                3000f,
                150f);
        //Elia Vouvon.setCustomDescriptionId("vic_phlegethon"); //reference descriptions.csv

        MarketAPI Bowditch_market = ps_gen.addMarketplace(
                "enmity",
                Kearsarge,
                null,
                "Bowditch",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.DESERT,
                                Conditions.RARE_ORE_ULTRARICH,
                                Conditions.MILD_CLIMATE,
                                Conditions.ORE_ABUNDANT,
                                Conditions.THIN_ATMOSPHERE
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
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
                                Industries.BATTLESTATION_HIGH,
                                Industries.REFINING,
                                Industries.ORBITALWORKS,
                                Industries.MEGAPORT
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                true,
                //junk and chatter
                true);

        //Asteroid belt
        system.addAsteroidBelt(AyubiaStar, 400, 6000, 200, 250, 300, Terrain.ASTEROID_BELT, "Inner Band");
        system.addRingBand(AyubiaStar, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 6000, 300f);

        //Ring
        system.addRingBand(AyubiaStar, "misc", "rings_dust0", 256f, 0, Color.gray, 600f, 4000, 220, Terrain.RING, "Outer ring");
        system.addRingBand(AyubiaStar, "misc", "rings_dust0", 256f, 0, Color.gray, 600f, 4500, 220, Terrain.RING, "Outer ring");

        //add Comm relay
        SectorEntityToken MakeshiftRelay = system.addCustomEntity("ps_comm_relay_makeshift", // unique id
                "Outer Ayubia Comm Relay", // name - if null, defaultName from custom_entities.json will be used
                "comm_relay_makeshift", // type of object, defined in custom_entities.json
                "enmity"); // faction
        MakeshiftRelay.setCircularOrbitPointingDown(AyubiaStar, 180f, 8700f, 265);

        // Nav beacon
        SectorEntityToken NavBeacon = system.addCustomEntity("ps_nav_buoy_makeshift", // unique id
                "Outer Ayubia Nav Beacon", // name - if null, defaultName from custom_entities.json will be used
                "nav_buoy_makeshift", // type of object, defined in custom_entities.json
                "enmity"); // faction
        NavBeacon.setCircularOrbitPointingDown(AyubiaStar, -90f, 6000f, 105);

        // Sensor relay
        SectorEntityToken SensorRelay = system.addCustomEntity("ps_sensor_array", // unique id
                "Ayubia Sensor Relay", // name - if null, defaultName from custom_entities.json will be used
                "sensor_array", // type of object, defined in custom_entities.json
                "enmity"); // faction
        SensorRelay.setCircularOrbitPointingDown(AyubiaStar, 100f, 9000f, 450f);

        //Jump point
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint(
                "ps_center_jump",
                "Center System Jump");

        jumpPoint1.setCircularOrbit(system.getEntityById("ps_star_Ayubia"), 280, 2400, 100f);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();

        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint(
                "ps_fringe_jump",
                "Fringe System Jump");

        jumpPoint2.setCircularOrbit(system.getEntityById("ps_star_Ayubia"), 100, 10000, 400f);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();

        //
        float radiusAfter2 = StarSystemGenerator.addOrbitingEntities(system, AyubiaStar, StarAge.YOUNG,
                3, 5, // min/max entities to add
                8000, // radius to start adding at
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
