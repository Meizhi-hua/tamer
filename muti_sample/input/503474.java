public class JCEIESCipher extends WrapCipherSpi
{
    private IESEngine               cipher;
    private int                     state = -1;
    private ByteArrayOutputStream   buffer = new ByteArrayOutputStream();
    private AlgorithmParameters     engineParam = null;
    private IESParameterSpec        engineParams = null;
    private Class[]                 availableSpecs =
                                    {
                                        IESParameterSpec.class
                                    };
    public JCEIESCipher(
        IESEngine   engine)
    {
        cipher = engine;
    }
    protected int engineGetBlockSize() 
    {
        return 0;
    }
    protected byte[] engineGetIV() 
    {
        return null;
    }
    protected int engineGetKeySize(
        Key     key) 
    {
        IESKey   ieKey = (IESKey)key;
        if (ieKey.getPrivate() instanceof DHPrivateKey)
        {
            DHPrivateKey   k = (DHPrivateKey)ieKey.getPrivate();
            return k.getX().bitLength();
        }
        throw new IllegalArgumentException("not an IE key!");
    }
    protected int engineGetOutputSize(
        int     inputLen) 
    {
        if (state == Cipher.ENCRYPT_MODE || state == Cipher.WRAP_MODE)
        {
            return buffer.size() + inputLen + 20; 
        }
        else if (state == Cipher.DECRYPT_MODE || state == Cipher.UNWRAP_MODE)
        {
            return buffer.size() + inputLen - 20;
        }
        else
        {
            throw new IllegalStateException("cipher not initialised");
        }
    }
    protected AlgorithmParameters engineGetParameters() 
    {
        if (engineParam == null)
        {
            if (engineParams != null)
            {
                String  name = "IES";
                try
                {
                    engineParam = AlgorithmParameters.getInstance(name, "BC");
                    engineParam.init(engineParams);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.toString());
                }
            }
        }
        return engineParam;
    }
    protected void engineSetMode(
        String  mode) 
    {
        throw new IllegalArgumentException("can't support mode " + mode);
    }
    protected void engineSetPadding(
        String  padding) 
        throws NoSuchPaddingException
    {
        throw new NoSuchPaddingException(padding + " unavailable with RSA.");
    }
    protected void engineInit(
        int                     opmode,
        Key                     key,
        AlgorithmParameterSpec  params,
        SecureRandom            random) 
    throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        if (!(key instanceof IESKey))
        {
            throw new InvalidKeyException("must be passed IE key");
        }
        if (params == null && (opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE))
        {
            byte[]  d = new byte[16];
            byte[]  e = new byte[16];
            if (random == null)
            {
                random = new SecureRandom();
            }
            random.nextBytes(d);
            random.nextBytes(e);
            params = new IESParameterSpec(d, e, 128);
        }
        else if (!(params instanceof IESParameterSpec))
        {
            throw new InvalidAlgorithmParameterException("must be passed IES parameters");
        }
        IESKey       ieKey = (IESKey)key;
        CipherParameters pubKey;
        CipherParameters privKey;
            pubKey = DHUtil.generatePublicKeyParameter(ieKey.getPublic());
            privKey = DHUtil.generatePrivateKeyParameter(ieKey.getPrivate());
        this.engineParams = (IESParameterSpec)params;
        IESParameters       p = new IESParameters(engineParams.getDerivationV(), engineParams.getEncodingV(), engineParams.getMacKeySize());
        this.state = opmode;
        buffer.reset();
        switch (opmode)
        {
        case Cipher.ENCRYPT_MODE:
        case Cipher.WRAP_MODE:
            cipher.init(true, privKey, pubKey, p);
            break;
        case Cipher.DECRYPT_MODE:
        case Cipher.UNWRAP_MODE:
            cipher.init(false, privKey, pubKey, p);
            break;
        default:
            System.out.println("eeek!");
        }
    }
    protected void engineInit(
        int                 opmode,
        Key                 key,
        AlgorithmParameters params,
        SecureRandom        random) 
    throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        AlgorithmParameterSpec  paramSpec = null;
        if (params != null)
        {
            for (int i = 0; i != availableSpecs.length; i++)
            {
                try
                {
                    paramSpec = params.getParameterSpec(availableSpecs[i]);
                    break;
                }
                catch (Exception e)
                {
                    continue;
                }
            }
            if (paramSpec == null)
            {
                throw new InvalidAlgorithmParameterException("can't handle parameter " + params.toString());
            }
        }
        engineParam = params;
        engineInit(opmode, key, paramSpec, random);
    }
    protected void engineInit(
        int                 opmode,
        Key                 key,
        SecureRandom        random) 
    throws InvalidKeyException
    {
        if (opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE)
        {
            try
            {
                engineInit(opmode, key, (AlgorithmParameterSpec)null, random);
                return;
            }
            catch (InvalidAlgorithmParameterException e)
            {
            }
        }
        throw new IllegalArgumentException("can't handle null parameter spec in IES");
    }
    protected byte[] engineUpdate(
        byte[]  input,
        int     inputOffset,
        int     inputLen) 
    {
        buffer.write(input, inputOffset, inputLen);
        return null;
    }
    protected int engineUpdate(
        byte[]  input,
        int     inputOffset,
        int     inputLen,
        byte[]  output,
        int     outputOffset) 
    {
        buffer.write(input, inputOffset, inputLen);
        return 0;
    }
    protected byte[] engineDoFinal(
        byte[]  input,
        int     inputOffset,
        int     inputLen) 
        throws IllegalBlockSizeException, BadPaddingException
    {
        if (inputLen != 0)
        {
            buffer.write(input, inputOffset, inputLen);
        }
        try
        {
            byte[]  buf = buffer.toByteArray();
            buffer.reset();
            return cipher.processBlock(buf, 0, buf.length);
        }
        catch (InvalidCipherTextException e)
        {
            throw new BadPaddingException(e.getMessage());
        }
    }
    protected int engineDoFinal(
        byte[]  input,
        int     inputOffset,
        int     inputLen,
        byte[]  output,
        int     outputOffset) 
        throws IllegalBlockSizeException, BadPaddingException
    {
        if (inputLen != 0)
        {
            buffer.write(input, inputOffset, inputLen);
        }
        try
        {
            byte[]  buf = buffer.toByteArray();
            buffer.reset();
            buf = cipher.processBlock(buf, 0, buf.length);
            System.arraycopy(buf, 0, output, outputOffset, buf.length);
            return buf.length;
        }
        catch (InvalidCipherTextException e)
        {
            throw new BadPaddingException(e.getMessage());
        }
    }
    static public class BrokenIES
        extends JCEIESCipher
    {
        public BrokenIES()
        {
            super(new IESEngine(
                   new DHBasicAgreement(),
                   new BrokenKDF2BytesGenerator(new SHA1Digest()),
                   new HMac(new SHA1Digest())));
        }
    }
    static public class IES
        extends JCEIESCipher
    {
        public IES()
        {
            super(new IESEngine(
                   new DHBasicAgreement(),
                   new KDF2BytesGenerator(new SHA1Digest()),
                   new HMac(new SHA1Digest())));
        }
    }
}
