package co.adrianblan.fastbrush.database;

import java.util.HashMap;

/**
 * A database which details all parameters of the brush snapshots.
 */
public class BrushSnapshotDatabase {
    HashMap<BrushKey, BristleParameters> hashMap;

    public BrushSnapshotDatabase () {
        hashMap = new HashMap<>();
        init();
    }

    /**
     * Initializes the database with default values
     */
    private void init() {

        // Neutral
        hashMap.put(new BrushKey(0, 1), new BristleParameters(0f, 0f, 1f, 0f));

        // Neutral pressure 1
        hashMap.put(new BrushKey(0, 0.96f), new BristleParameters(0.12f, 0.06f, 0.98f, 0.1f));

        // Neutral pressure 2
        hashMap.put(new BrushKey(0, 0.83f), new BristleParameters(0.32f, 0.10f, 0.60f, 0.42f));

        // Neutral pressure 3
        hashMap.put(new BrushKey(0, 0.56f), new BristleParameters(0.62f, 0.20f, 0.41f, 0.71f));

        // Neutral pressure 4
        hashMap.put(new BrushKey(0, 0.42f), new BristleParameters(0.78f, 0.26f, 0.38f, 0.68f));

        // Neutral pressure 5
        hashMap.put(new BrushKey(0, 0.29f), new BristleParameters(0.93f, 0.45f, 0.30f, 0.54f));


        // Front neutral
        hashMap.put(new BrushKey(45f, 1f), new BristleParameters(0f, 0f, 1f, 0f));

        // Front pressure 1
        hashMap.put(new BrushKey(45f, 0.97f), new BristleParameters(0.03f, 0f, 1f, 0f));

        // Front pressure 2
        hashMap.put(new BrushKey(45f, 0.85f), new BristleParameters(0.012f, 0.01f, 0.94f, 0f));

        // Front pressure 3
        hashMap.put(new BrushKey(45f, 0.68f), new BristleParameters(0.26f, 0.01f, 0.80f, 0f));

        // Front pressure 4
        hashMap.put(new BrushKey(45f, 0.54f), new BristleParameters(0.38f, 0.01f, 0.64f, 0f));

        // Front pressure 5
        hashMap.put(new BrushKey(45f, 0.24f), new BristleParameters(0.52f, 0.01f, 0.26f, 0f));


        // Front pressure extreme 1
        hashMap.put(new BrushKey(90f, 1f), new BristleParameters(0f, 0f, 1f, 0f));

        // Front pressure extreme 2
        hashMap.put(new BrushKey(90f, 0f), new BristleParameters(0f, 0f, 1f, 0f));

        /**
        // Back neutral
        hashMap.put(new BrushKey(-45f, 1f), new BristleParameters(0f, 0));

        // Back pressure 1 (approximated)
        hashMap.put(new BrushKey(-45f, 0.70f), new BristleParameters(0.30f, 0));

        // Back pressure 2
        hashMap.put(new BrushKey(-45f, 0.55f), new BristleParameters(0.60f, 0));

        // Back pressure 3
        hashMap.put(new BrushKey(-45f, 0.34f), new BristleParameters(0.60f, 0));

        // Back pressure 4
        hashMap.put(new BrushKey(-45f, 0.24f), new BristleParameters(0.66f, 0));
         */
    }

    /**
     * Takes a BrushKey (which may or may not exist in the HashMap), and returns an interpolated
     * BristleParameter value based on the nearest values in the HashMap.
     */
    public BristleParameters getBristleParameter(BrushKey targetKey){

        if (hashMap.isEmpty()) {
            return null;
        }

        // Parameters for the nearest key in a direction
        // The first parameter is angle, the second is height
        BrushKey keyHighHigh = null;
        BrushKey keyHighLow = null;
        BrushKey keyLowHigh = null;
        BrushKey keyLowLow = null;

        // Go through all the keys, get the keys furthest in two dimensions
        // This is to establish a large cover, in case our targetKey is outside valid range
        for(BrushKey tempKey : hashMap.keySet()) {
            keyHighHigh =
                    getFurthestKey(keyHighHigh, tempKey, true, true);
            keyHighLow =
                    getFurthestKey(keyHighLow, tempKey, true, false);
            keyLowHigh =
                    getFurthestKey(keyLowHigh, tempKey, false, true);
            keyLowLow =
                    getFurthestKey(keyLowLow, tempKey, false, false);
        }

        // Go through all the keys, get the keys nearest in two dimensions
        for(BrushKey tempKey : hashMap.keySet()) {
            keyHighHigh =
                    getNearestKey(targetKey, keyHighHigh, tempKey, true, true);
            keyHighLow =
                    getNearestKey(targetKey, keyHighLow, tempKey, true, false);
            keyLowHigh =
                    getNearestKey(targetKey, keyLowHigh, tempKey, false, true);
            keyLowLow =
                    getNearestKey(targetKey, keyLowLow, tempKey, false, false);
        }

        BristleParameters interpolatedBristleParameters =  interpolate(targetKey, keyHighHigh, keyHighLow, keyLowHigh, keyLowLow);
        return interpolatedBristleParameters;
    }

