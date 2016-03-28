package co.adrianblan.fastbrush.utils;

/**
 * Class which computes averages of time profiling.
 */
public class TimeProfilerHelper {
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
}
