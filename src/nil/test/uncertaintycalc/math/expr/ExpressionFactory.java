package nil.test.uncertaintycalc.math.expr;

import nil.test.uncertaintycalc.math.MathException;
import nil.test.uncertaintycalc.math.MathFunction;

import java.math.BigDecimal;
import java.util.Map;

public class ExpressionFactory {

    public static Expression createFromString(String exprStr, Map<String, Object> vars) throws MathException {
        return createFromString(exprStr, vars, false);
    }

    public static Expression createFromString(String exprStr, Map<String, Object> vars, boolean allowDynamic) throws MathException {
        if (exprStr == null || exprStr.equals("")) throw new IllegalArgumentException("empty expression");
        System.out.printf("ExpressionFactory/D eval: '%s'\n", exprStr);
        int priority = getLowestPriority(exprStr);
        if (priority == PRIORITY_CONST_OR_VARIABLE) {
            try {
                BigDecimal d;
                d = new BigDecimal(exprStr);
                return new ConstNumericExpr(d);
            } catch (NumberFormatException ignored) {
            }
            Object o = vars.get(exprStr);
            if (o != null) {
                if (o instanceof Expression) {
                    return (Expression) o;
                } else {
                    Variable v = (Variable) o;
                    return new NumericVariable(v.getName());
                }
            } else if (allowDynamic) {
                return new NumericVariable(exprStr);
            } else {
                throw new VariableNotFoundException("undefined symbol '" + exprStr + "'");
            }
        } else if (priority == PRIORITY_BRACE || priority == PRIORITY_FUNCTION_CALL) {
            //int braced=0;
            int d = exprStr.indexOf('(');
            if (d == 0) {
                //not a function call
                return createFromString(exprStr.substring(1, exprStr.length() - 1), vars, allowDynamic);
            } else {
                //function call
                String name = exprStr.substring(0, d);
                Object o = vars.get(name);
                MathFunction fun = null;
                if (o instanceof MathFunction) {
                    fun = (MathFunction) o;
                }
                String arg = exprStr.substring(d + 1, exprStr.length() - 1);
                if (arg.length() > 0) {
                    String[] args = arg.split(",");
                    Expression[] as = new Expression[args.length];
                    for (int i = 0; i < args.length; i++)
                        as[i] = createFromString(args[i], vars, allowDynamic);
                    if (fun == null && !allowDynamic) {
                        throw new VariableNotFoundException("function '" + name + "' not found");
                    }
                    if (fun == null) {
                        return new DynamicFunctionCall(name, as);
                    } else {
                        return new StaticFunctionCall(fun, as);
                    }
                } else {
                    if (fun == null && !allowDynamic) {
                        throw new VariableNotFoundException("function '" + name + "' not found");
                    }
                    if (fun == null) {
                        return new DynamicFunctionCall(name);
                    } else {
                        return new StaticFunctionCall(fun);
                    }
                }
            }
        } /*else if (priority == PRIORITY_SINGLE_OPERAND) {
            int braced = 0;
            double last = 1d;
            char c;
            int startpos = 0;
            for (int i = 0; i < exprStr.length(); i++) {
                c = exprStr.charAt(i);
                if (c == '(') {
                    braced++;
                }
                if (c == ')') {
                    braced--;
                }
                if (c == '!' && braced == 0) {
                    last = last * FuncPool.getGeneral().get("fac").f(new Value(createFromString(exprStr.substring(startpos, i), vars))).toDouble();
                    startpos = i + 1;
                }
            }
            if (startpos != exprStr.length()) {
                last = last * createFromString(exprStr.substring(startpos), vars).toDouble();
            }
            return new Value(last);
        } else if (priority == PRIORITY_SINGLE_OPERAND) {
            int braced = 0;
            double last = 1d;
            char c;
            int startpos = 0;
            boolean first = true;
            for (int i = 0; i < exprStr.length(); i++) {
                c = exprStr.charAt(i);
                if (c == '(') {
                    braced++;
                }
                if (c == ')') {
                    braced--;
                }
                if (c == '^' && braced == 0) {
                    if (first) {
                        last = createFromString(exprStr.substring(0, i), vars).toDouble();
                        first = false;
                    } else {
                        last = Math.pow(last, createFromString(exprStr.substring(startpos, i), vars).toDouble());
                    }
                    startpos = i + 1;
                }
            }
            return new Value(Math.pow(last, createFromString(exprStr.substring(startpos), vars).toDouble()));
        }*/ else if (priority == PRIORITY_MUL_OR_DIV) {
            MulChainExpr me = new MulChainExpr();
            int len = exprStr.length();
            boolean divide = false;
            int startPos = 0;
            for (int i = 0; i < len; i++) {
                char c = exprStr.charAt(i);
                if ((c == '*' || c == '/')) {
                    //last statement
                    Expression tmpExpr;
                    tmpExpr = createFromString(exprStr.substring(startPos, i), vars, allowDynamic);
                    if (divide) {
                        me.divide(tmpExpr);
                    } else {
                        me.multiply(tmpExpr);
                    }
                    //end of last statement
                    divide = c == '/';
                    startPos = i + 1;
                } else {
                    if (c == '(') {
                        int end = findBraceEnd(exprStr, i);
                        if (end == -1) throw new IllegalArgumentException("brace mismatch: " + exprStr);
                        i = end;
                    }
                }
            }
            if (startPos < len) {
                Expression tmpExpr = createFromString(exprStr.substring(startPos, len), vars, allowDynamic);
                if (divide) {
                    me.divide(tmpExpr);
                } else {
                    me.multiply(tmpExpr);
                }
            } else {
                throw new IllegalArgumentException("truncated expression, missing operand after '*'/'/'");
            }
            return me;
        } else if (priority == PRIORITY_ADD_OR_MIN) {
            PlusChainExpr pe = new PlusChainExpr();
            int len = exprStr.length();
            boolean negative = false;
            boolean dummySymbol = false;
            boolean firstExp = true;
            int startPos = 0;
            for (int i = 0; i < len; i++) {
                char c = exprStr.charAt(i);
                if (!dummySymbol && (c == '+' || c == '-')) {
                    //last statement
                    Expression tmpExpr;
                    if (i == 0 && firstExp) {
                        firstExp = false;
                        tmpExpr = ConstNumericExpr.CONST_0;
                    } else {
                        tmpExpr = createFromString(exprStr.substring(startPos, i), vars, allowDynamic);
                    }
                    dummySymbol = true;
                    if (negative) {
                        pe.minus(tmpExpr);
                    } else {
                        pe.plus(tmpExpr);
                    }
                    //end of last statement
                    negative = c == '-';
                    startPos = i + 1;
                } else {
                    if (c != '+' && c != '-') dummySymbol = false;
                    if (c == '(') {
                        int end = findBraceEnd(exprStr, i);
                        if (end == -1) throw new IllegalArgumentException("brace mismatch: " + exprStr);
                        i = end;
                    }
                }
            }
            if (startPos < len) {
                Expression tmpExpr = createFromString(exprStr.substring(startPos, len), vars, allowDynamic);
                if (negative) {
                    pe.minus(tmpExpr);
                } else {
                    pe.plus(tmpExpr);
                }
            } else {
                throw new IllegalArgumentException("truncated expression, missing operand after +/-");
            }
            return pe;
        }
        throw new IllegalStateException("unexpected priority: " + priority);
    }


