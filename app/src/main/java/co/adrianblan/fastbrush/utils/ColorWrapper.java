package co.adrianblan.fastbrush.utils;

import android.graphics.Color;

/**
 * Class which wraps a Color.
 */
public class ColorWrapper {

    private int red;
    private int green;
    private int blue;
    private int alpha;

    public ColorWrapper() {
        this.red = 0;
        this.blue = 0;
        this.green = 0;
        this.alpha = 0;
    }

    public ColorWrapper(int color) {
        setColor(color);
    }

    public ColorWrapper(int red, int green, int blue, int alpha) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = alpha;
    }

    public ColorWrapper(ColorWrapper color) {
        this.red = color.getRed();
        this.blue = color.getBlue();
        this.green = color.getGreen();
        this.alpha = color.getAlpha();
    }

    public void setColor(int color) {
        this.red = Color.red(color);
        this.blue = Color.blue(color);
        this.green = Color.green(color);
        this.alpha = Color.alpha(color);
    }

    public void setColorWithoutAlpha(int color) {
        this.red = Color.red(color);
        this.blue = Color.blue(color);
        this.green = Color.green(color);
    }

    /** Converts the individual components to int with Color.argb() */
    public int getColor() {
        return Color.argb(alpha, red, green, blue);
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public float[] toFloatArray() {
        float[] temp = new float[4];

        temp[0] = Utils.normalize(red, 0, 255);
        temp[1] = Utils.normalize(green, 0, 255);
        temp[2] = Utils.normalize(blue, 0, 255);
        temp[3] = Utils.normalize(alpha, 0, 255);

        return temp;
    }
}