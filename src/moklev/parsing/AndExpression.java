package moklev.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Моклев Вячеслав
 */
public class AndExpression extends TypedToken {

    List<Integer> ops;

    public static final int AND_OP = 0;

    public AndExpression(EqualExpression expression) {
        super(Arrays.asList(expression), null);
        ops = new ArrayList<>();
        ops.add(0);
    }

    public AndExpression add(EqualExpression expression, int op) {
        ops.add(op);
        contents.add(expression);
        return this;
    }

    static String getString(int op) {
        switch (op) {
            case AND_OP: return "&&";
            default: throw new IllegalArgumentException("unexpected op: " + op);
        }
    }

    @Override
    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder(contents.get(0).toString());
        for (int i = 1; i < contents.size(); i++) {
            sb.append(' ').append(getString(ops.get(i))).append(' ').append(contents.get(i).toString());
        }
        return sb.toString();
    }
}
