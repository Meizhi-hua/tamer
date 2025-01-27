public class RetryScheduler implements Observer {
    private static final String TAG = "RetryScheduler";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private RetryScheduler(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }
    private static RetryScheduler sInstance;
    public static RetryScheduler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RetryScheduler(context);
        }
        return sInstance;
    }
    private boolean isConnected() {
        ConnectivityManager mConnMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).
                isConnected());
    }
    public void update(Observable observable) {
        try {
            Transaction t = (Transaction) observable;
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "[RetryScheduler] update " + observable);
            }
            if ((t instanceof NotificationTransaction)
                    || (t instanceof RetrieveTransaction)
                    || (t instanceof ReadRecTransaction)
                    || (t instanceof SendTransaction)) {
                try {
                    TransactionState state = t.getState();
                    if (state.getState() == TransactionState.FAILED) {
                        Uri uri = state.getContentUri();
                        if (uri != null) {
                            scheduleRetry(uri);
                        }
                    }
                } finally {
                    t.detach(this);
                }
            }
        } finally {
            if (isConnected()) {
                setRetryAlarm(mContext);
            }
        }
    }
    private void scheduleRetry(Uri uri) {
        long msgId = ContentUris.parseId(uri);
        Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                uriBuilder.build(), null, null, null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    int msgType = cursor.getInt(cursor.getColumnIndexOrThrow(
                            PendingMessages.MSG_TYPE));
                    int retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow(
                            PendingMessages.RETRY_INDEX)) + 1; 
                    int errorType = MmsSms.ERR_TYPE_GENERIC;
                    DefaultRetryScheme scheme = new DefaultRetryScheme(mContext, retryIndex);
                    ContentValues values = new ContentValues(4);
                    long current = System.currentTimeMillis();
                    boolean isRetryDownloading =
                            (msgType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND);
                    boolean retry = true;
                    int respStatus = getResponseStatus(msgId);
                    if (respStatus == PduHeaders.RESPONSE_STATUS_ERROR_SENDING_ADDRESS_UNRESOLVED) {
                        DownloadManager.getInstance().showErrorCodeToast(R.string.invalid_destination);
                        retry = false;
                    }
                    if ((retryIndex < scheme.getRetryLimit()) && retry) {
                        long retryAt = current + scheme.getWaitingInterval();
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "scheduleRetry: retry for " + uri + " is scheduled at "
                                    + (retryAt - System.currentTimeMillis()) + "ms from now");
                        }
                        values.put(PendingMessages.DUE_TIME, retryAt);
                        if (isRetryDownloading) {
                            DownloadManager.getInstance().markState(
                                    uri, DownloadManager.STATE_TRANSIENT_FAILURE);
                        }
                    } else {
                        errorType = MmsSms.ERR_TYPE_GENERIC_PERMANENT;
                        if (isRetryDownloading) {
                            Cursor c = SqliteWrapper.query(mContext, mContext.getContentResolver(), uri,
                                    new String[] { Mms.THREAD_ID }, null, null, null);
                            long threadId = -1;
                            if (c != null) {
                                try {
                                    if (c.moveToFirst()) {
                                        threadId = c.getLong(0);
                                    }
                                } finally {
                                    c.close();
                                }
                            }
                            if (threadId != -1) {
                                MessagingNotification.notifyDownloadFailed(mContext, threadId);
                            }
                            DownloadManager.getInstance().markState(
                                    uri, DownloadManager.STATE_PERMANENT_FAILURE);
                        } else {
                            ContentValues readValues = new ContentValues(1);
                            readValues.put(Mms.READ, 0);
                            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                    uri, readValues, null, null);
                            MessagingNotification.notifySendFailed(mContext, true);
                        }
                    }
                    values.put(PendingMessages.ERROR_TYPE,  errorType);
                    values.put(PendingMessages.RETRY_INDEX, retryIndex);
                    values.put(PendingMessages.LAST_TRY,    current);
                    int columnIndex = cursor.getColumnIndexOrThrow(
                            PendingMessages._ID);
                    long id = cursor.getLong(columnIndex);
                    SqliteWrapper.update(mContext, mContentResolver,
                            PendingMessages.CONTENT_URI,
                            values, PendingMessages._ID + "=" + id, null);
                } else if (LOCAL_LOGV) {
                    Log.v(TAG, "Cannot found correct pending status for: " + msgId);
                }
            } finally {
                cursor.close();
            }
        }
    }
    private int getResponseStatus(long msgID) {
        int respStatus = 0;
        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                Mms.Outbox.CONTENT_URI, null, Mms._ID + "=" + msgID, null, null);
        try {
            if (cursor.moveToFirst()) {
                respStatus = cursor.getInt(cursor.getColumnIndexOrThrow(Mms.RESPONSE_STATUS));
            }
        } finally {
            cursor.close();
        }
        if (respStatus != 0) {
            Log.e(TAG, "Response status is: " + respStatus);
        }
        return respStatus;
    }
    public static void setRetryAlarm(Context context) {
        Cursor cursor = PduPersister.getPduPersister(context).getPendingMessages(
                Long.MAX_VALUE);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long retryAt = cursor.getLong(cursor.getColumnIndexOrThrow(
                            PendingMessages.DUE_TIME));
                    Intent service = new Intent(TransactionService.ACTION_ONALARM,
                                        null, context, TransactionService.class);
                    PendingIntent operation = PendingIntent.getService(
                            context, 0, service, PendingIntent.FLAG_ONE_SHOT);
                    AlarmManager am = (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC, retryAt, operation);
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Next retry is scheduled at"
                                + (retryAt - System.currentTimeMillis()) + "ms from now");
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }
}
