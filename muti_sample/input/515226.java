@TestTargetClass(StackTraceElement.class) 
public class StackTraceElementTest extends TestCase {
    private StackTraceElementOriginal original;
    @Override
    protected void setUp() throws Exception {
        original = new StackTraceElementOriginal();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StackTraceElement",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, int.class}
    )
    public void
    test_ConstructorLjava_lang_StringLjava_lang_StringLjava_lang_StringI() {
        StackTraceElement ste2 = null;
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste1 = e.getStackTrace()[0];
            ste2 = new StackTraceElement(ste1.getClassName(),
                    ste1.getMethodName(),
                    ste1.getFileName(),
                    ste1.getLineNumber());
            assertEquals("Incorrect value of class name",
                    ste1.getClassName(), ste2.getClassName());
            assertEquals("Incorrect value of method name",
                    ste1.getMethodName(), ste2.getMethodName());
            assertEquals("Incorrect value of file name",
                    ste1.getFileName(), ste2.getFileName());
            assertEquals("Incorrect value of line number",
                    ste1.getLineNumber(), ste2.getLineNumber());
        }
        assertNotNull("Incorrect stack trace object", ste2);
        try {
            new StackTraceElement(null,
                    ste2.getMethodName(),
                    ste2.getFileName(),
                    ste2.getLineNumber());
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
        }
        try {
            new StackTraceElement(ste2.getClassName(),
                    null,
                    ste2.getFileName(),
                    ste2.getLineNumber());
            fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
        }
        try {
            new StackTraceElement(ste2.getClassName(),
                    ste2.getMethodName(),
                    null,
                    ste2.getLineNumber());
        } catch (NullPointerException e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste1 = e.getStackTrace()[0];
            StackTraceElement ste2 =
                new StackTraceElement(ste1.getClassName(),
                        ste1.getMethodName(),
                        ste1.getFileName(),
                        ste1.getLineNumber());
            assertEquals("Objects are equaled", ste1, ste2);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClassName",
        args = {}
    )
    public void test_getClassName() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect class name",
                    getClass().getPackage().getName() +
                    ".StackTraceElementOriginal",
                    e.getStackTrace()[0].getClassName());
            assertEquals("Incorrect class name",
                    getClass().getPackage().getName() +
                    ".StackTraceElementTest",
                    e.getStackTrace()[1].getClassName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFileName",
        args = {}
    )
    public void test_getFileName() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect file name",
                    "StackTraceElementOriginal.java",
                    e.getStackTrace()[0].getFileName());
            assertEquals("Incorrect file name",
                    "StackTraceElementTest.java",
                    e.getStackTrace()[1].getFileName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLineNumber",
        args = {}
    )
    public void test_getLineNumber() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect line number",
                    26, e.getStackTrace()[0].getLineNumber());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMethodName",
        args = {}
    )
    public void test_getMethodName() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertEquals("Incorrect method name",
                    "pureJavaMethod",
                    e.getStackTrace()[0].getMethodName());
            assertEquals("Incorrect method name",
                    "test_getMethodName",
                    e.getStackTrace()[1].getMethodName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste1 = e.getStackTrace()[0];
            StackTraceElement ste2 =
                new StackTraceElement(ste1.getClassName(),
                        ste1.getMethodName(),
                        ste1.getFileName(),
                        ste1.getLineNumber());
            assertEquals("Incorrect value of hash code",
                    ste1.hashCode(), ste2.hashCode());
            assertFalse("Incorrect value of hash code",
                    ste1.hashCode() == e.getStackTrace()[1].hashCode());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isNativeMethod",
        args = {}
    )
    public void test_isNativeMethod() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            assertFalse("Incorrect method type",
                    e.getStackTrace()[0].isNativeMethod());
        }
        try {
            original.pureNativeMethod(new Object());
        } catch (Error e) {
            assertTrue("Incorrect method type",
                    e.getStackTrace()[0].isNativeMethod());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        try {
            original.pureJavaMethod(new Object());
        } catch (Exception e) {
            StackTraceElement ste = e.getStackTrace()[0];
            assertTrue("String representation doesn't contain a package name",
                    ste.toString().contains(getClass().getPackage().getName()));
            assertTrue("String representation doesn't contain a class name",
                    ste.toString().contains("StackTraceElementOriginal"));
            assertTrue("String representation doesn't contain a file name",
                    ste.toString().contains("StackTraceElementOriginal.java"));
            assertTrue("String representation doesn't contain a line number",
                    ste.toString().contains("26"));
            assertTrue("String representation doesn't contain a method name",
                    ste.toString().contains("pureJavaMethod"));
        }
    }
}
