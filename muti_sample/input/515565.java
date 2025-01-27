public class V2AttributeCertificateInfoGenerator
{
    private DERInteger version;
    private Holder holder;
    private AttCertIssuer issuer;
    private AlgorithmIdentifier signature;
    private DERInteger serialNumber;
    private AttCertValidityPeriod attrCertValidityPeriod;
    private ASN1EncodableVector attributes;
    private DERBitString issuerUniqueID;
    private X509Extensions extensions;
    private DERGeneralizedTime startDate, endDate;
    public V2AttributeCertificateInfoGenerator()
    {
        this.version = new DERInteger(1);
        attributes = new ASN1EncodableVector();
    }
    public void setHolder(Holder holder)
    {
        this.holder = holder;
    }
    public void addAttribute(String oid, ASN1Encodable value) 
    {
        attributes.add(new Attribute(new DERObjectIdentifier(oid), new DERSet(value)));
    }
    public void addAttribute(Attribute attribute)
    {
        attributes.add(attribute);
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
        AttCertIssuer    issuer)
    {
        this.issuer = issuer;
    }
    public void setStartDate(
        DERGeneralizedTime startDate)
    {
        this.startDate = startDate;
    }
    public void setEndDate(
        DERGeneralizedTime endDate)
    {
        this.endDate = endDate;
    }
    public void setIssuerUniqueID(
        DERBitString    issuerUniqueID)
    {
        this.issuerUniqueID = issuerUniqueID;
    }
    public void setExtensions(
        X509Extensions    extensions)
    {
        this.extensions = extensions;
    }
    public AttributeCertificateInfo generateAttributeCertificateInfo()
    {
        if ((serialNumber == null) || (signature == null)
            || (issuer == null) || (startDate == null) || (endDate == null)
            || (holder == null) || (attributes == null))
        {
            throw new IllegalStateException("not all mandatory fields set in V2 AttributeCertificateInfo generator");
        }
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(version);
        v.add(holder);
        v.add(issuer);
        v.add(signature);
        v.add(serialNumber);
        AttCertValidityPeriod validity = new AttCertValidityPeriod(startDate, endDate);
        v.add(validity);
        v.add(new DERSequence(attributes));
        if (issuerUniqueID != null)
        {
            v.add(issuerUniqueID);
        }
        if (extensions != null)
        {
            v.add(extensions);
        }
        return new AttributeCertificateInfo(new DERSequence(v));
    }
}
