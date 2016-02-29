package co.adrianblan.calligraphy.globject;

/**
 * Basic OpenGL object.
 */
public class GLobject {

    protected static final String DEFAULT_VERTEX_SHADER_CODE =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    protected static final String DEFAULT_FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    protected static final int DEFAULT_COORDS_PER_VERTEX = 3;
    protected static final int DEFAULT_VERTEX_STRIDE = 3 * 4; // 4 bytes per vertex
}
