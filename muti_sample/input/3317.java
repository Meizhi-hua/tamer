public class ParseStatus {
    int parseLength;
    int errorIndex;
    String errorMsg;
    public ParseStatus() {
        reset();
    }
    public void reset() {
        parseLength = 0;
        errorIndex = -1;
        errorMsg = null;
    }
    public boolean isError() {
        return (errorIndex >= 0);
    }
    public int getErrorIndex() {
        return errorIndex;
    }
    public int getParseLength() {
        return parseLength;
    }
    public String getErrorMessage() {
        return errorMsg;
    }
}
