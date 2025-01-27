public class MemoryFileProvider extends ContentProvider {
    private static final String TAG = "MemoryFileProvider";
    private static final String DATA_FILE = "data.bin";
    public static final byte[] TEST_BLOB = new byte[] {
        -12,  127, 0, 3, 1, 2, 3, 4, 5, 6, 1, -128, -1, -54, -65, 35,
        -53, -96, -74, -74, -55, -43, -69, 3, 52, -58,
        -121, 127, 87, -73, 16, -13, -103, -65, -128, -36,
        107, 24, 118, -17, 97, 97, -88, 19, -94, -54,
        53, 43, 44, -27, -124, 28, -74, 26, 35, -36,
        16, -124, -31, -31, -128, -79, 108, 116, 43, -17 };
    private SQLiteOpenHelper mOpenHelper;
    private static final int DATA_ID_BLOB = 1;
    private static final int HUGE = 2;
    private static final int FILE = 3;
    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURLMatcher.addURI("*", "data/#/blob", DATA_ID_BLOB);
        sURLMatcher.addURI("*", "huge", HUGE);
        sURLMatcher.addURI("*", "file", FILE);
    }
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "local.db";
        private static final int DATABASE_VERSION = 1;
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE data (" +
                       "_id INTEGER PRIMARY KEY," +
                       "_blob TEXT, " +
                       "integer INTEGER);");
            ContentValues values = new ContentValues();
            values.put("_id", 1);
            values.put("_blob", TEST_BLOB);
            values.put("integer", 100);
            db.insert("data", null, values);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            Log.w(TAG, "Upgrading test database from version " +
                  oldVersion + " to " + currentVersion +
                  ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS data");
            onCreate(db);
        }
    }
    public MemoryFileProvider() {
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        try {
            OutputStream out = getContext().openFileOutput(DATA_FILE, Context.MODE_PRIVATE);
            out.write(TEST_BLOB);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }
    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        throw new UnsupportedOperationException("query not supported");
    }
    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case DATA_ID_BLOB:
                return "application/octet-stream";
            case FILE:
                return "application/octet-stream";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }
    @Override
    public AssetFileDescriptor openAssetFile(Uri url, String mode) throws FileNotFoundException {
        int match = sURLMatcher.match(url);
        switch (match) {
            case DATA_ID_BLOB:
                String sql = "SELECT _blob FROM data WHERE _id=" + url.getPathSegments().get(1);
                return getBlobColumnAsAssetFile(url, mode, sql);
            case HUGE:
                try {
                    MemoryFile memoryFile = new MemoryFile(null, 5000000);
                    memoryFile.writeBytes(TEST_BLOB, 0, 1000000, TEST_BLOB.length);
                    memoryFile.deactivate();
                    return AssetFileDescriptor.fromMemoryFile(memoryFile);
                } catch (IOException ex) {
                    throw new FileNotFoundException("Error reading " + url + ":" + ex.toString());
                }
            case FILE:
                File file = getContext().getFileStreamPath(DATA_FILE);
                ParcelFileDescriptor fd =
                        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                return new AssetFileDescriptor(fd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
            default:
                throw new FileNotFoundException("No files supported by provider at " + url);
        }
    }
    private AssetFileDescriptor getBlobColumnAsAssetFile(Uri url, String mode, String sql)
            throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new FileNotFoundException("Mode " + mode + " not supported for " + url);
        }
        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            MemoryFile file = simpleQueryForBlobMemoryFile(db, sql);
            if (file == null) throw new FileNotFoundException("No such entry: " + url);
            AssetFileDescriptor afd = AssetFileDescriptor.fromMemoryFile(file);
            file.deactivate();
            return afd;
        } catch (IOException ex) {
            throw new FileNotFoundException("Error reading " + url + ":" + ex.toString());
        }
    }
    private MemoryFile simpleQueryForBlobMemoryFile(SQLiteDatabase db, String sql) throws IOException {
        Cursor cursor = db.rawQuery(sql, null);
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            byte[] bytes = cursor.getBlob(0);
            MemoryFile file = new MemoryFile(null, bytes.length);
            file.writeBytes(bytes, 0, 0, bytes.length);
            return file;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        throw new UnsupportedOperationException("update not supported");
    }
    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        throw new UnsupportedOperationException("insert not supported");
    }
    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        throw new UnsupportedOperationException("delete not supported");
    }
}
