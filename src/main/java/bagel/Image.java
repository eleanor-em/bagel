package bagel;

import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Class to load and draw an image.
 *
 * @author Eleanor McMurtry
 */
public class Image {
    private final Texture tex;

    /**
     * Load an image from a file.
     * @param filename the filename to load
     */
    public Image(String filename) {
        // Check that a window exists first
        Window.get();
        tex = TextureManager.getTexture(filename);
    }

    /**
     * Draw the image with its top-left at (x, y).
     */
    public void drawFromTopLeft(double x, double y) {
        drawFromTopLeft(x, y, new DrawOptions());
    }

    /**
     * Draw the image with its top-left at (x, y) with extra options.
     */
    public void drawFromTopLeft(double x, double y, DrawOptions options) {
        draw(x + tex.w / 2.0, y + tex.h / 2.0, options);
    }

    /**
     * Draw the image with its centre at (x, y).
     */
    public void draw(double x, double y) {
        draw(x, y, new DrawOptions());
    }

    /**
     * Draw the image with advanced options as described by an {@link DrawOptions} object.
     */
    public void draw(double x, double y, DrawOptions options) {
        Window.get().submitRenderJob(options.toRenderInfo(tex, (float) x, (float) y));
    }

    /**
     * Returns the width of the image in pixels.
     */
    public double getWidth() {
        return tex.w;
    }

    /**
     * Returns the height of the image in pixels.
     */
    public double getHeight() {
        return tex.h;
    }

    /**
     * Creates a {@link Rectangle} as defined by this image, located with its top-left at (0, 0).
     */
    public Rectangle getBoundingBox() {
        return new Rectangle(0, 0, tex.w, tex.h);
    }

    /**
     * Helper method to create a {@link Rectangle} around this image, centred at a {@link Point}.
     */
    public Rectangle getBoundingBoxAt(Point point) {
        return new Rectangle(point.x - tex.w / 2.0, point.y - tex.h / 2.0, tex.w, tex.h);
    }
}
