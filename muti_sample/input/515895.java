public class ASN1SequenceOf extends ASN1ValueCollection {
    public ASN1SequenceOf(ASN1Type type) {
        super(TAG_SEQUENCE, type);
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readSequenceOf(this);
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public final void encodeContent(BerOutputStream out) {
        out.encodeSequenceOf(this);
    }
    public final void setEncodingContent(BerOutputStream out) {
        out.getSequenceOfLength(this);
    }
    public static ASN1SequenceOf asArrayOf(ASN1Type type) {
        return new ASN1SequenceOf(type) {
            public Object getDecodedObject(BerInputStream in)
                    throws IOException {
                return ((List) in.content).toArray();
            }
            public Collection getValues(Object object) {
                return Arrays.asList((Object[]) object);
            }
        };
    }
}
