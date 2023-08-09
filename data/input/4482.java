public class DefaultEntryType {
    private static class PrivKey1 implements PrivateKey {
        public String getAlgorithm() { return ("matching_alg"); }
        public String getFormat() { return "privkey1"; }
        public byte[] getEncoded() { return (byte[])null; }
    }
    private static class PubKey1 implements PublicKey {
        public String getAlgorithm() { return ("non_matching_alg"); }
        public String getFormat() { return "pubkey1"; }
        public byte[] getEncoded() { return (byte[])null; }
    }
    private static class PubKey2 implements PublicKey {
        public String getAlgorithm() { return ("matching_alg"); }
        public String getFormat() { return "pubkey2"; }
        public byte[] getEncoded() { return (byte[])null; }
    }
    private static class Cert extends Certificate {
        public Cert() { super("cert"); }
        public byte[] getEncoded()
                throws CertificateEncodingException { return (byte[])null; }
        public void verify(PublicKey key)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException { }
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException { }
        public String toString() { return "cert"; }
        public PublicKey getPublicKey() { return new PubKey1(); }
    }
    private static class X509Cert extends X509Certificate {
        public byte[] getEncoded()
                throws CertificateEncodingException { return (byte[])null; }
        public void verify(PublicKey key)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException { }
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException { }
        public String toString() { return "x509cert"; }
        public PublicKey getPublicKey() { return new PubKey2(); }
        public void checkValidity()
                throws CertificateExpiredException,
                CertificateNotYetValidException { }
        public void checkValidity(java.util.Date date)
                throws CertificateExpiredException,
                CertificateNotYetValidException { }
        public int getVersion() { return 1; }
        public BigInteger getSerialNumber() { return new BigInteger("5", 10); }
        public Principal getIssuerDN()
                                { return new X500Principal("cn=x509cert"); }
        public X500Principal getIssuerX500Principal()
                                { return new X500Principal("cn=x509cert"); }
        public Principal getSubjectDN()
                                { return new X500Principal("cn=x509cert"); }
        public X500Principal getSubjectX500Principal()
                                { return new X500Principal("cn=x509cert"); }
        public Date getNotBefore() { return new Date(); }
        public Date getNotAfter() { return new Date(); }
        public byte[] getTBSCertificate() throws CertificateEncodingException
                                { return (byte[])null; }
        public byte[] getSignature() { return (byte[])null; }
        public String getSigAlgName() { return "x509cert"; }
        public String getSigAlgOID() { return "x509cert"; }
        public byte[] getSigAlgParams() { return (byte[])null; }
        public boolean[] getIssuerUniqueID() { return (boolean[])null; }
        public boolean[] getSubjectUniqueID() { return (boolean[])null; }
        public boolean[] getKeyUsage() { return (boolean[]) null; }
        public int getBasicConstraints() { return 1; }
        public boolean hasUnsupportedCriticalExtension() { return true; }
        public Set getCriticalExtensionOIDs() { return new HashSet(); }
        public Set getNonCriticalExtensionOIDs() { return new HashSet(); }
        public byte[] getExtensionValue(String oid) { return (byte[])null; }
    }
    public static void main(String[] args) throws Exception {
        testPrivateKeyEntry();
        testSecretKeyEntry();
        testTrustedCertificateEntry();
    }
    private static void testPrivateKeyEntry() throws Exception {
        try {
            Certificate[] chain = new Certificate[0];
            KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry
                                                        (null, chain);
            throw new SecurityException("test 1 failed");
        } catch (NullPointerException npe) {
            System.out.println("test 1 passed");
        }
        try {
            KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry
                                                (new PrivKey1(), null);
            throw new SecurityException("test 2 failed");
        } catch (NullPointerException npe) {
            System.out.println("test 2 passed");
        }
        try {
            Certificate[] chain = new Certificate[0];
            KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry
                                                (new PrivKey1(), chain);
            throw new SecurityException("test 3 failed");
        } catch (IllegalArgumentException npe) {
            System.out.println("test 3 passed");
        }
        try {
            Certificate[] chain = new Certificate[2];
            chain[0] = new Cert();
            chain[1] = new X509Cert();
            KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry
                                        (new PrivKey1(), chain);
            throw new SecurityException("test 4 failed");
        } catch (IllegalArgumentException npe) {
            System.out.println("test 4 passed");
        }
        try {
            Certificate[] chain = new Certificate[1];
            chain[0] = new Cert();
            KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry
                                        (new PrivKey1(), chain);
            throw new SecurityException("test 5 failed");
        } catch (IllegalArgumentException npe) {
            System.out.println("test 5 passed");
        }
        Certificate[] chain = new Certificate[2];
        chain[0] = new X509Cert();
        chain[1] = new X509Cert();
        PrivateKey pkey = new PrivKey1();
        KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry
                                                (pkey, chain);
        Certificate[] gotChain = pke.getCertificateChain();
        if (gotChain instanceof X509Certificate[]) {
            System.out.println("test 6 passed");
        } else {
            throw new SecurityException("test 6 failed");
        }
        if (gotChain.length == 2 &&
            gotChain[0] == chain[0] &&
            gotChain[1] == chain[1]) {
            System.out.println("test 7 passed");
        } else {
            throw new SecurityException("test 7 failed");
        }
        if (pke.getPrivateKey() == pkey) {
            System.out.println("test 8 passed");
        } else {
            throw new SecurityException("test 8 failed");
        }
    }
    private static void testSecretKeyEntry() throws Exception {
    }
    private static void testTrustedCertificateEntry() throws Exception {
    }
}
