public class NoSuchFieldError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -3456430195886129035L;
    public NoSuchFieldError() {
        super();
    }
    public NoSuchFieldError(String detailMessage) {
        super(detailMessage);
    }
}