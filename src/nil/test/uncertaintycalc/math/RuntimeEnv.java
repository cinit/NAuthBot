package nil.test.uncertaintycalc.math;

import java.math.MathContext;

public final class RuntimeEnv {
    public int RESERVED_DECIMALS = 3;
    private MathContext mMathCtx;

    public RuntimeEnv(MathContext mc) {
        mMathCtx = mc;
    }

    public MathContext getMathContext() {
        return mMathCtx;
    }

    public void setMathContext(MathContext mMathCtx) {
        this.mMathCtx = mMathCtx;
    }
}
