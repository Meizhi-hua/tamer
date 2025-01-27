class AddressDataSource implements DataSource {
  AddressDataSource(Address addr) {
    this.addr = addr;
    offset = 0;
  }
  public byte readByte() throws IOException {
    try {
      byte res = (byte) addr.getCIntegerAt(offset, 1, false);
      ++offset;
      return res;
    } catch (UnmappedAddressException e) {
      throw (IOException) new IOException("Unmapped address at 0x" + Long.toHexString(e.getAddress())).initCause(e);
    } catch (DebuggerException e) {
      throw (IOException) new IOException(e.toString()).initCause(e);
    }
  }
  public short readShort() throws IOException {
    int b1 = readByte() & 0xFF;
    int b2 = readByte() & 0xFF;
    return (short) ((b1 << 8) | b2);
  }
  public int readInt() throws IOException {
    int b1 = ((int) readByte()) & 0xFF;
    int b2 = ((int) readByte()) & 0xFF;
    int b3 = ((int) readByte()) & 0xFF;
    int b4 = ((int) readByte()) & 0xFF;
    return ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
  }
  public long readLong() throws IOException {
    long b1 = ((long) readByte()) & 0xFFL;
    long b2 = ((long) readByte()) & 0xFFL;
    long b3 = ((long) readByte()) & 0xFFL;
    long b4 = ((long) readByte()) & 0xFFL;
    long b5 = ((long) readByte()) & 0xFFL;
    long b6 = ((long) readByte()) & 0xFFL;
    long b7 = ((long) readByte()) & 0xFFL;
    long b8 = ((long) readByte()) & 0xFFL;
    return (((((b1 << 24) | (b2 << 16) | (b3 << 8) | b4)) << 32) |
            ((((b5 << 24) | (b6 << 16) | (b7 << 8) | b8))));
  }
  public int read(byte[] b) throws IOException {
    for (int i = 0; i < b.length; i++) {
      b[i] = readByte();
    }
    return b.length;
  }
  public void seek(long pos) throws IOException {
    offset = pos;
  }
  public long getFilePointer() throws IOException {
    return offset;
  }
  public void close() throws IOException {
  }
  private Address addr;
  private long offset;
}
