public class IllegalArgumentException extends RuntimeException {
    private static final long serialVersionUID = -5365630128856068164L;
    public IllegalArgumentException() {
        super();
    }
    public IllegalArgumentException(String detailMessage) {
        super(detailMessage);
    }
    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
    public IllegalArgumentException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}
