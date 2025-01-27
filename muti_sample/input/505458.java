@TestTargetClass(SecretKeyFactory.class)
public class SecretKeyFactoryTest extends TestCase {
    public static final String srvSecretKeyFactory = "SecretKeyFactory";
    private static String defaultAlgorithm1 = "DESede";
    private static String defaultAlgorithm2 = "DES";
    public static String defaultAlgorithm = null;
    private static String defaultProviderName = null;
    private static Provider defaultProvider = null;
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    public static final String[] validValues = new String[2];
    private static boolean DEFSupported = false;
    private static final String NotSupportMsg = "Default algorithm is not supported";
    static {
        defaultProvider = SpiEngUtils.isSupport(defaultAlgorithm1,
                srvSecretKeyFactory);
        DEFSupported = (defaultProvider != null);
        if (DEFSupported) {
            defaultAlgorithm = defaultAlgorithm1;
            validValues[0] = defaultAlgorithm.toUpperCase();
            validValues[1] = defaultAlgorithm.toLowerCase();
            defaultProviderName = defaultProvider.getName();
        } else {
            defaultProvider = SpiEngUtils.isSupport(defaultAlgorithm2,
                    srvSecretKeyFactory);
            DEFSupported = (defaultProvider != null);
            if (DEFSupported) {
                defaultAlgorithm = defaultAlgorithm2;
                validValues[0] = defaultAlgorithm.toUpperCase();
                validValues[2] = defaultAlgorithm.toLowerCase();
                defaultProviderName = defaultProvider.getName();
            } else {
                defaultAlgorithm = null;
                defaultProviderName = null;
                defaultProvider = null;
            }
        }
    }
    protected SecretKeyFactory[] createSKFac() {
        if (!DEFSupported) {
            fail(defaultAlgorithm + " algorithm is not supported");
            return null;
        }
        SecretKeyFactory[] skF = new SecretKeyFactory[3];
        try {
            skF[0] = SecretKeyFactory.getInstance(defaultAlgorithm);
            skF[1] = SecretKeyFactory.getInstance(defaultAlgorithm,
                    defaultProvider);
            skF[2] = SecretKeyFactory.getInstance(defaultAlgorithm,
                    defaultProviderName);
            return skF;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecretKeyFactory",
        args = {javax.crypto.SecretKeyFactorySpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void testSecretKeyFactory01() throws NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        SecretKeyFactorySpi spi = new MySecretKeyFactorySpi();
        SecretKeyFactory secKF = new mySecretKeyFactory(spi, defaultProvider,
                defaultAlgorithm);
        assertEquals("Incorrect algorithm", secKF.getAlgorithm(),
                defaultAlgorithm);
        assertEquals("Incorrect provider", secKF.getProvider(), defaultProvider);
        assertNull("Incorrect result", secKF.generateSecret(null));
        assertNull("Incorrect result", secKF.getKeySpec(null, null));
        assertNull("Incorrect result", secKF.translateKey(null));
        secKF = new mySecretKeyFactory(null, null, null);
        assertNull("Algorithm must be null", secKF.getAlgorithm());
        assertNull("Provider must be null", secKF.getProvider());
        try {
            secKF.translateKey(null);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {            
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testSecretKeyFactory02() throws NoSuchAlgorithmException {
        try {
            SecretKeyFactory.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown if algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SecretKeyFactory.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException was not thrown as expected");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testSecretKeyFactory03() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            SecretKeyFactory secKF = SecretKeyFactory
                    .getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", secKF.getAlgorithm(),
                    validValues[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testSecretKeyFactory04() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        try {
            SecretKeyFactory.getInstance(null, defaultProviderName);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown if algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SecretKeyFactory.getInstance(invalidValues[i],
                        defaultProviderName);
                fail("NoSuchAlgorithmException was not thrown as expected (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testSecretKeyFactory05() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                SecretKeyFactory.getInstance(validValues[i], prov);
                fail("IllegalArgumentException was not thrown as expected (algorithm: "
                        .concat(validValues[i]).concat(" provider: null"));
            } catch (IllegalArgumentException e) {
            }
            try {
                SecretKeyFactory.getInstance(validValues[i], "");
                fail("IllegalArgumentException was not thrown as expected (algorithm: "
                        .concat(validValues[i]).concat(" provider: empty"));
            } catch (IllegalArgumentException e) {
            }
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    SecretKeyFactory.getInstance(validValues[i],
                            invalidValues[j]);
                    fail("NoSuchProviderException was not thrown as expected (algorithm: "
                            .concat(validValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testSecretKeyFactory06() throws NoSuchProviderException,
            NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            SecretKeyFactory secKF = SecretKeyFactory.getInstance(
                    validValues[i], defaultProviderName);
            assertEquals("Incorrect algorithm", secKF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", secKF.getProvider().getName(),
                    defaultProviderName);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testSecretKeyFactory07() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        try {
            SecretKeyFactory.getInstance(null, defaultProvider);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown if algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SecretKeyFactory.getInstance(invalidValues[i], defaultProvider);
                fail("NoSuchAlgorithmException was not thrown as expected (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testSecretKeyFactory08() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                SecretKeyFactory.getInstance(validValues[i], prov);
                fail("IllegalArgumentException was not thrown as expected (provider is null, algorithm: "
                        .concat(validValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getInstance method.",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testSecretKeyFactory09() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            SecretKeyFactory secKF = SecretKeyFactory.getInstance(
                    validValues[i], defaultProvider);
            assertEquals("Incorrect algorithm", secKF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", secKF.getProvider(),
                    defaultProvider);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Tis test is checking two methods.",
            method = "generateSecret",
            args = {java.security.spec.KeySpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Tis test is checking two methods.",
            clazz = SecretKeyFactorySpi.class,
            method = "engineGenerateSecret",
            args = {java.security.spec.KeySpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Tis test is checking two methods.",
            method = "getKeySpec",
            args = {javax.crypto.SecretKey.class, java.lang.Class.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Tis test is checking two methods.",
            clazz = SecretKeyFactorySpi.class,
            method = "engineGetKeySpec",
            args = {javax.crypto.SecretKey.class, java.lang.Class.class}
        )
    })
    public void testSecretKeyFactory10() throws InvalidKeyException,
            InvalidKeySpecException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        byte[] bb = new byte[24];
        KeySpec ks = (defaultAlgorithm.equals(defaultAlgorithm2) ? (KeySpec)new DESKeySpec(bb) :
            (KeySpec)new DESedeKeySpec(bb));
        KeySpec rks = null;
        SecretKeySpec secKeySpec = new SecretKeySpec(bb, defaultAlgorithm);
        SecretKey secKey = null;
        SecretKeyFactory[] skF = createSKFac();
        assertNotNull("SecretKeyFactory object were not created", skF);
        for (int i = 0; i < skF.length; i++) {
            try {
                skF[i].generateSecret(null);
                fail("generateSecret(null): InvalidKeySpecException must be thrown");
            } catch (InvalidKeySpecException e) {
            }
            secKey = skF[i].generateSecret(ks);
            try {
                skF[i].getKeySpec(null, null);
                fail("getKeySpec(null,null): InvalidKeySpecException must be thrown");
            } catch (InvalidKeySpecException e) {
            }
            try {
                skF[i].getKeySpec(null, ks.getClass());
                fail("getKeySpec(null, Class): InvalidKeySpecException must be thrown");
            } catch (InvalidKeySpecException e) {
            }
            try {
                skF[i].getKeySpec(secKey, null);
                fail("getKeySpec(secKey, null): NullPointerException or InvalidKeySpecException must be thrown");
            } catch (InvalidKeySpecException e) {
            } catch (NullPointerException e) {
            }
            try {
                Class c;
                if (defaultAlgorithm.equals(defaultAlgorithm2)) {
                    c = DESedeKeySpec.class;
                } else {
                    c = DESKeySpec.class;
                }
                skF[i].getKeySpec(secKeySpec, c);
                fail("getKeySpec(secKey, Class): InvalidKeySpecException must be thrown");
            } catch (InvalidKeySpecException e) {
            }
            rks = skF[i].getKeySpec(secKeySpec, ks.getClass());          
            if (defaultAlgorithm.equals(defaultAlgorithm1)) {
                assertTrue("Incorrect getKeySpec() result 1",
                        rks instanceof DESedeKeySpec);
            } else {
                assertTrue("Incorrect getKeySpec() result 1",
                        rks instanceof DESKeySpec);
            }
            rks = skF[i].getKeySpec(secKey, ks.getClass());
            if (defaultAlgorithm.equals(defaultAlgorithm1)) {
                assertTrue("Incorrect getKeySpec() result 2",
                        rks instanceof DESedeKeySpec);
            } else {
                assertTrue("Incorrect getKeySpec() result 2",
                        rks instanceof DESKeySpec);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() throws NoSuchAlgorithmException {
        for (int i = 0; i < validValues.length; i++) {
            SecretKeyFactory secKF = SecretKeyFactory
                    .getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", secKF.getAlgorithm(),
                    validValues[i]);
        }
        Mock_SecretKeyFactory msf = new Mock_SecretKeyFactory(null, null, null);
        assertNull(msf.getAlgorithm());
    }
    class Mock_SecretKeyFactory extends SecretKeyFactory{
        protected Mock_SecretKeyFactory(SecretKeyFactorySpi arg0, Provider arg1, String arg2) {
            super(arg0, arg1, arg2);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() throws NoSuchAlgorithmException {
        for (int i = 0; i < validValues.length; i++) {
            SecretKeyFactory secKF = SecretKeyFactory
                    .getInstance(validValues[i]);
            assertNotNull(secKF.getProvider());
        }
        Mock_SecretKeyFactory msf = new Mock_SecretKeyFactory(null, null, null);
        assertNull(msf.getProvider());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "translateKey",
            args = {javax.crypto.SecretKey.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SecretKeyFactorySpi.class,
            method = "engineTranslateKey",
            args = {javax.crypto.SecretKey.class}
        )
    })
    public void test_translateKeyLjavax_crypto_SecretKey()
            throws NoSuchAlgorithmException, InvalidKeyException {
        KeyGenerator kg = null;
        Key key = null;
        SecretKeyFactory secKF = null;
        for (int i = 0; i < validValues.length; i++) {
            secKF = SecretKeyFactory
                    .getInstance(validValues[i]);
            assertNotNull(secKF.getProvider());
            kg = KeyGenerator.getInstance(secKF.getAlgorithm());
            kg.init(new SecureRandom());
            key = kg.generateKey();
            secKF.translateKey((SecretKey) key);
        }
        try {
            secKF.translateKey(null);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
        }
    }
}
class mySecretKeyFactory extends SecretKeyFactory {
    public mySecretKeyFactory(SecretKeyFactorySpi spi, Provider prov, String alg) {
        super(spi, prov, alg);
    }
}
