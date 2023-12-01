package pigeonpun.projectsolace.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import pigeonpun.projectsolace.com.ps_misc;

import java.util.ArrayList;
import java.util.List;

public class ps_sciencemarketplugin extends BaseSubmarketPlugin {

    private final RepLevel minStanding = RepLevel.FRIENDLY;

    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }
    public void updateCargoPrePlayerInteraction() {
        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            pruneWeapons(0f);

            int weapons = 14 + Math.max(0, market.getSize() - 1) * 2;

            addWeaponRareOnly(weapons, weapons + 2, 3, submarket.getFaction().getId());


            float stability = market.getStabilityValue();
            float sMult = Math.max(0.1f, stability / 10f);
            getCargo().getMothballedShips().clear();

            int size = submarket.getFaction().getDoctrine().getShipSize();
            int add = 0;
            if (stability <= 4) {
                add = 2;
            } else if (stability <= 6) {
                add = 1;
            }

            size += add;
            if (size > 5) size = 5;

            FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
            doctrineOverride.setShipSize(size);

            addShips(submarket.getFaction().getId(),
                    //(150f + market.getSize() * 25f) * sMult, // combat
                    200f * sMult, // combat
                    15f, // freighter
                    10f, // tanker
                    20f, // transport
                    10f, // liner
                    10f, // utilityPts
                    null, // qualityOverride
                    0f, // qualityMod
                    null,
                    doctrineOverride);
        }

        getCargo().sort();
    }

    @Override
    public boolean okToUpdateShipsAndWeapons() {
        return super.okToUpdateShipsAndWeapons();
    }

    @Override
    public float getTariff() {
        return 0.5f;
    }

    //modified version of addWeapons() in BaseSubmarketPlugin
    protected void addWeaponRareOnly(int min, int max, int maxTier, String pickerFactionId) {
        WeightedRandomPicker<WeaponSpecAPI> picker = new WeightedRandomPicker<WeaponSpecAPI>(itemGenRandom);
        WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<String>(itemGenRandom);
        factionPicker.add(pickerFactionId);

//        List<String> hiddenWeapons = ps_misc.ENMITY_SPECIAL_WEAPONS_LIST;

        for (int i = 0; i < factionPicker.getItems().size(); i++) {
            String factionId = factionPicker.getItems().get(i);
            float w = factionPicker.getWeight(i);
            if (factionId == null) factionId = market.getFactionId();

            float quality = Misc.getShipQuality(market, factionId);
            FactionAPI faction = Global.getSector().getFaction(factionId);

            for (String id : faction.getKnownWeapons()) {
                WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
                if (spec.getTier() > maxTier) continue;
                if (!spec.hasTag("rare_bp")) continue;
                if (spec.getAIHints().contains(WeaponAPI.AIHints.SYSTEM)) continue;
                if (spec.hasTag(Tags.WEAPON_NO_SELL)) continue;

                float p = DefaultFleetInflater.getTierProbability(spec.getTier(), quality);
                p = 1f; //
                p *= w;
                if (faction.getWeaponSellFrequency().containsKey(id)) {
                    p *= faction.getWeaponSellFrequency().get(id);
                }
                picker.add(spec, p);
            }

//            for (String id : hiddenWeapons) {
//                WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
//                if (spec.getTier() > maxTier) continue;
//
//                float p = 1f; //
//                p *= w;
//                picker.add(spec, p);
//            }
        }

        int num = min + itemGenRandom.nextInt(max - min + 1);

        for (int i = 0; i < num && !picker.isEmpty(); i++) {
            pickAndAddWeapons(picker);
        }
    }

    @Override
    public boolean isMilitaryMarket() {
        return true;
    }
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        return true;
    }
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (stack.isWeaponStack()) {
            return false;
        }
        return true;
    }
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        RepLevel req = getRequiredLevelAssumingLegal(stack, action);

        if (req != null) {
            return "Req: " +
                    submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
        }
        if(stack.isWeaponStack()) {
            return "Illegal to sell here";
        }

        return "Illegal to trade in " + stack.getDisplayName() + " here";
    }
    private RepLevel getRequiredLevelAssumingLegal(CargoStackAPI stack, TransferAction action) {
        int tier = -1;
        if (stack.isWeaponStack()) {
            WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
            tier = spec.getTier();
        }

        if (tier >= 0) {
            if (action == TransferAction.PLAYER_BUY) {
                switch (tier) {
                    case 0: return RepLevel.FAVORABLE;
                    case 1: return RepLevel.WELCOMING;
                    case 2: return RepLevel.FRIENDLY;
                    case 3: return RepLevel.COOPERATIVE;
                }
            }
            return RepLevel.VENGEFUL;
        }

        return null;
    }
    public boolean isEnabled(CoreUIAPI ui) {
        //if (mode == CoreUITradeMode.OPEN) return false;
        if (ui.getTradeMode() == CampaignUIAPI.CoreUITradeMode.SNEAK) return false;

        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        return level.isAtWorst(minStanding);
    }
    public String getTooltipAppendix(CoreUIAPI ui) {
        if (!isEnabled(ui)) {
            return "Requires: " + submarket.getFaction().getDisplayName() + " - " + minStanding.getDisplayName().toLowerCase();
        }
        if (ui.getTradeMode() == CampaignUIAPI.CoreUITradeMode.SNEAK) {
            return "Requires: proper docking authorization";
        }
        return null;
    }
}
