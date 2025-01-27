@TestTargetClass(ContentResolver.class)
public class ContentResolverTest extends AndroidTestCase {
    private final static String COLUMN_ID_NAME = "_id";
    private final static String COLUMN_KEY_NAME = "key";
    private final static String COLUMN_VALUE_NAME = "value";
    private static final String AUTHORITY = "ctstest";
    private static final Uri TABLE1_URI = Uri.parse("content:
    private static final Uri TABLE2_URI = Uri.parse("content:
    private static final Account ACCOUNT = new Account("cts", "cts");
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final int VALUE1 = 1;
    private static final int VALUE2 = 2;
    private static final int VALUE3 = 3;
    private static final String TEST_PACKAGE_NAME = "com.android.cts.stub";
    private Context mContext;
    private ContentResolver mContentResolver;
    private Cursor mCursor;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mContentResolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY_NAME, KEY1);
        values.put(COLUMN_VALUE_NAME, VALUE1);
        mContentResolver.insert(TABLE1_URI, values);
        values.put(COLUMN_KEY_NAME, KEY2);
        values.put(COLUMN_VALUE_NAME, VALUE2);
        mContentResolver.insert(TABLE1_URI, values);
        values.put(COLUMN_KEY_NAME, KEY3);
        values.put(COLUMN_VALUE_NAME, VALUE3);
        mContentResolver.insert(TABLE1_URI, values);
    }
    @Override
    protected void tearDown() throws Exception {
        mContentResolver.delete(TABLE1_URI, null, null);
        if ( null != mCursor && !mCursor.isClosed() ) {
            mCursor.close();
        }
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ContentResolver",
        args = {android.content.Context.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        assertNotNull(mContentResolver);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getType",
        args = {android.net.Uri.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#getType(Uri) when the input Uri is null")
    public void testGetType() {
        String type1 = mContentResolver.getType(TABLE1_URI);
        assertTrue(type1.startsWith(ContentResolver.CURSOR_DIR_BASE_TYPE, 0));
        String type2 = mContentResolver.getType(TABLE2_URI);
        assertTrue(type2.startsWith(ContentResolver.CURSOR_DIR_BASE_TYPE, 0));
        Uri invalidUri = Uri.parse("abc");
        assertNull(mContentResolver.getType(invalidUri));
        try {
            mContentResolver.getType(null);
            fail("did not throw NullPointerException when Uri is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "query",
        args = {android.net.Uri.class, java.lang.String[].class, java.lang.String.class,
                java.lang.String[].class, java.lang.String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#query(Uri, String[], String, String[], String) when the input " +
            "param Uri is null")
    public void testQuery() {
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(3, mCursor.getCount());
        assertEquals(3, mCursor.getColumnCount());
        mCursor.moveToLast();
        assertEquals(3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(KEY3, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(VALUE3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.moveToPrevious();
        assertEquals(2, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(KEY2, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(VALUE2, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        String selection = COLUMN_ID_NAME + "=1";
        mCursor = mContentResolver.query(TABLE1_URI, null, selection, null, null);
        assertNotNull(mCursor);
        assertEquals(1, mCursor.getCount());
        assertEquals(3, mCursor.getColumnCount());
        mCursor.moveToFirst();
        assertEquals(1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(KEY1, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(VALUE1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        selection = COLUMN_KEY_NAME + "=\"" + KEY3 + "\"";
        mCursor = mContentResolver.query(TABLE1_URI, null, selection, null, null);
        assertNotNull(mCursor);
        assertEquals(1, mCursor.getCount());
        assertEquals(3, mCursor.getColumnCount());
        mCursor.moveToFirst();
        assertEquals(3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(KEY3, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(VALUE3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        try {
            mContentResolver.query(null, null, null, null, null);
            fail("did not throw NullPointerException when uri is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "openInputStream",
        args = {android.net.Uri.class}
    )
    public void testOpenInputStream() throws IOException {
        final Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                ":
        InputStream is = mContentResolver.openInputStream(uri);
        assertNotNull(is);
        is.close();
        final Uri invalidUri = Uri.parse("abc");
        try {
            mContentResolver.openInputStream(invalidUri);
            fail("did not throw FileNotFoundException when uri is invalid.");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOutputStream",
            args = {android.net.Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOutputStream",
            args = {android.net.Uri.class, java.lang.String.class}
        )
    })
    public void testOpenOutputStream() throws IOException {
        Uri uri = Uri.parse(ContentResolver.SCHEME_FILE + ":
                getContext().getCacheDir().getAbsolutePath() +
                "/temp.jpg");
        OutputStream os = mContentResolver.openOutputStream(uri);
        assertNotNull(os);
        os.close();
        os = mContentResolver.openOutputStream(uri, "wa");
        assertNotNull(os);
        os.close();
        uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                ":
        try {
            mContentResolver.openOutputStream(uri);
            fail("did not throw FileNotFoundException when scheme is not accepted.");
        } catch (FileNotFoundException e) {
        }
        try {
            mContentResolver.openOutputStream(uri, "w");
            fail("did not throw FileNotFoundException when scheme is not accepted.");
        } catch (FileNotFoundException e) {
        }
        Uri invalidUri = Uri.parse("abc");
        try {
            mContentResolver.openOutputStream(invalidUri);
            fail("did not throw FileNotFoundException when uri is invalid.");
        } catch (FileNotFoundException e) {
        }
        try {
            mContentResolver.openOutputStream(invalidUri, "w");
            fail("did not throw FileNotFoundException when uri is invalid.");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "openAssetFileDescriptor",
        args = {android.net.Uri.class, java.lang.String.class}
    )
    public void testOpenAssetFileDescriptor() throws IOException {
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                ":
        AssetFileDescriptor afd = mContentResolver.openAssetFileDescriptor(uri, "r");
        assertNotNull(afd);
        afd.close();
        try {
            mContentResolver.openAssetFileDescriptor(uri, "d");
            fail("did not throw FileNotFoundException when mode is unknown.");
        } catch (FileNotFoundException e) {
        }
        Uri invalidUri = Uri.parse("abc");
        try {
            mContentResolver.openAssetFileDescriptor(invalidUri, "r");
            fail("did not throw FileNotFoundException when uri is invalid.");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "openFileDescriptor",
        args = {android.net.Uri.class, java.lang.String.class}
    )
    public void testOpenFileDescriptor() throws IOException {
        Uri uri = Uri.parse(ContentResolver.SCHEME_FILE + ":
                getContext().getCacheDir().getAbsolutePath() +
                "/temp.jpg");
        ParcelFileDescriptor pfd = mContentResolver.openFileDescriptor(uri, "w");
        assertNotNull(pfd);
        pfd.close();
        try {
            mContentResolver.openFileDescriptor(uri, "d");
            fail("did not throw FileNotFoundException when mode is unknown.");
        } catch (FileNotFoundException e) {
        }
        Uri invalidUri = Uri.parse("abc");
        try {
            mContentResolver.openFileDescriptor(invalidUri, "w");
            fail("did not throw FileNotFoundException when uri is invalid.");
        } catch (FileNotFoundException e) {
        }
        uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                ":
        try {
            mContentResolver.openFileDescriptor(uri, "w");
            fail("did not throw FileNotFoundException when scheme is not accepted.");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "insert",
        args = {android.net.Uri.class, android.content.ContentValues.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#insert(Uri, ContentValues) when the input Uri are null")
    public void testInsert() {
        String key4 = "key4";
        String key5 = "key5";
        int value4 = 4;
        int value5 = 5;
        String key4Selection = COLUMN_KEY_NAME + "=\"" + key4 + "\"";
        mCursor = mContentResolver.query(TABLE1_URI, null, key4Selection, null, null);
        assertEquals(0, mCursor.getCount());
        mCursor.close();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY_NAME, key4);
        values.put(COLUMN_VALUE_NAME, value4);
        Uri uri = mContentResolver.insert(TABLE1_URI, values);
        assertNotNull(uri);
        mCursor = mContentResolver.query(TABLE1_URI, null, key4Selection, null, null);
        assertNotNull(mCursor);
        assertEquals(1, mCursor.getCount());
        mCursor.moveToFirst();
        assertEquals(4, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key4, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value4, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        values.put(COLUMN_KEY_NAME, key5);
        values.put(COLUMN_VALUE_NAME, value5);
        uri = mContentResolver.insert(TABLE1_URI, values);
        assertNotNull(uri);
        mCursor = mContentResolver.query(uri, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(1, mCursor.getCount());
        mCursor.moveToLast();
        assertEquals(5, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key5, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value5, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        try {
            mContentResolver.insert(null, values);
            fail("did not throw NullPointerException when uri is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "bulkInsert",
        args = {android.net.Uri.class, android.content.ContentValues[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#bulkInsert(Uri, ContentValues[]) when the input Uri are null")
    public void testBulkInsert() {
        String key4 = "key4";
        String key5 = "key5";
        int value4 = 4;
        int value5 = 5;
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(3, mCursor.getCount());
        mCursor.close();
        ContentValues[] cvs = new ContentValues[2];
        cvs[0] = new ContentValues();
        cvs[0].put(COLUMN_KEY_NAME, key4);
        cvs[0].put(COLUMN_VALUE_NAME, value4);
        cvs[1] = new ContentValues();
        cvs[1].put(COLUMN_KEY_NAME, key5);
        cvs[1].put(COLUMN_VALUE_NAME, value5);
        assertEquals(2, mContentResolver.bulkInsert(TABLE1_URI, cvs));
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(5, mCursor.getCount());
        mCursor.moveToLast();
        assertEquals(5, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key5, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value5, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.moveToPrevious();
        assertEquals(4, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key4, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value4, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        try {
            mContentResolver.bulkInsert(null, cvs);
            fail("did not throw NullPointerException when uri is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "delete",
        args = {android.net.Uri.class, java.lang.String.class, java.lang.String[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#delete(Uri, String, String[]) when the input Uri are null")
    public void testDelete() {
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(3, mCursor.getCount());
        mCursor.close();
        assertEquals(3, mContentResolver.delete(TABLE1_URI, null, null));
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(0, mCursor.getCount());
        mCursor.close();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY_NAME, KEY1);
        values.put(COLUMN_VALUE_NAME, VALUE1);
        mContentResolver.insert(TABLE1_URI, values);
        values.put(COLUMN_KEY_NAME, KEY2);
        values.put(COLUMN_VALUE_NAME, VALUE2);
        mContentResolver.insert(TABLE1_URI, values);
        values.put(COLUMN_KEY_NAME, KEY3);
        values.put(COLUMN_VALUE_NAME, VALUE3);
        mContentResolver.insert(TABLE1_URI, values);
        String selection = COLUMN_ID_NAME + "=2";
        assertEquals(1, mContentResolver.delete(TABLE1_URI, selection, null));
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(2, mCursor.getCount());
        mCursor.moveToFirst();
        assertEquals(1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(KEY1, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(VALUE1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.moveToNext();
        assertEquals(3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(KEY3, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(VALUE3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        selection = COLUMN_VALUE_NAME + "=3";
        assertEquals(1, mContentResolver.delete(TABLE1_URI, selection, null));
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(1, mCursor.getCount());
        mCursor.moveToFirst();
        assertEquals(1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(KEY1, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(VALUE1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        selection = COLUMN_KEY_NAME + "=\"" + KEY1 + "\"";
        assertEquals(1, mContentResolver.delete(TABLE1_URI, selection, null));
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(0, mCursor.getCount());
        mCursor.close();
        try {
            mContentResolver.delete(null, null, null);
            fail("did not throw NullPointerException when uri is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "update",
        args = {android.net.Uri.class, android.content.ContentValues.class,
                java.lang.String.class, java.lang.String[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "The javadoc says \"return the URL of the " +
            "newly created row\", but actually it return an integer.")
    public void testUpdate() {
        ContentValues values = new ContentValues();
        String key10 = "key10";
        String key20 = "key20";
        int value10 = 10;
        int value20 = 20;
        values.put(COLUMN_KEY_NAME, key10);
        values.put(COLUMN_VALUE_NAME, value10);
        assertEquals(3, mContentResolver.update(TABLE1_URI, values, null, null));
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(3, mCursor.getCount());
        mCursor.moveToFirst();
        assertEquals(1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key10, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value10, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.moveToNext();
        assertEquals(2, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key10, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value10, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.moveToLast();
        assertEquals(3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key10, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value10, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        String selection = COLUMN_ID_NAME + "=1";
        values.put(COLUMN_KEY_NAME, key20);
        values.put(COLUMN_VALUE_NAME, value20);
        assertEquals(1, mContentResolver.update(TABLE1_URI, values, selection, null));
        mCursor = mContentResolver.query(TABLE1_URI, null, null, null, null);
        assertNotNull(mCursor);
        assertEquals(3, mCursor.getCount());
        mCursor.moveToFirst();
        assertEquals(1, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key20, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value20, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.moveToNext();
        assertEquals(2, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key10, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value10, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.moveToLast();
        assertEquals(3, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_ID_NAME)));
        assertEquals(key10, mCursor.getString(mCursor.getColumnIndexOrThrow(COLUMN_KEY_NAME)));
        assertEquals(value10, mCursor.getInt(mCursor.getColumnIndexOrThrow(COLUMN_VALUE_NAME)));
        mCursor.close();
        try {
            mContentResolver.update(null, values, null, null);
            fail("did not throw NullPointerException when uri is null.");
        } catch (NullPointerException e) {
        }
        try {
            mContentResolver.update(TABLE1_URI, null, null, null);
            fail("did not throw IllegalArgumentException when values are null.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerContentObserver",
            args = {android.net.Uri.class, boolean.class, android.database.ContentObserver.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterContentObserver",
            args = {android.database.ContentObserver.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#registerContentObserver(Uri, boolean, ContentObserver) and " +
            "ContentResolver#unregisterContentObserver(ContentObserver) when the input " +
            "params are null")
    public void testRegisterContentObserver() {
        MockContentObserver mco = new MockContentObserver();
        mContentResolver.registerContentObserver(TABLE1_URI, true, mco);
        assertFalse(mco.hadOnChanged());
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY_NAME, "key10");
        values.put(COLUMN_VALUE_NAME, 10);
        mContentResolver.update(TABLE1_URI, values, null, null);
        assertTrue(mco.hadOnChanged());
        mco.reset();
        mContentResolver.unregisterContentObserver(mco);
        assertFalse(mco.hadOnChanged());
        mContentResolver.update(TABLE1_URI, values, null, null);
        assertFalse(mco.hadOnChanged());
        try {
            mContentResolver.registerContentObserver(null, false, mco);
            fail("did not throw NullPointerException or IllegalArgumentException when uri is null.");
        } catch (NullPointerException e) {
        } catch (IllegalArgumentException e) {
        }
        try {
            mContentResolver.registerContentObserver(TABLE1_URI, false, null);
            fail("did not throw NullPointerException when register null content observer.");
        } catch (NullPointerException e) {
        }
        try {
            mContentResolver.unregisterContentObserver(null);
            fail("did not throw NullPointerException when unregister null content observer.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "notifyChange",
        args = {android.net.Uri.class, android.database.ContentObserver.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#notifyChange(Uri, ContentObserver) when uri is null")
    public void testNotifyChange1() throws InterruptedException {
        MockContentObserver mco = new MockContentObserver();
        mContentResolver.registerContentObserver(TABLE1_URI, true, mco);
        assertFalse(mco.hadOnChanged());
        mContentResolver.notifyChange(TABLE1_URI, mco);
        assertTrue(mco.hadOnChanged());
        mContentResolver.unregisterContentObserver(mco);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "notifyChange",
        args = {android.net.Uri.class, android.database.ContentObserver.class, boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#notifyChange(Uri, ContentObserver, boolean) when uri is null ")
    public void testNotifyChange2() {
        MockContentObserver mco = new MockContentObserver();
        mContentResolver.registerContentObserver(TABLE1_URI, true, mco);
        assertFalse(mco.hadOnChanged());
        mContentResolver.notifyChange(TABLE1_URI, mco, false);
        assertTrue(mco.hadOnChanged());
        mContentResolver.unregisterContentObserver(mco);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "startSync",
            args = {android.net.Uri.class, android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "cancelSync",
            args = {android.net.Uri.class}
        )
    })
    @ToBeFixed(bug = "1400231", explanation = "the key is SyncObserver class is deleted" +
            " under currently enviroment(but still exists in doc)")
    public void testStartCancelSync() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(ACCOUNT, AUTHORITY, extras);
        ContentResolver.cancelSync(ACCOUNT, AUTHORITY);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startSync",
        args = {android.net.Uri.class, android.os.Bundle.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ContentResolver#startSync(Uri, Bundle) when extras is null")
    public void testStartSyncFailure() {
        try {
            ContentResolver.requestSync(null, null, null);
            fail("did not throw IllegalArgumentException when extras is null.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "validateSyncExtrasBundle",
        args = {android.os.Bundle.class}
    )
    public void testValidateSyncExtrasBundle() {
        Bundle extras = new Bundle();
        extras.putInt("Integer", 20);
        extras.putLong("Long", 10l);
        extras.putBoolean("Boolean", true);
        extras.putFloat("Float", 5.5f);
        extras.putDouble("Double", 2.5);
        extras.putString("String", "cts");
        extras.putCharSequence("CharSequence", null);
        ContentResolver.validateSyncExtrasBundle(extras);
        extras.putChar("Char", 'a'); 
        try {
            ContentResolver.validateSyncExtrasBundle(extras);
            fail("did not throw IllegalArgumentException when extras is invalide.");
        } catch (IllegalArgumentException e) {
        }
    }
    private class MockContentObserver extends ContentObserver {
        private boolean mHadOnChanged = false;
        public MockContentObserver() {
            super(null);
        }
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHadOnChanged = true;
        }
        public boolean hadOnChanged() {
            return mHadOnChanged;
        }
        public void reset() {
            mHadOnChanged = false;
        }
    }
}
