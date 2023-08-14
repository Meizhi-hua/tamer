@TestTargetClass(InvalidKeyException.class)
public class InvalidKeyExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidKeyException",
        args = {}
    )
    public void testInvalidKeyException01() {
        InvalidKeyException tE = new InvalidKeyException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidKeyException",
        args = {java.lang.String.class}
    )
    public void testInvalidKeyException02() {
        InvalidKeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeyException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidKeyException",
        args = {java.lang.String.class}
    )
    public void testInvalidKeyException03() {
        String msg = null;
        InvalidKeyException tE = new InvalidKeyException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidKeyException",
        args = {java.lang.Throwable.class}
    )
    public void testInvalidKeyException04() {
        Throwable cause = null;
        InvalidKeyException tE = new InvalidKeyException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidKeyException",
        args = {java.lang.Throwable.class}
    )
    public void testInvalidKeyException05() {
        InvalidKeyException tE = new InvalidKeyException(tCause);
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
        method = "InvalidKeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeyException06() {
        InvalidKeyException tE = new InvalidKeyException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidKeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeyException07() {
        InvalidKeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeyException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "InvalidKeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeyException08() {
        InvalidKeyException tE = new InvalidKeyException(null, tCause);
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
        method = "InvalidKeyException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testInvalidKeyException09() {
        InvalidKeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeyException(msgs[i], tCause);
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