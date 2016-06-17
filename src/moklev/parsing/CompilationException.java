package moklev.parsing;

/**
 * @author Моклев Вячеслав
 */
public class CompilationException extends RuntimeException {
    public CompilationException() {
        super();
    }

    public CompilationException(String message) {
        super(message);
    }

    public CompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilationException(Throwable cause) {
        super(cause);
    }
}
