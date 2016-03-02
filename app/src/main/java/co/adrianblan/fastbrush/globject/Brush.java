package co.adrianblan.fastbrush.globject;

import android.opengl.GLES30;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import co.adrianblan.fastbrush.data.TouchData;
import co.adrianblan.fastbrush.utils.GLhelper;
import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector3;

/**
 * Class which contains the writing primitives for the brush.
 */
public class Brush {

    private ArrayList<Bristle> bristles;
    private Vector3 position;
    private float[] vertexData;

    private static final int NUM_BRISTLES = 3000;
    public static final float BRISTLE_THICKNESS = 2f;

    private final FloatBuffer vertexBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    public Brush() {
        bristles = new ArrayList<>();
        position = new Vector3();

        for(int i = 0; i < NUM_BRISTLES; i++){
            bristles.add(new Bristle());
        }

        vertexData = new float[GLobject.DEFAULT_COORDS_PER_VERTEX * 2 * NUM_BRISTLES];
        vertexBuffer = GLhelper.initFloatBuffer(6 * NUM_BRISTLES);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, GLobject.DEFAULT_VERTEX_SHADER_CODE,
                GLobject.DEFAULT_FRAGMENT_SHADER_CODE);
    }

    /** Updates the positions of the brush and all bristles, and puts the data inside the vertexBuffer*/
    public void update(TouchData touchData) {
        position.set(touchData.getPosition(), Bristle.LENGTH - Bristle.TIP_LENGTH * touchData.getNormalizedTouchSize());

        vertexBuffer.position(0);

        int index = 0;
        float [] vector;

        for(Bristle bristle : bristles){
            bristle.update(position);

            vector = bristle.absoluteStart.vector;

            vertexData[index] = vector[0];
            vertexData[index + 1] = vector[1];
            vertexData[index + 2] = vector[2];

            index += 3;
            vector = bristle.absoluteEnd.vector;

            vertexData[index] = vector[0];
            vertexData[index + 1] = vector[1];
            vertexData[index + 2] = vector[2];
            index += 3;
        }

        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
    }

    public void draw(float[] mvpMatrix) {

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
        GLES30.glUniform4fv(mColorHandle, 1, Utils.blackColor, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLhelper.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLhelper.checkGlError("glUniformMatrix4fv");

        // Draw line
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, 2 * NUM_BRISTLES);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}
