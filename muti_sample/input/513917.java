public class Support_ASimpleInputStream extends InputStream {
    public static final int DEFAULT_BUFFER_SIZE = 32;
    public byte[] buf;
    public int pos;
    public int len;
    public boolean throwExceptionOnNextUse = false;
    public Support_ASimpleInputStream() {
        this("BEGIN Bla bla, some text...END");
    }
    public Support_ASimpleInputStream(boolean throwException) {
        this();
        throwExceptionOnNextUse = throwException;
    }
    public Support_ASimpleInputStream(String input) {
        buf = input.getBytes();
        pos = 0;
        len = buf.length;
    }
    public Support_ASimpleInputStream(byte[] input) {
        pos = 0;
        len = input.length;
        buf = new byte[len];
        System.arraycopy(input, 0, buf, 0, len);
    }
    @Override
    public void close() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
    }
    @Override
    public int available() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
        return len - pos;
    }
    @Override
    public int read() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
        if (pos < len) {
            int res = buf[pos];
            pos++;
            return res;
        } else {
            return -1;
        }
    }
}
