public class InputLexer {
  public InputLexer(BufferedInputStream in) throws IOException {
    this.in = in;
    pushedBack = false;
  }
  public void close() throws IOException {
    in.close();
  }
  public boolean parseBoolean() throws IOException {
    int val = parseInt();
    return (val != 0);
  }
  public int parseInt() throws IOException {
    long l = parseLong();
    long mask = 0xFFFFFFFF00000000L;
    if ((l & mask) != 0) {
      throw new IOException("Overflow error reading int from debug server (read " + l + ")");
    }
    return (int) l;
  }
  public long parseLong() throws IOException {
    skipWhitespace();
    byte b = readByte();
    if (!Character.isDigit((char) b)) {
      error();
    }
    long l = 0;
    while (Character.isDigit((char) b)) {
      l *= 10;
      l += (b - '0');
      b = readByte();
    }
    pushBack(b);
    return l;
  }
  public long parseAddress() throws IOException {
    skipWhitespace();
    byte b;
    if ((b = readByte()) != '0') {
      error();
    }
    b = readByte();
    if (b != 'x') {
      error();
    }
    long val = 0;
    while (isHexDigit((char) (b = readByte()))) {
      val *= 16;
      val += Character.digit((char) b, 16);
    }
    pushBack(b);
    return val;
  }
  public void skipByte() throws IOException {
    readByte();
  }
  public byte readByte() throws IOException {
    if (pushedBack) {
      pushedBack = false;
      return backBuf;
    }
    return readByteInternal();
  }
  public void readBytes(byte[] buf, int off, int len) throws IOException {
    int startIdx = off;
    int numRead = 0;
    if (pushedBack) {
      buf[startIdx] = backBuf;
      pushedBack = false;
      ++startIdx;
      ++numRead;
    }
    while (numRead < len) {
      numRead += in.read(buf, startIdx + numRead, len - numRead);
    }
  }
  public char readChar() throws IOException {
    int hi = ((int) readByte()) & 0xFF;
    int lo = ((int) readByte()) & 0xFF;
    return (char) ((hi << 8) | lo);
  }
  public long readUnsignedInt() throws IOException {
    long b1 = ((long) readByte()) & 0xFF;
    long b2 = ((long) readByte()) & 0xFF;
    long b3 = ((long) readByte()) & 0xFF;
    long b4 = ((long) readByte()) & 0xFF;
    return ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
  }
  public String readByteString(int len) throws IOException {
    byte[] b = new byte[len];
    for (int i = 0; i < len; i++) {
      b[i] = readByte();
    }
    try {
      return new String(b, "US-ASCII");
    }
    catch (UnsupportedEncodingException e) {
      throw new IOException(e.toString());
    }
  }
  public String readCharString(int len) throws IOException {
    char[] c = new char[len];
    for (int i = 0; i < len; i++) {
      c[i] = readChar();
    }
    return new String(c);
  }
  private void skipWhitespace() throws IOException {
    byte b;
    while (Character.isWhitespace((char) (b = readByte()))) {
    }
    pushBack(b);
  }
  private boolean isHexDigit(char c) {
    return (('0' <= c && c <= '9') ||
            ('a' <= c && c <= 'f') ||
            ('A' <= c && c <= 'F'));
  }
  private void pushBack(byte b) {
    if (pushedBack) {
      throw new InternalError("Only one character pushback supported");
    }
    backBuf = b;
    pushedBack = true;
  }
  private byte readByteInternal() throws IOException {
    int i = in.read();
    if (i == -1) {
      throw new IOException("End-of-file reached while reading from server");
    }
    return (byte) i;
  }
  private void error() throws IOException {
    throw new IOException("Error parsing output of debug server");
  }
  private BufferedInputStream in;
  private boolean pushedBack;
  private byte backBuf;
}
