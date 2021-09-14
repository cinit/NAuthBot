package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.latex.MsConsts;
import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.TargetNotDifferentiableException;
import nil.test.uncertaintycalc.math.Types;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class MulChainExpr implements Expression {

    private ArrayList<Integer> mExponents = new ArrayList<>();
    private ArrayList<Expression> mExpressions = new ArrayList<>();
    private boolean mFinal = false;


    public MulChainExpr multiply(Expression expr, int e) {
        checkFinal();
        if (e != 0) {
            mExpressions.add(expr);
            mExponents.add(e);
        }
        return this;
    }

    public MulChainExpr multiply(Expression expr) {
        checkFinal();
        mExpressions.add(expr);
        mExponents.add(1);
        return this;
    }

    public MulChainExpr multiply(long val) {
        checkFinal();
        mExpressions.add(new ConstNumericExpr(val));
        mExponents.add(1);
        return this;
    }

    public MulChainExpr divide(Expression expr) {
        checkFinal();
        mExpressions.add(expr);
        mExponents.add(-1);
        return this;
    }

    public MulChainExpr clearAll() {
        checkFinal();
        mExponents.clear();
        mExpressions.clear();
        return this;
    }

    @Override
    public BigDecimal eval(RuntimeEnv env, Map<String, Object> variables) throws MathException {
        BigDecimal ret = null;
        Expression exp;
        MathContext mc = env.getMathContext();
        int e;
        for (int i = 0; i < mExpressions.size(); i++) {
            exp = mExpressions.get(i);
            e = mExponents.get(i);
            if (ret == null) {
                if (e >= 0) {
                    ret = exp.eval(env, variables).pow(e, mc);
                } else {
                    ret = new BigDecimal(1).divide(exp.eval(env, variables).pow(-e, mc), mc);
                }
            } else {
                if (e >= 0) {
                    ret = ret.multiply(exp.eval(env, variables).pow(e, mc), mc);
                } else {
                    ret = ret.divide(exp.eval(env, variables).pow(-e, mc), mc);
                }
            }
        }
        if (ret != null) return ret;
        else {
            System.err.println("MulChainExpr/W evaluate an empty multiply chain, return 1");
            return new BigDecimal(1);
        }
    }

    @Override
    public Expression differentiate(String sym, Map<String, Object> variables) throws TargetNotDifferentiableException {
        ArrayList<Integer> rIndex = new ArrayList<>();
        for (int i = 0; i < mExpressions.size(); i++) {
            if (!mExpressions.get(i).isConstantFor(sym)) {
                rIndex.add(i);
            }
        }
        if (rIndex.size() == 0) return ConstNumericExpr.CONST_0;
        if (rIndex.size() == 1) {
            int re = mExponents.get(rIndex.get(0));
            Expression rexp = mExpressions.get(rIndex.get(0));
            MulChainExpr step = new MulChainExpr();
            if (re != 1) {
                step.multiply(new ConstNumericExpr(re));
            }
            for (int i = 0; i < mExpressions.size(); i++) {
                Expression _exp = mExpressions.get(i);
                int _e = mExponents.get(i);
                if (_exp != rexp) {
                    step.multiply(_exp, _e);
                }
            }
            step.multiply(rexp, re - 1).multiply(rexp.differentiate(sym, variables));
            return step;
        } else {
            PlusChainExpr ret = new PlusChainExpr();
            for (int rIdx : rIndex) {
                int re = mExponents.get(rIdx);
                Expression rexp = mExpressions.get(rIdx);
                MulChainExpr step = new MulChainExpr();
                if (re != 1) {
                    step.multiply(new ConstNumericExpr(re));
                }
                for (int i = 0; i < mExpressions.size(); i++) {
                    Expression _exp = mExpressions.get(i);
                    int _e = mExponents.get(i);
                    if (_exp != rexp) {
                        step.multiply(_exp, _e);
                    }
                }
                step.multiply(rexp, re - 1).multiply(rexp.differentiate(sym, variables));
                ret.plus(step);
            }
            return ret;
        }
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

    public boolean isFinal() {
        return mFinal;
    }

    public void makeFinal() {
        mFinal = true;
    }

    private void checkFinal() {
        if (mFinal) throw new IllegalStateException("try to modify a final expression");
    }

    public MulChainExpr fork() {
        MulChainExpr p = new MulChainExpr();
        p.mExpressions.addAll(mExpressions);
        p.mExponents.addAll(mExponents);
        return p;
    }

    public boolean isConstantZero() {
        for (Expression expr : mExpressions) {
            if (expr.isConstantZero()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConstant() {
        if (isConstantZero()) return true;
        for (Expression expr : mExpressions) {
            if (!expr.isConstant()) {
                return false;
            }
        }
        return true;
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
    public boolean isVolatile() {
        for (Expression expr : mExpressions) {
            if (expr.isVolatile()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toLaTeX() {
        StringBuilder ret = new StringBuilder();
        Expression expr;
        int exponent;
        int mulCount = 0;
        int divCount = 0;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < mExpressions.size(); i++) {
            expr = mExpressions.get(i);
            exponent = mExponents.get(i);
            if (exponent > 0) {
                if (mulCount != 0) {
                    tmp.append(MsConsts.CHAR_MULTIPLY);
                }
                mulCount++;
                if (exponent == 1) {
                    if (expr.needBraceIn(Types.PRIORITY_MUL_OR_DIV)) {
                        tmp.append('(');
                        tmp.append(expr.toLaTeX());
                        tmp.append(')');
                    } else {
                        tmp.append(expr.toLaTeX());
                    }
                } else {
                    if (expr.needBraceIn(Types.PRIORITY_POWER)) {
                        tmp.append('(');
                        tmp.append(expr.toLaTeX());
                        tmp.append(')');
                    } else {
                        tmp.append(expr.toLaTeX());
                    }
                    tmp.append('^');
                    tmp.append(exponent);
                }
            }
        }
        if (mulCount == 0) {
            //nothing
        } else if (mulCount == 1) {
            ret.append(tmp);
        } else {
            ret.append('(');
            ret.append(tmp);
            ret.append(')');
        }
        tmp = new StringBuilder();
        for (int i = 0; i < mExpressions.size(); i++) {
            expr = mExpressions.get(i);
            exponent = mExponents.get(i);
            if (exponent < 0) {
                if (divCount != 0) {
                    tmp.append(MsConsts.CHAR_MULTIPLY);
                }
                divCount++;
                if (exponent == -1) {
                    if (expr.needBraceIn(Types.PRIORITY_MUL_OR_DIV)) {
                        tmp.append('(');
                        tmp.append(expr.toLaTeX());
                        tmp.append(')');
                    } else {
                        tmp.append(expr.toLaTeX());
                    }
                } else {
                    if (expr.needBraceIn(Types.PRIORITY_POWER)) {
                        tmp.append('(');
                        tmp.append(expr.toLaTeX());
                        tmp.append(')');
                    } else {
                        tmp.append(expr.toLaTeX());
                    }
                    tmp.append('^');
                    tmp.append(-exponent);
                }
            }
        }
        if (divCount == 0) {
            //nothing
        } else {
            if (mulCount == 0) {
                ret.append('1');
            }
            ret.append('/');
            if (divCount == 1) {
                ret.append(tmp);
            } else {
                ret.append('(');
                ret.append(tmp);
                ret.append(')');
            }
        }
        return ret.toString();//Er=(4×sin(x)×x)/((x-1)×5×cos(x))
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean needBraceIn(int level) {
        return mExpressions.size() == 1 ? (mExpressions.get(0).needBraceIn(level)) : level > Types.PRIORITY_MUL_OR_DIV;
    }

    @Override
    public String toString() {
        return toLaTeX();
    }
}
