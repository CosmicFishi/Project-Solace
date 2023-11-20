package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.SectorManager;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.projectsolace.campaign.ps_sodalityfleetadjustment;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPushClientAttrib;

public class ps_util {
    public static Logger log = Global.getLogger(ps_util.class);
    private static final Random rng = new Random();
    public static boolean checkFactionAlive(String factionId) {
        log.info("Checking " + factionId + " is dead: " + Misc.getFactionMarkets(factionId).isEmpty());
        if(Misc.getFactionMarkets(factionId).isEmpty()) {
            return true;
        }
        return false;
    }
    //Credits to RuddyGreat
    /**
     * Draws a textured ring of a given thickness at a given location. winding & scrolling is counter-clockwise; sprites must be a power of 2 wide for best results.
     * @param loc the center of the ring
     * @param radius the radius of the ring
     * @param thickness the thickness of the ring
     * @param numPoints the number of points around the edge
     * @param numRepetitions the number of times that the sprite should repeat, along the sprite's +X axis
     * @param scrollSpeed the speed at which the sprite scrolls, along the sprite's +X axis
     * @param sprite the sprite
     */
    public static void drawTexturedRing(Vector2f loc, float radius, float thickness, int numPoints, int numRepetitions, float scrollSpeed, SpriteAPI sprite, Color color) {

        float angleDiffPerPoint = 360f / numPoints;
        float scrollPerPoint = (float) numRepetitions / numPoints;
        float scrollFromTime = Global.getCombatEngine().getTotalElapsedTime(false) * -scrollSpeed;
        float outertexScroll = sprite.getTextureHeight();

        float[] vertices = new float[(numPoints + 1) * 4];
        float[] texcoords = new float[(numPoints + 1) *  4];

        //counter-clockwise winding starting from 3 oclock
        for (int i = 0; i < numPoints + 1; i++) {

            Vector2f innerLoc = MathUtils.getPointOnCircumference(loc, radius - (thickness / 2f), (angleDiffPerPoint * i));
            Vector2f outerLoc = MathUtils.getPointOnCircumference(loc, radius + (thickness / 2f), (angleDiffPerPoint * i));

            int indexReal = i * 4;
            vertices[indexReal] = innerLoc.x; //inner vec x
            vertices[indexReal + 1] = innerLoc.y; //inner vec y
            vertices[indexReal + 2] = outerLoc.x; //outer vec x
            vertices[indexReal + 3] = outerLoc.y; //outer vec y

            texcoords[indexReal] = scrollFromTime + (scrollPerPoint * i) ; //inner vec x
            texcoords[indexReal + 1] = 0; //inner vec y
            texcoords[indexReal + 2] = scrollFromTime + (scrollPerPoint * i); //outer vec x
            texcoords[indexReal + 3] = outertexScroll; //outer vec y

        }

        sprite.bindTexture();
        drawPoints(vertices, texcoords, color, 1, GL_QUAD_STRIP);
    }
    //Credits to RuddyGreat
    /**
     * @param vertices array of vertices, elements n & n + 1 make up vertex n
     * @param texCoords array of tex coords, elements n & n + 1 are applied to vertex n
     * @param color colour for the entire shape
     * @param alphaMult an additional alpha mult
     * @param mode the openGL mode to use to render the shape
     */
    public static void drawPoints(float[] vertices, float[] texCoords, Color color, float alphaMult, int mode) {

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(texCoords.length);

        vertexBuffer.put(vertices);
        vertexBuffer.flip();
        texCoordBuffer.put(texCoords);
        texCoordBuffer.flip();

        Misc.setColor(color, alphaMult);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, 0, vertexBuffer);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glTexCoordPointer(2, 0, texCoordBuffer);
        glDrawArrays(mode, 0, vertices.length / 2);
        glPopClientAttrib();
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }
    /**
     * credits to Lyravega
     * @param color to work with
     * @param ra red adjustment
     * @param ga green adjustment
     * @param ba blue adjustment
     * @return adjusted colour
     */
    public static final Color adjustColour(Color color, Integer ra, Integer ga, Integer ba) {
        return color != null ? new Color(
                ra != null ? Math.min(Math.max(color.getRed()+ra, 0), 255) : color.getRed(),
                ga != null ? Math.min(Math.max(color.getGreen()+ga, 0), 255) : color.getGreen(),
                ba != null ? Math.min(Math.max(color.getBlue()+ba, 0), 255) : color.getBlue(),
                color.getAlpha()
        ) : Color.WHITE;
    }
//
}
