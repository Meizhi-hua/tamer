@TestTargetClass(KeyPairGenerator.class)
public class KeyPairGenerator2Test extends TestCase {
    private String KeyPairGeneratorProviderClass = "";
    private static final String KeyPairGeneratorProviderClass1 = "org.apache.harmony.security.tests.support.MyKeyPairGenerator1";
    private static final String KeyPairGeneratorProviderClass2 = "org.apache.harmony.security.tests.support.MyKeyPairGenerator2";
    private static final String KeyPairGeneratorProviderClass3 = "org.apache.harmony.security.tests.support.MyKeyPairGenerator3";
    private static final String KeyPairGeneratorProviderClass4 = "org.apache.harmony.security.tests.support.MyKeyPairGeneratorSpi";
    private static final String defaultAlg = "KPGen";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static final String[] validValues;
    String post;
    static {
        validValues = new String[4];
        validValues[0] = defaultAlg;
        validValues[1] = defaultAlg.toLowerCase();
        validValues[2] = "kpGEN";
        validValues[3] = "kPGEn";
    }
    Provider mProv; 
    String resAlg;
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }
    protected void setProv() {
        mProv = (new SpiEngUtils()).new MyProvider("MyKPGenProvider".concat(post),
                "Testing provider", KeyPairGenerator1Test.srvKeyPairGenerator.concat(".")
                        .concat(defaultAlg.concat(post)),
                KeyPairGeneratorProviderClass);
        Security.insertProviderAt(mProv, 1);
    }
    private void checkResult(KeyPairGenerator keyPairGen, int mode)
            throws InvalidAlgorithmParameterException {
        AlgorithmParameterSpec pp = null;
        switch (mode) {
        case 1:
            try {
                keyPairGen.initialize(pp, new SecureRandom());
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
            keyPairGen.initialize(1000, new SecureRandom());
            try {
                keyPairGen.initialize(-1024, new SecureRandom());
                fail("InvalidParameterException must be thrown");
            } catch (InvalidParameterException e) {
                assertEquals("Incorrect exception", e.getMessage(),
                        "Incorrect keysize parameter");
            }
            try {
                keyPairGen.initialize(100, null);
                fail("InvalidParameterException must be thrown");
            } catch (InvalidParameterException e) {
                assertEquals("Incorrect exception", e.getMessage(),
                        "Incorrect random");
            }
            keyPairGen.generateKeyPair();
            keyPairGen.genKeyPair();
            break;
        case 2:
            try {
                keyPairGen.initialize(pp, new SecureRandom());
            } catch (UnsupportedOperationException e) {
            }
            keyPairGen.initialize(1000, new SecureRandom());
            try {
                keyPairGen.initialize(63, new SecureRandom());
                fail("InvalidParameterException must be thrown");
            } catch (InvalidParameterException e) {
            }
            keyPairGen.initialize(100, null);
            assertNull("Not null KeyPair", keyPairGen.generateKeyPair());
            assertNull("Not null KeyPair", keyPairGen.genKeyPair());
            break;
        case 3:
            keyPairGen.initialize(pp, new SecureRandom());
            keyPairGen.initialize(pp);
            keyPairGen.initialize(1000, new SecureRandom());
            keyPairGen.initialize(100);
            assertNotNull("Null KeyPair", keyPairGen.generateKeyPair());
            assertNotNull("Null KeyPair", keyPairGen.genKeyPair());
            break;
        case 4:
            try {
                keyPairGen.initialize(pp, null);
                fail("UnsupportedOperationException must be thrown");
            } catch (UnsupportedOperationException e) {
            }
            keyPairGen.initialize(pp, new SecureRandom());
            keyPairGen.initialize(101, new SecureRandom());
            keyPairGen.initialize(10000);
            try {
                keyPairGen.initialize(101, null);
                fail("IllegalArgumentException must be thrown for null random");
            } catch (IllegalArgumentException e) {
            }
            try {
                keyPairGen.initialize(99, new SecureRandom());
                fail("InvalidParameterException must be thrown for invalid key");
            } catch (InvalidParameterException e) {
            }
            try {
                keyPairGen.initialize(99);
                fail("InvalidParameterException must be thrown for invalid key");
            } catch (InvalidParameterException e) {
            }
            try {
                keyPairGen.initialize(199, null);
                fail("IllegalArgumentException must be thrown for null random");
            } catch (IllegalArgumentException e) {
            }
            assertNull("Not null KeyPair", keyPairGen.generateKeyPair());
            assertNull("Not null KeyPair", keyPairGen.genKeyPair());
            break;
        }
    }
    private void GetInstance01(int mode) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        try {
            KeyPairGenerator.getInstance(null);
            fail("NullPointerException or KeyStoreException must be thrown");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        KeyPairGenerator kpG;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            kpG = KeyPairGenerator.getInstance(alg);
            assertEquals("Incorrect algorithm", kpG.getAlgorithm()
                    .toUpperCase(), (mode <= 2 ? resAlg : alg).toUpperCase());
            assertEquals("Incorrect provider", kpG.getProvider(), mProv);
            checkResult(kpG, mode);
        }
    }
    public void GetInstance02(int mode) throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        try {
            KeyPairGenerator.getInstance(null, mProv.getName());
            fail("NullPointerException or KeyStoreException must be thrown");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i], mProv.getName());
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            try {
                KeyPairGenerator.getInstance(alg, prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(alg).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    KeyPairGenerator.getInstance(alg, invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (algorithm: "
                            .concat(alg).concat(" provider: ").concat(
                                    invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        KeyPairGenerator kpG;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            kpG = KeyPairGenerator.getInstance(alg, mProv.getName());
            assertEquals("Incorrect algorithm", kpG.getAlgorithm()
                    .toUpperCase(), (mode <= 2 ? resAlg : alg).toUpperCase());
            assertEquals("Incorrect provider", kpG.getProvider().getName(),
                    mProv.getName());
            checkResult(kpG, mode);
        }
    }
    private void GetInstance03(int mode) throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        try {
            KeyPairGenerator.getInstance(null, mProv);
            fail("NullPointerException or KeyStoreException must be thrown");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i], mProv);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            try {
                KeyPairGenerator.getInstance(alg, prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(alg).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        KeyPairGenerator kpG;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            kpG = KeyPairGenerator.getInstance(alg, mProv);
            assertEquals("Incorrect algorithm", kpG.getAlgorithm()
                    .toUpperCase(), (mode <= 2 ? resAlg : alg).toUpperCase());
            assertEquals("Incorrect provider", kpG.getProvider(), mProv);
            checkResult(kpG, mode);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance01() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass1;
        resAlg = MyKeyPairGenerator1.getResAlgorithm();
        post = "_1";
        setProv();
        GetInstance01(1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetInstance02() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass1;
        resAlg = MyKeyPairGenerator1.getResAlgorithm();
        post = "_1";
        setProv();
        GetInstance02(1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testGetInstance03() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass1;
        resAlg = MyKeyPairGenerator1.getResAlgorithm();
        post = "_1";
        setProv();
        GetInstance03(1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance04() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass2;
        resAlg = MyKeyPairGenerator2.getResAlgorithm();
        post = "_2";
        setProv();
        GetInstance01(2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetInstance05() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass2;
        resAlg = MyKeyPairGenerator2.getResAlgorithm();
        post = "_2";
        setProv();
        GetInstance02(2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testGetInstance06() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass2;
        resAlg = MyKeyPairGenerator2.getResAlgorithm();
        post = "_2";
        setProv();
        GetInstance03(2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance07() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass3;
        resAlg = "";
        post = "_3";
        setProv();
        GetInstance01(3);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetInstance08() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass3;
        resAlg = "";
        post = "_3";
        setProv();
        GetInstance02(3);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testGetInstance09() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass3;
        resAlg = "";
        post = "_3";
        setProv();
        GetInstance03(3);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance10() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass4;
        resAlg = "";
        post = "_4";
        setProv();
        GetInstance01(4);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetInstance11() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass4;
        resAlg = "";
        post = "_4";
        setProv();
        GetInstance02(4);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testGetInstance12() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass4;
        resAlg = "";
        post = "_4";
        setProv();
        GetInstance03(4);
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(KeyPairGenerator2Test.class);
    }
}
