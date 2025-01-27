@TestTargetClass(ContentProvider.class)
public class ContentProviderTest extends AndroidTestCase {
    private static final String TEST_PACKAGE_NAME = "com.android.cts.stub";
    private static final String TEST_FILE_NAME = "testFile.tmp";
    private static final String TEST_DB_NAME = "test.db";
    @Override
    protected void tearDown() throws Exception {
        mContext.deleteDatabase(TEST_DB_NAME);
        mContext.deleteFile(TEST_FILE_NAME);
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "openAssetFile",
        args = {android.net.Uri.class, java.lang.String.class}
    )
    public void testOpenAssetFile() throws IOException {
        MockContentProvider mockContentProvider = new MockContentProvider();
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                ":
        try {
            mockContentProvider.openAssetFile(uri, "r");
            fail("Should always throw out FileNotFoundException!");
        } catch (FileNotFoundException e) {
        }
        try {
            mockContentProvider.openFile(null, null);
            fail("Should always throw out FileNotFoundException!");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "attachInfo",
        args = {android.content.Context.class, android.content.pm.ProviderInfo.class}
    )
    public void testAttachInfo() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        ProviderInfo info1 = new ProviderInfo();
        info1.readPermission = "android.permission.READ_SMS";
        info1.writePermission = "android.permission.WRITE_SMS";
        mockContentProvider.attachInfo(getContext(), info1);
        assertSame(getContext(), mockContentProvider.getContext());
        assertEquals(info1.readPermission, mockContentProvider.getReadPermission());
        assertEquals(info1.writePermission, mockContentProvider.getWritePermission());
        ProviderInfo info2 = new ProviderInfo();
        info2.readPermission = "android.permission.READ_CONTACTS";
        info2.writePermission = "android.permission.WRITE_CONTACTS";
        mockContentProvider.attachInfo(null, info2);
        assertSame(getContext(), mockContentProvider.getContext());
        assertEquals(info1.readPermission, mockContentProvider.getReadPermission());
        assertEquals(info1.writePermission, mockContentProvider.getWritePermission());
        mockContentProvider = new MockContentProvider();
        mockContentProvider.attachInfo(null, null);
        assertNull(mockContentProvider.getContext());
        assertNull(mockContentProvider.getReadPermission());
        assertNull(mockContentProvider.getWritePermission());
        mockContentProvider.attachInfo(null, info2);
        assertNull(mockContentProvider.getContext());
        assertEquals(info2.readPermission, mockContentProvider.getReadPermission());
        assertEquals(info2.writePermission, mockContentProvider.getWritePermission());
        mockContentProvider.attachInfo(getContext(), info1);
        assertSame(getContext(), mockContentProvider.getContext());
        assertEquals(info1.readPermission, mockContentProvider.getReadPermission());
        assertEquals(info1.writePermission, mockContentProvider.getWritePermission());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "bulkInsert",
        args = {android.net.Uri.class, android.content.ContentValues[].class}
    )
    public void testBulkInsert() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        int count = 2;
        ContentValues[] values = new ContentValues[count];
        for (int i = 0; i < count; i++) {
            values[i] = new ContentValues();
        }
        Uri uri = Uri.parse("content:
        assertEquals(count, mockContentProvider.bulkInsert(uri, values));
        assertEquals(count, mockContentProvider.getInsertCount());
        mockContentProvider = new MockContentProvider();
        try {
            assertEquals(count, mockContentProvider.bulkInsert(null, values));
        } finally {
            assertEquals(count, mockContentProvider.getInsertCount());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContext",
        args = {}
    )
    public void testGetContext() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        assertNull(mockContentProvider.getContext());
        mockContentProvider.attachInfo(getContext(), null);
        assertSame(getContext(), mockContentProvider.getContext());
        mockContentProvider.attachInfo(null, null);
        assertSame(getContext(), mockContentProvider.getContext());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getReadPermission",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setReadPermission",
            args = {java.lang.String.class}
        )
    })
    public void testAccessReadPermission() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        assertNull(mockContentProvider.getReadPermission());
        String expected = "android.permission.READ_CONTACTS";
        mockContentProvider.setReadPermissionWrapper(expected);
        assertEquals(expected, mockContentProvider.getReadPermission());
        expected = "android.permission.READ_SMS";
        mockContentProvider.setReadPermissionWrapper(expected);
        assertEquals(expected, mockContentProvider.getReadPermission());
        mockContentProvider.setReadPermissionWrapper(null);
        assertNull(mockContentProvider.getReadPermission());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWritePermission",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWritePermission",
            args = {java.lang.String.class}
        )
    })
    public void testAccessWritePermission() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        assertNull(mockContentProvider.getWritePermission());
        String expected = "android.permission.WRITE_CONTACTS";
        mockContentProvider.setWritePermissionWrapper(expected);
        assertEquals(expected, mockContentProvider.getWritePermission());
        expected = "android.permission.WRITE_SMS";
        mockContentProvider.setWritePermissionWrapper(expected);
        assertEquals(expected, mockContentProvider.getWritePermission());
        mockContentProvider.setWritePermissionWrapper(null);
        assertNull(mockContentProvider.getWritePermission());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isTemporary",
        args = {}
    )
    public void testIsTemporary() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        assertFalse(mockContentProvider.isTemporary());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "openFile",
        args = {android.net.Uri.class, java.lang.String.class}
    )
    public void testOpenFile() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        try {
            Uri uri = Uri.parse("content:
            mockContentProvider.openFile(uri, "r");
            fail("Should always throw out FileNotFoundException!");
        } catch (FileNotFoundException e) {
        }
        try {
            mockContentProvider.openFile(null, null);
            fail("Should always throw out FileNotFoundException!");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "openFileHelper",
        args = {android.net.Uri.class, java.lang.String.class}
    )
    public void testOpenFileHelper() throws IOException {
        mContext.openFileOutput(TEST_FILE_NAME, Context.MODE_PRIVATE).close();
        File file = mContext.getFileStreamPath(TEST_FILE_NAME);
        assertTrue(file.exists());
        ContentProvider cp = new OpenFileContentProvider(file.getAbsolutePath(), TEST_DB_NAME);
        Uri uri = Uri.parse("content:
        assertNotNull(cp.openFile(uri, "r"));
        try {
            uri = Uri.parse("content:
            cp.openFile(uri, "wrong");
            fail("Should throw FileNotFoundException!");
        } catch (FileNotFoundException e) {
        }
        file.delete();
        try {
            uri = Uri.parse("content:
            cp.openFile(uri, "r");
            fail("Should throw FileNotFoundException!");
        } catch (FileNotFoundException e) {
        }
        try {
            cp.openFile((Uri) null, "r");
            fail("Should always throw FileNotFoundException!");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "onConfigurationChanged",
        args = {android.content.res.Configuration.class}
    )
    public void testOnConfigurationChanged() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "onLowMemory",
        args = {}
    )
    public void testOnLowMemory() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "coerceToLocalContentProvider",
        args = {android.content.IContentProvider.class}
    )
    public void testCoerceToLocalContentProvider() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        IContentProvider transport = mockContentProvider.getIContentProvider();
        assertSame(mockContentProvider, ContentProvider.coerceToLocalContentProvider(transport));
        IContentProvider iContentProvider = new IContentProvider() {
            public IBulkCursor bulkQuery(Uri url, String[] projection, String selection,
                    String[] selectionArgs, String sortOrder, IContentObserver observer,
                    CursorWindow window) throws RemoteException {
                return null;
            }
            public int bulkInsert(Uri url, ContentValues[] initialValues) {
                return 0;
            }
            public int delete(Uri url, String selection, String[] selectionArgs) {
                return 0;
            }
            public String getType(Uri url) {
                return null;
            }
            public Uri insert(Uri url, ContentValues initialValues) {
                return null;
            }
            public ParcelFileDescriptor openFile(Uri url, String mode) {
                return null;
            }
            public AssetFileDescriptor openAssetFile(Uri url, String mode) {
                return null;
            }
            public ContentProviderResult[] applyBatch(
                    ArrayList<ContentProviderOperation> operations)
                    throws RemoteException, OperationApplicationException {
                return null;
            }
            public Cursor query(Uri url, String[] projection, String selection,
                    String[] selectionArgs, String sortOrder) {
                return null;
            }
            public EntityIterator queryEntities(Uri url, String selection, String[] selectionArgs,
                    String sortOrder) throws RemoteException {
                return null;
            }
            public int update(Uri url, ContentValues values, String selection,
                    String[] selectionArgs) {
                return 0;
            }
            public IBinder asBinder() {
                return null;
            }
            public Bundle call(String method, String request, Bundle args) {
                return null;
            }
        };
        assertNull(ContentProvider.coerceToLocalContentProvider(iContentProvider));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getIContentProvider()",
        method = "getIContentProvider",
        args = {}
    )
    public void testGetIContentProvider() {
        MockContentProvider mockContentProvider = new MockContentProvider();
        assertNotNull(mockContentProvider.getIContentProvider());
    }
    private class MockContentProvider extends ContentProvider {
        private int mInsertCount = 0;
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }
        @Override
        public String getType(Uri uri) {
            return null;
        }
        @Override
        public Uri insert(Uri uri, ContentValues values) {
            mInsertCount++;
            return null;
        }
        public int getInsertCount() {
            return mInsertCount;
        }
        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                String[] selectionArgs, String sortOrder) {
            return null;
        }
        @Override
        public int update(Uri uri, ContentValues values, String selection,
                String[] selectionArgs) {
            return 0;
        }
        @Override
        public boolean onCreate() {
            return false;
        }
        public void setReadPermissionWrapper(String permission) {
            super.setReadPermission(permission);
        }
        public void setWritePermissionWrapper(String permission) {
            super.setWritePermission(permission);
        }
        @Override
        protected boolean isTemporary() {
            return super.isTemporary();
        }
        public ParcelFileDescriptor openFileHelperWrapper(Uri uri, String mode)
                throws FileNotFoundException {
            return super.openFileHelper(uri, mode);
        }
    }
    private class OpenFileContentProvider extends ContentProvider {
        private SQLiteDatabase mDb;
        OpenFileContentProvider(String fileName, String dbName) {
            mContext.deleteDatabase(dbName);
            mDb = mContext.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
            mDb.execSQL("CREATE TABLE files ( _data TEXT );");
            mDb.execSQL("INSERT INTO files VALUES ( \"" + fileName + "\");");
        }
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            throw new RuntimeException("not implemented");
        }
        @Override
        public String getType(Uri uri) {
            throw new RuntimeException("not implemented");
        }
        @Override
        public Uri insert(Uri uri, ContentValues values) {
            throw new RuntimeException("not implemented");
        }
        @Override
        public boolean onCreate() {
            return true;
        }
        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                String sortOrder) {
            return mDb.query("files", projection, selection, selectionArgs, null, null, null);
        }
        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            throw new RuntimeException("not implemented");
        }
        @Override
        public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
            return openFileHelper(uri, mode);
        }
    }
}
