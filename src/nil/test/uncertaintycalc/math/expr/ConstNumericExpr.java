package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.TargetNotDifferentiableException;

import java.math.BigDecimal;
import java.util.Map;

public class ConstNumericExpr implements Expression {
    public static final ConstNumericExpr CONST_0 = new ConstNumericExpr(new BigDecimal(0));
    public static final ConstNumericExpr CONST_1 = new ConstNumericExpr(new BigDecimal(1));

    private final BigDecimal mValue;
    private final String mName;

    public ConstNumericExpr(BigDecimal val) {
        mValue = val;
        mName = null;
    }

    public ConstNumericExpr(long val) {
        mValue = new BigDecimal(val);
        mName = null;
    }

    public ConstNumericExpr(String val) {
        mValue = new BigDecimal(val);
        mName = null;
    }

    @Deprecated
    public ConstNumericExpr(double val) {
        mValue = new BigDecimal(val);
        mName = null;
    }

    public ConstNumericExpr(BigDecimal val, String name) {
        mValue = val;
        mName = name;
    }

    @Override
    public BigDecimal eval(RuntimeEnv mc, Map<String, Object> variables) throws MathException {
        return mValue;
    }

    @Override
    public Expression differentiate(String sym, Map<String, Object> variables) throws TargetNotDifferentiableException {
        return CONST_0;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isConstantFor(String sym) {
        return true;
    }

    public boolean isConstantZero() {
        return mValue.equals(BigDecimal.ZERO);
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public boolean hasVariable() {
        return false;
    }

    @Override
    public Variable[] queryVariables() {
        return EMPTY_VARIABLE_ARRAY;
    }

    @Override
    public boolean needBraceIn(int level) {
        return mValue.signum() < 0;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public String toLaTeX() {
        return mValue.toString();
    }

    @Override
    public String toString() {
        return mValue.toString();
    }

}
