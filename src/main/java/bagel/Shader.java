package bagel;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

/**
 * A shader that can be used by the rendering pipeline.
 */
public abstract class Shader {
    private static final float[] QUAD_VERTICES = {
             0.5f,   0.5f, 0f, 1f, 1f,
             0.5f,  -0.5f, 0f, 1f, 0f,
            -0.5f,  -0.5f, 0f, 0f, 0f,
            -0.5f,   0.5f, 0f, 0f, 1f,
    };
    private static final byte[] QUAD_INDICES = {
            0, 1, 2,
            2, 3, 0
    };

    private static Shader current;

    public void bind() {
        if (current != this) {
            current = this;
            bindInternal();
        }
    }

    protected abstract void bindInternal();
    abstract void render(RenderInfo info);

    protected static void drawElements() {
        GL11.glDrawElements(GL11.GL_TRIANGLES, QUAD_INDICES.length, GL_UNSIGNED_BYTE, 0);
    }

    protected static int generateShaderProgram(String vertexShaderName, String fragmentShaderName) {
        // Compile the vertex shader
        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vs, IOUtils.readResource(vertexShaderName));
        GL20.glCompileShader(vs);
        if (GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS) == GL_FALSE){
            throw new BagelError("Vertex shader failed to compile: " + GL20.glGetShaderInfoLog(vs, 500));
        }

        // Compile the fragment shader
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fs, IOUtils.readResource(fragmentShaderName));
        GL20.glCompileShader(fs);
        if (GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS) == GL_FALSE){
            throw new BagelError("Fragment shader failed to compile: " + GL20.glGetShaderInfoLog(fs, 500));
        }

        // Link the program
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL_FALSE) {
            throw new BagelError("Shader program failed to link: " +
                    GL20.glGetProgramInfoLog(program, GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH)));
        }

        // We don't need to keep the shaders loaded now
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);

        return program;
    }

    protected static int generateVaoId() {
        // Create the vertex array
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(QUAD_VERTICES.length);
        vertexBuffer.put(QUAD_VERTICES);
        vertexBuffer.flip();

        int vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        int tempVboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tempVboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return vaoId;
    }

    protected static int generateVboId() {
        // Create the vertex buffer (for indices)
        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(QUAD_INDICES.length);
        indicesBuffer.put(QUAD_INDICES);
        indicesBuffer.flip();

        int vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

        return vboId;
    }
}
