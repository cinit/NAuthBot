package nil.test.uncertaintycalc.math.func;

import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.MathFunction;
import nil.test.uncertaintycalc.math.RuntimeEnv;

import java.math.BigDecimal;

public class Rnd extends MathFunction {
    @Override
    public BigDecimal call(RuntimeEnv ctx, BigDecimal... argv) throws MathException {
        if (argv.length != 0)
            throw new IndexOutOfBoundsException("function has " + getArgumentCount() + " arguments, but "
                    + argv.length + " provided");
        return BigDecimal.valueOf(Math.random());
    }

    @Override
    public int getArgumentCount() {
        return 0;
    }

    @Override
    public String[] getArgumentNames() {
        return new String[0];
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public boolean isVolatile() {
        return true;
    }

    @Override
    public String getName() {
        return "rnd";
    }
}
