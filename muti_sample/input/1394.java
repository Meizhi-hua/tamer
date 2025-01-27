class UTF_16BE extends Unicode
{
    public UTF_16BE() {
        super("UTF-16BE", StandardCharsets.aliases_UTF_16BE);
    }
    public String historicalName() {
        return "UnicodeBigUnmarked";
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends UnicodeDecoder {
        public Decoder(Charset cs) {
            super(cs, BIG);
        }
    }
    private static class Encoder extends UnicodeEncoder {
        public Encoder(Charset cs) {
           super(cs, BIG, false);
        }
    }
}
