package bagel;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

class FontClassification {
    final String filename;
    final int size;

    FontClassification(String filename, int size) {
        this.size = size;
        this.filename = filename;
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof FontClassification && ((FontClassification) rhs).size == size
                                                 && ((FontClassification) rhs).filename.equals(filename);
    }

    @Override
    public int hashCode() {
        return (filename + size).hashCode();
    }
}

class InternalFont {
    final FontClassification key;

    private final STBTTFontinfo info;
    private final Texture tex;
    private final STBTTBakedChar.Buffer chars;
    private final int size;
    private static final int NUM_CHARS = 96;
    private static final int FIRST_CHAR = Byte.MAX_VALUE - NUM_CHARS + 1;

    private static int nextPowerOfTwo(double x) {
        final double log2 = Math.log(2);
        return 1 << ((int) (Math.log(x) / log2) + 1);
    }

    InternalFont(String fontFile, int size) {
        try {
            this.key = new FontClassification(fontFile, size);
            this.size = size;

            ByteBuffer fontData = IOUtils.fileToByteBuffer(fontFile);

            this.info = STBTTFontinfo.create();
            if (!STBTruetype.stbtt_InitFont(info, fontData)) {
                throw new BagelError("Failed to load font `" + fontFile + "`");
            }

            int width = nextPowerOfTwo(size * NUM_CHARS);
            int height = nextPowerOfTwo(size + 1);
            ByteBuffer bitmap = BufferUtils.createByteBuffer(width * height);
            chars = STBTTBakedChar.malloc(NUM_CHARS);

            int result;
            if ((result = STBTruetype.stbtt_BakeFontBitmap(fontData, size, bitmap, width, height, FIRST_CHAR, chars)) <= 0) {
                System.out.println("[Bagel] warning: font data not fully loaded: returned " + result);
            }

            // Convert single-channel to 3-channel plus alpha
            ByteBuffer rgbaBitmap = BufferUtils.createByteBuffer(width * height * 4);
            while (bitmap.hasRemaining()) {
                byte next = bitmap.get();
                // Threshold the colour, and use the actual value for the alpha
                // (it looks better this way)
                byte blackOrWhite = (byte) (next != Byte.MIN_VALUE ? 255 : Byte.MIN_VALUE);
                rgbaBitmap.put(blackOrWhite);
                rgbaBitmap.put(blackOrWhite);
                rgbaBitmap.put(blackOrWhite);
                rgbaBitmap.put(next);
            }
            rgbaBitmap.rewind();

            tex = new Texture(width, height, rgbaBitmap);
        } catch (IOException e) {
            throw new BagelError("Error loading font file `" + fontFile + "`: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // This is very inefficient but whatever.
    double getWidth(String string) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fx = stack.floats(0);
            FloatBuffer fy = stack.floats(0);
            STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);

            float maxWidth = 0;
            float width = 0;
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if (c == '\n') {
                    // Find the coordinates for the new line
                    maxWidth = Math.max(width, maxWidth);
                    width = 0;
                    float oldFy = fy.get();
                    fx = stack.floats(0);
                    fy = stack.floats(oldFy + size);
                } else if (c < FIRST_CHAR || c >= FIRST_CHAR + NUM_CHARS) {
                    System.out.println("[Bagel] warning: asked to print character " + c + " that is not in the font");
                } else {
                    STBTruetype.stbtt_GetBakedQuad(chars, tex.w, tex.h, c - FIRST_CHAR, fx, fy, quad, true);
                    width = quad.x1();
                }
            }
            maxWidth = Math.max(width, maxWidth);
            return maxWidth;
        }
    }

    void drawString(String string, double x, double y, DrawOptions options) {
        x += tex.w / 2;
        y += tex.h / 2;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fx = stack.floats((float) x);
            FloatBuffer fy = stack.floats((float) y);

            STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if (c == '\n') {
                    // Find the coordinates for the new line
                    float oldFy = fy.get();
                    fx = stack.floats((float) x);
                    fy = stack.floats(oldFy + size);
                } else if (c < FIRST_CHAR || c >= FIRST_CHAR + NUM_CHARS) {
                    System.out.println("[Bagel] warning: asked to print character " + c + " that is not in the font");
                } else {
                    STBTruetype.stbtt_GetBakedQuad(chars, tex.w, tex.h, c - FIRST_CHAR, fx, fy, quad, true);
                    options.setSection(quad.s0() * tex.w, quad.t0() * tex.h, (quad.s1() - quad.s0()) * tex.w, (quad.t1() - quad.t0()) * tex.h);
                    Window.get().submitRenderJob(options.toRenderInfo(tex, quad.x0(), quad.y0()));
                }
            }
        }
    }
}
