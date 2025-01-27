@TestTargetClass(ExemptionMechanism.class)
public class ExemptionMechanismTest extends TestCase {
    private static final String srvExemptionMechanism = "ExemptionMechanism";
    private static final String defaultAlg = "EMech";
    private static final String ExemptionMechanismProviderClass = "org.apache.harmony.crypto.tests.support.MyExemptionMechanismSpi";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ExemptionMechanism",
        args = {javax.crypto.ExemptionMechanismSpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void testExemptionMechanism() throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanismSpi spi = new MyExemptionMechanismSpi();
        ExemptionMechanism em = new ExemptionMechanism(spi, mProv, defaultAlg) {};
        assertEquals("Incorrect provider", em.getProvider(), mProv);
        assertEquals("Incorrect algorithm", em.getName(), defaultAlg);
        try {
            em.init(null);
            fail("InvalidKeyException must be thrown");
        } catch (InvalidKeyException e) {}
        try {
            em.getOutputSize(100);
            fail("IllegalStateException must be thrown");
        } catch (IllegalStateException e) {}
        em = new ExemptionMechanism(null, null, null) {};
        assertNull("Incorrect mechanism", em.getName());
        assertNull("Incorrect provider", em.getProvider());
        try {
            em.init(null);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {}
        try {
            em.getOutputSize(100);
            fail("IllegalStateException must be thrown");
        } catch (IllegalStateException e) {}
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetInstance() throws Exception {
        try {
            ExemptionMechanism.getInstance((String) null, "aaa");
            fail("NoSuchProviderException must be thrown");
        } catch (NoSuchProviderException pe) {
        }
        try {
            ExemptionMechanism.getInstance("AlgName", (String)null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isCryptoAllowed",
        args = {java.security.Key.class}
    )
    public void testIsCryptoAllowed() throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        assertFalse(em.isCryptoAllowed(key));
        em.init(key);
        assertFalse(em.isCryptoAllowed(key));
        em.genExemptionBlob();
        assertTrue(em.isCryptoAllowed(key));
        Key key1 = new MyExemptionMechanismSpi().new tmpKey("Proba",
                new byte[] { 1 });
        assertFalse(em.isCryptoAllowed(key1));
        em.init(key1);
        assertFalse(em.isCryptoAllowed(key));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test",
        method = "genExemptionBlob",
        args = {byte[].class, int.class}
    )
    public void testGenExemptionBlob() throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        em.init(key);
        em.genExemptionBlob(null, 0);
        em.genExemptionBlob(new byte[0], 0);
        em.genExemptionBlob(new byte[10], -5);
    }
    static boolean flag = false;
    class Mock_ExemptionMechanism extends  ExemptionMechanism  {
        protected Mock_ExemptionMechanism(ExemptionMechanismSpi exmechSpi, Provider provider, String mechanism) {
            super(exmechSpi, provider, mechanism);
        }
        @Override
        protected void finalize() {
            flag = true;
            super.finalize();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "finalize",
        args = {}
    )
    @SideEffect("Causes OutOfMemoryError to test finalization")
    public void test_finalize () {
        Mock_ExemptionMechanism mem = new Mock_ExemptionMechanism(null, null, "Name");
        assertNotNull(mem);
        mem = null;
        assertFalse(flag);
        Vector v = new Vector();
        int capacity;
        try {
            while(true) {
                v.add(this);
            }
        } catch (OutOfMemoryError e) {
            capacity = v.size();
            v = null;
        }
        v = new Vector();
        for (int i = 0; i < capacity/2; i++) {
            v.add(this);
        }
        v = null;
        assertTrue(flag);
    }
    class Mock_ExemptionMechanismSpi extends MyExemptionMechanismSpi {
        @Override
        protected byte[] engineGenExemptionBlob()
        throws ExemptionMechanismException {
            throw new ExemptionMechanismException();
        }
        @Override
        protected int engineGenExemptionBlob(byte[] output, int outputOffset)
        throws ShortBufferException, ExemptionMechanismException {
            if (output.length - outputOffset < 
                    super.engineGenExemptionBlob(output, outputOffset)) {
                throw new ShortBufferException();
            }
            if (output[outputOffset + 3] == 33) {
                throw new ExemptionMechanismException();
            }
            return super.engineGenExemptionBlob(output, outputOffset);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "genExemptionBlob",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = ExemptionMechanismSpi.class,
            method = "engineGenExemptionBlob",
            args = {}
        )
    })
    public void test_genExemptionBlob() throws InvalidKeyException, 
    ExemptionMechanismException {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        try {
            em.genExemptionBlob();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        em.init(key);
        assertNotNull(em.genExemptionBlob());
        em = new ExemptionMechanism(
                new Mock_ExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        key = new Mock_ExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        em.init(key);
        try {
            em.genExemptionBlob();
            fail("ExemptionMechanismException expected");
        } catch (ExemptionMechanismException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "genExemptionBlob",
        args = {byte[].class}
    )
    public void test_genExemptionBlob$B() throws InvalidKeyException, 
    ExemptionMechanismException, ShortBufferException {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new Mock_ExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new Mock_ExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        try {
            em.genExemptionBlob(new byte[10]);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        em.init(key);
        assertEquals(5, (em.genExemptionBlob(new byte[10])));
        try {
            em.genExemptionBlob(new byte[2]);
            fail("ShortBufferException expected");
        } catch (ShortBufferException e) {
        }
        byte[] b = new byte[] {0,0,0,33,0};
        try {
            em.genExemptionBlob(b);
            fail("ExemptionMechanismException expected");
        } catch (ExemptionMechanismException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "genExemptionBlob",
            args = {byte[].class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = ExemptionMechanismSpi.class,
            method = "engineGenExemptionBlob",
            args = {byte[].class, int.class}
        )
    })
    public void test_genExemptionBlob$BI() throws InvalidKeyException, 
    ExemptionMechanismException, ShortBufferException {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new Mock_ExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new Mock_ExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        try {
            em.genExemptionBlob(new byte[10], 2);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        em.init(key);
        assertEquals(5, (em.genExemptionBlob(new byte[10], 5)));
        try {
            em.genExemptionBlob(new byte[7], 3);
            fail("ShortBufferException expected");
        } catch (ShortBufferException e) {
        }
        byte[] b = new byte[] {0, 0, 0, 1, 2, 3, 33, 0};
        try {
            em.genExemptionBlob(b, 3);
            fail("ExemptionMechanismException expected");
        } catch (ExemptionMechanismException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Tests Exceptions",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() throws Exception {
        try {
            ExemptionMechanism.getInstance((String) null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new Mock_ExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        try {
            em.getInstance("WrongAlgName");
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Tests Exceptions",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider()
    throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        try {
            ExemptionMechanism.getInstance((String) null, mProv);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        ExemptionMechanism em = new ExemptionMechanism(
                new Mock_ExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        try {
            em.getInstance("WrongAlgName", mProv);
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            em.getInstance("WrongAlgName", (Provider)null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        assertEquals(defaultAlg, em.getName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getOutputSize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = ExemptionMechanismSpi.class,
            method = "engineGetOutputSize",
            args = {int.class}
        )
    })
    public void test_getOutputSizeI() throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        try {
            em.getOutputSize(10);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        em.init(key);
        assertEquals(10, em.getOutputSize(10));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        assertEquals(mProv, em.getProvider());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "init",
            args = {java.security.Key.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = ExemptionMechanismSpi.class,
            method = "engineInit",
            args = {java.security.Key.class}
        )
    })
    public void test_initLjava_security_Key() throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        em.init(key);
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        kg.init(56, new SecureRandom());
        key = kg.generateKey();
        try {
            em.init(null);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
        }
        try {
            em.init(key);
            fail("ExemptionMechanismException expected");
        } catch (ExemptionMechanismException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "init",
            args = {java.security.Key.class, java.security.AlgorithmParameters.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = ExemptionMechanismSpi.class,
            method = "engineInit",
            args = {java.security.Key.class, java.security.AlgorithmParameters.class}
        )
    })
    public void test_initLjava_security_KeyLjava_security_AlgorithmParameters()
            throws Exception {
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        em.init(key, AlgorithmParameters.getInstance("DES"));
        try {
            em.init(key, (AlgorithmParameters)null);
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
        }
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        kg.init(56, new SecureRandom());
        key = kg.generateKey();
        try {
            em.init(null, AlgorithmParameters.getInstance("DES"));
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
        }
        try {
            em.init(key, AlgorithmParameters.getInstance("DES"));
            fail("ExemptionMechanismException expected");
        } catch (ExemptionMechanismException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "init",
            args = {java.security.Key.class, java.security.spec.AlgorithmParameterSpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = ExemptionMechanismSpi.class,
            method = "engineInit",
            args = {java.security.Key.class, java.security.spec.AlgorithmParameterSpec.class}
        )
    })
    public void test_initLjava_security_KeyLjava_security_spec_AlgorithmParameterSpec()
            throws Exception{
        Provider mProv = (new SpiEngUtils()).new MyProvider("MyExMechProvider",
                "Provider for ExemptionMechanism testing",
                srvExemptionMechanism.concat(".").concat(defaultAlg),
                ExemptionMechanismProviderClass);
        ExemptionMechanism em = new ExemptionMechanism(
                new MyExemptionMechanismSpi(), mProv, defaultAlg) {
        };
        Key key = new MyExemptionMechanismSpi().new tmpKey("Proba", new byte[0]);
        em.init(key, new RSAKeyGenParameterSpec(10, new BigInteger("10")));
        try {
            em.init(key, (AlgorithmParameterSpec)null);
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
        }
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        kg.init(56, new SecureRandom());
        key = kg.generateKey();
        try {
            em.init(null, new RSAKeyGenParameterSpec(10, new BigInteger("10")));
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
        }
        try {
            em.init(key, new RSAKeyGenParameterSpec(10, new BigInteger("10")));
            fail("ExemptionMechanismException expected");
        } catch (ExemptionMechanismException e) {
        }
    }
}
