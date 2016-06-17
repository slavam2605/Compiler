package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class FunctionDefinition extends Token {

    public static final int TYPE_INDEX = 0;
    public static final int ARGS_INDEX = 1;

    public FunctionDefinition(Type type, ArgsList argsList, String value) {
        super(Arrays.asList(type, argsList), value);
    }

    @Override
    public String toString(String prefix) {
        return prefix + contents.get(TYPE_INDEX) + " " + value
                + '(' + contents.get(ARGS_INDEX) + ");\n";
    }
}
