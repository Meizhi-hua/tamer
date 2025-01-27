public abstract class CharToByteDBCS_EBCDIC extends CharToByteConverter
{
    private static final int SBCS = 0;
    private static final int DBCS = 1;
    private static final byte SO = 0x0e;
    private static final byte SI = 0x0f;
    private int  currentState;
    private char highHalfZoneCode;
    private byte[] outputByte = new byte[2];
    private DoubleByte.Encoder enc;
    public CharToByteDBCS_EBCDIC(DoubleByte.Encoder enc) {
        super();
        highHalfZoneCode = 0;
        currentState = SBCS;
        this.enc = enc;
    }
    int encodeChar(char c) {
        return enc.encodeChar(c);
    }
    public int flush(byte [] output, int outStart, int outEnd)
        throws MalformedInputException, ConversionBufferFullException
    {
        int bytesOut = 0;
        if (highHalfZoneCode != 0) {
            reset();
            badInputLength = 0;
            throw new MalformedInputException();
        }
        if (currentState == DBCS) {
          if (outStart >= outEnd)
            throw new ConversionBufferFullException();
          output[outStart] = SI;
          bytesOut++;
        }
        reset();
        return bytesOut;
    }
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
        char    inputChar;
        int     inputSize;
        byteOff = outOff;
        charOff = inOff;
        while(charOff < inEnd) {
           int   index;
           int   theBytes;
           int   spaceNeeded;
           if (highHalfZoneCode == 0) {
              inputChar = input[charOff];
              inputSize = 1;
           } else {
              inputChar = highHalfZoneCode;
              inputSize = 0;
              highHalfZoneCode = 0;
           }
           if (Character.isHighSurrogate(inputChar)) {
              if (charOff + inputSize >= inEnd) {
                 highHalfZoneCode = inputChar;
                 charOff += inputSize;
                 break;
              }
              inputChar = input[charOff + inputSize];
              if (Character.isLowSurrogate(inputChar)) {
                 if (subMode) {
                    if (subBytes.length == 1) {
                       outputByte[0] = 0x00;
                       outputByte[1] = subBytes[0];
                    }
                    else {
                       outputByte[0] = subBytes[0];
                       outputByte[1] = subBytes[1];
                    }
                    inputSize++;
                 } else {
                    badInputLength = 2;
                    throw new UnknownCharacterException();
                 }
              } else {
                 badInputLength = 1;
                 throw new MalformedInputException();
              }
           }
           else if (Character.isLowSurrogate(inputChar)) {
               badInputLength = 1;
               throw new MalformedInputException();
           } else {
               theBytes = encodeChar(inputChar);
               if (theBytes == UNMAPPABLE_ENCODING) {
                   if (subMode) {
                       if (subBytes.length == 1) {
                           outputByte[0] = 0x00;
                           outputByte[1] = subBytes[0];
                       } else {
                           outputByte[0] = subBytes[0];
                           outputByte[1] = subBytes[1];
                       }
                   } else {
                       badInputLength = 1;
                       throw new UnknownCharacterException();
                   }
               } else {
                   outputByte[0] = (byte)((theBytes & 0x0000ff00)>>8);
                   outputByte[1] = (byte)(theBytes & 0x000000ff);
               }
           }
           if (currentState == DBCS && outputByte[0] == 0x00) {
              if (byteOff >= outEnd)
                 throw new ConversionBufferFullException();
              currentState = SBCS;
              output[byteOff++] = SI;
           } else
              if (currentState == SBCS && outputByte[0] != 0x00) {
                 if (byteOff >= outEnd) {
                    throw new ConversionBufferFullException();
                 }
                 currentState = DBCS;
                 output[byteOff++] = SO;
              }
           if (currentState == DBCS)
              spaceNeeded = 2;
           else
              spaceNeeded = 1;
           if (byteOff + spaceNeeded > outEnd) {
              throw new ConversionBufferFullException();
           }
           if (currentState == SBCS)
              output[byteOff++] = outputByte[1];
           else {
              output[byteOff++] = outputByte[0];
              output[byteOff++] = outputByte[1];
           }
           charOff += inputSize;
        }
        return byteOff - outOff;
    }
    public void reset() {
       charOff = byteOff = 0;
       highHalfZoneCode = 0;
       currentState = SBCS;
    }
    public int getMaxBytesPerChar() {
       return 4;    
    }
    public void setSubstitutionBytes( byte[] newSubBytes )
       throws IllegalArgumentException
    {
       if( newSubBytes.length > 2 || newSubBytes.length == 0) {
           throw new IllegalArgumentException();
       }
       subBytes = new byte[ newSubBytes.length ];
       System.arraycopy( newSubBytes, 0, subBytes, 0, newSubBytes.length );
    }
    public boolean canConvert(char c) {
        return encodeChar(c) != UNMAPPABLE_ENCODING;
    }
}
