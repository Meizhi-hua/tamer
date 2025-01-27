public class ByteToCharJISAutoDetect extends ByteToCharConverter {
    private final static int EUCJP_MASK = 0x01;
    private final static int SJIS2B_MASK = 0x02;
    private final static int SJIS1B_MASK = 0x04;
    private final static int EUCJP_KANA1_MASK = 0x08;
    private final static int EUCJP_KANA2_MASK = 0x10;
    private final static byte[] maskTable1 = JISAutoDetect.getByteMask1();
    private final static byte[] maskTable2 = JISAutoDetect.getByteMask2();
    private final static int SS2 = 0x8e;
    private final static int SS3 = 0x8f;
    private String SJISName;
    private String EUCJPName;
    private String convName = null;
    private ByteToCharConverter detectedConv = null;
    private ByteToCharConverter defaultConv = null;
    public ByteToCharJISAutoDetect() {
        super();
        SJISName = CharacterEncoding.getSJISName();
        EUCJPName = CharacterEncoding.getEUCJPName();
        defaultConv = new ByteToCharISO8859_1();
        defaultConv.subChars = subChars;
        defaultConv.subMode = subMode;
    }
    public int flush(char [] output, int outStart, int outEnd)
        throws MalformedInputException, ConversionBufferFullException
    {
        badInputLength = 0;
        if(detectedConv != null)
             return detectedConv.flush(output, outStart, outEnd);
        else
             return defaultConv.flush(output, outStart, outEnd);
    }
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
        int num = 0;
        charOff = outOff;
        byteOff = inOff;
        try {
            if (detectedConv == null) {
                int euckana = 0;
                int ss2count = 0;
                int firstmask = 0;
                int secondmask = 0;
                int cnt;
                boolean nonAsciiFound = false;
                for (cnt = inOff; cnt < inEnd; cnt++) {
                    firstmask = 0;
                    secondmask = 0;
                    int byte1 = input[cnt]&0xff;
                    int byte2;
                    if (byte1 == 0x1b) {
                        convName = "ISO2022JP";
                        break;
                    }
                    if ((nonAsciiFound == false) && (byte1 < 0x80)) {
                        if (charOff >= outEnd)
                            throw new ConversionBufferFullException();
                        output[charOff++] = (char) byte1;
                        byteOff++;
                        num++;
                        continue;
                    }
                    nonAsciiFound = true;
                    firstmask = maskTable1[byte1];
                    if (byte1 == SS2)
                        ss2count++;
                    if (firstmask != 0) {
                        if (cnt+1 < inEnd) {
                            byte2 = input[++cnt] & 0xff;
                            secondmask = maskTable2[byte2];
                            int mask = firstmask & secondmask;
                            if (mask == EUCJP_MASK) {
                                convName = EUCJPName;
                                break;
                            }
                            if ((mask == SJIS2B_MASK) || (mask == SJIS1B_MASK)
                                || (JISAutoDetect.canBeSJIS1B(firstmask) && secondmask == 0)) {
                                convName = SJISName;
                                break;
                            }
                            if ((byte1 == SS3) && JISAutoDetect.canBeEUCJP(secondmask)) {
                                if (cnt+1 < inEnd) {
                                    int nextbyte = input[cnt+1] & 0xff;
                                    if (! JISAutoDetect.canBeEUCJP(maskTable2[nextbyte]))
                                        convName = SJISName;
                                } else
                                    convName = SJISName;
                            }
                            if (JISAutoDetect.canBeEUCKana(firstmask, secondmask))
                                euckana++;
                        } else {
                            if ((firstmask & SJIS1B_MASK) != 0) {
                                convName = SJISName;
                                break;
                            }
                        }
                    }
                }
                if (nonAsciiFound && (convName == null)) {
                    if ((euckana > 1) || (ss2count > 1))
                        convName = EUCJPName;
                    else
                        convName = SJISName;
                }
                if (convName != null) {
                    try {
                        detectedConv = ByteToCharConverter.getConverter(convName);
                        detectedConv.subChars = subChars;
                        detectedConv.subMode = subMode;
                    } catch (UnsupportedEncodingException e){
                        detectedConv = null;
                        convName = null;
                    }
                }
            }
        } catch (ConversionBufferFullException bufferFullException) {
                throw bufferFullException;
        } catch (Exception e) {
            detectedConv = defaultConv;
        }
        if (byteOff == inEnd) {
            return num;
        }
        if(detectedConv != null) {
            try {
                num += detectedConv.convert(input, inOff + num, inEnd,
                                            output, outOff + num, outEnd);
            } finally {
                charOff = detectedConv.nextCharIndex();
                byteOff = detectedConv.nextByteIndex();
                badInputLength = detectedConv.badInputLength;
            }
        } else {
            try {
                num += defaultConv.convert(input, inOff + num, inEnd,
                                           output, outOff + num, outEnd);
            } finally {
                charOff = defaultConv.nextCharIndex();
                byteOff = defaultConv.nextByteIndex();
                badInputLength = defaultConv.badInputLength;
            }
        }
        return num;
    }
    public void reset() {
        if(detectedConv != null) {
             detectedConv.reset();
             detectedConv = null;
             convName = null;
        } else
             defaultConv.reset();
        charOff = byteOff = 0;
    }
    public String getCharacterEncoding() {
        return "JISAutoDetect";
    }
    public String toString() {
        String s = getCharacterEncoding();
        if (detectedConv != null) {
            s += "[" + detectedConv.getCharacterEncoding() + "]";
        } else {
            s += "[unknown]";
        }
        return s;
    }
}
