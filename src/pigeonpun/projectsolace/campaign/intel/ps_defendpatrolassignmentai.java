package pigeonpun.projectsolace.campaign.intel;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.intel.defensefleet.DefenseAssignmentAI;
import exerelin.campaign.intel.defensefleet.DefenseFleetIntel;
import exerelin.utilities.StringHelper;

//modified version of DefendAssignmentAI
public class ps_defendpatrolassignmentai extends DefenseAssignmentAI {

    public ps_defendpatrolassignmentai(DefenseFleetIntel intel, CampaignFleetAPI fleet, RouteManager.RouteData route, FleetActionDelegate delegate) {
        super(intel, fleet, route, delegate);
    }

    @Override
    protected void giveRaidOrder(MarketAPI target) {
        float busyTime = 7;

        String name = target.getName();
        String capText = StringHelper.getFleetAssignmentString("defending", name);
        String moveText = StringHelper.getFleetAssignmentString("movingToDefend", name);
        if (delegate != null) {
            String s = delegate.getRaidApproachText(fleet, target);
            if (s != null) moveText = s;

            s = delegate.getRaidActionText(fleet, target);
            if (s != null) capText = s;
        }

        fleet.addAssignmentAtStart(FleetAssignment.PATROL_SYSTEM, target.getPrimaryEntity(), busyTime, capText, null);

        float dist = Misc.getDistance(target.getPrimaryEntity(), fleet);
        //if (dist > fleet.getRadius() + target.getPrimaryEntity().getRadius() + 300f) {
        if (dist > fleet.getRadius() + target.getPrimaryEntity().getRadius()) {
            fleet.addAssignmentAtStart(FleetAssignment.DELIVER_CREW, target.getPrimaryEntity(), 3f, moveText, null);
            busyTime += 3;
        }

        Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(),
                MemFlags.FLEET_BUSY, BUSY_REASON, true, busyTime);
    }
}
