@TestTargetClass(InsertHelper.class)
public class DatabaseUtils_InsertHelperTest extends AndroidTestCase {
    private static final String TEST_TABLE_NAME = "test";
    private static final String DATABASE_NAME = "database_test.db";
    private SQLiteDatabase mDatabase;
    private InsertHelper mInsertHelper;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getContext().deleteDatabase(DATABASE_NAME);
        mDatabase = getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        assertNotNull(mDatabase);
        mInsertHelper = new InsertHelper(mDatabase, TEST_TABLE_NAME);
    }
    @Override
    protected void tearDown() throws Exception {
        mInsertHelper.close();
        mDatabase.close();
        getContext().deleteDatabase(DATABASE_NAME);
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "InsertHelper",
        args = {android.database.sqlite.SQLiteDatabase.class, java.lang.String.class}
    )
    public void testConstructor() {
        new InsertHelper(mDatabase, TEST_TABLE_NAME);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "close",
        args = {}
    )
    public void testClose() {
        mInsertHelper.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getColumnIndex",
        args = {String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Javadoc does not specify exceptions thrown.")
    public void testGetColumnIndex() {
        mDatabase.execSQL("CREATE TABLE " + TEST_TABLE_NAME + " (_id INTEGER PRIMARY KEY, " +
                "name TEXT, age INTEGER, address TEXT);");
        assertEquals(1, mInsertHelper.getColumnIndex("_id"));
        assertEquals(2, mInsertHelper.getColumnIndex("name"));
        assertEquals(3, mInsertHelper.getColumnIndex("age"));
        assertEquals(4, mInsertHelper.getColumnIndex("address"));
        try {
            mInsertHelper.getColumnIndex("missing_column");
            fail("Should throw exception (column does not exist)");
        } catch (IllegalArgumentException expected) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "prepareForInsert",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "execute",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bindNull",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "insert",
            args = {android.content.ContentValues.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Javadoc does not specify exceptions thrown.")
    public void testInsert() {
        mDatabase.execSQL("CREATE TABLE " + TEST_TABLE_NAME + "(_id INTEGER PRIMARY KEY," +
                " boolean_value INTEGER, int_value INTEGER, long_value INTEGER," +
                " double_value DOUBLE, float_value DOUBLE, string_value TEXT," +
                " blob_value BLOB, null_value TEXT);");
        final int booleanValueIndex = 1;
        final int intValueIndex     = 2;
        final int longValueIndex    = 3;
        final int doubleValueIndex  = 4;
        final int floatValueIndex   = 5;
        final int stringValueIndex  = 6;
        final int blobValueIndex    = 7;
        final int nullValueIndex    = 8;
        final String[] projection = new String[] {
            "_id",                    
            "boolean_value",          
            "int_value",              
            "long_value",             
            "double_value",           
            "float_value",            
            "string_value",           
            "blob_value",             
            "null_value"              
        };
        Cursor cursor = mDatabase.query(TEST_TABLE_NAME, projection, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
        try {
            mInsertHelper.execute();
            fail("Should throw exception (execute without prepare)");
        } catch (IllegalStateException expected) {
        }
        mInsertHelper.prepareForInsert();
        mInsertHelper.bind(mInsertHelper.getColumnIndex("boolean_value"), true);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("int_value"), 10);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("long_value"), 1000L);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("double_value"), 123.456);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("float_value"), 1.0f);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("string_value"), "test insert");
        byte[] blob = new byte[] { '1', '2', '3' };
        mInsertHelper.bind(mInsertHelper.getColumnIndex("blob_value"), blob);
        mInsertHelper.bindNull(mInsertHelper.getColumnIndex("null_value"));
        long id = mInsertHelper.execute();
        assertEquals(1, id);
        cursor = mDatabase.query(TEST_TABLE_NAME, projection, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(1, cursor.getInt(booleanValueIndex));
        assertEquals(10, cursor.getInt(intValueIndex));
        assertEquals(1000L, cursor.getLong(longValueIndex));
        assertEquals(123.456, cursor.getDouble(doubleValueIndex));
        assertEquals(1.0f, cursor.getFloat(floatValueIndex));
        assertEquals("test insert", cursor.getString(stringValueIndex));
        byte[] value = cursor.getBlob(blobValueIndex);
        MoreAsserts.assertEquals(blob, value);
        assertNull(cursor.getString(nullValueIndex));
        cursor.close();
        mInsertHelper.prepareForInsert();
        mInsertHelper.bind(mInsertHelper.getColumnIndex("_id"), id);
        assertEquals(-1, mInsertHelper.execute());
        ContentValues values = new ContentValues();
        values.put("boolean_value", false);
        values.put("int_value", 123);
        values.put("long_value", 987654L);
        values.put("double_value", 654.321);
        values.put("float_value", 21.1f);
        values.put("string_value", "insert another row");
        values.put("blob_value", blob);
        values.putNull("null_value");
        id = mInsertHelper.insert(values);
        assertEquals(2, id);
        cursor = mDatabase.query(TEST_TABLE_NAME, projection, "_id = " + id,
                null, null, null, null);
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(0, cursor.getInt(booleanValueIndex));
        assertEquals(123, cursor.getInt(intValueIndex));
        assertEquals(987654L, cursor.getLong(longValueIndex));
        assertEquals(654.321, cursor.getDouble(doubleValueIndex));
        assertEquals(21.1f, cursor.getFloat(floatValueIndex));
        assertEquals("insert another row", cursor.getString(stringValueIndex));
        value = cursor.getBlob(blobValueIndex);
        MoreAsserts.assertEquals(blob, value);
        assertNull(cursor.getString(nullValueIndex));
        cursor.close();
        values.put("_id", id);
        assertEquals(-1, mInsertHelper.insert(values));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "prepareForReplace",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "execute",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bind",
            args = {int.class, byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "bindNull",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "replace",
            args = {android.content.ContentValues.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Javadoc does not specify exceptions thrown.")
    public void testReplace() {
        mDatabase.execSQL("CREATE TABLE " + TEST_TABLE_NAME + "(_id INTEGER PRIMARY KEY," +
                " boolean_value INTEGER, int_value INTEGER, long_value INTEGER," +
                " double_value DOUBLE, float_value DOUBLE, string_value TEXT," +
                " blob_value BLOB, null_value TEXT);");
        final int booleanValueIndex = 1;
        final int intValueIndex     = 2;
        final int longValueIndex    = 3;
        final int doubleValueIndex  = 4;
        final int floatValueIndex   = 5;
        final int stringValueIndex  = 6;
        final int blobValueIndex    = 7;
        final int nullValueIndex    = 8;
        final String[] projection = new String[] {
            "_id",                    
            "boolean_value",          
            "int_value",              
            "long_value",             
            "double_value",           
            "float_value",            
            "string_value",           
            "blob_value",             
            "null_value"              
        };
        Cursor cursor = mDatabase.query(TEST_TABLE_NAME, projection, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
        mInsertHelper.prepareForReplace();
        mInsertHelper.bind(mInsertHelper.getColumnIndex("boolean_value"), true);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("int_value"), 10);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("long_value"), 1000L);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("double_value"), 123.456);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("float_value"), 1.0f);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("string_value"), "test insert");
        byte[] blob = new byte[] { '1', '2', '3' };
        mInsertHelper.bind(mInsertHelper.getColumnIndex("blob_value"), blob);
        mInsertHelper.bindNull(mInsertHelper.getColumnIndex("null_value"));
        long id = mInsertHelper.execute();
        assertEquals(1, id);
        cursor = mDatabase.query(TEST_TABLE_NAME, projection, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(1, cursor.getInt(booleanValueIndex));
        assertEquals(10, cursor.getInt(intValueIndex));
        assertEquals(1000L, cursor.getLong(longValueIndex));
        assertEquals(123.456, cursor.getDouble(doubleValueIndex));
        assertEquals(1.0f, cursor.getFloat(floatValueIndex));
        assertEquals("test insert", cursor.getString(stringValueIndex));
        byte[] value = cursor.getBlob(blobValueIndex);
        MoreAsserts.assertEquals(blob, value);
        assertNull(cursor.getString(nullValueIndex));
        cursor.close();
        mInsertHelper.prepareForReplace();
        mInsertHelper.bind(mInsertHelper.getColumnIndex("_id"), id);
        mInsertHelper.bind(mInsertHelper.getColumnIndex("int_value"), 42);
        mInsertHelper.execute();
        cursor = mDatabase.query(TEST_TABLE_NAME, projection, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(42, cursor.getInt(intValueIndex));
        assertNull(cursor.getString(stringValueIndex));
        cursor.close();
        mInsertHelper.prepareForReplace();
        mInsertHelper.bind(mInsertHelper.getColumnIndex("_id"), "illegal_id");
        assertEquals(-1, mInsertHelper.execute());
        ContentValues values = new ContentValues();
        values.put("_id", id);
        values.put("boolean_value", false);
        values.put("int_value", 123);
        values.put("long_value", 987654L);
        values.put("double_value", 654.321);
        values.put("float_value", 21.1f);
        values.put("string_value", "replace the row");
        values.put("blob_value", blob);
        values.putNull("null_value");
        id = mInsertHelper.replace(values);
        assertEquals(1, id);
        cursor = mDatabase.query(TEST_TABLE_NAME, projection, null, null, null, null, null);
        assertEquals(1, cursor.getCount());
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(0, cursor.getInt(booleanValueIndex));
        assertEquals(123, cursor.getInt(intValueIndex));
        assertEquals(987654L, cursor.getLong(longValueIndex));
        assertEquals(654.321, cursor.getDouble(doubleValueIndex));
        assertEquals(21.1f, cursor.getFloat(floatValueIndex));
        assertEquals("replace the row", cursor.getString(stringValueIndex));
        value = cursor.getBlob(blobValueIndex);
        MoreAsserts.assertEquals(blob, value);
        assertNull(cursor.getString(nullValueIndex));
        cursor.close();
        values.put("_id", "illegal_id");
        assertEquals(-1, mInsertHelper.replace(values));
    }
}
