package nil.test.uncertaintycalc.math.func;

import nil.test.uncertaintycalc.math.*;

import java.math.BigDecimal;

public class Sin extends MathFunction {
    @Override
    @PrecisionLoss
    public BigDecimal call(RuntimeEnv ctx, BigDecimal... argv) throws MathException {
        if (argv.length != 1)
            throw new IndexOutOfBoundsException("function has " + getArgumentCount() + " arguments, but "
                    + argv.length + " provided");
        return BigDecimal.valueOf(Math.sin(argv[0].doubleValue()));
    }

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public MathFunction pdiff(int index) throws TargetNotDifferentiableException {
        if (index != 0) {
            throw new IndexOutOfBoundsException("try to differentiate index " + index + " for " + toString());
        }
        return new Cos();
    }

    @Override
    public String[] getArgumentNames() {
        return new String[]{"x"};
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{Types.TYPE_NUMERIC};
    }

    @Override
    public String getName() {
        return "sin";
    }
}
