public class EnvelopedData
    extends ASN1Encodable
{
    private DERInteger              version;
    private OriginatorInfo          originatorInfo;
    private ASN1Set                 recipientInfos;
    private EncryptedContentInfo    encryptedContentInfo;
    private ASN1Set                 unprotectedAttrs;
    public EnvelopedData(
        OriginatorInfo          originatorInfo,
        ASN1Set                 recipientInfos,
        EncryptedContentInfo    encryptedContentInfo,
        ASN1Set                 unprotectedAttrs)
    {
        if (originatorInfo != null || unprotectedAttrs != null)
        {
            version = new DERInteger(2);
        }
        else
        {
            version = new DERInteger(0);
            Enumeration e = recipientInfos.getObjects();
            while (e.hasMoreElements())
            {
                RecipientInfo   ri = RecipientInfo.getInstance(e.nextElement());
                if (!ri.getVersion().equals(version))
                {
                    version = new DERInteger(2);
                    break;
                }
            }
        }
        this.originatorInfo = originatorInfo;
        this.recipientInfos = recipientInfos;
        this.encryptedContentInfo = encryptedContentInfo;
        this.unprotectedAttrs = unprotectedAttrs;
    }
    public EnvelopedData(
        ASN1Sequence seq)
    {
        int     index = 0;
        version = (DERInteger)seq.getObjectAt(index++);
        Object  tmp = seq.getObjectAt(index++);
        if (tmp instanceof ASN1TaggedObject)
        {
            originatorInfo = OriginatorInfo.getInstance((ASN1TaggedObject)tmp, false);
            tmp = seq.getObjectAt(index++);
        }
        recipientInfos = ASN1Set.getInstance(tmp);
        encryptedContentInfo = EncryptedContentInfo.getInstance(seq.getObjectAt(index++));
        if(seq.size() > index)
        {
            unprotectedAttrs = ASN1Set.getInstance((ASN1TaggedObject)seq.getObjectAt(index), false);
        }
    }
    public static EnvelopedData getInstance(
        ASN1TaggedObject obj,
        boolean explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static EnvelopedData getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof EnvelopedData)
        {
            return (EnvelopedData)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new EnvelopedData((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Invalid EnvelopedData: " + obj.getClass().getName());
    }
    public DERInteger getVersion()
    {
        return version;
    }
    public OriginatorInfo getOriginatorInfo()
    {
        return originatorInfo;
    }
    public ASN1Set getRecipientInfos()
    {
        return recipientInfos;
    }
    public EncryptedContentInfo getEncryptedContentInfo()
    {
        return encryptedContentInfo;
    }
    public ASN1Set getUnprotectedAttrs()
    {
        return unprotectedAttrs;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(version);
        if (originatorInfo != null)
        {
            v.add(new DERTaggedObject(false, 0, originatorInfo));
        }
        v.add(recipientInfos);
        v.add(encryptedContentInfo);
        if (unprotectedAttrs != null)
        {
            v.add(new DERTaggedObject(false, 1, unprotectedAttrs));
        }
        return new BERSequence(v);
    }
}
