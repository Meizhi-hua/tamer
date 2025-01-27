public class DERUniversalString
    extends DERObject
    implements DERString
{
    private static final char[]  table = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    private byte[] string;
    public static DERUniversalString getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERUniversalString)
        {
            return (DERUniversalString)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERUniversalString(((ASN1OctetString)obj).getOctets());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERUniversalString getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERUniversalString(
        byte[]   string)
    {
        this.string = string;
    }
    public String getString()
    {
        StringBuffer    buf = new StringBuffer("#");
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        ASN1OutputStream            aOut = new ASN1OutputStream(bOut);
        try
        {
            aOut.writeObject(this);
        }
        catch (IOException e)
        {
           throw new RuntimeException("internal error encoding BitString");
        }
        byte[]    string = bOut.toByteArray();
        for (int i = 0; i != string.length; i++)
        {
            buf.append(table[(string[i] >>> 4) % 0xf]);
            buf.append(table[string[i] & 0xf]);
        }
        return buf.toString();
    }
    public byte[] getOctets()
    {
        return string;
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        out.writeEncoded(UNIVERSAL_STRING, this.getOctets());
    }
    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DERUniversalString))
        {
            return false;
        }
        return this.getString().equals(((DERUniversalString)o).getString());
    }
    public int hashCode()
    {
        return this.getString().hashCode();
    }
}
