package co.adrianblan.fastbrush.settings;

import java.util.Set;

import co.adrianblan.fastbrush.utils.ColorWrapper;

/**
 * Data to store user settings.
 */
public class SettingsData {

    private float size;
    private int numBristles;
    private float pressureFactor;
    private float bristleThickness;

    private boolean isDry;
    private float opacity;
    private ColorWrapper colorWrapper;

    private boolean showBrushView;

    public SettingsData(){
        size = 0.5f;
        numBristles = 300;
        pressureFactor = 1.0f;
        bristleThickness = 0.3f;

        isDry = true;
        opacity = 0.8f;
        colorWrapper = new ColorWrapper(0, 0, 0, 150);

        showBrushView = true;
    }

    public SettingsData clone() {
        SettingsData sd = new SettingsData();

        sd.setSize(size);
        sd.setNumBristles(numBristles);
        sd.setPressureFactor(pressureFactor);
        sd.setBristleThickness(bristleThickness);

        sd.setIsDry(isDry);
        sd.setOpacity(opacity);
        sd.setColorWrapper(colorWrapper);

        sd.setShowBrushView(showBrushView);

        return sd;
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

    public float getBristleThickness() {
        return bristleThickness;
    }

    public void setBristleThickness(float bristleThickness) {
        this.bristleThickness = bristleThickness;
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

    public ColorWrapper getColorWrapper() {
        return colorWrapper;
    }

    public void setColorWrapper(ColorWrapper colorWrapper) {
        this.colorWrapper = colorWrapper;
    }

    public boolean isShowBrushView() {
        return showBrushView;
    }

    public void setShowBrushView(boolean showBrushView) {
        this.showBrushView = showBrushView;
    }
}
