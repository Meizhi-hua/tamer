public final class PicasaService extends Service {
    public static final String ACTION_SYNC = "com.cooliris.picasa.action.SYNC";
    public static final String ACTION_PERIODIC_SYNC = "com.cooliris.picasa.action.PERIODIC_SYNC";
    public static final String ACCOUNT_TYPE = "com.google";
    public static final String SERVICE_NAME = "lh2";
    public static final String FEATURE_SERVICE_NAME = "service_" + SERVICE_NAME;
    public static final String KEY_TYPE = "com.cooliris.SYNC_TYPE";
    public static final String KEY_ID = "com.cooliris.SYNC_ID";
    public static final int TYPE_USERS = 0;
    public static final int TYPE_USERS_ALBUMS = 1;
    public static final int TYPE_ALBUM_PHOTOS = 2;
    private final HandlerThread mSyncThread = new HandlerThread("PicasaSyncThread");
    private final Handler mSyncHandler;
    private static final AtomicBoolean sSyncPending = new AtomicBoolean(false);
    public static void requestSync(Context context, int type, long id) {
        Bundle extras = new Bundle();
        extras.putInt(KEY_TYPE, type);
        extras.putLong(KEY_ID, id);
        Account[] accounts = PicasaApi.getAccounts(context);
        for (Account account : accounts) {
            ContentResolver.requestSync(account, PicasaContentProvider.AUTHORITY, extras);
        }
    }
    public PicasaService() {
        super();
        mSyncThread.start();
        mSyncHandler = new Handler(mSyncThread.getLooper());
        mSyncHandler.post(new Runnable() {
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            }
        });
    }
    private static PicasaContentProvider getContentProvider(Context context) {
        ContentResolver cr = context.getContentResolver();
        ContentProviderClient client = cr.acquireContentProviderClient(PicasaContentProvider.AUTHORITY);
        return (PicasaContentProvider) client.getLocalContentProvider();
    }
    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        mSyncHandler.post(new Runnable() {
            public void run() {
                performSync(PicasaService.this, null, intent.getExtras(), new SyncResult());
                stopSelf(startId);
            }
        });
        return START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new PicasaSyncAdapter(getApplicationContext()).getSyncAdapterBinder();
    }
    @Override
    public void onDestroy() {
        mSyncThread.quit();
    }
    public static boolean performSync(Context context, Account account, Bundle extras, SyncResult syncResult) {
        if (!sSyncPending.compareAndSet(false, true)) {
            return false;
        }
        performSyncImpl(context, account, extras, syncResult);
        sSyncPending.set(false);
        synchronized (sSyncPending) {
            sSyncPending.notifyAll();
        }
        return true;
    }
    public static void waitForPerformSync() {
        synchronized (sSyncPending) {
            while (sSyncPending.get()) {
                try {
                    sSyncPending.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    private static void performSyncImpl(Context context, Account account, Bundle extras, SyncResult syncResult) {
        String authority = PicasaContentProvider.AUTHORITY;
        if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false)) {
            if (account != null && ContentResolver.getIsSyncable(account, authority) < 0) {
                try {
                    ContentResolver.setIsSyncable(account, authority, getIsSyncable(context, account) ? 1 : 0);
                } catch (OperationCanceledException e) {
                } catch (IOException e) {
                }
            }
            return;
        }
        if (account != null && ContentResolver.getIsSyncable(account, authority) < 0) {
            ++syncResult.stats.numSkippedEntries;
            return;
        }
        int type = extras.getInt(PicasaService.KEY_TYPE, PicasaService.TYPE_USERS_ALBUMS);
        long id = extras.getLong(PicasaService.KEY_ID, -1);
        PicasaContentProvider provider = getContentProvider(context);
        provider.reloadAccounts();
        provider.setActiveSyncAccount(account);
        switch (type) {
        case PicasaService.TYPE_USERS:
            provider.syncUsers(syncResult);
            break;
        case PicasaService.TYPE_USERS_ALBUMS:
            provider.syncUsersAndAlbums(true, syncResult);
            break;
        case PicasaService.TYPE_ALBUM_PHOTOS:
            provider.syncAlbumPhotos(id, true, syncResult);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    private static boolean getIsSyncable(Context context, Account account) throws IOException, OperationCanceledException {
        try {
            Account[] picasaAccounts = AccountManager.get(context).getAccountsByTypeAndFeatures(ACCOUNT_TYPE,
                    new String[] { FEATURE_SERVICE_NAME }, null , null ).getResult();
            for (Account picasaAccount : picasaAccounts) {
                if (account.equals(picasaAccount)) {
                    return true;
                }
            }
            return false;
        } catch (AuthenticatorException e) {
            throw new IOException(e.getMessage());
        }
    }
}
