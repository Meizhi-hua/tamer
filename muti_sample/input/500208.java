@TestTargetClass(java.nio.CharBuffer.class)
public class WrappedCharBufferTest2 extends ReadOnlyCharBufferTest {
    protected static final String TEST_STRING = "123456789abcdef12345";
    protected void setUp() throws Exception {
        super.setUp();
        capacity = TEST_STRING.length();
        buf = CharBuffer.wrap(TEST_STRING);
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        baseBuf = null;
        buf = null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException, IndexOutOfBoundsException.",
        method = "wrap",
        args = {java.lang.CharSequence.class, int.class, int.class}
    )
    public void testWrappedCharSequence_IllegalArg() {
        String str = TEST_STRING;
        try {
            CharBuffer.wrap(str, -1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(str, 21, 21);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(str, 2, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap(str, 0, 21);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharBuffer.wrap((String)null, -1, 21);
            fail("Should throw Exception"); 
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnsupportedOperationException for CharSequenceAdapter.",
        method = "array",
        args = {}
    )
    public void testArray() {
        try {
            buf.array();
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnsupportedOperationException.",
        method = "arrayOffset",
        args = {}
    )
    public void testArrayOffset() {
        try {
            buf.arrayOffset();
            fail("Should throw Exception"); 
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException, NullPointerException, BufferOverflowException, IndexOutOfBoundsException.",
        method = "put",
        args = {char[].class, int.class, int.class}
    )
    public void testPutcharArrayintint() {
        char array[] = new char[1];
        try {
            buf.put(array, 0, array.length);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put((char[]) null, 0, 1);
            fail("Should throw NullPointerException"); 
        } catch (NullPointerException e) {
        }
        try {
            buf.put(new char[buf.capacity() + 1], 0, buf.capacity() + 1);
            fail("Should throw BufferOverflowException"); 
        } catch (BufferOverflowException e) {
        }
        try {
            buf.put(array, -1, array.length);
            fail("Should throw IndexOutOfBoundsException"); 
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ReadOnlyBufferException, NullPointerException, IllegalArgumentException.",
        method = "read",
        args = {java.nio.CharBuffer.class}
    )
    public void testPutCharBuffer() {
        CharBuffer other = CharBuffer.allocate(1);
        try {
            buf.put(other);
            fail("Should throw ReadOnlyBufferException"); 
        } catch (ReadOnlyBufferException e) {
        }
        try {
            buf.put((CharBuffer) null);
            fail("Should throw NullPointerException"); 
        } catch (NullPointerException e) {
        }
        try {
            buf.put(buf);
            fail("Should throw IllegalArgumentException"); 
        } catch (IllegalArgumentException e) {
        }
    }    
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "slice",
        args = {}
    )
    @AndroidOnly("Fails on RI")
    public void testSlice() {
        super.testSlice();  
    }
}
