public class FolderSyncParser extends AbstractSyncParser {
    public static final String TAG = "FolderSyncParser";
    public static final int USER_FOLDER_TYPE = 1;
    public static final int INBOX_TYPE = 2;
    public static final int DRAFTS_TYPE = 3;
    public static final int DELETED_TYPE = 4;
    public static final int SENT_TYPE = 5;
    public static final int OUTBOX_TYPE = 6;
    public static final int TASKS_TYPE = 7;
    public static final int CALENDAR_TYPE = 8;
    public static final int CONTACTS_TYPE = 9;
    public static final int NOTES_TYPE = 10;
    public static final int JOURNAL_TYPE = 11;
    public static final int USER_MAILBOX_TYPE = 12;
    public static final List<Integer> mValidFolderTypes = Arrays.asList(INBOX_TYPE, DRAFTS_TYPE,
            DELETED_TYPE, SENT_TYPE, OUTBOX_TYPE, USER_MAILBOX_TYPE, CALENDAR_TYPE, CONTACTS_TYPE);
    public static final String ALL_BUT_ACCOUNT_MAILBOX = MailboxColumns.ACCOUNT_KEY + "=? and " +
        MailboxColumns.TYPE + "!=" + Mailbox.TYPE_EAS_ACCOUNT_MAILBOX;
   private static final String WHERE_SERVER_ID_AND_ACCOUNT = MailboxColumns.SERVER_ID + "=? and " +
        MailboxColumns.ACCOUNT_KEY + "=?";
    private static final String WHERE_DISPLAY_NAME_AND_ACCOUNT = MailboxColumns.DISPLAY_NAME +
        "=? and " + MailboxColumns.ACCOUNT_KEY + "=?";
    private static final String WHERE_PARENT_SERVER_ID_AND_ACCOUNT =
        MailboxColumns.PARENT_SERVER_ID +"=? and " + MailboxColumns.ACCOUNT_KEY + "=?";
    private static final String[] MAILBOX_ID_COLUMNS_PROJECTION =
        new String[] {MailboxColumns.ID, MailboxColumns.SERVER_ID};
    private long mAccountId;
    private String mAccountIdAsString;
    private MockParserStream mMock = null;
    private String[] mBindArguments = new String[2];
    public FolderSyncParser(InputStream in, AbstractSyncAdapter adapter) throws IOException {
        super(in, adapter);
        mAccountId = mAccount.mId;
        mAccountIdAsString = Long.toString(mAccountId);
        if (in instanceof MockParserStream) {
            mMock = (MockParserStream)in;
        }
    }
    @Override
    public boolean parse() throws IOException {
        int status;
        boolean res = false;
        boolean resetFolders = false;
        if (nextTag(START_DOCUMENT) != Tags.FOLDER_FOLDER_SYNC)
            throw new EasParserException();
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == Tags.FOLDER_STATUS) {
                status = getValueInt();
                if (status != Eas.FOLDER_STATUS_OK) {
                    mService.errorLog("FolderSync failed: " + status);
                    if (status == Eas.FOLDER_STATUS_INVALID_KEY) {
                        mAccount.mSyncKey = "0";
                        mService.errorLog("Bad sync key; RESET and delete all folders");
                        mContentResolver.delete(Mailbox.CONTENT_URI, ALL_BUT_ACCOUNT_MAILBOX,
                                new String[] {Long.toString(mAccountId)});
                        SyncManager.stopNonAccountMailboxSyncsForAccount(mAccountId);
                        res = true;
                        resetFolders = true;
                    } else {
                        mService.errorLog("Throwing IOException; will retry later");
                        throw new EasParserException("Folder status error");
                    }
                }
            } else if (tag == Tags.FOLDER_SYNC_KEY) {
                mAccount.mSyncKey = getValue();
                userLog("New Account SyncKey: ", mAccount.mSyncKey);
            } else if (tag == Tags.FOLDER_CHANGES) {
                changesParser();
            } else
                skipTag();
        }
        synchronized (mService.getSynchronizer()) {
            if (!mService.isStopped() || resetFolders) {
                ContentValues cv = new ContentValues();
                cv.put(AccountColumns.SYNC_KEY, mAccount.mSyncKey);
                mAccount.update(mContext, cv);
                userLog("Leaving FolderSyncParser with Account syncKey=", mAccount.mSyncKey);
            }
        }
        return res;
    }
    private Cursor getServerIdCursor(String serverId) {
        mBindArguments[0] = serverId;
        mBindArguments[1] = mAccountIdAsString;
        return mContentResolver.query(Mailbox.CONTENT_URI, EmailContent.ID_PROJECTION,
                WHERE_SERVER_ID_AND_ACCOUNT, mBindArguments, null);
    }
    public void deleteParser(ArrayList<ContentProviderOperation> ops) throws IOException {
        while (nextTag(Tags.FOLDER_DELETE) != END) {
            switch (tag) {
                case Tags.FOLDER_SERVER_ID:
                    String serverId = getValue();
                    Cursor c = getServerIdCursor(serverId);
                    try {
                        if (c.moveToFirst()) {
                            userLog("Deleting ", serverId);
                            ops.add(ContentProviderOperation.newDelete(
                                    ContentUris.withAppendedId(Mailbox.CONTENT_URI,
                                            c.getLong(0))).build());
                            AttachmentProvider.deleteAllMailboxAttachmentFiles(mContext,
                                    mAccountId, mMailbox.mId);
                        }
                    } finally {
                        c.close();
                    }
                    break;
                default:
                    skipTag();
            }
        }
    }
    public void addParser(ArrayList<ContentProviderOperation> ops) throws IOException {
        String name = null;
        String serverId = null;
        String parentId = null;
        int type = 0;
        while (nextTag(Tags.FOLDER_ADD) != END) {
            switch (tag) {
                case Tags.FOLDER_DISPLAY_NAME: {
                    name = getValue();
                    break;
                }
                case Tags.FOLDER_TYPE: {
                    type = getValueInt();
                    break;
                }
                case Tags.FOLDER_PARENT_ID: {
                    parentId = getValue();
                    break;
                }
                case Tags.FOLDER_SERVER_ID: {
                    serverId = getValue();
                    break;
                }
                default:
                    skipTag();
            }
        }
        if (mValidFolderTypes.contains(type)) {
            Mailbox m = new Mailbox();
            m.mDisplayName = name;
            m.mServerId = serverId;
            m.mAccountKey = mAccountId;
            m.mType = Mailbox.TYPE_MAIL;
            m.mSyncInterval = Mailbox.CHECK_INTERVAL_NEVER;
            switch (type) {
                case INBOX_TYPE:
                    m.mType = Mailbox.TYPE_INBOX;
                    m.mSyncInterval = mAccount.mSyncInterval;
                    break;
                case CONTACTS_TYPE:
                    m.mType = Mailbox.TYPE_CONTACTS;
                    m.mSyncInterval = mAccount.mSyncInterval;
                    break;
                case OUTBOX_TYPE:
                    m.mType = Mailbox.TYPE_OUTBOX;
                    break;
                case SENT_TYPE:
                    m.mType = Mailbox.TYPE_SENT;
                    break;
                case DRAFTS_TYPE:
                    m.mType = Mailbox.TYPE_DRAFTS;
                    break;
                case DELETED_TYPE:
                    m.mType = Mailbox.TYPE_TRASH;
                    break;
                case CALENDAR_TYPE:
                    m.mType = Mailbox.TYPE_CALENDAR;
                    m.mSyncInterval = mAccount.mSyncInterval;
                    break;
            }
            m.mFlagVisible = (m.mType < Mailbox.TYPE_NOT_EMAIL);
            if (!parentId.equals("0")) {
                m.mParentServerId = parentId;
            }
            userLog("Adding mailbox: ", m.mDisplayName);
            ops.add(ContentProviderOperation
                    .newInsert(Mailbox.CONTENT_URI).withValues(m.toContentValues()).build());
        }
        return;
    }
    public void updateParser(ArrayList<ContentProviderOperation> ops) throws IOException {
        String serverId = null;
        String displayName = null;
        String parentId = null;
        while (nextTag(Tags.FOLDER_UPDATE) != END) {
            switch (tag) {
                case Tags.FOLDER_SERVER_ID:
                    serverId = getValue();
                    break;
                case Tags.FOLDER_DISPLAY_NAME:
                    displayName = getValue();
                    break;
                case Tags.FOLDER_PARENT_ID:
                    parentId = getValue();
                    break;
                default:
                    skipTag();
                    break;
            }
        }
        if (serverId != null && (displayName != null || parentId != null)) {
            Cursor c = getServerIdCursor(serverId);
            try {
                if (c.moveToFirst()) {
                    userLog("Updating ", serverId);
                    ContentValues cv = new ContentValues();
                    if (displayName != null) {
                        cv.put(Mailbox.DISPLAY_NAME, displayName);
                    }
                    if (parentId != null) {
                        cv.put(Mailbox.PARENT_SERVER_ID, parentId);
                    }
                    ops.add(ContentProviderOperation.newUpdate(
                            ContentUris.withAppendedId(Mailbox.CONTENT_URI,
                                    c.getLong(0))).withValues(cv).build());
                }
            } finally {
                c.close();
            }
        }
    }
    public void changesParser() throws IOException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        while (nextTag(Tags.FOLDER_CHANGES) != END) {
            if (tag == Tags.FOLDER_ADD) {
                addParser(ops);
            } else if (tag == Tags.FOLDER_DELETE) {
                deleteParser(ops);
            } else if (tag == Tags.FOLDER_UPDATE) {
                updateParser(ops);
            } else if (tag == Tags.FOLDER_COUNT) {
                getValueInt();
            } else
                skipTag();
        }
        if (mMock != null) {
            mMock.setResult(null);
            return;
        }
        synchronized (mService.getSynchronizer()) {
            if (!ops.isEmpty() && !mService.isStopped()) {
                userLog("Applying ", ops.size(), " mailbox operations.");
                try {
                    mContentResolver.applyBatch(EmailProvider.EMAIL_AUTHORITY, ops);
                    userLog("New Account SyncKey: ", mAccount.mSyncKey);
                } catch (RemoteException e) {
                } catch (OperationApplicationException e) {
                }
                mBindArguments[0] = "Sync Issues";
                mBindArguments[1] = mAccountIdAsString;
                Cursor c = mContentResolver.query(Mailbox.CONTENT_URI,
                        MAILBOX_ID_COLUMNS_PROJECTION, WHERE_DISPLAY_NAME_AND_ACCOUNT,
                        mBindArguments, null);
                String parentServerId = null;
                long id = 0;
                try {
                    if (c.moveToFirst()) {
                        id = c.getLong(0);
                        parentServerId = c.getString(1);
                    }
                } finally {
                    c.close();
                }
                if (parentServerId != null) {
                    mContentResolver.delete(ContentUris.withAppendedId(Mailbox.CONTENT_URI, id),
                            null, null);
                    mBindArguments[0] = parentServerId;
                    mContentResolver.delete(Mailbox.CONTENT_URI, WHERE_PARENT_SERVER_ID_AND_ACCOUNT,
                            mBindArguments);
                }
            }
        }
    }
    @Override
    public void commandsParser() throws IOException {
    }
    @Override
    public void commit() throws IOException {
    }
    @Override
    public void wipe() {
    }
    @Override
    public void responsesParser() throws IOException {
    }
}
