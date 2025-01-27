public final class DownloadProvider extends ContentProvider {
    private static final String DB_NAME = "downloads.db";
    private static final int DB_VERSION = 100;
    private static final int DB_VERSION_NOP_UPGRADE_FROM = 31;
    private static final int DB_VERSION_NOP_UPGRADE_TO = 100;
    private static final String DB_TABLE = "downloads";
    private static final String DOWNLOAD_LIST_TYPE = "vnd.android.cursor.dir/download";
    private static final String DOWNLOAD_TYPE = "vnd.android.cursor.item/download";
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int DOWNLOADS = 1;
    private static final int DOWNLOADS_ID = 2;
    static {
        sURIMatcher.addURI("downloads", "download", DOWNLOADS);
        sURIMatcher.addURI("downloads", "download/#", DOWNLOADS_ID);
    }
    private static final String[] sAppReadableColumnsArray = new String[] {
        Downloads.Impl._ID,
        Downloads.Impl.COLUMN_APP_DATA,
        Downloads.Impl._DATA,
        Downloads.Impl.COLUMN_MIME_TYPE,
        Downloads.Impl.COLUMN_VISIBILITY,
        Downloads.Impl.COLUMN_DESTINATION,
        Downloads.Impl.COLUMN_CONTROL,
        Downloads.Impl.COLUMN_STATUS,
        Downloads.Impl.COLUMN_LAST_MODIFICATION,
        Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE,
        Downloads.Impl.COLUMN_NOTIFICATION_CLASS,
        Downloads.Impl.COLUMN_TOTAL_BYTES,
        Downloads.Impl.COLUMN_CURRENT_BYTES,
        Downloads.Impl.COLUMN_TITLE,
        Downloads.Impl.COLUMN_DESCRIPTION
    };
    private static HashSet<String> sAppReadableColumnsSet;
    static {
        sAppReadableColumnsSet = new HashSet<String>();
        for (int i = 0; i < sAppReadableColumnsArray.length; ++i) {
            sAppReadableColumnsSet.add(sAppReadableColumnsArray[i]);
        }
    }
    private SQLiteOpenHelper mOpenHelper = null;
    private int mSystemUid = -1;
    private int mDefContainerUid = -1;
    private final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(final SQLiteDatabase db) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "populating new database");
            }
            createTable(db);
        }
        @Override
        public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
            if (oldV == DB_VERSION_NOP_UPGRADE_FROM) {
                if (newV == DB_VERSION_NOP_UPGRADE_TO) { 
                    return;
                }
                oldV = DB_VERSION_NOP_UPGRADE_TO;
            }
            Log.i(Constants.TAG, "Upgrading downloads database from version " + oldV + " to " + newV
                    + ", which will destroy all old data");
            dropTable(db);
            createTable(db);
        }
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        mSystemUid = Process.SYSTEM_UID;
        ApplicationInfo appInfo = null;
        try {
            appInfo = getContext().getPackageManager().
                    getApplicationInfo("com.android.defcontainer", 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo != null) {
            mDefContainerUid = appInfo.uid;
        }
        return true;
    }
    @Override
    public String getType(final Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case DOWNLOADS: {
                return DOWNLOAD_LIST_TYPE;
            }
            case DOWNLOADS_ID: {
                return DOWNLOAD_TYPE;
            }
            default: {
                if (Constants.LOGV) {
                    Log.v(Constants.TAG, "calling getType on an unknown URI: " + uri);
                }
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
    }
    private void createTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + DB_TABLE + "(" +
                    Downloads.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Downloads.Impl.COLUMN_URI + " TEXT, " +
                    Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, " +
                    Downloads.Impl.COLUMN_APP_DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_NO_INTEGRITY + " BOOLEAN, " +
                    Downloads.Impl.COLUMN_FILE_NAME_HINT + " TEXT, " +
                    Constants.OTA_UPDATE + " BOOLEAN, " +
                    Downloads.Impl._DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_MIME_TYPE + " TEXT, " +
                    Downloads.Impl.COLUMN_DESTINATION + " INTEGER, " +
                    Constants.NO_SYSTEM_FILES + " BOOLEAN, " +
                    Downloads.Impl.COLUMN_VISIBILITY + " INTEGER, " +
                    Downloads.Impl.COLUMN_CONTROL + " INTEGER, " +
                    Downloads.Impl.COLUMN_STATUS + " INTEGER, " +
                    Constants.FAILED_CONNECTIONS + " INTEGER, " +
                    Downloads.Impl.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                    Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE + " TEXT, " +
                    Downloads.Impl.COLUMN_NOTIFICATION_CLASS + " TEXT, " +
                    Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                    Downloads.Impl.COLUMN_COOKIE_DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_USER_AGENT + " TEXT, " +
                    Downloads.Impl.COLUMN_REFERER + " TEXT, " +
                    Downloads.Impl.COLUMN_TOTAL_BYTES + " INTEGER, " +
                    Downloads.Impl.COLUMN_CURRENT_BYTES + " INTEGER, " +
                    Constants.ETAG + " TEXT, " +
                    Constants.UID + " INTEGER, " +
                    Downloads.Impl.COLUMN_OTHER_UID + " INTEGER, " +
                    Downloads.Impl.COLUMN_TITLE + " TEXT, " +
                    Downloads.Impl.COLUMN_DESCRIPTION + " TEXT, " +
                    Constants.MEDIA_SCANNED + " BOOLEAN);");
        } catch (SQLException ex) {
            Log.e(Constants.TAG, "couldn't create table in downloads database");
            throw ex;
        }
    }
    private void dropTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        } catch (SQLException ex) {
            Log.e(Constants.TAG, "couldn't drop table in downloads database");
            throw ex;
        }
    }
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (sURIMatcher.match(uri) != DOWNLOADS) {
            if (Config.LOGD) {
                Log.d(Constants.TAG, "calling insert on an unknown/invalid URI: " + uri);
            }
            throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
        }
        ContentValues filteredValues = new ContentValues();
        copyString(Downloads.Impl.COLUMN_URI, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_APP_DATA, values, filteredValues);
        copyBoolean(Downloads.Impl.COLUMN_NO_INTEGRITY, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_FILE_NAME_HINT, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_MIME_TYPE, values, filteredValues);
        Integer dest = values.getAsInteger(Downloads.Impl.COLUMN_DESTINATION);
        if (dest != null) {
            if (getContext().checkCallingPermission(Downloads.Impl.PERMISSION_ACCESS_ADVANCED)
                    != PackageManager.PERMISSION_GRANTED
                    && dest != Downloads.Impl.DESTINATION_EXTERNAL
                    && dest != Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE) {
                throw new SecurityException("unauthorized destination code");
            }
            filteredValues.put(Downloads.Impl.COLUMN_DESTINATION, dest);
        }
        Integer vis = values.getAsInteger(Downloads.Impl.COLUMN_VISIBILITY);
        if (vis == null) {
            if (dest == Downloads.Impl.DESTINATION_EXTERNAL) {
                filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY,
                        Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            } else {
                filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY,
                        Downloads.Impl.VISIBILITY_HIDDEN);
            }
        } else {
            filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY, vis);
        }
        copyInteger(Downloads.Impl.COLUMN_CONTROL, values, filteredValues);
        filteredValues.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
        filteredValues.put(Downloads.Impl.COLUMN_LAST_MODIFICATION, System.currentTimeMillis());
        String pckg = values.getAsString(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE);
        String clazz = values.getAsString(Downloads.Impl.COLUMN_NOTIFICATION_CLASS);
        if (pckg != null && clazz != null) {
            int uid = Binder.getCallingUid();
            try {
                if (uid == 0 ||
                        getContext().getPackageManager().getApplicationInfo(pckg, 0).uid == uid) {
                    filteredValues.put(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE, pckg);
                    filteredValues.put(Downloads.Impl.COLUMN_NOTIFICATION_CLASS, clazz);
                }
            } catch (PackageManager.NameNotFoundException ex) {
            }
        }
        copyString(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_COOKIE_DATA, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_USER_AGENT, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_REFERER, values, filteredValues);
        if (getContext().checkCallingPermission(Downloads.Impl.PERMISSION_ACCESS_ADVANCED)
                == PackageManager.PERMISSION_GRANTED) {
            copyInteger(Downloads.Impl.COLUMN_OTHER_UID, values, filteredValues);
        }
        filteredValues.put(Constants.UID, Binder.getCallingUid());
        if (Binder.getCallingUid() == 0) {
            copyInteger(Constants.UID, values, filteredValues);
        }
        copyString(Downloads.Impl.COLUMN_TITLE, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_DESCRIPTION, values, filteredValues);
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "initiating download with UID "
                    + filteredValues.getAsInteger(Constants.UID));
            if (filteredValues.containsKey(Downloads.Impl.COLUMN_OTHER_UID)) {
                Log.v(Constants.TAG, "other UID " +
                        filteredValues.getAsInteger(Downloads.Impl.COLUMN_OTHER_UID));
            }
        }
        Context context = getContext();
        context.startService(new Intent(context, DownloadService.class));
        long rowID = db.insert(DB_TABLE, null, filteredValues);
        Uri ret = null;
        if (rowID != -1) {
            context.startService(new Intent(context, DownloadService.class));
            ret = Uri.parse(Downloads.Impl.CONTENT_URI + "/" + rowID);
            context.getContentResolver().notifyChange(uri, null);
        } else {
            if (Config.LOGD) {
                Log.d(Constants.TAG, "couldn't insert into downloads database");
            }
        }
        return ret;
    }
    @Override
    public Cursor query(final Uri uri, String[] projection,
             final String selection, final String[] selectionArgs,
             final String sort) {
        Helpers.validateSelection(selection, sAppReadableColumnsSet);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int match = sURIMatcher.match(uri);
        boolean emptyWhere = true;
        switch (match) {
            case DOWNLOADS: {
                qb.setTables(DB_TABLE);
                break;
            }
            case DOWNLOADS_ID: {
                qb.setTables(DB_TABLE);
                qb.appendWhere(Downloads.Impl._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                emptyWhere = false;
                break;
            }
            default: {
                if (Constants.LOGV) {
                    Log.v(Constants.TAG, "querying unknown URI: " + uri);
                }
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        int callingUid = Binder.getCallingUid();
        if (Binder.getCallingPid() != Process.myPid() &&
                callingUid != mSystemUid &&
                callingUid != mDefContainerUid &&
                Process.supportsProcesses()) {
            boolean canSeeAllExternal;
            if (projection == null) {
                projection = sAppReadableColumnsArray;
                canSeeAllExternal = false;
            } else {
                canSeeAllExternal = getContext().checkCallingPermission(
                        Downloads.Impl.PERMISSION_SEE_ALL_EXTERNAL)
                        == PackageManager.PERMISSION_GRANTED;
                for (int i = 0; i < projection.length; ++i) {
                    if (!sAppReadableColumnsSet.contains(projection[i])) {
                        throw new IllegalArgumentException(
                                "column " + projection[i] + " is not allowed in queries");
                    }
                    canSeeAllExternal = canSeeAllExternal
                            && !projection[i].equals(Downloads.Impl._DATA);
                }
            }
            if (!emptyWhere) {
                qb.appendWhere(" AND ");
                emptyWhere = false;
            }
            String validUid = "( " + Constants.UID + "="
                    + Binder.getCallingUid() + " OR "
                    + Downloads.Impl.COLUMN_OTHER_UID + "="
                    + Binder.getCallingUid() + " )";
            if (canSeeAllExternal) {
                qb.appendWhere("( " + validUid + " OR "
                        + Downloads.Impl.DESTINATION_EXTERNAL + " = "
                        + Downloads.Impl.COLUMN_DESTINATION + " )");
            } else {
                qb.appendWhere(validUid);
            }
        }
        if (Constants.LOGVV) {
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            sb.append("starting query, database is ");
            if (db != null) {
                sb.append("not ");
            }
            sb.append("null; ");
            if (projection == null) {
                sb.append("projection is null; ");
            } else if (projection.length == 0) {
                sb.append("projection is empty; ");
            } else {
                for (int i = 0; i < projection.length; ++i) {
                    sb.append("projection[");
                    sb.append(i);
                    sb.append("] is ");
                    sb.append(projection[i]);
                    sb.append("; ");
                }
            }
            sb.append("selection is ");
            sb.append(selection);
            sb.append("; ");
            if (selectionArgs == null) {
                sb.append("selectionArgs is null; ");
            } else if (selectionArgs.length == 0) {
                sb.append("selectionArgs is empty; ");
            } else {
                for (int i = 0; i < selectionArgs.length; ++i) {
                    sb.append("selectionArgs[");
                    sb.append(i);
                    sb.append("] is ");
                    sb.append(selectionArgs[i]);
                    sb.append("; ");
                }
            }
            sb.append("sort is ");
            sb.append(sort);
            sb.append(".");
            Log.v(Constants.TAG, sb.toString());
        }
        Cursor ret = qb.query(db, projection, selection, selectionArgs,
                              null, null, sort);
        if (ret != null) {
           ret = new ReadOnlyCursorWrapper(ret);
        }
        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
            if (Constants.LOGVV) {
                Log.v(Constants.TAG,
                        "created cursor " + ret + " on behalf of " + Binder.getCallingPid());
            }
        } else {
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "query failed in downloads database");
            }
        }
        return ret;
    }
    @Override
    public int update(final Uri uri, final ContentValues values,
            final String where, final String[] whereArgs) {
        Helpers.validateSelection(where, sAppReadableColumnsSet);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        boolean startService = false;
        ContentValues filteredValues;
        if (Binder.getCallingPid() != Process.myPid()) {
            filteredValues = new ContentValues();
            copyString(Downloads.Impl.COLUMN_APP_DATA, values, filteredValues);
            copyInteger(Downloads.Impl.COLUMN_VISIBILITY, values, filteredValues);
            Integer i = values.getAsInteger(Downloads.Impl.COLUMN_CONTROL);
            if (i != null) {
                filteredValues.put(Downloads.Impl.COLUMN_CONTROL, i);
                startService = true;
            }
            copyInteger(Downloads.Impl.COLUMN_CONTROL, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_TITLE, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_DESCRIPTION, values, filteredValues);
        } else {
            filteredValues = values;
            String filename = values.getAsString(Downloads.Impl._DATA);
            if (filename != null) {
                Cursor c = query(uri, new String[]
                        { Downloads.Impl.COLUMN_TITLE }, null, null, null);
                if (!c.moveToFirst() || c.getString(0) == null) {
                    values.put(Downloads.Impl.COLUMN_TITLE,
                            new File(filename).getName());
                }
                c.close();
            }
        }
        int match = sURIMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
            case DOWNLOADS_ID: {
                String myWhere;
                if (where != null) {
                    if (match == DOWNLOADS) {
                        myWhere = "( " + where + " )";
                    } else {
                        myWhere = "( " + where + " ) AND ";
                    }
                } else {
                    myWhere = "";
                }
                if (match == DOWNLOADS_ID) {
                    String segment = uri.getPathSegments().get(1);
                    rowId = Long.parseLong(segment);
                    myWhere += " ( " + Downloads.Impl._ID + " = " + rowId + " ) ";
                }
                int callingUid = Binder.getCallingUid();
                if (Binder.getCallingPid() != Process.myPid() &&
                        callingUid != mSystemUid &&
                        callingUid != mDefContainerUid) {
                    myWhere += " AND ( " + Constants.UID + "=" +  Binder.getCallingUid() + " OR "
                            + Downloads.Impl.COLUMN_OTHER_UID + "=" +  Binder.getCallingUid() + " )";
                }
                if (filteredValues.size() > 0) {
                    count = db.update(DB_TABLE, filteredValues, myWhere, whereArgs);
                } else {
                    count = 0;
                }
                break;
            }
            default: {
                if (Config.LOGD) {
                    Log.d(Constants.TAG, "updating unknown/invalid URI: " + uri);
                }
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if (startService) {
            Context context = getContext();
            context.startService(new Intent(context, DownloadService.class));
        }
        return count;
    }
    @Override
    public int delete(final Uri uri, final String where,
            final String[] whereArgs) {
        Helpers.validateSelection(where, sAppReadableColumnsSet);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        int match = sURIMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
            case DOWNLOADS_ID: {
                String myWhere;
                if (where != null) {
                    if (match == DOWNLOADS) {
                        myWhere = "( " + where + " )";
                    } else {
                        myWhere = "( " + where + " ) AND ";
                    }
                } else {
                    myWhere = "";
                }
                if (match == DOWNLOADS_ID) {
                    String segment = uri.getPathSegments().get(1);
                    long rowId = Long.parseLong(segment);
                    myWhere += " ( " + Downloads.Impl._ID + " = " + rowId + " ) ";
                }
                int callingUid = Binder.getCallingUid();
                if (Binder.getCallingPid() != Process.myPid() &&
                        callingUid != mSystemUid &&
                        callingUid != mDefContainerUid) {
                    myWhere += " AND ( " + Constants.UID + "=" +  Binder.getCallingUid() + " OR "
                            + Downloads.Impl.COLUMN_OTHER_UID + "="
                            +  Binder.getCallingUid() + " )";
                }
                count = db.delete(DB_TABLE, myWhere, whereArgs);
                break;
            }
            default: {
                if (Config.LOGD) {
                    Log.d(Constants.TAG, "deleting unknown/invalid URI: " + uri);
                }
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "openFile uri: " + uri + ", mode: " + mode
                    + ", uid: " + Binder.getCallingUid());
            Cursor cursor = query(Downloads.Impl.CONTENT_URI,
                    new String[] { "_id" }, null, null, "_id");
            if (cursor == null) {
                Log.v(Constants.TAG, "null cursor in openFile");
            } else {
                if (!cursor.moveToFirst()) {
                    Log.v(Constants.TAG, "empty cursor in openFile");
                } else {
                    do {
                        Log.v(Constants.TAG, "row " + cursor.getInt(0) + " available");
                    } while(cursor.moveToNext());
                }
                cursor.close();
            }
            cursor = query(uri, new String[] { "_data" }, null, null, null);
            if (cursor == null) {
                Log.v(Constants.TAG, "null cursor in openFile");
            } else {
                if (!cursor.moveToFirst()) {
                    Log.v(Constants.TAG, "empty cursor in openFile");
                } else {
                    String filename = cursor.getString(0);
                    Log.v(Constants.TAG, "filename in openFile: " + filename);
                    if (new java.io.File(filename).isFile()) {
                        Log.v(Constants.TAG, "file exists in openFile");
                    }
                }
               cursor.close();
            }
        }
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        int count = (c != null) ? c.getCount() : 0;
        if (count != 1) {
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }
        c.moveToFirst();
        String path = c.getString(0);
        c.close();
        if (path == null) {
            throw new FileNotFoundException("No filename found.");
        }
        if (!Helpers.isFilenameValid(path)) {
            throw new FileNotFoundException("Invalid filename.");
        }
        if (!"r".equals(mode)) {
            throw new FileNotFoundException("Bad mode for " + uri + ": " + mode);
        }
        ParcelFileDescriptor ret = ParcelFileDescriptor.open(new File(path),
                ParcelFileDescriptor.MODE_READ_ONLY);
        if (ret == null) {
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "couldn't open file");
            }
            throw new FileNotFoundException("couldn't open file");
        } else {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_LAST_MODIFICATION, System.currentTimeMillis());
            update(uri, values, null, null);
        }
        return ret;
    }
    private static final void copyInteger(String key, ContentValues from, ContentValues to) {
        Integer i = from.getAsInteger(key);
        if (i != null) {
            to.put(key, i);
        }
    }
    private static final void copyBoolean(String key, ContentValues from, ContentValues to) {
        Boolean b = from.getAsBoolean(key);
        if (b != null) {
            to.put(key, b);
        }
    }
    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }
    private class ReadOnlyCursorWrapper extends CursorWrapper implements CrossProcessCursor {
        public ReadOnlyCursorWrapper(Cursor cursor) {
            super(cursor);
            mCursor = (CrossProcessCursor) cursor;
        }
        public boolean deleteRow() {
            throw new SecurityException("Download manager cursors are read-only");
        }
        public boolean commitUpdates() {
            throw new SecurityException("Download manager cursors are read-only");
        }
        public void fillWindow(int pos, CursorWindow window) {
            mCursor.fillWindow(pos, window);
        }
        public CursorWindow getWindow() {
            return mCursor.getWindow();
        }
        public boolean onMove(int oldPosition, int newPosition) {
            return mCursor.onMove(oldPosition, newPosition);
        }
        private CrossProcessCursor mCursor;
    }
}
