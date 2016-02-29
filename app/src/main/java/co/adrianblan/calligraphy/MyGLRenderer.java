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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.util.ArrayList;

import co.adrianblan.calligraphy.globject.BackBufferSquare;
import co.adrianblan.calligraphy.globject.Brush;
import co.adrianblan.calligraphy.globject.Point;
import co.adrianblan.calligraphy.utils.Utils;
import co.adrianblan.calligraphy.vector.Vector2;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final int CAMERA_DISTANCE = 10;

    private Context context;

    private int mWidth;
    private int mHeight;
    private float mRatio;

    private int[] frameBufferArray = new int[1];
    private int[] depthBufferArray = new int[1];
    private int[] renderTextureArray = new int[1];
    private int[] brushTextureArray = new int[1];

    private Brush brush;
    private TouchData prevTouchData;
    private ArrayList<TouchData> unrenderedPointArrayList;
    private BackBufferSquare backBufferSquare;

    public MyGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        unrenderedPointArrayList = new ArrayList<>();
        brush = new Brush();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW), EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);

        mWidth = width;
        mHeight = height;
        mRatio = (float) width / height;

        backBufferSquare = new BackBufferSquare(mRatio);
        generateBackFramebuffer();

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1, 1, CAMERA_DISTANCE, CAMERA_DISTANCE + 3);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -CAMERA_DISTANCE, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Generate and load textures
        GLES20.glGenTextures(1, brushTextureArray, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, brushTextureArray[0]);
        loadDrawableToTexture(R.drawable.brushdot2, context);

        // Bind standard buffer
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // WARNING: Totally unsupported hack to enable GL_MAX
        int GL_MAX = 0x8008;

        GLES20.glViewport(0, 0, mWidth, mHeight);

        // Bind back buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBufferArray[0]);

        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        //GLES20.glBlendEquationSeparate(GLES20.GL_FUNC_ADD, GL_MAX);

        // Enable depth testing to slightly above paper
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearDepthf(0.1f);

        GLES20.glLineWidth(3f);

        // Imprint brush on paper
        for(TouchData td : unrenderedPointArrayList) {
            brush.draw(mMVPMatrix, td);
        }

        // We rendered all the points, now we clear them
        unrenderedPointArrayList.clear();

        // Bind default buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);

        // Disable blending and depth test
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Clear color and depth
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Draw render texture to default buffer
        backBufferSquare.draw(mMVPMatrix, renderTextureArray[0]);

        // Draw brush
        if(prevTouchData != null) {
            brush.draw(mMVPMatrix, prevTouchData);
        }
    }

    /** Loads a drawable into the currently bound texture */
    private void loadDrawableToTexture(int drawable, Context context) {
        // loading texture
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawable);

        // create nearest filtered texture
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // Clean up
        bitmap.recycle();
    }

    /** Generates the framebuffer and texture necessary to render to a second screen */
    private void generateBackFramebuffer() {

        // Generate frame buffer and texture
        GLES20.glGenFramebuffers(1, frameBufferArray, 0);
        GLES20.glGenFramebuffers(1, depthBufferArray, 0);

        GLES20.glGenTextures(1, renderTextureArray, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureArray[0]);

        // Clamp the render texture to the edges
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // Make sure that mWidth and mHeight have been set and are nonzero
        if(mWidth == 0 || mHeight == 0) {
            System.err.println("Height or width can not be zero");
        }

        int[] maxTextureSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);

        if(maxTextureSize[0] < mWidth) {
            System.err.println("Texture size not large enough! " + maxTextureSize[0] + " < " + mWidth);
        }

        // Depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBufferArray[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mWidth, mHeight);

        // Generate texture
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        // Bind depth buffer and texture to back buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTextureArray[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBufferArray[0]);

        // Check status of framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBufferArray[0]);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);

        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer error: " + status);
        }

        // Clear back buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    /** Takes touch data information, and interpolates objects based on a distance to the previous object */
    private void addInterpolatedTouchData(TouchData touchData){

        final float MIN_DISTANCE = 0.004f;

        if(prevTouchData == null) {
            addPoint(touchData);
        } else {

            float distance = touchData.getPosition().distance(prevTouchData.getPosition());
            TouchData parentTouchData = prevTouchData;
            TouchData prevInterpolatedTouchData = prevTouchData;
            int interpolations = (int) (distance / MIN_DISTANCE);

            // Interpolate so that there are no gaps larger than MIN_DISTANCE
            if (interpolations > 0) {

                for (int i = 0; i < interpolations; i++) {

                    float x = parentTouchData.getX() + (touchData.getX() - parentTouchData.getX()) *
                            (i + 1f) / ((float) interpolations + 1f);
                    float y = parentTouchData.getY() + (touchData.getY() - parentTouchData.getY()) *
                            (i + 1f) / ((float) interpolations + 1f);

                    float size = Utils.getThrottledValue(prevInterpolatedTouchData.getSize(),
                            touchData.getSize());
                    float pressure  = Utils.getThrottledValue(prevInterpolatedTouchData.getPressure(),
                            touchData.getPressure());

                    TouchData interpolatedTouchData = new TouchData(x, y, size, pressure);
                    prevInterpolatedTouchData = interpolatedTouchData;
                    addPoint(interpolatedTouchData);
                }
            }

            // Only add if not too close to last point
            if(distance >= MIN_DISTANCE) {

                // Throttle values so that they do not increase too quickly
                float size = Utils.getThrottledValue(prevTouchData.getSize(), touchData.getSize());
                float pressure = Utils.getThrottledValue(prevTouchData.getPressure(), touchData.getPressure());

                TouchData td = new TouchData(touchData.getPosition(), size, pressure);
                addPoint(td);
            }
        }
    }

    public void addPoints(ArrayList<TouchData> touchDataList) {
        for(TouchData td : touchDataList) {
            addInterpolatedTouchData(td);
        }
    }

    /** Takes a coord and adds it to the renderer */
    public void addPoint(TouchData touchData) {
        unrenderedPointArrayList.add(touchData);
        prevTouchData = touchData;
    }

    /** Clears all the ArrayList of Point of all objects*/
    public void clearPoints() {
        unrenderedPointArrayList.clear();
        prevTouchData = null;
    }

    public void clearScreen() {
        clearPoints();

        // Bind back buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBufferArray[0]);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Bind default buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
    }

    /** Translates a viewport vector to world vector */
    public Vector2 viewportToWorld(Vector2 vec) {
        float worldX = -(2 * (vec.getX() / mWidth) - 1) * mRatio;
        float worldY = -(2 * (vec.getY() / mHeight) - 1);

        return new Vector2(worldX, worldY);
    }
}