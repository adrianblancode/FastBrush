package co.adrianblan.fastbrush.globject;

import android.opengl.GLES30;

import java.nio.FloatBuffer;

import co.adrianblan.fastbrush.buffer.VertexBufferManager;
import co.adrianblan.fastbrush.database.BristleParameters;
import co.adrianblan.fastbrush.database.BrushKey;
import co.adrianblan.fastbrush.database.BrushParameterDatabaseHandler;
import co.adrianblan.fastbrush.touch.TouchData;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.utils.GLhelper;
import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Class which contains the writing primitives for the brush.
 */
public class Brush {

    public static final float BRUSH_VIEW_BRISTLE_THICKNESS = Utils.convertPixelsToDp(3f);
    public static final int SEGMENTS_PER_BRISTLE = 4;
    private static final float MAX_TILT_VERTICAL = 15f;

    public int numBristles;
    private float sizePressureFactor;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private VertexBufferManager vertexBufferManager;

    private BrushParameterDatabaseHandler brushParameterDatabaseHandler;
    private BrushKey brushKey;
    private BristleParameters bristleParameters;
    private BristleParameters defaultBristleParameters;

    public Bristle[] bristles;
    private Vector3 position;
    private Vector3 jitter;

    private float verticalAngle;
    private float horizontalAngle;
    private float xTilt;
    private float yTilt;
    private float dip;

    public Brush(SettingsData settingsData) {

        numBristles = settingsData.getNumBristles();

        sizePressureFactor = settingsData.getPressureFactor();

        position = new Vector3();
        resetPosition();

        brushParameterDatabaseHandler = new BrushParameterDatabaseHandler();
        brushKey = new BrushKey();
        bristleParameters = new BristleParameters();
        defaultBristleParameters = brushParameterDatabaseHandler.getBrushParameterDatabase().bristleParameters[0];

        bristles = new Bristle[numBristles];
        jitter = new Vector3();

        // Adds the bristles
        for(int i = 0; i < numBristles; i++){
            bristles[i] = new Bristle(settingsData);
        }

        // Manages multiple vertex buffers for us
        vertexBufferManager = new VertexBufferManager(3, GLobject.DEFAULT_COORDS_PER_VERTEX * 2 * numBristles * SEGMENTS_PER_BRISTLE);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, GLobject.DEFAULT_VERTEX_SHADER_CODE,
                GLobject.DEFAULT_FRAGMENT_SHADER_CODE);
    }

    /**
     * Takes a TouchData object and updates the brush accordingly
     */
    public void updateBrush(TouchData touchData) {

        // How far the brush is to dip down from the highest position
        dip = Utils.getThrottledValue(dip,
                Bristle.BASE_TIP_LENGTH * touchData.getNormalizedSize() * 2.0f * sizePressureFactor + 0.001f);

        position.set(touchData.getPosition(), Bristle.BASE_LENGTH - dip);
        
        xTilt = Utils.clamp(Utils.getThrottledValue(xTilt, touchData.getTiltX(), 0.05f), -Bristle.BASE_LENGTH, Bristle.BASE_LENGTH);
        yTilt = Utils.clamp(Utils.getThrottledValue(yTilt, touchData.getTiltY(), 0.05f), -Bristle.BASE_LENGTH, Bristle.BASE_LENGTH);

        float tiltLength = (float) Math.sqrt(xTilt * xTilt + yTilt * yTilt);

        // Calculates the current brush angles
        float xAngle = (float) Math.toDegrees(Math.acos(xTilt / (tiltLength + Utils.EPSILON)));
        float tempHorizontalAngle = xAngle;

        if (Math.asin(yTilt / (tiltLength + Utils.EPSILON)) < 0) {
            tempHorizontalAngle = Math.abs(360 - tempHorizontalAngle);
        }

        float difference = tempHorizontalAngle - horizontalAngle;

        // Make sure we wrap around 360 > 0 (and vice versa correctly)
        if(difference < -180f) {
            difference = 360f + difference;
        } else if(difference > 180f) {
            difference = difference - 360f;
        }

        float horizontalAngleDifferenceRatio = 0.5f;

        // Angle in degrees
        horizontalAngle = ((horizontalAngle + (difference * horizontalAngleDifferenceRatio)) + 360) % 360;

        float maxVerticalAngleStep = 0.05f;

        // Clamp the vertical angle
        verticalAngle = Utils.clamp(
                Utils.getThrottledValue(verticalAngle, (tiltLength / Bristle.BASE_LENGTH) * 90f, maxVerticalAngleStep),
                0, MAX_TILT_VERTICAL);

        // Get the bristle parameters associated with the current brush state
        brushKey.set(verticalAngle, position.getZ() / Bristle.BASE_LENGTH);
        bristleParameters = brushParameterDatabaseHandler.getBristleParameter(brushKey);
    }

    /**
     * Takes a float array and puts the vertex positions inside our vertex buffer.
     */
    public void putVertexData(float[] bristlePositions) {

        vertexBufferManager.setNextBuffer();
        FloatBuffer vertexBuffer = vertexBufferManager.getCurrentBuffer();

        vertexBuffer.position(0);
        vertexBuffer.put(bristlePositions);
        vertexBuffer.position(0);
    }

    public void resetVertexBufferPosition() {
        vertexBufferManager.getCurrentBuffer().position(0);
    }

    /**
     * Takes a mvpMatrix and a color, and draws the brush vertexes currently in our vertex buffer.
     */
    public void draw(float[] mvpMatrix, float[] color) {

        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        //GLhelper.checkGlError("glGetAttribLocation");

        // Enable a handle to the vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
                mPositionHandle, GLobject.DEFAULT_COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                GLobject.DEFAULT_VERTEX_STRIDE, vertexBufferManager.getCurrentBuffer());

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
        //GLhelper.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //GLhelper.checkGlError("glUniformMatrix4fv");

        GLES30.glDrawArrays(GLES30.GL_LINES, 0, 2 * numBristles * SEGMENTS_PER_BRISTLE);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    public Vector3 getPosition() {
        return position;
    }

    public void resetPosition() {
        position.set(0, 0, Bristle.BASE_LENGTH);
        verticalAngle = 0;
        horizontalAngle = 0;
        xTilt = 0;
        yTilt = 0;
        dip = 0;

        bristleParameters = defaultBristleParameters;
    }

    public Bristle[] getBristles() {
        return bristles;
    }

    public float getVerticalAngle() {
        return verticalAngle;
    }

    public float getHorizontalAngle() {
        return horizontalAngle;
    }

    public float getxTilt() {
        return xTilt;
    }

    public float getyTilt() {
        return yTilt;
    }

    public BristleParameters getBristleParameters() {
        return bristleParameters;
    }

    private void updateJitter() {
        float maxJitter = 0.001f;

        float jitterX = (2.0f - 1.0f * (float) Math.random()) * maxJitter;
        float jitterY = (2.0f - 1.0f * (float) Math.random()) * maxJitter;
        //float jitterZ = (2.0f - 1.0f * (float) Math.random()) * maxJitter;

        jitter.set(jitterX, jitterY, 0);
    }
}
