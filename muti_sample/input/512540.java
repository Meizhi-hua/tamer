public class DownloadService extends Service {
    private DownloadManagerContentObserver mObserver;
    private DownloadNotification mNotifier;
    private ArrayList<DownloadInfo> mDownloads;
    private UpdateThread mUpdateThread;
    private boolean mPendingUpdate;
    private MediaScannerConnection mMediaScannerConnection;
    private boolean mMediaScannerConnecting;
    private IMediaScannerService mMediaScannerService;
    private CharArrayBuffer oldChars;
    private CharArrayBuffer mNewChars;
    private class DownloadManagerContentObserver extends ContentObserver {
        public DownloadManagerContentObserver() {
            super(new Handler());
        }
        public void onChange(final boolean selfChange) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "Service ContentObserver received notification");
            }
            updateFromProvider();
        }
    }
    public class MediaScannerConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "Connected to Media Scanner");
            }
            mMediaScannerConnecting = false;
            synchronized (DownloadService.this) {
                mMediaScannerService = IMediaScannerService.Stub.asInterface(service);
                if (mMediaScannerService != null) {
                    updateFromProvider();
                }
            }
        }
        public void disconnectMediaScanner() {
            synchronized (DownloadService.this) {
                if (mMediaScannerService != null) {
                    mMediaScannerService = null;
                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "Disconnecting from Media Scanner");
                    }
                    try {
                        unbindService(this);
                    } catch (IllegalArgumentException ex) {
                        if (Constants.LOGV) {
                            Log.v(Constants.TAG, "unbindService threw up: " + ex);
                        }
                    }
                }
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "Disconnected from Media Scanner");
            }
            synchronized (DownloadService.this) {
                mMediaScannerService = null;
            }
        }
    }
    public IBinder onBind(Intent i) {
        throw new UnsupportedOperationException("Cannot bind to Download Manager Service");
    }
    public void onCreate() {
        super.onCreate();
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Service onCreate");
        }
        mDownloads = Lists.newArrayList();
        mObserver = new DownloadManagerContentObserver();
        getContentResolver().registerContentObserver(Downloads.Impl.CONTENT_URI,
                true, mObserver);
        mMediaScannerService = null;
        mMediaScannerConnecting = false;
        mMediaScannerConnection = new MediaScannerConnection();
        mNotifier = new DownloadNotification(this);
        mNotifier.mNotificationMgr.cancelAll();
        mNotifier.updateNotification();
        trimDatabase();
        removeSpuriousFiles();
        updateFromProvider();
    }
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Service onStart");
        }
        updateFromProvider();
    }
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mObserver);
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Service onDestroy");
        }
        super.onDestroy();
    }
    private void updateFromProvider() {
        synchronized (this) {
            mPendingUpdate = true;
            if (mUpdateThread == null) {
                mUpdateThread = new UpdateThread();
                mUpdateThread.start();
            }
        }
    }
    private class UpdateThread extends Thread {
        public UpdateThread() {
            super("Download Service");
        }
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            boolean keepService = false;
            long wakeUp = Long.MAX_VALUE;
            for (;;) {
                synchronized (DownloadService.this) {
                    if (mUpdateThread != this) {
                        throw new IllegalStateException(
                                "multiple UpdateThreads in DownloadService");
                    }
                    if (!mPendingUpdate) {
                        mUpdateThread = null;
                        if (!keepService) {
                            stopSelf();
                        }
                        if (wakeUp != Long.MAX_VALUE) {
                            AlarmManager alarms =
                                    (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            if (alarms == null) {
                                Log.e(Constants.TAG, "couldn't get alarm manager");
                            } else {
                                if (Constants.LOGV) {
                                    Log.v(Constants.TAG, "scheduling retry in " + wakeUp + "ms");
                                }
                                Intent intent = new Intent(Constants.ACTION_RETRY);
                                intent.setClassName("com.android.providers.downloads",
                                        DownloadReceiver.class.getName());
                                alarms.set(
                                        AlarmManager.RTC_WAKEUP,
                                        System.currentTimeMillis() + wakeUp,
                                        PendingIntent.getBroadcast(DownloadService.this, 0, intent,
                                                PendingIntent.FLAG_ONE_SHOT));
                            }
                        }
                        oldChars = null;
                        mNewChars = null;
                        return;
                    }
                    mPendingUpdate = false;
                }
                boolean networkAvailable = Helpers.isNetworkAvailable(DownloadService.this);
                boolean networkRoaming = Helpers.isNetworkRoaming(DownloadService.this);
                long now = System.currentTimeMillis();
                Cursor cursor = getContentResolver().query(Downloads.Impl.CONTENT_URI,
                        null, null, null, Downloads.Impl._ID);
                if (cursor == null) {
                    return;
                }
                cursor.moveToFirst();
                int arrayPos = 0;
                boolean mustScan = false;
                keepService = false;
                wakeUp = Long.MAX_VALUE;
                boolean isAfterLast = cursor.isAfterLast();
                int idColumn = cursor.getColumnIndexOrThrow(Downloads.Impl._ID);
                while (!isAfterLast || arrayPos < mDownloads.size()) {
                    if (isAfterLast) {
                        if (Constants.LOGVV) {
                            int arrayId = ((DownloadInfo) mDownloads.get(arrayPos)).mId;
                            Log.v(Constants.TAG, "Array update: trimming " +
                                    arrayId + " @ "  + arrayPos);
                        }
                        if (shouldScanFile(arrayPos) && mediaScannerConnected()) {
                            scanFile(null, arrayPos);
                        }
                        deleteDownload(arrayPos); 
                    } else {
                        int id = cursor.getInt(idColumn);
                        if (arrayPos == mDownloads.size()) {
                            insertDownload(cursor, arrayPos, networkAvailable, networkRoaming, now);
                            if (Constants.LOGVV) {
                                Log.v(Constants.TAG, "Array update: appending " +
                                        id + " @ " + arrayPos);
                            }
                            if (shouldScanFile(arrayPos)
                                    && (!mediaScannerConnected() || !scanFile(cursor, arrayPos))) {
                                mustScan = true;
                                keepService = true;
                            }
                            if (visibleNotification(arrayPos)) {
                                keepService = true;
                            }
                            long next = nextAction(arrayPos, now);
                            if (next == 0) {
                                keepService = true;
                            } else if (next > 0 && next < wakeUp) {
                                wakeUp = next;
                            }
                            ++arrayPos;
                            cursor.moveToNext();
                            isAfterLast = cursor.isAfterLast();
                        } else {
                            int arrayId = mDownloads.get(arrayPos).mId;
                            if (arrayId < id) {
                                if (Constants.LOGVV) {
                                    Log.v(Constants.TAG, "Array update: removing " + arrayId
                                            + " @ " + arrayPos);
                                }
                                if (shouldScanFile(arrayPos) && mediaScannerConnected()) {
                                    scanFile(null, arrayPos);
                                }
                                deleteDownload(arrayPos); 
                            } else if (arrayId == id) {
                                updateDownload(
                                        cursor, arrayPos,
                                        networkAvailable, networkRoaming, now);
                                if (shouldScanFile(arrayPos)
                                        && (!mediaScannerConnected()
                                                || !scanFile(cursor, arrayPos))) {
                                    mustScan = true;
                                    keepService = true;
                                }
                                if (visibleNotification(arrayPos)) {
                                    keepService = true;
                                }
                                long next = nextAction(arrayPos, now);
                                if (next == 0) {
                                    keepService = true;
                                } else if (next > 0 && next < wakeUp) {
                                    wakeUp = next;
                                }
                                ++arrayPos;
                                cursor.moveToNext();
                                isAfterLast = cursor.isAfterLast();
                            } else {
                                if (Constants.LOGVV) {
                                    Log.v(Constants.TAG, "Array update: inserting " +
                                            id + " @ " + arrayPos);
                                }
                                insertDownload(
                                        cursor, arrayPos,
                                        networkAvailable, networkRoaming, now);
                                if (shouldScanFile(arrayPos)
                                        && (!mediaScannerConnected()
                                                || !scanFile(cursor, arrayPos))) {
                                    mustScan = true;
                                    keepService = true;
                                }
                                if (visibleNotification(arrayPos)) {
                                    keepService = true;
                                }
                                long next = nextAction(arrayPos, now);
                                if (next == 0) {
                                    keepService = true;
                                } else if (next > 0 && next < wakeUp) {
                                    wakeUp = next;
                                }
                                ++arrayPos;
                                cursor.moveToNext();
                                isAfterLast = cursor.isAfterLast();
                            }
                        }
                    }
                }
                mNotifier.updateNotification();
                if (mustScan) {
                    if (!mMediaScannerConnecting) {
                        Intent intent = new Intent();
                        intent.setClassName("com.android.providers.media",
                                "com.android.providers.media.MediaScannerService");
                        mMediaScannerConnecting = true;
                        bindService(intent, mMediaScannerConnection, BIND_AUTO_CREATE);
                    }
                } else {
                    mMediaScannerConnection.disconnectMediaScanner();
                }
                cursor.close();
            }
        }
    }
    private void removeSpuriousFiles() {
        File[] files = Environment.getDownloadCacheDirectory().listFiles();
        if (files == null) {
            return;
        }
        HashSet<String> fileSet = new HashSet();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(Constants.KNOWN_SPURIOUS_FILENAME)) {
                continue;
            }
            if (files[i].getName().equalsIgnoreCase(Constants.RECOVERY_DIRECTORY)) {
                continue;
            }
            fileSet.add(files[i].getPath());
        }
        Cursor cursor = getContentResolver().query(Downloads.Impl.CONTENT_URI,
                new String[] { Downloads.Impl._DATA }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    fileSet.remove(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Iterator<String> iterator = fileSet.iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "deleting spurious file " + filename);
            }
            new File(filename).delete();
        }
    }
    private void trimDatabase() {
        Cursor cursor = getContentResolver().query(Downloads.Impl.CONTENT_URI,
                new String[] { Downloads.Impl._ID },
                Downloads.Impl.COLUMN_STATUS + " >= '200'", null,
                Downloads.Impl.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            Log.e(Constants.TAG, "null cursor in trimDatabase");
            return;
        }
        if (cursor.moveToFirst()) {
            int numDelete = cursor.getCount() - Constants.MAX_DOWNLOADS;
            int columnId = cursor.getColumnIndexOrThrow(Downloads.Impl._ID);
            while (numDelete > 0) {
                getContentResolver().delete(
                        ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI,
                        cursor.getLong(columnId)), null, null);
                if (!cursor.moveToNext()) {
                    break;
                }
                numDelete--;
            }
        }
        cursor.close();
    }
    private void insertDownload(
            Cursor cursor, int arrayPos,
            boolean networkAvailable, boolean networkRoaming, long now) {
        int statusColumn = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_STATUS);
        int failedColumn = cursor.getColumnIndexOrThrow(Constants.FAILED_CONNECTIONS);
        int retryRedirect =
                cursor.getInt(cursor.getColumnIndexOrThrow(Constants.RETRY_AFTER_X_REDIRECT_COUNT));
        DownloadInfo info = new DownloadInfo(
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Impl._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_URI)),
                cursor.getInt(cursor.getColumnIndexOrThrow(
                        Downloads.Impl.COLUMN_NO_INTEGRITY)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.Impl.COLUMN_FILE_NAME_HINT)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl._DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_MIME_TYPE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_DESTINATION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_VISIBILITY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_CONTROL)),
                cursor.getInt(statusColumn),
                cursor.getInt(failedColumn),
                retryRedirect & 0xfffffff,
                retryRedirect >> 28,
                cursor.getLong(cursor.getColumnIndexOrThrow(
                        Downloads.Impl.COLUMN_LAST_MODIFICATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.Impl.COLUMN_NOTIFICATION_CLASS)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_COOKIE_DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_USER_AGENT)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_REFERER)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_TOTAL_BYTES)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_CURRENT_BYTES)),
                cursor.getString(cursor.getColumnIndexOrThrow(Constants.ETAG)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Constants.MEDIA_SCANNED)) == 1);
        if (Constants.LOGVV) {
            Log.v(Constants.TAG, "Service adding new entry");
            Log.v(Constants.TAG, "ID      : " + info.mId);
            Log.v(Constants.TAG, "URI     : " + ((info.mUri != null) ? "yes" : "no"));
            Log.v(Constants.TAG, "NO_INTEG: " + info.mNoIntegrity);
            Log.v(Constants.TAG, "HINT    : " + info.mHint);
            Log.v(Constants.TAG, "FILENAME: " + info.mFileName);
            Log.v(Constants.TAG, "MIMETYPE: " + info.mMimeType);
            Log.v(Constants.TAG, "DESTINAT: " + info.mDestination);
            Log.v(Constants.TAG, "VISIBILI: " + info.mVisibility);
            Log.v(Constants.TAG, "CONTROL : " + info.mControl);
            Log.v(Constants.TAG, "STATUS  : " + info.mStatus);
            Log.v(Constants.TAG, "FAILED_C: " + info.mNumFailed);
            Log.v(Constants.TAG, "RETRY_AF: " + info.mRetryAfter);
            Log.v(Constants.TAG, "REDIRECT: " + info.mRedirectCount);
            Log.v(Constants.TAG, "LAST_MOD: " + info.mLastMod);
            Log.v(Constants.TAG, "PACKAGE : " + info.mPackage);
            Log.v(Constants.TAG, "CLASS   : " + info.mClass);
            Log.v(Constants.TAG, "COOKIES : " + ((info.mCookies != null) ? "yes" : "no"));
            Log.v(Constants.TAG, "AGENT   : " + info.mUserAgent);
            Log.v(Constants.TAG, "REFERER : " + ((info.mReferer != null) ? "yes" : "no"));
            Log.v(Constants.TAG, "TOTAL   : " + info.mTotalBytes);
            Log.v(Constants.TAG, "CURRENT : " + info.mCurrentBytes);
            Log.v(Constants.TAG, "ETAG    : " + info.mETag);
            Log.v(Constants.TAG, "SCANNED : " + info.mMediaScanned);
        }
        mDownloads.add(arrayPos, info);
        if (info.mStatus == 0
                && (info.mDestination == Downloads.Impl.DESTINATION_EXTERNAL
                    || info.mDestination == Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE)
                && info.mMimeType != null
                && !DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING.equalsIgnoreCase(info.mMimeType)) {
            Intent mimetypeIntent = new Intent(Intent.ACTION_VIEW);
            mimetypeIntent.setDataAndType(Uri.fromParts("file", "", null), info.mMimeType);
            ResolveInfo ri = getPackageManager().resolveActivity(mimetypeIntent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (ri == null) {
                if (Config.LOGD) {
                    Log.d(Constants.TAG, "no application to handle MIME type " + info.mMimeType);
                }
                info.mStatus = Downloads.Impl.STATUS_NOT_ACCEPTABLE;
                Uri uri = ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, info.mId);
                ContentValues values = new ContentValues();
                values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_NOT_ACCEPTABLE);
                getContentResolver().update(uri, values, null, null);
                info.sendIntentIfRequested(uri, this);
                return;
            }
        }
        if (info.canUseNetwork(networkAvailable, networkRoaming)) {
            if (info.isReadyToStart(now)) {
                if (Constants.LOGV) {
                    Log.v(Constants.TAG, "Service spawning thread to handle new download " +
                            info.mId);
                }
                if (info.mHasActiveThread) {
                    throw new IllegalStateException("Multiple threads on same download on insert");
                }
                if (info.mStatus != Downloads.Impl.STATUS_RUNNING) {
                    info.mStatus = Downloads.Impl.STATUS_RUNNING;
                    ContentValues values = new ContentValues();
                    values.put(Downloads.Impl.COLUMN_STATUS, info.mStatus);
                    getContentResolver().update(
                            ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, info.mId),
                            values, null, null);
                }
                DownloadThread downloader = new DownloadThread(this, info);
                info.mHasActiveThread = true;
                downloader.start();
            }
        } else {
            if (info.mStatus == 0
                    || info.mStatus == Downloads.Impl.STATUS_PENDING
                    || info.mStatus == Downloads.Impl.STATUS_RUNNING) {
                info.mStatus = Downloads.Impl.STATUS_RUNNING_PAUSED;
                Uri uri = ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, info.mId);
                ContentValues values = new ContentValues();
                values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_RUNNING_PAUSED);
                getContentResolver().update(uri, values, null, null);
            }
        }
    }
    private void updateDownload(
            Cursor cursor, int arrayPos,
            boolean networkAvailable, boolean networkRoaming, long now) {
        DownloadInfo info = (DownloadInfo) mDownloads.get(arrayPos);
        int statusColumn = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_STATUS);
        int failedColumn = cursor.getColumnIndexOrThrow(Constants.FAILED_CONNECTIONS);
        info.mId = cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.Impl._ID));
        info.mUri = stringFromCursor(info.mUri, cursor, Downloads.Impl.COLUMN_URI);
        info.mNoIntegrity = cursor.getInt(cursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_NO_INTEGRITY)) == 1;
        info.mHint = stringFromCursor(info.mHint, cursor, Downloads.Impl.COLUMN_FILE_NAME_HINT);
        info.mFileName = stringFromCursor(info.mFileName, cursor, Downloads.Impl._DATA);
        info.mMimeType = stringFromCursor(info.mMimeType, cursor, Downloads.Impl.COLUMN_MIME_TYPE);
        info.mDestination = cursor.getInt(cursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_DESTINATION));
        int newVisibility = cursor.getInt(cursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_VISIBILITY));
        if (info.mVisibility == Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && newVisibility != Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && Downloads.Impl.isStatusCompleted(info.mStatus)) {
            mNotifier.mNotificationMgr.cancel(info.mId);
        }
        info.mVisibility = newVisibility;
        synchronized (info) {
            info.mControl = cursor.getInt(cursor.getColumnIndexOrThrow(
                    Downloads.Impl.COLUMN_CONTROL));
        }
        int newStatus = cursor.getInt(statusColumn);
        if (!Downloads.Impl.isStatusCompleted(info.mStatus) &&
                    Downloads.Impl.isStatusCompleted(newStatus)) {
            mNotifier.mNotificationMgr.cancel(info.mId);
        }
        info.mStatus = newStatus;
        info.mNumFailed = cursor.getInt(failedColumn);
        int retryRedirect =
                cursor.getInt(cursor.getColumnIndexOrThrow(Constants.RETRY_AFTER_X_REDIRECT_COUNT));
        info.mRetryAfter = retryRedirect & 0xfffffff;
        info.mRedirectCount = retryRedirect >> 28;
        info.mLastMod = cursor.getLong(cursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_LAST_MODIFICATION));
        info.mPackage = stringFromCursor(
                info.mPackage, cursor, Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE);
        info.mClass = stringFromCursor(
                info.mClass, cursor, Downloads.Impl.COLUMN_NOTIFICATION_CLASS);
        info.mCookies = stringFromCursor(info.mCookies, cursor, Downloads.Impl.COLUMN_COOKIE_DATA);
        info.mUserAgent = stringFromCursor(
                info.mUserAgent, cursor, Downloads.Impl.COLUMN_USER_AGENT);
        info.mReferer = stringFromCursor(info.mReferer, cursor, Downloads.Impl.COLUMN_REFERER);
        info.mTotalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_TOTAL_BYTES));
        info.mCurrentBytes = cursor.getInt(cursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_CURRENT_BYTES));
        info.mETag = stringFromCursor(info.mETag, cursor, Constants.ETAG);
        info.mMediaScanned =
                cursor.getInt(cursor.getColumnIndexOrThrow(Constants.MEDIA_SCANNED)) == 1;
        if (info.canUseNetwork(networkAvailable, networkRoaming)) {
            if (info.isReadyToRestart(now)) {
                if (Constants.LOGV) {
                    Log.v(Constants.TAG, "Service spawning thread to handle updated download " +
                            info.mId);
                }
                if (info.mHasActiveThread) {
                    throw new IllegalStateException("Multiple threads on same download on update");
                }
                info.mStatus = Downloads.Impl.STATUS_RUNNING;
                ContentValues values = new ContentValues();
                values.put(Downloads.Impl.COLUMN_STATUS, info.mStatus);
                getContentResolver().update(
                        ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, info.mId),
                        values, null, null);
                DownloadThread downloader = new DownloadThread(this, info);
                info.mHasActiveThread = true;
                downloader.start();
            }
        }
    }
    private String stringFromCursor(String old, Cursor cursor, String column) {
        int index = cursor.getColumnIndexOrThrow(column);
        if (old == null) {
            return cursor.getString(index);
        }
        if (mNewChars == null) {
            mNewChars = new CharArrayBuffer(128);
        }
        cursor.copyStringToBuffer(index, mNewChars);
        int length = mNewChars.sizeCopied;
        if (length != old.length()) {
            return cursor.getString(index);
        }
        if (oldChars == null || oldChars.sizeCopied < length) {
            oldChars = new CharArrayBuffer(length);
        }
        char[] oldArray = oldChars.data;
        char[] newArray = mNewChars.data;
        old.getChars(0, length, oldArray, 0);
        for (int i = length - 1; i >= 0; --i) {
            if (oldArray[i] != newArray[i]) {
                return new String(newArray, 0, length);
            }
        }
        return old;
    }
    private void deleteDownload(int arrayPos) {
        DownloadInfo info = (DownloadInfo) mDownloads.get(arrayPos);
        if (info.mStatus == Downloads.Impl.STATUS_RUNNING) {
            info.mStatus = Downloads.Impl.STATUS_CANCELED;
        } else if (info.mDestination != Downloads.Impl.DESTINATION_EXTERNAL
                    && info.mFileName != null) {
            new File(info.mFileName).delete();
        }
        mNotifier.mNotificationMgr.cancel(info.mId);
        mDownloads.remove(arrayPos);
    }
    private long nextAction(int arrayPos, long now) {
        DownloadInfo info = (DownloadInfo) mDownloads.get(arrayPos);
        if (Downloads.Impl.isStatusCompleted(info.mStatus)) {
            return -1;
        }
        if (info.mStatus != Downloads.Impl.STATUS_RUNNING_PAUSED) {
            return 0;
        }
        if (info.mNumFailed == 0) {
            return 0;
        }
        long when = info.restartTime();
        if (when <= now) {
            return 0;
        }
        return when - now;
    }
    private boolean visibleNotification(int arrayPos) {
        DownloadInfo info = (DownloadInfo) mDownloads.get(arrayPos);
        return info.hasCompletionNotification();
    }
    private boolean shouldScanFile(int arrayPos) {
        DownloadInfo info = (DownloadInfo) mDownloads.get(arrayPos);
        return !info.mMediaScanned
                && info.mDestination == Downloads.Impl.DESTINATION_EXTERNAL
                && Downloads.Impl.isStatusSuccess(info.mStatus)
                && !DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING.equalsIgnoreCase(info.mMimeType);
    }
    private boolean mediaScannerConnected() {
        return mMediaScannerService != null;
    }
    private boolean scanFile(Cursor cursor, int arrayPos) {
        DownloadInfo info = (DownloadInfo) mDownloads.get(arrayPos);
        synchronized (this) {
            if (mMediaScannerService != null) {
                try {
                    if (Constants.LOGV) {
                        Log.v(Constants.TAG, "Scanning file " + info.mFileName);
                    }
                    mMediaScannerService.scanFile(info.mFileName, info.mMimeType);
                    if (cursor != null) {
                        ContentValues values = new ContentValues();
                        values.put(Constants.MEDIA_SCANNED, 1);
                        getContentResolver().update(ContentUris.withAppendedId(
                                       Downloads.Impl.CONTENT_URI, cursor.getLong(
                                               cursor.getColumnIndexOrThrow(Downloads.Impl._ID))),
                                values, null, null);
                    }
                    return true;
                } catch (RemoteException e) {
                    if (Config.LOGD) {
                        Log.d(Constants.TAG, "Failed to scan file " + info.mFileName);
                    }
                }
            }
        }
        return false;
    }
}
