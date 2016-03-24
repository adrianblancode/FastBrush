package co.adrianblan.fastbrush.database;

/**
 * Parameters for the bristles in a brush snapshot.
 */
public class BristleParameters {

    // The planar distance the bristle end is from the handle
    public float planarDistanceFromHandle;

    // The length of the imprint on the plane
    public float planarImprintLength;

    // Length of the control points of the bezier curves
    public float upperControlPointLength;
    public float lowerControlPointLength;

    public BristleParameters() {}

    public BristleParameters(float planarDistanceFromHandle, float planarImprintLength,
                             float upperControlPointLength, float lowerControlPointLength) {
        set(planarDistanceFromHandle, planarImprintLength,
                upperControlPointLength, lowerControlPointLength);
    }

    public BristleParameters(BristleParameters b) {
        set(b);
    }

    public void set(BristleParameters b) {
        set(b.getPlanarDistanceFromHandle(), b.getPlanarImprintLength(),
                b.getUpperControlPointLength(), b.getLowerControlPointLength());
    }

    public void set(float planarDistanceFromHandle, float planarImprintLength,
                    float upperControlPointLength, float lowerControlPointLength) {
        this.planarDistanceFromHandle = planarDistanceFromHandle;
        this.planarImprintLength = planarImprintLength;
        this.upperControlPointLength = upperControlPointLength;
        this.lowerControlPointLength = lowerControlPointLength;
    }

    public float getPlanarImprintLength() {
        return planarImprintLength;
    }

    public void setPlanarImprintLength(float planarImprintLength) {
        this.planarImprintLength = planarImprintLength;
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

    @Override
    public String toString(){
        return "distance: " + planarDistanceFromHandle;
    }
}