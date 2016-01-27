/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.adrianblan.calligraphy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public class Point {

    private static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final FloatBuffer vertexBuffer;
    private static ShortBuffer drawListBuffer;
    private static int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private Vector2 mCoord;
    private float mTouchSize;
    private float mTouchPressure;
    private static boolean initialized = false;

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;
    private static float squareCoords[] = {
            -0.05f,  0.05f, 0.0f,   // top left
            -0.05f, -0.05f, 0.0f,   // bottom left
            0.05f, -0.05f, 0.0f,   // bottom right
            0.05f,  0.05f, 0.0f }; // top right

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private static final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private static final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static float blackColor[] = {0f, 0f, 0f, 1.0f};
    static float blueColor[] = { 0.17647058f, 0.63921568f, 0.90980392f, 1f };
    private float [] mColor;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Point(float x, float y, float touchSize, float touchPressure) {

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();

        mCoord = new Vector2(x, y);

        // The touch size is always 0.0 on an emulator
        if(!Utils.floatsAreEquivalent(touchSize, 0f)) {
            mTouchSize = touchSize;
        } else {
            mTouchSize = 1.0f;
        }

        mTouchPressure = touchPressure;

        mColor = blackColor.clone();
        mColor[3] = getAlpha(getNormalizedPressure());
        float [] vertexCoords = getTranslatedVertexCoords(x, y, getNormalizedTouchSize(touchSize, getNormalizedPressure()));

        // add the coordinates to the FloatBuffer
        vertexBuffer.put(vertexCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // Ugly static hack to only initialize common variables once
        // TODO proper initialization
        if(!initialized) {

            // initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);

            // prepare shaders and OpenGL program
            int vertexShader = MyGLRenderer.loadShader(
                    GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = MyGLRenderer.loadShader(
                    GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
            GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
            GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
            GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

            initialized = true;
        }
    }

    public Point() {
        this(0f, 0f, 1.0f, 1.0f);
    }

    public Point(Vector2 vec) {
        this(vec.getX(), vec.getY(), 1.0f, 1.0f);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    /** Returns a matrix of vertex coords that has been translated in x and y axis */
    private float[] getTranslatedVertexCoords(float x, float y, float vertexScale) {
        float [] coords = squareCoords.clone();

        for(int vertexOffset = 0; vertexOffset < coords.length; vertexOffset += COORDS_PER_VERTEX) {
            coords[vertexOffset + 0] *= vertexScale;
            coords[vertexOffset + 1] *= vertexScale;
            coords[vertexOffset + 2] *= vertexScale;

            coords[vertexOffset + 0] += x;
            coords[vertexOffset + 1] += y;
        }

        return coords;
    }

    /** Get the (x, y) position of the point */
    public Vector2 getCoord() {
        return mCoord;
    }

    /** Returns the touch size of the point */
    public float getTouchSize () {
        return mTouchSize;
    }

    /** Returns the touch pressure of the point */
    public float getTouchPressure () {
        return mTouchPressure;
    }

    /** Gets the normalized pressure in proportion to screen size */
    public float getNormalizedPressure() {
        return mTouchPressure / (mTouchSize * 10f);
    }

    /** Takes a touch size and returns the resulting vertex scale */
    public static float getNormalizedTouchSize(float touchSize, float normalizedPressure) {

        float TOUCH_NORMALIZATION_TRESHOLD = 0.3f; // The amount of size which is counted as "zero"
        float SCALE_BASE = 0.5f; // The base scale of the vertex
        float TOUCH_SIZE_MIN = 0.2f; // The minimum touch size treshold
        float TOUCH_SIZE_MAX = 0.4f; // The maximum touch size treshold
        float NORMALIZED_TOUCH_SCALE = 0.7f; // The scale of the normalized touch size

        touchSize = touchSize + touchSize * (1f - TOUCH_NORMALIZATION_TRESHOLD - normalizedPressure);
        float normalizedTouchSize = Utils.normalize(touchSize, TOUCH_SIZE_MIN, TOUCH_SIZE_MAX);
        return SCALE_BASE + normalizedTouchSize * NORMALIZED_TOUCH_SCALE;
    }

    /** Returns a throttled value that is a given percent from previousvalue towards targetValue*/
    public static float getThrottledValue(float previousValue, float targetValue) {
        final float VALUE_SCALE = 0.02f;
        float difference = targetValue - previousValue;

        return previousValue + difference * VALUE_SCALE;
    }

    /** Takes pressure, and returns the alpha for that pressure*/
    public static float getAlpha(float pressure) {

        float ALPHA_BASE = 0.15f; // The base alpha level
        float ALPHA_DELTA_MIN = -0.15f; // The maximum negative change treshold in alpha
        float ALPHA_DELTA_MAX = 0.15f; // The maximum positive change treshold in alpha
        float PRESSURE_NORMALIZATION_TRESHOLD = 0.3f; // The amount of pressure which is counted as "zero"
        float PRESSURE_SCALE = 1.0f; // The amount to scale the pressure by


        float normalizedPressure = (1f - PRESSURE_NORMALIZATION_TRESHOLD - pressure) * PRESSURE_SCALE;
        float alphaDelta = Utils.clamp(normalizedPressure, ALPHA_DELTA_MIN, ALPHA_DELTA_MAX);
        return ALPHA_BASE + alphaDelta;
    }
}
