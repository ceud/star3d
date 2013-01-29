package Star3D;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Craig `Ceud` Allan
 */
public class Star2D {
    public float x, y;
    public String name, classification;

    public static int DIAMETER = 5;
    public static Map<String, Color> COLOURS = Star2D.getColours();

    public Star2D(float X, float Y, String NAME, String CLASSIFICATION) {
        x = X;
	y = Y;
	name = NAME;
	classification = CLASSIFICATION;
    }

    private static Map<String, Color> getColours() {
        Map<String, Color> hash = new HashMap<String, Color>();
        hash.put("O", new Color(64, 64, 255));
        hash.put("B", new Color(128, 128, 255));
        hash.put("A", new Color(255, 255, 255));
        hash.put("F", new Color(255, 255, 128));
        hash.put("G", new Color(255, 255, 64));
        hash.put("K", new Color(255, 128, 0));
        hash.put("M", new Color(255, 0, 0));
        hash.put("L", new Color(128, 0, 0));
        hash.put("D", new Color(250, 250, 250));

        return hash;
    }

    public Color getColour() {
        return (Color) Star2D.COLOURS.get(classification);
    }

    public static int getDiameter() {
        return Star2D.DIAMETER;
    }

    public static double getRadius() {
        return (double) Star2D.DIAMETER / 2;
    }

    public static int displayOffset() {
        return (int) (Star2D.DIAMETER - 1) / 2;
    }

    public boolean intersects(int X, int Y, int offset) {
        if (X >= x - offset && X <= x + offset
                && Y >= y - offset && Y <= y + offset) {
            return true;
        }

        return false;
    }
}