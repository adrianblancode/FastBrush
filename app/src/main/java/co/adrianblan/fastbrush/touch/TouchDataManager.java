package co.adrianblan.fastbrush.touch;

import android.util.Log;

import java.util.ArrayList;

import co.adrianblan.fastbrush.utils.Utils;

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

        final float MIN_DISTANCE = 0.002f;

        addTouchStatistics(touchData);

        if(prevTouchData == null) {
            add(touchData);
        } else {

            float distance = touchData.getPosition().distance(prevTouchData.getPosition());
            TouchData parentTouchData = prevTouchData;
            TouchData prevInterpolatedTouchData = parentTouchData;

            int maxInterpolations = (int) (distance / MIN_DISTANCE);

            // Interpolate so that there are no gaps larger than MIN_DISTANCE
            if (maxInterpolations > 0) {

                for (int i = 0; i < maxInterpolations; i++) {

                    prevInterpolatedTouchData = getInterpolatedTouchData(touchData, parentTouchData,
                            prevInterpolatedTouchData.getSize(), prevInterpolatedTouchData.getPressure(),
                            i, maxInterpolations);

                    add(prevInterpolatedTouchData);
                }
            }


            if(distance < MIN_DISTANCE && !touchIsEnding) {
                // Throttle values so that they do not increase too quickly
                float size = Utils.getThrottledValue(parentTouchData.getSize(), touchData.getSize(), 0.01f);
                float pressure = Utils.getThrottledValue(parentTouchData.getPressure(), touchData.getPressure());

                TouchData td = new TouchData(touchData.getPosition(), touchData.getVelocity(), size, pressure);

                add(td);
            }
        }
    }

    /**
     * Cretates an interpolated TouchData object.
     *
     * @param touchData the TouchData object to interpolate to
     * @param parentTouchData the TouchData object to interpolate position from
     * @param prevSize the size to interpolate from
     * @param prevPressure the pressure to interpolate from
     * @param interpolation the current interpolation
     * @param maxInterpolations the maximum number of interpolations
     * @return
     */
    private TouchData getInterpolatedTouchData(TouchData touchData, TouchData parentTouchData,
                                          float prevSize, float prevPressure,
                                          int interpolation, int maxInterpolations) {

        float x = parentTouchData.getX() + (touchData.getX() - parentTouchData.getX()) *
                (interpolation + 1f) / ((float) maxInterpolations + 1f);
        float y = parentTouchData.getY() + (touchData.getY() - parentTouchData.getY()) *
                (interpolation + 1f) / ((float) maxInterpolations + 1f);

        float xv = parentTouchData.getVelocity().getX() + (touchData.getVelocity().getX() - parentTouchData.getVelocity().getX()) *
                (interpolation + 1f) / ((float) maxInterpolations + 1f);
        float yv = parentTouchData.getVelocity().getY() + (touchData.getVelocity().getY() - parentTouchData.getVelocity().getY()) *
                (interpolation + 1f) / ((float) maxInterpolations + 1f);

        float size = Utils.getThrottledValue(prevSize, touchData.getSize(), 0.01f);
        float pressure  = Utils.getThrottledValue(prevPressure, touchData.getPressure());

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
