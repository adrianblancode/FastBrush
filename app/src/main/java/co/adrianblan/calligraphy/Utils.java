package co.adrianblan.calligraphy;

/** Utility functions */
public class Utils {

    /** Returns the value of val, clamped between min and max */
    public static float clamp (float val, float min, float max) {
        return Math.max(min, Math.min(val, max));
    }

    /** Normalizes a value from [min, max] to [0, 1] */
    public static float normalize(float val, float min, float max) {
        float clampedVal = clamp(val, min, max);

        float offset = 0f - min;
        float scale = 1f / (max + offset);

        return (clampedVal + offset) * scale;
    }

    public static boolean floatsAreEquivalent(float val1, float val2) {
        float epsilon = 0.0001f;

        return Math.abs(val1 - val2) < epsilon;
    }

    /** Returns a throttled value that is a given percent from previousvalue towards targetValue */
    public static float getThrottledValue(float previousValue, float targetValue) {
        final float VALUE_SCALE = 0.02f;
        float difference = targetValue - previousValue;

        return previousValue + difference * VALUE_SCALE;
    }

}
