public class SmsProvider extends ContentProvider {
    private static final Uri NOTIFICATION_URI = Uri.parse("content:
    private static final Uri ICC_URI = Uri.parse("content:
    static final String TABLE_SMS = "sms";
    private static final String TABLE_RAW = "raw";
    private static final String TABLE_SR_PENDING = "sr_pending";
    private static final String TABLE_WORDS = "words";
    private static final Integer ONE = Integer.valueOf(1);
    private static final String[] CONTACT_QUERY_PROJECTION =
            new String[] { Contacts.Phones.PERSON_ID };
    private static final int PERSON_ID_COLUMN = 0;
    private final static String[] ICC_COLUMNS = new String[] {
        "service_center_address",       
        "address",                      
        "message_class",                
        "body",                         
        "date",                         
        "status",                       
        "index_on_icc",                 
        "is_status_report",             
        "transport_type",               
        "type",                         
        "locked",                       
        "error_code"                    
    };
    @Override
    public boolean onCreate() {
        mOpenHelper = MmsSmsDatabaseHelper.getInstance(getContext());
        return true;
    }
    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int match = sURLMatcher.match(url);
        switch (match) {
            case SMS_ALL:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_ALL);
                break;
            case SMS_UNDELIVERED:
                constructQueryForUndelivered(qb);
                break;
            case SMS_FAILED:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_FAILED);
                break;
            case SMS_QUEUED:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_QUEUED);
                break;
            case SMS_INBOX:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_INBOX);
                break;
            case SMS_SENT:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_SENT);
                break;
            case SMS_DRAFT:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_DRAFT);
                break;
            case SMS_OUTBOX:
                constructQueryForBox(qb, Sms.MESSAGE_TYPE_OUTBOX);
                break;
            case SMS_ALL_ID:
                qb.setTables(TABLE_SMS);
                qb.appendWhere("(_id = " + url.getPathSegments().get(0) + ")");
                break;
            case SMS_INBOX_ID:
            case SMS_FAILED_ID:
            case SMS_SENT_ID:
            case SMS_DRAFT_ID:
            case SMS_OUTBOX_ID:
                qb.setTables(TABLE_SMS);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;
            case SMS_CONVERSATIONS_ID:
                int threadID;
                try {
                    threadID = Integer.parseInt(url.getPathSegments().get(1));
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.d(TAG, "query conversations: threadID=" + threadID);
                    }
                }
                catch (Exception ex) {
                    Log.e(TAG,
                          "Bad conversation thread id: "
                          + url.getPathSegments().get(1));
                    return null;
                }
                qb.setTables(TABLE_SMS);
                qb.appendWhere("thread_id = " + threadID);
                break;
            case SMS_CONVERSATIONS:
                qb.setTables("sms, (SELECT thread_id AS group_thread_id, MAX(date)AS group_date,"
                       + "COUNT(*) AS msg_count FROM sms GROUP BY thread_id) AS groups");
                qb.appendWhere("sms.thread_id = groups.group_thread_id AND sms.date ="
                       + "groups.group_date");
                qb.setProjectionMap(sConversationProjectionMap);
                break;
            case SMS_RAW_MESSAGE:
                qb.setTables("raw");
                break;
            case SMS_STATUS_PENDING:
                qb.setTables("sr_pending");
                break;
            case SMS_ATTACHMENT:
                qb.setTables("attachments");
                break;
            case SMS_ATTACHMENT_ID:
                qb.setTables("attachments");
                qb.appendWhere(
                        "(sms_id = " + url.getPathSegments().get(1) + ")");
                break;
            case SMS_QUERY_THREAD_ID:
                qb.setTables("canonical_addresses");
                if (projectionIn == null) {
                    projectionIn = sIDProjection;
                }
                break;
            case SMS_STATUS_ID:
                qb.setTables(TABLE_SMS);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;
            case SMS_ALL_ICC:
                return getAllMessagesFromIcc();
            case SMS_ICC:
                String messageIndexString = url.getPathSegments().get(1);
                return getSingleMessageFromIcc(messageIndexString);
            default:
                Log.e(TAG, "Invalid request: " + url);
                return null;
        }
        String orderBy = null;
        if (!TextUtils.isEmpty(sort)) {
            orderBy = sort;
        } else if (qb.getTables().equals(TABLE_SMS)) {
            orderBy = Sms.DEFAULT_SORT_ORDER;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, orderBy);
        ret.setNotificationUri(getContext().getContentResolver(),
                NOTIFICATION_URI);
        return ret;
    }
    private ArrayList<String> convertIccToSms(SmsMessage message) {
        ArrayList result = new ArrayList();
        result.add(message.getServiceCenterAddress());
        result.add(message.getDisplayOriginatingAddress());
        result.add(String.valueOf(message.getMessageClass()));
        result.add(message.getDisplayMessageBody());
        result.add(message.getTimestampMillis());
        result.add(Sms.STATUS_NONE);
        result.add(message.getIndexOnIcc());
        result.add(message.isStatusReportMessage());
        result.add("sms");
        result.add(TextBasedSmsColumns.MESSAGE_TYPE_ALL);
        result.add(0);      
        result.add(0);      
        return result;
    }
    private Cursor getSingleMessageFromIcc(String messageIndexString) {
        try {
            int messageIndex = Integer.parseInt(messageIndexString);
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<SmsMessage> messages = smsManager.getAllMessagesFromIcc();
            ArrayList<ArrayList> singleRow = new ArrayList<ArrayList>();
            SmsMessage message = messages.get(messageIndex);
            if (message == null) {
                throw new IllegalArgumentException(
                        "Message not retrieved. ID: " + messageIndexString);
            }
            singleRow.add(convertIccToSms(message));
            return withIccNotificationUri(
                    new ArrayListCursor(ICC_COLUMNS, singleRow));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Bad SMS ICC ID: " + messageIndexString);
        }
    }
    private Cursor getAllMessagesFromIcc() {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<SmsMessage> messages = smsManager.getAllMessagesFromIcc();
        ArrayList<ArrayList> rows = new ArrayList<ArrayList>();
        for (int count = messages.size(), i = 0; i < count; i++) {
            SmsMessage message = messages.get(i);
            if (message != null) {
                rows.add(convertIccToSms(message));
            }
        }
        return withIccNotificationUri(new ArrayListCursor(ICC_COLUMNS, rows));
    }
    private Cursor withIccNotificationUri(Cursor cursor) {
        cursor.setNotificationUri(getContext().getContentResolver(), ICC_URI);
        return cursor;
    }
    private void constructQueryForBox(SQLiteQueryBuilder qb, int type) {
        qb.setTables(TABLE_SMS);
        if (type != Sms.MESSAGE_TYPE_ALL) {
            qb.appendWhere("type=" + type);
        }
    }
    private void constructQueryForUndelivered(SQLiteQueryBuilder qb) {
        qb.setTables(TABLE_SMS);
        qb.appendWhere("(type=" + Sms.MESSAGE_TYPE_OUTBOX +
                       " OR type=" + Sms.MESSAGE_TYPE_FAILED +
                       " OR type=" + Sms.MESSAGE_TYPE_QUEUED + ")");
    }
    @Override
    public String getType(Uri url) {
        switch (url.getPathSegments().size()) {
        case 0:
            return VND_ANDROID_DIR_SMS;
            case 1:
                try {
                    Integer.parseInt(url.getPathSegments().get(0));
                    return VND_ANDROID_SMS;
                } catch (NumberFormatException ex) {
                    return VND_ANDROID_DIR_SMS;
                }
            case 2:
                if (url.getPathSegments().get(0).equals("conversations")) {
                    return VND_ANDROID_SMSCHAT;
                } else {
                    return VND_ANDROID_SMS;
                }
        }
        return null;
    }
    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        ContentValues values;
        long rowID;
        int type = Sms.MESSAGE_TYPE_ALL;
        int match = sURLMatcher.match(url);
        String table = TABLE_SMS;
        switch (match) {
            case SMS_ALL:
                Integer typeObj = initialValues.getAsInteger(Sms.TYPE);
                if (typeObj != null) {
                    type = typeObj.intValue();
                } else {
                    type = Sms.MESSAGE_TYPE_INBOX;
                }
                break;
            case SMS_INBOX:
                type = Sms.MESSAGE_TYPE_INBOX;
                break;
            case SMS_FAILED:
                type = Sms.MESSAGE_TYPE_FAILED;
                break;
            case SMS_QUEUED:
                type = Sms.MESSAGE_TYPE_QUEUED;
                break;
            case SMS_SENT:
                type = Sms.MESSAGE_TYPE_SENT;
                break;
            case SMS_DRAFT:
                type = Sms.MESSAGE_TYPE_DRAFT;
                break;
            case SMS_OUTBOX:
                type = Sms.MESSAGE_TYPE_OUTBOX;
                break;
            case SMS_RAW_MESSAGE:
                table = "raw";
                break;
            case SMS_STATUS_PENDING:
                table = "sr_pending";
                break;
            case SMS_ATTACHMENT:
                table = "attachments";
                break;
            case SMS_NEW_THREAD_ID:
                table = "canonical_addresses";
                break;
            default:
                Log.e(TAG, "Invalid request: " + url);
                return null;
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (table.equals(TABLE_SMS)) {
            boolean addDate = false;
            boolean addType = false;
            if (initialValues == null) {
                values = new ContentValues(1);
                addDate = true;
                addType = true;
            } else {
                values = new ContentValues(initialValues);
                if (!initialValues.containsKey(Sms.DATE)) {
                    addDate = true;
                }
                if (!initialValues.containsKey(Sms.TYPE)) {
                    addType = true;
                }
            }
            if (addDate) {
                values.put(Sms.DATE, new Long(System.currentTimeMillis()));
            }
            if (addType && (type != Sms.MESSAGE_TYPE_ALL)) {
                values.put(Sms.TYPE, Integer.valueOf(type));
            }
            Long threadId = values.getAsLong(Sms.THREAD_ID);
            String address = values.getAsString(Sms.ADDRESS);
            if (((threadId == null) || (threadId == 0)) && (address != null)) {
                values.put(Sms.THREAD_ID, Threads.getOrCreateThreadId(
                                   getContext(), address));
            }
            if (values.getAsInteger(Sms.TYPE) == Sms.MESSAGE_TYPE_DRAFT) {
                db.delete(TABLE_SMS, "thread_id=? AND type=?",
                        new String[] { values.getAsString(Sms.THREAD_ID),
                                       Integer.toString(Sms.MESSAGE_TYPE_DRAFT) });
            }
            if (type == Sms.MESSAGE_TYPE_INBOX) {
                if ((values.getAsLong(Sms.PERSON) == null) && (!TextUtils.isEmpty(address))) {
                    Cursor cursor = null;
                    Uri uri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL,
                            Uri.encode(address));
                    try {
                        cursor = getContext().getContentResolver().query(
                                uri,
                                CONTACT_QUERY_PROJECTION,
                                null, null, null);
                        if (cursor.moveToFirst()) {
                            Long id = Long.valueOf(cursor.getLong(PERSON_ID_COLUMN));
                            values.put(Sms.PERSON, id);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "insert: query contact uri " + uri + " caught ", ex);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            } else {
                values.put(Sms.READ, ONE);
            }
        } else {
            if (initialValues == null) {
                values = new ContentValues(1);
            } else {
                values = initialValues;
            }
        }
        rowID = db.insert(table, "body", values);
        if (table == TABLE_SMS) {
            ContentValues cv = new ContentValues();
            cv.put(Telephony.MmsSms.WordsTable.ID, rowID);
            cv.put(Telephony.MmsSms.WordsTable.INDEXED_TEXT, values.getAsString("body"));
            cv.put(Telephony.MmsSms.WordsTable.SOURCE_ROW_ID, rowID);
            cv.put(Telephony.MmsSms.WordsTable.TABLE_ID, 1);
            db.insert(TABLE_WORDS, Telephony.MmsSms.WordsTable.INDEXED_TEXT, cv);
        }
        if (rowID > 0) {
            Uri uri = Uri.parse("content:
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "insert " + uri + " succeeded");
            }
            notifyChange(uri);
            return uri;
        } else {
            Log.e(TAG,"insert: failed! " + values.toString());
        }
        return null;
    }
    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        int count;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case SMS_ALL:
                count = db.delete(TABLE_SMS, where, whereArgs);
                if (count != 0) {
                    MmsSmsDatabaseHelper.updateAllThreads(db, where, whereArgs);
                }
                break;
            case SMS_ALL_ID:
                try {
                    int message_id = Integer.parseInt(url.getPathSegments().get(0));
                    count = MmsSmsDatabaseHelper.deleteOneSms(db, message_id);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Bad message id: " + url.getPathSegments().get(0));
                }
                break;
            case SMS_CONVERSATIONS_ID:
                int threadID;
                try {
                    threadID = Integer.parseInt(url.getPathSegments().get(1));
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            "Bad conversation thread id: "
                            + url.getPathSegments().get(1));
                }
                where = DatabaseUtils.concatenateWhere("thread_id=" + threadID, where);
                count = db.delete(TABLE_SMS, where, whereArgs);
                MmsSmsDatabaseHelper.updateThread(db, threadID);
                break;
            case SMS_RAW_MESSAGE:
                count = db.delete("raw", where, whereArgs);
                break;
            case SMS_STATUS_PENDING:
                count = db.delete("sr_pending", where, whereArgs);
                break;
            case SMS_ICC:
                String messageIndexString = url.getPathSegments().get(1);
                return deleteMessageFromIcc(messageIndexString);
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
        if (count > 0) {
            notifyChange(url);
        }
        return count;
    }
    private int deleteMessageFromIcc(String messageIndexString) {
        SmsManager smsManager = SmsManager.getDefault();
        try {
            return smsManager.deleteMessageFromIcc(
                    Integer.parseInt(messageIndexString))
                    ? 1 : 0;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Bad SMS ICC ID: " + messageIndexString);
        } finally {
            ContentResolver cr = getContext().getContentResolver();
            cr.notifyChange(ICC_URI, null);
        }
    }
    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count = 0;
        String table = TABLE_SMS;
        String extraWhere = null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(url)) {
            case SMS_RAW_MESSAGE:
                table = TABLE_RAW;
                break;
            case SMS_STATUS_PENDING:
                table = TABLE_SR_PENDING;
                break;
            case SMS_ALL:
            case SMS_FAILED:
            case SMS_QUEUED:
            case SMS_INBOX:
            case SMS_SENT:
            case SMS_DRAFT:
            case SMS_OUTBOX:
            case SMS_CONVERSATIONS:
                break;
            case SMS_ALL_ID:
                extraWhere = "_id=" + url.getPathSegments().get(0);
                break;
            case SMS_INBOX_ID:
            case SMS_FAILED_ID:
            case SMS_SENT_ID:
            case SMS_DRAFT_ID:
            case SMS_OUTBOX_ID:
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;
            case SMS_CONVERSATIONS_ID: {
                String threadId = url.getPathSegments().get(1);
                try {
                    Integer.parseInt(threadId);
                } catch (Exception ex) {
                    Log.e(TAG, "Bad conversation thread id: " + threadId);
                    break;
                }
                extraWhere = "thread_id=" + threadId;
                break;
            }
            case SMS_STATUS_ID:
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;
            default:
                throw new UnsupportedOperationException(
                        "URI " + url + " not supported");
        }
        where = DatabaseUtils.concatenateWhere(where, extraWhere);
        count = db.update(table, values, where, whereArgs);
        if (count > 0) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "update " + url + " succeeded");
            }
            notifyChange(url);
        }
        return count;
    }
    private void notifyChange(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        cr.notifyChange(uri, null);
        cr.notifyChange(MmsSms.CONTENT_URI, null);
        cr.notifyChange(Uri.parse("content:
    }
    private SQLiteOpenHelper mOpenHelper;
    private final static String TAG = "SmsProvider";
    private final static String VND_ANDROID_SMS = "vnd.android.cursor.item/sms";
    private final static String VND_ANDROID_SMSCHAT =
            "vnd.android.cursor.item/sms-chat";
    private final static String VND_ANDROID_DIR_SMS =
            "vnd.android.cursor.dir/sms";
    private static final HashMap<String, String> sConversationProjectionMap =
            new HashMap<String, String>();
    private static final String[] sIDProjection = new String[] { "_id" };
    private static final int SMS_ALL = 0;
    private static final int SMS_ALL_ID = 1;
    private static final int SMS_INBOX = 2;
    private static final int SMS_INBOX_ID = 3;
    private static final int SMS_SENT = 4;
    private static final int SMS_SENT_ID = 5;
    private static final int SMS_DRAFT = 6;
    private static final int SMS_DRAFT_ID = 7;
    private static final int SMS_OUTBOX = 8;
    private static final int SMS_OUTBOX_ID = 9;
    private static final int SMS_CONVERSATIONS = 10;
    private static final int SMS_CONVERSATIONS_ID = 11;
    private static final int SMS_RAW_MESSAGE = 15;
    private static final int SMS_ATTACHMENT = 16;
    private static final int SMS_ATTACHMENT_ID = 17;
    private static final int SMS_NEW_THREAD_ID = 18;
    private static final int SMS_QUERY_THREAD_ID = 19;
    private static final int SMS_STATUS_ID = 20;
    private static final int SMS_STATUS_PENDING = 21;
    private static final int SMS_ALL_ICC = 22;
    private static final int SMS_ICC = 23;
    private static final int SMS_FAILED = 24;
    private static final int SMS_FAILED_ID = 25;
    private static final int SMS_QUEUED = 26;
    private static final int SMS_UNDELIVERED = 27;
    private static final UriMatcher sURLMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURLMatcher.addURI("sms", null, SMS_ALL);
        sURLMatcher.addURI("sms", "#", SMS_ALL_ID);
        sURLMatcher.addURI("sms", "inbox", SMS_INBOX);
        sURLMatcher.addURI("sms", "inbox/#", SMS_INBOX_ID);
        sURLMatcher.addURI("sms", "sent", SMS_SENT);
        sURLMatcher.addURI("sms", "sent/#", SMS_SENT_ID);
        sURLMatcher.addURI("sms", "draft", SMS_DRAFT);
        sURLMatcher.addURI("sms", "draft/#", SMS_DRAFT_ID);
        sURLMatcher.addURI("sms", "outbox", SMS_OUTBOX);
        sURLMatcher.addURI("sms", "outbox/#", SMS_OUTBOX_ID);
        sURLMatcher.addURI("sms", "undelivered", SMS_UNDELIVERED);
        sURLMatcher.addURI("sms", "failed", SMS_FAILED);
        sURLMatcher.addURI("sms", "failed/#", SMS_FAILED_ID);
        sURLMatcher.addURI("sms", "queued", SMS_QUEUED);
        sURLMatcher.addURI("sms", "conversations", SMS_CONVERSATIONS);
        sURLMatcher.addURI("sms", "conversations/*", SMS_CONVERSATIONS_ID);
        sURLMatcher.addURI("sms", "raw", SMS_RAW_MESSAGE);
        sURLMatcher.addURI("sms", "attachments", SMS_ATTACHMENT);
        sURLMatcher.addURI("sms", "attachments/#", SMS_ATTACHMENT_ID);
        sURLMatcher.addURI("sms", "threadID", SMS_NEW_THREAD_ID);
        sURLMatcher.addURI("sms", "threadID/*", SMS_QUERY_THREAD_ID);
        sURLMatcher.addURI("sms", "status/#", SMS_STATUS_ID);
        sURLMatcher.addURI("sms", "sr_pending", SMS_STATUS_PENDING);
        sURLMatcher.addURI("sms", "icc", SMS_ALL_ICC);
        sURLMatcher.addURI("sms", "icc/#", SMS_ICC);
        sURLMatcher.addURI("sms", "sim", SMS_ALL_ICC);
        sURLMatcher.addURI("sms", "sim/#", SMS_ICC);
        sConversationProjectionMap.put(Sms.Conversations.SNIPPET,
            "sms.body AS snippet");
        sConversationProjectionMap.put(Sms.Conversations.THREAD_ID,
            "sms.thread_id AS thread_id");
        sConversationProjectionMap.put(Sms.Conversations.MESSAGE_COUNT,
            "groups.msg_count AS msg_count");
        sConversationProjectionMap.put("delta", null);
    }
}
