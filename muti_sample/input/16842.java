class UTF_16LE extends Unicode
{
    public UTF_16LE() {
        super("UTF-16LE", StandardCharsets.aliases_UTF_16LE);
    }
    public String historicalName() {
        return "UnicodeLittleUnmarked";
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends UnicodeDecoder {
        public Decoder(Charset cs) {
            super(cs, LITTLE);
        }
    }
    private static class Encoder extends UnicodeEncoder {
        public Encoder(Charset cs) {
            super(cs, LITTLE, false);
        }
    }
}
