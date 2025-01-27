public class OAEPEncoding
    implements AsymmetricBlockCipher
{
    private byte[]                  defHash;
    private Digest                  hash;
    private AsymmetricBlockCipher   engine;
    private SecureRandom            random;
    private boolean                 forEncryption;
    public OAEPEncoding(
        AsymmetricBlockCipher   cipher)
    {
        this(cipher, new SHA1Digest(), null);
    }
    public OAEPEncoding(
        AsymmetricBlockCipher       cipher,
        Digest                      hash)
    {
        this(cipher, hash, null);
    }
    public OAEPEncoding(
        AsymmetricBlockCipher       cipher,
        Digest                      hash,
        byte[]                      encodingParams)
    {
        this.engine = cipher;
        this.hash = hash;
        this.defHash = new byte[hash.getDigestSize()];
        if (encodingParams != null)
        {
            hash.update(encodingParams, 0, encodingParams.length);
        }
        hash.doFinal(defHash, 0);
    }
    public AsymmetricBlockCipher getUnderlyingCipher()
    {
        return engine;
    }
    public void init(
        boolean             forEncryption,
        CipherParameters    param)
    {
        AsymmetricKeyParameter  kParam;
        if (param instanceof ParametersWithRandom)
        {
            ParametersWithRandom  rParam = (ParametersWithRandom)param;
            this.random = rParam.getRandom();
            kParam = (AsymmetricKeyParameter)rParam.getParameters();
        }
        else
        {   
            this.random = new SecureRandom();
            kParam = (AsymmetricKeyParameter)param;
        }
        engine.init(forEncryption, kParam);
        this.forEncryption = forEncryption;
    }
    public int getInputBlockSize()
    {
        int     baseBlockSize = engine.getInputBlockSize();
        if (forEncryption)
        {
            return baseBlockSize - 1 - 2 * defHash.length;
        }
        else
        {
            return baseBlockSize;
        }
    }
    public int getOutputBlockSize()
    {
        int     baseBlockSize = engine.getOutputBlockSize();
        if (forEncryption)
        {
            return baseBlockSize;
        }
        else
        {
            return baseBlockSize - 1 - 2 * defHash.length;
        }
    }
    public byte[] processBlock(
        byte[]  in,
        int     inOff,
        int     inLen)
        throws InvalidCipherTextException
    {
        if (forEncryption)
        {
            return encodeBlock(in, inOff, inLen);
        }
        else
        {
            return decodeBlock(in, inOff, inLen);
        }
    }
    public byte[] encodeBlock(
        byte[]  in,
        int     inOff,
        int     inLen)
        throws InvalidCipherTextException
    {
        byte[]  block = new byte[getInputBlockSize() + 1 + 2 * defHash.length];
        System.arraycopy(in, inOff, block, block.length - inLen, inLen);
        block[block.length - inLen - 1] = 0x01;
        System.arraycopy(defHash, 0, block, defHash.length, defHash.length);
        byte[]  seed = new byte[defHash.length];
        random.nextBytes(seed);
        byte[]  mask = maskGeneratorFunction1(seed, 0, seed.length, block.length - defHash.length);
        for (int i = defHash.length; i != block.length; i++)
        {
            block[i] ^= mask[i - defHash.length];
        }
        System.arraycopy(seed, 0, block, 0, defHash.length);
        mask = maskGeneratorFunction1(
                        block, defHash.length, block.length - defHash.length, defHash.length);
        for (int i = 0; i != defHash.length; i++)
        {
            block[i] ^= mask[i];
        }
        return engine.processBlock(block, 0, block.length);
    }
    public byte[] decodeBlock(
        byte[]  in,
        int     inOff,
        int     inLen)
        throws InvalidCipherTextException
    {
        byte[]  data = engine.processBlock(in, inOff, inLen);
        byte[]  block = null;
        if (data.length < engine.getOutputBlockSize())
        {
            block = new byte[engine.getOutputBlockSize()];
            System.arraycopy(data, 0, block, block.length - data.length, data.length);
        }
        else
        {
            block = data;
        }
        if (block.length < (2 * defHash.length) + 1)
        {
            throw new InvalidCipherTextException("data too short");
        }
        byte[] mask = maskGeneratorFunction1(
                    block, defHash.length, block.length - defHash.length, defHash.length);
        for (int i = 0; i != defHash.length; i++)
        {
            block[i] ^= mask[i];
        }
        mask = maskGeneratorFunction1(block, 0, defHash.length, block.length - defHash.length);
        for (int i = defHash.length; i != block.length; i++)
        {
            block[i] ^= mask[i - defHash.length];
        }
        for (int i = 0; i != defHash.length; i++)
        {
            if (defHash[i] != block[defHash.length + i])
            {
                throw new InvalidCipherTextException("data hash wrong");
            }
        }
        int start;
        for (start = 2 * defHash.length; start != block.length; start++)
        {
            if (block[start] == 1 || block[start] != 0)
            {
                break;
            }
        }
        if (start >= (block.length - 1) || block[start] != 1)
        {
            throw new InvalidCipherTextException("data start wrong " + start);
        }
        start++;
        byte[]  output = new byte[block.length - start];
        System.arraycopy(block, start, output, 0, output.length);
        return output;
    }
    private void ItoOSP(
        int     i,
        byte[]  sp)
    {
        sp[0] = (byte)(i >>> 24);
        sp[1] = (byte)(i >>> 16);
        sp[2] = (byte)(i >>> 8);
        sp[3] = (byte)(i >>> 0);
    }
    private byte[] maskGeneratorFunction1(
        byte[]  Z,
        int     zOff,
        int     zLen,
        int     length)
    {
        byte[]  mask = new byte[length];
        byte[]  hashBuf = new byte[defHash.length];
        byte[]  C = new byte[4];
        int     counter = 0;
        hash.reset();
        do
        {
            ItoOSP(counter, C);
            hash.update(Z, zOff, zLen);
            hash.update(C, 0, C.length);
            hash.doFinal(hashBuf, 0);
            System.arraycopy(hashBuf, 0, mask, counter * defHash.length, defHash.length);
        }
        while (++counter < (length / defHash.length));
        if ((counter * defHash.length) < length)
        {
            ItoOSP(counter, C);
            hash.update(Z, zOff, zLen);
            hash.update(C, 0, C.length);
            hash.doFinal(hashBuf, 0);
            System.arraycopy(hashBuf, 0, mask, counter * defHash.length, mask.length - (counter * defHash.length));
        }
        return mask;
    }
}
