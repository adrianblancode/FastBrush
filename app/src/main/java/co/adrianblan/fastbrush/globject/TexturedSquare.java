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

import android.opengl.GLES30;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import co.adrianblan.fastbrush.utils.GLhelper;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public class TexturedSquare {

    protected static final String DEFAULT_VERTEX_SHADER_CODE =
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

    protected static final String DEFAULT_FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
            "}";

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer textureDrawOrderBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;

    protected static final float DEFAULT_SQUARE_COORDS[] = {
            -1.0f,  1.0f, 0.0f,   // top left
            -1.0f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,    // bottom right
            1.0f,  1.0f, 0.0f     // top right
    };

    protected static final float DEFAULT_TEXTURE_COORDS[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    protected static final short DEFAULT_TEXTURE_DRAW_ORDER[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    protected static final int DEFAULT_TEXTURE_VERTEX_STRIDE = 2 * 4;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public TexturedSquare() {
        vertexBuffer = GLhelper.initFloatBuffer(DEFAULT_SQUARE_COORDS);
        textureBuffer = GLhelper.initFloatBuffer(DEFAULT_TEXTURE_COORDS);
        textureDrawOrderBuffer = GLhelper.initShortBuffer(DEFAULT_TEXTURE_DRAW_ORDER);

        mProgram = GLES30.glCreateProgram();
        GLhelper.loadShaders(mProgram, DEFAULT_VERTEX_SHADER_CODE, DEFAULT_FRAGMENT_SHADER_CODE);
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

        // Enable a handle to the vertices
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
                DEFAULT_TEXTURE_VERTEX_STRIDE, textureBuffer);

        // Set the active texture unit to texture unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES30.glUniform1i(mTextureUniformHandle, 0);

        // Draw the square
        GLES30.glDrawElements(
                GLES30.GL_TRIANGLES, DEFAULT_TEXTURE_DRAW_ORDER.length,
                GLES30.GL_UNSIGNED_SHORT, textureDrawOrderBuffer);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }
}
