@TestTargetClass(java.nio.DoubleBuffer.class)
public abstract class DoubleBufferTest extends AbstractBufferTest {
    protected static final int SMALL_TEST_LENGTH = 5;
    protected static final int BUFFER_LENGTH = 20;
    protected DoubleBuffer buf;
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = DoubleBuffer.allocate(BUFFER_LENGTH);
        loadTestData1(buf);
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "allocate",
        args = {int.class}
    )
    public void test_AllocateI() {
        DoubleBuffer testBuf = DoubleBuffer.allocate(20);
        assertEquals(0, testBuf.position());
        assertNotNull(testBuf.array());
        assertEquals(0, testBuf.arrayOffset());
        assertEquals(20, testBuf.limit());
        assertEquals(20, testBuf.capacity());
        testBuf = DoubleBuffer.allocate(0);
        assertEquals(0, testBuf.position());
        assertNotNull(testBuf.array());
        assertEquals(0, testBuf.arrayOffset());
        assertEquals(0, testBuf.limit());
        assertEquals(0, testBuf.capacity());
        try {
            testBuf = DoubleBuffer.allocate(-20);
            fail("allocate method does not throws expected exception");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies boundary values.",
        method = "put",
        args = {double.class}
    )
    public void testNaNs() {
        long[] nans = new long[] { 0x7ff0000000000000L, 0xfff0000000000000L,
                0x7ff8000000000000L };
        for (int i = 0; i < nans.length; i++) {
            long longBitsIn = nans[i];
            double dbl = Double.longBitsToDouble(longBitsIn);
            long longBitsOut = Double.doubleToRawLongBits(dbl);
            assertTrue(longBitsIn == longBitsOut);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putDouble(dbl);
            double bufDoubleOut = buffer.getDouble(0);
            long bufLongOut = Double.doubleToRawLongBits(bufDoubleOut);
            assertTrue(longBitsIn == bufLongOut);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "array",
        args = {}
    )
    public void testArray() {
        double array[] = buf.array();
        assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());
        loadTestData1(array, buf.arrayOffset(), buf.capacity());
        assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());
        loadTestData2(array, buf.arrayOffset(), buf.capacity());
        assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());
        loadTestData1(buf);
        assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());
        loadTestData2(buf);
        assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "arrayOffset",
        args = {}
    )
    public void testArrayOffset() {
        double array[] = buf.array();
        for(int i = 0; i < buf.capacity(); i++) {
            array[i] = i;
        }
        int offset = buf.arrayOffset();
        assertContentEquals(buf, array, offset, buf.capacity());
        DoubleBuffer wrapped = DoubleBuffer.wrap(array, 3, array.length - 3);
        loadTestData1(array, wrapped.arrayOffset(), wrapped.capacity());
        assertContentEquals(buf, array, offset, buf.capacity());
        loadTestData2(array, wrapped.arrayOffset(), wrapped.capacity());
        assertContentEquals(buf, array, offset, buf.capacity());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "asReadOnlyBuffer",
        args = {}
    )
    public void testAsReadOnlyBuffer() {
        buf.clear();
        buf.mark();
        buf.position(buf.limit());
        DoubleBuffer readonly = buf.asReadOnlyBuffer();
        assertNotSame(buf, readonly);
        assertTrue(readonly.isReadOnly());
        assertEquals(buf.position(), readonly.position());
        assertEquals(buf.limit(), readonly.limit());
        assertEquals(buf.isDirect(), readonly.isDirect());
        assertEquals(buf.order(), readonly.order());
        assertContentEquals(buf, readonly);
        readonly.reset();
        assertEquals(readonly.position(), 0);
        readonly.clear();
        assertEquals(buf.position(), buf.limit());
        buf.reset();
        assertEquals(buf.position(), 0);
        DoubleBuffer dbuffer1 = DoubleBuffer.wrap(new double[] { Double.NaN });
        DoubleBuffer dbuffer2 = DoubleBuffer.wrap(new double[] { Double.NaN });
        DoubleBuffer dbuffer3 = DoubleBuffer.wrap(new double[] { 42d });
        assertEquals("Failed equal comparison with NaN entry", 0, dbuffer1
                .compareTo(dbuffer2));
        assertEquals("Failed greater than comparison with NaN entry", 1, dbuffer3
                .compareTo(dbuffer1));
        assertEquals("Failed greater than comparison with NaN entry", 1, dbuffer1
                .compareTo(dbuffer3));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "compact",
        args = {}
    )
    @AndroidOnly("Fails on RI. See comment below")
    public void testCompact() {
        buf.clear();
        buf.mark();
        loadTestData1(buf);
        DoubleBuffer ret = buf.compact();
        assertSame(ret, buf);
        assertEquals(buf.position(), buf.capacity());
        assertEquals(buf.limit(), buf.capacity());
        assertContentLikeTestData1(buf, 0, 0.0, buf.capacity());
        try {
            buf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        buf.position(0);
        buf.limit(0);
        buf.mark();
        ret = buf.compact();
        assertSame(ret, buf);
        assertEquals(buf.position(), 0);
        assertEquals(buf.limit(), buf.capacity());
        assertContentLikeTestData1(buf, 0, 0.0, buf.capacity());
        try {
            buf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        assertTrue(buf.capacity() > 5);
        buf.position(1);
        buf.limit(5);
        buf.mark();
        ret = buf.compact();
        assertSame(ret, buf);
        assertEquals(buf.position(), 4);
        assertEquals(buf.limit(), buf.capacity());
        assertContentLikeTestData1(buf, 0, 1.0, 4);
        try {
            buf.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.nio.DoubleBuffer.class}
    )
    public void testCompareTo() {
        DoubleBuffer other = DoubleBuffer.allocate(buf.capacity());
        loadTestData1(other);
        assertEquals(0, buf.compareTo(other));
        assertEquals(0, other.compareTo(buf));
        buf.position(1);
        assertTrue(buf.compareTo(other) > 0);
        assertTrue(other.compareTo(buf) < 0);
        other.position(2);
        assertTrue(buf.compareTo(other) < 0);
        assertTrue(other.compareTo(buf) > 0);
        buf.position(2);
        other.limit(5);
        assertTrue(buf.compareTo(other) > 0);
        assertTrue(other.compareTo(buf) < 0);
        DoubleBuffer dbuffer1 = DoubleBuffer.wrap(new double[] { Double.NaN });
        DoubleBuffer dbuffer2 = DoubleBuffer.wrap(new double[] { Double.NaN });
        DoubleBuffer dbuffer3 = DoubleBuffer.wrap(new double[] { 42d });
        assertEquals("Failed equal comparison with NaN entry", 0, dbuffer1
                .compareTo(dbuffer2));
        assertEquals("Failed greater than comparison with NaN entry", 1, dbuffer3
                .compareTo(dbuffer1));
        assertEquals("Failed greater than comparison with NaN entry", 1, dbuffer1
                .compareTo(dbuffer3));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "duplicate",
        args = {}
    )
    public void testDuplicate() {
        buf.clear();
        buf.mark();
        buf.position(buf.limit());
        DoubleBuffer duplicate = buf.duplicate();
        assertNotSame(buf, duplicate);
        assertEquals(buf.position(), duplicate.position());
        assertEquals(buf.limit(), duplicate.limit());
        assertEquals(buf.isReadOnly(), duplicate.isReadOnly());
        assertEquals(buf.isDirect(), duplicate.isDirect());
        assertEquals(buf.order(), duplicate.order());
        assertContentEquals(buf, duplicate);
        duplicate.reset();
        assertEquals(duplicate.position(), 0);
        duplicate.clear();
        assertEquals(buf.position(), buf.limit());
        buf.reset();
        assertEquals(buf.position(), 0);
        if (!duplicate.isReadOnly()) {
            loadTestData1(buf);
            assertContentEquals(buf, duplicate);
            loadTestData2(duplicate);
            assertContentEquals(buf, duplicate);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        assertTrue(buf.equals(buf));
        DoubleBuffer readonly = buf.asReadOnlyBuffer();
        assertTrue(buf.equals(readonly));
        DoubleBuffer duplicate = buf.duplicate();
        assertTrue(buf.equals(duplicate));
        assertFalse(buf.equals(Boolean.TRUE));
        assertTrue(buf.capacity() > 5);
        buf.limit(buf.capacity()).position(0);
        readonly.limit(readonly.capacity()).position(1);
        assertFalse(buf.equals(readonly));
        buf.limit(buf.capacity() - 1).position(0);
        duplicate.limit(duplicate.capacity()).position(0);
        assertFalse(buf.equals(duplicate));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "get",
        args = {}
    )
    public void testGet() {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            assertEquals(buf.get(), buf.get(i), 0.01);
        }
        try {
            buf.get();
            fail("Should throw Exception"); 
        } catch (BufferUnderflowException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "get",
        args = {double[].class}
    )
    public void testGetdoubleArray() {
        double array[] = new double[1];
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            DoubleBuffer ret = buf.get(array);
            assertEquals(array[0], buf.get(i), 0.01);
            assertSame(ret, buf);
        }
        buf.get(new double[0]);
        try {
            buf.get(array);
            fail("Should throw Exception"); 
        } catch (BufferUnderflowException e) {
        }
        try {
            buf.get((double[])null);
            fail("Should throw Exception"); 
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "get",
        args = {double[].class, int.class, int.class}
    )
    public void testGetdoubleArrayintint() {
        buf.clear();
        double array[] = new double[buf.capacity()];
        try {
            buf.get(new double[buf.capacity() + 1], 0, buf.capacity() + 1);
            fail("Should throw Exception"); 
        } catch (BufferUnderflowException e) {
        }
        assertEquals(buf.position(), 0);
        try {
            buf.get(array, -1, array.length);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        buf.get(array, array.length, 0);
        try {
            buf.get(array, array.length + 1, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals(buf.position(), 0);
        try {
            buf.get(array, 2, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.get((double[])null, 0, -1);
            fail("Should throw Exception"); 
        } catch (NullPointerException e) {
        }
        try {
            buf.get(array, 2, array.length);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.get(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.get(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals(buf.position(), 0);
        buf.clear();
        DoubleBuffer ret = buf.get(array, 0, array.length);
        assertEquals(buf.position(), buf.capacity());
        assertContentEquals(buf, array, 0, array.length);
        assertSame(ret, buf);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "get",
        args = {int.class}
    )
    public void testGetint() {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            assertEquals(buf.get(), buf.get(i), 0.01);
        }
        try {
            buf.get(-1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.get(buf.limit());
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hasArray",
        args = {}
    )
    public void testHasArray() {
        if (buf.hasArray()) {
            assertNotNull(buf.array());
        } else {
            try {
                buf.array();
                fail("Should throw Exception"); 
            } catch (UnsupportedOperationException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        buf.clear();
        DoubleBuffer readonly = buf.asReadOnlyBuffer();
        DoubleBuffer duplicate = buf.duplicate();
        assertTrue(buf.hashCode() == readonly.hashCode());
        assertTrue(buf.capacity() > 5);
        duplicate.position(buf.capacity() / 2);
        assertTrue(buf.hashCode() != duplicate.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify direct buffer.",
        method = "isDirect",
        args = {}
    )
    public void testIsDirect() {
        assertFalse(buf.isDirect());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Abstract method.",
        method = "isReadOnly",
        args = {}
    )
    public void testIsReadOnly() {
        assertFalse(buf.isReadOnly());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "order",
        args = {}
    )
    public void testOrder() {
        assertEquals(ByteOrder.nativeOrder(), buf.order());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify boundary values, and ReadOnlyBufferException.",
        method = "put",
        args = {double.class}
    )
    public void testPutdouble() {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            DoubleBuffer ret = buf.put((double) i);
            assertEquals(buf.get(i), (double) i, 0.0);
            assertSame(ret, buf);
        }
        try {
            buf.put(0);
            fail("Should throw Exception"); 
        } catch (BufferOverflowException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ReadOnlyBufferException.",
        method = "put",
        args = {double[].class}
    )
    public void testPutdoubleArray() {
        double array[] = new double[1];
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            array[0] = (double) i;
            DoubleBuffer ret = buf.put(array);
            assertEquals(buf.get(i), (double) i, 0.0);
            assertSame(ret, buf);
        }
        try {
            buf.put(array);
            fail("Should throw Exception"); 
        } catch (BufferOverflowException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ReadOnlyBufferException.",
        method = "put",
        args = {double[].class, int.class, int.class}
    )
    public void testPutdoubleArrayintint() {
        buf.clear();
        double array[] = new double[buf.capacity()];
        try {
            buf.put(new double[buf.capacity() + 1], 0, buf.capacity() + 1);
            fail("Should throw Exception"); 
        } catch (BufferOverflowException e) {
        }
        assertEquals(buf.position(), 0);
        try {
            buf.put(array, -1, array.length);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.put(array, array.length + 1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        buf.put(array, array.length, 0);
        assertEquals(buf.position(), 0);
        try {
            buf.put(array, 0, -1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.put((double[])null, 0, -1);
            fail("Should throw Exception"); 
        } catch (NullPointerException e) {
        }
        try {
            buf.put(array, 2, array.length);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.put(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.put(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals(buf.position(), 0);
        loadTestData2(array, 0, array.length);
        DoubleBuffer ret = buf.put(array, 0, array.length);
        assertEquals(buf.position(), buf.capacity());
        assertContentEquals(buf, array, 0, array.length);
        assertSame(ret, buf);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ReadOnlyBufferException.",
        method = "put",
        args = {java.nio.DoubleBuffer.class}
    )
    public void testPutDoubleBuffer() {
        DoubleBuffer other = DoubleBuffer.allocate(buf.capacity());
        try {
            buf.put(buf);
            fail("Should throw Exception"); 
        } catch (IllegalArgumentException e) {
        }
        try {
            buf.put(DoubleBuffer.allocate(buf.capacity() + 1));
            fail("Should throw Exception"); 
        } catch (BufferOverflowException e) {
        }
        loadTestData2(other);
        other.clear();
        buf.clear();
        DoubleBuffer ret = buf.put(other);
        assertEquals(other.position(), other.capacity());
        assertEquals(buf.position(), buf.capacity());
        assertContentEquals(other, buf);
        assertSame(ret, buf);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ReadOnlyBufferException.",
        method = "put",
        args = {int.class, double.class}
    )
    public void testPutintdouble() {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), 0);
            DoubleBuffer ret = buf.put(i, (double) i);
            assertEquals(buf.get(i), (double) i, 0.0);
            assertSame(ret, buf);
        }
        try {
            buf.put(-1, 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            buf.put(buf.limit(), 0);
            fail("Should throw Exception"); 
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "slice",
        args = {}
    )
    public void testSlice() {
        assertTrue(buf.capacity() > 5);
        buf.position(1);
        buf.limit(buf.capacity() - 1);
        DoubleBuffer slice = buf.slice();
        assertEquals(buf.isReadOnly(), slice.isReadOnly());
        assertEquals(buf.isDirect(), slice.isDirect());
        assertEquals(buf.order(), slice.order());
        assertEquals(slice.position(), 0);
        assertEquals(slice.limit(), buf.remaining());
        assertEquals(slice.capacity(), buf.remaining());
        try {
            slice.reset();
            fail("Should throw Exception"); 
        } catch (InvalidMarkException e) {
        }
        if (!slice.isReadOnly()) {
            loadTestData1(slice);
            assertContentLikeTestData1(buf, 1, 0, slice.capacity());
            buf.put(2, 500);
            assertEquals(slice.get(1), 500, 0.0);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        String str = buf.toString();
        assertTrue(str.indexOf("Double") >= 0 || str.indexOf("double") >= 0);
        assertTrue(str.indexOf("" + buf.position()) >= 0);
        assertTrue(str.indexOf("" + buf.limit()) >= 0);
        assertTrue(str.indexOf("" + buf.capacity()) >= 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {double[].class}
    )
    public void test_Wrap$D() {
        double array[] = new double[BUFFER_LENGTH];
        loadTestData1(array, 0, BUFFER_LENGTH);
        DoubleBuffer buf2 = DoubleBuffer.wrap(array);
        assertEquals(buf2.capacity(), array.length);
        assertEquals(buf2.limit(), array.length);
        assertEquals(buf2.position(), 0);
        assertContentEquals(buf2, array, 0, array.length);
        loadTestData2(array, 0, buf.capacity());
        assertContentEquals(buf2, array, 0, array.length);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {double[].class, int.class, int.class}
    )
    public void test_Wrap$DII() {
        double array[] = new double[BUFFER_LENGTH];
        int offset = 5;
        int length = BUFFER_LENGTH - offset;
        loadTestData1(array, 0, BUFFER_LENGTH);
        DoubleBuffer buf2 = DoubleBuffer.wrap(array, offset, length);
        assertEquals(buf2.capacity(), array.length);
        assertEquals(buf2.position(), offset);
        assertEquals(buf2.limit(), offset + length);
        assertEquals(buf2.arrayOffset(), 0);
        assertContentEquals(buf2, array, 0, array.length);
        loadTestData2(array, 0, buf.capacity());
        assertContentEquals(buf2, array, 0, array.length);
        try {
            offset = 7;
            buf2 = DoubleBuffer.wrap(array, offset, length);
            fail("wrap method does not throws expected exception");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    void loadTestData1(double array[], int offset, int length) {
        for (int i = 0; i < length; i++) {
            array[offset + i] = (double) i;
        }
    }
    void loadTestData2(double array[], int offset, int length) {
        for (int i = 0; i < length; i++) {
            array[offset + i] = (double) length - i;
        }
    }
    void loadTestData1(DoubleBuffer buf) {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            buf.put(i, (double) i);
        }
    }
    void loadTestData2(DoubleBuffer buf) {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            buf.put(i, (double) buf.capacity() - i);
        }
    }
    private void assertContentEquals(DoubleBuffer buf, double array[],
            int offset, int length) {
        for (int i = 0; i < length; i++) {
            assertEquals(buf.get(i), array[offset + i], 0.01);
        }
    }
    private void assertContentEquals(DoubleBuffer buf, DoubleBuffer other) {
        assertEquals(buf.capacity(), other.capacity());
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.get(i), other.get(i), 0.01);
        }
    }
    private void assertContentLikeTestData1(DoubleBuffer buf, int startIndex,
            double startValue, int length) {
        double value = startValue;
        for (int i = 0; i < length; i++) {
            assertEquals(buf.get(startIndex + i), value, 0.01);
            value = value + 1.0;
        }
    }
}
