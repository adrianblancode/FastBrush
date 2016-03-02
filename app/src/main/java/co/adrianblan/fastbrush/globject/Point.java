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
package co.adrianblan.fastbrush.globject;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import co.adrianblan.fastbrush.data.TouchData;
import co.adrianblan.fastbrush.utils.GLhelper;
import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector2;

/**
 * A Square object for use as a drawn object in OpenGL ES 2.0.
 */
public class Point {

    private static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec2 a_TexCoordinate;" + // Per-vertex texture coordinate information we will pass in.
            "attribute vec4 vPosition;" +
            "varying vec2 v_TexCoordinate;" +   // This will be passed into the fragment shader.
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            // Pass through the texture coordinate.
            "  v_TexCoordinate = a_TexCoordinate;" +
            "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            //"  gl_FragColor = vColor;" +
            "  vec4 col = vColor * texture2D(u_Texture, v_TexCoordinate);" +
            //"  col.w = vColor.w;" +
            "  gl_FragColor = col;" +
            "}";

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private static ShortBuffer textureDrawOrderBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;

    private float [] mColor;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Point() {

        vertexBuffer = GLhelper.initFloatBuffer(TexturedSquare.DEFAULT_SQUARE_COORDS);

        textureBuffer = GLhelper.initFloatBuffer(TexturedSquare.DEFAULT_TEXTURE_COORDS);
        textureDrawOrderBuffer = GLhelper.initShortBuffer(TexturedSquare.DEFAULT_TEXTURE_DRAW_ORDER);

        mProgram = GLES20.glCreateProgram();
        GLhelper.loadShaders(mProgram, vertexShaderCode, fragmentShaderCode);

        mColor = Utils.blackColor;
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix, int texture, TouchData touchData) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // Init properties for touch data
        vertexBuffer.clear();
        vertexBuffer.put(getTranslatedVertexCoords(TexturedSquare.DEFAULT_SQUARE_COORDS, touchData.getPosition(),
                touchData.getNormalizedTouchSize() * 0.2f + 0.03f));
        vertexBuffer.position(0);

        mColor[3] = getAlpha(touchData.getNormalizedTouchSize());

        // Shader code

        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLhelper.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, GLobject.DEFAULT_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                GLobject.DEFAULT_VERTEX_STRIDE, vertexBuffer);

        // Get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLhelper.checkGlError("glGetUniformLocation");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLhelper.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLhelper.checkGlError("glUniformMatrix4fv");

        /*  Textures */
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false,
                TexturedSquare.DEFAULT_TEXTURE_VERTEX_STRIDE, textureBuffer);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, TexturedSquare.DEFAULT_TEXTURE_DRAW_ORDER.length,
                GLES20.GL_UNSIGNED_SHORT, textureDrawOrderBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }

    /** Returns a matrix of vertex coords that has been translated in x and y axis */
    private float[] getTranslatedVertexCoords(float[] vertexCoords, Vector2 position, float vertexScale) {

        float [] coords = vertexCoords.clone();

        //rotateMatrix(coords);

        for(int vertexOffset = 0; vertexOffset < coords.length; vertexOffset += GLobject.DEFAULT_COORDS_PER_VERTEX) {
            coords[vertexOffset + 0] *= vertexScale;
            coords[vertexOffset + 1] *= vertexScale;
            coords[vertexOffset + 2] *= vertexScale;

            coords[vertexOffset + 0] += position.getX();
            coords[vertexOffset + 1] += position.getY();
        }

        return coords;
    }

    /** Rotates a 3x3 matrix by a random amount */
    private void rotateMatrix(float[] coords) {
        float [] vector = {0f, 0f, 0f, 1f};

        int angle = (int)(Math.random() * 361);
        float [] rotationMatrix = new float[16];
        Matrix.setIdentityM(rotationMatrix, 0);

        for(int i = 0; i < 4; i++) {

            vector[0] = coords[0 + i];
            vector[1] = coords[1 + i];
            vector[2] = coords[2 + i];

            // TODO buggy as hell
            Matrix.setRotateM(rotationMatrix, 0, angle, 0, 0, 1f);
            Matrix.multiplyMV(vector, 0, rotationMatrix, 0, vector, 0);

            coords[0 + i] = vector[0];
            coords[1 + i] = vector[1];
            coords[2 + i] = vector[2];
        }
    }

    /** Takes pressure, and returns the alpha for that pressure */
    private static float getAlpha(float normalizedSize) {

        float ALPHA_BASE = 0.50f; // The base alpha level
        float ALPHA_DELTA_MIN = -0.40f; // The maximum negative change treshold in alpha
        float ALPHA_DELTA_MAX = 0.40f; // The maximum positive change treshold in alpha

        return Utils.clamp(ALPHA_BASE * (1f - normalizedSize) * 2.0f, ALPHA_BASE + ALPHA_DELTA_MIN,
                ALPHA_BASE + ALPHA_DELTA_MAX);
    }
}
