package moklev.parsing;

/**
 * @author Моклев Вячеслав
 */
public class TypeInstance {

    public static final TypeInstance INT = new TypeInstance("int");
    public static final TypeInstance CHAR = new TypeInstance("char");
    public static final TypeInstance PCHAR = new TypeInstance("char*");

    private static final int POINTER_SIZE = 8;

    private static int rawSizeOf(String rawType) {
        switch (rawType) {
            case "int": return 8;
            case "char": return 1;
            default: throw new IllegalArgumentException("Unknown raw type: " + rawType);
        }
    }

    private String rawType;
    private int pointerLevel;

    public TypeInstance(String name) {
        Parser parser = new EmptyParser(name);
        rawType = Parsers.parseLexem(parser);
        pointerLevel = 0;
        while (Parsers.parseSequence(parser, "*")) {
            pointerLevel++;
        }
        Parsers.skipSpaces(parser);
        if (rawType == null || parser.getPosition() < parser.getString().length()) {
            throw new IllegalArgumentException(name + " is not a valid type name");
        }
    }

    private TypeInstance(String rawType, int pointerLevel) {
        this.rawType = rawType;
        this.pointerLevel = pointerLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof TypeInstance) {
            TypeInstance other = (TypeInstance) obj;
            return other.rawType.equals(rawType) && other.pointerLevel == pointerLevel;
        } else {
            return false;
        }
    }

    public int sizeof() {
        if (pointerLevel > 0) {
            return POINTER_SIZE;
        } else {
            return rawSizeOf(rawType);
        }
    }

    public boolean pointer() {
        return pointerLevel > 0;
    }

    public TypeInstance dereference() {
        if (pointerLevel == 0) {
            throw new IllegalStateException("This type is not pointer");
        }
        return new TypeInstance(rawType, pointerLevel - 1);
    }

    public TypeInstance enreference() {
        return new TypeInstance(rawType, pointerLevel + 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(rawType);
        for (int i = 0; i < pointerLevel; i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}
