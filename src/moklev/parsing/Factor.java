package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class Factor extends TypedToken {

    public static final int EXPR = 0;
    public static final int VAR = 1;
    public static final int NUMBER = 2;
    public static final int CHAR = 3;
    public static final int STRING = 4;
    public static final int FUNCTION = 5;

    int mode;

    public Factor(String value) {
        super(null, value);
        if (Parsers.isDigit(value.charAt(0))) {
            mode = NUMBER;
        } else {
            mode = VAR;
        }
    }

    public Factor(Expression expression) {
        super(Arrays.asList(expression), null);
        mode = EXPR;
    }

    public Factor(ExprList exprList, String lexem) {
        super(Arrays.asList(exprList), lexem);
        mode = FUNCTION;
    }

    public Factor(char c) {
        super(null, "" + c);
        mode = CHAR;
    }

    public Factor(String string, boolean isString) {
        super(null, string);
        if (!isString) {
            throw new ParsingException("Marker argument is not true");
        }
        mode = STRING;
    }

    @Override
    public String toString(String prefix) {
        switch (mode) {
            case EXPR:
                return "(" + contents.get(0) + ")";
            case FUNCTION:
                return value + '(' + contents.get(0) + ')';
            case CHAR:
                return "'" + value + "'";
            case STRING:
                return "\"" + value + "\"";
            case NUMBER:
            case VAR:
                return value;
            default:
                throw new ParsingException("Unknown mode: " + mode);
        }
    }

}
