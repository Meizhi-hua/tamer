@TestTargetClass(AbstractWindowedCursor.class)
public class AbstractWindowedCursorTest extends InstrumentationTestCase {
    private static final String TEST_STRING = "TESTSTRING";
    private static final int COLUMN_INDEX0 = 0;
    private static final int COLUMN_INDEX1 = 1;
    private static final int ROW_INDEX0 = 0;
    private static final int TEST_COLUMN_COUNT = 7;
    private MockAbstractWindowedCursor mCursor;
    private CursorWindow mWindow;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCursor = new MockAbstractWindowedCursor();
        mWindow = new CursorWindow(false);
    }
    @Override
    protected void tearDown() throws Exception {
        mCursor.close();
        mWindow.close();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isNull",
        args = {int.class}
    )
    public void testIsNull() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        mCursor.moveToFirst();
        assertTrue(mCursor.isNull(COLUMN_INDEX0));
        assertTrue(mWindow.allocRow());
        String str = "abcdefg";
        assertTrue(mWindow.putString(str, ROW_INDEX0, COLUMN_INDEX0));
        assertFalse(mCursor.isNull(COLUMN_INDEX0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isBlob",
        args = {int.class}
    )
    public void testIsBlob() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        mCursor.moveToFirst();
        assertFalse(mCursor.isBlob(COLUMN_INDEX0));
        String str = "abcdefg";
        assertTrue(mWindow.putString(str, ROW_INDEX0, COLUMN_INDEX0));
        assertTrue(mWindow.putBlob(new byte[10], ROW_INDEX0, COLUMN_INDEX1));
        assertTrue(mCursor.isBlob(COLUMN_INDEX1));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWindow",
            args = {android.database.CursorWindow.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        )
    })
    public void testHasWindow() {
        assertFalse(mCursor.hasWindow());
        assertNull(mCursor.getWindow());
        mCursor.setWindow(mWindow);
        assertTrue(mCursor.hasWindow());
        assertSame(mWindow, mCursor.getWindow());
        mCursor.setWindow(null);
        assertFalse(mCursor.hasWindow());
        assertNull(mCursor.getWindow());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getString",
        args = {int.class}
    )
    public void testGetString() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        mCursor.moveToFirst();
        String str = "abcdefg";
        assertTrue(mWindow.putString(str, ROW_INDEX0, COLUMN_INDEX0));
        assertEquals(str, mCursor.getString(COLUMN_INDEX0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getShort",
        args = {int.class}
    )
    public void testGetShort() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        mCursor.moveToFirst();
        short shortNumber = 10;
        assertTrue(mWindow.putLong((long) shortNumber, ROW_INDEX0, COLUMN_INDEX0));
        assertEquals(shortNumber, mCursor.getShort(COLUMN_INDEX0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLong",
        args = {int.class}
    )
    public void testGetLong() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        mCursor.moveToFirst();
        long longNumber = 10;
        assertTrue(mWindow.putLong(longNumber, ROW_INDEX0, COLUMN_INDEX0));
        assertEquals(longNumber, mCursor.getLong(COLUMN_INDEX0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInt",
        args = {int.class}
    )
    public void testGetInt() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        mCursor.moveToFirst();
        int intNumber = 10;
        assertTrue(mWindow.putLong((long) intNumber, ROW_INDEX0, COLUMN_INDEX0));
        assertEquals(intNumber, mCursor.getInt(COLUMN_INDEX0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFloat",
        args = {int.class}
    )
    public void testGetFloat() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        mCursor.moveToFirst();
        float f1oatNumber = 1.26f;
        assertTrue(mWindow.putDouble((double) f1oatNumber, ROW_INDEX0, COLUMN_INDEX0));
        assertEquals(f1oatNumber, mCursor.getFloat(COLUMN_INDEX0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDouble",
        args = {int.class}
    )
    public void testGetDouble() {
        mCursor.setWindow(mWindow);
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        double db1 = 1.26;
        assertTrue(mWindow.putDouble(db1, ROW_INDEX0, COLUMN_INDEX0));
        double db2 = mWindow.getDouble(ROW_INDEX0, COLUMN_INDEX0);
        assertEquals(db1, db2);
        mCursor.moveToFirst();
        double cd = mCursor.getDouble(COLUMN_INDEX0);
        assertEquals(db1, cd);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBlob",
        args = {int.class}
    )
    public void testGetBlob() {
        byte TEST_VALUE = 3;
        byte BLOB_SIZE = 100;
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        assertTrue(mWindow.putString("", ROW_INDEX0, COLUMN_INDEX0));
        byte[] blob = new byte[BLOB_SIZE];
        Arrays.fill(blob, TEST_VALUE);
        assertTrue(mWindow.putBlob(blob, ROW_INDEX0, COLUMN_INDEX1));
        mCursor.setWindow(mWindow);
        mCursor.moveToFirst();
        byte[] targetBuffer = mCursor.getBlob(COLUMN_INDEX1);
        assertEquals(BLOB_SIZE, targetBuffer.length);
        assertTrue(Arrays.equals(blob, targetBuffer));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "copyStringToBuffer",
        args = {int.class, android.database.CharArrayBuffer.class}
    )
    public void testCopyStringToBuffer() {
        assertTrue(mWindow.setNumColumns(TEST_COLUMN_COUNT));
        assertTrue(mWindow.allocRow());
        assertTrue(mWindow.putString(TEST_STRING, ROW_INDEX0, COLUMN_INDEX0));
        assertTrue(mWindow.putString("", ROW_INDEX0, COLUMN_INDEX1));
        mCursor.setWindow(mWindow);
        mCursor.moveToFirst();
        CharArrayBuffer charArrayBuffer = new CharArrayBuffer(TEST_STRING.length());
        mCursor.copyStringToBuffer(COLUMN_INDEX0, charArrayBuffer);
        assertEquals(TEST_STRING.length(), charArrayBuffer.sizeCopied);
        assertTrue(Arrays.equals(TEST_STRING.toCharArray(), charArrayBuffer.data));
        Arrays.fill(charArrayBuffer.data, '\0');
        mCursor.copyStringToBuffer(COLUMN_INDEX1, charArrayBuffer);
        assertEquals(0, charArrayBuffer.sizeCopied);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "checkPosition",
        args = {}
    )
    public void testCheckPosition() {
        try {
            mCursor.checkPosition();
            fail("testCheckPosition failed");
        } catch (CursorIndexOutOfBoundsException e) {
        }
        try {
            assertTrue(mCursor.moveToPosition(mCursor.getCount() - 1));
            mCursor.checkPosition();
            fail("testCheckPosition failed");
        } catch (StaleDataException e) {
        }
        assertTrue(mCursor.moveToPosition(mCursor.getCount() - 1));
        mCursor.setWindow(mWindow);
        mCursor.checkPosition();
    }
    private class MockAbstractWindowedCursor extends AbstractWindowedCursor {
        public MockAbstractWindowedCursor() {
        }
        @Override
        public String[] getColumnNames() {
            return new String[] {
                    "col1", "col2", "col3"
            };
        }
        @Override
        public int getCount() {
            return 1;
        }
        @Override
        protected void checkPosition() {
            super.checkPosition();
        }
    }
}
