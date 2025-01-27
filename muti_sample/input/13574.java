public abstract class AbstractSaslImpl {
    protected boolean completed = false;
    protected boolean privacy = false;
    protected boolean integrity = false;
    protected byte[] qop;           
    protected byte allQop;          
    protected byte[] strength;      
    protected int sendMaxBufSize = 0;     
    protected int recvMaxBufSize = 65536; 
    protected int rawSendSize;            
    protected String myClassName;
    protected AbstractSaslImpl(Map props, String className) throws SaslException {
        myClassName = className;
        if (props != null) {
            String prop;
            qop = parseQop(prop=(String)props.get(Sasl.QOP));
            logger.logp(Level.FINE, myClassName, "constructor",
                "SASLIMPL01:Preferred qop property: {0}", prop);
            allQop = combineMasks(qop);
            if (logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, myClassName, "constructor",
                    "SASLIMPL02:Preferred qop mask: {0}", new Byte(allQop));
                if (qop.length > 0) {
                    StringBuffer qopbuf = new StringBuffer();
                    for (int i = 0; i < qop.length; i++) {
                        qopbuf.append(Byte.toString(qop[i]));
                        qopbuf.append(' ');
                    }
                    logger.logp(Level.FINE, myClassName, "constructor",
                        "SASLIMPL03:Preferred qops : {0}", qopbuf.toString());
                }
            }
            strength = parseStrength(prop=(String)props.get(Sasl.STRENGTH));
            logger.logp(Level.FINE, myClassName, "constructor",
                "SASLIMPL04:Preferred strength property: {0}", prop);
            if (logger.isLoggable(Level.FINE) && strength.length > 0) {
                StringBuffer strbuf = new StringBuffer();
                for (int i = 0; i < strength.length; i++) {
                    strbuf.append(Byte.toString(strength[i]));
                    strbuf.append(' ');
                }
                logger.logp(Level.FINE, myClassName, "constructor",
                    "SASLIMPL05:Cipher strengths: {0}", strbuf.toString());
            }
            prop = (String)props.get(Sasl.MAX_BUFFER);
            if (prop != null) {
                try {
                    logger.logp(Level.FINE, myClassName, "constructor",
                        "SASLIMPL06:Max receive buffer size: {0}", prop);
                    recvMaxBufSize = Integer.parseInt(prop);
                } catch (NumberFormatException e) {
                    throw new SaslException(
                "Property must be string representation of integer: " +
                        Sasl.MAX_BUFFER);
                }
            }
            prop = (String)props.get(MAX_SEND_BUF);
            if (prop != null) {
                try {
                    logger.logp(Level.FINE, myClassName, "constructor",
                        "SASLIMPL07:Max send buffer size: {0}", prop);
                    sendMaxBufSize = Integer.parseInt(prop);
                } catch (NumberFormatException e) {
                    throw new SaslException(
                "Property must be string representation of integer: " +
                        MAX_SEND_BUF);
                }
            }
        } else {
            qop = DEFAULT_QOP;
            allQop = NO_PROTECTION;
            strength = STRENGTH_MASKS;
        }
    }
    public boolean isComplete() {
        return completed;
    }
    public Object getNegotiatedProperty(String propName) {
        if (!completed) {
            throw new IllegalStateException("SASL authentication not completed");
        }
        if (propName.equals(Sasl.QOP)) {
            if (privacy) {
                return "auth-conf";
            } else if (integrity) {
                return "auth-int";
            } else {
                return "auth";
            }
        } else if (propName.equals(Sasl.MAX_BUFFER)) {
            return Integer.toString(recvMaxBufSize);
        } else if (propName.equals(Sasl.RAW_SEND_SIZE)) {
            return Integer.toString(rawSendSize);
        } else if (propName.equals(MAX_SEND_BUF)) {
            return Integer.toString(sendMaxBufSize);
        } else {
            return null;
        }
    }
    protected static final byte combineMasks(byte[] in) {
        byte answer = 0;
        for (int i = 0; i < in.length; i++) {
            answer |= in[i];
        }
        return answer;
    }
    protected static final byte findPreferredMask(byte pref, byte[] in) {
        for (int i = 0; i < in.length; i++) {
            if ((in[i]&pref) != 0) {
                return in[i];
            }
        }
        return (byte)0;
    }
    private static final byte[] parseQop(String qop) throws SaslException {
        return parseQop(qop, null, false);
    }
    protected static final byte[] parseQop(String qop, String[] saveTokens,
        boolean ignore) throws SaslException {
        if (qop == null) {
            return DEFAULT_QOP;   
        }
        return parseProp(Sasl.QOP, qop, QOP_TOKENS, QOP_MASKS, saveTokens, ignore);
    }
    private static final byte[] parseStrength(String strength)
        throws SaslException {
        if (strength == null) {
            return DEFAULT_STRENGTH;   
        }
        return parseProp(Sasl.STRENGTH, strength, STRENGTH_TOKENS,
            STRENGTH_MASKS, null, false);
    }
    private static final byte[] parseProp(String propName, String propVal,
        String[] vals, byte[] masks, String[] tokens, boolean ignore)
        throws SaslException {
        StringTokenizer parser = new StringTokenizer(propVal, ", \t\n");
        String token;
        byte[] answer = new byte[vals.length];
        int i = 0;
        boolean found;
        while (parser.hasMoreTokens() && i < answer.length) {
            token = parser.nextToken();
            found = false;
            for (int j = 0; !found && j < vals.length; j++) {
                if (token.equalsIgnoreCase(vals[j])) {
                    found = true;
                    answer[i++] = masks[j];
                    if (tokens != null) {
                        tokens[j] = token;    
                    }
                }
            }
            if (!found && !ignore) {
                throw new SaslException(
                    "Invalid token in " + propName + ": " + propVal);
            }
        }
        for (int j = i; j < answer.length; j++) {
            answer[j] = 0;
        }
        return answer;
    }
    protected static final void traceOutput(String srcClass, String srcMethod,
        String traceTag, byte[] output) {
        traceOutput(srcClass, srcMethod, traceTag, output, 0, output.length);
    }
    protected static final void traceOutput(String srcClass, String srcMethod,
        String traceTag, byte[] output, int offset, int len) {
        try {
            int origlen = len;
            Level lev;
            if (!logger.isLoggable(Level.FINEST)) {
                len = Math.min(16, len);
                lev = Level.FINER;
            } else {
                lev = Level.FINEST;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(len);
            new HexDumpEncoder().encodeBuffer(
                new ByteArrayInputStream(output, offset, len), out);
            logger.logp(lev, srcClass, srcMethod, "{0} ( {1} ): {2}",
                new Object[] {traceTag, new Integer(origlen), out.toString()});
        } catch (Exception e) {
            logger.logp(Level.WARNING, srcClass, srcMethod,
                "SASLIMPL09:Error generating trace output: {0}", e);
        }
    }
    protected static final int networkByteOrderToInt(byte[] buf, int start,
        int count) {
        if (count > 4) {
            throw new IllegalArgumentException("Cannot handle more than 4 bytes");
        }
        int answer = 0;
        for (int i = 0; i < count; i++) {
            answer <<= 8;
            answer |= ((int)buf[start+i] & 0xff);
        }
        return answer;
    }
    protected static final void intToNetworkByteOrder(int num, byte[] buf,
        int start, int count) {
        if (count > 4) {
            throw new IllegalArgumentException("Cannot handle more than 4 bytes");
        }
        for (int i = count-1; i >= 0; i--) {
            buf[start+i] = (byte)(num & 0xff);
            num >>>= 8;
        }
    }
    private static final String SASL_LOGGER_NAME = "javax.security.sasl";
    protected static final String MAX_SEND_BUF = "javax.security.sasl.sendmaxbuffer";
    protected static final Logger logger = Logger.getLogger(SASL_LOGGER_NAME);
    protected static final byte NO_PROTECTION = (byte)1;
    protected static final byte INTEGRITY_ONLY_PROTECTION = (byte)2;
    protected static final byte PRIVACY_PROTECTION = (byte)4;
    protected static final byte LOW_STRENGTH = (byte)1;
    protected static final byte MEDIUM_STRENGTH = (byte)2;
    protected static final byte HIGH_STRENGTH = (byte)4;
    private static final byte[] DEFAULT_QOP = new byte[]{NO_PROTECTION};
    private static final String[] QOP_TOKENS = {"auth-conf",
                                       "auth-int",
                                       "auth"};
    private static final byte[] QOP_MASKS = {PRIVACY_PROTECTION,
                                     INTEGRITY_ONLY_PROTECTION,
                                     NO_PROTECTION};
    private static final byte[] DEFAULT_STRENGTH = new byte[]{
        HIGH_STRENGTH, MEDIUM_STRENGTH, LOW_STRENGTH};
    private static final String[] STRENGTH_TOKENS = {"low",
                                                     "medium",
                                                     "high"};
    private static final byte[] STRENGTH_MASKS = {LOW_STRENGTH,
                                                  MEDIUM_STRENGTH,
                                                  HIGH_STRENGTH};
}
