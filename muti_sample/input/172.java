public class CharToByteUTF8 extends CharToByteConverter {
    private char highHalfZoneCode;
    public int flush(byte[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        if (highHalfZoneCode != 0) {
            highHalfZoneCode = 0;
            badInputLength = 0;
            throw new MalformedInputException();
        }
        byteOff = charOff = 0;
        return 0;
    }
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws ConversionBufferFullException, MalformedInputException
    {
        char inputChar;
        byte[] outputByte = new byte[6];
        int inputSize;
        int outputSize;
        charOff = inOff;
        byteOff = outOff;
        if (highHalfZoneCode != 0) {
            inputChar = highHalfZoneCode;
            highHalfZoneCode = 0;
            if (input[inOff] >= 0xdc00 && input[inOff] <= 0xdfff) {
                int ucs4 = (highHalfZoneCode - 0xd800) * 0x400
                    + (input[inOff] - 0xdc00) + 0x10000;
                output[0] = (byte)(0xf0 | ((ucs4 >> 18)) & 0x07);
                output[1] = (byte)(0x80 | ((ucs4 >> 12) & 0x3f));
                output[2] = (byte)(0x80 | ((ucs4 >> 6) & 0x3f));
                output[3] = (byte)(0x80 | (ucs4 & 0x3f));
                charOff++;
                highHalfZoneCode = 0;
            } else {
                badInputLength = 0;
                throw new MalformedInputException();
            }
        }
        while(charOff < inEnd) {
            inputChar = input[charOff];
            if (inputChar < 0x80) {
                outputByte[0] = (byte)inputChar;
                inputSize = 1;
                outputSize = 1;
            } else if (inputChar < 0x800) {
                outputByte[0] = (byte)(0xc0 | ((inputChar >> 6) & 0x1f));
                outputByte[1] = (byte)(0x80 | (inputChar & 0x3f));
                inputSize = 1;
                outputSize = 2;
            } else if (inputChar >= 0xd800 && inputChar <= 0xdbff) {
                if (charOff + 1 >= inEnd) {
                    highHalfZoneCode = inputChar;
                    break;
                }
                char lowChar = input[charOff + 1];
                if (lowChar < 0xdc00 || lowChar > 0xdfff) {
                    badInputLength = 1;
                    throw new MalformedInputException();
                }
                int ucs4 = (inputChar - 0xd800) * 0x400 + (lowChar - 0xdc00)
                    + 0x10000;
                outputByte[0] = (byte)(0xf0 | ((ucs4 >> 18)) & 0x07);
                outputByte[1] = (byte)(0x80 | ((ucs4 >> 12) & 0x3f));
                outputByte[2] = (byte)(0x80 | ((ucs4 >> 6) & 0x3f));
                outputByte[3] = (byte)(0x80 | (ucs4 & 0x3f));
                outputSize = 4;
                inputSize = 2;
            } else {
                outputByte[0] = (byte)(0xe0 | ((inputChar >> 12)) & 0x0f);
                outputByte[1] = (byte)(0x80 | ((inputChar >> 6) & 0x3f));
                outputByte[2] = (byte)(0x80 | (inputChar & 0x3f));
                inputSize = 1;
                outputSize = 3;
            }
            if (byteOff + outputSize > outEnd) {
                throw new ConversionBufferFullException();
            }
            for (int i = 0; i < outputSize; i++) {
                output[byteOff++] = outputByte[i];
            }
            charOff += inputSize;
        }
        return byteOff - outOff;
    }
    public boolean canConvert(char ch) {
        return true;
    }
    public int getMaxBytesPerChar() {
        return 3;
    }
    public void reset() {
        byteOff = charOff = 0;
        highHalfZoneCode = 0;
    }
    public String getCharacterEncoding() {
        return "UTF8";
    }
}
