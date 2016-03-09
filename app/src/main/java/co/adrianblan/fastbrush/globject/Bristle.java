package co.adrianblan.fastbrush.globject;

import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Writing primitive for the brush.
 */
public class Bristle {

    public static final float BASE_LENGTH = 1.0f;
    public static final float BASE_TIP_LENGTH = 0.35f;
    private static final float BRUSH_RADIUS_UPPER = 0.4f;
    private static final float BRUSH_RADIUS_LOWER = 0.2f;
    private static final float MIN_SIZE_SCALE = 0.1f;

    public static float length;
    public static float tipLength;

    private Vector3 top;
    private Vector3 bottom;

    public Vector3 absoluteTop;
    public Vector3 absoluteBottom;

    public Bristle(SettingsData settingsData) {

        length = BASE_LENGTH * (settingsData.getSize() + MIN_SIZE_SCALE);
        tipLength = BASE_TIP_LENGTH * (settingsData.getSize() + MIN_SIZE_SCALE);
        float radiusUpper = BRUSH_RADIUS_UPPER * (settingsData.getSize() + MIN_SIZE_SCALE);
        float radiusLower = BRUSH_RADIUS_LOWER * (settingsData.getSize() + MIN_SIZE_SCALE);

        float radiusAngle = (float) (Math.random() * 2f * Math.PI);
        float verticalAngle = (float) Math.random();

        float horizontal = (float) Math.cos(radiusAngle) * verticalAngle;
        float vertical = (float) Math.sin(radiusAngle) * verticalAngle;

        top = new Vector3(radiusUpper * horizontal, radiusUpper * vertical, 0f);
        bottom = new Vector3(radiusLower * horizontal, radiusLower * vertical,
                -(length - tipLength + tipLength * (float) Math.cos(verticalAngle * 0.5f * Math.PI)));

        absoluteTop = new Vector3();
        absoluteBottom = new Vector3();
    }

    public void update(Vector3 brushPosition) {

        absoluteTop.addFast(top, brushPosition);
        absoluteBottom.addFast(bottom, brushPosition);
    }
}
