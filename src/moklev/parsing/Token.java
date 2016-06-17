package moklev.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Моклев Вячеслав
 */
public abstract class Token {
    protected List<Token> contents;
    protected String value;

    public Token(Collection<? extends Token> contents, String value) {
        this.contents = contents != null ? new ArrayList<>(contents) : null;
        this.value = value;
    }

    public List<Token> getContents() {
        return contents;
    }

    public String getValue() {
        return value;
    }

    public Token get(int index) {
        return contents.get(index);
    }

    public abstract String toString(String prefix);

    @Override
    public String toString() {
        return toString("");
    }

}
