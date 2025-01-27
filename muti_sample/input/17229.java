public class MS932_0213 extends Charset {
    public MS932_0213() {
        super("x-MS932_0213", ExtendedCharsets.aliasesFor("MS932_0213"));
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof MS932)
                || (cs instanceof MS932_0213));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    protected static class Decoder extends SJIS_0213.Decoder {
        static DoubleByte.Decoder decMS932 =
            (DoubleByte.Decoder)new MS932().newDecoder();
        protected Decoder(Charset cs) {
            super(cs);
        }
        protected char decodeDouble(int b1, int b2) {
            char c = decMS932.decodeDouble(b1, b2);
            if (c == UNMAPPABLE_DECODING)
                return super.decodeDouble(b1, b2);
            return c;
        }
    }
    protected static class Encoder extends SJIS_0213.Encoder {
        static DoubleByte.Encoder encMS932 =
            (DoubleByte.Encoder)new MS932().newEncoder();
        protected Encoder(Charset cs) {
            super(cs);
        }
        protected int encodeChar(char ch) {
            int db = encMS932.encodeChar(ch);
            if (db == UNMAPPABLE_ENCODING)
                return super.encodeChar(ch);
            return db;
        }
    }
}
