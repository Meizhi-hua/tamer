final class P11DSAKeyFactory extends P11KeyFactory {
    P11DSAKeyFactory(Token token, String algorithm) {
        super(token, algorithm);
    }
    PublicKey implTranslatePublicKey(PublicKey key) throws InvalidKeyException {
        try {
            if (key instanceof DSAPublicKey) {
                DSAPublicKey dsaKey = (DSAPublicKey)key;
                DSAParams params = dsaKey.getParams();
                return generatePublic(
                    dsaKey.getY(),
                    params.getP(),
                    params.getQ(),
                    params.getG()
                );
            } else if ("X.509".equals(key.getFormat())) {
                byte[] encoded = key.getEncoded();
                key = new sun.security.provider.DSAPublicKey(encoded);
                return implTranslatePublicKey(key);
            } else {
                throw new InvalidKeyException("PublicKey must be instance "
                        + "of DSAPublicKey or have X.509 encoding");
            }
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("Could not create DSA public key", e);
        }
    }
    PrivateKey implTranslatePrivateKey(PrivateKey key)
            throws InvalidKeyException {
        try {
            if (key instanceof DSAPrivateKey) {
                DSAPrivateKey dsaKey = (DSAPrivateKey)key;
                DSAParams params = dsaKey.getParams();
                return generatePrivate(
                    dsaKey.getX(),
                    params.getP(),
                    params.getQ(),
                    params.getG()
                );
            } else if ("PKCS#8".equals(key.getFormat())) {
                byte[] encoded = key.getEncoded();
                key = new sun.security.provider.DSAPrivateKey(encoded);
                return implTranslatePrivateKey(key);
            } else {
                throw new InvalidKeyException("PrivateKey must be instance "
                        + "of DSAPrivateKey or have PKCS#8 encoding");
            }
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("Could not create DSA private key", e);
        }
    }
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
            throws InvalidKeySpecException {
        token.ensureValid();
        if (keySpec instanceof X509EncodedKeySpec) {
            try {
                byte[] encoded = ((X509EncodedKeySpec)keySpec).getEncoded();
                PublicKey key = new sun.security.provider.DSAPublicKey(encoded);
                return implTranslatePublicKey(key);
            } catch (InvalidKeyException e) {
                throw new InvalidKeySpecException
                        ("Could not create DSA public key", e);
            }
        }
        if (keySpec instanceof DSAPublicKeySpec == false) {
            throw new InvalidKeySpecException("Only DSAPublicKeySpec and "
                + "X509EncodedKeySpec supported for DSA public keys");
        }
        try {
            DSAPublicKeySpec ds = (DSAPublicKeySpec)keySpec;
            return generatePublic(
                ds.getY(),
                ds.getP(),
                ds.getQ(),
                ds.getG()
            );
        } catch (PKCS11Exception e) {
            throw new InvalidKeySpecException
                ("Could not create DSA public key", e);
        }
    }
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        token.ensureValid();
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            try {
                byte[] encoded = ((PKCS8EncodedKeySpec)keySpec).getEncoded();
                PrivateKey key = new sun.security.provider.DSAPrivateKey(encoded);
                return implTranslatePrivateKey(key);
            } catch (GeneralSecurityException e) {
                throw new InvalidKeySpecException
                        ("Could not create DSA private key", e);
            }
        }
        if (keySpec instanceof DSAPrivateKeySpec == false) {
            throw new InvalidKeySpecException("Only DSAPrivateKeySpec and "
                + "PKCS8EncodedKeySpec supported for DSA private keys");
        }
        try {
            DSAPrivateKeySpec ds = (DSAPrivateKeySpec)keySpec;
            return generatePrivate(
                ds.getX(),
                ds.getP(),
                ds.getQ(),
                ds.getG()
            );
        } catch (PKCS11Exception e) {
            throw new InvalidKeySpecException
                ("Could not create DSA private key", e);
        }
    }
    private PublicKey generatePublic(BigInteger y, BigInteger p, BigInteger q,
            BigInteger g) throws PKCS11Exception {
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
            new CK_ATTRIBUTE(CKA_CLASS, CKO_PUBLIC_KEY),
            new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_DSA),
            new CK_ATTRIBUTE(CKA_VALUE, y),
            new CK_ATTRIBUTE(CKA_PRIME, p),
            new CK_ATTRIBUTE(CKA_SUBPRIME, q),
            new CK_ATTRIBUTE(CKA_BASE, g),
        };
        attributes = token.getAttributes
                (O_IMPORT, CKO_PUBLIC_KEY, CKK_DSA, attributes);
        Session session = null;
        try {
            session = token.getObjSession();
            long keyID = token.p11.C_CreateObject(session.id(), attributes);
            return P11Key.publicKey
                (session, keyID, "DSA", p.bitLength(), attributes);
        } finally {
            token.releaseSession(session);
        }
    }
    private PrivateKey generatePrivate(BigInteger x, BigInteger p,
            BigInteger q, BigInteger g) throws PKCS11Exception {
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
            new CK_ATTRIBUTE(CKA_CLASS, CKO_PRIVATE_KEY),
            new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_DSA),
            new CK_ATTRIBUTE(CKA_VALUE, x),
            new CK_ATTRIBUTE(CKA_PRIME, p),
            new CK_ATTRIBUTE(CKA_SUBPRIME, q),
            new CK_ATTRIBUTE(CKA_BASE, g),
        };
        attributes = token.getAttributes
                (O_IMPORT, CKO_PRIVATE_KEY, CKK_DSA, attributes);
        Session session = null;
        try {
            session = token.getObjSession();
            long keyID = token.p11.C_CreateObject(session.id(), attributes);
            return P11Key.privateKey
                (session, keyID, "DSA", p.bitLength(), attributes);
        } finally {
            token.releaseSession(session);
        }
    }
    KeySpec implGetPublicKeySpec(P11Key key, Class keySpec, Session[] session)
            throws PKCS11Exception, InvalidKeySpecException {
        if (DSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
            session[0] = token.getObjSession();
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_VALUE),
                new CK_ATTRIBUTE(CKA_PRIME),
                new CK_ATTRIBUTE(CKA_SUBPRIME),
                new CK_ATTRIBUTE(CKA_BASE),
            };
            token.p11.C_GetAttributeValue(session[0].id(), key.keyID, attributes);
            KeySpec spec = new DSAPublicKeySpec(
                attributes[0].getBigInteger(),
                attributes[1].getBigInteger(),
                attributes[2].getBigInteger(),
                attributes[3].getBigInteger()
            );
            return spec;
        } else { 
            throw new InvalidKeySpecException("Only DSAPublicKeySpec and "
                + "X509EncodedKeySpec supported for DSA public keys");
        }
    }
    KeySpec implGetPrivateKeySpec(P11Key key, Class keySpec, Session[] session)
            throws PKCS11Exception, InvalidKeySpecException {
        if (DSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            session[0] = token.getObjSession();
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(CKA_VALUE),
                new CK_ATTRIBUTE(CKA_PRIME),
                new CK_ATTRIBUTE(CKA_SUBPRIME),
                new CK_ATTRIBUTE(CKA_BASE),
            };
            token.p11.C_GetAttributeValue(session[0].id(), key.keyID, attributes);
            KeySpec spec = new DSAPrivateKeySpec(
                attributes[0].getBigInteger(),
                attributes[1].getBigInteger(),
                attributes[2].getBigInteger(),
                attributes[3].getBigInteger()
            );
            return spec;
        } else { 
            throw new InvalidKeySpecException("Only DSAPrivateKeySpec "
                + "and PKCS8EncodedKeySpec supported for DSA private keys");
        }
    }
    KeyFactory implGetSoftwareFactory() throws GeneralSecurityException {
        return KeyFactory.getInstance("DSA", P11Util.getSunProvider());
    }
}
