@TestTargetClass(PortUnreachableException.class) 
public class PortUnreachableExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PortUnreachableException",
        args = {}
    )
    public void test_Constructor() {
        PortUnreachableException pue = new PortUnreachableException();
        assertNull(pue.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PortUnreachableException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLString() {
        String [] messages = {"", null, "Test Message"};
        for(String str:messages) {
            PortUnreachableException pue = new PortUnreachableException(str);
            assertEquals(str, pue.getMessage());
        }
    }    
}
