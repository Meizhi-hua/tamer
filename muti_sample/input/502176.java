public class ConnectionPoolTimeoutException extends ConnectTimeoutException {
    private static final long serialVersionUID = -7898874842020245128L;
    public ConnectionPoolTimeoutException() {
        super();
    }
    public ConnectionPoolTimeoutException(String message) {
        super(message);
    }
}