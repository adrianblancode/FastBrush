package co.adrianblan.fastbrush.database;

/**
 * Key for the database.
 *
 * Details parameters of the brush handle, to retrieve parameters of the brush bristles.
 */
public class BrushKey {

    // Angle of the brush in degrees
    private float angle;

    // Height of the brush handle, normalized to [0, 1]
    private float height;

    public BrushKey() {}

    public BrushKey(float angle, float height) {
        set(angle, height);
    }

    public void set(float angle, float height) {
        this.angle = angle;
        this.height = height;
    }

    public float getAngle() {
        return angle;
    }

    public float getHeight() {
        return height;
    }

    public float distance(BrushKey key) {
        return (Math.abs(getAngle() - key.getAngle()) / 45f) + Math.abs(getHeight() - key.getHeight());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrushKey)) {
            return false;
        } else if (obj == this) {
            return true;
        }

        BrushKey bk = (BrushKey) obj;

        // Return whether all values match
        return (bk.getAngle() == this.angle && bk.getHeight() == this.height);
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(angle) ^ Float.floatToIntBits(height);
    }
}
