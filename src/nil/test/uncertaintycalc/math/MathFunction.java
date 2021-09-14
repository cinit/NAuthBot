package nil.test.uncertaintycalc.math;

import java.math.BigDecimal;
import java.util.Arrays;

public abstract class MathFunction implements IFunction {
    public static final String[] EMPTY_ARGUMENT = new String[0];

    @Override
    public Object call(RuntimeEnv ctx, Object... argv) throws Exception {
        return call(ctx, Utils.arrayCast(argv, BigDecimal.class));
    }

    public abstract BigDecimal call(RuntimeEnv ctx, BigDecimal... argv) throws MathException;

    public abstract int getArgumentCount();

    public abstract String[] getArgumentNames();

    public abstract int[] getArgumentTypes();

    public MathFunction pdiff(int index) throws TargetNotDifferentiableException {
        if (index < 0 || index >= getArgumentCount())
            throw new IndexOutOfBoundsException("try to differentiate index " + index + " for " + toString());
        throw new TargetNotDifferentiableException(toString() + " is not differentiable for variable " + getArgumentNames()[index]);
    }

    public abstract String getName();

    @Override
    public String toString() {
        return getName() + Arrays.deepToString(getArgumentNames()).replace('[', '(').replace(']', ')');
    }

    @Override
    public boolean isVolatile() {
        return false;
    }
}
