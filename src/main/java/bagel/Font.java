package bagel;

/**
 * Represents a loaded font that can be used to draw text.
 */
public class Font {
    private final InternalFont font;

    /**
     * Load the font from the given file at the given size.
     *
     * Creating multiple copies of the same font (at the same size) is efficient.
     */
    public Font(String filename, int size) {
        font = TextureManager.getFont(filename, size);
    }

    /**
     * Draw the provided string with its top-left at the given (x, y) location.
     * The text will be left-aligned.
     */
    public void drawString(String string, double x, double y) {
        drawString(string, x, y, new DrawOptions());
    }

    /**
     * Draw the provided string with its top-left at the given (x, y) location with additional options.
     * The text will be left-aligned.
     */
    public void drawString(String string, double x, double y, DrawOptions options) {
        font.drawString(string, x, y, options);
    }

    public double getWidth(String string) {
        return font.getWidth(string);
    }
}
