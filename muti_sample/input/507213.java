public class DESedeEngine
    extends DESEngine
{
    protected static final int  BLOCK_SIZE = 8;
    private int[]               workingKey1 = null;
    private int[]               workingKey2 = null;
    private int[]               workingKey3 = null;
    private boolean             forEncryption;
    public DESedeEngine()
    {
    }
    public void init(
        boolean           encrypting,
        CipherParameters  params)
    {
        if (!(params instanceof KeyParameter))
        {
            throw new IllegalArgumentException("invalid parameter passed to DESede init - " + params.getClass().getName());
        }
        byte[]      keyMaster = ((KeyParameter)params).getKey();
        byte[]      key1 = new byte[8], key2 = new byte[8], key3 = new byte[8];
        if (keyMaster.length > 24)
        {
            throw new IllegalArgumentException("key size greater than 24 bytes");
        }
        this.forEncryption = encrypting;
        if (keyMaster.length == 24)
        {
            System.arraycopy(keyMaster, 0, key1, 0, key1.length);
            System.arraycopy(keyMaster, 8, key2, 0, key2.length);
            System.arraycopy(keyMaster, 16, key3, 0, key3.length);
            workingKey1 = generateWorkingKey(encrypting, key1);
            workingKey2 = generateWorkingKey(!encrypting, key2);
            workingKey3 = generateWorkingKey(encrypting, key3);
        }
        else    
        {
            System.arraycopy(keyMaster, 0, key1, 0, key1.length);
            System.arraycopy(keyMaster, 8, key2, 0, key2.length);
            workingKey1 = generateWorkingKey(encrypting, key1);
            workingKey2 = generateWorkingKey(!encrypting, key2);
            workingKey3 = workingKey1;
        }
    }
    public String getAlgorithmName()
    {
        return "DESede";
    }
    public int getBlockSize()
    {
        return BLOCK_SIZE;
    }
    public int processBlock(
        byte[] in,
        int inOff,
        byte[] out,
        int outOff)
    {
        if (workingKey1 == null)
        {
            throw new IllegalStateException("DESede engine not initialised");
        }
        if ((inOff + BLOCK_SIZE) > in.length)
        {
            throw new DataLengthException("input buffer too short");
        }
        if ((outOff + BLOCK_SIZE) > out.length)
        {
            throw new DataLengthException("output buffer too short");
        }
        if (forEncryption)
        {
            desFunc(workingKey1, in, inOff, out, outOff);
            desFunc(workingKey2, out, outOff, out, outOff);
            desFunc(workingKey3, out, outOff, out, outOff);
        }
        else
        {
            desFunc(workingKey3, in, inOff, out, outOff);
            desFunc(workingKey2, out, outOff, out, outOff);
            desFunc(workingKey1, out, outOff, out, outOff);
        }
        return BLOCK_SIZE;
    }
    public void reset()
    {
    }
}
