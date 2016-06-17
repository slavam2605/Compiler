package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class LineCode extends Token {

    int contentsType;

    public static final int VARDEF_TYPE = 0;
    public static final int EXPR_TYPE = 1;
    public static final int BLOCK_TYPE = 2;
    public static final int RETURN_TYPE = 3;
    public static final int IF_TYPE = 4;
    public static final int FOR_TYPE = 5;

    public LineCode(VarDefinition varDefinition) {
        super(Arrays.asList(varDefinition), null);
        contentsType = VARDEF_TYPE;
    }

    public LineCode(Expression expression) {
        super(Arrays.asList(expression), null);
        contentsType = EXPR_TYPE;
    }

    public LineCode(BlockCode blockCode) {
        super(Arrays.asList(blockCode), null);
        contentsType = BLOCK_TYPE;
    }

    public LineCode(Expression expression, int flag) {
        super(Arrays.asList(expression), null);
        contentsType = flag;
    }

    public LineCode(Expression expression, LineCode lineCode, int flag) {
        super(Arrays.asList(expression, lineCode), null);
        contentsType = flag;
    }

    public LineCode(Expression init, Expression condition, Expression iteration, LineCode lineCode) {
        super(Arrays.asList(init, condition, iteration, lineCode), null);
        contentsType = FOR_TYPE;
    }

    public String toString(String prefix) {
        switch (contentsType) {
            case IF_TYPE:
                if (get(1) instanceof LineCode) {
                    return prefix + "if (" + get(0) + ") \n" + get(1).toString(prefix + "    ");
                } else {
                    return prefix + "if (" + get(0) + ") \n" + get(1).toString(prefix);
                }
            case RETURN_TYPE: return prefix + "return " + get(0).toString() + ";\n";
            case BLOCK_TYPE: return get(0).toString(prefix);
            case FOR_TYPE:
                if (get(3) instanceof LineCode) {
                    return prefix + "for (" + get(0) + "; " + get(1) + "; " + get(2) + ")\n" + get(3).toString(prefix + "    ");
                } else {
                    return prefix + "for (" + get(0) + "; " + get(1) + "; " + get(2) + ")\n" + get(3).toString(prefix);
                }
            default: return prefix + get(0).toString(prefix) + ";\n";
        }
    }
}
