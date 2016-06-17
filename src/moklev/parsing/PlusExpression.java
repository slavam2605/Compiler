package moklev.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Моклев Вячеслав
 */
public class PlusExpression extends TypedToken {

    List<Boolean> signs;

    public PlusExpression(MultExpression expression) {
        super(Arrays.asList(expression), null);
        signs = new ArrayList<>();
        signs.add(false);
    }

    public PlusExpression add(MultExpression expression, boolean isPlus) {
        signs.add(isPlus);
        contents.add(expression);
        return this;
    }

    @Override
    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder(contents.get(0).toString());
        for (int i = 1; i < contents.size(); i++) {
            sb.append(signs.get(i) ? " + " : " - ").append(contents.get(i).toString());
        }
        return sb.toString();
    }

}
