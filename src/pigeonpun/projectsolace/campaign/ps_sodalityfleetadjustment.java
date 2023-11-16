package pigeonpun.projectsolace.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyDecivListener;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Nex_MarketCMD;
import exerelin.campaign.*;
import exerelin.campaign.alliances.Alliance;
import exerelin.campaign.intel.groundbattle.GroundBattleCampaignListener;
import exerelin.campaign.intel.groundbattle.GroundBattleIntel;
import exerelin.utilities.InvasionListener;
import org.apache.log4j.Logger;
import pigeonpun.projectsolace.campaign.intel.ps_sodalityfleetadjustmentintel;
import pigeonpun.projectsolace.com.ps_misc;
import pigeonpun.projectsolace.com.ps_util;
import pigeonpun.projectsolace.scripts.projectsolaceplugin;

import java.util.List;

public class ps_sodalityfleetadjustment implements InvasionListener, ColonyDecivListener, GroundBattleCampaignListener {
    public static final String PS_SODALITYFLEETADJUSTMENT_ACTIVED_IN_SAVE = "$ps_sodalityfleetadjustment_active_in_save";
    public static Logger log = Global.getLogger(ps_sodalityfleetadjustment.class);
    public static float LEARNED_HULL_FREQUENCY = 0.5f;
    public static float RANDOM_AUTOFIT_CHANCE = 0.8f;
    public ps_sodalityfleetadjustment() {
        //save compatibility
//        log.info("Registered Solace fleet adjustment listener");
        Global.getSector().getMemoryWithoutUpdate().set(PS_SODALITYFLEETADJUSTMENT_ACTIVED_IN_SAVE, true);
    }

    protected void checkIfApplyEffects() {
        //check if setting is enabled
        if(!projectsolaceplugin.ps_sodalityFleetAdjustmentActive) return;
        //check if sodality already active
        if(Global.getSector().getMemoryWithoutUpdate().contains(projectsolaceplugin.ps_sodalityFleetAdjusted)) {
            if((Global.getSector().getMemoryWithoutUpdate().get(projectsolaceplugin.ps_sodalityFleetAdjusted)) == true) {
                log.info("Sodality adjusted");
                return;
            }
        }
        //check if one of the faction is still alive
        if(getRemainingFaction().equals(projectsolaceplugin.solace_ID) || getRemainingFaction().equals(projectsolaceplugin.enmity_ID)) {
            activateEffects();
        }
    }
    protected void activateEffects() {
        log.info("Changing Solace+Enmity fleet composition");
        FactionAPI enmity = Global.getSector().getFaction(projectsolaceplugin.enmity_ID);
        FactionAPI solace = Global.getSector().getFaction(projectsolaceplugin.solace_ID);
        //Intel informing
        ps_sodalityfleetadjustmentintel sodalityIntel = new ps_sodalityfleetadjustmentintel();
        Global.getSector().getIntelManager().addIntel(sodalityIntel);
        Global.getSector().addScript(sodalityIntel);
        //add toys to Enmity
        for(String id :ps_misc.SOLACE_SHIPS_LINEUP) {
            enmity.addKnownShip(id, false);
            enmity.addUseWhenImportingShip(id);
            enmity.getHullFrequency().put(id, LEARNED_HULL_FREQUENCY);
        }
        for(String id :ps_misc.SOLACE_WEAPONS_LINEUP) {
            enmity.addKnownWeapon(id, false);
        }
        if(projectsolaceplugin.ps_hardmodeSodalityActive) {
            for(String id :ps_misc.ENMITY_SPECIAL_WEAPONS_LIST) {
                enmity.addKnownWeapon(id, false);
            }
        }
        enmity.getDoctrine().setAutofitRandomizeProbability(RANDOM_AUTOFIT_CHANCE);
        //Solace turn to get more juice
        for(String id :ps_misc.ENMITY_SHIPS_LINEUP) {
            solace.addKnownShip(id, false);
            solace.addUseWhenImportingShip(id);
            solace.getHullFrequency().put(id, LEARNED_HULL_FREQUENCY);
        }
        for(String id :ps_misc.ENMITY_WEAPONS_LINEUP) {
            solace.addKnownWeapon(id, false);
        }
        for(String id :ps_misc.ENMITY_FIGHTER_LINEUP) {
            solace.addKnownFighter(id, false);
        }
        if(projectsolaceplugin.ps_hardmodeSodalityActive) {
            for(String id :ps_misc.ENMITY_SPECIAL_WEAPONS_LIST) {
                solace.addKnownWeapon(id, false);
            }
        }
        solace.getDoctrine().setAutofitRandomizeProbability(RANDOM_AUTOFIT_CHANCE);
        Global.getSector().getMemoryWithoutUpdate().set(projectsolaceplugin.ps_sodalityFleetAdjusted, true);
    }
    public void checkIfAdjustRelationship(FactionAPI attackedFaction, FactionAPI attackerFaction) {
        FactionAPI enmity = Global.getSector().getFaction(projectsolaceplugin.enmity_ID);
        FactionAPI solace = Global.getSector().getFaction(projectsolaceplugin.solace_ID);

        Alliance alliance = AllianceManager.getFactionAlliance(enmity.getId());
        if(alliance.getMembersCopy().contains(attackedFaction.getId())) {
            adjustFactionRelationship(attackedFaction, attackerFaction);
        }
    }
    protected void adjustFactionRelationship(FactionAPI attackedFaction, FactionAPI attackerFaction) {
        log.info("solace/enmity changing relationship for attacker: " + attackerFaction.getDisplayName());
        FactionAPI enmity = Global.getSector().getFaction(projectsolaceplugin.enmity_ID);
        FactionAPI solace = Global.getSector().getFaction(projectsolaceplugin.solace_ID);
        if(!attackedFaction.getId().equals(projectsolaceplugin.enmity_ID)) {
            DiplomacyManager.createDiplomacyEventV2(enmity, attackerFaction, "hostilities_toward_ally", null);
        }
        if(!attackedFaction.getId().equals(projectsolaceplugin.solace_ID)) {
            DiplomacyManager.createDiplomacyEventV2(solace, attackerFaction, "hostilities_toward_ally", null);
        }
    }
    /**
     * @return Faction ID of the surviving faction
     * Can be "" of none survived or both survived
     */
    public static String getRemainingFaction() {
        if(ps_util.checkFactionAlive(projectsolaceplugin.enmity_ID) && !ps_util.checkFactionAlive(projectsolaceplugin.solace_ID)) {
            return projectsolaceplugin.enmity_ID;
        }
        if(ps_util.checkFactionAlive(projectsolaceplugin.solace_ID) && !ps_util.checkFactionAlive(projectsolaceplugin.enmity_ID)) {
            return projectsolaceplugin.solace_ID;
        }
        return "";
    }
    @Override
    public void reportInvadeLoot(InteractionDialogAPI dialog, MarketAPI market, Nex_MarketCMD.TempDataInvasion actionData, CargoAPI cargo) {

    }

