package nil.test.uncertaintycalc.latex;

public interface LaTeXFormula {
    boolean needBraceIn(int level);

    boolean isAtomic();

    String toLaTeX();
}
