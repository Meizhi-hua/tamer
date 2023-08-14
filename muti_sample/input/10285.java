public class CharToByteISO8859_2 extends CharToByteSingleByte {
    private final static ISO_8859_2 nioCoder = new ISO_8859_2();
    public String getCharacterEncoding() {
        return "ISO8859_2";
    }
    public CharToByteISO8859_2() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}