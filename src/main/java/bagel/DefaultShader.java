package bagel;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Handles the GLSL vertex and fragment shaders.
 */
class DefaultShader extends Shader {
    private final int vaoId;
    private final int vboId;

    private final int positionUniformLocation;
    private final int scaleUniformLocation;
    private final int rotationUniformLocation;
    private final int blendUniformLocation;
    private final int xOffUniformLocation;
    private final int yOffUniformLocation;
    private final int xMaxUniformLocation;
    private final int yMaxUniformLocation;

    private static DefaultShader INSTANCE;
    static DefaultShader get() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultShader();
        }
        return INSTANCE;
    }

    /**
     * Initialise the shader.
     */
    private DefaultShader() {
        // Compile the shader and create buffer objects
        int shaderProgram = Shader.generateShaderProgram("default.vert", "default.frag");
        vaoId = Shader.generateVaoId();
        vboId = Shader.generateVboId();

        // Load uniform locations to send data to the shader
        int widthUniformLocation = GL20.glGetUniformLocation(shaderProgram, "width");
        int heightUniformLocation = GL20.glGetUniformLocation(shaderProgram, "height");
        positionUniformLocation = GL20.glGetUniformLocation(shaderProgram, "translation");
        scaleUniformLocation = GL20.glGetUniformLocation(shaderProgram, "scale");
        rotationUniformLocation = GL20.glGetUniformLocation(shaderProgram, "rotation");
        blendUniformLocation = GL20.glGetUniformLocation(shaderProgram, "blend");
        xOffUniformLocation = GL20.glGetUniformLocation(shaderProgram, "xOffset");
        yOffUniformLocation = GL20.glGetUniformLocation(shaderProgram, "yOffset");
        xMaxUniformLocation = GL20.glGetUniformLocation(shaderProgram, "xMax");
        yMaxUniformLocation = GL20.glGetUniformLocation(shaderProgram, "yMax");

        // Set up the shader
        GL20.glUseProgram(shaderProgram);
        GL20.glUniform1f(widthUniformLocation, Window.getWidth());
        GL20.glUniform1f(heightUniformLocation, Window.getHeight());
    }

    private void setUniforms(RenderInfo info) {
        // Assign values to the shader
        GL20.glUniform3f(positionUniformLocation, info.x, info.y, 0);
        GL20.glUniform3f(scaleUniformLocation, (float) info.tex.w * info.xScale, (float) info.tex.h * info.yScale, 1);
        GL20.glUniform1f(rotationUniformLocation, info.rotation);
        GL20.glUniform4f(blendUniformLocation, info.rBlend, info.gBlend, info.bBlend, info.aBlend);
        GL20.glUniform1f(xOffUniformLocation, info.xOffset);
        GL20.glUniform1f(yOffUniformLocation, info.yOffset);
        GL20.glUniform1f(xMaxUniformLocation, info.xMax);
        GL20.glUniform1f(yMaxUniformLocation, info.yMax);
//        System.out.println("Drawing section (" + info.xOffset + ", " + info.yOffset + ") to (" + info.xMax + ", " + info.yMax + ") at (" + info.x + ", " + info.y + ")");
    }

    @Override
    protected void bindInternal() {
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
    }

    /**
     * Perform a render operation.
     */
    @Override
    void render(RenderInfo info) {
        setUniforms(info);
        drawElements();
    }
}
