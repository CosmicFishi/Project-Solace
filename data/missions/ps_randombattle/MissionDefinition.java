package data.missions.ps_randombattle;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.missions.PS_BaseRandomBattle;
import org.lazywizard.lazylib.MathUtils;

public class MissionDefinition extends PS_BaseRandomBattle {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        super.defineMission(api);

        String playerFaction = "projectsolace";
        String enemyFaction = FACTIONS.pick().toString();

        double r = Math.random();

        api.addBriefingItem("Defeat all enemy forces");

        int size = 5 + (int) ((float) Math.random() * 55);
        float width, height;

        width = (12000f + 10000f * (size / 40f)) * ((float) Math.random() * 0.4f + 0.6f);
        height = (12000f + 10000f * (size / 40f)) * ((float) Math.random() * 0.4f + 0.6f);
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        api.initFleet(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getEntityNamePrefix(), FleetGoal.ATTACK, false, size / 8);
        api.initFleet(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getEntityNamePrefix(), FleetGoal.ATTACK, true, size / 8);

        int objectiveCount = (int) Math.floor(size * ((float) Math.random() * 0.75f + 0.5f) / 8f);

        String battleSize;
        if (size >= 50) {
            battleSize = "Armageddon";
        } else if (size >= 35) {
            battleSize = "War";
        } else if (size >= 20) {
            battleSize = "Assault";
        } else if (size >= 10) {
            battleSize = "Raid";
        } else {
            battleSize = "Skirmish";
        }
        switch (objectiveCount) {
            case 0:
                api.addBriefingItem("Battle size: " + battleSize);
                break;
            case 1:
                api.addBriefingItem("Battle size: " + battleSize + "  -  " + objectiveCount + " objective");
                break;
            default:
                api.addBriefingItem("Battle size: " + battleSize + "  -  " + objectiveCount + " objectives");
                break;
        }

        int playerSize = (int) (size * 5f * 1f);
        int enemySize = (int) (size * 5f * 1f * 1.05f + 5f);

        WeightedRandomPicker<String> fleetTypes = new WeightedRandomPicker<String>();

        if (size < 30) {
            fleetTypes.add("convoy", 2f);
        }
        if (size >= 15 && size < 45) {
            fleetTypes.add("blockade-runners", 1f);
        }
        if (size >= 30) {
            fleetTypes.add("invasion fleet", 1f);
        }

        String playerFleet = fleetTypes.pick().toString();
        String enemyFleet = fleetTypes.pick().toString();

        float playerQualityFactor = MathUtils.getRandomNumberInRange(0f, 1.25f);
        float enemyQualityFactor = MathUtils.getRandomNumberInRange(0f, 1.25f);

        api.setFleetTagline(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getDisplayName() + " " + playerFleet
                + " (" + playerSize + " points)" + " (QF " + String.format("%.2f", playerQualityFactor) + ")");
        api.setFleetTagline(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getDisplayName() + " " + enemyFleet
                + " (" + enemySize + " points)" + " (QF " + String.format("%.2f", enemyQualityFactor) + ")");

        FleetDataAPI playerFleetData = generateFleet(playerSize, playerQualityFactor, 0f, -1, FleetSide.PLAYER, playerFaction, playerFleet, api, MathUtils.getRandom().nextLong(), false);
        FleetDataAPI enemyFleetData = generateFleet(enemySize, enemyQualityFactor, 0f, -1, FleetSide.ENEMY, enemyFaction, enemyFleet, api, MathUtils.getRandom().nextLong(), false);

        float friendlyDP = 0f;
        float friendlyFP = 0f;
        for (FleetMemberAPI member : playerFleetData.getMembersListCopy()) {
            friendlyDP += member.getDeploymentPointsCost();
            friendlyFP += member.getFleetPointCost();
        }

        float enemyDP = 0f;
        float enemyFP = 0f;
        for (FleetMemberAPI member : enemyFleetData.getMembersListCopy()) {
            enemyDP += member.getDeploymentPointsCost();
            enemyFP += member.getFleetPointCost();
        }

        float distance = Math.abs(enemyDP - friendlyDP) + Math.abs(enemyFP - friendlyFP);

