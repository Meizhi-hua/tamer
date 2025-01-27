public class SQLTimeoutException extends SQLTransientException {
        public SQLTimeoutException() {
                super();
        }
        public SQLTimeoutException(String reason) {
                super(reason);
        }
        public SQLTimeoutException(String reason, String SQLState) {
                super(reason, SQLState);
        }
        public SQLTimeoutException(String reason, String SQLState, int vendorCode) {
                super(reason, SQLState, vendorCode);
        }
    public SQLTimeoutException(Throwable cause) {
        super(cause);
    }
    public SQLTimeoutException(String reason, Throwable cause) {
        super(reason, cause);
    }
    public SQLTimeoutException(String reason, String SQLState, Throwable cause) {
        super(reason, SQLState, cause);
    }
    public SQLTimeoutException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
    }
    private static final long serialVersionUID = -4487171280562520262L;
}
