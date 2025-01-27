@TestTargetClass(JarInputStream.class)
public class JarInputStreamTest extends junit.framework.TestCase {
    private String jarName;
    private String jarName2;
    private final String entryName = "foo/bar/A.class";
    private static final int indexofDSA = 2;
    private static final int indexofTESTCLASS = 4;
    private static final int totalEntries = 4;
    @Override
    protected void setUp() {
        jarName = Support_Resources.getURL("morestuff/hyts_patch.jar");
        jarName2 = Support_Resources.getURL("morestuff/hyts_patch2.jar");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "JarInputStream",
        args = {java.io.InputStream.class}
    )
    public void test_ConstructorLjava_io_InputStream() {
        InputStream is = null;
        JarInputStream jis = null;
        try {
            is = new URL(jarName).openConnection().getInputStream();
            boolean hasCorrectEntry = false;
            jis = new JarInputStream(is);
            assertNotNull("The jar input stream should have a manifest", jis
                    .getManifest());
            JarEntry je = jis.getNextJarEntry();
            while (je != null) {
                if (je.getName().equals(entryName)) {
                    hasCorrectEntry = true;
                }
                je = jis.getNextJarEntry();
            }
            assertTrue(
                    "The jar input stream does not contain the correct entries",
                    hasCorrectEntry);
        } catch (Exception e) {
            fail("Exception during test: " + e.toString());
        }
        try {
            is.close();
            jis = new JarInputStream(is);
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getManifest",
        args = {}
    )
    public void test_getManifest() {
        try {
            Manifest m;
            InputStream is = new URL(jarName2).openConnection()
                    .getInputStream();
            JarInputStream jis = new JarInputStream(is);
            m = jis.getManifest();
            assertNull("The jar input stream should not have a manifest", m);
            is = new URL(jarName).openConnection().getInputStream();
            jis = new JarInputStream(is);
            m = jis.getManifest();
            assertNotNull("The jar input stream should have a manifest", m);
        } catch (Exception e) {
            fail("Exception during test: " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "getNextJarEntry",
        args = {}
    )
    public void test_getNextJarEntry() throws Exception {
        final Set<String> desired = new HashSet<String>(Arrays
                .asList(new String[] {
                        "foo/", "foo/bar/", "foo/bar/A.class", "Blah.txt"}));
        Set<String> actual = new HashSet<String>();
        InputStream is = new URL(jarName).openConnection().getInputStream();
        JarInputStream jis = new JarInputStream(is);
        JarEntry je = jis.getNextJarEntry();
        while (je != null) {
            actual.add(je.toString());
            je = jis.getNextJarEntry();
        }
        assertEquals(actual, desired);
        jis.close();
        try {
            jis.getNextJarEntry();
            fail("IOException expected");
        } catch (IOException ee) {
        }
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        is = Support_Resources.getStream("Broken_entry.jar");
        jis = new JarInputStream(is, false);
        jis.getNextJarEntry();
        try {
            jis.getNextJarEntry();
            fail("ZipException expected");
        } catch (ZipException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "getNextJarEntry",
        args = {}
    )
    public void test_getNextJarEntry_Ex() throws Exception {
        final Set<String> desired = new HashSet<String>(Arrays
                .asList("foo/", "foo/bar/", "foo/bar/A.class", "Blah.txt"));
        Set<String> actual = new HashSet<String>();
        InputStream is = new URL(jarName).openConnection().getInputStream();
        JarInputStream jis = new JarInputStream(is);
        JarEntry je = jis.getNextJarEntry();
        while (je != null) {
            actual.add(je.toString());
            je = jis.getNextJarEntry();
        }
        assertEquals(actual, desired);
        jis.close();
        try {
            jis.getNextJarEntry();
            fail("IOException expected");
        } catch (IOException ee) {
        }
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        is = Support_Resources.getStream("Broken_entry.jar");
        jis = new JarInputStream(is, false);
        jis.getNextJarEntry();
        try {
            jis.getNextJarEntry();
            fail("ZipException expected");
        } catch (ZipException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Exceptions checking missed. Case2",
        method = "getNextJarEntry",
        args = {}
    )
    public void test_JarInputStream_Integrate_Jar_getNextEntry()
            throws IOException {
        String intJarName = Support_Resources.getURL("Integrate.jar");
        InputStream is = new URL(intJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        ZipEntry entry = null;
        int count = 0;
        while (count == 0 || entry != null) {
            count++;
            entry = jin.getNextEntry();
        }
        assertEquals(totalEntries + 1, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException & ZipException checking missed.",
        method = "getNextEntry",
        args = {}
    )
    public void test_JarInputStream_Modified_Class_getNextEntry()
            throws IOException {
        String modJarName = Support_Resources.getURL("Modified_Class.jar");
        InputStream is = new URL(modJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        ZipEntry zipEntry = null;
        int count = 0;
        while (count == 0 || zipEntry != null) {
            count++;
            try {
                zipEntry = jin.getNextEntry();
                if (count == indexofTESTCLASS + 1) {
                    fail("Should throw Security Exception");
                }
            } catch (SecurityException e) {
                if (count != indexofTESTCLASS + 1) {
                    throw e;
                }
            }
        }
        assertEquals(totalEntries + 2, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException & ZipException checking missed.",
        method = "getNextEntry",
        args = {}
    )
    public void test_JarInputStream_Modified_Manifest_MainAttributes_getNextEntry()
            throws IOException {
        String modJarName = Support_Resources.getURL("Modified_Manifest_MainAttributes.jar");
        InputStream is = new URL(modJarName).openConnection()
                .getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        assertEquals("META-INF/TESTROOT.SF", jin.getNextEntry().getName());
        assertEquals("META-INF/TESTROOT.DSA", jin.getNextEntry().getName());
        try {
            jin.getNextEntry();
            fail();
        } catch (SecurityException expected) {
        }
        assertEquals("META-INF/", jin.getNextEntry().getName());
        assertEquals("Test.class", jin.getNextEntry().getName());
        assertNull(jin.getNextEntry());
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException & ZipException checking missed.",
        method = "getNextEntry",
        args = {}
    )
    public void test_JarInputStream_Modified_Manifest_EntryAttributes_getNextEntry()
            throws IOException {
        String modJarName = Support_Resources
                .getURL("Modified_Manifest_EntryAttributes.jar");
        InputStream is = new URL(modJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        ZipEntry zipEntry = null;
        int count = 0;
        while (count == 0 || zipEntry != null) {
            count++;
            try {
                zipEntry = jin.getNextEntry();
                if (count == indexofDSA + 1) {
                    fail("Should throw Security Exception");
                }
            } catch (SecurityException e) {
                if (count != indexofDSA + 1) {
                    throw e;
                }
            }
        }
        assertEquals(totalEntries + 2, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException & ZipException checking missed.",
        method = "getNextEntry",
        args = {}
    )
    public void test_JarInputStream_Modified_SF_EntryAttributes_getNextEntry()
            throws IOException {
        String modJarName = Support_Resources
                .getURL("Modified_SF_EntryAttributes.jar");
        InputStream is = new URL(modJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        ZipEntry zipEntry = null;
        int count = 0;
        while (count == 0 || zipEntry != null) {
            count++;
            try {
                zipEntry = jin.getNextEntry();
                if (count == indexofDSA + 1) {
                    fail("Should throw Security Exception");
                }
            } catch (SecurityException e) {
                if (count != indexofDSA + 1) {
                    throw e;
                }
            }
        }
        assertEquals(totalEntries + 2, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException & ZipException checking missed.",
        method = "read",
        args = {byte[].class}
    )
    public void test_JarInputStream_Modified_Class_read() throws IOException {
        String modJarName = Support_Resources.getURL("Modified_Class.jar");
        InputStream is = new URL(modJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        int count = 0;
        ZipEntry zipEntry = null;
        while (count == 0 || zipEntry != null) {
            count++;
            zipEntry = jin.getNextEntry();
            byte[] buffer = new byte[1024];
            try {
                int length = 0;
                while (length >= 0) {
                    length = jin.read(buffer);
                }
                if (count == indexofTESTCLASS) {
                    fail("Should throw Security Exception");
                }
            } catch (SecurityException e) {
                if (count < indexofTESTCLASS) {
                    throw e;
                }
            }
        }
        assertEquals(totalEntries + 1, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exception checking missed.",
        method = "read",
        args = {byte[].class}
    )
    public void test_Integrate_Jar_read() throws IOException {
        String intJarName = Support_Resources.getURL("Integrate.jar");
        InputStream is = new URL(intJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        int count = 0;
        ZipEntry zipEntry = null;
        while (count == 0 || zipEntry != null) {
            count++;
            zipEntry = jin.getNextEntry();
            byte[] buffer = new byte[1024];
            int length = 0;
            while (length >= 0) {
                length = jin.read(buffer);
            }
        }
        assertEquals(totalEntries + 1, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException & ZipException checking missed.",
        method = "read",
        args = {byte[].class}
    )
    public void test_JarInputStream_Modified_Manifest_MainAttributes_read()
            throws IOException {
        String modJarName = Support_Resources
                .getURL("Modified_Manifest_MainAttributes.jar");
        InputStream is = new URL(modJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        int count = 0;
        ZipEntry zipEntry = null;
        while (count == 0 || zipEntry != null) {
            count++;
            zipEntry = jin.getNextEntry();
            byte[] buffer = new byte[1024];
            try {
                int length = 0;
                while (length >= 0) {
                    length = jin.read(buffer);
                }
                if (count == indexofDSA) {
                    fail("Should throw Security Exception");
                }
            } catch (SecurityException e) {
                if (count != indexofDSA) {
                    throw e;
                }
            }
        }
        assertEquals(totalEntries + 1, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException & ZipException checking missed.",
        method = "read",
        args = {byte[].class}
    )
    public void test_JarInputStream_Modified_SF_EntryAttributes_read()
            throws IOException {
        String modJarName = Support_Resources
                .getURL("Modified_SF_EntryAttributes.jar");
        InputStream is = new URL(modJarName).openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        int count = 0;
        ZipEntry zipEntry = null;
        while (count == 0 || zipEntry != null) {
            count++;
            zipEntry = jin.getNextEntry();
            byte[] buffer = new byte[1024];
            try {
                int length = 0;
                while (length >= 0) {
                    length = jin.read(buffer);
                }
                if (count == indexofDSA) {
                    fail("Should throw Security Exception");
                }
            } catch (SecurityException e) {
                if (count != indexofDSA) {
                    throw e;
                }
            }
        }
        assertEquals(totalEntries + 1, count);
        jin.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "JarInputStream",
        args = {java.io.InputStream.class, boolean.class}
    )
    public void test_ConstructorLjava_io_InputStreamZ() {
        InputStream is = null;
        JarInputStream jis = null;
        try {
            is = new URL(jarName).openConnection().getInputStream();
            boolean hasCorrectEntry = false;
            jis = new JarInputStream(is, true);
            assertNotNull("The jar input stream should have a manifest", jis
                    .getManifest());
            JarEntry je = jis.getNextJarEntry();
            while (je != null) {
                if (je.getName().equals(entryName)) {
                    hasCorrectEntry = true;
                }
                je = jis.getNextJarEntry();
            }
            assertTrue(
                    "The jar input stream does not contain the correct entries",
                    hasCorrectEntry);
        } catch (Exception e) {
            fail("Exception during test: " + e.toString());
        }
        try {
            is.close();
            jis = new JarInputStream(is, false);
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "close",
        args = {}
    )
    public void test_closeAfterException() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        InputStream is = Support_Resources.getStream("Broken_entry.jar");
        JarInputStream jis = new JarInputStream(is, false);
        jis.getNextEntry();
        try {
            jis.getNextEntry();
            fail("ZipException expected");
        } catch (ZipException ee) {
        }
        jis.close();
        try {
            jis.getNextEntry();
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getNextEntry",
        args = {}
    )
    public void test_getNextEntry() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        InputStream is = Support_Resources.getStream("Broken_entry.jar");
        JarInputStream jis = new JarInputStream(is, false);
        jis.getNextEntry();
        try {
            jis.getNextEntry();
            fail("ZipException expected");
        } catch (ZipException ee) {
        }
        try {
            jis.close();  
            jis.getNextEntry();  
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    class Mock_JarInputStream extends JarInputStream {
        public Mock_JarInputStream(InputStream in) throws IOException {
            super(in);
        }
        public ZipEntry createZipEntry(String str) {
            return super.createZipEntry(str);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createZipEntry",
        args = {java.lang.String.class}
    )
    public void test_createZipEntryLjava_lang_String() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        InputStream is = Support_Resources.getStream("Broken_entry.jar");
        Mock_JarInputStream mjis = new Mock_JarInputStream(is);
        assertNotNull(mjis.createZipEntry("New entry"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$ZII() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry_data.jar");
        InputStream is = Support_Resources.getStream("Broken_entry_data.jar");
        JarInputStream jis = new JarInputStream(is, true);
        byte b[] = new byte[100];
        jis.getNextEntry();
        jis.read(b, 0, 100);
        jis.getNextEntry();
        jis.getNextEntry();
        jis.getNextEntry();
        try {
            jis.read(b, 0, 100);
            fail("ZipException expected");
        } catch (ZipException ee) {
        }
        try {
            jis.close();  
            jis.read(b, 0, 100);  
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
}
