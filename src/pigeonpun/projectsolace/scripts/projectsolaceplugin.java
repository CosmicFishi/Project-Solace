package pigeonpun.projectsolace.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.campaign.Faction;
import exerelin.campaign.SectorManager;
import org.apache.log4j.Logger;
import pigeonpun.projectsolace.campaign.ps_defendpatrolfleetmanager;
import pigeonpun.projectsolace.campaign.ps_sodalityfleetadjustment;
import pigeonpun.projectsolace.com.lunaconfighelper;
import pigeonpun.projectsolace.com.ps_misc;
import pigeonpun.projectsolace.weapons.ai.ps_anaxyr_ai;
import pigeonpun.projectsolace.world.ps_gen;
import pigeonpun.projectsolace.world.ps_salvagesplacer;
import pigeonpun.projectsolace.world.ps_vagrantseergen;

public class projectsolaceplugin extends BaseModPlugin {
    static Logger log = Global.getLogger(projectsolaceplugin.class);
    public static boolean blackrockExists = false;
    public static boolean borkenExists = false;
    public static boolean checkMemory = false;
    public static boolean diableExists = false;
    public static boolean exigencyExists = false;
    public static boolean hasDynaSector = false;
    public static boolean hasGraphicsLib = false;
    public static boolean hasMagicLib = false;
    public static boolean hasUnderworld = false;
    public static boolean iceExists = false;
    public static boolean imperiumExists = false;
    public static boolean junkPiratesExists = false;
    public static boolean oraExists = false;
    public static boolean scalarTechExists = false;
    public static boolean scyExists = false;
    public static boolean shadowyardsExists = false;
    public static boolean templarsExists = false;
    public static boolean tiandongExists = false;
    public static boolean tyradorExists = false;
    public static boolean dmeExists = false;
    public static boolean arkgneisisExists = false;
    public static boolean nexerelinEnabled = false;
    public static final String enmity_ID = "enmity";
    public static final String solace_ID = "projectsolace";
    public static float ps_solaceDefendTimeoutPeriod = 90;
    public static float ps_defendPointEconomyMult = 0.5f;
    public static float ps_pointsRequiredForDefendFleet = 00f;
    public static boolean ps_solaceDefend = true;
    public static boolean ps_vagrantseerGenerateSalvage = true;
    public static boolean ps_bygonedameGenerateSalvage = true;
    public static final String ps_vagrantseerSalvage_Generated = "$ps_vagrantseerSalvage_Generated";
    public static final String ps_bygonedameSalvage_Generated = "$ps_bygonedameSalvage_Generated";
    public static boolean ps_sodalityFleetAdjustmentActive = true;
    public static final String ps_sodalityFleetAdjusted = "$ps_sodalityFleetAdjusted";
    public static boolean ps_hardmodeSodalityActive = false;
    public static boolean ps_lunalibEnabled = false;
    public static boolean ps_epilepsy = true;
    public static float ps_glitchAlphaMult = 0.65f;
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        nexerelinEnabled = Global.getSettings().getModManager().isModEnabled("nexerelin");
        ps_lunalibEnabled = Global.getSettings().getModManager().isModEnabled("lunalib");

        ps_solaceDefendTimeoutPeriod = Global.getSettings().getFloat("ps_solaceDefendTimeoutPeriod");
        ps_defendPointEconomyMult = Global.getSettings().getFloat("ps_defendPointEconomyMult");
        ps_pointsRequiredForDefendFleet = Global.getSettings().getFloat("ps_pointsRequiredForDefendFleet");
        ps_solaceDefend = Global.getSettings().getBoolean("ps_solaceDefend");
        ps_vagrantseerGenerateSalvage = Global.getSettings().getBoolean("ps_vagrantseerGenerateSalvage");
        ps_bygonedameGenerateSalvage = Global.getSettings().getBoolean("ps_bygonedameGenerateSalvage");
        ps_sodalityFleetAdjustmentActive = Global.getSettings().getBoolean("ps_sodalityFleetAdjustmentActive");
        ps_hardmodeSodalityActive = Global.getSettings().getBoolean("ps_hardmodeSodalityActive");
        ps_epilepsy = Global.getSettings().getBoolean("ps_epilepsy");
        ps_glitchAlphaMult = Global.getSettings().getFloat("ps_glitchAlphaMult");

