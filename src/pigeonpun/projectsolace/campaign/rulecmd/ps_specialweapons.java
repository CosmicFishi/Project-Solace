package pigeonpun.projectsolace.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import pigeonpun.projectsolace.com.ps_misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    public static final int STOCK_COUNT_MIN = 7;
    public static final int STOCK_COUNT_MAX = 10;
    public static final String STOCK_ARRAY_KEY = "$ps_specialWeaponsStock";
    public static final String TOTAL_PURCHASE_COST_KEY = "$ps_totalPurchaseCost";
    public static final String PURCHASE_INVENTORY_KEY = "$ps_purchaseInventory";
    private static final int PURCHASE_TAX = 80;

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
            purchaseWeapons(memory);
        }

        return true;
    }

    protected void selectWeapons() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (WeaponPurchaseInfo weaponStock : getWeaponStock(market.getMemoryWithoutUpdate())) {
            WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponStock.id);
            if (spec != null) {
                copy.addWeapons(weaponStock.id, weaponStock.count);
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
                memory.set(PURCHASE_INVENTORY_KEY, listWeaponToBuy,  0.5f);
                cargo.sort();

                FireBest.fire(null, dialog, memoryMap, "ps_purchaseSpecialWeapons");
            }
            public void cancelledCargoSelection() {
            }
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

            }
        });
    }
    protected void purchaseWeapons(MemoryAPI memory) {
        //display the purchased weapon list with information about price - quantity
        //calculate the tax, total cost
        //set the memory key TOTAL_PURCHASE_COST_KEY to the total cost
        //todo: test that shit
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
            totalTax += weapon.getTotalCountPrice() * PURCHASE_TAX;
            totalPurchaseWithTax += totalTax + weapon.getTotalCountPrice();
            para += weapon.name + " " + Strings.X + " " + (int) weapon.count + " ="+ " " + totalWeaponPrice + "\n";
            highlights.add(" " + totalWeaponPrice);
        }
        para = para.substring(0, para.length() - 1);
        text.addParagraph(para);
        text.highlightInLastPara(hl, highlights.toArray(new String[0]));
        text.addParagraph("-----------------------------------------------------------------------------");
        text.addParagraph("Tariffs(" + PURCHASE_TAX + "%): " + totalTax );
        text.addParagraph("Total: " + totalPurchaseWithTax);

        memory.set(TOTAL_PURCHASE_COST_KEY, totalPurchaseWithTax);
    }
    protected List<WeaponPurchaseInfo> getWeaponStock(MemoryAPI memory) {
        if(memory.contains(STOCK_ARRAY_KEY)) {
           return (List<WeaponPurchaseInfo>) memory.get(STOCK_ARRAY_KEY);
        }
        List<WeaponPurchaseInfo> weapons = generateWeaponStock();
        setWeaponsStock(memory, weapons, true);
        return weapons;
    }
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
            float w = 2 * spec.getRarity();

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
            count = count + random.nextInt(count + 2) - random.nextInt(count + 1);
            if (count < 1) count = 1;

            WeaponPurchaseInfo weaponPurchaseInfo = new WeaponPurchaseInfo(spec.getWeaponId(), count, spec.getWeaponName(), (int) spec.getBaseValue());
            weapons.add(weaponPurchaseInfo);
        }

        return weapons;
    }

    protected int calculateTotalAmount(List<WeaponPurchaseInfo> listWeapons) {
        int totalCost = 0;
        for (WeaponPurchaseInfo w: listWeapons) {

        }
        return totalCost;
    }

    public static class WeaponPurchaseInfo {
        public String id;
        public Integer count;
        public String name;
        public int price;
        public WeaponPurchaseInfo(String id, int count, String name, int price) {
            this.id = id;
            this.count = count;
            this.name = name;
            this.price = price;
        }
        public int getTotalCountPrice() {
            return (int) price * count;
        }
    }
}
