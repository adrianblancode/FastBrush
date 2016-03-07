package co.adrianblan.fastbrush.globject;

import android.opengl.GLES30;

import java.nio.FloatBuffer;

import co.adrianblan.fastbrush.utils.GLhelper;
import co.adrianblan.fastbrush.utils.Utils;

/**
 * Class to draw a simple line to show the canvas in the orthogonal view.
 */
public class Line {

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private final FloatBuffer vertexBuffer;

    private static final float LINE_LENGTH = 1.0f;
    private final float[] start = {LINE_LENGTH / 2f, 0f, 0f};
    private final float[] end = {-LINE_LENGTH / 2f, 0f, 0f};

    public Line() {
        vertexBuffer = GLhelper.initFloatBuffer(6);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, GLobject.DEFAULT_VERTEX_SHADER_CODE,
                GLobject.DEFAULT_FRAGMENT_SHADER_CODE);
    }

    public void draw(float[] mvpMatrix) {
        draw(mvpMatrix, Utils.blackColor);
    }

    public void draw(float[] mvpMatrix, float[] color) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        vertexBuffer.clear();
        vertexBuffer.put(start, 0, 3);
        vertexBuffer.put(end, 0, 3);
        vertexBuffer.position(0);

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
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, 2);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}
