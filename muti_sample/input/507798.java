public class V2Form
    extends ASN1Encodable
{
    GeneralNames        issuerName;
    IssuerSerial        baseCertificateID;
    ObjectDigestInfo    objectDigestInfo;
    public static V2Form getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static V2Form getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof V2Form)
        {
            return (V2Form)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new V2Form((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public V2Form(
        GeneralNames    issuerName)
    {
        this.issuerName = issuerName;
    }
    public V2Form(
        ASN1Sequence seq)
    {
        if (seq.size() > 3)
        {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        int    index = 0;
        if (!(seq.getObjectAt(0) instanceof ASN1TaggedObject))
        {
            index++;
            this.issuerName = GeneralNames.getInstance(seq.getObjectAt(0));
        }
        for (int i = index; i != seq.size(); i++)
        {
            ASN1TaggedObject o = ASN1TaggedObject.getInstance(seq.getObjectAt(i));
            if (o.getTagNo() == 0)
            {
                baseCertificateID = IssuerSerial.getInstance(o, false);
            }
            else if (o.getTagNo() == 1)
            {
                objectDigestInfo = ObjectDigestInfo.getInstance(o, false);
            }
            else 
            {
                throw new IllegalArgumentException("Bad tag number: "
                        + o.getTagNo());
            }
        }
    }
    public GeneralNames getIssuerName()
    {
        return issuerName;
    }
    public IssuerSerial getBaseCertificateID()
    {
        return baseCertificateID;
    }
    public ObjectDigestInfo getObjectDigestInfo()
    {
        return objectDigestInfo;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        if (issuerName != null)
        {
            v.add(issuerName);
        }
        if (baseCertificateID != null)
        {
            v.add(new DERTaggedObject(false, 0, baseCertificateID));
        }
        if (objectDigestInfo != null)
        {
            v.add(new DERTaggedObject(false, 1, objectDigestInfo));
        }
        return new DERSequence(v);
    }
}
