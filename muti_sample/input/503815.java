public class LimitInputStream extends FilterInputStream {
  private long left;
  private long mark = -1;
  public LimitInputStream(InputStream in, long limit) {
    super(in);
    Preconditions.checkNotNull(in);
    Preconditions.checkArgument(limit >= 0, "limit must be non-negative");
    left = limit;
  }
  @Override public int available() throws IOException {
    return (int) Math.min(in.available(), left);
  }
  @Override public void mark(int readlimit) {
    in.mark(readlimit);
    mark = left;
  }
  @Override public int read() throws IOException {
    if (left == 0) {
      return -1;
    }
    int result = in.read();
    if (result != -1) {
      --left;
    }
    return result;
  }
  @Override public int read(byte[] b, int off, int len) throws IOException {
    if (left == 0) {
      return -1;
    }
    len = (int) Math.min(len, left);
    int result = in.read(b, off, len);
    if (result != -1) {
      left -= result;
    }
    return result;
  }
  @Override public void reset() throws IOException {
    if (!in.markSupported()) {
      throw new IOException("Mark not supported");
    }
    if (mark == -1) {
      throw new IOException("Mark not set");
    }
    in.reset();
    left = mark;
  }
  @Override public long skip(long n) throws IOException {
    n = Math.min(n, left);
    long skipped = in.skip(n);
    left -= skipped;
    return skipped;
  }
}
