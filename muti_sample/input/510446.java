@TestTargetClass(X500Principal.class) 
public class X500PrincipalTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X500Principal",
        args = {String.class}
    )
    public void test_X500Principal_01() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        try {
            X500Principal xpr = new X500Principal(name);
            assertNotNull("Null object returned", xpr);
            String resName = xpr.getName();
            assertEquals(name, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            X500Principal xpr = new X500Principal((String)null);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }
        try {
            X500Principal xpr = new X500Principal("X500PrincipalName");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X500Principal",
        args = {InputStream.class}
    )
    public void test_X500Principal_02() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        byte[] ba = getByteArray(TestUtils.getX509Certificate_v1());
        ByteArrayInputStream is = new ByteArrayInputStream(ba);
        InputStream isNull = null;
        try {
            X500Principal xpr = new X500Principal(is);
            assertNotNull("Null object returned", xpr);
            byte[] resArray = xpr.getEncoded();
            assertEquals(ba.length, resArray.length);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            X500Principal xpr = new X500Principal(isNull);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }
        is = new ByteArrayInputStream(name.getBytes());
        try {
            X500Principal xpr = new X500Principal(is);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X500Principal",
        args = {byte[].class}
    )
    public void test_X500Principal_03() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        byte[] ba = getByteArray(TestUtils.getX509Certificate_v1());
        byte[] baNull = null;
        try {
            X500Principal xpr = new X500Principal(ba);
            assertNotNull("Null object returned", xpr);
            byte[] resArray = xpr.getEncoded();
            assertEquals(ba.length, resArray.length);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            X500Principal xpr = new X500Principal(baNull);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        ba = name.getBytes();
        try {
            X500Principal xpr = new X500Principal(ba);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        X500Principal xpr = new X500Principal(name);        
        try {
            String resName = xpr.getName();
            assertEquals(name, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {String.class}
    )
    public void test_getName_Format() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        String expectedName = "cn=duke,ou=javasoft,o=sun microsystems,c=us";
        X500Principal xpr = new X500Principal(name);
        try {
            String resName = xpr.getName(X500Principal.CANONICAL);
            assertEquals(expectedName, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        expectedName = "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US";
        try {
            String resName = xpr.getName(X500Principal.RFC1779);
            assertEquals(expectedName, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            String resName = xpr.getName(X500Principal.RFC2253);
            assertEquals(name, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            String resName = xpr.getName(null);
            fail("IllegalArgumentException  wasn't thrown");
        } catch (IllegalArgumentException  iae) {
        }
        try {
            String resName = xpr.getName("RFC2254");
            fail("IllegalArgumentException  wasn't thrown");
        } catch (IllegalArgumentException  iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        X500Principal xpr = new X500Principal(name);        
        try {
            int res = xpr.hashCode();
            assertNotNull(res);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        String name = "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US";
        X500Principal xpr = new X500Principal(name);        
        try {
            String res = xpr.toString();
            assertNotNull(res);
            assertEquals(name, res);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public void test_getEncoded() {
        byte[] ba = getByteArray(TestUtils.getX509Certificate_v1());
        X500Principal xpr = new X500Principal(ba);        
        try {
            byte[] res = xpr.getEncoded();
            assertNotNull(res);
            assertEquals(ba.length, res.length);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {Object.class}
    )
    public void test_equals() {
        String name1 = "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US";
        String name2 = "cn=duke,ou=javasoft,o=sun microsystems,c=us";
        String name3 = "CN=Alex Astapchuk, OU=SSG, O=Intel ZAO, C=RU";
        X500Principal xpr1 = new X500Principal(name1);        
        X500Principal xpr2 = new X500Principal(name2);
        X500Principal xpr3 = new X500Principal(name3);
        try {
            assertTrue("False returned", xpr1.equals(xpr2));
            assertFalse("True returned", xpr1.equals(xpr3));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    private byte[] getByteArray(byte[] array) {
        byte[] x = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(array);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
            X500Principal xx = cert.getIssuerX500Principal();
            x = xx.getEncoded();
        } catch (Exception e) {
            return null;
        }  
        return x;
    }
}