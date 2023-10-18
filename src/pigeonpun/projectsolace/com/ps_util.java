package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.SectorManager;
import org.apache.log4j.Logger;
import pigeonpun.projectsolace.campaign.ps_sodalityfleetadjustment;

import java.util.List;

public class ps_util {
    public static Logger log = Global.getLogger(ps_util.class);
    public static boolean checkFactionAlive(String factionId) {
        log.info("Checking " + factionId + " is dead: " + Misc.getFactionMarkets(factionId).isEmpty());
        if(Misc.getFactionMarkets(factionId).isEmpty()) {
            return true;
        }
        return false;
    }
}
