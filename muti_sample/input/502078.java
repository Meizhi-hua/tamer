@TestTargetClass(SQLiteCursor.class)
public class SQLiteCursorTest extends AndroidTestCase {
    private SQLiteDatabase mDatabase;
    private static final String[] COLUMNS = new String[] { "_id", "number_1", "number_2" };
    private static final String TABLE_NAME = "test";
    private static final String TABLE_COLUMNS = " number_1 INTEGER, number_2 INTEGER";
    private static final int DEFAULT_TABLE_VALUE_BEGINS = 1;
    private static final int TEST_COUNT = 10;
    private static final String TEST_SQL = "SELECT * FROM test ORDER BY number_1";
    private static final String DATABASE_FILE = "database_test.db";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getContext().deleteDatabase(DATABASE_FILE);
        mDatabase = getContext().openOrCreateDatabase(DATABASE_FILE, Context.MODE_PRIVATE, null);
        createTable(TABLE_NAME, TABLE_COLUMNS);
        addValuesIntoTable(TABLE_NAME, DEFAULT_TABLE_VALUE_BEGINS, TEST_COUNT);
    }
    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        getContext().deleteDatabase(DATABASE_FILE);
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SQLiteCursor",
        args = {android.database.sqlite.SQLiteDatabase.class,
                android.database.sqlite.SQLiteCursorDriver.class, java.lang.String.class,
                android.database.sqlite.SQLiteQuery.class}
    )
    @ToBeFixed(bug = "1686574", explanation = "can not get an instance of SQLiteQuery" +
            " or construct it directly for testing, and if SQLiteQuery is null, SQLiteCursor" +
            " constructor should throw NullPointerException")
    public void testConstructor() {
        SQLiteDirectCursorDriver cursorDriver = new SQLiteDirectCursorDriver(mDatabase,
                TEST_SQL, TABLE_NAME);
        try {
            new SQLiteCursor(mDatabase, cursorDriver, TABLE_NAME, null);
            fail("constructor didn't throw NullPointerException when SQLiteQuery is null");
        } catch (NullPointerException e) {
        }
        SQLiteCursor cursor = getCursor();
        assertNotNull(cursor);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requery",
            args = {}
        )
    })
    public void testClose() {
        SQLiteCursor cursor = getCursor();
        assertTrue(cursor.moveToFirst());
        assertFalse(cursor.isClosed());
        assertTrue(cursor.requery());
        cursor.close();
        assertFalse(cursor.requery());
        try {
            cursor.moveToFirst();
            fail("moveToFirst didn't throw IllegalStateException after closed.");
        } catch (IllegalStateException e) {
        }
        assertTrue(cursor.isClosed());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "deactivate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWindow",
            args = {android.database.CursorWindow.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerDataSetObserver",
            args = {android.database.DataSetObserver.class}
        )
    })
    public void testRegisterDataSetObserver() {
        SQLiteCursor cursor = getCursor();
        MockCursorWindow cursorWindow = new MockCursorWindow(false);
        MockObserver observer = new MockObserver();
        cursor.setWindow(cursorWindow);
        assertFalse(observer.hasInvalidated());
        cursor.moveToLast();
        assertFalse(cursorWindow.isClosed());
        cursor.deactivate();
        assertFalse(observer.hasInvalidated());
        assertTrue(cursorWindow.isClosed());
        assertTrue(cursor.requery());
        cursor.registerDataSetObserver(observer);
        assertFalse(observer.hasInvalidated());
        cursor.moveToLast();
        assertEquals(TEST_COUNT, cursor.getInt(1));
        cursor.deactivate();
        assertTrue(observer.hasInvalidated());
        try {
            cursor.getInt(1);
            fail("After deactivating, cursor cannot execute getting value operations.");
        } catch (StaleDataException e) {
        }
        assertTrue(cursor.requery());
        cursor.moveToLast();
        assertEquals(TEST_COUNT, cursor.getInt(1));
        try {
            cursor.registerDataSetObserver(observer);
            fail("didn't throw IllegalStateException when register existed observer");
        } catch (IllegalStateException e) {
        }
        cursor.unregisterDataSetObserver(observer);
        observer.resetStatus();
        assertFalse(observer.hasInvalidated());
        cursor.deactivate();
        assertFalse(observer.hasInvalidated());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requery",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCount",
            args = {}
        )
    })
    public void testRequery() {
        final String DELETE = "DELETE FROM " + TABLE_NAME + " WHERE number_1 =";
        final String DELETE_1 = DELETE + "1;";
        final String DELETE_2 = DELETE + "2;";
        mDatabase.execSQL(DELETE_1);
        SQLiteCursor cursor = getCursor();
        MockObserver observer = new MockObserver();
        cursor.registerDataSetObserver(observer);
        assertEquals(TEST_COUNT - 1, cursor.getCount());
        assertFalse(observer.hasChanged());
        mDatabase.execSQL(DELETE_2);
        assertEquals(TEST_COUNT - 1, cursor.getCount());
        assertTrue(cursor.requery());
        assertEquals(TEST_COUNT - 2, cursor.getCount());
        assertTrue(observer.hasChanged());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColumnIndex",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColumnNames",
            args = {}
        )
    })
    public void testGetColumnIndex() {
        SQLiteCursor cursor = getCursor();
        for (int i = 0; i < COLUMNS.length; i++) {
            assertEquals(i, cursor.getColumnIndex(COLUMNS[i]));
        }
        assertTrue(Arrays.equals(COLUMNS, cursor.getColumnNames()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDatabase",
        args = {}
    )
    public void testGetDatabase() {
        SQLiteCursor cursor = getCursor();
        assertSame(mDatabase, cursor.getDatabase());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setSelectionArguments",
        args = {java.lang.String[].class}
    )
    public void testSetSelectionArguments() {
        final String SELECTION = "_id > ?";
        int TEST_ARG1 = 2;
        int TEST_ARG2 = 5;
        SQLiteCursor cursor = (SQLiteCursor) mDatabase.query(TABLE_NAME, null, SELECTION,
                new String[] { Integer.toString(TEST_ARG1) }, null, null, null);
        assertEquals(TEST_COUNT - TEST_ARG1, cursor.getCount());
        cursor.setSelectionArguments(new String[] { Integer.toString(TEST_ARG2) });
        cursor.requery();
        assertEquals(TEST_COUNT - TEST_ARG2, cursor.getCount());
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onMove",
        args = {int.class, int.class}
    )
    public void testOnMove() {
    }
    private void createTable(String tableName, String columnNames) {
        String sql = "Create TABLE " + tableName + " (_id INTEGER PRIMARY KEY, "
                + columnNames + " );";
        mDatabase.execSQL(sql);
    }
    private void addValuesIntoTable(String tableName, int start, int end) {
        for (int i = start; i <= end; i++) {
            mDatabase.execSQL("INSERT INTO " + tableName + "(number_1) VALUES ('" + i + "');");
        }
    }
    private SQLiteCursor getCursor() {
        SQLiteCursor cursor = (SQLiteCursor) mDatabase.query(TABLE_NAME, null, null,
                null, null, null, null);
        return cursor;
    }
    private class MockObserver extends DataSetObserver {
        private boolean mHasChanged = false;
        private boolean mHasInvalidated = false;
        @Override
        public void onChanged() {
            super.onChanged();
            mHasChanged = true;
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mHasInvalidated = true;
        }
        protected void resetStatus() {
            mHasChanged = false;
            mHasInvalidated = false;
        }
        protected boolean hasChanged() {
            return mHasChanged;
        }
        protected boolean hasInvalidated () {
            return mHasInvalidated;
        }
    }
    private class MockCursorWindow extends CursorWindow {
        private boolean mIsClosed = false;
        public MockCursorWindow(boolean localWindow) {
            super(localWindow);
        }
        @Override
        public void close() {
            super.close();
            mIsClosed = true;
        }
        public boolean isClosed() {
            return mIsClosed;
        }
    }
}
