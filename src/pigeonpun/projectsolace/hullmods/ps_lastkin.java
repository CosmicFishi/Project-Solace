package pigeonpun.projectsolace.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.combat.CombatUtils;
import pigeonpun.projectsolace.com.ps_ringrenderer;

import java.awt.*;
import java.util.List;

public class ps_lastkin extends BaseHullMod {
    public static final Color RING_COLOR_SHIP = new Color(15, 187, 110, 155);
    public static final Color MISSILE_SLOWDOWN_COLOR = new Color(255,187,95,155);
    public static final float MISSILE_SLOW_RADIUS = 800f;
    public static final String ZERO_FLUX_ACTIVE_KEY = "ps_lastkinzeroflux";
    public static final float ZERO_FLUX_LEVEL = 100f;
    public static final float VELOCITY_REDUCED = 20f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }
        if(ship.isPhased()) {
            ship.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat(ZERO_FLUX_ACTIVE_KEY, ZERO_FLUX_LEVEL * 0.01f);
        } else {
            ship.getMutableStats().getZeroFluxMinimumFluxLevel().unmodify(ZERO_FLUX_ACTIVE_KEY);
        }
        Global.getCombatEngine().addLayeredRenderingPlugin(new ps_ringrenderer(ship, MISSILE_SLOW_RADIUS, RING_COLOR_SHIP, 3f, 40));
        //slowdown missiles
        List<MissileAPI> missilesNearby = CombatUtils.getMissilesWithinRange(ship.getLocation(), MISSILE_SLOW_RADIUS);
        for (MissileAPI missile: missilesNearby) {
            if(missile != null && missile.getOwner() == 1) {
                missile.getVelocity().scale((float) (100 - 20) / 100);
                missile.setJitter(this, MISSILE_SLOWDOWN_COLOR, 1, 15, 2f, 5f);
            }
        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.MOUNT_UNIVERSAL;
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        LabelAPI label = tooltip.addPara("Description here", opad, h, "");

        tooltip.addSectionHeading("Effects", Alignment.MID, opad);

        label = tooltip.addPara("When ship is phased, zero flux speed bonus applied at any flux level", opad, h,"");

        label = tooltip.addPara("An aura circles the ship, any missiles enter the zone will have its velocity reduced by %s", opad, h,"" + VELOCITY_REDUCED + "%");
        label.setHighlight("" + VELOCITY_REDUCED + "%");
        label.setHighlightColors(good);
    }
}