        if(ps_lunalibEnabled) {
            lunaconfighelper.initLunaConfig();
        }

//        hasUnderworld = Global.getSettings().getModManager().isModEnabled("underworld");
//        hasDynaSector = Global.getSettings().getModManager().isModEnabled("dynasector");
//
//        borkenExists = Global.getSettings().getModManager().isModEnabled("fob");
//        iceExists = Global.getSettings().getModManager().isModEnabled("nbj_ice");
//        imperiumExists = Global.getSettings().getModManager().isModEnabled("Imperium");
//        templarsExists = Global.getSettings().getModManager().isModEnabled("Templars");
//        blackrockExists = Global.getSettings().getModManager().isModEnabled("blackrock_driveyards");
//        exigencyExists = Global.getSettings().getModManager().isModEnabled("exigency");
//        shadowyardsExists = Global.getSettings().getModManager().isModEnabled("shadow_ships");
//        junkPiratesExists = Global.getSettings().getModManager().isModEnabled("junk_pirates_release");
//        scyExists = Global.getSettings().getModManager().isModEnabled("SCY");
//        tiandongExists = Global.getSettings().getModManager().isModEnabled("THI");
//        diableExists = Global.getSettings().getModManager().isModEnabled("diableavionics");
//        oraExists = Global.getSettings().getModManager().isModEnabled("ORA");
//        tyradorExists = Global.getSettings().getModManager().isModEnabled("TS_Coalition");
//        dmeExists = Global.getSettings().getModManager().isModEnabled("istl_dam");
//        scalarTechExists = Global.getSettings().getModManager().isModEnabled("tahlan_scalartech");
//        nexerelinEnabled = Global.getSettings().getModManager().isModEnabled("nexerelin");

        // Test that the .jar is loaded and working, using the most obnoxious way possible.
        //throw new RuntimeException("Template mod loaded! Remove this crash in TemplateModPlugin.");
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case ps_misc.ANAXYR_MSL_ID:
                return new PluginPick<MissileAIPlugin>(new ps_anaxyr_ai(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
                return null;
        }
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        if (!nexerelinEnabled || SectorManager.getManager().isCorvusMode()) {
            new ps_gen().generate(Global.getSector());
//             Add code that creates a new star system (will only run if Nexerelin's Random (corvus) mode is disabled).
        }
        if(nexerelinEnabled) {
            addScripts();
        }
        if(ps_lunalibEnabled) {
            lunaconfighelper.tryLoadLunaConfig();
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if(ps_lunalibEnabled) {
            lunaconfighelper.tryLoadLunaConfig();
        }
        boolean hasProjectSolace = SharedData.getData().getPersonBountyEventData().getParticipatingFactions().contains("projectsolace");
        SectorAPI sector = Global.getSector();
        if (!hasProjectSolace) {
            new ps_gen().generate(Global.getSector());
            new ps_vagrantseergen().generate(Global.getSector());
            if (!Global.getSector().getMemoryWithoutUpdate().contains(ps_vagrantseerSalvage_Generated) || !Global.getSector().getMemoryWithoutUpdate().contains(ps_bygonedameSalvage_Generated))
                new ps_salvagesplacer().generate(Global.getSector());
            if(!Global.getSector().getMemoryWithoutUpdate().contains(ps_sodalityfleetadjustment.PS_SODALITYFLEETADJUSTMENT_ACTIVED_IN_SAVE)) {
                sector.getListenerManager().addListener(new ps_sodalityfleetadjustment());
            }
            if(!Global.getSector().getMemoryWithoutUpdate().contains(ps_defendpatrolfleetmanager.PS_DEFENDPATROLFLEETMANAGER_ACTIVED_IN_SAVE)) {
                sector.addScript(ps_defendpatrolfleetmanager.create());
            }
        }
        Global.getSector().addTransientListener(new allianceListener());
    }
    @Override
    public void onNewGameAfterProcGen() {
        if(ps_vagrantseerGenerateSalvage) {
            new ps_salvagesplacer().generate(Global.getSector());
            new ps_vagrantseergen().generate(Global.getSector());
        }
    }
    public static void addScripts() {
        SectorAPI sector = Global.getSector();
        sector.addScript(ps_defendpatrolfleetmanager.create());
        sector.getListenerManager().addListener(new ps_sodalityfleetadjustment());
    }
    private static class allianceListener extends BaseCampaignEventListener {

        public allianceListener() {
            super(false);
        }

        @Override
        public void reportPlayerReputationChange(String faction, float delta) {
            super.reportPlayerReputationChange(faction, delta);
            FactionAPI enmity = Global.getSector().getFaction(enmity_ID);
            FactionAPI solace = Global.getSector().getFaction(solace_ID);

            if(enmity_ID.equals(faction) || solace_ID.equals(faction)) {
//                System.out.println("report relation change: " + Global.getSector().getFaction(faction).getRelToPlayer().getRel());
                if(enmity.getRelToPlayer().getRel() < -0.5f) {
                    solace.adjustRelationship(Factions.PLAYER, delta, RepLevel.HOSTILE);
                } else if (solace.getRelToPlayer().getRel() < -0.5f) {
                    enmity.adjustRelationship(Factions.PLAYER, delta, RepLevel.HOSTILE);
                }
            }
        }
    }
}
