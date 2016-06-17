package moklev.parsing;

import java.util.Collection;

/**
 * @author Моклев Вячеслав
 */
public class Lexem extends Token {

    public Lexem(String value) {
        super(null, value);
    }

    @Override
    public String toString(String prefix) {
        return value;
    }
}
