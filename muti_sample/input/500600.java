public class DSAPublicKeyParameters
    extends DSAKeyParameters
{
    private BigInteger      y;
    public DSAPublicKeyParameters(
        BigInteger      y,
        DSAParameters   params)
    {
        super(false, params);
        this.y = y;
    }   
    public BigInteger getY()
    {
        return y;
    }
}
