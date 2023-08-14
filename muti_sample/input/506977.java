@TestTargetClass(android.database.CursorWrapper.class)
public class CursorWrapperTest extends DatabaseCursorTest {
    private static final String FIRST_NUMBER = "123";
    private static final String SECOND_NUMBER = "5555";
    private static final int TESTVALUE1 = 199;
    private static final int TESTVALUE2 = 200;
    private static final String[] NUMBER_PROJECTION = new String[] {
        "_id",             
        "number"           
    };
    private static final int DEFAULT_RECORD_COUNT = 2;
    private static final int DEFAULT_COLUMN_COUNT = 2;
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    private Cursor mCursor;
    private static final int CURRENT_DATABASE_VERSION = 42;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTestType(TYPE_CURSORWRAPPER);
        setupDatabase();
    }
    @Override
    protected void tearDown() throws Exception {
        closeDatabase();
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test close() and isClosed() and the constructor method.",
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test close() and isClosed() and the constructor method.",
            method = "isClosed",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test close() and isClosed() and the constructor method.",
            method = "requery",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test close() and isClosed() and the constructor method.",
            method = "CursorWrapper",
            args = {android.database.Cursor.class}
        )
    })
    public void testConstrucotorAndClose() {
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        assertTrue(cursorWrapper.requery());
        cursorWrapper.deactivate();
        cursorWrapper.move(1);
        assertEquals(DEFAULT_RECORD_COUNT, cursorWrapper.getCount());
        assertFalse(cursorWrapper.isClosed());
        assertTrue(cursorWrapper.requery());
        cursorWrapper.close();
        assertTrue(cursorWrapper.isClosed());
        assertFalse(cursorWrapper.requery());
    }
    private Cursor getCursor() {
        Cursor cursor = mDatabase.query("test1", NUMBER_PROJECTION, null, null, null, null, null);
        return cursor;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "requery",
            args = {}
        )
    })
    public void testGetCount() {
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        int defaultCount = cursorWrapper.getCount();
        addWithValue(mDatabase, TESTVALUE1);
        int expected = defaultCount + 1;
        assertTrue(cursorWrapper.requery());
        assertEquals(expected, cursorWrapper.getCount());
        addWithValue(mDatabase, TESTVALUE2);
        expected += 1;
        assertTrue(cursorWrapper.requery());
        assertEquals(expected, cursorWrapper.getCount());
        deleteWithValue(mDatabase, TESTVALUE1);
        assertTrue(cursorWrapper.requery());
        assertEquals(defaultCount + 1, cursorWrapper.getCount());
        deleteWithValue(mDatabase, TESTVALUE2);
        assertTrue(cursorWrapper.requery());
        assertEquals(defaultCount, cursorWrapper.getCount());
        deleteAllRecords(mDatabase);
        assertTrue(cursorWrapper.requery());
        assertEquals(0, cursorWrapper.getCount());
        cursorWrapper.close();
        assertFalse(cursorWrapper.requery());
        rebuildDatabase();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "deactivate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "registerDataSetObserver",
            args = {android.database.DataSetObserver.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unregisterDataSetObserver",
            args = {android.database.DataSetObserver.class}
        )
    })
    public void testDeactivate() throws IllegalStateException {
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        MockObserver observer = new MockObserver();
        try{
            cursorWrapper.unregisterDataSetObserver(observer);
            fail("testUnregisterDataSetObserver failed");
        }catch(IllegalStateException e){
        }
        assertFalse(observer.hasInvalidated());
        cursorWrapper.moveToLast();
        cursorWrapper.deactivate();
        assertFalse(observer.hasInvalidated());
        assertTrue(cursorWrapper.requery());
        cursorWrapper.registerDataSetObserver(observer);
        assertFalse(observer.hasInvalidated());
        cursorWrapper.moveToLast();
        assertEquals(Integer.parseInt(SECOND_NUMBER), cursorWrapper.getInt(1));
        cursorWrapper.deactivate();
        assertTrue(observer.hasInvalidated());
        try {
            cursorWrapper.getInt(1);
            fail("After deactivating, cursor cannot execute getting value operations.");
        } catch (StaleDataException e) {
        }
        try{
            cursorWrapper.registerDataSetObserver(observer);
            fail("testRegisterDataSetObserver failed");
        }catch(IllegalStateException e){
        }
        cursorWrapper.unregisterDataSetObserver(observer);
        observer.resetStatus();
        assertFalse(observer.hasInvalidated());
        cursorWrapper.moveToLast();
        cursorWrapper.deactivate();
        assertFalse(observer.hasInvalidated());
        try{
            cursorWrapper.unregisterDataSetObserver(observer);
            fail("testUnregisterDataSetObserver failed");
        }catch(IllegalStateException e){
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getColumnCount()",
            method = "getColumnCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getColumnIndex(String)",
            method = "getColumnIndex",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getColumnIndexOrThrow(String)",
            method = "getColumnIndexOrThrow",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getColumnName(int).",
            method = "getColumnName",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getColumnNames().",
            method = "getColumnNames",
            args = {}
        )
    })
    public void testGettingColumnInfos() {
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        assertEquals(DEFAULT_COLUMN_COUNT, cursorWrapper.getColumnCount());
        assertEquals(0, cursorWrapper.getColumnIndex("_id"));
        assertEquals(1, cursorWrapper.getColumnIndex("number"));
        assertEquals(-1, cursorWrapper.getColumnIndex("NON_EXISTENCE"));
        assertEquals(0, cursorWrapper.getColumnIndexOrThrow("_id"));
        assertEquals(1, cursorWrapper.getColumnIndexOrThrow("number"));
        try {
            cursorWrapper.getColumnIndexOrThrow("NON_EXISTENCE");
            fail("getColumnIndexOrThrow should throws IllegalArgumentException if the column"
                    + "does not exist");
        } catch (IllegalArgumentException e) {
        }
        assertEquals("_id", cursorWrapper.getColumnName(0));
        assertEquals("number", cursorWrapper.getColumnName(1));
        String[] columnNames = cursorWrapper.getColumnNames();
        assertEquals(DEFAULT_COLUMN_COUNT, cursorWrapper.getColumnCount());
        assertEquals("_id", columnNames[0]);
        assertEquals("number", columnNames[1]);
        cursorWrapper.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "moveToFirst",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "isAfterLast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "isBeforeFirst",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "isFirst",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "isLast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "moveToLast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "move",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "moveToPosition",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "moveToNext",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "getPosition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test cursor positioning related fuctions.",
            method = "moveToPrevious",
            args = {}
        )
    })
    public void testPositioning() {
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        assertTrue(cursorWrapper.isBeforeFirst());
        assertEquals(-1, cursorWrapper.getPosition());
        assertFalse(cursorWrapper.isFirst());
        assertTrue(cursorWrapper.moveToNext());
        assertEquals(0, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.isFirst());
        assertFalse(cursorWrapper.isLast());
        assertTrue(cursorWrapper.moveToNext());
        assertEquals(1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.isLast());
        assertFalse(cursorWrapper.moveToNext());
        assertFalse(cursorWrapper.isLast());
        assertTrue(cursorWrapper.isAfterLast());
        assertEquals(2, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.move(-1));
        assertEquals(1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.move(-1));
        assertEquals(0, cursorWrapper.getPosition());
        assertFalse(cursorWrapper.move(-1));
        assertEquals(-1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.move(2));
        assertEquals(1, cursorWrapper.getPosition());
        assertFalse(cursorWrapper.move(1));
        assertTrue(cursorWrapper.isAfterLast());
        assertEquals(2, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.moveToPrevious());
        assertEquals(1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.moveToPrevious());
        assertEquals(0, cursorWrapper.getPosition());
        assertFalse(cursorWrapper.moveToPrevious());
        assertEquals(-1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.isBeforeFirst());
        assertFalse(cursorWrapper.moveToPosition(2));
        assertEquals(2, cursorWrapper.getPosition());
        assertFalse(cursorWrapper.moveToPosition(-1));
        assertEquals(-1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.moveToPosition(1));
        assertEquals(1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.moveToPosition(0));
        assertEquals(0, cursorWrapper.getPosition());
        assertFalse(cursorWrapper.isLast());
        assertTrue(cursorWrapper.moveToLast());
        assertEquals(1, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.isLast());
        assertFalse(cursorWrapper.isFirst());
        assertTrue(cursorWrapper.moveToFirst());
        assertEquals(0, cursorWrapper.getPosition());
        assertTrue(cursorWrapper.isFirst());
        cursorWrapper.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getDouble",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getFloat",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getInt",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getLong",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getShort",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getString",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getBlob",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "getColumnCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test value getting methods.",
            method = "isNull",
            args = {int.class}
        )
    })
    public void testGettingValues() {
        final byte NUMBER_BLOB_UNIT = 99;
        final String STRING_TEXT = "Test String";
        final String STRING_TEXT2 = "Test String2";
        final double NUMBER_DOUBLE = Double.MAX_VALUE;
        final double NUMBER_FLOAT = (float) NUMBER_DOUBLE;
        final long NUMBER_LONG_INTEGER = (long) 0xaabbccddffL;
        final long NUMBER_INTEGER = (int) NUMBER_LONG_INTEGER;
        final long NUMBER_SHORT = (short) NUMBER_INTEGER;
        assertTrue(NUMBER_DOUBLE != (double) NUMBER_FLOAT);
        assertTrue(NUMBER_LONG_INTEGER != (long) NUMBER_INTEGER);
        assertTrue(NUMBER_LONG_INTEGER != (short) NUMBER_SHORT);
        assertTrue(NUMBER_INTEGER != (int) NUMBER_SHORT);
        mDatabase.execSQL("CREATE TABLE test2 (_id INTEGER PRIMARY KEY, string_text TEXT,"
                + "double_number REAL, int_number INTEGER, blob_data BLOB);");
        Object[] args = new Object[4];
        byte[] originalBlob = new byte[1000];
        Arrays.fill(originalBlob, NUMBER_BLOB_UNIT);
        args[0] = STRING_TEXT;
        args[1] = NUMBER_DOUBLE;
        args[2] = NUMBER_LONG_INTEGER;
        args[3] = originalBlob;
        String sql = "INSERT INTO test2 (string_text, double_number, int_number, blob_data)"
                + "VALUES (?,?,?,?)";
        mDatabase.execSQL(sql, args);
        Cursor cursor = mDatabase.query("test2", null, null, null, null, null, null);
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        assertEquals(DEFAULT_COLUMN_COUNT, cursorWrapper.getColumnCount());
        cursorWrapper.close();
        cursorWrapper = new CursorWrapper(cursor);
        assertEquals(5, cursorWrapper.getColumnCount());
        cursorWrapper.moveToNext();
        int columnBlob = cursorWrapper.getColumnIndexOrThrow("blob_data");
        int columnString = cursorWrapper.getColumnIndexOrThrow("string_text");
        int columnDouble = cursorWrapper.getColumnIndexOrThrow("double_number");
        int columnInteger = cursorWrapper.getColumnIndexOrThrow("int_number");
        byte[] targetBlob = cursorWrapper.getBlob(columnBlob);
        assertTrue(Arrays.equals(originalBlob, targetBlob));
        assertEquals(STRING_TEXT, cursorWrapper.getString(columnString));
        assertEquals(NUMBER_DOUBLE, cursorWrapper.getDouble(columnDouble), 0.000000000001);
        assertEquals(NUMBER_FLOAT, cursorWrapper.getFloat(columnDouble), 0.000000000001f);
        assertEquals(NUMBER_LONG_INTEGER, cursorWrapper.getLong(columnInteger));
        assertEquals(NUMBER_INTEGER, cursorWrapper.getInt(columnInteger));
        assertEquals(NUMBER_SHORT, cursorWrapper.getShort(columnInteger));
        assertFalse(cursorWrapper.isNull(columnBlob));
        sql = "INSERT INTO test2 (string_text) VALUES ('" + STRING_TEXT2 + "')";
        mDatabase.execSQL(sql);
        cursorWrapper.close();
        cursor = mDatabase.query("test2", null, null, null, null, null, null);
        cursorWrapper = new CursorWrapper(cursor);
        cursorWrapper.moveToPosition(1);
        assertTrue(cursorWrapper.isNull(columnBlob));
        mDatabase.execSQL("DROP TABLE test2");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Tets getExtras().",
        method = "getExtras",
        args = {}
    )
    public void testGetExtras() {
        CursorWrapper cursor = new CursorWrapper(getCursor());
        Bundle bundle = cursor.getExtras();
        assertSame(Bundle.EMPTY, bundle);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test copyStringToBuffer(int, CharArrayBuffer).",
        method = "copyStringToBuffer",
        args = {int.class, android.database.CharArrayBuffer.class}
    )
    public void testCopyStringToBuffer() {
        CharArrayBuffer charArrayBuffer = new CharArrayBuffer(1000);
        Cursor cursor = getCursor();
        CursorWrapper cursorWrapper = new CursorWrapper(cursor);
        cursorWrapper.moveToFirst();
        assertEquals(0, charArrayBuffer.sizeCopied);
        cursorWrapper.copyStringToBuffer(0, charArrayBuffer);
        String string = new String(charArrayBuffer.data);
        assertTrue(charArrayBuffer.sizeCopied > 0);
        assertEquals("1", string.substring(0, charArrayBuffer.sizeCopied));
        cursorWrapper.copyStringToBuffer(1, charArrayBuffer);
        string = new String(charArrayBuffer.data);
        assertTrue(charArrayBuffer.sizeCopied > 0);
        assertEquals(FIRST_NUMBER, string.substring(0, charArrayBuffer.sizeCopied));
        cursorWrapper.moveToNext();
        cursorWrapper.copyStringToBuffer(1, charArrayBuffer);
        string = new String(charArrayBuffer.data);
        assertTrue(charArrayBuffer.sizeCopied > 0);
        assertEquals(SECOND_NUMBER, string.substring(0, charArrayBuffer.sizeCopied));
        cursorWrapper.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test respond(Bundle).",
        method = "respond",
        args = {android.os.Bundle.class}
    )
    public void testRespond() {
        Bundle b = new Bundle();
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        Bundle bundle = cursorWrapper.respond(b);
        assertSame(Bundle.EMPTY, bundle);
        cursorWrapper.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getWantsAllOnMoveCalls()",
        method = "getWantsAllOnMoveCalls",
        args = {}
    )
    public void testGetWantsAllOnMoveCalls() {
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        assertFalse(cursorWrapper.getWantsAllOnMoveCalls());
        cursorWrapper.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test registerContentObserver and unregisterContentObserver.",
            method = "registerContentObserver",
            args = {android.database.ContentObserver.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test registerContentObserver and unregisterContentObserver.",
            method = "unregisterContentObserver",
            args = {android.database.ContentObserver.class}
        )
    })
    public void testContentObsererOperations() throws IllegalStateException {
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        MockContentObserver observer = new MockContentObserver(null);
        try{
            cursorWrapper.unregisterContentObserver(observer);
            fail("testUnregisterContentObserver failed");
        }catch(IllegalStateException e){
            assertTrue(true);
        }
        cursorWrapper.registerContentObserver(observer);
        try{
            cursorWrapper.registerContentObserver(observer);
            fail("testRegisterContentObserver failed");
        }catch(IllegalStateException e){
        }
        cursorWrapper.unregisterContentObserver(observer);
        cursorWrapper.registerContentObserver(observer);
        cursorWrapper.unregisterContentObserver(observer);
        try{
            cursorWrapper.unregisterContentObserver(observer);
            fail("testUnregisterContentObserver failed");
        }catch(IllegalStateException e){
        }
        cursorWrapper.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setNotificationUri().",
        method = "setNotificationUri",
        args = {android.content.ContentResolver.class, android.net.Uri.class}
    )
    public void testSetNotificationUri() {
        String MOCK_URI = "content:
        CursorWrapper cursorWrapper = new CursorWrapper(getCursor());
        MockContentResolver contentResolver = new MockContentResolver(null);
        cursorWrapper.setNotificationUri(contentResolver, Uri.parse(MOCK_URI));
    }
    private class MockContentResolver extends ContentResolver {
        public MockContentResolver(Context context) {
            super(context);
        }
        @Override
        protected IContentProvider acquireProvider(Context c, String name) {
            return null;
        }
        @Override
        public boolean releaseProvider(IContentProvider icp) {
            return false;
        }
    }
    private class MockContentObserver extends ContentObserver {
        public MockContentObserver(Handler handler) {
            super(handler);
        }
    }
    private void deleteWithValue(SQLiteDatabase database, int value) {
        database.execSQL("DELETE FROM test1 WHERE number = " + value + ";");
    }
    private void addWithValue(SQLiteDatabase database, int value) {
        database.execSQL("INSERT INTO test1 (number) VALUES ('" + value + "');");
    }
    private void deleteAllRecords(SQLiteDatabase database) {
        database.delete("test1", null, null);
    }
    private void setupDatabase() {
        File dbDir = getContext().getDir("tests", Context.MODE_PRIVATE);
        mDatabaseFile = new File(dbDir, "database_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabase);
        mDatabase.setVersion(CURRENT_DATABASE_VERSION);
        mDatabase.execSQL("CREATE TABLE test1 (_id INTEGER PRIMARY KEY, number TEXT);");
        mDatabase.execSQL("INSERT INTO test1 (number) VALUES ('" + FIRST_NUMBER + "');");
        mDatabase.execSQL("INSERT INTO test1 (number) VALUES ('" + SECOND_NUMBER + "');");
        mCursor = getCursor();
    }
    private void closeDatabase() {
        if (null != mCursor) {
            mCursor.close();
            mCursor = null;
        }
        mDatabase.close();
        mDatabaseFile.delete();
    }
    private void rebuildDatabase() {
        closeDatabase();
        setupDatabase();
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
}