@TestTargetClass(ClosedSelectorException.class)
public class ClosedSelectorExceptionTest extends TestCase {
    public void test_Constructor() {
        ClosedSelectorException e = new ClosedSelectorException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "ClosedSelectorException",
            args = {}
        )
    })     
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new ClosedSelectorException());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationGolden",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "ClosedSelectorException",
            args = {}
        )
    })     
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new ClosedSelectorException());
    }
}