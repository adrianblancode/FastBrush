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
package co.adrianblan.caligraphy;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

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
    private final float[] mModelMatrix = new float[16];
    private final float[] matrixProduct = new float[16];

    private int mWidth;
    private int mHeight;
    private float mRatio;

    private ArrayList<Point> pointArrayList;
    private ArrayList<Point> unrenderedPointArrayList;

    private boolean shouldFollowPreviousPoint = true;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        pointArrayList = new ArrayList<>();
        unrenderedPointArrayList = new ArrayList<>();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        if(pointArrayList.isEmpty()) {
            // Clear everything if there are no rendered points
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        } else {
            // Otherwise, just clear the color
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        }

        // Draw point
        for (Point t : unrenderedPointArrayList) {
            t.draw(mMVPMatrix);
        }

        pointArrayList.addAll(unrenderedPointArrayList);
        unrenderedPointArrayList.clear();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        // TODO use render to texture?
        EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW),
                EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);

        mWidth = width;
        mHeight = height;
        mRatio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1, 1, 3, 7);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
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
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
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

    /** Takes a Vector2, and interpolates objects based on a distance to the previous object */
    private void addInterpolatedPoints(Vector2 coord) {

        final float MIN_DISTANCE = 0.020f;

        if(!hasPreviousPoint()) {

            addPoint(coord);
            shouldFollowPreviousPoint = true;

        } else {

            Vector2 previousCoord = getPreviousPointCoord();
            float distance = coord.distance(previousCoord);

            // If it's a new line, don't interpolate
            if(!shouldFollowPreviousPoint) {
                shouldFollowPreviousPoint = true;
            } else {

                int interpolations = (int) (distance / MIN_DISTANCE);
                if (interpolations > 0) {

                    // Interpolate so that there are no gaps larger than MIN_DISTANCE
                    for (int i = 0; i < interpolations; i++) {

                        float x = previousCoord.getX() + (coord.getX() - previousCoord.getX()) * (i + 1f) / ((float) interpolations + 1f);
                        float y = previousCoord.getY() + (coord.getY() - previousCoord.getY()) * (i + 1f) / ((float) interpolations + 1f);

                        addPoint(x, y);
                    }
                }
            }

            // Don't add point if not enough distance
            if (distance > MIN_DISTANCE) {
                addPoint(coord);
            }
        }
    }

    /** Translates a viewport vector to world vector */
    public Vector2 viewportToWorld(Vector2 vec) {
        float worldX = -(2 * (vec.getX() / mWidth) - 1) * mRatio;
        float worldY = -(2 * (vec.getY() / mHeight) - 1);

        return new Vector2(worldX, worldY);
    }

    /** Takes a list of coords and adds them to the renderer */
    public void addPoints(ArrayList<Vector2> coordList) {

        for(Vector2 coord : coordList) {
            addInterpolatedPoints(viewportToWorld(coord));
        }
    }

    /** Takes a coord and adds it to the renderer */
    public void addPoint(Vector2 coord) {
        addPoint(coord.getX(), coord.getY());
    }

    /** Takes a coord and adds it to the renderer */
    public void addPoint(float x, float y) {
        Point tri = new Point(x, y);
        unrenderedPointArrayList.add(tri);
    }

    /** Return whether there is a previous point to interpolate from */
    private boolean hasPreviousPoint() {
        return !pointArrayList.isEmpty() && !unrenderedPointArrayList.isEmpty();
    }

    /** Return the previous point to interpolate from */
    private Vector2 getPreviousPointCoord() {

        if(!hasPreviousPoint()) {
            return null;
        }

        // The previous point should be undered, if there are no unrendered points get from rendered
        if (!unrenderedPointArrayList.isEmpty()) {
            return unrenderedPointArrayList.get(unrenderedPointArrayList.size() - 1).getCoord();
        } else {
            return pointArrayList.get(pointArrayList.size() - 1).getCoord();
        }
    }

    /** Clears all the ArrayList of Point of all objects*/
    public void clearPoints() {
        unrenderedPointArrayList.clear();
        pointArrayList.clear();
    }

    public void setTouchInactive() {
        shouldFollowPreviousPoint = false;
    }

}