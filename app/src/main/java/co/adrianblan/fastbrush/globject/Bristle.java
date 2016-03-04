package co.adrianblan.fastbrush.globject;

import android.opengl.Matrix;

import co.adrianblan.fastbrush.vector.Vector2;
import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Writing primitive for the brush.
 */
public class Bristle {

    public static final float BASE_LENGTH = 1.0f;
    public static final float TIP_LENGTH = 0.35f;
    private static final float BASE_VERTICAL_OFFSET = 0.0f;
    private static final float BRUSH_RADIUS_UPPER = 0.4f;
    private static final float BRUSH_RADIUS_LOWER = 0.2f;
    private static final Vector3 basePos = new Vector3(0, 0, -BASE_LENGTH);

    private Vector3 top;
    private Vector3 bottom;
    private Vector3 diff;
    float distanceToBasePos;
    float inherentAngle;
    float length;

    public Vector3 absoluteTop;
    public Vector3 absoluteBottom;

    public Bristle() {

        float radiusAngle = (float) (Math.random() * 2f * Math.PI);
        float verticalAngle = (float) Math.random();

        float horizontal = (float) Math.cos(radiusAngle) * verticalAngle;
        float vertical = (float) Math.sin(radiusAngle) * verticalAngle;

        inherentAngle = (float) (Math.sqrt(horizontal * horizontal + vertical * vertical) / (BASE_LENGTH * Math.PI / 2f));

        top = new Vector3(BRUSH_RADIUS_UPPER * horizontal, BRUSH_RADIUS_UPPER * vertical, 0f);
        bottom = new Vector3(BRUSH_RADIUS_LOWER * horizontal, BRUSH_RADIUS_LOWER * vertical,
                -(BASE_LENGTH - TIP_LENGTH + TIP_LENGTH * (float) Math.cos(verticalAngle * 0.5f * Math.PI)));

        absoluteTop = new Vector3();
        absoluteBottom = new Vector3();
        diff = new Vector3();
        length = basePos.distance(top);

        distanceToBasePos = basePos.distance(bottom);
    }

    public void update(Vector3 brushPosition) {

        absoluteTop.addFast(top, brushPosition);
        absoluteBottom.addFast(bottom, brushPosition);
    }
}
