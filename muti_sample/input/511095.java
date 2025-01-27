@TestTargetClass(AccessController.class)
public class AccessController2Test extends junit.framework.TestCase {
    PrivilegedAction<Boolean> privAction = new PrivilegedAction<Boolean>() {
        public Boolean run() {
            try {
                AccessController.checkPermission(new AllPermission());
                return new Boolean(false);
            } catch (SecurityException ex) {
                return new Boolean(true);
            }
        }
    };
    PrivilegedExceptionAction<Boolean> privExceptAction = 
        new PrivilegedExceptionAction<Boolean>() {
        public Boolean run() {
            try {
                AccessController.checkPermission(new AllPermission());
                return new Boolean(false);
            } catch (SecurityException ex) {
                return new Boolean(true);
            }
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "doPrivileged",
        args = {java.security.PrivilegedAction.class, java.security.AccessControlContext.class}
    )
    @KnownFailure("Fails (probably) because no protection domain is set.")
    public void test_doPrivilegedLjava_security_PrivilegedActionLjava_security_AccessControlContext() {
        Boolean pass;
        try {
            AccessController.doPrivileged((PrivilegedAction<?>) null, null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
        pass = AccessController.doPrivileged(privAction, null);
        assertTrue("Test 2: Got AllPermission when providing a null " +
                "AccessControlContext.", pass.booleanValue());
        AccessControlContext acc = AccessController.getContext();
        assertNotNull("Test 3: AccessControlContext must not be null", acc);
        pass = AccessController.doPrivileged(privAction, acc);
        assertTrue("Test 4: Got AllPermission when providing a non-null " +
                "AccessControlContext.", pass.booleanValue());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "doPrivileged",
        args = {java.security.PrivilegedAction.class}
    )
    @KnownFailure("Fails (probably) because no protection domain is set.")
    public void test_doPrivilegedLjava_security_PrivilegedAction() {
        Boolean pass;
        try {
            AccessController.doPrivileged((PrivilegedAction<?>) null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
        pass = AccessController.doPrivileged(privAction);
        assertTrue("Test 2: Got AllPermission when providing no " +
                "AccessControlContext.", pass.booleanValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "doPrivileged",
        args = {java.security.PrivilegedExceptionAction.class, java.security.AccessControlContext.class}
    )
    @KnownFailure("Fails (probably) because no protection domain is set.")
    public void test_doPrivilegedLjava_security_PrivilegedExceptionActionLjava_security_AccessControlContext() {
        Boolean pass;
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<?>) null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        } catch (PrivilegedActionException e) {
            fail("Test 2: Unexpected PrivilegedActionException " + 
                    e.getMessage());
        }
        try {
            pass = AccessController.doPrivileged(privExceptAction, null);
            assertTrue("Test 3: Got AllPermission when providing a null " +
                    "AccessControlContext.", pass.booleanValue());
        } catch (PrivilegedActionException e) {
            fail("Test 4: Unexpected PrivilegedActionException " + 
                    e.getMessage());
        }
        AccessControlContext acc = AccessController.getContext();
        assertNotNull("Test 5: AccessControlContext must not be null", acc);
        try {
            pass = AccessController.doPrivileged(privExceptAction, acc);
            assertTrue("Test 6: Got AllPermission when providing non-null " +
                    "AccessControlContext.", pass.booleanValue());
        } catch (PrivilegedActionException e) {
            fail("Test 7: Unexpected PrivilegedActionException " + 
                    e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "doPrivileged",
        args = {java.security.PrivilegedExceptionAction.class}
    )
    @KnownFailure("Fails (probably) because no protection domain is set.")
    public void test_doPrivilegedLjava_security_PrivilegedExceptionAction() {
        Boolean pass;
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<?>) null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        } catch (PrivilegedActionException e) {
            fail("Test 2: Unexpected PrivilegedActionException " + 
                    e.getMessage());
        }
        try {
            pass = AccessController.doPrivileged(privExceptAction);
            assertTrue("Test 3: Got AllPermission when providing no " +
                    "AccessControlContext.", pass.booleanValue());
        } catch (PrivilegedActionException e) {
            fail("Test 4: Unexpected exception " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkPermission",
        args = {java.security.Permission.class}
    )
    public void test_checkPermission_NullParameter() {
        try {
            AccessController.checkPermission(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkPermission",
        args = {java.security.Permission.class}
    )
    @KnownFailure("Fails (probably) because no protection domain is set.")
    public void test_checkPermission_InvalidPermission() {
        String[] perm_invalid = {null, "1", "", "invalid", "bla-bla", "testCtor123^%$#&^ &^$"};
        Permission perm;
        try {
            AccessController.checkPermission(null);
            fail("NullPointerException should be thrown for NULL parameter");
        } catch (NullPointerException npe) {
        }
        for (int i = 0; i < perm_invalid.length; i++) {
            try {
                perm = new RealPermission(perm_invalid[i]);
                AccessController.checkPermission(perm);
                fail("AccessControlException should be thrown for INVALID parameter " + perm_invalid[i]);
            } catch (AccessControlException ace) {
            } catch (Exception e) {
                fail("Unexpected exception caught: " + e.toString());
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getContext",
        args = {}
    )
    public void test_getContext() {
        try {
            AccessControlContext acc = AccessController.getContext();
            assertNotNull(acc);
            assertTrue(acc instanceof AccessControlContext);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    static final class RealPermission extends Permission {
        private static final long serialVersionUID = 1L;
        public RealPermission(String name) {
            super(name);
        }
        public boolean equals(Object obj) {
            return false;
        }
        public String getActions() {
            return null;
        }
        public int hashCode() {
            return 0;
        }
        public boolean implies(Permission permission) {
            return false;
        }
    }
}