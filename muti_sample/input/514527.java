public class NotActiveException extends ObjectStreamException {
    private static final long serialVersionUID = -3893467273049808895L;
    public NotActiveException() {
        super();
    }
    public NotActiveException(String detailMessage) {
        super(detailMessage);
    }
}
