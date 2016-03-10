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

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.util.ArrayList;

import co.adrianblan.fastbrush.data.TouchData;
import co.adrianblan.fastbrush.vector.Vector2;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private VelocityTracker mVelocityTracker;
    int[] savedPixelBuffer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 3.0 context.
        setEGLContextClientVersion(3);

        // Sets a paper background to the scene
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setBackgroundResource(R.drawable.paperbright);
        setZOrderOnTop(true);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setPreserveEGLContextOnPause(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }

                // No break is intentional

            case MotionEvent.ACTION_MOVE:

                mVelocityTracker.addMovement(e);

                // Compute velocity in pixels per second
                mVelocityTracker.computeCurrentVelocity(1);

                final ArrayList<TouchData> touchDataList = new ArrayList<>(e.getHistorySize() + 1);
                Vector2 viewportPosition;

                Vector2 viewportVelocity =
                        new Vector2(VelocityTrackerCompat.getXVelocity(mVelocityTracker, e.getActionIndex()),
                        VelocityTrackerCompat.getYVelocity(mVelocityTracker, e.getActionIndex()));

                // Add previous touch coordinates
                for(int i = 0; i < e.getHistorySize(); i++) {
                    viewportPosition = new Vector2(e.getHistoricalX(i), e.getHistoricalY(i));

                            touchDataList.add(new TouchData(mRenderer.viewportToWorld(viewportPosition),
                                    viewportVelocity,
                                    e.getHistoricalSize(i), e.getHistoricalPressure(i)));
                }

                // Add current touch coordinates
                viewportPosition = new Vector2(e.getX(), e.getY());
                touchDataList.add(new TouchData(mRenderer.viewportToWorld(viewportPosition), viewportVelocity, e.getSize(), e.getPressure()));

                // Ensure we call switchMode() on the OpenGL thread.
                // queueEvent() is a method of GLSurfaceView that will do this for us.
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.addTouchData(touchDataList);
                    }
                });

                requestRender();
                break;

            case MotionEvent.ACTION_UP:

                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.touchHasEnded();
                    }
                });
                requestRender();
                break;
        }
        return true;
    }

    public void clearScreen() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.clearScreen();
            }
        });
        requestRender();
    }

    public void saveImage() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.saveImage();
            }
        });
    }

    @Override
    public void onPause(){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                savedPixelBuffer = mRenderer.getPixelBufferFromScreen();
            }
        });

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(savedPixelBuffer != null && savedPixelBuffer.length > 0) {

            System.err.println("PBL: " + savedPixelBuffer.length);

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRenderer.renderPixelBuffer(savedPixelBuffer);
                    requestRender();
                }
            });
        }
    }
}
