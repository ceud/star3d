package Star3D;

/**
 * @author Ceud
 */
public class Star3D extends Star2D {
    public float z;

    public Star3D(float X, float Y, float Z, String NAME, String CLASSIFICATION) {
        super(X, Y, NAME, CLASSIFICATION);
	z = Z;
    }
}
