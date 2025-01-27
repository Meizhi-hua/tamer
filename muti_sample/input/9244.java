public class SJIS
    extends Charset
    implements HistoricallyNamedCharset
{
    public SJIS() {
        super("Shift_JIS", ExtendedCharsets.aliasesFor("Shift_JIS"));
    }
    public String historicalName() {
        return "SJIS";
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof JIS_X_0201)
                || (cs instanceof SJIS)
                || (cs instanceof JIS_X_0208));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        byte[] replacementBytes = { (byte)0x3f };
        return new Encoder(this).replaceWith(replacementBytes);
    }
    static class Decoder extends JIS_X_0208_Decoder
        implements DelegatableDecoder {
        JIS_X_0201.Decoder jis0201;
        protected Decoder(Charset cs) {
            super(cs);
            jis0201 = new JIS_X_0201.Decoder(cs);
        }
        protected char decodeSingle(int b) {
            if ((b & 0xFF80) == 0) {
                return (char)b;
            }
            return jis0201.decode(b);
        }
        protected char decodeDouble(int c1, int c2) {
            int adjust = c2 < 0x9F ? 1 : 0;
            int rowOffset = c1 < 0xA0 ? 0x70 : 0xB0;
            int cellOffset = (adjust == 1) ? (c2 > 0x7F ? 0x20 : 0x1F) : 0x7E;
            int b1 = ((c1 - rowOffset) << 1) - adjust;
            int b2 = c2 - cellOffset;
            return super.decodeDouble(b1, b2);
        }
        public CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
            return super.decodeLoop(src, dst);
        }
        public void implReset() {
            super.implReset();
        }
        public CoderResult implFlush(CharBuffer out) {
            return super.implFlush(out);
        }
    }
    static class Encoder extends JIS_X_0208_Encoder {
        private JIS_X_0201.Encoder jis0201;
        private static final short[] j0208Index1 =
            JIS_X_0208_Encoder.getIndex1();
        private static final String[] j0208Index2 =
            JIS_X_0208_Encoder.getIndex2();
        protected Encoder(Charset cs) {
            super(cs);
            jis0201 = new JIS_X_0201.Encoder(cs);
        }
        protected int encodeSingle(char inputChar) {
            byte b;
            if ((inputChar & 0xFF80) == 0)
                return (byte)inputChar;
            if ((b = jis0201.encode(inputChar)) == 0)
                return -1;
            else
                return b;
        }
        protected int encodeDouble(char ch) {
            int offset = j0208Index1[ch >> 8] << 8;
            int pos = j0208Index2[offset >> 12].charAt((offset & 0xfff) + (ch & 0xff));
            if (pos == 0) {
                return 0;
            }
            int c1 = (pos >> 8) & 0xff;
            int c2 = pos & 0xff;
            int rowOffset = c1 < 0x5F ? 0x70 : 0xB0;
            int cellOffset = (c1 % 2 == 1) ? (c2 > 0x5F ? 0x20 : 0x1F) : 0x7E;
            return ((((c1 + 1 ) >> 1) + rowOffset) << 8) | (c2 + cellOffset);
        }
    }
}
