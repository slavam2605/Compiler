package moklev.parsing;

import java.util.ArrayList;

/**
 * @author Моклев Вячеслав
 */
public class ExprList extends Token {

    public ExprList() {
        super(new ArrayList<>(), null);
    }

    public void add(Expression expression) {
        contents.add(expression);
    }

    @Override
    public String toString(String prefix) {
        if (contents.size() <= 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder(contents.get(0).toString());
            for (int i = 1; i < contents.size(); i++) {
                sb.append(", ").append(contents.get(i).toString());
            }
            return sb.toString();
        }
    }

}
