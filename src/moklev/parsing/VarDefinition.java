package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class VarDefinition extends Token {

    public VarDefinition(Type type, String value) {
        super(Arrays.asList(type), value);
    }

    @Override
    public String toString(String prefix) {
        return "" + contents.get(0) + ' ' + value;
    }

}
