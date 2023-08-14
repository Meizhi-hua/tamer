public class RIFFReader extends InputStream {
    private RIFFReader root;
    private long filepointer = 0;
    private String fourcc;
    private String riff_type = null;
    private long ckSize = 0;
    private InputStream stream;
    private long avail;
    private RIFFReader lastiterator = null;
    public RIFFReader(InputStream stream) throws IOException {
        if (stream instanceof RIFFReader)
            root = ((RIFFReader)stream).root;
        else
            root = this;
        this.stream = stream;
        avail = Integer.MAX_VALUE;
        ckSize = Integer.MAX_VALUE;
        int b;
        while (true) {
            b = read();
            if (b == -1) {
                fourcc = ""; 
                riff_type = null;
                avail = 0;
                return;
            }
            if (b != 0)
                break;
        }
        byte[] fourcc = new byte[4];
        fourcc[0] = (byte) b;
        readFully(fourcc, 1, 3);
        this.fourcc = new String(fourcc, "ascii");
        ckSize = readUnsignedInt();
        avail = this.ckSize;
        if (getFormat().equals("RIFF") || getFormat().equals("LIST")) {
            byte[] format = new byte[4];
            readFully(format);
            this.riff_type = new String(format, "ascii");
        }
    }
    public long getFilePointer() throws IOException {
        return root.filepointer;
    }
    public boolean hasNextChunk() throws IOException {
        if (lastiterator != null)
            lastiterator.finish();
        return avail != 0;
    }
    public RIFFReader nextChunk() throws IOException {
        if (lastiterator != null)
            lastiterator.finish();
        if (avail == 0)
            return null;
        lastiterator = new RIFFReader(this);
        return lastiterator;
    }
    public String getFormat() {
        return fourcc;
    }
    public String getType() {
        return riff_type;
    }
    public long getSize() {
        return ckSize;
    }
    public int read() throws IOException {
        if (avail == 0)
            return -1;
        int b = stream.read();
        if (b == -1)
            return -1;
        avail--;
        filepointer++;
        return b;
    }
    public int read(byte[] b, int offset, int len) throws IOException {
        if (avail == 0)
            return -1;
        if (len > avail) {
            int rlen = stream.read(b, offset, (int)avail);
            if (rlen != -1)
                filepointer += rlen;
            avail = 0;
            return rlen;
        } else {
            int ret = stream.read(b, offset, len);
            if (ret == -1)
                return -1;
            avail -= ret;
            filepointer += ret;
            return ret;
        }
    }
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }
    public final void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        while (len > 0) {
            int s = read(b, off, len);
            if (s < 0)
                throw new EOFException();
            if (s == 0)
                Thread.yield();
            off += s;
            len -= s;
        }
    }
    public final long skipBytes(long n) throws IOException {
        if (n < 0)
            return 0;
        long skipped = 0;
        while (skipped != n) {
            long s = skip(n - skipped);
            if (s < 0)
                break;
            if (s == 0)
                Thread.yield();
            skipped += s;
        }
        return skipped;
    }
    public long skip(long n) throws IOException {
        if (avail == 0)
            return -1;
        if (n > avail) {
            long len = stream.skip(avail);
            if (len != -1)
                filepointer += len;
            avail = 0;
            return len;
        } else {
            long ret = stream.skip(n);
            if (ret == -1)
                return -1;
            avail -= ret;
            filepointer += ret;
            return ret;
        }
    }
    public int available() {
        return (int)avail;
    }
    public void finish() throws IOException {
        if (avail != 0) {
            skipBytes(avail);
        }
    }
    public String readString(int len) throws IOException {
        byte[] buff = new byte[len];
        readFully(buff);
        for (int i = 0; i < buff.length; i++) {
            if (buff[i] == 0) {
                return new String(buff, 0, i, "ascii");
            }
        }
        return new String(buff, "ascii");
    }
    public byte readByte() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return (byte) ch;
    }
    public short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if (ch1 < 0)
            throw new EOFException();
        if (ch2 < 0)
            throw new EOFException();
        return (short)(ch1 | (ch2 << 8));
    }
    public int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if (ch1 < 0)
            throw new EOFException();
        if (ch2 < 0)
            throw new EOFException();
        if (ch3 < 0)
            throw new EOFException();
        if (ch4 < 0)
            throw new EOFException();
        return ch1 + (ch2 << 8) | (ch3 << 16) | (ch4 << 24);
    }
    public long readLong() throws IOException {
        long ch1 = read();
        long ch2 = read();
        long ch3 = read();
        long ch4 = read();
        long ch5 = read();
        long ch6 = read();
        long ch7 = read();
        long ch8 = read();
        if (ch1 < 0)
            throw new EOFException();
        if (ch2 < 0)
            throw new EOFException();
        if (ch3 < 0)
            throw new EOFException();
        if (ch4 < 0)
            throw new EOFException();
        if (ch5 < 0)
            throw new EOFException();
        if (ch6 < 0)
            throw new EOFException();
        if (ch7 < 0)
            throw new EOFException();
        if (ch8 < 0)
            throw new EOFException();
        return ch1 | (ch2 << 8) | (ch3 << 16) | (ch4 << 24)
                | (ch5 << 32) | (ch6 << 40) | (ch7 << 48) | (ch8 << 56);
    }
    public int readUnsignedByte() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }
    public int readUnsignedShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if (ch1 < 0)
            throw new EOFException();
        if (ch2 < 0)
            throw new EOFException();
        return ch1 | (ch2 << 8);
    }
    public long readUnsignedInt() throws IOException {
        long ch1 = read();
        long ch2 = read();
        long ch3 = read();
        long ch4 = read();
        if (ch1 < 0)
            throw new EOFException();
        if (ch2 < 0)
            throw new EOFException();
        if (ch3 < 0)
            throw new EOFException();
        if (ch4 < 0)
            throw new EOFException();
        return ch1 + (ch2 << 8) | (ch3 << 16) | (ch4 << 24);
    }
    public void close() throws IOException {
        finish();
        if (this == root)
            stream.close();
        stream = null;
    }
}