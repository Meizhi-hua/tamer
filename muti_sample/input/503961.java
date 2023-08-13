public class JCEPBEKey
    implements PBEKey
{
    String              algorithm;
    DERObjectIdentifier oid;
    int                 type;
    int                 digest;
    int                 keySize;
    int                 ivSize;
    CipherParameters    param;
    PBEKeySpec          pbeKeySpec;
    boolean             tryWrong = false;
    public JCEPBEKey(
        String              algorithm,
        DERObjectIdentifier oid,
        int                 type,
        int                 digest,
        int                 keySize,
        int                 ivSize,
        PBEKeySpec          pbeKeySpec,
        CipherParameters    param)
    {
        this.algorithm = algorithm;
        this.oid = oid;
        this.type = type;
        this.digest = digest;
        this.keySize = keySize;
        this.ivSize = ivSize;
        this.pbeKeySpec = pbeKeySpec;
        this.param = param;
    }
    public String getAlgorithm()
    {
        return algorithm;
    }
    public String getFormat()
    {
        return "RAW";
    }
    public byte[] getEncoded()
    {
        if (param != null)
        {
            KeyParameter    kParam;
            if (param instanceof ParametersWithIV)
            {
                kParam = (KeyParameter)((ParametersWithIV)param).getParameters();
            }
            else
            {
                kParam = (KeyParameter)param;
            }
            return kParam.getKey();
        }
        else
        {
            if (type == PBE.PKCS12)
            {
                return PBEParametersGenerator.PKCS12PasswordToBytes(pbeKeySpec.getPassword());
            }
            else
            {   
                return PBEParametersGenerator.PKCS5PasswordToBytes(pbeKeySpec.getPassword());
            }
        }
    }
    int getType()
    {
        return type;
    }
    int getDigest()
    {
        return digest;
    }
    int getKeySize()
    {
        return keySize;
    }
    int getIvSize()
    {
        return ivSize;
    }
    CipherParameters getParam()
    {
        return param;
    }
    public char[] getPassword()
    {
        return pbeKeySpec.getPassword();
    }
    public byte[] getSalt()
    {
        return pbeKeySpec.getSalt();
    }
    public int getIterationCount()
    {
        return pbeKeySpec.getIterationCount();
    }
    public DERObjectIdentifier getOID()
    {
        return oid;
    }
    void setTryWrongPKCS12Zero(boolean tryWrong)
    {
        this.tryWrong = tryWrong; 
    }
    boolean shouldTryWrongPKCS12()
    {
        return tryWrong;
    }
}
