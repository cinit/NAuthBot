package nil.test.uncertaintycalc.junk;

import nil.test.uncertaintycalc.math.expr.*;
import nil.test.uncertaintycalc.math.func.Cos;
import nil.test.uncertaintycalc.math.func.Ln;
import nil.test.uncertaintycalc.math.func.Sin;
import nil.test.uncertaintycalc.math.func.Sqrt;

import java.security.SecureRandom;
import java.util.Random;

public class RandomExprGenerator {
    private static final Random r = new SecureRandom();

    public static Expression next(int d) {
        if (d > 30 && r.nextFloat() > 0.35) {
            d -= 30;
            Expression exp1 = next(d % 89);
            switch (Math.abs(r.nextInt()) % 4) {
                case 0:
                    return new StaticFunctionCall(new Sqrt(), exp1);
                case 1:
                    return new StaticFunctionCall(new Sin(), exp1);
                case 2:
                    return new StaticFunctionCall(new Cos(), exp1);
                case 3:
                    return new StaticFunctionCall(new Ln(), exp1);
            }
        } else if (d > 20 && r.nextFloat() > 0.4) {
            d -= 20;
            Expression exp1 = next(d);
            Expression exp2 = next(d);
            int a = 0;
            switch (Math.abs(r.nextInt()) % 2) {
                case 0:
                    a = 1;
                case 1:
                    if (exp1 instanceof MulChainExpr) {
                        if (a == 0) {
                            return ((MulChainExpr) exp1).multiply(exp2);
                        } else {
                            return ((MulChainExpr) exp1).divide(exp2);
                        }
                    }
                    if (exp2 instanceof MulChainExpr) {
                        if (a == 0) {
                            return ((MulChainExpr) exp2).multiply(exp1);
                        } else {
                            return ((MulChainExpr) exp2).divide(exp1);
                        }
                    }
                    MulChainExpr e = new MulChainExpr().multiply(exp1);
                    if (a == 0) {
                        return e.multiply(exp2);
                    } else {
                        return e.divide(exp2);
                    }
            }
        } else if (d > 10) {
            d -= 10;
            Expression exp1 = next(d);
            Expression exp2 = next(d);
            int a = 0;
            switch (Math.abs(r.nextInt()) % 2) {
                case 0:
                    a = 1;
                case 1:
                    if (exp1 instanceof PlusChainExpr) {
                        if (a == 0) {
                            return ((PlusChainExpr) exp1).plus(exp2);
                        } else {
                            return ((PlusChainExpr) exp1).minus(exp2);
                        }
                    }
                    if (exp2 instanceof PlusChainExpr) {
                        if (a == 0) {
                            return ((PlusChainExpr) exp2).plus(exp1);
                        } else {
                            return ((PlusChainExpr) exp2).minus(exp1);
                        }
                    }
                    PlusChainExpr e = new PlusChainExpr().plus(exp1);
                    if (a == 0) {
                        return e.plus(exp2);
                    } else {
                        return e.minus(exp2);
                    }
            }
        } else {
            int i = Math.abs(r.nextInt()) % 6;
            switch (i) {
                case 0:
                case 5:
                case 4:
                    return new NumericVariable("x");
                case 1:
                case 2:
                case 3:
                    return new ConstNumericExpr(i);
            }
        }
        throw new RuntimeException("condition flip, this should NOT happen ???");
    }
}
