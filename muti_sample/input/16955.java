final class CompoundTextSupport {
    private static final class ControlSequence {
        final int hash;
        final byte[] escSequence;
        final byte[] encoding;
        ControlSequence(byte[] escSequence) {
            this(escSequence, null);
        }
        ControlSequence(byte[] escSequence, byte[] encoding) {
            if (escSequence == null) {
                throw new NullPointerException();
            }
            this.escSequence = escSequence;
            this.encoding = encoding;
            int hash = 0;
            int length = escSequence.length;
            for (int i = 0; i < escSequence.length; i++) {
                hash += (((int)escSequence[i]) & 0xff) << (i % 4);
            }
            if (encoding != null) {
                for (int i = 0; i < encoding.length; i++) {
                    hash += (((int)encoding[i]) & 0xff) << (i % 4);
                }
                length += 2  + encoding.length + 1 ;
            }
            this.hash = hash;
            if (MAX_CONTROL_SEQUENCE_LEN < length) {
                MAX_CONTROL_SEQUENCE_LEN = length;
            }
        }
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ControlSequence)) {
                return false;
            }
            ControlSequence rhs = (ControlSequence)obj;
            if (escSequence != rhs.escSequence) {
                if (escSequence.length != rhs.escSequence.length) {
                    return false;
                }
                for (int i = 0; i < escSequence.length; i++) {
                    if (escSequence[i] != rhs.escSequence[i]) {
                        return false;
                    }
                }
            }
            if (encoding != rhs.encoding) {
                if (encoding == null || rhs.encoding == null ||
                    encoding.length != rhs.encoding.length)
                {
                    return false;
                }
                for (int i = 0; i < encoding.length; i++) {
                    if (encoding[i] != rhs.encoding[i]) {
                        return false;
                    }
                }
            }
            return true;
        }
        public int hashCode() {
            return hash;
        }
        ControlSequence concatenate(ControlSequence rhs) {
            if (encoding != null) {
                throw new IllegalArgumentException
                    ("cannot concatenate to a non-standard charset escape " +
                     "sequence");
            }
            int len = escSequence.length + rhs.escSequence.length;
            byte[] newEscSequence = new byte[len];
            System.arraycopy(escSequence, 0, newEscSequence, 0,
                             escSequence.length);
            System.arraycopy(rhs.escSequence, 0, newEscSequence,
                             escSequence.length, rhs.escSequence.length);
            return new ControlSequence(newEscSequence, rhs.encoding);
        }
    }
    static int MAX_CONTROL_SEQUENCE_LEN;
    private static final Map<ControlSequence, String> sequenceToEncodingMap;
    private static final Map<ControlSequence, Boolean> highBitsMap;
    private static final Map<String, ControlSequence> encodingToSequenceMap;
    private static final List<String> encodings;
    static {
        HashMap<ControlSequence, String> tSequenceToEncodingMap =
            new HashMap<>(33, 1.0f);
        HashMap<ControlSequence, Boolean> tHighBitsMap =
            new HashMap<>(31, 1.0f);
        HashMap<String, ControlSequence> tEncodingToSequenceMap =
            new HashMap<>(21, 1.0f);
        ArrayList<String> tEncodings = new ArrayList<>(21);
        if (!(isEncodingSupported("US-ASCII") &&
              isEncodingSupported("ISO-8859-1")))
        {
            throw new ExceptionInInitializerError
                ("US-ASCII and ISO-8859-1 unsupported");
        }
        ControlSequence leftAscii = 
            new ControlSequence(new byte[] { 0x1B, 0x28, 0x42 });
        tSequenceToEncodingMap.put(leftAscii, "US-ASCII");
        tHighBitsMap.put(leftAscii, Boolean.FALSE);
        {
            ControlSequence rightAscii = 
                new ControlSequence(new byte[] { 0x1B, 0x29, 0x42 });
            tSequenceToEncodingMap.put(rightAscii, "US-ASCII");
            tHighBitsMap.put(rightAscii, Boolean.FALSE);
        }
        {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x41 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-1");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-1", fullSet);
            tEncodings.add("ISO-8859-1");
        }
        if (isEncodingSupported("ISO-8859-2")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x42 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-2");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-2", fullSet);
            tEncodings.add("ISO-8859-2");
        }
        if (isEncodingSupported("ISO-8859-3")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x43 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-3");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-3", fullSet);
            tEncodings.add("ISO-8859-3");
        }
        if (isEncodingSupported("ISO-8859-4")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x44 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-4");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-4", fullSet);
            tEncodings.add("ISO-8859-4");
        }
        if (isEncodingSupported("ISO-8859-5")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x4C });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-5");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-5", fullSet);
            tEncodings.add("ISO-8859-5");
        }
        if (isEncodingSupported("ISO-8859-6")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x47 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-6");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-6", fullSet);
            tEncodings.add("ISO-8859-6");
        }
        if (isEncodingSupported("ISO-8859-7")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x46 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-7");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-7", fullSet);
            tEncodings.add("ISO-8859-7");
        }
        if (isEncodingSupported("ISO-8859-8")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x48 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-8");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-8", fullSet);
            tEncodings.add("ISO-8859-8");
        }
        if (isEncodingSupported("ISO-8859-9")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x4D });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-9");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-9", fullSet);
            tEncodings.add("ISO-8859-9");
        }
        if (isEncodingSupported("JIS_X0201")) {
            ControlSequence glLeft = 
                new ControlSequence(new byte[] { 0x1B, 0x28, 0x4A });
            ControlSequence glRight = 
                new ControlSequence(new byte[] { 0x1B, 0x28, 0x49 });
            ControlSequence grLeft = 
                new ControlSequence(new byte[] { 0x1B, 0x29, 0x4A });
            ControlSequence grRight = 
                new ControlSequence(new byte[] { 0x1B, 0x29, 0x49 });
            tSequenceToEncodingMap.put(glLeft, "JIS_X0201");
            tSequenceToEncodingMap.put(glRight, "JIS_X0201");
            tSequenceToEncodingMap.put(grLeft, "JIS_X0201");
            tSequenceToEncodingMap.put(grRight, "JIS_X0201");
            tHighBitsMap.put(glLeft, Boolean.FALSE);
            tHighBitsMap.put(glRight, Boolean.TRUE);
            tHighBitsMap.put(grLeft, Boolean.FALSE);
            tHighBitsMap.put(grRight, Boolean.TRUE);
            ControlSequence fullSet = glLeft.concatenate(grRight);
            tEncodingToSequenceMap.put("JIS_X0201", fullSet);
            tEncodings.add("JIS_X0201");
        }
        if (isEncodingSupported("X11GB2312")) {
            ControlSequence leftHalf =  
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x28, 0x41 });
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x29, 0x41 });
            tSequenceToEncodingMap.put(leftHalf, "X11GB2312");
            tSequenceToEncodingMap.put(rightHalf, "X11GB2312");
            tHighBitsMap.put(leftHalf, Boolean.FALSE);
            tHighBitsMap.put(rightHalf, Boolean.FALSE);
            tEncodingToSequenceMap.put("X11GB2312", leftHalf);
            tEncodings.add("X11GB2312");
        }
        if (isEncodingSupported("x-JIS0208")) {
            ControlSequence leftHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x28, 0x42 });
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x29, 0x42 });
            tSequenceToEncodingMap.put(leftHalf, "x-JIS0208");
            tSequenceToEncodingMap.put(rightHalf, "x-JIS0208");
            tHighBitsMap.put(leftHalf, Boolean.FALSE);
            tHighBitsMap.put(rightHalf, Boolean.FALSE);
            tEncodingToSequenceMap.put("x-JIS0208", leftHalf);
            tEncodings.add("x-JIS0208");
        }
        if (isEncodingSupported("X11KSC5601")) {
            ControlSequence leftHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x28, 0x43 });
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x29, 0x43 });
            tSequenceToEncodingMap.put(leftHalf, "X11KSC5601");
            tSequenceToEncodingMap.put(rightHalf, "X11KSC5601");
            tHighBitsMap.put(leftHalf, Boolean.FALSE);
            tHighBitsMap.put(rightHalf, Boolean.FALSE);
            tEncodingToSequenceMap.put("X11KSC5601", leftHalf);
            tEncodings.add("X11KSC5601");
        }
        if (isEncodingSupported("ISO-8859-15")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x62 });
            tSequenceToEncodingMap.put(rightHalf, "ISO-8859-15");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("ISO-8859-15", fullSet);
            tEncodings.add("ISO-8859-15");
        }
        if (isEncodingSupported("TIS-620")) {
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x2D, 0x54 });
            tSequenceToEncodingMap.put(rightHalf, "TIS-620");
            tHighBitsMap.put(rightHalf, Boolean.TRUE);
            ControlSequence fullSet = leftAscii.concatenate(rightHalf);
            tEncodingToSequenceMap.put("TIS-620", fullSet);
            tEncodings.add("TIS-620");
        }
        if (isEncodingSupported("JIS_X0212-1990")) {
            ControlSequence leftHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x28, 0x44 });
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x29, 0x44 });
            tSequenceToEncodingMap.put(leftHalf, "JIS_X0212-1990");
            tSequenceToEncodingMap.put(rightHalf, "JIS_X0212-1990");
            tHighBitsMap.put(leftHalf, Boolean.FALSE);
            tHighBitsMap.put(rightHalf, Boolean.FALSE);
            tEncodingToSequenceMap.put("JIS_X0212-1990", leftHalf);
            tEncodings.add("JIS_X0212-1990");
        }
        if (isEncodingSupported("X11CNS11643P1")) {
            ControlSequence leftHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x28, 0x47 });
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x29, 0x47 });
            tSequenceToEncodingMap.put(leftHalf, "X11CNS11643P1");
            tSequenceToEncodingMap.put(rightHalf, "X11CNS11643P1");
            tHighBitsMap.put(leftHalf, Boolean.FALSE);
            tHighBitsMap.put(rightHalf, Boolean.FALSE);
            tEncodingToSequenceMap.put("X11CNS11643P1", leftHalf);
            tEncodings.add("X11CNS11643P1");
        }
        if (isEncodingSupported("X11CNS11643P2")) {
            ControlSequence leftHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x28, 0x48 });
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x29, 0x48 });
            tSequenceToEncodingMap.put(leftHalf, "X11CNS11643P2");
            tSequenceToEncodingMap.put(rightHalf, "X11CNS11643P2");
            tHighBitsMap.put(leftHalf, Boolean.FALSE);
            tHighBitsMap.put(rightHalf, Boolean.FALSE);
            tEncodingToSequenceMap.put("X11CNS11643P2", leftHalf);
            tEncodings.add("X11CNS11643P2");
        }
        if (isEncodingSupported("X11CNS11643P3")) {
            ControlSequence leftHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x28, 0x49 });
            ControlSequence rightHalf = 
                new ControlSequence(new byte[] { 0x1B, 0x24, 0x29, 0x49 });
            tSequenceToEncodingMap.put(leftHalf, "X11CNS11643P3");
            tSequenceToEncodingMap.put(rightHalf, "X11CNS11643P3");
            tHighBitsMap.put(leftHalf, Boolean.FALSE);
            tHighBitsMap.put(rightHalf, Boolean.FALSE);
            tEncodingToSequenceMap.put("X11CNS11643P3", leftHalf);
            tEncodings.add("X11CNS11643P3");
        }
        if (isEncodingSupported("x-Johab")) {
            ControlSequence johab = new ControlSequence(
                new byte[] { 0x1b, 0x25, 0x2f, 0x32 },
                new byte[] { 0x53, 0x55, 0x4e, 0x2d, 0x4b, 0x53, 0x43, 0x35,
                             0x36, 0x30, 0x31, 0x2e, 0x31, 0x39, 0x39, 0x32,
                             0x2d, 0x33 });
            tSequenceToEncodingMap.put(johab, "x-Johab");
            tEncodingToSequenceMap.put("x-Johab", johab);
            tEncodings.add("x-Johab");
        }
        if (isEncodingSupported("Big5")) {
            ControlSequence big5 = new ControlSequence(
                new byte[] { 0x1b, 0x25, 0x2f, 0x32 },
                new byte[] { 0x53, 0x55, 0x4e, 0x2d, 0x42, 0x49, 0x47, 0x35,
                             0x2d, 0x31 });
            tSequenceToEncodingMap.put(big5, "Big5");
            tEncodingToSequenceMap.put("Big5", big5);
            tEncodings.add("Big5");
        }
        sequenceToEncodingMap =
            Collections.unmodifiableMap(tSequenceToEncodingMap);
        highBitsMap = Collections.unmodifiableMap(tHighBitsMap);
        encodingToSequenceMap =
            Collections.unmodifiableMap(tEncodingToSequenceMap);
        encodings = Collections.unmodifiableList(tEncodings);
    }
    private static boolean isEncodingSupported(String encoding) {
        try {
            if (Charset.isSupported(encoding))
                return true;
        } catch (IllegalArgumentException x) { }
        return (getDecoder(encoding) != null &&
                getEncoder(encoding) != null);
    }
    static CharsetDecoder getStandardDecoder(byte[] escSequence) {
        return getNonStandardDecoder(escSequence, null);
    }
    static boolean getHighBit(byte[] escSequence) {
        Boolean bool = highBitsMap.get(new ControlSequence(escSequence));
        return (bool == Boolean.TRUE);
    }
    static CharsetDecoder getNonStandardDecoder(byte[] escSequence,
                                                       byte[] encoding) {
        return getDecoder(sequenceToEncodingMap.get
            (new ControlSequence(escSequence, encoding)));
    }
    static CharsetDecoder getDecoder(String enc) {
        if (enc == null) {
            return null;
        }
        Charset cs = null;
        try {
            cs = Charset.forName(enc);
        } catch (IllegalArgumentException e) {
            Class<?> cls;
            try {
                cls = Class.forName("sun.awt.motif." + enc);
            } catch (ClassNotFoundException ee) {
                return null;
            }
            try {
                cs = (Charset)cls.newInstance();
            } catch (InstantiationException ee) {
                return null;
            } catch (IllegalAccessException ee) {
                return null;
            }
        }
        try {
            return cs.newDecoder();
        } catch (UnsupportedOperationException e) {}
        return null;
    }
    static byte[] getEscapeSequence(String encoding) {
        ControlSequence seq = encodingToSequenceMap.get(encoding);
        if (seq != null) {
            return seq.escSequence;
        }
        return null;
    }
    static byte[] getEncoding(String encoding) {
        ControlSequence seq = encodingToSequenceMap.get(encoding);
        if (seq != null) {
            return seq.encoding;
        }
        return null;
    }
    static List<String> getEncodings() {
        return encodings;
    }
    static CharsetEncoder getEncoder(String enc) {
        if (enc == null) {
            return null;
        }
        Charset cs = null;
        try {
            cs = Charset.forName(enc);
        } catch (IllegalArgumentException e) {
            Class<?> cls;
            try {
                cls = Class.forName("sun.awt.motif." + enc);
            } catch (ClassNotFoundException ee) {
                return null;
            }
            try {
                cs = (Charset)cls.newInstance();
            } catch (InstantiationException ee) {
                return null;
            } catch (IllegalAccessException ee) {
                return null;
            }
        }
        try {
            return cs.newEncoder();
        } catch (Throwable e) {}
        return null;
    }
    private CompoundTextSupport() {}
}
