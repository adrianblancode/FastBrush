package co.adrianblan.fastbrush.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.adrianblan.fastbrush.globject.Bristle;
import co.adrianblan.fastbrush.utils.Utils;

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
        BrushKey brushKey = new BrushKey();
        BristleParameters bristleParameters = new BristleParameters();

        // Neutral
        brushKey.set(0, 1);
        bristleParameters.set(0, 0, 1, 0);
        hashMap.put(brushKey, bristleParameters);
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
                    getFurthestKey(keyHighHigh, tempKey, KeyDirection.HIGH, KeyDirection.HIGH);
            keyHighLow =
                    getFurthestKey(keyHighLow, tempKey, KeyDirection.HIGH, KeyDirection.LOW);
            keyLowHigh =
                    getFurthestKey(keyLowHigh, tempKey, KeyDirection.LOW, KeyDirection.HIGH);
            keyLowLow =
                    getFurthestKey(keyLowLow, tempKey, KeyDirection.LOW, KeyDirection.LOW);
        }

        Set<BrushKey> hashSet = new HashSet<>();
        hashSet.add(keyHighHigh);
        hashSet.add(keyHighLow);
        hashSet.add(keyLowHigh);
        hashSet.add(keyLowLow);

        if(hashSet.size() < 4) {
            System.err.println("Duplicate furthest keys");
            return null;
        }

        // Go through all the keys, get the keys nearest in two dimensions
        for(BrushKey tempKey : hashMap.keySet()) {
            keyHighHigh =
                    getNearestKey(targetKey, keyHighHigh, tempKey, KeyDirection.HIGH, KeyDirection.HIGH);
            keyHighLow =
                    getNearestKey(targetKey, keyHighLow, tempKey, KeyDirection.HIGH, KeyDirection.LOW);
            keyLowHigh =
                    getNearestKey(targetKey, keyLowHigh, tempKey, KeyDirection.LOW, KeyDirection.HIGH);
            keyLowLow =
                    getNearestKey(targetKey, keyLowLow, tempKey, KeyDirection.LOW, KeyDirection.LOW);
        }

        return interpolate(targetKey, keyHighHigh, keyHighLow, keyLowHigh, keyLowLow);
    }

    /** Expands the key to the farthest range possible according to the KeyDirections */
    public BrushKey getFurthestKey(BrushKey currentKey, BrushKey newKey,
                                   KeyDirection angleDirection, KeyDirection heightDirection) {

        float epsilon = 0.0001f;
        boolean isHighAngle = angleDirection.equals(KeyDirection.HIGH);
        boolean isHighHeight = heightDirection.equals(KeyDirection.HIGH);

        if(currentKey == null) {
            return newKey;
        }

        if(
                (
                        isHighAngle && (currentKey.getAngle() < newKey.getAngle() + epsilon)
                        ||
                        (!isHighAngle && (newKey.getAngle() < currentKey.getAngle() + epsilon))
                ) && (
                        isHighHeight && (currentKey.getHeight() < newKey.getHeight() + epsilon)
                        ||
                        (!isHighHeight && (newKey.getHeight() < currentKey.getHeight() + epsilon))

                )
                ){
            return newKey;
        }

        return currentKey;
    }

    public BrushKey getNearestKey(BrushKey targetKey, BrushKey currentKey, BrushKey newKey,
                                  KeyDirection angleDirection, KeyDirection heightDirection) {

        boolean isHighAngle = angleDirection.equals(KeyDirection.HIGH);
        boolean isHighHeight = heightDirection.equals(KeyDirection.HIGH);

        assert currentKey != null;

        if((isHighAngle == targetKey.getAngle() < newKey.getAngle())
                && (isHighAngle == newKey.getAngle() < currentKey.getAngle())
                && (isHighHeight == targetKey.getHeight() < newKey.getHeight())
                && (isHighHeight == newKey.getHeight() < currentKey.getHeight())
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

        float highAngleHeightPercent = (targetKey.getHeight() - keyHighLow.getHeight()) / (keyHighHigh.getHeight() - keyHighLow.getHeight());
        BristleParameters interpolatedValueHighAngle = getInterpolatedValue(valueHighLow, valueHighHigh, highAngleHeightPercent);
        BrushKey interpolatedKeyHighAngle = new BrushKey((keyHighHigh.getAngle() + keyHighLow.getAngle()) / 2f, targetKey.getHeight());

        float lowAngleHeightPercent = (targetKey.getHeight() - keyLowLow.getHeight()) / (keyLowHigh.getHeight() - keyLowLow.getHeight());
        BristleParameters interpolatedValueLowAngle = getInterpolatedValue(valueLowLow, valueLowHigh, lowAngleHeightPercent);
        BrushKey interpolatedKeyLowAngle = new BrushKey((keyLowHigh.getAngle() + keyLowLow.getAngle()) / 2f, targetKey.getHeight());

        float interpolatedAnglePercent = (targetKey.getAngle() -  (interpolatedKeyLowAngle.getAngle()))
                / (interpolatedKeyHighAngle.getAngle() - interpolatedKeyLowAngle.getAngle());
        BristleParameters finalInterpolatedValue = getInterpolatedValue(interpolatedValueLowAngle, interpolatedValueHighAngle, interpolatedAnglePercent);

        return finalInterpolatedValue;
    }

    /**
     * Takes in two BristleParameters and interpolates a new BristleParameter, based on a scale
     */
    private BristleParameters getInterpolatedValue(BristleParameters first, BristleParameters second, float scale) {
        BristleParameters interpolatedValue = new BristleParameters();

        interpolatedValue.setSpreadAngle(first.getSpreadAngle() + (second.getSpreadAngle() - first.getSpreadAngle()) * scale);
        interpolatedValue.setPlanarDistanceFromHandle(first.getPlanarDistanceFromHandle() * scale + second.getPlanarDistanceFromHandle() * (1f - scale));
        interpolatedValue.setUpperControlPointLength(first.getUpperControlPointLength() * scale + second.getUpperControlPointLength() * (1f - scale));
        interpolatedValue.setLowerControlPointLength(first.getLowerControlPointLength() * scale + second.getLowerControlPointLength() * (1f - scale));

        return interpolatedValue;
    }
}
