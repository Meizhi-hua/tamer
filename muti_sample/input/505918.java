public class WebViewDatabase {
    private static final String DATABASE_FILE = "webview.db";
    private static final String CACHE_DATABASE_FILE = "webviewCache.db";
    protected static final String LOGTAG = "webviewdatabase";
    private static final int DATABASE_VERSION = 10;
    private static final int CACHE_DATABASE_VERSION = 4;
    private static WebViewDatabase mInstance = null;
    private static SQLiteDatabase mDatabase = null;
    private static SQLiteDatabase mCacheDatabase = null;
    private final Object mCookieLock = new Object();
    private final Object mPasswordLock = new Object();
    private final Object mFormLock = new Object();
    private final Object mHttpAuthLock = new Object();
    private static final String mTableNames[] = {
        "cookies", "password", "formurl", "formdata", "httpauth"
    };
    private static final int TABLE_COOKIES_ID = 0;
    private static final int TABLE_PASSWORD_ID = 1;
    private static final int TABLE_FORMURL_ID = 2;
    private static final int TABLE_FORMDATA_ID = 3;
    private static final int TABLE_HTTPAUTH_ID = 4;
    private static final String ID_COL = "_id";
    private static final String[] ID_PROJECTION = new String[] {
        "_id"
    };
    private static final String COOKIES_NAME_COL = "name";
    private static final String COOKIES_VALUE_COL = "value";
    private static final String COOKIES_DOMAIN_COL = "domain";
    private static final String COOKIES_PATH_COL = "path";
    private static final String COOKIES_EXPIRES_COL = "expires";
    private static final String COOKIES_SECURE_COL = "secure";
    private static final String CACHE_URL_COL = "url";
    private static final String CACHE_FILE_PATH_COL = "filepath";
    private static final String CACHE_LAST_MODIFY_COL = "lastmodify";
    private static final String CACHE_ETAG_COL = "etag";
    private static final String CACHE_EXPIRES_COL = "expires";
    private static final String CACHE_EXPIRES_STRING_COL = "expiresstring";
    private static final String CACHE_MIMETYPE_COL = "mimetype";
    private static final String CACHE_ENCODING_COL = "encoding";
    private static final String CACHE_HTTP_STATUS_COL = "httpstatus";
    private static final String CACHE_LOCATION_COL = "location";
    private static final String CACHE_CONTENTLENGTH_COL = "contentlength";
    private static final String CACHE_CONTENTDISPOSITION_COL = "contentdisposition";
    private static final String CACHE_CROSSDOMAIN_COL = "crossdomain";
    private static final String PASSWORD_HOST_COL = "host";
    private static final String PASSWORD_USERNAME_COL = "username";
    private static final String PASSWORD_PASSWORD_COL = "password";
    private static final String FORMURL_URL_COL = "url";
    private static final String FORMDATA_URLID_COL = "urlid";
    private static final String FORMDATA_NAME_COL = "name";
    private static final String FORMDATA_VALUE_COL = "value";
    private static final String HTTPAUTH_HOST_COL = "host";
    private static final String HTTPAUTH_REALM_COL = "realm";
    private static final String HTTPAUTH_USERNAME_COL = "username";
    private static final String HTTPAUTH_PASSWORD_COL = "password";
    private static DatabaseUtils.InsertHelper mCacheInserter;
    private static int mCacheUrlColIndex;
    private static int mCacheFilePathColIndex;
    private static int mCacheLastModifyColIndex;
    private static int mCacheETagColIndex;
    private static int mCacheExpiresColIndex;
    private static int mCacheExpiresStringColIndex;
    private static int mCacheMimeTypeColIndex;
    private static int mCacheEncodingColIndex;
    private static int mCacheHttpStatusColIndex;
    private static int mCacheLocationColIndex;
    private static int mCacheContentLengthColIndex;
    private static int mCacheContentDispositionColIndex;
    private static int mCacheCrossDomainColIndex;
    private static int mCacheTransactionRefcount;
    private WebViewDatabase() {
    }
    public static synchronized WebViewDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WebViewDatabase();
            try {
                mDatabase = context
                        .openOrCreateDatabase(DATABASE_FILE, 0, null);
            } catch (SQLiteException e) {
                if (context.deleteDatabase(DATABASE_FILE)) {
                    mDatabase = context.openOrCreateDatabase(DATABASE_FILE, 0,
                            null);
                }
            }
            if (mDatabase != null && mDatabase.getVersion() != DATABASE_VERSION) {
                mDatabase.beginTransaction();
                try {
                    upgradeDatabase();
                    mDatabase.setTransactionSuccessful();
                } finally {
                    mDatabase.endTransaction();
                }
            }
            if (mDatabase != null) {
                mDatabase.setLockingEnabled(false);
            }
            try {
                mCacheDatabase = context.openOrCreateDatabase(
                        CACHE_DATABASE_FILE, 0, null);
            } catch (SQLiteException e) {
                if (context.deleteDatabase(CACHE_DATABASE_FILE)) {
                    mCacheDatabase = context.openOrCreateDatabase(
                            CACHE_DATABASE_FILE, 0, null);
                }
            }
            if (mCacheDatabase != null
                    && mCacheDatabase.getVersion() != CACHE_DATABASE_VERSION) {
                mCacheDatabase.beginTransaction();
                try {
                    upgradeCacheDatabase();
                    bootstrapCacheDatabase();
                    mCacheDatabase.setTransactionSuccessful();
                } finally {
                    mCacheDatabase.endTransaction();
                }
                CacheManager.removeAllCacheFiles();
            }
            if (mCacheDatabase != null) {
                mCacheDatabase.execSQL("PRAGMA read_uncommitted = true;");
                mCacheDatabase.setLockingEnabled(false);
                mCacheInserter = new DatabaseUtils.InsertHelper(mCacheDatabase,
                        "cache");
                mCacheUrlColIndex = mCacheInserter
                        .getColumnIndex(CACHE_URL_COL);
                mCacheFilePathColIndex = mCacheInserter
                        .getColumnIndex(CACHE_FILE_PATH_COL);
                mCacheLastModifyColIndex = mCacheInserter
                        .getColumnIndex(CACHE_LAST_MODIFY_COL);
                mCacheETagColIndex = mCacheInserter
                        .getColumnIndex(CACHE_ETAG_COL);
                mCacheExpiresColIndex = mCacheInserter
                        .getColumnIndex(CACHE_EXPIRES_COL);
                mCacheExpiresStringColIndex = mCacheInserter
                        .getColumnIndex(CACHE_EXPIRES_STRING_COL);
                mCacheMimeTypeColIndex = mCacheInserter
                        .getColumnIndex(CACHE_MIMETYPE_COL);
                mCacheEncodingColIndex = mCacheInserter
                        .getColumnIndex(CACHE_ENCODING_COL);
                mCacheHttpStatusColIndex = mCacheInserter
                        .getColumnIndex(CACHE_HTTP_STATUS_COL);
                mCacheLocationColIndex = mCacheInserter
                        .getColumnIndex(CACHE_LOCATION_COL);
                mCacheContentLengthColIndex = mCacheInserter
                        .getColumnIndex(CACHE_CONTENTLENGTH_COL);
                mCacheContentDispositionColIndex = mCacheInserter
                        .getColumnIndex(CACHE_CONTENTDISPOSITION_COL);
                mCacheCrossDomainColIndex = mCacheInserter
                        .getColumnIndex(CACHE_CROSSDOMAIN_COL);
            }
        }
        return mInstance;
    }
    private static void upgradeDatabase() {
        int oldVersion = mDatabase.getVersion();
        if (oldVersion != 0) {
            Log.i(LOGTAG, "Upgrading database from version "
                    + oldVersion + " to "
                    + DATABASE_VERSION + ", which will destroy old data");
        }
        boolean justPasswords = 8 == oldVersion && 9 == DATABASE_VERSION;
        boolean justAuth = 9 == oldVersion && 10 == DATABASE_VERSION;
        if (justAuth) {
            mDatabase.execSQL("DROP TABLE IF EXISTS "
                    + mTableNames[TABLE_HTTPAUTH_ID]);
            mDatabase.execSQL("CREATE TABLE " + mTableNames[TABLE_HTTPAUTH_ID]
                    + " (" + ID_COL + " INTEGER PRIMARY KEY, "
                    + HTTPAUTH_HOST_COL + " TEXT, " + HTTPAUTH_REALM_COL
                    + " TEXT, " + HTTPAUTH_USERNAME_COL + " TEXT, "
                    + HTTPAUTH_PASSWORD_COL + " TEXT," + " UNIQUE ("
                    + HTTPAUTH_HOST_COL + ", " + HTTPAUTH_REALM_COL
                    + ") ON CONFLICT REPLACE);");
            return;
        }
        if (!justPasswords) {
            mDatabase.execSQL("DROP TABLE IF EXISTS "
                    + mTableNames[TABLE_COOKIES_ID]);
            mDatabase.execSQL("DROP TABLE IF EXISTS cache");
            mDatabase.execSQL("DROP TABLE IF EXISTS "
                    + mTableNames[TABLE_FORMURL_ID]);
            mDatabase.execSQL("DROP TABLE IF EXISTS "
                    + mTableNames[TABLE_FORMDATA_ID]);
            mDatabase.execSQL("DROP TABLE IF EXISTS "
                    + mTableNames[TABLE_HTTPAUTH_ID]);
        }
        mDatabase.execSQL("DROP TABLE IF EXISTS "
                + mTableNames[TABLE_PASSWORD_ID]);
        mDatabase.setVersion(DATABASE_VERSION);
        if (!justPasswords) {
            mDatabase.execSQL("CREATE TABLE " + mTableNames[TABLE_COOKIES_ID]
                    + " (" + ID_COL + " INTEGER PRIMARY KEY, "
                    + COOKIES_NAME_COL + " TEXT, " + COOKIES_VALUE_COL
                    + " TEXT, " + COOKIES_DOMAIN_COL + " TEXT, "
                    + COOKIES_PATH_COL + " TEXT, " + COOKIES_EXPIRES_COL
                    + " INTEGER, " + COOKIES_SECURE_COL + " INTEGER" + ");");
            mDatabase.execSQL("CREATE INDEX cookiesIndex ON "
                    + mTableNames[TABLE_COOKIES_ID] + " (path)");
            mDatabase.execSQL("CREATE TABLE " + mTableNames[TABLE_FORMURL_ID]
                    + " (" + ID_COL + " INTEGER PRIMARY KEY, " + FORMURL_URL_COL
                    + " TEXT" + ");");
            mDatabase.execSQL("CREATE TABLE " + mTableNames[TABLE_FORMDATA_ID]
                    + " (" + ID_COL + " INTEGER PRIMARY KEY, "
                    + FORMDATA_URLID_COL + " INTEGER, " + FORMDATA_NAME_COL
                    + " TEXT, " + FORMDATA_VALUE_COL + " TEXT," + " UNIQUE ("
                    + FORMDATA_URLID_COL + ", " + FORMDATA_NAME_COL + ", "
                    + FORMDATA_VALUE_COL + ") ON CONFLICT IGNORE);");
            mDatabase.execSQL("CREATE TABLE " + mTableNames[TABLE_HTTPAUTH_ID]
                    + " (" + ID_COL + " INTEGER PRIMARY KEY, "
                    + HTTPAUTH_HOST_COL + " TEXT, " + HTTPAUTH_REALM_COL
                    + " TEXT, " + HTTPAUTH_USERNAME_COL + " TEXT, "
                    + HTTPAUTH_PASSWORD_COL + " TEXT," + " UNIQUE ("
                    + HTTPAUTH_HOST_COL + ", " + HTTPAUTH_REALM_COL
                    + ") ON CONFLICT REPLACE);");
        }
        mDatabase.execSQL("CREATE TABLE " + mTableNames[TABLE_PASSWORD_ID]
                + " (" + ID_COL + " INTEGER PRIMARY KEY, "
                + PASSWORD_HOST_COL + " TEXT, " + PASSWORD_USERNAME_COL
                + " TEXT, " + PASSWORD_PASSWORD_COL + " TEXT," + " UNIQUE ("
                + PASSWORD_HOST_COL + ", " + PASSWORD_USERNAME_COL
                + ") ON CONFLICT REPLACE);");
    }
    private static void upgradeCacheDatabase() {
        int oldVersion = mCacheDatabase.getVersion();
        if (oldVersion != 0) {
            Log.i(LOGTAG, "Upgrading cache database from version "
                    + oldVersion + " to "
                    + DATABASE_VERSION + ", which will destroy all old data");
        }
        mCacheDatabase.execSQL("DROP TABLE IF EXISTS cache");
        mCacheDatabase.setVersion(CACHE_DATABASE_VERSION);
    }
    private static void bootstrapCacheDatabase() {
        if (mCacheDatabase != null) {
            mCacheDatabase.execSQL("CREATE TABLE cache"
                    + " (" + ID_COL + " INTEGER PRIMARY KEY, " + CACHE_URL_COL
                    + " TEXT, " + CACHE_FILE_PATH_COL + " TEXT, "
                    + CACHE_LAST_MODIFY_COL + " TEXT, " + CACHE_ETAG_COL
                    + " TEXT, " + CACHE_EXPIRES_COL + " INTEGER, "
                    + CACHE_EXPIRES_STRING_COL + " TEXT, "
                    + CACHE_MIMETYPE_COL + " TEXT, " + CACHE_ENCODING_COL
                    + " TEXT," + CACHE_HTTP_STATUS_COL + " INTEGER, "
                    + CACHE_LOCATION_COL + " TEXT, " + CACHE_CONTENTLENGTH_COL
                    + " INTEGER, " + CACHE_CONTENTDISPOSITION_COL + " TEXT, "
                    + CACHE_CROSSDOMAIN_COL + " TEXT,"
                    + " UNIQUE (" + CACHE_URL_COL + ") ON CONFLICT REPLACE);");
            mCacheDatabase.execSQL("CREATE INDEX cacheUrlIndex ON cache ("
                    + CACHE_URL_COL + ")");
        }
    }
    private boolean hasEntries(int tableId) {
        if (mDatabase == null) {
            return false;
        }
        Cursor cursor = null;
        boolean ret = false;
        try {
            cursor = mDatabase.query(mTableNames[tableId], ID_PROJECTION,
                    null, null, null, null, null);
            ret = cursor.moveToFirst() == true;
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "hasEntries", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return ret;
    }
    ArrayList<Cookie> getCookiesForDomain(String domain) {
        ArrayList<Cookie> list = new ArrayList<Cookie>();
        if (domain == null || mDatabase == null) {
            return list;
        }
        synchronized (mCookieLock) {
            final String[] columns = new String[] {
                    ID_COL, COOKIES_DOMAIN_COL, COOKIES_PATH_COL,
                    COOKIES_NAME_COL, COOKIES_VALUE_COL, COOKIES_EXPIRES_COL,
                    COOKIES_SECURE_COL
            };
            final String selection = "(" + COOKIES_DOMAIN_COL
                    + " GLOB '*' || ?)";
            Cursor cursor = null;
            try {
                cursor = mDatabase.query(mTableNames[TABLE_COOKIES_ID],
                        columns, selection, new String[] { domain }, null, null,
                        null);
                if (cursor.moveToFirst()) {
                    int domainCol = cursor.getColumnIndex(COOKIES_DOMAIN_COL);
                    int pathCol = cursor.getColumnIndex(COOKIES_PATH_COL);
                    int nameCol = cursor.getColumnIndex(COOKIES_NAME_COL);
                    int valueCol = cursor.getColumnIndex(COOKIES_VALUE_COL);
                    int expiresCol = cursor.getColumnIndex(COOKIES_EXPIRES_COL);
                    int secureCol = cursor.getColumnIndex(COOKIES_SECURE_COL);
                    do {
                        Cookie cookie = new Cookie();
                        cookie.domain = cursor.getString(domainCol);
                        cookie.path = cursor.getString(pathCol);
                        cookie.name = cursor.getString(nameCol);
                        cookie.value = cursor.getString(valueCol);
                        if (cursor.isNull(expiresCol)) {
                            cookie.expires = -1;
                        } else {
                            cookie.expires = cursor.getLong(expiresCol);
                        }
                        cookie.secure = cursor.getShort(secureCol) != 0;
                        cookie.mode = Cookie.MODE_NORMAL;
                        list.add(cookie);
                    } while (cursor.moveToNext());
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "getCookiesForDomain", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return list;
        }
    }
    void deleteCookies(String domain, String path, String name) {
        if (domain == null || mDatabase == null) {
            return;
        }
        synchronized (mCookieLock) {
            final String where = "(" + COOKIES_DOMAIN_COL + " == ?) AND ("
                    + COOKIES_PATH_COL + " == ?) AND (" + COOKIES_NAME_COL
                    + " == ?)";
            mDatabase.delete(mTableNames[TABLE_COOKIES_ID], where,
                    new String[] { domain, path, name });
        }
    }
    void addCookie(Cookie cookie) {
        if (cookie.domain == null || cookie.path == null || cookie.name == null
                || mDatabase == null) {
            return;
        }
        synchronized (mCookieLock) {
            ContentValues cookieVal = new ContentValues();
            cookieVal.put(COOKIES_DOMAIN_COL, cookie.domain);
            cookieVal.put(COOKIES_PATH_COL, cookie.path);
            cookieVal.put(COOKIES_NAME_COL, cookie.name);
            cookieVal.put(COOKIES_VALUE_COL, cookie.value);
            if (cookie.expires != -1) {
                cookieVal.put(COOKIES_EXPIRES_COL, cookie.expires);
            }
            cookieVal.put(COOKIES_SECURE_COL, cookie.secure);
            mDatabase.insert(mTableNames[TABLE_COOKIES_ID], null, cookieVal);
        }
    }
    boolean hasCookies() {
        synchronized (mCookieLock) {
            return hasEntries(TABLE_COOKIES_ID);
        }
    }
    void clearCookies() {
        if (mDatabase == null) {
            return;
        }
        synchronized (mCookieLock) {
            mDatabase.delete(mTableNames[TABLE_COOKIES_ID], null, null);
        }
    }
    void clearSessionCookies() {
        if (mDatabase == null) {
            return;
        }
        final String sessionExpired = COOKIES_EXPIRES_COL + " ISNULL";
        synchronized (mCookieLock) {
            mDatabase.delete(mTableNames[TABLE_COOKIES_ID], sessionExpired,
                    null);
        }
    }
    void clearExpiredCookies(long now) {
        if (mDatabase == null) {
            return;
        }
        final String expires = COOKIES_EXPIRES_COL + " <= ?";
        synchronized (mCookieLock) {
            mDatabase.delete(mTableNames[TABLE_COOKIES_ID], expires,
                    new String[] { Long.toString(now) });
        }
    }
    boolean startCacheTransaction() {
        if (++mCacheTransactionRefcount == 1) {
            if (!Thread.currentThread().equals(
                    WebViewWorker.getHandler().getLooper().getThread())) {
                Log.w(LOGTAG, "startCacheTransaction should be called from "
                        + "WebViewWorkerThread instead of from "
                        + Thread.currentThread().getName());
            }
            mCacheDatabase.beginTransaction();
            return true;
        }
        return false;
    }
    boolean endCacheTransaction() {
        if (--mCacheTransactionRefcount == 0) {
            if (!Thread.currentThread().equals(
                    WebViewWorker.getHandler().getLooper().getThread())) {
                Log.w(LOGTAG, "endCacheTransaction should be called from "
                        + "WebViewWorkerThread instead of from "
                        + Thread.currentThread().getName());
            }
            try {
                mCacheDatabase.setTransactionSuccessful();
            } finally {
                mCacheDatabase.endTransaction();
            }
            return true;
        }
        return false;
    }
    CacheResult getCache(String url) {
        if (url == null || mCacheDatabase == null) {
            return null;
        }
        Cursor cursor = null;
        final String query = "SELECT filepath, lastmodify, etag, expires, "
                + "expiresstring, mimetype, encoding, httpstatus, location, contentlength, "
                + "contentdisposition, crossdomain FROM cache WHERE url = ?";
        try {
            cursor = mCacheDatabase.rawQuery(query, new String[] { url });
            if (cursor.moveToFirst()) {
                CacheResult ret = new CacheResult();
                ret.localPath = cursor.getString(0);
                ret.lastModified = cursor.getString(1);
                ret.etag = cursor.getString(2);
                ret.expires = cursor.getLong(3);
                ret.expiresString = cursor.getString(4);
                ret.mimeType = cursor.getString(5);
                ret.encoding = cursor.getString(6);
                ret.httpStatusCode = cursor.getInt(7);
                ret.location = cursor.getString(8);
                ret.contentLength = cursor.getLong(9);
                ret.contentdisposition = cursor.getString(10);
                ret.crossDomain = cursor.getString(11);
                return ret;
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "getCache", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
    void removeCache(String url) {
        if (url == null || mCacheDatabase == null) {
            return;
        }
        mCacheDatabase.execSQL("DELETE FROM cache WHERE url = ?", new String[] { url });
    }
    void addCache(String url, CacheResult c) {
        if (url == null || mCacheDatabase == null) {
            return;
        }
        mCacheInserter.prepareForInsert();
        mCacheInserter.bind(mCacheUrlColIndex, url);
        mCacheInserter.bind(mCacheFilePathColIndex, c.localPath);
        mCacheInserter.bind(mCacheLastModifyColIndex, c.lastModified);
        mCacheInserter.bind(mCacheETagColIndex, c.etag);
        mCacheInserter.bind(mCacheExpiresColIndex, c.expires);
        mCacheInserter.bind(mCacheExpiresStringColIndex, c.expiresString);
        mCacheInserter.bind(mCacheMimeTypeColIndex, c.mimeType);
        mCacheInserter.bind(mCacheEncodingColIndex, c.encoding);
        mCacheInserter.bind(mCacheHttpStatusColIndex, c.httpStatusCode);
        mCacheInserter.bind(mCacheLocationColIndex, c.location);
        mCacheInserter.bind(mCacheContentLengthColIndex, c.contentLength);
        mCacheInserter.bind(mCacheContentDispositionColIndex,
                c.contentdisposition);
        mCacheInserter.bind(mCacheCrossDomainColIndex, c.crossDomain);
        mCacheInserter.execute();
    }
    void clearCache() {
        if (mCacheDatabase == null) {
            return;
        }
        mCacheDatabase.delete("cache", null, null);
    }
    boolean hasCache() {
        if (mCacheDatabase == null) {
            return false;
        }
        Cursor cursor = null;
        boolean ret = false;
        try {
            cursor = mCacheDatabase.query("cache", ID_PROJECTION,
                    null, null, null, null, null);
            ret = cursor.moveToFirst() == true;
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "hasCache", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return ret;
    }
    long getCacheTotalSize() {
        long size = 0;
        Cursor cursor = null;
        final String query = "SELECT SUM(contentlength) as sum FROM cache";
        try {
            cursor = mCacheDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                size = cursor.getLong(0);
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "getCacheTotalSize", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return size;
    }
    List<String> trimCache(long amount) {
        ArrayList<String> pathList = new ArrayList<String>(100);
        Cursor cursor = null;
        final String query = "SELECT contentlength, filepath FROM cache ORDER BY expires ASC";
        try {
            cursor = mCacheDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                int batchSize = 100;
                StringBuilder pathStr = new StringBuilder(20 + 16 * batchSize);
                pathStr.append("DELETE FROM cache WHERE filepath IN (?");
                for (int i = 1; i < batchSize; i++) {
                    pathStr.append(", ?");
                }
                pathStr.append(")");
                SQLiteStatement statement = null;
                try {
                    statement = mCacheDatabase.compileStatement(
                            pathStr.toString());
                    int index = 1;
                    do {
                        long length = cursor.getLong(0);
                        if (length == 0) {
                            continue;
                        }
                        amount -= length;
                        String filePath = cursor.getString(1);
                        statement.bindString(index, filePath);
                        pathList.add(filePath);
                        if (index++ == batchSize) {
                            statement.execute();
                            statement.clearBindings();
                            index = 1;
                        }
                    } while (cursor.moveToNext() && amount > 0);
                    if (index > 1) {
                        statement.execute();
                    }
                } catch (IllegalStateException e) {
                    Log.e(LOGTAG, "trimCache SQLiteStatement", e);
                } finally {
                    if (statement != null) statement.close();
                }
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "trimCache Cursor", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return pathList;
    }
    List<String> getAllCacheFileNames() {
        ArrayList<String> pathList = null;
        Cursor cursor = null;
        try {
            cursor = mCacheDatabase.rawQuery("SELECT filepath FROM cache",
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                pathList = new ArrayList<String>(cursor.getCount());
                do {
                    pathList.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "getAllCacheFileNames", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return pathList;
    }
    void setUsernamePassword(String schemePlusHost, String username,
                String password) {
        if (schemePlusHost == null || mDatabase == null) {
            return;
        }
        synchronized (mPasswordLock) {
            final ContentValues c = new ContentValues();
            c.put(PASSWORD_HOST_COL, schemePlusHost);
            c.put(PASSWORD_USERNAME_COL, username);
            c.put(PASSWORD_PASSWORD_COL, password);
            mDatabase.insert(mTableNames[TABLE_PASSWORD_ID], PASSWORD_HOST_COL,
                    c);
        }
    }
    String[] getUsernamePassword(String schemePlusHost) {
        if (schemePlusHost == null || mDatabase == null) {
            return null;
        }
        final String[] columns = new String[] {
                PASSWORD_USERNAME_COL, PASSWORD_PASSWORD_COL
        };
        final String selection = "(" + PASSWORD_HOST_COL + " == ?)";
        synchronized (mPasswordLock) {
            String[] ret = null;
            Cursor cursor = null;
            try {
                cursor = mDatabase.query(mTableNames[TABLE_PASSWORD_ID],
                        columns, selection, new String[] { schemePlusHost }, null,
                        null, null);
                if (cursor.moveToFirst()) {
                    ret = new String[2];
                    ret[0] = cursor.getString(
                            cursor.getColumnIndex(PASSWORD_USERNAME_COL));
                    ret[1] = cursor.getString(
                            cursor.getColumnIndex(PASSWORD_PASSWORD_COL));
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "getUsernamePassword", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return ret;
        }
    }
    public boolean hasUsernamePassword() {
        synchronized (mPasswordLock) {
            return hasEntries(TABLE_PASSWORD_ID);
        }
    }
    public void clearUsernamePassword() {
        if (mDatabase == null) {
            return;
        }
        synchronized (mPasswordLock) {
            mDatabase.delete(mTableNames[TABLE_PASSWORD_ID], null, null);
        }
    }
    void setHttpAuthUsernamePassword(String host, String realm, String username,
            String password) {
        if (host == null || realm == null || mDatabase == null) {
            return;
        }
        synchronized (mHttpAuthLock) {
            final ContentValues c = new ContentValues();
            c.put(HTTPAUTH_HOST_COL, host);
            c.put(HTTPAUTH_REALM_COL, realm);
            c.put(HTTPAUTH_USERNAME_COL, username);
            c.put(HTTPAUTH_PASSWORD_COL, password);
            mDatabase.insert(mTableNames[TABLE_HTTPAUTH_ID], HTTPAUTH_HOST_COL,
                    c);
        }
    }
    String[] getHttpAuthUsernamePassword(String host, String realm) {
        if (host == null || realm == null || mDatabase == null){
            return null;
        }
        final String[] columns = new String[] {
                HTTPAUTH_USERNAME_COL, HTTPAUTH_PASSWORD_COL
        };
        final String selection = "(" + HTTPAUTH_HOST_COL + " == ?) AND ("
                + HTTPAUTH_REALM_COL + " == ?)";
        synchronized (mHttpAuthLock) {
            String[] ret = null;
            Cursor cursor = null;
            try {
                cursor = mDatabase.query(mTableNames[TABLE_HTTPAUTH_ID],
                        columns, selection, new String[] { host, realm }, null,
                        null, null);
                if (cursor.moveToFirst()) {
                    ret = new String[2];
                    ret[0] = cursor.getString(
                            cursor.getColumnIndex(HTTPAUTH_USERNAME_COL));
                    ret[1] = cursor.getString(
                            cursor.getColumnIndex(HTTPAUTH_PASSWORD_COL));
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "getHttpAuthUsernamePassword", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return ret;
        }
    }
    public boolean hasHttpAuthUsernamePassword() {
        synchronized (mHttpAuthLock) {
            return hasEntries(TABLE_HTTPAUTH_ID);
        }
    }
    public void clearHttpAuthUsernamePassword() {
        if (mDatabase == null) {
            return;
        }
        synchronized (mHttpAuthLock) {
            mDatabase.delete(mTableNames[TABLE_HTTPAUTH_ID], null, null);
        }
    }
    void setFormData(String url, HashMap<String, String> formdata) {
        if (url == null || formdata == null || mDatabase == null) {
            return;
        }
        final String selection = "(" + FORMURL_URL_COL + " == ?)";
        synchronized (mFormLock) {
            long urlid = -1;
            Cursor cursor = null;
            try {
                cursor = mDatabase.query(mTableNames[TABLE_FORMURL_ID],
                        ID_PROJECTION, selection, new String[] { url }, null, null,
                        null);
                if (cursor.moveToFirst()) {
                    urlid = cursor.getLong(cursor.getColumnIndex(ID_COL));
                } else {
                    ContentValues c = new ContentValues();
                    c.put(FORMURL_URL_COL, url);
                    urlid = mDatabase.insert(
                            mTableNames[TABLE_FORMURL_ID], null, c);
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "setFormData", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            if (urlid >= 0) {
                Set<Entry<String, String>> set = formdata.entrySet();
                Iterator<Entry<String, String>> iter = set.iterator();
                ContentValues map = new ContentValues();
                map.put(FORMDATA_URLID_COL, urlid);
                while (iter.hasNext()) {
                    Entry<String, String> entry = iter.next();
                    map.put(FORMDATA_NAME_COL, entry.getKey());
                    map.put(FORMDATA_VALUE_COL, entry.getValue());
                    mDatabase.insert(mTableNames[TABLE_FORMDATA_ID], null, map);
                }
            }
        }
    }
    ArrayList<String> getFormData(String url, String name) {
        ArrayList<String> values = new ArrayList<String>();
        if (url == null || name == null || mDatabase == null) {
            return values;
        }
        final String urlSelection = "(" + FORMURL_URL_COL + " == ?)";
        final String dataSelection = "(" + FORMDATA_URLID_COL + " == ?) AND ("
                + FORMDATA_NAME_COL + " == ?)";
        synchronized (mFormLock) {
            Cursor cursor = null;
            try {
                cursor = mDatabase.query(mTableNames[TABLE_FORMURL_ID],
                        ID_PROJECTION, urlSelection, new String[] { url }, null,
                        null, null);
                if (cursor.moveToFirst()) {
                    long urlid = cursor.getLong(cursor.getColumnIndex(ID_COL));
                    Cursor dataCursor = null;
                    try {
                        dataCursor = mDatabase.query(
                                mTableNames[TABLE_FORMDATA_ID],
                                new String[] { ID_COL, FORMDATA_VALUE_COL },
                                dataSelection,
                                new String[] { Long.toString(urlid), name },
                                null, null, null);
                        if (dataCursor.moveToFirst()) {
                            int valueCol = dataCursor.getColumnIndex(
                                    FORMDATA_VALUE_COL);
                            do {
                                values.add(dataCursor.getString(valueCol));
                            } while (dataCursor.moveToNext());
                        }
                    } catch (IllegalStateException e) {
                        Log.e(LOGTAG, "getFormData dataCursor", e);
                    } finally {
                        if (dataCursor != null) dataCursor.close();
                    }
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "getFormData cursor", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return values;
        }
    }
    public boolean hasFormData() {
        synchronized (mFormLock) {
            return hasEntries(TABLE_FORMURL_ID);
        }
    }
    public void clearFormData() {
        if (mDatabase == null) {
            return;
        }
        synchronized (mFormLock) {
            mDatabase.delete(mTableNames[TABLE_FORMURL_ID], null, null);
            mDatabase.delete(mTableNames[TABLE_FORMDATA_ID], null, null);
        }
    }
}
