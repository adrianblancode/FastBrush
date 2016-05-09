package co.adrianblan.fastbrush.touch;

import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector2;

/**
 * Class which encapsulates touch data.
 */
public class TouchData {

    private static final float MAX_TILT = 0.5f;
    private static final float TILT_SCALE = 0.5f;

    public Vector2 position;
    public Vector2 velocity;
    private float size;
    private float pressure;

    private float normalizedSize;

    public TouchData(Vector2 position, Vector2 velocity, float size, float pressure) {

        float resetSize;

        // The touch size is always 0.0 on an emulator
        if (!Utils.floatsAreEquivalent(size, 0f)) {
            resetSize = size;
        } else {
            resetSize = 1.0f;
        }

        set(position, velocity, resetSize, pressure);
    }

    public TouchData(float x, float y, float xv, float yv, float size, float pressure) {
        float resetSize;

        // The touch size is always 0.0 on an emulator
        if (!Utils.floatsAreEquivalent(size, 0f)) {
            resetSize = size;
        } else {
            resetSize = 1.0f;
        }

        set(new Vector2(x, y), new Vector2(xv, yv), resetSize, pressure);
    }

    public TouchData (TouchData touchData){
        set(touchData.position, touchData.velocity, touchData.size, touchData.pressure);
    }

    private void set(Vector2 position, Vector2 velocity, float size, float pressure) {
        this.position = position;
        this.velocity = velocity;
        this.size = size;
        this.normalizedSize = size;
        this.pressure = pressure;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
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

    public float getNormalizedSize() {
        return normalizedSize;
    }

    public void setNormalizedSize(float normalizedSize) {
        this.normalizedSize = normalizedSize;
    }

    /** Gets the normalized pressure in proportion to screen size, 1f is roughly normal */
    public float getNormalizedPressure() {

        if(Utils.floatsAreEquivalent(pressure, 1.0f)) {
            return 1.0f;
        } else {
            return pressure / (size * 10f);
        }
    }

    public float getTiltX() {
        return Utils.clamp(getVelocity().getX() * TILT_SCALE, -MAX_TILT, MAX_TILT);
    }

    public float getTiltY() {

        return Utils.clamp(getVelocity().getY() * TILT_SCALE, -MAX_TILT, MAX_TILT);
    }
}
