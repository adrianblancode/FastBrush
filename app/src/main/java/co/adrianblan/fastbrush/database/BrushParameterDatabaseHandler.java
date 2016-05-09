package co.adrianblan.fastbrush.database;

/**
 * A database which details all parameters of the brush snapshots.
 */
public class BrushParameterDatabaseHandler {
    private BrushParameterDatabase brushParameterDatabase;

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

        bristleParameters.setLowerPathUpperControlPointLength(0.10f);
        bristleParameters.setLowerPathLowerControlPointLength(0.06f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.20f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.20f);
        bristleParameters.setUpperPathUpperControlPointLength(0.15f);
        bristleParameters.setUpperPathLowerControlPointLength(0.10f);

        bristleParameters.setLowerPathDistanceFromHandle(0);
        bristleParameters.setMiddlePathDistanceFromHandle(0);
        bristleParameters.setUpperPathDistanceFromHandle(0);

        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 1), bristleParameters);

        // Neutral pressure 2
        bristleParameters = new BristleParameters();

        bristleParameters.setLowerPathUpperControlPointLength(0.10f);
        bristleParameters.setLowerPathLowerControlPointLength(0.20f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.18f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.40f);
        bristleParameters.setUpperPathUpperControlPointLength(0.20f);
        bristleParameters.setUpperPathLowerControlPointLength(0.30f);

        bristleParameters.setLowerPathDistanceFromHandle(0);
        bristleParameters.setMiddlePathDistanceFromHandle(0.40f);
        bristleParameters.setUpperPathDistanceFromHandle(0.35f);

        bristleParameters.setPlanarImprintLength(0.20f);
        bristleParameters.setBristleHorizontalAngle(8);


        brushParameterDatabase.put(new BrushKey(0, 0.85f), bristleParameters);

        // Neutral pressure 4
        bristleParameters = new BristleParameters();

        bristleParameters.setLowerPathUpperControlPointLength(0.10f);
        bristleParameters.setLowerPathLowerControlPointLength(0.20f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.38f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.65f);
        bristleParameters.setUpperPathUpperControlPointLength(0.30f);
        bristleParameters.setUpperPathLowerControlPointLength(0.50f);

        bristleParameters.setLowerPathDistanceFromHandle(0.15f);
        bristleParameters.setMiddlePathDistanceFromHandle(0.65f);
        bristleParameters.setUpperPathDistanceFromHandle(0.55f);

        bristleParameters.setPlanarImprintLength(0.40f);
        bristleParameters.setBristleHorizontalAngle(22);

        brushParameterDatabase.put(new BrushKey(0, 0.60f), bristleParameters);

        // Neutral pressure 5
        bristleParameters = new BristleParameters();
        bristleParameters.setLowerPathUpperControlPointLength(0.35f);
        bristleParameters.setLowerPathLowerControlPointLength(0.80f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.23f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.70f);
        bristleParameters.setUpperPathUpperControlPointLength(0.15f);
        bristleParameters.setUpperPathLowerControlPointLength(0.55f);

        bristleParameters.setLowerPathDistanceFromHandle(0.52f);
        bristleParameters.setMiddlePathDistanceFromHandle(0.80f);
        bristleParameters.setUpperPathDistanceFromHandle(0.70f);

        bristleParameters.setPlanarImprintLength(0.50f);
        bristleParameters.setBristleHorizontalAngle(35);
        brushParameterDatabase.put(new BrushKey(0, 0.20f), bristleParameters);


        // Front neutral 1
        bristleParameters = new BristleParameters();
        bristleParameters.setLowerPathUpperControlPointLength(0.05f);
        bristleParameters.setLowerPathLowerControlPointLength(0.04f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.10f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.10f);
        bristleParameters.setUpperPathUpperControlPointLength(0.12f);
        bristleParameters.setUpperPathLowerControlPointLength(0.08f);

        bristleParameters.setLowerPathDistanceFromHandle(0.04f);
        bristleParameters.setMiddlePathDistanceFromHandle(0);
        bristleParameters.setUpperPathDistanceFromHandle(0.04f);

        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(45f, 1f), bristleParameters);

        // Front pressure 2
        bristleParameters = new BristleParameters();
        bristleParameters.setLowerPathUpperControlPointLength(0.07f);
        bristleParameters.setLowerPathLowerControlPointLength(0.06f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.08f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.18f);
        bristleParameters.setUpperPathUpperControlPointLength(0.17f);
        bristleParameters.setUpperPathLowerControlPointLength(0.20f);

        bristleParameters.setLowerPathDistanceFromHandle(0.06f);
        bristleParameters.setMiddlePathDistanceFromHandle(0.19f);
        bristleParameters.setUpperPathDistanceFromHandle(0.05f);

        bristleParameters.setPlanarImprintLength(0.15f);
        bristleParameters.setBristleHorizontalAngle(15);

        brushParameterDatabase.put(new BrushKey(45f, 0.9f), bristleParameters);

        // Front pressure 3
        bristleParameters = new BristleParameters();
        bristleParameters.setLowerPathUpperControlPointLength(0.23f);
        bristleParameters.setLowerPathLowerControlPointLength(0.04f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.36f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.06f);
        bristleParameters.setUpperPathUpperControlPointLength(0.06f);
        bristleParameters.setUpperPathLowerControlPointLength(0.20f);

        bristleParameters.setLowerPathDistanceFromHandle(0.14f);
        bristleParameters.setMiddlePathDistanceFromHandle(0.30f);
        bristleParameters.setUpperPathDistanceFromHandle(0.17f);

        bristleParameters.setPlanarImprintLength(0.23f);
        bristleParameters.setBristleHorizontalAngle(35);
        brushParameterDatabase.put(new BrushKey(45f, 0.70f), bristleParameters);

        // Front pressure 4
        bristleParameters = new BristleParameters();
        bristleParameters.setLowerPathUpperControlPointLength(0.33f);
        bristleParameters.setLowerPathLowerControlPointLength(0.0f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.33f);
        bristleParameters.setMiddlePathLowerControlPointLength(0);
        bristleParameters.setUpperPathUpperControlPointLength(0.23f);
        bristleParameters.setUpperPathLowerControlPointLength(0.30f);

        bristleParameters.setLowerPathDistanceFromHandle(0.25f);
        bristleParameters.setMiddlePathDistanceFromHandle(0.41f);
        bristleParameters.setUpperPathDistanceFromHandle(0.30f);

        bristleParameters.setPlanarImprintLength(0.30f);
        bristleParameters.setBristleHorizontalAngle(45);

        brushParameterDatabase.put(new BrushKey(45f, 0.60f), bristleParameters);


        // Front pressure extreme 1
        bristleParameters = new BristleParameters();
        bristleParameters.setLowerPathUpperControlPointLength(0.05f);
        bristleParameters.setLowerPathLowerControlPointLength(0.04f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.10f);
        bristleParameters.setMiddlePathLowerControlPointLength(0.10f);
        bristleParameters.setUpperPathUpperControlPointLength(0.17f);
        bristleParameters.setUpperPathLowerControlPointLength(0.30f);

        bristleParameters.setLowerPathDistanceFromHandle(0.04f);
        bristleParameters.setMiddlePathDistanceFromHandle(0);
        bristleParameters.setUpperPathDistanceFromHandle(0.04f);

        bristleParameters.setPlanarImprintLength(0.5f);
        bristleParameters.setBristleHorizontalAngle(20);

        brushParameterDatabase.put(new BrushKey(90f, 1f), bristleParameters);

        // Front pressure extreme 2
        bristleParameters = new BristleParameters();
        bristleParameters.setLowerPathUpperControlPointLength(0.33f);
        bristleParameters.setLowerPathLowerControlPointLength(0.0f);
        bristleParameters.setMiddlePathUpperControlPointLength(0.33f);
        bristleParameters.setMiddlePathLowerControlPointLength(0);
        bristleParameters.setUpperPathUpperControlPointLength(0.08f);
        bristleParameters.setUpperPathLowerControlPointLength(0.40f);

        bristleParameters.setLowerPathDistanceFromHandle(0.30f);
        bristleParameters.setMiddlePathDistanceFromHandle(0.41f);
        bristleParameters.setUpperPathDistanceFromHandle(0.30f);

        bristleParameters.setPlanarImprintLength(0.5f);
        bristleParameters.setBristleHorizontalAngle(25);

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

                // If we have no current key, then accept any key whose both parameters are on the "correct" side
                if((isHighAngle == (targetKey.angle < tempKey.angle))
                    && (isHighHeight == (targetKey.height < tempKey.height))) {
                        currentKey = tempKey;
                        currentIndex = i;
                }
                // If we already have a current key, make sure that the distance of the parameters are lowered while being on the correct side
            } else if ((isHighAngle == (targetKey.angle < tempKey.angle))
                    && (isHighAngle == (tempKey.angle < currentKey.angle))
                    && (isHighHeight == (targetKey.height < tempKey.height))
                    && (isHighHeight == (tempKey.height < currentKey.height))
                    ) {
                        currentKey = tempKey;
                        currentIndex = i;
            }
        }

        if(currentKey == null) {
            System.err.println("Couldn't find nearest key for parameters angle: " + targetKey.angle + ", height: " + targetKey.height);
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

        float interpolatedAnglePercent = (targetKey.angle - interpolatedLowAngle)
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


        interpolatedValue.upperPathDistanceFromHandle = first.upperPathDistanceFromHandle
                + (second.upperPathDistanceFromHandle - first.upperPathDistanceFromHandle) * scale;

        interpolatedValue.middlePathDistanceFromHandle = first.middlePathDistanceFromHandle
                + (second.middlePathDistanceFromHandle - first.middlePathDistanceFromHandle) * scale;

        interpolatedValue.lowerPathDistanceFromHandle = first.lowerPathDistanceFromHandle
                + (second.lowerPathDistanceFromHandle - first.lowerPathDistanceFromHandle) * scale;

        interpolatedValue.planarImprintLength = first.planarImprintLength
                + (second.planarImprintLength - first.planarImprintLength) * scale;

        interpolatedValue.bristleHorizontalAngle = first.bristleHorizontalAngle
                + (second.bristleHorizontalAngle - first.bristleHorizontalAngle) * scale;

        return interpolatedValue;
    }

    public BrushParameterDatabase getBrushParameterDatabase() {
        return brushParameterDatabase;
    }
}
