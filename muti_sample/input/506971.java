public class RevokedInfo
    extends ASN1Encodable
{
    private DERGeneralizedTime  revocationTime;
    private CRLReason           revocationReason;
    public RevokedInfo(
        DERGeneralizedTime  revocationTime,
        CRLReason           revocationReason)
    {
        this.revocationTime = revocationTime;
        this.revocationReason = revocationReason;
    }
    public RevokedInfo(
        ASN1Sequence    seq)
    {
        this.revocationTime = (DERGeneralizedTime)seq.getObjectAt(0);
        if (seq.size() > 1)
        {
            this.revocationReason = new CRLReason(DEREnumerated.getInstance(
                                (ASN1TaggedObject)seq.getObjectAt(1), true));
        }
    }
    public static RevokedInfo getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static RevokedInfo getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof RevokedInfo)
        {
            return (RevokedInfo)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new RevokedInfo((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public DERGeneralizedTime getRevocationTime()
    {
        return revocationTime;
    }
    public CRLReason getRevocationReason()
    {
        return revocationReason;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(revocationTime);
        if (revocationReason != null)
        {
            v.add(new DERTaggedObject(true, 0, revocationReason));
        }
        return new DERSequence(v);
    }
}
