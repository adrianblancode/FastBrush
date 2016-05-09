package co.adrianblan.fastbrush.utils;

/**
 * Class which computes averages of time profiling.
 */
public class TimeProfiler {
    private float average;
    private long count;

    public void add(float val) {
        average = (average * (count / (count + 1f)))
                + (val / (count + 1f));

        count++;
    }

    public float getAverage() {
        return average;
    }

    public long getCount() {
        return  count;
    }

    public void reset() {
        average = 0;
        count = 0;
    }
}
