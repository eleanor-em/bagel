package bagel;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

/**
 * Immutable class that represents a loaded texture.
 */
class Texture {
    /**
     * The width of the texture.
     */
    final int w;
    /**
     * The height of the texture.
     */
    final int h;
    private final int texId;
    private final String filename;

    private static int boundTexture;

    private static void bindTexture(int texId) {
        if (boundTexture != texId) {
            glBindTexture(GL_TEXTURE_2D, texId);
            boundTexture = texId;
        }
    }

    /**
     * Create the texture from a byte buffer; the filename is only used for `toString`.
     */
    Texture(String filename, int w, int h, int components, ByteBuffer buffer) {
        this.w = w;
        this.h = h;
        this.filename = filename;

        // GL stuff: see https://github.com/LWJGL/lwjgl3/blob/18975883e844d9dc53874836ec45257da13085d9/modules/samples/src/test/java/org/lwjgl/demo/stb/Image.java#L244
        this.texId = glGenTextures();
        bindTexture(texId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int format = GL_RGBA;

        if (components == 3) {
            // Sorry. I know this makes no sense. See above link.
            if ((w & 3) != 0) {
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (w & 1));
            }
            format = GL_RGB;
        }

        glTexImage2D(GL_TEXTURE_2D, 0, format, w, h, 0, format, GL_UNSIGNED_BYTE, buffer);
    }

    Texture(int w, int h, ByteBuffer bitmap) {
        this.w = w;
        this.h = h;
        this.filename = "";
        this.texId = glGenTextures();
        bindTexture(texId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, bitmap);
    }

    private static OnceAssignable<Texture> singlePixelTex = new OnceAssignable<>();

    static Texture singlePixel() {
        singlePixelTex.setIfEmpty(() -> {
            ByteBuffer whitePixel = BufferUtils.createByteBuffer(4);
            whitePixel.put((byte) 255);
            whitePixel.put((byte) 255);
            whitePixel.put((byte) 255);
            whitePixel.put((byte) 255);
            whitePixel.rewind();
            return new Texture(1, 1, whitePixel);
        });
        return singlePixelTex.get();
    }

    /**
     * Binds this texture to the graphics card, if it's not already bound.
     */
    void bind() {
        bindTexture(texId);
    }

    void destroy() {
        glDeleteTextures(texId);
    }

    @Override
    public String toString() {
        return "Texture: " + filename + " (tex id " + texId + ")";
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof Texture && ((Texture) rhs).texId == texId;
    }

    @Override
    public int hashCode() {
        return texId;
    }
}
