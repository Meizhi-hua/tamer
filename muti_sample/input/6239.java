public class RIFFWriter extends OutputStream {
    private interface RandomAccessWriter {
        public void seek(long chunksizepointer) throws IOException;
        public long getPointer() throws IOException;
        public void close() throws IOException;
        public void write(int b) throws IOException;
        public void write(byte[] b, int off, int len) throws IOException;
        public void write(byte[] bytes) throws IOException;
        public long length() throws IOException;
        public void setLength(long i) throws IOException;
    }
    private static class RandomAccessFileWriter implements RandomAccessWriter {
        RandomAccessFile raf;
        public RandomAccessFileWriter(File file) throws FileNotFoundException {
            this.raf = new RandomAccessFile(file, "rw");
        }
        public RandomAccessFileWriter(String name) throws FileNotFoundException {
            this.raf = new RandomAccessFile(name, "rw");
        }
        public void seek(long chunksizepointer) throws IOException {
            raf.seek(chunksizepointer);
        }
        public long getPointer() throws IOException {
            return raf.getFilePointer();
        }
        public void close() throws IOException {
            raf.close();
        }
        public void write(int b) throws IOException {
            raf.write(b);
        }
        public void write(byte[] b, int off, int len) throws IOException {
            raf.write(b, off, len);
        }
        public void write(byte[] bytes) throws IOException {
            raf.write(bytes);
        }
        public long length() throws IOException {
            return raf.length();
        }
        public void setLength(long i) throws IOException {
            raf.setLength(i);
        }
    }
    private static class RandomAccessByteWriter implements RandomAccessWriter {
        byte[] buff = new byte[32];
        int length = 0;
        int pos = 0;
        byte[] s;
        OutputStream stream;
        public RandomAccessByteWriter(OutputStream stream) {
            this.stream = stream;
        }
        public void seek(long chunksizepointer) throws IOException {
            pos = (int) chunksizepointer;
        }
        public long getPointer() throws IOException {
            return pos;
        }
        public void close() throws IOException {
            stream.write(buff, 0, length);
            stream.close();
        }
        public void write(int b) throws IOException {
            if (s == null)
                s = new byte[1];
            s[0] = (byte)b;
            write(s, 0, 1);
        }
        public void write(byte[] b, int off, int len) throws IOException {
            int newsize = pos + len;
            if (newsize > length)
                setLength(newsize);
            int end = off + len;
            for (int i = off; i < end; i++) {
                buff[pos++] = b[i];
            }
        }
        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }
        public long length() throws IOException {
            return length;
        }
        public void setLength(long i) throws IOException {
            length = (int) i;
            if (length > buff.length) {
                int newlen = Math.max(buff.length << 1, length);
                byte[] newbuff = new byte[newlen];
                System.arraycopy(buff, 0, newbuff, 0, buff.length);
                buff = newbuff;
            }
        }
    }
    private int chunktype = 0; 
    private RandomAccessWriter raf;
    private long chunksizepointer;
    private long startpointer;
    private RIFFWriter childchunk = null;
    private boolean open = true;
    private boolean writeoverride = false;
    public RIFFWriter(String name, String format) throws IOException {
        this(new RandomAccessFileWriter(name), format, 0);
    }
    public RIFFWriter(File file, String format) throws IOException {
        this(new RandomAccessFileWriter(file), format, 0);
    }
    public RIFFWriter(OutputStream stream, String format) throws IOException {
        this(new RandomAccessByteWriter(stream), format, 0);
    }
    private RIFFWriter(RandomAccessWriter raf, String format, int chunktype)
            throws IOException {
        if (chunktype == 0)
            if (raf.length() != 0)
                raf.setLength(0);
        this.raf = raf;
        if (raf.getPointer() % 2 != 0)
            raf.write(0);
        if (chunktype == 0)
            raf.write("RIFF".getBytes("ascii"));
        else if (chunktype == 1)
            raf.write("LIST".getBytes("ascii"));
        else
            raf.write((format + "    ").substring(0, 4).getBytes("ascii"));
        chunksizepointer = raf.getPointer();
        this.chunktype = 2;
        writeUnsignedInt(0);
        this.chunktype = chunktype;
        startpointer = raf.getPointer();
        if (chunktype != 2)
            raf.write((format + "    ").substring(0, 4).getBytes("ascii"));
    }
    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }
    public long getFilePointer() throws IOException {
        return raf.getPointer();
    }
    public void setWriteOverride(boolean writeoverride) {
        this.writeoverride = writeoverride;
    }
    public boolean getWriteOverride() {
        return writeoverride;
    }
    public void close() throws IOException {
        if (!open)
            return;
        if (childchunk != null) {
            childchunk.close();
            childchunk = null;
        }
        int bakchunktype = chunktype;
        long fpointer = raf.getPointer();
        raf.seek(chunksizepointer);
        chunktype = 2;
        writeUnsignedInt(fpointer - startpointer);
        if (bakchunktype == 0)
            raf.close();
        else
            raf.seek(fpointer);
        open = false;
        raf = null;
    }
    public void write(int b) throws IOException {
        if (!writeoverride) {
            if (chunktype != 2) {
                throw new IllegalArgumentException(
                        "Only chunks can write bytes!");
            }
            if (childchunk != null) {
                childchunk.close();
                childchunk = null;
            }
        }
        raf.write(b);
    }
    public void write(byte b[], int off, int len) throws IOException {
        if (!writeoverride) {
            if (chunktype != 2) {
                throw new IllegalArgumentException(
                        "Only chunks can write bytes!");
            }
            if (childchunk != null) {
                childchunk.close();
                childchunk = null;
            }
        }
        raf.write(b, off, len);
    }
    public RIFFWriter writeList(String format) throws IOException {
        if (chunktype == 2) {
            throw new IllegalArgumentException(
                    "Only LIST and RIFF can write lists!");
        }
        if (childchunk != null) {
            childchunk.close();
            childchunk = null;
        }
        childchunk = new RIFFWriter(this.raf, format, 1);
        return childchunk;
    }
    public RIFFWriter writeChunk(String format) throws IOException {
        if (chunktype == 2) {
            throw new IllegalArgumentException(
                    "Only LIST and RIFF can write chunks!");
        }
        if (childchunk != null) {
            childchunk.close();
            childchunk = null;
        }
        childchunk = new RIFFWriter(this.raf, format, 2);
        return childchunk;
    }
    public void writeString(String string) throws IOException {
        byte[] buff = string.getBytes();
        write(buff);
    }
    public void writeString(String string, int len) throws IOException {
        byte[] buff = string.getBytes();
        if (buff.length > len)
            write(buff, 0, len);
        else {
            write(buff);
            for (int i = buff.length; i < len; i++)
                write(0);
        }
    }
    public void writeByte(int b) throws IOException {
        write(b);
    }
    public void writeShort(short b) throws IOException {
        write((b >>> 0) & 0xFF);
        write((b >>> 8) & 0xFF);
    }
    public void writeInt(int b) throws IOException {
        write((b >>> 0) & 0xFF);
        write((b >>> 8) & 0xFF);
        write((b >>> 16) & 0xFF);
        write((b >>> 24) & 0xFF);
    }
    public void writeLong(long b) throws IOException {
        write((int) (b >>> 0) & 0xFF);
        write((int) (b >>> 8) & 0xFF);
        write((int) (b >>> 16) & 0xFF);
        write((int) (b >>> 24) & 0xFF);
        write((int) (b >>> 32) & 0xFF);
        write((int) (b >>> 40) & 0xFF);
        write((int) (b >>> 48) & 0xFF);
        write((int) (b >>> 56) & 0xFF);
    }
    public void writeUnsignedByte(int b) throws IOException {
        writeByte((byte) b);
    }
    public void writeUnsignedShort(int b) throws IOException {
        writeShort((short) b);
    }
    public void writeUnsignedInt(long b) throws IOException {
        writeInt((int) b);
    }
}
