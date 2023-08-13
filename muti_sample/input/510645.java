public class ASN1Integer extends ASN1Primitive {
    private static final ASN1Integer ASN1 = new ASN1Integer();
    public ASN1Integer() {
        super(TAG_INTEGER);
    }
    public static ASN1Integer getInstance() {
        return ASN1;
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readInteger();
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public Object getDecodedObject(BerInputStream in) throws IOException {
        byte[] bytesEncoded = new byte[in.length];
        System.arraycopy(in.buffer, in.contentOffset, bytesEncoded, 0,
                in.length);
        return bytesEncoded;
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeInteger();
    }
    public void setEncodingContent(BerOutputStream out) {
        out.length = ((byte[]) out.content).length;
    }
    public static int toIntValue(Object decoded) {
        return new BigInteger((byte[]) decoded).intValue();
    }
    public static BigInteger toBigIntegerValue(Object decoded) {
        return new BigInteger((byte[]) decoded);
    }
    public static Object fromIntValue(int value) {
        return BigInteger.valueOf(value).toByteArray();
    }
}
