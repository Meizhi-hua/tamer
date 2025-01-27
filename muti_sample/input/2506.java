public class CharToByteUnicode extends CharToByteConverter {
    static final char BYTE_ORDER_MARK = (char) 0xfeff;
    protected boolean usesMark = true;      
    private boolean markWritten = false;  
    static final int UNKNOWN = 0;
    static final int BIG = 1;
    static final int LITTLE = 2;
    protected int byteOrder = UNKNOWN;
    public CharToByteUnicode() {
        String enc = java.security.AccessController.doPrivileged(
           new sun.security.action.GetPropertyAction("sun.io.unicode.encoding",
                                                          "UnicodeBig"));
        if (enc.equals("UnicodeBig"))
            byteOrder = BIG;
        else if (enc.equals("UnicodeLittle"))
            byteOrder = LITTLE;
        else
            byteOrder = BIG;
    }
    public CharToByteUnicode(int byteOrder, boolean usesMark) {
        this.byteOrder = byteOrder;
        this.usesMark = usesMark;
    }
    public CharToByteUnicode(boolean usesMark) {
        this();
        this.usesMark = usesMark;
    }
    public String getCharacterEncoding() {
        switch (byteOrder) {
        case BIG:
            return usesMark ? "UnicodeBig" : "UnicodeBigUnmarked";
        case LITTLE:
            return usesMark ? "UnicodeLittle" : "UnicodeLittleUnmarked";
        default:
            return "UnicodeUnknown";
        }
    }
    public int convert(char in[], int inOff, int inEnd,
                       byte out[], int outOff, int outEnd)
        throws ConversionBufferFullException, MalformedInputException
    {
        charOff = inOff;
        byteOff = outOff;
        if (inOff >= inEnd)
            return 0;
        int inI = inOff,
            outI = outOff,
            outTop = outEnd - 2;
        if (usesMark && !markWritten) {
            if (outI > outTop)
                throw new ConversionBufferFullException();
            if (byteOrder == BIG) {
                out[outI++] = (byte) (BYTE_ORDER_MARK >> 8);
                out[outI++] = (byte) (BYTE_ORDER_MARK & 0xff);
            }
            else {
                out[outI++] = (byte) (BYTE_ORDER_MARK & 0xff);
                out[outI++] = (byte) (BYTE_ORDER_MARK >> 8);
            }
            markWritten = true;
        }
        if (byteOrder == BIG) {
            while (inI < inEnd) {
                if (outI > outTop) {
                    charOff = inI;
                    byteOff = outI;
                    throw new ConversionBufferFullException();
                }
                char c = in[inI++];
                out[outI++] = (byte) (c >> 8);
                out[outI++] = (byte) (c & 0xff);
            }
        }
        else {
            while (inI < inEnd) {
                if (outI > outTop) {
                    charOff = inI;
                    byteOff = outI;
                    throw new ConversionBufferFullException();
                }
                char c = in[inI++];
                out[outI++] = (byte) (c & 0xff);
                out[outI++] = (byte) (c >> 8);
            }
        }
        charOff = inI;
        byteOff = outI;
        return outI - outOff;
    }
    public int flush(byte in[], int inOff, int inEnd) {
        byteOff = charOff = 0;
        return 0;
    }
    public void reset () {
        byteOff = charOff = 0;
        markWritten = false;
    }
    public int getMaxBytesPerChar() {
        return 4;               
    }
}
