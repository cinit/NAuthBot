package nil.test.uncertaintycalc.math.func;

import nil.test.uncertaintycalc.math.*;

import java.math.BigDecimal;

public class ConstantFunction extends MathFunction {
    private final BigDecimal mValue;
    private final String mName;
    private final String[] mArgNames;

    public ConstantFunction(BigDecimal val, String name, String[] argNames) {
        mName = name;
        mValue = val;
        mArgNames = argNames;
    }

    @Override
    public BigDecimal call(RuntimeEnv mc, BigDecimal... argv) throws MathException {
        return mValue;
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
        return new ConstantFunction(new BigDecimal(0), mName + "'" + (mArgNames.length == 1 ? "" : mArgNames[index]), mArgNames);
    }

    @Override
    public String getName() {
        return mName;
    }
}
