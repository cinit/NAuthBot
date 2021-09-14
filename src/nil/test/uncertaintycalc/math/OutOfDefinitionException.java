package nil.test.uncertaintycalc.math;

public class OutOfDefinitionException extends MathException {
    public OutOfDefinitionException(String message) {
        super(message);
    }

    public OutOfDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutOfDefinitionException(Throwable cause) {
        super(cause);
    }
}
