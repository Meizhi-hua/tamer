public class SignatureDSA extends SignatureAlgorithmSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(SignatureDSA.class.getName());
    public static final String _URI = Constants.SignatureSpecNS + "dsa-sha1";
    private java.security.Signature _signatureAlgorithm = null;
    protected String engineGetURI() {
        return SignatureDSA._URI;
    }
    public SignatureDSA() throws XMLSignatureException {
        String algorithmID = JCEMapper.translateURItoJCEID(SignatureDSA._URI);
        if (log.isLoggable(java.util.logging.Level.FINE))
            log.log(java.util.logging.Level.FINE, "Created SignatureDSA using " + algorithmID);
        String provider = JCEMapper.getProviderId();
        try {
            if (provider == null) {
                this._signatureAlgorithm = Signature.getInstance(algorithmID);
            } else {
                this._signatureAlgorithm =
                    Signature.getInstance(algorithmID, provider);
            }
        } catch (java.security.NoSuchAlgorithmException ex) {
            Object[] exArgs = { algorithmID, ex.getLocalizedMessage() };
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        } catch (java.security.NoSuchProviderException ex) {
            Object[] exArgs = { algorithmID, ex.getLocalizedMessage() };
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        }
    }
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws XMLSignatureException {
        try {
            this._signatureAlgorithm.setParameter(params);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected boolean engineVerify(byte[] signature)
           throws XMLSignatureException {
        try {
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Called DSA.verify() on " + Base64.encode(signature));
            byte[] jcebytes = SignatureDSA.convertXMLDSIGtoASN1(signature);
            return this._signatureAlgorithm.verify(jcebytes);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (IOException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected void engineInitVerify(Key publicKey) throws XMLSignatureException {
        if (!(publicKey instanceof PublicKey)) {
            String supplied = publicKey.getClass().getName();
            String needed = PublicKey.class.getName();
            Object exArgs[] = { supplied, needed };
            throw new XMLSignatureException
                ("algorithms.WrongKeyForThisOperation", exArgs);
        }
        try {
            this._signatureAlgorithm.initVerify((PublicKey) publicKey);
        } catch (InvalidKeyException ex) {
            Signature sig = this._signatureAlgorithm;
            try {
                this._signatureAlgorithm = Signature.getInstance
                    (_signatureAlgorithm.getAlgorithm());
            } catch (Exception e) {
                if (log.isLoggable(java.util.logging.Level.FINE)) {
                    log.log(java.util.logging.Level.FINE, "Exception when reinstantiating Signature:" + e);
                }
                this._signatureAlgorithm = sig;
            }
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected byte[] engineSign() throws XMLSignatureException {
        try {
            byte jcebytes[] = this._signatureAlgorithm.sign();
            return SignatureDSA.convertASN1toXMLDSIG(jcebytes);
        } catch (IOException ex) {
            throw new XMLSignatureException("empty", ex);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected void engineInitSign(Key privateKey, SecureRandom secureRandom)
           throws XMLSignatureException {
        if (!(privateKey instanceof PrivateKey)) {
            String supplied = privateKey.getClass().getName();
            String needed = PrivateKey.class.getName();
            Object exArgs[] = { supplied, needed };
            throw new XMLSignatureException
                ("algorithms.WrongKeyForThisOperation", exArgs);
        }
        try {
            this._signatureAlgorithm.initSign((PrivateKey) privateKey,
                                           secureRandom);
        } catch (InvalidKeyException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected void engineInitSign(Key privateKey) throws XMLSignatureException {
        if (!(privateKey instanceof PrivateKey)) {
            String supplied = privateKey.getClass().getName();
            String needed = PrivateKey.class.getName();
            Object exArgs[] = { supplied, needed };
            throw new XMLSignatureException
                ("algorithms.WrongKeyForThisOperation", exArgs);
        }
        try {
            this._signatureAlgorithm.initSign((PrivateKey) privateKey);
        } catch (InvalidKeyException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected void engineUpdate(byte[] input) throws XMLSignatureException {
        try {
            this._signatureAlgorithm.update(input);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected void engineUpdate(byte input) throws XMLSignatureException {
        try {
            this._signatureAlgorithm.update(input);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected void engineUpdate(byte buf[], int offset, int len)
        throws XMLSignatureException {
        try {
            this._signatureAlgorithm.update(buf, offset, len);
        } catch (SignatureException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    protected String engineGetJCEAlgorithmString() {
        return this._signatureAlgorithm.getAlgorithm();
    }
    protected String engineGetJCEProviderName() {
        return this._signatureAlgorithm.getProvider().getName();
    }
    private static byte[] convertASN1toXMLDSIG(byte asn1Bytes[])
           throws IOException {
        byte rLength = asn1Bytes[3];
        int i;
        for (i = rLength; (i > 0) && (asn1Bytes[(4 + rLength) - i] == 0); i--);
        byte sLength = asn1Bytes[5 + rLength];
        int j;
        for (j = sLength;
              (j > 0) && (asn1Bytes[(6 + rLength + sLength) - j] == 0); j--);
        if ((asn1Bytes[0] != 48) || (asn1Bytes[1] != asn1Bytes.length - 2)
              || (asn1Bytes[2] != 2) || (i > 20)
              || (asn1Bytes[4 + rLength] != 2) || (j > 20)) {
            throw new IOException("Invalid ASN.1 format of DSA signature");
        }
        byte xmldsigBytes[] = new byte[40];
        System.arraycopy(asn1Bytes, (4 + rLength) - i, xmldsigBytes, 20 - i,
                          i);
        System.arraycopy(asn1Bytes, (6 + rLength + sLength) - j, xmldsigBytes,
                          40 - j, j);
        return xmldsigBytes;
    }
    private static byte[] convertXMLDSIGtoASN1(byte xmldsigBytes[])
           throws IOException {
        if (xmldsigBytes.length != 40) {
            throw new IOException("Invalid XMLDSIG format of DSA signature");
        }
        int i;
        for (i = 20; (i > 0) && (xmldsigBytes[20 - i] == 0); i--);
        int j = i;
        if (xmldsigBytes[20 - i] < 0) {
         j += 1;
        }
        int k;
        for (k = 20; (k > 0) && (xmldsigBytes[40 - k] == 0); k--);
        int l = k;
        if (xmldsigBytes[40 - k] < 0) {
            l += 1;
        }
        byte asn1Bytes[] = new byte[6 + j + l];
        asn1Bytes[0] = 48;
        asn1Bytes[1] = (byte) (4 + j + l);
        asn1Bytes[2] = 2;
        asn1Bytes[3] = (byte) j;
        System.arraycopy(xmldsigBytes, 20 - i, asn1Bytes, (4 + j) - i, i);
        asn1Bytes[4 + j] = 2;
        asn1Bytes[5 + j] = (byte) l;
        System.arraycopy(xmldsigBytes, 40 - k, asn1Bytes, (6 + j + l) - k, k);
        return asn1Bytes;
    }
    protected void engineSetHMACOutputLength(int HMACOutputLength)
            throws XMLSignatureException {
        throw new XMLSignatureException(
            "algorithms.HMACOutputLengthOnlyForHMAC");
    }
    protected void engineInitSign(
        Key signingKey, AlgorithmParameterSpec algorithmParameterSpec)
            throws XMLSignatureException {
        throw new XMLSignatureException(
            "algorithms.CannotUseAlgorithmParameterSpecOnDSA");
    }
}
