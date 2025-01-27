public class ApnSettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    static final String TAG = "ApnSettings";
    public static final String EXTRA_POSITION = "position";
    public static final String RESTORE_CARRIERS_URI =
        "content:
    public static final String PREFERRED_APN_URI =
        "content:
    public static final String APN_ID = "apn_id";
    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int TYPES_INDEX = 3;
    private static final int MENU_NEW = Menu.FIRST;
    private static final int MENU_RESTORE = Menu.FIRST + 1;
    private static final int EVENT_RESTORE_DEFAULTAPN_START = 1;
    private static final int EVENT_RESTORE_DEFAULTAPN_COMPLETE = 2;
    private static final int DIALOG_RESTORE_DEFAULTAPN = 1001;
    private static final Uri DEFAULTAPN_URI = Uri.parse(RESTORE_CARRIERS_URI);
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
    private static boolean mRestoreDefaultApnMode;
    private RestoreApnUiHandler mRestoreApnUiHandler;
    private RestoreApnProcessHandler mRestoreApnProcessHandler;
    private String mSelectedKey;
    private IntentFilter mMobileStateFilter;
    private final BroadcastReceiver mMobileStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                Phone.DataState state = getMobileDataState(intent);
                switch (state) {
                case CONNECTED:
                    if (!mRestoreDefaultApnMode) {
                        fillList();
                    } else {
                        showDialog(DIALOG_RESTORE_DEFAULTAPN);
                    }
                    break;
                }
            }
        }
    };
    private static Phone.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(Phone.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(Phone.DataState.class, str);
        } else {
            return Phone.DataState.DISCONNECTED;
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.apn_settings);
        getListView().setItemsCanFocus(true);
        mMobileStateFilter = new IntentFilter(
                TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMobileStateReceiver, mMobileStateFilter);
        if (!mRestoreDefaultApnMode) {
            fillList();
        } else {
            showDialog(DIALOG_RESTORE_DEFAULTAPN);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMobileStateReceiver);
    }
    private void fillList() {
        String where = "numeric=\""
            + android.os.SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "")
            + "\"";
        Cursor cursor = managedQuery(Telephony.Carriers.CONTENT_URI, new String[] {
                "_id", "name", "apn", "type"}, where,
                Telephony.Carriers.DEFAULT_SORT_ORDER);
        PreferenceGroup apnList = (PreferenceGroup) findPreference("apn_list");
        apnList.removeAll();
        ArrayList<Preference> mmsApnList = new ArrayList<Preference>();
        mSelectedKey = getSelectedApnKey();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(NAME_INDEX);
            String apn = cursor.getString(APN_INDEX);
            String key = cursor.getString(ID_INDEX);
            String type = cursor.getString(TYPES_INDEX);
            ApnPreference pref = new ApnPreference(this);
            pref.setKey(key);
            pref.setTitle(name);
            pref.setSummary(apn);
            pref.setPersistent(false);
            pref.setOnPreferenceChangeListener(this);
            boolean selectable = ((type == null) || !type.equals("mms"));
            pref.setSelectable(selectable);
            if (selectable) {
                if ((mSelectedKey != null) && mSelectedKey.equals(key)) {
                    pref.setChecked();
                }
                apnList.addPreference(pref);
            } else {
                mmsApnList.add(pref);
            }
            cursor.moveToNext();
        }
        cursor.close();
        for (Preference preference : mmsApnList) {
            apnList.addPreference(preference);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_NEW, 0,
                getResources().getString(R.string.menu_new))
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_RESTORE, 0,
                getResources().getString(R.string.menu_restore))
                .setIcon(android.R.drawable.ic_menu_upload);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_NEW:
            addNewApn();
            return true;
        case MENU_RESTORE:
            restoreDefaultApn();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void addNewApn() {
        startActivity(new Intent(Intent.ACTION_INSERT, Telephony.Carriers.CONTENT_URI));
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        int pos = Integer.parseInt(preference.getKey());
        Uri url = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, pos);
        startActivity(new Intent(Intent.ACTION_EDIT, url));
        return true;
    }
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange(): Preference - " + preference
                + ", newValue - " + newValue + ", newValue type - "
                + newValue.getClass());
        if (newValue instanceof String) {
            setSelectedApnKey((String) newValue);
        }
        return true;
    }
    private void setSelectedApnKey(String key) {
        mSelectedKey = key;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(APN_ID, mSelectedKey);
        resolver.update(PREFERAPN_URI, values, null, null);
    }
    private String getSelectedApnKey() {
        String key = null;
        Cursor cursor = managedQuery(PREFERAPN_URI, new String[] {"_id"},
                null, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(ID_INDEX);
        }
        cursor.close();
        return key;
    }
    private boolean restoreDefaultApn() {
        showDialog(DIALOG_RESTORE_DEFAULTAPN);
        mRestoreDefaultApnMode = true;
        if (mRestoreApnUiHandler == null) {
            mRestoreApnUiHandler = new RestoreApnUiHandler();
        }
        if (mRestoreApnProcessHandler == null) {
            HandlerThread restoreDefaultApnThread = new HandlerThread(
                    "Restore default APN Handler: Process Thread");
            restoreDefaultApnThread.start();
            mRestoreApnProcessHandler = new RestoreApnProcessHandler(
                    restoreDefaultApnThread.getLooper(), mRestoreApnUiHandler);
        }
        mRestoreApnProcessHandler
                .sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_START);
        return true;
    }
    private class RestoreApnUiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RESTORE_DEFAULTAPN_COMPLETE:
                    fillList();
                    getPreferenceScreen().setEnabled(true);
                    mRestoreDefaultApnMode = false;
                    dismissDialog(DIALOG_RESTORE_DEFAULTAPN);
                    Toast.makeText(
                        ApnSettings.this,
                        getResources().getString(
                                R.string.restore_default_apn_completed),
                        Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
    private class RestoreApnProcessHandler extends Handler {
        private Handler mRestoreApnUiHandler;
        public RestoreApnProcessHandler(Looper looper, Handler restoreApnUiHandler) {
            super(looper);
            this.mRestoreApnUiHandler = restoreApnUiHandler;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RESTORE_DEFAULTAPN_START:
                    ContentResolver resolver = getContentResolver();
                    resolver.delete(DEFAULTAPN_URI, null, null);                    
                    mRestoreApnUiHandler
                        .sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_COMPLETE);
                    break;
            }
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_RESTORE_DEFAULTAPN) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.restore_default_apn));
            dialog.setCancelable(false);
            return dialog;
        }
        return null;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == DIALOG_RESTORE_DEFAULTAPN) {
            getPreferenceScreen().setEnabled(false);
        }
    }
}
