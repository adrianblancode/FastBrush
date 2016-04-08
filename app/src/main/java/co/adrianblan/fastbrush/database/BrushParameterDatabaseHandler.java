package co.adrianblan.fastbrush.database;

/**
 * A database which details all parameters of the brush snapshots.
 */
public class BrushParameterDatabaseHandler {
    BrushParameterDatabase brushParameterDatabase;

    public BrushParameterDatabaseHandler() {
        brushParameterDatabase = new BrushParameterDatabase(20);
        init();
    }

    /**
     * Initializes the database with default values
     */
    private void init() {

        BristleParameters bristleParameters;

        // Neutral
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(1);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0);
        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 1), bristleParameters);

        // Neutral pressure 1
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.98f);
        bristleParameters.setLowerControlPointLength(0.1f);
        bristleParameters.setPlanarDistanceFromHandle(0.12f);
        bristleParameters.setPlanarImprintLength(0.06f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 0.96f), bristleParameters);

        // Neutral pressure 2
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.60f);
        bristleParameters.setLowerControlPointLength(0.42f);
        bristleParameters.setPlanarDistanceFromHandle(0.32f);
        bristleParameters.setPlanarImprintLength(0.10f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(0, 0.83f), bristleParameters);

        // Neutral pressure 3
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.41f);
        bristleParameters.setLowerControlPointLength(0.71f);
        bristleParameters.setPlanarDistanceFromHandle(0.62f);
        bristleParameters.setPlanarImprintLength(0.20f);
        bristleParameters.setBristleHorizontalAngle(10);

        brushParameterDatabase.put(new BrushKey(0, 0.56f), bristleParameters);

        // Neutral pressure 4
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.38f);
        bristleParameters.setLowerControlPointLength(0.68f);
        bristleParameters.setPlanarDistanceFromHandle(0.78f);
        bristleParameters.setPlanarImprintLength(0.26f);
        bristleParameters.setBristleHorizontalAngle(15);

        brushParameterDatabase.put(new BrushKey(0, 0.42f), bristleParameters);

        // Neutral pressure 5
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.30f);
        bristleParameters.setLowerControlPointLength(0.54f);
        bristleParameters.setPlanarDistanceFromHandle(0.93f);
        bristleParameters.setPlanarImprintLength(0.45f);
        bristleParameters.setBristleHorizontalAngle(22);

        brushParameterDatabase.put(new BrushKey(0, 0.29f), bristleParameters);

        // Front neutral
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(1);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0);
        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(45f, 1f), bristleParameters);

        // Front pressure 1
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(1);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0.03f);
        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);
        brushParameterDatabase.put(new BrushKey(45f, 0.97f), bristleParameters);

        // Front pressure 2
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.94f);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0.12f);
        bristleParameters.setPlanarImprintLength(0.01f);
        bristleParameters.setBristleHorizontalAngle(0);
        brushParameterDatabase.put(new BrushKey(45f, 0.85f), bristleParameters);

        // Front pressure 3
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.80f);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0.26f);
        bristleParameters.setPlanarImprintLength(0.01f);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(45f, 0.68f), bristleParameters);

        // Front pressure 4
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.64f);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0.38f);
        bristleParameters.setPlanarImprintLength(0.01f);
        bristleParameters.setBristleHorizontalAngle(5);

        brushParameterDatabase.put(new BrushKey(45f, 0.54f), bristleParameters);

        // Front pressure 5
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(0.26f);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0.52f);
        bristleParameters.setPlanarImprintLength(0.01f);
        bristleParameters.setBristleHorizontalAngle(15);

        brushParameterDatabase.put(new BrushKey(45f, 0.24f), bristleParameters);


        // Front pressure extreme 1
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(1);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0);
        bristleParameters.setPlanarImprintLength(0);
        bristleParameters.setBristleHorizontalAngle(0);

        brushParameterDatabase.put(new BrushKey(90f, 1f), bristleParameters);

        // Front pressure extreme 2
        bristleParameters = new BristleParameters();
        bristleParameters.setUpperControlPointLength(1);
        bristleParameters.setLowerControlPointLength(0);
        bristleParameters.setPlanarDistanceFromHandle(0);
        bristleParameters.setPlanarImprintLength(0);
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

        interpolatedValue.upperControlPointLength = first.upperControlPointLength
                + (second.upperControlPointLength - first.upperControlPointLength) * scale;
        interpolatedValue.lowerControlPointLength = first.lowerControlPointLength
                + (second.lowerControlPointLength - first.lowerControlPointLength) * scale;

        interpolatedValue.bristleHorizontalAngle = first.bristleHorizontalAngle
                + (second.bristleHorizontalAngle - first.bristleHorizontalAngle) * scale;

        return interpolatedValue;
    }
}