    /** Expands the key to the farthest range possible according to the KeyDirections */
    public BrushKey getFurthestKey(BrushKey currentKey, BrushKey newKey,
                                   boolean isHighAngle, boolean isHighHeight) {

        float epsilon = 0.0001f;

        if(currentKey == null) {
            return newKey;
        }

        if(
                (
                        isHighAngle && (currentKey.angle < newKey.angle + epsilon)
                        ||
                        (!isHighAngle && (newKey.angle < currentKey.angle + epsilon))
                ) && (
                        isHighHeight && (currentKey.height < newKey.height + epsilon)
                        ||
                        (!isHighHeight && (newKey.height < currentKey.height + epsilon))

                )
                ){
            return newKey;
        }

        return currentKey;
    }

    public BristleParameters getNearestValue(BrushKey targetKey) {
        BrushKey currentKey = getNearestKey(targetKey);

        if(currentKey != null) {
            return hashMap.get(currentKey);
        } else {
            return null;
        }
    }

    public BrushKey getNearestKey(BrushKey targetKey) {

        final float epsilon = 0.0001f;
        BrushKey currentKey = null;

        for(BrushKey sourceKey : hashMap.keySet()) {
            if(currentKey == null) {
                currentKey = sourceKey;
            } else if(targetKey.distance(sourceKey) < targetKey.distance(currentKey) + epsilon) {
                currentKey = sourceKey;
            }
        }

        return currentKey;
    }

    public BrushKey getNearestKey(BrushKey targetKey, BrushKey currentKey, BrushKey newKey,
                                  boolean isHighAngle, boolean isHighHeight) {

        assert currentKey != null;

        if((isHighAngle == targetKey.angle < newKey.angle)
                && (isHighAngle == newKey.angle < currentKey.angle)
                && (isHighHeight == targetKey.height < newKey.height)
                && (isHighHeight == newKey.height < currentKey.height)
                ) {
            return newKey;
        }

        return currentKey;
    }

    /**
     * Takes in a four keys of different directions, and one target key. The end result is the value
     * from all four source keys intepolated with respect to the target key.
     */
    private BristleParameters interpolate(BrushKey targetKey, BrushKey keyHighHigh, BrushKey keyHighLow,
                                         BrushKey keyLowHigh, BrushKey keyLowLow) {

        BristleParameters valueHighHigh = hashMap.get(keyHighHigh);
        BristleParameters valueHighLow = hashMap.get(keyHighLow);
        BristleParameters valueLowHigh = hashMap.get(keyLowHigh);
        BristleParameters valueLowLow = hashMap.get(keyLowLow);

        float highAngleHeightPercent = (targetKey.height - keyHighLow.height) / (keyHighHigh.height - keyHighLow.height);
        BristleParameters interpolatedValueHighAngle = getInterpolatedValue(valueHighLow, valueHighHigh, highAngleHeightPercent);
        BrushKey interpolatedKeyHighAngle = new BrushKey((keyHighHigh.angle + keyHighLow.angle) / 2f, targetKey.height);

        float lowAngleHeightPercent = (targetKey.height - keyLowLow.height) / (keyLowHigh.height - keyLowLow.height);
        BristleParameters interpolatedValueLowAngle = getInterpolatedValue(valueLowLow, valueLowHigh, lowAngleHeightPercent);
        BrushKey interpolatedKeyLowAngle = new BrushKey((keyLowHigh.angle + keyLowLow.angle) / 2f, targetKey.height);

        float interpolatedAnglePercent = (targetKey.angle -  (interpolatedKeyLowAngle.angle))
                / (interpolatedKeyLowAngle.angle - interpolatedKeyHighAngle.angle);

        BristleParameters finalInterpolatedValue
                = getInterpolatedValue(interpolatedValueLowAngle, interpolatedValueHighAngle, interpolatedAnglePercent);

        return finalInterpolatedValue;
    }

    /**
     * Takes in two BristleParameters and interpolates a new BristleParameter, based on a scale
     */
    private BristleParameters getInterpolatedValue(BristleParameters first, BristleParameters second, float scale) {
        BristleParameters interpolatedValue = new BristleParameters();

        interpolatedValue.setPlanarDistanceFromHandle(first.planarDistanceFromHandle
                + (second.planarDistanceFromHandle - first.planarDistanceFromHandle) * scale);
        interpolatedValue.setPlanarImprintLength(first.planarImprintLength
                + (second.planarImprintLength - first.planarImprintLength) * scale);

        interpolatedValue.setUpperControlPointLength(first.upperControlPointLength
                + (second.upperControlPointLength - first.upperControlPointLength) * scale);
        interpolatedValue.setLowerControlPointLength(first.lowerControlPointLength
                + (second.lowerControlPointLength - first.lowerControlPointLength) * scale);

        return interpolatedValue;
    }
}
