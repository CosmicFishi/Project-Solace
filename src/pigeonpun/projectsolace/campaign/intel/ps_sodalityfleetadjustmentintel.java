package pigeonpun.projectsolace.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.AllianceManager;
import exerelin.campaign.alliances.Alliance;
import exerelin.utilities.StringHelper;
import pigeonpun.projectsolace.com.ps_util;
import pigeonpun.projectsolace.scripts.projectsolaceplugin;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ps_sodalityfleetadjustmentintel extends BaseIntelPlugin {

    public ps_sodalityfleetadjustmentintel() {

    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        float opad = 10f;

        Color h = Misc.getHighlightColor();

        String str = "A new dawn";
        LabelAPI para = info.addTitle(str);
        para.setHighlight(str);
        para.setHighlightColors(h);
    }

    @Override
    public Color getTitleColor(ListInfoMode mode) {
        return Misc.getBasePlayerColor();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;

        Color h = Misc.getHighlightColor();

        FactionAPI enmity = Global.getSector().getFaction(projectsolaceplugin.enmity_ID);
        FactionAPI solace = Global.getSector().getFaction(projectsolaceplugin.solace_ID);
        List<String> ps_sodalityAlliance = new ArrayList<>();
        ps_sodalityAlliance.add(projectsolaceplugin.enmity_ID);
        ps_sodalityAlliance.add(projectsolaceplugin.solace_ID);

        printSodalityCrests(info, ps_sodalityAlliance, width, opad);

        String str = "" + enmity.getDisplayName() + " and " + solace.getDisplayName() + " now knows each other technology.";
        LabelAPI para = info.addPara(str, opad);
        para.setHighlight(enmity.getDisplayName(), solace.getDisplayName());
        para.setHighlightColors(h, h);

        if(projectsolaceplugin.ps_hardmodeSodalityActive) {
            String str1 = "This is gonna be fun";
            para = info.addPara(str1, opad);
            para.setHighlight(str1);
            para.setHighlightColors(h, h);
        }

        // days ago
        info.addPara(Misc.getAgoStringForTimestamp(timestamp) + ".", opad);
    }

    public static void printSodalityCrests(TooltipMakerAPI info, List<String> sodality, float width, float padding)
    {
        List<String> members = new ArrayList<>(sodality);
        Collections.sort(members);
        List<String> crests = new ArrayList<>();
        int count = 0;
        for (String factionId : members)
        {
            crests.add(Global.getSector().getFaction(factionId).getCrest());
            count++;
            if (count >= 8) break;
        }

        if (count <= 0) return;

        // use two rows for crests if alliance has > 4 members
        String[] crestsArray = crests.toArray(new String[0]);

        info.addImages(width, (int)(256/count), padding, padding, Arrays.copyOfRange(crestsArray, 0, count));
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        if(ps_util.checkFactionAlive(projectsolaceplugin.enmity_ID)) {
            tags.add(projectsolaceplugin.enmity_ID);
        }
        if(ps_util.checkFactionAlive(projectsolaceplugin.solace_ID)) {
            tags.add(projectsolaceplugin.solace_ID);
        }
        return tags;
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "important");
    }

    @Override
    public String getSmallDescriptionTitle() {
        return "A new dawn";
    }
    @Override
    public String getSortString() {
        return "A new dawn";
    }
}
