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
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

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

    private static final String TAG = "MyGLRenderer";

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private Context context;

    private int mWidth;
    private int mHeight;
    private float mRatio;

    private int[] frameBufferArray = new int[1];
    private int[] depthBufferArray = new int[1];
    private int[] renderTextureArray = new int[1];
    private int[] brushTextureArray = new int[1];

    private Point point;
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
        point = new Point();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        // TODO use render to texture?
        EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW), EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);

        mWidth = width;
        mHeight = height;
        mRatio = (float) width / height;

        backBufferSquare = new BackBufferSquare(mRatio);
        generateBackFramebuffer();

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1, 1, 3, 7);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Generate and load textures
        GLES20.glGenTextures(1, brushTextureArray, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, brushTextureArray[0]);
        loadDrawableToTexture(R.drawable.brushdot2, context);

        // Bind standard buffer
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // WARNING: Totally unsupported hack to enable GL_MAX
        int GL_MAX = 0x8008;

        GLES20.glViewport(0, 0, mWidth, mHeight);

        // Bind default buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Bind back buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBufferArray[0]);

        GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glBlendEquationSeparate(GLES20.GL_FUNC_ADD, GL_MAX);

        // Draw everything to back buffer
        for (TouchData td : unrenderedPointArrayList) {
            point.draw(mMVPMatrix, brushTextureArray[0], td);
        }

        if(!unrenderedPointArrayList.isEmpty()) {
            prevTouchData = unrenderedPointArrayList.get(unrenderedPointArrayList.size() - 1);
            unrenderedPointArrayList.clear();
        }

        // Bind default buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // Draw render texture to default buffer
        backBufferSquare.draw(mMVPMatrix, renderTextureArray[0]);
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

        // Depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBufferArray[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mWidth, mHeight);

        // Generate texture
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        // Bind depth buffer and texture to frame buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTextureArray[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBufferArray[0]);

        // Check status of framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0]);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);

        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer error: " + status);
        }
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shaderId = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shaderId, shaderCode);
        GLES20.glCompileShader(shaderId);

        return shaderId;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /** Takes touch data information, and interpolates objects based on a distance to the previous object */
    private void addInterpolatedTouchData(TouchData touchData){

        final float MIN_DISTANCE = 0.005f;

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