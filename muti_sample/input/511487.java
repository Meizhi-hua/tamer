public final class ActivityManagerService extends ActivityManagerNative implements Watchdog.Monitor {
    static final String TAG = "ActivityManager";
    static final boolean DEBUG = false;
    static final boolean localLOGV = DEBUG ? Config.LOGD : Config.LOGV;
    static final boolean DEBUG_SWITCH = localLOGV || false;
    static final boolean DEBUG_TASKS = localLOGV || false;
    static final boolean DEBUG_PAUSE = localLOGV || false;
    static final boolean DEBUG_OOM_ADJ = localLOGV || false;
    static final boolean DEBUG_TRANSITION = localLOGV || false;
    static final boolean DEBUG_BROADCAST = localLOGV || false;
    static final boolean DEBUG_BROADCAST_LIGHT = DEBUG_BROADCAST || false;
    static final boolean DEBUG_SERVICE = localLOGV || false;
    static final boolean DEBUG_VISBILITY = localLOGV || false;
    static final boolean DEBUG_PROCESSES = localLOGV || false;
    static final boolean DEBUG_PROVIDER = localLOGV || false;
    static final boolean DEBUG_URI_PERMISSION = localLOGV || false;
    static final boolean DEBUG_USER_LEAVING = localLOGV || false;
    static final boolean DEBUG_RESULTS = localLOGV || false;
    static final boolean DEBUG_BACKUP = localLOGV || false;
    static final boolean DEBUG_CONFIGURATION = localLOGV || false;
    static final boolean VALIDATE_TOKENS = false;
    static final boolean SHOW_ACTIVITY_START_TIME = true;
    static final long BATTERY_STATS_TIME = 30*60*1000;      
    static final boolean MONITOR_CPU_USAGE = true;
    static final long MONITOR_CPU_MIN_TIME = 5*1000;        
    static final long MONITOR_CPU_MAX_TIME = 0x0fffffff;    
    static final boolean MONITOR_THREAD_CPU_USAGE = false;
    static final int STOCK_PM_FLAGS = PackageManager.GET_SHARED_LIBRARY_FILES;
    private static final String SYSTEM_SECURE = "ro.secure";
    static final int MAX_PROCESSES = 2;
    static final boolean ENFORCE_PROCESS_LIMIT = false;
    static final int MAX_ACTIVITIES = 20;
    static final int MAX_RECENT_TASKS = 20;
    static final long APP_SWITCH_DELAY_TIME = 5*1000;
    static final long ACTIVITY_INACTIVE_RESET_TIME = 1000*60*30;
    static final boolean SHOW_APP_STARTING_ICON = true;
    static final int PAUSE_TIMEOUT = 500;
    static final int LAUNCH_TIMEOUT = 10*1000;
    static final int PROC_START_TIMEOUT = 10*1000;
    static final int IDLE_TIMEOUT = 10*1000;
    static final int GC_TIMEOUT = 5*1000;
    static final int GC_MIN_INTERVAL = 60*1000;
    static final int DESTROY_TIMEOUT = 10*1000;
    static final int BROADCAST_TIMEOUT = 10*1000;
    static final int SERVICE_TIMEOUT = 20*1000;
    static final int SERVICE_RESTART_DURATION = 5*1000;
    static final int SERVICE_RESET_RUN_DURATION = 60*1000;
    static final int SERVICE_RESTART_DURATION_FACTOR = 4;
    static final int SERVICE_MIN_RESTART_TIME_BETWEEN = 10*1000;
    static final int MAX_SERVICE_INACTIVITY = 30*60*1000;
    static final int KEY_DISPATCHING_TIMEOUT = 5*1000;
    static final int MIN_CRASH_INTERVAL = 60*1000;
    static final int INSTRUMENTATION_KEY_DISPATCHING_TIMEOUT = 60*1000;
    static final int EMPTY_APP_ADJ;
    static final int HIDDEN_APP_MAX_ADJ;
    static int HIDDEN_APP_MIN_ADJ;
    static final int HOME_APP_ADJ;
    static final int BACKUP_APP_ADJ;
    static final int SECONDARY_SERVER_ADJ;
    static final int VISIBLE_APP_ADJ;
    static final int FOREGROUND_APP_ADJ;
    static final int CORE_SERVER_ADJ = -12;
    static final int SYSTEM_ADJ = -16;
    static final int PAGE_SIZE = 4*1024;
    static final int EMPTY_APP_MEM;
    static final int HIDDEN_APP_MEM;
    static final int HOME_APP_MEM;
    static final int BACKUP_APP_MEM;
    static final int SECONDARY_SERVER_MEM;
    static final int VISIBLE_APP_MEM;
    static final int FOREGROUND_APP_MEM;
    static final int MIN_HIDDEN_APPS = 2;
    static final int MAX_HIDDEN_APPS = 15;
    static final long CONTENT_APP_IDLE_OFFSET = 15*1000;
    static final long EMPTY_APP_IDLE_OFFSET = 120*1000;
    static {
        FOREGROUND_APP_ADJ =
            Integer.valueOf(SystemProperties.get("ro.FOREGROUND_APP_ADJ"));
        VISIBLE_APP_ADJ =
            Integer.valueOf(SystemProperties.get("ro.VISIBLE_APP_ADJ"));
        SECONDARY_SERVER_ADJ =
            Integer.valueOf(SystemProperties.get("ro.SECONDARY_SERVER_ADJ"));
        BACKUP_APP_ADJ =
            Integer.valueOf(SystemProperties.get("ro.BACKUP_APP_ADJ"));
        HOME_APP_ADJ =
            Integer.valueOf(SystemProperties.get("ro.HOME_APP_ADJ"));
        HIDDEN_APP_MIN_ADJ =
            Integer.valueOf(SystemProperties.get("ro.HIDDEN_APP_MIN_ADJ"));
        EMPTY_APP_ADJ =
            Integer.valueOf(SystemProperties.get("ro.EMPTY_APP_ADJ"));
        HIDDEN_APP_MAX_ADJ = EMPTY_APP_ADJ-1;
        FOREGROUND_APP_MEM =
            Integer.valueOf(SystemProperties.get("ro.FOREGROUND_APP_MEM"))*PAGE_SIZE;
        VISIBLE_APP_MEM =
            Integer.valueOf(SystemProperties.get("ro.VISIBLE_APP_MEM"))*PAGE_SIZE;
        SECONDARY_SERVER_MEM =
            Integer.valueOf(SystemProperties.get("ro.SECONDARY_SERVER_MEM"))*PAGE_SIZE;
        BACKUP_APP_MEM =
            Integer.valueOf(SystemProperties.get("ro.BACKUP_APP_MEM"))*PAGE_SIZE;
        HOME_APP_MEM =
            Integer.valueOf(SystemProperties.get("ro.HOME_APP_MEM"))*PAGE_SIZE;
        HIDDEN_APP_MEM =
            Integer.valueOf(SystemProperties.get("ro.HIDDEN_APP_MEM"))*PAGE_SIZE;
        EMPTY_APP_MEM =
            Integer.valueOf(SystemProperties.get("ro.EMPTY_APP_MEM"))*PAGE_SIZE;
    }
    static final int MY_PID = Process.myPid();
    static final String[] EMPTY_STRING_ARRAY = new String[0];
    enum ActivityState {
        INITIALIZING,
        RESUMED,
        PAUSING,
        PAUSED,
        STOPPING,
        STOPPED,
        FINISHING,
        DESTROYING,
        DESTROYED
    }
    final ArrayList mHistory = new ArrayList();
    class PendingActivityLaunch {
        HistoryRecord r;
        HistoryRecord sourceRecord;
        Uri[] grantedUriPermissions;
        int grantedMode;
        boolean onlyIfNeeded;
    }
    final ArrayList<PendingActivityLaunch> mPendingActivityLaunches
            = new ArrayList<PendingActivityLaunch>();
    final ArrayList<IActivityManager.WaitResult> mWaitingActivityLaunched
            = new ArrayList<IActivityManager.WaitResult>();
    final ArrayList<IActivityManager.WaitResult> mWaitingActivityVisible
            = new ArrayList<IActivityManager.WaitResult>();
    final ArrayList<BroadcastRecord> mParallelBroadcasts
            = new ArrayList<BroadcastRecord>();
    final ArrayList<BroadcastRecord> mOrderedBroadcasts
            = new ArrayList<BroadcastRecord>();
    static final int MAX_BROADCAST_HISTORY = 100;
    final BroadcastRecord[] mBroadcastHistory
            = new BroadcastRecord[MAX_BROADCAST_HISTORY];
    boolean mBroadcastsScheduled = false;
    boolean mUserLeaving = false;
    HistoryRecord mPausingActivity = null;
    HistoryRecord mResumedActivity = null;
    HistoryRecord mFocusedActivity = null;
    HistoryRecord mLastPausedActivity = null;
    final ArrayList mWaitingVisibleActivities = new ArrayList();
    final ArrayList<HistoryRecord> mStoppingActivities
            = new ArrayList<HistoryRecord>();
    final ArrayList<HistoryRecord> mNoAnimActivities
            = new ArrayList<HistoryRecord>();
    final ArrayList<TaskRecord> mRecentTasks
            = new ArrayList<TaskRecord>();
    final ArrayList mFinishingActivities = new ArrayList();
    final ProcessMap<ProcessRecord> mProcessNames
            = new ProcessMap<ProcessRecord>();
    final ProcessMap<Long> mProcessCrashTimes = new ProcessMap<Long>();
    final ProcessMap<Long> mBadProcesses = new ProcessMap<Long>();
    final SparseArray<ProcessRecord> mPidsSelfLocked
            = new SparseArray<ProcessRecord>();
    abstract class ForegroundToken implements IBinder.DeathRecipient {
        int pid;
        IBinder token;
    }
    final SparseArray<ForegroundToken> mForegroundProcesses
            = new SparseArray<ForegroundToken>();
    final ArrayList<ProcessRecord> mProcessesOnHold
            = new ArrayList<ProcessRecord>();
    final ArrayList<ProcessRecord> mStartingProcesses
            = new ArrayList<ProcessRecord>();
    final ArrayList<ProcessRecord> mPersistentStartingProcesses
            = new ArrayList<ProcessRecord>();
    final ArrayList<ProcessRecord> mRemovedProcesses
            = new ArrayList<ProcessRecord>();
    final ArrayList<ProcessRecord> mLruProcesses
            = new ArrayList<ProcessRecord>();
    final ArrayList<ProcessRecord> mProcessesToGc
            = new ArrayList<ProcessRecord>();
    private ProcessRecord mHomeProcess;
    private final ArrayList mLRUActivities = new ArrayList();
    final HashSet mPendingResultRecords = new HashSet();
    final HashMap<PendingIntentRecord.Key, WeakReference<PendingIntentRecord>> mIntentSenderRecords
            = new HashMap<PendingIntentRecord.Key, WeakReference<PendingIntentRecord>>();
    BroadcastRecord mPendingBroadcast = null;
    final HashMap mRegisteredReceivers = new HashMap();
    final IntentResolver<BroadcastFilter, BroadcastFilter> mReceiverResolver
            = new IntentResolver<BroadcastFilter, BroadcastFilter>() {
        @Override
        protected boolean allowFilterResult(
                BroadcastFilter filter, List<BroadcastFilter> dest) {
            IBinder target = filter.receiverList.receiver.asBinder();
            for (int i=dest.size()-1; i>=0; i--) {
                if (dest.get(i).receiverList.receiver.asBinder() == target) {
                    return false;
                }
            }
            return true;
        }
    };
    final HashMap<String, ArrayList<Intent>> mStickyBroadcasts =
            new HashMap<String, ArrayList<Intent>>();
    final HashMap<ComponentName, ServiceRecord> mServices =
        new HashMap<ComponentName, ServiceRecord>();
    final HashMap<Intent.FilterComparison, ServiceRecord> mServicesByIntent =
        new HashMap<Intent.FilterComparison, ServiceRecord>();
    final HashMap<IBinder, ConnectionRecord> mServiceConnections
            = new HashMap<IBinder, ConnectionRecord>();
    final ArrayList<ServiceRecord> mPendingServices
            = new ArrayList<ServiceRecord>();
    final ArrayList<ServiceRecord> mRestartingServices
            = new ArrayList<ServiceRecord>();
    final ArrayList<ServiceRecord> mStoppingServices
            = new ArrayList<ServiceRecord>();
    String mBackupAppName = null;
    BackupRecord mBackupTarget = null;
    final ArrayList mPendingThumbnails = new ArrayList();
    final ArrayList mCancelledThumbnails = new ArrayList();
    final HashMap mProvidersByName = new HashMap();
    final HashMap mProvidersByClass = new HashMap();
    final ArrayList mLaunchingProviders = new ArrayList();
    final private SparseArray<HashMap<Uri, UriPermission>> mGrantedUriPermissions
            = new SparseArray<HashMap<Uri, UriPermission>>();
    private class Identity {
        public int pid;
        public int uid;
        Identity(int _pid, int _uid) {
            pid = _pid;
            uid = _uid;
        }
    }
    private static ThreadLocal<Identity> sCallerIdentity = new ThreadLocal<Identity>();
    final BatteryStatsService mBatteryStatsService;
    final UsageStatsService mUsageStatsService;
    Configuration mConfiguration = new Configuration();
    int mConfigurationSeq = 0;
    boolean mConfigWillChange;
    final int GL_ES_VERSION;
    HashMap<String, IBinder> mAppBindArgs;
    final StringBuilder mStringBuilder = new StringBuilder(256);
    boolean mStartRunning = false;
    ComponentName mTopComponent;
    String mTopAction;
    String mTopData;
    boolean mSystemReady = false;
    boolean mBooting = false;
    boolean mWaitingUpdate = false;
    boolean mDidUpdate = false;
    Context mContext;
    int mFactoryTest;
    boolean mCheckedForSetup;
    long mAppSwitchesAllowedTime;
    boolean mDidAppSwitch;
    boolean mSleeping = false;
    boolean mShuttingDown = false;
    PowerManager.WakeLock mGoingToSleep;
    PowerManager.WakeLock mLaunchingActivity;
    int mCurTask = 1;
    int mAdjSeq = 0;
    int mLruSeq = 0;
    boolean mSimpleProcessManagement = false;
    int[] mProcDeaths = new int[20];
    boolean mDidDexOpt;
    String mDebugApp = null;
    boolean mWaitForDebugger = false;
    boolean mDebugTransient = false;
    String mOrigDebugApp = null;
    boolean mOrigWaitForDebugger = false;
    boolean mAlwaysFinishActivities = false;
    IActivityController mController = null;
    final RemoteCallbackList<IActivityWatcher> mWatchers
            = new RemoteCallbackList<IActivityWatcher>();
    Runnable mRequestPssCallback;
    final ArrayList<ProcessRecord> mRequestPssList
            = new ArrayList<ProcessRecord>();
    final Thread mProcessStatsThread;
    final ProcessStats mProcessStats = new ProcessStats(
            MONITOR_THREAD_CPU_USAGE);
    final AtomicLong mLastCpuTime = new AtomicLong(0);
    final AtomicBoolean mProcessStatsMutexFree = new AtomicBoolean(true);
    long mLastWriteTime = 0;
    long mInitialStartTime = 0;
    boolean mBooted = false;
    int mProcessLimit = 0;
    WindowManagerService mWindowManager;
    static ActivityManagerService mSelf;
    static ActivityThread mSystemThread;
    private final class AppDeathRecipient implements IBinder.DeathRecipient {
        final ProcessRecord mApp;
        final int mPid;
        final IApplicationThread mAppThread;
        AppDeathRecipient(ProcessRecord app, int pid,
                IApplicationThread thread) {
            if (localLOGV) Slog.v(
                TAG, "New death recipient " + this
                + " for thread " + thread.asBinder());
            mApp = app;
            mPid = pid;
            mAppThread = thread;
        }
        public void binderDied() {
            if (localLOGV) Slog.v(
                TAG, "Death received in " + this
                + " for thread " + mAppThread.asBinder());
            removeRequestedPss(mApp);
            synchronized(ActivityManagerService.this) {
                appDiedLocked(mApp, mPid, mAppThread);
            }
        }
    }
    static final int SHOW_ERROR_MSG = 1;
    static final int SHOW_NOT_RESPONDING_MSG = 2;
    static final int SHOW_FACTORY_ERROR_MSG = 3;
    static final int UPDATE_CONFIGURATION_MSG = 4;
    static final int GC_BACKGROUND_PROCESSES_MSG = 5;
    static final int WAIT_FOR_DEBUGGER_MSG = 6;
    static final int BROADCAST_INTENT_MSG = 7;
    static final int BROADCAST_TIMEOUT_MSG = 8;
    static final int PAUSE_TIMEOUT_MSG = 9;
    static final int IDLE_TIMEOUT_MSG = 10;
    static final int IDLE_NOW_MSG = 11;
    static final int SERVICE_TIMEOUT_MSG = 12;
    static final int UPDATE_TIME_ZONE = 13;
    static final int SHOW_UID_ERROR_MSG = 14;
    static final int IM_FEELING_LUCKY_MSG = 15;
    static final int LAUNCH_TIMEOUT_MSG = 16;
    static final int DESTROY_TIMEOUT_MSG = 17;
    static final int RESUME_TOP_ACTIVITY_MSG = 19;
    static final int PROC_START_TIMEOUT_MSG = 20;
    static final int DO_PENDING_ACTIVITY_LAUNCHES_MSG = 21;
    static final int KILL_APPLICATION_MSG = 22;
    static final int FINALIZE_PENDING_INTENT_MSG = 23;
    AlertDialog mUidAlert;
    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHOW_ERROR_MSG: {
                HashMap data = (HashMap) msg.obj;
                synchronized (ActivityManagerService.this) {
                    ProcessRecord proc = (ProcessRecord)data.get("app");
                    if (proc != null && proc.crashDialog != null) {
                        Slog.e(TAG, "App already has crash dialog: " + proc);
                        return;
                    }
                    AppErrorResult res = (AppErrorResult) data.get("result");
                    if (!mSleeping && !mShuttingDown) {
                        Dialog d = new AppErrorDialog(mContext, res, proc);
                        d.show();
                        proc.crashDialog = d;
                    } else {
                        res.set(0);
                    }
                }
                ensureBootCompleted();
            } break;
            case SHOW_NOT_RESPONDING_MSG: {
                synchronized (ActivityManagerService.this) {
                    HashMap data = (HashMap) msg.obj;
                    ProcessRecord proc = (ProcessRecord)data.get("app");
                    if (proc != null && proc.anrDialog != null) {
                        Slog.e(TAG, "App already has anr dialog: " + proc);
                        return;
                    }
                    broadcastIntentLocked(null, null, new Intent("android.intent.action.ANR"),
                            null, null, 0, null, null, null,
                            false, false, MY_PID, Process.SYSTEM_UID);
                    Dialog d = new AppNotRespondingDialog(ActivityManagerService.this,
                            mContext, proc, (HistoryRecord)data.get("activity"));
                    d.show();
                    proc.anrDialog = d;
                }
                ensureBootCompleted();
            } break;
            case SHOW_FACTORY_ERROR_MSG: {
                Dialog d = new FactoryErrorDialog(
                    mContext, msg.getData().getCharSequence("msg"));
                d.show();
                ensureBootCompleted();
            } break;
            case UPDATE_CONFIGURATION_MSG: {
                final ContentResolver resolver = mContext.getContentResolver();
                Settings.System.putConfiguration(resolver, (Configuration)msg.obj);
            } break;
            case GC_BACKGROUND_PROCESSES_MSG: {
                synchronized (ActivityManagerService.this) {
                    performAppGcsIfAppropriateLocked();
                }
            } break;
            case WAIT_FOR_DEBUGGER_MSG: {
                synchronized (ActivityManagerService.this) {
                    ProcessRecord app = (ProcessRecord)msg.obj;
                    if (msg.arg1 != 0) {
                        if (!app.waitedForDebugger) {
                            Dialog d = new AppWaitingForDebuggerDialog(
                                    ActivityManagerService.this,
                                    mContext, app);
                            app.waitDialog = d;
                            app.waitedForDebugger = true;
                            d.show();
                        }
                    } else {
                        if (app.waitDialog != null) {
                            app.waitDialog.dismiss();
                            app.waitDialog = null;
                        }
                    }
                }
            } break;
            case BROADCAST_INTENT_MSG: {
                if (DEBUG_BROADCAST) Slog.v(
                        TAG, "Received BROADCAST_INTENT_MSG");
                processNextBroadcast(true);
            } break;
            case BROADCAST_TIMEOUT_MSG: {
                if (mDidDexOpt) {
                    mDidDexOpt = false;
                    Message nmsg = mHandler.obtainMessage(BROADCAST_TIMEOUT_MSG);
                    mHandler.sendMessageDelayed(nmsg, BROADCAST_TIMEOUT);
                    return;
                }
                if (mSystemReady) {
                    broadcastTimeout();
                }
            } break;
            case PAUSE_TIMEOUT_MSG: {
                IBinder token = (IBinder)msg.obj;
                Slog.w(TAG, "Activity pause timeout for " + token);
                activityPaused(token, null, true);
            } break;
            case IDLE_TIMEOUT_MSG: {
                if (mDidDexOpt) {
                    mDidDexOpt = false;
                    Message nmsg = mHandler.obtainMessage(IDLE_TIMEOUT_MSG);
                    nmsg.obj = msg.obj;
                    mHandler.sendMessageDelayed(nmsg, IDLE_TIMEOUT);
                    return;
                }
                IBinder token = (IBinder)msg.obj;
                Slog.w(TAG, "Activity idle timeout for " + token);
                activityIdleInternal(token, true, null);
            } break;
            case DESTROY_TIMEOUT_MSG: {
                IBinder token = (IBinder)msg.obj;
                Slog.w(TAG, "Activity destroy timeout for " + token);
                activityDestroyed(token);
            } break;
            case IDLE_NOW_MSG: {
                IBinder token = (IBinder)msg.obj;
                activityIdle(token, null);
            } break;
            case SERVICE_TIMEOUT_MSG: {
                if (mDidDexOpt) {
                    mDidDexOpt = false;
                    Message nmsg = mHandler.obtainMessage(SERVICE_TIMEOUT_MSG);
                    nmsg.obj = msg.obj;
                    mHandler.sendMessageDelayed(nmsg, SERVICE_TIMEOUT);
                    return;
                }
                serviceTimeout((ProcessRecord)msg.obj);
            } break;
            case UPDATE_TIME_ZONE: {
                synchronized (ActivityManagerService.this) {
                    for (int i = mLruProcesses.size() - 1 ; i >= 0 ; i--) {
                        ProcessRecord r = mLruProcesses.get(i);
                        if (r.thread != null) {
                            try {
                                r.thread.updateTimeZone();
                            } catch (RemoteException ex) {
                                Slog.w(TAG, "Failed to update time zone for: " + r.info.processName);
                            }
                        }
                    }
                }
            } break;
            case SHOW_UID_ERROR_MSG: {
                AlertDialog d = new BaseErrorDialog(mContext);
                d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                d.setCancelable(false);
                d.setTitle("System UIDs Inconsistent");
                d.setMessage("UIDs on the system are inconsistent, you need to wipe your data partition or your device will be unstable.");
                d.setButton("I'm Feeling Lucky",
                        mHandler.obtainMessage(IM_FEELING_LUCKY_MSG));
                mUidAlert = d;
                d.show();
            } break;
            case IM_FEELING_LUCKY_MSG: {
                if (mUidAlert != null) {
                    mUidAlert.dismiss();
                    mUidAlert = null;
                }
            } break;
            case LAUNCH_TIMEOUT_MSG: {
                if (mDidDexOpt) {
                    mDidDexOpt = false;
                    Message nmsg = mHandler.obtainMessage(LAUNCH_TIMEOUT_MSG);
                    mHandler.sendMessageDelayed(nmsg, LAUNCH_TIMEOUT);
                    return;
                }
                synchronized (ActivityManagerService.this) {
                    if (mLaunchingActivity.isHeld()) {
                        Slog.w(TAG, "Launch timeout has expired, giving up wake lock!");
                        mLaunchingActivity.release();
                    }
                }
            } break;
            case RESUME_TOP_ACTIVITY_MSG: {
                synchronized (ActivityManagerService.this) {
                    resumeTopActivityLocked(null);
                }
            } break;
            case PROC_START_TIMEOUT_MSG: {
                if (mDidDexOpt) {
                    mDidDexOpt = false;
                    Message nmsg = mHandler.obtainMessage(PROC_START_TIMEOUT_MSG);
                    nmsg.obj = msg.obj;
                    mHandler.sendMessageDelayed(nmsg, PROC_START_TIMEOUT);
                    return;
                }
                ProcessRecord app = (ProcessRecord)msg.obj;
                synchronized (ActivityManagerService.this) {
                    processStartTimedOutLocked(app);
                }
            } break;
            case DO_PENDING_ACTIVITY_LAUNCHES_MSG: {
                synchronized (ActivityManagerService.this) {
                    doPendingActivityLaunchesLocked(true);
                }
            } break;
            case KILL_APPLICATION_MSG: {
                synchronized (ActivityManagerService.this) {
                    int uid = msg.arg1;
                    boolean restart = (msg.arg2 == 1);
                    String pkg = (String) msg.obj;
                    forceStopPackageLocked(pkg, uid, restart, false, true);
                }
            } break;
            case FINALIZE_PENDING_INTENT_MSG: {
                ((PendingIntentRecord)msg.obj).completeFinalize();
            } break;
            }
        }
    };
    public static void setSystemProcess() {
        try {
            ActivityManagerService m = mSelf;
            ServiceManager.addService("activity", m);
            ServiceManager.addService("meminfo", new MemBinder(m));
            if (MONITOR_CPU_USAGE) {
                ServiceManager.addService("cpuinfo", new CpuBinder(m));
            }
            ServiceManager.addService("permission", new PermissionController(m));
            ApplicationInfo info =
                mSelf.mContext.getPackageManager().getApplicationInfo(
                        "android", STOCK_PM_FLAGS);
            mSystemThread.installSystemApplicationInfo(info);
            synchronized (mSelf) {
                ProcessRecord app = mSelf.newProcessRecordLocked(
                        mSystemThread.getApplicationThread(), info,
                        info.processName);
                app.persistent = true;
                app.pid = MY_PID;
                app.maxAdj = SYSTEM_ADJ;
                mSelf.mProcessNames.put(app.processName, app.info.uid, app);
                synchronized (mSelf.mPidsSelfLocked) {
                    mSelf.mPidsSelfLocked.put(app.pid, app);
                }
                mSelf.updateLruProcessLocked(app, true, true);
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(
                    "Unable to find android system package", e);
        }
    }
    public void setWindowManager(WindowManagerService wm) {
        mWindowManager = wm;
    }
    public static final Context main(int factoryTest) {
        AThread thr = new AThread();
        thr.start();
        synchronized (thr) {
            while (thr.mService == null) {
                try {
                    thr.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        ActivityManagerService m = thr.mService;
        mSelf = m;
        ActivityThread at = ActivityThread.systemMain();
        mSystemThread = at;
        Context context = at.getSystemContext();
        m.mContext = context;
        m.mFactoryTest = factoryTest;
        PowerManager pm =
            (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        m.mGoingToSleep = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ActivityManager-Sleep");
        m.mLaunchingActivity = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ActivityManager-Launch");
        m.mLaunchingActivity.setReferenceCounted(false);
        m.mBatteryStatsService.publish(context);
        m.mUsageStatsService.publish(context);
        synchronized (thr) {
            thr.mReady = true;
            thr.notifyAll();
        }
        m.startRunning(null, null, null, null);
        return context;
    }
    public static ActivityManagerService self() {
        return mSelf;
    }
    static class AThread extends Thread {
        ActivityManagerService mService;
        boolean mReady = false;
        public AThread() {
            super("ActivityManager");
        }
        public void run() {
            Looper.prepare();
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_FOREGROUND);
            ActivityManagerService m = new ActivityManagerService();
            synchronized (this) {
                mService = m;
                notifyAll();
            }
            synchronized (this) {
                while (!mReady) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            Looper.loop();
        }
    }
    static class MemBinder extends Binder {
        ActivityManagerService mActivityManagerService;
        MemBinder(ActivityManagerService activityManagerService) {
            mActivityManagerService = activityManagerService;
        }
        @Override
        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            ActivityManagerService service = mActivityManagerService;
            ArrayList<ProcessRecord> procs;
            synchronized (mActivityManagerService) {
                if (args != null && args.length > 0
                        && args[0].charAt(0) != '-') {
                    procs = new ArrayList<ProcessRecord>();
                    int pid = -1;
                    try {
                        pid = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                    }
                    for (int i=service.mLruProcesses.size()-1; i>=0; i--) {
                        ProcessRecord proc = service.mLruProcesses.get(i);
                        if (proc.pid == pid) {
                            procs.add(proc);
                        } else if (proc.processName.equals(args[0])) {
                            procs.add(proc);
                        }
                    }
                    if (procs.size() <= 0) {
                        pw.println("No process found for: " + args[0]);
                        return;
                    }
                } else {
                    procs = new ArrayList<ProcessRecord>(service.mLruProcesses);
                }
            }
            dumpApplicationMemoryUsage(fd, pw, procs, "  ", args);
        }
    }
    static class CpuBinder extends Binder {
        ActivityManagerService mActivityManagerService;
        CpuBinder(ActivityManagerService activityManagerService) {
            mActivityManagerService = activityManagerService;
        }
        @Override
        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            synchronized (mActivityManagerService.mProcessStatsThread) {
                pw.print(mActivityManagerService.mProcessStats.printCurrentState());
            }
        }
    }
    private ActivityManagerService() {
        String v = System.getenv("ANDROID_SIMPLE_PROCESS_MANAGEMENT");
        if (v != null && Integer.getInteger(v) != 0) {
            mSimpleProcessManagement = true;
        }
        v = System.getenv("ANDROID_DEBUG_APP");
        if (v != null) {
            mSimpleProcessManagement = true;
        }
        Slog.i(TAG, "Memory class: " + ActivityManager.staticGetMemoryClass());
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        systemDir.mkdirs();
        mBatteryStatsService = new BatteryStatsService(new File(
                systemDir, "batterystats.bin").toString());
        mBatteryStatsService.getActiveStatistics().readLocked();
        mBatteryStatsService.getActiveStatistics().writeLocked();
        mUsageStatsService = new UsageStatsService( new File(
                systemDir, "usagestats").toString());
        GL_ES_VERSION = SystemProperties.getInt("ro.opengles.version",
            ConfigurationInfo.GL_ES_VERSION_UNDEFINED);
        mConfiguration.setToDefaults();
        mConfiguration.locale = Locale.getDefault();
        mProcessStats.init();
        Watchdog.getInstance().addMonitor(this);
        mProcessStatsThread = new Thread("ProcessStats") {
            public void run() {
                while (true) {
                    try {
                        try {
                            synchronized(this) {
                                final long now = SystemClock.uptimeMillis();
                                long nextCpuDelay = (mLastCpuTime.get()+MONITOR_CPU_MAX_TIME)-now;
                                long nextWriteDelay = (mLastWriteTime+BATTERY_STATS_TIME)-now;
                                if (nextWriteDelay < nextCpuDelay) {
                                    nextCpuDelay = nextWriteDelay;
                                }
                                if (nextCpuDelay > 0) {
                                    mProcessStatsMutexFree.set(true);
                                    this.wait(nextCpuDelay);
                                }
                            }
                        } catch (InterruptedException e) {
                        }
                        updateCpuStatsNow();
                    } catch (Exception e) {
                        Slog.e(TAG, "Unexpected exception collecting process stats", e);
                    }
                }
            }
        };
        mProcessStatsThread.start();
    }
    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.e(TAG, "Activity Manager Crash", e);
            }
            throw e;
        }
    }
    void updateCpuStats() {
        final long now = SystemClock.uptimeMillis();
        if (mLastCpuTime.get() >= now - MONITOR_CPU_MIN_TIME) {
            return;
        }
        if (mProcessStatsMutexFree.compareAndSet(true, false)) {
            synchronized (mProcessStatsThread) {
                mProcessStatsThread.notify();
            }
        }
    }
    void updateCpuStatsNow() {
        synchronized (mProcessStatsThread) {
            mProcessStatsMutexFree.set(false);
            final long now = SystemClock.uptimeMillis();
            boolean haveNewCpuStats = false;
            if (MONITOR_CPU_USAGE &&
                    mLastCpuTime.get() < (now-MONITOR_CPU_MIN_TIME)) {
                mLastCpuTime.set(now);
                haveNewCpuStats = true;
                mProcessStats.update();
                if ("true".equals(SystemProperties.get("events.cpu"))) {
                    int user = mProcessStats.getLastUserTime();
                    int system = mProcessStats.getLastSystemTime();
                    int iowait = mProcessStats.getLastIoWaitTime();
                    int irq = mProcessStats.getLastIrqTime();
                    int softIrq = mProcessStats.getLastSoftIrqTime();
                    int idle = mProcessStats.getLastIdleTime();
                    int total = user + system + iowait + irq + softIrq + idle;
                    if (total == 0) total = 1;
                    EventLog.writeEvent(EventLogTags.CPU,
                            ((user+system+iowait+irq+softIrq) * 100) / total,
                            (user * 100) / total,
                            (system * 100) / total,
                            (iowait * 100) / total,
                            (irq * 100) / total,
                            (softIrq * 100) / total);
                }
            }
            long[] cpuSpeedTimes = mProcessStats.getLastCpuSpeedTimes();
            final BatteryStatsImpl bstats = mBatteryStatsService.getActiveStatistics();
            synchronized(bstats) {
                synchronized(mPidsSelfLocked) {
                    if (haveNewCpuStats) {
                        if (mBatteryStatsService.isOnBattery()) {
                            final int N = mProcessStats.countWorkingStats();
                            for (int i=0; i<N; i++) {
                                ProcessStats.Stats st
                                        = mProcessStats.getWorkingStats(i);
                                ProcessRecord pr = mPidsSelfLocked.get(st.pid);
                                if (pr != null) {
                                    BatteryStatsImpl.Uid.Proc ps = pr.batteryStats;
                                    ps.addCpuTimeLocked(st.rel_utime, st.rel_stime);
                                    ps.addSpeedStepTimes(cpuSpeedTimes);
                                } else {
                                    BatteryStatsImpl.Uid.Proc ps =
                                            bstats.getProcessStatsLocked(st.name, st.pid);
                                    if (ps != null) {
                                        ps.addCpuTimeLocked(st.rel_utime, st.rel_stime);
                                        ps.addSpeedStepTimes(cpuSpeedTimes);
                                    }
                                }
                            }
                        }
                    }
                }
                if (mLastWriteTime < (now-BATTERY_STATS_TIME)) {
                    mLastWriteTime = now;
                    mBatteryStatsService.getActiveStatistics().writeLocked();
                }
            }
        }
    }
    private HashMap<String, IBinder> getCommonServicesLocked() {
        if (mAppBindArgs == null) {
            mAppBindArgs = new HashMap<String, IBinder>();
            mAppBindArgs.put("package", ServiceManager.getService("package"));
            mAppBindArgs.put("window", ServiceManager.getService("window"));
            mAppBindArgs.put(Context.ALARM_SERVICE,
                    ServiceManager.getService(Context.ALARM_SERVICE));
        }
        return mAppBindArgs;
    }
    private final void setFocusedActivityLocked(HistoryRecord r) {
        if (mFocusedActivity != r) {
            mFocusedActivity = r;
            mWindowManager.setFocusedApp(r, true);
        }
    }
    private final void updateLruProcessInternalLocked(ProcessRecord app,
            boolean oomAdj, boolean updateActivityTime, int bestPos) {
        int lrui = mLruProcesses.indexOf(app);
        if (lrui >= 0) mLruProcesses.remove(lrui);
        int i = mLruProcesses.size()-1;
        int skipTop = 0;
        app.lruSeq = mLruSeq;
        if (updateActivityTime) {
            app.lastActivityTime = SystemClock.uptimeMillis();
        }
        if (app.activities.size() > 0) {
            app.lruWeight = app.lastActivityTime;
        } else if (app.pubProviders.size() > 0) {
            app.lruWeight = app.lastActivityTime - CONTENT_APP_IDLE_OFFSET;
            skipTop = MIN_HIDDEN_APPS;
        } else {
            app.lruWeight = app.lastActivityTime - EMPTY_APP_IDLE_OFFSET;
            skipTop = MIN_HIDDEN_APPS;
        }
        while (i >= 0) {
            ProcessRecord p = mLruProcesses.get(i);
            if (skipTop > 0 && p.setAdj >= HIDDEN_APP_MIN_ADJ) {
                skipTop--;
            }
            if (p.lruWeight <= app.lruWeight || i < bestPos) {
                mLruProcesses.add(i+1, app);
                break;
            }
            i--;
        }
        if (i < 0) {
            mLruProcesses.add(0, app);
        }
        if (app.connections.size() > 0) {
            for (ConnectionRecord cr : app.connections) {
                if (cr.binding != null && cr.binding.service != null
                        && cr.binding.service.app != null
                        && cr.binding.service.app.lruSeq != mLruSeq) {
                    updateLruProcessInternalLocked(cr.binding.service.app, oomAdj,
                            updateActivityTime, i+1);
                }
            }
        }
        if (app.conProviders.size() > 0) {
            for (ContentProviderRecord cpr : app.conProviders.keySet()) {
                if (cpr.app != null && cpr.app.lruSeq != mLruSeq) {
                    updateLruProcessInternalLocked(cpr.app, oomAdj,
                            updateActivityTime, i+1);
                }
            }
        }
        if (oomAdj) {
            updateOomAdjLocked();
        }
    }
    private final void updateLruProcessLocked(ProcessRecord app,
            boolean oomAdj, boolean updateActivityTime) {
        mLruSeq++;
        updateLruProcessInternalLocked(app, oomAdj, updateActivityTime, 0);
    }
    private final boolean updateLRUListLocked(HistoryRecord r) {
        final boolean hadit = mLRUActivities.remove(r);
        mLRUActivities.add(r);
        return hadit;
    }
    private final HistoryRecord topRunningActivityLocked(HistoryRecord notTop) {
        int i = mHistory.size()-1;
        while (i >= 0) {
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (!r.finishing && r != notTop) {
                return r;
            }
            i--;
        }
        return null;
    }
    private final HistoryRecord topRunningNonDelayedActivityLocked(HistoryRecord notTop) {
        int i = mHistory.size()-1;
        while (i >= 0) {
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (!r.finishing && !r.delayedResume && r != notTop) {
                return r;
            }
            i--;
        }
        return null;
    }
    private final HistoryRecord topRunningActivityLocked(IBinder token, int taskId) {
        int i = mHistory.size()-1;
        while (i >= 0) {
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (!r.finishing && (token != r) && (taskId != r.task.taskId)) {
                return r;
            }
            i--;
        }
        return null;
    }
    private final ProcessRecord getProcessRecordLocked(
            String processName, int uid) {
        if (uid == Process.SYSTEM_UID) {
            SparseArray<ProcessRecord> procs = mProcessNames.getMap().get(
                    processName);
            return procs != null ? procs.valueAt(0) : null;
        }
        ProcessRecord proc = mProcessNames.get(processName, uid);
        return proc;
    }
    private void ensurePackageDexOpt(String packageName) {
        IPackageManager pm = ActivityThread.getPackageManager();
        try {
            if (pm.performDexOpt(packageName)) {
                mDidDexOpt = true;
            }
        } catch (RemoteException e) {
        }
    }
    private boolean isNextTransitionForward() {
        int transit = mWindowManager.getPendingAppTransition();
        return transit == WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN
                || transit == WindowManagerPolicy.TRANSIT_TASK_OPEN
                || transit == WindowManagerPolicy.TRANSIT_TASK_TO_FRONT;
    }
    private final boolean realStartActivityLocked(HistoryRecord r,
            ProcessRecord app, boolean andResume, boolean checkConfig)
            throws RemoteException {
        r.startFreezingScreenLocked(app, 0);
        mWindowManager.setAppVisibility(r, true);
        if (checkConfig) {
            Configuration config = mWindowManager.updateOrientationFromAppTokens(
                    mConfiguration,
                    r.mayFreezeScreenLocked(app) ? r : null);
            updateConfigurationLocked(config, r);
        }
        r.app = app;
        if (localLOGV) Slog.v(TAG, "Launching: " + r);
        int idx = app.activities.indexOf(r);
        if (idx < 0) {
            app.activities.add(r);
        }
        updateLruProcessLocked(app, true, true);
        try {
            if (app.thread == null) {
                throw new RemoteException();
            }
            List<ResultInfo> results = null;
            List<Intent> newIntents = null;
            if (andResume) {
                results = r.results;
                newIntents = r.newIntents;
            }
            if (DEBUG_SWITCH) Slog.v(TAG, "Launching: " + r
                    + " icicle=" + r.icicle
                    + " with results=" + results + " newIntents=" + newIntents
                    + " andResume=" + andResume);
            if (andResume) {
                EventLog.writeEvent(EventLogTags.AM_RESTART_ACTIVITY,
                        System.identityHashCode(r),
                        r.task.taskId, r.shortComponentName);
            }
            if (r.isHomeActivity) {
                mHomeProcess = app;
            }
            ensurePackageDexOpt(r.intent.getComponent().getPackageName());
            app.thread.scheduleLaunchActivity(new Intent(r.intent), r,
                    System.identityHashCode(r),
                    r.info, r.icicle, results, newIntents, !andResume,
                    isNextTransitionForward());
        } catch (RemoteException e) {
            if (r.launchFailed) {
                Slog.e(TAG, "Second failure launching "
                      + r.intent.getComponent().flattenToShortString()
                      + ", giving up", e);
                appDiedLocked(app, app.pid, app.thread);
                requestFinishActivityLocked(r, Activity.RESULT_CANCELED, null,
                        "2nd-crash");
                return false;
            }
            app.activities.remove(r);
            throw e;
        }
        r.launchFailed = false;
        if (updateLRUListLocked(r)) {
            Slog.w(TAG, "Activity " + r
                  + " being launched, but already in LRU list");
        }
        if (andResume) {
            r.state = ActivityState.RESUMED;
            r.icicle = null;
            r.haveState = false;
            r.stopped = false;
            mResumedActivity = r;
            r.task.touchActiveTime();
            completeResumeLocked(r);
            pauseIfSleepingLocked();                
        } else {
            r.state = ActivityState.STOPPED;
            r.stopped = true;
        }
        startSetupActivityLocked();
        return true;
    }
    private final void startSpecificActivityLocked(HistoryRecord r,
            boolean andResume, boolean checkConfig) {
        ProcessRecord app = getProcessRecordLocked(r.processName,
                r.info.applicationInfo.uid);
        if (r.startTime == 0) {
            r.startTime = SystemClock.uptimeMillis();
            if (mInitialStartTime == 0) {
                mInitialStartTime = r.startTime;
            }
        } else if (mInitialStartTime == 0) {
            mInitialStartTime = SystemClock.uptimeMillis();
        }
        if (app != null && app.thread != null) {
            try {
                realStartActivityLocked(r, app, andResume, checkConfig);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception when starting activity "
                        + r.intent.getComponent().flattenToShortString(), e);
            }
        }
        startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
                "activity", r.intent.getComponent(), false);
    }
    private final ProcessRecord startProcessLocked(String processName,
            ApplicationInfo info, boolean knownToBeDead, int intentFlags,
            String hostingType, ComponentName hostingName, boolean allowWhileBooting) {
        ProcessRecord app = getProcessRecordLocked(processName, info.uid);
        if (DEBUG_PROCESSES) Slog.v(TAG, "startProcess: name=" + processName
                + " app=" + app + " knownToBeDead=" + knownToBeDead
                + " thread=" + (app != null ? app.thread : null)
                + " pid=" + (app != null ? app.pid : -1));
        if (app != null && app.pid > 0) {
            if (!knownToBeDead || app.thread == null) {
                return app;
            } else {
                handleAppDiedLocked(app, true);
            }
        }
        String hostingNameStr = hostingName != null
                ? hostingName.flattenToShortString() : null;
        if ((intentFlags&Intent.FLAG_FROM_BACKGROUND) != 0) {
            if (mBadProcesses.get(info.processName, info.uid) != null) {
                return null;
            }
        } else {
            mProcessCrashTimes.remove(info.processName, info.uid);
            if (mBadProcesses.get(info.processName, info.uid) != null) {
                EventLog.writeEvent(EventLogTags.AM_PROC_GOOD, info.uid,
                        info.processName);
                mBadProcesses.remove(info.processName, info.uid);
                if (app != null) {
                    app.bad = false;
                }
            }
        }
        if (app == null) {
            app = newProcessRecordLocked(null, info, processName);
            mProcessNames.put(processName, info.uid, app);
        } else {
            app.addPackage(info.packageName);
        }
        if (!mSystemReady
                && !isAllowedWhileBooting(info)
                && !allowWhileBooting) {
            if (!mProcessesOnHold.contains(app)) {
                mProcessesOnHold.add(app);
            }
            return app;
        }
        startProcessLocked(app, hostingType, hostingNameStr);
        return (app.pid != 0) ? app : null;
    }
    boolean isAllowedWhileBooting(ApplicationInfo ai) {
        return (ai.flags&ApplicationInfo.FLAG_PERSISTENT) != 0;
    }
    private final void startProcessLocked(ProcessRecord app,
            String hostingType, String hostingNameStr) {
        if (app.pid > 0 && app.pid != MY_PID) {
            synchronized (mPidsSelfLocked) {
                mPidsSelfLocked.remove(app.pid);
                mHandler.removeMessages(PROC_START_TIMEOUT_MSG, app);
            }
            app.pid = 0;
        }
        mProcessesOnHold.remove(app);
        updateCpuStats();
        System.arraycopy(mProcDeaths, 0, mProcDeaths, 1, mProcDeaths.length-1);
        mProcDeaths[0] = 0;
        try {
            int uid = app.info.uid;
            int[] gids = null;
            try {
                gids = mContext.getPackageManager().getPackageGids(
                        app.info.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.w(TAG, "Unable to retrieve gids", e);
            }
            if (mFactoryTest != SystemServer.FACTORY_TEST_OFF) {
                if (mFactoryTest == SystemServer.FACTORY_TEST_LOW_LEVEL
                        && mTopComponent != null
                        && app.processName.equals(mTopComponent.getPackageName())) {
                    uid = 0;
                }
                if (mFactoryTest == SystemServer.FACTORY_TEST_HIGH_LEVEL
                        && (app.info.flags&ApplicationInfo.FLAG_FACTORY_TEST) != 0) {
                    uid = 0;
                }
            }
            int debugFlags = 0;
            if ((app.info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                debugFlags |= Zygote.DEBUG_ENABLE_DEBUGGER;
            }
            if ((app.info.flags & ApplicationInfo.FLAG_VM_SAFE_MODE) != 0 ||
                Zygote.systemInSafeMode == true) {
                debugFlags |= Zygote.DEBUG_ENABLE_SAFEMODE;
            }
            if ("1".equals(SystemProperties.get("debug.checkjni"))) {
                debugFlags |= Zygote.DEBUG_ENABLE_CHECKJNI;
            }
            if ("1".equals(SystemProperties.get("debug.assert"))) {
                debugFlags |= Zygote.DEBUG_ENABLE_ASSERT;
            }
            int pid = Process.start("android.app.ActivityThread",
                    mSimpleProcessManagement ? app.processName : null, uid, uid,
                    gids, debugFlags, null);
            BatteryStatsImpl bs = app.batteryStats.getBatteryStats();
            synchronized (bs) {
                if (bs.isOnBattery()) {
                    app.batteryStats.incStartsLocked();
                }
            }
            EventLog.writeEvent(EventLogTags.AM_PROC_START, pid, uid,
                    app.processName, hostingType,
                    hostingNameStr != null ? hostingNameStr : "");
            if (app.persistent) {
                Watchdog.getInstance().processStarted(app, app.processName, pid);
            }
            StringBuilder buf = mStringBuilder;
            buf.setLength(0);
            buf.append("Start proc ");
            buf.append(app.processName);
            buf.append(" for ");
            buf.append(hostingType);
            if (hostingNameStr != null) {
                buf.append(" ");
                buf.append(hostingNameStr);
            }
            buf.append(": pid=");
            buf.append(pid);
            buf.append(" uid=");
            buf.append(uid);
            buf.append(" gids={");
            if (gids != null) {
                for (int gi=0; gi<gids.length; gi++) {
                    if (gi != 0) buf.append(", ");
                    buf.append(gids[gi]);
                }
            }
            buf.append("}");
            Slog.i(TAG, buf.toString());
            if (pid == 0 || pid == MY_PID) {
                app.pid = MY_PID;
                app.removed = false;
                mStartingProcesses.add(app);
            } else if (pid > 0) {
                app.pid = pid;
                app.removed = false;
                synchronized (mPidsSelfLocked) {
                    this.mPidsSelfLocked.put(pid, app);
                    Message msg = mHandler.obtainMessage(PROC_START_TIMEOUT_MSG);
                    msg.obj = app;
                    mHandler.sendMessageDelayed(msg, PROC_START_TIMEOUT);
                }
            } else {
                app.pid = 0;
                RuntimeException e = new RuntimeException(
                        "Failure starting process " + app.processName
                        + ": returned pid=" + pid);
                Slog.e(TAG, e.getMessage(), e);
            }
        } catch (RuntimeException e) {
            app.pid = 0;
            Slog.e(TAG, "Failure starting process " + app.processName, e);
        }
    }
    private final void startPausingLocked(boolean userLeaving, boolean uiSleeping) {
        if (mPausingActivity != null) {
            RuntimeException e = new RuntimeException();
            Slog.e(TAG, "Trying to pause when pause is already pending for "
                  + mPausingActivity, e);
        }
        HistoryRecord prev = mResumedActivity;
        if (prev == null) {
            RuntimeException e = new RuntimeException();
            Slog.e(TAG, "Trying to pause when nothing is resumed", e);
            resumeTopActivityLocked(null);
            return;
        }
        if (DEBUG_PAUSE) Slog.v(TAG, "Start pausing: " + prev);
        mResumedActivity = null;
        mPausingActivity = prev;
        mLastPausedActivity = prev;
        prev.state = ActivityState.PAUSING;
        prev.task.touchActiveTime();
        updateCpuStats();
        if (prev.app != null && prev.app.thread != null) {
            if (DEBUG_PAUSE) Slog.v(TAG, "Enqueueing pending pause: " + prev);
            try {
                EventLog.writeEvent(EventLogTags.AM_PAUSE_ACTIVITY,
                        System.identityHashCode(prev),
                        prev.shortComponentName);
                prev.app.thread.schedulePauseActivity(prev, prev.finishing, userLeaving,
                        prev.configChangeFlags);
                updateUsageStats(prev, false);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                mPausingActivity = null;
                mLastPausedActivity = null;
            }
        } else {
            mPausingActivity = null;
            mLastPausedActivity = null;
        }
        if (!mSleeping && !mShuttingDown) {
            mLaunchingActivity.acquire();
            if (!mHandler.hasMessages(LAUNCH_TIMEOUT_MSG)) {
                Message msg = mHandler.obtainMessage(LAUNCH_TIMEOUT_MSG);
                mHandler.sendMessageDelayed(msg, LAUNCH_TIMEOUT);
            }
        }
        if (mPausingActivity != null) {
            if (!uiSleeping) {
                prev.pauseKeyDispatchingLocked();
            } else {
                if (DEBUG_PAUSE) Slog.v(TAG, "Key dispatch not paused for screen off");
            }
            Message msg = mHandler.obtainMessage(PAUSE_TIMEOUT_MSG);
            msg.obj = prev;
            mHandler.sendMessageDelayed(msg, PAUSE_TIMEOUT);
            if (DEBUG_PAUSE) Slog.v(TAG, "Waiting for pause to complete...");
        } else {
            if (DEBUG_PAUSE) Slog.v(TAG, "Activity not running, resuming next.");
            resumeTopActivityLocked(null);
        }
    }
    private final void completePauseLocked() {
        HistoryRecord prev = mPausingActivity;
        if (DEBUG_PAUSE) Slog.v(TAG, "Complete pause: " + prev);
        if (prev != null) {
            if (prev.finishing) {
                if (DEBUG_PAUSE) Slog.v(TAG, "Executing finish of activity: " + prev);
                prev = finishCurrentActivityLocked(prev, FINISH_AFTER_VISIBLE);
            } else if (prev.app != null) {
                if (DEBUG_PAUSE) Slog.v(TAG, "Enqueueing pending stop: " + prev);
                if (prev.waitingVisible) {
                    prev.waitingVisible = false;
                    mWaitingVisibleActivities.remove(prev);
                    if (DEBUG_SWITCH || DEBUG_PAUSE) Slog.v(
                            TAG, "Complete pause, no longer waiting: " + prev);
                }
                if (prev.configDestroy) {
                    if (DEBUG_PAUSE) Slog.v(TAG, "Destroying after pause: " + prev);
                    destroyActivityLocked(prev, true);
                } else {
                    mStoppingActivities.add(prev);
                    if (mStoppingActivities.size() > 3) {
                        if (DEBUG_PAUSE) Slog.v(TAG, "To many pending stops, forcing idle");
                        Message msg = Message.obtain();
                        msg.what = ActivityManagerService.IDLE_NOW_MSG;
                        mHandler.sendMessage(msg);
                    }
                }
            } else {
                if (DEBUG_PAUSE) Slog.v(TAG, "App died during pause, not stopping: " + prev);
                prev = null;
            }
            mPausingActivity = null;
        }
        if (!mSleeping && !mShuttingDown) {
            resumeTopActivityLocked(prev);
        } else {
            if (mGoingToSleep.isHeld()) {
                mGoingToSleep.release();
            }
            if (mShuttingDown) {
                notifyAll();
            }
        }
        if (prev != null) {
            prev.resumeKeyDispatchingLocked();
        }
        if (prev.app != null && prev.cpuTimeAtResume > 0 && mBatteryStatsService.isOnBattery()) {
            long diff = 0;
            synchronized (mProcessStatsThread) {
                diff = mProcessStats.getCpuTimeForPid(prev.app.pid) - prev.cpuTimeAtResume;
            }
            if (diff > 0) {
                BatteryStatsImpl bsi = mBatteryStatsService.getActiveStatistics();
                synchronized (bsi) {
                    BatteryStatsImpl.Uid.Proc ps =
                            bsi.getProcessStatsLocked(prev.info.applicationInfo.uid,
                            prev.info.packageName);
                    if (ps != null) {
                        ps.addForegroundTimeLocked(diff);
                    }
                }
            }
        }
        prev.cpuTimeAtResume = 0; 
    }
    private final void completeResumeLocked(HistoryRecord next) {
        next.idle = false;
        next.results = null;
        next.newIntents = null;
        Message msg = mHandler.obtainMessage(IDLE_TIMEOUT_MSG);
        msg.obj = next;
        mHandler.sendMessageDelayed(msg, IDLE_TIMEOUT);
        if (false) {
            msg = mHandler.obtainMessage(IDLE_NOW_MSG);
            msg.obj = next;
            mHandler.sendMessage(msg);
        }
        reportResumedActivityLocked(next);
        next.thumbnail = null;
        setFocusedActivityLocked(next);
        next.resumeKeyDispatchingLocked();
        ensureActivitiesVisibleLocked(null, 0);
        mWindowManager.executeAppTransition();
        mNoAnimActivities.clear();
        if (next.app != null) {
            synchronized (mProcessStatsThread) {
                next.cpuTimeAtResume = mProcessStats.getCpuTimeForPid(next.app.pid);
            }
        } else {
            next.cpuTimeAtResume = 0; 
        }
    }
    private final void ensureActivitiesVisibleLocked(HistoryRecord top,
            HistoryRecord starting, String onlyThisProcess, int configChanges) {
        if (DEBUG_VISBILITY) Slog.v(
                TAG, "ensureActivitiesVisible behind " + top
                + " configChanges=0x" + Integer.toHexString(configChanges));
        final int count = mHistory.size();
        int i = count-1;
        while (mHistory.get(i) != top) {
            i--;
        }
        HistoryRecord r;
        boolean behindFullscreen = false;
        for (; i>=0; i--) {
            r = (HistoryRecord)mHistory.get(i);
            if (DEBUG_VISBILITY) Slog.v(
                    TAG, "Make visible? " + r + " finishing=" + r.finishing
                    + " state=" + r.state);
            if (r.finishing) {
                continue;
            }
            final boolean doThisProcess = onlyThisProcess == null
                    || onlyThisProcess.equals(r.processName);
            if (r != starting && doThisProcess) {
                ensureActivityConfigurationLocked(r, 0);
            }
            if (r.app == null || r.app.thread == null) {
                if (onlyThisProcess == null
                        || onlyThisProcess.equals(r.processName)) {
                    if (DEBUG_VISBILITY) Slog.v(
                            TAG, "Start and freeze screen for " + r);
                    if (r != starting) {
                        r.startFreezingScreenLocked(r.app, configChanges);
                    }
                    if (!r.visible) {
                        if (DEBUG_VISBILITY) Slog.v(
                                TAG, "Starting and making visible: " + r);
                        mWindowManager.setAppVisibility(r, true);
                    }
                    if (r != starting) {
                        startSpecificActivityLocked(r, false, false);
                    }
                }
            } else if (r.visible) {
                if (DEBUG_VISBILITY) Slog.v(
                        TAG, "Skipping: already visible at " + r);
                r.stopFreezingScreenLocked(false);
            } else if (onlyThisProcess == null) {
                r.visible = true;
                if (r.state != ActivityState.RESUMED && r != starting) {
                    if (DEBUG_VISBILITY) Slog.v(
                            TAG, "Making visible and scheduling visibility: " + r);
                    try {
                        mWindowManager.setAppVisibility(r, true);
                        r.app.thread.scheduleWindowVisibility(r, true);
                        r.stopFreezingScreenLocked(false);
                    } catch (Exception e) {
                        Slog.w(TAG, "Exception thrown making visibile: "
                                + r.intent.getComponent(), e);
                    }
                }
            }
            configChanges |= r.configChangeFlags;
            if (r.fullscreen) {
                if (DEBUG_VISBILITY) Slog.v(
                        TAG, "Stopping: fullscreen at " + r);
                behindFullscreen = true;
                i--;
                break;
            }
        }
        while (i >= 0) {
            r = (HistoryRecord)mHistory.get(i);
            if (DEBUG_VISBILITY) Slog.v(
                    TAG, "Make invisible? " + r + " finishing=" + r.finishing
                    + " state=" + r.state
                    + " behindFullscreen=" + behindFullscreen);
            if (!r.finishing) {
                if (behindFullscreen) {
                    if (r.visible) {
                        if (DEBUG_VISBILITY) Slog.v(
                                TAG, "Making invisible: " + r);
                        r.visible = false;
                        try {
                            mWindowManager.setAppVisibility(r, false);
                            if ((r.state == ActivityState.STOPPING
                                    || r.state == ActivityState.STOPPED)
                                    && r.app != null && r.app.thread != null) {
                                if (DEBUG_VISBILITY) Slog.v(
                                        TAG, "Scheduling invisibility: " + r);
                                r.app.thread.scheduleWindowVisibility(r, false);
                            }
                        } catch (Exception e) {
                            Slog.w(TAG, "Exception thrown making hidden: "
                                    + r.intent.getComponent(), e);
                        }
                    } else {
                        if (DEBUG_VISBILITY) Slog.v(
                                TAG, "Already invisible: " + r);
                    }
                } else if (r.fullscreen) {
                    if (DEBUG_VISBILITY) Slog.v(
                            TAG, "Now behindFullscreen: " + r);
                    behindFullscreen = true;
                }
            }
            i--;
        }
    }
    private final void ensureActivitiesVisibleLocked(HistoryRecord starting,
            int configChanges) {
        HistoryRecord r = topRunningActivityLocked(null);
        if (r != null) {
            ensureActivitiesVisibleLocked(r, starting, null, configChanges);
        }
    }
    private void updateUsageStats(HistoryRecord resumedComponent, boolean resumed) {
        if (resumed) {
            mUsageStatsService.noteResumeComponent(resumedComponent.realActivity);
        } else {
            mUsageStatsService.notePauseComponent(resumedComponent.realActivity);
        }
    }
    private boolean startHomeActivityLocked() {
        if (mFactoryTest == SystemServer.FACTORY_TEST_LOW_LEVEL
                && mTopAction == null) {
            return false;
        }
        Intent intent = new Intent(
            mTopAction,
            mTopData != null ? Uri.parse(mTopData) : null);
        intent.setComponent(mTopComponent);
        if (mFactoryTest != SystemServer.FACTORY_TEST_LOW_LEVEL) {
            intent.addCategory(Intent.CATEGORY_HOME);
        }
        ActivityInfo aInfo =
            intent.resolveActivityInfo(mContext.getPackageManager(),
                    STOCK_PM_FLAGS);
        if (aInfo != null) {
            intent.setComponent(new ComponentName(
                    aInfo.applicationInfo.packageName, aInfo.name));
            ProcessRecord app = getProcessRecordLocked(aInfo.processName,
                    aInfo.applicationInfo.uid);
            if (app == null || app.instrumentationClass == null) {
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityLocked(null, intent, null, null, 0, aInfo,
                        null, null, 0, 0, 0, false, false);
            }
        }
        return true;
    }
    private void startSetupActivityLocked() {
        if (mCheckedForSetup) {
            return;
        }
        final ContentResolver resolver = mContext.getContentResolver();
        if (mFactoryTest != SystemServer.FACTORY_TEST_LOW_LEVEL &&
                Settings.Secure.getInt(resolver,
                        Settings.Secure.DEVICE_PROVISIONED, 0) != 0) {
            mCheckedForSetup = true;
            Intent intent = new Intent(Intent.ACTION_UPGRADE_SETUP);
            List<ResolveInfo> ris = mSelf.mContext.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.GET_META_DATA);
            ResolveInfo ri = null;
            for (int i=0; ris != null && i<ris.size(); i++) {
                if ((ris.get(i).activityInfo.applicationInfo.flags
                        & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    ri = ris.get(i);
                    break;
                }
            }
            if (ri != null) {
                String vers = ri.activityInfo.metaData != null
                        ? ri.activityInfo.metaData.getString(Intent.METADATA_SETUP_VERSION)
                        : null;
                if (vers == null && ri.activityInfo.applicationInfo.metaData != null) {
                    vers = ri.activityInfo.applicationInfo.metaData.getString(
                            Intent.METADATA_SETUP_VERSION);
                }
                String lastVers = Settings.Secure.getString(
                        resolver, Settings.Secure.LAST_SETUP_SHOWN);
                if (vers != null && !vers.equals(lastVers)) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(new ComponentName(
                            ri.activityInfo.packageName, ri.activityInfo.name));
                    startActivityLocked(null, intent, null, null, 0, ri.activityInfo,
                            null, null, 0, 0, 0, false, false);
                }
            }
        }
    }
    private void reportResumedActivityLocked(HistoryRecord r) {
        final int identHash = System.identityHashCode(r);
        updateUsageStats(r, true);
        int i = mWatchers.beginBroadcast();
        while (i > 0) {
            i--;
            IActivityWatcher w = mWatchers.getBroadcastItem(i);
            if (w != null) {
                try {
                    w.activityResuming(identHash);
                } catch (RemoteException e) {
                }
            }
        }
        mWatchers.finishBroadcast();
    }
    private final boolean resumeTopActivityLocked(HistoryRecord prev) {
        HistoryRecord next = topRunningActivityLocked(null);
        final boolean userLeaving = mUserLeaving;
        mUserLeaving = false;
        if (next == null) {
            return startHomeActivityLocked();
        }
        next.delayedResume = false;
        if (mResumedActivity == next && next.state == ActivityState.RESUMED) {
            mWindowManager.executeAppTransition();
            mNoAnimActivities.clear();
            return false;
        }
        if ((mSleeping || mShuttingDown)
                && mLastPausedActivity == next && next.state == ActivityState.PAUSED) {
            mWindowManager.executeAppTransition();
            mNoAnimActivities.clear();
            return false;
        }
        mStoppingActivities.remove(next);
        mWaitingVisibleActivities.remove(next);
        if (DEBUG_SWITCH) Slog.v(TAG, "Resuming " + next);
        if (mPausingActivity != null) {
            if (DEBUG_SWITCH) Slog.v(TAG, "Skip resume: pausing=" + mPausingActivity);
            return false;
        }
        if (mResumedActivity != null) {
            if (DEBUG_SWITCH) Slog.v(TAG, "Skip resume: need to start pausing");
            startPausingLocked(userLeaving, false);
            return true;
        }
        if (prev != null && prev != next) {
            if (!prev.waitingVisible && next != null && !next.nowVisible) {
                prev.waitingVisible = true;
                mWaitingVisibleActivities.add(prev);
                if (DEBUG_SWITCH) Slog.v(
                        TAG, "Resuming top, waiting visible to hide: " + prev);
            } else {
                if (prev.finishing) {
                    mWindowManager.setAppVisibility(prev, false);
                    if (DEBUG_SWITCH) Slog.v(TAG, "Not waiting for visible to hide: "
                            + prev + ", waitingVisible="
                            + (prev != null ? prev.waitingVisible : null)
                            + ", nowVisible=" + next.nowVisible);
                } else {
                    if (DEBUG_SWITCH) Slog.v(TAG, "Previous already visible but still waiting to hide: "
                        + prev + ", waitingVisible="
                        + (prev != null ? prev.waitingVisible : null)
                        + ", nowVisible=" + next.nowVisible);
                }
            }
        }
        if (prev != null) {
            if (prev.finishing) {
                if (DEBUG_TRANSITION) Slog.v(TAG,
                        "Prepare close transition: prev=" + prev);
                if (mNoAnimActivities.contains(prev)) {
                    mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_NONE);
                } else {
                    mWindowManager.prepareAppTransition(prev.task == next.task
                            ? WindowManagerPolicy.TRANSIT_ACTIVITY_CLOSE
                            : WindowManagerPolicy.TRANSIT_TASK_CLOSE);
                }
                mWindowManager.setAppWillBeHidden(prev);
                mWindowManager.setAppVisibility(prev, false);
            } else {
                if (DEBUG_TRANSITION) Slog.v(TAG,
                        "Prepare open transition: prev=" + prev);
                if (mNoAnimActivities.contains(next)) {
                    mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_NONE);
                } else {
                    mWindowManager.prepareAppTransition(prev.task == next.task
                            ? WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN
                            : WindowManagerPolicy.TRANSIT_TASK_OPEN);
                }
            }
            if (false) {
                mWindowManager.setAppWillBeHidden(prev);
                mWindowManager.setAppVisibility(prev, false);
            }
        } else if (mHistory.size() > 1) {
            if (DEBUG_TRANSITION) Slog.v(TAG,
                    "Prepare open transition: no previous");
            if (mNoAnimActivities.contains(next)) {
                mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_NONE);
            } else {
                mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN);
            }
        }
        if (next.app != null && next.app.thread != null) {
            if (DEBUG_SWITCH) Slog.v(TAG, "Resume running: " + next);
            mWindowManager.setAppVisibility(next, true);
            HistoryRecord lastResumedActivity = mResumedActivity;
            ActivityState lastState = next.state;
            updateCpuStats();
            next.state = ActivityState.RESUMED;
            mResumedActivity = next;
            next.task.touchActiveTime();
            updateLruProcessLocked(next.app, true, true);
            updateLRUListLocked(next);
            boolean updated;
            synchronized (this) {
                Configuration config = mWindowManager.updateOrientationFromAppTokens(
                        mConfiguration,
                        next.mayFreezeScreenLocked(next.app) ? next : null);
                if (config != null) {
                    next.frozenBeforeDestroy = true;
                }
                updated = updateConfigurationLocked(config, next);
            }
            if (!updated) {
                HistoryRecord nextNext = topRunningActivityLocked(null);
                if (DEBUG_SWITCH) Slog.i(TAG,
                        "Activity config changed during resume: " + next
                        + ", new next: " + nextNext);
                if (nextNext != next) {
                    mHandler.sendEmptyMessage(RESUME_TOP_ACTIVITY_MSG);
                }
                setFocusedActivityLocked(next);
                ensureActivitiesVisibleLocked(null, 0);
                mWindowManager.executeAppTransition();
                mNoAnimActivities.clear();
                return true;
            }
            try {
                ArrayList a = next.results;
                if (a != null) {
                    final int N = a.size();
                    if (!next.finishing && N > 0) {
                        if (DEBUG_RESULTS) Slog.v(
                                TAG, "Delivering results to " + next
                                + ": " + a);
                        next.app.thread.scheduleSendResult(next, a);
                    }
                }
                if (next.newIntents != null) {
                    next.app.thread.scheduleNewIntent(next.newIntents, next);
                }
                EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY,
                        System.identityHashCode(next),
                        next.task.taskId, next.shortComponentName);
                next.app.thread.scheduleResumeActivity(next,
                        isNextTransitionForward());
                pauseIfSleepingLocked();
            } catch (Exception e) {
                next.state = lastState;
                mResumedActivity = lastResumedActivity;
                Slog.i(TAG, "Restarting because process died: " + next);
                if (!next.hasBeenLaunched) {
                    next.hasBeenLaunched = true;
                } else {
                    if (SHOW_APP_STARTING_ICON) {
                        mWindowManager.setAppStartingWindow(
                                next, next.packageName, next.theme,
                                next.nonLocalizedLabel,
                                next.labelRes, next.icon, null, true);
                    }
                }
                startSpecificActivityLocked(next, true, false);
                return true;
            }
            try {
                next.visible = true;
                completeResumeLocked(next);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during resume of " + next, e);
                requestFinishActivityLocked(next, Activity.RESULT_CANCELED, null,
                        "resume-exception");
                return true;
            }
            next.icicle = null;
            next.haveState = false;
            next.stopped = false;
        } else {
            if (!next.hasBeenLaunched) {
                next.hasBeenLaunched = true;
            } else {
                if (SHOW_APP_STARTING_ICON) {
                    mWindowManager.setAppStartingWindow(
                            next, next.packageName, next.theme,
                            next.nonLocalizedLabel,
                            next.labelRes, next.icon, null, true);
                }
                if (DEBUG_SWITCH) Slog.v(TAG, "Restarting: " + next);
            }
            startSpecificActivityLocked(next, true, true);
        }
        return true;
    }
    private final void startActivityLocked(HistoryRecord r, boolean newTask,
            boolean doResume) {
        final int NH = mHistory.size();
        int addPos = -1;
        if (!newTask) {
            HistoryRecord next = null;
            boolean startIt = true;
            for (int i = NH-1; i >= 0; i--) {
                HistoryRecord p = (HistoryRecord)mHistory.get(i);
                if (p.finishing) {
                    continue;
                }
                if (p.task == r.task) {
                    addPos = i+1;
                    if (!startIt) {
                        mHistory.add(addPos, r);
                        r.inHistory = true;
                        r.task.numActivities++;
                        mWindowManager.addAppToken(addPos, r, r.task.taskId,
                                r.info.screenOrientation, r.fullscreen);
                        if (VALIDATE_TOKENS) {
                            mWindowManager.validateAppTokens(mHistory);
                        }
                        return;
                    }
                    break;
                }
                if (p.fullscreen) {
                    startIt = false;
                }
                next = p;
            }
        }
        if (addPos < 0) {
            addPos = mHistory.size();
        }
        if (addPos < NH) {
            mUserLeaving = false;
            if (DEBUG_USER_LEAVING) Slog.v(TAG, "startActivity() behind front, mUserLeaving=false");
        }
        mHistory.add(addPos, r);
        r.inHistory = true;
        r.frontOfTask = newTask;
        r.task.numActivities++;
        if (NH > 0) {
            boolean showStartingIcon = newTask;
            ProcessRecord proc = r.app;
            if (proc == null) {
                proc = mProcessNames.get(r.processName, r.info.applicationInfo.uid);
            }
            if (proc == null || proc.thread == null) {
                showStartingIcon = true;
            }
            if (DEBUG_TRANSITION) Slog.v(TAG,
                    "Prepare open transition: starting " + r);
            if ((r.intent.getFlags()&Intent.FLAG_ACTIVITY_NO_ANIMATION) != 0) {
                mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_NONE);
                mNoAnimActivities.add(r);
            } else if ((r.intent.getFlags()&Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) != 0) {
                mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_TASK_OPEN);
                mNoAnimActivities.remove(r);
            } else {
                mWindowManager.prepareAppTransition(newTask
                        ? WindowManagerPolicy.TRANSIT_TASK_OPEN
                        : WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN);
                mNoAnimActivities.remove(r);
            }
            mWindowManager.addAppToken(
                    addPos, r, r.task.taskId, r.info.screenOrientation, r.fullscreen);
            boolean doShow = true;
            if (newTask) {
                if ((r.intent.getFlags()
                        &Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) != 0) {
                    resetTaskIfNeededLocked(r, r);
                    doShow = topRunningNonDelayedActivityLocked(null) == r;
                }
            }
            if (SHOW_APP_STARTING_ICON && doShow) {
                HistoryRecord prev = mResumedActivity;
                if (prev != null) {
                    if (prev.task != r.task) prev = null;
                    else if (prev.nowVisible) prev = null;
                }
                mWindowManager.setAppStartingWindow(
                        r, r.packageName, r.theme, r.nonLocalizedLabel,
                        r.labelRes, r.icon, prev, showStartingIcon);
            }
        } else {
            mWindowManager.addAppToken(addPos, r, r.task.taskId,
                    r.info.screenOrientation, r.fullscreen);
        }
        if (VALIDATE_TOKENS) {
            mWindowManager.validateAppTokens(mHistory);
        }
        if (doResume) {
            resumeTopActivityLocked(null);
        }
    }
    private final HistoryRecord performClearTaskLocked(int taskId,
            HistoryRecord newR, int launchFlags, boolean doClear) {
        int i = mHistory.size();
        while (i > 0) {
            i--;
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (r.task.taskId == taskId) {
                i++;
                break;
            }
        }
        while (i > 0) {
            i--;
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (r.finishing) {
                continue;
            }
            if (r.task.taskId != taskId) {
                return null;
            }
            if (r.realActivity.equals(newR.realActivity)) {
                HistoryRecord ret = r;
                if (doClear) {
                    while (i < (mHistory.size()-1)) {
                        i++;
                        r = (HistoryRecord)mHistory.get(i);
                        if (r.finishing) {
                            continue;
                        }
                        if (finishActivityLocked(r, i, Activity.RESULT_CANCELED,
                                null, "clear")) {
                            i--;
                        }
                    }
                }
                if (ret.launchMode == ActivityInfo.LAUNCH_MULTIPLE
                        && (launchFlags&Intent.FLAG_ACTIVITY_SINGLE_TOP) == 0) {
                    if (!ret.finishing) {
                        int index = indexOfTokenLocked(ret);
                        if (index >= 0) {
                            finishActivityLocked(ret, index, Activity.RESULT_CANCELED,
                                    null, "clear");
                        }
                        return null;
                    }
                }
                return ret;
            }
        }
        return null;
    }
    private final int findActivityInHistoryLocked(HistoryRecord r, int task) {
        int i = mHistory.size();
        while (i > 0) {
            i--;
            HistoryRecord candidate = (HistoryRecord)mHistory.get(i);
            if (candidate.task.taskId != task) {
                break;
            }
            if (candidate.realActivity.equals(r.realActivity)) {
                return i;
            }
        }
        return -1;
    }
    private final HistoryRecord moveActivityToFrontLocked(int where) {
        HistoryRecord newTop = (HistoryRecord)mHistory.remove(where);
        int top = mHistory.size();
        HistoryRecord oldTop = (HistoryRecord)mHistory.get(top-1);
        mHistory.add(top, newTop);
        oldTop.frontOfTask = false;
        newTop.frontOfTask = true;
        return newTop;
    }
    private final void deliverNewIntentLocked(HistoryRecord r, Intent intent) {
        boolean sent = false;
        if (r.state == ActivityState.RESUMED
                && r.app != null && r.app.thread != null) {
            try {
                ArrayList<Intent> ar = new ArrayList<Intent>();
                ar.add(new Intent(intent));
                r.app.thread.scheduleNewIntent(ar, r);
                sent = true;
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown sending new intent to " + r, e);
            }
        }
        if (!sent) {
            r.addNewIntentLocked(new Intent(intent));
        }
    }
    private final void logStartActivity(int tag, HistoryRecord r,
            TaskRecord task) {
        EventLog.writeEvent(tag,
                System.identityHashCode(r), task.taskId,
                r.shortComponentName, r.intent.getAction(),
                r.intent.getType(), r.intent.getDataString(),
                r.intent.getFlags());
    }
    private final int startActivityLocked(IApplicationThread caller,
            Intent intent, String resolvedType,
            Uri[] grantedUriPermissions,
            int grantedMode, ActivityInfo aInfo, IBinder resultTo,
            String resultWho, int requestCode,
            int callingPid, int callingUid, boolean onlyIfNeeded,
            boolean componentSpecified) {
        Slog.i(TAG, "Starting activity: " + intent);
        HistoryRecord sourceRecord = null;
        HistoryRecord resultRecord = null;
        if (resultTo != null) {
            int index = indexOfTokenLocked(resultTo);
            if (DEBUG_RESULTS) Slog.v(
                TAG, "Sending result to " + resultTo + " (index " + index + ")");
            if (index >= 0) {
                sourceRecord = (HistoryRecord)mHistory.get(index);
                if (requestCode >= 0 && !sourceRecord.finishing) {
                    resultRecord = sourceRecord;
                }
            }
        }
        int launchFlags = intent.getFlags();
        if ((launchFlags&Intent.FLAG_ACTIVITY_FORWARD_RESULT) != 0
                && sourceRecord != null) {
            if (requestCode >= 0) {
                return START_FORWARD_AND_REQUEST_CONFLICT;
            }
            resultRecord = sourceRecord.resultTo;
            resultWho = sourceRecord.resultWho;
            requestCode = sourceRecord.requestCode;
            sourceRecord.resultTo = null;
            if (resultRecord != null) {
                resultRecord.removeResultsLocked(
                    sourceRecord, resultWho, requestCode);
            }
        }
        int err = START_SUCCESS;
        if (intent.getComponent() == null) {
            err = START_INTENT_NOT_RESOLVED;
        }
        if (err == START_SUCCESS && aInfo == null) {
            err = START_CLASS_NOT_FOUND;
        }
        ProcessRecord callerApp = null;
        if (err == START_SUCCESS && caller != null) {
            callerApp = getRecordForAppLocked(caller);
            if (callerApp != null) {
                callingPid = callerApp.pid;
                callingUid = callerApp.info.uid;
            } else {
                Slog.w(TAG, "Unable to find app for caller " + caller
                      + " (pid=" + callingPid + ") when starting: "
                      + intent.toString());
                err = START_PERMISSION_DENIED;
            }
        }
        if (err != START_SUCCESS) {
            if (resultRecord != null) {
                sendActivityResultLocked(-1,
                    resultRecord, resultWho, requestCode,
                    Activity.RESULT_CANCELED, null);
            }
            return err;
        }
        final int perm = checkComponentPermission(aInfo.permission, callingPid,
                callingUid, aInfo.exported ? -1 : aInfo.applicationInfo.uid);
        if (perm != PackageManager.PERMISSION_GRANTED) {
            if (resultRecord != null) {
                sendActivityResultLocked(-1,
                    resultRecord, resultWho, requestCode,
                    Activity.RESULT_CANCELED, null);
            }
            String msg = "Permission Denial: starting " + intent.toString()
                    + " from " + callerApp + " (pid=" + callingPid
                    + ", uid=" + callingUid + ")"
                    + " requires " + aInfo.permission;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        if (mController != null) {
            boolean abort = false;
            try {
                Intent watchIntent = intent.cloneFilter();
                abort = !mController.activityStarting(watchIntent,
                        aInfo.applicationInfo.packageName);
            } catch (RemoteException e) {
                mController = null;
            }
            if (abort) {
                if (resultRecord != null) {
                    sendActivityResultLocked(-1,
                        resultRecord, resultWho, requestCode,
                        Activity.RESULT_CANCELED, null);
                }
                return START_SUCCESS;
            }
        }
        HistoryRecord r = new HistoryRecord(this, callerApp, callingUid,
                intent, resolvedType, aInfo, mConfiguration,
                resultRecord, resultWho, requestCode, componentSpecified);
        if (mResumedActivity == null
                || mResumedActivity.info.applicationInfo.uid != callingUid) {
            if (!checkAppSwitchAllowedLocked(callingPid, callingUid, "Activity start")) {
                PendingActivityLaunch pal = new PendingActivityLaunch();
                pal.r = r;
                pal.sourceRecord = sourceRecord;
                pal.grantedUriPermissions = grantedUriPermissions;
                pal.grantedMode = grantedMode;
                pal.onlyIfNeeded = onlyIfNeeded;
                mPendingActivityLaunches.add(pal);
                return START_SWITCHES_CANCELED;
            }
        }
        if (mDidAppSwitch) {
            mAppSwitchesAllowedTime = 0;
        } else {
            mDidAppSwitch = true;
        }
        doPendingActivityLaunchesLocked(false);
        return startActivityUncheckedLocked(r, sourceRecord,
                grantedUriPermissions, grantedMode, onlyIfNeeded, true);
    }
    private final void doPendingActivityLaunchesLocked(boolean doResume) {
        final int N = mPendingActivityLaunches.size();
        if (N <= 0) {
            return;
        }
        for (int i=0; i<N; i++) {
            PendingActivityLaunch pal = mPendingActivityLaunches.get(i);
            startActivityUncheckedLocked(pal.r, pal.sourceRecord,
                    pal.grantedUriPermissions, pal.grantedMode, pal.onlyIfNeeded,
                    doResume && i == (N-1));
        }
        mPendingActivityLaunches.clear();
    }
    private final int startActivityUncheckedLocked(HistoryRecord r,
            HistoryRecord sourceRecord, Uri[] grantedUriPermissions,
            int grantedMode, boolean onlyIfNeeded, boolean doResume) {
        final Intent intent = r.intent;
        final int callingUid = r.launchedFromUid;
        int launchFlags = intent.getFlags();
        mUserLeaving = (launchFlags&Intent.FLAG_ACTIVITY_NO_USER_ACTION) == 0;
        if (DEBUG_USER_LEAVING) Slog.v(TAG,
                "startActivity() => mUserLeaving=" + mUserLeaving);
        if (!doResume) {
            r.delayedResume = true;
        }
        HistoryRecord notTop = (launchFlags&Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                != 0 ? r : null;
        if (onlyIfNeeded) {
            HistoryRecord checkedCaller = sourceRecord;
            if (checkedCaller == null) {
                checkedCaller = topRunningNonDelayedActivityLocked(notTop);
            }
            if (!checkedCaller.realActivity.equals(r.realActivity)) {
                onlyIfNeeded = false;
            }
        }
        if (grantedUriPermissions != null && callingUid > 0) {
            for (int i=0; i<grantedUriPermissions.length; i++) {
                grantUriPermissionLocked(callingUid, r.packageName,
                        grantedUriPermissions[i], grantedMode, r);
            }
        }
        grantUriPermissionFromIntentLocked(callingUid, r.packageName,
                intent, r);
        if (sourceRecord == null) {
            if ((launchFlags&Intent.FLAG_ACTIVITY_NEW_TASK) == 0) {
                Slog.w(TAG, "startActivity called from non-Activity context; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: "
                      + intent);
                launchFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
            }
        } else if (sourceRecord.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            launchFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
        } else if (r.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE
                || r.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {
            launchFlags |= Intent.FLAG_ACTIVITY_NEW_TASK;
        }
        if (r.resultTo != null && (launchFlags&Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            Slog.w(TAG, "Activity is launching as a new task, so cancelling activity result.");
            sendActivityResultLocked(-1,
                    r.resultTo, r.resultWho, r.requestCode,
                Activity.RESULT_CANCELED, null);
            r.resultTo = null;
        }
        boolean addingToTask = false;
        if (((launchFlags&Intent.FLAG_ACTIVITY_NEW_TASK) != 0 &&
                (launchFlags&Intent.FLAG_ACTIVITY_MULTIPLE_TASK) == 0)
                || r.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK
                || r.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            if (r.resultTo == null) {
                HistoryRecord taskTop = r.launchMode != ActivityInfo.LAUNCH_SINGLE_INSTANCE
                        ? findTaskLocked(intent, r.info)
                        : findActivityLocked(intent, r.info);
                if (taskTop != null) {
                    if (taskTop.task.intent == null) {
                        taskTop.task.setIntent(intent, r.info);
                    }
                    HistoryRecord curTop = topRunningNonDelayedActivityLocked(notTop);
                    if (curTop.task != taskTop.task) {
                        r.intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        boolean callerAtFront = sourceRecord == null
                                || curTop.task == sourceRecord.task;
                        if (callerAtFront) {
                            moveTaskToFrontLocked(taskTop.task, r);
                        }
                    }
                    if ((launchFlags&Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) != 0) {
                        taskTop = resetTaskIfNeededLocked(taskTop, r);
                    }
                    if (onlyIfNeeded) {
                        if (doResume) {
                            resumeTopActivityLocked(null);
                        }
                        return START_RETURN_INTENT_TO_CALLER;
                    }
                    if ((launchFlags&Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0
                            || r.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK
                            || r.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
                        HistoryRecord top = performClearTaskLocked(
                                taskTop.task.taskId, r, launchFlags, true);
                        if (top != null) {
                            if (top.frontOfTask) {
                                top.task.setIntent(r.intent, r.info);
                            }
                            logStartActivity(EventLogTags.AM_NEW_INTENT, r, top.task);
                            deliverNewIntentLocked(top, r.intent);
                        } else {
                            addingToTask = true;
                            sourceRecord = taskTop;
                        }
                    } else if (r.realActivity.equals(taskTop.task.realActivity)) {
                        if ((launchFlags&Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0
                                && taskTop.realActivity.equals(r.realActivity)) {
                            logStartActivity(EventLogTags.AM_NEW_INTENT, r, taskTop.task);
                            if (taskTop.frontOfTask) {
                                taskTop.task.setIntent(r.intent, r.info);
                            }
                            deliverNewIntentLocked(taskTop, r.intent);
                        } else if (!r.intent.filterEquals(taskTop.task.intent)) {
                            addingToTask = true;
                            sourceRecord = taskTop;
                        }
                    } else if ((launchFlags&Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) == 0) {
                        addingToTask = true;
                        sourceRecord = taskTop;
                    } else if (!taskTop.task.rootWasReset) {
                        taskTop.task.setIntent(r.intent, r.info);
                    }
                    if (!addingToTask) {
                        if (doResume) {
                            resumeTopActivityLocked(null);
                        }
                        return START_TASK_TO_FRONT;
                    }
                }
            }
        }
        if (r.packageName != null) {
            HistoryRecord top = topRunningNonDelayedActivityLocked(notTop);
            if (top != null && r.resultTo == null) {
                if (top.realActivity.equals(r.realActivity)) {
                    if (top.app != null && top.app.thread != null) {
                        if ((launchFlags&Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0
                            || r.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP
                            || r.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {
                            logStartActivity(EventLogTags.AM_NEW_INTENT, top, top.task);
                            if (doResume) {
                                resumeTopActivityLocked(null);
                            }
                            if (onlyIfNeeded) {
                                return START_RETURN_INTENT_TO_CALLER;
                            }
                            deliverNewIntentLocked(top, r.intent);
                            return START_DELIVERED_TO_TOP;
                        }
                    }
                }
            }
        } else {
            if (r.resultTo != null) {
                sendActivityResultLocked(-1,
                        r.resultTo, r.resultWho, r.requestCode,
                    Activity.RESULT_CANCELED, null);
            }
            return START_CLASS_NOT_FOUND;
        }
        boolean newTask = false;
        if (r.resultTo == null && !addingToTask
                && (launchFlags&Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            mCurTask++;
            if (mCurTask <= 0) {
                mCurTask = 1;
            }
            r.task = new TaskRecord(mCurTask, r.info, intent,
                    (r.info.flags&ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH) != 0);
            if (DEBUG_TASKS) Slog.v(TAG, "Starting new activity " + r
                    + " in new task " + r.task);
            newTask = true;
            addRecentTaskLocked(r.task);
        } else if (sourceRecord != null) {
            if (!addingToTask &&
                    (launchFlags&Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0) {
                HistoryRecord top = performClearTaskLocked(
                        sourceRecord.task.taskId, r, launchFlags, true);
                if (top != null) {
                    logStartActivity(EventLogTags.AM_NEW_INTENT, r, top.task);
                    deliverNewIntentLocked(top, r.intent);
                    if (doResume) {
                        resumeTopActivityLocked(null);
                    }
                    return START_DELIVERED_TO_TOP;
                }
            } else if (!addingToTask &&
                    (launchFlags&Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) != 0) {
                int where = findActivityInHistoryLocked(r, sourceRecord.task.taskId);
                if (where >= 0) {
                    HistoryRecord top = moveActivityToFrontLocked(where);
                    logStartActivity(EventLogTags.AM_NEW_INTENT, r, top.task);
                    deliverNewIntentLocked(top, r.intent);
                    if (doResume) {
                        resumeTopActivityLocked(null);
                    }
                    return START_DELIVERED_TO_TOP;
                }
            }
            r.task = sourceRecord.task;
            if (DEBUG_TASKS) Slog.v(TAG, "Starting new activity " + r
                    + " in existing task " + r.task);
        } else {
            final int N = mHistory.size();
            HistoryRecord prev =
                N > 0 ? (HistoryRecord)mHistory.get(N-1) : null;
            r.task = prev != null
                ? prev.task
                : new TaskRecord(mCurTask, r.info, intent,
                        (r.info.flags&ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH) != 0);
            if (DEBUG_TASKS) Slog.v(TAG, "Starting new activity " + r
                    + " in new guessed " + r.task);
        }
        if (newTask) {
            EventLog.writeEvent(EventLogTags.AM_CREATE_TASK, r.task.taskId);
        }
        logStartActivity(EventLogTags.AM_CREATE_ACTIVITY, r, r.task);
        startActivityLocked(r, newTask, doResume);
        return START_SUCCESS;
    }
    void reportActivityLaunchedLocked(boolean timeout, HistoryRecord r,
            long thisTime, long totalTime) {
        for (int i=mWaitingActivityLaunched.size()-1; i>=0; i--) {
            WaitResult w = mWaitingActivityLaunched.get(i);
            w.timeout = timeout;
            if (r != null) {
                w.who = new ComponentName(r.info.packageName, r.info.name);
            }
            w.thisTime = thisTime;
            w.totalTime = totalTime;
        }
        notify();
    }
    void reportActivityVisibleLocked(HistoryRecord r) {
        for (int i=mWaitingActivityVisible.size()-1; i>=0; i--) {
            WaitResult w = mWaitingActivityVisible.get(i);
            w.timeout = false;
            if (r != null) {
                w.who = new ComponentName(r.info.packageName, r.info.name);
            }
            w.totalTime = SystemClock.uptimeMillis() - w.thisTime;
            w.thisTime = w.totalTime;
        }
        notify();
    }
    private final int startActivityMayWait(IApplicationThread caller,
            Intent intent, String resolvedType, Uri[] grantedUriPermissions,
            int grantedMode, IBinder resultTo,
            String resultWho, int requestCode, boolean onlyIfNeeded,
            boolean debug, WaitResult outResult, Configuration config) {
        if (intent != null && intent.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        final boolean componentSpecified = intent.getComponent() != null;
        intent = new Intent(intent);
        ActivityInfo aInfo;
        try {
            ResolveInfo rInfo =
                ActivityThread.getPackageManager().resolveIntent(
                        intent, resolvedType,
                        PackageManager.MATCH_DEFAULT_ONLY
                        | STOCK_PM_FLAGS);
            aInfo = rInfo != null ? rInfo.activityInfo : null;
        } catch (RemoteException e) {
            aInfo = null;
        }
        if (aInfo != null) {
            intent.setComponent(new ComponentName(
                    aInfo.applicationInfo.packageName, aInfo.name));
            if (debug) {
                if (!aInfo.processName.equals("system")) {
                    setDebugApp(aInfo.processName, true, false);
                }
            }
        }
        synchronized (this) {
            int callingPid;
            int callingUid;
            if (caller == null) {
                callingPid = Binder.getCallingPid();
                callingUid = Binder.getCallingUid();
            } else {
                callingPid = callingUid = -1;
            }
            mConfigWillChange = config != null && mConfiguration.diff(config) != 0;
            if (DEBUG_CONFIGURATION) Slog.v(TAG,
                    "Starting activity when config will change = " + mConfigWillChange);
            final long origId = Binder.clearCallingIdentity();
            int res = startActivityLocked(caller, intent, resolvedType,
                    grantedUriPermissions, grantedMode, aInfo,
                    resultTo, resultWho, requestCode, callingPid, callingUid,
                    onlyIfNeeded, componentSpecified);
            if (mConfigWillChange) {
                enforceCallingPermission(android.Manifest.permission.CHANGE_CONFIGURATION,
                        "updateConfiguration()");
                mConfigWillChange = false;
                if (DEBUG_CONFIGURATION) Slog.v(TAG,
                        "Updating to new configuration after starting activity.");
                updateConfigurationLocked(config, null);
            }
            Binder.restoreCallingIdentity(origId);
            if (outResult != null) {
                outResult.result = res;
                if (res == IActivityManager.START_SUCCESS) {
                    mWaitingActivityLaunched.add(outResult);
                    do {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    } while (!outResult.timeout && outResult.who == null);
                } else if (res == IActivityManager.START_TASK_TO_FRONT) {
                    HistoryRecord r = this.topRunningActivityLocked(null);
                    if (r.nowVisible) {
                        outResult.timeout = false;
                        outResult.who = new ComponentName(r.info.packageName, r.info.name);
                        outResult.totalTime = 0;
                        outResult.thisTime = 0;
                    } else {
                        outResult.thisTime = SystemClock.uptimeMillis();
                        mWaitingActivityVisible.add(outResult);
                        do {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                            }
                        } while (!outResult.timeout && outResult.who == null);
                    }
                }
            }
            return res;
        }
    }
    public final int startActivity(IApplicationThread caller,
            Intent intent, String resolvedType, Uri[] grantedUriPermissions,
            int grantedMode, IBinder resultTo,
            String resultWho, int requestCode, boolean onlyIfNeeded,
            boolean debug) {
        return startActivityMayWait(caller, intent, resolvedType,
                grantedUriPermissions, grantedMode, resultTo, resultWho,
                requestCode, onlyIfNeeded, debug, null, null);
    }
    public final WaitResult startActivityAndWait(IApplicationThread caller,
            Intent intent, String resolvedType, Uri[] grantedUriPermissions,
            int grantedMode, IBinder resultTo,
            String resultWho, int requestCode, boolean onlyIfNeeded,
            boolean debug) {
        WaitResult res = new WaitResult();
        startActivityMayWait(caller, intent, resolvedType,
                grantedUriPermissions, grantedMode, resultTo, resultWho,
                requestCode, onlyIfNeeded, debug, res, null);
        return res;
    }
    public final int startActivityWithConfig(IApplicationThread caller,
            Intent intent, String resolvedType, Uri[] grantedUriPermissions,
            int grantedMode, IBinder resultTo,
            String resultWho, int requestCode, boolean onlyIfNeeded,
            boolean debug, Configuration config) {
        return startActivityMayWait(caller, intent, resolvedType,
                grantedUriPermissions, grantedMode, resultTo, resultWho,
                requestCode, onlyIfNeeded, debug, null, config);
    }
     public int startActivityIntentSender(IApplicationThread caller,
            IntentSender intent, Intent fillInIntent, String resolvedType,
            IBinder resultTo, String resultWho, int requestCode,
            int flagsMask, int flagsValues) {
        if (fillInIntent != null && fillInIntent.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        IIntentSender sender = intent.getTarget();
        if (!(sender instanceof PendingIntentRecord)) {
            throw new IllegalArgumentException("Bad PendingIntent object");
        }
        PendingIntentRecord pir = (PendingIntentRecord)sender;
        synchronized (this) {
            if (mResumedActivity != null
                    && mResumedActivity.info.applicationInfo.uid ==
                            Binder.getCallingUid()) {
                mAppSwitchesAllowedTime = 0;
            }
        }
        return pir.sendInner(0, fillInIntent, resolvedType,
                null, resultTo, resultWho, requestCode, flagsMask, flagsValues);
    }
    public boolean startNextMatchingActivity(IBinder callingActivity,
            Intent intent) {
        if (intent != null && intent.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized (this) {
            int index = indexOfTokenLocked(callingActivity);
            if (index < 0) {
                return false;
            }
            HistoryRecord r = (HistoryRecord)mHistory.get(index);
            if (r.app == null || r.app.thread == null) {
                return false;
            }
            intent = new Intent(intent);
            intent.setDataAndType(r.intent.getData(), r.intent.getType());
            intent.setComponent(null);
            ActivityInfo aInfo = null;
            try {
                List<ResolveInfo> resolves =
                    ActivityThread.getPackageManager().queryIntentActivities(
                            intent, r.resolvedType,
                            PackageManager.MATCH_DEFAULT_ONLY | STOCK_PM_FLAGS);
                final int N = resolves != null ? resolves.size() : 0;
                for (int i=0; i<N; i++) {
                    ResolveInfo rInfo = resolves.get(i);
                    if (rInfo.activityInfo.packageName.equals(r.packageName)
                            && rInfo.activityInfo.name.equals(r.info.name)) {
                        i++;
                        if (i<N) {
                            aInfo = resolves.get(i).activityInfo;
                        }
                        break;
                    }
                }
            } catch (RemoteException e) {
            }
            if (aInfo == null) {
                return false;
            }
            intent.setComponent(new ComponentName(
                    aInfo.applicationInfo.packageName, aInfo.name));
            intent.setFlags(intent.getFlags()&~(
                    Intent.FLAG_ACTIVITY_FORWARD_RESULT|
                    Intent.FLAG_ACTIVITY_CLEAR_TOP|
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK|
                    Intent.FLAG_ACTIVITY_NEW_TASK));
            final boolean wasFinishing = r.finishing;
            r.finishing = true;
            final HistoryRecord resultTo = r.resultTo;
            final String resultWho = r.resultWho;
            final int requestCode = r.requestCode;
            r.resultTo = null;
            if (resultTo != null) {
                resultTo.removeResultsLocked(r, resultWho, requestCode);
            }
            final long origId = Binder.clearCallingIdentity();
            int res = startActivityLocked(r.app.thread, intent,
                    r.resolvedType, null, 0, aInfo, resultTo, resultWho,
                    requestCode, -1, r.launchedFromUid, false, false);
            Binder.restoreCallingIdentity(origId);
            r.finishing = wasFinishing;
            if (res != START_SUCCESS) {
                return false;
            }
            return true;
        }
    }
    public final int startActivityInPackage(int uid,
            Intent intent, String resolvedType, IBinder resultTo,
            String resultWho, int requestCode, boolean onlyIfNeeded) {
        final int callingUid = Binder.getCallingUid();
        if (callingUid != 0 && callingUid != Process.myUid()) {
            throw new SecurityException(
                    "startActivityInPackage only available to the system");
        }
        final boolean componentSpecified = intent.getComponent() != null;
        intent = new Intent(intent);
        ActivityInfo aInfo;
        try {
            ResolveInfo rInfo =
                ActivityThread.getPackageManager().resolveIntent(
                        intent, resolvedType,
                        PackageManager.MATCH_DEFAULT_ONLY | STOCK_PM_FLAGS);
            aInfo = rInfo != null ? rInfo.activityInfo : null;
        } catch (RemoteException e) {
            aInfo = null;
        }
        if (aInfo != null) {
            intent.setComponent(new ComponentName(
                    aInfo.applicationInfo.packageName, aInfo.name));
        }
        synchronized(this) {
            return startActivityLocked(null, intent, resolvedType,
                    null, 0, aInfo, resultTo, resultWho, requestCode, -1, uid,
                    onlyIfNeeded, componentSpecified);
        }
    }
    private final void addRecentTaskLocked(TaskRecord task) {
        int N = mRecentTasks.size();
        for (int i=0; i<N; i++) {
            TaskRecord tr = mRecentTasks.get(i);
            if ((task.affinity != null && task.affinity.equals(tr.affinity))
                    || (task.intent != null && task.intent.filterEquals(tr.intent))) {
                mRecentTasks.remove(i);
                i--;
                N--;
                if (task.intent == null) {
                    task = tr;
                }
            }
        }
        if (N >= MAX_RECENT_TASKS) {
            mRecentTasks.remove(N-1);
        }
        mRecentTasks.add(0, task);
    }
    public void setRequestedOrientation(IBinder token,
            int requestedOrientation) {
        synchronized (this) {
            int index = indexOfTokenLocked(token);
            if (index < 0) {
                return;
            }
            HistoryRecord r = (HistoryRecord)mHistory.get(index);
            final long origId = Binder.clearCallingIdentity();
            mWindowManager.setAppOrientation(r, requestedOrientation);
            Configuration config = mWindowManager.updateOrientationFromAppTokens(
                    mConfiguration,
                    r.mayFreezeScreenLocked(r.app) ? r : null);
            if (config != null) {
                r.frozenBeforeDestroy = true;
                if (!updateConfigurationLocked(config, r)) {
                    resumeTopActivityLocked(null);
                }
            }
            Binder.restoreCallingIdentity(origId);
        }
    }
    public int getRequestedOrientation(IBinder token) {
        synchronized (this) {
            int index = indexOfTokenLocked(token);
            if (index < 0) {
                return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            }
            HistoryRecord r = (HistoryRecord)mHistory.get(index);
            return mWindowManager.getAppOrientation(r);
        }
    }
    private final void stopActivityLocked(HistoryRecord r) {
        if (DEBUG_SWITCH) Slog.d(TAG, "Stopping: " + r);
        if ((r.intent.getFlags()&Intent.FLAG_ACTIVITY_NO_HISTORY) != 0
                || (r.info.flags&ActivityInfo.FLAG_NO_HISTORY) != 0) {
            if (!r.finishing) {
                requestFinishActivityLocked(r, Activity.RESULT_CANCELED, null,
                        "no-history");
            }
        } else if (r.app != null && r.app.thread != null) {
            if (mFocusedActivity == r) {
                setFocusedActivityLocked(topRunningActivityLocked(null));
            }
            r.resumeKeyDispatchingLocked();
            try {
                r.stopped = false;
                r.state = ActivityState.STOPPING;
                if (DEBUG_VISBILITY) Slog.v(
                        TAG, "Stopping visible=" + r.visible + " for " + r);
                if (!r.visible) {
                    mWindowManager.setAppVisibility(r, false);
                }
                r.app.thread.scheduleStopActivity(r, r.visible, r.configChangeFlags);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                r.stopped = true;
                r.state = ActivityState.STOPPED;
                if (r.configDestroy) {
                    destroyActivityLocked(r, true);
                }
            }
        }
    }
    private final boolean requestFinishActivityLocked(IBinder token, int resultCode,
            Intent resultData, String reason) {
        if (DEBUG_RESULTS) Slog.v(
            TAG, "Finishing activity: token=" + token
            + ", result=" + resultCode + ", data=" + resultData);
        int index = indexOfTokenLocked(token);
        if (index < 0) {
            return false;
        }
        HistoryRecord r = (HistoryRecord)mHistory.get(index);
        boolean lastActivity = true;
        for (int i=mHistory.size()-1; i>=0; i--) {
            HistoryRecord p = (HistoryRecord)mHistory.get(i);
            if (!p.finishing && p != r) {
                lastActivity = false;
                break;
            }
        }
        if (lastActivity) {
            if (r.intent.hasCategory(Intent.CATEGORY_HOME)) {
                return false;
            }
        }
        finishActivityLocked(r, index, resultCode, resultData, reason);
        return true;
    }
    private final boolean finishActivityLocked(HistoryRecord r, int index,
            int resultCode, Intent resultData, String reason) {
        if (r.finishing) {
            Slog.w(TAG, "Duplicate finish request for " + r);
            return false;
        }
        r.finishing = true;
        EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY,
                System.identityHashCode(r),
                r.task.taskId, r.shortComponentName, reason);
        r.task.numActivities--;
        if (index < (mHistory.size()-1)) {
            HistoryRecord next = (HistoryRecord)mHistory.get(index+1);
            if (next.task == r.task) {
                if (r.frontOfTask) {
                    next.frontOfTask = true;
                }
                if ((r.intent.getFlags()&Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) != 0) {
                    next.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                }
            }
        }
        r.pauseKeyDispatchingLocked();
        if (mFocusedActivity == r) {
            setFocusedActivityLocked(topRunningActivityLocked(null));
        }
        HistoryRecord resultTo = r.resultTo;
        if (resultTo != null) {
            if (DEBUG_RESULTS) Slog.v(TAG, "Adding result to " + resultTo
                    + " who=" + r.resultWho + " req=" + r.requestCode
                    + " res=" + resultCode + " data=" + resultData);
            if (r.info.applicationInfo.uid > 0) {
                grantUriPermissionFromIntentLocked(r.info.applicationInfo.uid,
                        r.packageName, resultData, r);
            }
            resultTo.addResultLocked(r, r.resultWho, r.requestCode, resultCode,
                                     resultData);
            r.resultTo = null;
        }
        else if (DEBUG_RESULTS) Slog.v(TAG, "No result destination from " + r);
        r.results = null;
        r.pendingResults = null;
        r.newIntents = null;
        r.icicle = null;
        if (mPendingThumbnails.size() > 0) {
            mCancelledThumbnails.add(r);
        }
        if (mResumedActivity == r) {
            boolean endTask = index <= 0
                    || ((HistoryRecord)mHistory.get(index-1)).task != r.task;
            if (DEBUG_TRANSITION) Slog.v(TAG,
                    "Prepare close transition: finishing " + r);
            mWindowManager.prepareAppTransition(endTask
                    ? WindowManagerPolicy.TRANSIT_TASK_CLOSE
                    : WindowManagerPolicy.TRANSIT_ACTIVITY_CLOSE);
            mWindowManager.setAppVisibility(r, false);
            if (mPausingActivity == null) {
                if (DEBUG_PAUSE) Slog.v(TAG, "Finish needs to pause: " + r);
                if (DEBUG_USER_LEAVING) Slog.v(TAG, "finish() => pause with userLeaving=false");
                startPausingLocked(false, false);
            }
        } else if (r.state != ActivityState.PAUSING) {
            if (DEBUG_PAUSE) Slog.v(TAG, "Finish not pausing: " + r);
            return finishCurrentActivityLocked(r, index,
                    FINISH_AFTER_PAUSE) == null;
        } else {
            if (DEBUG_PAUSE) Slog.v(TAG, "Finish waiting for pause of: " + r);
        }
        return false;
    }
    private static final int FINISH_IMMEDIATELY = 0;
    private static final int FINISH_AFTER_PAUSE = 1;
    private static final int FINISH_AFTER_VISIBLE = 2;
    private final HistoryRecord finishCurrentActivityLocked(HistoryRecord r,
            int mode) {
        final int index = indexOfTokenLocked(r);
        if (index < 0) {
            return null;
        }
        return finishCurrentActivityLocked(r, index, mode);
    }
    private final HistoryRecord finishCurrentActivityLocked(HistoryRecord r,
            int index, int mode) {
        if (mode == FINISH_AFTER_VISIBLE && r.nowVisible) {
            if (!mStoppingActivities.contains(r)) {
                mStoppingActivities.add(r);
                if (mStoppingActivities.size() > 3) {
                    Message msg = Message.obtain();
                    msg.what = ActivityManagerService.IDLE_NOW_MSG;
                    mHandler.sendMessage(msg);
                }
            }
            r.state = ActivityState.STOPPING;
            updateOomAdjLocked();
            return r;
        }
        mStoppingActivities.remove(r);
        mWaitingVisibleActivities.remove(r);
        if (mResumedActivity == r) {
            mResumedActivity = null;
        }
        final ActivityState prevState = r.state;
        r.state = ActivityState.FINISHING;
        if (mode == FINISH_IMMEDIATELY
                || prevState == ActivityState.STOPPED
                || prevState == ActivityState.INITIALIZING) {
            return destroyActivityLocked(r, true) ? null : r;
        } else {
            if (localLOGV) Slog.v(TAG, "Enqueueing pending finish: " + r);
            mFinishingActivities.add(r);
            resumeTopActivityLocked(null);
        }
        return r;
    }
    public final boolean finishActivity(IBinder token, int resultCode, Intent resultData) {
        if (resultData != null && resultData.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            if (mController != null) {
                HistoryRecord next = topRunningActivityLocked(token, 0);
                if (next != null) {
                    boolean resumeOK = true;
                    try {
                        resumeOK = mController.activityResuming(next.packageName);
                    } catch (RemoteException e) {
                        mController = null;
                    }
                    if (!resumeOK) {
                        return false;
                    }
                }
            }
            final long origId = Binder.clearCallingIdentity();
            boolean res = requestFinishActivityLocked(token, resultCode,
                    resultData, "app-request");
            Binder.restoreCallingIdentity(origId);
            return res;
        }
    }
    void sendActivityResultLocked(int callingUid, HistoryRecord r,
            String resultWho, int requestCode, int resultCode, Intent data) {
        if (callingUid > 0) {
            grantUriPermissionFromIntentLocked(callingUid, r.packageName,
                    data, r);
        }
        if (DEBUG_RESULTS) Slog.v(TAG, "Send activity result to " + r
                + " : who=" + resultWho + " req=" + requestCode
                + " res=" + resultCode + " data=" + data);
        if (mResumedActivity == r && r.app != null && r.app.thread != null) {
            try {
                ArrayList<ResultInfo> list = new ArrayList<ResultInfo>();
                list.add(new ResultInfo(resultWho, requestCode,
                        resultCode, data));
                r.app.thread.scheduleSendResult(r, list);
                return;
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown sending result to " + r, e);
            }
        }
        r.addResultLocked(null, resultWho, requestCode, resultCode, data);
    }
    public final void finishSubActivity(IBinder token, String resultWho,
            int requestCode) {
        synchronized(this) {
            int index = indexOfTokenLocked(token);
            if (index < 0) {
                return;
            }
            HistoryRecord self = (HistoryRecord)mHistory.get(index);
            final long origId = Binder.clearCallingIdentity();
            int i;
            for (i=mHistory.size()-1; i>=0; i--) {
                HistoryRecord r = (HistoryRecord)mHistory.get(i);
                if (r.resultTo == self && r.requestCode == requestCode) {
                    if ((r.resultWho == null && resultWho == null) ||
                        (r.resultWho != null && r.resultWho.equals(resultWho))) {
                        finishActivityLocked(r, i,
                                Activity.RESULT_CANCELED, null, "request-sub");
                    }
                }
            }
            Binder.restoreCallingIdentity(origId);
        }
    }
    public boolean willActivityBeVisible(IBinder token) {
        synchronized(this) {
            int i;
            for (i=mHistory.size()-1; i>=0; i--) {
                HistoryRecord r = (HistoryRecord)mHistory.get(i);
                if (r == token) {
                    return true;
                }
                if (r.fullscreen && !r.finishing) {
                    return false;
                }
            }
            return true;
        }
    }
    public void overridePendingTransition(IBinder token, String packageName,
            int enterAnim, int exitAnim) {
        synchronized(this) {
            int index = indexOfTokenLocked(token);
            if (index < 0) {
                return;
            }
            HistoryRecord self = (HistoryRecord)mHistory.get(index);
            final long origId = Binder.clearCallingIdentity();
            if (self.state == ActivityState.RESUMED
                    || self.state == ActivityState.PAUSING) {
                mWindowManager.overridePendingAppTransition(packageName,
                        enterAnim, exitAnim);
            }
            Binder.restoreCallingIdentity(origId);
        }
    }
    private final void cleanUpActivityServicesLocked(HistoryRecord r) {
        if (r.connections != null) {
            Iterator<ConnectionRecord> it = r.connections.iterator();
            while (it.hasNext()) {
                ConnectionRecord c = it.next();
                removeConnectionLocked(c, null, r);
            }
            r.connections = null;
        }
    }
    private final void cleanUpActivityLocked(HistoryRecord r, boolean cleanServices) {
        if (mResumedActivity == r) {
            mResumedActivity = null;
        }
        if (mFocusedActivity == r) {
            mFocusedActivity = null;
        }
        r.configDestroy = false;
        r.frozenBeforeDestroy = false;
        mFinishingActivities.remove(r);
        mWaitingVisibleActivities.remove(r);
        if (r.finishing && r.pendingResults != null) {
            for (WeakReference<PendingIntentRecord> apr : r.pendingResults) {
                PendingIntentRecord rec = apr.get();
                if (rec != null) {
                    cancelIntentSenderLocked(rec, false);
                }
            }
            r.pendingResults = null;
        }
        if (cleanServices) {
            cleanUpActivityServicesLocked(r);            
        }
        if (mPendingThumbnails.size() > 0) {
            mCancelledThumbnails.add(r);
        }
        mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
        mHandler.removeMessages(IDLE_TIMEOUT_MSG, r);
    }
    private final void removeActivityFromHistoryLocked(HistoryRecord r) {
        if (r.state != ActivityState.DESTROYED) {
            mHistory.remove(r);
            r.inHistory = false;
            r.state = ActivityState.DESTROYED;
            mWindowManager.removeAppToken(r);
            if (VALIDATE_TOKENS) {
                mWindowManager.validateAppTokens(mHistory);
            }
            cleanUpActivityServicesLocked(r);
            removeActivityUriPermissionsLocked(r);
        }
    }
    private final boolean destroyActivityLocked(HistoryRecord r,
            boolean removeFromApp) {
        if (DEBUG_SWITCH) Slog.v(
            TAG, "Removing activity: token=" + r
              + ", app=" + (r.app != null ? r.app.processName : "(null)"));
        EventLog.writeEvent(EventLogTags.AM_DESTROY_ACTIVITY,
                System.identityHashCode(r),
                r.task.taskId, r.shortComponentName);
        boolean removedFromHistory = false;
        cleanUpActivityLocked(r, false);
        final boolean hadApp = r.app != null;
        if (hadApp) {
            if (removeFromApp) {
                int idx = r.app.activities.indexOf(r);
                if (idx >= 0) {
                    r.app.activities.remove(idx);
                }
                if (r.persistent) {
                    decPersistentCountLocked(r.app);
                }
                if (r.app.activities.size() == 0) {
                    updateLruProcessLocked(r.app, true, false);
                }
            }
            boolean skipDestroy = false;
            try {
                if (DEBUG_SWITCH) Slog.i(TAG, "Destroying: " + r);
                r.app.thread.scheduleDestroyActivity(r, r.finishing,
                        r.configChangeFlags);
            } catch (Exception e) {
                if (r.finishing) {
                    removeActivityFromHistoryLocked(r);
                    removedFromHistory = true;
                    skipDestroy = true;
                }
            }
            r.app = null;
            r.nowVisible = false;
            if (r.finishing && !skipDestroy) {
                r.state = ActivityState.DESTROYING;
                Message msg = mHandler.obtainMessage(DESTROY_TIMEOUT_MSG);
                msg.obj = r;
                mHandler.sendMessageDelayed(msg, DESTROY_TIMEOUT);
            } else {
                r.state = ActivityState.DESTROYED;
            }
        } else {
            if (r.finishing) {
                removeActivityFromHistoryLocked(r);
                removedFromHistory = true;
            } else {
                r.state = ActivityState.DESTROYED;
            }
        }
        r.configChangeFlags = 0;
        if (!mLRUActivities.remove(r) && hadApp) {
            Slog.w(TAG, "Activity " + r + " being finished, but not in LRU list");
        }
        return removedFromHistory;
    }
    private static void removeHistoryRecordsForAppLocked(ArrayList list, ProcessRecord app) {
        int i = list.size();
        if (localLOGV) Slog.v(
            TAG, "Removing app " + app + " from list " + list
            + " with " + i + " entries");
        while (i > 0) {
            i--;
            HistoryRecord r = (HistoryRecord)list.get(i);
            if (localLOGV) Slog.v(
                TAG, "Record #" + i + " " + r + ": app=" + r.app);
            if (r.app == app) {
                if (localLOGV) Slog.v(TAG, "Removing this entry!");
                list.remove(i);
            }
        }
    }
    private final void handleAppDiedLocked(ProcessRecord app,
            boolean restarting) {
        cleanUpApplicationRecordLocked(app, restarting, -1);
        if (!restarting) {
            mLruProcesses.remove(app);
        }
        if (mPausingActivity != null && mPausingActivity.app == app) {
            if (DEBUG_PAUSE) Slog.v(TAG, "App died while pausing: " + mPausingActivity);
            mPausingActivity = null;
        }
        if (mLastPausedActivity != null && mLastPausedActivity.app == app) {
            mLastPausedActivity = null;
        }
        removeHistoryRecordsForAppLocked(mLRUActivities, app);
        removeHistoryRecordsForAppLocked(mStoppingActivities, app);
        removeHistoryRecordsForAppLocked(mWaitingVisibleActivities, app);
        removeHistoryRecordsForAppLocked(mFinishingActivities, app);
        boolean atTop = true;
        boolean hasVisibleActivities = false;
        int i = mHistory.size();
        if (localLOGV) Slog.v(
            TAG, "Removing app " + app + " from history with " + i + " entries");
        while (i > 0) {
            i--;
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (localLOGV) Slog.v(
                TAG, "Record #" + i + " " + r + ": app=" + r.app);
            if (r.app == app) {
                if ((!r.haveState && !r.stateNotNeeded) || r.finishing) {
                    if (localLOGV) Slog.v(
                        TAG, "Removing this entry!  frozen=" + r.haveState
                        + " finishing=" + r.finishing);
                    mHistory.remove(i);
                    r.inHistory = false;
                    mWindowManager.removeAppToken(r);
                    if (VALIDATE_TOKENS) {
                        mWindowManager.validateAppTokens(mHistory);
                    }
                    removeActivityUriPermissionsLocked(r);
                } else {
                    if (localLOGV) Slog.v(
                        TAG, "Keeping entry, setting app to null");
                    if (r.visible) {
                        hasVisibleActivities = true;
                    }
                    r.app = null;
                    r.nowVisible = false;
                    if (!r.haveState) {
                        r.icicle = null;
                    }
                }
                cleanUpActivityLocked(r, true);
                r.state = ActivityState.STOPPED;
            }
            atTop = false;
        }
        app.activities.clear();
        if (app.instrumentationClass != null) {
            Slog.w(TAG, "Crash of app " + app.processName
                  + " running instrumentation " + app.instrumentationClass);
            Bundle info = new Bundle();
            info.putString("shortMsg", "Process crashed.");
            finishInstrumentationLocked(app, Activity.RESULT_CANCELED, info);
        }
        if (!restarting) {
            if (!resumeTopActivityLocked(null)) {
                if (hasVisibleActivities) {
                    ensureActivitiesVisibleLocked(null, 0);
                }
            }
        }
    }
    private final int getLRURecordIndexForAppLocked(IApplicationThread thread) {
        IBinder threadBinder = thread.asBinder();
        for (int i=mLruProcesses.size()-1; i>=0; i--) {
            ProcessRecord rec = mLruProcesses.get(i);
            if (rec.thread != null && rec.thread.asBinder() == threadBinder) {
                return i;
            }
        }
        return -1;
    }
    private final ProcessRecord getRecordForAppLocked(
            IApplicationThread thread) {
        if (thread == null) {
            return null;
        }
        int appIndex = getLRURecordIndexForAppLocked(thread);
        return appIndex >= 0 ? mLruProcesses.get(appIndex) : null;
    }
    private final void appDiedLocked(ProcessRecord app, int pid,
            IApplicationThread thread) {
        mProcDeaths[0]++;
        if (app.pid == pid && app.thread != null &&
                app.thread.asBinder() == thread.asBinder()) {
            if (!app.killedBackground) {
                Slog.i(TAG, "Process " + app.processName + " (pid " + pid
                        + ") has died.");
            }
            EventLog.writeEvent(EventLogTags.AM_PROC_DIED, app.pid, app.processName);
            if (localLOGV) Slog.v(
                TAG, "Dying app: " + app + ", pid: " + pid
                + ", thread: " + thread.asBinder());
            boolean doLowMem = app.instrumentationClass == null;
            handleAppDiedLocked(app, false);
            if (doLowMem) {
                boolean haveBg = false;
                for (int i=mLruProcesses.size()-1; i>=0; i--) {
                    ProcessRecord rec = mLruProcesses.get(i);
                    if (rec.thread != null && rec.setAdj >= HIDDEN_APP_MIN_ADJ) {
                        haveBg = true;
                        break;
                    }
                }
                if (!haveBg) {
                    Slog.i(TAG, "Low Memory: No more background processes.");
                    EventLog.writeEvent(EventLogTags.AM_LOW_MEMORY, mLruProcesses.size());
                    long now = SystemClock.uptimeMillis();
                    for (int i=mLruProcesses.size()-1; i>=0; i--) {
                        ProcessRecord rec = mLruProcesses.get(i);
                        if (rec != app && rec.thread != null &&
                                (rec.lastLowMemory+GC_MIN_INTERVAL) <= now) {
                            if (rec.setAdj <= VISIBLE_APP_ADJ) {
                                rec.lastRequestedGc = 0;
                            } else {
                                rec.lastRequestedGc = rec.lastLowMemory;
                            }
                            rec.reportLowMemory = true;
                            rec.lastLowMemory = now;
                            mProcessesToGc.remove(rec);
                            addProcessToGcListLocked(rec);
                        }
                    }
                    scheduleAppGcsLocked();
                }
            }
        } else if (app.pid != pid) {
            Slog.i(TAG, "Process " + app.processName + " (pid " + pid
                    + ") has died and restarted (pid " + app.pid + ").");
            EventLog.writeEvent(EventLogTags.AM_PROC_DIED, app.pid, app.processName);
        } else if (DEBUG_PROCESSES) {
            Slog.d(TAG, "Received spurious death notification for thread "
                    + thread.asBinder());
        }
    }
    public static File dumpStackTraces(boolean clearTraces, ArrayList<Integer> pids) {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        File tracesFile = new File(tracesPath);
        try {
            File tracesDir = tracesFile.getParentFile();
            if (!tracesDir.exists()) tracesFile.mkdirs();
            FileUtils.setPermissions(tracesDir.getPath(), 0775, -1, -1);  
            if (clearTraces && tracesFile.exists()) tracesFile.delete();
            tracesFile.createNewFile();
            FileUtils.setPermissions(tracesFile.getPath(), 0666, -1, -1); 
        } catch (IOException e) {
            Slog.w(TAG, "Unable to prepare ANR traces file: " + tracesPath, e);
            return null;
        }
        FileObserver observer = new FileObserver(tracesPath, FileObserver.CLOSE_WRITE) {
            public synchronized void onEvent(int event, String path) { notify(); }
        };
        try {
            observer.startWatching();
            int num = pids.size();
            for (int i = 0; i < num; i++) {
                synchronized (observer) {
                    Process.sendSignal(pids.get(i), Process.SIGNAL_QUIT);
                    observer.wait(200);  
                }
            }
        } catch (InterruptedException e) {
            Log.wtf(TAG, e);
        } finally {
            observer.stopWatching();
        }
        return tracesFile;
    }
    final void appNotResponding(ProcessRecord app, HistoryRecord activity,
            HistoryRecord parent, final String annotation) {
        ArrayList<Integer> pids = new ArrayList<Integer>(20);
        synchronized (this) {
            if (mShuttingDown) {
                Slog.i(TAG, "During shutdown skipping ANR: " + app + " " + annotation);
                return;
            } else if (app.notResponding) {
                Slog.i(TAG, "Skipping duplicate ANR: " + app + " " + annotation);
                return;
            } else if (app.crashing) {
                Slog.i(TAG, "Crashing app skipping ANR: " + app + " " + annotation);
                return;
            }
            app.notResponding = true;
            EventLog.writeEvent(EventLogTags.AM_ANR, app.pid, app.processName, app.info.flags,
                    annotation);
            pids.add(app.pid);
            int parentPid = app.pid;
            if (parent != null && parent.app != null && parent.app.pid > 0) parentPid = parent.app.pid;
            if (parentPid != app.pid) pids.add(parentPid);
            if (MY_PID != app.pid && MY_PID != parentPid) pids.add(MY_PID);
            for (int i = mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord r = mLruProcesses.get(i);
                if (r != null && r.thread != null) {
                    int pid = r.pid;
                    if (pid > 0 && pid != app.pid && pid != parentPid && pid != MY_PID) pids.add(pid);
                }
            }
        }
        File tracesFile = dumpStackTraces(true, pids);
        StringBuilder info = mStringBuilder;
        info.setLength(0);
        info.append("ANR in ").append(app.processName);
        if (activity != null && activity.shortComponentName != null) {
            info.append(" (").append(activity.shortComponentName).append(")");
        }
        info.append("\n");
        if (annotation != null) {
            info.append("Reason: ").append(annotation).append("\n");
        }
        if (parent != null && parent != activity) {
            info.append("Parent: ").append(parent.shortComponentName).append("\n");
        }
        String cpuInfo = null;
        if (MONITOR_CPU_USAGE) {
            updateCpuStatsNow();
            synchronized (mProcessStatsThread) {
                cpuInfo = mProcessStats.printCurrentState();
            }
            info.append(cpuInfo);
        }
        Slog.e(TAG, info.toString());
        if (tracesFile == null) {
            Process.sendSignal(app.pid, Process.SIGNAL_QUIT);
        }
        addErrorToDropBox("anr", app, activity, parent, annotation, cpuInfo, tracesFile, null);
        if (mController != null) {
            try {
                int res = mController.appNotResponding(app.processName, app.pid, info.toString());
                if (res != 0) {
                    if (res < 0 && app.pid != MY_PID) Process.killProcess(app.pid);
                    return;
                }
            } catch (RemoteException e) {
                mController = null;
            }
        }
        boolean showBackground = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.ANR_SHOW_BACKGROUND, 0) != 0;
        synchronized (this) {
            if (!showBackground && !app.isInterestingToUserLocked() && app.pid != MY_PID) {
                Process.killProcess(app.pid);
                return;
            }
            makeAppNotRespondingLocked(app,
                    activity != null ? activity.shortComponentName : null,
                    annotation != null ? "ANR " + annotation : "ANR",
                    info.toString());
            Message msg = Message.obtain();
            HashMap map = new HashMap();
            msg.what = SHOW_NOT_RESPONDING_MSG;
            msg.obj = map;
            map.put("app", app);
            if (activity != null) {
                map.put("activity", activity);
            }
            mHandler.sendMessage(msg);
        }
    }
    private final void decPersistentCountLocked(ProcessRecord app)
    {
        app.persistentActivities--;
        if (app.persistentActivities > 0) {
            return;
        }
        if (app.persistent) {
            return;
        }
        updateOomAdjLocked();
    }
    public void setPersistent(IBinder token, boolean isPersistent) {
        if (checkCallingPermission(android.Manifest.permission.PERSISTENT_ACTIVITY)
                != PackageManager.PERMISSION_GRANTED) {
            String msg = "Permission Denial: setPersistent() from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid()
                    + " requires " + android.Manifest.permission.PERSISTENT_ACTIVITY;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        synchronized(this) {
            int index = indexOfTokenLocked(token);
            if (index < 0) {
                return;
            }
            HistoryRecord r = (HistoryRecord)mHistory.get(index);
            ProcessRecord app = r.app;
            if (localLOGV) Slog.v(
                TAG, "Setting persistence " + isPersistent + ": " + r);
            if (isPersistent) {
                if (r.persistent) {
                    if (localLOGV) Slog.v(TAG, "Already persistent!");
                    return;
                }
                r.persistent = true;
                app.persistentActivities++;
                if (localLOGV) Slog.v(TAG, "Num persistent now: " + app.persistentActivities);
                if (app.persistentActivities > 1) {
                    if (localLOGV) Slog.v(TAG, "Not the first!");
                    return;
                }
                if (app.persistent) {
                    if (localLOGV) Slog.v(TAG, "App is persistent!");
                    return;
                }
                final long origId = Binder.clearCallingIdentity();
                updateOomAdjLocked();
                Binder.restoreCallingIdentity(origId);
            } else {
                if (!r.persistent) {
                    return;
                }
                r.persistent = false;
                final long origId = Binder.clearCallingIdentity();
                decPersistentCountLocked(app);
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    public boolean clearApplicationUserData(final String packageName,
            final IPackageDataObserver observer) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        long callingId = Binder.clearCallingIdentity();
        try {
            IPackageManager pm = ActivityThread.getPackageManager();
            int pkgUid = -1;
            synchronized(this) {
                try {
                    pkgUid = pm.getPackageUid(packageName);
                } catch (RemoteException e) {
                }
                if (pkgUid == -1) {
                    Slog.w(TAG, "Invalid packageName:" + packageName);
                    return false;
                }
                if (uid == pkgUid || checkComponentPermission(
                        android.Manifest.permission.CLEAR_APP_USER_DATA,
                        pid, uid, -1)
                        == PackageManager.PERMISSION_GRANTED) {
                    forceStopPackageLocked(packageName, pkgUid);
                } else {
                    throw new SecurityException(pid+" does not have permission:"+
                            android.Manifest.permission.CLEAR_APP_USER_DATA+" to clear data" +
                                    "for process:"+packageName);
                }
            }
            try {
                pm.clearApplicationUserData(packageName, observer);
                Intent intent = new Intent(Intent.ACTION_PACKAGE_DATA_CLEARED,
                        Uri.fromParts("package", packageName, null));
                intent.putExtra(Intent.EXTRA_UID, pkgUid);
                synchronized (this) {
                    broadcastIntentLocked(null, null, intent,
                            null, null, 0, null, null, null,
                            false, false, MY_PID, Process.SYSTEM_UID);
                }
            } catch (RemoteException e) {
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
        return true;
    }
    public void killBackgroundProcesses(final String packageName) {
        if (checkCallingPermission(android.Manifest.permission.KILL_BACKGROUND_PROCESSES)
                != PackageManager.PERMISSION_GRANTED &&
                checkCallingPermission(android.Manifest.permission.RESTART_PACKAGES)
                        != PackageManager.PERMISSION_GRANTED) {
            String msg = "Permission Denial: killBackgroundProcesses() from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid()
                    + " requires " + android.Manifest.permission.KILL_BACKGROUND_PROCESSES;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            IPackageManager pm = ActivityThread.getPackageManager();
            int pkgUid = -1;
            synchronized(this) {
                try {
                    pkgUid = pm.getPackageUid(packageName);
                } catch (RemoteException e) {
                }
                if (pkgUid == -1) {
                    Slog.w(TAG, "Invalid packageName: " + packageName);
                    return;
                }
                killPackageProcessesLocked(packageName, pkgUid,
                        SECONDARY_SERVER_ADJ, false, true);
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }
    public void forceStopPackage(final String packageName) {
        if (checkCallingPermission(android.Manifest.permission.FORCE_STOP_PACKAGES)
                != PackageManager.PERMISSION_GRANTED) {
            String msg = "Permission Denial: forceStopPackage() from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid()
                    + " requires " + android.Manifest.permission.FORCE_STOP_PACKAGES;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            IPackageManager pm = ActivityThread.getPackageManager();
            int pkgUid = -1;
            synchronized(this) {
                try {
                    pkgUid = pm.getPackageUid(packageName);
                } catch (RemoteException e) {
                }
                if (pkgUid == -1) {
                    Slog.w(TAG, "Invalid packageName: " + packageName);
                    return;
                }
                forceStopPackageLocked(packageName, pkgUid);
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }
    public void killApplicationWithUid(String pkg, int uid) {
        if (pkg == null) {
            return;
        }
        if (uid < 0) {
            Slog.w(TAG, "Invalid uid specified for pkg : " + pkg);
            return;
        }
        int callerUid = Binder.getCallingUid();
        if (callerUid == Process.SYSTEM_UID) {
            Message msg = mHandler.obtainMessage(KILL_APPLICATION_MSG);
            msg.arg1 = uid;
            msg.arg2 = 0;
            msg.obj = pkg;
            mHandler.sendMessage(msg);
        } else {
            throw new SecurityException(callerUid + " cannot kill pkg: " +
                    pkg);
        }
    }
    public void closeSystemDialogs(String reason) {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        if (reason != null) {
            intent.putExtra("reason", reason);
        }
        final int uid = Binder.getCallingUid();
        final long origId = Binder.clearCallingIdentity();
        synchronized (this) {
            int i = mWatchers.beginBroadcast();
            while (i > 0) {
                i--;
                IActivityWatcher w = mWatchers.getBroadcastItem(i);
                if (w != null) {
                    try {
                        w.closingSystemDialogs(reason);
                    } catch (RemoteException e) {
                    }
                }
            }
            mWatchers.finishBroadcast();
            mWindowManager.closeSystemDialogs(reason);
            for (i=mHistory.size()-1; i>=0; i--) {
                HistoryRecord r = (HistoryRecord)mHistory.get(i);
                if ((r.info.flags&ActivityInfo.FLAG_FINISH_ON_CLOSE_SYSTEM_DIALOGS) != 0) {
                    finishActivityLocked(r, i,
                            Activity.RESULT_CANCELED, null, "close-sys");
                }
            }
            broadcastIntentLocked(null, null, intent, null,
                    null, 0, null, null, null, false, false, -1, uid);
        }
        Binder.restoreCallingIdentity(origId);
    }
    public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids)
            throws RemoteException {
        Debug.MemoryInfo[] infos = new Debug.MemoryInfo[pids.length];
        for (int i=pids.length-1; i>=0; i--) {
            infos[i] = new Debug.MemoryInfo();
            Debug.getMemoryInfo(pids[i], infos[i]);
        }
        return infos;
    }
    public void killApplicationProcess(String processName, int uid) {
        if (processName == null) {
            return;
        }
        int callerUid = Binder.getCallingUid();
        if (callerUid == Process.SYSTEM_UID) {
            synchronized (this) {
                ProcessRecord app = getProcessRecordLocked(processName, uid);
                if (app != null) {
                    try {
                        app.thread.scheduleSuicide();
                    } catch (RemoteException e) {
                    }
                } else {
                    Slog.w(TAG, "Process/uid not found attempting kill of "
                            + processName + " / " + uid);
                }
            }
        } else {
            throw new SecurityException(callerUid + " cannot kill app process: " +
                    processName);
        }
    }
    private void forceStopPackageLocked(final String packageName, int uid) {
        forceStopPackageLocked(packageName, uid, false, false, true);
        Intent intent = new Intent(Intent.ACTION_PACKAGE_RESTARTED,
                Uri.fromParts("package", packageName, null));
        intent.putExtra(Intent.EXTRA_UID, uid);
        broadcastIntentLocked(null, null, intent,
                null, null, 0, null, null, null,
                false, false, MY_PID, Process.SYSTEM_UID);
    }
    private final boolean killPackageProcessesLocked(String packageName, int uid,
            int minOomAdj, boolean callerWillRestart, boolean doit) {
        ArrayList<ProcessRecord> procs = new ArrayList<ProcessRecord>();
        final String procNamePrefix = packageName + ":";
        for (SparseArray<ProcessRecord> apps : mProcessNames.getMap().values()) {
            final int NA = apps.size();
            for (int ia=0; ia<NA; ia++) {
                ProcessRecord app = apps.valueAt(ia);
                if (app.removed) {
                    if (doit) {
                        procs.add(app);
                    }
                } else if ((uid > 0 && uid != Process.SYSTEM_UID && app.info.uid == uid)
                        || app.processName.equals(packageName)
                        || app.processName.startsWith(procNamePrefix)) {
                    if (app.setAdj >= minOomAdj) {
                        if (!doit) {
                            return true;
                        }
                        app.removed = true;
                        procs.add(app);
                    }
                }
            }
        }
        int N = procs.size();
        for (int i=0; i<N; i++) {
            removeProcessLocked(procs.get(i), callerWillRestart);
        }
        return N > 0;
    }
    private final boolean forceStopPackageLocked(String name, int uid,
            boolean callerWillRestart, boolean purgeCache, boolean doit) {
        int i, N;
        if (uid < 0) {
            try {
                uid = ActivityThread.getPackageManager().getPackageUid(name);
            } catch (RemoteException e) {
            }
        }
        if (doit) {
            Slog.i(TAG, "Force stopping package " + name + " uid=" + uid);
            Iterator<SparseArray<Long>> badApps = mProcessCrashTimes.getMap().values().iterator();
            while (badApps.hasNext()) {
                SparseArray<Long> ba = badApps.next();
                if (ba.get(uid) != null) {
                    badApps.remove();
                }
            }
        }
        boolean didSomething = killPackageProcessesLocked(name, uid, -100,
                callerWillRestart, doit);
        for (i=mHistory.size()-1; i>=0; i--) {
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (r.packageName.equals(name)) {
                if (!doit) {
                    return true;
                }
                didSomething = true;
                Slog.i(TAG, "  Force finishing activity " + r);
                if (r.app != null) {
                    r.app.removed = true;
                }
                r.app = null;
                finishActivityLocked(r, i, Activity.RESULT_CANCELED, null, "uninstall");
            }
        }
        ArrayList<ServiceRecord> services = new ArrayList<ServiceRecord>();
        for (ServiceRecord service : mServices.values()) {
            if (service.packageName.equals(name)) {
                if (!doit) {
                    return true;
                }
                didSomething = true;
                Slog.i(TAG, "  Force stopping service " + service);
                if (service.app != null) {
                    service.app.removed = true;
                }
                service.app = null;
                services.add(service);
            }
        }
        N = services.size();
        for (i=0; i<N; i++) {
            bringDownServiceLocked(services.get(i), true);
        }
        if (doit) {
            if (purgeCache) {
                AttributeCache ac = AttributeCache.instance();
                if (ac != null) {
                    ac.removePackage(name);
                }
            }
            resumeTopActivityLocked(null);
        }
        return didSomething;
    }
    private final boolean removeProcessLocked(ProcessRecord app, boolean callerWillRestart) {
        final String name = app.processName;
        final int uid = app.info.uid;
        if (DEBUG_PROCESSES) Slog.d(
            TAG, "Force removing process " + app + " (" + name
            + "/" + uid + ")");
        mProcessNames.remove(name, uid);
        boolean needRestart = false;
        if (app.pid > 0 && app.pid != MY_PID) {
            int pid = app.pid;
            synchronized (mPidsSelfLocked) {
                mPidsSelfLocked.remove(pid);
                mHandler.removeMessages(PROC_START_TIMEOUT_MSG, app);
            }
            handleAppDiedLocked(app, true);
            mLruProcesses.remove(app);
            Process.killProcess(pid);
            if (app.persistent) {
                if (!callerWillRestart) {
                    addAppLocked(app.info);
                } else {
                    needRestart = true;
                }
            }
        } else {
            mRemovedProcesses.add(app);
        }
        return needRestart;
    }
    private final void processStartTimedOutLocked(ProcessRecord app) {
        final int pid = app.pid;
        boolean gone = false;
        synchronized (mPidsSelfLocked) {
            ProcessRecord knownApp = mPidsSelfLocked.get(pid);
            if (knownApp != null && knownApp.thread == null) {
                mPidsSelfLocked.remove(pid);
                gone = true;
            }        
        }
        if (gone) {
            Slog.w(TAG, "Process " + app + " failed to attach");
            EventLog.writeEvent(EventLogTags.AM_PROCESS_START_TIMEOUT, pid, app.info.uid,
                    app.processName);
            mProcessNames.remove(app.processName, app.info.uid);
            checkAppInLaunchingProvidersLocked(app, true);
            for (int i=0; i<mPendingServices.size(); i++) {
                ServiceRecord sr = mPendingServices.get(i);
                if (app.info.uid == sr.appInfo.uid
                        && app.processName.equals(sr.processName)) {
                    Slog.w(TAG, "Forcing bringing down service: " + sr);
                    mPendingServices.remove(i);
                    i--;
                    bringDownServiceLocked(sr, true);
                }
            }
            Process.killProcess(pid);
            if (mBackupTarget != null && mBackupTarget.app.pid == pid) {
                Slog.w(TAG, "Unattached app died before backup, skipping");
                try {
                    IBackupManager bm = IBackupManager.Stub.asInterface(
                            ServiceManager.getService(Context.BACKUP_SERVICE));
                    bm.agentDisconnected(app.info.packageName);
                } catch (RemoteException e) {
                }
            }
            if (mPendingBroadcast != null && mPendingBroadcast.curApp.pid == pid) {
                Slog.w(TAG, "Unattached app died before broadcast acknowledged, skipping");
                mPendingBroadcast = null;
                scheduleBroadcastsLocked();
            }
        } else {
            Slog.w(TAG, "Spurious process start timeout - pid not known for " + app);
        }
    }
    private final boolean attachApplicationLocked(IApplicationThread thread,
            int pid) {
        ProcessRecord app;
        if (pid != MY_PID && pid >= 0) {
            synchronized (mPidsSelfLocked) {
                app = mPidsSelfLocked.get(pid);
            }
        } else if (mStartingProcesses.size() > 0) {
            app = mStartingProcesses.remove(0);
            app.setPid(pid);
        } else {
            app = null;
        }
        if (app == null) {
            Slog.w(TAG, "No pending application record for pid " + pid
                    + " (IApplicationThread " + thread + "); dropping process");
            EventLog.writeEvent(EventLogTags.AM_DROP_PROCESS, pid);
            if (pid > 0 && pid != MY_PID) {
                Process.killProcess(pid);
            } else {
                try {
                    thread.scheduleExit();
                } catch (Exception e) {
                }
            }
            return false;
        }
        if (app.thread != null) {
            handleAppDiedLocked(app, true);
        }
        if (localLOGV) Slog.v(
                TAG, "Binding process pid " + pid + " to record " + app);
        String processName = app.processName;
        try {
            thread.asBinder().linkToDeath(new AppDeathRecipient(
                    app, pid, thread), 0);
        } catch (RemoteException e) {
            app.resetPackageList();
            startProcessLocked(app, "link fail", processName);
            return false;
        }
        EventLog.writeEvent(EventLogTags.AM_PROC_BOUND, app.pid, app.processName);
        app.thread = thread;
        app.curAdj = app.setAdj = -100;
        app.curSchedGroup = Process.THREAD_GROUP_DEFAULT;
        app.setSchedGroup = Process.THREAD_GROUP_BG_NONINTERACTIVE;
        app.forcingToForeground = null;
        app.foregroundServices = false;
        app.debugging = false;
        mHandler.removeMessages(PROC_START_TIMEOUT_MSG, app);
        boolean normalMode = mSystemReady || isAllowedWhileBooting(app.info);
        List providers = normalMode ? generateApplicationProvidersLocked(app) : null;
        if (!normalMode) {
            Slog.i(TAG, "Launching preboot mode app: " + app);
        }
        if (localLOGV) Slog.v(
            TAG, "New app record " + app
            + " thread=" + thread.asBinder() + " pid=" + pid);
        try {
            int testMode = IApplicationThread.DEBUG_OFF;
            if (mDebugApp != null && mDebugApp.equals(processName)) {
                testMode = mWaitForDebugger
                    ? IApplicationThread.DEBUG_WAIT
                    : IApplicationThread.DEBUG_ON;
                app.debugging = true;
                if (mDebugTransient) {
                    mDebugApp = mOrigDebugApp;
                    mWaitForDebugger = mOrigWaitForDebugger;
                }
            }
            boolean isRestrictedBackupMode = false;
            if (mBackupTarget != null && mBackupAppName.equals(processName)) {
                isRestrictedBackupMode = (mBackupTarget.backupMode == BackupRecord.RESTORE)
                        || (mBackupTarget.backupMode == BackupRecord.BACKUP_FULL);
            }
            ensurePackageDexOpt(app.instrumentationInfo != null
                    ? app.instrumentationInfo.packageName
                    : app.info.packageName);
            if (app.instrumentationClass != null) {
                ensurePackageDexOpt(app.instrumentationClass.getPackageName());
            }
            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Binding proc "
                    + processName + " with config " + mConfiguration);
            thread.bindApplication(processName, app.instrumentationInfo != null
                    ? app.instrumentationInfo : app.info, providers,
                    app.instrumentationClass, app.instrumentationProfileFile,
                    app.instrumentationArguments, app.instrumentationWatcher, testMode, 
                    isRestrictedBackupMode || !normalMode,
                    mConfiguration, getCommonServicesLocked());
            updateLruProcessLocked(app, false, true);
            app.lastRequestedGc = app.lastLowMemory = SystemClock.uptimeMillis();
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown during bind!", e);
            app.resetPackageList();
            startProcessLocked(app, "bind fail", processName);
            return false;
        }
        mPersistentStartingProcesses.remove(app);
        mProcessesOnHold.remove(app);
        boolean badApp = false;
        boolean didSomething = false;
        HistoryRecord hr = topRunningActivityLocked(null);
        if (hr != null && normalMode) {
            if (hr.app == null && app.info.uid == hr.info.applicationInfo.uid
                    && processName.equals(hr.processName)) {
                try {
                    if (realStartActivityLocked(hr, app, true, true)) {
                        didSomething = true;
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Exception in new application when starting activity "
                          + hr.intent.getComponent().flattenToShortString(), e);
                    badApp = true;
                }
            } else {
                ensureActivitiesVisibleLocked(hr, null, processName, 0);
            }
        }
        if (!badApp && mPendingServices.size() > 0) {
            ServiceRecord sr = null;
            try {
                for (int i=0; i<mPendingServices.size(); i++) {
                    sr = mPendingServices.get(i);
                    if (app.info.uid != sr.appInfo.uid
                            || !processName.equals(sr.processName)) {
                        continue;
                    }
                    mPendingServices.remove(i);
                    i--;
                    realStartServiceLocked(sr, app);
                    didSomething = true;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception in new application when starting service "
                      + sr.shortName, e);
                badApp = true;
            }
        }
        BroadcastRecord br = mPendingBroadcast;
        if (!badApp && br != null && br.curApp == app) {
            try {
                mPendingBroadcast = null;
                processCurBroadcastLocked(br, app);
                didSomething = true;
            } catch (Exception e) {
                Slog.w(TAG, "Exception in new application when starting receiver "
                      + br.curComponent.flattenToShortString(), e);
                badApp = true;
                logBroadcastReceiverDiscard(br);
                finishReceiverLocked(br.receiver, br.resultCode, br.resultData,
                        br.resultExtras, br.resultAbort, true);
                scheduleBroadcastsLocked();
                br.state = BroadcastRecord.IDLE;
            }
        }
        if (!badApp && mBackupTarget != null && mBackupTarget.appInfo.uid == app.info.uid) {
            if (DEBUG_BACKUP) Slog.v(TAG, "New app is backup target, launching agent for " + app);
            ensurePackageDexOpt(mBackupTarget.appInfo.packageName);
            try {
                thread.scheduleCreateBackupAgent(mBackupTarget.appInfo, mBackupTarget.backupMode);
            } catch (Exception e) {
                Slog.w(TAG, "Exception scheduling backup agent creation: ");
                e.printStackTrace();
            }
        }
        if (badApp) {
            handleAppDiedLocked(app, false);
            return false;
        }
        if (!didSomething) {
            updateOomAdjLocked();
        }
        return true;
    }
    public final void attachApplication(IApplicationThread thread) {
        synchronized (this) {
            int callingPid = Binder.getCallingPid();
            final long origId = Binder.clearCallingIdentity();
            attachApplicationLocked(thread, callingPid);
            Binder.restoreCallingIdentity(origId);
        }
    }
    public final void activityIdle(IBinder token, Configuration config) {
        final long origId = Binder.clearCallingIdentity();
        activityIdleInternal(token, false, config);
        Binder.restoreCallingIdentity(origId);
    }
    final ArrayList<HistoryRecord> processStoppingActivitiesLocked(
            boolean remove) {
        int N = mStoppingActivities.size();
        if (N <= 0) return null;
        ArrayList<HistoryRecord> stops = null;
        final boolean nowVisible = mResumedActivity != null
                && mResumedActivity.nowVisible
                && !mResumedActivity.waitingVisible;
        for (int i=0; i<N; i++) {
            HistoryRecord s = mStoppingActivities.get(i);
            if (localLOGV) Slog.v(TAG, "Stopping " + s + ": nowVisible="
                    + nowVisible + " waitingVisible=" + s.waitingVisible
                    + " finishing=" + s.finishing);
            if (s.waitingVisible && nowVisible) {
                mWaitingVisibleActivities.remove(s);
                s.waitingVisible = false;
                if (s.finishing) {
                    if (localLOGV) Slog.v(TAG, "Before stopping, can hide: " + s);
                    mWindowManager.setAppVisibility(s, false);
                }
            }
            if (!s.waitingVisible && remove) {
                if (localLOGV) Slog.v(TAG, "Ready to stop: " + s);
                if (stops == null) {
                    stops = new ArrayList<HistoryRecord>();
                }
                stops.add(s);
                mStoppingActivities.remove(i);
                N--;
                i--;
            }
        }
        return stops;
    }
    void enableScreenAfterBoot() {
        EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_ENABLE_SCREEN,
                SystemClock.uptimeMillis());
        mWindowManager.enableScreenAfterBoot();
    }
    final void activityIdleInternal(IBinder token, boolean fromTimeout,
            Configuration config) {
        if (localLOGV) Slog.v(TAG, "Activity idle: " + token);
        ArrayList<HistoryRecord> stops = null;
        ArrayList<HistoryRecord> finishes = null;
        ArrayList<HistoryRecord> thumbnails = null;
        int NS = 0;
        int NF = 0;
        int NT = 0;
        IApplicationThread sendThumbnail = null;
        boolean booting = false;
        boolean enableScreen = false;
        synchronized (this) {
            if (token != null) {
                mHandler.removeMessages(IDLE_TIMEOUT_MSG, token);
            }
            int index = indexOfTokenLocked(token);
            if (index >= 0) {
                HistoryRecord r = (HistoryRecord)mHistory.get(index);
                if (fromTimeout) {
                    reportActivityLaunchedLocked(fromTimeout, r, -1, -1);
                }
                if (config != null) {
                    r.configuration = config;
                }
                if (mResumedActivity == r && mLaunchingActivity.isHeld()) {
                    mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
                    mLaunchingActivity.release();
                }
                r.idle = true;
                scheduleAppGcsLocked();
                if (r.thumbnailNeeded && r.app != null && r.app.thread != null) {
                    sendThumbnail = r.app.thread;
                    r.thumbnailNeeded = false;
                }
                if (DEBUG_VISBILITY) Slog.v(TAG, "Idle activity for " + r);
                ensureActivitiesVisibleLocked(null, 0);
                if (!mBooted && !fromTimeout) {
                    mBooted = true;
                    enableScreen = true;
                }
            } else if (fromTimeout) {
                reportActivityLaunchedLocked(fromTimeout, null, -1, -1);
            }
            stops = processStoppingActivitiesLocked(true);
            NS = stops != null ? stops.size() : 0;
            if ((NF=mFinishingActivities.size()) > 0) {
                finishes = new ArrayList<HistoryRecord>(mFinishingActivities);
                mFinishingActivities.clear();
            }
            if ((NT=mCancelledThumbnails.size()) > 0) {
                thumbnails = new ArrayList<HistoryRecord>(mCancelledThumbnails);
                mCancelledThumbnails.clear();
            }
            booting = mBooting;
            mBooting = false;
        }
        int i;
        if (sendThumbnail != null) {
            try {
                sendThumbnail.requestThumbnail(token);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown when requesting thumbnail", e);
                sendPendingThumbnail(null, token, null, null, true);
            }
        }
        for (i=0; i<NS; i++) {
            HistoryRecord r = (HistoryRecord)stops.get(i);
            synchronized (this) {
                if (r.finishing) {
                    finishCurrentActivityLocked(r, FINISH_IMMEDIATELY);
                } else {
                    stopActivityLocked(r);
                }
            }
        }
        for (i=0; i<NF; i++) {
            HistoryRecord r = (HistoryRecord)finishes.get(i);
            synchronized (this) {
                destroyActivityLocked(r, true);
            }
        }
        for (i=0; i<NT; i++) {
            HistoryRecord r = (HistoryRecord)thumbnails.get(i);
            sendPendingThumbnail(r, null, null, null, true);
        }
        if (booting) {
            finishBooting();
        }
        trimApplications();
        if (enableScreen) {
            enableScreenAfterBoot();
        }
    }
    final void finishBooting() {
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction(Intent.ACTION_QUERY_PACKAGE_RESTART);
        pkgFilter.addDataScheme("package");
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String[] pkgs = intent.getStringArrayExtra(Intent.EXTRA_PACKAGES);
                if (pkgs != null) {
                    for (String pkg : pkgs) {
                        if (forceStopPackageLocked(pkg, -1, false, false, false)) {
                            setResultCode(Activity.RESULT_OK);
                            return;
                        }
                    }
                }
            }
        }, pkgFilter);
        synchronized (this) {
            final int NP = mProcessesOnHold.size();
            if (NP > 0) {
                ArrayList<ProcessRecord> procs =
                    new ArrayList<ProcessRecord>(mProcessesOnHold);
                for (int ip=0; ip<NP; ip++) {
                    this.startProcessLocked(procs.get(ip), "on-hold", null);
                }
            }
            if (mFactoryTest != SystemServer.FACTORY_TEST_LOW_LEVEL) {
                broadcastIntentLocked(null, null,
                        new Intent(Intent.ACTION_BOOT_COMPLETED, null),
                        null, null, 0, null, null,
                        android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        false, false, MY_PID, Process.SYSTEM_UID);
            }
        }
    }
    final void ensureBootCompleted() {
        boolean booting;
        boolean enableScreen;
        synchronized (this) {
            booting = mBooting;
            mBooting = false;
            enableScreen = !mBooted;
            mBooted = true;
        }
        if (booting) {
            finishBooting();
        }
        if (enableScreen) {
            enableScreenAfterBoot();
        }
    }
    public final void activityPaused(IBinder token, Bundle icicle) {
        if (icicle != null && icicle.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Bundle");
        }
        final long origId = Binder.clearCallingIdentity();
        activityPaused(token, icicle, false);
        Binder.restoreCallingIdentity(origId);
    }
    final void activityPaused(IBinder token, Bundle icicle, boolean timeout) {
        if (DEBUG_PAUSE) Slog.v(
            TAG, "Activity paused: token=" + token + ", icicle=" + icicle
            + ", timeout=" + timeout);
        HistoryRecord r = null;
        synchronized (this) {
            int index = indexOfTokenLocked(token);
            if (index >= 0) {
                r = (HistoryRecord)mHistory.get(index);
                if (!timeout) {
                    r.icicle = icicle;
                    r.haveState = true;
                }
                mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
                if (mPausingActivity == r) {
                    r.state = ActivityState.PAUSED;
                    completePauseLocked();
                } else {
                	EventLog.writeEvent(EventLogTags.AM_FAILED_TO_PAUSE,
                	        System.identityHashCode(r), r.shortComponentName, 
                			mPausingActivity != null
                			    ? mPausingActivity.shortComponentName : "(none)");
                }
            }
        }
    }
    public final void activityStopped(IBinder token, Bitmap thumbnail,
            CharSequence description) {
        if (localLOGV) Slog.v(
            TAG, "Activity stopped: token=" + token);
        HistoryRecord r = null;
        final long origId = Binder.clearCallingIdentity();
        synchronized (this) {
            int index = indexOfTokenLocked(token);
            if (index >= 0) {
                r = (HistoryRecord)mHistory.get(index);
                r.thumbnail = thumbnail;
                r.description = description;
                r.stopped = true;
                r.state = ActivityState.STOPPED;
                if (!r.finishing) {
                    if (r.configDestroy) {
                        destroyActivityLocked(r, true);
                        resumeTopActivityLocked(null);
                    }
                }
            }
        }
        if (r != null) {
            sendPendingThumbnail(r, null, null, null, false);
        }
        trimApplications();
        Binder.restoreCallingIdentity(origId);
    }
    public final void activityDestroyed(IBinder token) {
        if (DEBUG_SWITCH) Slog.v(TAG, "ACTIVITY DESTROYED: " + token);
        synchronized (this) {
            mHandler.removeMessages(DESTROY_TIMEOUT_MSG, token);
            int index = indexOfTokenLocked(token);
            if (index >= 0) {
                HistoryRecord r = (HistoryRecord)mHistory.get(index);
                if (r.state == ActivityState.DESTROYING) {
                    final long origId = Binder.clearCallingIdentity();
                    removeActivityFromHistoryLocked(r);
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }
    }
    public String getCallingPackage(IBinder token) {
        synchronized (this) {
            HistoryRecord r = getCallingRecordLocked(token);
            return r != null && r.app != null ? r.info.packageName : null;
        }
    }
    public ComponentName getCallingActivity(IBinder token) {
        synchronized (this) {
            HistoryRecord r = getCallingRecordLocked(token);
            return r != null ? r.intent.getComponent() : null;
        }
    }
    private HistoryRecord getCallingRecordLocked(IBinder token) {
        int index = indexOfTokenLocked(token);
        if (index >= 0) {
            HistoryRecord r = (HistoryRecord)mHistory.get(index);
            if (r != null) {
                return r.resultTo;
            }
        }
        return null;
    }
    public ComponentName getActivityClassForToken(IBinder token) {
        synchronized(this) {
            int index = indexOfTokenLocked(token);
            if (index >= 0) {
                HistoryRecord r = (HistoryRecord)mHistory.get(index);
                return r.intent.getComponent();
            }
            return null;
        }
    }
    public String getPackageForToken(IBinder token) {
        synchronized(this) {
            int index = indexOfTokenLocked(token);
            if (index >= 0) {
                HistoryRecord r = (HistoryRecord)mHistory.get(index);
                return r.packageName;
            }
            return null;
        }
    }
    public IIntentSender getIntentSender(int type,
            String packageName, IBinder token, String resultWho,
            int requestCode, Intent intent, String resolvedType, int flags) {
        if (intent != null && intent.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        if (type == INTENT_SENDER_BROADCAST) {
            if ((intent.getFlags()&Intent.FLAG_RECEIVER_BOOT_UPGRADE) != 0) {
                throw new IllegalArgumentException(
                        "Can't use FLAG_RECEIVER_BOOT_UPGRADE here");
            }
        }
        synchronized(this) {
            int callingUid = Binder.getCallingUid();
            try {
                if (callingUid != 0 && callingUid != Process.SYSTEM_UID &&
                        Process.supportsProcesses()) {
                    int uid = ActivityThread.getPackageManager()
                            .getPackageUid(packageName);
                    if (uid != Binder.getCallingUid()) {
                        String msg = "Permission Denial: getIntentSender() from pid="
                            + Binder.getCallingPid()
                            + ", uid=" + Binder.getCallingUid()
                            + ", (need uid=" + uid + ")"
                            + " is not allowed to send as package " + packageName;
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    }
                }
            } catch (RemoteException e) {
                throw new SecurityException(e);
            }
            HistoryRecord activity = null;
            if (type == INTENT_SENDER_ACTIVITY_RESULT) {
                int index = indexOfTokenLocked(token);
                if (index < 0) {
                    return null;
                }
                activity = (HistoryRecord)mHistory.get(index);
                if (activity.finishing) {
                    return null;
                }
            }
            final boolean noCreate = (flags&PendingIntent.FLAG_NO_CREATE) != 0;
            final boolean cancelCurrent = (flags&PendingIntent.FLAG_CANCEL_CURRENT) != 0;
            final boolean updateCurrent = (flags&PendingIntent.FLAG_UPDATE_CURRENT) != 0;
            flags &= ~(PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_CANCEL_CURRENT
                    |PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntentRecord.Key key = new PendingIntentRecord.Key(
                    type, packageName, activity, resultWho,
                    requestCode, intent, resolvedType, flags);
            WeakReference<PendingIntentRecord> ref;
            ref = mIntentSenderRecords.get(key);
            PendingIntentRecord rec = ref != null ? ref.get() : null;
            if (rec != null) {
                if (!cancelCurrent) {
                    if (updateCurrent) {
                        rec.key.requestIntent.replaceExtras(intent);
                    }
                    return rec;
                }
                rec.canceled = true;
                mIntentSenderRecords.remove(key);
            }
            if (noCreate) {
                return rec;
            }
            rec = new PendingIntentRecord(this, key, callingUid);
            mIntentSenderRecords.put(key, rec.ref);
            if (type == INTENT_SENDER_ACTIVITY_RESULT) {
                if (activity.pendingResults == null) {
                    activity.pendingResults
                            = new HashSet<WeakReference<PendingIntentRecord>>();
                }
                activity.pendingResults.add(rec.ref);
            }
            return rec;
        }
    }
    public void cancelIntentSender(IIntentSender sender) {
        if (!(sender instanceof PendingIntentRecord)) {
            return;
        }
        synchronized(this) {
            PendingIntentRecord rec = (PendingIntentRecord)sender;
            try {
                int uid = ActivityThread.getPackageManager()
                        .getPackageUid(rec.key.packageName);
                if (uid != Binder.getCallingUid()) {
                    String msg = "Permission Denial: cancelIntentSender() from pid="
                        + Binder.getCallingPid()
                        + ", uid=" + Binder.getCallingUid()
                        + " is not allowed to cancel packges "
                        + rec.key.packageName;
                    Slog.w(TAG, msg);
                    throw new SecurityException(msg);
                }
            } catch (RemoteException e) {
                throw new SecurityException(e);
            }
            cancelIntentSenderLocked(rec, true);
        }
    }
    void cancelIntentSenderLocked(PendingIntentRecord rec, boolean cleanActivity) {
        rec.canceled = true;
        mIntentSenderRecords.remove(rec.key);
        if (cleanActivity && rec.key.activity != null) {
            rec.key.activity.pendingResults.remove(rec.ref);
        }
    }
    public String getPackageForIntentSender(IIntentSender pendingResult) {
        if (!(pendingResult instanceof PendingIntentRecord)) {
            return null;
        }
        try {
            PendingIntentRecord res = (PendingIntentRecord)pendingResult;
            return res.key.packageName;
        } catch (ClassCastException e) {
        }
        return null;
    }
    public void setProcessLimit(int max) {
        enforceCallingPermission(android.Manifest.permission.SET_PROCESS_LIMIT,
                "setProcessLimit()");
        mProcessLimit = max;
    }
    public int getProcessLimit() {
        return mProcessLimit;
    }
    void foregroundTokenDied(ForegroundToken token) {
        synchronized (ActivityManagerService.this) {
            synchronized (mPidsSelfLocked) {
                ForegroundToken cur
                    = mForegroundProcesses.get(token.pid);
                if (cur != token) {
                    return;
                }
                mForegroundProcesses.remove(token.pid);
                ProcessRecord pr = mPidsSelfLocked.get(token.pid);
                if (pr == null) {
                    return;
                }
                pr.forcingToForeground = null;
                pr.foregroundServices = false;
            }
            updateOomAdjLocked();
        }
    }
    public void setProcessForeground(IBinder token, int pid, boolean isForeground) {
        enforceCallingPermission(android.Manifest.permission.SET_PROCESS_LIMIT,
                "setProcessForeground()");
        synchronized(this) {
            boolean changed = false;
            synchronized (mPidsSelfLocked) {
                ProcessRecord pr = mPidsSelfLocked.get(pid);
                if (pr == null) {
                    Slog.w(TAG, "setProcessForeground called on unknown pid: " + pid);
                    return;
                }
                ForegroundToken oldToken = mForegroundProcesses.get(pid);
                if (oldToken != null) {
                    oldToken.token.unlinkToDeath(oldToken, 0);
                    mForegroundProcesses.remove(pid);
                    pr.forcingToForeground = null;
                    changed = true;
                }
                if (isForeground && token != null) {
                    ForegroundToken newToken = new ForegroundToken() {
                        public void binderDied() {
                            foregroundTokenDied(this);
                        }
                    };
                    newToken.pid = pid;
                    newToken.token = token;
                    try {
                        token.linkToDeath(newToken, 0);
                        mForegroundProcesses.put(pid, newToken);
                        pr.forcingToForeground = token;
                        changed = true;
                    } catch (RemoteException e) {
                    }
                }
            }
            if (changed) {
                updateOomAdjLocked();
            }
        }
    }
    static class PermissionController extends IPermissionController.Stub {
        ActivityManagerService mActivityManagerService;
        PermissionController(ActivityManagerService activityManagerService) {
            mActivityManagerService = activityManagerService;
        }
        public boolean checkPermission(String permission, int pid, int uid) {
            return mActivityManagerService.checkPermission(permission, pid,
                    uid) == PackageManager.PERMISSION_GRANTED;
        }
    }
    int checkComponentPermission(String permission, int pid, int uid,
            int reqUid) {
        Identity tlsIdentity = sCallerIdentity.get();
        if (tlsIdentity != null) {
            Slog.d(TAG, "checkComponentPermission() adjusting {pid,uid} to {"
                    + tlsIdentity.pid + "," + tlsIdentity.uid + "}");
            uid = tlsIdentity.uid;
            pid = tlsIdentity.pid;
        }
        if (uid == 0 || uid == Process.SYSTEM_UID || pid == MY_PID ||
            !Process.supportsProcesses()) {
            return PackageManager.PERMISSION_GRANTED;
        }
        if (reqUid >= 0 && uid != reqUid) {
            Slog.w(TAG, "Permission denied: checkComponentPermission() reqUid=" + reqUid);
            return PackageManager.PERMISSION_DENIED;
        }
        if (permission == null) {
            return PackageManager.PERMISSION_GRANTED;
        }
        try {
            return ActivityThread.getPackageManager()
                    .checkUidPermission(permission, uid);
        } catch (RemoteException e) {
            Slog.e(TAG, "PackageManager is dead?!?", e);
        }
        return PackageManager.PERMISSION_DENIED;
    }
    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return PackageManager.PERMISSION_DENIED;
        }
        return checkComponentPermission(permission, pid, uid, -1);
    }
    int checkCallingPermission(String permission) {
        return checkPermission(permission,
                Binder.getCallingPid(),
                Binder.getCallingUid());
    }
    void enforceCallingPermission(String permission, String func) {
        if (checkCallingPermission(permission)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + ", uid=" + Binder.getCallingUid()
                + " requires " + permission;
        Slog.w(TAG, msg);
        throw new SecurityException(msg);
    }
    private final boolean checkHoldingPermissionsLocked(IPackageManager pm,
            ProviderInfo pi, int uid, int modeFlags) {
        try {
            if ((modeFlags&Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                if ((pi.readPermission != null) &&
                        (pm.checkUidPermission(pi.readPermission, uid)
                                != PackageManager.PERMISSION_GRANTED)) {
                    return false;
                }
            }
            if ((modeFlags&Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0) {
                if ((pi.writePermission != null) &&
                        (pm.checkUidPermission(pi.writePermission, uid)
                                != PackageManager.PERMISSION_GRANTED)) {
                    return false;
                }
            }
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    private final boolean checkUriPermissionLocked(Uri uri, int uid,
            int modeFlags) {
        if (uid == 0 || !Process.supportsProcesses()) {
            return true;
        }
        HashMap<Uri, UriPermission> perms = mGrantedUriPermissions.get(uid);
        if (perms == null) return false;
        UriPermission perm = perms.get(uri);
        if (perm == null) return false;
        return (modeFlags&perm.modeFlags) == modeFlags;
    }
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        Identity tlsIdentity = sCallerIdentity.get();
        if (tlsIdentity != null) {
            uid = tlsIdentity.uid;
            pid = tlsIdentity.pid;
        }
        if (pid == MY_PID) {
            return PackageManager.PERMISSION_GRANTED;
        }
        synchronized(this) {
            return checkUriPermissionLocked(uri, uid, modeFlags)
                    ? PackageManager.PERMISSION_GRANTED
                    : PackageManager.PERMISSION_DENIED;
        }
    }
    private void grantUriPermissionLocked(int callingUid,
            String targetPkg, Uri uri, int modeFlags, HistoryRecord activity) {
        modeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (modeFlags == 0) {
            return;
        }
        if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                "Requested grant " + targetPkg + " permission to " + uri);
        final IPackageManager pm = ActivityThread.getPackageManager();
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                    "Can't grant URI permission for non-content URI: " + uri);
            return;
        }
        String name = uri.getAuthority();
        ProviderInfo pi = null;
        ContentProviderRecord cpr
                = (ContentProviderRecord)mProvidersByName.get(name);
        if (cpr != null) {
            pi = cpr.info;
        } else {
            try {
                pi = pm.resolveContentProvider(name,
                        PackageManager.GET_URI_PERMISSION_PATTERNS);
            } catch (RemoteException ex) {
            }
        }
        if (pi == null) {
            Slog.w(TAG, "No content provider found for: " + name);
            return;
        }
        int targetUid;
        try {
            targetUid = pm.getPackageUid(targetPkg);
            if (targetUid < 0) {
                if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                        "Can't grant URI permission no uid for: " + targetPkg);
                return;
            }
        } catch (RemoteException ex) {
            return;
        }
        if (checkHoldingPermissionsLocked(pm, pi, targetUid, modeFlags)) {
            if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                    "Target " + targetPkg + " already has full permission to " + uri);
            return;
        }
        if (!pi.grantUriPermissions) {
            throw new SecurityException("Provider " + pi.packageName
                    + "/" + pi.name
                    + " does not allow granting of Uri permissions (uri "
                    + uri + ")");
        }
        if (pi.uriPermissionPatterns != null) {
            final int N = pi.uriPermissionPatterns.length;
            boolean allowed = false;
            for (int i=0; i<N; i++) {
                if (pi.uriPermissionPatterns[i] != null
                        && pi.uriPermissionPatterns[i].match(uri.getPath())) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new SecurityException("Provider " + pi.packageName
                        + "/" + pi.name
                        + " does not allow granting of permission to path of Uri "
                        + uri);
            }
        }
        if (!checkHoldingPermissionsLocked(pm, pi, callingUid, modeFlags)) {
            if (!checkUriPermissionLocked(uri, callingUid, modeFlags)) {
                throw new SecurityException("Uid " + callingUid
                        + " does not have permission to uri " + uri);
            }
        }
        if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                "Granting " + targetPkg + " permission to " + uri);
        HashMap<Uri, UriPermission> targetUris
                = mGrantedUriPermissions.get(targetUid);
        if (targetUris == null) {
            targetUris = new HashMap<Uri, UriPermission>();
            mGrantedUriPermissions.put(targetUid, targetUris);
        }
        UriPermission perm = targetUris.get(uri);
        if (perm == null) {
            perm = new UriPermission(targetUid, uri);
            targetUris.put(uri, perm);
        }
        perm.modeFlags |= modeFlags;
        if (activity == null) {
            perm.globalModeFlags |= modeFlags;
        } else if ((modeFlags&Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
            perm.readActivities.add(activity);
            if (activity.readUriPermissions == null) {
                activity.readUriPermissions = new HashSet<UriPermission>();
            }
            activity.readUriPermissions.add(perm);
        } else if ((modeFlags&Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0) {
            perm.writeActivities.add(activity);
            if (activity.writeUriPermissions == null) {
                activity.writeUriPermissions = new HashSet<UriPermission>();
            }
            activity.writeUriPermissions.add(perm);
        }
    }
    private void grantUriPermissionFromIntentLocked(int callingUid,
            String targetPkg, Intent intent, HistoryRecord activity) {
        if (intent == null) {
            return;
        }
        Uri data = intent.getData();
        if (data == null) {
            return;
        }
        grantUriPermissionLocked(callingUid, targetPkg, data,
                intent.getFlags(), activity);
    }
    public void grantUriPermission(IApplicationThread caller, String targetPkg,
            Uri uri, int modeFlags) {
        synchronized(this) {
            final ProcessRecord r = getRecordForAppLocked(caller);
            if (r == null) {
                throw new SecurityException("Unable to find app for caller "
                        + caller
                        + " when granting permission to uri " + uri);
            }
            if (targetPkg == null) {
                Slog.w(TAG, "grantUriPermission: null target");
                return;
            }
            if (uri == null) {
                Slog.w(TAG, "grantUriPermission: null uri");
                return;
            }
            grantUriPermissionLocked(r.info.uid, targetPkg, uri, modeFlags,
                    null);
        }
    }
    private void removeUriPermissionIfNeededLocked(UriPermission perm) {
        if ((perm.modeFlags&(Intent.FLAG_GRANT_READ_URI_PERMISSION
                |Intent.FLAG_GRANT_WRITE_URI_PERMISSION)) == 0) {
            HashMap<Uri, UriPermission> perms
                    = mGrantedUriPermissions.get(perm.uid);
            if (perms != null) {
                if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                        "Removing " + perm.uid + " permission to " + perm.uri);
                perms.remove(perm.uri);
                if (perms.size() == 0) {
                    mGrantedUriPermissions.remove(perm.uid);
                }
            }
        }
    }
    private void removeActivityUriPermissionsLocked(HistoryRecord activity) {
        if (activity.readUriPermissions != null) {
            for (UriPermission perm : activity.readUriPermissions) {
                perm.readActivities.remove(activity);
                if (perm.readActivities.size() == 0 && (perm.globalModeFlags
                        &Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
                    perm.modeFlags &= ~Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    removeUriPermissionIfNeededLocked(perm);
                }
            }
        }
        if (activity.writeUriPermissions != null) {
            for (UriPermission perm : activity.writeUriPermissions) {
                perm.writeActivities.remove(activity);
                if (perm.writeActivities.size() == 0 && (perm.globalModeFlags
                        &Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0) {
                    perm.modeFlags &= ~Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    removeUriPermissionIfNeededLocked(perm);
                }
            }
        }
    }
    private void revokeUriPermissionLocked(int callingUid, Uri uri,
            int modeFlags) {
        modeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (modeFlags == 0) {
            return;
        }
        if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                "Revoking all granted permissions to " + uri);
        final IPackageManager pm = ActivityThread.getPackageManager();
        final String authority = uri.getAuthority();
        ProviderInfo pi = null;
        ContentProviderRecord cpr
                = (ContentProviderRecord)mProvidersByName.get(authority);
        if (cpr != null) {
            pi = cpr.info;
        } else {
            try {
                pi = pm.resolveContentProvider(authority,
                        PackageManager.GET_URI_PERMISSION_PATTERNS);
            } catch (RemoteException ex) {
            }
        }
        if (pi == null) {
            Slog.w(TAG, "No content provider found for: " + authority);
            return;
        }
        if (!checkHoldingPermissionsLocked(pm, pi, callingUid, modeFlags)) {
                throw new SecurityException("Uid " + callingUid
                        + " does not have permission to uri " + uri);
        }
        final List<String> SEGMENTS = uri.getPathSegments();
        if (SEGMENTS != null) {
            final int NS = SEGMENTS.size();
            int N = mGrantedUriPermissions.size();
            for (int i=0; i<N; i++) {
                HashMap<Uri, UriPermission> perms
                        = mGrantedUriPermissions.valueAt(i);
                Iterator<UriPermission> it = perms.values().iterator();
            toploop:
                while (it.hasNext()) {
                    UriPermission perm = it.next();
                    Uri targetUri = perm.uri;
                    if (!authority.equals(targetUri.getAuthority())) {
                        continue;
                    }
                    List<String> targetSegments = targetUri.getPathSegments();
                    if (targetSegments == null) {
                        continue;
                    }
                    if (targetSegments.size() < NS) {
                        continue;
                    }
                    for (int j=0; j<NS; j++) {
                        if (!SEGMENTS.get(j).equals(targetSegments.get(j))) {
                            continue toploop;
                        }
                    }
                    if (DEBUG_URI_PERMISSION) Slog.v(TAG, 
                            "Revoking " + perm.uid + " permission to " + perm.uri);
                    perm.clearModes(modeFlags);
                    if (perm.modeFlags == 0) {
                        it.remove();
                    }
                }
                if (perms.size() == 0) {
                    mGrantedUriPermissions.remove(
                            mGrantedUriPermissions.keyAt(i));
                    N--;
                    i--;
                }
            }
        }
    }
    public void revokeUriPermission(IApplicationThread caller, Uri uri,
            int modeFlags) {
        synchronized(this) {
            final ProcessRecord r = getRecordForAppLocked(caller);
            if (r == null) {
                throw new SecurityException("Unable to find app for caller "
                        + caller
                        + " when revoking permission to uri " + uri);
            }
            if (uri == null) {
                Slog.w(TAG, "revokeUriPermission: null uri");
                return;
            }
            modeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (modeFlags == 0) {
                return;
            }
            final IPackageManager pm = ActivityThread.getPackageManager();
            final String authority = uri.getAuthority();
            ProviderInfo pi = null;
            ContentProviderRecord cpr
                    = (ContentProviderRecord)mProvidersByName.get(authority);
            if (cpr != null) {
                pi = cpr.info;
            } else {
                try {
                    pi = pm.resolveContentProvider(authority,
                            PackageManager.GET_URI_PERMISSION_PATTERNS);
                } catch (RemoteException ex) {
                }
            }
            if (pi == null) {
                Slog.w(TAG, "No content provider found for: " + authority);
                return;
            }
            revokeUriPermissionLocked(r.info.uid, uri, modeFlags);
        }
    }
    public void showWaitingForDebugger(IApplicationThread who, boolean waiting) {
        synchronized (this) {
            ProcessRecord app =
                who != null ? getRecordForAppLocked(who) : null;
            if (app == null) return;
            Message msg = Message.obtain();
            msg.what = WAIT_FOR_DEBUGGER_MSG;
            msg.obj = app;
            msg.arg1 = waiting ? 1 : 0;
            mHandler.sendMessage(msg);
        }
    }
    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
        outInfo.availMem = Process.getFreeMemory();
        outInfo.threshold = HOME_APP_MEM;
        outInfo.lowMemory = outInfo.availMem <
                (HOME_APP_MEM + ((HIDDEN_APP_MEM-HOME_APP_MEM)/2));
    }
    public List getTasks(int maxNum, int flags,
                         IThumbnailReceiver receiver) {
        ArrayList list = new ArrayList();
        PendingThumbnailsRecord pending = null;
        IApplicationThread topThumbnail = null;
        HistoryRecord topRecord = null;
        synchronized(this) {
            if (localLOGV) Slog.v(
                TAG, "getTasks: max=" + maxNum + ", flags=" + flags
                + ", receiver=" + receiver);
            if (checkCallingPermission(android.Manifest.permission.GET_TASKS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (receiver != null) {
                    try {
                        receiver.finished();
                    } catch (RemoteException ex) {
                    }
                }
                String msg = "Permission Denial: getTasks() from pid="
                        + Binder.getCallingPid()
                        + ", uid=" + Binder.getCallingUid()
                        + " requires " + android.Manifest.permission.GET_TASKS;
                Slog.w(TAG, msg);
                throw new SecurityException(msg);
            }
            int pos = mHistory.size()-1;
            HistoryRecord next =
                pos >= 0 ? (HistoryRecord)mHistory.get(pos) : null;
            HistoryRecord top = null;
            CharSequence topDescription = null;
            TaskRecord curTask = null;
            int numActivities = 0;
            int numRunning = 0;
            while (pos >= 0 && maxNum > 0) {
                final HistoryRecord r = next;
                pos--;
                next = pos >= 0 ? (HistoryRecord)mHistory.get(pos) : null;
                if (top == null ||
                        (top.state == ActivityState.INITIALIZING
                            && top.task == r.task)) {
                    top = r;
                    topDescription = r.description;
                    curTask = r.task;
                    numActivities = numRunning = 0;
                }
                numActivities++;
                if (r.app != null && r.app.thread != null) {
                    numRunning++;
                }
                if (topDescription == null) {
                    topDescription = r.description;
                }
                if (localLOGV) Slog.v(
                    TAG, r.intent.getComponent().flattenToShortString()
                    + ": task=" + r.task);
                if (next == null || next.task != curTask) {
                    ActivityManager.RunningTaskInfo ci
                            = new ActivityManager.RunningTaskInfo();
                    ci.id = curTask.taskId;
                    ci.baseActivity = r.intent.getComponent();
                    ci.topActivity = top.intent.getComponent();
                    ci.thumbnail = top.thumbnail;
                    ci.description = topDescription;
                    ci.numActivities = numActivities;
                    ci.numRunning = numRunning;
                    if (ci.thumbnail == null && receiver != null) {
                        if (localLOGV) Slog.v(
                            TAG, "State=" + top.state + "Idle=" + top.idle
                            + " app=" + top.app
                            + " thr=" + (top.app != null ? top.app.thread : null));
                        if (top.state == ActivityState.RESUMED
                                || top.state == ActivityState.PAUSING) {
                            if (top.idle && top.app != null
                                && top.app.thread != null) {
                                topRecord = top;
                                topThumbnail = top.app.thread;
                            } else {
                                top.thumbnailNeeded = true;
                            }
                        }
                        if (pending == null) {
                            pending = new PendingThumbnailsRecord(receiver);
                        }
                        pending.pendingRecords.add(top);
                    }
                    list.add(ci);
                    maxNum--;
                    top = null;
                }
            }
            if (pending != null) {
                mPendingThumbnails.add(pending);
            }
        }
        if (localLOGV) Slog.v(TAG, "We have pending thumbnails: " + pending);
        if (topThumbnail != null) {
            if (localLOGV) Slog.v(TAG, "Requesting top thumbnail");
            try {
                topThumbnail.requestThumbnail(topRecord);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown when requesting thumbnail", e);
                sendPendingThumbnail(null, topRecord, null, null, true);
            }
        }
        if (pending == null && receiver != null) {
            try {
                receiver.finished();
            } catch (RemoteException ex) {
            }
        }
        return list;
    }
    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum,
            int flags) {
        synchronized (this) {
            enforceCallingPermission(android.Manifest.permission.GET_TASKS,
                    "getRecentTasks()");
            IPackageManager pm = ActivityThread.getPackageManager();
            final int N = mRecentTasks.size();
            ArrayList<ActivityManager.RecentTaskInfo> res
                    = new ArrayList<ActivityManager.RecentTaskInfo>(
                            maxNum < N ? maxNum : N);
            for (int i=0; i<N && maxNum > 0; i++) {
                TaskRecord tr = mRecentTasks.get(i);
                if (((flags&ActivityManager.RECENT_WITH_EXCLUDED) != 0)
                        || (tr.intent == null)
                        || ((tr.intent.getFlags()
                                &Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) == 0)) {
                    ActivityManager.RecentTaskInfo rti
                            = new ActivityManager.RecentTaskInfo();
                    rti.id = tr.numActivities > 0 ? tr.taskId : -1;
                    rti.baseIntent = new Intent(
                            tr.intent != null ? tr.intent : tr.affinityIntent);
                    rti.origActivity = tr.origActivity;
                    if ((flags&ActivityManager.RECENT_IGNORE_UNAVAILABLE) != 0) {
                        try {
                            if (rti.origActivity != null) {
                                if (pm.getActivityInfo(rti.origActivity, 0) == null) {
                                    continue;
                                }
                            } else if (rti.baseIntent != null) {
                                if (pm.queryIntentActivities(rti.baseIntent,
                                        null, 0) == null) {
                                    continue;
                                }
                            }
                        } catch (RemoteException e) {
                        }
                    }
                    res.add(rti);
                    maxNum--;
                }
            }
            return res;
        }
    }
    private final int findAffinityTaskTopLocked(int startIndex, String affinity) {
        int j;
        TaskRecord startTask = ((HistoryRecord)mHistory.get(startIndex)).task; 
        TaskRecord jt = startTask;
        for (j=startIndex-1; j>=0; j--) {
            HistoryRecord r = (HistoryRecord)mHistory.get(j);
            if (r.task != jt) {
                jt = r.task;
                if (affinity.equals(jt.affinity)) {
                    return j;
                }
            }
        }
        final int N = mHistory.size();
        jt = startTask;
        for (j=startIndex+1; j<N; j++) {
            HistoryRecord r = (HistoryRecord)mHistory.get(j);
            if (r.task != jt) {
                if (affinity.equals(jt.affinity)) {
                    return j;
                }
                jt = r.task;
            }
        }
        if (affinity.equals(((HistoryRecord)mHistory.get(N-1)).task.affinity)) {
            return N-1;
        }
        return -1;
    }
    private final HistoryRecord resetTaskIfNeededLocked(HistoryRecord taskTop,
            HistoryRecord newActivity) {
        boolean forceReset = (newActivity.info.flags
                &ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH) != 0;
        if (taskTop.task.getInactiveDuration() > ACTIVITY_INACTIVE_RESET_TIME) {
            if ((newActivity.info.flags
                    &ActivityInfo.FLAG_ALWAYS_RETAIN_TASK_STATE) == 0) {
                forceReset = true;
            }
        }
        final TaskRecord task = taskTop.task;
        HistoryRecord target = null;
        int targetI = 0;
        int taskTopI = -1;
        int replyChainEnd = -1;
        int lastReparentPos = -1;
        for (int i=mHistory.size()-1; i>=-1; i--) {
            HistoryRecord below = i >= 0 ? (HistoryRecord)mHistory.get(i) : null;
            if (below != null && below.finishing) {
                continue;
            }
            if (target == null) {
                target = below;
                targetI = i;
                replyChainEnd = -1;
                continue;
            }
            final int flags = target.info.flags;
            final boolean finishOnTaskLaunch =
                (flags&ActivityInfo.FLAG_FINISH_ON_TASK_LAUNCH) != 0;
            final boolean allowTaskReparenting =
                (flags&ActivityInfo.FLAG_ALLOW_TASK_REPARENTING) != 0;
            if (target.task == task) {
                if (taskTopI < 0) {
                    taskTopI = targetI;
                }
                if (below != null && below.task == task) {
                    final boolean clearWhenTaskReset =
                            (target.intent.getFlags()
                                    &Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) != 0;
                    if (!finishOnTaskLaunch && !clearWhenTaskReset && target.resultTo != null) {
                        if (replyChainEnd < 0) {
                            replyChainEnd = targetI;
                        }
                    } else if (!finishOnTaskLaunch && !clearWhenTaskReset && allowTaskReparenting
                            && target.taskAffinity != null
                            && !target.taskAffinity.equals(task.affinity)) {
                        HistoryRecord p = (HistoryRecord)mHistory.get(0);
                        if (target.taskAffinity != null
                                && target.taskAffinity.equals(p.task.affinity)) {
                            target.task = p.task;
                            if (DEBUG_TASKS) Slog.v(TAG, "Start pushing activity " + target
                                    + " out to bottom task " + p.task);
                        } else {
                            mCurTask++;
                            if (mCurTask <= 0) {
                                mCurTask = 1;
                            }
                            target.task = new TaskRecord(mCurTask, target.info, null,
                                    (target.info.flags&ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH) != 0);
                            target.task.affinityIntent = target.intent;
                            if (DEBUG_TASKS) Slog.v(TAG, "Start pushing activity " + target
                                    + " out to new task " + target.task);
                        }
                        mWindowManager.setAppGroupId(target, task.taskId);
                        if (replyChainEnd < 0) {
                            replyChainEnd = targetI;
                        }
                        int dstPos = 0;
                        for (int srcPos=targetI; srcPos<=replyChainEnd; srcPos++) {
                            p = (HistoryRecord)mHistory.get(srcPos);
                            if (p.finishing) {
                                continue;
                            }
                            if (DEBUG_TASKS) Slog.v(TAG, "Pushing next activity " + p
                                    + " out to target's task " + target.task);
                            task.numActivities--;
                            p.task = target.task;
                            target.task.numActivities++;
                            mHistory.remove(srcPos);
                            mHistory.add(dstPos, p);
                            mWindowManager.moveAppToken(dstPos, p);
                            mWindowManager.setAppGroupId(p, p.task.taskId);
                            dstPos++;
                            if (VALIDATE_TOKENS) {
                                mWindowManager.validateAppTokens(mHistory);
                            }
                            i++;
                        }
                        if (taskTop == p) {
                            taskTop = below;
                        }
                        if (taskTopI == replyChainEnd) {
                            taskTopI = -1;
                        }
                        replyChainEnd = -1;
                        addRecentTaskLocked(target.task);
                    } else if (forceReset || finishOnTaskLaunch
                            || clearWhenTaskReset) {
                        if (clearWhenTaskReset) {
                            replyChainEnd = targetI+1;
                            while (replyChainEnd < mHistory.size() &&
                                    ((HistoryRecord)mHistory.get(
                                                replyChainEnd)).task == task) {
                                replyChainEnd++;
                            }
                            replyChainEnd--;
                        } else if (replyChainEnd < 0) {
                            replyChainEnd = targetI;
                        }
                        HistoryRecord p = null;
                        for (int srcPos=targetI; srcPos<=replyChainEnd; srcPos++) {
                            p = (HistoryRecord)mHistory.get(srcPos);
                            if (p.finishing) {
                                continue;
                            }
                            if (finishActivityLocked(p, srcPos,
                                    Activity.RESULT_CANCELED, null, "reset")) {
                                replyChainEnd--;
                                srcPos--;
                            }
                        }
                        if (taskTop == p) {
                            taskTop = below;
                        }
                        if (taskTopI == replyChainEnd) {
                            taskTopI = -1;
                        }
                        replyChainEnd = -1;
                    } else {
                        replyChainEnd = -1;
                    }
                } else {
                    replyChainEnd = -1;
                }
            } else if (target.resultTo != null) {
                if (replyChainEnd < 0) {
                    replyChainEnd = targetI;
                }
            } else if (taskTopI >= 0 && allowTaskReparenting
                    && task.affinity != null
                    && task.affinity.equals(target.taskAffinity)) {
                if (forceReset || finishOnTaskLaunch) {
                    if (replyChainEnd < 0) {
                        replyChainEnd = targetI;
                    }
                    HistoryRecord p = null;
                    for (int srcPos=targetI; srcPos<=replyChainEnd; srcPos++) {
                        p = (HistoryRecord)mHistory.get(srcPos);
                        if (p.finishing) {
                            continue;
                        }
                        if (finishActivityLocked(p, srcPos,
                                Activity.RESULT_CANCELED, null, "reset")) {
                            taskTopI--;
                            lastReparentPos--;
                            replyChainEnd--;
                            srcPos--;
                        }
                    }
                    replyChainEnd = -1;
                } else {
                    if (replyChainEnd < 0) {
                        replyChainEnd = targetI;
                    }
                    for (int srcPos=replyChainEnd; srcPos>=targetI; srcPos--) {
                        HistoryRecord p = (HistoryRecord)mHistory.get(srcPos);
                        if (p.finishing) {
                            continue;
                        }
                        if (lastReparentPos < 0) {
                            lastReparentPos = taskTopI;
                            taskTop = p;
                        } else {
                            lastReparentPos--;
                        }
                        mHistory.remove(srcPos);
                        p.task.numActivities--;
                        p.task = task;
                        mHistory.add(lastReparentPos, p);
                        if (DEBUG_TASKS) Slog.v(TAG, "Pulling activity " + p
                                + " in to resetting task " + task);
                        task.numActivities++;
                        mWindowManager.moveAppToken(lastReparentPos, p);
                        mWindowManager.setAppGroupId(p, p.task.taskId);
                        if (VALIDATE_TOKENS) {
                            mWindowManager.validateAppTokens(mHistory);
                        }
                    }
                    replyChainEnd = -1;
                    if (target.info.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {
                        for (int j=lastReparentPos-1; j>=0; j--) {
                            HistoryRecord p = (HistoryRecord)mHistory.get(j);
                            if (p.finishing) {
                                continue;
                            }
                            if (p.intent.getComponent().equals(target.intent.getComponent())) {
                                if (finishActivityLocked(p, j,
                                        Activity.RESULT_CANCELED, null, "replace")) {
                                    taskTopI--;
                                    lastReparentPos--;
                                }
                            }
                        }
                    }
                }
            }
            target = below;
            targetI = i;
        }
        return taskTop;
    }
    public void moveTaskToFront(int task) {
        enforceCallingPermission(android.Manifest.permission.REORDER_TASKS,
                "moveTaskToFront()");
        synchronized(this) {
            if (!checkAppSwitchAllowedLocked(Binder.getCallingPid(),
                    Binder.getCallingUid(), "Task to front")) {
                return;
            }
            final long origId = Binder.clearCallingIdentity();
            try {
                int N = mRecentTasks.size();
                for (int i=0; i<N; i++) {
                    TaskRecord tr = mRecentTasks.get(i);
                    if (tr.taskId == task) {
                        moveTaskToFrontLocked(tr, null);
                        return;
                    }
                }
                for (int i=mHistory.size()-1; i>=0; i--) {
                    HistoryRecord hr = (HistoryRecord)mHistory.get(i);
                    if (hr.task.taskId == task) {
                        moveTaskToFrontLocked(hr.task, null);
                        return;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    private final void moveTaskToFrontLocked(TaskRecord tr, HistoryRecord reason) {
        if (DEBUG_SWITCH) Slog.v(TAG, "moveTaskToFront: " + tr);
        final int task = tr.taskId;
        int top = mHistory.size()-1;
        if (top < 0 || ((HistoryRecord)mHistory.get(top)).task.taskId == task) {
            return;
        }
        ArrayList moved = new ArrayList();
        top = mHistory.size()-1;
        int pos = top;
        while (pos >= 0) {
            HistoryRecord r = (HistoryRecord)mHistory.get(pos);
            if (localLOGV) Slog.v(
                TAG, "At " + pos + " ckp " + r.task + ": " + r);
            boolean first = true;
            if (r.task.taskId == task) {
                if (localLOGV) Slog.v(TAG, "Removing and adding at " + top);
                mHistory.remove(pos);
                mHistory.add(top, r);
                moved.add(0, r);
                top--;
                if (first) {
                    addRecentTaskLocked(r.task);
                    first = false;
                }
            }
            pos--;
        }
        if (DEBUG_TRANSITION) Slog.v(TAG,
                "Prepare to front transition: task=" + tr);
        if (reason != null &&
                (reason.intent.getFlags()&Intent.FLAG_ACTIVITY_NO_ANIMATION) != 0) {
            mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_NONE);
            HistoryRecord r = topRunningActivityLocked(null);
            if (r != null) {
                mNoAnimActivities.add(r);
            }
        } else {
            mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_TASK_TO_FRONT);
        }
        mWindowManager.moveAppTokensToTop(moved);
        if (VALIDATE_TOKENS) {
            mWindowManager.validateAppTokens(mHistory);
        }
        finishTaskMoveLocked(task);
        EventLog.writeEvent(EventLogTags.AM_TASK_TO_FRONT, task);
    }
    private final void finishTaskMoveLocked(int task) {
        resumeTopActivityLocked(null);
    }
    public void moveTaskToBack(int task) {
        enforceCallingPermission(android.Manifest.permission.REORDER_TASKS,
                "moveTaskToBack()");
        synchronized(this) {
            if (mResumedActivity != null && mResumedActivity.task.taskId == task) {
                if (!checkAppSwitchAllowedLocked(Binder.getCallingPid(),
                        Binder.getCallingUid(), "Task to back")) {
                    return;
                }
            }
            final long origId = Binder.clearCallingIdentity();
            moveTaskToBackLocked(task, null);
            Binder.restoreCallingIdentity(origId);
        }
    }
    public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) {
        synchronized(this) {
            final long origId = Binder.clearCallingIdentity();
            int taskId = getTaskForActivityLocked(token, !nonRoot);
            if (taskId >= 0) {
                return moveTaskToBackLocked(taskId, null);
            }
            Binder.restoreCallingIdentity(origId);
        }
        return false;
    }
    private final boolean moveTaskToBackLocked(int task, HistoryRecord reason) {
        Slog.i(TAG, "moveTaskToBack: " + task);
        if (mController != null) {
            HistoryRecord next = topRunningActivityLocked(null, task);
            if (next == null) {
                next = topRunningActivityLocked(null, 0);
            }
            if (next != null) {
                boolean moveOK = true;
                try {
                    moveOK = mController.activityResuming(next.packageName);
                } catch (RemoteException e) {
                    mController = null;
                }
                if (!moveOK) {
                    return false;
                }
            }
        }
        ArrayList moved = new ArrayList();
        if (DEBUG_TRANSITION) Slog.v(TAG,
                "Prepare to back transition: task=" + task);
        final int N = mHistory.size();
        int bottom = 0;
        int pos = 0;
        while (pos < N) {
            HistoryRecord r = (HistoryRecord)mHistory.get(pos);
            if (localLOGV) Slog.v(
                TAG, "At " + pos + " ckp " + r.task + ": " + r);
            if (r.task.taskId == task) {
                if (localLOGV) Slog.v(TAG, "Removing and adding at " + (N-1));
                mHistory.remove(pos);
                mHistory.add(bottom, r);
                moved.add(r);
                bottom++;
            }
            pos++;
        }
        if (reason != null &&
                (reason.intent.getFlags()&Intent.FLAG_ACTIVITY_NO_ANIMATION) != 0) {
            mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_NONE);
            HistoryRecord r = topRunningActivityLocked(null);
            if (r != null) {
                mNoAnimActivities.add(r);
            }
        } else {
            mWindowManager.prepareAppTransition(WindowManagerPolicy.TRANSIT_TASK_TO_BACK);
        }
        mWindowManager.moveAppTokensToBottom(moved);
        if (VALIDATE_TOKENS) {
            mWindowManager.validateAppTokens(mHistory);
        }
        finishTaskMoveLocked(task);
        return true;
    }
    public void moveTaskBackwards(int task) {
        enforceCallingPermission(android.Manifest.permission.REORDER_TASKS,
                "moveTaskBackwards()");
        synchronized(this) {
            if (!checkAppSwitchAllowedLocked(Binder.getCallingPid(),
                    Binder.getCallingUid(), "Task backwards")) {
                return;
            }
            final long origId = Binder.clearCallingIdentity();
            moveTaskBackwardsLocked(task);
            Binder.restoreCallingIdentity(origId);
        }
    }
    private final void moveTaskBackwardsLocked(int task) {
        Slog.e(TAG, "moveTaskBackwards not yet implemented!");
    }
    public int getTaskForActivity(IBinder token, boolean onlyRoot) {
        synchronized(this) {
            return getTaskForActivityLocked(token, onlyRoot);
        }
    }
    int getTaskForActivityLocked(IBinder token, boolean onlyRoot) {
        final int N = mHistory.size();
        TaskRecord lastTask = null;
        for (int i=0; i<N; i++) {
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (r == token) {
                if (!onlyRoot || lastTask != r.task) {
                    return r.task.taskId;
                }
                return -1;
            }
            lastTask = r.task;
        }
        return -1;
    }
    private HistoryRecord findTaskLocked(Intent intent, ActivityInfo info) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        TaskRecord cp = null;
        final int N = mHistory.size();
        for (int i=(N-1); i>=0; i--) {
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (!r.finishing && r.task != cp
                    && r.launchMode != ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
                cp = r.task;
                if (r.task.affinity != null) {
                    if (r.task.affinity.equals(info.taskAffinity)) {
                        return r;
                    }
                } else if (r.task.intent != null
                        && r.task.intent.getComponent().equals(cls)) {
                    return r;
                } else if (r.task.affinityIntent != null
                        && r.task.affinityIntent.getComponent().equals(cls)) {
                    return r;
                }
            }
        }
        return null;
    }
    private HistoryRecord findActivityLocked(Intent intent, ActivityInfo info) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        final int N = mHistory.size();
        for (int i=(N-1); i>=0; i--) {
            HistoryRecord r = (HistoryRecord)mHistory.get(i);
            if (!r.finishing) {
                if (r.intent.getComponent().equals(cls)) {
                    return r;
                }
            }
        }
        return null;
    }
    public void finishOtherInstances(IBinder token, ComponentName className) {
        synchronized(this) {
            final long origId = Binder.clearCallingIdentity();
            int N = mHistory.size();
            TaskRecord lastTask = null;
            for (int i=0; i<N; i++) {
                HistoryRecord r = (HistoryRecord)mHistory.get(i);
                if (r.realActivity.equals(className)
                        && r != token && lastTask != r.task) {
                    if (finishActivityLocked(r, i, Activity.RESULT_CANCELED,
                            null, "others")) {
                        i--;
                        N--;
                    }
                }
                lastTask = r.task;
            }
            Binder.restoreCallingIdentity(origId);
        }
    }
    public void reportThumbnail(IBinder token,
            Bitmap thumbnail, CharSequence description) {
        final long origId = Binder.clearCallingIdentity();
        sendPendingThumbnail(null, token, thumbnail, description, true);
        Binder.restoreCallingIdentity(origId);
    }
    final void sendPendingThumbnail(HistoryRecord r, IBinder token,
            Bitmap thumbnail, CharSequence description, boolean always) {
        TaskRecord task = null;
        ArrayList receivers = null;
        synchronized(this) {
            if (r == null) {
                int index = indexOfTokenLocked(token);
                if (index < 0) {
                    return;
                }
                r = (HistoryRecord)mHistory.get(index);
            }
            if (thumbnail == null) {
                thumbnail = r.thumbnail;
                description = r.description;
            }
            if (thumbnail == null && !always) {
                return;
            }
            task = r.task;
            int N = mPendingThumbnails.size();
            int i=0;
            while (i<N) {
                PendingThumbnailsRecord pr =
                    (PendingThumbnailsRecord)mPendingThumbnails.get(i);
                if (pr.pendingRecords.remove(r)) {
                    if (receivers == null) {
                        receivers = new ArrayList();
                    }
                    receivers.add(pr);
                    if (pr.pendingRecords.size() == 0) {
                        pr.finished = true;
                        mPendingThumbnails.remove(i);
                        N--;
                        continue;
                    }
                }
                i++;
            }
        }
        if (receivers != null) {
            final int N = receivers.size();
            for (int i=0; i<N; i++) {
                try {
                    PendingThumbnailsRecord pr =
                        (PendingThumbnailsRecord)receivers.get(i);
                    pr.receiver.newThumbnail(
                        task != null ? task.taskId : -1, thumbnail, description);
                    if (pr.finished) {
                        pr.receiver.finished();
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Exception thrown when sending thumbnail", e);
                }
            }
        }
    }
    private final List generateApplicationProvidersLocked(ProcessRecord app) {
        List providers = null;
        try {
            providers = ActivityThread.getPackageManager().
                queryContentProviders(app.processName, app.info.uid,
                        STOCK_PM_FLAGS | PackageManager.GET_URI_PERMISSION_PATTERNS);
        } catch (RemoteException ex) {
        }
        if (providers != null) {
            final int N = providers.size();
            for (int i=0; i<N; i++) {
                ProviderInfo cpi =
                    (ProviderInfo)providers.get(i);
                ContentProviderRecord cpr =
                    (ContentProviderRecord)mProvidersByClass.get(cpi.name);
                if (cpr == null) {
                    cpr = new ContentProviderRecord(cpi, app.info);
                    mProvidersByClass.put(cpi.name, cpr);
                }
                app.pubProviders.put(cpi.name, cpr);
                app.addPackage(cpi.applicationInfo.packageName);
                ensurePackageDexOpt(cpi.applicationInfo.packageName);
            }
        }
        return providers;
    }
    private final String checkContentProviderPermissionLocked(
            ProviderInfo cpi, ProcessRecord r, int mode) {
        final int callingPid = (r != null) ? r.pid : Binder.getCallingPid();
        final int callingUid = (r != null) ? r.info.uid : Binder.getCallingUid();
        if (checkComponentPermission(cpi.readPermission, callingPid, callingUid,
                cpi.exported ? -1 : cpi.applicationInfo.uid)
                == PackageManager.PERMISSION_GRANTED
                && mode == ParcelFileDescriptor.MODE_READ_ONLY || mode == -1) {
            return null;
        }
        if (checkComponentPermission(cpi.writePermission, callingPid, callingUid,
                cpi.exported ? -1 : cpi.applicationInfo.uid)
                == PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        PathPermission[] pps = cpi.pathPermissions;
        if (pps != null) {
            int i = pps.length;
            while (i > 0) {
                i--;
                PathPermission pp = pps[i];
                if (checkComponentPermission(pp.getReadPermission(), callingPid, callingUid,
                        cpi.exported ? -1 : cpi.applicationInfo.uid)
                        == PackageManager.PERMISSION_GRANTED
                        && mode == ParcelFileDescriptor.MODE_READ_ONLY || mode == -1) {
                    return null;
                }
                if (checkComponentPermission(pp.getWritePermission(), callingPid, callingUid,
                        cpi.exported ? -1 : cpi.applicationInfo.uid)
                        == PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
            }
        }
        String msg = "Permission Denial: opening provider " + cpi.name
                + " from " + (r != null ? r : "(null)") + " (pid=" + callingPid
                + ", uid=" + callingUid + ") requires "
                + cpi.readPermission + " or " + cpi.writePermission;
        Slog.w(TAG, msg);
        return msg;
    }
    private final ContentProviderHolder getContentProviderImpl(
        IApplicationThread caller, String name) {
        ContentProviderRecord cpr;
        ProviderInfo cpi = null;
        synchronized(this) {
            ProcessRecord r = null;
            if (caller != null) {
                r = getRecordForAppLocked(caller);
                if (r == null) {
                    throw new SecurityException(
                            "Unable to find app for caller " + caller
                          + " (pid=" + Binder.getCallingPid()
                          + ") when getting content provider " + name);
                }
            }
            cpr = (ContentProviderRecord)mProvidersByName.get(name);
            if (cpr != null) {
                cpi = cpr.info;
                if (checkContentProviderPermissionLocked(cpi, r, -1) != null) {
                    return new ContentProviderHolder(cpi,
                            cpi.readPermission != null
                                    ? cpi.readPermission : cpi.writePermission);
                }
                if (r != null && cpr.canRunHere(r)) {
                    if (cpr.provider != null) {
                        cpr = new ContentProviderRecord(cpr);
                    }
                    return cpr;
                }
                final long origId = Binder.clearCallingIdentity();
                if (r != null) {
                    if (DEBUG_PROVIDER) Slog.v(TAG,
                            "Adding provider requested by "
                            + r.processName + " from process "
                            + cpr.info.processName);
                    Integer cnt = r.conProviders.get(cpr);
                    if (cnt == null) {
                        r.conProviders.put(cpr, new Integer(1));
                    } else {
                        r.conProviders.put(cpr, new Integer(cnt.intValue()+1));
                    }
                    cpr.clients.add(r);
                    if (cpr.app != null && r.setAdj >= VISIBLE_APP_ADJ) {
                        updateLruProcessLocked(cpr.app, false, true);
                    }
                } else {
                    cpr.externals++;
                }
                if (cpr.app != null) {
                    updateOomAdjLocked(cpr.app);
                }
                Binder.restoreCallingIdentity(origId);
            } else {
                try {
                    cpi = ActivityThread.getPackageManager().
                        resolveContentProvider(name,
                                STOCK_PM_FLAGS | PackageManager.GET_URI_PERMISSION_PATTERNS);
                } catch (RemoteException ex) {
                }
                if (cpi == null) {
                    return null;
                }
                if (checkContentProviderPermissionLocked(cpi, r, -1) != null) {
                    return new ContentProviderHolder(cpi,
                            cpi.readPermission != null
                                    ? cpi.readPermission : cpi.writePermission);
                }
                if (!mSystemReady && !mDidUpdate && !mWaitingUpdate
                        && !cpi.processName.equals("system")) {
                    throw new IllegalArgumentException(
                            "Attempt to launch content provider before system ready");
                }
                cpr = (ContentProviderRecord)mProvidersByClass.get(cpi.name);
                final boolean firstClass = cpr == null;
                if (firstClass) {
                    try {
                        ApplicationInfo ai =
                            ActivityThread.getPackageManager().
                                getApplicationInfo(
                                        cpi.applicationInfo.packageName,
                                        STOCK_PM_FLAGS);
                        if (ai == null) {
                            Slog.w(TAG, "No package info for content provider "
                                    + cpi.name);
                            return null;
                        }
                        cpr = new ContentProviderRecord(cpi, ai);
                    } catch (RemoteException ex) {
                    }
                }
                if (r != null && cpr.canRunHere(r)) {
                    return cpr;
                }
                if (DEBUG_PROVIDER) {
                    RuntimeException e = new RuntimeException("here");
                    Slog.w(TAG, "LAUNCHING REMOTE PROVIDER (myuid " + r.info.uid
                          + " pruid " + cpr.appInfo.uid + "): " + cpr.info.name, e);
                }
                final int N = mLaunchingProviders.size();
                int i;
                for (i=0; i<N; i++) {
                    if (mLaunchingProviders.get(i) == cpr) {
                        break;
                    }
                }
                if (i >= N) {
                    final long origId = Binder.clearCallingIdentity();
                    ProcessRecord proc = startProcessLocked(cpi.processName,
                            cpr.appInfo, false, 0, "content provider",
                            new ComponentName(cpi.applicationInfo.packageName,
                                    cpi.name), false);
                    if (proc == null) {
                        Slog.w(TAG, "Unable to launch app "
                                + cpi.applicationInfo.packageName + "/"
                                + cpi.applicationInfo.uid + " for provider "
                                + name + ": process is bad");
                        return null;
                    }
                    cpr.launchingApp = proc;
                    mLaunchingProviders.add(cpr);
                    Binder.restoreCallingIdentity(origId);
                }
                if (firstClass) {
                    mProvidersByClass.put(cpi.name, cpr);
                }
                mProvidersByName.put(name, cpr);
                if (r != null) {
                    if (DEBUG_PROVIDER) Slog.v(TAG,
                            "Adding provider requested by "
                            + r.processName + " from process "
                            + cpr.info.processName);
                    Integer cnt = r.conProviders.get(cpr);
                    if (cnt == null) {
                        r.conProviders.put(cpr, new Integer(1));
                    } else {
                        r.conProviders.put(cpr, new Integer(cnt.intValue()+1));
                    }
                    cpr.clients.add(r);
                } else {
                    cpr.externals++;
                }
            }
        }
        synchronized (cpr) {
            while (cpr.provider == null) {
                if (cpr.launchingApp == null) {
                    Slog.w(TAG, "Unable to launch app "
                            + cpi.applicationInfo.packageName + "/"
                            + cpi.applicationInfo.uid + " for provider "
                            + name + ": launching app became null");
                    EventLog.writeEvent(EventLogTags.AM_PROVIDER_LOST_PROCESS,
                            cpi.applicationInfo.packageName,
                            cpi.applicationInfo.uid, name);
                    return null;
                }
                try {
                    cpr.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        return cpr;
    }
    public final ContentProviderHolder getContentProvider(
            IApplicationThread caller, String name) {
        if (caller == null) {
            String msg = "null IApplicationThread when getting content provider "
                    + name;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        return getContentProviderImpl(caller, name);
    }
    private ContentProviderHolder getContentProviderExternal(String name) {
        return getContentProviderImpl(null, name);
    }
    public void removeContentProvider(IApplicationThread caller, String name) {
        synchronized (this) {
            ContentProviderRecord cpr = (ContentProviderRecord)mProvidersByName.get(name);
            if(cpr == null) {
                if (DEBUG_PROVIDER) Slog.v(TAG, name +
                        " provider not found in providers list");
                return;
            }
            final ProcessRecord r = getRecordForAppLocked(caller);
            if (r == null) {
                throw new SecurityException(
                        "Unable to find app for caller " + caller +
                        " when removing content provider " + name);
            }
            ContentProviderRecord localCpr = (ContentProviderRecord)
                    mProvidersByClass.get(cpr.info.name);
            if (DEBUG_PROVIDER) Slog.v(TAG, "Removing provider requested by "
                    + r.info.processName + " from process "
                    + localCpr.appInfo.processName);
            if (localCpr.app == r) {
                Slog.w(TAG, "removeContentProvider called on local provider: "
                        + cpr.info.name + " in process " + r.processName);
                return;
            } else {
                Integer cnt = r.conProviders.get(localCpr);
                if (cnt == null || cnt.intValue() <= 1) {
                    localCpr.clients.remove(r);
                    r.conProviders.remove(localCpr);
                } else {
                    r.conProviders.put(localCpr, new Integer(cnt.intValue()-1));
                }
            }
            updateOomAdjLocked();
        }
    }
    private void removeContentProviderExternal(String name) {
        synchronized (this) {
            ContentProviderRecord cpr = (ContentProviderRecord)mProvidersByName.get(name);
            if(cpr == null) {
                if(localLOGV) Slog.v(TAG, name+" content provider not found in providers list");
                return;
            }
            ContentProviderRecord localCpr = (ContentProviderRecord) mProvidersByClass.get(cpr.info.name);
            localCpr.externals--;
            if (localCpr.externals < 0) {
                Slog.e(TAG, "Externals < 0 for content provider " + localCpr);
            }
            updateOomAdjLocked();
        }
    }
    public final void publishContentProviders(IApplicationThread caller,
            List<ContentProviderHolder> providers) {
        if (providers == null) {
            return;
        }
        synchronized(this) {
            final ProcessRecord r = getRecordForAppLocked(caller);
            if (r == null) {
                throw new SecurityException(
                        "Unable to find app for caller " + caller
                      + " (pid=" + Binder.getCallingPid()
                      + ") when publishing content providers");
            }
            final long origId = Binder.clearCallingIdentity();
            final int N = providers.size();
            for (int i=0; i<N; i++) {
                ContentProviderHolder src = providers.get(i);
                if (src == null || src.info == null || src.provider == null) {
                    continue;
                }
                ContentProviderRecord dst =
                    (ContentProviderRecord)r.pubProviders.get(src.info.name);
                if (dst != null) {
                    mProvidersByClass.put(dst.info.name, dst);
                    String names[] = dst.info.authority.split(";");
                    for (int j = 0; j < names.length; j++) {
                        mProvidersByName.put(names[j], dst);
                    }
                    int NL = mLaunchingProviders.size();
                    int j;
                    for (j=0; j<NL; j++) {
                        if (mLaunchingProviders.get(j) == dst) {
                            mLaunchingProviders.remove(j);
                            j--;
                            NL--;
                        }
                    }
                    synchronized (dst) {
                        dst.provider = src.provider;
                        dst.app = r;
                        dst.notifyAll();
                    }
                    updateOomAdjLocked(r);
                }
            }
            Binder.restoreCallingIdentity(origId);
        }
    }
    public static final void installSystemProviders() {
        List providers;
        synchronized (mSelf) {
            ProcessRecord app = mSelf.mProcessNames.get("system", Process.SYSTEM_UID);
            providers = mSelf.generateApplicationProvidersLocked(app);
            if (providers != null) {
                for (int i=providers.size()-1; i>=0; i--) {
                    ProviderInfo pi = (ProviderInfo)providers.get(i);
                    if ((pi.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM) == 0) {
                        Slog.w(TAG, "Not installing system proc provider " + pi.name
                                + ": not system .apk");
                        providers.remove(i);
                    }
                }
            }
        }
        if (providers != null) {
            mSystemThread.installSystemProviders(providers);
        }
    }
    final ProcessRecord newProcessRecordLocked(IApplicationThread thread,
            ApplicationInfo info, String customProcess) {
        String proc = customProcess != null ? customProcess : info.processName;
        BatteryStatsImpl.Uid.Proc ps = null;
        BatteryStatsImpl stats = mBatteryStatsService.getActiveStatistics();
        synchronized (stats) {
            ps = stats.getProcessStatsLocked(info.uid, proc);
        }
        return new ProcessRecord(ps, thread, info, proc);
    }
    final ProcessRecord addAppLocked(ApplicationInfo info) {
        ProcessRecord app = getProcessRecordLocked(info.processName, info.uid);
        if (app == null) {
            app = newProcessRecordLocked(null, info, null);
            mProcessNames.put(info.processName, info.uid, app);
            updateLruProcessLocked(app, true, true);
        }
        if ((info.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_PERSISTENT))
                == (ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_PERSISTENT)) {
            app.persistent = true;
            app.maxAdj = CORE_SERVER_ADJ;
        }
        if (app.thread == null && mPersistentStartingProcesses.indexOf(app) < 0) {
            mPersistentStartingProcesses.add(app);
            startProcessLocked(app, "added application", app.processName);
        }
        return app;
    }
    public void unhandledBack() {
        enforceCallingPermission(android.Manifest.permission.FORCE_BACK,
                "unhandledBack()");
        synchronized(this) {
            int count = mHistory.size();
            if (DEBUG_SWITCH) Slog.d(
                TAG, "Performing unhandledBack(): stack size = " + count);
            if (count > 1) {
                final long origId = Binder.clearCallingIdentity();
                finishActivityLocked((HistoryRecord)mHistory.get(count-1),
                        count-1, Activity.RESULT_CANCELED, null, "unhandled-back");
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    public ParcelFileDescriptor openContentUri(Uri uri) throws RemoteException {
        String name = uri.getAuthority();
        ContentProviderHolder cph = getContentProviderExternal(name);
        ParcelFileDescriptor pfd = null;
        if (cph != null) {
            sCallerIdentity.set(new Identity(
                    Binder.getCallingPid(), Binder.getCallingUid()));
            try {
                pfd = cph.provider.openFile(uri, "r");
            } catch (FileNotFoundException e) {
            } finally {
                sCallerIdentity.remove();
            }
            removeContentProviderExternal(name);
        } else {
            Slog.d(TAG, "Failed to get provider for authority '" + name + "'");
        }
        return pfd;
    }
    public void goingToSleep() {
        synchronized(this) {
            mSleeping = true;
            mWindowManager.setEventDispatching(false);
            if (mResumedActivity != null) {
                pauseIfSleepingLocked();
            } else {
                Slog.w(TAG, "goingToSleep with no resumed activity!");
            }
        }
    }
    public boolean shutdown(int timeout) {
        if (checkCallingPermission(android.Manifest.permission.SHUTDOWN)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires permission "
                    + android.Manifest.permission.SHUTDOWN);
        }
        boolean timedout = false;
        synchronized(this) {
            mShuttingDown = true;
            mWindowManager.setEventDispatching(false);
            if (mResumedActivity != null) {
                pauseIfSleepingLocked();
                final long endTime = System.currentTimeMillis() + timeout;
                while (mResumedActivity != null || mPausingActivity != null) {
                    long delay = endTime - System.currentTimeMillis();
                    if (delay <= 0) {
                        Slog.w(TAG, "Activity manager shutdown timed out");
                        timedout = true;
                        break;
                    }
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        mUsageStatsService.shutdown();
        mBatteryStatsService.shutdown();
        return timedout;
    }
    void pauseIfSleepingLocked() {
        if (mSleeping || mShuttingDown) {
            if (!mGoingToSleep.isHeld()) {
                mGoingToSleep.acquire();
                if (mLaunchingActivity.isHeld()) {
                    mLaunchingActivity.release();
                    mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
                }
            }
            if (mPausingActivity == null) {
                if (DEBUG_PAUSE) Slog.v(TAG, "Sleep needs to pause...");
                if (DEBUG_USER_LEAVING) Slog.v(TAG, "Sleep => pause with userLeaving=false");
                startPausingLocked(false, true);
            }
        }
    }
    public void wakingUp() {
        synchronized(this) {
            if (mGoingToSleep.isHeld()) {
                mGoingToSleep.release();
            }
            mWindowManager.setEventDispatching(true);
            mSleeping = false;
            resumeTopActivityLocked(null);
        }
    }
    public void stopAppSwitches() {
        if (checkCallingPermission(android.Manifest.permission.STOP_APP_SWITCHES)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires permission "
                    + android.Manifest.permission.STOP_APP_SWITCHES);
        }
        synchronized(this) {
            mAppSwitchesAllowedTime = SystemClock.uptimeMillis()
                    + APP_SWITCH_DELAY_TIME;
            mDidAppSwitch = false;
            mHandler.removeMessages(DO_PENDING_ACTIVITY_LAUNCHES_MSG);
            Message msg = mHandler.obtainMessage(DO_PENDING_ACTIVITY_LAUNCHES_MSG);
            mHandler.sendMessageDelayed(msg, APP_SWITCH_DELAY_TIME);
        }
    }
    public void resumeAppSwitches() {
        if (checkCallingPermission(android.Manifest.permission.STOP_APP_SWITCHES)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires permission "
                    + android.Manifest.permission.STOP_APP_SWITCHES);
        }
        synchronized(this) {
            mAppSwitchesAllowedTime = 0;
        }
    }
    boolean checkAppSwitchAllowedLocked(int callingPid, int callingUid,
            String name) {
        if (mAppSwitchesAllowedTime < SystemClock.uptimeMillis()) {
            return true;
        }
        final int perm = checkComponentPermission(
                android.Manifest.permission.STOP_APP_SWITCHES, callingPid,
                callingUid, -1);
        if (perm == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Slog.w(TAG, name + " request from " + callingUid + " stopped");
        return false;
    }
    public void setDebugApp(String packageName, boolean waitForDebugger,
            boolean persistent) {
        enforceCallingPermission(android.Manifest.permission.SET_DEBUG_APP,
                "setDebugApp()");
        if (persistent) {
            final ContentResolver resolver = mContext.getContentResolver();
            Settings.System.putString(
                resolver, Settings.System.DEBUG_APP,
                packageName);
            Settings.System.putInt(
                resolver, Settings.System.WAIT_FOR_DEBUGGER,
                waitForDebugger ? 1 : 0);
        }
        synchronized (this) {
            if (!persistent) {
                mOrigDebugApp = mDebugApp;
                mOrigWaitForDebugger = mWaitForDebugger;
            }
            mDebugApp = packageName;
            mWaitForDebugger = waitForDebugger;
            mDebugTransient = !persistent;
            if (packageName != null) {
                final long origId = Binder.clearCallingIdentity();
                forceStopPackageLocked(packageName, -1, false, false, true);
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    public void setAlwaysFinish(boolean enabled) {
        enforceCallingPermission(android.Manifest.permission.SET_ALWAYS_FINISH,
                "setAlwaysFinish()");
        Settings.System.putInt(
                mContext.getContentResolver(),
                Settings.System.ALWAYS_FINISH_ACTIVITIES, enabled ? 1 : 0);
        synchronized (this) {
            mAlwaysFinishActivities = enabled;
        }
    }
    public void setActivityController(IActivityController controller) {
        enforceCallingPermission(android.Manifest.permission.SET_ACTIVITY_WATCHER,
                "setActivityController()");
        synchronized (this) {
            mController = controller;
        }
    }
    public boolean isUserAMonkey() {
        synchronized (this) {
            return mController != null;
        }
    }
    public void registerActivityWatcher(IActivityWatcher watcher) {
        synchronized (this) {
            mWatchers.register(watcher);
        }
    }
    public void unregisterActivityWatcher(IActivityWatcher watcher) {
        synchronized (this) {
            mWatchers.unregister(watcher);
        }
    }
    public final void enterSafeMode() {
        synchronized(this) {
            if (!mSystemReady) {
                try {
                    ActivityThread.getPackageManager().enterSafeMode();
                } catch (RemoteException e) {
                }
                View v = LayoutInflater.from(mContext).inflate(
                        com.android.internal.R.layout.safe_mode, null);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
                lp.format = v.getBackground().getOpacity();
                lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                ((WindowManager)mContext.getSystemService(
                        Context.WINDOW_SERVICE)).addView(v, lp);
            }
        }
    }
    public void noteWakeupAlarm(IIntentSender sender) {
        if (!(sender instanceof PendingIntentRecord)) {
            return;
        }
        BatteryStatsImpl stats = mBatteryStatsService.getActiveStatistics();
        synchronized (stats) {
            if (mBatteryStatsService.isOnBattery()) {
                mBatteryStatsService.enforceCallingPermission();
                PendingIntentRecord rec = (PendingIntentRecord)sender;
                int MY_UID = Binder.getCallingUid();
                int uid = rec.uid == MY_UID ? Process.SYSTEM_UID : rec.uid;
                BatteryStatsImpl.Uid.Pkg pkg =
                    stats.getPackageStatsLocked(uid, rec.key.packageName);
                pkg.incWakeupsLocked();
            }
        }
    }
    public boolean killPids(int[] pids, String pReason) {
        if (Binder.getCallingUid() != Process.SYSTEM_UID) {
            throw new SecurityException("killPids only available to the system");
        }
        String reason = (pReason == null) ? "Unknown" : pReason;
        boolean killed = false;
        synchronized (mPidsSelfLocked) {
            int[] types = new int[pids.length];
            int worstType = 0;
            for (int i=0; i<pids.length; i++) {
                ProcessRecord proc = mPidsSelfLocked.get(pids[i]);
                if (proc != null) {
                    int type = proc.setAdj;
                    types[i] = type;
                    if (type > worstType) {
                        worstType = type;
                    }
                }
            }
            if (worstType < EMPTY_APP_ADJ && worstType > HIDDEN_APP_MIN_ADJ) {
                worstType = HIDDEN_APP_MIN_ADJ;
            }
            Slog.w(TAG, "Killing processes " + reason + " at adjustment " + worstType);
            for (int i=0; i<pids.length; i++) {
                ProcessRecord proc = mPidsSelfLocked.get(pids[i]);
                if (proc == null) {
                    continue;
                }
                int adj = proc.setAdj;
                if (adj >= worstType && !proc.killedBackground) {
                    Slog.w(TAG, "Killing " + proc + " (adj " + adj + "): " + reason);
                    EventLog.writeEvent(EventLogTags.AM_KILL, proc.pid,
                            proc.processName, adj, reason);
                    killed = true;
                    proc.killedBackground = true;
                    Process.killProcessQuiet(pids[i]);
                }
            }
        }
        return killed;
    }
    public void reportPss(IApplicationThread caller, int pss) {
        Watchdog.PssRequestor req;
        String name;
        ProcessRecord callerApp;
        synchronized (this) {
            if (caller == null) {
                return;
            }
            callerApp = getRecordForAppLocked(caller);
            if (callerApp == null) {
                return;
            }
            callerApp.lastPss = pss;
            req = callerApp;
            name = callerApp.processName;
        }
        Watchdog.getInstance().reportPss(req, name, pss);
        if (!callerApp.persistent) {
            removeRequestedPss(callerApp);
        }
    }
    public void requestPss(Runnable completeCallback) {
        ArrayList<ProcessRecord> procs;
        synchronized (this) {
            mRequestPssCallback = completeCallback;
            mRequestPssList.clear();
            for (int i=mLruProcesses.size()-1; i>=0; i--) {
                ProcessRecord proc = mLruProcesses.get(i);
                if (!proc.persistent) {
                    mRequestPssList.add(proc);
                }
            }
            procs = new ArrayList<ProcessRecord>(mRequestPssList);
        }
        int oldPri = Process.getThreadPriority(Process.myTid()); 
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        for (int i=procs.size()-1; i>=0; i--) {
            ProcessRecord proc = procs.get(i);
            proc.lastPss = 0;
            proc.requestPss();
        }
        Process.setThreadPriority(oldPri);
    }
    void removeRequestedPss(ProcessRecord proc) {
        Runnable callback = null;
        synchronized (this) {
            if (mRequestPssList.remove(proc)) {
                if (mRequestPssList.size() == 0) {
                    callback = mRequestPssCallback;
                    mRequestPssCallback = null;
                }
            }
        }
        if (callback != null) {
            callback.run();
        }
    }
    public void collectPss(Watchdog.PssStats stats) {
        stats.mEmptyPss = 0;
        stats.mEmptyCount = 0;
        stats.mBackgroundPss = 0;
        stats.mBackgroundCount = 0;
        stats.mServicePss = 0;
        stats.mServiceCount = 0;
        stats.mVisiblePss = 0;
        stats.mVisibleCount = 0;
        stats.mForegroundPss = 0;
        stats.mForegroundCount = 0;
        stats.mNoPssCount = 0;
        synchronized (this) {
            int i;
            int NPD = mProcDeaths.length < stats.mProcDeaths.length
                    ? mProcDeaths.length : stats.mProcDeaths.length;
            int aggr = 0;
            for (i=0; i<NPD; i++) {
                aggr += mProcDeaths[i];
                stats.mProcDeaths[i] = aggr;
            }
            while (i<stats.mProcDeaths.length) {
                stats.mProcDeaths[i] = 0;
                i++;
            }
            for (i=mLruProcesses.size()-1; i>=0; i--) {
                ProcessRecord proc = mLruProcesses.get(i);
                if (proc.persistent) {
                    continue;
                }
                if (proc.lastPss == 0) {
                    stats.mNoPssCount++;
                    continue;
                }
                if (proc.setAdj >= HIDDEN_APP_MIN_ADJ) {
                    if (proc.empty) {
                        stats.mEmptyPss += proc.lastPss;
                        stats.mEmptyCount++;
                    } else {
                        stats.mBackgroundPss += proc.lastPss;
                        stats.mBackgroundCount++;
                    }
                } else if (proc.setAdj >= VISIBLE_APP_ADJ) {
                    stats.mVisiblePss += proc.lastPss;
                    stats.mVisibleCount++;
                } else {
                    stats.mForegroundPss += proc.lastPss;
                    stats.mForegroundCount++;
                }
            }
        }
    }
    public final void startRunning(String pkg, String cls, String action,
            String data) {
        synchronized(this) {
            if (mStartRunning) {
                return;
            }
            mStartRunning = true;
            mTopComponent = pkg != null && cls != null
                    ? new ComponentName(pkg, cls) : null;
            mTopAction = action != null ? action : Intent.ACTION_MAIN;
            mTopData = data;
            if (!mSystemReady) {
                return;
            }
        }
        systemReady(null);
    }
    private void retrieveSettings() {
        final ContentResolver resolver = mContext.getContentResolver();
        String debugApp = Settings.System.getString(
            resolver, Settings.System.DEBUG_APP);
        boolean waitForDebugger = Settings.System.getInt(
            resolver, Settings.System.WAIT_FOR_DEBUGGER, 0) != 0;
        boolean alwaysFinishActivities = Settings.System.getInt(
            resolver, Settings.System.ALWAYS_FINISH_ACTIVITIES, 0) != 0;
        Configuration configuration = new Configuration();
        Settings.System.getConfiguration(resolver, configuration);
        synchronized (this) {
            mDebugApp = mOrigDebugApp = debugApp;
            mWaitForDebugger = mOrigWaitForDebugger = waitForDebugger;
            mAlwaysFinishActivities = alwaysFinishActivities;
            mConfiguration.updateFrom(configuration);
            mConfigurationSeq = mConfiguration.seq = 1;
            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Initial config: " + mConfiguration);
        }
    }
    public boolean testIsSystemReady() {
        return mSystemReady;
    }
    public void systemReady(final Runnable goingCallback) {
        if (!Process.supportsProcesses()) {
            mStartRunning = true;
            mTopAction = Intent.ACTION_MAIN;
        }
        synchronized(this) {
            if (mSystemReady) {
                if (goingCallback != null) goingCallback.run();
                return;
            }
            if (!mDidUpdate) {
                if (mWaitingUpdate) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_PRE_BOOT_COMPLETED);
                List<ResolveInfo> ris = null;
                try {
                    ris = ActivityThread.getPackageManager().queryIntentReceivers(
                                intent, null, 0);
                } catch (RemoteException e) {
                }
                if (ris != null) {
                    for (int i=ris.size()-1; i>=0; i--) {
                        if ((ris.get(i).activityInfo.applicationInfo.flags
                                &ApplicationInfo.FLAG_SYSTEM) == 0) {
                            ris.remove(i);
                        }
                    }
                    intent.addFlags(Intent.FLAG_RECEIVER_BOOT_UPGRADE);
                    for (int i=0; i<ris.size(); i++) {
                        ActivityInfo ai = ris.get(i).activityInfo;
                        intent.setComponent(new ComponentName(ai.packageName, ai.name));
                        IIntentReceiver finisher = null;
                        if (i == ris.size()-1) {
                            finisher = new IIntentReceiver.Stub() {
                                public void performReceive(Intent intent, int resultCode,
                                        String data, Bundle extras, boolean ordered,
                                        boolean sticky)
                                        throws RemoteException {
                                    synchronized (ActivityManagerService.this) {
                                        mDidUpdate = true;
                                    }
                                    systemReady(goingCallback);
                                }
                            };
                        }
                        Slog.i(TAG, "Sending system update to: " + intent.getComponent());
                        broadcastIntentLocked(null, null, intent, null, finisher,
                                0, null, null, null, true, false, MY_PID, Process.SYSTEM_UID);
                        if (finisher != null) {
                            mWaitingUpdate = true;
                        }
                    }
                }
                if (mWaitingUpdate) {
                    return;
                }
                mDidUpdate = true;
            }
            mSystemReady = true;
            if (!mStartRunning) {
                return;
            }
        }
        ArrayList<ProcessRecord> procsToKill = null;
        synchronized(mPidsSelfLocked) {
            for (int i=mPidsSelfLocked.size()-1; i>=0; i--) {
                ProcessRecord proc = mPidsSelfLocked.valueAt(i);
                if (!isAllowedWhileBooting(proc.info)){
                    if (procsToKill == null) {
                        procsToKill = new ArrayList<ProcessRecord>();
                    }
                    procsToKill.add(proc);
                }
            }
        }
        if (procsToKill != null) {
            synchronized(this) {
                for (int i=procsToKill.size()-1; i>=0; i--) {
                    ProcessRecord proc = procsToKill.get(i);
                    Slog.i(TAG, "Removing system update proc: " + proc);
                    removeProcessLocked(proc, true);
                }
            }
        }
        Slog.i(TAG, "System now ready");
        EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_AMS_READY,
            SystemClock.uptimeMillis());
        synchronized(this) {
            if (mFactoryTest == SystemServer.FACTORY_TEST_LOW_LEVEL) {
                ResolveInfo ri = mContext.getPackageManager()
                        .resolveActivity(new Intent(Intent.ACTION_FACTORY_TEST),
                                STOCK_PM_FLAGS);
                CharSequence errorMsg = null;
                if (ri != null) {
                    ActivityInfo ai = ri.activityInfo;
                    ApplicationInfo app = ai.applicationInfo;
                    if ((app.flags&ApplicationInfo.FLAG_SYSTEM) != 0) {
                        mTopAction = Intent.ACTION_FACTORY_TEST;
                        mTopData = null;
                        mTopComponent = new ComponentName(app.packageName,
                                ai.name);
                    } else {
                        errorMsg = mContext.getResources().getText(
                                com.android.internal.R.string.factorytest_not_system);
                    }
                } else {
                    errorMsg = mContext.getResources().getText(
                            com.android.internal.R.string.factorytest_no_action);
                }
                if (errorMsg != null) {
                    mTopAction = null;
                    mTopData = null;
                    mTopComponent = null;
                    Message msg = Message.obtain();
                    msg.what = SHOW_FACTORY_ERROR_MSG;
                    msg.getData().putCharSequence("msg", errorMsg);
                    mHandler.sendMessage(msg);
                }
            }
        }
        retrieveSettings();
        if (goingCallback != null) goingCallback.run();
        synchronized (this) {
            if (mFactoryTest != SystemServer.FACTORY_TEST_LOW_LEVEL) {
                try {
                    List apps = ActivityThread.getPackageManager().
                        getPersistentApplications(STOCK_PM_FLAGS);
                    if (apps != null) {
                        int N = apps.size();
                        int i;
                        for (i=0; i<N; i++) {
                            ApplicationInfo info
                                = (ApplicationInfo)apps.get(i);
                            if (info != null &&
                                    !info.packageName.equals("android")) {
                                addAppLocked(info);
                            }
                        }
                    }
                } catch (RemoteException ex) {
                }
            }
            mBooting = true;
            try {
                if (ActivityThread.getPackageManager().hasSystemUidErrors()) {
                    Message msg = Message.obtain();
                    msg.what = SHOW_UID_ERROR_MSG;
                    mHandler.sendMessage(msg);
                }
            } catch (RemoteException e) {
            }
            resumeTopActivityLocked(null);
        }
    }
    private boolean makeAppCrashingLocked(ProcessRecord app,
            String shortMsg, String longMsg, String stackTrace) {
        app.crashing = true;
        app.crashingReport = generateProcessError(app,
                ActivityManager.ProcessErrorStateInfo.CRASHED, null, shortMsg, longMsg, stackTrace);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
        return handleAppCrashLocked(app);
    }
    private void makeAppNotRespondingLocked(ProcessRecord app,
            String activity, String shortMsg, String longMsg) {
        app.notResponding = true;
        app.notRespondingReport = generateProcessError(app,
                ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING,
                activity, shortMsg, longMsg, null);
        startAppProblemLocked(app);
        app.stopFreezingAllLocked();
    }
    private ActivityManager.ProcessErrorStateInfo generateProcessError(ProcessRecord app, 
            int condition, String activity, String shortMsg, String longMsg, String stackTrace) {
        ActivityManager.ProcessErrorStateInfo report = new ActivityManager.ProcessErrorStateInfo();
        report.condition = condition;
        report.processName = app.processName;
        report.pid = app.pid;
        report.uid = app.info.uid;
        report.tag = activity;
        report.shortMsg = shortMsg;
        report.longMsg = longMsg;
        report.stackTrace = stackTrace;
        return report;
    }
    void killAppAtUsersRequest(ProcessRecord app, Dialog fromDialog) {
        synchronized (this) {
            app.crashing = false;
            app.crashingReport = null;
            app.notResponding = false;
            app.notRespondingReport = null;
            if (app.anrDialog == fromDialog) {
                app.anrDialog = null;
            }
            if (app.waitDialog == fromDialog) {
                app.waitDialog = null;
            }
            if (app.pid > 0 && app.pid != MY_PID) {
                handleAppCrashLocked(app);
                Slog.i(ActivityManagerService.TAG, "Killing "
                        + app.processName + " (pid=" + app.pid + "): user's request");
                EventLog.writeEvent(EventLogTags.AM_KILL, app.pid,
                        app.processName, app.setAdj, "user's request after error");
                Process.killProcess(app.pid);
            }
        }
    }
    private boolean handleAppCrashLocked(ProcessRecord app) {
        long now = SystemClock.uptimeMillis();
        Long crashTime = mProcessCrashTimes.get(app.info.processName,
                app.info.uid);
        if (crashTime != null && now < crashTime+MIN_CRASH_INTERVAL) {
            Slog.w(TAG, "Process " + app.info.processName
                    + " has crashed too many times: killing!");
            EventLog.writeEvent(EventLogTags.AM_PROCESS_CRASHED_TOO_MUCH,
                    app.info.processName, app.info.uid);
            killServicesLocked(app, false);
            for (int i=mHistory.size()-1; i>=0; i--) {
                HistoryRecord r = (HistoryRecord)mHistory.get(i);
                if (r.app == app) {
                    Slog.w(TAG, "  Force finishing activity "
                        + r.intent.getComponent().flattenToShortString());
                    finishActivityLocked(r, i, Activity.RESULT_CANCELED, null, "crashed");
                }
            }
            if (!app.persistent) {
                EventLog.writeEvent(EventLogTags.AM_PROC_BAD, app.info.uid,
                        app.info.processName);
                mBadProcesses.put(app.info.processName, app.info.uid, now);
                app.bad = true;
                mProcessCrashTimes.remove(app.info.processName, app.info.uid);
                app.removed = true;
                removeProcessLocked(app, false);
                return false;
            }
        } else {
            HistoryRecord r = topRunningActivityLocked(null);
            if (r.app == app) {
                Slog.w(TAG, "  Force finishing activity "
                        + r.intent.getComponent().flattenToShortString());
                int index = indexOfTokenLocked(r);
                finishActivityLocked(r, index,
                        Activity.RESULT_CANCELED, null, "crashed");
                index--;
                if (index >= 0) {
                    r = (HistoryRecord)mHistory.get(index);
                    if (r.state == ActivityState.RESUMED
                            || r.state == ActivityState.PAUSING
                            || r.state == ActivityState.PAUSED) {
                        if (!r.isHomeActivity) {
                            Slog.w(TAG, "  Force finishing activity "
                                    + r.intent.getComponent().flattenToShortString());
                            finishActivityLocked(r, index,
                                    Activity.RESULT_CANCELED, null, "crashed");
                        }
                    }
                }
            }
        }
        if (app.services.size() != 0) {
            Iterator it = app.services.iterator();
            while (it.hasNext()) {
                ServiceRecord sr = (ServiceRecord)it.next();
                sr.crashCount++;
            }
        }
        mProcessCrashTimes.put(app.info.processName, app.info.uid, now);
        return true;
    }
    void startAppProblemLocked(ProcessRecord app) {
        app.errorReportReceiver = ApplicationErrorReport.getErrorReportReceiver(
                mContext, app.info.packageName, app.info.flags);
        skipCurrentReceiverLocked(app);
    }
    void skipCurrentReceiverLocked(ProcessRecord app) {
        boolean reschedule = false;
        BroadcastRecord r = app.curReceiver;
        if (r != null) {
            logBroadcastReceiverDiscard(r);
            finishReceiverLocked(r.receiver, r.resultCode, r.resultData,
                    r.resultExtras, r.resultAbort, true);
            reschedule = true;
        }
        r = mPendingBroadcast;
        if (r != null && r.curApp == app) {
            if (DEBUG_BROADCAST) Slog.v(TAG,
                    "skip & discard pending app " + r);
            logBroadcastReceiverDiscard(r);
            finishReceiverLocked(r.receiver, r.resultCode, r.resultData,
                    r.resultExtras, r.resultAbort, true);
            reschedule = true;
        }
        if (reschedule) {
            scheduleBroadcastsLocked();
        }
    }
    public void handleApplicationCrash(IBinder app, ApplicationErrorReport.CrashInfo crashInfo) {
        ProcessRecord r = findAppProcess(app);
        EventLog.writeEvent(EventLogTags.AM_CRASH, Binder.getCallingPid(),
                app == null ? "system" : (r == null ? "unknown" : r.processName),
                r == null ? -1 : r.info.flags,
                crashInfo.exceptionClassName,
                crashInfo.exceptionMessage,
                crashInfo.throwFileName,
                crashInfo.throwLineNumber);
        addErrorToDropBox("crash", r, null, null, null, null, null, crashInfo);
        crashApplication(r, crashInfo);
    }
    public boolean handleApplicationWtf(IBinder app, String tag,
            ApplicationErrorReport.CrashInfo crashInfo) {
        ProcessRecord r = findAppProcess(app);
        EventLog.writeEvent(EventLogTags.AM_WTF, Binder.getCallingPid(),
                app == null ? "system" : (r == null ? "unknown" : r.processName),
                r == null ? -1 : r.info.flags,
                tag, crashInfo.exceptionMessage);
        addErrorToDropBox("wtf", r, null, null, tag, null, null, crashInfo);
        if (Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.WTF_IS_FATAL, 0) != 0) {
            crashApplication(r, crashInfo);
            return true;
        } else {
            return false;
        }
    }
    private ProcessRecord findAppProcess(IBinder app) {
        if (app == null) {
            return null;
        }
        synchronized (this) {
            for (SparseArray<ProcessRecord> apps : mProcessNames.getMap().values()) {
                final int NA = apps.size();
                for (int ia=0; ia<NA; ia++) {
                    ProcessRecord p = apps.valueAt(ia);
                    if (p.thread != null && p.thread.asBinder() == app) {
                        return p;
                    }
                }
            }
            Slog.w(TAG, "Can't find mystery application: " + app);
            return null;
        }
    }
    public void addErrorToDropBox(String eventType,
            ProcessRecord process, HistoryRecord activity, HistoryRecord parent, String subject,
            final String report, final File logFile,
            final ApplicationErrorReport.CrashInfo crashInfo) {
        String prefix;
        if (process == null || process.pid == MY_PID) {
            prefix = "system_server_";
        } else if ((process.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            prefix = "system_app_";
        } else {
            prefix = "data_app_";
        }
        final String dropboxTag = prefix + eventType;
        final DropBoxManager dbox = (DropBoxManager)
                mContext.getSystemService(Context.DROPBOX_SERVICE);
        if (dbox == null || !dbox.isTagEnabled(dropboxTag)) return;
        final StringBuilder sb = new StringBuilder(1024);
        if (process == null || process.pid == MY_PID) {
            sb.append("Process: system_server\n");
        } else {
            sb.append("Process: ").append(process.processName).append("\n");
        }
        if (process != null) {
            int flags = process.info.flags;
            IPackageManager pm = ActivityThread.getPackageManager();
            sb.append("Flags: 0x").append(Integer.toString(flags, 16)).append("\n");
            for (String pkg : process.pkgList) {
                sb.append("Package: ").append(pkg);
                try {
                    PackageInfo pi = pm.getPackageInfo(pkg, 0);
                    if (pi != null) {
                        sb.append(" v").append(pi.versionCode);
                        if (pi.versionName != null) {
                            sb.append(" (").append(pi.versionName).append(")");
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error getting package info: " + pkg, e);
                }
                sb.append("\n");
            }
        }
        if (activity != null) {
            sb.append("Activity: ").append(activity.shortComponentName).append("\n");
        }
        if (parent != null && parent.app != null && parent.app.pid != process.pid) {
            sb.append("Parent-Process: ").append(parent.app.processName).append("\n");
        }
        if (parent != null && parent != activity) {
            sb.append("Parent-Activity: ").append(parent.shortComponentName).append("\n");
        }
        if (subject != null) {
            sb.append("Subject: ").append(subject).append("\n");
        }
        sb.append("Build: ").append(Build.FINGERPRINT).append("\n");
        sb.append("\n");
        Thread worker = new Thread("Error dump: " + dropboxTag) {
            @Override
            public void run() {
                if (report != null) {
                    sb.append(report);
                }
                if (logFile != null) {
                    try {
                        sb.append(FileUtils.readTextFile(logFile, 128 * 1024, "\n\n[[TRUNCATED]]"));
                    } catch (IOException e) {
                        Slog.e(TAG, "Error reading " + logFile, e);
                    }
                }
                if (crashInfo != null && crashInfo.stackTrace != null) {
                    sb.append(crashInfo.stackTrace);
                }
                String setting = Settings.Secure.ERROR_LOGCAT_PREFIX + dropboxTag;
                int lines = Settings.Secure.getInt(mContext.getContentResolver(), setting, 0);
                if (lines > 0) {
                    sb.append("\n");
                    InputStreamReader input = null;
                    try {
                        java.lang.Process logcat = new ProcessBuilder("/system/bin/logcat",
                                "-v", "time", "-b", "events", "-b", "system", "-b", "main",
                                "-t", String.valueOf(lines)).redirectErrorStream(true).start();
                        try { logcat.getOutputStream().close(); } catch (IOException e) {}
                        try { logcat.getErrorStream().close(); } catch (IOException e) {}
                        input = new InputStreamReader(logcat.getInputStream());
                        int num;
                        char[] buf = new char[8192];
                        while ((num = input.read(buf)) > 0) sb.append(buf, 0, num);
                    } catch (IOException e) {
                        Slog.e(TAG, "Error running logcat", e);
                    } finally {
                        if (input != null) try { input.close(); } catch (IOException e) {}
                    }
                }
                dbox.addText(dropboxTag, sb.toString());
            }
        };
        if (process == null || process.pid == MY_PID) {
            worker.run();  
        } else {
            worker.start();
        }
    }
    private void crashApplication(ProcessRecord r, ApplicationErrorReport.CrashInfo crashInfo) {
        long timeMillis = System.currentTimeMillis();
        String shortMsg = crashInfo.exceptionClassName;
        String longMsg = crashInfo.exceptionMessage;
        String stackTrace = crashInfo.stackTrace;
        if (shortMsg != null && longMsg != null) {
            longMsg = shortMsg + ": " + longMsg;
        } else if (shortMsg != null) {
            longMsg = shortMsg;
        }
        AppErrorResult result = new AppErrorResult();
        synchronized (this) {
            if (mController != null) {
                try {
                    String name = r != null ? r.processName : null;
                    int pid = r != null ? r.pid : Binder.getCallingPid();
                    if (!mController.appCrashed(name, pid,
                            shortMsg, longMsg, timeMillis, crashInfo.stackTrace)) {
                        Slog.w(TAG, "Force-killing crashed app " + name
                                + " at watcher's request");
                        Process.killProcess(pid);
                        return;
                    }
                } catch (RemoteException e) {
                    mController = null;
                }
            }
            final long origId = Binder.clearCallingIdentity();
            if (r != null && r.instrumentationClass != null) {
                Slog.w(TAG, "Error in app " + r.processName
                      + " running instrumentation " + r.instrumentationClass + ":");
                if (shortMsg != null) Slog.w(TAG, "  " + shortMsg);
                if (longMsg != null) Slog.w(TAG, "  " + longMsg);
                Bundle info = new Bundle();
                info.putString("shortMsg", shortMsg);
                info.putString("longMsg", longMsg);
                finishInstrumentationLocked(r, Activity.RESULT_CANCELED, info);
                Binder.restoreCallingIdentity(origId);
                return;
            }
            if (r == null || !makeAppCrashingLocked(r, shortMsg, longMsg, stackTrace)) {
                Binder.restoreCallingIdentity(origId);
                return;
            }
            Message msg = Message.obtain();
            msg.what = SHOW_ERROR_MSG;
            HashMap data = new HashMap();
            data.put("result", result);
            data.put("app", r);
            msg.obj = data;
            mHandler.sendMessage(msg);
            Binder.restoreCallingIdentity(origId);
        }
        int res = result.get();
        Intent appErrorIntent = null;
        synchronized (this) {
            if (r != null) {
                mProcessCrashTimes.put(r.info.processName, r.info.uid,
                        SystemClock.uptimeMillis());
            }
            if (res == AppErrorDialog.FORCE_QUIT_AND_REPORT) {
                appErrorIntent = createAppErrorIntentLocked(r, timeMillis, crashInfo);
            }
        }
        if (appErrorIntent != null) {
            try {
                mContext.startActivity(appErrorIntent);
            } catch (ActivityNotFoundException e) {
                Slog.w(TAG, "bug report receiver dissappeared", e);
            }
        }
    }
    Intent createAppErrorIntentLocked(ProcessRecord r,
            long timeMillis, ApplicationErrorReport.CrashInfo crashInfo) {
        ApplicationErrorReport report = createAppErrorReportLocked(r, timeMillis, crashInfo);
        if (report == null) {
            return null;
        }
        Intent result = new Intent(Intent.ACTION_APP_ERROR);
        result.setComponent(r.errorReportReceiver);
        result.putExtra(Intent.EXTRA_BUG_REPORT, report);
        result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return result;
    }
    private ApplicationErrorReport createAppErrorReportLocked(ProcessRecord r,
            long timeMillis, ApplicationErrorReport.CrashInfo crashInfo) {
        if (r.errorReportReceiver == null) {
            return null;
        }
        if (!r.crashing && !r.notResponding) {
            return null;
        }
        ApplicationErrorReport report = new ApplicationErrorReport();
        report.packageName = r.info.packageName;
        report.installerPackageName = r.errorReportReceiver.getPackageName();
        report.processName = r.processName;
        report.time = timeMillis;
        report.systemApp = (r.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        if (r.crashing) {
            report.type = ApplicationErrorReport.TYPE_CRASH;
            report.crashInfo = crashInfo;
        } else if (r.notResponding) {
            report.type = ApplicationErrorReport.TYPE_ANR;
            report.anrInfo = new ApplicationErrorReport.AnrInfo();
            report.anrInfo.activity = r.notRespondingReport.tag;
            report.anrInfo.cause = r.notRespondingReport.shortMsg;
            report.anrInfo.info = r.notRespondingReport.longMsg;
        }
        return report;
    }
    public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() {
        List<ActivityManager.ProcessErrorStateInfo> errList = null;
        synchronized (this) {
            for (int i=mLruProcesses.size()-1; i>=0; i--) {
                ProcessRecord app = mLruProcesses.get(i);
                if ((app.thread != null) && (app.crashing || app.notResponding)) {
                    ActivityManager.ProcessErrorStateInfo report = null;
                    if (app.crashing) {
                        report = app.crashingReport;
                    } else if (app.notResponding) {
                        report = app.notRespondingReport;
                    }
                    if (report != null) {
                        if (errList == null) {
                            errList = new ArrayList<ActivityManager.ProcessErrorStateInfo>(1);
                        }
                        errList.add(report);
                    } else {
                        Slog.w(TAG, "Missing app error report, app = " + app.processName + 
                                " crashing = " + app.crashing +
                                " notResponding = " + app.notResponding);
                    }
                }
            }
        }
        return errList;
    }
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        List<ActivityManager.RunningAppProcessInfo> runList = null;
        synchronized (this) {
            for (int i=mLruProcesses.size()-1; i>=0; i--) {
                ProcessRecord app = mLruProcesses.get(i);
                if ((app.thread != null) && (!app.crashing && !app.notResponding)) {
                    ActivityManager.RunningAppProcessInfo currApp = 
                        new ActivityManager.RunningAppProcessInfo(app.processName,
                                app.pid, app.getPackageList());
                    currApp.uid = app.info.uid;
                    int adj = app.curAdj;
                    if (adj >= EMPTY_APP_ADJ) {
                        currApp.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY;
                    } else if (adj >= HIDDEN_APP_MIN_ADJ) {
                        currApp.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
                        currApp.lru = adj - HIDDEN_APP_MIN_ADJ + 1;
                    } else if (adj >= HOME_APP_ADJ) {
                        currApp.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
                        currApp.lru = 0;
                    } else if (adj >= SECONDARY_SERVER_ADJ) {
                        currApp.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE;
                    } else if (adj >= VISIBLE_APP_ADJ) {
                        currApp.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
                    } else {
                        currApp.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
                    }
                    currApp.importanceReasonCode = app.adjTypeCode;
                    if (app.adjSource instanceof ProcessRecord) {
                        currApp.importanceReasonPid = ((ProcessRecord)app.adjSource).pid;
                    } else if (app.adjSource instanceof HistoryRecord) {
                        HistoryRecord r = (HistoryRecord)app.adjSource;
                        if (r.app != null) currApp.importanceReasonPid = r.app.pid;
                    }
                    if (app.adjTarget instanceof ComponentName) {
                        currApp.importanceReasonComponent = (ComponentName)app.adjTarget;
                    }
                    if (runList == null) {
                        runList = new ArrayList<ActivityManager.RunningAppProcessInfo>();
                    }
                    runList.add(currApp);
                }
            }
        }
        return runList;
    }
    public List<ApplicationInfo> getRunningExternalApplications() {
        List<ActivityManager.RunningAppProcessInfo> runningApps = getRunningAppProcesses();
        List<ApplicationInfo> retList = new ArrayList<ApplicationInfo>();
        if (runningApps != null && runningApps.size() > 0) {
            Set<String> extList = new HashSet<String>();
            for (ActivityManager.RunningAppProcessInfo app : runningApps) {
                if (app.pkgList != null) {
                    for (String pkg : app.pkgList) {
                        extList.add(pkg);
                    }
                }
            }
            IPackageManager pm = ActivityThread.getPackageManager();
            for (String pkg : extList) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                    if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        retList.add(info);
                    }
                } catch (RemoteException e) {
                }
            }
        }
        return retList;
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump ActivityManager from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid()
                    + " without permission "
                    + android.Manifest.permission.DUMP);
            return;
        }
        boolean dumpAll = false;
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if ("-a".equals(opt)) {
                dumpAll = true;
            } else if ("-h".equals(opt)) {
                pw.println("Activity manager dump options:");
                pw.println("  [-a] [-h] [cmd] ...");
                pw.println("  cmd may be one of:");
                pw.println("    activities: activity stack state");
                pw.println("    broadcasts: broadcast state");
                pw.println("    intents: pending intent state");
                pw.println("    processes: process state");
                pw.println("    providers: content provider state");
                pw.println("    services: service state");
                pw.println("    service [name]: service client-side state");
                return;
            } else {
                pw.println("Unknown argument: " + opt + "; use -h for help");
            }
        }
        if (opti < args.length) {
            String cmd = args[opti];
            opti++;
            if ("activities".equals(cmd) || "a".equals(cmd)) {
                synchronized (this) {
                    dumpActivitiesLocked(fd, pw, args, opti, true, true);
                }
                return;
            } else if ("broadcasts".equals(cmd) || "b".equals(cmd)) {
                synchronized (this) {
                    dumpBroadcastsLocked(fd, pw, args, opti, true);
                }
                return;
            } else if ("intents".equals(cmd) || "i".equals(cmd)) {
                synchronized (this) {
                    dumpPendingIntentsLocked(fd, pw, args, opti, true);
                }
                return;
            } else if ("processes".equals(cmd) || "p".equals(cmd)) {
                synchronized (this) {
                    dumpProcessesLocked(fd, pw, args, opti, true);
                }
                return;
            } else if ("providers".equals(cmd) || "prov".equals(cmd)) {
                synchronized (this) {
                    dumpProvidersLocked(fd, pw, args, opti, true);
                }
                return;
            } else if ("service".equals(cmd)) {
                dumpService(fd, pw, args, opti, true);
                return;
            } else if ("services".equals(cmd) || "s".equals(cmd)) {
                synchronized (this) {
                    dumpServicesLocked(fd, pw, args, opti, true);
                }
                return;
            }
        }
        synchronized (this) {
            boolean needSep;
            if (dumpAll) {
                pw.println("Providers in Current Activity Manager State:");
            }
            needSep = dumpProvidersLocked(fd, pw, args, opti, dumpAll);
            if (needSep) {
                pw.println(" ");
            }
            if (dumpAll) {
                pw.println("-------------------------------------------------------------------------------");
                pw.println("Broadcasts in Current Activity Manager State:");
            }
            needSep = dumpBroadcastsLocked(fd, pw, args, opti, dumpAll);
            if (needSep) {
                pw.println(" ");
            }
            if (dumpAll) {
                pw.println("-------------------------------------------------------------------------------");
                pw.println("Services in Current Activity Manager State:");
            }
            needSep = dumpServicesLocked(fd, pw, args, opti, dumpAll);
            if (needSep) {
                pw.println(" ");
            }
            if (dumpAll) {
                pw.println("-------------------------------------------------------------------------------");
                pw.println("PendingIntents in Current Activity Manager State:");
            }
            needSep = dumpPendingIntentsLocked(fd, pw, args, opti, dumpAll);
            if (needSep) {
                pw.println(" ");
            }
            if (dumpAll) {
                pw.println("-------------------------------------------------------------------------------");
                pw.println("Activities in Current Activity Manager State:");
            }
            needSep = dumpActivitiesLocked(fd, pw, args, opti, dumpAll, !dumpAll);
            if (needSep) {
                pw.println(" ");
            }
            if (dumpAll) {
                pw.println("-------------------------------------------------------------------------------");
                pw.println("Processes in Current Activity Manager State:");
            }
            dumpProcessesLocked(fd, pw, args, opti, dumpAll);
        }
    }
    boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, String[] args,
            int opti, boolean dumpAll, boolean needHeader) {
        if (needHeader) {
            pw.println("  Activity stack:");
        }
        dumpHistoryList(pw, mHistory, "  ", "Hist", true);
        pw.println(" ");
        pw.println("  Running activities (most recent first):");
        dumpHistoryList(pw, mLRUActivities, "  ", "Run", false);
        if (mWaitingVisibleActivities.size() > 0) {
            pw.println(" ");
            pw.println("  Activities waiting for another to become visible:");
            dumpHistoryList(pw, mWaitingVisibleActivities, "  ", "Wait", false);
        }
        if (mStoppingActivities.size() > 0) {
            pw.println(" ");
            pw.println("  Activities waiting to stop:");
            dumpHistoryList(pw, mStoppingActivities, "  ", "Stop", false);
        }
        if (mFinishingActivities.size() > 0) {
            pw.println(" ");
            pw.println("  Activities waiting to finish:");
            dumpHistoryList(pw, mFinishingActivities, "  ", "Fin", false);
        }
        pw.println(" ");
        pw.println("  mPausingActivity: " + mPausingActivity);
        pw.println("  mResumedActivity: " + mResumedActivity);
        pw.println("  mFocusedActivity: " + mFocusedActivity);
        pw.println("  mLastPausedActivity: " + mLastPausedActivity);
        if (dumpAll && mRecentTasks.size() > 0) {
            pw.println(" ");
            pw.println("Recent tasks in Current Activity Manager State:");
            final int N = mRecentTasks.size();
            for (int i=0; i<N; i++) {
                TaskRecord tr = mRecentTasks.get(i);
                pw.print("  * Recent #"); pw.print(i); pw.print(": ");
                        pw.println(tr);
                mRecentTasks.get(i).dump(pw, "    ");
            }
        }
        pw.println(" ");
        pw.println("  mCurTask: " + mCurTask);
        return true;
    }
    boolean dumpProcessesLocked(FileDescriptor fd, PrintWriter pw, String[] args,
            int opti, boolean dumpAll) {
        boolean needSep = false;
        int numPers = 0;
        if (dumpAll) {
            for (SparseArray<ProcessRecord> procs : mProcessNames.getMap().values()) {
                final int NA = procs.size();
                for (int ia=0; ia<NA; ia++) {
                    if (!needSep) {
                        pw.println("  All known processes:");
                        needSep = true;
                    }
                    ProcessRecord r = procs.valueAt(ia);
                    pw.print(r.persistent ? "  *PERS*" : "  *APP*");
                        pw.print(" UID "); pw.print(procs.keyAt(ia));
                        pw.print(" "); pw.println(r);
                    r.dump(pw, "    ");
                    if (r.persistent) {
                        numPers++;
                    }
                }
            }
        }
        if (mLruProcesses.size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Running processes (most recent first):");
            dumpProcessList(pw, this, mLruProcesses, "    ",
                    "App ", "PERS", true);
            needSep = true;
        }
        synchronized (mPidsSelfLocked) {
            if (mPidsSelfLocked.size() > 0) {
                if (needSep) pw.println(" ");
                needSep = true;
                pw.println("  PID mappings:");
                for (int i=0; i<mPidsSelfLocked.size(); i++) {
                    pw.print("    PID #"); pw.print(mPidsSelfLocked.keyAt(i));
                        pw.print(": "); pw.println(mPidsSelfLocked.valueAt(i));
                }
            }
        }
        if (mForegroundProcesses.size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Foreground Processes:");
            for (int i=0; i<mForegroundProcesses.size(); i++) {
                pw.print("    PID #"); pw.print(mForegroundProcesses.keyAt(i));
                        pw.print(": "); pw.println(mForegroundProcesses.valueAt(i));
            }
        }
        if (mPersistentStartingProcesses.size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Persisent processes that are starting:");
            dumpProcessList(pw, this, mPersistentStartingProcesses, "    ",
                    "Starting Norm", "Restarting PERS", false);
        }
        if (mStartingProcesses.size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Processes that are starting:");
            dumpProcessList(pw, this, mStartingProcesses, "    ",
                    "Starting Norm", "Starting PERS", false);
        }
        if (mRemovedProcesses.size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Processes that are being removed:");
            dumpProcessList(pw, this, mRemovedProcesses, "    ",
                    "Removed Norm", "Removed PERS", false);
        }
        if (mProcessesOnHold.size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Processes that are on old until the system is ready:");
            dumpProcessList(pw, this, mProcessesOnHold, "    ",
                    "OnHold Norm", "OnHold PERS", false);
        }
        if (mProcessesToGc.size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Processes that are waiting to GC:");
            long now = SystemClock.uptimeMillis();
            for (int i=0; i<mProcessesToGc.size(); i++) {
                ProcessRecord proc = mProcessesToGc.get(i);
                pw.print("    Process "); pw.println(proc);
                pw.print("      lowMem="); pw.print(proc.reportLowMemory);
                        pw.print(", last gced=");
                        pw.print(now-proc.lastRequestedGc);
                        pw.print(" ms ago, last lowMem=");
                        pw.print(now-proc.lastLowMemory);
                        pw.println(" ms ago");
            }
        }
        if (mProcessCrashTimes.getMap().size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Time since processes crashed:");
            long now = SystemClock.uptimeMillis();
            for (Map.Entry<String, SparseArray<Long>> procs
                    : mProcessCrashTimes.getMap().entrySet()) {
                SparseArray<Long> uids = procs.getValue();
                final int N = uids.size();
                for (int i=0; i<N; i++) {
                    pw.print("    Process "); pw.print(procs.getKey());
                            pw.print(" uid "); pw.print(uids.keyAt(i));
                            pw.print(": last crashed ");
                            pw.print((now-uids.valueAt(i)));
                            pw.println(" ms ago");
                }
            }
        }
        if (mBadProcesses.getMap().size() > 0) {
            if (needSep) pw.println(" ");
            needSep = true;
            pw.println("  Bad processes:");
            for (Map.Entry<String, SparseArray<Long>> procs
                    : mBadProcesses.getMap().entrySet()) {
                SparseArray<Long> uids = procs.getValue();
                final int N = uids.size();
                for (int i=0; i<N; i++) {
                    pw.print("    Bad process "); pw.print(procs.getKey());
                            pw.print(" uid "); pw.print(uids.keyAt(i));
                            pw.print(": crashed at time ");
                            pw.println(uids.valueAt(i));
                }
            }
        }
        pw.println(" ");
        pw.println("  mHomeProcess: " + mHomeProcess);
        pw.println("  mConfiguration: " + mConfiguration);
        pw.println("  mConfigWillChange: " + mConfigWillChange);
        pw.println("  mSleeping=" + mSleeping + " mShuttingDown=" + mShuttingDown);
        if (mDebugApp != null || mOrigDebugApp != null || mDebugTransient
                || mOrigWaitForDebugger) {
            pw.println("  mDebugApp=" + mDebugApp + "/orig=" + mOrigDebugApp
                    + " mDebugTransient=" + mDebugTransient
                    + " mOrigWaitForDebugger=" + mOrigWaitForDebugger);
        }
        if (mAlwaysFinishActivities || mController != null) {
            pw.println("  mAlwaysFinishActivities=" + mAlwaysFinishActivities
                    + " mController=" + mController);
        }
        if (dumpAll) {
            pw.println("  Total persistent processes: " + numPers);
            pw.println("  mStartRunning=" + mStartRunning
                    + " mSystemReady=" + mSystemReady
                    + " mBooting=" + mBooting
                    + " mBooted=" + mBooted
                    + " mFactoryTest=" + mFactoryTest);
            pw.println("  mGoingToSleep=" + mGoingToSleep);
            pw.println("  mLaunchingActivity=" + mLaunchingActivity);
            pw.println("  mAdjSeq=" + mAdjSeq + " mLruSeq=" + mLruSeq);
        }
        return true;
    }
    protected void dumpService(FileDescriptor fd, PrintWriter pw, String[] args,
            int opti, boolean dumpAll) {
        String[] newArgs;
        String componentNameString;
        ServiceRecord r;
        if (opti >= args.length) {
            componentNameString = null;
            newArgs = EMPTY_STRING_ARRAY;
            r = null;
        } else {
            componentNameString = args[opti];
            opti++;
            ComponentName componentName = ComponentName.unflattenFromString(componentNameString);
            r = componentName != null ? mServices.get(componentName) : null;
            newArgs = new String[args.length - opti];
            if (args.length > 2) System.arraycopy(args, opti, newArgs, 0, args.length - opti);
        }
        if (r != null) {
            dumpService(fd, pw, r, newArgs);
        } else {
            for (ServiceRecord r1 : mServices.values()) {
                if (componentNameString == null
                        || r1.name.flattenToString().contains(componentNameString)) {
                    dumpService(fd, pw, r1, newArgs);
                }
            }
        }
    }
    private void dumpService(FileDescriptor fd, PrintWriter pw, ServiceRecord r, String[] args) {
        pw.println("  Service " + r.name.flattenToString());
        if (r.app != null && r.app.thread != null) {
            try {
                pw.flush();
                r.app.thread.dumpService(fd, r, args);
                pw.print("\n");
            } catch (RemoteException e) {
                pw.println("got a RemoteException while dumping the service");
            }
        }
    }
    boolean dumpBroadcastsLocked(FileDescriptor fd, PrintWriter pw, String[] args,
            int opti, boolean dumpAll) {
        boolean needSep = false;
        if (dumpAll) {
            if (mRegisteredReceivers.size() > 0) {
                pw.println(" ");
                pw.println("  Registered Receivers:");
                Iterator it = mRegisteredReceivers.values().iterator();
                while (it.hasNext()) {
                    ReceiverList r = (ReceiverList)it.next();
                    pw.print("  * "); pw.println(r);
                    r.dump(pw, "    ");
                }
            }
            pw.println(" ");
            pw.println("Receiver Resolver Table:");
            mReceiverResolver.dump(pw, null, "  ", null);
            needSep = true;
        }
        if (mParallelBroadcasts.size() > 0 || mOrderedBroadcasts.size() > 0
                || mPendingBroadcast != null) {
            if (mParallelBroadcasts.size() > 0) {
                pw.println(" ");
                pw.println("  Active broadcasts:");
            }
            for (int i=mParallelBroadcasts.size()-1; i>=0; i--) {
                pw.println("  Broadcast #" + i + ":");
                mParallelBroadcasts.get(i).dump(pw, "    ");
            }
            if (mOrderedBroadcasts.size() > 0) {
                pw.println(" ");
                pw.println("  Active ordered broadcasts:");
            }
            for (int i=mOrderedBroadcasts.size()-1; i>=0; i--) {
                pw.println("  Serialized Broadcast #" + i + ":");
                mOrderedBroadcasts.get(i).dump(pw, "    ");
            }
            pw.println(" ");
            pw.println("  Pending broadcast:");
            if (mPendingBroadcast != null) {
                mPendingBroadcast.dump(pw, "    ");
            } else {
                pw.println("    (null)");
            }
            needSep = true;
        }
        if (dumpAll) {
            pw.println(" ");
            pw.println("  Historical broadcasts:");
            for (int i=0; i<MAX_BROADCAST_HISTORY; i++) {
                BroadcastRecord r = mBroadcastHistory[i];
                if (r == null) {
                    break;
                }
                pw.println("  Historical Broadcast #" + i + ":");
                r.dump(pw, "    ");
            }
            needSep = true;
        }
        if (mStickyBroadcasts != null) {
            pw.println(" ");
            pw.println("  Sticky broadcasts:");
            StringBuilder sb = new StringBuilder(128);
            for (Map.Entry<String, ArrayList<Intent>> ent
                    : mStickyBroadcasts.entrySet()) {
                pw.print("  * Sticky action "); pw.print(ent.getKey());
                        pw.println(":");
                ArrayList<Intent> intents = ent.getValue();
                final int N = intents.size();
                for (int i=0; i<N; i++) {
                    sb.setLength(0);
                    sb.append("    Intent: ");
                    intents.get(i).toShortString(sb, true, false);
                    pw.println(sb.toString());
                    Bundle bundle = intents.get(i).getExtras();
                    if (bundle != null) {
                        pw.print("      ");
                        pw.println(bundle.toString());
                    }
                }
            }
            needSep = true;
        }
        if (dumpAll) {
            pw.println(" ");
            pw.println("  mBroadcastsScheduled=" + mBroadcastsScheduled);
            pw.println("  mHandler:");
            mHandler.dump(new PrintWriterPrinter(pw), "    ");
            needSep = true;
        }
        return needSep;
    }
    boolean dumpServicesLocked(FileDescriptor fd, PrintWriter pw, String[] args,
            int opti, boolean dumpAll) {
        boolean needSep = false;
        if (dumpAll) {
            if (mServices.size() > 0) {
                pw.println("  Active services:");
                Iterator<ServiceRecord> it = mServices.values().iterator();
                while (it.hasNext()) {
                    ServiceRecord r = it.next();
                    pw.print("  * "); pw.println(r);
                    r.dump(pw, "    ");
                }
                needSep = true;
            }
        }
        if (mPendingServices.size() > 0) {
            if (needSep) pw.println(" ");
            pw.println("  Pending services:");
            for (int i=0; i<mPendingServices.size(); i++) {
                ServiceRecord r = mPendingServices.get(i);
                pw.print("  * Pending "); pw.println(r);
                r.dump(pw, "    ");
            }
            needSep = true;
        }
        if (mRestartingServices.size() > 0) {
            if (needSep) pw.println(" ");
            pw.println("  Restarting services:");
            for (int i=0; i<mRestartingServices.size(); i++) {
                ServiceRecord r = mRestartingServices.get(i);
                pw.print("  * Restarting "); pw.println(r);
                r.dump(pw, "    ");
            }
            needSep = true;
        }
        if (mStoppingServices.size() > 0) {
            if (needSep) pw.println(" ");
            pw.println("  Stopping services:");
            for (int i=0; i<mStoppingServices.size(); i++) {
                ServiceRecord r = mStoppingServices.get(i);
                pw.print("  * Stopping "); pw.println(r);
                r.dump(pw, "    ");
            }
            needSep = true;
        }
        if (dumpAll) {
            if (mServiceConnections.size() > 0) {
                if (needSep) pw.println(" ");
                pw.println("  Connection bindings to services:");
                Iterator<ConnectionRecord> it
                        = mServiceConnections.values().iterator();
                while (it.hasNext()) {
                    ConnectionRecord r = it.next();
                    pw.print("  * "); pw.println(r);
                    r.dump(pw, "    ");
                }
                needSep = true;
            }
        }
        return needSep;
    }
    boolean dumpProvidersLocked(FileDescriptor fd, PrintWriter pw, String[] args,
            int opti, boolean dumpAll) {
        boolean needSep = false;
        if (dumpAll) {
            if (mProvidersByClass.size() > 0) {
                if (needSep) pw.println(" ");
                pw.println("  Published content providers (by class):");
                Iterator it = mProvidersByClass.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry)it.next();
                    ContentProviderRecord r = (ContentProviderRecord)e.getValue();
                    pw.print("  * "); pw.println(r);
                    r.dump(pw, "    ");
                }
                needSep = true;
            }
            if (mProvidersByName.size() > 0) {
                pw.println(" ");
                pw.println("  Authority to provider mappings:");
                Iterator it = mProvidersByName.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry)it.next();
                    ContentProviderRecord r = (ContentProviderRecord)e.getValue();
                    pw.print("  "); pw.print(e.getKey()); pw.print(": ");
                            pw.println(r);
                }
                needSep = true;
            }
        }
        if (mLaunchingProviders.size() > 0) {
            if (needSep) pw.println(" ");
            pw.println("  Launching content providers:");
            for (int i=mLaunchingProviders.size()-1; i>=0; i--) {
                pw.print("  Launching #"); pw.print(i); pw.print(": ");
                        pw.println(mLaunchingProviders.get(i));
            }
            needSep = true;
        }
        if (mGrantedUriPermissions.size() > 0) {
            pw.println();
            pw.println("Granted Uri Permissions:");
            for (int i=0; i<mGrantedUriPermissions.size(); i++) {
                int uid = mGrantedUriPermissions.keyAt(i);
                HashMap<Uri, UriPermission> perms
                        = mGrantedUriPermissions.valueAt(i);
                pw.print("  * UID "); pw.print(uid);
                        pw.println(" holds:");
                for (UriPermission perm : perms.values()) {
                    pw.print("    "); pw.println(perm);
                    perm.dump(pw, "      ");
                }
            }
            needSep = true;
        }
        return needSep;
    }
    boolean dumpPendingIntentsLocked(FileDescriptor fd, PrintWriter pw, String[] args,
            int opti, boolean dumpAll) {
        boolean needSep = false;
        if (dumpAll) {
            if (this.mIntentSenderRecords.size() > 0) {
                Iterator<WeakReference<PendingIntentRecord>> it
                        = mIntentSenderRecords.values().iterator();
                while (it.hasNext()) {
                    WeakReference<PendingIntentRecord> ref = it.next();
                    PendingIntentRecord rec = ref != null ? ref.get(): null;
                    needSep = true;
                    if (rec != null) {
                        pw.print("  * "); pw.println(rec);
                        rec.dump(pw, "    ");
                    } else {
                        pw.print("  * "); pw.print(ref);
                    }
                }
            }
        }
        return needSep;
    }
    private static final void dumpHistoryList(PrintWriter pw, List list,
            String prefix, String label, boolean complete) {
        TaskRecord lastTask = null;
        for (int i=list.size()-1; i>=0; i--) {
            HistoryRecord r = (HistoryRecord)list.get(i);
            final boolean full = complete || !r.inHistory;
            if (lastTask != r.task) {
                lastTask = r.task;
                pw.print(prefix);
                pw.print(full ? "* " : "  ");
                pw.println(lastTask);
                if (full) {
                    lastTask.dump(pw, prefix + "  ");
                }
            }
            pw.print(prefix); pw.print(full ? "  * " : "    "); pw.print(label);
            pw.print(" #"); pw.print(i); pw.print(": ");
            pw.println(r);
            if (full) {
                r.dump(pw, prefix + "      ");
            }
        }
    }
    private static String buildOomTag(String prefix, String space, int val, int base) {
        if (val == base) {
            if (space == null) return prefix;
            return prefix + "  ";
        }
        return prefix + "+" + Integer.toString(val-base);
    }
    private static final int dumpProcessList(PrintWriter pw,
            ActivityManagerService service, List list,
            String prefix, String normalLabel, String persistentLabel,
            boolean inclOomAdj) {
        int numPers = 0;
        for (int i=list.size()-1; i>=0; i--) {
            ProcessRecord r = (ProcessRecord)list.get(i);
            if (false) {
                pw.println(prefix + (r.persistent ? persistentLabel : normalLabel)
                      + " #" + i + ":");
                r.dump(pw, prefix + "  ");
            } else if (inclOomAdj) {
                String oomAdj;
                if (r.setAdj >= EMPTY_APP_ADJ) {
                    oomAdj = buildOomTag("empty", null, r.setAdj, EMPTY_APP_ADJ);
                } else if (r.setAdj >= HIDDEN_APP_MIN_ADJ) {
                    oomAdj = buildOomTag("bak", "  ", r.setAdj, HIDDEN_APP_MIN_ADJ);
                } else if (r.setAdj >= HOME_APP_ADJ) {
                    oomAdj = buildOomTag("home ", null, r.setAdj, HOME_APP_ADJ);
                } else if (r.setAdj >= SECONDARY_SERVER_ADJ) {
                    oomAdj = buildOomTag("svc", "  ", r.setAdj, SECONDARY_SERVER_ADJ);
                } else if (r.setAdj >= BACKUP_APP_ADJ) {
                    oomAdj = buildOomTag("bckup", null, r.setAdj, BACKUP_APP_ADJ);
                } else if (r.setAdj >= VISIBLE_APP_ADJ) {
                    oomAdj = buildOomTag("vis  ", null, r.setAdj, VISIBLE_APP_ADJ);
                } else if (r.setAdj >= FOREGROUND_APP_ADJ) {
                    oomAdj = buildOomTag("fore ", null, r.setAdj, FOREGROUND_APP_ADJ);
                } else if (r.setAdj >= CORE_SERVER_ADJ) {
                    oomAdj = buildOomTag("core ", null, r.setAdj, CORE_SERVER_ADJ);
                } else if (r.setAdj >= SYSTEM_ADJ) {
                    oomAdj = buildOomTag("sys  ", null, r.setAdj, SYSTEM_ADJ);
                } else {
                    oomAdj = Integer.toString(r.setAdj);
                }
                String schedGroup;
                switch (r.setSchedGroup) {
                    case Process.THREAD_GROUP_BG_NONINTERACTIVE:
                        schedGroup = "B";
                        break;
                    case Process.THREAD_GROUP_DEFAULT:
                        schedGroup = "F";
                        break;
                    default:
                        schedGroup = Integer.toString(r.setSchedGroup);
                        break;
                }
                pw.println(String.format("%s%s #%2d: adj=%s/%s %s (%s)",
                        prefix, (r.persistent ? persistentLabel : normalLabel),
                        i, oomAdj, schedGroup, r.toShortString(), r.adjType));
                if (r.adjSource != null || r.adjTarget != null) {
                    pw.println(prefix + "          " + r.adjTarget
                            + "<=" + r.adjSource);
                }
            } else {
                pw.println(String.format("%s%s #%2d: %s",
                        prefix, (r.persistent ? persistentLabel : normalLabel),
                        i, r.toString()));
            }
            if (r.persistent) {
                numPers++;
            }
        }
        return numPers;
    }
    static final void dumpApplicationMemoryUsage(FileDescriptor fd,
            PrintWriter pw, List list, String prefix, String[] args) {
        final boolean isCheckinRequest = scanArgs(args, "--checkin");
        long uptime = SystemClock.uptimeMillis();
        long realtime = SystemClock.elapsedRealtime();
        if (isCheckinRequest) {
            pw.println(uptime + "," + realtime);
            pw.flush();
        } else {
            pw.println("Applications Memory Usage (kB):");
            pw.println("Uptime: " + uptime + " Realtime: " + realtime);
        }
        for (int i = list.size() - 1 ; i >= 0 ; i--) {
            ProcessRecord r = (ProcessRecord)list.get(i);
            if (r.thread != null) {
                if (!isCheckinRequest) {
                    pw.println("\n** MEMINFO in pid " + r.pid + " [" + r.processName + "] **");
                    pw.flush();
                }
                try {
                    r.thread.asBinder().dump(fd, args);
                } catch (RemoteException e) {
                    if (!isCheckinRequest) {
                        pw.println("Got RemoteException!");
                        pw.flush();
                    }
                }
            }
        }
    }
    private static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }
    private final int indexOfTokenLocked(IBinder token) {
        int count = mHistory.size();
        int index = -1;
        for (int i=count-1; i>=0; i--) {
            Object o = mHistory.get(i);
            if (o == token) {
                index = i;
                break;
            }
        }
        return index;
    }
    private final void killServicesLocked(ProcessRecord app,
            boolean allowRestart) {
        if (false) {
            if (app.services.size() > 0) {
                Iterator it = app.services.iterator();
                while (it.hasNext()) {
                    ServiceRecord r = (ServiceRecord)it.next();
                    if (r.connections.size() > 0) {
                        Iterator<ConnectionRecord> jt
                                = r.connections.values().iterator();
                        while (jt.hasNext()) {
                            ConnectionRecord c = jt.next();
                            if (c.binding.client != app) {
                                try {
                                } catch (Exception e) {
                                    Slog.w(TAG, "Exception thrown disconnected servce "
                                          + r.shortName
                                          + " from app " + app.processName, e);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (app.connections.size() > 0) {
            Iterator<ConnectionRecord> it = app.connections.iterator();
            while (it.hasNext()) {
                ConnectionRecord r = it.next();
                removeConnectionLocked(r, app, null);
            }
        }
        app.connections.clear();
        if (app.services.size() != 0) {
            Iterator it = app.services.iterator();
            while (it.hasNext()) {
                ServiceRecord sr = (ServiceRecord)it.next();
                synchronized (sr.stats.getBatteryStats()) {
                    sr.stats.stopLaunchedLocked();
                }
                sr.app = null;
                sr.executeNesting = 0;
                mStoppingServices.remove(sr);
                boolean hasClients = sr.bindings.size() > 0;
                if (hasClients) {
                    Iterator<IntentBindRecord> bindings
                            = sr.bindings.values().iterator();
                    while (bindings.hasNext()) {
                        IntentBindRecord b = bindings.next();
                        if (DEBUG_SERVICE) Slog.v(TAG, "Killing binding " + b
                                + ": shouldUnbind=" + b.hasBound);
                        b.binder = null;
                        b.requested = b.received = b.hasBound = false;
                    }
                }
                if (sr.crashCount >= 2) {
                    Slog.w(TAG, "Service crashed " + sr.crashCount
                            + " times, stopping: " + sr);
                    EventLog.writeEvent(EventLogTags.AM_SERVICE_CRASHED_TOO_MUCH,
                            sr.crashCount, sr.shortName, app.pid);
                    bringDownServiceLocked(sr, true);
                } else if (!allowRestart) {
                    bringDownServiceLocked(sr, true);
                } else {
                    boolean canceled = scheduleServiceRestartLocked(sr, true);
                    if (sr.startRequested && (sr.stopIfKilled || canceled)) {
                        if (sr.pendingStarts.size() == 0) {
                            sr.startRequested = false;
                            if (!hasClients) {
                                bringDownServiceLocked(sr, true);
                            }
                        }
                    }
                }
            }
            if (!allowRestart) {
                app.services.clear();
            }
        }
        int i = mStoppingServices.size();
        while (i > 0) {
            i--;
            ServiceRecord sr = mStoppingServices.get(i);
            if (sr.app == app) {
                mStoppingServices.remove(i);
            }
        }
        app.executingServices.clear();
    }
    private final void removeDyingProviderLocked(ProcessRecord proc,
            ContentProviderRecord cpr) {
        synchronized (cpr) {
            cpr.launchingApp = null;
            cpr.notifyAll();
        }
        mProvidersByClass.remove(cpr.info.name);
        String names[] = cpr.info.authority.split(";");
        for (int j = 0; j < names.length; j++) {
            mProvidersByName.remove(names[j]);
        }
        Iterator<ProcessRecord> cit = cpr.clients.iterator();
        while (cit.hasNext()) {
            ProcessRecord capp = cit.next();
            if (!capp.persistent && capp.thread != null
                    && capp.pid != 0
                    && capp.pid != MY_PID) {
                Slog.i(TAG, "Kill " + capp.processName
                        + " (pid " + capp.pid + "): provider " + cpr.info.name
                        + " in dying process " + proc.processName);
                EventLog.writeEvent(EventLogTags.AM_KILL, capp.pid,
                        capp.processName, capp.setAdj, "dying provider " + proc.processName);
                Process.killProcess(capp.pid);
            }
        }
        mLaunchingProviders.remove(cpr);
    }
    private final void cleanUpApplicationRecordLocked(ProcessRecord app,
            boolean restarting, int index) {
        if (index >= 0) {
            mLruProcesses.remove(index);
        }
        mProcessesToGc.remove(app);
        if (app.crashDialog != null) {
            app.crashDialog.dismiss();
            app.crashDialog = null;
        }
        if (app.anrDialog != null) {
            app.anrDialog.dismiss();
            app.anrDialog = null;
        }
        if (app.waitDialog != null) {
            app.waitDialog.dismiss();
            app.waitDialog = null;
        }
        app.crashing = false;
        app.notResponding = false;
        app.resetPackageList();
        app.thread = null;
        app.forcingToForeground = null;
        app.foregroundServices = false;
        killServicesLocked(app, true);
        boolean restart = false;
        int NL = mLaunchingProviders.size();
        if (!app.pubProviders.isEmpty()) {
            Iterator it = app.pubProviders.values().iterator();
            while (it.hasNext()) {
                ContentProviderRecord cpr = (ContentProviderRecord)it.next();
                cpr.provider = null;
                cpr.app = null;
                int i = 0;
                if (!app.bad) {
                    for (; i<NL; i++) {
                        if (mLaunchingProviders.get(i) == cpr) {
                            restart = true;
                            break;
                        }
                    }
                } else {
                    i = NL;
                }
                if (i >= NL) {
                    removeDyingProviderLocked(app, cpr);
                    NL = mLaunchingProviders.size();
                }
            }
            app.pubProviders.clear();
        }
        if (checkAppInLaunchingProvidersLocked(app, false)) {
            restart = true;
        }
        if (!app.conProviders.isEmpty()) {
            Iterator it = app.conProviders.keySet().iterator();
            while (it.hasNext()) {
                ContentProviderRecord cpr = (ContentProviderRecord)it.next();
                cpr.clients.remove(app);
            }
            app.conProviders.clear();
        }
        if (false) {
            for (int i=0; i<NL; i++) {
                ContentProviderRecord cpr = (ContentProviderRecord)
                        mLaunchingProviders.get(i);
                if (cpr.clients.size() <= 0 && cpr.externals <= 0) {
                    synchronized (cpr) {
                        cpr.launchingApp = null;
                        cpr.notifyAll();
                    }
                }
            }
        }
        skipCurrentReceiverLocked(app);
        if (app.receivers.size() > 0) {
            Iterator<ReceiverList> it = app.receivers.iterator();
            while (it.hasNext()) {
                removeReceiverLocked(it.next());
            }
            app.receivers.clear();
        }
        if (mBackupTarget != null && app.pid == mBackupTarget.app.pid) {
            if (DEBUG_BACKUP) Slog.d(TAG, "App " + mBackupTarget.appInfo + " died during backup");
            try {
                IBackupManager bm = IBackupManager.Stub.asInterface(
                        ServiceManager.getService(Context.BACKUP_SERVICE));
                bm.agentDisconnected(app.info.packageName);
            } catch (RemoteException e) {
            }
        }
        if (restarting) {
            return;
        }
        if (!app.persistent) {
            if (DEBUG_PROCESSES) Slog.v(TAG,
                    "Removing non-persistent process during cleanup: " + app);
            mProcessNames.remove(app.processName, app.info.uid);
        } else if (!app.removed) {
            app.thread = null;
            app.forcingToForeground = null;
            app.foregroundServices = false;
            if (mPersistentStartingProcesses.indexOf(app) < 0) {
                mPersistentStartingProcesses.add(app);
                restart = true;
            }
        }
        mProcessesOnHold.remove(app);
        if (app == mHomeProcess) {
            mHomeProcess = null;
        }
        if (restart) {
            mProcessNames.put(app.processName, app.info.uid, app);
            startProcessLocked(app, "restart", app.processName);
        } else if (app.pid > 0 && app.pid != MY_PID) {
            synchronized (mPidsSelfLocked) {
                mPidsSelfLocked.remove(app.pid);
                mHandler.removeMessages(PROC_START_TIMEOUT_MSG, app);
            }
            app.setPid(0);
        }
    }
    boolean checkAppInLaunchingProvidersLocked(ProcessRecord app, boolean alwaysBad) {
        int NL = mLaunchingProviders.size();
        boolean restart = false;
        for (int i=0; i<NL; i++) {
            ContentProviderRecord cpr = (ContentProviderRecord)
                    mLaunchingProviders.get(i);
            if (cpr.launchingApp == app) {
                if (!alwaysBad && !app.bad) {
                    restart = true;
                } else {
                    removeDyingProviderLocked(app, cpr);
                    NL = mLaunchingProviders.size();
                }
            }
        }
        return restart;
    }
    ActivityManager.RunningServiceInfo makeRunningServiceInfoLocked(ServiceRecord r) {
        ActivityManager.RunningServiceInfo info =
            new ActivityManager.RunningServiceInfo();
        info.service = r.name;
        if (r.app != null) {
            info.pid = r.app.pid;
        }
        info.uid = r.appInfo.uid;
        info.process = r.processName;
        info.foreground = r.isForeground;
        info.activeSince = r.createTime;
        info.started = r.startRequested;
        info.clientCount = r.connections.size();
        info.crashCount = r.crashCount;
        info.lastActivityTime = r.lastActivity;
        if (r.isForeground) {
            info.flags |= ActivityManager.RunningServiceInfo.FLAG_FOREGROUND;
        }
        if (r.startRequested) {
            info.flags |= ActivityManager.RunningServiceInfo.FLAG_STARTED;
        }
        if (r.app != null && r.app.pid == MY_PID) {
            info.flags |= ActivityManager.RunningServiceInfo.FLAG_SYSTEM_PROCESS;
        }
        if (r.app != null && r.app.persistent) {
            info.flags |= ActivityManager.RunningServiceInfo.FLAG_PERSISTENT_PROCESS;
        }
        for (ConnectionRecord conn : r.connections.values()) {
            if (conn.clientLabel != 0) {
                info.clientPackage = conn.binding.client.info.packageName;
                info.clientLabel = conn.clientLabel;
                break;
            }
        }
        return info;
    }
    public List<ActivityManager.RunningServiceInfo> getServices(int maxNum,
            int flags) {
        synchronized (this) {
            ArrayList<ActivityManager.RunningServiceInfo> res
                    = new ArrayList<ActivityManager.RunningServiceInfo>();
            if (mServices.size() > 0) {
                Iterator<ServiceRecord> it = mServices.values().iterator();
                while (it.hasNext() && res.size() < maxNum) {
                    res.add(makeRunningServiceInfoLocked(it.next()));
                }
            }
            for (int i=0; i<mRestartingServices.size() && res.size() < maxNum; i++) {
                ServiceRecord r = mRestartingServices.get(i);
                ActivityManager.RunningServiceInfo info =
                        makeRunningServiceInfoLocked(r);
                info.restarting = r.nextRestartTime;
                res.add(info);
            }
            return res;
        }
    }
    public PendingIntent getRunningServiceControlPanel(ComponentName name) {
        synchronized (this) {
            ServiceRecord r = mServices.get(name);
            if (r != null) {
                for (ConnectionRecord conn : r.connections.values()) {
                    if (conn.clientIntent != null) {
                        return conn.clientIntent;
                    }
                }
            }
        }
        return null;
    }
    private final ServiceRecord findServiceLocked(ComponentName name,
            IBinder token) {
        ServiceRecord r = mServices.get(name);
        return r == token ? r : null;
    }
    private final class ServiceLookupResult {
        final ServiceRecord record;
        final String permission;
        ServiceLookupResult(ServiceRecord _record, String _permission) {
            record = _record;
            permission = _permission;
        }
    };
    private ServiceLookupResult findServiceLocked(Intent service,
            String resolvedType) {
        ServiceRecord r = null;
        if (service.getComponent() != null) {
            r = mServices.get(service.getComponent());
        }
        if (r == null) {
            Intent.FilterComparison filter = new Intent.FilterComparison(service);
            r = mServicesByIntent.get(filter);
        }
        if (r == null) {
            try {
                ResolveInfo rInfo =
                    ActivityThread.getPackageManager().resolveService(
                            service, resolvedType, 0);
                ServiceInfo sInfo =
                    rInfo != null ? rInfo.serviceInfo : null;
                if (sInfo == null) {
                    return null;
                }
                ComponentName name = new ComponentName(
                        sInfo.applicationInfo.packageName, sInfo.name);
                r = mServices.get(name);
            } catch (RemoteException ex) {
            }
        }
        if (r != null) {
            int callingPid = Binder.getCallingPid();
            int callingUid = Binder.getCallingUid();
            if (checkComponentPermission(r.permission,
                    callingPid, callingUid, r.exported ? -1 : r.appInfo.uid)
                    != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission Denial: Accessing service " + r.name
                        + " from pid=" + callingPid
                        + ", uid=" + callingUid
                        + " requires " + r.permission);
                return new ServiceLookupResult(null, r.permission);
            }
            return new ServiceLookupResult(r, null);
        }
        return null;
    }
    private class ServiceRestarter implements Runnable {
        private ServiceRecord mService;
        void setService(ServiceRecord service) {
            mService = service;
        }
        public void run() {
            synchronized(ActivityManagerService.this) {
                performServiceRestartLocked(mService);
            }
        }
    }
    private ServiceLookupResult retrieveServiceLocked(Intent service,
            String resolvedType, int callingPid, int callingUid) {
        ServiceRecord r = null;
        if (service.getComponent() != null) {
            r = mServices.get(service.getComponent());
        }
        Intent.FilterComparison filter = new Intent.FilterComparison(service);
        r = mServicesByIntent.get(filter);
        if (r == null) {
            try {
                ResolveInfo rInfo =
                    ActivityThread.getPackageManager().resolveService(
                            service, resolvedType, STOCK_PM_FLAGS);
                ServiceInfo sInfo =
                    rInfo != null ? rInfo.serviceInfo : null;
                if (sInfo == null) {
                    Slog.w(TAG, "Unable to start service " + service +
                          ": not found");
                    return null;
                }
                ComponentName name = new ComponentName(
                        sInfo.applicationInfo.packageName, sInfo.name);
                r = mServices.get(name);
                if (r == null) {
                    filter = new Intent.FilterComparison(service.cloneFilter());
                    ServiceRestarter res = new ServiceRestarter();
                    BatteryStatsImpl.Uid.Pkg.Serv ss = null;
                    BatteryStatsImpl stats = mBatteryStatsService.getActiveStatistics();
                    synchronized (stats) {
                        ss = stats.getServiceStatsLocked(
                                sInfo.applicationInfo.uid, sInfo.packageName,
                                sInfo.name);
                    }
                    r = new ServiceRecord(this, ss, name, filter, sInfo, res);
                    res.setService(r);
                    mServices.put(name, r);
                    mServicesByIntent.put(filter, r);
                    int N = mPendingServices.size();
                    for (int i=0; i<N; i++) {
                        ServiceRecord pr = mPendingServices.get(i);
                        if (pr.name.equals(name)) {
                            mPendingServices.remove(i);
                            i--;
                            N--;
                        }
                    }
                }
            } catch (RemoteException ex) {
            }
        }
        if (r != null) {
            if (checkComponentPermission(r.permission,
                    callingPid, callingUid, r.exported ? -1 : r.appInfo.uid)
                    != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission Denial: Accessing service " + r.name
                        + " from pid=" + Binder.getCallingPid()
                        + ", uid=" + Binder.getCallingUid()
                        + " requires " + r.permission);
                return new ServiceLookupResult(null, r.permission);
            }
            return new ServiceLookupResult(r, null);
        }
        return null;
    }
    private final void bumpServiceExecutingLocked(ServiceRecord r) {
        long now = SystemClock.uptimeMillis();
        if (r.executeNesting == 0 && r.app != null) {
            if (r.app.executingServices.size() == 0) {
                Message msg = mHandler.obtainMessage(SERVICE_TIMEOUT_MSG);
                msg.obj = r.app;
                mHandler.sendMessageAtTime(msg, now+SERVICE_TIMEOUT);
            }
            r.app.executingServices.add(r);
        }
        r.executeNesting++;
        r.executingStart = now;
    }
    private final void sendServiceArgsLocked(ServiceRecord r,
            boolean oomAdjusted) {
        final int N = r.pendingStarts.size();
        if (N == 0) {
            return;
        }
        int i = 0;
        while (i < N) {
            try {
                ServiceRecord.StartItem si = r.pendingStarts.get(i);
                if (DEBUG_SERVICE) Slog.v(TAG, "Sending arguments to service: "
                        + r.name + " " + r.intent + " args=" + si.intent);
                if (si.intent == null && N > 1) {
                    i++;
                    continue;
                }
                bumpServiceExecutingLocked(r);
                if (!oomAdjusted) {
                    oomAdjusted = true;
                    updateOomAdjLocked(r.app);
                }
                int flags = 0;
                if (si.deliveryCount > 0) {
                    flags |= Service.START_FLAG_RETRY;
                }
                if (si.doneExecutingCount > 0) {
                    flags |= Service.START_FLAG_REDELIVERY;
                }
                r.app.thread.scheduleServiceArgs(r, si.id, flags, si.intent);
                si.deliveredTime = SystemClock.uptimeMillis();
                r.deliveredStarts.add(si);
                si.deliveryCount++;
                i++;
            } catch (RemoteException e) {
                break;
            } catch (Exception e) {
                Slog.w(TAG, "Unexpected exception", e);
                break;
            }
        }
        if (i == N) {
            r.pendingStarts.clear();
        } else {
            while (i > 0) {
                i--;
                r.pendingStarts.remove(i);
            }
        }
    }
    private final boolean requestServiceBindingLocked(ServiceRecord r,
            IntentBindRecord i, boolean rebind) {
        if (r.app == null || r.app.thread == null) {
            return false;
        }
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r);
                if (DEBUG_SERVICE) Slog.v(TAG, "Connecting binding " + i
                        + ": shouldUnbind=" + i.hasBound);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind);
                if (!rebind) {
                    i.requested = true;
                }
                i.hasBound = true;
                i.doRebind = false;
            } catch (RemoteException e) {
                return false;
            }
        }
        return true;
    }
    private final void requestServiceBindingsLocked(ServiceRecord r) {
        Iterator<IntentBindRecord> bindings = r.bindings.values().iterator();
        while (bindings.hasNext()) {
            IntentBindRecord i = bindings.next();
            if (!requestServiceBindingLocked(r, i, false)) {
                break;
            }
        }
    }
    private final void realStartServiceLocked(ServiceRecord r,
            ProcessRecord app) throws RemoteException {
        if (app.thread == null) {
            throw new RemoteException();
        }
        r.app = app;
        r.restartTime = r.lastActivity = SystemClock.uptimeMillis();
        app.services.add(r);
        bumpServiceExecutingLocked(r);
        updateLruProcessLocked(app, true, true);
        boolean created = false;
        try {
            if (DEBUG_SERVICE) Slog.v(TAG, "Scheduling start service: "
                    + r.name + " " + r.intent);
            mStringBuilder.setLength(0);
            r.intent.getIntent().toShortString(mStringBuilder, false, true);
            EventLog.writeEvent(EventLogTags.AM_CREATE_SERVICE,
                    System.identityHashCode(r), r.shortName,
                    mStringBuilder.toString(), r.app.pid);
            synchronized (r.stats.getBatteryStats()) {
                r.stats.startLaunchedLocked();
            }
            ensurePackageDexOpt(r.serviceInfo.packageName);
            app.thread.scheduleCreateService(r, r.serviceInfo);
            r.postNotification();
            created = true;
        } finally {
            if (!created) {
                app.services.remove(r);
                scheduleServiceRestartLocked(r, false);
            }
        }
        requestServiceBindingsLocked(r);
        if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
            r.lastStartId++;
            if (r.lastStartId < 1) {
                r.lastStartId = 1;
            }
            r.pendingStarts.add(new ServiceRecord.StartItem(r.lastStartId, null));
        }
        sendServiceArgsLocked(r, true);
    }
    private final boolean scheduleServiceRestartLocked(ServiceRecord r,
            boolean allowCancel) {
        boolean canceled = false;
        final long now = SystemClock.uptimeMillis();
        long minDuration = SERVICE_RESTART_DURATION;
        long resetTime = SERVICE_RESET_RUN_DURATION;
        final int N = r.deliveredStarts.size();
        if (N > 0) {
            for (int i=N-1; i>=0; i--) {
                ServiceRecord.StartItem si = r.deliveredStarts.get(i);
                if (si.intent == null) {
                } else if (!allowCancel || (si.deliveryCount < ServiceRecord.MAX_DELIVERY_COUNT
                        && si.doneExecutingCount < ServiceRecord.MAX_DONE_EXECUTING_COUNT)) {
                    r.pendingStarts.add(0, si);
                    long dur = SystemClock.uptimeMillis() - si.deliveredTime;
                    dur *= 2;
                    if (minDuration < dur) minDuration = dur;
                    if (resetTime < dur) resetTime = dur;
                } else {
                    Slog.w(TAG, "Canceling start item " + si.intent + " in service "
                            + r.name);
                    canceled = true;
                }
            }
            r.deliveredStarts.clear();
        }
        r.totalRestartCount++;
        if (r.restartDelay == 0) {
            r.restartCount++;
            r.restartDelay = minDuration;
        } else {
            if (now > (r.restartTime+resetTime)) {
                r.restartCount = 1;
                r.restartDelay = minDuration;
            } else {
                r.restartDelay *= SERVICE_RESTART_DURATION_FACTOR;
                if (r.restartDelay < minDuration) {
                    r.restartDelay = minDuration;
                }
            }
        }
        r.nextRestartTime = now + r.restartDelay;
        boolean repeat;
        do {
            repeat = false;
            for (int i=mRestartingServices.size()-1; i>=0; i--) {
                ServiceRecord r2 = mRestartingServices.get(i);
                if (r2 != r && r.nextRestartTime
                        >= (r2.nextRestartTime-SERVICE_MIN_RESTART_TIME_BETWEEN)
                        && r.nextRestartTime
                        < (r2.nextRestartTime+SERVICE_MIN_RESTART_TIME_BETWEEN)) {
                    r.nextRestartTime = r2.nextRestartTime + SERVICE_MIN_RESTART_TIME_BETWEEN;
                    r.restartDelay = r.nextRestartTime - now;
                    repeat = true;
                    break;
                }
            }
        } while (repeat);
        if (!mRestartingServices.contains(r)) {
            mRestartingServices.add(r);
        }
        r.cancelNotification();
        mHandler.removeCallbacks(r.restarter);
        mHandler.postAtTime(r.restarter, r.nextRestartTime);
        r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
        Slog.w(TAG, "Scheduling restart of crashed service "
                + r.shortName + " in " + r.restartDelay + "ms");
        EventLog.writeEvent(EventLogTags.AM_SCHEDULE_SERVICE_RESTART,
                r.shortName, r.restartDelay);
        return canceled;
    }
    final void performServiceRestartLocked(ServiceRecord r) {
        if (!mRestartingServices.contains(r)) {
            return;
        }
        bringUpServiceLocked(r, r.intent.getIntent().getFlags(), true);
    }
    private final boolean unscheduleServiceRestartLocked(ServiceRecord r) {
        if (r.restartDelay == 0) {
            return false;
        }
        r.resetRestartCounter();
        mRestartingServices.remove(r);
        mHandler.removeCallbacks(r.restarter);
        return true;
    }
    private final boolean bringUpServiceLocked(ServiceRecord r,
            int intentFlags, boolean whileRestarting) {
        if (r.app != null && r.app.thread != null) {
            sendServiceArgsLocked(r, false);
            return true;
        }
        if (!whileRestarting && r.restartDelay > 0) {
            return true;
        }
        if (DEBUG_SERVICE) Slog.v(TAG, "Bringing up service " + r.name
                + " " + r.intent);
        mRestartingServices.remove(r);
        final String appName = r.processName;
        ProcessRecord app = getProcessRecordLocked(appName, r.appInfo.uid);
        if (app != null && app.thread != null) {
            try {
                realStartServiceLocked(r, app);
                return true;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception when starting service " + r.shortName, e);
            }
        }
        if (startProcessLocked(appName, r.appInfo, true, intentFlags,
                "service", r.name, false) == null) {
            Slog.w(TAG, "Unable to launch app "
                    + r.appInfo.packageName + "/"
                    + r.appInfo.uid + " for service "
                    + r.intent.getIntent() + ": process is bad");
            bringDownServiceLocked(r, true);
            return false;
        }
        if (!mPendingServices.contains(r)) {
            mPendingServices.add(r);
        }
        return true;
    }
    private final void bringDownServiceLocked(ServiceRecord r, boolean force) {
        if (!force && r.startRequested) {
            return;
        }
        if (r.connections.size() > 0) {
            if (!force) {
                Iterator<ConnectionRecord> it = r.connections.values().iterator();
                while (it.hasNext()) {
                    ConnectionRecord cr = it.next();
                    if ((cr.flags&Context.BIND_AUTO_CREATE) != 0) {
                        return;
                    }
                }
            }
            Iterator<ConnectionRecord> it = r.connections.values().iterator();
            while (it.hasNext()) {
                ConnectionRecord c = it.next();
                try {
                    c.conn.connected(r.name, null);
                } catch (Exception e) {
                    Slog.w(TAG, "Failure disconnecting service " + r.name +
                          " to connection " + c.conn.asBinder() +
                          " (in " + c.binding.client.processName + ")", e);
                }
            }
        }
        if (r.bindings.size() > 0 && r.app != null && r.app.thread != null) {
            Iterator<IntentBindRecord> it = r.bindings.values().iterator();
            while (it.hasNext()) {
                IntentBindRecord ibr = it.next();
                if (DEBUG_SERVICE) Slog.v(TAG, "Bringing down binding " + ibr
                        + ": hasBound=" + ibr.hasBound);
                if (r.app != null && r.app.thread != null && ibr.hasBound) {
                    try {
                        bumpServiceExecutingLocked(r);
                        updateOomAdjLocked(r.app);
                        ibr.hasBound = false;
                        r.app.thread.scheduleUnbindService(r,
                                ibr.intent.getIntent());
                    } catch (Exception e) {
                        Slog.w(TAG, "Exception when unbinding service "
                                + r.shortName, e);
                        serviceDoneExecutingLocked(r, true);
                    }
                }
            }
        }
        if (DEBUG_SERVICE) Slog.v(TAG, "Bringing down service " + r.name
                 + " " + r.intent);
        EventLog.writeEvent(EventLogTags.AM_DESTROY_SERVICE,
                System.identityHashCode(r), r.shortName,
                (r.app != null) ? r.app.pid : -1);
        mServices.remove(r.name);
        mServicesByIntent.remove(r.intent);
        if (localLOGV) Slog.v(TAG, "BRING DOWN SERVICE: " + r.shortName);
        r.totalRestartCount = 0;
        unscheduleServiceRestartLocked(r);
        int N = mPendingServices.size();
        for (int i=0; i<N; i++) {
            if (mPendingServices.get(i) == r) {
                mPendingServices.remove(i);
                if (DEBUG_SERVICE) Slog.v(
                    TAG, "Removed pending service: " + r.shortName);
                i--;
                N--;
            }
        }
        r.cancelNotification();
        r.isForeground = false;
        r.foregroundId = 0;
        r.foregroundNoti = null;
        r.deliveredStarts.clear();
        r.pendingStarts.clear();
        if (r.app != null) {
            synchronized (r.stats.getBatteryStats()) {
                r.stats.stopLaunchedLocked();
            }
            r.app.services.remove(r);
            if (r.app.thread != null) {
                try {
                    if (DEBUG_SERVICE) Slog.v(TAG,
                            "Stopping service: " + r.shortName);
                    bumpServiceExecutingLocked(r);
                    mStoppingServices.add(r);
                    updateOomAdjLocked(r.app);
                    r.app.thread.scheduleStopService(r);
                } catch (Exception e) {
                    Slog.w(TAG, "Exception when stopping service "
                            + r.shortName, e);
                    serviceDoneExecutingLocked(r, true);
                }
                updateServiceForegroundLocked(r.app, false);
            } else {
                if (DEBUG_SERVICE) Slog.v(
                    TAG, "Removed service that has no process: " + r.shortName);
            }
        } else {
            if (DEBUG_SERVICE) Slog.v(
                TAG, "Removed service that is not running: " + r.shortName);
        }
    }
    ComponentName startServiceLocked(IApplicationThread caller,
            Intent service, String resolvedType,
            int callingPid, int callingUid) {
        synchronized(this) {
            if (DEBUG_SERVICE) Slog.v(TAG, "startService: " + service
                    + " type=" + resolvedType + " args=" + service.getExtras());
            if (caller != null) {
                final ProcessRecord callerApp = getRecordForAppLocked(caller);
                if (callerApp == null) {
                    throw new SecurityException(
                            "Unable to find app for caller " + caller
                            + " (pid=" + Binder.getCallingPid()
                            + ") when starting service " + service);
                }
            }
            ServiceLookupResult res =
                retrieveServiceLocked(service, resolvedType,
                        callingPid, callingUid);
            if (res == null) {
                return null;
            }
            if (res.record == null) {
                return new ComponentName("!", res.permission != null
                        ? res.permission : "private to package");
            }
            ServiceRecord r = res.record;
            if (unscheduleServiceRestartLocked(r)) {
                if (DEBUG_SERVICE) Slog.v(TAG, "START SERVICE WHILE RESTART PENDING: "
                        + r.shortName);
            }
            r.startRequested = true;
            r.callStart = false;
            r.lastStartId++;
            if (r.lastStartId < 1) {
                r.lastStartId = 1;
            }
            r.pendingStarts.add(new ServiceRecord.StartItem(r.lastStartId, service));
            r.lastActivity = SystemClock.uptimeMillis();
            synchronized (r.stats.getBatteryStats()) {
                r.stats.startRunningLocked();
            }
            if (!bringUpServiceLocked(r, service.getFlags(), false)) {
                return new ComponentName("!", "Service process is bad");
            }
            return r.name;
        }
    }
    public ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType) {
        if (service != null && service.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            final int callingPid = Binder.getCallingPid();
            final int callingUid = Binder.getCallingUid();
            final long origId = Binder.clearCallingIdentity();
            ComponentName res = startServiceLocked(caller, service,
                    resolvedType, callingPid, callingUid);
            Binder.restoreCallingIdentity(origId);
            return res;
        }
    }
    ComponentName startServiceInPackage(int uid,
            Intent service, String resolvedType) {
        synchronized(this) {
            final long origId = Binder.clearCallingIdentity();
            ComponentName res = startServiceLocked(null, service,
                    resolvedType, -1, uid);
            Binder.restoreCallingIdentity(origId);
            return res;
        }
    }
    public int stopService(IApplicationThread caller, Intent service,
            String resolvedType) {
        if (service != null && service.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            if (DEBUG_SERVICE) Slog.v(TAG, "stopService: " + service
                    + " type=" + resolvedType);
            final ProcessRecord callerApp = getRecordForAppLocked(caller);
            if (caller != null && callerApp == null) {
                throw new SecurityException(
                        "Unable to find app for caller " + caller
                        + " (pid=" + Binder.getCallingPid()
                        + ") when stopping service " + service);
            }
            ServiceLookupResult r = findServiceLocked(service, resolvedType);
            if (r != null) {
                if (r.record != null) {
                    synchronized (r.record.stats.getBatteryStats()) {
                        r.record.stats.stopRunningLocked();
                    }
                    r.record.startRequested = false;
                    r.record.callStart = false;
                    final long origId = Binder.clearCallingIdentity();
                    bringDownServiceLocked(r.record, false);
                    Binder.restoreCallingIdentity(origId);
                    return 1;
                }
                return -1;
            }
        }
        return 0;
    }
    public IBinder peekService(Intent service, String resolvedType) {
        if (service != null && service.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        IBinder ret = null;
        synchronized(this) {
            ServiceLookupResult r = findServiceLocked(service, resolvedType);
            if (r != null) {
                if (r.record == null) {
                    throw new SecurityException(
                            "Permission Denial: Accessing service " + r.record.name
                            + " from pid=" + Binder.getCallingPid()
                            + ", uid=" + Binder.getCallingUid()
                            + " requires " + r.permission);
                }
                IntentBindRecord ib = r.record.bindings.get(r.record.intent);
                if (ib != null) {
                    ret = ib.binder;
                }
            }
        }
        return ret;
    }
    public boolean stopServiceToken(ComponentName className, IBinder token,
            int startId) {
        synchronized(this) {
            if (DEBUG_SERVICE) Slog.v(TAG, "stopServiceToken: " + className
                    + " " + token + " startId=" + startId);
            ServiceRecord r = findServiceLocked(className, token);
            if (r != null) {
                if (startId >= 0) {
                    ServiceRecord.StartItem si = r.findDeliveredStart(startId, false);
                    if (si != null) {
                        while (r.deliveredStarts.size() > 0) {
                            if (r.deliveredStarts.remove(0) == si) {
                                break;
                            }
                        }
                    }
                    if (r.lastStartId != startId) {
                        return false;
                    }
                    if (r.deliveredStarts.size() > 0) {
                        Slog.w(TAG, "stopServiceToken startId " + startId
                                + " is last, but have " + r.deliveredStarts.size()
                                + " remaining args");
                    }
                }
                synchronized (r.stats.getBatteryStats()) {
                    r.stats.stopRunningLocked();
                    r.startRequested = false;
                    r.callStart = false;
                }
                final long origId = Binder.clearCallingIdentity();
                bringDownServiceLocked(r, false);
                Binder.restoreCallingIdentity(origId);
                return true;
            }
        }
        return false;
    }
    public void setServiceForeground(ComponentName className, IBinder token,
            int id, Notification notification, boolean removeNotification) {
        final long origId = Binder.clearCallingIdentity();
        try {
        synchronized(this) {
            ServiceRecord r = findServiceLocked(className, token);
            if (r != null) {
                if (id != 0) {
                    if (notification == null) {
                        throw new IllegalArgumentException("null notification");
                    }
                    if (r.foregroundId != id) {
                        r.cancelNotification();
                        r.foregroundId = id;
                    }
                    notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
                    r.foregroundNoti = notification;
                    r.isForeground = true;
                    r.postNotification();
                    if (r.app != null) {
                        updateServiceForegroundLocked(r.app, true);
                    }
                } else {
                    if (r.isForeground) {
                        r.isForeground = false;
                        if (r.app != null) {
                            updateLruProcessLocked(r.app, false, true);
                            updateServiceForegroundLocked(r.app, true);
                        }
                    }
                    if (removeNotification) {
                        r.cancelNotification();
                        r.foregroundId = 0;
                        r.foregroundNoti = null;
                    }
                }
            }
        }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }
    public void updateServiceForegroundLocked(ProcessRecord proc, boolean oomAdj) {
        boolean anyForeground = false;
        for (ServiceRecord sr : (HashSet<ServiceRecord>)proc.services) {
            if (sr.isForeground) {
                anyForeground = true;
                break;
            }
        }
        if (anyForeground != proc.foregroundServices) {
            proc.foregroundServices = anyForeground;
            if (oomAdj) {
                updateOomAdjLocked();
            }
        }
    }
    public int bindService(IApplicationThread caller, IBinder token,
            Intent service, String resolvedType,
            IServiceConnection connection, int flags) {
        if (service != null && service.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            if (DEBUG_SERVICE) Slog.v(TAG, "bindService: " + service
                    + " type=" + resolvedType + " conn=" + connection.asBinder()
                    + " flags=0x" + Integer.toHexString(flags));
            final ProcessRecord callerApp = getRecordForAppLocked(caller);
            if (callerApp == null) {
                throw new SecurityException(
                        "Unable to find app for caller " + caller
                        + " (pid=" + Binder.getCallingPid()
                        + ") when binding service " + service);
            }
            HistoryRecord activity = null;
            if (token != null) {
                int aindex = indexOfTokenLocked(token);
                if (aindex < 0) {
                    Slog.w(TAG, "Binding with unknown activity: " + token);
                    return 0;
                }
                activity = (HistoryRecord)mHistory.get(aindex);
            }
            int clientLabel = 0;
            PendingIntent clientIntent = null;
            if (callerApp.info.uid == Process.SYSTEM_UID) {
                try {
                    clientIntent = (PendingIntent)service.getParcelableExtra(
                            Intent.EXTRA_CLIENT_INTENT);
                } catch (RuntimeException e) {
                }
                if (clientIntent != null) {
                    clientLabel = service.getIntExtra(Intent.EXTRA_CLIENT_LABEL, 0);
                    if (clientLabel != 0) {
                        service = service.cloneFilter();
                    }
                }
            }
            ServiceLookupResult res =
                retrieveServiceLocked(service, resolvedType,
                        Binder.getCallingPid(), Binder.getCallingUid());
            if (res == null) {
                return 0;
            }
            if (res.record == null) {
                return -1;
            }
            ServiceRecord s = res.record;
            final long origId = Binder.clearCallingIdentity();
            if (unscheduleServiceRestartLocked(s)) {
                if (DEBUG_SERVICE) Slog.v(TAG, "BIND SERVICE WHILE RESTART PENDING: "
                        + s.shortName);
            }
            AppBindRecord b = s.retrieveAppBindingLocked(service, callerApp);
            ConnectionRecord c = new ConnectionRecord(b, activity,
                    connection, flags, clientLabel, clientIntent);
            IBinder binder = connection.asBinder();
            s.connections.put(binder, c);
            b.connections.add(c);
            if (activity != null) {
                if (activity.connections == null) {
                    activity.connections = new HashSet<ConnectionRecord>();
                }
                activity.connections.add(c);
            }
            b.client.connections.add(c);
            mServiceConnections.put(binder, c);
            if ((flags&Context.BIND_AUTO_CREATE) != 0) {
                s.lastActivity = SystemClock.uptimeMillis();
                if (!bringUpServiceLocked(s, service.getFlags(), false)) {
                    return 0;
                }
            }
            if (s.app != null) {
                updateOomAdjLocked(s.app);
            }
            if (DEBUG_SERVICE) Slog.v(TAG, "Bind " + s + " with " + b
                    + ": received=" + b.intent.received
                    + " apps=" + b.intent.apps.size()
                    + " doRebind=" + b.intent.doRebind);
            if (s.app != null && b.intent.received) {
                try {
                    c.conn.connected(s.name, b.intent.binder);
                } catch (Exception e) {
                    Slog.w(TAG, "Failure sending service " + s.shortName
                            + " to connection " + c.conn.asBinder()
                            + " (in " + c.binding.client.processName + ")", e);
                }
                if (b.intent.apps.size() == 1 && b.intent.doRebind) {
                    requestServiceBindingLocked(s, b.intent, true);
                }
            } else if (!b.intent.requested) {
                requestServiceBindingLocked(s, b.intent, false);
            }
            Binder.restoreCallingIdentity(origId);
        }
        return 1;
    }
    private void removeConnectionLocked(
        ConnectionRecord c, ProcessRecord skipApp, HistoryRecord skipAct) {
        IBinder binder = c.conn.asBinder();
        AppBindRecord b = c.binding;
        ServiceRecord s = b.service;
        s.connections.remove(binder);
        b.connections.remove(c);
        if (c.activity != null && c.activity != skipAct) {
            if (c.activity.connections != null) {
                c.activity.connections.remove(c);
            }
        }
        if (b.client != skipApp) {
            b.client.connections.remove(c);
        }
        mServiceConnections.remove(binder);
        if (b.connections.size() == 0) {
            b.intent.apps.remove(b.client);
        }
        if (DEBUG_SERVICE) Slog.v(TAG, "Disconnecting binding " + b.intent
                + ": shouldUnbind=" + b.intent.hasBound);
        if (s.app != null && s.app.thread != null && b.intent.apps.size() == 0
                && b.intent.hasBound) {
            try {
                bumpServiceExecutingLocked(s);
                updateOomAdjLocked(s.app);
                b.intent.hasBound = false;
                b.intent.doRebind = false;
                s.app.thread.scheduleUnbindService(s, b.intent.intent.getIntent());
            } catch (Exception e) {
                Slog.w(TAG, "Exception when unbinding service " + s.shortName, e);
                serviceDoneExecutingLocked(s, true);
            }
        }
        if ((c.flags&Context.BIND_AUTO_CREATE) != 0) {
            bringDownServiceLocked(s, false);
        }
    }
    public boolean unbindService(IServiceConnection connection) {
        synchronized (this) {
            IBinder binder = connection.asBinder();
            if (DEBUG_SERVICE) Slog.v(TAG, "unbindService: conn=" + binder);
            ConnectionRecord r = mServiceConnections.get(binder);
            if (r == null) {
                Slog.w(TAG, "Unbind failed: could not find connection for "
                      + connection.asBinder());
                return false;
            }
            final long origId = Binder.clearCallingIdentity();
            removeConnectionLocked(r, null, null);
            if (r.binding.service.app != null) {
                updateOomAdjLocked(r.binding.service.app);
            }
            Binder.restoreCallingIdentity(origId);
        }
        return true;
    }
    public void publishService(IBinder token, Intent intent, IBinder service) {
        if (intent != null && intent.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            if (!(token instanceof ServiceRecord)) {
                throw new IllegalArgumentException("Invalid service token");
            }
            ServiceRecord r = (ServiceRecord)token;
            final long origId = Binder.clearCallingIdentity();
            if (DEBUG_SERVICE) Slog.v(TAG, "PUBLISHING SERVICE " + r.name
                    + " " + intent + ": " + service);
            if (r != null) {
                Intent.FilterComparison filter
                        = new Intent.FilterComparison(intent);
                IntentBindRecord b = r.bindings.get(filter);
                if (b != null && !b.received) {
                    b.binder = service;
                    b.requested = true;
                    b.received = true;
                    if (r.connections.size() > 0) {
                        Iterator<ConnectionRecord> it
                                = r.connections.values().iterator();
                        while (it.hasNext()) {
                            ConnectionRecord c = it.next();
                            if (!filter.equals(c.binding.intent.intent)) {
                                if (DEBUG_SERVICE) Slog.v(
                                        TAG, "Not publishing to: " + c);
                                if (DEBUG_SERVICE) Slog.v(
                                        TAG, "Bound intent: " + c.binding.intent.intent);
                                if (DEBUG_SERVICE) Slog.v(
                                        TAG, "Published intent: " + intent);
                                continue;
                            }
                            if (DEBUG_SERVICE) Slog.v(TAG, "Publishing to: " + c);
                            try {
                                c.conn.connected(r.name, service);
                            } catch (Exception e) {
                                Slog.w(TAG, "Failure sending service " + r.name +
                                      " to connection " + c.conn.asBinder() +
                                      " (in " + c.binding.client.processName + ")", e);
                            }
                        }
                    }
                }
                serviceDoneExecutingLocked(r, mStoppingServices.contains(r));
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    public void unbindFinished(IBinder token, Intent intent, boolean doRebind) {
        if (intent != null && intent.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            if (!(token instanceof ServiceRecord)) {
                throw new IllegalArgumentException("Invalid service token");
            }
            ServiceRecord r = (ServiceRecord)token;
            final long origId = Binder.clearCallingIdentity();
            if (r != null) {
                Intent.FilterComparison filter
                        = new Intent.FilterComparison(intent);
                IntentBindRecord b = r.bindings.get(filter);
                if (DEBUG_SERVICE) Slog.v(TAG, "unbindFinished in " + r
                        + " at " + b + ": apps="
                        + (b != null ? b.apps.size() : 0));
                if (b != null) {
                    if (b.apps.size() > 0) {
                        requestServiceBindingLocked(r, b, true);
                    } else {
                        b.doRebind = true;
                    }
                }
                serviceDoneExecutingLocked(r, mStoppingServices.contains(r));
                Binder.restoreCallingIdentity(origId);
            }
        }
    }
    public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
        synchronized(this) {
            if (!(token instanceof ServiceRecord)) {
                throw new IllegalArgumentException("Invalid service token");
            }
            ServiceRecord r = (ServiceRecord)token;
            boolean inStopping = mStoppingServices.contains(token);
            if (r != null) {
                if (DEBUG_SERVICE) Slog.v(TAG, "DONE EXECUTING SERVICE " + r.name
                        + ": nesting=" + r.executeNesting
                        + ", inStopping=" + inStopping);
                if (r != token) {
                    Slog.w(TAG, "Done executing service " + r.name
                          + " with incorrect token: given " + token
                          + ", expected " + r);
                    return;
                }
                if (type == 1) {
                    r.callStart = true;
                    switch (res) {
                        case Service.START_STICKY_COMPATIBILITY:
                        case Service.START_STICKY: {
                            r.findDeliveredStart(startId, true);
                            r.stopIfKilled = false;
                            break;
                        }
                        case Service.START_NOT_STICKY: {
                            r.findDeliveredStart(startId, true);
                            if (r.lastStartId == startId) {
                                r.stopIfKilled = true;
                            }
                            break;
                        }
                        case Service.START_REDELIVER_INTENT: {
                            ServiceRecord.StartItem si = r.findDeliveredStart(startId, false);
                            if (si != null) {
                                si.deliveryCount = 0;
                                si.doneExecutingCount++;
                                r.stopIfKilled = true;
                            }
                            break;
                        }
                        default:
                            throw new IllegalArgumentException(
                                    "Unknown service start result: " + res);
                    }
                    if (res == Service.START_STICKY_COMPATIBILITY) {
                        r.callStart = false;
                    }
                }
                final long origId = Binder.clearCallingIdentity();
                serviceDoneExecutingLocked(r, inStopping);
                Binder.restoreCallingIdentity(origId);
            } else {
                Slog.w(TAG, "Done executing unknown service " + r.name
                        + " with token " + token);
            }
        }
    }
    public void serviceDoneExecutingLocked(ServiceRecord r, boolean inStopping) {
        r.executeNesting--;
        if (r.executeNesting <= 0 && r.app != null) {
            r.app.executingServices.remove(r);
            if (r.app.executingServices.size() == 0) {
                mHandler.removeMessages(SERVICE_TIMEOUT_MSG, r.app);
            }
            if (inStopping) {
                mStoppingServices.remove(r);
            }
            updateOomAdjLocked(r.app);
        }
    }
    void serviceTimeout(ProcessRecord proc) {
        String anrMessage = null;
        synchronized(this) {
            if (proc.executingServices.size() == 0 || proc.thread == null) {
                return;
            }
            long maxTime = SystemClock.uptimeMillis() - SERVICE_TIMEOUT;
            Iterator<ServiceRecord> it = proc.executingServices.iterator();
            ServiceRecord timeout = null;
            long nextTime = 0;
            while (it.hasNext()) {
                ServiceRecord sr = it.next();
                if (sr.executingStart < maxTime) {
                    timeout = sr;
                    break;
                }
                if (sr.executingStart > nextTime) {
                    nextTime = sr.executingStart;
                }
            }
            if (timeout != null && mLruProcesses.contains(proc)) {
                Slog.w(TAG, "Timeout executing service: " + timeout);
                anrMessage = "Executing service " + timeout.shortName;
            } else {
                Message msg = mHandler.obtainMessage(SERVICE_TIMEOUT_MSG);
                msg.obj = proc;
                mHandler.sendMessageAtTime(msg, nextTime+SERVICE_TIMEOUT);
            }
        }
        if (anrMessage != null) {
            appNotResponding(proc, null, null, anrMessage);
        }
    }
    public boolean bindBackupAgent(ApplicationInfo app, int backupMode) {
        if (DEBUG_BACKUP) Slog.v(TAG, "startBackupAgent: app=" + app + " mode=" + backupMode);
        enforceCallingPermission("android.permission.BACKUP", "startBackupAgent");
        synchronized(this) {
            BatteryStatsImpl.Uid.Pkg.Serv ss = null;
            BatteryStatsImpl stats = mBatteryStatsService.getActiveStatistics();
            synchronized (stats) {
                ss = stats.getServiceStatsLocked(app.uid, app.packageName, app.name);
            }
            BackupRecord r = new BackupRecord(ss, app, backupMode);
            ComponentName hostingName = new ComponentName(app.packageName, app.backupAgentName);
            ProcessRecord proc = startProcessLocked(app.processName, app,
                    false, 0, "backup", hostingName, false);
            if (proc == null) {
                Slog.e(TAG, "Unable to start backup agent process " + r);
                return false;
            }
            r.app = proc;
            mBackupTarget = r;
            mBackupAppName = app.packageName;
            updateOomAdjLocked(proc);
            if (proc.thread != null) {
                if (DEBUG_BACKUP) Slog.v(TAG, "Agent proc already running: " + proc);
                try {
                    proc.thread.scheduleCreateBackupAgent(app, backupMode);
                } catch (RemoteException e) {
                }
            } else {
                if (DEBUG_BACKUP) Slog.v(TAG, "Agent proc not running, waiting for attach");
            }
        }
        return true;
    }
    public void backupAgentCreated(String agentPackageName, IBinder agent) {
        if (DEBUG_BACKUP) Slog.v(TAG, "backupAgentCreated: " + agentPackageName
                + " = " + agent);
        synchronized(this) {
            if (!agentPackageName.equals(mBackupAppName)) {
                Slog.e(TAG, "Backup agent created for " + agentPackageName + " but not requested!");
                return;
            }
            long oldIdent = Binder.clearCallingIdentity();
            try {
                IBackupManager bm = IBackupManager.Stub.asInterface(
                        ServiceManager.getService(Context.BACKUP_SERVICE));
                bm.agentConnected(agentPackageName, agent);
            } catch (RemoteException e) {
            } catch (Exception e) {
                Slog.w(TAG, "Exception trying to deliver BackupAgent binding: ");
                e.printStackTrace();
            } finally {
                Binder.restoreCallingIdentity(oldIdent);
            }
        }
    }
    public void unbindBackupAgent(ApplicationInfo appInfo) {
        if (DEBUG_BACKUP) Slog.v(TAG, "unbindBackupAgent: " + appInfo);
        if (appInfo == null) {
            Slog.w(TAG, "unbind backup agent for null app");
            return;
        }
        synchronized(this) {
            if (mBackupAppName == null) {
                Slog.w(TAG, "Unbinding backup agent with no active backup");
                return;
            }
            if (!mBackupAppName.equals(appInfo.packageName)) {
                Slog.e(TAG, "Unbind of " + appInfo + " but is not the current backup target");
                return;
            }
            ProcessRecord proc = mBackupTarget.app;
            mBackupTarget = null;
            mBackupAppName = null;
            updateOomAdjLocked(proc);
            if (proc.thread != null) {
                try {
                    proc.thread.scheduleDestroyBackupAgent(appInfo);
                } catch (Exception e) {
                    Slog.e(TAG, "Exception when unbinding backup agent:");
                    e.printStackTrace();
                }
            }
        }
    }
    private final List getStickiesLocked(String action, IntentFilter filter,
            List cur) {
        final ContentResolver resolver = mContext.getContentResolver();
        final ArrayList<Intent> list = mStickyBroadcasts.get(action);
        if (list == null) {
            return cur;
        }
        int N = list.size();
        for (int i=0; i<N; i++) {
            Intent intent = list.get(i);
            if (filter.match(resolver, intent, true, TAG) >= 0) {
                if (cur == null) {
                    cur = new ArrayList<Intent>();
                }
                cur.add(intent);
            }
        }
        return cur;
    }
    private final void scheduleBroadcastsLocked() {
        if (DEBUG_BROADCAST) Slog.v(TAG, "Schedule broadcasts: current="
                + mBroadcastsScheduled);
        if (mBroadcastsScheduled) {
            return;
        }
        mHandler.sendEmptyMessage(BROADCAST_INTENT_MSG);
        mBroadcastsScheduled = true;
    }
    public Intent registerReceiver(IApplicationThread caller,
            IIntentReceiver receiver, IntentFilter filter, String permission) {
        synchronized(this) {
            ProcessRecord callerApp = null;
            if (caller != null) {
                callerApp = getRecordForAppLocked(caller);
                if (callerApp == null) {
                    throw new SecurityException(
                            "Unable to find app for caller " + caller
                            + " (pid=" + Binder.getCallingPid()
                            + ") when registering receiver " + receiver);
                }
            }
            List allSticky = null;
            Iterator actions = filter.actionsIterator();
            if (actions != null) {
                while (actions.hasNext()) {
                    String action = (String)actions.next();
                    allSticky = getStickiesLocked(action, filter, allSticky);
                }
            } else {
                allSticky = getStickiesLocked(null, filter, allSticky);
            }
            Intent sticky = allSticky != null ? (Intent)allSticky.get(0) : null;
            if (DEBUG_BROADCAST) Slog.v(TAG, "Register receiver " + filter
                    + ": " + sticky);
            if (receiver == null) {
                return sticky;
            }
            ReceiverList rl
                = (ReceiverList)mRegisteredReceivers.get(receiver.asBinder());
            if (rl == null) {
                rl = new ReceiverList(this, callerApp,
                        Binder.getCallingPid(),
                        Binder.getCallingUid(), receiver);
                if (rl.app != null) {
                    rl.app.receivers.add(rl);
                } else {
                    try {
                        receiver.asBinder().linkToDeath(rl, 0);
                    } catch (RemoteException e) {
                        return sticky;
                    }
                    rl.linkedToDeath = true;
                }
                mRegisteredReceivers.put(receiver.asBinder(), rl);
            }
            BroadcastFilter bf = new BroadcastFilter(filter, rl, permission);
            rl.add(bf);
            if (!bf.debugCheck()) {
                Slog.w(TAG, "==> For Dynamic broadast");
            }
            mReceiverResolver.addFilter(bf);
            if (allSticky != null) {
                ArrayList receivers = new ArrayList();
                receivers.add(bf);
                int N = allSticky.size();
                for (int i=0; i<N; i++) {
                    Intent intent = (Intent)allSticky.get(i);
                    BroadcastRecord r = new BroadcastRecord(intent, null,
                            null, -1, -1, null, receivers, null, 0, null, null,
                            false, true, true);
                    if (mParallelBroadcasts.size() == 0) {
                        scheduleBroadcastsLocked();
                    }
                    mParallelBroadcasts.add(r);
                }
            }
            return sticky;
        }
    }
    public void unregisterReceiver(IIntentReceiver receiver) {
        if (DEBUG_BROADCAST) Slog.v(TAG, "Unregister receiver: " + receiver);
        boolean doNext = false;
        synchronized(this) {
            ReceiverList rl
                = (ReceiverList)mRegisteredReceivers.get(receiver.asBinder());
            if (rl != null) {
                if (rl.curBroadcast != null) {
                    BroadcastRecord r = rl.curBroadcast;
                    doNext = finishReceiverLocked(
                        receiver.asBinder(), r.resultCode, r.resultData,
                        r.resultExtras, r.resultAbort, true);
                }
                if (rl.app != null) {
                    rl.app.receivers.remove(rl);
                }
                removeReceiverLocked(rl);
                if (rl.linkedToDeath) {
                    rl.linkedToDeath = false;
                    rl.receiver.asBinder().unlinkToDeath(rl, 0);
                }
            }
        }
        if (!doNext) {
            return;
        }
        final long origId = Binder.clearCallingIdentity();
        processNextBroadcast(false);
        trimApplications();
        Binder.restoreCallingIdentity(origId);
    }
    void removeReceiverLocked(ReceiverList rl) {
        mRegisteredReceivers.remove(rl.receiver.asBinder());
        int N = rl.size();
        for (int i=0; i<N; i++) {
            mReceiverResolver.removeFilter(rl.get(i));
        }
    }
    private final void sendPackageBroadcastLocked(int cmd, String[] packages) {
        for (int i = mLruProcesses.size() - 1 ; i >= 0 ; i--) {
            ProcessRecord r = mLruProcesses.get(i);
            if (r.thread != null) {
                try {
                    r.thread.dispatchPackageBroadcast(cmd, packages);
                } catch (RemoteException ex) {
                }
            }
        }
    }
    private final int broadcastIntentLocked(ProcessRecord callerApp,
            String callerPackage, Intent intent, String resolvedType,
            IIntentReceiver resultTo, int resultCode, String resultData,
            Bundle map, String requiredPermission,
            boolean ordered, boolean sticky, int callingPid, int callingUid) {
        intent = new Intent(intent);
        if (DEBUG_BROADCAST_LIGHT) Slog.v(
            TAG, (sticky ? "Broadcast sticky: ": "Broadcast: ") + intent
            + " ordered=" + ordered);
        if ((resultTo != null) && !ordered) {
            Slog.w(TAG, "Broadcast " + intent + " not ordered but result callback requested!");
        }
        final boolean uidRemoved = intent.ACTION_UID_REMOVED.equals(
                intent.getAction());
        if (intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())
                || intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())
                || Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(intent.getAction())
                || uidRemoved) {
            if (checkComponentPermission(
                    android.Manifest.permission.BROADCAST_PACKAGE_REMOVED,
                    callingPid, callingUid, -1)
                    == PackageManager.PERMISSION_GRANTED) {
                if (uidRemoved) {
                    final Bundle intentExtras = intent.getExtras();
                    final int uid = intentExtras != null
                            ? intentExtras.getInt(Intent.EXTRA_UID) : -1;
                    if (uid >= 0) {
                        BatteryStatsImpl bs = mBatteryStatsService.getActiveStatistics();
                        synchronized (bs) {
                            bs.removeUidStatsLocked(uid);
                        }
                    }
                } else {
                    if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(intent.getAction())) {
                        String list[] = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                        if (list != null && (list.length > 0)) {
                            for (String pkg : list) {
                                forceStopPackageLocked(pkg, -1, false, true, true);
                            }
                            sendPackageBroadcastLocked(
                                    IApplicationThread.EXTERNAL_STORAGE_UNAVAILABLE, list);
                        }
                    } else {
                        Uri data = intent.getData();
                        String ssp;
                        if (data != null && (ssp=data.getSchemeSpecificPart()) != null) {
                            if (!intent.getBooleanExtra(Intent.EXTRA_DONT_KILL_APP, false)) {
                                forceStopPackageLocked(ssp,
                                        intent.getIntExtra(Intent.EXTRA_UID, -1), false, true, true);
                            }
                            if (intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                                sendPackageBroadcastLocked(IApplicationThread.PACKAGE_REMOVED,
                                        new String[] {ssp});
                            }
                        }
                    }
                }
            } else {
                String msg = "Permission Denial: " + intent.getAction()
                        + " broadcast from " + callerPackage + " (pid=" + callingPid
                        + ", uid=" + callingUid + ")"
                        + " requires "
                        + android.Manifest.permission.BROADCAST_PACKAGE_REMOVED;
                Slog.w(TAG, msg);
                throw new SecurityException(msg);
            }
        }
        if (intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
            mHandler.sendEmptyMessage(UPDATE_TIME_ZONE);
        }
        if (callingUid == Process.SYSTEM_UID || callingUid == Process.PHONE_UID
                || callingUid == Process.SHELL_UID || callingUid == 0) {
        } else if (callerApp == null || !callerApp.persistent) {
            try {
                if (ActivityThread.getPackageManager().isProtectedBroadcast(
                        intent.getAction())) {
                    String msg = "Permission Denial: not allowed to send broadcast "
                            + intent.getAction() + " from pid="
                            + callingPid + ", uid=" + callingUid;
                    Slog.w(TAG, msg);
                    throw new SecurityException(msg);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception", e);
                return BROADCAST_SUCCESS;
            }
        }
        if (sticky) {
            if (checkPermission(android.Manifest.permission.BROADCAST_STICKY,
                    callingPid, callingUid)
                    != PackageManager.PERMISSION_GRANTED) {
                String msg = "Permission Denial: broadcastIntent() requesting a sticky broadcast from pid="
                        + callingPid + ", uid=" + callingUid
                        + " requires " + android.Manifest.permission.BROADCAST_STICKY;
                Slog.w(TAG, msg);
                throw new SecurityException(msg);
            }
            if (requiredPermission != null) {
                Slog.w(TAG, "Can't broadcast sticky intent " + intent
                        + " and enforce permission " + requiredPermission);
                return BROADCAST_STICKY_CANT_HAVE_PERMISSION;
            }
            if (intent.getComponent() != null) {
                throw new SecurityException(
                        "Sticky broadcasts can't target a specific component");
            }
            ArrayList<Intent> list = mStickyBroadcasts.get(intent.getAction());
            if (list == null) {
                list = new ArrayList<Intent>();
                mStickyBroadcasts.put(intent.getAction(), list);
            }
            int N = list.size();
            int i;
            for (i=0; i<N; i++) {
                if (intent.filterEquals(list.get(i))) {
                    list.set(i, new Intent(intent));
                    break;
                }
            }
            if (i >= N) {
                list.add(new Intent(intent));
            }
        }
        List receivers = null;
        List<BroadcastFilter> registeredReceivers = null;
        try {
            if (intent.getComponent() != null) {
                ActivityInfo ai = ActivityThread.getPackageManager().
                    getReceiverInfo(intent.getComponent(), STOCK_PM_FLAGS);
                if (ai != null) {
                    receivers = new ArrayList();
                    ResolveInfo ri = new ResolveInfo();
                    ri.activityInfo = ai;
                    receivers.add(ri);
                }
            } else {
                if ((intent.getFlags()&Intent.FLAG_RECEIVER_REGISTERED_ONLY)
                         == 0) {
                    receivers =
                        ActivityThread.getPackageManager().queryIntentReceivers(
                                intent, resolvedType, STOCK_PM_FLAGS);
                }
                registeredReceivers = mReceiverResolver.queryIntent(intent, resolvedType, false);
            }
        } catch (RemoteException ex) {
        }
        final boolean replacePending =
                (intent.getFlags()&Intent.FLAG_RECEIVER_REPLACE_PENDING) != 0;
        if (DEBUG_BROADCAST) Slog.v(TAG, "Enqueing broadcast: " + intent.getAction()
                + " replacePending=" + replacePending);
        int NR = registeredReceivers != null ? registeredReceivers.size() : 0;
        if (!ordered && NR > 0) {
            BroadcastRecord r = new BroadcastRecord(intent, callerApp,
                    callerPackage, callingPid, callingUid, requiredPermission,
                    registeredReceivers, resultTo, resultCode, resultData, map,
                    ordered, sticky, false);
            if (DEBUG_BROADCAST) Slog.v(
                    TAG, "Enqueueing parallel broadcast " + r
                    + ": prev had " + mParallelBroadcasts.size());
            boolean replaced = false;
            if (replacePending) {
                for (int i=mParallelBroadcasts.size()-1; i>=0; i--) {
                    if (intent.filterEquals(mParallelBroadcasts.get(i).intent)) {
                        if (DEBUG_BROADCAST) Slog.v(TAG,
                                "***** DROPPING PARALLEL: " + intent);
                        mParallelBroadcasts.set(i, r);
                        replaced = true;
                        break;
                    }
                }
            }
            if (!replaced) {
                mParallelBroadcasts.add(r);
                scheduleBroadcastsLocked();
            }
            registeredReceivers = null;
            NR = 0;
        }
        int ir = 0;
        if (receivers != null) {
            String skipPackages[] = null;
            if (intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                    || intent.ACTION_PACKAGE_RESTARTED.equals(intent.getAction())
                    || intent.ACTION_PACKAGE_DATA_CLEARED.equals(intent.getAction())) {
                Uri data = intent.getData();
                if (data != null) {
                    String pkgName = data.getSchemeSpecificPart();
                    if (pkgName != null) {
                        skipPackages = new String[] { pkgName };
                    }
                }
            } else if (intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(intent.getAction())) {
                skipPackages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            }
            if (skipPackages != null && (skipPackages.length > 0)) {
                for (String skipPackage : skipPackages) {
                    if (skipPackage != null) {
                        int NT = receivers.size();
                        for (int it=0; it<NT; it++) {
                            ResolveInfo curt = (ResolveInfo)receivers.get(it);
                            if (curt.activityInfo.packageName.equals(skipPackage)) {
                                receivers.remove(it);
                                it--;
                                NT--;
                            }
                        }
                    }
                }
            }
            int NT = receivers != null ? receivers.size() : 0;
            int it = 0;
            ResolveInfo curt = null;
            BroadcastFilter curr = null;
            while (it < NT && ir < NR) {
                if (curt == null) {
                    curt = (ResolveInfo)receivers.get(it);
                }
                if (curr == null) {
                    curr = registeredReceivers.get(ir);
                }
                if (curr.getPriority() >= curt.priority) {
                    receivers.add(it, curr);
                    ir++;
                    curr = null;
                    it++;
                    NT++;
                } else {
                    it++;
                    curt = null;
                }
            }
        }
        while (ir < NR) {
            if (receivers == null) {
                receivers = new ArrayList();
            }
            receivers.add(registeredReceivers.get(ir));
            ir++;
        }
        if ((receivers != null && receivers.size() > 0)
                || resultTo != null) {
            BroadcastRecord r = new BroadcastRecord(intent, callerApp,
                    callerPackage, callingPid, callingUid, requiredPermission,
                    receivers, resultTo, resultCode, resultData, map, ordered,
                    sticky, false);
            if (DEBUG_BROADCAST) Slog.v(
                    TAG, "Enqueueing ordered broadcast " + r
                    + ": prev had " + mOrderedBroadcasts.size());
            if (DEBUG_BROADCAST) {
                int seq = r.intent.getIntExtra("seq", -1);
                Slog.i(TAG, "Enqueueing broadcast " + r.intent.getAction() + " seq=" + seq);
            }
            boolean replaced = false;
            if (replacePending) {
                for (int i=mOrderedBroadcasts.size()-1; i>=0; i--) {
                    if (intent.filterEquals(mOrderedBroadcasts.get(i).intent)) {
                        if (DEBUG_BROADCAST) Slog.v(TAG,
                                "***** DROPPING ORDERED: " + intent);
                        mOrderedBroadcasts.set(i, r);
                        replaced = true;
                        break;
                    }
                }
            }
            if (!replaced) {
                mOrderedBroadcasts.add(r);
                scheduleBroadcastsLocked();
            }
        }
        return BROADCAST_SUCCESS;
    }
    public final int broadcastIntent(IApplicationThread caller,
            Intent intent, String resolvedType, IIntentReceiver resultTo,
            int resultCode, String resultData, Bundle map,
            String requiredPermission, boolean serialized, boolean sticky) {
        if (intent != null && intent.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            int flags = intent.getFlags();
            if (!mSystemReady) {
                if ((flags&Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT) != 0) {
                    intent = new Intent(intent);
                    intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
                } else if ((flags&Intent.FLAG_RECEIVER_REGISTERED_ONLY) == 0){
                    Slog.e(TAG, "Attempt to launch receivers of broadcast intent " + intent
                            + " before boot completion");
                    throw new IllegalStateException("Cannot broadcast before boot completed");
                }
            }
            if ((flags&Intent.FLAG_RECEIVER_BOOT_UPGRADE) != 0) {
                throw new IllegalArgumentException(
                        "Can't use FLAG_RECEIVER_BOOT_UPGRADE here");
            }
            final ProcessRecord callerApp = getRecordForAppLocked(caller);
            final int callingPid = Binder.getCallingPid();
            final int callingUid = Binder.getCallingUid();
            final long origId = Binder.clearCallingIdentity();
            int res = broadcastIntentLocked(callerApp,
                    callerApp != null ? callerApp.info.packageName : null,
                    intent, resolvedType, resultTo,
                    resultCode, resultData, map, requiredPermission, serialized,
                    sticky, callingPid, callingUid);
            Binder.restoreCallingIdentity(origId);
            return res;
        }
    }
    int broadcastIntentInPackage(String packageName, int uid,
            Intent intent, String resolvedType, IIntentReceiver resultTo,
            int resultCode, String resultData, Bundle map,
            String requiredPermission, boolean serialized, boolean sticky) {
        synchronized(this) {
            final long origId = Binder.clearCallingIdentity();
            int res = broadcastIntentLocked(null, packageName, intent, resolvedType,
                    resultTo, resultCode, resultData, map, requiredPermission,
                    serialized, sticky, -1, uid);
            Binder.restoreCallingIdentity(origId);
            return res;
        }
    }
    public final void unbroadcastIntent(IApplicationThread caller,
            Intent intent) {
        if (intent != null && intent.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            if (checkCallingPermission(android.Manifest.permission.BROADCAST_STICKY)
                    != PackageManager.PERMISSION_GRANTED) {
                String msg = "Permission Denial: unbroadcastIntent() from pid="
                        + Binder.getCallingPid()
                        + ", uid=" + Binder.getCallingUid()
                        + " requires " + android.Manifest.permission.BROADCAST_STICKY;
                Slog.w(TAG, msg);
                throw new SecurityException(msg);
            }
            ArrayList<Intent> list = mStickyBroadcasts.get(intent.getAction());
            if (list != null) {
                int N = list.size();
                int i;
                for (i=0; i<N; i++) {
                    if (intent.filterEquals(list.get(i))) {
                        list.remove(i);
                        break;
                    }
                }
            }
        }
    }
    private final boolean finishReceiverLocked(IBinder receiver, int resultCode,
            String resultData, Bundle resultExtras, boolean resultAbort,
            boolean explicit) {
        if (mOrderedBroadcasts.size() == 0) {
            if (explicit) {
                Slog.w(TAG, "finishReceiver called but no pending broadcasts");
            }
            return false;
        }
        BroadcastRecord r = mOrderedBroadcasts.get(0);
        if (r.receiver == null) {
            if (explicit) {
                Slog.w(TAG, "finishReceiver called but none active");
            }
            return false;
        }
        if (r.receiver != receiver) {
            Slog.w(TAG, "finishReceiver called but active receiver is different");
            return false;
        }
        int state = r.state;
        r.state = r.IDLE;
        if (state == r.IDLE) {
            if (explicit) {
                Slog.w(TAG, "finishReceiver called but state is IDLE");
            }
        }
        r.receiver = null;
        r.intent.setComponent(null);
        if (r.curApp != null) {
            r.curApp.curReceiver = null;
        }
        if (r.curFilter != null) {
            r.curFilter.receiverList.curBroadcast = null;
        }
        r.curFilter = null;
        r.curApp = null;
        r.curComponent = null;
        r.curReceiver = null;
        mPendingBroadcast = null;
        r.resultCode = resultCode;
        r.resultData = resultData;
        r.resultExtras = resultExtras;
        r.resultAbort = resultAbort;
        return state == BroadcastRecord.APP_RECEIVE
                || state == BroadcastRecord.CALL_DONE_RECEIVE;
    }
    public void finishReceiver(IBinder who, int resultCode, String resultData,
            Bundle resultExtras, boolean resultAbort) {
        if (DEBUG_BROADCAST) Slog.v(TAG, "Finish receiver: " + who);
        if (resultExtras != null && resultExtras.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Bundle");
        }
        boolean doNext;
        final long origId = Binder.clearCallingIdentity();
        synchronized(this) {
            doNext = finishReceiverLocked(
                who, resultCode, resultData, resultExtras, resultAbort, true);
        }
        if (doNext) {
            processNextBroadcast(false);
        }
        trimApplications();
        Binder.restoreCallingIdentity(origId);
    }
    private final void logBroadcastReceiverDiscard(BroadcastRecord r) {
        if (r.nextReceiver > 0) {
            Object curReceiver = r.receivers.get(r.nextReceiver-1);
            if (curReceiver instanceof BroadcastFilter) {
                BroadcastFilter bf = (BroadcastFilter) curReceiver;
                EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_FILTER,
                        System.identityHashCode(r),
                        r.intent.getAction(),
                        r.nextReceiver - 1,
                        System.identityHashCode(bf));
            } else {
                EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP,
                        System.identityHashCode(r),
                        r.intent.getAction(),
                        r.nextReceiver - 1,
                        ((ResolveInfo)curReceiver).toString());
            }
        } else {
            Slog.w(TAG, "Discarding broadcast before first receiver is invoked: "
                    + r);
            EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP,
                    System.identityHashCode(r),
                    r.intent.getAction(),
                    r.nextReceiver,
                    "NONE");
        }
    }
    private final void broadcastTimeout() {
        ProcessRecord app = null;
        String anrMessage = null;
        synchronized (this) {
            if (mOrderedBroadcasts.size() == 0) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            BroadcastRecord r = mOrderedBroadcasts.get(0);
            if ((r.receiverTime+BROADCAST_TIMEOUT) > now) {
                if (DEBUG_BROADCAST) Slog.v(TAG,
                        "Premature timeout @ " + now + ": resetting BROADCAST_TIMEOUT_MSG for "
                        + (r.receiverTime + BROADCAST_TIMEOUT));
                Message msg = mHandler.obtainMessage(BROADCAST_TIMEOUT_MSG);
                mHandler.sendMessageAtTime(msg, r.receiverTime+BROADCAST_TIMEOUT);
                return;
            }
            Slog.w(TAG, "Timeout of broadcast " + r + " - receiver=" + r.receiver);
            r.receiverTime = now;
            r.anrCount++;
            if (r.nextReceiver <= 0) {
                Slog.w(TAG, "Timeout on receiver with nextReceiver <= 0");
                return;
            }
            Object curReceiver = r.receivers.get(r.nextReceiver-1);
            Slog.w(TAG, "Receiver during timeout: " + curReceiver);
            logBroadcastReceiverDiscard(r);
            if (curReceiver instanceof BroadcastFilter) {
                BroadcastFilter bf = (BroadcastFilter)curReceiver;
                if (bf.receiverList.pid != 0
                        && bf.receiverList.pid != MY_PID) {
                    synchronized (this.mPidsSelfLocked) {
                        app = this.mPidsSelfLocked.get(
                                bf.receiverList.pid);
                    }
                }
            } else {
                app = r.curApp;
            }
            if (app != null) {
                anrMessage = "Broadcast of " + r.intent.toString();
            }
            if (mPendingBroadcast == r) {
                mPendingBroadcast = null;
            }
            finishReceiverLocked(r.receiver, r.resultCode, r.resultData,
                    r.resultExtras, r.resultAbort, true);
            scheduleBroadcastsLocked();
        }
        if (anrMessage != null) {
            appNotResponding(app, null, null, anrMessage);
        }
    }
    private final void processCurBroadcastLocked(BroadcastRecord r,
            ProcessRecord app) throws RemoteException {
        if (app.thread == null) {
            throw new RemoteException();
        }
        r.receiver = app.thread.asBinder();
        r.curApp = app;
        app.curReceiver = r;
        updateLruProcessLocked(app, true, true);
        r.intent.setComponent(r.curComponent);
        boolean started = false;
        try {
            if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG,
                    "Delivering to component " + r.curComponent
                    + ": " + r);
            ensurePackageDexOpt(r.intent.getComponent().getPackageName());
            app.thread.scheduleReceiver(new Intent(r.intent), r.curReceiver,
                    r.resultCode, r.resultData, r.resultExtras, r.ordered);
            started = true;
        } finally {
            if (!started) {
                r.receiver = null;
                r.curApp = null;
                app.curReceiver = null;
            }
        }
    }
    static void performReceive(ProcessRecord app, IIntentReceiver receiver,
            Intent intent, int resultCode, String data, Bundle extras,
            boolean ordered, boolean sticky) throws RemoteException {
        if (app != null && app.thread != null) {
            app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode,
                    data, extras, ordered, sticky);
        } else {
            receiver.performReceive(intent, resultCode, data, extras, ordered, sticky);
        }
    }
    private final void deliverToRegisteredReceiver(BroadcastRecord r,
            BroadcastFilter filter, boolean ordered) {
        boolean skip = false;
        if (filter.requiredPermission != null) {
            int perm = checkComponentPermission(filter.requiredPermission,
                    r.callingPid, r.callingUid, -1);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission Denial: broadcasting "
                        + r.intent.toString()
                        + " from " + r.callerPackage + " (pid="
                        + r.callingPid + ", uid=" + r.callingUid + ")"
                        + " requires " + filter.requiredPermission
                        + " due to registered receiver " + filter);
                skip = true;
            }
        }
        if (r.requiredPermission != null) {
            int perm = checkComponentPermission(r.requiredPermission,
                    filter.receiverList.pid, filter.receiverList.uid, -1);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission Denial: receiving "
                        + r.intent.toString()
                        + " to " + filter.receiverList.app
                        + " (pid=" + filter.receiverList.pid
                        + ", uid=" + filter.receiverList.uid + ")"
                        + " requires " + r.requiredPermission
                        + " due to sender " + r.callerPackage
                        + " (uid " + r.callingUid + ")");
                skip = true;
            }
        }
        if (!skip) {
            if (ordered) {
                r.receiver = filter.receiverList.receiver.asBinder();
                r.curFilter = filter;
                filter.receiverList.curBroadcast = r;
                r.state = BroadcastRecord.CALL_IN_RECEIVE;
                if (filter.receiverList.app != null) {
                    r.curApp = filter.receiverList.app;
                    filter.receiverList.app.curReceiver = r;
                    updateOomAdjLocked();
                }
            }
            try {
                if (DEBUG_BROADCAST_LIGHT) {
                    int seq = r.intent.getIntExtra("seq", -1);
                    Slog.i(TAG, "Delivering to " + filter
                            + " (seq=" + seq + "): " + r);
                }
                performReceive(filter.receiverList.app, filter.receiverList.receiver,
                    new Intent(r.intent), r.resultCode,
                    r.resultData, r.resultExtras, r.ordered, r.initialSticky);
                if (ordered) {
                    r.state = BroadcastRecord.CALL_DONE_RECEIVE;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Failure sending broadcast " + r.intent, e);
                if (ordered) {
                    r.receiver = null;
                    r.curFilter = null;
                    filter.receiverList.curBroadcast = null;
                    if (filter.receiverList.app != null) {
                        filter.receiverList.app.curReceiver = null;
                    }
                }
            }
        }
    }
    private final void addBroadcastToHistoryLocked(BroadcastRecord r) {
        if (r.callingUid < 0) {
            return;
        }
        System.arraycopy(mBroadcastHistory, 0, mBroadcastHistory, 1,
                MAX_BROADCAST_HISTORY-1);
        r.finishTime = SystemClock.uptimeMillis();
        mBroadcastHistory[0] = r;
    }
    private final void processNextBroadcast(boolean fromMsg) {
        synchronized(this) {
            BroadcastRecord r;
            if (DEBUG_BROADCAST) Slog.v(TAG, "processNextBroadcast: "
                    + mParallelBroadcasts.size() + " broadcasts, "
                    + mOrderedBroadcasts.size() + " ordered broadcasts");
            updateCpuStats();
            if (fromMsg) {
                mBroadcastsScheduled = false;
            }
            while (mParallelBroadcasts.size() > 0) {
                r = mParallelBroadcasts.remove(0);
                r.dispatchTime = SystemClock.uptimeMillis();
                final int N = r.receivers.size();
                if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG, "Processing parallel broadcast "
                        + r);
                for (int i=0; i<N; i++) {
                    Object target = r.receivers.get(i);
                    if (DEBUG_BROADCAST)  Slog.v(TAG,
                            "Delivering non-ordered to registered "
                            + target + ": " + r);
                    deliverToRegisteredReceiver(r, (BroadcastFilter)target, false);
                }
                addBroadcastToHistoryLocked(r);
                if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG, "Done with parallel broadcast "
                        + r);
            }
            if (mPendingBroadcast != null) {
                if (DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG, "processNextBroadcast: waiting for "
                            + mPendingBroadcast.curApp);
                }
                boolean isDead;
                synchronized (mPidsSelfLocked) {
                    isDead = (mPidsSelfLocked.get(mPendingBroadcast.curApp.pid) == null);
                }
                if (!isDead) {
                    return;
                } else {
                    Slog.w(TAG, "pending app " + mPendingBroadcast.curApp
                            + " died before responding to broadcast");
                    mPendingBroadcast = null;
                }
            }
            boolean looped = false;
            do {
                if (mOrderedBroadcasts.size() == 0) {
                    scheduleAppGcsLocked();
                    if (looped) {
                        updateOomAdjLocked();
                    }
                    return;
                }
                r = mOrderedBroadcasts.get(0);
                boolean forceReceive = false;
                int numReceivers = (r.receivers != null) ? r.receivers.size() : 0;
                if (mSystemReady && r.dispatchTime > 0) {
                    long now = SystemClock.uptimeMillis();
                    if ((numReceivers > 0) &&
                            (now > r.dispatchTime + (2*BROADCAST_TIMEOUT*numReceivers))) {
                        Slog.w(TAG, "Hung broadcast discarded after timeout failure:"
                                + " now=" + now
                                + " dispatchTime=" + r.dispatchTime
                                + " startTime=" + r.receiverTime
                                + " intent=" + r.intent
                                + " numReceivers=" + numReceivers
                                + " nextReceiver=" + r.nextReceiver
                                + " state=" + r.state);
                        broadcastTimeout(); 
                        forceReceive = true;
                        r.state = BroadcastRecord.IDLE;
                    }
                }
                if (r.state != BroadcastRecord.IDLE) {
                    if (DEBUG_BROADCAST) Slog.d(TAG,
                            "processNextBroadcast() called when not idle (state="
                            + r.state + ")");
                    return;
                }
                if (r.receivers == null || r.nextReceiver >= numReceivers
                        || r.resultAbort || forceReceive) {
                    if (r.resultTo != null) {
                        try {
                            if (DEBUG_BROADCAST) {
                                int seq = r.intent.getIntExtra("seq", -1);
                                Slog.i(TAG, "Finishing broadcast " + r.intent.getAction()
                                        + " seq=" + seq + " app=" + r.callerApp);
                            }
                            performReceive(r.callerApp, r.resultTo,
                                new Intent(r.intent), r.resultCode,
                                r.resultData, r.resultExtras, false, false);
                        } catch (RemoteException e) {
                            Slog.w(TAG, "Failure sending broadcast result of " + r.intent, e);
                        }
                    }
                    if (DEBUG_BROADCAST) Slog.v(TAG, "Cancelling BROADCAST_TIMEOUT_MSG");
                    mHandler.removeMessages(BROADCAST_TIMEOUT_MSG);
                    if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG, "Finished with ordered broadcast "
                            + r);
                    addBroadcastToHistoryLocked(r);
                    mOrderedBroadcasts.remove(0);
                    r = null;
                    looped = true;
                    continue;
                }
            } while (r == null);
            int recIdx = r.nextReceiver++;
            r.receiverTime = SystemClock.uptimeMillis();
            if (recIdx == 0) {
                r.dispatchTime = r.receiverTime;
                if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG, "Processing ordered broadcast "
                        + r);
                if (DEBUG_BROADCAST) Slog.v(TAG,
                        "Submitting BROADCAST_TIMEOUT_MSG for "
                        + (r.receiverTime + BROADCAST_TIMEOUT));
                Message msg = mHandler.obtainMessage(BROADCAST_TIMEOUT_MSG);
                mHandler.sendMessageAtTime(msg, r.receiverTime+BROADCAST_TIMEOUT);
            }
            Object nextReceiver = r.receivers.get(recIdx);
            if (nextReceiver instanceof BroadcastFilter) {
                BroadcastFilter filter = (BroadcastFilter)nextReceiver;
                if (DEBUG_BROADCAST)  Slog.v(TAG,
                        "Delivering ordered to registered "
                        + filter + ": " + r);
                deliverToRegisteredReceiver(r, filter, r.ordered);
                if (r.receiver == null || !r.ordered) {
                    if (DEBUG_BROADCAST) Slog.v(TAG, "Quick finishing: ordered="
                            + r.ordered + " receiver=" + r.receiver);
                    r.state = BroadcastRecord.IDLE;
                    scheduleBroadcastsLocked();
                }
                return;
            }
            ResolveInfo info =
                (ResolveInfo)nextReceiver;
            boolean skip = false;
            int perm = checkComponentPermission(info.activityInfo.permission,
                    r.callingPid, r.callingUid,
                    info.activityInfo.exported
                            ? -1 : info.activityInfo.applicationInfo.uid);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                Slog.w(TAG, "Permission Denial: broadcasting "
                        + r.intent.toString()
                        + " from " + r.callerPackage + " (pid=" + r.callingPid
                        + ", uid=" + r.callingUid + ")"
                        + " requires " + info.activityInfo.permission
                        + " due to receiver " + info.activityInfo.packageName
                        + "/" + info.activityInfo.name);
                skip = true;
            }
            if (r.callingUid != Process.SYSTEM_UID &&
                r.requiredPermission != null) {
                try {
                    perm = ActivityThread.getPackageManager().
                            checkPermission(r.requiredPermission,
                                    info.activityInfo.applicationInfo.packageName);
                } catch (RemoteException e) {
                    perm = PackageManager.PERMISSION_DENIED;
                }
                if (perm != PackageManager.PERMISSION_GRANTED) {
                    Slog.w(TAG, "Permission Denial: receiving "
                            + r.intent + " to "
                            + info.activityInfo.applicationInfo.packageName
                            + " requires " + r.requiredPermission
                            + " due to sender " + r.callerPackage
                            + " (uid " + r.callingUid + ")");
                    skip = true;
                }
            }
            if (r.curApp != null && r.curApp.crashing) {
                skip = true;
            }
            if (skip) {
                r.receiver = null;
                r.curFilter = null;
                r.state = BroadcastRecord.IDLE;
                scheduleBroadcastsLocked();
                return;
            }
            r.state = BroadcastRecord.APP_RECEIVE;
            String targetProcess = info.activityInfo.processName;
            r.curComponent = new ComponentName(
                    info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name);
            r.curReceiver = info.activityInfo;
            ProcessRecord app = getProcessRecordLocked(targetProcess,
                    info.activityInfo.applicationInfo.uid);
            if (app != null && app.thread != null) {
                try {
                    processCurBroadcastLocked(r, app);
                    return;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception when sending broadcast to "
                          + r.curComponent, e);
                }
            }
            if ((r.curApp=startProcessLocked(targetProcess,
                    info.activityInfo.applicationInfo, true,
                    r.intent.getFlags() | Intent.FLAG_FROM_BACKGROUND,
                    "broadcast", r.curComponent,
                    (r.intent.getFlags()&Intent.FLAG_RECEIVER_BOOT_UPGRADE) != 0))
                            == null) {
                Slog.w(TAG, "Unable to launch app "
                        + info.activityInfo.applicationInfo.packageName + "/"
                        + info.activityInfo.applicationInfo.uid + " for broadcast "
                        + r.intent + ": process is bad");
                logBroadcastReceiverDiscard(r);
                finishReceiverLocked(r.receiver, r.resultCode, r.resultData,
                        r.resultExtras, r.resultAbort, true);
                scheduleBroadcastsLocked();
                r.state = BroadcastRecord.IDLE;
                return;
            }
            mPendingBroadcast = r;
        }
    }
    public boolean startInstrumentation(ComponentName className,
            String profileFile, int flags, Bundle arguments,
            IInstrumentationWatcher watcher) {
        if (arguments != null && arguments.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Bundle");
        }
        synchronized(this) {
            InstrumentationInfo ii = null;
            ApplicationInfo ai = null;
            try {
                ii = mContext.getPackageManager().getInstrumentationInfo(
                    className, STOCK_PM_FLAGS);
                ai = mContext.getPackageManager().getApplicationInfo(
                    ii.targetPackage, STOCK_PM_FLAGS);
            } catch (PackageManager.NameNotFoundException e) {
            }
            if (ii == null) {
                reportStartInstrumentationFailure(watcher, className,
                        "Unable to find instrumentation info for: " + className);
                return false;
            }
            if (ai == null) {
                reportStartInstrumentationFailure(watcher, className,
                        "Unable to find instrumentation target package: " + ii.targetPackage);
                return false;
            }
            int match = mContext.getPackageManager().checkSignatures(
                    ii.targetPackage, ii.packageName);
            if (match < 0 && match != PackageManager.SIGNATURE_FIRST_NOT_SIGNED) {
                String msg = "Permission Denial: starting instrumentation "
                        + className + " from pid="
                        + Binder.getCallingPid()
                        + ", uid=" + Binder.getCallingPid()
                        + " not allowed because package " + ii.packageName
                        + " does not have a signature matching the target "
                        + ii.targetPackage;
                reportStartInstrumentationFailure(watcher, className, msg);
                throw new SecurityException(msg);
            }
            final long origId = Binder.clearCallingIdentity();
            forceStopPackageLocked(ii.targetPackage, -1, true, false, true);
            ProcessRecord app = addAppLocked(ai);
            app.instrumentationClass = className;
            app.instrumentationInfo = ai;
            app.instrumentationProfileFile = profileFile;
            app.instrumentationArguments = arguments;
            app.instrumentationWatcher = watcher;
            app.instrumentationResultClass = className;
            Binder.restoreCallingIdentity(origId);
        }
        return true;
    }
    private void reportStartInstrumentationFailure(IInstrumentationWatcher watcher, 
            ComponentName cn, String report) {
        Slog.w(TAG, report);
        try {
            if (watcher != null) {
                Bundle results = new Bundle();
                results.putString(Instrumentation.REPORT_KEY_IDENTIFIER, "ActivityManagerService");
                results.putString("Error", report);
                watcher.instrumentationStatus(cn, -1, results);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, e);
        }
    }
    void finishInstrumentationLocked(ProcessRecord app, int resultCode, Bundle results) {
        if (app.instrumentationWatcher != null) {
            try {
                app.instrumentationWatcher.instrumentationFinished(
                    app.instrumentationClass,
                    resultCode,
                    results);
            } catch (RemoteException e) {
            }
        }
        app.instrumentationWatcher = null;
        app.instrumentationClass = null;
        app.instrumentationInfo = null;
        app.instrumentationProfileFile = null;
        app.instrumentationArguments = null;
        forceStopPackageLocked(app.processName, -1, false, false, true);
    }
    public void finishInstrumentation(IApplicationThread target,
            int resultCode, Bundle results) {
        if (results != null && results.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        synchronized(this) {
            ProcessRecord app = getRecordForAppLocked(target);
            if (app == null) {
                Slog.w(TAG, "finishInstrumentation: no app for " + target);
                return;
            }
            final long origId = Binder.clearCallingIdentity();
            finishInstrumentationLocked(app, resultCode, results);
            Binder.restoreCallingIdentity(origId);
        }
    }
    public ConfigurationInfo getDeviceConfigurationInfo() {
        ConfigurationInfo config = new ConfigurationInfo();
        synchronized (this) {
            config.reqTouchScreen = mConfiguration.touchscreen;
            config.reqKeyboardType = mConfiguration.keyboard;
            config.reqNavigation = mConfiguration.navigation;
            if (mConfiguration.navigation == Configuration.NAVIGATION_DPAD
                    || mConfiguration.navigation == Configuration.NAVIGATION_TRACKBALL) {
                config.reqInputFeatures |= ConfigurationInfo.INPUT_FEATURE_FIVE_WAY_NAV;
            }
            if (mConfiguration.keyboard != Configuration.KEYBOARD_UNDEFINED
                    && mConfiguration.keyboard != Configuration.KEYBOARD_NOKEYS) {
                config.reqInputFeatures |= ConfigurationInfo.INPUT_FEATURE_HARD_KEYBOARD;
            }
            config.reqGlEsVersion = GL_ES_VERSION;
        }
        return config;
    }
    public Configuration getConfiguration() {
        Configuration ci;
        synchronized(this) {
            ci = new Configuration(mConfiguration);
        }
        return ci;
    }
    public void updateConfiguration(Configuration values) {
        enforceCallingPermission(android.Manifest.permission.CHANGE_CONFIGURATION,
                "updateConfiguration()");
        synchronized(this) {
            if (values == null && mWindowManager != null) {
                values = mWindowManager.computeNewConfiguration();
            }
            final long origId = Binder.clearCallingIdentity();
            updateConfigurationLocked(values, null);
            Binder.restoreCallingIdentity(origId);
        }
    }
    public boolean updateConfigurationLocked(Configuration values,
            HistoryRecord starting) {
        int changes = 0;
        boolean kept = true;
        if (values != null) {
            Configuration newConfig = new Configuration(mConfiguration);
            changes = newConfig.updateFrom(values);
            if (changes != 0) {
                if (DEBUG_SWITCH || DEBUG_CONFIGURATION) {
                    Slog.i(TAG, "Updating configuration to: " + values);
                }
                EventLog.writeEvent(EventLogTags.CONFIGURATION_CHANGED, changes);
                if (values.locale != null) {
                    saveLocaleLocked(values.locale, 
                                     !values.locale.equals(mConfiguration.locale),
                                     values.userSetLocale);
                }
                mConfigurationSeq++;
                if (mConfigurationSeq <= 0) {
                    mConfigurationSeq = 1;
                }
                newConfig.seq = mConfigurationSeq;
                mConfiguration = newConfig;
                Slog.i(TAG, "Config changed: " + newConfig);
                AttributeCache ac = AttributeCache.instance();
                if (ac != null) {
                    ac.updateConfiguration(mConfiguration);
                }
                if (Settings.System.hasInterestingConfigurationChanges(changes)) {
                    Message msg = mHandler.obtainMessage(UPDATE_CONFIGURATION_MSG);
                    msg.obj = new Configuration(mConfiguration);
                    mHandler.sendMessage(msg);
                }
                for (int i=mLruProcesses.size()-1; i>=0; i--) {
                    ProcessRecord app = mLruProcesses.get(i);
                    try {
                        if (app.thread != null) {
                            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Sending to proc "
                                    + app.processName + " new config " + mConfiguration);
                            app.thread.scheduleConfigurationChanged(mConfiguration);
                        }
                    } catch (Exception e) {
                    }
                }
                Intent intent = new Intent(Intent.ACTION_CONFIGURATION_CHANGED);
                intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY
                        | Intent.FLAG_RECEIVER_REPLACE_PENDING);
                broadcastIntentLocked(null, null, intent, null, null, 0, null, null,
                        null, false, false, MY_PID, Process.SYSTEM_UID);
                if ((changes&ActivityInfo.CONFIG_LOCALE) != 0) {
                    broadcastIntentLocked(null, null,
                            new Intent(Intent.ACTION_LOCALE_CHANGED),
                            null, null, 0, null, null,
                            null, false, false, MY_PID, Process.SYSTEM_UID);
                }
            }
        }
        if (changes != 0 && starting == null) {
            starting = topRunningActivityLocked(null);
        }
        if (starting != null) {
            kept = ensureActivityConfigurationLocked(starting, changes);
            if (kept) {
                if (DEBUG_SWITCH) Slog.i(TAG, "Config didn't destroy " + starting
                        + ", ensuring others are correct.");
                ensureActivitiesVisibleLocked(starting, changes);
            }
        }
        if (values != null && mWindowManager != null) {
            mWindowManager.setNewConfiguration(mConfiguration);
        }
        return kept;
    }
    private final boolean relaunchActivityLocked(HistoryRecord r,
            int changes, boolean andResume) {
        List<ResultInfo> results = null;
        List<Intent> newIntents = null;
        if (andResume) {
            results = r.results;
            newIntents = r.newIntents;
        }
        if (DEBUG_SWITCH) Slog.v(TAG, "Relaunching: " + r
                + " with results=" + results + " newIntents=" + newIntents
                + " andResume=" + andResume);
        EventLog.writeEvent(andResume ? EventLogTags.AM_RELAUNCH_RESUME_ACTIVITY
                : EventLogTags.AM_RELAUNCH_ACTIVITY, System.identityHashCode(r),
                r.task.taskId, r.shortComponentName);
        r.startFreezingScreenLocked(r.app, 0);
        try {
            if (DEBUG_SWITCH) Slog.i(TAG, "Switch is restarting resumed " + r);
            r.app.thread.scheduleRelaunchActivity(r, results, newIntents,
                    changes, !andResume, mConfiguration);
        } catch (RemoteException e) {
            return false;
        }
        if (andResume) {
            r.results = null;
            r.newIntents = null;
            reportResumedActivityLocked(r);
        }
        return true;
    }
    private final boolean ensureActivityConfigurationLocked(HistoryRecord r,
            int globalChanges) {
        if (mConfigWillChange) {
            if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                    "Skipping config check (will change): " + r);
            return true;
        }
        if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                "Ensuring correct configuration: " + r);
        Configuration newConfig = mConfiguration;
        if (r.configuration == newConfig) {
            if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                    "Configuration unchanged in " + r);
            return true;
        }
        if (r.finishing) {
            if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                    "Configuration doesn't matter in finishing " + r);
            r.stopFreezingScreenLocked(false);
            return true;
        }
        Configuration oldConfig = r.configuration;
        r.configuration = newConfig;
        if (r.app == null || r.app.thread == null) {
            if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                    "Configuration doesn't matter not running " + r);
            r.stopFreezingScreenLocked(false);
            return true;
        }
        if (!r.persistent) {
            int changes = oldConfig.diff(newConfig);
            if (DEBUG_SWITCH || DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Checking to restart " + r.info.name + ": changed=0x"
                        + Integer.toHexString(changes) + ", handles=0x"
                        + Integer.toHexString(r.info.configChanges)
                        + ", newConfig=" + newConfig);
            }
            if ((changes&(~r.info.configChanges)) != 0) {
                r.configChangeFlags |= changes;
                r.startFreezingScreenLocked(r.app, globalChanges);
                if (r.app == null || r.app.thread == null) {
                    if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                            "Switch is destroying non-running " + r);
                    destroyActivityLocked(r, true);
                } else if (r.state == ActivityState.PAUSING) {
                    if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                            "Switch is skipping already pausing " + r);
                    r.configDestroy = true;
                    return true;
                } else if (r.state == ActivityState.RESUMED) {
                    if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                            "Switch is restarting resumed " + r);
                    relaunchActivityLocked(r, r.configChangeFlags, true);
                    r.configChangeFlags = 0;
                } else {
                    if (DEBUG_SWITCH || DEBUG_CONFIGURATION) Slog.v(TAG,
                            "Switch is restarting non-resumed " + r);
                    relaunchActivityLocked(r, r.configChangeFlags, false);
                    r.configChangeFlags = 0;
                }
                return false;
            }
        }
        if (r.app != null && r.app.thread != null) {
            try {
                if (DEBUG_CONFIGURATION) Slog.v(TAG, "Sending new config to " + r);
                r.app.thread.scheduleActivityConfigurationChanged(r);
            } catch (RemoteException e) {
            }
        }
        r.stopFreezingScreenLocked(false);
        return true;
    }
    private void saveLocaleLocked(Locale l, boolean isDiff, boolean isPersist) {
        if(isDiff) {
            SystemProperties.set("user.language", l.getLanguage());
            SystemProperties.set("user.region", l.getCountry());
        } 
        if(isPersist) {
            SystemProperties.set("persist.sys.language", l.getLanguage());
            SystemProperties.set("persist.sys.country", l.getCountry());
            SystemProperties.set("persist.sys.localevar", l.getVariant());
        }
    }
    private final int computeOomAdjLocked(ProcessRecord app, int hiddenAdj,
            ProcessRecord TOP_APP, boolean recursed) {
        if (mAdjSeq == app.adjSeq) {
            if (!recursed && app.hidden) {
                app.curAdj = hiddenAdj;
            }
            return app.curAdj;
        }
        if (app.thread == null) {
            app.adjSeq = mAdjSeq;
            app.curSchedGroup = Process.THREAD_GROUP_BG_NONINTERACTIVE;
            return (app.curAdj=EMPTY_APP_ADJ);
        }
        if (app.maxAdj <= FOREGROUND_APP_ADJ) {
            app.adjType = "fixed";
            app.adjSeq = mAdjSeq;
            app.curRawAdj = app.maxAdj;
            app.curSchedGroup = Process.THREAD_GROUP_DEFAULT;
            return (app.curAdj=app.maxAdj);
       }
        app.adjTypeCode = ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN;
        app.adjSource = null;
        app.adjTarget = null;
        app.empty = false;
        app.hidden = false;
        int adj;
        int schedGroup;
        int N;
        if (app == TOP_APP) {
            adj = FOREGROUND_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_DEFAULT;
            app.adjType = "top-activity";
        } else if (app.instrumentationClass != null) {
            adj = FOREGROUND_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_DEFAULT;
            app.adjType = "instrumentation";
        } else if (app.persistentActivities > 0) {
            adj = FOREGROUND_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_DEFAULT;
            app.adjType = "persistent";
        } else if (app.curReceiver != null ||
                (mPendingBroadcast != null && mPendingBroadcast.curApp == app)) {
            adj = FOREGROUND_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_DEFAULT;
            app.adjType = "broadcast";
        } else if (app.executingServices.size() > 0) {
            adj = FOREGROUND_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_DEFAULT;
            app.adjType = "exec-service";
        } else if (app.foregroundServices) {
            adj = VISIBLE_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_DEFAULT;
            app.adjType = "foreground-service";
        } else if (app.forcingToForeground != null) {
            adj = VISIBLE_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_DEFAULT;
            app.adjType = "force-foreground";
            app.adjSource = app.forcingToForeground;
        } else if (app == mHomeProcess) {
            adj = HOME_APP_ADJ;
            schedGroup = Process.THREAD_GROUP_BG_NONINTERACTIVE;
            app.adjType = "home";
        } else if ((N=app.activities.size()) != 0) {
            app.hidden = true;
            adj = hiddenAdj;
            schedGroup = Process.THREAD_GROUP_BG_NONINTERACTIVE;
            app.adjType = "bg-activities";
            N = app.activities.size();
            for (int j=0; j<N; j++) {
                if (((HistoryRecord)app.activities.get(j)).visible) {
                    app.hidden = false;
                    adj = VISIBLE_APP_ADJ;
                    schedGroup = Process.THREAD_GROUP_DEFAULT;
                    app.adjType = "visible";
                    break;
                }
            }
        } else {
            app.hidden = true;
            app.empty = true;
            schedGroup = Process.THREAD_GROUP_BG_NONINTERACTIVE;
            adj = hiddenAdj;
            app.adjType = "bg-empty";
        }
        app.adjSeq = mAdjSeq;
        app.curRawAdj = adj;
        if (mBackupTarget != null && app == mBackupTarget.app) {
            if (adj > BACKUP_APP_ADJ) {
                if (DEBUG_BACKUP) Slog.v(TAG, "oom BACKUP_APP_ADJ for " + app);
                adj = BACKUP_APP_ADJ;
                app.adjType = "backup";
                app.hidden = false;
            }
        }
        if (app.services.size() != 0 && (adj > FOREGROUND_APP_ADJ
                || schedGroup == Process.THREAD_GROUP_BG_NONINTERACTIVE)) {
            final long now = SystemClock.uptimeMillis();
            Iterator jt = app.services.iterator();
            while (jt.hasNext() && adj > FOREGROUND_APP_ADJ) {
                ServiceRecord s = (ServiceRecord)jt.next();
                if (s.startRequested) {
                    if (now < (s.lastActivity+MAX_SERVICE_INACTIVITY)) {
                        if (adj > SECONDARY_SERVER_ADJ) {
                            adj = SECONDARY_SERVER_ADJ;
                            app.adjType = "started-services";
                            app.hidden = false;
                        }
                    }
                    if (adj > SECONDARY_SERVER_ADJ) {
                        app.adjType = "started-bg-services";
                    }
                }
                if (s.connections.size() > 0 && (adj > FOREGROUND_APP_ADJ
                        || schedGroup == Process.THREAD_GROUP_BG_NONINTERACTIVE)) {
                    Iterator<ConnectionRecord> kt
                            = s.connections.values().iterator();
                    while (kt.hasNext() && adj > FOREGROUND_APP_ADJ) {
                        ConnectionRecord cr = kt.next();
                        if (cr.binding.client == app) {
                            continue;
                        }
                        if ((cr.flags&Context.BIND_AUTO_CREATE) != 0) {
                            ProcessRecord client = cr.binding.client;
                            int myHiddenAdj = hiddenAdj;
                            if (myHiddenAdj > client.hiddenAdj) {
                                if (client.hiddenAdj > VISIBLE_APP_ADJ) {
                                    myHiddenAdj = client.hiddenAdj;
                                } else {
                                    myHiddenAdj = VISIBLE_APP_ADJ;
                                }
                            }
                            int clientAdj = computeOomAdjLocked(
                                client, myHiddenAdj, TOP_APP, true);
                            if (adj > clientAdj) {
                                adj = clientAdj > VISIBLE_APP_ADJ
                                        ? clientAdj : VISIBLE_APP_ADJ;
                                if (!client.hidden) {
                                    app.hidden = false;
                                }
                                app.adjType = "service";
                                app.adjTypeCode = ActivityManager.RunningAppProcessInfo
                                        .REASON_SERVICE_IN_USE;
                                app.adjSource = cr.binding.client;
                                app.adjTarget = s.serviceInfo.name;
                            }
                            if ((cr.flags&Context.BIND_NOT_FOREGROUND) == 0) {
                                if (client.curSchedGroup == Process.THREAD_GROUP_DEFAULT) {
                                    schedGroup = Process.THREAD_GROUP_DEFAULT;
                                }
                            }
                        }
                        HistoryRecord a = cr.activity;
                        if (a != null && adj > FOREGROUND_APP_ADJ &&
                                (a.state == ActivityState.RESUMED
                                 || a.state == ActivityState.PAUSING)) {
                            adj = FOREGROUND_APP_ADJ;
                            schedGroup = Process.THREAD_GROUP_DEFAULT;
                            app.hidden = false;
                            app.adjType = "service";
                            app.adjTypeCode = ActivityManager.RunningAppProcessInfo
                                    .REASON_SERVICE_IN_USE;
                            app.adjSource = a;
                            app.adjTarget = s.serviceInfo.name;
                        }
                    }
                }
            }
            if (adj > hiddenAdj) {
                adj = hiddenAdj;
                app.hidden = false;
                app.adjType = "bg-services";
            }
        }
        if (app.pubProviders.size() != 0 && (adj > FOREGROUND_APP_ADJ
                || schedGroup == Process.THREAD_GROUP_BG_NONINTERACTIVE)) {
            Iterator jt = app.pubProviders.values().iterator();
            while (jt.hasNext() && (adj > FOREGROUND_APP_ADJ
                    || schedGroup == Process.THREAD_GROUP_BG_NONINTERACTIVE)) {
                ContentProviderRecord cpr = (ContentProviderRecord)jt.next();
                if (cpr.clients.size() != 0) {
                    Iterator<ProcessRecord> kt = cpr.clients.iterator();
                    while (kt.hasNext() && adj > FOREGROUND_APP_ADJ) {
                        ProcessRecord client = kt.next();
                        if (client == app) {
                            continue;
                        }
                        int myHiddenAdj = hiddenAdj;
                        if (myHiddenAdj > client.hiddenAdj) {
                            if (client.hiddenAdj > FOREGROUND_APP_ADJ) {
                                myHiddenAdj = client.hiddenAdj;
                            } else {
                                myHiddenAdj = FOREGROUND_APP_ADJ;
                            }
                        }
                        int clientAdj = computeOomAdjLocked(
                            client, myHiddenAdj, TOP_APP, true);
                        if (adj > clientAdj) {
                            adj = clientAdj > FOREGROUND_APP_ADJ
                                    ? clientAdj : FOREGROUND_APP_ADJ;
                            if (!client.hidden) {
                                app.hidden = false;
                            }
                            app.adjType = "provider";
                            app.adjTypeCode = ActivityManager.RunningAppProcessInfo
                                    .REASON_PROVIDER_IN_USE;
                            app.adjSource = client;
                            app.adjTarget = cpr.info.name;
                        }
                        if (client.curSchedGroup == Process.THREAD_GROUP_DEFAULT) {
                            schedGroup = Process.THREAD_GROUP_DEFAULT;
                        }
                    }
                }
                if (cpr.externals != 0) {
                    if (adj > FOREGROUND_APP_ADJ) {
                        adj = FOREGROUND_APP_ADJ;
                        schedGroup = Process.THREAD_GROUP_DEFAULT;
                        app.hidden = false;
                        app.adjType = "provider";
                        app.adjTarget = cpr.info.name;
                    }
                }
            }
        }
        app.curRawAdj = adj;
        if (adj > app.maxAdj) {
            adj = app.maxAdj;
            if (app.maxAdj <= VISIBLE_APP_ADJ) {
                schedGroup = Process.THREAD_GROUP_DEFAULT;
            }
        }
        app.curAdj = adj;
        app.curSchedGroup = schedGroup;
        return adj;
    }
    final void performAppGcLocked(ProcessRecord app) {
        try {
            app.lastRequestedGc = SystemClock.uptimeMillis();
            if (app.thread != null) {
                if (app.reportLowMemory) {
                    app.reportLowMemory = false;
                    app.thread.scheduleLowMemory();
                } else {
                    app.thread.processInBackground();
                }
            }
        } catch (Exception e) {
        }
    }
    private final boolean canGcNowLocked() {
        return mParallelBroadcasts.size() == 0
                && mOrderedBroadcasts.size() == 0
                && (mSleeping || (mResumedActivity != null &&
                        mResumedActivity.idle));
    }
    final void performAppGcsLocked() {
        final int N = mProcessesToGc.size();
        if (N <= 0) {
            return;
        }
        if (canGcNowLocked()) {
            while (mProcessesToGc.size() > 0) {
                ProcessRecord proc = mProcessesToGc.remove(0);
                if (proc.curRawAdj > VISIBLE_APP_ADJ || proc.reportLowMemory) {
                    if ((proc.lastRequestedGc+GC_MIN_INTERVAL)
                            <= SystemClock.uptimeMillis()) {
                        performAppGcLocked(proc);
                        scheduleAppGcsLocked();
                        return;
                    } else {
                        addProcessToGcListLocked(proc);
                        break;
                    }
                }
            }
            scheduleAppGcsLocked();
        }
    }
    final void performAppGcsIfAppropriateLocked() {
        if (canGcNowLocked()) {
            performAppGcsLocked();
            return;
        }
        scheduleAppGcsLocked();
    }
    final void scheduleAppGcsLocked() {
        mHandler.removeMessages(GC_BACKGROUND_PROCESSES_MSG);
        if (mProcessesToGc.size() > 0) {
            ProcessRecord proc = mProcessesToGc.get(0);
            Message msg = mHandler.obtainMessage(GC_BACKGROUND_PROCESSES_MSG);
            long when = mProcessesToGc.get(0).lastRequestedGc + GC_MIN_INTERVAL;
            long now = SystemClock.uptimeMillis();
            if (when < (now+GC_TIMEOUT)) {
                when = now + GC_TIMEOUT;
            }
            mHandler.sendMessageAtTime(msg, when);
        }
    }
    final void addProcessToGcListLocked(ProcessRecord proc) {
        boolean added = false;
        for (int i=mProcessesToGc.size()-1; i>=0; i--) {
            if (mProcessesToGc.get(i).lastRequestedGc <
                    proc.lastRequestedGc) {
                added = true;
                mProcessesToGc.add(i+1, proc);
                break;
            }
        }
        if (!added) {
            mProcessesToGc.add(0, proc);
        }
    }
    final void scheduleAppGcLocked(ProcessRecord app) {
        long now = SystemClock.uptimeMillis();
        if ((app.lastRequestedGc+GC_MIN_INTERVAL) > now) {
            return;
        }
        if (!mProcessesToGc.contains(app)) {
            addProcessToGcListLocked(app);
            scheduleAppGcsLocked();
        }
    }
    private final boolean updateOomAdjLocked(
        ProcessRecord app, int hiddenAdj, ProcessRecord TOP_APP) {
        app.hiddenAdj = hiddenAdj;
        if (app.thread == null) {
            return true;
        }
        int adj = computeOomAdjLocked(app, hiddenAdj, TOP_APP, false);
        if ((app.pid != 0 && app.pid != MY_PID) || Process.supportsProcesses()) {
            if (app.curRawAdj != app.setRawAdj) {
                if (app.curRawAdj > FOREGROUND_APP_ADJ
                        && app.setRawAdj <= FOREGROUND_APP_ADJ) {
                    scheduleAppGcLocked(app);
                } else if (app.curRawAdj >= HIDDEN_APP_MIN_ADJ
                        && app.setRawAdj < HIDDEN_APP_MIN_ADJ) {
                    scheduleAppGcLocked(app);
                }
                app.setRawAdj = app.curRawAdj;
            }
            if (adj != app.setAdj) {
                if (Process.setOomAdj(app.pid, adj)) {
                    if (DEBUG_SWITCH || DEBUG_OOM_ADJ) Slog.v(
                        TAG, "Set app " + app.processName +
                        " oom adj to " + adj);
                    app.setAdj = adj;
                } else {
                    return false;
                }
            }
            if (app.setSchedGroup != app.curSchedGroup) {
                app.setSchedGroup = app.curSchedGroup;
                if (DEBUG_SWITCH || DEBUG_OOM_ADJ) Slog.v(TAG,
                        "Setting process group of " + app.processName
                        + " to " + app.curSchedGroup);
                if (true) {
                    long oldId = Binder.clearCallingIdentity();
                    try {
                        Process.setProcessGroup(app.pid, app.curSchedGroup);
                    } catch (Exception e) {
                        Slog.w(TAG, "Failed setting process group of " + app.pid
                                + " to " + app.curSchedGroup);
                        e.printStackTrace();
                    } finally {
                        Binder.restoreCallingIdentity(oldId);
                    }
                }
                if (false) {
                    if (app.thread != null) {
                        try {
                            app.thread.setSchedulingGroup(app.curSchedGroup);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
        }
        return true;
    }
    private final HistoryRecord resumedAppLocked() {
        HistoryRecord resumedActivity = mResumedActivity;
        if (resumedActivity == null || resumedActivity.app == null) {
            resumedActivity = mPausingActivity;
            if (resumedActivity == null || resumedActivity.app == null) {
                resumedActivity = topRunningActivityLocked(null);
            }
        }
        return resumedActivity;
    }
    private final boolean updateOomAdjLocked(ProcessRecord app) {
        final HistoryRecord TOP_ACT = resumedAppLocked();
        final ProcessRecord TOP_APP = TOP_ACT != null ? TOP_ACT.app : null;
        int curAdj = app.curAdj;
        final boolean wasHidden = app.curAdj >= HIDDEN_APP_MIN_ADJ
            && app.curAdj <= HIDDEN_APP_MAX_ADJ;
        mAdjSeq++;
        final boolean res = updateOomAdjLocked(app, app.hiddenAdj, TOP_APP);
        if (res) {
            final boolean nowHidden = app.curAdj >= HIDDEN_APP_MIN_ADJ
                && app.curAdj <= HIDDEN_APP_MAX_ADJ;
            if (nowHidden != wasHidden) {
                updateOomAdjLocked();
            }
        }
        return res;
    }
    private final boolean updateOomAdjLocked() {
        boolean didOomAdj = true;
        final HistoryRecord TOP_ACT = resumedAppLocked();
        final ProcessRecord TOP_APP = TOP_ACT != null ? TOP_ACT.app : null;
        if (false) {
            RuntimeException e = new RuntimeException();
            e.fillInStackTrace();
            Slog.i(TAG, "updateOomAdj: top=" + TOP_ACT, e);
        }
        mAdjSeq++;
        int numSlots = HIDDEN_APP_MAX_ADJ - HIDDEN_APP_MIN_ADJ + 1;
        int factor = (mLruProcesses.size()-4)/numSlots;
        if (factor < 1) factor = 1;
        int step = 0;
        int numHidden = 0;
        int i = mLruProcesses.size();
        int curHiddenAdj = HIDDEN_APP_MIN_ADJ;
        while (i > 0) {
            i--;
            ProcessRecord app = mLruProcesses.get(i);
            if (updateOomAdjLocked(app, curHiddenAdj, TOP_APP)) {
                if (curHiddenAdj < EMPTY_APP_ADJ
                    && app.curAdj == curHiddenAdj) {
                    step++;
                    if (step >= factor) {
                        step = 0;
                        curHiddenAdj++;
                    }
                }
                if (app.curAdj >= HIDDEN_APP_MIN_ADJ) {
                    if (!app.killedBackground) {
                        numHidden++;
                        if (numHidden > MAX_HIDDEN_APPS) {
                            Slog.i(TAG, "No longer want " + app.processName
                                    + " (pid " + app.pid + "): hidden #" + numHidden);
                            EventLog.writeEvent(EventLogTags.AM_KILL, app.pid,
                                    app.processName, app.setAdj, "too many background");
                            app.killedBackground = true;
                            Process.killProcessQuiet(app.pid);
                        }
                    }
                }
            } else {
                didOomAdj = false;
            }
        }
        return ENFORCE_PROCESS_LIMIT || mProcessLimit > 0 ? false : didOomAdj;
    }
    private final void trimApplications() {
        synchronized (this) {
            int i;
            for (i=mRemovedProcesses.size()-1; i>=0; i--) {
                final ProcessRecord app = mRemovedProcesses.get(i);
                if (app.activities.size() == 0
                        && app.curReceiver == null && app.services.size() == 0) {
                    Slog.i(
                        TAG, "Exiting empty application process "
                        + app.processName + " ("
                        + (app.thread != null ? app.thread.asBinder() : null)
                        + ")\n");
                    if (app.pid > 0 && app.pid != MY_PID) {
                        Process.killProcess(app.pid);
                    } else {
                        try {
                            app.thread.scheduleExit();
                        } catch (Exception e) {
                        }
                    }
                    cleanUpApplicationRecordLocked(app, false, -1);
                    mRemovedProcesses.remove(i);
                    if (app.persistent) {
                        if (app.persistent) {
                            addAppLocked(app.info);
                        }
                    }
                }
            }
            if (!updateOomAdjLocked()) {
                int numServiceProcs = 0;
                for (i=mLruProcesses.size()-1; i>=0; i--) {
                    final ProcessRecord app = mLruProcesses.get(i);
                    if (app.persistent || app.services.size() != 0
                            || app.curReceiver != null
                            || app.persistentActivities > 0) {
                        if (localLOGV) Slog.v(
                            TAG, "Not trimming app " + app + " with services: "
                            + app.services);
                        numServiceProcs++;
                    }
                }
                int curMaxProcs = mProcessLimit;
                if (curMaxProcs <= 0) curMaxProcs = MAX_PROCESSES;
                if (mAlwaysFinishActivities) {
                    curMaxProcs = 1;
                }
                curMaxProcs += numServiceProcs;
                for (   i=0;
                        i<mLruProcesses.size()
                            && mLruProcesses.size() > curMaxProcs;
                        i++) {
                    final ProcessRecord app = mLruProcesses.get(i);
                    if (!app.persistent && app.activities.size() == 0
                            && app.curReceiver == null && app.services.size() == 0) {
                        Slog.i(
                            TAG, "Exiting empty application process "
                            + app.processName + " ("
                            + (app.thread != null ? app.thread.asBinder() : null)
                            + ")\n");
                        if (app.pid > 0 && app.pid != MY_PID) {
                            Process.killProcess(app.pid);
                        } else {
                            try {
                                app.thread.scheduleExit();
                            } catch (Exception e) {
                            }
                        }
                        cleanUpApplicationRecordLocked(app, false, i);
                        i--;
                    }
                }
                if (Config.LOGV) Slog.v(
                    TAG, "*** NOW HAVE " + mLruProcesses.size() +
                    " of " + curMaxProcs + " processes");
                for (   i=0;
                        i<mLruProcesses.size()
                            && mLruProcesses.size() > curMaxProcs;
                        i++) {
                    final ProcessRecord app = mLruProcesses.get(i);
                    boolean canQuit = !app.persistent && app.curReceiver == null
                        && app.services.size() == 0
                        && app.persistentActivities == 0;
                    int NUMA = app.activities.size();
                    int j;
                    if (Config.LOGV) Slog.v(
                        TAG, "Looking to quit " + app.processName);
                    for (j=0; j<NUMA && canQuit; j++) {
                        HistoryRecord r = (HistoryRecord)app.activities.get(j);
                        if (Config.LOGV) Slog.v(
                            TAG, "  " + r.intent.getComponent().flattenToShortString()
                            + ": frozen=" + r.haveState + ", visible=" + r.visible);
                        canQuit = (r.haveState || !r.stateNotNeeded)
                                && !r.visible && r.stopped;
                    }
                    if (canQuit) {
                        for (j=0; j<NUMA; j++) {
                            HistoryRecord r = (HistoryRecord)app.activities.get(j);
                            if (!r.finishing) {
                                destroyActivityLocked(r, false);
                            }
                            r.resultTo = null;
                        }
                        Slog.i(TAG, "Exiting application process "
                              + app.processName + " ("
                              + (app.thread != null ? app.thread.asBinder() : null)
                              + ")\n");
                        if (app.pid > 0 && app.pid != MY_PID) {
                            Process.killProcess(app.pid);
                        } else {
                            try {
                                app.thread.scheduleExit();
                            } catch (Exception e) {
                            }
                        }
                        cleanUpApplicationRecordLocked(app, false, i);
                        i--;
                    }
                }
            }
            int curMaxActivities = MAX_ACTIVITIES;
            if (mAlwaysFinishActivities) {
                curMaxActivities = 1;
            }
            for (   i=0;
                    i<mLRUActivities.size()
                        && mLRUActivities.size() > curMaxActivities;
                    i++) {
                final HistoryRecord r
                    = (HistoryRecord)mLRUActivities.get(i);
                if ((r.haveState || !r.stateNotNeeded) && !r.visible
                        && r.stopped && !r.persistent && !r.finishing) {
                    final int origSize = mLRUActivities.size();
                    destroyActivityLocked(r, true);
                    if (origSize > mLRUActivities.size()) {
                        i--;
                    }
                }
            }
        }
    }
    public void signalPersistentProcesses(int sig) throws RemoteException {
        if (sig != Process.SIGNAL_USR1) {
            throw new SecurityException("Only SIGNAL_USR1 is allowed");
        }
        synchronized (this) {
            if (checkCallingPermission(android.Manifest.permission.SIGNAL_PERSISTENT_PROCESSES)
                    != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException("Requires permission "
                        + android.Manifest.permission.SIGNAL_PERSISTENT_PROCESSES);
            }
            for (int i = mLruProcesses.size() - 1 ; i >= 0 ; i--) {
                ProcessRecord r = mLruProcesses.get(i);
                if (r.thread != null && r.persistent) {
                    Process.sendSignal(r.pid, sig);
                }
            }
        }
    }
    public boolean profileControl(String process, boolean start,
            String path, ParcelFileDescriptor fd) throws RemoteException {
        try {
            synchronized (this) {
                if (checkCallingPermission(android.Manifest.permission.SET_ACTIVITY_WATCHER)
                        != PackageManager.PERMISSION_GRANTED) {
                    throw new SecurityException("Requires permission "
                            + android.Manifest.permission.SET_ACTIVITY_WATCHER);
                }
                if (start && fd == null) {
                    throw new IllegalArgumentException("null fd");
                }
                ProcessRecord proc = null;
                try {
                    int pid = Integer.parseInt(process);
                    synchronized (mPidsSelfLocked) {
                        proc = mPidsSelfLocked.get(pid);
                    }
                } catch (NumberFormatException e) {
                }
                if (proc == null) {
                    HashMap<String, SparseArray<ProcessRecord>> all
                            = mProcessNames.getMap();
                    SparseArray<ProcessRecord> procs = all.get(process);
                    if (procs != null && procs.size() > 0) {
                        proc = procs.valueAt(0);
                    }
                }
                if (proc == null || proc.thread == null) {
                    throw new IllegalArgumentException("Unknown process: " + process);
                }
                boolean isSecure = "1".equals(SystemProperties.get(SYSTEM_SECURE, "0"));
                if (isSecure) {
                    if ((proc.info.flags&ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
                        throw new SecurityException("Process not debuggable: " + proc);
                    }
                }
                proc.thread.profilerControl(start, path, fd);
                fd = null;
                return true;
            }
        } catch (RemoteException e) {
            throw new IllegalStateException("Process disappeared");
        } finally {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public void monitor() {
        synchronized (this) { }
    }
}
