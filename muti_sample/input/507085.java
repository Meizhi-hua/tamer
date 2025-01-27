public class ResponderID
    extends ASN1Encodable
    implements ASN1Choice
{
    private DEREncodable    value;
    public ResponderID(
        ASN1OctetString    value)
    {
        this.value = value;
    }
    public ResponderID(
        X509Name    value)
    {
        this.value = value;
    }
    public static ResponderID getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof ResponderID)
        {
            return (ResponderID)obj;
        }
        else if (obj instanceof DEROctetString)
        {
            return new ResponderID((DEROctetString)obj);
        }
        else if (obj instanceof ASN1TaggedObject)
        {
            ASN1TaggedObject    o = (ASN1TaggedObject)obj;
            if (o.getTagNo() == 1)
            {
                return new ResponderID(X509Name.getInstance(o, true));
            }
            else
            {
                return new ResponderID(ASN1OctetString.getInstance(o, true));
            }
        }
        return new ResponderID(X509Name.getInstance(obj));
    }
    public static ResponderID getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject()); 
    }
    public DERObject toASN1Object()
    {
        if (value instanceof ASN1OctetString)
        {
            return new DERTaggedObject(true, 2, value);
        }
        return new DERTaggedObject(true, 1, value);
    }
}
