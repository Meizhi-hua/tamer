@TestTargetClass(PrivilegedExceptionAction.class)
public class PrivilegedExceptionActionTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    private class MyPrivilegedExceptionAction implements
            PrivilegedExceptionAction<String> {
        private boolean called = false;
        public String run() throws Exception {
            called = true;
            return "ok";
        }
    }
    private class MyPrivilegedExceptionAction2 implements
            PrivilegedExceptionAction<String> {
        private boolean called = false;
        private Exception toThrow = null;
        public MyPrivilegedExceptionAction2(Exception toThrow) {
            this.toThrow = toThrow;
        }
        public String run() throws Exception {
            called = true;
            if (toThrow == null) {
                return "ok";
            } else {
                throw toThrow;
            }
        }
    }
    @TestTargetNew(
            level=TestLevel.COMPLETE,
            method="run"
    )
    public void testRun() {
        MyPrivilegedExceptionAction action1 = new MyPrivilegedExceptionAction();
        try {
            String result = AccessController.doPrivileged(action1);
            assertEquals("unexpected result", "ok", result);
            assertTrue("method not called", action1.called);
        } catch (PrivilegedActionException e) {
            fail("unexpected exception : " + e);
        }
        Exception[] exceptions = {new NullPointerException(), new IOException(), null};
        for (int i = 0; i < exceptions.length; i++) {
            Exception exception = exceptions[i];
            MyPrivilegedExceptionAction2 action2 = new MyPrivilegedExceptionAction2(exception);
            try {
                String result = AccessController.doPrivileged(action2);
                assertTrue("method not called", action1.called);
                if (exception == null)
                {
                    assertEquals("unexpected result", "ok", result);
                }
                else {
                    fail("privileged action exception expected");
                }
            } catch (PrivilegedActionException e) {
                assertTrue("method not called", action2.called);
                assertSame("expected exception not thrown", exception, e.getCause());
            } catch (RuntimeException e) {
                assertSame("expected exception not thrown", exception, e);
            }
        }
    }
}
