package co.adrianblan.fastbrush.globject;

import android.opengl.GLES30;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import co.adrianblan.fastbrush.database.BristleParameters;
import co.adrianblan.fastbrush.database.BrushKey;
import co.adrianblan.fastbrush.database.BrushSnapshotDatabase;
import co.adrianblan.fastbrush.touch.TouchData;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.utils.GLhelper;
import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector2;
import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Class which contains the writing primitives for the brush.
 */
public class Brush {

    public static final float BRUSH_VIEW_BRISTLE_THICKNESS = Utils.convertPixelsToDp(0.2f);
    private static final int SEGMENTS_PER_BRISTLE = 5;

    private int numBristles;
    private float sizePressureFactor;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private float[] vertexData;

    private final FloatBuffer vertexBuffer;

    private BrushSnapshotDatabase brushSnapshotDatabase;
    private BrushKey brushKey;
    private BristleParameters bristleParameters;

    private ArrayList<Bristle> bristles;
    private Vector3 position;
    private Vector3 jitter;

    private float verticalAngle;
    private float horizontalAngle;
    private float xTilt;
    private float yTilt;

    public Brush(SettingsData settingsData) {

        numBristles = settingsData.getNumBristles();
        sizePressureFactor = settingsData.getPressureFactor();

        position = new Vector3();
        resetPosition();

        brushSnapshotDatabase = new BrushSnapshotDatabase();
        brushKey = new BrushKey();
        bristleParameters = new BristleParameters();

        bristles = new ArrayList<>();
        jitter = new Vector3();

        for(int i = 0; i < numBristles; i++){
            bristles.add(new Bristle(settingsData));
        }

        vertexData = new float[GLobject.DEFAULT_COORDS_PER_VERTEX * 2 * numBristles * SEGMENTS_PER_BRISTLE];
        vertexBuffer = GLhelper.initFloatBuffer(6 * numBristles * SEGMENTS_PER_BRISTLE);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, GLobject.DEFAULT_VERTEX_SHADER_CODE,
                GLobject.DEFAULT_FRAGMENT_SHADER_CODE);
    }

    public void updateBrush(TouchData touchData) {

        float dip = Bristle.BASE_TIP_LENGTH * touchData.getNormalizedSize() * 2f * sizePressureFactor + 0.001f;

        position.set(touchData.getPosition(), Bristle.BASE_LENGTH - dip);

        xTilt = Utils.clamp(Utils.getThrottledValue(xTilt, touchData.getTiltX()), -2f, 2f);
        yTilt = Utils.clamp(Utils.getThrottledValue(yTilt, touchData.getTiltY()), -2f, 2f);

        float tiltLength = (float) Math.sqrt(xTilt * xTilt + yTilt * yTilt);

        float xAngle = (float) Math.toDegrees(Math.acos(xTilt / tiltLength));
        float tempHorizontalAngle = xAngle;

        if (Math.asin(yTilt / tiltLength) < 0) {
            tempHorizontalAngle = Math.abs(360 - tempHorizontalAngle);
        }

        horizontalAngle = tempHorizontalAngle;

        verticalAngle = Utils.clamp(
                Utils.getThrottledValue(verticalAngle, (tiltLength / Bristle.BASE_LENGTH) * 90f, 0.01f),
                0, 90);

        System.out.println("hAngle: " + horizontalAngle + ", vAngle: " + verticalAngle);

        brushKey.set(verticalAngle, position.getZ() / Bristle.BASE_LENGTH);
        bristleParameters.set(brushSnapshotDatabase.getBristleParameter(brushKey));

        updateBristles(bristleParameters);
    }

    /** Updates the positions of the brush and all bristles, and puts the data inside the vertexBuffer*/
    public void updateBristles(BristleParameters bristleParameters) {
        updateJitter();
        position.addFast(jitter);

        vertexBuffer.position(0);

        int index = 0;

        // TODO multithread
        for(Bristle bristle : bristles){
            bristle.update(position);

            float interpolatedX = bristle.absoluteTop.vector[0];
            float interpolatedY = bristle.absoluteTop.vector[1];
            float interpolatedZ = bristle.absoluteTop.vector[2];

            for(int i = 1; i <= SEGMENTS_PER_BRISTLE; i++) {

                float scale = ((float) i / SEGMENTS_PER_BRISTLE) * (bristle.getLength() / Bristle.BASE_LENGTH);

                vertexData[index] = interpolatedX;
                vertexData[index + 1] = interpolatedY;
                vertexData[index + 2] = interpolatedZ;
                index += 3;

                interpolatedX = (1f - scale) * (1f - scale) * bristle.absoluteTop.vector[0]
                        + 2 * (1f - scale) * scale * bristle.absoluteExtendedBottom.vector[0]
                        + scale * scale * (bristle.absoluteBottom.vector[0] - bristleParameters.getPlanarDistanceFromHandle());

                interpolatedY = (1f - scale) * (1f - scale) * bristle.absoluteTop.vector[1]
                        + 2 * (1f - scale) * scale * bristle.absoluteExtendedBottom.vector[1]
                        + scale * scale * bristle.absoluteBottom.vector[1];

                interpolatedZ = (1f - scale) * (1f - scale) * bristle.absoluteTop.vector[2]
                        + 2 * (1f - scale) * scale * 0
                        + scale * scale * 0;

                vertexData[index] = interpolatedX;
                vertexData[index + 1] = interpolatedY;
                vertexData[index + 2] = interpolatedZ;
                index += 3;
            }
        }

        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
    }

    public void draw(float[] mvpMatrix) {
        draw(mvpMatrix, Utils.blackColor);
    }

    public void draw(float[] mvpMatrix, float[] color) {

        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        GLhelper.checkGlError("glGetAttribLocation");

        // Enable a handle to the vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
                mPositionHandle, GLobject.DEFAULT_COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                GLobject.DEFAULT_VERTEX_STRIDE, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLhelper.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLhelper.checkGlError("glUniformMatrix4fv");

        // Draw line
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, 2 * numBristles);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    public Vector3 getPosition() {
        return position;
    }

    public void resetPosition() {
        position.set(0, 0, Bristle.BASE_LENGTH);
        verticalAngle = 0;
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
