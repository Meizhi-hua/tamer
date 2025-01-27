public class PrivacySettings extends PreferenceActivity implements
        DialogInterface.OnClickListener {
    private static final String GSETTINGS_PROVIDER = "com.google.settings";
    private static final String BACKUP_CATEGORY = "backup_category";
    private static final String BACKUP_DATA = "backup_data";
    private static final String AUTO_RESTORE = "auto_restore";
    private CheckBoxPreference mBackup;
    private CheckBoxPreference mAutoRestore;
    private Dialog mConfirmDialog;
    private static final int DIALOG_ERASE_BACKUP = 2;
    private int     mDialogType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.privacy_settings);
        final PreferenceScreen screen = getPreferenceScreen();
        mBackup = (CheckBoxPreference) screen.findPreference(BACKUP_DATA);
        mAutoRestore = (CheckBoxPreference) screen.findPreference(AUTO_RESTORE);
        if (getPackageManager().resolveContentProvider(GSETTINGS_PROVIDER, 0) == null) {
            screen.removePreference(findPreference(BACKUP_CATEGORY));
        }
        updateToggles();
    }
    @Override
    public void onStop() {
        if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
            mConfirmDialog.dismiss();
        }
        mConfirmDialog = null;
        mDialogType = 0;
        super.onStop();
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mBackup) {
            if (!mBackup.isChecked()) {
                showEraseBackupDialog();
            } else {
                setBackupEnabled(true);
            }
        } else if (preference == mAutoRestore) {
            IBackupManager bm = IBackupManager.Stub.asInterface(
                    ServiceManager.getService(Context.BACKUP_SERVICE));
            if (bm != null) {
                boolean curState = mAutoRestore.isChecked();
                try {
                    bm.setAutoRestore(curState);
                } catch (RemoteException e) {
                    mAutoRestore.setChecked(!curState);
                }
            }
        }
        return false;
    }
    private void showEraseBackupDialog() {
        mBackup.setChecked(true);
        mDialogType = DIALOG_ERASE_BACKUP;
        CharSequence msg = getResources().getText(R.string.backup_erase_dialog_message);
        mConfirmDialog = new AlertDialog.Builder(this).setMessage(msg)
                .setTitle(R.string.backup_erase_dialog_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this)
                .show();
    }
    private void updateToggles() {
        ContentResolver res = getContentResolver();
        final boolean backupEnabled = Settings.Secure.getInt(res,
                Settings.Secure.BACKUP_ENABLED, 0) == 1;
        mBackup.setChecked(backupEnabled);
        mAutoRestore.setChecked(Settings.Secure.getInt(res,
                Settings.Secure.BACKUP_AUTO_RESTORE, 1) == 1);
        mAutoRestore.setEnabled(backupEnabled);
    }
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (mDialogType == DIALOG_ERASE_BACKUP) {
                setBackupEnabled(false);
            }
        } else {
            if (mDialogType == DIALOG_ERASE_BACKUP) {
                mBackup.setChecked(true);
                mAutoRestore.setEnabled(true);
            }
        }
        mDialogType = 0;
    }
    private void setBackupEnabled(boolean enable) {
        IBackupManager bm = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        if (bm != null) {
            try {
                bm.setBackupEnabled(enable);
            } catch (RemoteException e) {
                mBackup.setChecked(!enable);
                mAutoRestore.setEnabled(!enable);
                return;
            }
        }
        mBackup.setChecked(enable);
        mAutoRestore.setEnabled(enable);
    }
}
