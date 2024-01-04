package pigeonpun.projectsolace.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.MutableValue;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.*;
import java.util.List;

//modified version of Nex_BlueprintSwap
public class ps_specialweapons extends BaseCommandPlugin {

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected PersonAPI person;
    protected FactionAPI faction;
    protected MarketAPI market;
    protected boolean buysAICores;
    protected float valueMult;
    protected float repMult;
    public static final float STOCK_KEEP_DAYS = 30;
    public static final int STOCK_COUNT_MIN = 3;
    public static final int STOCK_COUNT_MAX = 7;
    public static final String STOCK_ARRAY_KEY = "$ps_specialWeaponsStock";
    public static final String TOTAL_PURCHASE_COST_KEY = "$ps_totalPurchaseCost";
    public static final String PURCHASE_INVENTORY_KEY = "$ps_purchaseInventory";
    private static final int PURCHASE_TAX = 50;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        person = dialog.getInteractionTarget().getActivePerson();
        faction = person.getFaction();
        market = dialog.getInteractionTarget().getMarket();

        buysAICores = faction.getCustomBoolean("buysAICores");
        valueMult = faction.getCustomFloat("AICoreValueMult");
        repMult = faction.getCustomFloat("AICoreRepMult");

        if (command.equals("selectWeapons")) {
            selectWeapons();
        } else if (command.equals("purchaseWeapons")) {
            purchaseWeapons();
        } else if (command.equals("confirmPurchaseWeapons")) {
            confirmPurchaseWeapons();
        } else if (command.equals("checkIfCanSell")) {
            checkIfCanSell();
        }
        return true;
    }

    protected void selectWeapons() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (WeaponPurchaseInfo weaponStock : getWeaponStock(market.getMemoryWithoutUpdate())) {
            WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponStock.id);
            if (spec != null) {
                copy.addWeapons(weaponStock.id, weaponStock.getCount());
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select weapons to purchase", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            public void pickedCargo(CargoAPI cargo) {
                if (cargo.isEmpty()) {
                    cancelledCargoSelection();
                    return;
                }
                List<WeaponPurchaseInfo> listWeaponToBuy = new ArrayList<>();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    if(stack.isWeaponStack()) {
                        WeaponPurchaseInfo weaponToBuy = new WeaponPurchaseInfo(
                                stack.getWeaponSpecIfWeapon().getWeaponId(),
                                (int) stack.getSize(),
                                stack.getWeaponSpecIfWeapon().getWeaponName(),
                                (int) stack.getWeaponSpecIfWeapon().getBaseValue()
                        );
                        listWeaponToBuy.add(weaponToBuy);
                    }
                }
                memory.set(PURCHASE_INVENTORY_KEY, listWeaponToBuy,  0f);
                cargo.sort();

                FireBest.fire(null, dialog, memoryMap, "ps_purchaseSpecialWeapons");
            }
            public void cancelledCargoSelection() {
            }
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

            }
        });
    }
    protected void purchaseWeapons() {
        //display the purchased weapon list with information about price - quantity
        //calculate the tax, total cost
        List<WeaponPurchaseInfo> purchasingInventory = (List<WeaponPurchaseInfo>) memory.get(PURCHASE_INVENTORY_KEY);
        TextPanelAPI text = dialog.getTextPanel();

        //text.setFontVictor();
        text.setFontSmallInsignia();

        Color hl = Misc.getHighlightColor();
        Color red = Misc.getNegativeHighlightColor();
        text.addParagraph("-----------------------------------------------------------------------------");

        String para = "";
        List<String> highlights = new ArrayList<String>();
        float totalTax = 0;
        float totalPurchaseWithTax = 0;
        for (WeaponPurchaseInfo weapon : purchasingInventory) {
            int totalWeaponPrice = weapon.getTotalCountPrice();
            totalTax += weapon.getTotalCountPrice() * PURCHASE_TAX / 100f;
            totalPurchaseWithTax += totalTax + weapon.getTotalCountPrice();
            para += weapon.name + " " + Strings.X + " " + (int) weapon.getCount() + " = "+ Misc.getDGSCredits(totalWeaponPrice) + "\n";
            highlights.add(Misc.getDGSCredits(totalWeaponPrice));
        }
        para = para.substring(0, para.length() - 1);
        text.addParagraph(para);
        text.highlightInLastPara(hl, highlights.toArray(new String[0]));
        text.addParagraph("-----------------------------------------------------------------------------");
        text.addParagraph("Tariffs (" + PURCHASE_TAX + "%): " + Misc.getDGSCredits(totalTax)) ;
        text.highlightFirstInLastPara("" + Misc.getDGSCredits(totalTax), hl);
        text.addParagraph("Total: " + Misc.getDGSCredits(totalPurchaseWithTax));
        text.highlightFirstInLastPara("" + Misc.getDGSCredits(totalPurchaseWithTax), hl);
        text.addParagraph("-----------------------------------------------------------------------------");
        memory.set(TOTAL_PURCHASE_COST_KEY, totalPurchaseWithTax, 0);
        if(playerCargo.getCredits().get() < totalPurchaseWithTax) {
            text.setFontInsignia();
            text.addParagraph("It seems like you current do not have enough to purchase. How about we do some adjustment about your to-buy armament inventory ?");
            dialog.getOptionPanel().setEnabled("ps_confirmPurchaseSpecialWeapons", false);
        } else {
            dialog.getOptionPanel().setEnabled("ps_confirmPurchaseSpecialWeapons", true);
        }
    }
    protected void confirmPurchaseWeapons() {
        List<WeaponPurchaseInfo> purchasingInventory = (List<WeaponPurchaseInfo>) memory.get(PURCHASE_INVENTORY_KEY);
        List<WeaponPurchaseInfo> stockInventory = getWeaponStock(market.getMemoryWithoutUpdate());
        List<WeaponPurchaseInfo> toRemoveFromStock = new ArrayList<>();
        float purchaseCost = (float) memory.get(TOTAL_PURCHASE_COST_KEY);
        for (WeaponPurchaseInfo weaponPurchaseInfo: purchasingInventory) {
            for(WeaponPurchaseInfo stockWeapon: stockInventory) {
                if (Objects.equals(stockWeapon.id, weaponPurchaseInfo.id)) {
                    if(stockWeapon.count == weaponPurchaseInfo.count) {
                        toRemoveFromStock.add(stockWeapon);
                    } else {
                        stockWeapon.setCount(stockWeapon.count - weaponPurchaseInfo.count);
                    }
                }
            }
            playerCargo.addWeapons(weaponPurchaseInfo.id, weaponPurchaseInfo.getCount());
        }
        for (WeaponPurchaseInfo w: toRemoveFromStock) {
            stockInventory.remove(w);
        }
//        setWeaponsStock(memory, newStockInventory, false);
        setWeaponsStock(market.getMemoryWithoutUpdate(), stockInventory, false);
//        playerCargo.getCredits().set(playerCargo.getCredits().get() - purchaseCost);
        MutableValue credits = Global.getSector().getPlayerFleet().getCargo().getCredits();
        AddRemoveCommodity.addCreditsLossText((int)purchaseCost, dialog.getTextPanel());
        credits.subtract(purchaseCost);
    }
    protected boolean checkIfCanSell() {
        if (person == null) return false;

        return Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
                Ranks.POST_STATION_COMMANDER.equals(person.getPostId()) ||
                Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
                Ranks.POST_OUTPOST_COMMANDER.equals(person.getPostId());
    }
    //get from market.getMemoryWithoutUpdate()
    protected List<WeaponPurchaseInfo> getWeaponStock(MemoryAPI memory) {
        if(memory.contains(STOCK_ARRAY_KEY)) {
           return (List<WeaponPurchaseInfo>) memory.get(STOCK_ARRAY_KEY);
        }
        List<WeaponPurchaseInfo> weapons = generateWeaponStock();
        setWeaponsStock(memory, weapons, true);
        return weapons;
    }
    //set from market.getMemoryWithoutUpdate()
    protected void setWeaponsStock(MemoryAPI memory, List<WeaponPurchaseInfo> weapons, boolean isRefreshing) {
        float time = STOCK_KEEP_DAYS;
        if(!isRefreshing && memory.contains(STOCK_ARRAY_KEY)) {
            time = memory.getExpire(STOCK_ARRAY_KEY);
        }
        memory.set(STOCK_ARRAY_KEY, weapons, time);
    }
    protected List<WeaponPurchaseInfo> generateWeaponStock() {
        Random random = new Random();
        List<WeaponPurchaseInfo> weapons = new ArrayList<>();
        WeightedRandomPicker<WeaponSpecAPI> pickerSpecialWeapons = new WeightedRandomPicker<>(random);
        List<String> hiddenWeapons = ps_misc.ENMITY_SPECIAL_WEAPONS_LIST;

        for (String id : hiddenWeapons) {
            WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
            float w = 2 * (spec.getRarity() != 0 ? spec.getRarity() : 1);
            float p = 1f; //
            p *= w;
            pickerSpecialWeapons.add(spec, p);
        }

        int num = STOCK_COUNT_MIN + random.nextInt(STOCK_COUNT_MAX - STOCK_COUNT_MIN + 1);

        for (int i = 0; i < num && !pickerSpecialWeapons.isEmpty(); i++) {
            int count = 1;
            WeaponSpecAPI spec = pickerSpecialWeapons.pick(random);
            switch (spec.getSize()) {
                case LARGE: count = 1; break;
                case MEDIUM: count = 2; break;
                case SMALL: count = 3; break;
            }
            count = random.nextInt(count + 2);
            if (count < 1) count = 1;

            WeaponPurchaseInfo weaponPurchaseInfo = new WeaponPurchaseInfo(spec.getWeaponId(), count, spec.getWeaponName(), (int) spec.getBaseValue());
            weapons.add(weaponPurchaseInfo);
        }

        return weapons;
    }

    public static class WeaponPurchaseInfo {
        public String id;
        private Integer count;
        public String name;
        public int price;
        public WeaponPurchaseInfo(String id, int count, String name, int price) {
            this.id = id;
            this.setCount(count);
            this.name = name;
            this.price = price;
        }
        public int getTotalCountPrice() {
            return (int) price * getCount();
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}
