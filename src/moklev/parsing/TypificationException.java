package moklev.parsing;

/**
 * @author Моклев Вячеслав
 */
public class TypificationException extends RuntimeException {
    public TypificationException() {
        super();
    }

    public TypificationException(String message) {
        super(message);
    }

    public TypificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypificationException(Throwable cause) {
        super(cause);
    }
}
