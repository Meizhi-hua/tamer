public class Big5_HKSCS_2001 extends Charset
{
    public Big5_HKSCS_2001() {
        super("x-Big5-HKSCS-2001", ExtendedCharsets.aliasesFor("x-Big5-HKSCS-2001"));
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof Big5)
                || (cs instanceof Big5_HKSCS_2001));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends HKSCS.Decoder {
        private static DoubleByte.Decoder big5 =
            (DoubleByte.Decoder)new Big5().newDecoder();
        private static char[][] b2cBmp = new char[0x100][];
        private static char[][] b2cSupp = new char[0x100][];
        static {
            initb2c(b2cBmp, HKSCS2001Mapping.b2cBmpStr);
            initb2c(b2cSupp, HKSCS2001Mapping.b2cSuppStr);
        }
        private Decoder(Charset cs) {
            super(cs, big5, b2cBmp, b2cSupp);
        }
    }
    private static class Encoder extends HKSCS.Encoder {
        private static DoubleByte.Encoder big5 =
            (DoubleByte.Encoder)new Big5().newEncoder();
        static char[][] c2bBmp = new char[0x100][];
        static char[][] c2bSupp = new char[0x100][];
        static {
            initc2b(c2bBmp, HKSCS2001Mapping.b2cBmpStr,
                    HKSCS2001Mapping.pua);
            initc2b(c2bSupp, HKSCS2001Mapping.b2cSuppStr, null);
        }
        private Encoder(Charset cs) {
            super(cs, big5, c2bBmp, c2bSupp);
        }
    }
}
