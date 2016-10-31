package co.adrianblan.fastbrush.globject;

import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Writing primitive for the brush.
 */
public class Bristle {

    public static final float BASE_LENGTH = 0.8f;
    public static final float BASE_TIP_LENGTH = 0.35f * BASE_LENGTH;
    public static final float BRUSH_RADIUS_UPPER = 0.25f * BASE_LENGTH;
    private static final float BRUSH_RADIUS_LOWER = 0.27f * BASE_LENGTH;
    private static final float MIN_SIZE_SCALE = 0.1f;

    public static float radiusUpper = BRUSH_RADIUS_UPPER;
    public static float radiusLower = BRUSH_RADIUS_LOWER;

    public float length;
    public Vector3 top;
    public Vector3 bottom;
    public Vector3 extendedBottom;

    public Bristle(SettingsData settingsData) {

        radiusUpper = BRUSH_RADIUS_UPPER * (settingsData.getSize() + MIN_SIZE_SCALE);
        radiusLower = BRUSH_RADIUS_LOWER * (settingsData.getSize() + MIN_SIZE_SCALE);

        // We take the square root in order to guarantee uniform distribution in the circle
        float radiusAngle = (float) (Math.sqrt(Math.random()) * 2f * Math.PI);
        float radiusLength = (float) Math.random();

        float horizontal = (float) Math.cos(radiusAngle) * radiusLength;
        float vertical = (float) Math.sin(radiusAngle) * radiusLength;

        top = new Vector3(radiusUpper * horizontal, radiusUpper * vertical, 0f);

        length = BASE_LENGTH - BASE_TIP_LENGTH + BASE_TIP_LENGTH * (float) Math.cos(radiusLength * 0.5f * Math.PI);
        bottom = new Vector3(radiusLower * horizontal, radiusLower * vertical, -length);
        extendedBottom = new Vector3(bottom);
        extendedBottom.setZ(-BASE_LENGTH);
    }

    public float getLength() {
        return length;
    }
}
