package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class Term extends TypedToken {

    public static final int FACTOR = 0;
    public static final int DEREFERENCE = 1;
    public static final int CAST = 2;
    public static final int GET = 3;
    public static final int NOT = 4;
    public static final int ENREFERENCE = 5;

    int mode;

    public Term(Term term) {
        super(Arrays.asList(term), null);
        mode = DEREFERENCE;
    }

    public Term(Term term, int mode) {
        super(Arrays.asList(term), null);
        this.mode = mode;
    }

    public Term(Factor factor) {
        super(Arrays.asList(factor), null);
        mode = FACTOR;
    }

    public Term(Type type, Term term) {
        super(Arrays.asList(type, term), null);
        mode = CAST;
    }

    public Term(Term term, Expression expression) {
        super(Arrays.asList(term, expression), null);
        mode = GET;
    }

    @Override
    public String toString(String prefix) {
        switch (mode) {
            case DEREFERENCE:
                return "*" + contents.get(0);
            case CAST:
                return "(" + contents.get(0) + ") " + contents.get(1);
            case FACTOR:
                return "" + contents.get(0);
            case GET:
                return "" + contents.get(0) + "[" + contents.get(1) + "]";
            case NOT:
                return "!" + contents.get(0);
            case ENREFERENCE:
                return "&" + contents.get(0);
            default:
                throw new ParsingException("Unknown mode: " + mode);
        }
    }

}
