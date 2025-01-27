public class JCERSACipher extends WrapCipherSpi
{
    private AsymmetricBlockCipher   cipher;
    private AlgorithmParameterSpec  paramSpec;
    private AlgorithmParameters     engineParams;
    private boolean                 publicKeyOnly = false;
    private boolean                 privateKeyOnly = false;
    private ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
    public JCERSACipher(
        AsymmetricBlockCipher   engine)
    {
        cipher = engine;
    }
    public JCERSACipher(
        OAEPParameterSpec  pSpec)
    {
        try
        {
            initFromSpec(pSpec);
        }
        catch (NoSuchPaddingException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    public JCERSACipher(
        boolean                 publicKeyOnly,
        boolean                 privateKeyOnly,
        AsymmetricBlockCipher   engine)
    {
        this.publicKeyOnly = publicKeyOnly;
        this.privateKeyOnly = privateKeyOnly;
        cipher = engine;
    }
    private void initFromSpec(
        OAEPParameterSpec pSpec) 
        throws NoSuchPaddingException
    {
        MGF1ParameterSpec   mgfParams = (MGF1ParameterSpec)pSpec.getMGFParameters();
        Digest              digest = JCEDigestUtil.getDigest(mgfParams.getDigestAlgorithm());
        if (digest == null)
        {
            throw new NoSuchPaddingException("no match on OAEP constructor for digest algorithm: "+ mgfParams.getDigestAlgorithm());
        }
        cipher = new OAEPEncoding(new RSAEngine(), digest, ((PSource.PSpecified)pSpec.getPSource()).getValue());        
        paramSpec = pSpec;
    }
    protected int engineGetBlockSize() 
    {
        try
        {
            return cipher.getInputBlockSize();
        }
        catch (NullPointerException e)
        {
            throw new IllegalStateException("RSA Cipher not initialised");
        }
    }
    protected byte[] engineGetIV() 
    {
        return null;
    }
    protected int engineGetKeySize(
        Key     key) 
    {
        if (key instanceof RSAPrivateKey)
        {
            RSAPrivateKey   k = (RSAPrivateKey)key;
            return k.getModulus().bitLength();
        }
        else if (key instanceof RSAPublicKey)
        {
            RSAPublicKey   k = (RSAPublicKey)key;
            return k.getModulus().bitLength();
        }
        throw new IllegalArgumentException("not an RSA key!");
    }
    protected int engineGetOutputSize(
        int     inputLen) 
    {
        try
        {
            return cipher.getOutputBlockSize();
        }
        catch (NullPointerException e)
        {
            throw new IllegalStateException("RSA Cipher not initialised");
        }
    }
    protected AlgorithmParameters engineGetParameters() 
    {
        if (engineParams == null)
        {
            if (paramSpec != null)
            {
                try
                {
                    engineParams = AlgorithmParameters.getInstance("OAEP", "BC");
                    engineParams.init(paramSpec);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.toString());
                }
            }
        }
        return engineParams;
    }
    protected void engineSetMode(
        String  mode)
        throws NoSuchAlgorithmException
    {
        String md = Strings.toUpperCase(mode);
        if (md.equals("NONE") || md.equals("ECB"))
        {
            return;
        }
        if (md.equals("1"))
        {
            privateKeyOnly = true;
            publicKeyOnly = false;
            return;
        }
        else if (md.equals("2"))
        {
            privateKeyOnly = false;
            publicKeyOnly = true;
            return;
        }
        throw new NoSuchAlgorithmException("can't support mode " + mode);
    }
    protected void engineSetPadding(
        String  padding) 
        throws NoSuchPaddingException
    {
        String pad = Strings.toUpperCase(padding);
        if (pad.equals("NOPADDING"))
        {
            cipher = new RSAEngine();
        }
        else if (pad.equals("PKCS1PADDING"))
        {
            cipher = new PKCS1Encoding(new RSAEngine());
        }
        else if (pad.equals("ISO9796-1PADDING"))
        {
            cipher = new ISO9796d1Encoding(new RSAEngine());
        }
        else if (pad.equals("OAEPWITHMD5ANDMGF1PADDING"))
        {
            initFromSpec(new OAEPParameterSpec("MD5", "MGF1", new MGF1ParameterSpec("MD5"), PSource.PSpecified.DEFAULT));
        }
        else if (pad.equals("OAEPPADDING"))
        {
            initFromSpec(OAEPParameterSpec.DEFAULT);
        }
        else if (pad.equals("OAEPWITHSHA1ANDMGF1PADDING"))
        {
            initFromSpec(OAEPParameterSpec.DEFAULT);
        }
        else if (pad.equals("OAEPWITHSHA224ANDMGF1PADDING"))
        {
            initFromSpec(new OAEPParameterSpec("SHA-224", "MGF1", new MGF1ParameterSpec("SHA-224"), PSource.PSpecified.DEFAULT));
        }
        else if (pad.equals("OAEPWITHSHA256ANDMGF1PADDING"))
        {
            initFromSpec(new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        }
        else if (pad.equals("OAEPWITHSHA384ANDMGF1PADDING"))
        {
            initFromSpec(new OAEPParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA384, PSource.PSpecified.DEFAULT));
        }
        else if (pad.equals("OAEPWITHSHA512ANDMGF1PADDING"))
        {
            initFromSpec(new OAEPParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, PSource.PSpecified.DEFAULT));
        }
        else
        {
            throw new NoSuchPaddingException(padding + " unavailable with RSA.");
        }
    }
    protected void engineInit(
        int                     opmode,
        Key                     key,
        AlgorithmParameterSpec  params,
        SecureRandom            random) 
    throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        CipherParameters        param;
        if (params == null || params instanceof OAEPParameterSpec)
        {
            if (key instanceof RSAPublicKey)
            {
                if (privateKeyOnly)
                {
                    throw new InvalidKeyException(
                                "mode 1 requires RSAPrivateKey");
                }
                param = RSAUtil.generatePublicKeyParameter((RSAPublicKey)key);
            }
            else if (key instanceof RSAPrivateKey)
            {
                if (publicKeyOnly)
                {
                    throw new InvalidKeyException(
                                "mode 2 requires RSAPublicKey");
                }
                param = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)key);
            }
            else
            {
                throw new InvalidKeyException("unknown key type passed to RSA");
            }
            if (params != null)
            {
                OAEPParameterSpec   spec = (OAEPParameterSpec)params;
                paramSpec = params;
                if (!spec.getMGFAlgorithm().equalsIgnoreCase("MGF1") && !spec.getMGFAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1.getId()))
                {
                    throw new InvalidAlgorithmParameterException("unknown mask generation function specified");
                }
                if (!(spec.getMGFParameters() instanceof MGF1ParameterSpec))
                {
                    throw new InvalidAlgorithmParameterException("unkown MGF parameters");
                }
                MGF1ParameterSpec   mgfParams = (MGF1ParameterSpec)spec.getMGFParameters();
                if (!JCEDigestUtil.isSameDigest(mgfParams.getDigestAlgorithm(), spec.getDigestAlgorithm()))
                {
                    throw new InvalidAlgorithmParameterException("digest algorithm for MGF should be the same as for OAEP parameters.");
                }
                Digest              digest = JCEDigestUtil.getDigest(mgfParams.getDigestAlgorithm());
                if (digest == null)
                {
                    throw new InvalidAlgorithmParameterException("no match on MGF digest algorithm: "+ mgfParams.getDigestAlgorithm());
                }
                cipher = new OAEPEncoding(new RSAEngine(), digest, ((PSource.PSpecified)spec.getPSource()).getValue());
            }
        }
        else
        {
            throw new IllegalArgumentException("unknown parameter type.");
        }
        if (!(cipher instanceof RSAEngine))
        {
            if (random != null)
            {
                param = new ParametersWithRandom(param, random);
            }
            else
            {
                param = new ParametersWithRandom(param, new SecureRandom());
            }
        }
        switch (opmode)
        {
        case Cipher.ENCRYPT_MODE:
        case Cipher.WRAP_MODE:
            cipher.init(true, param);
            break;
        case Cipher.DECRYPT_MODE:
        case Cipher.UNWRAP_MODE:
            cipher.init(false, param);
            break;
        default:
            throw new InvalidParameterException("unknown opmode " + opmode + " passed to RSA");
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
            try
            {
                paramSpec = params.getParameterSpec(OAEPParameterSpec.class);
            }
            catch (InvalidParameterSpecException e)
            {
                throw new InvalidAlgorithmParameterException("cannot recognise parameters: " + e.toString(), e);
            }
        }
        engineParams = params;
        engineInit(opmode, key, paramSpec, random);
    }
    protected void engineInit(
        int                 opmode,
        Key                 key,
        SecureRandom        random) 
    throws InvalidKeyException
    {
        try
        {
            engineInit(opmode, key, (AlgorithmParameterSpec)null, random);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new RuntimeException("Eeeek! " + e.toString(), e);
        }
    }
    protected byte[] engineUpdate(
        byte[]  input,
        int     inputOffset,
        int     inputLen) 
    {
        bOut.write(input, inputOffset, inputLen);
        if (cipher instanceof RSAEngine)
        {
            if (bOut.size() > cipher.getInputBlockSize() + 1)
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        else
        {
            if (bOut.size() > cipher.getInputBlockSize())
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        return null;
    }
    protected int engineUpdate(
        byte[]  input,
        int     inputOffset,
        int     inputLen,
        byte[]  output,
        int     outputOffset) 
    {
        bOut.write(input, inputOffset, inputLen);
        if (cipher instanceof RSAEngine)
        {
            if (bOut.size() > cipher.getInputBlockSize() + 1)
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        else
        {
            if (bOut.size() > cipher.getInputBlockSize())
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        return 0;
    }
    protected byte[] engineDoFinal(
        byte[]  input,
        int     inputOffset,
        int     inputLen) 
        throws IllegalBlockSizeException, BadPaddingException
    {
        if (input != null)
        {
            bOut.write(input, inputOffset, inputLen);
        }
        if (cipher instanceof RSAEngine)
        {
            if (bOut.size() > cipher.getInputBlockSize() + 1)
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        else
        {
            if (bOut.size() > cipher.getInputBlockSize())
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        try
        {
            byte[]  bytes = bOut.toByteArray();
            bOut.reset();
            return cipher.processBlock(bytes, 0, bytes.length);
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
        if (input != null)
        {
            bOut.write(input, inputOffset, inputLen);
        }
        if (cipher instanceof RSAEngine)
        {
            if (bOut.size() > cipher.getInputBlockSize() + 1)
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        else
        {
            if (bOut.size() > cipher.getInputBlockSize())
            {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        }
        byte[]  out;
        try
        {
            byte[]  bytes = bOut.toByteArray();
            bOut.reset();
            out = cipher.processBlock(bytes, 0, bytes.length);
        }
        catch (InvalidCipherTextException e)
        {
            throw new BadPaddingException(e.getMessage());
        }
        for (int i = 0; i != out.length; i++)
        {
            output[outputOffset + i] = out[i];
        }
        return out.length;
    }
    static public class NoPadding
        extends JCERSACipher
    {
        public NoPadding()
        {
            super(new RSAEngine());
        }
    }
    static public class PKCS1v1_5Padding
        extends JCERSACipher
    {
        public PKCS1v1_5Padding()
        {
            super(new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class PKCS1v1_5Padding_PrivateOnly
        extends JCERSACipher
    {
        public PKCS1v1_5Padding_PrivateOnly()
        {
            super(false, true, new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class PKCS1v1_5Padding_PublicOnly
        extends JCERSACipher
    {
        public PKCS1v1_5Padding_PublicOnly()
        {
            super(true, false, new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class OAEPPadding
        extends JCERSACipher
    {
        public OAEPPadding()
        {
            super(OAEPParameterSpec.DEFAULT);
        }
    }
    static public class ISO9796d1Padding
        extends JCERSACipher
    {
        public ISO9796d1Padding()
        {
            super(new ISO9796d1Encoding(new RSAEngine()));
        }
    }
}
