package co.adrianblan.fastbrush.globject;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import co.adrianblan.fastbrush.utils.GLhelper;

/**
 * Class which manages vertex buffers
 */
public class VertexBufferManager {
    private ArrayList<FloatBuffer> floatBuffers;
    private int currentBufferIndex;
    private int bufferCount;

    public VertexBufferManager(int bufferCount, int bufferSize) {
        this.bufferCount = bufferCount;
        floatBuffers = new ArrayList<>();

        for(int i = 0; i < bufferCount; i++) {
            floatBuffers.add(GLhelper.initFloatBuffer(bufferSize));
        }
    }

    public FloatBuffer getCurrentBuffer() {
        return floatBuffers.get(currentBufferIndex);
    }

    public void setNextBuffer() {
        currentBufferIndex = (currentBufferIndex + 1) % bufferCount;
    }
}
