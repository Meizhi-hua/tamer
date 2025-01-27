final class P11ECKeyFactory extends P11KeyFactory {
    P11ECKeyFactory(Token token, String algorithm) {
        super(token, algorithm);
    }
    static ECParameterSpec getECParameterSpec(String name) {
        return NamedCurve.getECParameterSpec(name);
    }
    static ECParameterSpec getECParameterSpec(int keySize) {
        return NamedCurve.getECParameterSpec(keySize);
    }
    static ECParameterSpec getECParameterSpec(ECParameterSpec spec) {
        return ECParameters.getNamedCurve(spec);
    }
    static ECParameterSpec decodeParameters(byte[] params) throws IOException {
        return ECParameters.decodeParameters(params);
    }
    static byte[] encodeParameters(ECParameterSpec params) {
        return ECParameters.encodeParameters(params);
    }
    static ECPoint decodePoint(byte[] encoded, EllipticCurve curve) throws IOException {
        return ECParameters.decodePoint(encoded, curve);
    }
    static byte[] getEncodedPublicValue(PublicKey key) throws InvalidKeyException {
        if (key instanceof ECPublicKeyImpl) {
            return ((ECPublicKeyImpl)key).getEncodedPublicValue();
        } else if (key instanceof ECPublicKey) {
            ECPublicKey ecKey = (ECPublicKey)key;
            ECPoint w = ecKey.getW();
            ECParameterSpec params = ecKey.getParams();
            return ECParameters.encodePoint(w, params.getCurve());
        } else {
            throw new InvalidKeyException
                ("Key class not yet supported: " + key.getClass().getName());
        }
    }
    PublicKey implTranslatePublicKey(PublicKey key) throws InvalidKeyException {
        try {
            if (key instanceof ECPublicKey) {
                ECPublicKey ecKey = (ECPublicKey)key;
                return generatePublic(
                    ecKey.getW(),
                    ecKey.getParams()
                );
            } else if ("X.509".equals(key.getFormat())) {
                byte[] encoded = key.getEncoded();
                key = new sun.security.ec.ECPublicKeyImpl(encoded);
                return implTranslatePublicKey(key);
            } else {
                throw new InvalidKeyException("PublicKey must be instance "
                        + "of ECPublicKey or have X.509 encoding");
            }
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("Could not create EC public key", e);
        }
    }
    PrivateKey implTranslatePrivateKey(PrivateKey key)
            throws InvalidKeyException {
        try {
            if (key instanceof ECPrivateKey) {
                ECPrivateKey ecKey = (ECPrivateKey)key;
                return generatePrivate(
                    ecKey.getS(),
                    ecKey.getParams()
                );
            } else if ("PKCS#8".equals(key.getFormat())) {
                byte[] encoded = key.getEncoded();
                key = new sun.security.ec.ECPrivateKeyImpl(encoded);
                return implTranslatePrivateKey(key);
            } else {
                throw new InvalidKeyException("PrivateKey must be instance "
                        + "of ECPrivateKey or have PKCS#8 encoding");
            }
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("Could not create EC private key", e);
        }
    }
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
            throws InvalidKeySpecException {
        token.ensureValid();
        if (keySpec instanceof X509EncodedKeySpec) {
            try {
                byte[] encoded = ((X509EncodedKeySpec)keySpec).getEncoded();
                PublicKey key = new sun.security.ec.ECPublicKeyImpl(encoded);
                return implTranslatePublicKey(key);
            } catch (InvalidKeyException e) {
                throw new InvalidKeySpecException
                        ("Could not create EC public key", e);
            }
        }
        if (keySpec instanceof ECPublicKeySpec == false) {
            throw new InvalidKeySpecException("Only ECPublicKeySpec and "
                + "X509EncodedKeySpec supported for EC public keys");
        }
        try {
            ECPublicKeySpec ec = (ECPublicKeySpec)keySpec;
            return generatePublic(
                ec.getW(),
                ec.getParams()
            );
        } catch (PKCS11Exception e) {
            throw new InvalidKeySpecException
                ("Could not create EC public key", e);
        }
    }
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        token.ensureValid();
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            try {
                byte[] encoded = ((PKCS8EncodedKeySpec)keySpec).getEncoded();
                PrivateKey key = new sun.security.ec.ECPrivateKeyImpl(encoded);
                return implTranslatePrivateKey(key);
            } catch (GeneralSecurityException e) {
                throw new InvalidKeySpecException
                        ("Could not create EC private key", e);
            }
        }
        if (keySpec instanceof ECPrivateKeySpec == false) {
            throw new InvalidKeySpecException("Only ECPrivateKeySpec and "
                + "PKCS8EncodedKeySpec supported for EC private keys");
        }
        try {
            ECPrivateKeySpec ec = (ECPrivateKeySpec)keySpec;
            return generatePrivate(
                ec.getS(),
                ec.getParams()
            );
        } catch (PKCS11Exception e) {
            throw new InvalidKeySpecException
                ("Could not create EC private key", e);
        }
    }
    private PublicKey generatePublic(ECPoint point, ECParameterSpec params) throws PKCS11Exception {
        byte[] encodedParams = ECParameters.encodeParameters(params);
        byte[] encodedPoint = null;
        DerValue pkECPoint = new DerValue(DerValue.tag_OctetString,
            ECParameters.encodePoint(point, params.getCurve()));
        try {
            encodedPoint = pkECPoint.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not DER encode point", e);
        }
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
            new CK_ATTRIBUTE(CKA_CLASS, CKO_PUBLIC_KEY),
            new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_EC),
            new CK_ATTRIBUTE(CKA_EC_POINT, encodedPoint),
            new CK_ATTRIBUTE(CKA_EC_PARAMS, encodedParams),
        };
        attributes = token.getAttributes
                (O_IMPORT, CKO_PUBLIC_KEY, CKK_EC, attributes);
        Session session = null;
        try {
            session = token.getObjSession();
            long keyID = token.p11.C_CreateObject(session.id(), attributes);
            return P11Key.publicKey
                (session, keyID, "EC", params.getCurve().getField().getFieldSize(), attributes);
        } finally {
            token.releaseSession(session);
        }
    }
    private PrivateKey generatePrivate(BigInteger s, ECParameterSpec params) throws PKCS11Exception {
        byte[] encodedParams = ECParameters.encodeParameters(params);
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
            new CK_ATTRIBUTE(CKA_CLASS, CKO_PRIVATE_KEY),
            new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_EC),
            new CK_ATTRIBUTE(CKA_VALUE, s),
            new CK_ATTRIBUTE(CKA_EC_PARAMS, encodedParams),
        };
        attributes = token.getAttributes
                (O_IMPORT, CKO_PRIVATE_KEY, CKK_EC, attributes);
        Session session = null;
        try {
            session = token.getObjSession();
            long keyID = token.p11.C_CreateObject(session.id(), attributes);
            return P11Key.privateKey
                (session, keyID, "EC", params.getCurve().getField().getFieldSize(), attributes);
        } finally {
            token.releaseSession(session);
        }
    }
    KeySpec implGetPublicKeySpec(P11Key key, Class keySpec, Session[] session)
            throws PKCS11Exception, InvalidKeySpecException {
        if (ECPublicKeySpec.class.isAssignableFrom(keySpec)) {
            session[0] = token.getObjSession();
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_EC_POINT),
                new CK_ATTRIBUTE(CKA_EC_PARAMS),
            };
            token.p11.C_GetAttributeValue(session[0].id(), key.keyID, attributes);
            try {
                ECParameterSpec params = decodeParameters(attributes[1].getByteArray());
                ECPoint point = decodePoint(attributes[0].getByteArray(), params.getCurve());
                return new ECPublicKeySpec(point, params);
            } catch (IOException e) {
                throw new InvalidKeySpecException("Could not parse key", e);
            }
        } else { 
            throw new InvalidKeySpecException("Only ECPublicKeySpec and "
                + "X509EncodedKeySpec supported for EC public keys");
        }
    }
    KeySpec implGetPrivateKeySpec(P11Key key, Class keySpec, Session[] session)
            throws PKCS11Exception, InvalidKeySpecException {
        if (ECPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            session[0] = token.getObjSession();
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_VALUE),
                new CK_ATTRIBUTE(CKA_EC_PARAMS),
            };
            token.p11.C_GetAttributeValue(session[0].id(), key.keyID, attributes);
            try {
                ECParameterSpec params = decodeParameters(attributes[1].getByteArray());
                return new ECPrivateKeySpec(attributes[0].getBigInteger(), params);
            } catch (IOException e) {
                throw new InvalidKeySpecException("Could not parse key", e);
            }
        } else { 
            throw new InvalidKeySpecException("Only ECPrivateKeySpec "
                + "and PKCS8EncodedKeySpec supported for EC private keys");
        }
    }
    KeyFactory implGetSoftwareFactory() throws GeneralSecurityException {
        return sun.security.ec.ECKeyFactory.INSTANCE;
    }
}
