public class ISO2022_KR extends ISO2022
implements HistoricallyNamedCharset
{
    private static Charset ksc5601_cs;
    public ISO2022_KR() {
        super("ISO-2022-KR", ExtendedCharsets.aliasesFor("ISO-2022-KR"));
        ksc5601_cs = new EUC_KR();
    }
    public boolean contains(Charset cs) {
        return ((cs instanceof EUC_KR) ||
             (cs.name().equals("US-ASCII")) ||
             (cs instanceof ISO2022_KR));
    }
    public String historicalName() {
        return "ISO2022KR";
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends ISO2022.Decoder {
        public Decoder(Charset cs)
        {
            super(cs);
            SODesig = new byte[][] {{(byte)'$', (byte)')', (byte)'C'}};
            SODecoder = new CharsetDecoder[1];
            try {
                SODecoder[0] = ksc5601_cs.newDecoder();
            } catch (Exception e) {};
        }
    }
    private static class Encoder extends ISO2022.Encoder {
        public Encoder(Charset cs)
        {
            super(cs);
            SODesig = "$)C";
            try {
                ISOEncoder = ksc5601_cs.newEncoder();
            } catch (Exception e) { }
        }
        public boolean canEncode(char c) {
            return (ISOEncoder.canEncode(c));
        }
    }
}
