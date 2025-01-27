public class AttributeCertificate
    extends ASN1Encodable
{
    AttributeCertificateInfo    acinfo;
    AlgorithmIdentifier         signatureAlgorithm;
    DERBitString                signatureValue;
    public static AttributeCertificate getInstance(Object obj)
    {
        if (obj instanceof AttributeCertificate)
        {
            return (AttributeCertificate)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new AttributeCertificate((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public AttributeCertificate(
        AttributeCertificateInfo    acinfo,
        AlgorithmIdentifier         signatureAlgorithm,
        DERBitString                signatureValue)
    {
        this.acinfo = acinfo;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureValue = signatureValue;
    }
    public AttributeCertificate(
        ASN1Sequence    seq)
    {
        if (seq.size() != 3)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }
        this.acinfo = AttributeCertificateInfo.getInstance(seq.getObjectAt(0));
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        this.signatureValue = DERBitString.getInstance(seq.getObjectAt(2));
    }
    public AttributeCertificateInfo getAcinfo()
    {
        return acinfo;
    }
    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return signatureAlgorithm;
    }
    public DERBitString getSignatureValue()
    {
        return signatureValue;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(acinfo);
        v.add(signatureAlgorithm);
        v.add(signatureValue);
        return new DERSequence(v);
    }
}
