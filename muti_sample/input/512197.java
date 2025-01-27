public class DERInteger
    extends DERObject
{
    byte[]      bytes;
    public static DERInteger getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERInteger)
        {
            return (DERInteger)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERInteger(((ASN1OctetString)obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERInteger getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERInteger(
        int         value)
    {
        bytes = BigInteger.valueOf(value).toByteArray();
    }
    public DERInteger(
        BigInteger   value)
    {
        bytes = value.toByteArray();
    }
    public DERInteger(
        byte[]   bytes)
    {
        this.bytes = bytes;
    }
    public BigInteger getValue()
    {
        return new BigInteger(bytes);
    }
    public BigInteger getPositiveValue()
    {
        return new BigInteger(1, bytes);
    }
    void encode(
        DEROutputStream out)
        throws IOException
    {
        out.writeEncoded(INTEGER, bytes);
    }
    public int hashCode()
    {
         int     value = 0;
         for (int i = 0; i != bytes.length; i++)
         {
             value ^= (bytes[i] & 0xff) << (i % 4);
         }
         return value;
    }
    public boolean equals(
        Object  o)
    {
        if (!(o instanceof DERInteger))
        {
            return false;
        }
        DERInteger other = (DERInteger)o;
        if (bytes.length != other.bytes.length)
        {
            return false;
        }
        for (int i = 0; i != bytes.length; i++)
        {
            if (bytes[i] != other.bytes[i])
            {
                return false;
            }
        }
        return true;
    }
    public String toString()
    {
      return getValue().toString();
    }
}
