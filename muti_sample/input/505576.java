@TestTargetClass(java.lang.reflect.AccessibleObject.class)
public class JavaLangReflectAccessibleObjectTest extends TestCase {
    SecurityManager old;
    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    static class TestClass {
        @SuppressWarnings("unused")
        private int field;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that java.lang.reflect.AccessibleObject.setAccessible(boolean) method calls checkPermission on security manager",
        method = "setAccessible",
        args = {boolean.class}
    )
    public void test_setAccessibleB() throws Exception {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof ReflectPermission
                        && "suppressAccessChecks".equals(permission.getName())) {
                    called = true;              
                }
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        Field field = TestClass.class.getDeclaredField("field");
        field.setAccessible(true);
        assertTrue(
                "java.lang.reflect.AccessibleObject.setAccessible(boolean)  " +
                "must call checkPermission on security permissions",
                s.called);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that java.lang.reflect.AccessibleObject.setAccessible(AccessibleObject[], boolean) method calls checkPermission on security manager",
        method = "setAccessible",
        args = {java.lang.reflect.AccessibleObject[].class, boolean.class}
    )
    public void test_setAccessibleLAccessibleObjectB() throws Exception {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof ReflectPermission
                        && "suppressAccessChecks".equals(permission.getName())) {
                    called = true;
                }
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        Field field = TestClass.class.getDeclaredField("field");
        field.setAccessible(TestClass.class.getDeclaredFields(), true);
        assertTrue(
                "java.lang.reflect.AccessibleObject.setAccessible(AccessibleObject[], boolean)  "
                        + "must call checkPermission on security permissions",
                s.called);
    }
}
