    public void finishOtherInstances(IBinder token, ComponentName className) throws RemoteException;
    public void reportThumbnail(IBinder token,
            Bitmap thumbnail, CharSequence description) throws RemoteException;
    public ContentProviderHolder getContentProvider(IApplicationThread caller,
            String name) throws RemoteException;
    public void removeContentProvider(IApplicationThread caller,
            String name) throws RemoteException;
    public void publishContentProviders(IApplicationThread caller,
            List<ContentProviderHolder> providers) throws RemoteException;
    public PendingIntent getRunningServiceControlPanel(ComponentName service)
            throws RemoteException;
    public ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType) throws RemoteException;
    public int stopService(IApplicationThread caller, Intent service,
            String resolvedType) throws RemoteException;
    public boolean stopServiceToken(ComponentName className, IBinder token,
            int startId) throws RemoteException;
    public void setServiceForeground(ComponentName className, IBinder token,
            int id, Notification notification, boolean keepNotification) throws RemoteException;
    public int bindService(IApplicationThread caller, IBinder token,
            Intent service, String resolvedType,
            IServiceConnection connection, int flags) throws RemoteException;
    public boolean unbindService(IServiceConnection connection) throws RemoteException;
    public void publishService(IBinder token,
            Intent intent, IBinder service) throws RemoteException;
    public void unbindFinished(IBinder token, Intent service,
            boolean doRebind) throws RemoteException;
    public void serviceDoneExecuting(IBinder token, int type, int startId,
            int res) throws RemoteException;
    public IBinder peekService(Intent service, String resolvedType) throws RemoteException;
    public boolean bindBackupAgent(ApplicationInfo appInfo, int backupRestoreMode)
            throws RemoteException;
    public void backupAgentCreated(String packageName, IBinder agent) throws RemoteException;
    public void unbindBackupAgent(ApplicationInfo appInfo) throws RemoteException;
    public void killApplicationProcess(String processName, int uid) throws RemoteException;
    public boolean startInstrumentation(ComponentName className, String profileFile,
            int flags, Bundle arguments, IInstrumentationWatcher watcher)
            throws RemoteException;
    public void finishInstrumentation(IApplicationThread target,
            int resultCode, Bundle results) throws RemoteException;
    public Configuration getConfiguration() throws RemoteException;
    public void updateConfiguration(Configuration values) throws RemoteException;
    public void setRequestedOrientation(IBinder token,
            int requestedOrientation) throws RemoteException;
    public int getRequestedOrientation(IBinder token) throws RemoteException;
    public ComponentName getActivityClassForToken(IBinder token) throws RemoteException;
    public String getPackageForToken(IBinder token) throws RemoteException;
    public static final int INTENT_SENDER_BROADCAST = 1;
    public static final int INTENT_SENDER_ACTIVITY = 2;
    public static final int INTENT_SENDER_ACTIVITY_RESULT = 3;
    public static final int INTENT_SENDER_SERVICE = 4;
    public IIntentSender getIntentSender(int type,
            String packageName, IBinder token, String resultWho,
            int requestCode, Intent intent, String resolvedType, int flags) throws RemoteException;
    public void cancelIntentSender(IIntentSender sender) throws RemoteException;
    public boolean clearApplicationUserData(final String packageName,
            final IPackageDataObserver observer) throws RemoteException;
    public String getPackageForIntentSender(IIntentSender sender) throws RemoteException;
    public void setProcessLimit(int max) throws RemoteException;
    public int getProcessLimit() throws RemoteException;
    public void setProcessForeground(IBinder token, int pid, boolean isForeground) throws RemoteException;
    public int checkPermission(String permission, int pid, int uid)
            throws RemoteException;
    public int checkUriPermission(Uri uri, int pid, int uid, int mode)
            throws RemoteException;
    public void grantUriPermission(IApplicationThread caller, String targetPkg,
            Uri uri, int mode) throws RemoteException;
    public void revokeUriPermission(IApplicationThread caller, Uri uri,
            int mode) throws RemoteException;
    public void showWaitingForDebugger(IApplicationThread who, boolean waiting)
            throws RemoteException;
    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) throws RemoteException;
    public void killBackgroundProcesses(final String packageName) throws RemoteException;
    public void forceStopPackage(final String packageName) throws RemoteException;
    public void goingToSleep() throws RemoteException;
    public void wakingUp() throws RemoteException;
    public void unhandledBack() throws RemoteException;
    public ParcelFileDescriptor openContentUri(Uri uri) throws RemoteException;
    public void setDebugApp(
        String packageName, boolean waitForDebugger, boolean persistent)
        throws RemoteException;
    public void setAlwaysFinish(boolean enabled) throws RemoteException;
    public void setActivityController(IActivityController watcher)
        throws RemoteException;
    public void enterSafeMode() throws RemoteException;
    public void noteWakeupAlarm(IIntentSender sender) throws RemoteException;
    public boolean killPids(int[] pids, String reason) throws RemoteException;
    public void reportPss(IApplicationThread caller, int pss) throws RemoteException;
    public void startRunning(String pkg, String cls, String action,
            String data) throws RemoteException;
    public void handleApplicationCrash(IBinder app,
            ApplicationErrorReport.CrashInfo crashInfo) throws RemoteException;
    public boolean handleApplicationWtf(IBinder app, String tag,
            ApplicationErrorReport.CrashInfo crashInfo) throws RemoteException;
    public void signalPersistentProcesses(int signal) throws RemoteException;
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses()
            throws RemoteException;
    public List<ApplicationInfo> getRunningExternalApplications()
            throws RemoteException;
    public ConfigurationInfo getDeviceConfigurationInfo() throws RemoteException;
    public boolean profileControl(String process, boolean start,
            String path, ParcelFileDescriptor fd) throws RemoteException;
    public boolean shutdown(int timeout) throws RemoteException;
    public void stopAppSwitches() throws RemoteException;
    public void resumeAppSwitches() throws RemoteException;
    public void registerActivityWatcher(IActivityWatcher watcher)
            throws RemoteException;
    public void unregisterActivityWatcher(IActivityWatcher watcher)
            throws RemoteException;
    public int startActivityInPackage(int uid,
            Intent intent, String resolvedType, IBinder resultTo,
            String resultWho, int requestCode, boolean onlyIfNeeded)
            throws RemoteException;
    public void killApplicationWithUid(String pkg, int uid) throws RemoteException;
    public void closeSystemDialogs(String reason) throws RemoteException;
    public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids)
            throws RemoteException;
    public void overridePendingTransition(IBinder token, String packageName,
            int enterAnim, int exitAnim) throws RemoteException;
    public boolean isUserAMonkey() throws RemoteException;
     boolean testIsSystemReady();
    public static class ContentProviderHolder implements Parcelable {
        public final ProviderInfo info;
        public final String permissionFailure;
        public IContentProvider provider;
        public boolean noReleaseNeeded;
        public ContentProviderHolder(ProviderInfo _info) {
            info = _info;
            permissionFailure = null;
        }
        public ContentProviderHolder(ProviderInfo _info,
                String _permissionFailure) {
            info = _info;
            permissionFailure = _permissionFailure;
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            info.writeToParcel(dest, 0);
            dest.writeString(permissionFailure);
            if (provider != null) {
                dest.writeStrongBinder(provider.asBinder());
            } else {
                dest.writeStrongBinder(null);
            }
            dest.writeInt(noReleaseNeeded ? 1:0);
        }
        public static final Parcelable.Creator<ContentProviderHolder> CREATOR
                = new Parcelable.Creator<ContentProviderHolder>() {
            public ContentProviderHolder createFromParcel(Parcel source) {
                return new ContentProviderHolder(source);
            }
            public ContentProviderHolder[] newArray(int size) {
                return new ContentProviderHolder[size];
            }
        };
        private ContentProviderHolder(Parcel source) {
            info = ProviderInfo.CREATOR.createFromParcel(source);
            permissionFailure = source.readString();
            provider = ContentProviderNative.asInterface(
                source.readStrongBinder());
            noReleaseNeeded = source.readInt() != 0;
        }
    };
    public static class WaitResult implements Parcelable {
        public int result;
        public boolean timeout;
        public ComponentName who;
        public long thisTime;
        public long totalTime;
        public WaitResult() {
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(result);
            dest.writeInt(timeout ? 1 : 0);
            ComponentName.writeToParcel(who, dest);
            dest.writeLong(thisTime);
            dest.writeLong(totalTime);
        }
        public static final Parcelable.Creator<WaitResult> CREATOR
                = new Parcelable.Creator<WaitResult>() {
            public WaitResult createFromParcel(Parcel source) {
                return new WaitResult(source);
            }
            public WaitResult[] newArray(int size) {
                return new WaitResult[size];
            }
        };
        private WaitResult(Parcel source) {
            result = source.readInt();
            timeout = source.readInt() != 0;
            who = ComponentName.readFromParcel(source);
            thisTime = source.readLong();
            totalTime = source.readLong();
        }
    };
    String descriptor = "android.app.IActivityManager";
    int START_RUNNING_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION;
    int HANDLE_APPLICATION_CRASH_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+1;
    int START_ACTIVITY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+2;
    int UNHANDLED_BACK_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+3;
    int OPEN_CONTENT_URI_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+4;
    int FINISH_ACTIVITY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+10;
    int REGISTER_RECEIVER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+11;
    int UNREGISTER_RECEIVER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+12;
    int BROADCAST_INTENT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+13;
    int UNBROADCAST_INTENT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+14;
    int FINISH_RECEIVER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+15;
    int ATTACH_APPLICATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+16;
    int ACTIVITY_IDLE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+17;
    int ACTIVITY_PAUSED_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+18;
    int ACTIVITY_STOPPED_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+19;
    int GET_CALLING_PACKAGE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+20;
    int GET_CALLING_ACTIVITY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+21;
    int GET_TASKS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+22;
    int MOVE_TASK_TO_FRONT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+23;
    int MOVE_TASK_TO_BACK_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+24;
    int MOVE_TASK_BACKWARDS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+25;
    int GET_TASK_FOR_ACTIVITY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+26;
    int REPORT_THUMBNAIL_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+27;
    int GET_CONTENT_PROVIDER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+28;
    int PUBLISH_CONTENT_PROVIDERS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+29;
    int SET_PERSISTENT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+30;
    int FINISH_SUB_ACTIVITY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+31;
    int GET_RUNNING_SERVICE_CONTROL_PANEL_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+32;
    int START_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+33;
    int STOP_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+34;
    int BIND_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+35;
    int UNBIND_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+36;
    int PUBLISH_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+37;
    int FINISH_OTHER_INSTANCES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+38;
    int GOING_TO_SLEEP_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+39;
    int WAKING_UP_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+40;
    int SET_DEBUG_APP_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+41;
    int SET_ALWAYS_FINISH_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+42;
    int START_INSTRUMENTATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+43;
    int FINISH_INSTRUMENTATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+44;
    int GET_CONFIGURATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+45;
    int UPDATE_CONFIGURATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+46;
    int STOP_SERVICE_TOKEN_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+47;
    int GET_ACTIVITY_CLASS_FOR_TOKEN_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+48;
    int GET_PACKAGE_FOR_TOKEN_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+49;
    int SET_PROCESS_LIMIT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+50;
    int GET_PROCESS_LIMIT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+51;
    int CHECK_PERMISSION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+52;
    int CHECK_URI_PERMISSION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+53;
    int GRANT_URI_PERMISSION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+54;
    int REVOKE_URI_PERMISSION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+55;
    int SET_ACTIVITY_CONTROLLER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+56;
    int SHOW_WAITING_FOR_DEBUGGER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+57;
    int SIGNAL_PERSISTENT_PROCESSES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+58;
    int GET_RECENT_TASKS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+59;
    int SERVICE_DONE_EXECUTING_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+60;
    int ACTIVITY_DESTROYED_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+61;
    int GET_INTENT_SENDER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+62;
    int CANCEL_INTENT_SENDER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+63;
    int GET_PACKAGE_FOR_INTENT_SENDER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+64;
    int ENTER_SAFE_MODE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+65;
    int START_NEXT_MATCHING_ACTIVITY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+66;
    int NOTE_WAKEUP_ALARM_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+67;
    int REMOVE_CONTENT_PROVIDER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+68;
    int SET_REQUESTED_ORIENTATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+69;
    int GET_REQUESTED_ORIENTATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+70;
    int UNBIND_FINISHED_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+71;
    int SET_PROCESS_FOREGROUND_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+72;
    int SET_SERVICE_FOREGROUND_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+73;
    int MOVE_ACTIVITY_TASK_TO_BACK_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+74;
    int GET_MEMORY_INFO_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+75;
    int GET_PROCESSES_IN_ERROR_STATE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+76;
    int CLEAR_APP_DATA_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+77;
    int FORCE_STOP_PACKAGE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+78;
    int KILL_PIDS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+79;
    int GET_SERVICES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+80;
    int REPORT_PSS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+81;
    int GET_RUNNING_APP_PROCESSES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+82;
    int GET_DEVICE_CONFIGURATION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+83;
    int PEEK_SERVICE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+84;
    int PROFILE_CONTROL_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+85;
    int SHUTDOWN_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+86;
    int STOP_APP_SWITCHES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+87;
    int RESUME_APP_SWITCHES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+88;
    int START_BACKUP_AGENT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+89;
    int BACKUP_AGENT_CREATED_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+90;
    int UNBIND_BACKUP_AGENT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+91;
    int REGISTER_ACTIVITY_WATCHER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+92;
    int UNREGISTER_ACTIVITY_WATCHER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+93;
    int START_ACTIVITY_IN_PACKAGE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+94;
    int KILL_APPLICATION_WITH_UID_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+95;
    int CLOSE_SYSTEM_DIALOGS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+96;
    int GET_PROCESS_MEMORY_INFO_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+97;
    int KILL_APPLICATION_PROCESS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+98;
    int START_ACTIVITY_INTENT_SENDER_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+99;
    int OVERRIDE_PENDING_TRANSITION_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+100;
    int HANDLE_APPLICATION_WTF_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+101;
    int KILL_BACKGROUND_PROCESSES_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+102;
    int IS_USER_A_MONKEY_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+103;
    int START_ACTIVITY_AND_WAIT_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+104;
    int WILL_ACTIVITY_BE_VISIBLE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+105;
    int START_ACTIVITY_WITH_CONFIG_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+106;
    int GET_RUNNING_EXTERNAL_APPLICATIONS_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION+107;
}
