public class ASN1Boolean extends ASN1Primitive {
    private static final ASN1Boolean ASN1 = new ASN1Boolean();
    public ASN1Boolean() {
        super(TAG_BOOLEAN);
    }
    public static ASN1Boolean getInstance() {
        return ASN1;
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readBoolean();
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public Object getDecodedObject(BerInputStream in) throws IOException {
        if (in.buffer[in.contentOffset] == 0) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeBoolean();
    }
    public void setEncodingContent(BerOutputStream out) {
        out.length = 1;
    }
}