        api.addBriefingItem("Match inequality: " + Math.round(distance));

        while (objectiveCount > 0) {
            String type = OBJECTIVE_TYPES[rand.nextInt(OBJECTIVE_TYPES.length)];
            int configuration;
            r = Math.random();
            if (r < 0.5) {
                configuration = 0;
            } else if (r < 0.75) {
                configuration = 1;
            } else {
                configuration = 2;
            }

            if (objectiveCount == 1) {
                r = Math.random();
                if (r < 0.75) {
                    api.addObjective(0f, 0f, type);
                } else if (r < 0.875) {
                    float x = (width * 0.075f + width * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    api.addObjective(x, 0f, type);
                } else {
                    float y = (height * 0.075f + height * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    api.addObjective(0f, y, type);
                }

                objectiveCount -= 1;
            } else {
                float x1, x2, y1, y2;
                r = Math.random();
                if ((r < 0.75 && configuration == 0) || (r < 0.5 && configuration == 1) || (r < 0.25 && configuration == 2)) {
                    float theta = (float) (Math.random() * Math.PI);
                    double radius = Math.min(width, height);
                    radius = radius * 0.1 + radius * 0.3 * Math.random();
                    x1 = (float) (Math.cos(theta) * radius);
                    y1 = (float) -(Math.sin(theta) * radius);
                    x2 = -x1;
                    y2 = -y1;
                } else if ((r < 0.875 && configuration == 0) || (r < 0.75 && configuration == 1) || (r < 0.625 && configuration == 2)) {
                    x1 = (width * 0.075f + width * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    x2 = x1;
                    y1 = (height * 0.075f + height * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    y2 = -y1;
                } else {
                    x1 = (width * 0.075f + width * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    x2 = -x1;
                    y1 = (height * 0.075f + height * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    y2 = y1;
                }

                r = Math.random();
                if (r < 0.75) {
                    api.addObjective(x1, y1, type);
                    api.addObjective(x2, y2, type);
                } else {
                    api.addObjective(x1, y1, type);
                    type = OBJECTIVE_TYPES[rand.nextInt(OBJECTIVE_TYPES.length)];
                    api.addObjective(x2, y2, type);
                }

                objectiveCount -= 2;
            }
        }

        int numNebula = 15;
        boolean inNebula = Math.random() > 0.5;
        if (inNebula) {
            numNebula = 100;
        }

        for (int i = 0; i < numNebula; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            if (inNebula) {
                radius += 100f + 500f * (float) Math.random();
            }
            api.addNebula(x, y, radius);
        }

        float numAsteroidsWithinRange = Math.max(0f, MathUtils.getRandomNumberInRange(-10f, 20f));
        int numAsteroids = Math.min(400, (int) ((numAsteroidsWithinRange + 1f) * 20f));

        api.addAsteroidField(0, 0, (float) Math.random() * 360f, width, 20f, 70f, numAsteroids);

        int numRings = 0;
        if (Math.random() > 0.75) {
            numRings++;
        }
        if (Math.random() > 0.75) {
            numRings++;
        }
        if (numRings > 0) {
            int numRingAsteroids = (int) (numRings * 300 + (numRings * 600f) * (float) Math.random());
            if (numRingAsteroids > 1500) {
                numRingAsteroids = 1500;
            }
            api.addRingAsteroids(0, 0, (float) Math.random() * 360f, width, 100f, 200f, numRingAsteroids);
        }

        String planet = PLANETS.get((int) (Math.random() * PLANETS.size())).toString();
        float radius = 25f + (float) Math.random() * (float) Math.random() * 500f;

        api.addPlanet(0, 0, radius, planet, 0f, true);
        if (planet.contentEquals("wormholeUnder")) {
            api.addPlanet(0, 0, radius, "wormholeA", 0f, true);
            api.addPlanet(0, 0, radius, "wormholeB", 0f, true);
            api.addPlanet(0, 0, radius, "wormholeC", 0f, true);
        }

        api.getContext().aiRetreatAllowed = false;
        api.getContext().enemyDeployAll = true;
        api.getContext().fightToTheLast = true;
    }

}
