public class TestCACerts extends PKCS11Test {
    private final static char SEP = File.separatorChar;
    public static void main(String[] args) throws Exception {
        main(new TestCACerts());
    }
    public void main(Provider p) throws Exception {
        long start = System.currentTimeMillis();
        Security.addProvider(p);
        String PROVIDER = p.getName();
        String javaHome = System.getProperty("java.home");
        String caCerts = javaHome + SEP + "lib" + SEP + "security" + SEP + "cacerts";
        InputStream in = new FileInputStream(caCerts);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, null);
        in.close();
        for (Enumeration e = ks.aliases(); e.hasMoreElements(); ) {
            String alias = (String)e.nextElement();
            if (ks.isCertificateEntry(alias)) {
                System.out.println("* Testing " + alias + "...");
                X509Certificate cert = (X509Certificate)ks.getCertificate(alias);
                PublicKey key = cert.getPublicKey();
                String alg = key.getAlgorithm();
                if (alg.equals("RSA")) {
                    System.out.println("Signature algorithm: " + cert.getSigAlgName());
                    cert.verify(key, PROVIDER);
                } else {
                    System.out.println("Skipping cert with key: " + alg);
                }
            } else {
                System.out.println("Skipping alias " + alias);
            }
        }
        long stop = System.currentTimeMillis();
        System.out.println("All tests passed (" + (stop - start) + " ms).");
    }
}