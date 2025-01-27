public class PKCS8Key implements PrivateKey {
    private static final long serialVersionUID = -3836890099307167124L;
    protected AlgorithmId algid;
    protected byte[] key;
    protected byte[] encodedKey;
    public static final BigInteger version = BigInteger.ZERO;
    public PKCS8Key() { }
    private PKCS8Key (AlgorithmId algid, byte key [])
    throws InvalidKeyException {
        this.algid = algid;
        this.key = key;
        encode();
    }
    public static PKCS8Key parse (DerValue in) throws IOException {
        PrivateKey key;
        key = parseKey(in);
        if (key instanceof PKCS8Key)
            return (PKCS8Key)key;
        throw new IOException("Provider did not return PKCS8Key");
    }
    public static PrivateKey parseKey (DerValue in) throws IOException
    {
        AlgorithmId algorithm;
        PrivateKey privKey;
        if (in.tag != DerValue.tag_Sequence)
            throw new IOException ("corrupt private key");
        BigInteger parsedVersion = in.data.getBigInteger();
        if (!version.equals(parsedVersion)) {
            throw new IOException("version mismatch: (supported: " +
                                  Debug.toHexString(version) +
                                  ", parsed: " +
                                  Debug.toHexString(parsedVersion));
        }
        algorithm = AlgorithmId.parse (in.data.getDerValue ());
        try {
            privKey = buildPKCS8Key (algorithm, in.data.getOctetString ());
        } catch (InvalidKeyException e) {
            throw new IOException("corrupt private key");
        }
        if (in.data.available () != 0)
            throw new IOException ("excess private key");
        return privKey;
    }
    protected void parseKeyBits () throws IOException, InvalidKeyException {
        encode();
    }
    static PrivateKey buildPKCS8Key (AlgorithmId algid, byte[] key)
    throws IOException, InvalidKeyException
    {
        DerOutputStream pkcs8EncodedKeyStream = new DerOutputStream();
        encode(pkcs8EncodedKeyStream, algid, key);
        PKCS8EncodedKeySpec pkcs8KeySpec
            = new PKCS8EncodedKeySpec(pkcs8EncodedKeyStream.toByteArray());
        try {
            KeyFactory keyFac = KeyFactory.getInstance(algid.getName());
            return keyFac.generatePrivate(pkcs8KeySpec);
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeySpecException e) {
        }
        String classname = "";
        try {
            Properties props;
            String keytype;
            Provider sunProvider;
            sunProvider = Security.getProvider("SUN");
            if (sunProvider == null)
                throw new InstantiationException();
            classname = sunProvider.getProperty("PrivateKey.PKCS#8." +
              algid.getName());
            if (classname == null) {
                throw new InstantiationException();
            }
            Class keyClass = null;
            try {
                keyClass = Class.forName(classname);
            } catch (ClassNotFoundException e) {
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                if (cl != null) {
                    keyClass = cl.loadClass(classname);
                }
            }
            Object      inst = null;
            PKCS8Key    result;
            if (keyClass != null)
                inst = keyClass.newInstance();
            if (inst instanceof PKCS8Key) {
                result = (PKCS8Key) inst;
                result.algid = algid;
                result.key = key;
                result.parseKeyBits();
                return result;
            }
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
            throw new IOException (classname + " [internal error]");
        }
        PKCS8Key result = new PKCS8Key();
        result.algid = algid;
        result.key = key;
        return result;
    }
    public String getAlgorithm() {
        return algid.getName();
    }
    public AlgorithmId  getAlgorithmId () { return algid; }
    public final void encode(DerOutputStream out) throws IOException
    {
        encode(out, this.algid, this.key);
    }
    public synchronized byte[] getEncoded() {
        byte[] result = null;
        try {
            result = encode();
        } catch (InvalidKeyException e) {
        }
        return result;
    }
    public String getFormat() {
        return "PKCS#8";
    }
    public byte[] encode() throws InvalidKeyException {
        if (encodedKey == null) {
            try {
                DerOutputStream out;
                out = new DerOutputStream ();
                encode (out);
                encodedKey = out.toByteArray();
            } catch (IOException e) {
                throw new InvalidKeyException ("IOException : " +
                                               e.getMessage());
            }
        }
        return encodedKey.clone();
    }
    public String toString ()
    {
        HexDumpEncoder  encoder = new HexDumpEncoder ();
        return "algorithm = " + algid.toString ()
            + ", unparsed keybits = \n" + encoder.encodeBuffer (key);
    }
    public void decode(InputStream in) throws InvalidKeyException
    {
        DerValue        val;
        try {
            val = new DerValue (in);
            if (val.tag != DerValue.tag_Sequence)
                throw new InvalidKeyException ("invalid key format");
            BigInteger version = val.data.getBigInteger();
            if (!version.equals(this.version)) {
                throw new IOException("version mismatch: (supported: " +
                                      Debug.toHexString(this.version) +
                                      ", parsed: " +
                                      Debug.toHexString(version));
            }
            algid = AlgorithmId.parse (val.data.getDerValue ());
            key = val.data.getOctetString ();
            parseKeyBits ();
            if (val.data.available () != 0)  {
            }
        } catch (IOException e) {
            throw new InvalidKeyException("IOException : " +
                                          e.getMessage());
        }
    }
    public void decode(byte[] encodedKey) throws InvalidKeyException {
        decode(new ByteArrayInputStream(encodedKey));
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PRIVATE,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }
    private void readObject (ObjectInputStream stream)
    throws IOException {
        try {
            decode(stream);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IOException("deserialized key is invalid: " +
                                  e.getMessage());
        }
    }
    static void encode(DerOutputStream out, AlgorithmId algid, byte[] key)
        throws IOException {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putInteger(version);
            algid.encode(tmp);
            tmp.putOctetString(key);
            out.write(DerValue.tag_Sequence, tmp);
    }
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Key) {
            byte[] b1;
            if (encodedKey != null) {
                b1 = encodedKey;
            } else {
                b1 = getEncoded();
            }
            byte[] b2 = ((Key)object).getEncoded();
            int i;
            if (b1.length != b2.length)
                return false;
            for (i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public int hashCode() {
        int retval = 0;
        byte[] b1 = getEncoded();
        for (int i = 1; i < b1.length; i++) {
            retval += b1[i] * i;
        }
        return(retval);
    }
}
