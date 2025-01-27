class ChunkedInputStream extends LeftOverInputStream {
    ChunkedInputStream (ExchangeImpl t, InputStream src) {
        super (t, src);
    }
    private int remaining;
    private boolean needToReadHeader = true;
    static char CR = '\r';
    static char LF = '\n';
    private int numeric (char[] arr, int nchars) throws IOException {
        assert arr.length >= nchars;
        int len = 0;
        for (int i=0; i<nchars; i++) {
            char c = arr[i];
            int val=0;
            if (c>='0' && c <='9') {
                val = c - '0';
            } else if (c>='a' && c<= 'f') {
                val = c - 'a' + 10;
            } else if (c>='A' && c<= 'F') {
                val = c - 'A' + 10;
            } else {
                throw new IOException ("invalid chunk length");
            }
            len = len * 16 + val;
        }
        return len;
    }
    private int readChunkHeader () throws IOException {
        boolean gotCR = false;
        int c;
        char[] len_arr = new char [16];
        int len_size = 0;
        boolean end_of_len = false;
        while ((c=in.read())!= -1) {
            char ch = (char) c;
            if (len_size == len_arr.length -1) {
                throw new IOException ("invalid chunk header");
            }
            if (gotCR) {
                if (ch == LF) {
                    int l = numeric (len_arr, len_size);
                    return l;
                } else {
                    gotCR = false;
                }
                if (!end_of_len) {
                    len_arr[len_size++] = ch;
                }
            } else {
                if (ch == CR) {
                    gotCR = true;
                } else if (ch == ';') {
                    end_of_len = true;
                } else if (!end_of_len) {
                    len_arr[len_size++] = ch;
                }
            }
        }
        throw new IOException ("end of stream reading chunk header");
    }
    protected int readImpl (byte[]b, int off, int len) throws IOException {
        if (eof) {
            return -1;
        }
        if (needToReadHeader) {
            remaining = readChunkHeader();
            if (remaining == 0) {
                eof = true;
                consumeCRLF();
                t.getServerImpl().requestCompleted (t.getConnection());
                return -1;
            }
            needToReadHeader = false;
        }
        if (len > remaining) {
            len = remaining;
        }
        int n = in.read(b, off, len);
        if (n > -1) {
            remaining -= n;
        }
        if (remaining == 0) {
            needToReadHeader = true;
            consumeCRLF();
        }
        return n;
    }
    private void consumeCRLF () throws IOException {
        char c;
        c = (char)in.read(); 
        if (c != CR) {
            throw new IOException ("invalid chunk end");
        }
        c = (char)in.read(); 
        if (c != LF) {
            throw new IOException ("invalid chunk end");
        }
    }
    public int available () throws IOException {
        if (eof || closed) {
            return 0;
        }
        int n = in.available();
        return n > remaining? remaining: n;
    }
    public boolean isDataBuffered () throws IOException {
        assert eof;
        return in.available() > 0;
    }
    public boolean markSupported () {return false;}
    public void mark (int l) {
    }
    public void reset () throws IOException {
        throw new IOException ("mark/reset not supported");
    }
}
