package co.adrianblan.fastbrush.globject;

import android.opengl.GLES30;

/**
 * Class which manages a number of back buffers.
 */
public class BackBufferManager {

    private int[] frameBufferArray;
    private int[] depthBufferArray;
    private int[] renderTextureArray;

    private int numBuffers;
    private int numPreviousBuffers;
    private int currentBuffer;

    private int width;
    private int height;

    public BackBufferManager(int numBuffers, int width, int height) {

        if(numBuffers < 1) {
            throw new IllegalArgumentException();
        }

        this.numBuffers = numBuffers;
        this.currentBuffer = 0;

        this.width = width;
        this.height = height;

        frameBufferArray = new int[numBuffers];
        depthBufferArray = new int[numBuffers];
        renderTextureArray = new int[numBuffers];

        generateBackBuffers();
    }

    /** Generates the framebuffer and texture necessary to render to a second screen */
    private void generateBackBuffers() {

        // Generate frame buffer and texture
        GLES30.glGenFramebuffers(numBuffers, frameBufferArray, 0);
        GLES30.glGenFramebuffers(numBuffers, depthBufferArray, 0);

        GLES30.glGenTextures(numBuffers, renderTextureArray, 0);

        // Make sure that width and height have been set and are nonzero
        if (width == 0 || height == 0) {
            System.err.println("Height or width can not be zero");
        }

        int[] maxTextureSize = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);

        if (maxTextureSize[0] < width) {
            System.err.println("Texture size not large enough! " + maxTextureSize[0] + " < " + width);
        }

        for(int i = 0; i < numBuffers; i++) {

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, renderTextureArray[i]);

            // Clamp the render texture to the edges
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);

            // Depth buffer
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBufferArray[i]);
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, width, height);

            // Generate texture
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);

            // Bind depth buffer and texture to back buffer
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferArray[i]);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, renderTextureArray[i], 0);
            GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, depthBufferArray[i]);

            // Check status of framebuffer
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferArray[i]);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBufferArray[i]);
            int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);

            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                System.err.println("Framebuffer error: " + status);
            }

            // Clear back buffer
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        }
    }

    public int getFrameBuffer() {
        return frameBufferArray[currentBuffer];
    }

    public int getDepthBuffer() {
        return depthBufferArray[currentBuffer];
    }

    public int getTextureBuffer() {
        return renderTextureArray[currentBuffer];
    }

    public int getNextFrameBuffer() {
        return frameBufferArray[getNextBufferIndex()];
    }

    public int getNextDepthBuffer() {
        return depthBufferArray[getNextBufferIndex()];
    }

    public int getNextTextureBuffer() {
        return renderTextureArray[getNextBufferIndex()];
    }

    private int getNextBufferIndex() {
        return (currentBuffer + 1) % numBuffers;
    }

    public void setNextBuffer() {
        currentBuffer = (currentBuffer + 1) % numBuffers;
        numPreviousBuffers = Math.min(numPreviousBuffers + 1, numBuffers);

        System.err.println("Next set, " + currentBuffer);
    }

    public boolean hasPreviousBuffers() {
        return (numPreviousBuffers > 0);
    }

    public void rewindBuffer() {
        if(hasPreviousBuffers()) {
            currentBuffer = currentBuffer - 1;

            if(currentBuffer < 0) {
                currentBuffer = numBuffers - 1;
            }

            numPreviousBuffers = Math.max(numPreviousBuffers - 1, 0);

            System.err.println("Rewind set, " + currentBuffer);
        }
    }

    public void resetBuffers() {
        numPreviousBuffers = 0;
        currentBuffer = 0;

        System.err.println("Reset set, " + currentBuffer);
    }
}
