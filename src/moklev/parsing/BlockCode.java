package moklev.parsing;

import java.util.Arrays;

/**
 * @author Моклев Вячеслав
 */
public class BlockCode extends Token {

    public BlockCode(Token contents) {
        super(Arrays.asList(contents), null);
    }

    @Override
    public String toString(String prefix) {
        return prefix + "{\n" + contents.get(0).toString(prefix) + prefix + "}\n";
    }
}
