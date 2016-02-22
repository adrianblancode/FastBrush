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

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public class BackBufferSquare extends TexturedSquare {

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private static ShortBuffer textureDrawOrderBuffer;

    private static int sProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public BackBufferSquare(float ratio) {
        vertexBuffer = initBuffer(getTranslatedVertexCoords(DEFAULT_SQUARE_COORDS, ratio));
        textureBuffer = initBuffer(DEFAULT_TEXTURE_COORDS);
        textureDrawOrderBuffer = initBuffer(DEFAULT_TEXTURE_DRAW_ORDER);

        sProgram = GLES20.glCreateProgram();
        loadShaders(sProgram, DEFAULT_VERTEX_SHADER_CODE, DEFAULT_FRAGMENT_SHADER_CODE);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix, int texture) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(sProgram);

        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(sProgram, "vPosition");
        MyGLRenderer.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, DEFAULT_COORDS_PER_SQUARE_VERTEX,
                GLES20.GL_FLOAT, false,
                DEFAULT_SQUARE_VERTEX_STRIDE, vertexBuffer);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(sProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        /*  Textures */
        mTextureUniformHandle = GLES20.glGetUniformLocation(sProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(sProgram, "a_TexCoordinate");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false,
                DEFAULT_TEXTURE_VERTEX_STRIDE, textureBuffer);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, DEFAULT_TEXTURE_DRAW_ORDER.length,
                GLES20.GL_UNSIGNED_SHORT, textureDrawOrderBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }

    /** Returns a matrix of vertex coords that has been translated in x and y axis */
    private float[] getTranslatedVertexCoords(float [] vertexCoords, float ratio) {
        float [] coords = vertexCoords.clone();

        coords[0 * 3] = -1.0f * ratio;
        coords[1 * 3] = -1.0f * ratio;
        coords[2 * 3] = 1.0f * ratio;
        coords[3 * 3] = 1.0f * ratio;

        return coords;
    }
}
