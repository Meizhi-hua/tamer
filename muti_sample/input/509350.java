public class IDEACBCPar
    extends ASN1Encodable
{
    ASN1OctetString  iv;
    public static IDEACBCPar getInstance(
        Object  o)
    {
        if (o instanceof IDEACBCPar)
        {
            return (IDEACBCPar)o;
        }
        else if (o instanceof ASN1Sequence)
        {
            return new IDEACBCPar((ASN1Sequence)o);
        }
        throw new IllegalArgumentException("unknown object in IDEACBCPar factory");
    }
    public IDEACBCPar(
        byte[]  iv)
    {
        this.iv = new DEROctetString(iv);
    }
    public IDEACBCPar(
        ASN1Sequence  seq)
    {
        if (seq.size() == 1)
        {
            iv = (ASN1OctetString)seq.getObjectAt(0);
        }
        else
        {
            iv = null;
        }
    }
    public byte[] getIV()
    {
        if (iv != null)
        {
            return iv.getOctets();
        }
        else
        {
            return null;
        }
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        if (iv != null)
        {
            v.add(iv);
        }
        return new DERSequence(v);
    }
}
