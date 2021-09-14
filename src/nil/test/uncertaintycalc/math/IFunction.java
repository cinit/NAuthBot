package nil.test.uncertaintycalc.math;

public interface IFunction {

    Object call(RuntimeEnv ctx, Object... argv) throws Exception;

    int getArgumentCount();

    int[] getArgumentTypes();

    String[] getArgumentNames();

    String getName();

    boolean isVolatile();
}
