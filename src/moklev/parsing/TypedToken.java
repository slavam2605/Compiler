package moklev.parsing;

import java.util.Collection;

/**
 * @author Моклев Вячеслав
 */
public abstract class TypedToken extends Token {

    TypeInstance typeInstance;

    public TypedToken(Collection<? extends Token> contents, String value) {
        super(contents, value);
        typeInstance = null;
    }

}
