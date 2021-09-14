package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.Disposable;
import nil.test.uncertaintycalc.latex.LaTeXFormula;
import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.TargetNotDifferentiableException;

import java.math.BigDecimal;
import java.util.Map;

public interface Expression extends Disposable, LaTeXFormula {

    String[] EMPTY_STRING_ARRAY = new String[0];
    Variable[] EMPTY_VARIABLE_ARRAY = new Variable[0];

    BigDecimal eval(RuntimeEnv env, Map<String, Object> variables) throws MathException;

    Expression differentiate(String sym, Map<String, Object> variables) throws TargetNotDifferentiableException;

    Variable[] queryVariables();

    boolean hasVariable();

    boolean isConstant();

    boolean isConstantZero();

    boolean isConstantFor(String sym);

    boolean isVolatile();
}
