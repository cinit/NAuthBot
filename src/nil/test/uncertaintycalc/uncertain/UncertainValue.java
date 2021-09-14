package nil.test.uncertaintycalc.uncertain;

import nil.test.uncertaintycalc.Disposable;

import java.math.BigDecimal;
import java.math.MathContext;

public class UncertainValue implements Disposable {

    private BigDecimal mValue, mUncertainty;

    private int mSignificantDigit;

    public UncertainValue(BigDecimal val, BigDecimal uncertainty, int digit) {
        mValue = val;
        mUncertainty = uncertainty;
        mSignificantDigit = digit;
    }

    public UncertainValue strip(int digit) {
        if (digit < mSignificantDigit)
            throw new IllegalArgumentException("value has digit 10^" + mSignificantDigit + " but attempt to strip to 10^" + digit);
        if (mSignificantDigit == digit) return this;
        return new UncertainValue(mValue, mUncertainty, digit);
    }

    public BigDecimal getRawValue() {
        return mValue;
    }

    public BigDecimal getRawUncertainty() {
        return mUncertainty;
    }

    public int getSignificantDigit() {
        return mSignificantDigit;
    }

    public float getUncPercentWildF() {
        if (mUncertainty.equals(BigDecimal.ZERO)) return 0f;
        return new BigDecimal(100).multiply(mUncertainty).divide(mValue, MathContext.DECIMAL32).floatValue();
    }

    public String getUncPercentageStr() {
        if (mUncertainty.equals(BigDecimal.ZERO)) return "0%";
        float f = new BigDecimal(100).multiply(mUncertainty).divide(mValue, MathContext.DECIMAL32).floatValue();
        return String.format("%.1f%%", f);
    }

//    public String getDisplayValueStr() {
//
//    }
//
//    public String getDisplayUncertaintyStr() {
//
//    }
//
//    @Override
//    public String toString() {
//        return getDisplayValueStr() + MsConsts.CHAR_POS_NEG + getDisplayUncertaintyStr();
//    }

}
