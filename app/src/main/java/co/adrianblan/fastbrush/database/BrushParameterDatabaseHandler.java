package co.adrianblan.fastbrush.database;

/**
 * A database which details all parameters of the brush snapshots.
 */
public class BrushParameterDatabaseHandler {
    BrushParameterDatabase brushParameterDatabase;

    public BrushParameterDatabaseHandler() {
        // Init the database with 20 positions
        brushParameterDatabase = new BrushParameterDatabase(20);
        init();
    }

    /**
     * Initializes the database with default values
     */
    private void init() {

        BristleParameters bristleParameters;

        // Neutral 1
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperPathUpperControlPointLength(0.10f);
        bristleParameters.setUpperPathLowerControlPointLength(0.06f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.20f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.20f);
        bristleParameters.setPlanarDistanceFromHandle(0);
        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 1), bristleParameters);

        // Neutral pressure 2
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperPathUpperControlPointLength(0.10f);
        bristleParameters.setUpperPathLowerControlPointLength(0.06f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.38f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.42f);
        bristleParameters.setPlanarDistanceFromHandle(0.50f);
        bristleParameters.setPlanarImprintLength(0.20f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 0.85f), bristleParameters);

        // Neutral pressure 3
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperPathUpperControlPointLength(0.10f);
        bristleParameters.setUpperPathLowerControlPointLength(0.06f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.38f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.55f);
        bristleParameters.setPlanarDistanceFromHandle(0.5f);
        bristleParameters.setPlanarImprintLength(0.30f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 0.71f), bristleParameters);

        // Neutral pressure 4
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperPathUpperControlPointLength(0.10f);
        bristleParameters.setUpperPathLowerControlPointLength(0.20f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.38f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.65f);
        bristleParameters.setPlanarDistanceFromHandle(0.8f);
        bristleParameters.setPlanarImprintLength(0.40f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 0.66f), bristleParameters);

        // Neutral pressure 5
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperPathUpperControlPointLength(0.35f);
        bristleParameters.setUpperPathLowerControlPointLength(0.8f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.23f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.70f);
        bristleParameters.setPlanarDistanceFromHandle(0.80f);
        bristleParameters.setPlanarImprintLength(0.50f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 0.20f), bristleParameters);


        // Front neutral 1
        bristleParameters = new BristleParameters();
        bristleParameters.setMiddlePathUpperControlPointLength(0);
        bristleParameters.setMiddlePathLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0);
        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(45f, 1f), bristleParameters);

        // Front pressure 2
        bristleParameters = new BristleParameters();
        bristleParameters.setMiddlePathUpperControlPointLength(0.08f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.18f);
        bristleParameters.setPlanarDistanceFromHandle(0.19f);
        bristleParameters.setPlanarImprintLength(0.25f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(45f, 0.9f), bristleParameters);

        // Front pressure 3
        bristleParameters = new BristleParameters();
        bristleParameters.setMiddlePathUpperControlPointLength(0.36f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.06f);
        bristleParameters.setPlanarDistanceFromHandle(0.30f);
        bristleParameters.setPlanarImprintLength(0.30f);
        bristleParameters.setBristleHorizontalAngle(0);
        brushParameterDatabase.put(new BrushKey(45f, 0.70f), bristleParameters);

        // Front pressure 4
        bristleParameters = new BristleParameters();
        bristleParameters.setMiddlePathUpperControlPointLength(0.33f);
        bristleParameters.setMiddlePathLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0.41f);
        bristleParameters.setPlanarImprintLength(0.4f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(45f, 0.60f), bristleParameters);


        // Front pressure extreme 1
        bristleParameters = new BristleParameters();
        bristleParameters.setMiddlePathUpperControlPointLength(1);
        bristleParameters.setMiddlePathLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(1);
        bristleParameters.setPlanarImprintLength(1);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(90f, 1f), bristleParameters);

        // Front pressure extreme 2
        bristleParameters = new BristleParameters();
        bristleParameters.setMiddlePathUpperControlPointLength(1);
        bristleParameters.setMiddlePathLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(1);
        bristleParameters.setPlanarImprintLength(1);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(90f, 0f), bristleParameters);
    }

    /**
     * Takes a BrushKey (which may or may not exist in the HashMap), and returns an interpolated
     * BristleParameter value based on the nearest values in the HashMap.
     */
    public BristleParameters getBristleParameter(BrushKey targetKey){

        if (brushParameterDatabase.isEmpty()) {
            return null;
        }

        // Parameters for the nearest key in a direction
        // The first parameter is angle, the second is height
        // Go through all the keys, get the keys nearest in two dimensions
        int indexHighHigh = getNearestKeyIndex(targetKey, true, true);
        int indexHighLow = getNearestKeyIndex(targetKey, true, false);
        int indexLowHigh  = getNearestKeyIndex(targetKey, false, true);
        int indexLowLow = getNearestKeyIndex(targetKey, false, false);

        BristleParameters interpolatedBristleParameters =  interpolate(targetKey, indexHighHigh, indexHighLow, indexLowHigh, indexLowLow);
        return interpolatedBristleParameters;
    }

    public int getNearestKeyIndex(BrushKey targetKey, boolean isHighAngle, boolean isHighHeight) {

        BrushKey currentKey = null;
        int currentIndex = 0;

        for(int i = 0; i < brushParameterDatabase.size; i++) {

            BrushKey tempKey = brushParameterDatabase.brushKeys[i];

            if(currentKey == null) {
                if((isHighAngle == targetKey.angle < tempKey.angle) && (isHighHeight == targetKey.height < tempKey.height)) {
                    currentKey = tempKey;
                    currentIndex = i;
                }
            } else if ((isHighAngle == targetKey.angle < tempKey.angle)
                    && (isHighAngle == (tempKey.angle - targetKey.angle) < currentKey.angle)
                    && (isHighHeight == targetKey.height < tempKey.height)
                    && (isHighHeight == tempKey.height < currentKey.height)
                    ) {
                currentKey = tempKey;
                currentIndex = i;
            }
        }

        return currentIndex;
    }

    /**
     * Takes in a four keys of different directions, and one target key. The end result is the value
     * from all four source keys intepolated with respect to the target key.
     */
    private BristleParameters interpolate(BrushKey targetKey, int indexHighHigh, int indexHighLow,
                                         int indexLowHigh, int indexLowLow) {

        BrushKey keyHighHigh = brushParameterDatabase.brushKeys[indexHighHigh];
        BrushKey keyHighLow = brushParameterDatabase.brushKeys[indexHighLow];
        BrushKey keyLowHigh = brushParameterDatabase.brushKeys[indexLowHigh];
        BrushKey keyLowLow = brushParameterDatabase.brushKeys[indexLowLow];

        BristleParameters valueHighHigh = brushParameterDatabase.bristleParameters[indexHighHigh];
        BristleParameters valueHighLow = brushParameterDatabase.bristleParameters[indexHighLow];
        BristleParameters valueLowHigh = brushParameterDatabase.bristleParameters[indexLowHigh];
        BristleParameters valueLowLow = brushParameterDatabase.bristleParameters[indexLowLow];

        float highAngleHeightPercent = (targetKey.height - keyHighLow.height) / (keyHighHigh.height - keyHighLow.height);
        BristleParameters interpolatedValueHighAngle = getInterpolatedValue(valueHighLow, valueHighHigh, highAngleHeightPercent);
        float interpolatedHighAngle = keyHighLow.angle * (1 - highAngleHeightPercent) +
                keyHighHigh.angle * highAngleHeightPercent;

        float lowAngleHeightPercent = (targetKey.height - keyLowLow.height) / (keyLowHigh.height - keyLowLow.height);
        BristleParameters interpolatedValueLowAngle = getInterpolatedValue(valueLowLow, valueLowHigh, lowAngleHeightPercent);
        float interpolatedLowAngle = keyLowLow.angle * (1 - lowAngleHeightPercent) +
                keyLowHigh.angle * lowAngleHeightPercent;

        float interpolatedAnglePercent = (targetKey.angle -  (interpolatedLowAngle))
                / (interpolatedLowAngle - interpolatedHighAngle);

        BristleParameters finalInterpolatedValue
                = getInterpolatedValue(interpolatedValueLowAngle, interpolatedValueHighAngle, interpolatedAnglePercent);

        return finalInterpolatedValue;
    }

    /**
     * Takes in two BristleParameters and interpolates a new BristleParameter, based on a scale
     */
    private BristleParameters getInterpolatedValue(BristleParameters first, BristleParameters second, float scale) {
        BristleParameters interpolatedValue = new BristleParameters();

        interpolatedValue.planarDistanceFromHandle = first.planarDistanceFromHandle
                + (second.planarDistanceFromHandle - first.planarDistanceFromHandle) * scale;
        interpolatedValue.planarImprintLength = first.planarImprintLength
                + (second.planarImprintLength - first.planarImprintLength) * scale;

        interpolatedValue.upperPathUpperControlPointLength = first.upperPathUpperControlPointLength
                + (second.upperPathUpperControlPointLength - first.upperPathUpperControlPointLength) * scale;
        interpolatedValue.upperPathLowerControlPointLength = first.upperPathLowerControlPointLength
                + (second.upperPathLowerControlPointLength - first.upperPathLowerControlPointLength) * scale;

        interpolatedValue.middlePathUpperControlPointLength = first.middlePathUpperControlPointLength
                + (second.middlePathUpperControlPointLength - first.middlePathUpperControlPointLength) * scale;
        interpolatedValue.middlePathLowerControlPointLength = first.middlePathLowerControlPointLength
                + (second.middlePathLowerControlPointLength - first.middlePathLowerControlPointLength) * scale;

        interpolatedValue.lowerPathUpperControlPointLength = first.lowerPathUpperControlPointLength
                + (second.lowerPathUpperControlPointLength - first.lowerPathUpperControlPointLength) * scale;
        interpolatedValue.lowerPathLowerControlPointLength = first.lowerPathLowerControlPointLength
                + (second.lowerPathLowerControlPointLength - first.lowerPathLowerControlPointLength) * scale;

        interpolatedValue.bristleHorizontalAngle = first.bristleHorizontalAngle
                + (second.bristleHorizontalAngle - first.bristleHorizontalAngle) * scale;

        return interpolatedValue;
    }
}
