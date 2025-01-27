public class SyncStorageEngine extends Handler {
    private static final String TAG = "SyncManager";
    private static final boolean DEBUG_FILE = false;
    private static final long DEFAULT_POLL_FREQUENCY_SECONDS = 60 * 60 * 24; 
    static final long MILLIS_IN_4WEEKS = 1000L * 60 * 60 * 24 * 7 * 4;
    public static final int EVENT_START = 0;
    public static final int EVENT_STOP = 1;
    public static final String[] EVENTS = { "START", "STOP" };
    public static final int SOURCE_SERVER = 0;
    public static final int SOURCE_LOCAL = 1;
    public static final int SOURCE_POLL = 2;
    public static final int SOURCE_USER = 3;
    public static final int SOURCE_PERIODIC = 4;
    public static final long NOT_IN_BACKOFF_MODE = -1;
    private static final Intent SYNC_CONNECTION_SETTING_CHANGED_INTENT =
            new Intent("com.android.sync.SYNC_CONN_STATUS_CHANGED");
    public static final String[] SOURCES = { "SERVER",
                                             "LOCAL",
                                             "POLL",
                                             "USER",
                                             "PERIODIC" };
    public static final String MESG_SUCCESS = "success";
    public static final String MESG_CANCELED = "canceled";
    public static final int MAX_HISTORY = 100;
    private static final int MSG_WRITE_STATUS = 1;
    private static final long WRITE_STATUS_DELAY = 1000*60*10; 
    private static final int MSG_WRITE_STATISTICS = 2;
    private static final long WRITE_STATISTICS_DELAY = 1000*60*30; 
    private static final boolean SYNC_ENABLED_DEFAULT = false;
    private static final int ACCOUNTS_VERSION = 2;
    private static HashMap<String, String> sAuthorityRenames;
    static {
        sAuthorityRenames = new HashMap<String, String>();
        sAuthorityRenames.put("contacts", "com.android.contacts");
        sAuthorityRenames.put("calendar", "com.android.calendar");
    }
    public static class PendingOperation {
        final Account account;
        final int syncSource;
        final String authority;
        final Bundle extras;        
        final boolean expedited;
        int authorityId;
        byte[] flatExtras;
        PendingOperation(Account account, int source,
                String authority, Bundle extras, boolean expedited) {
            this.account = account;
            this.syncSource = source;
            this.authority = authority;
            this.extras = extras != null ? new Bundle(extras) : extras;
            this.expedited = expedited;
            this.authorityId = -1;
        }
        PendingOperation(PendingOperation other) {
            this.account = other.account;
            this.syncSource = other.syncSource;
            this.authority = other.authority;
            this.extras = other.extras;
            this.authorityId = other.authorityId;
            this.expedited = other.expedited;
        }
    }
    static class AccountInfo {
        final Account account;
        final HashMap<String, AuthorityInfo> authorities =
                new HashMap<String, AuthorityInfo>();
        AccountInfo(Account account) {
            this.account = account;
        }
    }
    public static class AuthorityInfo {
        final Account account;
        final String authority;
        final int ident;
        boolean enabled;
        int syncable;
        long backoffTime;
        long backoffDelay;
        long delayUntil;
        final ArrayList<Pair<Bundle, Long>> periodicSyncs;
        AuthorityInfo(Account account, String authority, int ident) {
            this.account = account;
            this.authority = authority;
            this.ident = ident;
            enabled = SYNC_ENABLED_DEFAULT;
            syncable = -1; 
            backoffTime = -1; 
            backoffDelay = -1; 
            periodicSyncs = new ArrayList<Pair<Bundle, Long>>();
            periodicSyncs.add(Pair.create(new Bundle(), DEFAULT_POLL_FREQUENCY_SECONDS));
        }
    }
    public static class SyncHistoryItem {
        int authorityId;
        int historyId;
        long eventTime;
        long elapsedTime;
        int source;
        int event;
        long upstreamActivity;
        long downstreamActivity;
        String mesg;
    }
    public static class DayStats {
        public final int day;
        public int successCount;
        public long successTime;
        public int failureCount;
        public long failureTime;
        public DayStats(int day) {
            this.day = day;
        }
    }
    private final SparseArray<AuthorityInfo> mAuthorities =
            new SparseArray<AuthorityInfo>();
    private final HashMap<Account, AccountInfo> mAccounts =
        new HashMap<Account, AccountInfo>();
    private final ArrayList<PendingOperation> mPendingOperations =
            new ArrayList<PendingOperation>();
    private SyncInfo mCurrentSync;
    private final SparseArray<SyncStatusInfo> mSyncStatus =
            new SparseArray<SyncStatusInfo>();
    private final ArrayList<SyncHistoryItem> mSyncHistory =
            new ArrayList<SyncHistoryItem>();
    private final RemoteCallbackList<ISyncStatusObserver> mChangeListeners
            = new RemoteCallbackList<ISyncStatusObserver>();
    private int mNextAuthorityId = 0;
    private final DayStats[] mDayStats = new DayStats[7*4];
    private final Calendar mCal;
    private int mYear;
    private int mYearInDays;
    private final Context mContext;
    private static volatile SyncStorageEngine sSyncStorageEngine = null;
    private final AtomicFile mAccountInfoFile;
    private final AtomicFile mStatusFile;
    private final AtomicFile mStatisticsFile;
    private final AtomicFile mPendingFile;
    private static final int PENDING_FINISH_TO_WRITE = 4;
    private int mNumPendingFinished = 0;
    private int mNextHistoryId = 0;
    private boolean mMasterSyncAutomatically = true;
    private SyncStorageEngine(Context context, File dataDir) {
        mContext = context;
        sSyncStorageEngine = this;
        mCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        File systemDir = new File(dataDir, "system");
        File syncDir = new File(systemDir, "sync");
        syncDir.mkdirs();
        mAccountInfoFile = new AtomicFile(new File(syncDir, "accounts.xml"));
        mStatusFile = new AtomicFile(new File(syncDir, "status.bin"));
        mPendingFile = new AtomicFile(new File(syncDir, "pending.bin"));
        mStatisticsFile = new AtomicFile(new File(syncDir, "stats.bin"));
        readAccountInfoLocked();
        readStatusLocked();
        readPendingOperationsLocked();
        readStatisticsLocked();
        readAndDeleteLegacyAccountInfoLocked();
        writeAccountInfoLocked();
        writeStatusLocked();
        writePendingOperationsLocked();
        writeStatisticsLocked();
    }
    public static SyncStorageEngine newTestInstance(Context context) {
        return new SyncStorageEngine(context, context.getFilesDir());
    }
    public static void init(Context context) {
        if (sSyncStorageEngine != null) {
            return;
        }
        File dataDir = Environment.getDataDirectory();
        sSyncStorageEngine = new SyncStorageEngine(context, dataDir);
    }
    public static SyncStorageEngine getSingleton() {
        if (sSyncStorageEngine == null) {
            throw new IllegalStateException("not initialized");
        }
        return sSyncStorageEngine;
    }
    @Override public void handleMessage(Message msg) {
        if (msg.what == MSG_WRITE_STATUS) {
            synchronized (mAuthorities) {
                writeStatusLocked();
            }
        } else if (msg.what == MSG_WRITE_STATISTICS) {
            synchronized (mAuthorities) {
                writeStatisticsLocked();
            }
        }
    }
    public void addStatusChangeListener(int mask, ISyncStatusObserver callback) {
        synchronized (mAuthorities) {
            mChangeListeners.register(callback, mask);
        }
    }
    public void removeStatusChangeListener(ISyncStatusObserver callback) {
        synchronized (mAuthorities) {
            mChangeListeners.unregister(callback);
        }
    }
    private void reportChange(int which) {
        ArrayList<ISyncStatusObserver> reports = null;
        synchronized (mAuthorities) {
            int i = mChangeListeners.beginBroadcast();
            while (i > 0) {
                i--;
                Integer mask = (Integer)mChangeListeners.getBroadcastCookie(i);
                if ((which & mask.intValue()) == 0) {
                    continue;
                }
                if (reports == null) {
                    reports = new ArrayList<ISyncStatusObserver>(i);
                }
                reports.add(mChangeListeners.getBroadcastItem(i));
            }
            mChangeListeners.finishBroadcast();
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "reportChange " + which + " to: " + reports);
        }
        if (reports != null) {
            int i = reports.size();
            while (i > 0) {
                i--;
                try {
                    reports.get(i).onStatusChanged(which);
                } catch (RemoteException e) {
                }
            }
        }
    }
    public boolean getSyncAutomatically(Account account, String providerName) {
        synchronized (mAuthorities) {
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(account, providerName,
                        "getSyncAutomatically");
                return authority != null && authority.enabled;
            }
            int i = mAuthorities.size();
            while (i > 0) {
                i--;
                AuthorityInfo authority = mAuthorities.valueAt(i);
                if (authority.authority.equals(providerName)
                        && authority.enabled) {
                    return true;
                }
            }
            return false;
        }
    }
    public void setSyncAutomatically(Account account, String providerName, boolean sync) {
        Log.d(TAG, "setSyncAutomatically: " + account + ", provider " + providerName
                + " -> " + sync);
        synchronized (mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(account, providerName, -1, false);
            if (authority.enabled == sync) {
                Log.d(TAG, "setSyncAutomatically: already set to " + sync + ", doing nothing");
                return;
            }
            authority.enabled = sync;
            writeAccountInfoLocked();
        }
        if (sync) {
            ContentResolver.requestSync(account, providerName, new Bundle());
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS);
    }
    public int getIsSyncable(Account account, String providerName) {
        synchronized (mAuthorities) {
            if (account != null) {
                AuthorityInfo authority = getAuthorityLocked(account, providerName,
                        "getIsSyncable");
                if (authority == null) {
                    return -1;
                }
                return authority.syncable;
            }
            int i = mAuthorities.size();
            while (i > 0) {
                i--;
                AuthorityInfo authority = mAuthorities.valueAt(i);
                if (authority.authority.equals(providerName)) {
                    return authority.syncable;
                }
            }
            return -1;
        }
    }
    public void setIsSyncable(Account account, String providerName, int syncable) {
        if (syncable > 1) {
            syncable = 1;
        } else if (syncable < -1) {
            syncable = -1;
        }
        Log.d(TAG, "setIsSyncable: " + account + ", provider " + providerName + " -> " + syncable);
        synchronized (mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(account, providerName, -1, false);
            if (authority.syncable == syncable) {
                Log.d(TAG, "setIsSyncable: already set to " + syncable + ", doing nothing");
                return;
            }
            authority.syncable = syncable;
            writeAccountInfoLocked();
        }
        if (syncable > 0) {
            ContentResolver.requestSync(account, providerName, new Bundle());
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS);
    }
    public Pair<Long, Long> getBackoff(Account account, String providerName) {
        synchronized (mAuthorities) {
            AuthorityInfo authority = getAuthorityLocked(account, providerName, "getBackoff");
            if (authority == null || authority.backoffTime < 0) {
                return null;
            }
            return Pair.create(authority.backoffTime, authority.backoffDelay);
        }
    }
    public void setBackoff(Account account, String providerName,
            long nextSyncTime, long nextDelay) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "setBackoff: " + account + ", provider " + providerName
                    + " -> nextSyncTime " + nextSyncTime + ", nextDelay " + nextDelay);
        }
        boolean changed = false;
        synchronized (mAuthorities) {
            if (account == null || providerName == null) {
                for (AccountInfo accountInfo : mAccounts.values()) {
                    if (account != null && !account.equals(accountInfo.account)) continue;
                    for (AuthorityInfo authorityInfo : accountInfo.authorities.values()) {
                        if (providerName != null && !providerName.equals(authorityInfo.authority)) {
                            continue;
                        }
                        if (authorityInfo.backoffTime != nextSyncTime
                                || authorityInfo.backoffDelay != nextDelay) {
                            authorityInfo.backoffTime = nextSyncTime;
                            authorityInfo.backoffDelay = nextDelay;
                            changed = true;
                        }
                    }
                }
            } else {
                AuthorityInfo authority =
                        getOrCreateAuthorityLocked(account, providerName, -1 , true);
                if (authority.backoffTime == nextSyncTime && authority.backoffDelay == nextDelay) {
                    return;
                }
                authority.backoffTime = nextSyncTime;
                authority.backoffDelay = nextDelay;
                changed = true;
            }
        }
        if (changed) {
            reportChange(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS);
        }
    }
    public void setDelayUntilTime(Account account, String providerName, long delayUntil) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "setDelayUntil: " + account + ", provider " + providerName
                    + " -> delayUntil " + delayUntil);
        }
        synchronized (mAuthorities) {
            AuthorityInfo authority = getOrCreateAuthorityLocked(
                    account, providerName, -1 , true);
            if (authority.delayUntil == delayUntil) {
                return;
            }
            authority.delayUntil = delayUntil;
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS);
    }
    public long getDelayUntilTime(Account account, String providerName) {
        synchronized (mAuthorities) {
            AuthorityInfo authority = getAuthorityLocked(account, providerName, "getDelayUntil");
            if (authority == null) {
                return 0;
            }
            return authority.delayUntil;
        }
    }
    private void updateOrRemovePeriodicSync(Account account, String providerName, Bundle extras,
            long period, boolean add) {
        if (period <= 0) {
            period = 0;
        }
        if (extras == null) {
            extras = new Bundle();
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "addOrRemovePeriodicSync: " + account + ", provider " + providerName
                    + " -> period " + period + ", extras " + extras);
        }
        synchronized (mAuthorities) {
            try {
                AuthorityInfo authority =
                        getOrCreateAuthorityLocked(account, providerName, -1, false);
                if (add) {
                    boolean alreadyPresent = false;
                    for (int i = 0, N = authority.periodicSyncs.size(); i < N; i++) {
                        Pair<Bundle, Long> syncInfo = authority.periodicSyncs.get(i);
                        final Bundle existingExtras = syncInfo.first;
                        if (equals(existingExtras, extras)) {
                            if (syncInfo.second == period) {
                                return;
                            }
                            authority.periodicSyncs.set(i, Pair.create(extras, period));
                            alreadyPresent = true;
                            break;
                        }
                    }
                    if (!alreadyPresent) {
                        authority.periodicSyncs.add(Pair.create(extras, period));
                        SyncStatusInfo status = getOrCreateSyncStatusLocked(authority.ident);
                        status.setPeriodicSyncTime(authority.periodicSyncs.size() - 1, 0);
                    }
                } else {
                    SyncStatusInfo status = mSyncStatus.get(authority.ident);
                    boolean changed = false;
                    Iterator<Pair<Bundle, Long>> iterator = authority.periodicSyncs.iterator();
                    int i = 0;
                    while (iterator.hasNext()) {
                        Pair<Bundle, Long> syncInfo = iterator.next();
                        if (equals(syncInfo.first, extras)) {
                            iterator.remove();
                            changed = true;
                            if (status != null) {
                                status.removePeriodicSyncTime(i);
                            }
                        } else {
                            i++;
                        }
                    }
                    if (!changed) {
                        return;
                    }
                }
            } finally {
                writeAccountInfoLocked();
                writeStatusLocked();
            }
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS);
    }
    public void addPeriodicSync(Account account, String providerName, Bundle extras,
            long pollFrequency) {
        updateOrRemovePeriodicSync(account, providerName, extras, pollFrequency, true );
    }
    public void removePeriodicSync(Account account, String providerName, Bundle extras) {
        updateOrRemovePeriodicSync(account, providerName, extras, 0 ,
                false );
    }
    public List<PeriodicSync> getPeriodicSyncs(Account account, String providerName) {
        ArrayList<PeriodicSync> syncs = new ArrayList<PeriodicSync>();
        synchronized (mAuthorities) {
            AuthorityInfo authority = getAuthorityLocked(account, providerName, "getPeriodicSyncs");
            if (authority != null) {
                for (Pair<Bundle, Long> item : authority.periodicSyncs) {
                    syncs.add(new PeriodicSync(account, providerName, item.first, item.second));
                }
            }
        }
        return syncs;
    }
    public void setMasterSyncAutomatically(boolean flag) {
        synchronized (mAuthorities) {
            if (mMasterSyncAutomatically == flag) {
                return;
            }
            mMasterSyncAutomatically = flag;
            writeAccountInfoLocked();
        }
        if (flag) {
            ContentResolver.requestSync(null, null, new Bundle());
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS);
        mContext.sendBroadcast(SYNC_CONNECTION_SETTING_CHANGED_INTENT);
    }
    public boolean getMasterSyncAutomatically() {
        synchronized (mAuthorities) {
            return mMasterSyncAutomatically;
        }
    }
    public AuthorityInfo getOrCreateAuthority(Account account, String authority) {
        synchronized (mAuthorities) {
            return getOrCreateAuthorityLocked(account, authority,
                    -1 ,
                    true );
        }
    }
    public void removeAuthority(Account account, String authority) {
        synchronized (mAuthorities) {
            removeAuthorityLocked(account, authority, true );
        }
    }
    public AuthorityInfo getAuthority(int authorityId) {
        synchronized (mAuthorities) {
            return mAuthorities.get(authorityId);
        }
    }
    public boolean isSyncActive(Account account, String authority) {
        synchronized (mAuthorities) {
            int i = mPendingOperations.size();
            while (i > 0) {
                i--;
                PendingOperation op = mPendingOperations.get(i);
                if (op.account.equals(account) && op.authority.equals(authority)) {
                    return true;
                }
            }
            if (mCurrentSync != null) {
                AuthorityInfo ainfo = getAuthority(mCurrentSync.authorityId);
                if (ainfo != null && ainfo.account.equals(account)
                        && ainfo.authority.equals(authority)) {
                    return true;
                }
            }
        }
        return false;
    }
    public PendingOperation insertIntoPending(PendingOperation op) {
        synchronized (mAuthorities) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "insertIntoPending: account=" + op.account
                    + " auth=" + op.authority
                    + " src=" + op.syncSource
                    + " extras=" + op.extras);
            }
            AuthorityInfo authority = getOrCreateAuthorityLocked(op.account,
                    op.authority,
                    -1 ,
                    true );
            if (authority == null) {
                return null;
            }
            op = new PendingOperation(op);
            op.authorityId = authority.ident;
            mPendingOperations.add(op);
            appendPendingOperationLocked(op);
            SyncStatusInfo status = getOrCreateSyncStatusLocked(authority.ident);
            status.pending = true;
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_PENDING);
        return op;
    }
    public boolean deleteFromPending(PendingOperation op) {
        boolean res = false;
        synchronized (mAuthorities) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "deleteFromPending: account=" + op.account
                    + " auth=" + op.authority
                    + " src=" + op.syncSource
                    + " extras=" + op.extras);
            }
            if (mPendingOperations.remove(op)) {
                if (mPendingOperations.size() == 0
                        || mNumPendingFinished >= PENDING_FINISH_TO_WRITE) {
                    writePendingOperationsLocked();
                    mNumPendingFinished = 0;
                } else {
                    mNumPendingFinished++;
                }
                AuthorityInfo authority = getAuthorityLocked(op.account, op.authority,
                        "deleteFromPending");
                if (authority != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "removing - " + authority);
                    final int N = mPendingOperations.size();
                    boolean morePending = false;
                    for (int i=0; i<N; i++) {
                        PendingOperation cur = mPendingOperations.get(i);
                        if (cur.account.equals(op.account)
                                && cur.authority.equals(op.authority)) {
                            morePending = true;
                            break;
                        }
                    }
                    if (!morePending) {
                        if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "no more pending!");
                        SyncStatusInfo status = getOrCreateSyncStatusLocked(authority.ident);
                        status.pending = false;
                    }
                }
                res = true;
            }
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_PENDING);
        return res;
    }
    public int clearPending() {
        int num;
        synchronized (mAuthorities) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "clearPending");
            }
            num = mPendingOperations.size();
            mPendingOperations.clear();
            final int N = mSyncStatus.size();
            for (int i=0; i<N; i++) {
                mSyncStatus.valueAt(i).pending = false;
            }
            writePendingOperationsLocked();
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_PENDING);
        return num;
    }
    public ArrayList<PendingOperation> getPendingOperations() {
        synchronized (mAuthorities) {
            return new ArrayList<PendingOperation>(mPendingOperations);
        }
    }
    public int getPendingOperationCount() {
        synchronized (mAuthorities) {
            return mPendingOperations.size();
        }
    }
    public void doDatabaseCleanup(Account[] accounts) {
        synchronized (mAuthorities) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) Log.w(TAG, "Updating for new accounts...");
            SparseArray<AuthorityInfo> removing = new SparseArray<AuthorityInfo>();
            Iterator<AccountInfo> accIt = mAccounts.values().iterator();
            while (accIt.hasNext()) {
                AccountInfo acc = accIt.next();
                if (!ArrayUtils.contains(accounts, acc.account)) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.w(TAG, "Account removed: " + acc.account);
                    }
                    for (AuthorityInfo auth : acc.authorities.values()) {
                        removing.put(auth.ident, auth);
                    }
                    accIt.remove();
                }
            }
            int i = removing.size();
            if (i > 0) {
                while (i > 0) {
                    i--;
                    int ident = removing.keyAt(i);
                    mAuthorities.remove(ident);
                    int j = mSyncStatus.size();
                    while (j > 0) {
                        j--;
                        if (mSyncStatus.keyAt(j) == ident) {
                            mSyncStatus.remove(mSyncStatus.keyAt(j));
                        }
                    }
                    j = mSyncHistory.size();
                    while (j > 0) {
                        j--;
                        if (mSyncHistory.get(j).authorityId == ident) {
                            mSyncHistory.remove(j);
                        }
                    }
                }
                writeAccountInfoLocked();
                writeStatusLocked();
                writePendingOperationsLocked();
                writeStatisticsLocked();
            }
        }
    }
    public void setActiveSync(SyncManager.ActiveSyncContext activeSyncContext) {
        synchronized (mAuthorities) {
            if (activeSyncContext != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "setActiveSync: account="
                        + activeSyncContext.mSyncOperation.account
                        + " auth=" + activeSyncContext.mSyncOperation.authority
                        + " src=" + activeSyncContext.mSyncOperation.syncSource
                        + " extras=" + activeSyncContext.mSyncOperation.extras);
                }
                if (mCurrentSync != null) {
                    Log.w(TAG, "setActiveSync called with existing active sync!");
                }
                AuthorityInfo authority = getAuthorityLocked(
                        activeSyncContext.mSyncOperation.account,
                        activeSyncContext.mSyncOperation.authority,
                        "setActiveSync");
                if (authority == null) {
                    return;
                }
                mCurrentSync = new SyncInfo(authority.ident,
                        authority.account, authority.authority,
                        activeSyncContext.mStartTime);
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "setActiveSync: null");
                mCurrentSync = null;
            }
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE);
    }
    public void reportActiveChange() {
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE);
    }
    public long insertStartSyncEvent(Account accountName, String authorityName,
            long now, int source) {
        long id;
        synchronized (mAuthorities) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "insertStartSyncEvent: account=" + accountName
                    + " auth=" + authorityName + " source=" + source);
            }
            AuthorityInfo authority = getAuthorityLocked(accountName, authorityName,
                    "insertStartSyncEvent");
            if (authority == null) {
                return -1;
            }
            SyncHistoryItem item = new SyncHistoryItem();
            item.authorityId = authority.ident;
            item.historyId = mNextHistoryId++;
            if (mNextHistoryId < 0) mNextHistoryId = 0;
            item.eventTime = now;
            item.source = source;
            item.event = EVENT_START;
            mSyncHistory.add(0, item);
            while (mSyncHistory.size() > MAX_HISTORY) {
                mSyncHistory.remove(mSyncHistory.size()-1);
            }
            id = item.historyId;
            if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "returning historyId " + id);
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_STATUS);
        return id;
    }
    public static boolean equals(Bundle b1, Bundle b2) {
        if (b1.size() != b2.size()) {
            return false;
        }
        if (b1.isEmpty()) {
            return true;
        }
        for (String key : b1.keySet()) {
            if (!b2.containsKey(key)) {
                return false;
            }
            if (!b1.get(key).equals(b2.get(key))) {
                return false;
            }
        }
        return true;
    }
    public void stopSyncEvent(long historyId, long elapsedTime, String resultMessage,
            long downstreamActivity, long upstreamActivity) {
        synchronized (mAuthorities) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "stopSyncEvent: historyId=" + historyId);
            }
            SyncHistoryItem item = null;
            int i = mSyncHistory.size();
            while (i > 0) {
                i--;
                item = mSyncHistory.get(i);
                if (item.historyId == historyId) {
                    break;
                }
                item = null;
            }
            if (item == null) {
                Log.w(TAG, "stopSyncEvent: no history for id " + historyId);
                return;
            }
            item.elapsedTime = elapsedTime;
            item.event = EVENT_STOP;
            item.mesg = resultMessage;
            item.downstreamActivity = downstreamActivity;
            item.upstreamActivity = upstreamActivity;
            SyncStatusInfo status = getOrCreateSyncStatusLocked(item.authorityId);
            status.numSyncs++;
            status.totalElapsedTime += elapsedTime;
            switch (item.source) {
                case SOURCE_LOCAL:
                    status.numSourceLocal++;
                    break;
                case SOURCE_POLL:
                    status.numSourcePoll++;
                    break;
                case SOURCE_USER:
                    status.numSourceUser++;
                    break;
                case SOURCE_SERVER:
                    status.numSourceServer++;
                    break;
                case SOURCE_PERIODIC:
                    status.numSourcePeriodic++;
                    break;
            }
            boolean writeStatisticsNow = false;
            int day = getCurrentDayLocked();
            if (mDayStats[0] == null) {
                mDayStats[0] = new DayStats(day);
            } else if (day != mDayStats[0].day) {
                System.arraycopy(mDayStats, 0, mDayStats, 1, mDayStats.length-1);
                mDayStats[0] = new DayStats(day);
                writeStatisticsNow = true;
            } else if (mDayStats[0] == null) {
            }
            final DayStats ds = mDayStats[0];
            final long lastSyncTime = (item.eventTime + elapsedTime);
            boolean writeStatusNow = false;
            if (MESG_SUCCESS.equals(resultMessage)) {
                if (status.lastSuccessTime == 0 || status.lastFailureTime != 0) {
                    writeStatusNow = true;
                }
                status.lastSuccessTime = lastSyncTime;
                status.lastSuccessSource = item.source;
                status.lastFailureTime = 0;
                status.lastFailureSource = -1;
                status.lastFailureMesg = null;
                status.initialFailureTime = 0;
                ds.successCount++;
                ds.successTime += elapsedTime;
            } else if (!MESG_CANCELED.equals(resultMessage)) {
                if (status.lastFailureTime == 0) {
                    writeStatusNow = true;
                }
                status.lastFailureTime = lastSyncTime;
                status.lastFailureSource = item.source;
                status.lastFailureMesg = resultMessage;
                if (status.initialFailureTime == 0) {
                    status.initialFailureTime = lastSyncTime;
                }
                ds.failureCount++;
                ds.failureTime += elapsedTime;
            }
            if (writeStatusNow) {
                writeStatusLocked();
            } else if (!hasMessages(MSG_WRITE_STATUS)) {
                sendMessageDelayed(obtainMessage(MSG_WRITE_STATUS),
                        WRITE_STATUS_DELAY);
            }
            if (writeStatisticsNow) {
                writeStatisticsLocked();
            } else if (!hasMessages(MSG_WRITE_STATISTICS)) {
                sendMessageDelayed(obtainMessage(MSG_WRITE_STATISTICS),
                        WRITE_STATISTICS_DELAY);
            }
        }
        reportChange(ContentResolver.SYNC_OBSERVER_TYPE_STATUS);
    }
    public SyncInfo getCurrentSync() {
        synchronized (mAuthorities) {
            return mCurrentSync;
        }
    }
    public ArrayList<SyncStatusInfo> getSyncStatus() {
        synchronized (mAuthorities) {
            final int N = mSyncStatus.size();
            ArrayList<SyncStatusInfo> ops = new ArrayList<SyncStatusInfo>(N);
            for (int i=0; i<N; i++) {
                ops.add(mSyncStatus.valueAt(i));
            }
            return ops;
        }
    }
    public ArrayList<AuthorityInfo> getAuthorities() {
        synchronized (mAuthorities) {
            final int N = mAuthorities.size();
            ArrayList<AuthorityInfo> infos = new ArrayList<AuthorityInfo>(N);
            for (int i=0; i<N; i++) {
                infos.add(mAuthorities.valueAt(i));
            }
            return infos;
        }
    }
    public SyncStatusInfo getStatusByAccountAndAuthority(Account account, String authority) {
        if (account == null || authority == null) {
          throw new IllegalArgumentException();
        }
        synchronized (mAuthorities) {
            final int N = mSyncStatus.size();
            for (int i=0; i<N; i++) {
                SyncStatusInfo cur = mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = mAuthorities.get(cur.authorityId);
                if (ainfo != null && ainfo.authority.equals(authority) &&
                    account.equals(ainfo.account)) {
                  return cur;
                }
            }
            return null;
        }
    }
    public boolean isSyncPending(Account account, String authority) {
        synchronized (mAuthorities) {
            final int N = mSyncStatus.size();
            for (int i=0; i<N; i++) {
                SyncStatusInfo cur = mSyncStatus.valueAt(i);
                AuthorityInfo ainfo = mAuthorities.get(cur.authorityId);
                if (ainfo == null) {
                    continue;
                }
                if (account != null && !ainfo.account.equals(account)) {
                    continue;
                }
                if (ainfo.authority.equals(authority) && cur.pending) {
                    return true;
                }
            }
            return false;
        }
    }
    public ArrayList<SyncHistoryItem> getSyncHistory() {
        synchronized (mAuthorities) {
            final int N = mSyncHistory.size();
            ArrayList<SyncHistoryItem> items = new ArrayList<SyncHistoryItem>(N);
            for (int i=0; i<N; i++) {
                items.add(mSyncHistory.get(i));
            }
            return items;
        }
    }
    public DayStats[] getDayStatistics() {
        synchronized (mAuthorities) {
            DayStats[] ds = new DayStats[mDayStats.length];
            System.arraycopy(mDayStats, 0, ds, 0, ds.length);
            return ds;
        }
    }
    public long getInitialSyncFailureTime() {
        synchronized (mAuthorities) {
            if (!mMasterSyncAutomatically) {
                return 0;
            }
            long oldest = 0;
            int i = mSyncStatus.size();
            while (i > 0) {
                i--;
                SyncStatusInfo stats = mSyncStatus.valueAt(i);
                AuthorityInfo authority = mAuthorities.get(stats.authorityId);
                if (authority != null && authority.enabled) {
                    if (oldest == 0 || stats.initialFailureTime < oldest) {
                        oldest = stats.initialFailureTime;
                    }
                }
            }
            return oldest;
        }
    }
    private int getCurrentDayLocked() {
        mCal.setTimeInMillis(System.currentTimeMillis());
        final int dayOfYear = mCal.get(Calendar.DAY_OF_YEAR);
        if (mYear != mCal.get(Calendar.YEAR)) {
            mYear = mCal.get(Calendar.YEAR);
            mCal.clear();
            mCal.set(Calendar.YEAR, mYear);
            mYearInDays = (int)(mCal.getTimeInMillis()/86400000);
        }
        return dayOfYear + mYearInDays;
    }
    private AuthorityInfo getAuthorityLocked(Account accountName, String authorityName,
            String tag) {
        AccountInfo account = mAccounts.get(accountName);
        if (account == null) {
            if (tag != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, tag + ": unknown account " + accountName);
                }
            }
            return null;
        }
        AuthorityInfo authority = account.authorities.get(authorityName);
        if (authority == null) {
            if (tag != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, tag + ": unknown authority " + authorityName);
                }
            }
            return null;
        }
        return authority;
    }
    private AuthorityInfo getOrCreateAuthorityLocked(Account accountName,
            String authorityName, int ident, boolean doWrite) {
        AccountInfo account = mAccounts.get(accountName);
        if (account == null) {
            account = new AccountInfo(accountName);
            mAccounts.put(accountName, account);
        }
        AuthorityInfo authority = account.authorities.get(authorityName);
        if (authority == null) {
            if (ident < 0) {
                ident = mNextAuthorityId;
                mNextAuthorityId++;
                doWrite = true;
            }
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "created a new AuthorityInfo for " + accountName
                    + ", provider " + authorityName);
            }
            authority = new AuthorityInfo(accountName, authorityName, ident);
            account.authorities.put(authorityName, authority);
            mAuthorities.put(ident, authority);
            if (doWrite) {
                writeAccountInfoLocked();
            }
        }
        return authority;
    }
    private void removeAuthorityLocked(Account account, String authorityName, boolean doWrite) {
        AccountInfo accountInfo = mAccounts.get(account);
        if (accountInfo != null) {
            final AuthorityInfo authorityInfo = accountInfo.authorities.remove(authorityName);
            if (authorityInfo != null) {
                mAuthorities.remove(authorityInfo.ident);
                if (doWrite) {
                    writeAccountInfoLocked();
                }
            }
        }
    }
    public SyncStatusInfo getOrCreateSyncStatus(AuthorityInfo authority) {
        synchronized (mAuthorities) {
            return getOrCreateSyncStatusLocked(authority.ident);
        }
    }
    private SyncStatusInfo getOrCreateSyncStatusLocked(int authorityId) {
        SyncStatusInfo status = mSyncStatus.get(authorityId);
        if (status == null) {
            status = new SyncStatusInfo(authorityId);
            mSyncStatus.put(authorityId, status);
        }
        return status;
    }
    public void writeAllState() {
        synchronized (mAuthorities) {
            if (mNumPendingFinished > 0) {
                writePendingOperationsLocked();
            }
            writeStatusLocked();
            writeStatisticsLocked();
        }
    }
    public void clearAndReadState() {
        synchronized (mAuthorities) {
            mAuthorities.clear();
            mAccounts.clear();
            mPendingOperations.clear();
            mSyncStatus.clear();
            mSyncHistory.clear();
            readAccountInfoLocked();
            readStatusLocked();
            readPendingOperationsLocked();
            readStatisticsLocked();
            readAndDeleteLegacyAccountInfoLocked();
            writeAccountInfoLocked();
            writeStatusLocked();
            writePendingOperationsLocked();
            writeStatisticsLocked();
        }
    }
    private void readAccountInfoLocked() {
        int highestAuthorityId = -1;
        FileInputStream fis = null;
        try {
            fis = mAccountInfoFile.openRead();
            if (DEBUG_FILE) Log.v(TAG, "Reading " + mAccountInfoFile.getBaseFile());
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.START_TAG) {
                eventType = parser.next();
            }
            String tagName = parser.getName();
            if ("accounts".equals(tagName)) {
                String listen = parser.getAttributeValue(
                        null, "listen-for-tickles");
                String versionString = parser.getAttributeValue(null, "version");
                int version;
                try {
                    version = (versionString == null) ? 0 : Integer.parseInt(versionString);
                } catch (NumberFormatException e) {
                    version = 0;
                }
                String nextIdString = parser.getAttributeValue(null, "nextAuthorityId");
                try {
                    int id = (nextIdString == null) ? 0 : Integer.parseInt(nextIdString);
                    mNextAuthorityId = Math.max(mNextAuthorityId, id);
                } catch (NumberFormatException e) {
                }
                mMasterSyncAutomatically = listen == null || Boolean.parseBoolean(listen);
                eventType = parser.next();
                AuthorityInfo authority = null;
                Pair<Bundle, Long> periodicSync = null;
                do {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.getName();
                        if (parser.getDepth() == 2) {
                            if ("authority".equals(tagName)) {
                                authority = parseAuthority(parser, version);
                                periodicSync = null;
                                if (authority.ident > highestAuthorityId) {
                                    highestAuthorityId = authority.ident;
                                }
                            }
                        } else if (parser.getDepth() == 3) {
                            if ("periodicSync".equals(tagName) && authority != null) {
                                periodicSync = parsePeriodicSync(parser, authority);
                            }
                        } else if (parser.getDepth() == 4 && periodicSync != null) {
                            if ("extra".equals(tagName)) {
                                parseExtra(parser, periodicSync);
                            }
                        }
                    }
                    eventType = parser.next();
                } while (eventType != XmlPullParser.END_DOCUMENT);
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Error reading accounts", e);
            return;
        } catch (java.io.IOException e) {
            if (fis == null) Log.i(TAG, "No initial accounts");
            else Log.w(TAG, "Error reading accounts", e);
            return;
        } finally {
            mNextAuthorityId = Math.max(highestAuthorityId + 1, mNextAuthorityId);
            if (fis != null) {
                try {
                    fis.close();
                } catch (java.io.IOException e1) {
                }
            }
        }
        maybeMigrateSettingsForRenamedAuthorities();
    }
    private boolean maybeMigrateSettingsForRenamedAuthorities() {
        boolean writeNeeded = false;
        ArrayList<AuthorityInfo> authoritiesToRemove = new ArrayList<AuthorityInfo>();
        final int N = mAuthorities.size();
        for (int i=0; i<N; i++) {
            AuthorityInfo authority = mAuthorities.valueAt(i);
            final String newAuthorityName = sAuthorityRenames.get(authority.authority);
            if (newAuthorityName == null) {
                continue;
            }
            authoritiesToRemove.add(authority);
            if (!authority.enabled) {
                continue;
            }
            if (getAuthorityLocked(authority.account, newAuthorityName, "cleanup") != null) {
                continue;
            }
            AuthorityInfo newAuthority = getOrCreateAuthorityLocked(authority.account,
                    newAuthorityName, -1 , false );
            newAuthority.enabled = true;
            writeNeeded = true;
        }
        for (AuthorityInfo authorityInfo : authoritiesToRemove) {
            removeAuthorityLocked(authorityInfo.account, authorityInfo.authority,
                    false );
            writeNeeded = true;
        }
        return writeNeeded;
    }
    private AuthorityInfo parseAuthority(XmlPullParser parser, int version) {
        AuthorityInfo authority = null;
        int id = -1;
        try {
            id = Integer.parseInt(parser.getAttributeValue(
                    null, "id"));
        } catch (NumberFormatException e) {
            Log.e(TAG, "error parsing the id of the authority", e);
        } catch (NullPointerException e) {
            Log.e(TAG, "the id of the authority is null", e);
        }
        if (id >= 0) {
            String authorityName = parser.getAttributeValue(null, "authority");
            String enabled = parser.getAttributeValue(null, "enabled");
            String syncable = parser.getAttributeValue(null, "syncable");
            String accountName = parser.getAttributeValue(null, "account");
            String accountType = parser.getAttributeValue(null, "type");
            if (accountType == null) {
                accountType = "com.google";
                syncable = "unknown";
            }
            authority = mAuthorities.get(id);
            if (DEBUG_FILE) Log.v(TAG, "Adding authority: account="
                    + accountName + " auth=" + authorityName
                    + " enabled=" + enabled
                    + " syncable=" + syncable);
            if (authority == null) {
                if (DEBUG_FILE) Log.v(TAG, "Creating entry");
                authority = getOrCreateAuthorityLocked(
                        new Account(accountName, accountType), authorityName, id, false);
                if (version > 0) {
                    authority.periodicSyncs.clear();
                }
            }
            if (authority != null) {
                authority.enabled = enabled == null || Boolean.parseBoolean(enabled);
                if ("unknown".equals(syncable)) {
                    authority.syncable = -1;
                } else {
                    authority.syncable =
                            (syncable == null || Boolean.parseBoolean(syncable)) ? 1 : 0;
                }
            } else {
                Log.w(TAG, "Failure adding authority: account="
                        + accountName + " auth=" + authorityName
                        + " enabled=" + enabled
                        + " syncable=" + syncable);
            }
        }
        return authority;
    }
    private Pair<Bundle, Long> parsePeriodicSync(XmlPullParser parser, AuthorityInfo authority) {
        Bundle extras = new Bundle();
        String periodValue = parser.getAttributeValue(null, "period");
        final long period;
        try {
            period = Long.parseLong(periodValue);
        } catch (NumberFormatException e) {
            Log.e(TAG, "error parsing the period of a periodic sync", e);
            return null;
        } catch (NullPointerException e) {
            Log.e(TAG, "the period of a periodic sync is null", e);
            return null;
        }
        final Pair<Bundle, Long> periodicSync = Pair.create(extras, period);
        authority.periodicSyncs.add(periodicSync);
        return periodicSync;
    }
    private void parseExtra(XmlPullParser parser, Pair<Bundle, Long> periodicSync) {
        final Bundle extras = periodicSync.first;
        String name = parser.getAttributeValue(null, "name");
        String type = parser.getAttributeValue(null, "type");
        String value1 = parser.getAttributeValue(null, "value1");
        String value2 = parser.getAttributeValue(null, "value2");
        try {
            if ("long".equals(type)) {
                extras.putLong(name, Long.parseLong(value1));
            } else if ("integer".equals(type)) {
                extras.putInt(name, Integer.parseInt(value1));
            } else if ("double".equals(type)) {
                extras.putDouble(name, Double.parseDouble(value1));
            } else if ("float".equals(type)) {
                extras.putFloat(name, Float.parseFloat(value1));
            } else if ("boolean".equals(type)) {
                extras.putBoolean(name, Boolean.parseBoolean(value1));
            } else if ("string".equals(type)) {
                extras.putString(name, value1);
            } else if ("account".equals(type)) {
                extras.putParcelable(name, new Account(value1, value2));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "error parsing bundle value", e);
        } catch (NullPointerException e) {
            Log.e(TAG, "error parsing bundle value", e);
        }
    }
    private void writeAccountInfoLocked() {
        if (DEBUG_FILE) Log.v(TAG, "Writing new " + mAccountInfoFile.getBaseFile());
        FileOutputStream fos = null;
        try {
            fos = mAccountInfoFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, "utf-8");
            out.startDocument(null, true);
            out.setFeature("http:
            out.startTag(null, "accounts");
            out.attribute(null, "version", Integer.toString(ACCOUNTS_VERSION));
            out.attribute(null, "nextAuthorityId", Integer.toString(mNextAuthorityId));
            if (!mMasterSyncAutomatically) {
                out.attribute(null, "listen-for-tickles", "false");
            }
            final int N = mAuthorities.size();
            for (int i=0; i<N; i++) {
                AuthorityInfo authority = mAuthorities.valueAt(i);
                out.startTag(null, "authority");
                out.attribute(null, "id", Integer.toString(authority.ident));
                out.attribute(null, "account", authority.account.name);
                out.attribute(null, "type", authority.account.type);
                out.attribute(null, "authority", authority.authority);
                out.attribute(null, "enabled", Boolean.toString(authority.enabled));
                if (authority.syncable < 0) {
                    out.attribute(null, "syncable", "unknown");
                } else {
                    out.attribute(null, "syncable", Boolean.toString(authority.syncable != 0));
                }
                for (Pair<Bundle, Long> periodicSync : authority.periodicSyncs) {
                    out.startTag(null, "periodicSync");
                    out.attribute(null, "period", Long.toString(periodicSync.second));
                    final Bundle extras = periodicSync.first;
                    for (String key : extras.keySet()) {
                        out.startTag(null, "extra");
                        out.attribute(null, "name", key);
                        final Object value = extras.get(key);
                        if (value instanceof Long) {
                            out.attribute(null, "type", "long");
                            out.attribute(null, "value1", value.toString());
                        } else if (value instanceof Integer) {
                            out.attribute(null, "type", "integer");
                            out.attribute(null, "value1", value.toString());
                        } else if (value instanceof Boolean) {
                            out.attribute(null, "type", "boolean");
                            out.attribute(null, "value1", value.toString());
                        } else if (value instanceof Float) {
                            out.attribute(null, "type", "float");
                            out.attribute(null, "value1", value.toString());
                        } else if (value instanceof Double) {
                            out.attribute(null, "type", "double");
                            out.attribute(null, "value1", value.toString());
                        } else if (value instanceof String) {
                            out.attribute(null, "type", "string");
                            out.attribute(null, "value1", value.toString());
                        } else if (value instanceof Account) {
                            out.attribute(null, "type", "account");
                            out.attribute(null, "value1", ((Account)value).name);
                            out.attribute(null, "value2", ((Account)value).type);
                        }
                        out.endTag(null, "extra");
                    }
                    out.endTag(null, "periodicSync");
                }
                out.endTag(null, "authority");
            }
            out.endTag(null, "accounts");
            out.endDocument();
            mAccountInfoFile.finishWrite(fos);
        } catch (java.io.IOException e1) {
            Log.w(TAG, "Error writing accounts", e1);
            if (fos != null) {
                mAccountInfoFile.failWrite(fos);
            }
        }
    }
    static int getIntColumn(Cursor c, String name) {
        return c.getInt(c.getColumnIndex(name));
    }
    static long getLongColumn(Cursor c, String name) {
        return c.getLong(c.getColumnIndex(name));
    }
    private void readAndDeleteLegacyAccountInfoLocked() {
        File file = mContext.getDatabasePath("syncmanager.db");
        if (!file.exists()) {
            return;
        }
        String path = file.getPath();
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
        }
        if (db != null) {
            final boolean hasType = db.getVersion() >= 11;
            if (DEBUG_FILE) Log.v(TAG, "Reading legacy sync accounts db");
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables("stats, status");
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("_id", "status._id as _id");
            map.put("account", "stats.account as account");
            if (hasType) {
                map.put("account_type", "stats.account_type as account_type");
            }
            map.put("authority", "stats.authority as authority");
            map.put("totalElapsedTime", "totalElapsedTime");
            map.put("numSyncs", "numSyncs");
            map.put("numSourceLocal", "numSourceLocal");
            map.put("numSourcePoll", "numSourcePoll");
            map.put("numSourceServer", "numSourceServer");
            map.put("numSourceUser", "numSourceUser");
            map.put("lastSuccessSource", "lastSuccessSource");
            map.put("lastSuccessTime", "lastSuccessTime");
            map.put("lastFailureSource", "lastFailureSource");
            map.put("lastFailureTime", "lastFailureTime");
            map.put("lastFailureMesg", "lastFailureMesg");
            map.put("pending", "pending");
            qb.setProjectionMap(map);
            qb.appendWhere("stats._id = status.stats_id");
            Cursor c = qb.query(db, null, null, null, null, null, null);
            while (c.moveToNext()) {
                String accountName = c.getString(c.getColumnIndex("account"));
                String accountType = hasType
                        ? c.getString(c.getColumnIndex("account_type")) : null;
                if (accountType == null) {
                    accountType = "com.google";
                }
                String authorityName = c.getString(c.getColumnIndex("authority"));
                AuthorityInfo authority = this.getOrCreateAuthorityLocked(
                        new Account(accountName, accountType),
                        authorityName, -1, false);
                if (authority != null) {
                    int i = mSyncStatus.size();
                    boolean found = false;
                    SyncStatusInfo st = null;
                    while (i > 0) {
                        i--;
                        st = mSyncStatus.valueAt(i);
                        if (st.authorityId == authority.ident) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        st = new SyncStatusInfo(authority.ident);
                        mSyncStatus.put(authority.ident, st);
                    }
                    st.totalElapsedTime = getLongColumn(c, "totalElapsedTime");
                    st.numSyncs = getIntColumn(c, "numSyncs");
                    st.numSourceLocal = getIntColumn(c, "numSourceLocal");
                    st.numSourcePoll = getIntColumn(c, "numSourcePoll");
                    st.numSourceServer = getIntColumn(c, "numSourceServer");
                    st.numSourceUser = getIntColumn(c, "numSourceUser");
                    st.numSourcePeriodic = 0;
                    st.lastSuccessSource = getIntColumn(c, "lastSuccessSource");
                    st.lastSuccessTime = getLongColumn(c, "lastSuccessTime");
                    st.lastFailureSource = getIntColumn(c, "lastFailureSource");
                    st.lastFailureTime = getLongColumn(c, "lastFailureTime");
                    st.lastFailureMesg = c.getString(c.getColumnIndex("lastFailureMesg"));
                    st.pending = getIntColumn(c, "pending") != 0;
                }
            }
            c.close();
            qb = new SQLiteQueryBuilder();
            qb.setTables("settings");
            c = qb.query(db, null, null, null, null, null, null);
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex("name"));
                String value = c.getString(c.getColumnIndex("value"));
                if (name == null) continue;
                if (name.equals("listen_for_tickles")) {
                    setMasterSyncAutomatically(value == null || Boolean.parseBoolean(value));
                } else if (name.startsWith("sync_provider_")) {
                    String provider = name.substring("sync_provider_".length(),
                            name.length());
                    int i = mAuthorities.size();
                    while (i > 0) {
                        i--;
                        AuthorityInfo authority = mAuthorities.valueAt(i);
                        if (authority.authority.equals(provider)) {
                            authority.enabled = value == null || Boolean.parseBoolean(value);
                            authority.syncable = 1;
                        }
                    }
                }
            }
            c.close();
            db.close();
            (new File(path)).delete();
        }
    }
    public static final int STATUS_FILE_END = 0;
    public static final int STATUS_FILE_ITEM = 100;
    private void readStatusLocked() {
        if (DEBUG_FILE) Log.v(TAG, "Reading " + mStatusFile.getBaseFile());
        try {
            byte[] data = mStatusFile.readFully();
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            int token;
            while ((token=in.readInt()) != STATUS_FILE_END) {
                if (token == STATUS_FILE_ITEM) {
                    SyncStatusInfo status = new SyncStatusInfo(in);
                    if (mAuthorities.indexOfKey(status.authorityId) >= 0) {
                        status.pending = false;
                        if (DEBUG_FILE) Log.v(TAG, "Adding status for id "
                                + status.authorityId);
                        mSyncStatus.put(status.authorityId, status);
                    }
                } else {
                    Log.w(TAG, "Unknown status token: " + token);
                    break;
                }
            }
        } catch (java.io.IOException e) {
            Log.i(TAG, "No initial status");
        }
    }
    private void writeStatusLocked() {
        if (DEBUG_FILE) Log.v(TAG, "Writing new " + mStatusFile.getBaseFile());
        removeMessages(MSG_WRITE_STATUS);
        FileOutputStream fos = null;
        try {
            fos = mStatusFile.startWrite();
            Parcel out = Parcel.obtain();
            final int N = mSyncStatus.size();
            for (int i=0; i<N; i++) {
                SyncStatusInfo status = mSyncStatus.valueAt(i);
                out.writeInt(STATUS_FILE_ITEM);
                status.writeToParcel(out, 0);
            }
            out.writeInt(STATUS_FILE_END);
            fos.write(out.marshall());
            out.recycle();
            mStatusFile.finishWrite(fos);
        } catch (java.io.IOException e1) {
            Log.w(TAG, "Error writing status", e1);
            if (fos != null) {
                mStatusFile.failWrite(fos);
            }
        }
    }
    public static final int PENDING_OPERATION_VERSION = 2;
    private void readPendingOperationsLocked() {
        if (DEBUG_FILE) Log.v(TAG, "Reading " + mPendingFile.getBaseFile());
        try {
            byte[] data = mPendingFile.readFully();
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            final int SIZE = in.dataSize();
            while (in.dataPosition() < SIZE) {
                int version = in.readInt();
                if (version != PENDING_OPERATION_VERSION && version != 1) {
                    Log.w(TAG, "Unknown pending operation version "
                            + version + "; dropping all ops");
                    break;
                }
                int authorityId = in.readInt();
                int syncSource = in.readInt();
                byte[] flatExtras = in.createByteArray();
                boolean expedited;
                if (version == PENDING_OPERATION_VERSION) {
                    expedited = in.readInt() != 0;
                } else {
                    expedited = false;
                }
                AuthorityInfo authority = mAuthorities.get(authorityId);
                if (authority != null) {
                    Bundle extras = null;
                    if (flatExtras != null) {
                        extras = unflattenBundle(flatExtras);
                    }
                    PendingOperation op = new PendingOperation(
                            authority.account, syncSource,
                            authority.authority, extras, expedited);
                    op.authorityId = authorityId;
                    op.flatExtras = flatExtras;
                    if (DEBUG_FILE) Log.v(TAG, "Adding pending op: account=" + op.account
                            + " auth=" + op.authority
                            + " src=" + op.syncSource
                            + " expedited=" + op.expedited
                            + " extras=" + op.extras);
                    mPendingOperations.add(op);
                }
            }
        } catch (java.io.IOException e) {
            Log.i(TAG, "No initial pending operations");
        }
    }
    private void writePendingOperationLocked(PendingOperation op, Parcel out) {
        out.writeInt(PENDING_OPERATION_VERSION);
        out.writeInt(op.authorityId);
        out.writeInt(op.syncSource);
        if (op.flatExtras == null && op.extras != null) {
            op.flatExtras = flattenBundle(op.extras);
        }
        out.writeByteArray(op.flatExtras);
        out.writeInt(op.expedited ? 1 : 0);
    }
    private void writePendingOperationsLocked() {
        final int N = mPendingOperations.size();
        FileOutputStream fos = null;
        try {
            if (N == 0) {
                if (DEBUG_FILE) Log.v(TAG, "Truncating " + mPendingFile.getBaseFile());
                mPendingFile.truncate();
                return;
            }
            if (DEBUG_FILE) Log.v(TAG, "Writing new " + mPendingFile.getBaseFile());
            fos = mPendingFile.startWrite();
            Parcel out = Parcel.obtain();
            for (int i=0; i<N; i++) {
                PendingOperation op = mPendingOperations.get(i);
                writePendingOperationLocked(op, out);
            }
            fos.write(out.marshall());
            out.recycle();
            mPendingFile.finishWrite(fos);
        } catch (java.io.IOException e1) {
            Log.w(TAG, "Error writing pending operations", e1);
            if (fos != null) {
                mPendingFile.failWrite(fos);
            }
        }
    }
    private void appendPendingOperationLocked(PendingOperation op) {
        if (DEBUG_FILE) Log.v(TAG, "Appending to " + mPendingFile.getBaseFile());
        FileOutputStream fos = null;
        try {
            fos = mPendingFile.openAppend();
        } catch (java.io.IOException e) {
            if (DEBUG_FILE) Log.v(TAG, "Failed append; writing full file");
            writePendingOperationsLocked();
            return;
        }
        try {
            Parcel out = Parcel.obtain();
            writePendingOperationLocked(op, out);
            fos.write(out.marshall());
            out.recycle();
        } catch (java.io.IOException e1) {
            Log.w(TAG, "Error writing pending operations", e1);
        } finally {
            try {
                fos.close();
            } catch (java.io.IOException e2) {
            }
        }
    }
    static private byte[] flattenBundle(Bundle bundle) {
        byte[] flatData = null;
        Parcel parcel = Parcel.obtain();
        try {
            bundle.writeToParcel(parcel, 0);
            flatData = parcel.marshall();
        } finally {
            parcel.recycle();
        }
        return flatData;
    }
    static private Bundle unflattenBundle(byte[] flatData) {
        Bundle bundle;
        Parcel parcel = Parcel.obtain();
        try {
            parcel.unmarshall(flatData, 0, flatData.length);
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (RuntimeException e) {
            bundle = new Bundle();
        } finally {
            parcel.recycle();
        }
        return bundle;
    }
    public static final int STATISTICS_FILE_END = 0;
    public static final int STATISTICS_FILE_ITEM_OLD = 100;
    public static final int STATISTICS_FILE_ITEM = 101;
    private void readStatisticsLocked() {
        try {
            byte[] data = mStatisticsFile.readFully();
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            int token;
            int index = 0;
            while ((token=in.readInt()) != STATISTICS_FILE_END) {
                if (token == STATISTICS_FILE_ITEM
                        || token == STATISTICS_FILE_ITEM_OLD) {
                    int day = in.readInt();
                    if (token == STATISTICS_FILE_ITEM_OLD) {
                        day = day - 2009 + 14245;  
                    }
                    DayStats ds = new DayStats(day);
                    ds.successCount = in.readInt();
                    ds.successTime = in.readLong();
                    ds.failureCount = in.readInt();
                    ds.failureTime = in.readLong();
                    if (index < mDayStats.length) {
                        mDayStats[index] = ds;
                        index++;
                    }
                } else {
                    Log.w(TAG, "Unknown stats token: " + token);
                    break;
                }
            }
        } catch (java.io.IOException e) {
            Log.i(TAG, "No initial statistics");
        }
    }
    private void writeStatisticsLocked() {
        if (DEBUG_FILE) Log.v(TAG, "Writing new " + mStatisticsFile.getBaseFile());
        removeMessages(MSG_WRITE_STATISTICS);
        FileOutputStream fos = null;
        try {
            fos = mStatisticsFile.startWrite();
            Parcel out = Parcel.obtain();
            final int N = mDayStats.length;
            for (int i=0; i<N; i++) {
                DayStats ds = mDayStats[i];
                if (ds == null) {
                    break;
                }
                out.writeInt(STATISTICS_FILE_ITEM);
                out.writeInt(ds.day);
                out.writeInt(ds.successCount);
                out.writeLong(ds.successTime);
                out.writeInt(ds.failureCount);
                out.writeLong(ds.failureTime);
            }
            out.writeInt(STATISTICS_FILE_END);
            fos.write(out.marshall());
            out.recycle();
            mStatisticsFile.finishWrite(fos);
        } catch (java.io.IOException e1) {
            Log.w(TAG, "Error writing stats", e1);
            if (fos != null) {
                mStatisticsFile.failWrite(fos);
            }
        }
    }
}
