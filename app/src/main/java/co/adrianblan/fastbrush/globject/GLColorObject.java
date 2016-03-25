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
            "  vec4 texColor = texture2D(u_Texture, texPosition);" +
            "  gl_FragColor = texColor + (vColor - texColor) * vColor.w;" +
            "  gl_FragColor.w = texColor.w + (1.0 - texColor.w) * vColor.w;" +
            "}";
}
