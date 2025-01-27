@TestTargetClass(KeyManagementException.class)
public class KeyManagementExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {}
    )
    public void testKeyManagementException01() {
        KeyManagementException tE = new KeyManagementException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.String.class}
    )
    public void testKeyManagementException02() {
        KeyManagementException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyManagementException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.String.class}
    )
    public void testKeyManagementException03() {
        String msg = null;
        KeyManagementException tE = new KeyManagementException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.Throwable.class}
    )
    public void testKeyManagementException04() {
        Throwable cause = null;
        KeyManagementException tE = new KeyManagementException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.Throwable.class}
    )
    public void testKeyManagementException05() {
        KeyManagementException tE = new KeyManagementException(tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() should contain ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyManagementException06() {
        KeyManagementException tE = new KeyManagementException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyManagementException07() {
        KeyManagementException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyManagementException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyManagementException08() {
        KeyManagementException tE = new KeyManagementException(null, tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "KeyManagementException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testKeyManagementException09() {
        KeyManagementException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyManagementException(msgs[i], tCause);
            String getM = tE.getMessage();
            String toS = tCause.toString();
            if (msgs[i].length() > 0) {
                assertTrue("getMessage() must contain ".concat(msgs[i]), getM
                        .indexOf(msgs[i]) != -1);
                if (!getM.equals(msgs[i])) {
                    assertTrue("getMessage() should contain ".concat(toS), getM
                            .indexOf(toS) != -1);
                }
            }
            assertNotNull("getCause() must not return null", tE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    tE.getCause(), tCause);
        }
    }
}
