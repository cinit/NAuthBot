package nil.test.uncertaintycalc;

import nil.test.uncertaintycalc.junk.RandomExprGenerator;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.expr.Expression;

import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        RuntimeEnv rt = new RuntimeEnv(MathContext.DECIMAL32);
        Map<String, Object> vars = new HashMap<String, Object>();
//        vars.put("sqrt", new Sqrt());
//        vars.put("sin", new Sin());
//        vars.put("ln", new Ln());
//        vars.put("x", new NumericVariable("x"));
//        try {
//            Expression expr = ExpressionFactory.createFromString("sin(x)/ln(x)", vars);
//            System.out.println(expr);
//            MathFunction func = new ExpressionFunction(expr, "f", "x");
//            System.out.println(func);
//            MathFunction f_ = func.pdiff(0);
//            System.out.println(f_);
//            System.out.println(f_.call(rt, new BigDecimal(8)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        for (int i = 0; i < 40; i++) {
            int d = (int) (10f + Math.random() * 70f);
            Expression expr = RandomExprGenerator.next(d);
            System.out.println(expr.toString());
        }
    }
}
