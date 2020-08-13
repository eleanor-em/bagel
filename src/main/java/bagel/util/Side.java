package bagel.util;

/**
 * Represents the side that a collision occurred from.
 */
public enum Side {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    NONE;

    /**
     * Converts a normal unit vector to a side.
     */
    public static Side fromVec(Vector2 vec) {
        if (vec.equals(Vector2.left)) {
            return LEFT;
        } else if (vec.equals(Vector2.right)) {
            return RIGHT;
        } else if (vec.equals(Vector2.up)) {
            return TOP;
        } else if (vec.equals(Vector2.down)) {
            return BOTTOM;
        } else {
            return NONE;
        }
    }
}
