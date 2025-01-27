public class SynchronousContactsProvider2 extends ContactsProvider2 {
    public static final String READ_ONLY_ACCOUNT_TYPE = "ro";
    private static Boolean sDataWiped = false;
    private static ContactsDatabaseHelper mDbHelper;
    private boolean mDataWipeEnabled = true;
    private Account mAccount;
    private boolean mNetworkNotified;
    @Override
    protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
        if (mDbHelper == null) {
            mDbHelper = new ContactsDatabaseHelper(context);
        }
        return mDbHelper;
    }
    public static void resetOpenHelper() {
        mDbHelper = null;
    }
    public void setDataWipeEnabled(boolean flag) {
        mDataWipeEnabled = flag;
    }
    @Override
    protected void onBeginTransaction() {
        super.onBeginTransaction();
        mNetworkNotified = false;
    }
    @Override
    protected void notifyChange(boolean syncToNetwork) {
        mNetworkNotified |= syncToNetwork;
    }
    public boolean isNetworkNotified() {
        return mNetworkNotified;
    }
    @Override
    public boolean onCreate() {
        boolean created = super.onCreate();
        if (mDataWipeEnabled) {
            synchronized (sDataWiped) {
                if (!sDataWiped) {
                    sDataWiped = true;
                    wipeData();
                }
            }
        }
        return created;
    }
    @Override
    protected void verifyAccounts() {
    }
    @Override
    protected void verifyLocale() {
    }
    @Override
    protected Account getDefaultAccount() {
        if (mAccount == null) {
            mAccount = new Account("androidtest@gmail.com", "com.google");
        }
        return mAccount;
    }
    @Override
    PhotoPriorityResolver createPhotoPriorityResolver(Context context) {
        return new PhotoPriorityResolver(context) {
            @Override
            public synchronized int getPhotoPriority(String accountType) {
                if ("cupcake".equals(accountType)) {
                    return 3;
                }
                if ("donut".equals(accountType)) {
                    return 2;
                }
                if ("froyo".equals(accountType)) {
                    return 1;
                }
                return 0;
            }
        };
    }
    @Override
    protected Locale getLocale() {
        return Locale.US;
    }
    @Override
    protected boolean isWritableAccount(String accountType) {
        return !READ_ONLY_ACCOUNT_TYPE.equals(accountType);
    }
    public void prepareForFullAggregation(int maxContact) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        db.execSQL("UPDATE raw_contacts SET aggregation_mode=0,aggregation_needed=1;");
        long rowId =
            db.compileStatement("SELECT _id FROM raw_contacts LIMIT 1 OFFSET " + maxContact)
                .simpleQueryForLong();
        db.execSQL("DELETE FROM raw_contacts WHERE _id > " + rowId + ";");
    }
    public long getRawContactCount() {
        SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
        return db.compileStatement("SELECT COUNT(*) FROM raw_contacts").simpleQueryForLong();
    }
    public long getContactCount() {
        SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
        return db.compileStatement("SELECT COUNT(*) FROM contacts").simpleQueryForLong();
    }
    @Override
    public void wipeData() {
        super.wipeData();
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        db.execSQL("replace into SQLITE_SEQUENCE (name,seq) values('raw_contacts', 42)");
        db.execSQL("replace into SQLITE_SEQUENCE (name,seq) values('contacts', 2009)");
        db.execSQL("replace into SQLITE_SEQUENCE (name,seq) values('data', 777)");
    }
    @Override
    protected boolean isLegacyContactImportNeeded() {
        return false;
    }
}
