package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.MathFunction;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.TargetNotDifferentiableException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class StaticFunctionCall implements Expression {

    public static final Expression[] EMPTY_EXPR_ARRAY = new Expression[0];

    private final MathFunction mFunc;
    private final Expression[] mArgv;

    public StaticFunctionCall(MathFunction func, Expression... expr) {
        if (expr == null) {
            expr = EMPTY_EXPR_ARRAY;
        }
        if (func.getArgumentCount() != expr.length)
            throw new IllegalArgumentException("function has " + func.getArgumentCount() + " arguments, but got " + expr.length);
        mFunc = func;
        mArgv = expr;
    }

    @Override
    public BigDecimal eval(RuntimeEnv mc, Map<String, Object> variables) throws MathException {
        if (mArgv.length == 0) return mFunc.call(mc);
        BigDecimal[] arg = new BigDecimal[mArgv.length];
        for (int i = 0; i < mArgv.length; i++) {
            arg[i] = mArgv[i].eval(mc, variables);
        }
        return mFunc.call(mc, arg);
    }

    @Override
    public Expression differentiate(String sym, Map<String, Object> variables) throws TargetNotDifferentiableException {
        ArrayList<MulChainExpr> rets = new ArrayList<>();
        for (int i = 0; i < mArgv.length; i++) {
            if (mArgv[i].isConstantFor(sym)) continue;
            MulChainExpr mul = new MulChainExpr();
            mul.multiply(new StaticFunctionCall(mFunc.pdiff(i), mArgv)).multiply(mArgv[i].differentiate(sym, variables));
            rets.add(mul);
        }
        if (rets.size() == 0) return ConstNumericExpr.CONST_0;
        if (rets.size() == 1) return rets.get(0);
        PlusChainExpr plusChainExpr = new PlusChainExpr();
        for (MulChainExpr exp : rets) {
            plusChainExpr.plus(exp);
        }
        return plusChainExpr;
    }

    @Override
    public Variable[] queryVariables() {
        HashSet<Variable> vars = new HashSet<>();
        for (Expression expr : mArgv) {
            vars.addAll(Arrays.asList(expr.queryVariables()));
        }
        return vars.toArray(new Variable[0]);
    }

    @Override
    public boolean hasVariable() {
        for (Expression expr : mArgv) {
            if (expr.hasVariable()) return true;
        }
        return false;
    }

    @Override
    public boolean isVolatile() {
        if (mFunc.isVolatile()) return true;
        for (Expression expr : mArgv) {
            if (expr.isVolatile()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConstantFor(String sym) {
        for (Expression expr : mArgv) {
            if (!expr.isConstantFor(sym)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isConstantZero() {
        return false;
    }

    @Override
    public String toString() {
        return toLaTeX();
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean needBraceIn(int level) {
        return false;
    }

    @Override
    public String toLaTeX() {
//        if (mFunc instanceof ExpressionFunction) {
//            Expression expr = ((ExpressionFunction) mFunc).getExpression();
//            expr.queryVariables()[0].getName()
        /*} else*/
        {
            StringBuilder sb = new StringBuilder(mFunc.getName());
            sb.append('(');
            for (int i = 0; i < mArgv.length; i++) {
                sb.append(mArgv[i].toLaTeX());
                if (i != mArgv.length - 1) {
                    sb.append(',');
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }
}
