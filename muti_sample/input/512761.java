public class BluetoothPairingDialog extends AlertActivity implements DialogInterface.OnClickListener,
        TextWatcher {
    private static final String TAG = "BluetoothPairingDialog";
    private final int BLUETOOTH_PIN_MAX_LENGTH = 16;
    private final int BLUETOOTH_PASSKEY_MAX_LENGTH = 6;
    private LocalBluetoothManager mLocalManager;
    private BluetoothDevice mDevice;
    private int mType;
    private String mPasskey;
    private EditText mPairingView;
    private Button mOkButton;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                                   BluetoothDevice.ERROR);
                if (bondState == BluetoothDevice.BOND_BONDED ||
                        bondState == BluetoothDevice.BOND_NONE) {
                    dismissDialog();
                }
            } else if(BluetoothDevice.ACTION_PAIRING_CANCEL.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null || device.equals(mDevice)) {
                    dismissDialog();
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (!intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST))
        {
            Log.e(TAG,
                  "Error: this activity may be started only with intent " +
                  BluetoothDevice.ACTION_PAIRING_REQUEST);
            finish();
        }
        mLocalManager = LocalBluetoothManager.getInstance(this);
        mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
        if (mType == BluetoothDevice.PAIRING_VARIANT_PIN) {
            createUserEntryDialog();
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_PASSKEY) {
            createUserEntryDialog();
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION){
            int passkey =
                intent.getIntExtra(BluetoothDevice.EXTRA_PASSKEY, BluetoothDevice.ERROR);
            if (passkey == BluetoothDevice.ERROR) {
                Log.e(TAG, "Invalid ConfirmationPasskey received, not showing any dialog");
                return;
            }
            mPasskey = String.format("%06d", passkey);
            createConfirmationDialog();
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_CONSENT) {
            createConsentDialog();
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY) {
            int passkey =
                intent.getIntExtra(BluetoothDevice.EXTRA_PASSKEY, BluetoothDevice.ERROR);
            if (passkey == BluetoothDevice.ERROR) {
                Log.e(TAG, "Invalid ConfirmationPasskey received, not showing any dialog");
                return;
            }
            mPasskey = String.format("%06d", passkey);
            createDisplayPasskeyDialog();
        } else {
            Log.e(TAG, "Incorrect pairing type received, not showing any dialog");
        }
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_CANCEL));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }
    private void createUserEntryDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createView();
        p.mPositiveButtonText = getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(android.R.string.cancel);
        p.mNegativeButtonListener = this;
        setupAlert();
        mOkButton = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
        mOkButton.setEnabled(false);
    }
    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.bluetooth_pin_entry, null);
        String name = mLocalManager.getCachedDeviceManager().getName(mDevice);
        TextView messageView = (TextView) view.findViewById(R.id.message);
        mPairingView = (EditText) view.findViewById(R.id.text);
        mPairingView.addTextChangedListener(this);
        if (mType == BluetoothDevice.PAIRING_VARIANT_PIN) {
            messageView.setText(getString(R.string.bluetooth_enter_pin_msg, name));
            mPairingView.setFilters(new InputFilter[] {
                    new LengthFilter(BLUETOOTH_PIN_MAX_LENGTH) });
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_PASSKEY){
            messageView.setText(getString(R.string.bluetooth_enter_passkey_msg, name));
            mPairingView.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_SIGNED);
            mPairingView.setFilters(new InputFilter[] {
                    new LengthFilter(BLUETOOTH_PASSKEY_MAX_LENGTH)});
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
            mPairingView.setVisibility(View.GONE);
            messageView.setText(getString(R.string.bluetooth_confirm_passkey_msg, name,
                    mPasskey));
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_CONSENT) {
            mPairingView.setVisibility(View.GONE);
            messageView.setText(getString(R.string.bluetooth_incoming_pairing_msg, name));
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY) {
            mPairingView.setVisibility(View.GONE);
            messageView.setText(getString(R.string.bluetooth_display_passkey_msg, name, mPasskey));
        } else {
            Log.e(TAG, "Incorrect pairing type received, not creating view");
        }
        return view;
    }
    private void createConfirmationDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.bluetooth_pairing_accept);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.bluetooth_pairing_decline);
        p.mNegativeButtonListener = this;
        setupAlert();
    }
    private void createConsentDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.bluetooth_pairing_accept);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.bluetooth_pairing_decline);
        p.mNegativeButtonListener = this;
        setupAlert();
    }
    private void createDisplayPasskeyDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createView();
        p.mPositiveButtonText = getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        setupAlert();
        mDevice.setPairingConfirmation(true);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            mOkButton.setEnabled(true);
        }
    }
    private void dismissDialog() {
        this.dismiss();
    }
    private void onPair(String value) {
        if (mType == BluetoothDevice.PAIRING_VARIANT_PIN) {
            byte[] pinBytes = BluetoothDevice.convertPinToBytes(value);
            if (pinBytes == null) {
                return;
            }
            mDevice.setPin(pinBytes);
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_PASSKEY) {
            int passkey = Integer.parseInt(value);
            mDevice.setPasskey(passkey);
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
            mDevice.setPairingConfirmation(true);
        } else if (mType ==  BluetoothDevice.PAIRING_VARIANT_CONSENT) {
            mDevice.setPairingConfirmation(true);
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY) {
        } else {
            Log.e(TAG, "Incorrect pairing type received");
        }
    }
    private void onCancel() {
        mDevice.cancelPairingUserInput();
    }
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                onPair(mPairingView.getText().toString());
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                onCancel();
                break;
        }
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
