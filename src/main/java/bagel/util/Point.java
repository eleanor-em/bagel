package bagel.util;

/**
 * Immutable class that represents a 2D point in space.
 */
public class Point {
    public final double x;
    public final double y;

    /**
     * Create the point (0, 0).
     */
    public Point() {
        this(0, 0);
    }

    /**
     * Create the point (x, y).
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Converts this point to a {@link Vector2} with the same x and y values.
     */
    public Vector2 asVector() {
        return new Vector2(this.x, this.y);
    }

    /**
     * Performs <b>exact</b> equality checking.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Point) {
            Point rhs = (Point) other;
            return x == rhs.x && y == rhs.y;
        } else {
            return false;
        }
    }

    public double distanceTo(Point b) {
        return asVector().sub(b.asVector()).length();
    }

    @Override
    public String toString() {
        return String.format("Point: (%.2f, %.2f)", x, y);
    }
}
