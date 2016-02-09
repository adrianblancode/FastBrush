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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setBackgroundResource(R.drawable.paperbright);
        setZOrderOnTop(true);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        switch (e.getAction()) {

            case MotionEvent.ACTION_MOVE:

                final ArrayList<Vector2> coordList = new ArrayList<>(e.getHistorySize() + 1);
                final ArrayList<Float> touchSizeList = new ArrayList<>(e.getHistorySize() + 1);
                final ArrayList<Float> touchPressureList = new ArrayList<>(e.getHistorySize() + 1);

                // Add previous touch coordinates
                for(int i = 0; i < e.getHistorySize(); i++) {
                    coordList.add(new Vector2(e.getHistoricalX(i), e.getHistoricalY(i)));
                    touchSizeList.add(e.getHistoricalSize(i));
                    touchPressureList.add(e.getHistoricalPressure(i));
                }

                // Add current touch coordinates
                coordList.add(new Vector2(e.getX(), e.getY()));
                touchSizeList.add(e.getSize());
                touchPressureList.add(e.getPressure());

                // Ensure we call switchMode() on the OpenGL thread.
                // queueEvent() is a method of GLSurfaceView that will do this for us.
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.addPoints(coordList, touchSizeList, touchPressureList);

                    }
                });

                requestRender();
                break;

            case MotionEvent.ACTION_UP:

                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.clearPoints();

                    }
                });

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
}
