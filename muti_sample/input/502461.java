public class Support_ASimpleOutputStream extends OutputStream {
    public static final int DEFAULT_BUFFER_SIZE = 32;
    public byte[] buf;
    public int pos;
    public int size;
    public boolean throwExceptionOnNextUse = false;
    public Support_ASimpleOutputStream() {
        this(DEFAULT_BUFFER_SIZE);
    }
    public Support_ASimpleOutputStream(boolean throwException) {
        this(DEFAULT_BUFFER_SIZE);
        throwExceptionOnNextUse = throwException;
    }
    public Support_ASimpleOutputStream(int bufferSize) {
        buf = new byte[bufferSize];
        pos = 0;
        size = bufferSize;
    }
    @Override
    public void close() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
    }
    @Override
    public void flush() throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
    }
    @Override
    public void write(int oneByte) throws IOException {
        if (throwExceptionOnNextUse) {
            throw new IOException("Exception thrown for testing purpose.");
        }
        if (pos < size) {
            buf[pos] = (byte)(oneByte & 255);
            pos++;
        } else {
            throw new IOException("Internal buffer overflow.");
        }
    }
    public byte[] toByteArray() {
        byte[] toReturn = new byte[pos];
        System.arraycopy(buf, 0, toReturn, 0, pos);
        return toReturn;
    }
    public String toString() {
        return new String(buf, 0, pos);
    }
}
