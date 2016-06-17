package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class Expression extends TypedToken {

    public static final int SIMPLE = 0;
    public static final int ASSIGN = 1;

    int mode;

    public Expression(OrExpression expression) {
        super(Arrays.asList(expression), null);
        mode = SIMPLE;
    }

    public Expression(Term term, Expression expression) {
        super(Arrays.asList(term, expression), null);
        mode = ASSIGN;
    }

    @Override
    public String toString(String prefix) {
        switch (mode) {
            case SIMPLE:
                return "" + contents.get(0);
            case ASSIGN:
                return "" + contents.get(0) + " = " + contents.get(1);
            default:
                throw new ParsingException("Unknown mode: " + mode);
        }
    }

}
