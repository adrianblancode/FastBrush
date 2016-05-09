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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import co.adrianblan.fastbrush.compute.PhysicsCompute;
import co.adrianblan.fastbrush.globject.Bristle;
import co.adrianblan.fastbrush.touch.TouchData;
import co.adrianblan.fastbrush.touch.TouchDataManager;
import co.adrianblan.fastbrush.file.ImageSaver;
import co.adrianblan.fastbrush.buffer.BackBufferManager;
import co.adrianblan.fastbrush.globject.BackBufferSquare;
import co.adrianblan.fastbrush.globject.Brush;
import co.adrianblan.fastbrush.globject.Line;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.settings.SettingsManager;
import co.adrianblan.fastbrush.utils.TimeProfiler;
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

    private static final float IMPRINT_DEPTH = 0.0002f;

    private static final float BRUSH_VIEW_PADDING_HORIZONTAL = 0.25f;
    private static final float BRUSH_VIEW_PADDING_VERTICAL = 0.15f;
    private static final float BRUSH_VIEW_SCALE = 0.3f;

    private static final int NUM_BACK_BUFFERS = 5;

    // Matrices for the camera
    private final float[] mvpMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];

    // Matrices for displaying the brush
    private final float[] brushModelMatrix = new float[16];
    private final float[] brushModelOffsetMatrix = new float[16];
    private final float[] brushProjectionMatrix = new float[16];
    private final float[] brushViewMatrix = new float[16];
    private final float[] brushMVMatrix = new float[16];
    private final float[] brushMVPMatrix = new float[16];

    // Matrices for transforming the brush
    private final float[] translateToOrigin = new float[16];
    private final float[] translateFromOrigin = new float[16];
    private final float[] translateToBrushTip = new float[16];
    private final float[] translateToImprintCenter = new float[16];
    private final float[] verticalRotationMatrix = new float[16];

    private TouchDataManager touchDataManager;
    private SettingsManager settingsManager;
    private SettingsData settingsData;
    private PhysicsCompute physicsCompute;
    private TimeProfiler physicsProfiler;
    private TimeProfiler renderProfiler;
    private TimeProfiler compositeProfiler;


    // Buffers
    private BackBufferSquare backBufferSquare;
    private BackBufferManager backBufferManager;
    private int[] savedPixelBuffer;
    private int[] savedTextureArray;

    // Drawing objects
    private float[] color;
    private Brush brush;
    private Line line;

    private Context context;

    private int width;
    private int height;
    private float screenRatio;


    public MyGLRenderer(Context context) {
        this.context = context;

        // Initialize settings
        settingsManager = SettingsManager.getInstance(context);
        settingsData = settingsManager.getSettingsData();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Clear the background
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        SharedPreferences sp = settingsManager.getSharedPreferences();
        color = settingsData.getColorWrapper().toFloatArray();

        // Get touch variables
        int numTouches = sp.getInt("numTouches", 0);
        float averageTouchSize = sp.getFloat("averageTouchSize", 0);
        float minTouchSize = sp.getFloat("minTouchSize", 99999);
        float maxTouchSize = sp.getFloat("maxTouchSize", 0);

        savedTextureArray = new int[1];

        brush = new Brush(settingsData);
        line = new Line();
        touchDataManager = new TouchDataManager(numTouches, averageTouchSize, minTouchSize, maxTouchSize);

        physicsCompute = new PhysicsCompute(context, brush);
        physicsProfiler = new TimeProfiler();
        renderProfiler = new TimeProfiler();
        compositeProfiler = new TimeProfiler();


        // Set the brush offset matrix for the brush side view
        Matrix.setIdentityM(brushModelOffsetMatrix, 0);
        Matrix.translateM(brushModelOffsetMatrix, 0, 0f, 0f, -1f + BRUSH_VIEW_PADDING_VERTICAL);
        Matrix.scaleM(brushModelOffsetMatrix, 0, BRUSH_VIEW_SCALE, BRUSH_VIEW_SCALE, BRUSH_VIEW_SCALE);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES30.glViewport(0, 0, width, height);

        EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW), EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);

        this.width = width;
        this.height = height;
        screenRatio = (float) width / height;

        backBufferSquare = new BackBufferSquare(screenRatio);
        backBufferManager = new BackBufferManager(NUM_BACK_BUFFERS, this.width, this.height);

        // This projection matrix is applied to object coordinates in the onDrawFrame() method
        //Matrix.frustumM(projectionMatrix, 0, -screenRatio, screenRatio, -1, 1, CAMERA_DISTANCE, CAMERA_DISTANCE * CAMERA_DISTANCE_FAR_SCALE);
        Matrix.orthoM(projectionMatrix, 0, -screenRatio, screenRatio, -1, 1, CAMERA_DISTANCE, CAMERA_DISTANCE * CAMERA_DISTANCE_FAR_SCALE);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -CAMERA_DISTANCE, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.orthoM(brushProjectionMatrix, 0, -screenRatio, screenRatio, -1, 1, CAMERA_DISTANCE,
                CAMERA_DISTANCE * CAMERA_DISTANCE_FAR_SCALE);

        // Bind standard buffer
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        if(savedPixelBuffer != null) {
            //renderPixelBuffer(savedPixelBuffer);
            savedPixelBuffer = null;
        }

        if(settingsManager.hasChanges()){
            reconfigureSettingsChanges();
        }

        GLES30.glViewport(0, 0, width, height);

        // Bind back buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, backBufferManager.getFrameBuffer());
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, backBufferManager.getDepthBuffer());

        // Enable blending
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);

        //GLES30.glBlendFuncSeparate(GLES30.GL_SRC_ALPHA, GLES30.GL_DST_ALPHA, GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        //GLES30.glBlendEquationSeparate(GLES30.GL_FUNC_ADD, GLES30.GL_MAX);


        // Enable depth testing to slightly above paper
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glClearDepthf(IMPRINT_DEPTH);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glDepthFunc(GLES30.GL_LEQUAL);
        GLES30.glDepthMask(true);

        GLES30.glLineWidth(settingsData.getBristleThickness() * 10f);

        /** Imprint brush on paper **/
        for(TouchData td : touchDataManager.get()) {

            long physicsStartTime = System.nanoTime();

            brush.updateBrush(td);
            float[] vertexData = physicsCompute.computeVertexData();

            physicsProfiler.add((System.nanoTime() - physicsStartTime) / 1000000f);

            long renderStartTime = System.nanoTime();
            brush.putVertexData(vertexData);

            Matrix.setIdentityM(brushModelMatrix, 0);
            Matrix.setIdentityM(translateToOrigin, 0);
            Matrix.setIdentityM(translateFromOrigin, 0);
            Matrix.setIdentityM(translateToBrushTip, 0);
            Matrix.setIdentityM(translateToImprintCenter, 0);
            Matrix.setIdentityM(verticalRotationMatrix, 0);

            /**
             * Start model matrix transformations, first to translate brush it's tip,
             * then to rotate it vertically, and lastly move it to the center of the imprint.
             */
            Matrix.translateM(translateToOrigin, 0, -brush.getPosition().vector[0],
                    -brush.getPosition().vector[1], 0);

            float horizontalAngle = brush.getHorizontalAngle();
            float cosHorizontalAngle = (float) Math.cos(Math.toRadians(horizontalAngle));
            float sinHorizontalAngle = (float) Math.sin(Math.toRadians(horizontalAngle));

            Matrix.translateM(translateToBrushTip, 0,
                    -cosHorizontalAngle * brush.getBristleParameters().middlePathDistanceFromHandle * 1.0f,
                    -sinHorizontalAngle * brush.getBristleParameters().middlePathDistanceFromHandle * 1.0f, 0f);

            Matrix.translateM(translateToImprintCenter, 0,
                    cosHorizontalAngle * brush.getBristleParameters().planarImprintLength,
                    sinHorizontalAngle * brush.getBristleParameters().planarImprintLength, 0f);

            Matrix.setRotateM(verticalRotationMatrix, 0, brush.getVerticalAngle() * 0.2f,
                    (float) Math.cos(Math.toRadians(horizontalAngle - 90)),
                    (float) Math.sin(Math.toRadians(horizontalAngle - 90)), 0);

            Matrix.translateM(translateFromOrigin, 0, brush.getPosition().getX(), brush.getPosition().getY(), 0);
            Matrix.multiplyMM(brushModelMatrix, 0, translateToOrigin, 0, brushModelMatrix, 0);
            Matrix.multiplyMM(brushModelMatrix, 0, translateToBrushTip, 0, brushModelMatrix, 0);
            Matrix.multiplyMM(brushModelMatrix, 0, translateToImprintCenter, 0, brushModelMatrix, 0);
            Matrix.multiplyMM(brushModelMatrix, 0, verticalRotationMatrix, 0, brushModelMatrix, 0);
            Matrix.multiplyMM(brushModelMatrix, 0, translateFromOrigin, 0, brushModelMatrix, 0);

            // Add model matrix to camera matrices
            Matrix.multiplyMM(brushMVMatrix, 0, viewMatrix, 0, brushModelMatrix, 0);
            Matrix.multiplyMM(brushMVPMatrix, 0, projectionMatrix, 0, brushMVMatrix, 0);

            brush.draw(brushMVPMatrix, color);

            renderProfiler.add((System.nanoTime() - renderStartTime) / 1000000f);
        }


        long compositingStartTime = System.nanoTime();

        // Bind default buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);

        // Disable blending and depth test
        GLES30.glDisable(GLES30.GL_BLEND);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

        // Clear color and depth
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Draw render texture to default buffer
        backBufferSquare.draw(mvpMatrix, backBufferManager.getTextureBuffer());

        compositeProfiler.add((System.nanoTime() - compositingStartTime) / 1000000f);

        /** Draw Brush Head **/
        if(settingsData.isShowBrushView()
                && touchDataManager.hasTouchData() && !touchDataManager.hasTouchEnded()) {
            GLES30.glLineWidth(Utils.convertPixelsToDp(20f));
            brush.draw(brushMVPMatrix, Utils.BROWN_COLOR);
        }

        /** Draw Brush View **/
        if(settingsData.isShowBrushView() && !touchDataManager.hasTouchEnded()) {

            // Draw brush view line
            Matrix.setLookAtM(brushViewMatrix, 0,
                    0 - screenRatio + BRUSH_VIEW_PADDING_HORIZONTAL, CAMERA_DISTANCE + 1.0f, 0f,
                    0 - screenRatio + BRUSH_VIEW_PADDING_HORIZONTAL, 0f, 0f,
                    0f, 0.0f, 1.0f);

            Matrix.multiplyMM(brushMVMatrix, 0, brushViewMatrix, 0, brushModelOffsetMatrix, 0);
            Matrix.multiplyMM(brushMVPMatrix, 0, brushProjectionMatrix, 0, brushMVMatrix, 0);

            GLES30.glLineWidth(Utils.convertPixelsToDp(12f));
            line.draw(brushMVPMatrix, Utils.BROWN_COLOR);

            // Draw brush side view brush
            Matrix.setLookAtM(brushViewMatrix, 0,
                    (brush.getPosition().getX() * BRUSH_VIEW_SCALE) - screenRatio + BRUSH_VIEW_PADDING_HORIZONTAL, CAMERA_DISTANCE + 1.0f, 0f,
                    (brush.getPosition().getX() * BRUSH_VIEW_SCALE) - screenRatio + BRUSH_VIEW_PADDING_HORIZONTAL, 0f, 0f,
                    0f, 0.0f, 1.0f);

            Matrix.multiplyMM(brushModelMatrix, 0, brushModelOffsetMatrix, 0, brushModelMatrix, 0);
            Matrix.multiplyMM(brushMVMatrix, 0, brushViewMatrix, 0, brushModelMatrix, 0);
            Matrix.multiplyMM(brushMVPMatrix, 0, brushProjectionMatrix, 0, brushMVMatrix, 0);

            GLES30.glLineWidth(Brush.BRUSH_VIEW_BRISTLE_THICKNESS);
            brush.draw(brushMVPMatrix, Utils.BROWN_COLOR);

        }

        // We are done rendering TouchData, now we clear them
        touchDataManager.clear();

        System.out.println(brush.getPosition().getZ() / Bristle.BASE_LENGTH);


        /*
        System.out.println(String.format("Physics: %.2f, rendering: %.2f, compositing: %.2f, total: %.2f, count %d",
                physicsProfiler.getAverage(), renderProfiler.getAverage(), compositeProfiler.getAverage(),
                physicsProfiler.getAverage() + renderProfiler.getAverage() + compositeProfiler.getAverage(),
                physicsProfiler.getCount()));
        */
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


    /** Takes a list of TouchData and adds it to the container */
    public void addTouchData(ArrayList<TouchData> touchDataList) {
        for(TouchData touchData : touchDataList) {
            touchDataManager.addInterpolated(touchData);
        }
    }

    /**
     * Renders the current back buffer to the next back buffer,
     * and then iterates to the next back buffer.
     */
    public void touchHasStarted() {

        // Bind next back buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, backBufferManager.getNextFrameBuffer());
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, backBufferManager.getNextDepthBuffer());

        // Disable blending and depth test
        GLES30.glDisable(GLES30.GL_BLEND);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

        // Clear color and depth
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Draw current render texture to next back buffer
        backBufferSquare.draw(mvpMatrix, backBufferManager.getTextureBuffer());
        backBufferManager.setNextBuffer();
    }

    /** Indicates that the touch data has ended, clears the array and adds the last TouchData */
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
        brush.resetPosition();
    }

    /**
     * If there are previous buffers, rewind to the previous buffer and draw it to the main buffer.
     */
    public void undo() {

        if(backBufferManager.hasPreviousBuffers()) {
            backBufferManager.rewindBuffer();

            // Bind default buffer
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);

            // Disable blending and depth test
            GLES30.glDisable(GLES30.GL_BLEND);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);

            // Clear color and depth
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

            // Draw current render texture to main buffer
            backBufferSquare.draw(mvpMatrix, backBufferManager.getTextureBuffer());
        }
    }

    /**
     * Clears the screen, by deleting all it's content.
     */
    public void clearScreen() {
        touchHasEnded();
        backBufferManager.resetBuffers();
    }

    /** Translates a viewport vector to world vector */
    public Vector2 viewportToWorld(Vector2 vec) {
        float worldX = -(2 * (vec.getX() / width) - 1) * screenRatio;
        float worldY = -(2 * (vec.getY() / height) - 1);

        return new Vector2(worldX, worldY);
    }

    /** Saves the image on the current screen */
    public void saveImage() {
        int[] pixelBuffer = getPixelBufferFromScreen();
        Utils.convertRGBtoBGR(pixelBuffer);
        Bitmap b = createBitmapFromPixelBuffer(pixelBuffer);
        ImageSaver.saveImageToStorage(b, context);
    }

    /** Creates a bitmap from the current screen */
    private Bitmap createBitmapFromPixelBuffer(int[] pixelBuffer) {

        // Create bitmap of the default buffer
        Bitmap brushBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        brushBitmap.setPixels(pixelBuffer, (width * height) - width, -width, 0, 0, width, height);

        // Create bitmap of the background buffer
        Bitmap backgroundTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.paperbright);
        Bitmap backgroundTextureScaled = Bitmap.createScaledBitmap(backgroundTexture, width, height, true);

        // Create a canvas for compositing the two images
        Canvas canvasImage = new Canvas(backgroundTextureScaled);
        canvasImage.drawBitmap(brushBitmap, 0f, 0f, null);

        return backgroundTextureScaled;
    }

    /** Returns an int array with pixels from the current screen */
    public int[] getPixelBufferFromScreen() {

        // Bind default buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);

        ByteBuffer bb = ByteBuffer.allocateDirect(width * height * 4);
        bb.order(ByteOrder.nativeOrder());

        // Read the pixels from the default buffer
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, bb);

        int pixelBuffer[] = new int[width * height];
        bb.asIntBuffer().get(pixelBuffer);

        return pixelBuffer;
    }

    /** Renders an int buffer to screen
     * @param pixelBuffer*/
    public void renderPixelBuffer(int[] pixelBuffer) {
        // Bind default buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, backBufferManager.getFrameBuffer());
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, backBufferManager.getDepthBuffer());

        // Generate and load textures
        GLES30.glGenTextures(1, savedTextureArray, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, savedTextureArray[0]);

        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, IntBuffer.wrap(pixelBuffer));

        backBufferSquare.draw(mvpMatrix, savedTextureArray[0]);
    }

    /** Whenever new settings changes are made, this methods reconfigures the affected objects */
    private void reconfigureSettingsChanges() {
        settingsManager.setChangesRead();
        settingsData = settingsManager.getSettingsData();
        brush = new Brush(settingsData);
        physicsCompute.destroy();
        physicsCompute = new PhysicsCompute(context, brush);
        color = settingsData.getColorWrapper().toFloatArray();

        physicsProfiler.reset();
        renderProfiler.reset();
        compositeProfiler.reset();
    }

    public void onPause() {
        if(settingsManager != null && touchDataManager != null) {

            // Save settings
            SharedPreferences.Editor editor = settingsManager.getSharedPreferences().edit();
            editor.putInt("numTouches", touchDataManager.getNumTouches());
            editor.putFloat("averageTouchSize", touchDataManager.getAverageTouchSize());
            editor.putFloat("minTouchSize", touchDataManager.getMinTouchSize());
            editor.putFloat("maxTouchSize", touchDataManager.getMaxTouchSize());
            editor.apply();
        }
    }

    public void onResume() {}
}