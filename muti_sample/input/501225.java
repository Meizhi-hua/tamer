public class ComparisonFailure extends AssertionFailedError {
    private junit.framework.ComparisonFailure mComparison;
    public ComparisonFailure(String message, String expected, String actual) {
        mComparison = new junit.framework.ComparisonFailure(message, expected, actual);
    }
    public String getMessage() {
        return mComparison.getMessage();
    }
}
