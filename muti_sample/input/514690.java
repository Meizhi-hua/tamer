@TestTargetClass(KeyStore.class)
public class KeyStore3Test extends TestCase {
    private KeyStore mockKeyStore;
    private KeyPair keyPair;
    private Certificate certificate;
    public KeyStore3Test() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPair = keyPairGenerator.generateKeyPair();
        String certificateData = "-----BEGIN CERTIFICATE-----\n"
                + "MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw\n"
                + "HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl\n"
                + "IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa\n"
                + "Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n"
                + "cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0\n"
                + "ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY\n"
                + "BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n"
                + "iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk\n"
                + "wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT\n"
                + "WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG\n"
                + "SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ\n"
                + "H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC\n"
                + "1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=\n"
                + "-----END CERTIFICATE-----\n";
        ByteArrayInputStream certArray = new ByteArrayInputStream(
                certificateData.getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        certificate = cf.generateCertificate(certArray);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with null parameter only.",
        method = "load",
        args = {java.security.KeyStore.LoadStoreParameter.class}
    )
    public void test_load() throws Exception {
        mockKeyStore.load(null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with null parameter only",
        method = "store",
        args = {java.security.KeyStore.LoadStoreParameter.class}
    )
    public void test_store() throws Exception {
        try {
            mockKeyStore.store(null);
            fail("should throw KeyStoreException: not initialized");
        } catch (KeyStoreException e) {
        }
        mockKeyStore.load(null, null);
        mockKeyStore.store(null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with all null parameters only",
        method = "setKeyEntry",
        args = {java.lang.String.class, java.security.Key.class, char[].class, java.security.cert.Certificate[].class}
    )
    public void test_setKeyEntry_null() throws Exception {
        mockKeyStore.load(null, null); 
        mockKeyStore.setKeyEntry(null, null, null, null);        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with null parameters only",
        method = "setKeyEntry",
        args = {java.lang.String.class, java.security.Key.class, char[].class, java.security.cert.Certificate[].class}
    )
    public void test_setKeyEntry_key_is_null() throws Exception {
        mockKeyStore.load(null, null);
        mockKeyStore.setKeyEntry("Alias", null, null, new Certificate[]{certificate});        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with null parameters",
        method = "setKeyEntry",
        args = {java.lang.String.class, java.security.Key.class, char[].class, java.security.cert.Certificate[].class}
    )
    public void test_setKeyEntry_key_is_private() throws Exception {
        mockKeyStore.load(null, null);
        Key key = keyPair.getPrivate();
        try {
            mockKeyStore.setKeyEntry("Alias", key, null, null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            mockKeyStore.setKeyEntry("Alias", key, null,
                    new Certificate[0]);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        mockKeyStore.setKeyEntry("Alias", key, null, new Certificate[]{certificate});        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with null parameters",
        method = "setKeyEntry",
        args = {java.lang.String.class, java.security.Key.class, char[].class, java.security.cert.Certificate[].class}
    )
    public void test_setKeyEntry_key_is_public() throws Exception
    {
        mockKeyStore.load(null, null);
        Key key = keyPair.getPublic();
        mockKeyStore.setKeyEntry("Alias1", key, null, null);
        mockKeyStore.setKeyEntry("Alias2", key, null,
                new Certificate[0]);
        mockKeyStore.setKeyEntry("Alias3", key, null, new Certificate[]{certificate});
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with null parameters",
        method = "setCertificateEntry",
        args = {java.lang.String.class, java.security.cert.Certificate.class}
    )
    public void test_setCertificateEntry_null() throws Exception {
        mockKeyStore.load(null, null);
        mockKeyStore.setCertificateEntry(null, null);
        mockKeyStore.setCertificateEntry(null, certificate);
        mockKeyStore.setCertificateEntry("Alias", null);
    }
    @SuppressWarnings("cast")
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyStore",
        args = {java.security.KeyStoreSpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void test_KeyStore() {
        Provider p = new MyProvider();
        try {
            MyKeyStore ks = new MyKeyStore(new MyKeyStoreSpi(), p, "MyKeyStore");
            assertNotNull(ks);
            assertTrue(ks instanceof KeyStore);
        } catch (Exception e) {
            fail("Exception should be not thrown");
        }
        try {
            MyKeyStore ks = new MyKeyStore(null, null, null);
            assertNotNull(ks);
            assertTrue(ks instanceof KeyStore);
        } catch (Exception e) {
            fail("Exception should be not thrown");
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
        mockKeyStore = new MyKeyStore(new MyKeyStoreSpi(), null, "MyKeyStore");
    }
    private static class MyKeyStore extends KeyStore {
        public MyKeyStore(KeyStoreSpi keyStoreSpi, Provider provider,
                String type) {
            super(keyStoreSpi, provider, type);
        }
    }
    @SuppressWarnings("unused")
    private static class MyKeyStoreSpi extends KeyStoreSpi {
        public Enumeration<String> engineAliases() {
            return null;
        }
        public boolean engineContainsAlias(String arg0) {
            return false;
        }
        public void engineDeleteEntry(String arg0) throws KeyStoreException {
        }
        public Certificate engineGetCertificate(String arg0) {
            return null;
        }
        public String engineGetCertificateAlias(Certificate arg0) {
            return null;
        }
        public Certificate[] engineGetCertificateChain(String arg0) {
            return null;
        }
        public Date engineGetCreationDate(String arg0) {
            return null;
        }
        public Key engineGetKey(String arg0, char[] arg1)
                throws NoSuchAlgorithmException, UnrecoverableKeyException {
            return null;
        }
        public boolean engineIsCertificateEntry(String arg0) {
            return false;
        }
        public boolean engineIsKeyEntry(String arg0) {
            return false;
        }
        public void engineLoad(InputStream arg0, char[] arg1)
                throws IOException, NoSuchAlgorithmException,
                CertificateException {
            return;
        }
        public void engineSetCertificateEntry(String arg0, Certificate arg1)
                throws KeyStoreException {
            return;
        }
        public void engineSetKeyEntry(String arg0, byte[] arg1,
                Certificate[] arg2) throws KeyStoreException {
            return;
        }
        public void engineSetKeyEntry(String arg0, Key arg1, char[] arg2,
                Certificate[] arg3) throws KeyStoreException {
            return;
        }
        public int engineSize() { 
            return 0; 
        }
        public void engineStore(KeyStore.LoadStoreParameter param){ 
            return; 
        }
        public void engineStore(OutputStream arg0, char[] arg1) 
                throws IOException, NoSuchAlgorithmException, 
                CertificateException { 
            return; 
        }        
    }    
    @SuppressWarnings("serial")
    private class MyProvider extends Provider {
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("AlgorithmParameters.ABC", MyAlgorithmParameters.class
                    .getName());
        }
        MyProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }
}