    @Override
    public void reportInvasionRound(InvasionRound.InvasionRoundResult result, CampaignFleetAPI fleet, MarketAPI defender, float atkStr, float defStr) {

    }

    @Override
    public void reportInvasionFinished(CampaignFleetAPI fleet, FactionAPI attackerFaction, MarketAPI market, float numRounds, boolean success) {
//        log.info("Invasion finished, attacker: " + attackerFaction.getDisplayName());
        checkIfApplyEffects();
//        checkIfAdjustRelationship(market, attackerFaction);
    }

    @Override
    public void reportMarketTransfered(MarketAPI market, FactionAPI newOwner, FactionAPI oldOwner, boolean playerInvolved, boolean isCapture, List<String> factionsToNotify, float repChangeStrength) {
//        log.info("transfered market " + market.getName());
        checkIfApplyEffects();
    }

    @Override
    public void reportColonyAboutToBeDecivilized(MarketAPI market, boolean fullyDestroyed) {
        checkIfApplyEffects();
    }

    @Override
    public void reportColonyDecivilized(MarketAPI market, boolean fullyDestroyed) {
        checkIfApplyEffects();
    }

    @Override
    public void reportBattleStarted(GroundBattleIntel battle) {

    }

    @Override
    public void reportBattleBeforeTurn(GroundBattleIntel battle, int turn) {

    }

    @Override
    public void reportBattleAfterTurn(GroundBattleIntel battle, int turn) {

    }

    @Override
    public void reportBattleEnded(GroundBattleIntel battle) {
//        log.info("Groundbattle ended, attacker " + battle.getSide(true).getFaction().getDisplayName());
        checkIfApplyEffects();
        checkIfAdjustRelationship(battle.getSide(false).getFaction(), battle.getSide(true).getFaction());
    }
}
