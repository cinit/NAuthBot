package nil.test.uncertaintycalc;

public interface Disposable {
    default void makeFinal() {
    }

    default boolean isFinal() {
        return true;
    }
}
