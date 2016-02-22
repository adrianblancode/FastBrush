package co.adrianblan.calligraphy;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;

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

        return diagonalInches >= (7 - 0.001);
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
}
