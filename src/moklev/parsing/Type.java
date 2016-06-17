package moklev.parsing;

import java.util.Collection;

/**
 * @author Моклев Вячеслав
 */
public class Type extends Token {

    public Type(String value) {
        super(null, value);
    }

    @Override
    public String toString(String prefix) {
        return value;
    }

}
