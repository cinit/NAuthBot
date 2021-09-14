package nil.test.uncertaintycalc.math.func;

import nil.test.uncertaintycalc.math.*;
import nil.test.uncertaintycalc.math.expr.Expression;

import java.math.BigDecimal;
import java.util.HashMap;

public class ExpressionFunction extends MathFunction {
    private final Expression mExpr;
    private final String mName;
    private final String[] mArgNames;

    public ExpressionFunction(Expression expr, String name, String... argNames) {
//        Variable[] vars = expr.queryVariables();
//        if (vars.length != 0) {
//            throw new IllegalArgumentException(expr + " has unsolved variable " + Arrays.toString(vars));
//        }
        mName = name;
        mExpr = expr;
        mArgNames = argNames;
    }

    @Override
    public BigDecimal call(RuntimeEnv runtimeEnv, BigDecimal... argv) throws MathException {
        if (argv.length != mArgNames.length)
            throw new IllegalArgumentException("function has " + getArgumentCount() + " arguments, but got " + argv.length);
        HashMap<String, Object> hashMap = new HashMap<>();
        for (int i = 0; i < mArgNames.length; i++) {
            hashMap.put(mArgNames[i], argv[i]);
        }
        return mExpr.eval(runtimeEnv, hashMap);
    }

    @Override
    public int getArgumentCount() {
        return mArgNames.length;
    }

    @Override
    public String[] getArgumentNames() {
        return mArgNames;
    }

    @Override
    public int[] getArgumentTypes() {
        return Utils.repeatArray(Types.TYPE_NUMERIC, mArgNames.length);
    }

    @Override
    public MathFunction pdiff(int index) throws TargetNotDifferentiableException {
        if (index < 0 || index >= mArgNames.length)
            throw new IndexOutOfBoundsException("function has " + getArgumentCount() + " arguments, but try to differentiate " + index);
        String name = mName + "'" + (mArgNames.length == 1 ? "" : mArgNames[index]);
        return new ExpressionFunction(mExpr.differentiate(mArgNames[index], null), name, mArgNames);
    }

    public Expression getExpression() {
        return mExpr;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean isVolatile() {
        return mExpr.isVolatile();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mName);
        sb.append('(');
        for (int i = 0; i < mArgNames.length; i++) {
            sb.append(mArgNames[i]);
            if (i != mArgNames.length - 1) {
                sb.append(',');
            }
        }
        sb.append(')');
        sb.append('=');
        sb.append(mExpr.toLaTeX());
        return sb.toString();
    }
}
