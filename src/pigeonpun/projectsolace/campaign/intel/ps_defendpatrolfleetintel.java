package pigeonpun.projectsolace.campaign.intel;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import exerelin.campaign.intel.defensefleet.DefenseActionStage;
import exerelin.campaign.intel.defensefleet.DefenseFleetIntel;

public class ps_defendpatrolfleetintel extends DefenseFleetIntel {
    public ps_defendpatrolfleetintel(FactionAPI attacker, MarketAPI from, MarketAPI target, float fp, float orgDur) {
        super(attacker, from, target, fp, orgDur);
    }

    @Override
    public RouteFleetAssignmentAI createAssignmentAI(CampaignFleetAPI fleet, RouteManager.RouteData route) {
        ps_defendpatrolassignmentai defAI = new ps_defendpatrolassignmentai(this, fleet, route, (DefenseActionStage)action);
        return defAI;
    }
}
