public class EUC_JP_Open
    extends Charset
    implements HistoricallyNamedCharset
{
    public EUC_JP_Open() {
        super("x-eucJP-Open", ExtendedCharsets.aliasesFor("x-eucJP-Open"));
    }
    public String historicalName() {
        return "EUC_JP_Solaris";
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof JIS_X_0201)
                || (cs instanceof EUC_JP));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        byte[] replacementBytes = { (byte)0x3f };
        return new Encoder(this).replaceWith(replacementBytes);
    }
    private static class Decoder extends EUC_JP.Decoder {
        JIS_X_0201.Decoder decoderJ0201;
        JIS_X_0212_Solaris_Decoder decodeMappingJ0212;
        JIS_X_0208_Solaris_Decoder decodeMappingJ0208;
        private static final short[] j0208Index1 =
          JIS_X_0208_Solaris_Decoder.getIndex1();
        private static final String[] j0208Index2 =
          JIS_X_0208_Solaris_Decoder.getIndex2();
        private static final int start = 0xa1;
        private static final int end = 0xfe;
        protected final char REPLACE_CHAR='\uFFFD';
        private Decoder(Charset cs) {
            super(cs);
            decoderJ0201 = new JIS_X_0201.Decoder(cs);
            decodeMappingJ0212 = new JIS_X_0212_Solaris_Decoder(cs);
        }
        protected char decode0212(int byte1, int byte2) {
             return decodeMappingJ0212.decodeDouble(byte1, byte2);
        }
        protected char decodeDouble(int byte1, int byte2) {
            if (byte1 == 0x8e) {
                return decoderJ0201.decode(byte2 - 256);
            }
            if (((byte1 < 0)
                || (byte1 > j0208Index1.length))
                || ((byte2 < start)
                || (byte2 > end)))
                return REPLACE_CHAR;
            char result = super.decodeDouble(byte1, byte2);
            if (result != '\uFFFD') {
                return result;
            } else {
                int n = (j0208Index1[byte1 - 0x80] & 0xf) *
                        (end - start + 1)
                        + (byte2 - start);
                return j0208Index2[j0208Index1[byte1 - 0x80] >> 4].charAt(n);
            }
        }
    }
    private static class Encoder extends EUC_JP.Encoder {
        JIS_X_0201.Encoder encoderJ0201;
        JIS_X_0212_Solaris_Encoder encoderJ0212;
        private static final short[] j0208Index1 =
            JIS_X_0208_Solaris_Encoder.getIndex1();
        private static final String[] j0208Index2 =
            JIS_X_0208_Solaris_Encoder.getIndex2();
        private final Surrogate.Parser sgp = new Surrogate.Parser();
        private Encoder(Charset cs) {
            super(cs);
            encoderJ0201 = new JIS_X_0201.Encoder(cs);
            encoderJ0212 = new JIS_X_0212_Solaris_Encoder(cs);
        }
        protected int encodeSingle(char inputChar, byte[] outputByte) {
            byte b;
            if (inputChar == 0) {
                outputByte[0] = (byte)0;
                return 1;
            }
            if ((b = encoderJ0201.encode(inputChar)) == 0)
                return 0;
            if (b > 0 && b < 128) {
                outputByte[0] = b;
                return 1;
            }
            outputByte[0] = (byte)0x8e;
            outputByte[1] = b;
            return 2;
        }
        protected int encodeDouble(char ch) {
            int r = super.encodeDouble(ch);
            if (r != 0) {
                return r;
            }
            else {
                int offset = j0208Index1[((ch & 0xff00) >> 8 )] << 8;
                r = j0208Index2[offset >> 12].charAt((offset & 0xfff) +
                    (ch & 0xFF));
                if (r > 0x7500)
                   return 0x8F8080 + encoderJ0212.encodeDouble(ch);
                }
                return (r==0 ? 0: r + 0x8080);
        }
    }
}
