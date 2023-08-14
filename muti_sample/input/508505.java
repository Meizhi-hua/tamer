public class MalformedChallengeException extends ProtocolException {
    private static final long serialVersionUID = 814586927989932284L;
    public MalformedChallengeException() {
        super();
    }
    public MalformedChallengeException(String message) {
        super(message);
    }
    public MalformedChallengeException(String message, Throwable cause) {
        super(message, cause);
    }
}