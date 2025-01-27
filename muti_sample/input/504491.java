public class X509ExtensionUtil
{
    public static ASN1Encodable fromExtensionValue(
        byte[]  encodedValue) 
        throws IOException
    {
        ASN1InputStream aIn = new ASN1InputStream(encodedValue);
        aIn = new ASN1InputStream(((ASN1OctetString)aIn.readObject()).getOctets());
        return (ASN1Encodable)aIn.readObject();
    }
}
