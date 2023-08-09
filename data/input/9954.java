public class GenKeyStore {
    static final char[] password = "test12".toCharArray();
    private static X509Certificate getCertificate(String suffix, PublicKey publicKey, PrivateKey privateKey) throws Exception {
        X500Name name = new X500Name("CN=Dummy Certificate " + suffix);
        String algorithm = "SHA1with" + publicKey.getAlgorithm();
        Date date = new Date();
        AlgorithmId algID = AlgorithmId.getAlgorithmId(algorithm);
        X509CertInfo certInfo = new X509CertInfo();
        certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V1));
        certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(1));
        certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algID));
        certInfo.set(X509CertInfo.SUBJECT, new CertificateSubjectName(name));
        certInfo.set(X509CertInfo.ISSUER, new CertificateIssuerName(name));
        certInfo.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
        certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(date, date));
        X509CertImpl cert = new X509CertImpl(certInfo);
        cert.sign(privateKey, algorithm);
        return cert;
    }
    private static void addToKeyStore(KeyStore ks, KeyPair kp, String name) throws Exception {
        PublicKey pubKey = kp.getPublic();
        PrivateKey privKey = kp.getPrivate();
        X509Certificate cert = getCertificate(name, pubKey, privKey);
        ks.setKeyEntry(name, privKey, password, new X509Certificate[] {cert});
    }
    private static void generateKeyPair(KeyStore ks, int keyLength, String alias) throws Exception {
        System.out.println("Generating " + keyLength + " keypair " + alias + "...");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        kpg.initialize(keyLength);
        KeyPair kp = kpg.generateKeyPair();
        addToKeyStore(ks, kp, alias);
    }
    static KeyStore ks;
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        generateKeyPair(ks, 512, "rsa512a");
        generateKeyPair(ks, 512, "rsa512b");
        generateKeyPair(ks, 1024, "rsa1024a");
        generateKeyPair(ks, 1024, "rsa1024b");
        generateKeyPair(ks, 2048, "rsa2048a");
        generateKeyPair(ks, 2048, "rsa2048b");
        generateKeyPair(ks, 4096, "rsa4096a");
        OutputStream out = new FileOutputStream("rsakeys.ks");
        ks.store(out, password);
        out.close();
        long stop = System.currentTimeMillis();
        System.out.println("Done (" + (stop - start) + " ms).");
    }
}
