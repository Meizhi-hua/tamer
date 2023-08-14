public class NoSuchElementException extends RuntimeException {
    private static final long serialVersionUID = 6769829250639411880L;
    public NoSuchElementException() {
        super();
    }
    public NoSuchElementException(String detailMessage) {
        super(detailMessage);
    }
}