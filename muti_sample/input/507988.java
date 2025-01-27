public class TBSCertificateStructure
    extends ASN1Encodable
    implements X509ObjectIdentifiers, PKCSObjectIdentifiers
{
    ASN1Sequence            seq;
    DERInteger              version;
    DERInteger              serialNumber;
    AlgorithmIdentifier     signature;
    X509Name                issuer;
    Time                    startDate, endDate;
    X509Name                subject;
    SubjectPublicKeyInfo    subjectPublicKeyInfo;
    DERBitString            issuerUniqueId;
    DERBitString            subjectUniqueId;
    X509Extensions          extensions;
    public static TBSCertificateStructure getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static TBSCertificateStructure getInstance(
        Object  obj)
    {
        if (obj instanceof TBSCertificateStructure)
        {
            return (TBSCertificateStructure)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new TBSCertificateStructure((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public TBSCertificateStructure(
        ASN1Sequence  seq)
    {
        int         seqStart = 0;
        this.seq = seq;
        if (seq.getObjectAt(0) instanceof DERTaggedObject)
        {
            version = DERInteger.getInstance(seq.getObjectAt(0));
        }
        else
        {
            seqStart = -1;          
            version = new DERInteger(0);
        }
        serialNumber = DERInteger.getInstance(seq.getObjectAt(seqStart + 1));
        signature = AlgorithmIdentifier.getInstance(seq.getObjectAt(seqStart + 2));
        issuer = X509Name.getInstance(seq.getObjectAt(seqStart + 3));
        ASN1Sequence  dates = (ASN1Sequence)seq.getObjectAt(seqStart + 4);
        startDate = Time.getInstance(dates.getObjectAt(0));
        endDate = Time.getInstance(dates.getObjectAt(1));
        subject = X509Name.getInstance(seq.getObjectAt(seqStart + 5));
        subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(seq.getObjectAt(seqStart + 6));
        for (int extras = seq.size() - (seqStart + 6) - 1; extras > 0; extras--)
        {
            DERTaggedObject extra = (DERTaggedObject)seq.getObjectAt(seqStart + 6 + extras);
            switch (extra.getTagNo())
            {
            case 1:
                issuerUniqueId = DERBitString.getInstance(extra, false);
                break;
            case 2:
                subjectUniqueId = DERBitString.getInstance(extra, false);
                break;
            case 3:
                extensions = X509Extensions.getInstance(extra);
            }
        }
    }
    public int getVersion()
    {
        return version.getValue().intValue() + 1;
    }
    public DERInteger getVersionNumber()
    {
        return version;
    }
    public DERInteger getSerialNumber()
    {
        return serialNumber;
    }
    public AlgorithmIdentifier getSignature()
    {
        return signature;
    }
    public X509Name getIssuer()
    {
        return issuer;
    }
    public Time getStartDate()
    {
        return startDate;
    }
    public Time getEndDate()
    {
        return endDate;
    }
    public X509Name getSubject()
    {
        return subject;
    }
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo()
    {
        return subjectPublicKeyInfo;
    }
    public DERBitString getIssuerUniqueId()
    {
        return issuerUniqueId;
    }
    public DERBitString getSubjectUniqueId()
    {
        return subjectUniqueId;
    }
    public X509Extensions getExtensions()
    {
        return extensions;
    }
    public DERObject toASN1Object()
    {
        return seq;
    }
}
