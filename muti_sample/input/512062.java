public class InstalledAppDetails extends Activity implements View.OnClickListener {
    private static final String TAG="InstalledAppDetails";
    private static final int _UNKNOWN_APP=R.string.unknown;
    private ApplicationInfo mAppInfo;
    private Button mUninstallButton;
    private boolean mMoveInProgress = false;
    private boolean mUpdatedSysApp = false;
    private Button mActivitiesButton;
    private boolean localLOGV = false;
    private TextView mAppVersion;
    private TextView mTotalSize;
    private TextView mAppSize;
    private TextView mDataSize;
    private PkgSizeObserver mSizeObserver;
    private ClearUserDataObserver mClearDataObserver;
    private TextView mCacheSize;
    private Button mClearCacheButton;
    private ClearCacheObserver mClearCacheObserver;
    private Button mForceStopButton;
    private Button mClearDataButton;
    private Button mMoveAppButton;
    private int mMoveErrorCode;
    PackageStats mSizeInfo;
    private PackageManager mPm;
    private PackageMoveObserver mPackageMoveObserver;
    private static final int OP_SUCCESSFUL = 1;
    private static final int OP_FAILED = 2;
    private static final int CLEAR_USER_DATA = 1;
    private static final int GET_PKG_SIZE = 2;
    private static final int CLEAR_CACHE = 3;
    private static final int PACKAGE_MOVE = 4;
    private static final String ATTR_PACKAGE_STATS="PackageStats";
    private static final int SIZE_INVALID = -1;
    private CharSequence mInvalidSizeStr;
    private CharSequence mComputingStr;
    private static final int DLG_BASE = 0;
    private static final int DLG_CLEAR_DATA = DLG_BASE + 1;
    private static final int DLG_FACTORY_RESET = DLG_BASE + 2;
    private static final int DLG_APP_NOT_FOUND = DLG_BASE + 3;
    private static final int DLG_CANNOT_CLEAR_DATA = DLG_BASE + 4;
    private static final int DLG_FORCE_STOP = DLG_BASE + 5;
    private static final int DLG_MOVE_FAILED = DLG_BASE + 6;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (isFinishing()) {
                return;
            }
            switch (msg.what) {
                case CLEAR_USER_DATA:
                    processClearMsg(msg);
                    break;
                case GET_PKG_SIZE:
                    refreshSizeInfo(msg);
                    break;
                case CLEAR_CACHE:
                    mPm.getPackageSizeInfo(mAppInfo.packageName, mSizeObserver);
                    break;
                case PACKAGE_MOVE:
                    processMoveMsg(msg);
                    break;
                default:
                    break;
            }
        }
    };
    class ClearUserDataObserver extends IPackageDataObserver.Stub {
       public void onRemoveCompleted(final String packageName, final boolean succeeded) {
           final Message msg = mHandler.obtainMessage(CLEAR_USER_DATA);
           msg.arg1 = succeeded?OP_SUCCESSFUL:OP_FAILED;
           mHandler.sendMessage(msg);
        }
    }
    class PkgSizeObserver extends IPackageStatsObserver.Stub {
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
             Message msg = mHandler.obtainMessage(GET_PKG_SIZE);
             Bundle data = new Bundle();
             data.putParcelable(ATTR_PACKAGE_STATS, pStats);
             msg.setData(data);
             mHandler.sendMessage(msg);
         }
     }
    class ClearCacheObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(CLEAR_CACHE);
            msg.arg1 = succeeded ? OP_SUCCESSFUL:OP_FAILED;
            mHandler.sendMessage(msg);
         }
     }
    class PackageMoveObserver extends IPackageMoveObserver.Stub {
        public void packageMoved(String packageName, int returnCode) throws RemoteException {
            final Message msg = mHandler.obtainMessage(PACKAGE_MOVE);
            msg.arg1 = returnCode;
            mHandler.sendMessage(msg);
        }
    }
    private String getSizeStr(long size) {
        if (size == SIZE_INVALID) {
            return mInvalidSizeStr.toString();
        }
        return Formatter.formatFileSize(this, size);
    }
    private void initDataButtons() {
        if (mAppInfo.manageSpaceActivityName != null) {
            mClearDataButton.setText(R.string.manage_space_text);
        } else {
            mClearDataButton.setText(R.string.clear_user_data_text);
        }
        mClearDataButton.setOnClickListener(this);
    }
    private CharSequence getMoveErrMsg(int errCode) {
        switch (errCode) {
            case PackageManager.MOVE_FAILED_INSUFFICIENT_STORAGE:
                return getString(R.string.insufficient_storage);
            case PackageManager.MOVE_FAILED_DOESNT_EXIST:
                return getString(R.string.does_not_exist);
            case PackageManager.MOVE_FAILED_FORWARD_LOCKED:
                return getString(R.string.app_forward_locked);
            case PackageManager.MOVE_FAILED_INVALID_LOCATION:
                return getString(R.string.invalid_location);
            case PackageManager.MOVE_FAILED_SYSTEM_PACKAGE:
                return getString(R.string.system_package);
            case PackageManager.MOVE_FAILED_INTERNAL_ERROR:
                return "";
        }
        return "";
    }
    private void initMoveButton() {
        String pkgName = mAppInfo.packageName;
        boolean dataOnly = false;
        ApplicationInfo info1 = null;
        PackageInfo pkgInfo = null;
        try {
            info1 = mPm.getApplicationInfo(pkgName, 0);
            pkgInfo = mPm.getPackageInfo(mAppInfo.packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
        }
        dataOnly = (info1 == null) && (mAppInfo != null);
        boolean moveDisable = true;
        if (dataOnly) {
            mMoveAppButton.setText(R.string.move_app);
        } else if ((mAppInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            mMoveAppButton.setText(R.string.move_app_to_internal);
            moveDisable = false;
        } else {
            mMoveAppButton.setText(R.string.move_app_to_sdcard);
            if ((mAppInfo.flags & ApplicationInfo.FLAG_FORWARD_LOCK) == 0 &&
                    (mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 &&
                    pkgInfo != null) {
                if (pkgInfo.installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL ||
                        pkgInfo.installLocation == PackageInfo.INSTALL_LOCATION_AUTO) {
                    moveDisable = false;
                } else if (pkgInfo.installLocation == PackageInfo.INSTALL_LOCATION_UNSPECIFIED) {
                    IPackageManager ipm  = IPackageManager.Stub.asInterface(
                            ServiceManager.getService("package"));
                    int loc;
                    try {
                        loc = ipm.getInstallLocation();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Is Pakage Manager running?");
                        return;
                    }
                    if (loc == PackageHelper.APP_INSTALL_EXTERNAL) {
                        moveDisable = false;
                    }
                }
            }
        }
        if (moveDisable) {
            mMoveAppButton.setEnabled(false);
        } else {
            mMoveAppButton.setOnClickListener(this);
            mMoveAppButton.setEnabled(true);
        }
    }
    private void initUninstallButtons() {
        mUpdatedSysApp = (mAppInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        boolean enabled = true;
        if (mUpdatedSysApp) {
            mUninstallButton.setText(R.string.app_factory_reset);
        } else {
            if ((mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0){
                enabled = false;
            }
            mUninstallButton.setText(R.string.uninstall_text);
        }
        mUninstallButton.setEnabled(enabled);
        if (enabled) {
            mUninstallButton.setOnClickListener(this);
        }
    }
    private boolean initAppInfo(String packageName) {
        try {
            mAppInfo = mPm.getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Exception when retrieving package: " + packageName, e);
            showDialogInner(DLG_APP_NOT_FOUND);
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPm = getPackageManager();
        Intent intent = getIntent();
        final String packageName = intent.getStringExtra(ManageApplications.APP_PKG_NAME);
        if (! initAppInfo(packageName)) {
            return; 
        }
        CharSequence totalSizeStr, appSizeStr, dataSizeStr;
        mComputingStr = getText(R.string.computing_size);
        totalSizeStr = appSizeStr = dataSizeStr = mComputingStr;
        if(localLOGV) Log.i(TAG, "Have to compute package sizes");
        mSizeObserver = new PkgSizeObserver();
        setContentView(R.layout.installed_app_details);
        mTotalSize = (TextView)findViewById(R.id.total_size_text);
        mTotalSize.setText(totalSizeStr);
        mAppSize = (TextView)findViewById(R.id.application_size_text);
        mAppSize.setText(appSizeStr);
        mDataSize = (TextView)findViewById(R.id.data_size_text);
        mDataSize.setText(dataSizeStr);
        View btnPanel = findViewById(R.id.control_buttons_panel);
        mForceStopButton = (Button) btnPanel.findViewById(R.id.left_button);
        mForceStopButton.setText(R.string.force_stop);
        mUninstallButton = (Button)btnPanel.findViewById(R.id.right_button);
        mForceStopButton.setEnabled(false);
        View data_buttons_panel = findViewById(R.id.data_buttons_panel);
        mClearDataButton = (Button) data_buttons_panel.findViewById(R.id.left_button);
        mMoveAppButton = (Button) data_buttons_panel.findViewById(R.id.right_button);
         mCacheSize = (TextView) findViewById(R.id.cache_size_text);
         mCacheSize.setText(mComputingStr);
         mClearCacheButton = (Button) findViewById(R.id.clear_cache_button);
         mActivitiesButton = (Button)findViewById(R.id.clear_activities_button);
         List<ComponentName> prefActList = new ArrayList<ComponentName>();
         List<IntentFilter> intentList = new ArrayList<IntentFilter>();
         mPm.getPreferredActivities(intentList,  prefActList, packageName);
         if(localLOGV) Log.i(TAG, "Have "+prefActList.size()+" number of activities in prefered list");
         TextView autoLaunchView = (TextView)findViewById(R.id.auto_launch);
         if(prefActList.size() <= 0) {
             autoLaunchView.setText(R.string.auto_launch_disable_text);
             mActivitiesButton.setEnabled(false);
         } else {
             autoLaunchView.setText(R.string.auto_launch_enable_text);
             mActivitiesButton.setOnClickListener(this);
         }
         LinearLayout permsView = (LinearLayout) findViewById(R.id.permissions_section);
         AppSecurityPermissions asp = new AppSecurityPermissions(this, packageName);
         if(asp.getPermissionCount() > 0) {
             permsView.setVisibility(View.VISIBLE);
             LinearLayout securityList = (LinearLayout) permsView.findViewById(
                     R.id.security_settings_list);
             securityList.addView(asp.getPermissionsView());
         } else {
             permsView.setVisibility(View.GONE);
         }
    }
    private void setAppLabelAndIcon(PackageInfo pkgInfo) {
        View appSnippet = findViewById(R.id.app_snippet);
        ImageView icon = (ImageView) appSnippet.findViewById(R.id.app_icon);
        icon.setImageDrawable(mAppInfo.loadIcon(mPm));
        TextView label = (TextView) appSnippet.findViewById(R.id.app_name);
        label.setText(mAppInfo.loadLabel(mPm));
        mAppVersion = (TextView) appSnippet.findViewById(R.id.app_size);
        if (pkgInfo != null && pkgInfo.versionName != null) {
            mAppVersion.setVisibility(View.VISIBLE);
            mAppVersion.setText(getString(R.string.version_text,
                    String.valueOf(pkgInfo.versionName)));
        } else {
            mAppVersion.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mAppInfo == null) {
            setIntentAndFinish(true, true);
            return; 
        }
        if (! initAppInfo(mAppInfo.packageName)) {
            return; 
        }
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = mPm.getPackageInfo(mAppInfo.packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Exception when retrieving package:" + mAppInfo.packageName, e);
            showDialogInner(DLG_APP_NOT_FOUND);
            return; 
        }
        checkForceStop();
        setAppLabelAndIcon(pkgInfo);
        refreshButtons();
        if (mAppInfo != null && mAppInfo.packageName != null) {
            mPm.getPackageSizeInfo(mAppInfo.packageName, mSizeObserver);
        }
    }
    private void setIntentAndFinish(boolean finish, boolean appChanged) {
        if(localLOGV) Log.i(TAG, "appChanged="+appChanged);
        Intent intent = new Intent();
        intent.putExtra(ManageApplications.APP_CHG, appChanged);
        setResult(ManageApplications.RESULT_OK, intent);
        if(finish) {
            finish();
        }
    }
    private void refreshSizeInfo(Message msg) {
        boolean changed = false;
        PackageStats newPs = msg.getData().getParcelable(ATTR_PACKAGE_STATS);
        long newTot = newPs.cacheSize+newPs.codeSize+newPs.dataSize;
        if(mSizeInfo == null) {
            mSizeInfo = newPs;
            String str = getSizeStr(newTot);
            mTotalSize.setText(str);
            mAppSize.setText(getSizeStr(newPs.codeSize));
            mDataSize.setText(getSizeStr(newPs.dataSize));
            mCacheSize.setText(getSizeStr(newPs.cacheSize));
        } else {
            long oldTot = mSizeInfo.cacheSize+mSizeInfo.codeSize+mSizeInfo.dataSize;
            if(newTot != oldTot) {
                String str = getSizeStr(newTot);
                mTotalSize.setText(str);
                changed = true;
            }
            if(newPs.codeSize != mSizeInfo.codeSize) {
                mAppSize.setText(getSizeStr(newPs.codeSize));
                changed = true;
            }
            if(newPs.dataSize != mSizeInfo.dataSize) {
                mDataSize.setText(getSizeStr(newPs.dataSize));
                changed = true;
            }
            if(newPs.cacheSize != mSizeInfo.cacheSize) {
                mCacheSize.setText(getSizeStr(newPs.cacheSize));
                changed = true;
            }
            if(changed) {
                mSizeInfo = newPs;
            }
        }
        if (newPs.dataSize == 0) {
            mClearDataButton.setEnabled(false);
        }
        long data = mSizeInfo.dataSize;
        refreshCacheInfo(newPs.cacheSize);
    }
    private void refreshCacheInfo(long cacheSize) {
        mCacheSize.setText(getSizeStr(cacheSize));
        if (cacheSize <= 0) {
            mClearCacheButton.setEnabled(false);
        } else {
            mClearCacheButton.setOnClickListener(this);
        }
    }
    private void processClearMsg(Message msg) {
        int result = msg.arg1;
        String packageName = mAppInfo.packageName;
        mClearDataButton.setText(R.string.clear_user_data_text);
        if(result == OP_SUCCESSFUL) {
            Log.i(TAG, "Cleared user data for package : "+packageName);
            mPm.getPackageSizeInfo(packageName, mSizeObserver);
        } else {
            mClearDataButton.setEnabled(true);
        }
    }
    private void refreshButtons() {
        if (!mMoveInProgress) {
            initUninstallButtons();
            initDataButtons();
            initMoveButton();
        } else {
            mMoveAppButton.setText(R.string.moving);
            mMoveAppButton.setEnabled(false);
            mUninstallButton.setEnabled(false);
        }
    }
    private void processMoveMsg(Message msg) {
        int result = msg.arg1;
        String packageName = mAppInfo.packageName;
        mMoveInProgress = false;
        if(result == PackageManager.MOVE_SUCCEEDED) {
            Log.i(TAG, "Moved resources for " + packageName);
            mPm.getPackageSizeInfo(packageName, mSizeObserver);
        } else {
            mMoveErrorCode = result;
            showDialogInner(DLG_MOVE_FAILED);
        }
        if (! initAppInfo(packageName)) {
            return; 
        }
        refreshButtons();
    }
    private  void initiateClearUserData() {
        mClearDataButton.setEnabled(false);
        String packageName = mAppInfo.packageName;
        Log.i(TAG, "Clearing user data for package : " + packageName);
        if(mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
        if(!res) {
            Log.i(TAG, "Couldnt clear application user data for package:"+packageName);
            showDialogInner(DLG_CANNOT_CLEAR_DATA);
        } else {
            mClearDataButton.setText(R.string.recompute_size);
        }
    }
    private void showDialogInner(int id) {
        showDialog(id);
    }
    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case DLG_CLEAR_DATA:
            return new AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_data_dlg_title))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.clear_data_dlg_text))
            .setPositiveButton(R.string.dlg_ok,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    initiateClearUserData();
                }
            })
            .setNegativeButton(R.string.dlg_cancel, null)
            .create();
        case DLG_FACTORY_RESET:
            return new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_factory_reset_dlg_title))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.app_factory_reset_dlg_text))
            .setPositiveButton(R.string.dlg_ok,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    uninstallPkg(mAppInfo.packageName);
                }
            })
            .setNegativeButton(R.string.dlg_cancel, null)
            .create();
        case DLG_APP_NOT_FOUND:
            return new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_not_found_dlg_title))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.app_not_found_dlg_title))
            .setNeutralButton(getString(R.string.dlg_ok),
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setIntentAndFinish(true, true);
                }
            })
            .create();
        case DLG_CANNOT_CLEAR_DATA:
            return new AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_failed_dlg_title))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.clear_failed_dlg_text))
            .setNeutralButton(R.string.dlg_ok,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mClearDataButton.setEnabled(false);
                    setIntentAndFinish(false, false);
                }
            })
            .create();
            case DLG_FORCE_STOP:
                return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.force_stop_dlg_title))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(R.string.force_stop_dlg_text))
                .setPositiveButton(R.string.dlg_ok,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    forceStopPackage(mAppInfo.packageName);
                }
            })
            .setNegativeButton(R.string.dlg_cancel, null)
            .create();
            case DLG_MOVE_FAILED:
                CharSequence msg = getString(R.string.move_app_failed_dlg_text,
                        getMoveErrMsg(mMoveErrorCode));
                return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.move_app_failed_dlg_title))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(msg)
                .setNeutralButton(R.string.dlg_ok, null)
                .create();
        }
        return null;
    }
    private void uninstallPkg(String packageName) {
        Uri packageURI = Uri.parse("package:"+packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);
        setIntentAndFinish(true, true);
    }
    private void forceStopPackage(String pkgName) {
        ActivityManager am = (ActivityManager)getSystemService(
                Context.ACTIVITY_SERVICE);
        am.forceStopPackage(pkgName);
        checkForceStop();
    }
    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mForceStopButton.setEnabled(getResultCode() != RESULT_CANCELED);
            mForceStopButton.setOnClickListener(InstalledAppDetails.this);
        }
    };
    private void checkForceStop() {
        Intent intent = new Intent(Intent.ACTION_QUERY_PACKAGE_RESTART,
                Uri.fromParts("package", mAppInfo.packageName, null));
        intent.putExtra(Intent.EXTRA_PACKAGES, new String[] { mAppInfo.packageName });
        intent.putExtra(Intent.EXTRA_UID, mAppInfo.uid);
        sendOrderedBroadcast(intent, null, mCheckKillProcessesReceiver, null,
                Activity.RESULT_CANCELED, null, null);
    }
    public void onClick(View v) {
        String packageName = mAppInfo.packageName;
        if(v == mUninstallButton) {
            if (mUpdatedSysApp) {
                showDialogInner(DLG_FACTORY_RESET);
            } else {
                uninstallPkg(packageName);
            }
        } else if(v == mActivitiesButton) {
            mPm.clearPackagePreferredActivities(packageName);
            mActivitiesButton.setEnabled(false);
        } else if(v == mClearDataButton) {
            if (mAppInfo.manageSpaceActivityName != null) {
                Intent intent = new Intent(Intent.ACTION_DEFAULT);
                intent.setClassName(mAppInfo.packageName, mAppInfo.manageSpaceActivityName);
                startActivityForResult(intent, -1);
            } else {
                showDialogInner(DLG_CLEAR_DATA);
            }
        } else if (v == mClearCacheButton) {
            if (mClearCacheObserver == null) {
                mClearCacheObserver = new ClearCacheObserver();
            }
            mPm.deleteApplicationCacheFiles(packageName, mClearCacheObserver);
        } else if (v == mForceStopButton) {
            forceStopPackage(mAppInfo.packageName);
        } else if (v == mMoveAppButton) {
            if (mPackageMoveObserver == null) {
                mPackageMoveObserver = new PackageMoveObserver();
            }
            int moveFlags = (mAppInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0 ?
                    PackageManager.MOVE_INTERNAL : PackageManager.MOVE_EXTERNAL_MEDIA;
            mMoveInProgress = true;
            refreshButtons();
            mPm.movePackage(mAppInfo.packageName, mPackageMoveObserver, moveFlags);
        }
    }
}
