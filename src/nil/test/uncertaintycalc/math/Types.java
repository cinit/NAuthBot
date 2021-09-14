package nil.test.uncertaintycalc.math;

public final class Types {
    public static final int
            PRIORITY_BRACE = 1000,
            PRIORITY_CONST_OR_VARIABLE = 800,
            PRIORITY_SINGLE_OPERAND = 360,
            PRIORITY_POWER = 340,
            PRIORITY_MUL_OR_DIV = 320,
            PRIORITY_ADD_OR_MIN = 300,
            PRIORITY_SHIFT = 280,
            PRIORITY_MEASURE = 260,
            PRIORITY_EQUALITY = 240,
            PRIORITY_BAND = 220,
            PRIORITY_BOR = 200,
            PRIORITY_XOR = 180,
            PRIORITY_AND = 160,
            PRIORITY_OR = 140,
            PRIORITY_CONDOP = 120,
            PRIORITY_ASSIGN_OR_MIXED = 100,
            PRIORITY_COMMA = 80,
            PRIORITY_SEMICOLON = 40,
            PRIORITY_NULL = 0;

    public static final int TYPE_ANY = 0;
    public static final int TYPE_NUMERIC = 1;
    public static final int TYPE_STRING = 2;
    public static final int TYPE_FUNCTION = 3;
    public static final int TYPE_EXPRESSION = 4;
}
