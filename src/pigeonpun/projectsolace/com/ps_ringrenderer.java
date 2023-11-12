package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;

import java.awt.*;
import java.util.EnumSet;

public class ps_ringrenderer extends BaseCombatLayeredRenderingPlugin {
    CombatEntityAPI entity;
    private float radius = 0;
    private Color RING_COLOR;
    float ringThiccness = 8f;
    int ringPoints = 30;
    public ps_ringrenderer(CombatEntityAPI entity, float radius, Color color) {
        this.entity = entity;
        this.radius =radius;
        this.RING_COLOR = color;
    }
    public ps_ringrenderer(CombatEntityAPI entity, float radius, Color color, float ringThiccness, int ringPoints) {
        this.entity = entity;
        this.radius =radius;
        this.RING_COLOR = color;
        this.ringThiccness = ringThiccness;
        this.ringPoints = ringPoints;
    }

    @Override
    public float getRenderRadius() {
        return 1000000f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.BELOW_SHIPS_LAYER);
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (Global.getCombatEngine().isPaused()) return;
        if(this.radius > 0) {
            SpriteAPI sprite = Global.getSettings().getSprite("fx", "ps_ring_core");
            ps_util.drawTexturedRing(
                    entity.getLocation(),
                    radius,
                    ringThiccness,
                    ringPoints,
                    1,
                    0.2f,
                    sprite,
                    RING_COLOR
            );
        }
    }

    @Override
    public boolean isExpired() {
        if(entity instanceof MissileAPI) {
            return !Global.getCombatEngine().isEntityInPlay(entity);
        }
        return entity.isExpired();
    }
}
