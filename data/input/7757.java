public class RC2AlgorithmParameters {
    private final static char[] hexDigits = "0123456789abcdef".toCharArray();
    public static void main(String[] args) throws Exception {
        byte[] iv_1 = {
            (byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,
            (byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,
            (byte)0x33,(byte)0x33
        };
        AlgorithmParameters rc2Params =
           AlgorithmParameters.getInstance("RC2", "SunJCE");
        if (!rc2Params.getAlgorithm().equals("RC2")) {
            throw new Exception("getAlgorithm() returned "
                + rc2Params.getAlgorithm() + " instead of RC2");
        }
        byte[] encoded = testParams(rc2Params, new RC2ParameterSpec(2, iv_1));
        encoded = testParams(AlgorithmParameters.getInstance("RC2"),
            new RC2ParameterSpec(0, iv_1));
        runTests(tests);
    }
    static void runTests(CipherTest[] tests) throws Exception {
        for (int i = 0; i < tests.length; i++) {
            CipherTest test = tests[i];
            test.run();
        }
        System.out.println("All tests passed");
    }
    private static byte[] testParams(AlgorithmParameters rc2Params,
        RC2ParameterSpec rc2Spec) throws Exception {
        rc2Params.init(rc2Spec);
        RC2ParameterSpec rc2OtherSpec = (RC2ParameterSpec)
            rc2Params.getParameterSpec(RC2ParameterSpec.class);
        if (!rc2Spec.equals(rc2OtherSpec)) {
            throw new Exception("AlgorithmParameterSpecs should be equal");
        }
        Cipher rc2Cipher = Cipher.getInstance("RC2/CBC/PKCS5PADDING", "SunJCE");
        rc2Cipher.init(Cipher.ENCRYPT_MODE,
            new SecretKeySpec("secret".getBytes("ASCII"), "RC2"), rc2Spec);
        byte[] iv = rc2Cipher.getIV();
        if (!Arrays.equals(iv, rc2Spec.getIV())) {
            throw new Exception("ivs should be equal");
        }
        byte[] encoded = rc2Params.getEncoded();
        AlgorithmParameters params = AlgorithmParameters.getInstance("RC2");
        params.init(encoded);
        rc2Cipher.init(Cipher.ENCRYPT_MODE,
            new SecretKeySpec("secret".getBytes("ASCII"), "RC2"), params);
        iv = rc2Cipher.getIV();
        if (!Arrays.equals(iv, rc2Spec.getIV())) {
            throw new Exception("ivs should be equal");
        }
        return encoded;
    }
    private static void dumpBytes(byte[] encoded, String file)
        throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(encoded);
        fos.close();
    }
    public static String toString(byte[] b) {
        if (b == null) {
            return "(null)";
        }
        StringBuffer sb = new StringBuffer(b.length * 3);
        for (int i = 0; i < b.length; i++) {
            int k = b[i] & 0xff;
            if (i != 0) {
                sb.append(':');
            }
            sb.append(hexDigits[k >>> 4]);
            sb.append(hexDigits[k & 0xf]);
        }
        return sb.toString();
    }
    public static byte[] parse(String s) {
        try {
            int n = s.length();
            ByteArrayOutputStream out = new ByteArrayOutputStream(n / 3);
            StringReader r = new StringReader(s);
            while (true) {
                int b1 = nextNibble(r);
                if (b1 < 0) {
                    break;
                }
                int b2 = nextNibble(r);
                if (b2 < 0) {
                    throw new RuntimeException("Invalid string " + s);
                }
                int b = (b1 << 4) | b2;
                out.write(b);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static byte[] b(String s) {
        return parse(s);
    }
    private static int nextNibble(StringReader r) throws IOException {
        while (true) {
            int ch = r.read();
            if (ch == -1) {
                return -1;
            } else if ((ch >= '0') && (ch <= '9')) {
                return ch - '0';
            } else if ((ch >= 'a') && (ch <= 'f')) {
                return ch - 'a' + 10;
            } else if ((ch >= 'A') && (ch <= 'F')) {
                return ch - 'A' + 10;
            }
        }
    }
    static class CipherTest {
        private final byte[] plaintext;
        private final byte[] ciphertext;
        private final byte[] key;
        private final int effectiveKeySize;
        CipherTest(String plaintext, String ciphertext,
            String key, int effectiveKeySize) {
            this.plaintext = b(plaintext);
            this.ciphertext = b(ciphertext);
            this.key = b(key);
            this.effectiveKeySize = effectiveKeySize;
        }
        void run() throws Exception {
            Cipher cipher = Cipher.getInstance("RC2/ECB/NOPADDING", "SunJCE");
            SecretKey keySpec = new SecretKeySpec(key, "RC2");
            RC2ParameterSpec rc2Spec = new RC2ParameterSpec(effectiveKeySize);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, rc2Spec);
            byte[] enc = cipher.doFinal(plaintext);
            if (Arrays.equals(ciphertext, enc) == false) {
                System.out.println("RC2AlgorithmParameters Cipher test " +
                    "encryption failed:");
                System.out.println("plaintext:  "
                    + RC2AlgorithmParameters.toString(plaintext));
                System.out.println("ciphertext: "
                    + RC2AlgorithmParameters.toString(ciphertext));
                System.out.println("encrypted:  "
                    + RC2AlgorithmParameters.toString(enc));
                System.out.println("key:        "
                    + RC2AlgorithmParameters.toString(key));
                System.out.println("effective key length:        "
                    + effectiveKeySize);
                throw new Exception("RC2AlgorithmParameters Cipher test "
                    + "encryption failed");
            }
            enc = cipher.doFinal(plaintext);
            if (Arrays.equals(ciphertext, enc) == false) {
                throw new Exception("Re-encryption test failed");
            }
            cipher.init(Cipher.DECRYPT_MODE, keySpec, rc2Spec);
            byte[] dec = cipher.doFinal(ciphertext);
            if (Arrays.equals(plaintext, dec) == false) {
                System.out.println("RC2AlgorithmParameters Cipher test "
                    + "decryption failed:");
                System.out.println("plaintext:  "
                    + RC2AlgorithmParameters.toString(plaintext));
                System.out.println("ciphertext: "
                    + RC2AlgorithmParameters.toString(ciphertext));
                System.out.println("decrypted:  "
                    + RC2AlgorithmParameters.toString(dec));
                System.out.println("key:        "
                    + RC2AlgorithmParameters.toString(key));
                System.out.println("effective key length:        "
                    + effectiveKeySize);
                throw new Exception("RC2AlgorithmParameters Cipher test "
                    + "decryption failed");
            }
            System.out.println("passed");
        }
    }
    private final static CipherTest[] tests = {
        new CipherTest("00:00:00:00:00:00:00:00", "EB:B7:73:F9:93:27:8E:FF",
                       "00:00:00:00:00:00:00:00", 63),
        new CipherTest("FF:FF:FF:FF:FF:FF:FF:FF", "27:8B:27:E4:2E:2F:0D:49",
                       "FF:FF:FF:FF:FF:FF:FF:FF", 64),
        new CipherTest("10:00:00:00:00:00:00:01", "30:64:9E:DF:9B:E7:D2:C2",
                       "30:00:00:00:00:00:00:00", 64),
        new CipherTest("00:00:00:00:00:00:00:00", "6C:CF:43:08:97:4C:26:7F",
                       "88:BC:A9:0E:90:87:5A", 64),
        new CipherTest("00:00:00:00:00:00:00:00", "1A:80:7D:27:2B:BE:5D:B1",
                       "88:BC:A9:0E:90:87:5A:7F:0F:79:C3:84:62:7B:AF:B2", 64),
        new CipherTest("00:00:00:00:00:00:00:00", "22:69:55:2A:B0:F8:5C:A6",
                       "88:BC:A9:0E:90:87:5A:7F:0F:79:C3:84:62:7B:AF:B2", 128),
    };
}
