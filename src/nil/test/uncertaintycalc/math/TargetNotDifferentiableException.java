package nil.test.uncertaintycalc.math;

public class TargetNotDifferentiableException extends MathException {
    public TargetNotDifferentiableException(String message) {
        super(message);
    }

    public TargetNotDifferentiableException(String message, Throwable cause) {
        super(message, cause);
    }

    public TargetNotDifferentiableException(Throwable cause) {
        super(cause);
    }
}
