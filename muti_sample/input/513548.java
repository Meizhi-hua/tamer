public class MyKeyStore extends KeyStoreSpi {
    private Hashtable<String, Object> Keys = new Hashtable<String, Object>();
    private Hashtable<String, Object> Cert = new Hashtable<String, Object>();
    private Hashtable<String, Object> Chain = new Hashtable<String, Object>();
    private Hashtable<String, Object> Dates = new Hashtable<String, Object>();
    private Hashtable<String, Object> KeysSL = new Hashtable<String, Object>();
    private Hashtable<String, Object> CertSL = new Hashtable<String, Object>();
    private Hashtable<String, Object> ChainSL = new Hashtable<String, Object>();
    private Hashtable<String, Object> DatesSL = new Hashtable<String, Object>();
    public Key engineGetKey(String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (Keys.containsKey(alias)) {
            return (Key) Keys.get(alias);
        }
        return null;
    }
    public Certificate[] engineGetCertificateChain(String alias) {
        if (Chain.containsKey(alias)) {
            return (Certificate[]) Chain.get(alias);
        }
        return null;
    }
    public Certificate engineGetCertificate(String alias) {
        if (Cert.containsKey(alias)) {
            return (Certificate) Cert.get(alias);
        }
        return null;
    }
    public Date engineGetCreationDate(String alias) {
        if (Dates.containsKey(alias)) {
            return (Date) Dates.get(alias);
        }
        return null;
    }
    public void engineSetKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        if (Cert.containsKey(alias)) {
            Cert.remove(alias);
        }
        Keys.put(alias, key);
        if (chain != null) {
            Chain.put(alias, chain);
        }
        Dates.put(alias, new Date());
    }
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        if (key == null) {
            throw new KeyStoreException("Not Supported for null key");
        }
        if (Cert.containsKey(alias)) {
            Cert.remove(alias);
        }
        if (Chain.containsKey(alias)) {
            Chain.remove(alias);
        }
        KeyStoreTestSupport.MyPrivateKey keyK = new KeyStoreTestSupport.MyPrivateKey(
                alias, alias, key);
        Keys.put(alias, keyK);
        if (chain != null) {
            Chain.put(alias, chain);
        }
        Dates.put(alias, new Date());
    }
    public void engineSetCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException {
        Cert.put(alias, cert);
        Dates.put(alias, new Date());
    }
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (Keys.containsKey(alias)) {
            Keys.remove(alias);
            Chain.remove(alias);
            return;
        }
        if (Cert.containsKey(alias)) {
            Cert.remove(alias);
        }
    }
    public Enumeration<String> engineAliases() {
        return null;
    }
    public boolean engineContainsAlias(String alias) {
        if (Keys.containsKey(alias)) {
            return true;
        }
        if (Cert.containsKey(alias)) {
            return true;
        }
        return false;
    }
    public int engineSize() {
        return (Keys.size() + Cert.size());
    }
    public boolean engineIsKeyEntry(String alias) {
        if (Keys.containsKey(alias)) {
            return true;
        }
        return false;
    }
    public boolean engineIsCertificateEntry(String alias) {
        if (Cert.containsKey(alias)) {
            return true;
        }
        return false;
    }
    public String engineGetCertificateAlias(Certificate cert) {
        return "";
    }
    public void engineStore(OutputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!(stream instanceof ByteArrayOutputStream)) {
            throw new IOException("Incorrect stream");
        }
        String alias;
        Enumeration e = Keys.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            KeysSL.put(alias, Keys.get(alias));
            DatesSL.put(alias, Dates.get(alias));
            if (Chain.containsKey(alias)) {
                ChainSL.put(alias, Chain.get(alias));
            }
        }
        e = Cert.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            CertSL.put(alias, Cert.get(alias));
            DatesSL.put(alias, Dates.get(alias));
        }
    }
    public void engineLoad(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        Keys.clear();
        Cert.clear();
        Chain.clear();
        Dates.clear();
        String alias;
        Enumeration e = KeysSL.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            Keys.put(alias, KeysSL.get(alias));
            Dates.put(alias, DatesSL.get(alias));
            if (ChainSL.containsKey(alias)) {
                Chain.put(alias, ChainSL.get(alias));
            }
        }
        e = CertSL.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            Cert.put(alias, CertSL.get(alias));
            Dates.put(alias, DatesSL.get(alias));
        }
    }
    public void engineStore(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            throw new IOException("param is null");
        }
    }
    public void engineLoad(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!(param instanceof MyLoadStoreParams)) {
            throw new IllegalArgumentException("param is not MyLoadStoreParams: " + param);
        }
    }
}