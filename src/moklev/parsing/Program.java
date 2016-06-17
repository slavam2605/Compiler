package moklev.parsing;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Моклев Вячеслав
 */
public class Program extends Token {

    public static final int FUNCTION = 0;
    public static final int FUNCDEF = 1;

    List<Integer> modes;

    public Program() {
        super(new ArrayList<>(), null);
        modes = new ArrayList<>();
    }

    public void add(Function function) {
        contents.add(function);
        modes.add(FUNCTION);
    }

    public void add(FunctionDefinition definition) {
        contents.add(definition);
        modes.add(FUNCDEF);
    }

    @Override
    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        for (Token token: contents) {
            sb.append(token.toString(prefix)).append('\n');
        }
        return sb.toString();
    }

}
