@TestTargetClass(Package.class) 
public class PackageTest extends junit.framework.TestCase {
    private File resources;
    private String resPath;
    Class clazz;
    Package getTestPackage(String resourceJar, String className)
            throws Exception {
        if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
            resourceJar = resourceJar.substring(0, resourceJar.indexOf(".")) + 
                                                                    "_dex.jar";
        }
        Support_Resources.copyFile(resources, "Package", resourceJar);
        URL resourceURL = new URL("file:/" + resPath + "/Package/"
                + resourceJar);
        ClassLoader cl = Support_ClassLoader.getInstance(resourceURL,
                getClass().getClassLoader());
       clazz = cl.loadClass(className);
       return clazz.getPackage();
    }
    @Override
    protected void setUp() {
        resources = Support_Resources.createTempFolder();
        resPath = resources.toString();
        if (resPath.charAt(0) == '/' || resPath.charAt(0) == '\\')
            resPath = resPath.substring(1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getImplementationTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getImplementationVendor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getImplementationVersion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSpecificationTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSpecificationVendor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSpecificationVersion",
            args = {}
        )
    })
    @KnownFailure("get methods don't work.")
    public void test_helper_Attributes() throws Exception {
        Package p = getTestPackage("hyts_all_attributes.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (1)",
                "p Implementation-Title", p.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (1)",
                "p Implementation-Vendor", p.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (1)",
                "2.2.2", p.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (1)",
                "p Specification-Title", p.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (1)",
                "p Specification-Vendor", p.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (1)",
                "2.2.2", p.getSpecificationVersion());
        Package p2 = getTestPackage("hyts_no_entry.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (2)",
                "MF Implementation-Title", p2.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (2)",
                "MF Implementation-Vendor", p2.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (2)",
                "5.3.b1", p2.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (2)",
                "MF Specification-Title", p2.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (2)",
                "MF Specification-Vendor", p2.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (2)",
                "1.2.3", p2.getSpecificationVersion());
        Package p3 = getTestPackage("hyts_no_attributes.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (3)",
                "MF Implementation-Title", p3.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (3)",
                "MF Implementation-Vendor", p3.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (3)",
                "5.3.b1", p3.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (3)",
                "MF Specification-Title", p3.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (3)",
                "MF Specification-Vendor", p3.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (3)",
                "1.2.3", p3.getSpecificationVersion());
        Package p4 = getTestPackage("hyts_some_attributes.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (4)",
                "p Implementation-Title", p4.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (4)",
                "MF Implementation-Vendor", p4.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (4)",
                "2.2.2", p4.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (4)",
                "MF Specification-Title", p4.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (4)",
                "p Specification-Vendor", p4.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (4)",
                "2.2.2", p4.getSpecificationVersion());
        Package p5 = getTestPackage("hyts_pq.jar", "p.q.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (5)",
                "p Implementation-Title", p5.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (5)",
                "p Implementation-Vendor", p5.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (5)",
                "1.1.3", p5.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (5)",
                "p Specification-Title", p5.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (5)",
                "p Specification-Vendor", p5.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (5)",
                "2.2.0.0.0.0.0.0.0.0.0", p5.getSpecificationVersion());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    @BrokenTest("Different behavior between cts host and run-core-test")
    public void test_getName() throws Exception {
        Package p = getTestPackage("hyts_pq.jar", "p.q.C");
        assertEquals("Package getName returns a wrong string", "p.q", p
                .getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPackage",
        args = {java.lang.String.class}
    )
    @KnownFailure("Real package information missing on android.")
    public void test_getPackageLjava_lang_String() throws Exception {
        assertSame("Package getPackage failed for java.lang", Package
                .getPackage("java.lang"), Package.getPackage("java.lang"));
        assertSame("Package getPackage failed for java.lang", Package
                .getPackage("java.lang"), Object.class.getPackage());
        Package p = getTestPackage("hyts_package.jar", "C");
        assertNull("getPackage should return null.", p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPackages",
        args = {}
    )
    @KnownFailure("Package information missing on android")
    public void test_getPackages() throws Exception {
        Package[] pckgs = Package.getPackages();
        boolean found = false;
        for (int i = 0; i < pckgs.length; i++) {
            if (pckgs[i].getName().equals("java.util")) {
                found = true;
                break;
            }
        }
        assertTrue("Package getPackages failed to retrieve a package", found);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        Package p1 = Package.getPackage("java.lang");
        if (p1 != null) {
            assertEquals(p1.hashCode(), "java.lang".hashCode());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isCompatibleWith",
        args = {java.lang.String.class}
    )
    @KnownFailure("Dalvik packages are always version '0.0'.")
    public void test_isCompatibleWithLjava_lang_String() throws Exception {
        Package p = getTestPackage("hyts_c.jar", "p.C");
        assertTrue("Package isCompatibleWith fails with lower version", p
                .isCompatibleWith("2.1.9.9"));
        assertTrue("Package isCompatibleWith fails with same version (1)", p
                .isCompatibleWith("2.2.0"));
        assertTrue("Package isCompatibleWith fails with same version (2)", p
                .isCompatibleWith("2.2"));
        assertFalse("Package isCompatibleWith fails with higher version", p
                .isCompatibleWith("2.2.0.0.1"));
        try {
            p.isCompatibleWith(null);
            fail("Null version is illegal");
        } catch (NumberFormatException ok) {
        } catch (NullPointerException compatible) {
        }
        try {
            p.isCompatibleWith("");
            fail("Empty version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith(".");
            fail("'.' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith("1.2.");
            fail("'1.2.' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith(".9");
            fail("'.9' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith("2.4..5");
            fail("'2.4..5' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith("20.-4");
            fail("'20.-4' version is illegal");
        } catch (NumberFormatException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isSealed",
        args = {}
    )
    @KnownFailure("isSealed method returns false for sealed package.")    
    public void test_isSealed() throws Exception {
        Package p = getTestPackage("hyts_pq.jar", "p.q.C");
        assertTrue("Package isSealed returns wrong boolean", p.isSealed());
        p = String.class.getPackage();
        assertFalse("Package isSealed returns wrong boolean", p.isSealed());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isSealed",
        args = {java.net.URL.class}
    )
    @KnownFailure("isSealed method returns false for sealed package.")
    public void test_isSealedLjava_net_URL() throws Exception {
        Package p = getTestPackage("hyts_c.jar", "p.C");
        assertFalse("Package isSealed returns wrong boolean (1)", p
                .isSealed(new URL("file:/" + resPath + "/")));
        assertTrue("Package isSealed returns wrong boolean (2)", p
                .isSealed(new URL("file:/" + resPath + "/Package/hyts_c.jar")));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    @BrokenTest("Different behavior between cts host and run-core-test")
    public void test_toString() throws Exception {
        Package p = getTestPackage("hyts_c.jar", "p.C");
        assertTrue("Package toString returns wrong string", p.toString()
                .length() > 0);
    }
    @SuppressWarnings("unchecked")
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAnnotation",
        args = {java.lang.Class.class}
    )
    @KnownFailure("Class loader can't retrieve information about annotations.")
    public void test_getAnnotation() throws Exception {
        String annotationName = "a.b.PackageAnnotation";
        Package p = getTestPackage("hyts_package.jar", annotationName);
        assertEquals(annotationName, 
                p.getAnnotation(clazz).annotationType().getName());
        assertNull(String.class.getPackage().getAnnotation(Deprecated.class));
        assertNull(ExtendTestClass.class.getPackage().
                getAnnotation(Deprecated.class));        
    }
    @SuppressWarnings("unchecked")
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAnnotations",
        args = {}
    )
    @KnownFailure("Class loader can't retrieve information about annotations.")    
    public void test_getAnnotations() throws Exception {
        String annotationName = "a.b.PackageAnnotation";
        Package p = getTestPackage("hyts_package.jar", annotationName);
        Annotation [] annotations = p.getAnnotations();
        assertEquals(1, annotations.length);
        p = String.class.getPackage();
        assertEquals(0, p.getAnnotations().length);
    }   
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDeclaredAnnotations",
        args = {}
    )
    @KnownFailure("Class loader can't retrieve information about annotations.")
    public void test_getDeclaredAnnotations() throws Exception {
        String annotationName = "a.b.PackageAnnotation";
        Package p = getTestPackage("hyts_package.jar", annotationName);
        Annotation [] annotations = p.getDeclaredAnnotations();
        assertEquals(1, annotations.length);
        p = String.class.getPackage();
        assertEquals(0, p.getDeclaredAnnotations().length);
    } 
    @SuppressWarnings("unchecked")
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAnnotationPresent",
        args = {java.lang.Class.class}
    )
    @KnownFailure("Class loader can't retrieve information about annotations.")
    public void test_isAnnotationPresent() throws Exception {
        String annotationName = "a.b.PackageAnnotation";
        Package p = getTestPackage("hyts_package.jar", annotationName);
        assertTrue(p.isAnnotationPresent(clazz));
        assertFalse(p.isAnnotationPresent(Deprecated.class));
        p = String.class.getPackage();
        assertFalse(p.isAnnotationPresent(clazz));
        assertFalse(p.isAnnotationPresent(Deprecated.class));        
    }          
}
