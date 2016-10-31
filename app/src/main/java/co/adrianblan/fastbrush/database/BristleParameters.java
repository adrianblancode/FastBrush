package co.adrianblan.fastbrush.database;

/**
 * Parameters for the bristles in a brush snapshot.
 */
public class BristleParameters {

    /**
     * There are three paths. Upper, middle and lower paths mark the transformation
     * of the lower, middle and upper bristles. Each path has an upper control point which
     * is near the handle, and a lower control point which is close to the tip.
     *
     * The direction of the upper control point is parallel to the handle, while the direction
     * of the lower control point is parallel to the paper.
     */
    public float upperPathUpperControlPointLength;
    public float upperPathLowerControlPointLength;

    public float lowerPathUpperControlPointLength;
    public float lowerPathLowerControlPointLength;

    public float middlePathUpperControlPointLength;
    public float middlePathLowerControlPointLength;

    // The planar distance the bristle end is from the handle
    public float upperPathDistanceFromHandle;
    public float middlePathDistanceFromHandle;
    public float lowerPathDistanceFromHandle;

    // The length of the imprint on the plane
    public float planarImprintLength;

    // Angle of the maximum spread of the bristles
    public float bristleHorizontalAngle;

    public BristleParameters() {}

    public BristleParameters(float middlePathDistanceFromHandle, float planarImprintLength,
                             float middlePathUpperControlPointLength, float middlePathLowerControlPointLength,
                             float bristleHorizontalAngle) {
        set(middlePathDistanceFromHandle, planarImprintLength,
                middlePathUpperControlPointLength, middlePathLowerControlPointLength, bristleHorizontalAngle);
    }

    public BristleParameters(BristleParameters b) {
        set(b);
    }

    public void set(BristleParameters b) {
        set(b.getMiddlePathDistanceFromHandle(), b.getPlanarImprintLength(),
                b.getMiddlePathUpperControlPointLength(), b.getMiddlePathLowerControlPointLength(),
                b.getBristleHorizontalAngle());
    }

    public void set(float planarDistanceFromHandle, float planarImprintLength,
                    float upperControlPointLength, float lowerControlPointLength,
                    float bristleHorizontalAngle) {
        this.middlePathDistanceFromHandle = planarDistanceFromHandle;
        this.planarImprintLength = planarImprintLength;
        this.middlePathUpperControlPointLength = upperControlPointLength;
        this.middlePathLowerControlPointLength = lowerControlPointLength;
        this.bristleHorizontalAngle = bristleHorizontalAngle;
    }

    public float getUpperPathUpperControlPointLength() {
        return upperPathUpperControlPointLength;
    }

    public void setUpperPathUpperControlPointLength(float upperPathUpperControlPointLength) {
        this.upperPathUpperControlPointLength = upperPathUpperControlPointLength;
    }

    public float getUpperPathLowerControlPointLength() {
        return upperPathLowerControlPointLength;
    }

    public void setUpperPathLowerControlPointLength(float upperPathLowerControlPointLength) {
        this.upperPathLowerControlPointLength = upperPathLowerControlPointLength;
    }


    public float getMiddlePathUpperControlPointLength() {
        return middlePathUpperControlPointLength;
    }

    public void setMiddlePathUpperControlPointLength(float middlePathUpperControlPointLength) {
        this.middlePathUpperControlPointLength = middlePathUpperControlPointLength;
    }

    public float getMiddlePathLowerControlPointLength() {
        return middlePathLowerControlPointLength;
    }

    public void setMiddlePathLowerControlPointLength(float middlePathLowerControlPointLength) {
        this.middlePathLowerControlPointLength = middlePathLowerControlPointLength;
    }

    public float getBristleHorizontalAngle() {
        return bristleHorizontalAngle;
    }

    public void setBristleHorizontalAngle(float bristleHorizontalAngle) {
        this.bristleHorizontalAngle = bristleHorizontalAngle;
    }

    public float getLowerPathUpperControlPointLength() {
        return lowerPathUpperControlPointLength;
    }

    public void setLowerPathUpperControlPointLength(float lowerPathUpperControlPointLength) {
        this.lowerPathUpperControlPointLength = lowerPathUpperControlPointLength;
    }

    public float getLowerPathLowerControlPointLength() {
        return lowerPathLowerControlPointLength;
    }

    public void setLowerPathLowerControlPointLength(float lowerPathLowerControlPointLength) {
        this.lowerPathLowerControlPointLength = lowerPathLowerControlPointLength;
    }


    public float getUpperPathDistanceFromHandle() {
        return upperPathDistanceFromHandle;
    }

    public void setUpperPathDistanceFromHandle(float upperPathDistanceFromHandle) {
        this.upperPathDistanceFromHandle = upperPathDistanceFromHandle;
    }

    public float getMiddlePathDistanceFromHandle() {
        return middlePathDistanceFromHandle;
    }

    public void setMiddlePathDistanceFromHandle(float middlePathDistanceFromHandle) {
        this.middlePathDistanceFromHandle = middlePathDistanceFromHandle;
    }

    public float getLowerPathDistanceFromHandle() {
        return lowerPathDistanceFromHandle;
    }

    public void setLowerPathDistanceFromHandle(float lowerPathDistanceFromHandle) {
        this.lowerPathDistanceFromHandle = lowerPathDistanceFromHandle;
    }

    public float getPlanarImprintLength() {
        return planarImprintLength;
    }

    public void setPlanarImprintLength(float planarImprintLength) {
        this.planarImprintLength = planarImprintLength;
    }

    @Override
    public String toString(){
        return "distance: " + middlePathDistanceFromHandle;
    }
}