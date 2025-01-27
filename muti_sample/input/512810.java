public class OtherCertID
    extends ASN1Encodable
{
    private ASN1Encodable otherCertHash;
    private IssuerSerial issuerSerial;
    public static OtherCertID getInstance(Object o)
    {
        if (o == null || o instanceof OtherCertID)
        {
            return (OtherCertID) o;
        }
        else if (o instanceof ASN1Sequence)
        {
            return new OtherCertID((ASN1Sequence) o);
        }
        throw new IllegalArgumentException(
                "unknown object in 'OtherCertID' factory : "
                        + o.getClass().getName() + ".");
    }
    public OtherCertID(ASN1Sequence seq)
    {
        if (seq.size() < 1 || seq.size() > 2)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }
        if (seq.getObjectAt(0).getDERObject() instanceof ASN1OctetString)
        {
            otherCertHash = ASN1OctetString.getInstance(seq.getObjectAt(0));
        }
        else
        {
            otherCertHash = DigestInfo.getInstance(seq.getObjectAt(0));
        }
        if (seq.size() > 1)
        {
            issuerSerial = new IssuerSerial(ASN1Sequence.getInstance(seq.getObjectAt(1)));
        }
    }
    public OtherCertID(
        AlgorithmIdentifier  algId,
        byte[]               digest)
    {
        this.otherCertHash = new DigestInfo(algId, digest);
    }
    public OtherCertID(
        AlgorithmIdentifier  algId,
        byte[]               digest,
        IssuerSerial    issuerSerial)
    {
        this.otherCertHash = new DigestInfo(algId, digest);
        this.issuerSerial = issuerSerial;
    }
    public AlgorithmIdentifier getAlgorithmHash()
    {
        if (otherCertHash.getDERObject() instanceof ASN1OctetString)
        {
            return new AlgorithmIdentifier("1.3.14.3.2.26");
        }
        else
        {
            return DigestInfo.getInstance(otherCertHash).getAlgorithmId();
        }
    }
    public byte[] getCertHash()
    {
        if (otherCertHash.getDERObject() instanceof ASN1OctetString)
        {
            return ((ASN1OctetString)otherCertHash.getDERObject()).getOctets();
        }
        else
        {
            return DigestInfo.getInstance(otherCertHash).getDigest();
        }
    }
    public IssuerSerial getIssuerSerial()
    {
        return issuerSerial;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(otherCertHash);
        if (issuerSerial != null)
        {
            v.add(issuerSerial);
        }
        return new DERSequence(v);
    }
}