    public static final int
            PRIORITY_CONST_OR_VARIABLE = 32,
            PRIORITY_BRACE = 16,
            PRIORITY_FUNCTION_CALL = 15,
            PRIORITY_SINGLE_OPERAND = 14,
            PRIORITY_POWER = 13,
            PRIORITY_MUL_OR_DIV = 12,
            PRIORITY_ADD_OR_MIN = 11,
            PRIORITY_SHIFT = 10,
            PRIORITY_MEASURE = 9,
            PRIORITY_EQUALITY = 8,
            PRIORITY_BAND = 7,
            PRIORITY_BOR = 6,
            PRIORITY_XOR = 5,
            PRIORITY_AND = 4,
            PRIORITY_OR = 3,
            PRIORITY_CONDOP = 2,
            PRIORITY_ASSIGN_OR_MIXED = 1,
            PRIORITY_COMMA = 0;

    /**
     * 2+(1*3)
     * __^s__^r
     * 01234567
     *
     * @param expr
     * @param start s
     * @return r
     */
    private static int findBraceEnd(String expr, int start) {
        if (expr.charAt(start) != '(') throw new IllegalArgumentException("not left brace");
        int depth = 1;
        int len = expr.length();
        for (int i = start + 1; i < len; i++) {
            char c = expr.charAt(i);
            switch (c) {
                case '(': {
                    depth++;
                    break;
                }
                case ')': {
                    depth--;
                    break;
                }
            }
            if (depth == 0) return i;
        }
        return -1;
    }

    static private int getLowestPriority(String exp) {
        int a = exp.length();
        int braced = 0;
        int curr = PRIORITY_CONST_OR_VARIABLE;
        char c;
        for (int i = 0; i < a; i++) {
            c = exp.charAt(i);
            if (c == '(') {
                curr = Math.min(curr, PRIORITY_BRACE);
                braced++;
                continue;
            } else if (c == ')') {
                braced--;
                continue;
            }
            if (braced > 0) continue;
            if (c == '!') curr = Math.min(curr, PRIORITY_SINGLE_OPERAND);
            if (c == '^') curr = Math.min(curr, PRIORITY_POWER);
            if (i != 0 || true) {
                if (c == '+' || c == '-') curr = Math.min(curr, PRIORITY_ADD_OR_MIN);
                if (c == '*' || c == '/') curr = Math.min(curr, PRIORITY_MUL_OR_DIV);
            }
        }
        if (braced != 0) throw new IllegalArgumentException("Syntax Error:Braces not match in \"" + exp + '"');
        return curr;
    }
}
