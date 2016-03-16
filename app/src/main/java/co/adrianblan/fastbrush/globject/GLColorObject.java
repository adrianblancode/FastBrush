package co.adrianblan.fastbrush.globject;

/**
 * Class which provides custom color blending shaders.
 */
public class GLColorObject {

    protected static final String COLOR_FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "uniform vec2 resolution;" +
            "uniform sampler2D u_Texture;" +
            "void main() {" +
            "  vec2 texPosition = (gl_FragCoord.xy / resolution.xy);" +
            "  gl_FragColor = texture2D(u_Texture, texPosition);" +
            "  gl_FragColor = gl_FragColor + (vColor - gl_FragColor) * vColor.w;" +
            "}";
}
