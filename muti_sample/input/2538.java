public final class ArcFourHmacEType extends EType {
    public int eType() {
        return EncryptedData.ETYPE_ARCFOUR_HMAC;
    }
    public int minimumPadSize() {
        return 1;
    }
    public int confounderSize() {
        return 8;
    }
    public int checksumType() {
        return Checksum.CKSUMTYPE_HMAC_MD5_ARCFOUR;
    }
    public int checksumSize() {
        return ArcFourHmac.getChecksumLength();
    }
    public int blockSize() {
        return 1;
    }
    public int keyType() {
        return Krb5.KEYTYPE_ARCFOUR_HMAC;
    }
    public int keySize() {
        return 16; 
    }
    public byte[] encrypt(byte[] data, byte[] key, int usage)
        throws KrbCryptoException {
        byte[] ivec = new byte[blockSize()];
        return encrypt(data, key, ivec, usage);
    }
    public byte[] encrypt(byte[] data, byte[] key, byte[] ivec, int usage)
        throws KrbCryptoException {
        try {
            return ArcFourHmac.encrypt(key, usage, ivec, data, 0, data.length);
        } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
        }
    }
    public byte[] decrypt(byte[] cipher, byte[] key, int usage)
        throws KrbApErrException, KrbCryptoException {
        byte[] ivec = new byte[blockSize()];
        return decrypt(cipher, key, ivec, usage);
    }
    public byte[] decrypt(byte[] cipher, byte[] key, byte[] ivec, int usage)
        throws KrbApErrException, KrbCryptoException {
        try {
            return ArcFourHmac.decrypt(key, usage, ivec, cipher, 0, cipher.length);
        } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
        }
    }
    public byte[] decryptedData(byte[] data) {
        return data;
    }
}