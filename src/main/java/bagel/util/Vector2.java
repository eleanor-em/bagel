package bagel.util;

/**
 * Immutable class representing a two-dimensional real vector.
 */
public class Vector2 {
    private static final double EPSILON = 1e-6;

    public final double x;
    public final double y;

    /**
     * A unit vector pointing to the right.
     */
    public static final Vector2 right = new Vector2(1, 0);

    /**
     * A unit vector pointing to the left.
     */
    public static final Vector2 left = new Vector2(-1, 0);

    /**
     * A unit vector pointing upwards.
     */
    public static final Vector2 up = new Vector2(0, -1);

    /**
     * A unit vector pointing downwards.
     */
    public static final Vector2 down = new Vector2(0, 1);

    /**
     * Construct the zero vector.
     */
    public Vector2() {
        this(0, 0);
    }

    /**
     * Create a vector with a given x and y coordinate.
     */
    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the result of adding rhs to this vector.
     */
    public Vector2 add(Vector2 rhs) {
        return new Vector2(x + rhs.x, y + rhs.y);
    }

    /**
     * Returns the result of subtracting rhs from this vector.
     */
    public Vector2 sub(Vector2 rhs) {
        return new Vector2(x - rhs.x, y - rhs.y);
    }

    /**
     * Returns the result of multiplying this vector by scalar.
     */
    public Vector2 mul(double scalar) {
        return new Vector2(scalar * x, scalar * y);
    }

    /**
     * Returns the result of dividing this vector by scalar.
     */
    public Vector2 div(double scalar) {
        return new Vector2(x / scalar, y / scalar);
    }

    /**
     * Returns the dot (scalar) product of this vector and rhs.
     *
     * Calculated by `this.x * rhs.x + this.y * rhs.y`.
     */
    public double dot(Vector2 rhs) {
        return x * rhs.x + y * rhs.y;
    }

    /**
     * Returns the length of this vector.
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Returns the square of the length of this vector.
     */
    public double lengthSquared() {
        return dot(this);
    }

    /**
     * Converts the vector to a {@link Point} with the same x and y values.
     */
    public Point asPoint() {
        return new Point(x, y);
    }

    /**
     * Returns this vector normalised to have a length of 1 by dividing by its length.
     */
    public Vector2 normalised() {
        return div(length());
    }

    /**
     * Performs <b>exact</b> equality checking.
     */
    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof Vector2
            && sub((Vector2) rhs).lengthSquared() < EPSILON * EPSILON;
    }

    @Override
    public String toString() {
        return String.format("Vector2: (%.2f,%.2f)", x, y);
    }
}
