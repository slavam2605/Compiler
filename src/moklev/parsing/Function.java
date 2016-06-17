package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class Function extends Token {

    public static final int TYPE_INDEX = 0;
    public static final int ARGS_INDEX = 1;
    public static final int CODE_INDEX = 2;

    public Function(Type type, ArgsList argsList, BlockCode blockCode, String value) {
        super(Arrays.asList(type, argsList, blockCode), value);
    }

    @Override
    public String toString(String prefix) {
        return prefix + contents.get(TYPE_INDEX) + " " + value
                + '(' + contents.get(ARGS_INDEX) + ")\n"
                + contents.get(CODE_INDEX).toString(prefix);
    }
}
