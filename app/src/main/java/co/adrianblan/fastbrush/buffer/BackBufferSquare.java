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
package co.adrianblan.fastbrush.buffer;

import android.opengl.GLES30;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import co.adrianblan.fastbrush.globject.GLobject;
import co.adrianblan.fastbrush.globject.TexturedSquare;
import co.adrianblan.fastbrush.utils.GLhelper;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public class BackBufferSquare {

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private static ShortBuffer textureDrawOrderBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public BackBufferSquare(float ratio) {
        vertexBuffer = GLhelper.initFloatBuffer(getTranslatedVertexCoords(TexturedSquare.DEFAULT_SQUARE_COORDS, ratio));
        textureBuffer = GLhelper.initFloatBuffer(TexturedSquare.DEFAULT_TEXTURE_COORDS);
        textureDrawOrderBuffer = GLhelper.initShortBuffer(TexturedSquare.DEFAULT_TEXTURE_DRAW_ORDER);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, TexturedSquare.DEFAULT_VERTEX_SHADER_CODE,
                TexturedSquare.DEFAULT_FRAGMENT_SHADER_CODE);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix, int texture) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        GLhelper.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
                mPositionHandle, GLobject.DEFAULT_COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                GLobject.DEFAULT_VERTEX_STRIDE, vertexBuffer);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLhelper.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLhelper.checkGlError("glUniformMatrix4fv");

        /*  Textures */
        mTextureUniformHandle = GLES30.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES30.glGetAttribLocation(mProgram, "a_TexCoordinate");

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
                mTextureCoordinateHandle, 2,
                GLES30.GL_FLOAT, false,
                TexturedSquare.DEFAULT_TEXTURE_VERTEX_STRIDE, textureBuffer);

        // Set the active texture unit to texture unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES30.glUniform1i(mTextureUniformHandle, 0);

        // Draw the square
        GLES30.glDrawElements(
                GLES30.GL_TRIANGLES, TexturedSquare.DEFAULT_TEXTURE_DRAW_ORDER.length,
                GLES30.GL_UNSIGNED_SHORT, textureDrawOrderBuffer);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle);
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
