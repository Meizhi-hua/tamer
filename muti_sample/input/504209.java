public class DESParameters
    extends KeyParameter
{
    public DESParameters(
        byte[]  key)
    {
        super(key);
        if (isWeakKey(key, 0))
        {
            throw new IllegalArgumentException("attempt to create weak DES key");
        }
    }
    static public final int DES_KEY_LENGTH = 8;
    static private final int N_DES_WEAK_KEYS = 16;
    static private byte[] DES_weak_keys =
    {
        (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
        (byte)0x1f,(byte)0x1f,(byte)0x1f,(byte)0x1f, (byte)0x0e,(byte)0x0e,(byte)0x0e,(byte)0x0e,
        (byte)0xe0,(byte)0xe0,(byte)0xe0,(byte)0xe0, (byte)0xf1,(byte)0xf1,(byte)0xf1,(byte)0xf1,
        (byte)0xfe,(byte)0xfe,(byte)0xfe,(byte)0xfe, (byte)0xfe,(byte)0xfe,(byte)0xfe,(byte)0xfe,
        (byte)0x01,(byte)0xfe,(byte)0x01,(byte)0xfe, (byte)0x01,(byte)0xfe,(byte)0x01,(byte)0xfe,
        (byte)0x1f,(byte)0xe0,(byte)0x1f,(byte)0xe0, (byte)0x0e,(byte)0xf1,(byte)0x0e,(byte)0xf1,
        (byte)0x01,(byte)0xe0,(byte)0x01,(byte)0xe0, (byte)0x01,(byte)0xf1,(byte)0x01,(byte)0xf1,
        (byte)0x1f,(byte)0xfe,(byte)0x1f,(byte)0xfe, (byte)0x0e,(byte)0xfe,(byte)0x0e,(byte)0xfe,
        (byte)0x01,(byte)0x1f,(byte)0x01,(byte)0x1f, (byte)0x01,(byte)0x0e,(byte)0x01,(byte)0x0e,
        (byte)0xe0,(byte)0xfe,(byte)0xe0,(byte)0xfe, (byte)0xf1,(byte)0xfe,(byte)0xf1,(byte)0xfe,
        (byte)0xfe,(byte)0x01,(byte)0xfe,(byte)0x01, (byte)0xfe,(byte)0x01,(byte)0xfe,(byte)0x01,
        (byte)0xe0,(byte)0x1f,(byte)0xe0,(byte)0x1f, (byte)0xf1,(byte)0x0e,(byte)0xf1,(byte)0x0e,
        (byte)0xe0,(byte)0x01,(byte)0xe0,(byte)0x01, (byte)0xf1,(byte)0x01,(byte)0xf1,(byte)0x01,
        (byte)0xfe,(byte)0x1f,(byte)0xfe,(byte)0x1f, (byte)0xfe,(byte)0x0e,(byte)0xfe,(byte)0x0e,
        (byte)0x1f,(byte)0x01,(byte)0x1f,(byte)0x01, (byte)0x0e,(byte)0x01,(byte)0x0e,(byte)0x01,
        (byte)0xfe,(byte)0xe0,(byte)0xfe,(byte)0xe0, (byte)0xfe,(byte)0xf1,(byte)0xfe,(byte)0xf1
    };
    public static boolean isWeakKey(
        byte[] key,
        int offset)
    {
        if (key.length - offset < DES_KEY_LENGTH)
        {
            throw new IllegalArgumentException("key material too short.");
        }
        nextkey: for (int i = 0; i < N_DES_WEAK_KEYS; i++)
        {
            for (int j = 0; j < DES_KEY_LENGTH; j++)
            {
                if (key[j + offset] != DES_weak_keys[i * DES_KEY_LENGTH + j])
                {
                    continue nextkey;
                }
            }
            return true;
        }
        return false;
    }
    public static void setOddParity(
        byte[] bytes)
    {
        for (int i = 0; i < bytes.length; i++)
        {
            int b = bytes[i];
            bytes[i] = (byte)((b & 0xfe) |
                            ((((b >> 1) ^
                            (b >> 2) ^
                            (b >> 3) ^
                            (b >> 4) ^
                            (b >> 5) ^
                            (b >> 6) ^
                            (b >> 7)) ^ 0x01) & 0x01));
        }
    }
}
