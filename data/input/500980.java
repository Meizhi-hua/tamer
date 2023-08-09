public class OperationApplicationException extends Exception {
    private final int mNumSuccessfulYieldPoints;
    public OperationApplicationException() {
        super();
        mNumSuccessfulYieldPoints = 0;
    }
    public OperationApplicationException(String message) {
        super(message);
        mNumSuccessfulYieldPoints = 0;
    }
    public OperationApplicationException(String message, Throwable cause) {
        super(message, cause);
        mNumSuccessfulYieldPoints = 0;
    }
    public OperationApplicationException(Throwable cause) {
        super(cause);
        mNumSuccessfulYieldPoints = 0;
    }
    public OperationApplicationException(int numSuccessfulYieldPoints) {
        super();
        mNumSuccessfulYieldPoints = numSuccessfulYieldPoints;
    }
    public OperationApplicationException(String message, int numSuccessfulYieldPoints) {
        super(message);
        mNumSuccessfulYieldPoints = numSuccessfulYieldPoints;
    }
    public int getNumSuccessfulYieldPoints() {
        return mNumSuccessfulYieldPoints;
    }
}
