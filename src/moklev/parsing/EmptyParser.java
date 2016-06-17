package moklev.parsing;

/**
 * @author Моклев Вячеслав
 */
public class EmptyParser implements Parser {
    private String string;
    private int pos;

    public EmptyParser(String s) {
        string = s;
        pos = 0;
    }

    @Override
    public String getString() {
        return string;
    }

    @Override
    public void setString(String string) {
        this.string = string;
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public void setPosition(int pos) {
        this.pos = pos;
    }

    @Override
    public Object parse() {
        return null;
    }
}
