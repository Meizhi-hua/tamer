abstract class TestHelper<T> {
    void test(T testObject) {
        Assert.fail("test called unimplemented method");
    }
}
abstract class CipherHelper<T> extends TestHelper<T> {
    private final String algorithmName;
    private final String plainData;
    private final int mode1;
    private final int mode2;
    CipherHelper(String algorithmName, String plainData, int mode1, int mode2) {
        this.algorithmName = algorithmName;
        this.plainData = plainData;
        this.mode1 = mode1;
        this.mode2 = mode2;
    }
    void test(Key encryptKey, Key decryptKey) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        } catch (NoSuchPaddingException e) {
            Assert.fail(e.getMessage());
        }
        try {
            cipher.init(mode1, encryptKey);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        byte[] encrypted = crypt(cipher, plainData.getBytes());
        try {
            cipher.init(mode2, decryptKey);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        byte[] decrypted = crypt(cipher, encrypted);
        String decryptedString = new String(decrypted);
        Assert.assertEquals("transformed data does not match", plainData,
                decryptedString);
    }
    byte[] crypt(Cipher cipher, byte[] input) {
        try {
            return cipher.doFinal(input);
        } catch (IllegalBlockSizeException e) {
            Assert.fail(e.getMessage());
        } catch (BadPaddingException e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }
}
class CipherAsymmetricCryptHelper extends CipherHelper<KeyPair> {
    private static final String plainData = "some data to encrypt and decrypt test";
    CipherAsymmetricCryptHelper(String algorithmName) {
        super(algorithmName, plainData, Cipher.ENCRYPT_MODE,
                Cipher.DECRYPT_MODE);
    }
    @Override
    void test(KeyPair keyPair) {
        test(keyPair.getPrivate(), keyPair.getPublic());
    }
}
class CipherSymmetricCryptHelper extends CipherHelper<SecretKey> {
    private static final String plainData = "some data to encrypt and decrypt test";
    CipherSymmetricCryptHelper(String algorithmName) {
        super(algorithmName, plainData, Cipher.ENCRYPT_MODE,
                Cipher.DECRYPT_MODE);
    }
    @Override
    void test(SecretKey key) {
        test(key, key);
    }
}
class SignatureHelper extends TestHelper<KeyPair> {
    private final String algorithmName;
    private final String plainData = "some data do sign and verify";
    protected SignatureHelper(String algorithmName) {
        this.algorithmName = algorithmName;
    }
    @Override
    void test(KeyPair keyPair) {
        test(keyPair.getPrivate(), keyPair.getPublic());
    }
    void test(PrivateKey encryptKey, PublicKey decryptKey) {
        Signature signature = null;
        try {
            signature = Signature.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        try {
            signature.initSign(encryptKey);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        try {
            signature.update(plainData.getBytes());
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
        byte[] signed = null;
        try {
            signed = signature.sign();
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
        try {
            signature.initVerify(decryptKey);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        try {
            signature.update(plainData.getBytes());
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
        try {
            Assert.assertTrue("signature could not be verified", signature
                    .verify(signed));
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
    }
}
class KeyAgreementHelper extends TestHelper<KeyPair> {
    private final String algorithmName;
    protected KeyAgreementHelper(String algorithmName) {
        this.algorithmName = algorithmName;
    }
    @Override
    void test(KeyPair keyPair) {
        test(keyPair.getPrivate(), keyPair.getPublic());
    }
    void test(PrivateKey encryptKey, PublicKey decryptKey) {
        KeyAgreement keyAgreement = null;
        try {
            keyAgreement = KeyAgreement.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        try {
            keyAgreement.init(encryptKey);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        try {
            keyAgreement.doPhase(decryptKey, true);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        } catch (IllegalStateException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("generated secret is null", keyAgreement
                .generateSecret());
    }
}
class AlgorithmParameterAsymmetricHelper extends TestHelper<AlgorithmParameters> {
    private static final String plainData = "some data to encrypt and decrypt";
    private final String algorithmName;
    protected AlgorithmParameterAsymmetricHelper(String algorithmName) {
        this.algorithmName = algorithmName;
    }
    @Override
    void test(AlgorithmParameters parameters) {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        } catch (NoSuchPaddingException e) {
            Assert.fail(e.getMessage());
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic(), parameters);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Assert.fail(e.getMessage());
        }
        byte[] bs = null;
        try {
            bs = cipher.doFinal(plainData.getBytes());
        } catch (IllegalBlockSizeException e) {
            Assert.fail(e.getMessage());
        } catch (BadPaddingException e) {
            Assert.fail(e.getMessage());
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), parameters);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Assert.fail(e.getMessage());
        }
        byte[] decrypted = null;
        try {
            decrypted = cipher.doFinal(bs);
        } catch (IllegalBlockSizeException e) {
            Assert.fail(e.getMessage());
        } catch (BadPaddingException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(Arrays.equals(plainData.getBytes(), decrypted));
    }
}
class AlgorithmParameterSymmetricHelper extends TestHelper<AlgorithmParameters> {
    private static final String plainData = "some data to encrypt and decrypt";
    private final String algorithmName;
    private final int keySize;
    private String blockmode;
    protected AlgorithmParameterSymmetricHelper(String algorithmName, int keySize) {
        this.algorithmName = algorithmName;
        this.keySize = keySize;
    }
    protected AlgorithmParameterSymmetricHelper(String algorithmName, String blockmode, int keySize) {
        this(algorithmName, keySize);
        this.blockmode = blockmode;
    }
    @Override
    void test(AlgorithmParameters parameters) {
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        generator.init(keySize);
        Key key = generator.generateKey();
        Cipher cipher = null;
        try {
            String transformation = algorithmName;
            if (blockmode != null)
            {
                transformation += "/" + blockmode;
            }
            cipher = Cipher.getInstance(transformation);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        } catch (NoSuchPaddingException e) {
            Assert.fail(e.getMessage());
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, parameters);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Assert.fail(e.getMessage());
        }
        byte[] bs = null;
        try {
            bs = cipher.doFinal(plainData.getBytes());
        } catch (IllegalBlockSizeException e) {
            Assert.fail(e.getMessage());
        } catch (BadPaddingException e) {
            Assert.fail(e.getMessage());
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, parameters);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Assert.fail(e.getMessage());
        }
        byte[] decrypted = null;
        try {
            decrypted = cipher.doFinal(bs);
        } catch (IllegalBlockSizeException e) {
            Assert.fail(e.getMessage());
        } catch (BadPaddingException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(Arrays.equals(plainData.getBytes(), decrypted));
    }
}
class AlgorithmParameterSignatureHelper<T extends AlgorithmParameterSpec> extends TestHelper<AlgorithmParameters> {
    private final String algorithmName;
    private final String plainData = "some data do sign and verify";
    private final Class<T> parameterSpecClass;
    protected AlgorithmParameterSignatureHelper(String algorithmName, Class<T> parameterSpecCla1ss) {
        this.algorithmName = algorithmName;
        this.parameterSpecClass = parameterSpecCla1ss;
    }
    @Override
    void test(AlgorithmParameters parameters) {
        Signature signature = null;
        try {
            signature = Signature.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        T parameterSpec = null;
        try {
            parameterSpec = parameters.getParameterSpec(parameterSpecClass);
        } catch (InvalidParameterSpecException e) {
            Assert.fail(e.getMessage());
        }
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        try {
            generator.initialize(parameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            Assert.fail(e.getMessage());
        }
        KeyPair keyPair = generator.genKeyPair();
        try {
            signature.initSign(keyPair.getPrivate());
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        try {
            signature.update(plainData.getBytes());
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
        byte[] signed = null;
        try {
            signed = signature.sign();
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
        try {
            signature.initVerify(keyPair.getPublic());
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        try {
            signature.update(plainData.getBytes());
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
        try {
            Assert.assertTrue("signature could not be verified", signature
                    .verify(signed));
        } catch (SignatureException e) {
            Assert.fail(e.getMessage());
        }
    }
}
class AlgorithmParameterKeyAgreementHelper extends TestHelper<AlgorithmParameters> {
    private final String algorithmName;
    protected AlgorithmParameterKeyAgreementHelper(String algorithmName) {
        this.algorithmName = algorithmName;
    }
    @Override
    void test(AlgorithmParameters parameters) {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        KeyAgreement keyAgreement = null;
        try {
            keyAgreement = KeyAgreement.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        }
        try {
            keyAgreement.init(keyPair.getPrivate());
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        }
        try {
            keyAgreement.doPhase(keyPair.getPublic(), true);
        } catch (InvalidKeyException e) {
            Assert.fail(e.getMessage());
        } catch (IllegalStateException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("generated secret is null", keyAgreement
                .generateSecret());
    }
}