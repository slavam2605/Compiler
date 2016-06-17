package moklev.parsing;

/**
 * @author Моклев Вячеслав
 */
public class PreprocessingException extends RuntimeException {
    public PreprocessingException() {
        super();
    }

    public PreprocessingException(String message) {
        super(message);
    }

    public PreprocessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreprocessingException(Throwable cause) {
        super(cause);
    }
}
