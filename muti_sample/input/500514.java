public class BasicOCSPResponse
    extends ASN1Encodable
{
    private ResponseData        tbsResponseData;
    private AlgorithmIdentifier signatureAlgorithm;
    private DERBitString        signature;
    private ASN1Sequence        certs;
    public BasicOCSPResponse(
        ResponseData        tbsResponseData,
        AlgorithmIdentifier signatureAlgorithm,
        DERBitString        signature,
        ASN1Sequence        certs)
    {
        this.tbsResponseData = tbsResponseData;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signature = signature;
        this.certs = certs;
    }
    public BasicOCSPResponse(
        ASN1Sequence    seq)
    {
        this.tbsResponseData = ResponseData.getInstance(seq.getObjectAt(0));
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        this.signature = (DERBitString)seq.getObjectAt(2);
        if (seq.size() > 3)
        {
            this.certs = ASN1Sequence.getInstance((ASN1TaggedObject)seq.getObjectAt(3), true);
        }
    }
    public static BasicOCSPResponse getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static BasicOCSPResponse getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof BasicOCSPResponse)
        {
            return (BasicOCSPResponse)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new BasicOCSPResponse((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public ResponseData getTbsResponseData()
    {
        return tbsResponseData;
    }
    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return signatureAlgorithm;
    }
    public DERBitString getSignature()
    {
        return signature;
    }
    public ASN1Sequence getCerts()
    {
        return certs;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(tbsResponseData);
        v.add(signatureAlgorithm);
        v.add(signature);
        if (certs != null)
        {
            v.add(new DERTaggedObject(true, 0, certs));
        }
        return new DERSequence(v);
    }
}
