package pigeonpun.projectsolace.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Nex_MarketCMD;
import exerelin.campaign.ColonyManager;
import exerelin.campaign.InvasionRound;
import exerelin.campaign.SectorManager;
import exerelin.utilities.InvasionListener;
import pigeonpun.projectsolace.com.ps_misc;
import pigeonpun.projectsolace.scripts.projectsolaceplugin;

import java.util.List;

public class ps_sodalityfleetadjustment implements InvasionListener {

    @Override
    public void reportInvadeLoot(InteractionDialogAPI dialog, MarketAPI market, Nex_MarketCMD.TempDataInvasion actionData, CargoAPI cargo) {

    }

    @Override
    public void reportInvasionRound(InvasionRound.InvasionRoundResult result, CampaignFleetAPI fleet, MarketAPI defender, float atkStr, float defStr) {

    }

    @Override
    public void reportInvasionFinished(CampaignFleetAPI fleet, FactionAPI attackerFaction, MarketAPI market, float numRounds, boolean success) {
        List<String> liveFactionIds = SectorManager.getLiveFactionIdsCopy();
        //check if setting is enabled
        if(!projectsolaceplugin.ps_sodalityFleetAdjustmentActive) return;
        //check if sodality already active
        if(Global.getSector().getMemoryWithoutUpdate().contains(projectsolaceplugin.ps_sodalityFleetAdjusted)) return;
        //check if one of the faction is still alive
        if((liveFactionIds.contains(projectsolaceplugin.enmity_ID) && !liveFactionIds.contains(projectsolaceplugin.solace_ID)) || (liveFactionIds.contains(projectsolaceplugin.solace_ID) && !liveFactionIds.contains(projectsolaceplugin.enmity_ID))) {
            FactionAPI enmity = Global.getSector().getFaction(projectsolaceplugin.enmity_ID);
            FactionAPI solace = Global.getSector().getFaction(projectsolaceplugin.solace_ID);
            //add toys to Enmity
            for(String id :ps_misc.SOLACE_SHIPS_LINEUP) {
                enmity.getKnownShips().add(id);
            }
            for(String id :ps_misc.SOLACE_WEAPONS_LINEUP) {
                enmity.getKnownWeapons().add(id);
            }
            if(projectsolaceplugin.ps_hardmodeSodalityActive) {
                for(String id :ps_misc.ENMITY_SPECIAL_WEAPONS_LIST) {
                    enmity.getKnownWeapons().add(id);
                }
            }
            //Solace turn to get more juice
            for(String id :ps_misc.ENMITY_SHIPS_LINEUP) {
                solace.getKnownShips().add(id);
            }
            for(String id :ps_misc.ENMITY_WEAPONS_LINEUP) {
                solace.getKnownWeapons().add(id);
            }
            for(String id :ps_misc.ENMITY_FIGHTER_LINEUP) {
                solace.getKnownFighters().add(id);
            }
            if(projectsolaceplugin.ps_hardmodeSodalityActive) {
                for(String id :ps_misc.ENMITY_SPECIAL_WEAPONS_LIST) {
                    solace.getKnownWeapons().add(id);
                }
            }
            Global.getSector().getMemoryWithoutUpdate().set(projectsolaceplugin.ps_sodalityFleetAdjusted, true);
        }
    }

    @Override
    public void reportMarketTransfered(MarketAPI market, FactionAPI newOwner, FactionAPI oldOwner, boolean playerInvolved, boolean isCapture, List<String> factionsToNotify, float repChangeStrength) {

    }
}
