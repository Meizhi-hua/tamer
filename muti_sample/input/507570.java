public class BluetoothPbapActivity extends AlertActivity implements
        DialogInterface.OnClickListener, Preference.OnPreferenceChangeListener, TextWatcher {
    private static final String TAG = "BluetoothPbapActivity";
    private static final boolean V = BluetoothPbapService.VERBOSE;
    private static final int BLUETOOTH_OBEX_AUTHKEY_MAX_LENGTH = 16;
    private static final int DIALOG_YES_NO_CONNECT = 1;
    private static final int DIALOG_YES_NO_AUTH = 2;
    private static final String KEY_USER_TIMEOUT = "user_timeout";
    private View mView;
    private EditText mKeyView;
    private TextView messageView;
    private String mSessionKey = "";
    private int mCurrentDialog;
    private Button mOkButton;
    private CheckBox mAlwaysAllowed;
    private boolean mTimeout = false;
    private boolean mAlwaysAllowedValue = true;
    private static final int DISMISS_TIMEOUT_DIALOG = 0;
    private static final int DISMISS_TIMEOUT_DIALOG_VALUE = 2000;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!BluetoothPbapService.USER_CONFIRM_TIMEOUT_ACTION.equals(intent.getAction())) {
                return;
            }
            onTimeout();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        String action = i.getAction();
        if (action.equals(BluetoothPbapService.ACCESS_REQUEST_ACTION)) {
            showPbapDialog(DIALOG_YES_NO_CONNECT);
            mCurrentDialog = DIALOG_YES_NO_CONNECT;
        } else if (action.equals(BluetoothPbapService.AUTH_CHALL_ACTION)) {
            showPbapDialog(DIALOG_YES_NO_AUTH);
            mCurrentDialog = DIALOG_YES_NO_AUTH;
        } else {
            Log.e(TAG, "Error: this activity may be started only with intent "
                    + "PBAP_ACCESS_REQUEST or PBAP_AUTH_CHALL ");
            finish();
        }
        registerReceiver(mReceiver, new IntentFilter(
                BluetoothPbapService.USER_CONFIRM_TIMEOUT_ACTION));
    }
    private void showPbapDialog(int id) {
        final AlertController.AlertParams p = mAlertParams;
        switch (id) {
            case DIALOG_YES_NO_CONNECT:
                p.mIconId = android.R.drawable.ic_dialog_info;
                p.mTitle = getString(R.string.pbap_acceptance_dialog_header);
                p.mView = createView(DIALOG_YES_NO_CONNECT);
                p.mPositiveButtonText = getString(android.R.string.yes);
                p.mPositiveButtonListener = this;
                p.mNegativeButtonText = getString(android.R.string.no);
                p.mNegativeButtonListener = this;
                mOkButton = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
                setupAlert();
                break;
            case DIALOG_YES_NO_AUTH:
                p.mIconId = android.R.drawable.ic_dialog_info;
                p.mTitle = getString(R.string.pbap_session_key_dialog_header);
                p.mView = createView(DIALOG_YES_NO_AUTH);
                p.mPositiveButtonText = getString(android.R.string.ok);
                p.mPositiveButtonListener = this;
                p.mNegativeButtonText = getString(android.R.string.cancel);
                p.mNegativeButtonListener = this;
                setupAlert();
                mOkButton = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
                mOkButton.setEnabled(false);
                break;
            default:
                break;
        }
    }
    private String createDisplayText(final int id) {
        String mRemoteName = BluetoothPbapService.getRemoteDeviceName();
        switch (id) {
            case DIALOG_YES_NO_CONNECT:
                String mMessage1 = getString(R.string.pbap_acceptance_dialog_title, mRemoteName,
                        mRemoteName);
                return mMessage1;
            case DIALOG_YES_NO_AUTH:
                String mMessage2 = getString(R.string.pbap_session_key_dialog_title, mRemoteName);
                return mMessage2;
            default:
                return null;
        }
    }
    private View createView(final int id) {
        switch (id) {
            case DIALOG_YES_NO_CONNECT:
                mView = getLayoutInflater().inflate(R.layout.access, null);
                messageView = (TextView)mView.findViewById(R.id.message);
                messageView.setText(createDisplayText(id));
                mAlwaysAllowed = (CheckBox)mView.findViewById(R.id.alwaysallowed);
                mAlwaysAllowed.setChecked(true);
                mAlwaysAllowed.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mAlwaysAllowedValue = true;
                        } else {
                            mAlwaysAllowedValue = false;
                        }
                    }
                });
                return mView;
            case DIALOG_YES_NO_AUTH:
                mView = getLayoutInflater().inflate(R.layout.auth, null);
                messageView = (TextView)mView.findViewById(R.id.message);
                messageView.setText(createDisplayText(id));
                mKeyView = (EditText)mView.findViewById(R.id.text);
                mKeyView.addTextChangedListener(this);
                mKeyView.setFilters(new InputFilter[] {
                    new LengthFilter(BLUETOOTH_OBEX_AUTHKEY_MAX_LENGTH)
                });
                return mView;
            default:
                return null;
        }
    }
    private void onPositive() {
        if (!mTimeout) {
            if (mCurrentDialog == DIALOG_YES_NO_CONNECT) {
                sendIntentToReceiver(BluetoothPbapService.ACCESS_ALLOWED_ACTION,
                        BluetoothPbapService.EXTRA_ALWAYS_ALLOWED, mAlwaysAllowedValue);
            } else if (mCurrentDialog == DIALOG_YES_NO_AUTH) {
                sendIntentToReceiver(BluetoothPbapService.AUTH_RESPONSE_ACTION,
                        BluetoothPbapService.EXTRA_SESSION_KEY, mSessionKey);
                mKeyView.removeTextChangedListener(this);
            }
        }
        mTimeout = false;
        finish();
    }
    private void onNegative() {
        if (mCurrentDialog == DIALOG_YES_NO_CONNECT) {
            sendIntentToReceiver(BluetoothPbapService.ACCESS_DISALLOWED_ACTION, null, null);
        } else if (mCurrentDialog == DIALOG_YES_NO_AUTH) {
            sendIntentToReceiver(BluetoothPbapService.AUTH_CANCELLED_ACTION, null, null);
            mKeyView.removeTextChangedListener(this);
        }
        finish();
    }
    private void sendIntentToReceiver(final String intentName, final String extraName,
            final String extraValue) {
        Intent intent = new Intent(intentName);
        intent.setClassName(BluetoothPbapService.THIS_PACKAGE_NAME, BluetoothPbapReceiver.class
                .getName());
        if (extraName != null) {
            intent.putExtra(extraName, extraValue);
        }
        sendBroadcast(intent);
    }
    private void sendIntentToReceiver(final String intentName, final String extraName,
            final boolean extraValue) {
        Intent intent = new Intent(intentName);
        intent.setClassName(BluetoothPbapService.THIS_PACKAGE_NAME, BluetoothPbapReceiver.class
                .getName());
        if (extraName != null) {
            intent.putExtra(extraName, extraValue);
        }
        sendBroadcast(intent);
    }
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mCurrentDialog == DIALOG_YES_NO_AUTH) {
                    mSessionKey = mKeyView.getText().toString();
                }
                onPositive();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                onNegative();
                break;
            default:
                break;
        }
    }
    private void onTimeout() {
        mTimeout = true;
        if (mCurrentDialog == DIALOG_YES_NO_CONNECT) {
            messageView.setText(getString(R.string.pbap_acceptance_timeout_message,
                    BluetoothPbapService.getRemoteDeviceName()));
            mAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
            mAlwaysAllowed.setVisibility(View.GONE);
            mAlwaysAllowed.clearFocus();
        } else if (mCurrentDialog == DIALOG_YES_NO_AUTH) {
            messageView.setText(getString(R.string.pbap_authentication_timeout_message,
                    BluetoothPbapService.getRemoteDeviceName()));
            mKeyView.setVisibility(View.GONE);
            mKeyView.clearFocus();
            mKeyView.removeTextChangedListener(this);
            mOkButton.setEnabled(true);
            mAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
        }
        mTimeoutHandler.sendMessageDelayed(mTimeoutHandler.obtainMessage(DISMISS_TIMEOUT_DIALOG),
                DISMISS_TIMEOUT_DIALOG_VALUE);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTimeout = savedInstanceState.getBoolean(KEY_USER_TIMEOUT);
        if (V) Log.v(TAG, "onRestoreInstanceState() mTimeout: " + mTimeout);
        if (mTimeout) {
            onTimeout();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_USER_TIMEOUT, mTimeout);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
    public void beforeTextChanged(CharSequence s, int start, int before, int after) {
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
    public void afterTextChanged(android.text.Editable s) {
        if (s.length() > 0) {
            mOkButton.setEnabled(true);
        }
    }
    private final Handler mTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_TIMEOUT_DIALOG:
                    if (V) Log.v(TAG, "Received DISMISS_TIMEOUT_DIALOG msg.");
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
