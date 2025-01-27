public class TeeInputStream extends ProxyInputStream {
    private final OutputStream branch;
    private final boolean closeBranch;
    public TeeInputStream(InputStream input, OutputStream branch) {
        this(input, branch, false);
    }
    public TeeInputStream(
            InputStream input, OutputStream branch, boolean closeBranch) {
        super(input);
        this.branch = branch;
        this.closeBranch = closeBranch;
    }
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (closeBranch) {
                branch.close();
            }
        }
    }
    public int read() throws IOException {
        int ch = super.read();
        if (ch != -1) {
            branch.write(ch);
        }
        return ch;
    }
    public int read(byte[] bts, int st, int end) throws IOException {
        int n = super.read(bts, st, end);
        if (n != -1) {
            branch.write(bts, st, n);
        }
        return n;
    }
    public int read(byte[] bts) throws IOException {
        int n = super.read(bts);
        if (n != -1) {
            branch.write(bts, 0, n);
        }
        return n;
    }
}
