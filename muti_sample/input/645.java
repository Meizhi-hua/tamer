public class CharToByteCp1148 extends CharToByteSingleByte {
    private final static IBM1148 nioCoder = new IBM1148();
    public String getCharacterEncoding() {
        return "Cp1148";
    }
    public CharToByteCp1148() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
