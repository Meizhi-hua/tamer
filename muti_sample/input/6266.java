public class DoubleByte {
    public final static char[] B2C_UNMAPPABLE;
    static {
        B2C_UNMAPPABLE = new char[0x100];
        Arrays.fill(B2C_UNMAPPABLE, UNMAPPABLE_DECODING);
    }
    public static class Decoder extends CharsetDecoder
                                implements DelegatableDecoder
    {
        final char[][] b2c;
        final char[] b2cSB;
        final int b2Min;
        final int b2Max;
        protected CoderResult crMalformedOrUnderFlow(int b) {
            return CoderResult.UNDERFLOW;
        }
        protected CoderResult crMalformedOrUnmappable(int b) {
            return CoderResult.unmappableForLength(2);
        }
        Decoder(Charset cs, float avgcpb, float maxcpb,
                char[][] b2c, char[] b2cSB,
                int b2Min, int b2Max) {
            super(cs, avgcpb, maxcpb);
            this.b2c = b2c;
            this.b2cSB = b2cSB;
            this.b2Min = b2Min;
            this.b2Max = b2Max;
        }
        Decoder(Charset cs, char[][] b2c, char[] b2cSB, int b2Min, int b2Max) {
            this(cs, 0.5f, 1.0f, b2c, b2cSB, b2Min, b2Max);
        }
        protected CoderResult decodeArrayLoop(ByteBuffer src, CharBuffer dst) {
            byte[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            char[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            try {
                while (sp < sl && dp < dl) {
                    int inSize = 1;
                    int b1 = sa[sp] & 0xff;
                    char c = b2cSB[b1];
                    if (c == UNMAPPABLE_DECODING) {
                        if (sl - sp < 2)
                            return crMalformedOrUnderFlow(b1);
                        int b2 = sa[sp + 1] & 0xff;
                        if (b2 < b2Min || b2 > b2Max ||
                            (c = b2c[b1][b2 - b2Min]) == UNMAPPABLE_DECODING) {
                            return crMalformedOrUnmappable(b1);
                        }
                        inSize++;
                    }
                    da[dp++] = c;
                    sp += inSize;
                }
                return (sp >= sl) ? CoderResult.UNDERFLOW
                                  : CoderResult.OVERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        protected CoderResult decodeBufferLoop(ByteBuffer src, CharBuffer dst) {
            int mark = src.position();
            try {
                while (src.hasRemaining() && dst.hasRemaining()) {
                    int b1 = src.get() & 0xff;
                    char c = b2cSB[b1];
                    int inSize = 1;
                    if (c == UNMAPPABLE_DECODING) {
                        if (src.remaining() < 1)
                            return crMalformedOrUnderFlow(b1);
                        int b2 = src.get() & 0xff;
                        if (b2 < b2Min || b2 > b2Max ||
                            (c = b2c[b1][b2 - b2Min]) == UNMAPPABLE_DECODING)
                            return crMalformedOrUnmappable(b1);
                        inSize++;
                    }
                    dst.put(c);
                    mark += inSize;
                }
                return src.hasRemaining()? CoderResult.OVERFLOW
                                         : CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        public CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
            if (src.hasArray() && dst.hasArray())
                return decodeArrayLoop(src, dst);
            else
                return decodeBufferLoop(src, dst);
        }
        public void implReset() {
            super.implReset();
        }
        public CoderResult implFlush(CharBuffer out) {
            return super.implFlush(out);
        }
        public char decodeSingle(int b) {
            return b2cSB[b];
        }
        public char decodeDouble(int b1, int b2) {
            if (b2 < b2Min || b2 > b2Max)
                return UNMAPPABLE_DECODING;
            return  b2c[b1][b2 - b2Min];
        }
    }
    public static class Decoder_EBCDIC extends Decoder {
        private static final int SBCS = 0;
        private static final int DBCS = 1;
        private static final int SO = 0x0e;
        private static final int SI = 0x0f;
        private int  currentState;
        Decoder_EBCDIC(Charset cs,
                       char[][] b2c, char[] b2cSB, int b2Min, int b2Max) {
            super(cs, b2c, b2cSB, b2Min, b2Max);
        }
        public void implReset() {
            currentState = SBCS;
        }
        private static boolean isDoubleByte(int b1, int b2) {
            return (0x41 <= b1 && b1 <= 0xfe && 0x41 <= b2 && b2 <= 0xfe)
                   || (b1 == 0x40 && b2 == 0x40); 
        }
        protected CoderResult decodeArrayLoop(ByteBuffer src, CharBuffer dst) {
            byte[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            char[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            try {
                while (sp < sl) {
                    int b1 = sa[sp] & 0xff;
                    int inSize = 1;
                    if (b1 == SO) {  
                        if (currentState != SBCS)
                            return CoderResult.malformedForLength(1);
                        else
                            currentState = DBCS;
                    } else if (b1 == SI) {
                        if (currentState != DBCS)
                            return CoderResult.malformedForLength(1);
                        else
                            currentState = SBCS;
                    } else {
                        char c =  UNMAPPABLE_DECODING;
                        if (currentState == SBCS) {
                            c = b2cSB[b1];
                            if (c == UNMAPPABLE_DECODING)
                                return CoderResult.unmappableForLength(1);
                        } else {
                            if (sl - sp < 2)
                                return CoderResult.UNDERFLOW;
                            int b2 = sa[sp + 1] & 0xff;
                            if (b2 < b2Min || b2 > b2Max ||
                                (c = b2c[b1][b2 - b2Min]) == UNMAPPABLE_DECODING) {
                                if (!isDoubleByte(b1, b2))
                                    return CoderResult.malformedForLength(2);
                                return CoderResult.unmappableForLength(2);
                            }
                            inSize++;
                        }
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = c;
                    }
                    sp += inSize;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        protected CoderResult decodeBufferLoop(ByteBuffer src, CharBuffer dst) {
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    int b1 = src.get() & 0xff;
                    int inSize = 1;
                    if (b1 == SO) {  
                        if (currentState != SBCS)
                            return CoderResult.malformedForLength(1);
                        else
                            currentState = DBCS;
                    } else if (b1 == SI) {
                        if (currentState != DBCS)
                            return CoderResult.malformedForLength(1);
                        else
                            currentState = SBCS;
                    } else {
                        char c = UNMAPPABLE_DECODING;
                        if (currentState == SBCS) {
                            c = b2cSB[b1];
                            if (c == UNMAPPABLE_DECODING)
                                return CoderResult.unmappableForLength(1);
                        } else {
                            if (src.remaining() < 1)
                                return CoderResult.UNDERFLOW;
                            int b2 = src.get()&0xff;
                            if (b2 < b2Min || b2 > b2Max ||
                                (c = b2c[b1][b2 - b2Min]) == UNMAPPABLE_DECODING) {
                                if (!isDoubleByte(b1, b2))
                                    return CoderResult.malformedForLength(2);
                                return CoderResult.unmappableForLength(2);
                            }
                            inSize++;
                        }
                        if (dst.remaining() < 1)
                            return CoderResult.OVERFLOW;
                        dst.put(c);
                    }
                    mark += inSize;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
    }
    public static class Decoder_EBCDIC_DBCSONLY extends Decoder {
        static final char[] b2cSB;
        static {
            b2cSB = new char[0x100];
            Arrays.fill(b2cSB, UNMAPPABLE_DECODING);
        }
        Decoder_EBCDIC_DBCSONLY(Charset cs, char[][] b2c, int b2Min, int b2Max) {
            super(cs, 0.5f, 1.0f, b2c, b2cSB, b2Min, b2Max);
        }
    }
    public static class Decoder_EUC_SIM extends Decoder {
        private final int SS2 =  0x8E;
        private final int SS3 =  0x8F;
        Decoder_EUC_SIM(Charset cs,
                        char[][] b2c, char[] b2cSB, int b2Min, int b2Max) {
            super(cs, b2c, b2cSB, b2Min, b2Max);
        }
        protected CoderResult crMalformedOrUnderFlow(int b) {
            if (b == SS2 || b == SS3 )
                return CoderResult.malformedForLength(1);
            return CoderResult.UNDERFLOW;
        }
        protected CoderResult crMalformedOrUnmappable(int b) {
            if (b == SS2 || b == SS3 )
                return CoderResult.malformedForLength(1);
            return CoderResult.unmappableForLength(2);
        }
    }
    public static class Encoder extends CharsetEncoder {
        final int MAX_SINGLEBYTE = 0xff;
        private final char[] c2b;
        private final char[] c2bIndex;
        Surrogate.Parser sgp;
        protected Encoder(Charset cs, char[] c2b, char[] c2bIndex) {
            super(cs, 2.0f, 2.0f);
            this.c2b = c2b;
            this.c2bIndex = c2bIndex;
        }
        Encoder(Charset cs, float avg, float max, byte[] repl, char[] c2b, char[] c2bIndex) {
            super(cs, avg, max, repl);
            this.c2b = c2b;
            this.c2bIndex = c2bIndex;
        }
        public boolean canEncode(char c) {
            return encodeChar(c) != UNMAPPABLE_ENCODING;
        }
        Surrogate.Parser sgp() {
            if (sgp == null)
                sgp = new Surrogate.Parser();
            return sgp;
        }
        protected CoderResult encodeArrayLoop(CharBuffer src, ByteBuffer dst) {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            try {
                while (sp < sl) {
                    char c = sa[sp];
                    int bb = encodeChar(c);
                    if (bb == UNMAPPABLE_ENCODING) {
                        if (Character.isSurrogate(c)) {
                            if (sgp().parse(c, sa, sp, sl) < 0)
                                return sgp.error();
                            return sgp.unmappableResult();
                        }
                        return CoderResult.unmappableForLength(1);
                    }
                    if (bb > MAX_SINGLEBYTE) {    
                        if (dl - dp < 2)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)(bb >> 8);
                        da[dp++] = (byte)bb;
                    } else {                      
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)bb;
                    }
                    sp++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        protected CoderResult encodeBufferLoop(CharBuffer src, ByteBuffer dst) {
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    char c = src.get();
                    int bb = encodeChar(c);
                    if (bb == UNMAPPABLE_ENCODING) {
                        if (Character.isSurrogate(c)) {
                            if (sgp().parse(c, src) < 0)
                                return sgp.error();
                            return sgp.unmappableResult();
                        }
                        return CoderResult.unmappableForLength(1);
                    }
                    if (bb > MAX_SINGLEBYTE) {  
                        if (dst.remaining() < 2)
                            return CoderResult.OVERFLOW;
                        dst.put((byte)(bb >> 8));
                        dst.put((byte)(bb));
                    } else {
                        if (dst.remaining() < 1)
                        return CoderResult.OVERFLOW;
                        dst.put((byte)bb);
                    }
                    mark++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
            if (src.hasArray() && dst.hasArray())
                return encodeArrayLoop(src, dst);
            else
                return encodeBufferLoop(src, dst);
        }
        public int encodeChar(char ch) {
            return c2b[c2bIndex[ch >> 8] + (ch & 0xff)];
        }
        static void initC2B(String[] b2c, String b2cSB, String b2cNR,  String c2bNR,
                            int b2Min, int b2Max,
                            char[] c2b, char[] c2bIndex)
        {
            Arrays.fill(c2b, (char)UNMAPPABLE_ENCODING);
            int off = 0x100;
            char[][] b2c_ca = new char[b2c.length][];
            char[] b2cSB_ca = null;
            if (b2cSB != null)
                b2cSB_ca = b2cSB.toCharArray();
            for (int i = 0; i < b2c.length; i++) {
                if (b2c[i] == null)
                    continue;
                b2c_ca[i] = b2c[i].toCharArray();
            }
            if (b2cNR != null) {
                int j = 0;
                while (j < b2cNR.length()) {
                    char b  = b2cNR.charAt(j++);
                    char c  = b2cNR.charAt(j++);
                    if (b < 0x100 && b2cSB_ca != null) {
                        if (b2cSB_ca[b] == c)
                            b2cSB_ca[b] = UNMAPPABLE_DECODING;
                    } else {
                        if (b2c_ca[b >> 8][(b & 0xff) - b2Min] == c)
                            b2c_ca[b >> 8][(b & 0xff) - b2Min] = UNMAPPABLE_DECODING;
                    }
                }
            }
            if (b2cSB_ca != null) {      
                for (int b = 0; b < b2cSB_ca.length; b++) {
                    char c = b2cSB_ca[b];
                    if (c == UNMAPPABLE_DECODING)
                        continue;
                    int index = c2bIndex[c >> 8];
                    if (index == 0) {
                        index = off;
                        off += 0x100;
                        c2bIndex[c >> 8] = (char)index;
                    }
                    c2b[index + (c & 0xff)] = (char)b;
                }
            }
            for (int b1 = 0; b1 < b2c.length; b1++) {  
                char[] db = b2c_ca[b1];
                if (db == null)
                    continue;
                for (int b2 = b2Min; b2 <= b2Max; b2++) {
                    char c = db[b2 - b2Min];
                    if (c == UNMAPPABLE_DECODING)
                        continue;
                    int index = c2bIndex[c >> 8];
                    if (index == 0) {
                        index = off;
                        off += 0x100;
                        c2bIndex[c >> 8] = (char)index;
                    }
                    c2b[index + (c & 0xff)] = (char)((b1 << 8) | b2);
                }
            }
            if (c2bNR != null) {
                for (int i = 0; i < c2bNR.length(); i += 2) {
                    char b = c2bNR.charAt(i);
                    char c = c2bNR.charAt(i + 1);
                    int index = (c >> 8);
                    if (c2bIndex[index] == 0) {
                        c2bIndex[index] = (char)off;
                        off += 0x100;
                    }
                    index = c2bIndex[index] + (c & 0xff);
                    c2b[index] = b;
                }
            }
        }
    }
    public static class Encoder_EBCDIC_DBCSONLY extends Encoder {
        Encoder_EBCDIC_DBCSONLY(Charset cs, byte[] repl,
                                char[] c2b, char[] c2bIndex) {
            super(cs, 2.0f, 2.0f, repl, c2b, c2bIndex);
        }
        public int encodeChar(char ch) {
            int bb = super.encodeChar(ch);
            if (bb <= MAX_SINGLEBYTE)
                return UNMAPPABLE_ENCODING;
            return bb;
        }
    }
    public static class Encoder_EBCDIC extends Encoder {
        static final int SBCS = 0;
        static final int DBCS = 1;
        static final byte SO = 0x0e;
        static final byte SI = 0x0f;
        protected int  currentState = SBCS;
        Encoder_EBCDIC(Charset cs, char[] c2b, char[] c2bIndex) {
            super(cs, 4.0f, 5.0f, new byte[] {(byte)0x6f}, c2b, c2bIndex);
        }
        protected void implReset() {
            currentState = SBCS;
        }
        protected CoderResult implFlush(ByteBuffer out) {
            if (currentState == DBCS) {
                if (out.remaining() < 1)
                    return CoderResult.OVERFLOW;
                out.put(SI);
            }
            implReset();
            return CoderResult.UNDERFLOW;
        }
        protected CoderResult encodeArrayLoop(CharBuffer src, ByteBuffer dst) {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            try {
                while (sp < sl) {
                    char c = sa[sp];
                    int bb = encodeChar(c);
                    if (bb == UNMAPPABLE_ENCODING) {
                        if (Character.isSurrogate(c)) {
                            if (sgp().parse(c, sa, sp, sl) < 0)
                                return sgp.error();
                            return sgp.unmappableResult();
                        }
                        return CoderResult.unmappableForLength(1);
                    }
                    if (bb > MAX_SINGLEBYTE) {  
                        if (currentState == SBCS) {
                            if (dl - dp < 1)
                                return CoderResult.OVERFLOW;
                            currentState = DBCS;
                            da[dp++] = SO;
                        }
                        if (dl - dp < 2)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)(bb >> 8);
                        da[dp++] = (byte)bb;
                    } else {                    
                        if (currentState == DBCS) {
                            if (dl - dp < 1)
                                return CoderResult.OVERFLOW;
                            currentState = SBCS;
                            da[dp++] = SI;
                        }
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)bb;
                    }
                    sp++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        protected CoderResult encodeBufferLoop(CharBuffer src, ByteBuffer dst) {
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    char c = src.get();
                    int bb = encodeChar(c);
                    if (bb == UNMAPPABLE_ENCODING) {
                        if (Character.isSurrogate(c)) {
                            if (sgp().parse(c, src) < 0)
                                return sgp.error();
                            return sgp.unmappableResult();
                        }
                        return CoderResult.unmappableForLength(1);
                    }
                    if (bb > MAX_SINGLEBYTE) {  
                        if (currentState == SBCS) {
                            if (dst.remaining() < 1)
                                return CoderResult.OVERFLOW;
                            currentState = DBCS;
                            dst.put(SO);
                        }
                        if (dst.remaining() < 2)
                            return CoderResult.OVERFLOW;
                        dst.put((byte)(bb >> 8));
                        dst.put((byte)(bb));
                    } else {                  
                        if (currentState == DBCS) {
                            if (dst.remaining() < 1)
                                return CoderResult.OVERFLOW;
                            currentState = SBCS;
                            dst.put(SI);
                        }
                        if (dst.remaining() < 1)
                            return CoderResult.OVERFLOW;
                        dst.put((byte)bb);
                    }
                    mark++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
    }
    public static class Encoder_EUC_SIM extends Encoder {
        Encoder_EUC_SIM(Charset cs, char[] c2b, char[] c2bIndex) {
            super(cs, c2b, c2bIndex);
        }
    }
}
