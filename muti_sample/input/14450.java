public abstract class CksumType {
    private static boolean DEBUG = Krb5.DEBUG;
    public static CksumType getInstance(int cksumTypeConst)
        throws KdcErrException {
        CksumType cksumType = null;
        String cksumTypeName = null;
        switch (cksumTypeConst) {
        case Checksum.CKSUMTYPE_CRC32:
            cksumType = new Crc32CksumType();
            cksumTypeName = "sun.security.krb5.internal.crypto.Crc32CksumType";
            break;
        case Checksum.CKSUMTYPE_DES_MAC:
            cksumType = new DesMacCksumType();
            cksumTypeName = "sun.security.krb5.internal.crypto.DesMacCksumType";
            break;
        case Checksum.CKSUMTYPE_DES_MAC_K:
            cksumType = new DesMacKCksumType();
            cksumTypeName =
                "sun.security.krb5.internal.crypto.DesMacKCksumType";
            break;
        case Checksum.CKSUMTYPE_RSA_MD5:
            cksumType = new RsaMd5CksumType();
            cksumTypeName = "sun.security.krb5.internal.crypto.RsaMd5CksumType";
            break;
        case Checksum.CKSUMTYPE_RSA_MD5_DES:
            cksumType = new RsaMd5DesCksumType();
            cksumTypeName =
                "sun.security.krb5.internal.crypto.RsaMd5DesCksumType";
            break;
        case Checksum.CKSUMTYPE_HMAC_SHA1_DES3_KD:
            cksumType = new HmacSha1Des3KdCksumType();
            cksumTypeName =
                "sun.security.krb5.internal.crypto.HmacSha1Des3KdCksumType";
            break;
        case Checksum.CKSUMTYPE_HMAC_SHA1_96_AES128:
            cksumType = new HmacSha1Aes128CksumType();
            cksumTypeName =
                "sun.security.krb5.internal.crypto.HmacSha1Aes128CksumType";
            break;
        case Checksum.CKSUMTYPE_HMAC_SHA1_96_AES256:
            cksumType = new HmacSha1Aes256CksumType();
            cksumTypeName =
                "sun.security.krb5.internal.crypto.HmacSha1Aes256CksumType";
            break;
        case Checksum.CKSUMTYPE_HMAC_MD5_ARCFOUR:
            cksumType = new HmacMd5ArcFourCksumType();
            cksumTypeName =
                "sun.security.krb5.internal.crypto.HmacMd5ArcFourCksumType";
            break;
        case Checksum.CKSUMTYPE_RSA_MD4_DES_K:
        case Checksum.CKSUMTYPE_RSA_MD4:
        case Checksum.CKSUMTYPE_RSA_MD4_DES:
        default:
            throw new KdcErrException(Krb5.KDC_ERR_SUMTYPE_NOSUPP);
        }
        if (DEBUG) {
            System.out.println(">>> CksumType: " + cksumTypeName);
        }
        return cksumType;
    }
    public static CksumType getInstance() throws KdcErrException {
        int cksumType = Checksum.CKSUMTYPE_RSA_MD5; 
        try {
            Config c = Config.getInstance();
            if ((cksumType = (c.getType(c.getDefault("ap_req_checksum_type",
                                "libdefaults")))) == - 1) {
                if ((cksumType = c.getType(c.getDefault("checksum_type",
                                "libdefaults"))) == -1) {
                    cksumType = Checksum.CKSUMTYPE_RSA_MD5; 
                }
            }
        } catch (KrbException e) {
        }
        return getInstance(cksumType);
    }
    public abstract int confounderSize();
    public abstract int cksumType();
    public abstract boolean isSafe();
    public abstract int cksumSize();
    public abstract int keyType();
    public abstract int keySize();
    public abstract byte[] calculateChecksum(byte[] data, int size)
        throws KrbCryptoException;
    public abstract byte[] calculateKeyedChecksum(byte[] data, int size,
        byte[] key, int usage) throws KrbCryptoException;
    public abstract boolean verifyKeyedChecksum(byte[] data, int size,
        byte[] key, byte[] checksum, int usage) throws KrbCryptoException;
    public static boolean isChecksumEqual(byte[] cksum1, byte[] cksum2) {
        if (cksum1 == cksum2)
            return true;
        if ((cksum1 == null && cksum2 != null) ||
            (cksum1 != null && cksum2 == null))
            return false;
        if (cksum1.length != cksum2.length)
            return false;
        for (int i = 0; i < cksum1.length; i++)
            if (cksum1[i] != cksum2[i])
                return false;
        return true;
    }
}
