public class X11SunUnicode_0 extends Charset {
    public X11SunUnicode_0 () {
        super("X11SunUnicode_0", null);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    public CharsetDecoder newDecoder() {
        throw new Error("Decoder is not implemented for X11SunUnicode_0 Charset");
    }
    public boolean contains(Charset cs) {
        return cs instanceof X11SunUnicode_0;
    }
    private static class Encoder extends DoubleByteEncoder {
        public Encoder(Charset cs) {
            super(cs, index1, index2);
        }
        private final static String innerIndex0=
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0120\u0121\u0122\u0000\u0123\u0124\u0125"+
            "\u0126\u0127\u0128\u0129\u012A\u012B\u012C\u012D"+
            "\u012E\u012F\u0130\u0131\u0132\u0133\u0134\u0135"+
            "\u0136\u0137\u0138\u0139\u013A\u013B\u013C\u013D"+
            "\u013E\u013F\u0140\u0141\u0142\u0143\u0144\u0145"+
            "\u0146\u0147\u0148\u0149\u014A\u014B\u014C\u014D"+
            "\u014E\u014F\u0150\u0151\u0152\u0153\u0154\u0155"+
            "\u0156\u0157\u0000\u0000\u0158\u0159\u015A\u015B"+
            "\u015C\u015D\u015E\u015F\u0160\u0161\u0162\u0163"+
            "\u0164\u0165\u0166\u0167\u0168\u0169\u0000\u0000"+
            "\u016A\u016B\u016C\u016D\u016E\u0000\u0000\u0000"+
            "\u016F\u0170\u0171\u0172\u0173\u0174\u0175\u0176"+
            "\u0177\u0178\u0179\u017A\u017B\u017C\u017D\u017E"+
            "\u017F\u0180\u0181\u0182\u0183\u0184\u0185\u0186"+
            "\u0187\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"+
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
        private final static short index1[] = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        private final static String index2[] = {
            innerIndex0
        };
        public boolean isLegalReplacement(byte[] repl) {
            return true;
        }
    }
}
