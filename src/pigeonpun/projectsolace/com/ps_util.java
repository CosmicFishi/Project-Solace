package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.Global;
import exerelin.campaign.SectorManager;
import org.apache.log4j.Logger;
import pigeonpun.projectsolace.campaign.ps_sodalityfleetadjustment;

import java.util.List;

public class ps_util {
    public static Logger log = Global.getLogger(ps_util.class);
    public static boolean checkFactionAlive(String factionId) {
        List<String> liveFactionIds = SectorManager.getLiveFactionIdsCopy();
        log.info("Checking " + factionId + " is alive: " + liveFactionIds.contains(factionId));
        if(liveFactionIds.contains(factionId)) {
            return true;
        }
        return false;
    }
}
