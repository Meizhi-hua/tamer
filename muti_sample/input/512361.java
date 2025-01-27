public class EncryptedData
    extends ASN1Encodable
{
    ASN1Sequence                data;
    DERObjectIdentifier         bagId;
    DERObject                   bagValue;
    public static EncryptedData getInstance(
         Object  obj)
    {
         if (obj instanceof EncryptedData)
         {
             return (EncryptedData)obj;
         }
         else if (obj instanceof ASN1Sequence)
         {
             return new EncryptedData((ASN1Sequence)obj);
         }
         throw new IllegalArgumentException("unknown object in factory");
    }
    public EncryptedData(
        ASN1Sequence seq)
    {
        int version = ((DERInteger)seq.getObjectAt(0)).getValue().intValue();
        if (version != 0)
        {
            throw new IllegalArgumentException("sequence not version 0");
        }
        this.data = (ASN1Sequence)seq.getObjectAt(1);
    }
    public EncryptedData(
        DERObjectIdentifier     contentType,
        AlgorithmIdentifier     encryptionAlgorithm,
        DEREncodable            content)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(contentType);
        v.add(encryptionAlgorithm.getDERObject());
        v.add(new BERTaggedObject(false, 0, content));
        data = new BERSequence(v);
    }
    public DERObjectIdentifier getContentType()
    {
        return (DERObjectIdentifier)data.getObjectAt(0);
    }
    public AlgorithmIdentifier getEncryptionAlgorithm()
    {
        return AlgorithmIdentifier.getInstance(data.getObjectAt(1));
    }
    public ASN1OctetString getContent()
    {
        if (data.size() == 3)
        {
            DERTaggedObject o = (DERTaggedObject)data.getObjectAt(2);
            return ASN1OctetString.getInstance(o.getObject());
        }
        return null;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(new DERInteger(0));
        v.add(data);
        return new BERSequence(v);
    }
}
