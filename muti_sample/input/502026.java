public class V3TBSCertificateGenerator
{
    DERTaggedObject         version = new DERTaggedObject(0, new DERInteger(2));
    DERInteger              serialNumber;
    AlgorithmIdentifier     signature;
    X509Name                issuer;
    Time                    startDate, endDate;
    X509Name                subject;
    SubjectPublicKeyInfo    subjectPublicKeyInfo;
    X509Extensions          extensions;
    public V3TBSCertificateGenerator()
    {
    }
    public void setSerialNumber(
        DERInteger  serialNumber)
    {
        this.serialNumber = serialNumber;
    }
    public void setSignature(
        AlgorithmIdentifier    signature)
    {
        this.signature = signature;
    }
    public void setIssuer(
        X509Name    issuer)
    {
        this.issuer = issuer;
    }
    public void setStartDate(
        DERUTCTime startDate)
    {
        this.startDate = new Time(startDate);
    }
    public void setStartDate(
        Time startDate)
    {
        this.startDate = startDate;
    }
    public void setEndDate(
        DERUTCTime endDate)
    {
        this.endDate = new Time(endDate);
    }
    public void setEndDate(
        Time endDate)
    {
        this.endDate = endDate;
    }
    public void setSubject(
        X509Name    subject)
    {
        this.subject = subject;
    }
    public void setSubjectPublicKeyInfo(
        SubjectPublicKeyInfo    pubKeyInfo)
    {
        this.subjectPublicKeyInfo = pubKeyInfo;
    }
    public void setExtensions(
        X509Extensions    extensions)
    {
        this.extensions = extensions;
    }
    public TBSCertificateStructure generateTBSCertificate()
    {
        if ((serialNumber == null) || (signature == null)
            || (issuer == null) || (startDate == null) || (endDate == null)
            || (subject == null) || (subjectPublicKeyInfo == null))
        {
            throw new IllegalStateException("not all mandatory fields set in V3 TBScertificate generator");
        }
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(version);
        v.add(serialNumber);
        v.add(signature);
        v.add(issuer);
        ASN1EncodableVector  validity = new ASN1EncodableVector();
        validity.add(startDate);
        validity.add(endDate);
        v.add(new DERSequence(validity));
        v.add(subject);
        v.add(subjectPublicKeyInfo);
        if (extensions != null)
        {
            v.add(new DERTaggedObject(3, extensions));
        }
        return new TBSCertificateStructure(new DERSequence(v));
    }
}
