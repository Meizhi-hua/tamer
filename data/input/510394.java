public class ASN1SetOf extends ASN1ValueCollection {
    public ASN1SetOf(ASN1Type type) {
        super(TAG_SETOF, type);
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readSetOf(this);
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public final void encodeContent(BerOutputStream out) {
        out.encodeSetOf(this);
    }
    public final void setEncodingContent(BerOutputStream out) {
        out.getSetOfLength(this);
    }
    public static ASN1SetOf asArrayOf(ASN1Type type) throws IOException {
        return new ASN1SetOf(type) {
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
