public class CertID
    extends ASN1Encodable
{
    AlgorithmIdentifier    hashAlgorithm;
    ASN1OctetString        issuerNameHash;
    ASN1OctetString        issuerKeyHash;
    DERInteger             serialNumber;
    public CertID(
        AlgorithmIdentifier hashAlgorithm,
        ASN1OctetString     issuerNameHash,
        ASN1OctetString     issuerKeyHash,
        DERInteger          serialNumber)
    {
        this.hashAlgorithm = hashAlgorithm;
        this.issuerNameHash = issuerNameHash;
        this.issuerKeyHash = issuerKeyHash;
        this.serialNumber = serialNumber;
    }
    public CertID(
        ASN1Sequence    seq)
    {
        hashAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0));
        issuerNameHash = (ASN1OctetString)seq.getObjectAt(1);
        issuerKeyHash = (ASN1OctetString)seq.getObjectAt(2);
        serialNumber = (DERInteger)seq.getObjectAt(3);
    }
    public static CertID getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static CertID getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof CertID)
        {
            return (CertID)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new CertID((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public AlgorithmIdentifier getHashAlgorithm()
    {
        return hashAlgorithm;
    }
    public ASN1OctetString getIssuerNameHash()
    {
        return issuerNameHash;
    }
    public ASN1OctetString getIssuerKeyHash()
    {
        return issuerKeyHash;
    }
    public DERInteger getSerialNumber()
    {
        return serialNumber;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        v.add(hashAlgorithm);
        v.add(issuerNameHash);
        v.add(issuerKeyHash);
        v.add(serialNumber);
        return new DERSequence(v);
    }
}
