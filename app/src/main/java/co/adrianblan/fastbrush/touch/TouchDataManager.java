package co.adrianblan.fastbrush.touch;

import android.util.Log;

import java.util.ArrayList;

import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector2;

/**
 * Class which contains TouchData objects and methods.
 */
public class TouchDataManager {

    private ArrayList<TouchData> touchDataList;
    private TouchData prevTouchData;
    private boolean touchHasEnded;
    private boolean touchIsEnding;

    private int numTouches;
    private float averageTouchSize;
    private float minTouchSize;
    private float maxTouchSize;

    public TouchDataManager() {
        touchDataList = new ArrayList<>();
        touchHasEnded = true;
        minTouchSize = 99999;
    }

    public TouchDataManager(int numTouches, float averageTouchSize, float minTouchSize, float maxTouchSize) {
        this();
        this.numTouches = numTouches;
        this.averageTouchSize = averageTouchSize;
        this.minTouchSize = minTouchSize;
        this.maxTouchSize = maxTouchSize;
    }

    /** Takes touch data information, and interpolates objects based on a distance to the previous object */
    public void addInterpolated(TouchData touchData){

        final float MIN_DISTANCE = 0.003f;

        addTouchStatistics(touchData);

        if(prevTouchData == null) {
            add(touchData);
        } else {

            float distance = touchData.getPosition().distance(prevTouchData.getPosition());

            // parent touch data is the last data from the previous event
            TouchData parentTouchData = prevTouchData;

            // prev touch data is the last data from both the previous and current event
            TouchData prevInterpolatedTouchData = parentTouchData;

            int maxInterpolations = (int) (distance / MIN_DISTANCE);

            // Interpolate so that there are no gaps larger than MIN_DISTANCE
            if (maxInterpolations > 0) {

                for (int i = 0; i < maxInterpolations; i++) {

                    prevInterpolatedTouchData = getInterpolatedTouchData(touchData, parentTouchData,
                            prevInterpolatedTouchData, i, maxInterpolations);

                    add(prevInterpolatedTouchData);
                }
            }


            if(distance < MIN_DISTANCE && !touchIsEnding) {
                // Throttle values so that they do not increase too quickly
                float size = Utils.getThrottledValue(prevInterpolatedTouchData.getSize(), touchData.getSize(), 0.01f);
                float pressure = Utils.getThrottledValue(prevInterpolatedTouchData.getPressure(), touchData.getPressure());
                float xv = Utils.getThrottledValue(prevInterpolatedTouchData.velocity.x, touchData.velocity.x);
                float yv = Utils.getThrottledValue(prevInterpolatedTouchData.velocity.y, touchData.velocity.y);

                TouchData td = new TouchData(touchData.getPosition(), new Vector2(xv, yv), size, pressure);

                add(td);
            }
        }
    }

    /**
     * Cretates an interpolated TouchData object.
     *
     * @param touchData the TouchData object to interpolate to
     * @param parentTouchData the last TouchData object from the last touch event
     * @param prevIntepolatedTouchData the last TouchData object from the current touch event
     * @param interpolation the current interpolation
     * @param maxInterpolations the maximum number of interpolations
     * @return
     */
    private TouchData getInterpolatedTouchData(TouchData touchData, TouchData parentTouchData,
                                          TouchData prevIntepolatedTouchData,
                                          int interpolation, int maxInterpolations) {

        float interpolationScale = (interpolation + 1f) / ((float) maxInterpolations + 1f);

        float x = parentTouchData.position.x + (touchData.position.x - parentTouchData.position.x)
                * interpolationScale;

        float y = parentTouchData.position.y + (touchData.position.y - parentTouchData.position.y)
                * interpolationScale;

        float xv = Utils.getThrottledValue(prevIntepolatedTouchData.velocity.x, touchData.velocity.x);
        float yv = Utils.getThrottledValue(prevIntepolatedTouchData.velocity.y, touchData.velocity.y);

        float size = Utils.getThrottledValue(prevIntepolatedTouchData.getSize(), touchData.getSize(), 0.01f);
        float pressure  = Utils.getThrottledValue(prevIntepolatedTouchData.getPressure(), touchData.getPressure());

        TouchData interpolatedTouchData = new TouchData(x, y, xv, yv, size, pressure);

        return interpolatedTouchData;
    }

    /** Adds a TouchData object to the list */
    private void add(TouchData touchData) {
        normalizeTouchSize(touchData);
        touchDataList.add(touchData);
        prevTouchData = touchData;
        touchHasEnded = false;
    }

    private void addTouchStatistics(TouchData touchData) {
        averageTouchSize = (averageTouchSize * (numTouches / (numTouches + 1f)))
                + (touchData.getSize() / (numTouches + 1f));

        numTouches++;

        minTouchSize = Math.min(touchData.getSize(), minTouchSize);
        maxTouchSize = Math.max(touchData.getSize(), maxTouchSize);
    }

    /** Returns the list of TouchData */
    public ArrayList<TouchData> get() {
        return touchDataList;
    }

    /** Returns whether there is any TouchData in the list */
    public boolean hasTouchData() {
        return !touchDataList.isEmpty();
    }

    /** Gets the last TouchData if it exists, otherwise null */
    public TouchData getLast() {
        return prevTouchData;
    }

    public boolean hasLast() {
        return prevTouchData != null;
    }

    /** Clears all the TouchData */
    public void clear() {
        touchDataList.clear();
    }

    public void touchIsEnding() {
        touchIsEnding = true;
    }

    public void touchHasEnded() {
        prevTouchData = null;
        touchHasEnded = true;
        touchIsEnding = false;
    }

    public boolean hasTouchEnded() {
        return touchHasEnded;
    }

    public int getNumTouches() {
        return numTouches;
    }

    public float getAverageTouchSize() {
        return averageTouchSize;
    }

    public float getMinTouchSize() {
        return minTouchSize;
    }

    public float getMaxTouchSize() {
        return maxTouchSize;
    }

    /** Takes a touch size sets it to the normalized size [0, 1] */
    public void normalizeTouchSize(TouchData td) {

        float minTouchSizeAvgDistance = averageTouchSize - minTouchSize;
        float normalizedTouchSizeMin = minTouchSize + minTouchSizeAvgDistance / 2f;
        float normalizedTouchSizeMax = Math.min(averageTouchSize + minTouchSizeAvgDistance * 2, maxTouchSize);

        float normalizedSize = Utils.normalize(td.getSize(), normalizedTouchSizeMin,
                normalizedTouchSizeMax);

        td.setNormalizedSize(normalizedSize);
    }
}
