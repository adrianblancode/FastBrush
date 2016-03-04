package co.adrianblan.fastbrush.data;

import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector2;

/**
 * Class which encapsulates touch data.
 */
public class TouchData {

    private static final float MAX_TILT = 0.2f;
    private static final float TILT_SCALE = 0.4f;

    private Vector2 position;
    private Vector2 velocity;
    private float size;
    private float pressure;

    public TouchData(Vector2 position, Vector2 velocity, float size, float pressure) {
        this.position = position;
        this.velocity = velocity;
        this.pressure = pressure;

        // The touch size is always 0.0 on an emulator
        if(!Utils.floatsAreEquivalent(size, 0f)) {
            this.size = size;
        } else {
            this.size = 1.0f;
        }
    }

    public TouchData(float x, float y, float xv, float yv, float size, float pressure) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(xv, yv);
        this.pressure = pressure;

        // The touch size is always 0.0 on an emulator
        if(!Utils.floatsAreEquivalent(size, 0f)) {
            this.size = size;
        } else {
            this.size = 1.0f;
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    /** Gets the normalized pressure in proportion to screen size, 1f is roughly normal */
    public float getNormalizedPressure() {

        if(Utils.floatsAreEquivalent(pressure, 1.0f)) {
            return 1.0f;
        } else {
            return pressure / (size * 10f);
        }
    }

    /** Takes a touch size and returns the normalized size [0, 1] */
    public float getNormalizedTouchSize() {

        float TOUCH_SIZE_MIN; // The minimum touch size treshold
        float TOUCH_SIZE_MAX; // The maximum touch size treshold

        if(Utils.isTablet()) {
            TOUCH_SIZE_MIN = 0.1f;
            TOUCH_SIZE_MAX = 0.25f;
        } else {
            TOUCH_SIZE_MIN = 0.2f;
            TOUCH_SIZE_MAX = 0.4f;
        }

        return Utils.normalize(size * getNormalizedPressure(), TOUCH_SIZE_MIN, TOUCH_SIZE_MAX);
    }

    public float getTiltX() {


        return Utils.clamp(getVelocity().getX() * TILT_SCALE, -MAX_TILT, MAX_TILT);
    }

    public float getTiltY() {

        return Utils.clamp(getVelocity().getY() * TILT_SCALE, -MAX_TILT, MAX_TILT);
    }
}
