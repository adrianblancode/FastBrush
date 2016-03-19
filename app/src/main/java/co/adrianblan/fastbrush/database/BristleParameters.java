package co.adrianblan.fastbrush.database;

/**
 * Parameters for the bristles in a brush snapshot.
 */
public class BristleParameters {

    // Angle of the bristle spread
    private float spreadAngle;

    // The planar distance the bristle end is from the handle
    private float planarDistanceFromHandle;

    // Length of the control points of the bezier curves
    private float upperControlPointLength;
    private float lowerControlPointLength;

    public BristleParameters() {}

    public BristleParameters(float planarDistanceFromHandle, float spreadAngle) {
        set(planarDistanceFromHandle, spreadAngle);
    }

    public void set(float planarDistanceFromHandle, float spreadAngle) {
        this.spreadAngle = spreadAngle;
        this.planarDistanceFromHandle = planarDistanceFromHandle;
    }

    public float getSpreadAngle() {
        return spreadAngle;
    }

    public void setSpreadAngle(float spreadAngle) {
        this.spreadAngle = spreadAngle;
    }

    public float getPlanarDistanceFromHandle() {
        return planarDistanceFromHandle;
    }

    public void setPlanarDistanceFromHandle(float planarDistanceFromHandle) {
        this.planarDistanceFromHandle = planarDistanceFromHandle;
    }

    public float getUpperControlPointLength() {
        return upperControlPointLength;
    }

    public void setUpperControlPointLength(float upperControlPointLength) {
        this.upperControlPointLength = upperControlPointLength;
    }

    public float getLowerControlPointLength() {
        return lowerControlPointLength;
    }

    public void setLowerControlPointLength(float lowerControlPointLength) {
        this.lowerControlPointLength = lowerControlPointLength;
    }
}