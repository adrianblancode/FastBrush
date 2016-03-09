package co.adrianblan.fastbrush.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/** Utility functions */
public class Utils {

    public static final float blackColor[] = {0f, 0f, 0f, 0.6f};
    public static final float brownColor[] = {0.26f, 0.18f, 0.14f, 0.85f};

    public static float[] getColorWithAlpha(float[] color, float alpha) {
        float[] copy = color.clone();
        copy[3] = alpha;
        return copy;
    }

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

    /** Returns whether two floats are equivalent to each other within a treshold */
    public static boolean floatsAreEquivalent(float val1, float val2) {
        float epsilon = 0.0001f;

        return Math.abs(val1 - val2) < epsilon;
    }

    /**
     * Wrapper for replacing data in a float array, taken from another array from index zero.
     */
    public static void replaceInFloatArray(float[] floatArray, int floatArrayIndex, float [] contentArray) {
        replaceInFloatArray(floatArray, floatArrayIndex, contentArray, 0);
    }

    /**
     * Takes a float array, and a content array. Replaces the data in the float array from index
     * floatArrayIndex with the data from content array from contentArrayIndex.
     *
     * @param floatArray the array which data is replaced in
     * @param floatArrayIndex the index which data should start to be replaced from
     * @param contentArray the array which contains the content place in float array
     * @param contentArrayIndex the index which content begins from
     */
    public static void replaceInFloatArray(float[] floatArray, int floatArrayIndex, float [] contentArray,
                                           int contentArrayIndex) {

        if(floatArrayIndex < 0 || contentArrayIndex < 0 || floatArrayIndex >= floatArray.length ||
                contentArrayIndex >= contentArray.length) {
            System.err.println("Invalid input indexes");
            throw new ArrayIndexOutOfBoundsException();
        }

        int contentArrayReplaceLength = contentArray.length - contentArrayIndex;

        if(floatArrayIndex + contentArrayReplaceLength > floatArray.length) {
            System.err.println("Float array cannot fit content");
            throw new ArrayIndexOutOfBoundsException();
        }

        for(int i = 0; floatArrayIndex + i < floatArray.length; i++) {
            floatArray[floatArrayIndex + i] = contentArray[contentArrayIndex + i];
        }
    }

    /** Returns a throttled value that is a given percent from previousvalue towards targetValue */
    public static float getThrottledValue(float previousValue, float targetValue) {
        final float VALUE_SCALE = 0.02f;
        float difference = targetValue - previousValue;

        return previousValue + difference * VALUE_SCALE;
    }

    /**
     * Returns whether the device is a tablet.
     *
     * We consider any device equal to or over 7 inches to be a tablet.
     */
    public static boolean isTablet() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

        float widthDpi = metrics.xdpi;
        float heightDpi = metrics.ydpi;

        float widthInches = widthPixels / widthDpi;
        float heightInches = heightPixels / heightDpi;

        double diagonalInches = Math.sqrt((widthInches * widthInches)
                        + (heightInches * heightInches));

        return diagonalInches >= (6.8f);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static void printMatrix(float[] m) {

        for(int i = 0; i < m.length; i += 4) {
            System.out.println("{" + m[i + 0] + ", " + m[i + 1] + ", " + m[i + 2] + ", " + m[i + 3] + "}");
        }
    }
}
