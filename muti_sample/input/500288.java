public class Support_ASimpleReader extends Reader {
    public static final int DEFAULT_BUFFER_SIZE = 32;
    public char[] buf;
    public int pos;
    public int len;
    public boolean throwExceptionOnNextUse = false;
    public Support_ASimpleReader() {
        this("BEGIN Bla bla, some text...END");
    }
    public Support_ASimpleReader(boolean throwException) {
        this();
        throwExceptionOnNextUse = throwException;
    }
    public Support_ASimpleReader(String input) {
        buf = input.toCharArray();
        pos = 0;
        len = buf.length;
    }
    @Override
    public void close() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
    }
    @Override
    public boolean ready() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
        return len > pos;
    }
    @Override
    public int read(char[] dest, int offset, int count) throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
        int available = len - pos;
        if (available > 0) {
            int readable = (available < count ? available : count);
            System.arraycopy(buf, pos, dest, offset, readable);
            pos += readable;
            return readable;
        } else {
            return -1;
        }
    }
}
