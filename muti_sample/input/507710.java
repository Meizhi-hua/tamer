@TestTargetClass(CertPathBuilder.class)
public class CertPathBuilder1Test extends TestCase {
    public static final String srvCertPathBuilder = "CertPathBuilder";
    public static final String defaultType = "PKIX";    
    public static final String [] validValues = {
            "PKIX", "pkix", "PkiX", "pKiX" };
    private static String [] invalidValues = SpiEngUtils.invalidValues;
    private static boolean PKIXSupport = false;
    private static Provider defaultProvider;
    private static String defaultProviderName;
    private static String NotSupportMsg = "";
    public static final String DEFAULT_TYPE_PROPERTY = "certpathbuilder.type";
    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                srvCertPathBuilder);
        PKIXSupport = (defaultProvider != null);
        defaultProviderName = (PKIXSupport ? defaultProvider.getName() : null);
        NotSupportMsg = defaultType.concat(" is not supported");
    }
    private static CertPathBuilder[] createCPBs() {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return null;
        }
        try {
            CertPathBuilder[] certPBs = new CertPathBuilder[3];
            certPBs[0] = CertPathBuilder.getInstance(defaultType);
            certPBs[1] = CertPathBuilder.getInstance(defaultType,
                    defaultProviderName);
            certPBs[2] = CertPathBuilder.getInstance(defaultType,
                    defaultProvider);
            return certPBs;
        } catch (Exception e) {
            return null;
        }
    }    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultType",
        args = {}
    )
    public void test_getDefaultType() throws Exception {
        assertNull(Security.getProperty(DEFAULT_TYPE_PROPERTY));
        assertEquals("PKIX", CertPathBuilder.getDefaultType());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies NoSuchAlgorithmException.",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testCertPathBuilder02() throws NoSuchAlgorithmException {
        try {
            CertPathBuilder.getInstance(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies positive functionality.",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testCertPathBuilder03() throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            CertPathBuilder cpb = CertPathBuilder.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", cpb.getAlgorithm(), validValues[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathBuilder04()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {        
            try {
                CertPathBuilder.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown thrown");
            } catch (IllegalArgumentException e) {
            }
            try {
                CertPathBuilder.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown thrown");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance throws NoSuchProviderException when provider has invalid value.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathBuilder05()
            throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++ ) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    CertPathBuilder.getInstance(validValues[i], invalidValues[j]);
                    fail("NoSuchProviderException must be hrown");
                } catch (NoSuchProviderException e1) {
                }
            }
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException when algorithm is null; verifies NoSuchAlgorithmException when algorithm  is not correct.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathBuilder06()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertPathBuilder.getInstance(null, defaultProviderName);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i], defaultProviderName);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e1) {
            }
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathBuilder07()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathBuilder certPB;
        for (int i = 0; i < validValues.length; i++) {
            certPB = CertPathBuilder.getInstance(validValues[i], defaultProviderName);
            assertEquals("Incorrect algorithm", certPB.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider name", certPB.getProvider().getName(), defaultProviderName);
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getInstance method throws IllegalArgumentException when provider is null method.",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testCertPathBuilder08()
            throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        Provider prov = null;
        for (int t = 0; t < validValues.length; t++ ) {
            try {
                CertPathBuilder.getInstance(validValues[t], prov);
                fail("IllegalArgumentException must be thrown");
            } catch (IllegalArgumentException e1) {
            }
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getInstance method throws NullPointerException when algorithm is null, throws NoSuchAlgorithmException when algorithm  is not correct.",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testCertPathBuilder09()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertPathBuilder.getInstance(null, defaultProvider);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i], defaultProvider);
                fail("NoSuchAlgorithm must be thrown");
            } catch (NoSuchAlgorithmException e1) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance returns CertPathBuilder object.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathBuilder10()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathBuilder certPB;
        for (int i = 0; i < invalidValues.length; i++) {
            certPB = CertPathBuilder.getInstance(validValues[i], defaultProvider);
            assertEquals("Incorrect algorithm", certPB.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider name", certPB.getProvider(), defaultProvider);
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that build method throws InvalidAlgorithmParameterException if a parameter is null.",
        method = "build",
        args = {java.security.cert.CertPathParameters.class}
    )
    public void testCertPathBuilder11()
            throws NoSuchAlgorithmException, NoSuchProviderException, 
            CertPathBuilderException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }        
        CertPathBuilder [] certPB = createCPBs();
        assertNotNull("CertPathBuilder objects were not created", certPB);
        for (int i = 0; i < certPB.length; i++ ){
            try {
                certPB[i].build(null);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch(InvalidAlgorithmParameterException e) {
            }
        }
    }
    @TestTargetNew(
            level=TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies normal case",
            method="build",
            args={CertPathParameters.class}
    )
    @KnownFailure(value="expired certificate bug 2322662")
    public void testBuild() throws Exception {
        TestUtils.initCertPathSSCertChain();
        CertPathParameters params = TestUtils.getCertPathParameters();
        CertPathBuilder builder = TestUtils.getCertPathBuilder();
        try {
            CertPathBuilderResult result = builder.build(params);
            assertNotNull("builder result is null", result);
            CertPath certPath = result.getCertPath();
            assertNotNull("certpath of builder result is null", certPath);
        } catch (InvalidAlgorithmParameterException e) {
            fail("unexpected Exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertPathBuilder",
        args = {java.security.cert.CertPathBuilderSpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void testCertPathBuilder12()
            throws CertificateException, NoSuchProviderException, 
            NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            CertPathBuilderException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathBuilderSpi spi = new MyCertPathBuilderSpi();
        CertPathBuilder certPB = new myCertPathBuilder(spi, 
                    defaultProvider, defaultType);
        assertEquals("Incorrect algorithm", certPB.getAlgorithm(), defaultType);
        assertEquals("Incorrect provider", certPB.getProvider(), defaultProvider);
        try {
            certPB.build(null);
            fail("CertPathBuilderException must be thrown ");
        } catch (CertPathBuilderException e) {            
        }
        certPB = new myCertPathBuilder(null, null, null);
        assertNull("Incorrect algorithm", certPB.getAlgorithm());
        assertNull("Incorrect provider", certPB.getProvider());            
        try {
            certPB.build(null);
            fail("NullPointerException must be thrown ");
        } catch (NullPointerException e) {            
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void testCertPathBuilder13() throws NoSuchAlgorithmException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            CertPathBuilder cpb = CertPathBuilder.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", cpb.getAlgorithm(),
                    validValues[i]);
            try {
                cpb = CertPathBuilder.getInstance(validValues[i],
                        defaultProviderName);
                assertEquals("Incorrect algorithm", cpb.getAlgorithm(),
                        validValues[i]);
            } catch (NoSuchProviderException e) {
                fail("Unexpected NoSuchProviderException exeption "
                        + e.getMessage());
            }
            try {
                cpb = CertPathBuilder.getInstance(validValues[i],
                        defaultProviderName);
                assertEquals("Incorrect algorithm", cpb.getAlgorithm(),
                        validValues[i]);
            } catch (NoSuchProviderException e) {
                fail("Unexpected NoSuchProviderException " + e.getMessage());
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void testCertPathBuilder14() throws NoSuchAlgorithmException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            CertPathBuilder cpb2 = CertPathBuilder.getInstance(validValues[i],
                    defaultProvider);
            assertEquals("Incorrect provider", cpb2.getProvider(),
                    defaultProvider);
            try {
                CertPathBuilder cpb3 = CertPathBuilder.getInstance(
                        validValues[i], defaultProviderName);
                assertEquals("Incorrect provider", cpb3.getProvider(),
                        defaultProvider);
            } catch (NoSuchProviderException e) {
                fail("Unexpected NoSuchProviderException " + e.getMessage());
            }
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(CertPathBuilder1Test.class);
    }  
}
class myCertPathBuilder extends CertPathBuilder {
    private static Provider provider;
    public myCertPathBuilder(CertPathBuilderSpi spi, Provider prov, String type) {
        super(spi, prov, type);
    }
    public static CertPathBuilder getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        myCertPathBuilder mcpb = new myCertPathBuilder(null, null, null);
        provider = mcpb.new MyProvider();
        return CertPathBuilder.getInstance(algorithm);
    }
    public Provider getMyProvider() {
        return provider;
    }
    public class MyProvider extends Provider {
        private static final long serialVersionUID = -6537447905658191184L;
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
        }
        MyProvider(String name, double version, String info) {
            super(name, version, info);
        }
        public void putService(Provider.Service s) {
            super.putService(s);
        }
        public void removeService(Provider.Service s) {
            super.removeService(s);
        }
    }
}
