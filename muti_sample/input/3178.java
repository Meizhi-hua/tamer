abstract class UnicodeDecoder extends CharsetDecoder {
    protected static final char BYTE_ORDER_MARK = (char) 0xfeff;
    protected static final char REVERSED_MARK = (char) 0xfffe;
    protected static final int NONE = 0;
    protected static final int BIG = 1;
    protected static final int LITTLE = 2;
    private final int expectedByteOrder;
    private int currentByteOrder;
    private int defaultByteOrder = BIG;
    public UnicodeDecoder(Charset cs, int bo) {
        super(cs, 0.5f, 1.0f);
        expectedByteOrder = currentByteOrder = bo;
    }
    public UnicodeDecoder(Charset cs, int bo, int defaultBO) {
        this(cs, bo);
        defaultByteOrder = defaultBO;
    }
    private char decode(int b1, int b2) {
        if (currentByteOrder == BIG)
            return (char)((b1 << 8) | b2);
        else
            return (char)((b2 << 8) | b1);
    }
    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        int mark = src.position();
        try {
            while (src.remaining() > 1) {
                int b1 = src.get() & 0xff;
                int b2 = src.get() & 0xff;
                if (currentByteOrder == NONE) {
                    char c = (char)((b1 << 8) | b2);
                    if (c == BYTE_ORDER_MARK) {
                        currentByteOrder = BIG;
                        mark += 2;
                        continue;
                    } else if (c == REVERSED_MARK) {
                        currentByteOrder = LITTLE;
                        mark += 2;
                        continue;
                    } else {
                        currentByteOrder = defaultByteOrder;
                    }
                }
                char c = decode(b1, b2);
                if (c == REVERSED_MARK) {
                    return CoderResult.malformedForLength(2);
                }
                if (Character.isSurrogate(c)) {
                    if (Character.isHighSurrogate(c)) {
                        if (src.remaining() < 2)
                            return CoderResult.UNDERFLOW;
                        char c2 = decode(src.get() & 0xff, src.get() & 0xff);
                        if (!Character.isLowSurrogate(c2))
                            return CoderResult.malformedForLength(4);
                        if (dst.remaining() < 2)
                            return CoderResult.OVERFLOW;
                        mark += 4;
                        dst.put(c);
                        dst.put(c2);
                        continue;
                    }
                    return CoderResult.malformedForLength(2);
                }
                if (!dst.hasRemaining())
                    return CoderResult.OVERFLOW;
                mark += 2;
                dst.put(c);
            }
            return CoderResult.UNDERFLOW;
        } finally {
            src.position(mark);
        }
    }
    protected void implReset() {
        currentByteOrder = expectedByteOrder;
    }
}
