package nil.test.uncertaintycalc.math;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {CONSTRUCTOR, METHOD, PACKAGE, PARAMETER, TYPE})
public @interface PrecisionLoss {
}
