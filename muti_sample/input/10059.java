public class PCK
    extends Charset
    implements HistoricallyNamedCharset
{
    public PCK() {
        super("x-PCK", ExtendedCharsets.aliasesFor("x-PCK"));
    }
    public String historicalName() {
        return "PCK";
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof JIS_X_0201)
                || (cs instanceof PCK));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        byte[] replacementBytes = { (byte)0x3f };
        return new Encoder(this).replaceWith(replacementBytes);
    }
    private static class Decoder extends SJIS.Decoder {
        JIS_X_0208_Solaris_Decoder jis0208;
        private static final char REPLACE_CHAR='\uFFFD';
        private Decoder(Charset cs) {
            super(cs);
            jis0208 = new JIS_X_0208_Solaris_Decoder(cs);
        }
        protected char decodeDouble(int c1, int c2) {
            char outChar;
            if ((outChar = super.decodeDouble(c1, c2)) != '\uFFFD')  {
                return ((outChar != '\u2014')? outChar: '\u2015');
            } else {
                int adjust = c2 < 0x9F ? 1 : 0;
                int rowOffset = c1 < 0xA0 ? 0x70 : 0xB0;
                int cellOffset = (adjust == 1) ? (c2 > 0x7F ? 0x20 : 0x1F) : 0x7E;
                int b1 = ((c1 - rowOffset) << 1) - adjust;
                int b2 = c2 - cellOffset;
                char outChar2 = jis0208.decodeDouble(b1, b2);
                return outChar2;
            }
        }
    }
    private static class Encoder extends SJIS.Encoder {
        private JIS_X_0201.Encoder jis0201;
        private static final short[] j0208Index1 =
            JIS_X_0208_Solaris_Encoder.getIndex1();
        private static final String[] j0208Index2 =
            JIS_X_0208_Solaris_Encoder.getIndex2();
        private Encoder(Charset cs) {
            super(cs);
            jis0201 = new JIS_X_0201.Encoder(cs);
        }
        protected int encodeDouble(char ch) {
            int result = 0;
            switch (ch) {
                case '\u2015':
                    return 0x815C;
                case '\u2014':
                    return 0;
                default:
                    break;
            }
            if ((result = super.encodeDouble(ch)) != 0) {
                return result;
            }
            else {
                int offset = j0208Index1[ch >> 8] << 8;
                int pos = j0208Index2[offset >> 12].charAt((offset & 0xfff) + (ch & 0xff));
                if (pos != 0) {
                int c1 = (pos >> 8) & 0xff;
                int c2 = pos & 0xff;
                int rowOffset = c1 < 0x5F ? 0x70 : 0xB0;
                int cellOffset = (c1 % 2 == 1) ? (c2 > 0x5F ? 0x20 : 0x1F) : 0x7E;
                result = ((((c1 + 1 ) >> 1) + rowOffset) << 8) | (c2 + cellOffset);
                }
            }
            return result;
        }
    }
}
