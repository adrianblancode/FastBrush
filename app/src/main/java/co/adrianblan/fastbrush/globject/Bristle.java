package co.adrianblan.fastbrush.globject;

import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Writing primitive for the brush.
 */
public class Bristle {

    public static final float LENGTH = 2.0f;
    public static final float TIP_LENGTH = 0.5f;
    private static final float VERTICAL_OFFSET = 0.1f;

    private Vector3 top;
    private Vector3 bottom;

    public Vector3 absoluteStart;
    public Vector3 absoluteEnd;

    public Bristle() {

        float radiusAngle = (float) (Math.random() * 2f * Math.PI);
        float verticalAngle = (float) Math.random();

        float horizontal = (float) Math.cos(radiusAngle) * verticalAngle;
        float vertical = (float) Math.sin(radiusAngle) * verticalAngle;

        float upperRadius = 0.4f;
        float lowerRadius = 0.2f;

        top = new Vector3(upperRadius * horizontal, upperRadius * vertical + VERTICAL_OFFSET, 0f);
        bottom = new Vector3(lowerRadius * horizontal, lowerRadius * vertical,
                -(LENGTH - TIP_LENGTH + TIP_LENGTH * (float) Math.cos(verticalAngle * 0.5f * Math.PI)));

        absoluteStart = new Vector3();
        absoluteEnd = new Vector3();
    }

    public void update(Vector3 brushPosition) {
        absoluteStart.addFast(top, brushPosition);
        absoluteEnd.addFast(bottom, brushPosition);
    }
}
