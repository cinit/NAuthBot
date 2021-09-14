package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.TargetNotDifferentiableException;
import nil.test.uncertaintycalc.math.Types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class NumericVariable implements Expression, Variable {

    private final String mName;

    public NumericVariable(String name) {
        mName = name;
    }

    @Override
    public BigDecimal eval(RuntimeEnv mc, Map<String, Object> variables) throws MathException {
        if (variables == null || !variables.containsKey(mName))
            throw new VariableNotFoundException("variable " + mName + " not found");
        Object v = variables.get(mName);
        if (v instanceof Number) {
            if (v instanceof BigDecimal) {
                return (BigDecimal) v;
            } else if (v instanceof BigInteger) {
                return new BigDecimal(((BigInteger) v).toString());
            } else if (v instanceof Long || v instanceof Integer) {
                return new BigDecimal(((Number) v).longValue());
            }
            return BigDecimal.valueOf(((Number) v).doubleValue());
        } else if (v instanceof Expression) {
            if (v == this) throw new VariableNotFoundException("reject variable == this");
            return ((Expression) v).eval(mc, variables);
        }
        throw new MathException("Unexpected variable " + mName + " type " + v.getClass());
    }

    @Override
    public Expression differentiate(String sym, Map<String, Object> variables) throws TargetNotDifferentiableException {
        if (mName.equals(sym)) {
            return ConstNumericExpr.CONST_1;
        } else {
            return ConstNumericExpr.CONST_0;
        }
    }

    @Override
    public boolean hasVariable() {
        return true;
    }

    @Override
    public Variable[] queryVariables() {
        return new Variable[]{this};
    }

    public String getName() {
        return mName;
    }

    @Override
    public int getType() {
        return Types.TYPE_NUMERIC;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public boolean needBraceIn(int level) {
        return level >= Types.PRIORITY_CONST_OR_VARIABLE;
    }

    @Override
    public String toLaTeX() {
        return mName;
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isConstantFor(String sym) {
        return !mName.equals(sym);
    }

    @Override
    public boolean isConstantZero() {
        return false;
    }

    @Override
    public String toString() {
        return "NumVar{" + mName + '}';
    }
}
