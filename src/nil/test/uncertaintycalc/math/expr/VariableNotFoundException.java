package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.math.MathException;

public class VariableNotFoundException extends MathException {
    public VariableNotFoundException(String message) {
        super(message);
    }

    public VariableNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VariableNotFoundException(Throwable cause) {
        super(cause);
    }
}
