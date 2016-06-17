package moklev.parsing;

/**
 * @author Моклев Вячеслав
 */
public interface Parser {
    /**
     * Gets source string of parser or null if it was not set
     * @return source string
     */
    String getString();

    /**
     * Set new source string. Flush position to 0
     * @param string new source string
     */
    void setString(String string);

    /**
     * Get current position in source string
     * @return current position in string
     */
    int getPosition();

    /**
     * Set new position in source string
     * @throws java.lang.IndexOutOfBoundsException if
     * {@link #getString()} {@code == null || pos < 0 || pos >= }{@link #getString()}.length
     * @param pos new position in source string
     */
    void setPosition(int pos);

    /**
     * Parse source string from current position
     */
    Object parse();
}
