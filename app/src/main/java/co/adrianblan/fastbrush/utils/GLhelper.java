package co.adrianblan.fastbrush.utils;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/** Class which provides helper functions for OpenGL objects */
public abstract class GLhelper {

    private static final String TAG = "GLhelper";

    /** Initializes a buffer with the size of content */
    public static FloatBuffer initFloatBuffer(int size) {
        ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);
        bb.order(ByteOrder.nativeOrder());

        return bb.asFloatBuffer();
    }

    /** Initializes a buffer with the size of content, and places content inside */
    public static FloatBuffer initFloatBuffer(float[] content) {
        FloatBuffer buffer = initFloatBuffer(content.length);
        buffer.put(content);
        buffer.position(0);

        return buffer;
    }

    /** Initializes a buffer with the size of content, and places content inside */
    public static ShortBuffer initShortBuffer(short[] content) {
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
    public static void loadShaders(int program, String vertexShaderCode, String fragmentShaderCode) {
        // prepare shaders and OpenGL program
        int vertexShaderId = loadShader(
                GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderId = loadShader(
                GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);

        GLES30.glAttachShader(program, vertexShaderId);   // add the vertex shader to program
        GLES30.glAttachShader(program, fragmentShaderId); // add the fragment shader to program
        GLES30.glLinkProgram(program);                  // create OpenGL program executables
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

        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        int shaderId = GLES30.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shaderId, shaderCode);
        GLES30.glCompileShader(shaderId);

        return shaderId;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
