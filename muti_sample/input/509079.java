public abstract class Recycler {
    private static final boolean LOCAL_DEBUG = false;
    private static final String TAG = "Recycler";
    private static final boolean DEFAULT_AUTO_DELETE  = false;
    private static SmsRecycler sSmsRecycler;
    private static MmsRecycler sMmsRecycler;
    public static SmsRecycler getSmsRecycler() {
        if (sSmsRecycler == null) {
            sSmsRecycler = new SmsRecycler();
        }
        return sSmsRecycler;
    }
    public static MmsRecycler getMmsRecycler() {
        if (sMmsRecycler == null) {
            sMmsRecycler = new MmsRecycler();
        }
        return sMmsRecycler;
    }
    public static boolean checkForThreadsOverLimit(Context context) {
        Recycler smsRecycler = getSmsRecycler();
        Recycler mmsRecycler = getMmsRecycler();
        return smsRecycler.anyThreadOverLimit(context) || mmsRecycler.anyThreadOverLimit(context);
    }
    public void deleteOldMessages(Context context) {
        if (LOCAL_DEBUG) {
            Log.v(TAG, "Recycler.deleteOldMessages this: " + this);
        }
        if (!isAutoDeleteEnabled(context)) {
            return;
        }
        Cursor cursor = getAllThreads(context);
        try {
            int limit = getMessageLimit(context);
            while (cursor.moveToNext()) {
                long threadId = getThreadId(cursor);
                deleteMessagesForThread(context, threadId, limit);
            }
        } finally {
            cursor.close();
        }
    }
    public void deleteOldMessagesByThreadId(Context context, long threadId) {
        if (LOCAL_DEBUG) {
            Log.v(TAG, "Recycler.deleteOldMessagesByThreadId this: " + this +
                    " threadId: " + threadId);
        }
        if (!isAutoDeleteEnabled(context)) {
            return;
        }
        deleteMessagesForThread(context, threadId, getMessageLimit(context));
    }
    public static boolean isAutoDeleteEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(MessagingPreferenceActivity.AUTO_DELETE,
                DEFAULT_AUTO_DELETE);
    }
    abstract public int getMessageLimit(Context context);
    abstract public void setMessageLimit(Context context, int limit);
    public int getMessageMinLimit() {
        return MmsConfig.getMinMessageCountPerThread();
    }
    public int getMessageMaxLimit() {
        return MmsConfig.getMaxMessageCountPerThread();
    }
    abstract protected long getThreadId(Cursor cursor);
    abstract protected Cursor getAllThreads(Context context);
    abstract protected void deleteMessagesForThread(Context context, long threadId, int keep);
    abstract protected void dumpMessage(Cursor cursor, Context context);
    abstract protected boolean anyThreadOverLimit(Context context);
    public static class SmsRecycler extends Recycler {
        private static final String[] ALL_SMS_THREADS_PROJECTION = {
            Telephony.Sms.Conversations.THREAD_ID,
            Telephony.Sms.Conversations.MESSAGE_COUNT
        };
        private static final int ID             = 0;
        private static final int MESSAGE_COUNT  = 1;
        static private final String[] SMS_MESSAGE_PROJECTION = new String[] {
            BaseColumns._ID,
            Conversations.THREAD_ID,
            Sms.ADDRESS,
            Sms.BODY,
            Sms.DATE,
            Sms.READ,
            Sms.TYPE,
            Sms.STATUS,
        };
        static private final int COLUMN_ID                  = 0;
        static private final int COLUMN_THREAD_ID           = 1;
        static private final int COLUMN_SMS_ADDRESS         = 2;
        static private final int COLUMN_SMS_BODY            = 3;
        static private final int COLUMN_SMS_DATE            = 4;
        static private final int COLUMN_SMS_READ            = 5;
        static private final int COLUMN_SMS_TYPE            = 6;
        static private final int COLUMN_SMS_STATUS          = 7;
        private final String MAX_SMS_MESSAGES_PER_THREAD = "MaxSmsMessagesPerThread";
        public int getMessageLimit(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getInt(MAX_SMS_MESSAGES_PER_THREAD,
                    MmsConfig.getDefaultSMSMessagesPerThread());
        }
        public void setMessageLimit(Context context, int limit) {
            SharedPreferences.Editor editPrefs =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt(MAX_SMS_MESSAGES_PER_THREAD, limit);
            editPrefs.commit();
        }
        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(ID);
        }
        protected Cursor getAllThreads(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = SqliteWrapper.query(context, resolver,
                    Telephony.Sms.Conversations.CONTENT_URI,
                    ALL_SMS_THREADS_PROJECTION, null, null, Conversations.DEFAULT_SORT_ORDER);
            return cursor;
        }
        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            if (LOCAL_DEBUG) {
                Log.v(TAG, "SMS: deleteMessagesForThread");
            }
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, resolver,
                        ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                        SMS_MESSAGE_PROJECTION,
                        "locked=0",
                        null, "date DESC");     
                if (cursor == null) {
                    Log.e(TAG, "SMS: deleteMessagesForThread got back null cursor");
                    return;
                }
                int count = cursor.getCount();
                int numberToDelete = count - keep;
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "SMS: deleteMessagesForThread keep: " + keep +
                            " count: " + count +
                            " numberToDelete: " + numberToDelete);
                }
                if (numberToDelete <= 0) {
                    return;
                }
                cursor.move(keep);
                long latestDate = cursor.getLong(COLUMN_SMS_DATE);
                long cntDeleted = SqliteWrapper.delete(context, resolver,
                        ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                        "locked=0 AND date<" + latestDate,
                        null);
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "SMS: deleteMessagesForThread cntDeleted: " + cntDeleted);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        protected void dumpMessage(Cursor cursor, Context context) {
            long date = cursor.getLong(COLUMN_SMS_DATE);
            String dateStr = MessageUtils.formatTimeStampString(context, date, true);
            if (LOCAL_DEBUG) {
                Log.v(TAG, "Recycler message " +
                        "\n    address: " + cursor.getString(COLUMN_SMS_ADDRESS) +
                        "\n    body: " + cursor.getString(COLUMN_SMS_BODY) +
                        "\n    date: " + dateStr +
                        "\n    date: " + date +
                        "\n    read: " + cursor.getInt(COLUMN_SMS_READ));
            }
        }
        @Override
        protected boolean anyThreadOverLimit(Context context) {
            Cursor cursor = getAllThreads(context);
            int limit = getMessageLimit(context);
            try {
                while (cursor.moveToNext()) {
                    long threadId = getThreadId(cursor);
                    ContentResolver resolver = context.getContentResolver();
                    Cursor msgs = SqliteWrapper.query(context, resolver,
                            ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                            SMS_MESSAGE_PROJECTION,
                            "locked=0",
                            null, "date DESC");     
                    if (msgs.getCount() >= limit) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
            return false;
        }
    }
    public static class MmsRecycler extends Recycler {
        private static final String[] ALL_MMS_THREADS_PROJECTION = {
            "thread_id", "count(*) as msg_count"
        };
        private static final int ID             = 0;
        private static final int MESSAGE_COUNT  = 1;
        static private final String[] MMS_MESSAGE_PROJECTION = new String[] {
            BaseColumns._ID,
            Conversations.THREAD_ID,
            Mms.DATE,
        };
        static private final int COLUMN_ID                  = 0;
        static private final int COLUMN_THREAD_ID           = 1;
        static private final int COLUMN_MMS_DATE            = 2;
        static private final int COLUMN_MMS_READ            = 3;
        private final String MAX_MMS_MESSAGES_PER_THREAD = "MaxMmsMessagesPerThread";
        public int getMessageLimit(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getInt(MAX_MMS_MESSAGES_PER_THREAD,
                    MmsConfig.getDefaultMMSMessagesPerThread());
        }
        public void setMessageLimit(Context context, int limit) {
            SharedPreferences.Editor editPrefs =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt(MAX_MMS_MESSAGES_PER_THREAD, limit);
            editPrefs.commit();
        }
        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(ID);
        }
        protected Cursor getAllThreads(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = SqliteWrapper.query(context, resolver,
                    Uri.withAppendedPath(Telephony.Mms.CONTENT_URI, "threads"),
                    ALL_MMS_THREADS_PROJECTION, null, null, Conversations.DEFAULT_SORT_ORDER);
            return cursor;
        }
        public void deleteOldMessagesInSameThreadAsMessage(Context context, Uri uri) {
            if (LOCAL_DEBUG) {
                Log.v(TAG, "MMS: deleteOldMessagesByUri");
            }
            if (!isAutoDeleteEnabled(context)) {
                return;
            }
            Cursor cursor = null;
            long latestDate = 0;
            long threadId = 0;
            try {
                String msgId = uri.getLastPathSegment();
                ContentResolver resolver = context.getContentResolver();
                cursor = SqliteWrapper.query(context, resolver,
                        Telephony.Mms.CONTENT_URI,
                        MMS_MESSAGE_PROJECTION,
                        "thread_id in (select thread_id from pdu where _id=" + msgId +
                            ") AND locked=0",
                        null, "date DESC");     
                if (cursor == null) {
                    Log.e(TAG, "MMS: deleteOldMessagesInSameThreadAsMessage got back null cursor");
                    return;
                }
                int count = cursor.getCount();
                int keep = getMessageLimit(context);
                int numberToDelete = count - keep;
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "MMS: deleteOldMessagesByUri keep: " + keep +
                            " count: " + count +
                            " numberToDelete: " + numberToDelete);
                }
                if (numberToDelete <= 0) {
                    return;
                }
                cursor.move(keep);
                latestDate = cursor.getLong(COLUMN_MMS_DATE);
                threadId = cursor.getLong(COLUMN_THREAD_ID);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (threadId != 0) {
                deleteMessagesOlderThanDate(context, threadId, latestDate);
            }
        }
        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            if (LOCAL_DEBUG) {
                Log.v(TAG, "MMS: deleteMessagesForThread");
            }
            if (threadId == 0) {
                return;
            }
            Cursor cursor = null;
            long latestDate = 0;
            try {
                ContentResolver resolver = context.getContentResolver();
                cursor = SqliteWrapper.query(context, resolver,
                        Telephony.Mms.CONTENT_URI,
                        MMS_MESSAGE_PROJECTION,
                        "thread_id=" + threadId + " AND locked=0",
                        null, "date DESC");     
                if (cursor == null) {
                    Log.e(TAG, "MMS: deleteMessagesForThread got back null cursor");
                    return;
                }
                int count = cursor.getCount();
                int numberToDelete = count - keep;
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "MMS: deleteMessagesForThread keep: " + keep +
                            " count: " + count +
                            " numberToDelete: " + numberToDelete);
                }
                if (numberToDelete <= 0) {
                    return;
                }
                cursor.move(keep);
                latestDate = cursor.getLong(COLUMN_MMS_DATE);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            deleteMessagesOlderThanDate(context, threadId, latestDate);
        }
        private void deleteMessagesOlderThanDate(Context context, long threadId,
                long latestDate) {
            long cntDeleted = SqliteWrapper.delete(context, context.getContentResolver(),
                    Telephony.Mms.CONTENT_URI,
                    "thread_id=" + threadId + " AND locked=0 AND date<" + latestDate,
                    null);
            if (LOCAL_DEBUG) {
                Log.v(TAG, "MMS: deleteMessagesOlderThanDate cntDeleted: " + cntDeleted);
            }
        }
        protected void dumpMessage(Cursor cursor, Context context) {
            long id = cursor.getLong(COLUMN_ID);
            if (LOCAL_DEBUG) {
                Log.v(TAG, "Recycler message " +
                        "\n    id: " + id
                );
            }
        }
        @Override
        protected boolean anyThreadOverLimit(Context context) {
            Cursor cursor = getAllThreads(context);
            int limit = getMessageLimit(context);
            try {
                while (cursor.moveToNext()) {
                    long threadId = getThreadId(cursor);
                    ContentResolver resolver = context.getContentResolver();
                    Cursor msgs = SqliteWrapper.query(context, resolver,
                            Telephony.Mms.CONTENT_URI,
                            MMS_MESSAGE_PROJECTION,
                            "thread_id=" + threadId + " AND locked=0",
                            null, "date DESC");     
                    if (msgs.getCount() >= limit) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
            return false;
        }
    }
}
