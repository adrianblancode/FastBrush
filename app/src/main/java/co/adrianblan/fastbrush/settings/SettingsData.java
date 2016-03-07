package co.adrianblan.fastbrush.settings;

/**
 * Data to store user settings.
 */
public class SettingsData {

    private float size;
    private int numBristles;
    private float pressureFactor;
    private boolean isDry;
    private float opacity;

    public SettingsData(){
        size = 1.0f;
        numBristles = 500;
        pressureFactor = 1.0f;
        isDry = true;
        opacity = 0.8f;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public int getNumBristles() {
        return numBristles;
    }

    public void setNumBristles(int numBristles) {
        this.numBristles = numBristles;
    }

    public float getPressureFactor() {
        return pressureFactor;
    }

    public void setPressureFactor(float pressureFactor) {
        this.pressureFactor = pressureFactor;
    }

    public boolean isDry() {
        return isDry;
    }

    public void setIsDry(boolean isDry) {
        this.isDry = isDry;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }
}
