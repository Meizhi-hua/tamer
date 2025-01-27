public final class SunJCE extends Provider {
    private static final long serialVersionUID = 6812507587804302833L;
    private static final String info = "SunJCE Provider " +
    "(implements RSA, DES, Triple DES, AES, Blowfish, ARCFOUR, RC2, PBE, "
    + "Diffie-Hellman, HMAC)";
    private static final String OID_PKCS12_RC2_40 = "1.2.840.113549.1.12.1.6";
    private static final String OID_PKCS12_DESede = "1.2.840.113549.1.12.1.3";
    private static final String OID_PKCS5_MD5_DES = "1.2.840.113549.1.5.3";
    private static final String OID_PKCS5_PBKDF2 = "1.2.840.113549.1.5.12";
    private static final String OID_PKCS3 = "1.2.840.113549.1.3.1";
    static final boolean debug = false;
    static final SecureRandom RANDOM = new SecureRandom();
    public SunJCE() {
        super("SunJCE", 1.7d, info);
        final String BLOCK_MODES = "ECB|CBC|PCBC|CTR|CTS|CFB|OFB" +
            "|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64" +
            "|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64";
        final String BLOCK_MODES128 = BLOCK_MODES +
            "|CFB72|CFB80|CFB88|CFB96|CFB104|CFB112|CFB120|CFB128" +
            "|OFB72|OFB80|OFB88|OFB96|OFB104|OFB112|OFB120|OFB128";
        final String BLOCK_PADS = "NOPADDING|PKCS5PADDING|ISO10126PADDING";
        AccessController.doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                put("Cipher.RSA", "com.sun.crypto.provider.RSACipher");
                put("Cipher.RSA SupportedModes", "ECB");
                put("Cipher.RSA SupportedPaddings",
                        "NOPADDING|PKCS1PADDING|OAEPWITHMD5ANDMGF1PADDING"
                        + "|OAEPWITHSHA1ANDMGF1PADDING"
                        + "|OAEPWITHSHA-1ANDMGF1PADDING"
                        + "|OAEPWITHSHA-256ANDMGF1PADDING"
                        + "|OAEPWITHSHA-384ANDMGF1PADDING"
                        + "|OAEPWITHSHA-512ANDMGF1PADDING");
                put("Cipher.RSA SupportedKeyClasses",
                        "java.security.interfaces.RSAPublicKey" +
                        "|java.security.interfaces.RSAPrivateKey");
                put("Cipher.DES", "com.sun.crypto.provider.DESCipher");
                put("Cipher.DES SupportedModes", BLOCK_MODES);
                put("Cipher.DES SupportedPaddings", BLOCK_PADS);
                put("Cipher.DES SupportedKeyFormats", "RAW");
                put("Cipher.DESede", "com.sun.crypto.provider.DESedeCipher");
                put("Alg.Alias.Cipher.TripleDES", "DESede");
                put("Cipher.DESede SupportedModes", BLOCK_MODES);
                put("Cipher.DESede SupportedPaddings", BLOCK_PADS);
                put("Cipher.DESede SupportedKeyFormats", "RAW");
                put("Cipher.DESedeWrap",
                    "com.sun.crypto.provider.DESedeWrapCipher");
                put("Cipher.DESedeWrap SupportedModes", "CBC");
                put("Cipher.DESedeWrap SupportedPaddings", "NOPADDING");
                put("Cipher.DESedeWrap SupportedKeyFormats", "RAW");
                put("Cipher.PBEWithMD5AndDES",
                    "com.sun.crypto.provider.PBEWithMD5AndDESCipher");
                put("Alg.Alias.Cipher.OID."+OID_PKCS5_MD5_DES,
                    "PBEWithMD5AndDES");
                put("Alg.Alias.Cipher."+OID_PKCS5_MD5_DES,
                    "PBEWithMD5AndDES");
                put("Cipher.PBEWithMD5AndTripleDES",
                    "com.sun.crypto.provider.PBEWithMD5AndTripleDESCipher");
                put("Cipher.PBEWithSHA1AndRC2_40",
                    "com.sun.crypto.provider.PKCS12PBECipherCore$" +
                    "PBEWithSHA1AndRC2_40");
                put("Alg.Alias.Cipher.OID." + OID_PKCS12_RC2_40,
                    "PBEWithSHA1AndRC2_40");
                put("Alg.Alias.Cipher." + OID_PKCS12_RC2_40,
                    "PBEWithSHA1AndRC2_40");
                put("Cipher.PBEWithSHA1AndDESede",
                    "com.sun.crypto.provider.PKCS12PBECipherCore$" +
                    "PBEWithSHA1AndDESede");
                put("Alg.Alias.Cipher.OID." + OID_PKCS12_DESede,
                    "PBEWithSHA1AndDESede");
                put("Alg.Alias.Cipher." + OID_PKCS12_DESede,
                    "PBEWithSHA1AndDESede");
                put("Cipher.Blowfish",
                    "com.sun.crypto.provider.BlowfishCipher");
                put("Cipher.Blowfish SupportedModes", BLOCK_MODES);
                put("Cipher.Blowfish SupportedPaddings", BLOCK_PADS);
                put("Cipher.Blowfish SupportedKeyFormats", "RAW");
                put("Cipher.AES", "com.sun.crypto.provider.AESCipher");
                put("Alg.Alias.Cipher.Rijndael", "AES");
                put("Cipher.AES SupportedModes", BLOCK_MODES128);
                put("Cipher.AES SupportedPaddings", BLOCK_PADS);
                put("Cipher.AES SupportedKeyFormats", "RAW");
                put("Cipher.AESWrap", "com.sun.crypto.provider.AESWrapCipher");
                put("Cipher.AESWrap SupportedModes", "ECB");
                put("Cipher.AESWrap SupportedPaddings", "NOPADDING");
                put("Cipher.AESWrap SupportedKeyFormats", "RAW");
                put("Cipher.RC2",
                    "com.sun.crypto.provider.RC2Cipher");
                put("Cipher.RC2 SupportedModes", BLOCK_MODES);
                put("Cipher.RC2 SupportedPaddings", BLOCK_PADS);
                put("Cipher.RC2 SupportedKeyFormats", "RAW");
                put("Cipher.ARCFOUR",
                    "com.sun.crypto.provider.ARCFOURCipher");
                put("Alg.Alias.Cipher.RC4", "ARCFOUR");
                put("Cipher.ARCFOUR SupportedModes", "ECB");
                put("Cipher.ARCFOUR SupportedPaddings", "NOPADDING");
                put("Cipher.ARCFOUR SupportedKeyFormats", "RAW");
                put("KeyGenerator.DES",
                    "com.sun.crypto.provider.DESKeyGenerator");
                put("KeyGenerator.DESede",
                    "com.sun.crypto.provider.DESedeKeyGenerator");
                put("Alg.Alias.KeyGenerator.TripleDES", "DESede");
                put("KeyGenerator.Blowfish",
                    "com.sun.crypto.provider.BlowfishKeyGenerator");
                put("KeyGenerator.AES",
                    "com.sun.crypto.provider.AESKeyGenerator");
                put("Alg.Alias.KeyGenerator.Rijndael", "AES");
                put("KeyGenerator.RC2",
                    "com.sun.crypto.provider.KeyGeneratorCore$" +
                    "RC2KeyGenerator");
                put("KeyGenerator.ARCFOUR",
                    "com.sun.crypto.provider.KeyGeneratorCore$" +
                    "ARCFOURKeyGenerator");
                put("Alg.Alias.KeyGenerator.RC4", "ARCFOUR");
                put("KeyGenerator.HmacMD5",
                    "com.sun.crypto.provider.HmacMD5KeyGenerator");
                put("KeyGenerator.HmacSHA1",
                    "com.sun.crypto.provider.HmacSHA1KeyGenerator");
                put("KeyGenerator.HmacSHA256",
                    "com.sun.crypto.provider.KeyGeneratorCore$HmacSHA256KG");
                put("KeyGenerator.HmacSHA384",
                    "com.sun.crypto.provider.KeyGeneratorCore$HmacSHA384KG");
                put("KeyGenerator.HmacSHA512",
                    "com.sun.crypto.provider.KeyGeneratorCore$HmacSHA512KG");
                put("KeyPairGenerator.DiffieHellman",
                    "com.sun.crypto.provider.DHKeyPairGenerator");
                put("Alg.Alias.KeyPairGenerator.DH", "DiffieHellman");
                put("Alg.Alias.KeyPairGenerator.OID."+OID_PKCS3,
                    "DiffieHellman");
                put("Alg.Alias.KeyPairGenerator."+OID_PKCS3,
                    "DiffieHellman");
                put("AlgorithmParameterGenerator.DiffieHellman",
                    "com.sun.crypto.provider.DHParameterGenerator");
                put("Alg.Alias.AlgorithmParameterGenerator.DH",
                    "DiffieHellman");
                put("Alg.Alias.AlgorithmParameterGenerator.OID."+OID_PKCS3,
                    "DiffieHellman");
                put("Alg.Alias.AlgorithmParameterGenerator."+OID_PKCS3,
                    "DiffieHellman");
                put("KeyAgreement.DiffieHellman",
                    "com.sun.crypto.provider.DHKeyAgreement");
                put("Alg.Alias.KeyAgreement.DH", "DiffieHellman");
                put("Alg.Alias.KeyAgreement.OID."+OID_PKCS3, "DiffieHellman");
                put("Alg.Alias.KeyAgreement."+OID_PKCS3, "DiffieHellman");
                put("KeyAgreement.DiffieHellman SupportedKeyClasses",
                    "javax.crypto.interfaces.DHPublicKey" +
                    "|javax.crypto.interfaces.DHPrivateKey");
                put("AlgorithmParameters.DiffieHellman",
                    "com.sun.crypto.provider.DHParameters");
                put("Alg.Alias.AlgorithmParameters.DH", "DiffieHellman");
                put("Alg.Alias.AlgorithmParameters.OID."+OID_PKCS3,
                    "DiffieHellman");
                put("Alg.Alias.AlgorithmParameters."+OID_PKCS3,
                    "DiffieHellman");
                put("AlgorithmParameters.DES",
                    "com.sun.crypto.provider.DESParameters");
                put("AlgorithmParameters.DESede",
                    "com.sun.crypto.provider.DESedeParameters");
                put("Alg.Alias.AlgorithmParameters.TripleDES", "DESede");
                put("AlgorithmParameters.PBE",
                    "com.sun.crypto.provider.PBEParameters");
                put("AlgorithmParameters.PBEWithMD5AndDES",
                    "com.sun.crypto.provider.PBEParameters");
                put("Alg.Alias.AlgorithmParameters.OID."+OID_PKCS5_MD5_DES,
                    "PBEWithMD5AndDES");
                put("Alg.Alias.AlgorithmParameters."+OID_PKCS5_MD5_DES,
                    "PBEWithMD5AndDES");
                put("AlgorithmParameters.PBEWithMD5AndTripleDES",
                    "com.sun.crypto.provider.PBEParameters");
                put("AlgorithmParameters.PBEWithSHA1AndDESede",
                    "com.sun.crypto.provider.PBEParameters");
                put("Alg.Alias.AlgorithmParameters.OID."+OID_PKCS12_DESede,
                    "PBEWithSHA1AndDESede");
                put("Alg.Alias.AlgorithmParameters."+OID_PKCS12_DESede,
                    "PBEWithSHA1AndDESede");
                put("AlgorithmParameters.PBEWithSHA1AndRC2_40",
                    "com.sun.crypto.provider.PBEParameters");
                put("Alg.Alias.AlgorithmParameters.OID."+OID_PKCS12_RC2_40,
                    "PBEWithSHA1AndRC2_40");
                put("Alg.Alias.AlgorithmParameters." + OID_PKCS12_RC2_40,
                    "PBEWithSHA1AndRC2_40");
                put("AlgorithmParameters.Blowfish",
                    "com.sun.crypto.provider.BlowfishParameters");
                put("AlgorithmParameters.AES",
                    "com.sun.crypto.provider.AESParameters");
                put("Alg.Alias.AlgorithmParameters.Rijndael", "AES");
                put("AlgorithmParameters.RC2",
                    "com.sun.crypto.provider.RC2Parameters");
                put("AlgorithmParameters.OAEP",
                    "com.sun.crypto.provider.OAEPParameters");
                put("KeyFactory.DiffieHellman",
                    "com.sun.crypto.provider.DHKeyFactory");
                put("Alg.Alias.KeyFactory.DH", "DiffieHellman");
                put("Alg.Alias.KeyFactory.OID."+OID_PKCS3,
                    "DiffieHellman");
                put("Alg.Alias.KeyFactory."+OID_PKCS3, "DiffieHellman");
                put("SecretKeyFactory.DES",
                    "com.sun.crypto.provider.DESKeyFactory");
                put("SecretKeyFactory.DESede",
                    "com.sun.crypto.provider.DESedeKeyFactory");
                put("Alg.Alias.SecretKeyFactory.TripleDES", "DESede");
                put("SecretKeyFactory.PBEWithMD5AndDES",
                    "com.sun.crypto.provider.PBEKeyFactory$PBEWithMD5AndDES"
                    );
                put("Alg.Alias.SecretKeyFactory.OID."+OID_PKCS5_MD5_DES,
                    "PBEWithMD5AndDES");
                put("Alg.Alias.SecretKeyFactory."+OID_PKCS5_MD5_DES,
                    "PBEWithMD5AndDES");
                put("Alg.Alias.SecretKeyFactory.PBE",
                    "PBEWithMD5AndDES");
                put("SecretKeyFactory.PBEWithMD5AndTripleDES",
                    "com.sun.crypto.provider.PBEKeyFactory$" +
                    "PBEWithMD5AndTripleDES"
                    );
                put("SecretKeyFactory.PBEWithSHA1AndDESede",
                    "com.sun.crypto.provider.PBEKeyFactory$PBEWithSHA1AndDESede"
                    );
                put("Alg.Alias.SecretKeyFactory.OID."+OID_PKCS12_DESede,
                    "PBEWithSHA1AndDESede");
                put("Alg.Alias.SecretKeyFactory." + OID_PKCS12_DESede,
                    "PBEWithSHA1AndDESede");
                put("SecretKeyFactory.PBEWithSHA1AndRC2_40",
                    "com.sun.crypto.provider.PBEKeyFactory$PBEWithSHA1AndRC2_40"
                    );
                put("Alg.Alias.SecretKeyFactory.OID." + OID_PKCS12_RC2_40,
                    "PBEWithSHA1AndRC2_40");
                put("Alg.Alias.SecretKeyFactory." + OID_PKCS12_RC2_40,
                    "PBEWithSHA1AndRC2_40");
                put("SecretKeyFactory.PBKDF2WithHmacSHA1",
                    "com.sun.crypto.provider.PBKDF2HmacSHA1Factory");
                put("Alg.Alias.SecretKeyFactory.OID." + OID_PKCS5_PBKDF2,
                    "PBKDF2WithHmacSHA1");
                put("Alg.Alias.SecretKeyFactory." + OID_PKCS5_PBKDF2,
                    "PBKDF2WithHmacSHA1");
                put("Mac.HmacMD5", "com.sun.crypto.provider.HmacMD5");
                put("Mac.HmacSHA1", "com.sun.crypto.provider.HmacSHA1");
                put("Mac.HmacSHA256",
                    "com.sun.crypto.provider.HmacCore$HmacSHA256");
                put("Mac.HmacSHA384",
                    "com.sun.crypto.provider.HmacCore$HmacSHA384");
                put("Mac.HmacSHA512",
                    "com.sun.crypto.provider.HmacCore$HmacSHA512");
                put("Mac.HmacPBESHA1",
                    "com.sun.crypto.provider.HmacPKCS12PBESHA1");
                put("Mac.SslMacMD5",
                    "com.sun.crypto.provider.SslMacCore$SslMacMD5");
                put("Mac.SslMacSHA1",
                    "com.sun.crypto.provider.SslMacCore$SslMacSHA1");
                put("Mac.HmacMD5 SupportedKeyFormats", "RAW");
                put("Mac.HmacSHA1 SupportedKeyFormats", "RAW");
                put("Mac.HmacSHA256 SupportedKeyFormats", "RAW");
                put("Mac.HmacSHA384 SupportedKeyFormats", "RAW");
                put("Mac.HmacSHA512 SupportedKeyFormats", "RAW");
                put("Mac.HmacPBESHA1 SupportedKeyFormats", "RAW");
                put("Mac.SslMacMD5 SupportedKeyFormats", "RAW");
                put("Mac.SslMacSHA1 SupportedKeyFormats", "RAW");
                put("KeyStore.JCEKS", "com.sun.crypto.provider.JceKeyStore");
                put("KeyGenerator.SunTlsPrf",
                        "com.sun.crypto.provider.TlsPrfGenerator$V10");
                put("KeyGenerator.SunTls12Prf",
                        "com.sun.crypto.provider.TlsPrfGenerator$V12");
                put("KeyGenerator.SunTlsMasterSecret",
                    "com.sun.crypto.provider.TlsMasterSecretGenerator");
                put("Alg.Alias.KeyGenerator.SunTls12MasterSecret",
                    "SunTlsMasterSecret");
                put("KeyGenerator.SunTlsKeyMaterial",
                    "com.sun.crypto.provider.TlsKeyMaterialGenerator");
                put("Alg.Alias.KeyGenerator.SunTls12KeyMaterial",
                    "SunTlsKeyMaterial");
                put("KeyGenerator.SunTlsRsaPremasterSecret",
                    "com.sun.crypto.provider.TlsRsaPremasterSecretGenerator");
                put("Alg.Alias.KeyGenerator.SunTls12RsaPremasterSecret",
                    "SunTlsRsaPremasterSecret");
                return null;
            }
        });
    }
}
