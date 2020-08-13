package bagel.util;

public class Colour {
    public final double r;
    public final double g;
    public final double b;
    public final double a;

    public Colour(double r, double g, double b) {
        this(r, g, b, 1);
    }

    public Colour(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static final Colour BLACK = new Colour(0, 0, 0);
    public static final Colour WHITE = new Colour(1, 1, 1);
    public static final Colour RED = new Colour(1, 0, 0);
    public static final Colour GREEN = new Colour(0, 1, 0);
    public static final Colour BLUE = new Colour(0, 0, 1);
}
