public class CRLDistPoint
    extends ASN1Encodable
{
    ASN1Sequence  seq = null;
    public static CRLDistPoint getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static CRLDistPoint getInstance(
        Object  obj)
    {
        if (obj instanceof CRLDistPoint)
        {
            return (CRLDistPoint)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new CRLDistPoint((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public CRLDistPoint(
        ASN1Sequence seq)
    {
        this.seq = seq;
    }
    public CRLDistPoint(
        DistributionPoint[] points)
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        for (int i = 0; i != points.length; i++)
        {
            v.add(points[i]);
        }
        seq = new DERSequence(v);
    }
    public DistributionPoint[] getDistributionPoints()
    {
        DistributionPoint[]    dp = new DistributionPoint[seq.size()];
        for (int i = 0; i != seq.size(); i++)
        {
            dp[i] = DistributionPoint.getInstance(seq.getObjectAt(i));
        }
        return dp;
    }
    public DERObject toASN1Object()
    {
        return seq;
    }
}
