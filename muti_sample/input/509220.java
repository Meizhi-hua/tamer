public class CircularRedirectException extends RedirectException {
    private static final long serialVersionUID = 6830063487001091803L;
    public CircularRedirectException() {
        super();
    }
    public CircularRedirectException(String message) {
        super(message);
    }
    public CircularRedirectException(String message, Throwable cause) {
        super(message, cause);
    }
}