package co.adrianblan.calligraphy;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/** Class which provides helper functions for OpenGL objects */
public abstract class GLobject {

    /** Initializes a buffer with the size of content, and places content inside */
    protected FloatBuffer initBuffer(float [] content) {
        ByteBuffer bb = ByteBuffer.allocateDirect(content.length * 4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(content);
        buffer.position(0);

        return buffer;
    }

    /** Initializes a buffer with the size of content, and places content inside */
    protected ShortBuffer initBuffer(short [] content) {
        ByteBuffer bb = ByteBuffer.allocateDirect(content.length * 2);
        bb.order(ByteOrder.nativeOrder());

        ShortBuffer buffer = bb.asShortBuffer();
        buffer.put(content);
        buffer.position(0);

        return buffer;
    }

    /**
     * Takes an initialized program id, attaches and links the shaders to it.
     *
     * @param program the program to link and attach to
     * @param vertexShaderCode the vertex shader code in a String
     * @param fragmentShaderCode the fragment shader code in a String
     */
    protected void loadShaders(int program, String vertexShaderCode, String fragmentShaderCode) {
        // prepare shaders and OpenGL program
        int vertexShaderId = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderId = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        GLES20.glAttachShader(program, vertexShaderId);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShaderId); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // create OpenGL program executables
    }
}
