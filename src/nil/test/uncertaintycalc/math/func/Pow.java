package nil.test.uncertaintycalc.math.func;

import nil.test.uncertaintycalc.math.*;
import nil.test.uncertaintycalc.math.expr.MulChainExpr;
import nil.test.uncertaintycalc.math.expr.NumericVariable;
import nil.test.uncertaintycalc.math.expr.PlusChainExpr;
import nil.test.uncertaintycalc.math.expr.StaticFunctionCall;

import java.math.BigDecimal;
import java.math.MathContext;

public class Pow extends MathFunction {
    @Override
    public BigDecimal call(RuntimeEnv ctx, BigDecimal... argv) throws MathException {
        if (argv.length != 2)
            throw new IndexOutOfBoundsException("function has " + getArgumentCount() + " arguments, but "
                    + argv.length + " provided");
        BigDecimal x = argv[0];
        BigDecimal n = argv[1];
        MathContext mc = ctx.getMathContext();
        return new BigDecimal(Math.pow(x.doubleValue(), n.doubleValue()), ctx.getMathContext());
    }

    @Override
    public int getArgumentCount() {
        return 2;
    }

    @Override
    public MathFunction pdiff(int index) throws TargetNotDifferentiableException {
        if (index == 0) {
            return new ExpressionFunction(new MulChainExpr().multiply(new NumericVariable("n")).multiply(new StaticFunctionCall(new Pow(), new NumericVariable("x"), new PlusChainExpr().plus(new NumericVariable("n")).minus(1))), "pow'", "x", "m");
        } else if (index == 1) {
            throw new RuntimeException("pow'n(x,n) is not implemented");
        } else throw new IndexOutOfBoundsException("try to differentiate index " + index + " for " + toString());
    }

    @Override
    public String[] getArgumentNames() {
        return new String[]{"x", "n"};
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{Types.TYPE_NUMERIC, Types.TYPE_NUMERIC};
    }

    @Override
    public String getName() {
        return "pow";
    }
}
