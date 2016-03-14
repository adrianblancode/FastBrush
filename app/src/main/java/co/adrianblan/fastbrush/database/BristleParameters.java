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

    public BristleParameters(float spreadAngle, float planarDistanceFromHandle,
                             float upperControlPointLength, float lowerControlPointLength) {

        set(spreadAngle, planarDistanceFromHandle, upperControlPointLength, lowerControlPointLength);
    }

    public void set(float spreadAngle, float planarDistanceFromHandle,
               float upperControlPointLength, float lowerControlPointLength) {
        this.spreadAngle = spreadAngle;
        this.planarDistanceFromHandle = planarDistanceFromHandle;
        this.upperControlPointLength = upperControlPointLength;
        this.lowerControlPointLength = lowerControlPointLength;
    }

    public float getSpreadAngle() {
        return spreadAngle;
    }

    public float getPlanarDistanceFromHandle() {
        return planarDistanceFromHandle;
    }

    public float getUpperControlPointLength() {
        return upperControlPointLength;
    }

    public float getLowerControlPointLength() {
        return lowerControlPointLength;
    }
}
