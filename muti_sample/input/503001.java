public class ContentInfo {
    public static final int[] DATA = new int[] {1, 2, 840, 113549, 1, 7, 1};
    public static final int[] SIGNED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 2};
    public static final int[] ENVELOPED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 3};
    public static final int[] SIGNED_AND_ENVELOPED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 4};
    public static final int[] DIGESTED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 5};
    public static final int[] ENCRYPTED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 6};
    private int[] oid;
    private Object content;
    private byte[] encoding;
    public ContentInfo(int[] oid, Object content) {
        this.oid = oid;
        this.content = content;
    }
    private ContentInfo(int[] oid, Object content, byte[] encoding) {
        this.oid = oid;
        this.content = content;
        this.encoding = encoding;
    }
    public SignedData getSignedData() {
        if (Arrays.equals(oid, SIGNED_DATA)) {
            return (SignedData)content;
        }
        return null;
    }
    public Object getContent() {
        return content;
    }
    public int[] getContentType() {
        return oid;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("==== ContentInfo:"); 
        res.append("\n== ContentType (OID): "); 
        for (int i = 0; i< oid.length; i++) {
            res.append(oid[i]);
            res.append(' ');
        }
        res.append("\n== Content: ");        
        if (content != null) {
            res.append("\n"); 
            res.append(content.toString()); 
        }    
        res.append("\n== Content End"); 
        res.append("\n==== ContentInfo End\n"); 
        return res.toString();
    }
    public static final ASN1Sequence ASN1 = 
        new ASN1Sequence(new ASN1Type[] {
                ASN1Oid.getInstance(),
                new ASN1Explicit(0, ASN1Any.getInstance())
                })  {    
        {
            setOptional(1); 
        }
        protected void getValues(Object object, Object[] values) {
            ContentInfo ci = (ContentInfo) object;
            values[0] = ci.oid;
            if (ci.content != null) {
                if (Arrays.equals(ci.oid, DATA)) {
                    if (ci.content != null) {
                        values[1] = 
                            ASN1OctetString.getInstance().encode(ci.content);
                    }
                } else if (ci.content instanceof SignedData) {
                    values[1] = SignedData.ASN1.encode(ci.content);
                } else {
                    values[1] = ci.content;
                }
            }
        }
        protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            int[] oid = (int[]) values[0];
            if (Arrays.equals(oid, DATA)) {
                if (values[1] != null) {  
                    return new ContentInfo(oid, 
                            ASN1OctetString.getInstance().decode((byte[])values[1]),
                            in.getEncoded());
                }  else {
                    return new ContentInfo((int[])values[0], null,
                            in.getEncoded());
                }
            }
            if (Arrays.equals(oid, SIGNED_DATA)) {
                return new ContentInfo((int[])values[0],
                        SignedData.ASN1.decode((byte[])values[1]),
                        in.getEncoded());
            }
            return new ContentInfo((int[])values[0], (byte[])values[1],
                    in.getEncoded());
        } 
   };    
}
