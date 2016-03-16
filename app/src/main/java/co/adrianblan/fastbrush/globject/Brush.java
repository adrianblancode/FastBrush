package co.adrianblan.fastbrush.globject;

import android.opengl.GLES30;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import co.adrianblan.fastbrush.touch.TouchData;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.utils.GLhelper;
import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Class which contains the writing primitives for the brush.
 */
public class Brush {

    public static final float BRUSH_VIEW_BRISTLE_THICKNESS = Utils.convertPixelsToDp(0.2f);

    private int numBristles;
    private float sizePressureFactor;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTextureUniformHandle;
    private float[] vertexData;

    private final FloatBuffer vertexBuffer;

    private ArrayList<Bristle> bristles;
    private Vector3 position;
    private Vector3 jitter;
    private float angle;

    public Brush(SettingsData settingsData) {

        numBristles = settingsData.getNumBristles();
        sizePressureFactor = settingsData.getPressureFactor();

        position = new Vector3();
        resetPosition();

        bristles = new ArrayList<>();
        jitter = new Vector3();

        for(int i = 0; i < numBristles; i++){
            bristles.add(new Bristle(settingsData));
        }

        vertexData = new float[GLobject.DEFAULT_COORDS_PER_VERTEX * 2 * numBristles];
        vertexBuffer = GLhelper.initFloatBuffer(6 * numBristles);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, GLobject.DEFAULT_VERTEX_SHADER_CODE,
                GLColorObject.COLOR_FRAGMENT_SHADER_CODE);
    }

    public void update(TouchData touchData) {
        position.set(touchData.getPosition(), Bristle.length
                - Bristle.tipLength * touchData.getNormalizedSize());

        float xTilt = touchData.getTiltX();
        float yTilt = touchData.getTiltY();

        angle = Utils.clamp((float) (Math.sqrt(xTilt * xTilt + yTilt * yTilt) / (Bristle.BASE_LENGTH)) * 90,
                0, 90);

        update();
    }

    /** Updates the positions of the brush and all bristles, and puts the data inside the vertexBuffer*/
    public void update() {
        updateJitter();
        position.addFast(jitter);

        vertexBuffer.position(0);

        int index = 0;
        float [] vector;

        for(Bristle bristle : bristles){
            bristle.update(position);

            vector = bristle.absoluteTop.vector;

            vertexData[index] = vector[0];
            vertexData[index + 1] = vector[1];
            vertexData[index + 2] = vector[2];

            index += 3;
            vector = bristle.absoluteBottom.vector;

            vertexData[index] = vector[0];
            vertexData[index + 1] = vector[1];
            vertexData[index + 2] = vector[2];
            index += 3;
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
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, 2 * numBristles);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    public Vector3 getPosition() {
        return position;
    }

    public void resetPosition() {
        position.set(0, 0, Bristle.BASE_LENGTH);
        angle = 0;
    }

    public float getAngle() {
        return angle;
    }

    private void updateJitter() {
        float maxJitter = 0.001f;

        float jitterX = (2.0f - 1.0f * (float) Math.random()) * maxJitter;
        float jitterY = (2.0f - 1.0f * (float) Math.random()) * maxJitter;
        //float jitterZ = (2.0f - 1.0f * (float) Math.random()) * maxJitter;

        jitter.set(jitterX, jitterY, 0);
    }
}
