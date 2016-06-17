package moklev.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Моклев Вячеслав
 */
public class CompareExpression extends TypedToken {

    List<Integer> ops;

    public static final int GR_OP = 0;
    public static final int LS_OP = 1;
    public static final int GRE_OP = 2;
    public static final int LSE_OP = 3;

    public CompareExpression(PlusExpression plusExpression) {
        super(Arrays.asList(plusExpression), null);
        ops = new ArrayList<>();
        ops.add(0);
    }

    public CompareExpression add(PlusExpression expression, int op) {
        ops.add(op);
        contents.add(expression);
        return this;
    }

    static String getString(int op) {
        switch (op) {
            case GR_OP: return ">";
            case LS_OP: return "<";
            case GRE_OP: return ">=";
            case LSE_OP: return "<=";
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
