package pigeonpun.projectsolace.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.econ.Market;
import exerelin.campaign.DiplomacyManager;
import exerelin.campaign.econ.FleetPoolManager;
import exerelin.campaign.fleets.InvasionFleetManager;
import exerelin.campaign.intel.defensefleet.DefenseFleetIntel;
import exerelin.campaign.intel.fleets.OffensiveFleetIntel;
import exerelin.campaign.intel.raid.NexRaidIntel;
import exerelin.campaign.intel.rebellion.RebellionIntel;
import exerelin.utilities.NexUtils;
import exerelin.utilities.NexUtilsFaction;
import exerelin.utilities.NexUtilsMarket;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.campaign.intel.ps_defendpatrolfleetintel;

import java.util.*;

import static exerelin.campaign.fleets.InvasionFleetManager.getInvasionPointCost;
import static pigeonpun.projectsolace.scripts.projectsolaceplugin.*;

//modified version of InvationFleetManager from Nex
/**
 * Handles defend fleets generation.
 * Originally derived from Dark.Revenant's II_WarFleetManager.
 *
 * How it works: Every ingame day, Solace accumulates "defend points"
 * (primarily based on its markets). When it has enough points, it attempts to launch
 * a defend for one of its friendly/allied faction.
 * The relevant code paths start in {@code advance()}, so look there.
 */
public class ps_defendpatrolfleetmanager extends BaseCampaignEventListener implements EveryFrameScript {
    public static final String PS_DEFENDPATROLFLEETMANAGER_ACTIVED_IN_SAVE = "$ps_defendpatrolfleetmanager_actived_in_save";
    public static final String MANAGER_MAP_KEY = "ps_defendpatrolfleetmanager";
    public static final String MEMORY_KEY_POINTS_LAST_TICK = "$ps_defendPatrolPointsLastTick";
    public static final float DEFENCE_ESTIMATION_MULT = 0.75f;
    public static final float GENERAL_SIZE_MULT = 0.9f;
    public static final float BASE_DEFEND_SIZE = 500f;    // for reference, Jangala at start of game is around 500
    public static final float MAX_DEFEND_SIZE = 2000f;
    public static final float MAX_DEFEND_SIZE_ECONOMY_MULT = 6f;
    public static final float MAX_ONGOING_INTEL = 5;
    public static Logger log = Global.getLogger(ps_defendpatrolfleetmanager.class);
    protected final List<ps_defendpatrolfleetintel> activeIntel = new LinkedList();
    protected HashMap<String, Float> spawnCounter = new HashMap<>();
    protected final IntervalUtil tracker;
    public static final boolean PREFER_MILITARY_FOR_ORIGIN = false;
    protected int lifetimeDefends = 0;
    protected float daysElapsed = 0;

