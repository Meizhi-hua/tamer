public class TBSRequest
    extends ASN1Encodable
{
    private static final DERInteger V1 = new DERInteger(0);
    DERInteger      version;
    GeneralName     requestorName;
    ASN1Sequence    requestList;
    X509Extensions  requestExtensions;
    public TBSRequest(
        GeneralName     requestorName,
        ASN1Sequence    requestList,
        X509Extensions  requestExtensions)
    {
        this.version = V1;
        this.requestorName = requestorName;
        this.requestList = requestList;
        this.requestExtensions = requestExtensions;
    }
    public TBSRequest(
        ASN1Sequence    seq)
    {
        int    index = 0;
        if (seq.getObjectAt(0) instanceof ASN1TaggedObject)
        {
            ASN1TaggedObject    o = (ASN1TaggedObject)seq.getObjectAt(0);
            if (o.getTagNo() == 0)
            {
                version = DERInteger.getInstance((ASN1TaggedObject)seq.getObjectAt(0), true);
                index++;
            }
            else
            {
                version = V1;
            }
        }
        else
        {
            version = V1;
        }
        if (seq.getObjectAt(index) instanceof ASN1TaggedObject)
        {
            requestorName = GeneralName.getInstance((ASN1TaggedObject)seq.getObjectAt(index++), true);
        }
        requestList = (ASN1Sequence)seq.getObjectAt(index++);
        if (seq.size() == (index + 1))
        {
            requestExtensions = X509Extensions.getInstance((ASN1TaggedObject)seq.getObjectAt(index), true);
        }
    }
    public static TBSRequest getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static TBSRequest getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof TBSRequest)
        {
            return (TBSRequest)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new TBSRequest((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public DERInteger getVersion()
    {
        return version;
    }
    public GeneralName getRequestorName()
    {
        return requestorName;
    }
    public ASN1Sequence getRequestList()
    {
        return requestList;
    }
    public X509Extensions getRequestExtensions()
    {
        return requestExtensions;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        if (!version.equals(V1))
        {
            v.add(new DERTaggedObject(true, 0, version));
        }
        if (requestorName != null)
        {
            v.add(new DERTaggedObject(true, 1, requestorName));
        }
        v.add(requestList);
        if (requestExtensions != null)
        {
            v.add(new DERTaggedObject(true, 2, requestExtensions));
        }
        return new DERSequence(v);
    }
}
