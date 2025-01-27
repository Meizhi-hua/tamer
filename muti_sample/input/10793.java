public class ByteToCharUTF8 extends ByteToCharConverter {
    private int savedSize;
    private byte[] savedBytes;
    public ByteToCharUTF8() {
        super();
        savedSize = 0;
        savedBytes = new byte[5];
    }
    public int flush(char[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        if (savedSize != 0) {
            savedSize = 0;
            badInputLength = 0;
            throw new MalformedInputException();
        }
        byteOff = charOff = 0;
        return 0;
    }
    public int convert(byte[] input, int inOff, int inEnd,
                       char[] output, int outOff, int outEnd)
        throws MalformedInputException, ConversionBufferFullException
    {
        int byte1, byte2, byte3, byte4;
        char[] outputChar = new char[2];
        int outputSize;
        int byteOffAdjustment = 0;
        if (savedSize != 0) {
            byte[] newBuf;
            newBuf = new byte[inEnd - inOff + savedSize];
            for (int i = 0; i < savedSize; i++) {
                newBuf[i] = savedBytes[i];
            }
            System.arraycopy(input, inOff, newBuf, savedSize, inEnd - inOff);
            input = newBuf;
            inOff = 0;
            inEnd = newBuf.length;
            byteOffAdjustment = -savedSize;
            savedSize = 0;
        }
        charOff = outOff;
        byteOff = inOff;
        int startByteOff;
        while(byteOff < inEnd) {
            startByteOff = byteOff;
            byte1 = input[byteOff++] & 0xff;
            if ((byte1 & 0x80) == 0){
                outputChar[0] = (char)byte1;
                outputSize = 1;
            } else if ((byte1 & 0xe0) == 0xc0) {
                if (byteOff >= inEnd) {
                    savedSize = 1;
                    savedBytes[0] = (byte)byte1;
                    break;
                }
                byte2 = input[byteOff++] & 0xff;
                if ((byte2 & 0xc0) != 0x80) {
                    badInputLength = 2;
                    byteOff += byteOffAdjustment;
                    throw new MalformedInputException();
                }
                outputChar[0] = (char)(((byte1 & 0x1f) << 6) | (byte2 & 0x3f));
                outputSize = 1;
            } else if ((byte1 & 0xf0) == 0xe0){
                if (byteOff + 1 >= inEnd) {
                        savedBytes[0] = (byte)byte1;
                    if (byteOff >= inEnd) {
                        savedSize = 1;
                    } else {
                        savedSize = 2;
                        savedBytes[1] = input[byteOff++];
                    }
                    break;
                }
                byte2 = input[byteOff++] & 0xff;
                byte3 = input[byteOff++] & 0xff;
                if ((byte2 & 0xc0) != 0x80 || (byte3 & 0xc0) != 0x80) {
                    badInputLength = 3;
                    byteOff += byteOffAdjustment;
                    throw new MalformedInputException();
                }
                outputChar[0] = (char)(((byte1 & 0x0f) << 12)
                                       | ((byte2 & 0x3f) << 6)
                                       | (byte3 & 0x3f));
                outputSize = 1;
            } else if ((byte1 & 0xf8) == 0xf0) {
                if (byteOff + 2 >= inEnd) {
                    savedBytes[0] = (byte)byte1;
                    if (byteOff >= inEnd) {
                        savedSize = 1;
                    } else if (byteOff + 1 >= inEnd) {
                        savedSize = 2;
                        savedBytes[1] = input[byteOff++];
                    } else {
                        savedSize = 3;
                        savedBytes[1] = input[byteOff++];
                        savedBytes[2] = input[byteOff++];
                    }
                    break;
                }
                byte2 = input[byteOff++] & 0xff;
                byte3 = input[byteOff++] & 0xff;
                byte4 = input[byteOff++] & 0xff;
                if ((byte2 & 0xc0) != 0x80 ||
                    (byte3 & 0xc0) != 0x80 ||
                    (byte4 & 0xc0) != 0x80) {
                    badInputLength = 4;
                    byteOff += byteOffAdjustment;
                    throw new MalformedInputException();
                }
                int ucs4 = (0x07 & byte1) << 18 |
                           (0x3f & byte2) << 12 |
                           (0x3f & byte3) <<  6 |
                           (0x3f & byte4);
                outputChar[0] = (char)((ucs4 - 0x10000) / 0x400 + 0xd800);
                outputChar[1] = (char)((ucs4 - 0x10000) % 0x400 + 0xdc00);
                outputSize = 2;
            } else {
                badInputLength = 1;
                byteOff += byteOffAdjustment;
                throw new MalformedInputException();
            }
            if (charOff + outputSize > outEnd) {
                byteOff = startByteOff;
                byteOff += byteOffAdjustment;
                throw new ConversionBufferFullException();
            }
            for (int i = 0; i < outputSize; i++) {
                output[charOff + i] = outputChar[i];
            }
            charOff += outputSize;
        }
        byteOff += byteOffAdjustment;
        return charOff - outOff;
    }
    public String getCharacterEncoding() {
        return "UTF8";
    }
    public void reset() {
        byteOff = charOff = 0;
        savedSize = 0;
    }
}
