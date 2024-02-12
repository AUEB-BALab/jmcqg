package gr.aueb.dmst.dds.jmcqg;

public class QuestionException extends RuntimeException {
    // Constructor without parameters
    public QuestionException() {
        super();
    }

    // Constructor that accepts a message
    public QuestionException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public QuestionException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public QuestionException(Throwable cause) {
        super(cause);
    }
}
