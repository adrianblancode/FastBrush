package co.adrianblan.fastbrush.globject;

import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Writing primitive for the brush.
 */
public class Bristle {

    public static final float BASE_LENGTH = 1.6f;
    public static final float TIP_LENGTH = 0.4f;
    private static final float BASE_VERTICAL_OFFSET = 0.1f;
    private static final float BRUSH_RADIUS_UPPER = 0.4f;
    private static final float BRUSH_RADIUS_LOWER = 0.2f;

    private Vector3 top;
    private Vector3 bottom;

    public Vector3 absoluteStart;
    public Vector3 absoluteEnd;

    public Bristle() {

        float radiusAngle = (float) (Math.random() * 2f * Math.PI);
        float verticalAngle = (float) Math.random();

        float horizontal = (float) Math.cos(radiusAngle) * verticalAngle;
        float vertical = (float) Math.sin(radiusAngle) * verticalAngle;

        top = new Vector3(BRUSH_RADIUS_UPPER * horizontal, BRUSH_RADIUS_UPPER * vertical + BASE_VERTICAL_OFFSET, 0f);
        bottom = new Vector3(BRUSH_RADIUS_LOWER * horizontal, BRUSH_RADIUS_LOWER * vertical,
                -(BASE_LENGTH - TIP_LENGTH + TIP_LENGTH * (float) Math.cos(verticalAngle * 0.5f * Math.PI)));

        absoluteStart = new Vector3();
        absoluteEnd = new Vector3();
    }

    public void update(Vector3 brushPosition) {
        absoluteStart.addFast(top, brushPosition);
        absoluteEnd.addFast(bottom, brushPosition);
    }
}
