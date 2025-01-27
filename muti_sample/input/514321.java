public class CrlID
    extends ASN1Encodable
{
    DERIA5String        crlUrl;
    DERInteger          crlNum;
    DERGeneralizedTime  crlTime;
    public CrlID(
        ASN1Sequence    seq)
    {
        Enumeration    e = seq.getObjects();
        while (e.hasMoreElements())
        {
            ASN1TaggedObject    o = (ASN1TaggedObject)e.nextElement();
            switch (o.getTagNo())
            {
            case 0:
                crlUrl = DERIA5String.getInstance(o, true);
                break;
            case 1:
                crlNum = DERInteger.getInstance(o, true);
                break;
            case 2:
                crlTime = DERGeneralizedTime.getInstance(o, true);
                break;
            default:
                throw new IllegalArgumentException(
                        "unknown tag number: " + o.getTagNo());
            }
        }
    }
    public DERIA5String getCrlUrl()
    {
        return crlUrl;
    }
    public DERInteger getCrlNum()
    {
        return crlNum;
    }
    public DERGeneralizedTime getCrlTime()
    {
        return crlTime;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        if (crlUrl != null)
        {
            v.add(new DERTaggedObject(true, 0, crlUrl));
        }
        if (crlNum != null)
        {
            v.add(new DERTaggedObject(true, 1, crlNum));
        }
        if (crlTime != null)
        {
            v.add(new DERTaggedObject(true, 2, crlTime));
        }
        return new DERSequence(v);
    }
}
