public class NotSerializableException extends ObjectStreamException {
    private static final long serialVersionUID = 2906642554793891381L;
    public NotSerializableException() {
        super();
    }
    public NotSerializableException(String detailMessage) {
        super(detailMessage);
    }
}
