public class EncryptedContentInfo
    extends ASN1Encodable
{
    private DERObjectIdentifier contentType;
    private AlgorithmIdentifier contentEncryptionAlgorithm;
    private ASN1OctetString     encryptedContent;
    public EncryptedContentInfo(
        DERObjectIdentifier contentType, 
        AlgorithmIdentifier contentEncryptionAlgorithm,
        ASN1OctetString     encryptedContent)
    {
        this.contentType = contentType;
        this.contentEncryptionAlgorithm = contentEncryptionAlgorithm;
        this.encryptedContent = encryptedContent;
    }
    public EncryptedContentInfo(
        ASN1Sequence seq)
    {
        contentType = (DERObjectIdentifier)seq.getObjectAt(0);
        contentEncryptionAlgorithm = AlgorithmIdentifier.getInstance(
                                                        seq.getObjectAt(1));
        if (seq.size() > 2)
        {
            encryptedContent = ASN1OctetString.getInstance(
                                (ASN1TaggedObject)seq.getObjectAt(2), false);
        }
    }
    public static EncryptedContentInfo getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof EncryptedContentInfo)
        {
            return (EncryptedContentInfo)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new EncryptedContentInfo((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Invalid EncryptedContentInfo: "
                                                + obj.getClass().getName());
    }
    public DERObjectIdentifier getContentType()
    {
        return contentType;
    }
    public AlgorithmIdentifier getContentEncryptionAlgorithm()
    {
        return contentEncryptionAlgorithm;
    }
    public ASN1OctetString getEncryptedContent()
    {
        return encryptedContent;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(contentType);
        v.add(contentEncryptionAlgorithm);
        if (encryptedContent != null)
        {
            v.add(new BERTaggedObject(false, 0, encryptedContent));
        }
        return new BERSequence(v);
    }
}
