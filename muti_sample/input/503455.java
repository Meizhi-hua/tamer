@TestTargetClass(KeyPairGenerator.class)
public class KeyPairGenerator1Test extends TestCase {
    private static String[] invalidValues = SpiEngUtils.invalidValues;
    public static final String srvKeyPairGenerator = "KeyPairGenerator";
    public static String[] algs = {
            "DSA", "dsa", "Dsa", "DsA", "dsA" };
    public static String validAlgName = "DSA";
    private static String validProviderName = null;
    public static Provider validProvider = null;
    private static boolean DSASupported = false;
    public static String NotSupportMsg = "";
    static {
        validProvider = SpiEngUtils.isSupport(
                validAlgName,
                srvKeyPairGenerator);
        DSASupported = (validProvider != null);
        if (!DSASupported) {
            NotSupportMsg = validAlgName + " algorithm is not supported" ;
        }        
        validProviderName = (DSASupported ? validProvider.getName() : null);
    }
    protected KeyPairGenerator [] createKPGen() {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return null;
        }
        KeyPairGenerator[] kpg = new KeyPairGenerator[3];
        try {
            kpg[0] = KeyPairGenerator.getInstance(validAlgName);
            kpg[1] = KeyPairGenerator.getInstance(validAlgName, validProvider);
            kpg[2] = KeyPairGenerator.getInstance(validAlgName, validProviderName);
            return kpg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testKeyPairGenerator01() throws NoSuchAlgorithmException {
        try {
            KeyPairGenerator.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown  when algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown when algorithm is not available: "
                        .concat(invalidValues[i]));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testKeyPairGenerator02() throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyPairGenerator kpg;
        for (int i = 0; i < algs.length; i++) {
            kpg = KeyPairGenerator.getInstance(algs[i]);
            assertEquals("Incorrect algorithm ", kpg.getAlgorithm().toUpperCase(),
                    algs[i].toUpperCase());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testKeyPairGenerator03() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < algs.length; i++) {
            try {
                KeyPairGenerator.getInstance(algs[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
            try {
                KeyPairGenerator.getInstance(algs[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testKeyPairGenerator04() throws NoSuchAlgorithmException,
            IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < algs.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    KeyPairGenerator.getInstance(algs[i], invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (algorithm: "
                            .concat(algs[i]).concat(" provider: ").concat(
                                    invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testKeyPairGenerator05() throws NoSuchProviderException,
            IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        try {
            KeyPairGenerator.getInstance(null, validProviderName);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown  when algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i],
                        validProviderName);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(algs[i]).concat(" provider: ").concat(
                                validProviderName).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testKeyPairGenerator06() throws NoSuchProviderException,
            NoSuchAlgorithmException, IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyPairGenerator kpg;
        for (int i = 0; i < algs.length; i++) {
            kpg = KeyPairGenerator.getInstance(algs[i], validProviderName);
            assertEquals("Incorrect algorithm", kpg.getAlgorithm().toUpperCase(),
                    algs[i].toUpperCase());
            assertEquals("Incorrect provider", kpg.getProvider().getName(),
                    validProviderName);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testKeyPairGenerator07() throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        Provider provider = null;
        for (int i = 0; i < algs.length; i++) {
            try {
                KeyPairGenerator.getInstance(algs[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testKeyPairGenerator08() throws IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        try {
            KeyPairGenerator.getInstance(null, validProvider);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown  when algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i], validProvider);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(algs[i]).concat(" provider: ").concat(
                                validProviderName).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testKeyPairGenerator09() throws NoSuchAlgorithmException,
            IllegalArgumentException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyPairGenerator kpg;
        for (int i = 0; i < algs.length; i++) {
            kpg = KeyPairGenerator.getInstance(algs[i], validProvider);
            assertEquals("Incorrect algorithm", kpg.getAlgorithm().toUpperCase(),
                    algs[i].toUpperCase());
            assertEquals("Incorrect provider", kpg.getProvider(), validProvider);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "generateKeyPair",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "genKeyPair",
            args = {}
        )
    })
    public void testKeyPairGenerator10() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException {
            if (!DSASupported) {
                fail(NotSupportMsg);
                return;
            }
            KeyPairGenerator[] kpg = createKPGen();
            assertNotNull("KeyPairGenerator objects were not created", kpg);
            KeyPair kp, kp1;
            for (int i = 0; i < kpg.length; i++) {
                kpg[i].initialize(512);
                kp = kpg[i].generateKeyPair();
                kp1 = kpg[i].genKeyPair();
                assertFalse("Incorrect private key", kp.getPrivate().equals(
                        kp1.getPrivate()));
                assertFalse("Incorrect public key", kp.getPublic().equals(
                        kp1.getPublic()));
            }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {java.security.spec.AlgorithmParameterSpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {int.class, java.security.SecureRandom.class}
        )
    })
    public void testKeyPairGenerator11() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!DSASupported) {
            fail(NotSupportMsg);
            return;
        }
        int[] keys =  { -10000, -1024, -1, 0, 10000 };
        KeyPairGenerator[] kpg = createKPGen();
        assertNotNull("KeyPairGenerator objects were not created", kpg);
        SecureRandom random = new SecureRandom();
        AlgorithmParameterSpec aps = null;
        for (int i = 0; i < kpg.length; i++) {
            for (int j = 0; j < keys.length; j++) {                
                try {
                    kpg[i].initialize(keys[j]);
                    kpg[i].initialize(keys[j], random);
                } catch (InvalidParameterException e) {
                }
            }            
            try {
                kpg[i].initialize(aps);
                kpg[i].initialize(aps, random);
            } catch (InvalidAlgorithmParameterException e) {
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {java.security.spec.AlgorithmParameterSpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "initialize",
            args = {int.class, java.security.SecureRandom.class}
        )
    })
    public void testKeyPairGenerator12() {
        int[] keys = { -1, -250, 1, 64, 512, 1024 };
        SecureRandom random = new SecureRandom();
        AlgorithmParameterSpec aps;
        KeyPairGenerator mKPG = new MyKeyPairGenerator1("");
        assertEquals("Incorrect algorithm", mKPG.getAlgorithm(),
                MyKeyPairGenerator1.getResAlgorithm());
        mKPG.generateKeyPair();
        mKPG.genKeyPair();
        for (int i = 0; i < keys.length; i++) {
            try {
                mKPG.initialize(keys[i]);
                fail("InvalidParameterException must be thrown (key: "
                        + Integer.toString(keys[i]) + ")");
            } catch (InvalidParameterException e) {
            }
            try {
                mKPG.initialize(keys[i], random);
                fail("InvalidParameterException must be thrown (key: "
                        + Integer.toString(keys[i]) + ")");
            } catch (InvalidParameterException e) {
            }
        }
        try {
            mKPG.initialize(100, null);
            fail("InvalidParameterException must be thrown when random is null");
        } catch (InvalidParameterException e) {
        }
        mKPG.initialize(100, random);
        assertEquals("Incorrect random", random,
                ((MyKeyPairGenerator1) mKPG).secureRandom);
        assertEquals("Incorrect keysize", 100,
                ((MyKeyPairGenerator1) mKPG).keySize);
        try {
            mKPG.initialize(null, random);
            fail("InvalidAlgorithmParameterException must be thrown when param is null");
        } catch (InvalidAlgorithmParameterException e) {
        }
        if (DSASupported) {
            BigInteger bInt = new BigInteger("1");
            aps = new java.security.spec.DSAParameterSpec(bInt, bInt, bInt);
            try {
                mKPG.initialize(aps, null);
                fail("InvalidParameterException must be thrown when random is null");
            } catch (InvalidParameterException e) {
            } catch (InvalidAlgorithmParameterException e) {
                fail("Unexpected InvalidAlgorithmParameterException was thrown");
            }
            try {
                mKPG.initialize(aps, random);
                assertEquals("Incorrect random", random,
                        ((MyKeyPairGenerator1) mKPG).secureRandom);
                assertEquals("Incorrect params", aps,
                        ((MyKeyPairGenerator1) mKPG).paramSpec);
            } catch (InvalidAlgorithmParameterException e) {
                fail("Unexpected InvalidAlgorithmParameterException was thrown");
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "",
            method = "initialize",
            args = {java.security.spec.AlgorithmParameterSpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "",
            method = "initialize",
            args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "",
            method = "initialize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "",
            method = "initialize",
            args = {int.class, java.security.SecureRandom.class}
        )
    })
    public void testKeyPairGenerator13() {
        int[] keys = { -1, -250, 1, 63, -512, -1024 };
        SecureRandom random = new SecureRandom();
        KeyPairGenerator mKPG = new MyKeyPairGenerator2(null);
        assertEquals("Algorithm must be null", mKPG.getAlgorithm(),
                MyKeyPairGenerator2.getResAlgorithm());
        assertNull("genKeyPair() must return null", mKPG.genKeyPair());
        assertNull("generateKeyPair() mut return null", mKPG.generateKeyPair());
        for (int i = 0; i < keys.length; i++) {
            try {
                mKPG.initialize(keys[i]);
                fail("InvalidParameterException must be thrown (key: "
                        + Integer.toString(keys[i]) + ")");
            } catch (InvalidParameterException e) {
            }
            try {
                mKPG.initialize(keys[i], random);
                fail("InvalidParameterException must be thrown (key: "
                        + Integer.toString(keys[i]) + ")");
            } catch (InvalidParameterException e) {
            }
        }
        try {
            mKPG.initialize(64);
        } catch (InvalidParameterException e) {
            fail("Unexpected InvalidParameterException was thrown");
        }
        try {
            mKPG.initialize(64, null);
        } catch (InvalidParameterException e) {
            fail("Unexpected InvalidParameterException was thrown");
        }
        try {
            mKPG.initialize(null, random);
        } catch (UnsupportedOperationException e) {
        } catch (InvalidAlgorithmParameterException e) {
            fail("Unexpected InvalidAlgorithmParameterException was thrown");
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(KeyPairGenerator1Test.class);
    }
}
