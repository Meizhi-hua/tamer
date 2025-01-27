public class DERT61String
    extends DERObject
    implements DERString
{
    String  string;
    public static DERT61String getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERT61String)
        {
            return (DERT61String)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERT61String(((ASN1OctetString)obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERT61String getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERT61String(
        byte[]   string)
    {
        char[]  cs = new char[string.length];
        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(string[i] & 0xff);
        }
        this.string = new String(cs);
    }
    public DERT61String(
        String   string)
    {
        this.string = string;
    }
    public String getString()
    {
        return string;
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        out.writeEncoded(T61_STRING, this.getOctets());
    }
    public byte[] getOctets()
    {
        char[]  cs = string.toCharArray();
        byte[]  bs = new byte[cs.length];
        for (int i = 0; i != cs.length; i++)
        {
            bs[i] = (byte)cs[i];
        }
        return bs; 
    }
    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DERT61String))
        {
            return false;
        }
        return this.getString().equals(((DERT61String)o).getString());
    }
    public int hashCode()
    {
        return this.getString().hashCode();
    }
}
