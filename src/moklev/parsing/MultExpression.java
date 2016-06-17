package moklev.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Моклев Вячеслав
 */
public class MultExpression extends TypedToken {

    List<Integer> ops;

    public static final int MUL_OP = 0;
    public static final int DIV_OP = 1;
    public static final int MOD_OP = 2;

    public MultExpression(Term expression) {
        super(Arrays.asList(expression), null);
        ops = new ArrayList<>();
        ops.add(0);
    }

    public MultExpression add(Term expression, int op) {
        ops.add(op);
        contents.add(expression);
        return this;
    }

    static char getChar(int op) {
        switch (op) {
            case MUL_OP: return '*';
            case DIV_OP: return '/';
            case MOD_OP: return '%';
            default: throw new IllegalArgumentException("unexpected op: " + op);
        }
    }

    @Override
    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder(contents.get(0).toString());
        for (int i = 1; i < contents.size(); i++) {
            sb.append(' ').append(getChar(ops.get(i))).append(' ').append(contents.get(i).toString());
        }
        return sb.toString();
    }
}
