package co.adrianblan.fastbrush.database;

/**
 * A database which details all parameters of the brush snapshots.
 */
public class BrushParamaterDatabaseHandler {
    BrushParameterDatabase brushParameterDatabase;

    public BrushParamaterDatabaseHandler() {
        brushParameterDatabase = new BrushParameterDatabase(20);
        init();
    }

    /**
     * Initializes the database with default values
     */
    private void init() {

        // Neutral
        brushParameterDatabase.put(new BrushKey(0, 1), new BristleParameters(0f, 0f, 1f, 0f));

        // Neutral pressure 1
        brushParameterDatabase.put(new BrushKey(0, 0.96f), new BristleParameters(0.12f, 0.06f, 0.98f, 0.1f));

        // Neutral pressure 2
        brushParameterDatabase.put(new BrushKey(0, 0.83f), new BristleParameters(0.32f, 0.10f, 0.60f, 0.42f));

        // Neutral pressure 3
        brushParameterDatabase.put(new BrushKey(0, 0.56f), new BristleParameters(0.62f, 0.20f, 0.41f, 0.71f));

        // Neutral pressure 4
        brushParameterDatabase.put(new BrushKey(0, 0.42f), new BristleParameters(0.78f, 0.26f, 0.38f, 0.68f));

        // Neutral pressure 5
        brushParameterDatabase.put(new BrushKey(0, 0.29f), new BristleParameters(0.93f, 0.45f, 0.30f, 0.54f));


        // Front neutral
        brushParameterDatabase.put(new BrushKey(45f, 1f), new BristleParameters(0f, 0f, 1f, 0f));

        // Front pressure 1
        brushParameterDatabase.put(new BrushKey(45f, 0.97f), new BristleParameters(0.03f, 0f, 1f, 0f));

        // Front pressure 2
        brushParameterDatabase.put(new BrushKey(45f, 0.85f), new BristleParameters(0.012f, 0.01f, 0.94f, 0f));

        // Front pressure 3
        brushParameterDatabase.put(new BrushKey(45f, 0.68f), new BristleParameters(0.26f, 0.01f, 0.80f, 0f));

        // Front pressure 4
        brushParameterDatabase.put(new BrushKey(45f, 0.54f), new BristleParameters(0.38f, 0.01f, 0.64f, 0f));

        // Front pressure 5
        brushParameterDatabase.put(new BrushKey(45f, 0.24f), new BristleParameters(0.52f, 0.01f, 0.26f, 0f));


        // Front pressure extreme 1
        brushParameterDatabase.put(new BrushKey(90f, 1f), new BristleParameters(0f, 0f, 1f, 0f));

        // Front pressure extreme 2
        brushParameterDatabase.put(new BrushKey(90f, 0f), new BristleParameters(0f, 0f, 1f, 0f));

        /**
        // Back neutral
        brushParameterDatabase.put(new BrushKey(-45f, 1f), new BristleParameters(0f, 0));

        // Back pressure 1 (approximated)
        brushParameterDatabase.put(new BrushKey(-45f, 0.70f), new BristleParameters(0.30f, 0));

        // Back pressure 2
        brushParameterDatabase.put(new BrushKey(-45f, 0.55f), new BristleParameters(0.60f, 0));

        // Back pressure 3
        brushParameterDatabase.put(new BrushKey(-45f, 0.34f), new BristleParameters(0.60f, 0));

        // Back pressure 4
        brushParameterDatabase.put(new BrushKey(-45f, 0.24f), new BristleParameters(0.66f, 0));
         */
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

        interpolatedValue.planarDistanceFromHandle = (first.planarDistanceFromHandle
                + (second.planarDistanceFromHandle - first.planarDistanceFromHandle) * scale);
        interpolatedValue.planarImprintLength = (first.planarImprintLength
                + (second.planarImprintLength - first.planarImprintLength) * scale);

        interpolatedValue.upperControlPointLength = (first.upperControlPointLength
                + (second.upperControlPointLength - first.upperControlPointLength) * scale);
        interpolatedValue.lowerControlPointLength = (first.lowerControlPointLength
                + (second.lowerControlPointLength - first.lowerControlPointLength) * scale);

        return interpolatedValue;
    }
}
