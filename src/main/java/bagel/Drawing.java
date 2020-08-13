package bagel;

import bagel.util.Colour;
import bagel.util.Point;

/**
 * Contains utility methods for drawing various shapes.
 */
public class Drawing {
    /**
     * Draw a rectangle with its top-left at (x, y) with the provided width, height, and colour.
     */
    public static void drawRectangle(double x, double y, double width, double height, Colour colour) {
        drawRectangle(new Point(x, y), width, height, colour);
    }

    /**
     * Draw a rectangle using a Point to specify the top-left.
     */
    public static void drawRectangle(Point topLeft, double width, double height, Colour colour) {
        DrawOptions opts = new DrawOptions()
                .setBlendColour(colour)
                .setScale(width, height);
        Window.get().submitRenderJob(opts.toRenderInfo(Texture.singlePixel(), (float) (topLeft.x + width / 2), (float) (topLeft.y + height / 2)));
    }

    /**
     * Draw a line of the given thickness and colour from point a to point b.
     */
    public static void drawLine(Point a, Point b, double thickness, Colour colour) {
        double w = a.distanceTo(b);
        double theta = Math.atan2(b.y - a.y, b.x - a.x);

        DrawOptions opts = new DrawOptions()
                .setBlendColour(colour)
                .setScale(w, thickness)
                .setRotation(theta);
        Window.get().submitRenderJob(opts.toRenderInfo(Texture.singlePixel(), (float) ((b.x + a.x) / 2), (float) ((b.y + a.y) / 2)));
    }

    /**
     * Draw a circle of the given radius and colour, centred at the provided point.
     *
     * (Warning: this is not very efficient.)
     */
    public static void drawCircle(Point centre, double radius, Colour colour) {
        drawCircle(centre.x, centre.y, radius, colour);
    }

    /**
     * Draw a circle of the given radius and colour, centred at the point (x, y).
     *
     * (Warning: this is not very efficient.)
     */
    public static void drawCircle(double x, double y, double radius, Colour colour) {
        // see https://stackoverflow.com/questions/49806014/circle-rasterization-algorithm-center-between-pixels
        // inefficient but will do the job
        double stepSize = 1;
        for (double dy = 0; dy < radius; dy += stepSize) {
            int mdx = (int) Math.sqrt(radius * radius - dy * dy);
            drawLine(new Point(x - mdx - 1, y + dy), new Point(x + mdx, y + dy), 1, colour);
            drawLine(new Point(x - mdx - 1, y - dy - 1), new Point(x + mdx, y - dy - 1), 1, colour);
        }
    }
}
