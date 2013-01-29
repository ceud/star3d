package Star3D;

import java.io.Serializable;

/**
 *
 * @author Ceud
 */
public class Star3DStore implements Serializable {
    public float x, y, z;
    public String name, classification;

    public Star3DStore(float X, float Y, float Z, String NAME, String CLASSIFICATION) {
        x = X;
	y = Y;
	name = NAME;
	classification = CLASSIFICATION;
	z = Z;
    }

    @Override
    public String toString() {
        return "[Star3D: " + x + ", " + y + ", " + z + ", " + name + ", " + classification + "]";
    }
}
