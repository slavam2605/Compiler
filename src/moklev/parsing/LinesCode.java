package moklev.parsing;

import java.util.ArrayList;

/**
 * @author Моклев Вячеслав
 */
public class LinesCode extends Token {

    public LinesCode() {
        super(new ArrayList<>(), null);
    }

    public void add(Token token) {
        contents.add(token);
    }

    @Override
    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        for (Token line: contents) {
            sb.append(line.toString(prefix + "    "));
        }
        return sb.toString();
    }
}
