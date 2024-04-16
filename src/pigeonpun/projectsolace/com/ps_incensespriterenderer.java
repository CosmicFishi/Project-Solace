package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.LazyLib;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.hullmods.ps_incensemanufactured;
import pigeonpun.projectsolace.scripts.projectsolaceplugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class ps_incensespriterenderer extends BaseCombatLayeredRenderingPlugin {
    private ShipAPI ship;
    private final float spawnJitterTimerFrom = 0.2f;
    private final float spawnJitterTimerTo = 0.5f;
    private final float spawnJitterTimerWait = 0.6f;
    public Logger log = Global.getLogger(ps_incensespriterenderer.class);
    public ps_incensespriterenderer(ShipAPI ship) {
        this.ship = ship;
    }
    private static final int FRIGATE_GLITCH_CUT = 2;
    private static final int DESTROYER_GLITCH_CUT = 4;
    private static final int CRUISER_GLITCH_CUT = 6;
    private static final int CAPITAL_GLITCH_CUT = 8;
    private static final String LIST_GLITCH_DATA_ID = "ps_listGlitchData";
    private static final String GLITCH_WEAPON_SLOT_ID = "ps_glitch"; //need this to find the center of the sprite

    @Override
    public float getRenderRadius() {
        return 1000000f;
    }

    @Override
    public boolean isExpired() {
        return ship.isExpired() || !ship.isAlive();
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER);
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        //todo: Change this so the glitvh effect is base on a percentage chance of happening and per section of the glitch sprite have a percentage chance of not showing
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        float ps_spawnjitter_timer = 0;
        List<ps_incenseGlitchData> listGlitches = new ArrayList<>();

        if (customCombatData.get("ps_spawnjitter_timer" + ship.getId()) instanceof Float)
            ps_spawnjitter_timer = (float) customCombatData.get("ps_spawnjitter_timer" + ship.getId());
        if (customCombatData.get(LIST_GLITCH_DATA_ID + ship.getId()) instanceof List
                && ((List) customCombatData.get(LIST_GLITCH_DATA_ID + ship.getId())).get(0) != null
                && ((List) customCombatData.get(LIST_GLITCH_DATA_ID + ship.getId())).get(0) instanceof ps_incenseGlitchData
        ) {
            listGlitches = (List<ps_incenseGlitchData>) customCombatData.get(LIST_GLITCH_DATA_ID + ship.getId());
        }
        if(listGlitches.isEmpty()) {
            listGlitches = new ArrayList<>(createNewListGlitches(ship));
        }
        //timer advance is in ps_incensemanufactured.java
        if(ps_spawnjitter_timer > spawnJitterTimerFrom && ps_spawnjitter_timer < spawnJitterTimerTo) {
            //do glitches
            Vector2f currentShipCenter = null;
            for(WeaponAPI weapon: ship.getAllWeapons()) {
                if(weapon.getType() == WeaponAPI.WeaponType.DECORATIVE && weapon.getSlot().getId().equals(GLITCH_WEAPON_SLOT_ID)) {
                    currentShipCenter = weapon.getLocation();
                    break;
                }
            }
            if(currentShipCenter == null) {
                log.warn("Can't find Glitch deco on " + ship.getHullSpec().getBaseHullId() + ", not rendering glitch");
            } else {
                for (ps_incenseGlitchData glitchData: listGlitches) {
                    renderGlitchFx(ship, currentShipCenter, glitchData.glitchOffsetX, glitchData.glitchOffsetY, glitchData.spriteWidth, glitchData.spriteHeight, glitchData.offsetX ,glitchData.color);
//                    Global.getCombatEngine().addHitParticle(currentShipCenter, Misc.ZERO, 30f, 1f, 5, Color.PINK);
                }
            }
        } else {
            if(ps_spawnjitter_timer > (spawnJitterTimerTo + spawnJitterTimerWait)) {
                ps_spawnjitter_timer = 0;
                //reset glitch data
                listGlitches = new ArrayList<>(createNewListGlitches(ship));
            }
        }
        customCombatData.put(LIST_GLITCH_DATA_ID + ship.getId(), listGlitches);
        customCombatData.put("ps_spawnjitter_timer" + ship.getId(), ps_spawnjitter_timer);
    }
    public List<ps_incenseGlitchData> createNewListGlitches(ShipAPI ship) {
        List<ps_incenseGlitchData> newList = new ArrayList<>();
        int shipGlitchCut = 1;
        if (ship.getHullSize().equals(ShipAPI.HullSize.FRIGATE)){
            shipGlitchCut = FRIGATE_GLITCH_CUT;
        }
        if (ship.getHullSize().equals(ShipAPI.HullSize.DESTROYER)){
            shipGlitchCut = DESTROYER_GLITCH_CUT;
        }
        if (ship.getHullSize().equals(ShipAPI.HullSize.CRUISER)){
            shipGlitchCut = CRUISER_GLITCH_CUT;
        }
        if (ship.getHullSize().equals(ShipAPI.HullSize.CAPITAL_SHIP)){
            shipGlitchCut = CAPITAL_GLITCH_CUT;
        }
        int newListSectionCountRed = MathUtils.getRandomNumberInRange(shipGlitchCut, shipGlitchCut + 4);
        int newListSectionCountBlue = MathUtils.getRandomNumberInRange(shipGlitchCut, shipGlitchCut + 4);
        int newListSectionCountGreen = MathUtils.getRandomNumberInRange(shipGlitchCut, shipGlitchCut + 4);
        for (int i = 0; i < newListSectionCountRed; i++) {
            //to get height of each "cut" section
            float regionSelected = (float) 1 / newListSectionCountRed;
            newList.add(
                    new ps_incenseGlitchData(
                            0f,
                            regionSelected * i,
                            1f,
                            regionSelected,
                            MathUtils.getRandomNumberInRange(-20f, 20f),
                            Color.red
                    )
            );
        }
        for (int i = 0; i < newListSectionCountBlue; i++) {
            float regionSelected = (float) 1 / newListSectionCountBlue;
            newList.add(
                    new ps_incenseGlitchData(
                            0f,
                            regionSelected * i,
                            1f,
                            regionSelected,
                            MathUtils.getRandomNumberInRange(-20f, 20f),
                            Color.blue
                    )
            );
        }
        for (int i = 0; i < newListSectionCountGreen; i++) {
            float regionSelected = (float) 1 / newListSectionCountGreen;
            newList.add(
                    new ps_incenseGlitchData(
                            0f,
                            regionSelected * i,
                            1f,
                            regionSelected,
                            MathUtils.getRandomNumberInRange(-20f, 20f),
                            Color.green
                    )
            );
        }
        return newList;
    }
    /**
     * @param ship Ship that will take the sprite from
     * @param location location to spawn the glitch
     * @param regionX control offset X axis
     * @param regionY control offset Y axist
     * @param spriteWidth width of the sprite rendered. Ex: 1 -> render full sprite (take offset into calculation)
     * @param spriteHeight height of the sprite rendered. Ex: 0.5 -> render half the sprite (take offset into calculation)
     * @param extraGlitchOffset glitch offset X, in pixel
     * @param color Sprite color (Duh)
     * Make sure in settings.json, category "ship" have defined paths to your ship's sprite as ship_id
     * Make sure there is a deco weapon at the center of the ship (yes i know, its painful ;-;)
     * Selected region work by combining both region X and sprite width together (magic, i know. Thanks Alex)
     */
    //Thanks to PureTilt for guiding my baby ass into understanding how to do this
    protected void renderGlitchFx(ShipAPI ship, Vector2f location ,float regionX, float regionY, float spriteWidth, float spriteHeight, float extraGlitchOffset ,Color color) {
        SpriteAPI sprite = Global.getSettings().getSprite("ships", ship.getHullSpec().getBaseHullId());
        if(sprite != null) {
            sprite.setColor(color);
            sprite.setAngle(ship.getFacing() - 90);
            if(projectsolaceplugin.ps_epilepsy) {
                sprite.setAdditiveBlend();
            }
            sprite.setAlphaMult(projectsolaceplugin.ps_glitchAlphaMult);
            Vector2f offsetLocation = MathUtils.getPointOnCircumference(location, extraGlitchOffset, ship.getFacing() - 90);
            sprite.renderRegionAtCenter(
                    offsetLocation.x,
                    offsetLocation.y,
                    regionX, //control region x, 0 - 1
                    regionY, //control region y, 0 - 1
                    spriteWidth, // width of the sprite rendered. Ex: 1 -> render full sprite (take offset into calculation)
                    spriteHeight //height of the sprite rendered. Ex: 0.5 -> render half the sprite
            );
        }
    }
    public class ps_incenseGlitchData {
        public float glitchOffsetX;
        public float glitchOffsetY;
        public float spriteWidth;
        public float spriteHeight;
        public float offsetX;
        public Color color;
        ps_incenseGlitchData(float glitchOffsetX, float glitchOffsetY, float spriteWidth, float spriteHeight, float offsetX, Color color) {
            //only needed to run first turn
            this.color = color;
            this.glitchOffsetX = glitchOffsetX;
            this.glitchOffsetY = glitchOffsetY;
            this.spriteHeight = spriteHeight;
            this.spriteWidth = spriteWidth;
            this.offsetX = offsetX;
        }
    }
}
