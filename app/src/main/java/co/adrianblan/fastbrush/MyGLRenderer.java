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
package co.adrianblan.fastbrush;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import co.adrianblan.fastbrush.data.TouchData;
import co.adrianblan.fastbrush.data.TouchDataManager;
import co.adrianblan.fastbrush.file.ImageSaver;
import co.adrianblan.fastbrush.globject.BackBufferSquare;
import co.adrianblan.fastbrush.globject.Brush;
import co.adrianblan.fastbrush.globject.Line;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.settings.SettingsManager;
import co.adrianblan.fastbrush.utils.Utils;
import co.adrianblan.fastbrush.vector.Vector2;

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

    private static final float CAMERA_DISTANCE = 50;
    private static final float CAMERA_DISTANCE_FAR_SCALE = 5f;

    private static final float IMPRINT_DEPTH = 0.0001f;

    private static final float BRUSH_VIEW_PADDING_HORIZONTAL = 0.25f;
    private static final float BRUSH_VIEW_PADDING_VERTICAL = 0.15f;
    private static final float BRUSH_VIEW_SCALE = 0.3f;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];

    private final float[] mBrushModelMatrix = new float[16];
    private final float[] mBrushModelOffsetMatrix = new float[16];
    private final float[] mBrushProjectionMatrix = new float[16];
    private final float[] mBrushViewMatrix = new float[16];
    private final float[] mBrushMVMatrix = new float[16];
    private final float[] mBrushMVPMatrix = new float[16];

    private final float[] translateToOrigin = new float[16];
    private final float[] translateFromOrigin = new float[16];
    private final float[] rotationMatrix = new float[16];

    private Context context;

    private int mWidth;
    private int mHeight;
    private float mRatio;

    private int[] frameBufferArray = new int[1];
    private int[] depthBufferArray = new int[1];
    private int[] renderTextureArray = new int[1];
    private int[] brushTextureArray = new int[1];

    private SettingsManager settingsManager;
    private SettingsData settingsData;

    private Brush brush;
    private Line line;
    private TouchDataManager touchDataManager;
    private BackBufferSquare backBufferSquare;

    public MyGLRenderer(Context context) {
        this.context = context;
        settingsManager = SettingsManager.getInstance(context);
        settingsData = settingsManager.getSettingsData();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        brush = new Brush(settingsData);
        line = new Line();
        touchDataManager = new TouchDataManager();

        Matrix.setIdentityM(mBrushModelOffsetMatrix, 0);
        Matrix.translateM(mBrushModelOffsetMatrix, 0, 0f, 0f, -1f + BRUSH_VIEW_PADDING_VERTICAL);
        Matrix.scaleM(mBrushModelOffsetMatrix, 0, BRUSH_VIEW_SCALE, BRUSH_VIEW_SCALE, BRUSH_VIEW_SCALE);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES30.glViewport(0, 0, width, height);

        EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW), EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);

        mWidth = width;
        mHeight = height;
        mRatio = (float) width / height;

        backBufferSquare = new BackBufferSquare(mRatio);
        generateBackFramebuffer();

        // This projection matrix is applied to object coordinates in the onDrawFrame() method
        //Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1, 1, CAMERA_DISTANCE, CAMERA_DISTANCE * CAMERA_DISTANCE_FAR_SCALE);
        Matrix.orthoM(mProjectionMatrix, 0, -mRatio, mRatio, -1, 1, CAMERA_DISTANCE, CAMERA_DISTANCE * CAMERA_DISTANCE_FAR_SCALE);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -CAMERA_DISTANCE, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        Matrix.orthoM(mBrushProjectionMatrix, 0, -mRatio, mRatio, -1, 1, CAMERA_DISTANCE,
                CAMERA_DISTANCE * CAMERA_DISTANCE_FAR_SCALE);

        // Generate and load textures
        GLES30.glGenTextures(1, brushTextureArray, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, brushTextureArray[0]);
        loadDrawableToTexture(R.drawable.brushdot2, context);

        // Bind standard buffer
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        if(settingsManager.hasChanges()){
            reconfigureSettingsChanges();
        }

        GLES30.glViewport(0, 0, mWidth, mHeight);

        // Bind back buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBufferArray[0]);

        // Enable blending
        GLES30.glEnable(GLES30.GL_BLEND);

        if(settingsData.isDry()) {
            GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA);
            GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);
        } else {
            GLES30.glBlendFuncSeparate(GLES30.GL_SRC_ALPHA, GLES30.GL_DST_ALPHA, GLES20.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
            GLES30.glBlendEquation(GLES30.GL_MAX);
        }

        // Enable depth testing to slightly above paper
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LEQUAL);
        GLES30.glDepthMask(true);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearDepthf(IMPRINT_DEPTH);

        GLES30.glLineWidth(settingsData.getBristleThickness() * 10f);

        // Imprint brush on paper
        for(TouchData td : touchDataManager.get()) {

            brush.update(td);

            Matrix.setIdentityM(mBrushModelMatrix, 0);
            Matrix.setIdentityM(translateToOrigin, 0);
            Matrix.setIdentityM(translateFromOrigin, 0);
            Matrix.setIdentityM(rotationMatrix, 0);

            Matrix.translateM(translateToOrigin, 0, -brush.getPosition().getX(), -brush.getPosition().getY(), 0);
            Matrix.setRotateM(rotationMatrix, 0, brush.getAngle(), -td.getTiltY(), td.getTiltX(), 0);
            Matrix.translateM(translateFromOrigin, 0, brush.getPosition().getX(), brush.getPosition().getY(), 0);

            Matrix.multiplyMM(mBrushModelMatrix, 0, rotationMatrix, 0, translateToOrigin, 0);
            Matrix.multiplyMM(mBrushModelMatrix, 0, translateFromOrigin, 0, mBrushModelMatrix, 0);

            Matrix.multiplyMM(mBrushMVMatrix, 0, mViewMatrix, 0, mBrushModelMatrix, 0);
            Matrix.multiplyMM(mBrushMVPMatrix, 0, mProjectionMatrix, 0, mBrushMVMatrix, 0);

            brush.draw(mBrushMVPMatrix, settingsData.getColorWrapper().toFloatArray());
        }

        // Bind default buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);

        // Disable blending and depth test
        GLES30.glDisable(GLES30.GL_BLEND);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

        // Clear color and depth
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Draw render texture to default buffer
        backBufferSquare.draw(mMVPMatrix, renderTextureArray[0]);

        // Draw brush
        if(touchDataManager.hasTouchData() && !touchDataManager.hasTouchEnded()) {
            GLES30.glLineWidth(Utils.convertPixelsToDp(20f));
            brush.draw(mBrushMVPMatrix, Utils.brownColor);
        }

        if(settingsData.isShowBrushView() && !touchDataManager.hasTouchEnded()) {

            // Draw brush view line
            Matrix.setLookAtM(mBrushViewMatrix, 0,
                    0 + mRatio - BRUSH_VIEW_PADDING_HORIZONTAL, -CAMERA_DISTANCE - 1.0f, 0f,
                    0 + mRatio - BRUSH_VIEW_PADDING_HORIZONTAL, 0f, 0f,
                    0f, 0.0f, 1.0f);

            Matrix.multiplyMM(mBrushMVMatrix, 0, mBrushViewMatrix, 0, mBrushModelOffsetMatrix, 0);
            Matrix.multiplyMM(mBrushMVPMatrix, 0, mBrushProjectionMatrix, 0, mBrushMVMatrix, 0);

            GLES30.glLineWidth(Utils.convertPixelsToDp(15f));
            line.draw(mBrushMVPMatrix, Utils.brownColor);

            // Draw brush view brush
            Matrix.setLookAtM(mBrushViewMatrix, 0,
                    (brush.getPosition().getX() * BRUSH_VIEW_SCALE) + mRatio - BRUSH_VIEW_PADDING_HORIZONTAL, -CAMERA_DISTANCE - 1.0f, 0f,
                    (brush.getPosition().getX() * BRUSH_VIEW_SCALE) + mRatio - BRUSH_VIEW_PADDING_HORIZONTAL, 0f, 0f,
                    0f, 0.0f, 1.0f);

            Matrix.multiplyMM(mBrushModelMatrix, 0, mBrushModelOffsetMatrix, 0, mBrushModelMatrix, 0);
            Matrix.multiplyMM(mBrushMVMatrix, 0, mBrushViewMatrix, 0, mBrushModelMatrix, 0);
            Matrix.multiplyMM(mBrushMVPMatrix, 0, mBrushProjectionMatrix, 0, mBrushMVMatrix, 0);

            GLES30.glLineWidth(Brush.BRUSH_VIEW_BRISTLE_THICKNESS);
            brush.draw(mBrushMVPMatrix, Utils.brownColor);

        }

        // We are done rendering TouchData, now we clear them
        touchDataManager.clear();
    }

    /** Loads a drawable into the currently bound texture */
    private void loadDrawableToTexture(int drawable, Context context) {
        // loading texture
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawable);

        // create nearest filtered texture
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

        // Clean up
        bitmap.recycle();
    }

    /** Generates the framebuffer and texture necessary to render to a second screen */
    private void generateBackFramebuffer() {

        // Generate frame buffer and texture
        GLES30.glGenFramebuffers(1, frameBufferArray, 0);
        GLES30.glGenFramebuffers(1, depthBufferArray, 0);

        GLES30.glGenTextures(1, renderTextureArray, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, renderTextureArray[0]);

        // Clamp the render texture to the edges
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);

        // Make sure that mWidth and mHeight have been set and are nonzero
        if(mWidth == 0 || mHeight == 0) {
            System.err.println("Height or width can not be zero");
        }

        int[] maxTextureSize = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);

        if(maxTextureSize[0] < mWidth) {
            System.err.println("Texture size not large enough! " + maxTextureSize[0] + " < " + mWidth);
        }

        // Depth buffer
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBufferArray[0]);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, mWidth, mHeight);

        // Generate texture
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mWidth, mHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);

        // Bind depth buffer and texture to back buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, renderTextureArray[0], 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, depthBufferArray[0]);

        // Check status of framebuffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBufferArray[0]);
        int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);

        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer error: " + status);
        }

        // Clear back buffer
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }


    /** Takes a list of TouchData and adds it to the container */
    public void addTouchData(ArrayList<TouchData> touchDataList) {
        for(TouchData touchData : touchDataList) {
            touchDataManager.addInterpolated(touchData);
        }
    }

    /** Clears all the ArrayList of Point of all objects*/
    public void touchHasEnded() {

        touchDataManager.clear();
        touchDataManager.touchIsEnding();

        if(touchDataManager.hasLast()) {
            ArrayList<TouchData> touchDataList = new ArrayList<>();

            TouchData td = new TouchData(touchDataManager.getLast());
            td.setPosition(td.getX() + Math.min(td.getTiltX(), 0.001f), td.getY() + Math.min(td.getTiltY(), 0.001f));
            td.setSize(0f);

            touchDataList.add(td);
            addTouchData(touchDataList);
        }

        touchDataManager.touchHasEnded();
    }

    public void clearScreen() {
        touchHasEnded();

        // Bind back buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferArray[0]);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBufferArray[0]);

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Bind default buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);
    }

    /** Translates a viewport vector to world vector */
    public Vector2 viewportToWorld(Vector2 vec) {
        float worldX = -(2 * (vec.getX() / mWidth) - 1) * mRatio;
        float worldY = -(2 * (vec.getY() / mHeight) - 1);

        return new Vector2(worldX, worldY);
    }

    /** Saves the image on the current screen */
    public void saveImage() {
        ImageSaver.saveImageToStorage(createBitmapFromScreen(), context);
    }

    /** Creates a bitmap from the current screen */
    private Bitmap createBitmapFromScreen() {

        // Bind default buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);

        int screenshotSize = mWidth * mHeight;

        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());

        // Read the pixels from the default buffer
        GLES30.glReadPixels(0, 0, mWidth, mHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, bb);

        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);

        // Create bitmap of the default buffer
        Bitmap brushBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        brushBitmap.setPixels(pixelsBuffer, screenshotSize - mWidth, -mWidth, 0, 0, mWidth, mHeight);

        // Create bitmap of the background buffer
        Bitmap backgroundTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.paperbright);
        Bitmap backgroundTextureScaled = Bitmap.createScaledBitmap(backgroundTexture, mWidth, mHeight, true);

        // Create a canvas for compositing the two images
        Canvas canvasImage = new Canvas(backgroundTextureScaled);
        canvasImage.drawBitmap(brushBitmap, 0f, 0f, null);

        return backgroundTextureScaled;
    }

    private void reconfigureSettingsChanges() {
        settingsManager.setChangesRead();
        settingsData = settingsManager.getSettingsData();
        brush = new Brush(settingsData);
    }
}