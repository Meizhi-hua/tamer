public class US_ASCII
    extends Charset
    implements HistoricallyNamedCharset
{
    public US_ASCII() {
        super("US-ASCII", StandardCharsets.aliases_US_ASCII);
    }
    public String historicalName() {
        return "ASCII";
    }
    public boolean contains(Charset cs) {
        return (cs instanceof US_ASCII);
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends CharsetDecoder
                                 implements ArrayDecoder {
        private Decoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        private CoderResult decodeArrayLoop(ByteBuffer src,
                                            CharBuffer dst)
        {
            byte[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            assert (sp <= sl);
            sp = (sp <= sl ? sp : sl);
            char[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            assert (dp <= dl);
            dp = (dp <= dl ? dp : dl);
            try {
                while (sp < sl) {
                    byte b = sa[sp];
                    if (b >= 0) {
                        if (dp >= dl)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (char)b;
                        sp++;
                        continue;
                    }
                    return CoderResult.malformedForLength(1);
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        private CoderResult decodeBufferLoop(ByteBuffer src,
                                             CharBuffer dst)
        {
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    byte b = src.get();
                    if (b >= 0) {
                        if (!dst.hasRemaining())
                            return CoderResult.OVERFLOW;
                        dst.put((char)b);
                        mark++;
                        continue;
                    }
                    return CoderResult.malformedForLength(1);
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        protected CoderResult decodeLoop(ByteBuffer src,
                                         CharBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return decodeArrayLoop(src, dst);
            else
                return decodeBufferLoop(src, dst);
        }
        private char repl = '\uFFFD';
        protected void implReplaceWith(String newReplacement) {
            repl = newReplacement.charAt(0);
        }
        public int decode(byte[] src, int sp, int len, char[] dst) {
            int dp = 0;
            len = Math.min(len, dst.length);
            while (dp < len) {
                byte b = src[sp++];
                if (b >= 0)
                    dst[dp++] = (char)b;
                else
                    dst[dp++] = repl;
            }
            return dp;
        }
    }
    private static class Encoder extends CharsetEncoder
                                 implements ArrayEncoder {
        private Encoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        public boolean canEncode(char c) {
            return c < 0x80;
        }
        public boolean isLegalReplacement(byte[] repl) {
            return (repl.length == 1 && repl[0] >= 0) ||
                   super.isLegalReplacement(repl);
        }
        private final Surrogate.Parser sgp = new Surrogate.Parser();
        private CoderResult encodeArrayLoop(CharBuffer src,
                                            ByteBuffer dst)
        {
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
                    if (c < 0x80) {
                        if (dp >= dl)
                            return CoderResult.OVERFLOW;
                        da[dp] = (byte)c;
                        sp++; dp++;
                        continue;
                    }
                    if (sgp.parse(c, sa, sp, sl) < 0)
                        return sgp.error();
                    return sgp.unmappableResult();
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        private CoderResult encodeBufferLoop(CharBuffer src,
                                             ByteBuffer dst)
        {
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    char c = src.get();
                    if (c < 0x80) {
                        if (!dst.hasRemaining())
                            return CoderResult.OVERFLOW;
                        dst.put((byte)c);
                        mark++;
                        continue;
                    }
                    if (sgp.parse(c, src) < 0)
                        return sgp.error();
                    return sgp.unmappableResult();
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        protected CoderResult encodeLoop(CharBuffer src,
                                         ByteBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return encodeArrayLoop(src, dst);
            else
                return encodeBufferLoop(src, dst);
        }
        private byte repl = (byte)'?';
        protected void implReplaceWith(byte[] newReplacement) {
            repl = newReplacement[0];
        }
        public int encode(char[] src, int sp, int len, byte[] dst) {
            int dp = 0;
            int sl = sp + Math.min(len, dst.length);
            while (sp < sl) {
                char c = src[sp++];
                if (c < 0x80) {
                    dst[dp++] = (byte)c;
                    continue;
                }
                if (Character.isHighSurrogate(c) && sp < sl &&
                    Character.isLowSurrogate(src[sp])) {
                    if (len > dst.length) {
                        sl++;
                        len--;
                    }
                    sp++;
                }
                dst[dp++] = repl;
            }
            return dp;
        }
    }
}
