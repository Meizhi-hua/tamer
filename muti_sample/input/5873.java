public class CharToByteGB18030 extends CharToByteConverter
{
    private char highHalfZoneCode;
    boolean flushed = true;
    private final static int GB18030_SINGLE_BYTE = 1;
    private final static int GB18030_DOUBLE_BYTE = 2;
    private final static int GB18030_FOUR_BYTE = 3;
    private static short[] index1;
    private static String[] index2;
    private int currentState;
    public CharToByteGB18030() {
        GB18030 nioCoder = new GB18030();
        currentState = GB18030_DOUBLE_BYTE;
        subBytes = new byte[1];
        subBytes[0] = (byte)'?';
        index1 = nioCoder.getEncoderIndex1();
        index2 = nioCoder.getEncoderIndex2();
    }
    public int flush(byte[] output, int outStart, int outEnd)
        throws MalformedInputException
    {
        if (highHalfZoneCode != 0) {
            highHalfZoneCode = 0;
            badInputLength = 0;
            throw new MalformedInputException();
        }
        reset();
        flushed = true;
        return 0;
    }
    public void reset() {
        byteOff = charOff = 0;
        currentState = GB18030_DOUBLE_BYTE;
    }
    public boolean canConvert(char c) {
        if ((c >= 0xd800 && c <=0xdfff) || (c >= 0xfffe))
            return false;
        else
            return true;
    }
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
        int linearDiffValue = 0;
        int hiByte = 0 , loByte = 0;  
        char inputChar;  
        charOff = inOff;
        byteOff = outOff;
        int inputSize;  
        int outputSize; 
        flushed = false;
        if (highHalfZoneCode != 0) {
            if (input[inOff] >= 0xDC00 && input[inOff] <= 0xDFFF) {
                char[] newBuf = new char[inEnd - inOff + 1];
                newBuf[0] = highHalfZoneCode;
                System.arraycopy(input, inOff, newBuf, 1, inEnd - inOff);
                charOff -= 1;
                input = newBuf;
                inOff = 0;
                inEnd = newBuf.length;
                highHalfZoneCode = 0;
            } else {
                badInputLength = 0;
                throw new MalformedInputException();
            }
        }
        while (charOff < inEnd) {
            inputChar = input[charOff++];
            if(inputChar >= '\uD800' && inputChar <= '\uDBFF') {
                if (charOff + 1 > inEnd) {
                    highHalfZoneCode = inputChar;
                    break;
                }
                char previousChar = inputChar;
                inputChar = input[charOff];
                if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                    inputSize = 2;
                    charOff++;
                    linearDiffValue = ( previousChar - 0xD800) * 0x400 +
                                ( inputChar - 0xDC00) + 0x2E248;
                    currentState = GB18030_FOUR_BYTE;
                } else {
                    badInputLength = 1;
                    throw new MalformedInputException();
                }
            }
            else if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                badInputLength = 1;
                throw new MalformedInputException();
            }
            else if (inputChar >= 0x0000 && inputChar <= 0x007F) {
                if (byteOff >= outEnd) {
                   throw new ConversionBufferFullException();
                }
                currentState = GB18030_SINGLE_BYTE;
                output[byteOff++] = (byte) inputChar;
            }
            else if (inputChar <= 0xA4C6 || inputChar >= 0xE000) {
                int outByteVal = getGB18030(index1, index2, inputChar);
                if (outByteVal == 0xFFFD ) {
                    if (subMode) {
                        if (byteOff >= outEnd) {
                           throw new ConversionBufferFullException();
                        } else {
                            output[byteOff++] = subBytes[0];
                            continue;
                        }
                    } else {
                        badInputLength = 1;
                        throw new UnknownCharacterException();
                    }
                }
                hiByte = (outByteVal & 0xFF00) >> 8;
                loByte = (outByteVal & 0xFF);
                linearDiffValue = (hiByte - 0x20) * 256 + loByte;
                if (inputChar >= 0xE000 && inputChar < 0xF900)
                        linearDiffValue += 0x82BD;
                else if (inputChar >= 0xF900)
                        linearDiffValue += 0x93A9;
                if (hiByte > 0x80)
                     currentState = GB18030_DOUBLE_BYTE;
                else
                     currentState = GB18030_FOUR_BYTE;
            }
            else if (inputChar >= 0xA4C7 && inputChar <= 0xD7FF) {
                linearDiffValue = inputChar - 0x5543;
                currentState = GB18030_FOUR_BYTE;
            }
            else {
                badInputLength = 1;
                throw new MalformedInputException();
            }
            if (currentState == GB18030_SINGLE_BYTE)
                continue;
            if (currentState == GB18030_DOUBLE_BYTE) {
                if (byteOff + 2 > outEnd) {
                    throw new ConversionBufferFullException();
                }
                output[byteOff++] = (byte)hiByte;
                output[byteOff++] = (byte)loByte;
            }
            else { 
                if (byteOff + 4 > outEnd) {
                    throw new ConversionBufferFullException();
                }
                byte b1, b2, b3, b4;
                b4 = (byte)((linearDiffValue % 10) + 0x30);
                linearDiffValue /= 10;
                b3 = (byte)((linearDiffValue % 126) + 0x81);
                linearDiffValue /= 126;
                b2 = (byte)((linearDiffValue % 10) + 0x30);
                b1 = (byte)((linearDiffValue / 10) + 0x81);
                output[byteOff++] = b1;
                output[byteOff++] = b2;
                output[byteOff++] = b3;
                output[byteOff++] = b4;
            }
        }
        return byteOff - outOff;
    }
    public int getMaxBytesPerChar() {
        return 4;
    }
    public String getCharacterEncoding() {
        return "GB18030";
    }
    private int getGB18030(short[] outerIndex, String[] innerIndex, char ch) {
        int offset = outerIndex[((ch & 0xff00) >> 8 )] << 8;
        return innerIndex[offset >> 12].charAt((offset & 0xfff) + (ch & 0xff));
    }
}
