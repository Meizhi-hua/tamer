public class ESSCertID
    extends ASN1Encodable
{
    private ASN1OctetString certHash;
    private IssuerSerial issuerSerial;
    public static ESSCertID getInstance(Object o)
    {
        if (o == null || o instanceof ESSCertID)
        {
            return (ESSCertID)o;
        }
        else if (o instanceof ASN1Sequence)
        {
            return new ESSCertID((ASN1Sequence)o);
        }
        throw new IllegalArgumentException(
                "unknown object in 'ESSCertID' factory : "
                        + o.getClass().getName() + ".");
    }
    public ESSCertID(ASN1Sequence seq)
    {
        if (seq.size() < 1 || seq.size() > 2)
        {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        certHash = ASN1OctetString.getInstance(seq.getObjectAt(0));
        if (seq.size() > 1)
        {
            issuerSerial = IssuerSerial.getInstance(seq.getObjectAt(1));
        }
    }
    public ESSCertID(
        byte[]          hash)
    {
        certHash = new DEROctetString(hash);
    }
    public ESSCertID(
        byte[]          hash,
        IssuerSerial    issuerSerial)
    {
        this.certHash = new DEROctetString(hash);
        this.issuerSerial = issuerSerial;
    }
    public byte[] getCertHash()
    {
        return certHash.getOctets();
    }
    public IssuerSerial getIssuerSerial()
    {
        return issuerSerial;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(certHash);
        if (issuerSerial != null)
        {
            v.add(issuerSerial);
        }
        return new DERSequence(v);
    }
}
