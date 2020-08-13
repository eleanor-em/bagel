package bagel.util;

/**
 * Represents a rectangle, with its top-left at a point and a set size.
 *
 * Contains methods to easily calculate collisions with other types of geometry.
 */
public class Rectangle {
    private Point origin;
    private final double width;
    private final double height;

    /**
     * Returns a {@link Point} representing the top-left of the rectangle.
     */
    public Point topLeft() {
        return origin;
    }

    /**
     * Returns a {@link Point} representing the top-right of the rectangle.
     */
    public Point topRight() {
        return new Point(right(), top());
    }

    /**
     * Returns a {@link Point} representing the bottom-left of the rectangle.
     */
    public Point bottomLeft() {
        return new Point(left(), bottom());
    }

    /**
     * Returns a {@link Point} representing the bottom-right of the rectangle.
     */
    public Point bottomRight() {
        return new Point(right(), bottom());
    }

    /**
     * Returns a {@link Point} representing the centre of the rectangle.
     */
    public Point centre() {
        return new Point(left() + width / 2.0, top() + height / 2.0);
    }

    /**
     * Returns the x-coordinate of the right side of the rectangle.
     */
    public double right() {
        return origin.x + width;
    }

    /**
     * Returns the x-coordinate of the left side of the rectangle.
     */
    public double left() {
        return origin.x;
    }

    /**
     * Returns the y-coordinate of the top side of the rectangle.
     */
    public double top() {
        return origin.y;
    }

    /**
     * Returns the y-coordinate of the bottom side of the rectangle.
     */
    public double bottom() {
        return origin.y + height;
    }

    /**
     * Moves the rectangle so that its top-left is at the specified point.
     */
    public void moveTo(Point topLeft) {
        origin = topLeft;
    }

    /**
     * Copy constructor.
     */
    public Rectangle(Rectangle rect) {
        this(rect.topLeft(), rect.width, rect.height);
    }

    /**
     * Creates a rectangle at a particular (x, y) top-left position with a given width and height.
     */
    public Rectangle(double x, double y, double width, double height) {
        this(new Point(x, y), width, height);
    }

    /**
     * Creates a rectangle with a specified top-left position and a given width and height.
     */
    public Rectangle(Point topLeft, double width, double height) {
        this.origin = topLeft;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns whether the given point lies inside the rectangle (including its edges).
     */
    public boolean intersects(Point point) {
        return point.x >= left() && point.x <= right()
            && point.y >= top() && point.y <= bottom();
    }

    /**
     * Returns whether any part of the given rectangle overlaps with this rectangle.
     */
    public boolean intersects(Rectangle rectangle) {
        return left() < rectangle.right() && right() > rectangle.left()
            && top() < rectangle.bottom() && bottom() > rectangle.top();
    }

    /**
     * Estimates which side of the rectangle the point intersected at, using the previous position to calculate a more
     * accurate estimation.
     *
     * @see Rectangle#intersectedAt(Point, Vector2)
     */
    public Side intersectedAt(Point point, Point lastPoint) {
        return intersectedAt(point, point.asVector().sub(lastPoint.asVector()));
    }

    /**
     * Estimates which side of the rectangle the point intersected at, using the velocity of the point to inform the
     * result.
     *
     * Algorithm:
     * 1. Find the mid-point between `point` and its previous location (via velocity)
     * 2. If either point intersects: (to handle the case where the object "skips" through the rectangle):
     * 3.     Find the collision normal (the direction the point struck the rectangle)
     * 4.     Return the basis vector with maximal dot product
     * 5. Else return NONE
     */
    public Side intersectedAt(Point point, Vector2 velocity) {
        // Calculate the centre of the point and its previous position
        Vector2 midpoint = point.asVector().sub(velocity.div(2));
        // This condition handles the case where we skipped right through the rectangle
        if (intersects(point) || intersects(midpoint.asPoint())) {
            Vector2 centre = this.centre().asVector();
            // Therefore calculate the collision normal
            // h/t Charles (Hongwei Chen)
            Vector2 normal = midpoint.sub(centre);

            // Find the maximal overlap with the rectangle normals
            double max = Double.MIN_VALUE;
            Side bestSoFar = Side.NONE;

            Vector2[] sides = new Vector2[] { Vector2.left, Vector2.right, Vector2.up, Vector2.down };
            for (Vector2 v : sides) {
                double res = v.dot(normal);
                if (res > max) {
                    max = res;
                    bestSoFar = Side.fromVec(v);
                }
            }
            return bestSoFar;
        } else {
            return Side.NONE;
        }
    }

    @Override
    public String toString() {
        return "Rectangle: top left: " + topLeft() + ", width: " + width + ", height: " + height;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Rectangle) {
            Rectangle rhs = (Rectangle) other;
            return topLeft().equals(rhs.topLeft()) && width == rhs.width && height == rhs.height;
        } else {
            return false;
        }
    }
}