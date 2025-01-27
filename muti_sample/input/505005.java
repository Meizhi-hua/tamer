public class IssuerSerial
    extends ASN1Encodable
{
    GeneralNames            issuer;
    DERInteger              serial;
    DERBitString            issuerUID;
    public static IssuerSerial getInstance(
            Object  obj)
    {
        if (obj == null || obj instanceof IssuerSerial)
        {
            return (IssuerSerial)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new IssuerSerial((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static IssuerSerial getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public IssuerSerial(
        ASN1Sequence    seq)
    {
        if (seq.size() != 2 && seq.size() != 3)
        {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        issuer = GeneralNames.getInstance(seq.getObjectAt(0));
        serial = DERInteger.getInstance(seq.getObjectAt(1));
        if (seq.size() == 3)
        {
            issuerUID = DERBitString.getInstance(seq.getObjectAt(2));
        }
    }
    public IssuerSerial(
        GeneralNames    issuer,
        DERInteger      serial)
    {
        this.issuer = issuer;
        this.serial = serial;
    }
    public GeneralNames getIssuer()
    {
        return issuer;
    }
    public DERInteger getSerial()
    {
        return serial;
    }
    public DERBitString getIssuerUID()
    {
        return issuerUID;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(issuer);
        v.add(serial);
        if (issuerUID != null)
        {
            v.add(issuerUID);
        }
        return new DERSequence(v);
    }
}