    public ps_defendpatrolfleetmanager() {
        super(true);
        this.tracker = new IntervalUtil(1, 1);
    }
    /**
     * Gets the max defend size for Solace.
     * Capped at {@code MAX_DEFEND_SIZE}, and returns that value immediately if brawl mode is enabled.
     * @param factionId
     * @param maxMult Multiplier for maximum economy-based size.
     * @return
     */
    public static float getMaxDefendPatrolSize(String factionId, float maxMult) {
        if (Global.getSettings().getBoolean("nex_brawlMode")) {
            return MAX_DEFEND_SIZE;
        }

        float value = 0;
        List<MarketAPI> markets = NexUtilsFaction.getFactionMarkets(factionId);
        if (markets.isEmpty()) return MAX_DEFEND_SIZE;    // assume this is a Remnant raid or similar

        for (MarketAPI market : markets) {
            value += getMarketDefendCommodityValue(market);
        }
        value *= MAX_DEFEND_SIZE_ECONOMY_MULT * maxMult;

        if (value > MAX_DEFEND_SIZE)
            value = MAX_DEFEND_SIZE;

        return value;
    }
    /**
     * Gets the contribution of the specified market to defend points, based on its commodity availability.
     * @param market
     * @return
     */
    public static float getMarketDefendCommodityValue(MarketAPI market) {
        float ships = FleetPoolManager.getCommodityPoints(market, Commodities.SHIPS);
        float supplies = FleetPoolManager.getCommodityPoints(market, Commodities.SUPPLIES);
        float marines = FleetPoolManager.getCommodityPoints(market, Commodities.MARINES);
        float mechs = FleetPoolManager.getCommodityPoints(market, Commodities.HAND_WEAPONS);
        float fuel = FleetPoolManager.getCommodityPoints(market, Commodities.FUEL);

        float stabilityMult = 0.25f + (0.75f * market.getStabilityValue()/10);
        float marketSizeMult = 0.25f + 1f/market.getSize();

        float total = (ships*2 + supplies + marines + mechs + fuel) * stabilityMult;

        total *= marketSizeMult;
        return total;
    }
    public static float getPointsPerMarketPerTick(MarketAPI market)
    {
        return getMarketDefendCommodityValue(market) * ps_defendPointEconomyMult;
    }
    /**
     * Gets the accumulated defend points for the specific faction.
     * @param factionId
     * @return
     */
    public float getSpawnCounter(String factionId) {
        if (!spawnCounter.containsKey(factionId))
            spawnCounter.put(factionId, 0f);
        return spawnCounter.get(factionId);
    }
    public MarketAPI getSourceMarketForFleet(FactionAPI faction, Vector2f target, List<MarketAPI> markets, boolean allowHidden) {
        WeightedRandomPicker<MarketAPI> sourcePicker = new WeightedRandomPicker();
        WeightedRandomPicker<MarketAPI> sourcePickerBackup = new WeightedRandomPicker();
        for (MarketAPI market : markets) {
            if (market.getFaction() != faction) continue;
            if (!allowHidden && market.isHidden()) continue;
            if (market.hasCondition(Conditions.ABANDONED_STATION)) continue;
            if (market.getPrimaryEntity() instanceof CampaignFleetAPI) continue;
            if (!NexUtilsMarket.hasWorkingSpaceport(market)) continue;
            if (market.getSize() < 3) continue;
            if (RebellionIntel.isOngoing(market))
                continue;

            float weight = getMarketWeightForDefendSource(market, target);
            if (!PREFER_MILITARY_FOR_ORIGIN || Misc.isMilitary(market)) {
                sourcePicker.add(market, weight);
            }
            else {
                sourcePickerBackup.add(market, weight);
            }
        }
        MarketAPI originMarket = sourcePicker.pick();
        if (originMarket == null) originMarket = sourcePickerBackup.pick();
        return originMarket;
    }
    public MarketAPI getTargetMarketForDefendFleet(FactionAPI faction, List<MarketAPI> markets) {
        WeightedRandomPicker<MarketAPI> sourcePicker = new WeightedRandomPicker();
        WeightedRandomPicker<MarketAPI> sourcePickerBackup = new WeightedRandomPicker();
        for (MarketAPI market : markets) {
            if (market.getFaction() != faction) continue;
            if (market.isHidden()) continue;
            if (market.hasCondition(Conditions.ABANDONED_STATION)) continue;
            if (market.getPrimaryEntity() instanceof CampaignFleetAPI) continue;
            if (market.getStarSystem() == null) continue; //the market is in hyperspace -> no star system

            float weight = getMarketWeightForDefendTarget(market, null);
            if (!PREFER_MILITARY_FOR_ORIGIN || Misc.isMilitary(market)) {
                sourcePicker.add(market, weight);
            }
            else {
                sourcePickerBackup.add(market, weight);
            }
            for(PlanetAPI planet: market.getStarSystem().getPlanets()) {
                if(planet.getMarket() != null && planet.getMarket().getFaction().isHostileTo(faction)) {
                    weight *= 1.2f;
                }
            }
            for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(RaidIntel.class)) {
                if(Objects.equals(((RaidIntel) intel).getSystem().getId(), market.getStarSystem().getId())) {
                    weight *= 1.2f;
                }
            }
        }
        MarketAPI originMarket = sourcePicker.pick();
        if (originMarket == null) originMarket = sourcePickerBackup.pick();
        return originMarket;
    }
    public static float getMarketWeightForDefendTarget(MarketAPI market, Vector2f target) {
        float weight = 1;	 //marineStockpile;
        if (market.hasIndustry(Industries.PATROLHQ)) {
            weight *= 0.8f;
        }
        if (market.hasIndustry(Industries.MILITARYBASE)) {
            weight *= 0.6f;
        }
        if (market.hasIndustry(Industries.HIGHCOMMAND)) {
            weight *= 0.4f;
        }
        if (market.hasIndustry(Industries.MEGAPORT)) {
            weight *= 0.9f;
        }
        if (market.hasIndustry(Industries.HEAVYINDUSTRY)) {
            weight *= 1.2f;
        }
        if (market.hasIndustry(Industries.ORBITALWORKS)) {
            weight *= 1.5f;
        }
        if (market.hasIndustry(Industries.WAYSTATION)) {
            weight *= 1.2f;
        }
        weight *= 0.5f + 1f/market.getSize();
        weight *= 0.5f + 1f/market.getStabilityValue();

        if (target != null) {
            float dist = Misc.getDistance(market.getLocationInHyperspace(), target);
            if (dist < 5000.0F) {
                dist = 5000.0F;
            }
            // inverse square
            float distMult = 20000.0F / dist;
            distMult *= distMult;

            weight *= distMult;
        }

        return weight;
    }
    public static float getMarketWeightForDefendSource(MarketAPI market, Vector2f target) {
        float weight = 1;	 //marineStockpile;
        if (market.hasIndustry(Industries.PATROLHQ)) {
            weight *= 1.2f;
        }
        if (market.hasIndustry(Industries.MILITARYBASE)) {
            weight *= 1.5f;
        }
        if (market.hasIndustry(Industries.HIGHCOMMAND)) {
            weight *= 2;
        }
        if (market.hasIndustry(Industries.MEGAPORT)) {
            weight *= 1.5f;
        }
        if (market.hasIndustry(Industries.HEAVYINDUSTRY)) {
            weight *= 1.2f;
        }
        if (market.hasIndustry(Industries.ORBITALWORKS)) {
            weight *= 1.5f;
        }
        if (market.hasIndustry(Industries.WAYSTATION)) {
            weight *= 1.2f;
        }
        weight *= 0.5f + (0.5f * market.getSize() * market.getStabilityValue());

        if (target != null) {
            float dist = Misc.getDistance(market.getLocationInHyperspace(), target);
            if (dist < 5000.0F) {
                dist = 5000.0F;
            }
            // inverse square
            float distMult = 20000.0F / dist;
            distMult *= distMult;

            weight *= distMult;
        }

        return weight;
    }
    /**
     * Get the desired scaling fleet size.
     * @param attacker
     * @param target
     * @param variability Used to multiply the estimated strength by a random Gaussian value.
     * @param countAllHostile Count patrols only from markets belonging to the
     * target faction, or all that are hostile to the attacker?
     * @param maxMult Multiplier for maximum fleet size.
     * @return
     */
    public static float getWantedFleetSize(FactionAPI attacker, MarketAPI target,
                                           float variability, boolean countAllHostile, float maxMult)
    {
        FactionAPI targetFaction = target.getFaction();
        StarSystemAPI system = target.getStarSystem();

        float defenderStr = InvasionFleetManager.estimatePatrolStrength(attacker,
                countAllHostile ? null : targetFaction,
                system, variability);
        //log.info("\tPatrol strength: " + defenderStr);

        float stationStr = InvasionFleetManager.estimateStationStrength(target);
        //log.info("\tStation strength: " + stationStr);

        float defensiveStr = defenderStr + stationStr;
        defensiveStr *= DEFENCE_ESTIMATION_MULT;
        //log.info("\tModified total defense strength: " + defensiveStr);

        float strFromSize = target.getSize() * target.getSize() * 3;
        //log.info("\tMarket size modifier: " + strFromSize);
        defensiveStr += strFromSize;

        defensiveStr *= GENERAL_SIZE_MULT;

        float max = InvasionFleetManager.getMaxInvasionSize(attacker.getId(), maxMult);
        if (defensiveStr > max)
            defensiveStr = max;

        log.info("\tWanted defend fleet size for" + target.getName() + ": " + defensiveStr);
        return Math.max(defensiveStr, 30);
    }
    private void processDefendPatrolPoints() {
        SectorAPI sector = Global.getSector();
        FactionAPI solaceFaction = sector.getFaction(solace_ID);
        List<MarketAPI> markets = sector.getEconomy().getMarketsCopy();
        log.info("Starting defend fleet check");

        // slow down defend point accumulation when there are already a bunch of defend active
        float ongoingMod = (float)activeIntel.size() / MAX_ONGOING_INTEL;
        if (ongoingMod >= 1) return;
        ongoingMod = 1 - 0.75f * ongoingMod;

        // increment points by market
        HashMap<String, Float> pointsPerFaction = new HashMap<>();
        List<String> allies = DiplomacyManager.getFactionsFriendlyWithFaction(solace_ID);
        for (MarketAPI market : markets){
            String factionId = market.getFactionId();
            if (market.isHidden()) continue;
            if(!allies.contains(factionId)) {continue;}

            float mult = 1;

            if (!pointsPerFaction.containsKey(factionId))
                pointsPerFaction.put(factionId, 0f);

            float currPoints = pointsPerFaction.get(factionId);
            float addedPoints = getPointsPerMarketPerTick(market) * mult;

            currPoints += addedPoints;
            market.getMemoryWithoutUpdate().set(MEMORY_KEY_POINTS_LAST_TICK, addedPoints, 3);
            pointsPerFaction.put(factionId, currPoints);
        }

        for (String factionId: allies) {
            boolean isPirateFaction = NexUtilsFaction.isPirateFaction(factionId);
            FactionAPI faction = sector.getFaction(factionId);
            float mult = 0.25f;

            //pirate will have a bonus
            List<String> enemies = DiplomacyManager.getFactionsAtWarWithFaction(factionId, true, false, false);
            if (enemies.isEmpty()) continue;
            mult += 0.25f * enemies.size();
            if (mult > 1) mult = 1;

            // increment defend counter for faction
            if (!pointsPerFaction.containsKey(factionId))
                pointsPerFaction.put(factionId, 0f);

            float increment = pointsPerFaction.get(factionId);
            float counter = getSpawnCounter(factionId);

            increment *= mult;
            increment *= ps_defendPointEconomyMult;
            increment *= ongoingMod;
            counter += increment;

            faction.getMemoryWithoutUpdate().set(MEMORY_KEY_POINTS_LAST_TICK, increment, 3);
            float pointsRequired = ps_pointsRequiredForDefendFleet;
            boolean canSpawn = counter > pointsRequired;
            log.info("Current defend points:" + counter + factionId);

            if (!canSpawn)
            {
                spawnCounter.put(factionId, counter);
            }
            else
            {
                // okay, we can SEND DEFEND
                OffensiveFleetIntel intel;
                MarketAPI target = getTargetMarketForDefendFleet(faction, markets);
                MarketAPI sourceSolaceDefender = getSourceMarketForFleet(sector.getFaction(solace_ID), null, markets, false);
                if(sourceSolaceDefender != null) {
                    float fp = getWantedFleetSize(solaceFaction, sourceSolaceDefender, 0.15f, false, 1);
                    float organizeTime = InvasionFleetManager.getOrganizeTime(fp);
                    fp *= InvasionFleetManager.getInvasionSizeMult(factionId);
                    fp *= 1;
                    intel = new DefenseFleetIntel(
                            solaceFaction,
                            sourceSolaceDefender,
                            target,
                            fp,
                            organizeTime
                    );
                    intel.init();
                    if (intel != null)
                    {
                        counter -= getInvasionPointCost(intel);
                        lifetimeDefends++;
                        spawnCounter.put(factionId, counter);
                    }
                }
            }

        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector().isInNewGameAdvance())
            return;
        if (TutorialMissionIntel.isTutorialInProgress())
            return;

        float days = Global.getSector().getClock().convertToDays(amount);

        tracker.advance(days);
        boolean elapsed = this.tracker.intervalElapsed();

        // if still in time out period, do nothing further
        if (daysElapsed < ps_solaceDefendTimeoutPeriod)
        {
            daysElapsed += days;
            return;
        }

        if (!elapsed) {
            return;
        }
        if(!ps_solaceDefend) return; //disable if needed
        List<ps_defendpatrolfleetintel> remove = new LinkedList();
        for (ps_defendpatrolfleetintel intel : activeIntel) {
            if (intel.isEnded() || intel.isEnding()) {
                remove.add(intel);
            }
        }
        this.activeIntel.removeAll(remove);

        processDefendPatrolPoints();
    }
    public static ps_defendpatrolfleetmanager getManager()
    {
        Map<String, Object> data = Global.getSector().getPersistentData();
        ps_defendpatrolfleetmanager manager = (ps_defendpatrolfleetmanager)data.get(MANAGER_MAP_KEY);
        return manager;
    }

    public static ps_defendpatrolfleetmanager create()
    {
        ps_defendpatrolfleetmanager manager = getManager();
        log.info("Registered Solace Defend/Patrol manager");
        if (manager != null)
            return manager;

        Map<String, Object> data = Global.getSector().getPersistentData();
        //save compatibility
        Global.getSector().getMemoryWithoutUpdate().set(PS_DEFENDPATROLFLEETMANAGER_ACTIVED_IN_SAVE, true);
        manager = new ps_defendpatrolfleetmanager();
        data.put(MANAGER_MAP_KEY, manager);
        return manager;
    }

}
