public class SecuritySettings extends PreferenceActivity {
    private static final String KEY_UNLOCK_SET_OR_CHANGE = "unlock_set_or_change";
    private static final String PACKAGE = "com.android.settings";
    private static final String ICC_LOCK_SETTINGS = PACKAGE + ".IccLockSettings";
    private static final String KEY_LOCK_ENABLED = "lockenabled";
    private static final String KEY_VISIBLE_PATTERN = "visiblepattern";
    private static final String KEY_TACTILE_FEEDBACK_ENABLED = "unlock_tactile_feedback";
    private CheckBoxPreference mVisiblePattern;
    private CheckBoxPreference mTactileFeedback;
    private CheckBoxPreference mShowPassword;
    private static final String LOCATION_NETWORK = "location_network";
    private static final String LOCATION_GPS = "location_gps";
    private static final String ASSISTED_GPS = "assisted_gps";
    private static final int SET_OR_CHANGE_LOCK_METHOD_REQUEST = 123;
    private CredentialStorage mCredentialStorage = new CredentialStorage();
    private CheckBoxPreference mNetwork;
    private CheckBoxPreference mGps;
    private CheckBoxPreference mAssistedGps;
    DevicePolicyManager mDPM;
    private ContentQueryMap mContentQueryMap;
    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private LockPatternUtils mLockPatternUtils;
    private final class SettingsObserver implements Observer {
        public void update(Observable o, Object arg) {
            updateToggles();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockPatternUtils = new LockPatternUtils(this);
        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(this);
        createPreferenceHierarchy();
        updateToggles();
        Cursor settingsCursor = getContentResolver().query(Settings.Secure.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?)",
                new String[]{Settings.Secure.LOCATION_PROVIDERS_ALLOWED},
                null);
        mContentQueryMap = new ContentQueryMap(settingsCursor, Settings.System.NAME, true, null);
        mContentQueryMap.addObserver(new SettingsObserver());
    }
    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.security_settings);
        root = this.getPreferenceScreen();
        mNetwork = (CheckBoxPreference) getPreferenceScreen().findPreference(LOCATION_NETWORK);
        mGps = (CheckBoxPreference) getPreferenceScreen().findPreference(LOCATION_GPS);
        mAssistedGps = (CheckBoxPreference) getPreferenceScreen().findPreference(ASSISTED_GPS);
        PreferenceManager pm = getPreferenceManager();
        if (!mLockPatternUtils.isSecure()) {
            addPreferencesFromResource(R.xml.security_settings_chooser);
        } else {
            switch (mLockPatternUtils.getKeyguardStoredPasswordQuality()) {
                case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                    addPreferencesFromResource(R.xml.security_settings_pattern);
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                    addPreferencesFromResource(R.xml.security_settings_pin);
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                    addPreferencesFromResource(R.xml.security_settings_password);
                    break;
            }
        }
        mVisiblePattern = (CheckBoxPreference) pm.findPreference(KEY_VISIBLE_PATTERN);
        mTactileFeedback = (CheckBoxPreference) pm.findPreference(KEY_TACTILE_FEEDBACK_ENABLED);
        int activePhoneType = TelephonyManager.getDefault().getPhoneType();
        if (TelephonyManager.PHONE_TYPE_CDMA != activePhoneType)
        {
            PreferenceScreen simLockPreferences = getPreferenceManager()
                    .createPreferenceScreen(this);
            simLockPreferences.setTitle(R.string.sim_lock_settings_category);
            simLockPreferences.setIntent(new Intent().setClassName(PACKAGE, ICC_LOCK_SETTINGS));
            PreferenceCategory simLockCat = new PreferenceCategory(this);
            simLockCat.setTitle(R.string.sim_lock_settings_title);
            root.addPreference(simLockCat);
            simLockCat.addPreference(simLockPreferences);
        }
        PreferenceCategory passwordsCat = new PreferenceCategory(this);
        passwordsCat.setTitle(R.string.security_passwords_title);
        root.addPreference(passwordsCat);
        CheckBoxPreference showPassword = mShowPassword = new CheckBoxPreference(this);
        showPassword.setKey("show_password");
        showPassword.setTitle(R.string.show_password);
        showPassword.setSummary(R.string.show_password_summary);
        showPassword.setPersistent(false);
        passwordsCat.addPreference(showPassword);
        PreferenceCategory devicePoliciesCat = new PreferenceCategory(this);
        devicePoliciesCat.setTitle(R.string.device_admin_title);
        root.addPreference(devicePoliciesCat);
        Preference deviceAdminButton = new Preference(this);
        deviceAdminButton.setTitle(R.string.manage_device_admin);
        deviceAdminButton.setSummary(R.string.manage_device_admin_summary);
        Intent deviceAdminIntent = new Intent();
        deviceAdminIntent.setClass(this, DeviceAdminSettings.class);
        deviceAdminButton.setIntent(deviceAdminIntent);
        devicePoliciesCat.addPreference(deviceAdminButton);
        PreferenceCategory credentialsCat = new PreferenceCategory(this);
        credentialsCat.setTitle(R.string.credentials_category);
        root.addPreference(credentialsCat);
        mCredentialStorage.createPreferences(credentialsCat, CredentialStorage.TYPE_KEYSTORE);
        return root;
    }
    @Override
    protected void onResume() {
        super.onResume();
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (mVisiblePattern != null) {
            mVisiblePattern.setChecked(lockPatternUtils.isVisiblePatternEnabled());
        }
        if (mTactileFeedback != null) {
            mTactileFeedback.setChecked(lockPatternUtils.isTactileFeedbackEnabled());
        }
        mShowPassword.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.TEXT_SHOW_PASSWORD, 1) != 0);
        mCredentialStorage.resume();
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        final String key = preference.getKey();
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (KEY_UNLOCK_SET_OR_CHANGE.equals(key)) {
            Intent intent = new Intent(this, ChooseLockGeneric.class);
            startActivityForResult(intent, SET_OR_CHANGE_LOCK_METHOD_REQUEST);
        } else if (KEY_LOCK_ENABLED.equals(key)) {
            lockPatternUtils.setLockPatternEnabled(isToggled(preference));
        } else if (KEY_VISIBLE_PATTERN.equals(key)) {
            lockPatternUtils.setVisiblePatternEnabled(isToggled(preference));
        } else if (KEY_TACTILE_FEEDBACK_ENABLED.equals(key)) {
            lockPatternUtils.setTactileFeedbackEnabled(isToggled(preference));
        } else if (preference == mShowPassword) {
            Settings.System.putInt(getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD,
                    mShowPassword.isChecked() ? 1 : 0);
        } else if (preference == mNetwork) {
            Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                    LocationManager.NETWORK_PROVIDER, mNetwork.isChecked());
        } else if (preference == mGps) {
            boolean enabled = mGps.isChecked();
            Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                    LocationManager.GPS_PROVIDER, enabled);
            if (mAssistedGps != null) {
                mAssistedGps.setEnabled(enabled);
            }
        } else if (preference == mAssistedGps) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ASSISTED_GPS_ENABLED,
                    mAssistedGps.isChecked() ? 1 : 0);
        }
        return false;
    }
    private void updateToggles() {
        ContentResolver res = getContentResolver();
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                res, LocationManager.GPS_PROVIDER);
        mNetwork.setChecked(Settings.Secure.isLocationProviderEnabled(
                res, LocationManager.NETWORK_PROVIDER));
        mGps.setChecked(gpsEnabled);
        if (mAssistedGps != null) {
            mAssistedGps.setChecked(Settings.Secure.getInt(res,
                    Settings.Secure.ASSISTED_GPS_ENABLED, 2) == 1);
            mAssistedGps.setEnabled(gpsEnabled);
        }
    }
    private boolean isToggled(Preference pref) {
        return ((CheckBoxPreference) pref).isChecked();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        createPreferenceHierarchy();
    }
    private class CredentialStorage implements DialogInterface.OnClickListener,
            DialogInterface.OnDismissListener, Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener {
        private static final int MINIMUM_PASSWORD_LENGTH = 8;
        private static final int TYPE_KEYSTORE = 0;
        private static final int DLG_BASE = 0;
        private static final int DLG_UNLOCK = DLG_BASE + 1;
        private static final int DLG_PASSWORD = DLG_UNLOCK + 1;
        private static final int DLG_RESET = DLG_PASSWORD + 1;
        private KeyStore mKeyStore = KeyStore.getInstance();
        private int mState;
        private boolean mSubmit = false;
        private boolean mExternal = false;
        private int mShowingDialog = 0;
        private CheckBoxPreference mAccessCheckBox;
        private Preference mInstallButton;
        private Preference mPasswordButton;
        private Preference mResetButton;
        void resume() {
            mState = mKeyStore.test();
            updatePreferences(mState);
            Intent intent = getIntent();
            if (!mExternal && intent != null &&
                    Credentials.UNLOCK_ACTION.equals(intent.getAction())) {
                mExternal = true;
                if (mState == KeyStore.UNINITIALIZED) {
                    showPasswordDialog();
                } else if (mState == KeyStore.LOCKED) {
                    showUnlockDialog();
                } else {
                    finish();
                }
            }
        }
        private void initialize(String password) {
            mKeyStore.password(password);
            updatePreferences(KeyStore.NO_ERROR);
        }
        private void reset() {
            mKeyStore.reset();
            updatePreferences(KeyStore.UNINITIALIZED);
        }
        private void lock() {
            mKeyStore.lock();
            updatePreferences(KeyStore.LOCKED);
        }
        private int unlock(String password) {
            mKeyStore.unlock(password);
            return mKeyStore.getLastError();
        }
        private int changePassword(String oldPassword, String newPassword) {
            mKeyStore.password(oldPassword, newPassword);
            return mKeyStore.getLastError();
        }
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference == mAccessCheckBox) {
                if ((Boolean) value) {
                    showUnlockDialog();
                } else {
                    lock();
                }
                return true;
            }
            return true;
        }
        public boolean onPreferenceClick(Preference preference) {
            if (preference == mInstallButton) {
                Credentials.getInstance().installFromSdCard(SecuritySettings.this);
            } else if (preference == mPasswordButton) {
                showPasswordDialog();
            } else if (preference == mResetButton) {
                showResetDialog();
            } else {
                return false;
            }
            return true;
        }
        public void onClick(DialogInterface dialog, int button) {
            mSubmit = (button == DialogInterface.BUTTON_POSITIVE);
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                reset();
            }
        }
        public void onDismiss(DialogInterface dialog) {
            if (mSubmit && !isFinishing()) {
                mSubmit = false;
                if (!checkPassword((Dialog) dialog)) {
                    ((Dialog) dialog).show();
                    return;
                }
            }
            updatePreferences(mState);
            if (mExternal) {
                finish();
            }
        }
        private boolean checkPassword(Dialog dialog) {
            String oldPassword = getText(dialog, R.id.old_password);
            String newPassword = getText(dialog, R.id.new_password);
            String confirmPassword = getText(dialog, R.id.confirm_password);
            if (oldPassword != null && oldPassword.length() == 0) {
                showError(dialog, R.string.credentials_password_empty);
                return false;
            } else if (newPassword == null) {
                return !checkError(dialog, unlock(oldPassword));
            } else if (newPassword.length() == 0 || confirmPassword.length() == 0) {
                showError(dialog, R.string.credentials_passwords_empty);
            } else if (newPassword.length() < MINIMUM_PASSWORD_LENGTH) {
                showError(dialog, R.string.credentials_password_too_short);
            } else if (!newPassword.equals(confirmPassword)) {
                showError(dialog, R.string.credentials_passwords_mismatch);
            } else if (oldPassword == null) {
                initialize(newPassword);
                return true;
            } else {
                return !checkError(dialog, changePassword(oldPassword, newPassword));
            }
            return false;
        }
        private boolean checkError(Dialog dialog, int error) {
            if (error == KeyStore.NO_ERROR) {
                updatePreferences(KeyStore.NO_ERROR);
                return false;
            }
            if (error == KeyStore.UNINITIALIZED) {
                updatePreferences(KeyStore.UNINITIALIZED);
                return false;
            }
            if (error < KeyStore.WRONG_PASSWORD) {
                return false;
            }
            int count = error - KeyStore.WRONG_PASSWORD + 1;
            if (count > 3) {
                showError(dialog, R.string.credentials_wrong_password);
            } else if (count == 1) {
                showError(dialog, R.string.credentials_reset_warning);
            } else {
                showError(dialog, R.string.credentials_reset_warning_plural, count);
            }
            return true;
        }
        private String getText(Dialog dialog, int viewId) {
            TextView view = (TextView) dialog.findViewById(viewId);
            return (view == null || view.getVisibility() == View.GONE) ? null :
                            view.getText().toString();
        }
        private void showError(Dialog dialog, int stringId, Object... formatArgs) {
            TextView view = (TextView) dialog.findViewById(R.id.error);
            if (view != null) {
                if (formatArgs == null || formatArgs.length == 0) {
                    view.setText(stringId);
                } else {
                    view.setText(dialog.getContext().getString(stringId, formatArgs));
                }
                view.setVisibility(View.VISIBLE);
            }
        }
        private void createPreferences(PreferenceCategory category, int type) {
            switch(type) {
            case TYPE_KEYSTORE:
                mAccessCheckBox = new CheckBoxPreference(SecuritySettings.this);
                mAccessCheckBox.setTitle(R.string.credentials_access);
                mAccessCheckBox.setSummary(R.string.credentials_access_summary);
                mAccessCheckBox.setOnPreferenceChangeListener(this);
                category.addPreference(mAccessCheckBox);
                mInstallButton = new Preference(SecuritySettings.this);
                mInstallButton.setTitle(R.string.credentials_install_certificates);
                mInstallButton.setSummary(R.string.credentials_install_certificates_summary);
                mInstallButton.setOnPreferenceClickListener(this);
                category.addPreference(mInstallButton);
                mPasswordButton = new Preference(SecuritySettings.this);
                mPasswordButton.setTitle(R.string.credentials_set_password);
                mPasswordButton.setSummary(R.string.credentials_set_password_summary);
                mPasswordButton.setOnPreferenceClickListener(this);
                category.addPreference(mPasswordButton);
                mResetButton = new Preference(SecuritySettings.this);
                mResetButton.setTitle(R.string.credentials_reset);
                mResetButton.setSummary(R.string.credentials_reset_summary);
                mResetButton.setOnPreferenceClickListener(this);
                category.addPreference(mResetButton);
                break;
            }
        }
        private void updatePreferences(int state) {
            mAccessCheckBox.setChecked(state == KeyStore.NO_ERROR);
            mResetButton.setEnabled(state != KeyStore.UNINITIALIZED);
            mAccessCheckBox.setEnabled(state != KeyStore.UNINITIALIZED);
            if (mState == state) {
                return;
            } else if (state == KeyStore.NO_ERROR) {
                Toast.makeText(SecuritySettings.this, R.string.credentials_enabled,
                        Toast.LENGTH_SHORT).show();
            } else if (state == KeyStore.UNINITIALIZED) {
                Toast.makeText(SecuritySettings.this, R.string.credentials_erased,
                        Toast.LENGTH_SHORT).show();
            } else if (state == KeyStore.LOCKED) {
                Toast.makeText(SecuritySettings.this, R.string.credentials_disabled,
                        Toast.LENGTH_SHORT).show();
            }
            mState = state;
        }
        private void showUnlockDialog() {
            View view = View.inflate(SecuritySettings.this,
                    R.layout.credentials_unlock_dialog, null);
            if (mExternal) {
                view.findViewById(R.id.hint).setVisibility(View.VISIBLE);
            }
            Dialog dialog = new AlertDialog.Builder(SecuritySettings.this)
                    .setView(view)
                    .setTitle(R.string.credentials_unlock)
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .create();
            dialog.setOnDismissListener(this);
            mShowingDialog = DLG_UNLOCK;
            dialog.show();
        }
        private void showPasswordDialog() {
            View view = View.inflate(SecuritySettings.this,
                    R.layout.credentials_password_dialog, null);
            if (mState == KeyStore.UNINITIALIZED) {
                view.findViewById(R.id.hint).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.old_password_prompt).setVisibility(View.VISIBLE);
                view.findViewById(R.id.old_password).setVisibility(View.VISIBLE);
            }
            Dialog dialog = new AlertDialog.Builder(SecuritySettings.this)
                    .setView(view)
                    .setTitle(R.string.credentials_set_password)
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .create();
            dialog.setOnDismissListener(this);
            mShowingDialog = DLG_PASSWORD;
            dialog.show();
        }
        private void showResetDialog() {
            mShowingDialog = DLG_RESET;
            new AlertDialog.Builder(SecuritySettings.this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.credentials_reset_hint)
                    .setNeutralButton(getString(android.R.string.ok), this)
                    .setNegativeButton(getString(android.R.string.cancel), this)
                    .create().show();
        }
    }
}
