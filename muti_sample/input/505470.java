public class Memory extends PreferenceActivity implements OnCancelListener {
    private static final String TAG = "Memory";
    private static final boolean localLOGV = false;
    private static final String MEMORY_SD_SIZE = "memory_sd_size";
    private static final String MEMORY_SD_AVAIL = "memory_sd_avail";
    private static final String MEMORY_SD_MOUNT_TOGGLE = "memory_sd_mount_toggle";
    private static final String MEMORY_SD_FORMAT = "memory_sd_format";
    private static final int DLG_CONFIRM_UNMOUNT = 1;
    private static final int DLG_ERROR_UNMOUNT = 2;
    private Resources mRes;
    private Preference mSdSize;
    private Preference mSdAvail;
    private Preference mSdMountToggle;
    private Preference mSdFormat;
    private IMountService mMountService = null;
    private StorageManager mStorageManager = null;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }
        addPreferencesFromResource(R.xml.device_info_memory);
        mRes = getResources();
        mSdSize = findPreference(MEMORY_SD_SIZE);
        mSdAvail = findPreference(MEMORY_SD_AVAIL);
        mSdMountToggle = findPreference(MEMORY_SD_MOUNT_TOGGLE);
        mSdFormat = findPreference(MEMORY_SD_FORMAT);
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
        updateMemoryStatus();
    }
    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "Received storage state changed notification that " +
                    path + " changed state from " + oldState +
                    " to " + newState);
            updateMemoryStatus();
        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    @Override
    protected void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        super.onDestroy();
    }
    private synchronized IMountService getMountService() {
       if (mMountService == null) {
           IBinder service = ServiceManager.getService("mount");
           if (service != null) {
               mMountService = IMountService.Stub.asInterface(service);
           } else {
               Log.e(TAG, "Can't get mount service");
           }
       }
       return mMountService;
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSdMountToggle) {
            String status = Environment.getExternalStorageState();
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                unmount();
            } else {
                mount();
            }
            return true;
        } else if (preference == mSdFormat) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(this, com.android.settings.MediaFormat.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMemoryStatus();
        }
    };
    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case DLG_CONFIRM_UNMOUNT:
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.dlg_confirm_unmount_title)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doUnmount(true);
                        }})
                    .setNegativeButton(R.string.cancel, null)
                    .setMessage(R.string.dlg_confirm_unmount_text)
                    .setOnCancelListener(this)
                    .create();
        case DLG_ERROR_UNMOUNT:
            return new AlertDialog.Builder(this                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            )
            .setTitle(R.string.dlg_error_unmount_title)
            .setNeutralButton(R.string.dlg_ok, null)
            .setMessage(R.string.dlg_error_unmount_text)
            .setOnCancelListener(this)
            .create();
        }
        return null;
    }
    private void doUnmount(boolean force) {
        Toast.makeText(this, R.string.unmount_inform_text, Toast.LENGTH_SHORT).show();
        IMountService mountService = getMountService();
        String extStoragePath = Environment.getExternalStorageDirectory().toString();
        try {
            mSdMountToggle.setEnabled(false);
            mSdMountToggle.setTitle(mRes.getString(R.string.sd_ejecting_title));
            mSdMountToggle.setSummary(mRes.getString(R.string.sd_ejecting_summary));
            mountService.unmountVolume(extStoragePath, force);
        } catch (RemoteException e) {
            showDialogInner(DLG_ERROR_UNMOUNT);
        }
    }
    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }
    private boolean hasAppsAccessingStorage() throws RemoteException {
        String extStoragePath = Environment.getExternalStorageDirectory().toString();
        IMountService mountService = getMountService();
        boolean showPidDialog = false;
        int stUsers[] = mountService.getStorageUsers(extStoragePath);
        if (stUsers != null && stUsers.length > 0) {
            return true;
        }
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ApplicationInfo> list = am.getRunningExternalApplications();
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }
    private void unmount() {
        try {
           if (hasAppsAccessingStorage()) {
               if (localLOGV) Log.i(TAG, "Do have storage users accessing media");
               showDialogInner(DLG_CONFIRM_UNMOUNT);
           } else {
               doUnmount(true);
           }
        } catch (RemoteException e) {
            Log.e(TAG, "Is MountService running?");
            showDialogInner(DLG_ERROR_UNMOUNT);
        }
    }
    private void mount() {
        IMountService mountService = getMountService();
        try {
            if (mountService != null) {
                mountService.mountVolume(Environment.getExternalStorageDirectory().toString());
            } else {
                Log.e(TAG, "Mount service is null, can't mount");
            }
        } catch (RemoteException ex) {
        }
    }
    private void updateMemoryStatus() {
        String status = Environment.getExternalStorageState();
        String readOnly = "";
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            status = Environment.MEDIA_MOUNTED;
            readOnly = mRes.getString(R.string.read_only);
        }
        mSdFormat.setEnabled(false);
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File path = Environment.getExternalStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();
                mSdSize.setSummary(formatSize(totalBlocks * blockSize));
                mSdAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly);
                mSdMountToggle.setEnabled(true);
                mSdMountToggle.setTitle(mRes.getString(R.string.sd_eject));
                mSdMountToggle.setSummary(mRes.getString(R.string.sd_eject_summary));
            } catch (IllegalArgumentException e) {
                status = Environment.MEDIA_REMOVED;
            }
        } else {
            mSdSize.setSummary(mRes.getString(R.string.sd_unavailable));
            mSdAvail.setSummary(mRes.getString(R.string.sd_unavailable));
            if (status.equals(Environment.MEDIA_UNMOUNTED) ||
                status.equals(Environment.MEDIA_NOFS) ||
                status.equals(Environment.MEDIA_UNMOUNTABLE) ) {
                mSdFormat.setEnabled(true);
                mSdMountToggle.setEnabled(true);
                mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
                mSdMountToggle.setSummary(mRes.getString(R.string.sd_mount_summary));
            } else {
                mSdMountToggle.setEnabled(false);
                mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
                mSdMountToggle.setSummary(mRes.getString(R.string.sd_insert_summary));
            }
        }
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        findPreference("memory_internal_avail").setSummary(formatSize(availableBlocks * blockSize));
    }
    private String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
    }
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
