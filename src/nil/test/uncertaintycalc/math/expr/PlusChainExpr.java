package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.latex.MsConsts;
import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.TargetNotDifferentiableException;
import nil.test.uncertaintycalc.math.Types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class PlusChainExpr implements Expression {

    private ArrayList<BigDecimal> mFactors = new ArrayList<>();
    private ArrayList<Expression> mExpressions = new ArrayList<>();
    private boolean mFinal = false;

    public PlusChainExpr plus(Expression expr, BigDecimal factor) {
        checkFinal();
        if (factor.compareTo(new BigDecimal(0)) != 0) {
            mExpressions.add(expr);
            mFactors.add(factor);
        }
        return this;
    }

    public PlusChainExpr plus(Expression expr, double factor) {
        checkFinal();
        if (factor != 0d) {
            mExpressions.add(expr);
            mFactors.add(new BigDecimal(factor));
        }
        return this;
    }

    public PlusChainExpr plus(Expression expr, long factor) {
        checkFinal();
        if (factor != 0) {
            mExpressions.add(expr);
            mFactors.add(new BigDecimal(factor));
        }
        return this;
    }

    public PlusChainExpr plus(Expression expr) {
        checkFinal();
        mExpressions.add(expr);
        mFactors.add(new BigDecimal(1));
        return this;
    }

    public PlusChainExpr minus(Expression expr) {
        checkFinal();
        mExpressions.add(expr);
        mFactors.add(new BigDecimal(-1));
        return this;
    }

    public PlusChainExpr minus(long val) {
        checkFinal();
        mExpressions.add(new ConstNumericExpr(val));
        mFactors.add(new BigDecimal(-1));
        return this;
    }

    public PlusChainExpr clearAll() {
        checkFinal();
        mFactors.clear();
        mExpressions.clear();
        return this;
    }

    @Override
    public BigDecimal eval(RuntimeEnv env, Map<String, Object> variables) throws MathException {
        BigDecimal ret = null;
        Expression exp;
        BigDecimal fac;
        for (int i = 0; i < mExpressions.size(); i++) {
            exp = mExpressions.get(i);
            fac = mFactors.get(i);
            if (ret == null) ret = exp.eval(env, variables).multiply(fac);
            else ret = ret.add(exp.eval(env, variables).multiply(fac));
        }
        if (ret != null) return ret;
        else {
            System.err.println("PlusChainExpr/W evaluate an empty add chain, return 0");
            return new BigDecimal(0);
        }
    }

    @Override
    public Expression differentiate(String sym, Map<String, Object> variables) throws TargetNotDifferentiableException {
        PlusChainExpr ret = new PlusChainExpr();
        Expression expr, dexpr;
        BigDecimal fac;
        for (int i = 0; i < mExpressions.size(); i++) {
            expr = mExpressions.get(i);
            fac = mFactors.get(i);
            if (fac.equals(BigDecimal.ZERO)) continue;
            dexpr = expr.differentiate(sym, variables);
            if (dexpr instanceof ConstNumericExpr && ((ConstNumericExpr) dexpr).isConstantZero()) continue;
            if (dexpr instanceof MulChainExpr && ((MulChainExpr) dexpr).isConstantZero()) continue;
            ret.plus(dexpr, fac);
        }
        return ret;
    }

    @Override
    public Variable[] queryVariables() {
        HashSet<Variable> vars = new HashSet<>();
        for (Expression expr : mExpressions) {
            vars.addAll(Arrays.asList(expr.queryVariables()));
        }
        return vars.toArray(new Variable[0]);
    }

    @Override
    public boolean hasVariable() {
        for (Expression expr : mExpressions) {
            if (expr.hasVariable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConstantFor(String sym) {
        for (Expression expr : mExpressions) {
            if (!expr.isConstantFor(sym)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isConstant() {
        for (Expression expr : mExpressions) {
            if (expr.isConstant()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConstantZero() {
        for (Expression expr : mExpressions) {
            if (!expr.isConstantZero()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFinal() {
        return mFinal;
    }

    public void makeFinal() {
        mFinal = true;
    }

    private void checkFinal() {
        if (mFinal) throw new IllegalStateException("try to modify a final expression");
    }

    public PlusChainExpr fork() {
        PlusChainExpr p = new PlusChainExpr();
        p.mExpressions.addAll(mExpressions);
        p.mFactors.addAll(mFactors);
        return p;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean isVolatile() {
        for (Expression expr : mExpressions) {
            if (expr.isVolatile()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean needBraceIn(int level) {
        return level >= Types.PRIORITY_ADD_OR_MIN;
    }

    @Override
    public String toLaTeX() {
        StringBuilder sb = new StringBuilder();
        Expression expr;
        BigDecimal fac;
        for (int i = 0; i < mExpressions.size(); i++) {
            expr = mExpressions.get(i);
            fac = mFactors.get(i);
            if (fac.equals(BigDecimal.ZERO)) continue;
            if (fac.signum() < 0) {
                sb.append('-');
                if (!fac.negate().equals(BigDecimal.ONE)) {
                    sb.append(fac.negate().toString());
                    sb.append(MsConsts.CHAR_MULTIPLY);
                }
                if (expr.needBraceIn(Types.PRIORITY_MUL_OR_DIV)) {
                    sb.append('(');
                    sb.append(expr.toLaTeX());
                    sb.append(')');
                } else {
                    sb.append(expr.toLaTeX());
                }
            } else {
                if (i != 0) sb.append('+');
                if (!fac.equals(BigDecimal.ONE)) {
                    sb.append(fac.toString());
                    sb.append(MsConsts.CHAR_MULTIPLY);
                }
                if (expr.needBraceIn(Types.PRIORITY_MUL_OR_DIV)) {
                    sb.append('(');
                    sb.append(expr.toLaTeX());
                    sb.append(')');
                } else {
                    sb.append(expr.toLaTeX());
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toLaTeX();
    }
}
