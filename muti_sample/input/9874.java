public class X11Dingbats extends Charset {
    public X11Dingbats () {
        super("X11Dingbats", null);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    public CharsetDecoder newDecoder() {
        throw new Error("Decoder is not supported by X11Dingbats Charset");
    }
    public boolean contains(Charset cs) {
        return cs instanceof X11Dingbats;
    }
    private static class Encoder extends CharsetEncoder {
        public Encoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        public boolean canEncode(char ch) {
            if (ch >= 0x2701 && ch <= 0x275e) { 
                return true;
            }
            if (ch >= 0x2761 && ch <= 0x27be) {
                return (table[ch - 0x2761] != 0x00);
            }
            return false;
        }
        protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            assert (sp <= sl);
            sp = (sp <= sl ? sp : sl);
            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            assert (dp <= dl);
            dp = (dp <= dl ? dp : dl);
            try {
                while (sp < sl) {
                    char c = sa[sp];
                    if (dl - dp < 1)
                        return CoderResult.OVERFLOW;
                    if (!canEncode(c))
                        return CoderResult.unmappableForLength(1);
                    sp++;
                    if (c >= 0x2761){
                        da[dp++] = table[c - 0x2761]; 
                    } else {
                        da[dp++] = (byte)(c + 0x20 - 0x2700); 
                    }
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        private static byte[] table = {
            (byte)0xa1, (byte)0xa2, (byte)0xa3, (byte)0xa4,
            (byte)0xa5, (byte)0xa6, (byte)0xa7,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0xb6, (byte)0xb7,
            (byte)0xb8, (byte)0xb9, (byte)0xba, (byte)0xbb,
            (byte)0xbc, (byte)0xbd, (byte)0xbe, (byte)0xbf,
            (byte)0xc0, (byte)0xc1, (byte)0xc2, (byte)0xc3,
            (byte)0xc4, (byte)0xc5, (byte)0xc6, (byte)0xc7,
            (byte)0xc8, (byte)0xc9, (byte)0xca, (byte)0xcb,
            (byte)0xcc, (byte)0xcd, (byte)0xce, (byte)0xcf,
            (byte)0xd0, (byte)0xd1, (byte)0xd2, (byte)0xd3,
            (byte)0xd4, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xd8, (byte)0xd9, (byte)0xda, (byte)0xdb,
            (byte)0xdc, (byte)0xdd, (byte)0xde, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00};
        public boolean isLegalReplacement(byte[] repl) {
            return true;
        }
    }
}
