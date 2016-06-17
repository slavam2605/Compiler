package moklev.parsing;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Моклев Вячеслав
 */
public class ArgsList extends Token {

    public ArgsList(Collection<? extends Token> contents) {
        super(contents, null);
    }

    public ArgsList() {
        this(new ArrayList<>());
    }

    public void add(Type type, String lexem) {
        contents.add(type);
        contents.add(new Lexem(lexem));
    }

    @Override
    public String toString(String prefix) {
        if (contents.size() == 2) {
            return contents.get(0) + " " + contents.get(1);
        } else if (contents.size() > 2) {
            StringBuilder sb = new StringBuilder(contents.get(0) + " " + contents.get(1));
            for (int i = 1; i < contents.size() / 2; i++) {
                sb.append(", ").append(contents.get(2 * i))
                    .append(' ').append(contents.get(2 * i + 1));
            }
            return sb.toString();
        }
        return "";
    }

}
