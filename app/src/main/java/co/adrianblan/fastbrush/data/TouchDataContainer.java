package co.adrianblan.fastbrush.data;

import java.util.ArrayList;

import co.adrianblan.fastbrush.utils.Utils;

/**
 * Class which contains TouchData objects and methods.
 */
public class TouchDataContainer {

    private ArrayList<TouchData> touchDataList;
    private TouchData prevTouchData;

    public TouchDataContainer() {
        touchDataList = new ArrayList<>();
    }

    /** Takes touch data information, and interpolates objects based on a distance to the previous object */
    public void addInterpolated(TouchData touchData){

        final float MIN_DISTANCE = 0.003f;

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


            if(true) {
                // Throttle values so that they do not increase too quickly
                float size = Utils.getThrottledValue(parentTouchData.getSize(), touchData.getSize());
                float pressure = Utils.getThrottledValue(parentTouchData.getPressure(), touchData.getPressure());

                TouchData td = new TouchData(touchData.getPosition(), size, pressure);

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

        float size = Utils.getThrottledValue(prevSize, touchData.getSize());
        float pressure  = Utils.getThrottledValue(prevPressure, touchData.getPressure());

        TouchData interpolatedTouchData = new TouchData(x, y, size, pressure);

        return interpolatedTouchData;
    }

    /** Adds a TouchData object to the list */
    private void add(TouchData touchData) {
        touchDataList.add(touchData);
        prevTouchData = touchData;
    }

    /** Returns the list of TouchData */
    public ArrayList<TouchData> get() {
        return touchDataList;
    }

    /** Gets the last TouchData if it exists, otherwise null */
    public TouchData getLast() {
        if(hasTouchData()) {
            return touchDataList.get(touchDataList.size() - 1);
        } else {
            return null;
        }
    }

    /** Returns whether there is any TouchData in the list */
    public boolean hasTouchData() {
        return !touchDataList.isEmpty();
    }

    /** Clears all the TouchData */
    public void clear() {
        touchDataList.clear();
    }

    public void touchHasEnded() {
        prevTouchData = null;
    }
}
