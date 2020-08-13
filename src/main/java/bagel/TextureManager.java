package bagel;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches loaded textures for convenience's sake.
 */
class TextureManager {
    private static final Map<String, Texture> loadedTextures = new HashMap<>();
    private static final Map<FontClassification, InternalFont> loadedFonts = new HashMap<>();

    /**
     * Seal the class.
     */
    private TextureManager() {}

    /**
     * Free all loaded textures.
     */
    static void destroy() {
        for (Texture tex : loadedTextures.values()) {
            tex.destroy();
        }
    }

    /**
     * Looks up the provided texture, and loads it if it's not already present.
     */
    static Texture getTexture(String filename) {
        if (loadedTextures.containsKey(filename)) {
            return loadedTextures.get(filename);
        } else if (!new File(filename).exists()) {
            throw new BagelError("Error loading image: File " + filename + " not found (full path: " + Paths.get(filename).toAbsolutePath() + ")");
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer components = stack.mallocInt(1);

                ByteBuffer fname = IOUtils.stringToByteBuffer(stack, filename + '\0');
                // Load the image
                ByteBuffer image = STBImage.stbi_load(fname, w, h, components, 0);
                if (image == null) {
                    throw new BagelError("Error loading image: " + filename + ": " + STBImage.stbi_failure_reason());
                }

                // Create the texture object
                Texture tex = new Texture(filename, w.get(0), h.get(0), components.get(0), image);
                loadedTextures.put(filename, tex);
                return tex;
            }
        }
    }

    static InternalFont getFont(String filename, int size) {
        FontClassification key = new FontClassification(filename, size);
        if (loadedFonts.containsKey(key)) {
            return loadedFonts.get(key);
        } else {
            InternalFont font = new InternalFont(filename, size);
            loadedFonts.put(key, font);
            return font;
        }
    }
}
