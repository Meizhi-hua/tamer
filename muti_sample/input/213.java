public class CharToBytePCK extends CharToByteSJIS {
    CharToByteJIS0201 cbJIS0201 = new CharToByteJIS0201();
    short[] j0208Index1 = JIS_X_0208_Solaris_Encoder.getIndex1();
    String[] j0208Index2 = JIS_X_0208_Solaris_Encoder.getIndex2();
    public String getCharacterEncoding() {
        return "PCK";
    }
    protected int convSingleByte(char inputChar, byte[] outputByte) {
        byte b;
        if ((inputChar & 0xFF80) == 0) {
            outputByte[0] = (byte)inputChar;
            return 1;
        }
        if ((b = cbJIS0201.getNative(inputChar)) == 0)
            return 0;
        outputByte[0] = b;
        return 1;
    }
    protected int getNative(char ch) {
        int result = 0;
         switch (ch) {
            case '\u2015':
                return 0x815C;
            case '\u2014':
                return 0;
            default:
                break;
        }
        if ((result = super.getNative(ch)) != 0) {
            return result;
        } else {
            int offset = j0208Index1[ch >> 8] << 8;
            int pos = j0208Index2[offset >> 12].charAt((offset & 0xfff) + (ch & 0xff));
            if (pos != 0) {
                int c1 = (pos >> 8) & 0xff;
                int c2 = pos & 0xff;
                int rowOffset = c1 < 0x5F ? 0x70 : 0xB0;
                int cellOffset = (c1 % 2 == 1) ? (c2 > 0x5F ? 0x20 : 0x1F) : 0x7E;
                result =  ((((c1 + 1 ) >> 1) + rowOffset) << 8) | (c2 + cellOffset);
            }
        }
        return result;
    }
}
