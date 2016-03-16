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

    public static final float BRUSH_VIEW_BRISTLE_THICKNESS = Utils.convertPixelsToDp(0.5f);
    private static final int SEGMENTS_PER_BRISTLE = 4;
    private static final float MAX_TILT_VERTICAL = 20f;

    private int numBristles;
    private float sizePressureFactor;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTextureUniformHandle;
    private float[] vertexData;

    private final FloatBuffer vertexBuffer;

    private BrushSnapshotDatabase brushSnapshotDatabase;
    private BrushKey brushKey;
    private BristleParameters bristleParameters;

    private Bristle[] bristles;
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

        brushSnapshotDatabase = new BrushSnapshotDatabase();
        brushKey = new BrushKey();
        bristleParameters = new BristleParameters();

        bristles = new Bristle[numBristles];
        jitter = new Vector3();

        for(int i = 0; i < numBristles; i++){
            bristles[i] = new Bristle(settingsData);
        }

        vertexData = new float[GLobject.DEFAULT_COORDS_PER_VERTEX * 2 * numBristles * SEGMENTS_PER_BRISTLE];
        vertexBuffer = GLhelper.initFloatBuffer(GLobject.DEFAULT_COORDS_PER_VERTEX * 2 * numBristles * SEGMENTS_PER_BRISTLE);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, GLobject.DEFAULT_VERTEX_SHADER_CODE,
                GLColorObject.COLOR_FRAGMENT_SHADER_CODE);
    }

    public void updateBrush(TouchData touchData) {

        dip = Utils.getThrottledValue(dip,
                Bristle.BASE_TIP_LENGTH * touchData.getNormalizedSize() * 1.2f * sizePressureFactor + 0.001f);

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
                0, MAX_TILT_VERTICAL);

        //System.out.println("hAngle: " + horizontalAngle + ", vAngle: " + verticalAngle);

        brushKey.set(verticalAngle, position.getZ() / Bristle.BASE_LENGTH);
        bristleParameters.set(brushSnapshotDatabase.getBristleParameter(brushKey));

        updateBristles(horizontalAngle, bristleParameters);
    }

    /** Updates the positions of the brush and all bristles, and puts the data inside the vertexBuffer*/
    public void updateBristles(float horizontalAngle, BristleParameters bristleParameters) {
        updateJitter();
        //position.addFast(jitter);

        vertexBuffer.position(0);

        int index = 0;

        float cosHorizontalAngle = (float) Math.cos(Math.toRadians(horizontalAngle));
        float sinHorizontalAngle = (float) Math.sin(Math.toRadians(horizontalAngle));

        // TODO multithread
        for(Bristle bristle : bristles){
            //bristle.update(position);

            // Update bristle position
            bristle.absoluteTop.vector[0] = bristle.top.vector[0] +  position.vector[0];
            bristle.absoluteTop.vector[1] = bristle.top.vector[1] +  position.vector[1];
            bristle.absoluteTop.vector[2] = bristle.top.vector[2] +  position.vector[2];

            bristle.absoluteBottom.vector[0] = bristle.bottom.vector[0] +  position.vector[0];
            bristle.absoluteBottom.vector[1] = bristle.bottom.vector[1] +  position.vector[1];
            bristle.absoluteBottom.vector[2] = bristle.bottom.vector[2] +  position.vector[2];

            bristle.absoluteExtendedBottom.vector[0] = bristle.extendedBottom.vector[0] +  position.vector[0];
            bristle.absoluteExtendedBottom.vector[1] = bristle.extendedBottom.vector[1] +  position.vector[1];
            bristle.absoluteExtendedBottom.vector[2] = bristle.extendedBottom.vector[2] +  position.vector[2];

            float interpolatedX = bristle.absoluteTop.vector[0];
            float interpolatedY = bristle.absoluteTop.vector[1];
            float interpolatedZ = bristle.absoluteTop.vector[2];

            for(int i = 1; i <= SEGMENTS_PER_BRISTLE; i++) {

                vertexData[index] = interpolatedX;
                vertexData[index + 1] = interpolatedY;
                vertexData[index + 2] = interpolatedZ;
                index += 3;

                float scale = ((float) i / SEGMENTS_PER_BRISTLE) * (bristle.length / Bristle.BASE_LENGTH);
                float firstFactor = (1f - scale) * (1f - scale) * (1f - scale);
                float secondFactor = 3 * (1f - scale) * (1f - scale) * scale;
                float thirdFactor = 3 * (1f - scale) * scale * scale;
                float fourthFactor = scale * scale * scale;

                float bottom = bristle.absoluteBottom.vector[2];

                if(bottom < 0) {
                    bottom = 0;
                }

                interpolatedX =
                        firstFactor
                                * bristle.absoluteTop.vector[0]
                        + secondFactor
                                * (bristle.absoluteTop.vector[0] - (bristle.absoluteTop.vector[0] - bristle.absoluteBottom.vector[0])
                                * bristleParameters.upperControlPointLength)
                        + thirdFactor
                                * (bristle.absoluteBottom.vector[0]
                                + cosHorizontalAngle * bristleParameters.planarDistanceFromHandle
                                - cosHorizontalAngle * bristleParameters.lowerControlPointLength)
                        + fourthFactor
                                * (bristle.absoluteBottom.vector[0]
                                + cosHorizontalAngle * bristleParameters.planarDistanceFromHandle);

                interpolatedY =
                        firstFactor
                            * bristle.absoluteTop.vector[1]
                        + secondFactor
                            * (bristle.absoluteTop.vector[1] - (bristle.absoluteTop.vector[1] - bristle.absoluteBottom.vector[1])
                            * bristleParameters.upperControlPointLength)
                        + thirdFactor
                            * (bristle.absoluteBottom.vector[1]
                            + sinHorizontalAngle * bristleParameters.planarDistanceFromHandle
                            - sinHorizontalAngle * bristleParameters.lowerControlPointLength)
                        + fourthFactor
                            * (bristle.absoluteBottom.vector[1]
                            + sinHorizontalAngle * bristleParameters.planarDistanceFromHandle);

                interpolatedZ =
                        firstFactor
                            * bristle.absoluteTop.vector[2]
                        + secondFactor
                            * (bristle.absoluteTop.vector[2] - (bristle.absoluteTop.vector[2] - bristle.absoluteBottom.vector[2])
                            * bristleParameters.upperControlPointLength)
                        + thirdFactor
                            * bottom
                        + fourthFactor
                            * bottom;

                vertexData[index] = interpolatedX;
                vertexData[index + 1] = interpolatedY;
                vertexData[index + 2] = interpolatedZ;
                index += 3;
            }
        }

        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
    }

    public void draw(float[] mvpMatrix, int screenTexture) {
        draw(mvpMatrix, Utils.blackColor, screenTexture);
    }

    public void draw(float[] mvpMatrix, float[] color, int screenTexture) {

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


        /*  Textures */
        mTextureUniformHandle = GLES30.glGetUniformLocation(mProgram, "u_Texture");

        // Set the active texture unit to texture unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, screenTexture);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES30.glUniform1i(mTextureUniformHandle, 0);

        // Draw line
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
