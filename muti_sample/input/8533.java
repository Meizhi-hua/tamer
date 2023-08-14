final class P11ECDHKeyAgreement extends KeyAgreementSpi {
    private final Token token;
    private final String algorithm;
    private final long mechanism;
    private P11Key privateKey;
    private byte[] publicValue;
    private int secretLen;
    P11ECDHKeyAgreement(Token token, String algorithm, long mechanism) {
        super();
        this.token = token;
        this.algorithm = algorithm;
        this.mechanism = mechanism;
    }
    protected void engineInit(Key key, SecureRandom random)
            throws InvalidKeyException {
        if (key instanceof PrivateKey == false) {
            throw new InvalidKeyException
                        ("Key must be instance of PrivateKey");
        }
        privateKey = P11KeyFactory.convertKey(token, key, "EC");
        publicValue = null;
    }
    protected void engineInit(Key key, AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                        ("Parameters not supported");
        }
        engineInit(key, random);
    }
    protected Key engineDoPhase(Key key, boolean lastPhase)
            throws InvalidKeyException, IllegalStateException {
        if (privateKey == null) {
            throw new IllegalStateException("Not initialized");
        }
        if (publicValue != null) {
            throw new IllegalStateException("Phase already executed");
        }
        if (lastPhase == false) {
            throw new IllegalStateException
                ("Only two party agreement supported, lastPhase must be true");
        }
        if (key instanceof ECPublicKey == false) {
            throw new InvalidKeyException
                ("Key must be a PublicKey with algorithm EC");
        }
        ECPublicKey ecKey = (ECPublicKey)key;
        int keyLenBits = ecKey.getParams().getCurve().getField().getFieldSize();
        secretLen = (keyLenBits + 7) >> 3;
        publicValue = P11ECKeyFactory.getEncodedPublicValue(ecKey);
        return null;
    }
    protected byte[] engineGenerateSecret() throws IllegalStateException {
        if ((privateKey == null) || (publicValue == null)) {
            throw new IllegalStateException("Not initialized correctly");
        }
        Session session = null;
        try {
            session = token.getOpSession();
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY),
                new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_GENERIC_SECRET),
            };
            CK_ECDH1_DERIVE_PARAMS ckParams =
                    new CK_ECDH1_DERIVE_PARAMS(CKD_NULL, null, publicValue);
            attributes = token.getAttributes
                (O_GENERATE, CKO_SECRET_KEY, CKK_GENERIC_SECRET, attributes);
            long keyID = token.p11.C_DeriveKey(session.id(),
                new CK_MECHANISM(mechanism, ckParams), privateKey.keyID,
                attributes);
            attributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_VALUE)
            };
            token.p11.C_GetAttributeValue(session.id(), keyID, attributes);
            byte[] secret = attributes[0].getByteArray();
            token.p11.C_DestroyObject(session.id(), keyID);
            return secret;
        } catch (PKCS11Exception e) {
            throw new ProviderException("Could not derive key", e);
        } finally {
            publicValue = null;
            token.releaseSession(session);
        }
    }
    protected int engineGenerateSecret(byte[] sharedSecret, int
            offset) throws IllegalStateException, ShortBufferException {
        if (offset + secretLen > sharedSecret.length) {
            throw new ShortBufferException("Need " + secretLen
                + " bytes, only " + (sharedSecret.length - offset) + " available");
        }
        byte[] secret = engineGenerateSecret();
        System.arraycopy(secret, 0, sharedSecret, offset, secret.length);
        return secret.length;
    }
    protected SecretKey engineGenerateSecret(String algorithm)
            throws IllegalStateException, NoSuchAlgorithmException,
            InvalidKeyException {
        if (algorithm == null) {
            throw new NoSuchAlgorithmException("Algorithm must not be null");
        }
        if (algorithm.equals("TlsPremasterSecret") == false) {
            throw new NoSuchAlgorithmException
                ("Only supported for algorithm TlsPremasterSecret");
        }
        return nativeGenerateSecret(algorithm);
    }
    private SecretKey nativeGenerateSecret(String algorithm)
            throws IllegalStateException, NoSuchAlgorithmException,
            InvalidKeyException {
        if ((privateKey == null) || (publicValue == null)) {
            throw new IllegalStateException("Not initialized correctly");
        }
        long keyType = CKK_GENERIC_SECRET;
        Session session = null;
        try {
            session = token.getObjSession();
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY),
                new CK_ATTRIBUTE(CKA_KEY_TYPE, keyType),
            };
            CK_ECDH1_DERIVE_PARAMS ckParams =
                    new CK_ECDH1_DERIVE_PARAMS(CKD_NULL, null, publicValue);
            attributes = token.getAttributes
                (O_GENERATE, CKO_SECRET_KEY, keyType, attributes);
            long keyID = token.p11.C_DeriveKey(session.id(),
                new CK_MECHANISM(mechanism, ckParams), privateKey.keyID,
                attributes);
            CK_ATTRIBUTE[] lenAttributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_VALUE_LEN),
            };
            token.p11.C_GetAttributeValue(session.id(), keyID, lenAttributes);
            int keyLen = (int)lenAttributes[0].getLong();
            SecretKey key = P11Key.secretKey
                        (session, keyID, algorithm, keyLen << 3, attributes);
            return key;
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("Could not derive key", e);
        } finally {
            publicValue = null;
            token.releaseSession(session);
        }
    }
}