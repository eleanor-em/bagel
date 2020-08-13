package bagel;

/**
 * A wrapper class for any exceptions thrown by Bagel.
 *
 * As Bagel is used for introductory Java, no checked exceptions may be thrown.
 *
 * @author Eleanor McMurtry
 */
public class BagelError extends RuntimeException {
    public BagelError(String err) {
        super(err);
    }
    public BagelError(String err, Throwable cause) {
        super(err, cause);
    }

    public BagelError(Throwable t) {
        super(t);
    }
}
