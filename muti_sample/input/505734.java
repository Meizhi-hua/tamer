public class ChangeIccPinScreen extends Activity {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private static final boolean DBG = false;
    private static final int EVENT_PIN_CHANGED = 100;
    private enum EntryState {
        ES_PIN,
        ES_PUK
    }
    private EntryState mState;
    private static final int NO_ERROR = 0;
    private static final int PIN_MISMATCH = 1;
    private static final int PIN_INVALID_LENGTH = 2;
    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 8;
    private Phone mPhone;
    private boolean mChangePin2;
    private TextView mBadPinError;
    private TextView mMismatchError;
    private EditText mOldPin;
    private EditText mNewPin1;
    private EditText mNewPin2;
    private EditText mPUKCode;
    private Button mButton;
    private Button mPUKSubmit;
    private ScrollView mScrollView;
    private LinearLayout mIccPUKPanel;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PIN_CHANGED:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    handleResult(ar);
                    break;
            }
            return;
        }
    };
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPhone = PhoneFactory.getDefaultPhone();
        resolveIntent();
        setContentView(R.layout.change_sim_pin_screen);
        mOldPin = (EditText) findViewById(R.id.old_pin);
        mOldPin.setKeyListener(DigitsKeyListener.getInstance());
        mOldPin.setMovementMethod(null);
        mOldPin.setOnClickListener(mClicked);
        mNewPin1 = (EditText) findViewById(R.id.new_pin1);
        mNewPin1.setKeyListener(DigitsKeyListener.getInstance());
        mNewPin1.setMovementMethod(null);
        mNewPin1.setOnClickListener(mClicked);
        mNewPin2 = (EditText) findViewById(R.id.new_pin2);
        mNewPin2.setKeyListener(DigitsKeyListener.getInstance());
        mNewPin2.setMovementMethod(null);
        mNewPin2.setOnClickListener(mClicked);
        mBadPinError = (TextView) findViewById(R.id.bad_pin);
        mMismatchError = (TextView) findViewById(R.id.mismatch);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(mClicked);
        mScrollView = (ScrollView) findViewById(R.id.scroll);
        mPUKCode = (EditText) findViewById(R.id.puk_code);
        mPUKCode.setKeyListener(DigitsKeyListener.getInstance());
        mPUKCode.setMovementMethod(null);
        mPUKCode.setOnClickListener(mClicked);
        mPUKSubmit = (Button) findViewById(R.id.puk_submit);
        mPUKSubmit.setOnClickListener(mClicked);
        mIccPUKPanel = (LinearLayout) findViewById(R.id.puk_panel);
        int id = mChangePin2 ? R.string.change_pin2 : R.string.change_pin;
        setTitle(getResources().getText(id));
        mState = EntryState.ES_PIN;
    }
    private void resolveIntent() {
        Intent intent = getIntent();
        mChangePin2 = intent.getBooleanExtra("pin2", mChangePin2);
    }
    private void reset() {
        mScrollView.scrollTo(0, 0);
        mBadPinError.setVisibility(View.GONE);
        mMismatchError.setVisibility(View.GONE);
    }
    private int validateNewPin(String p1, String p2) {
        if (p1 == null) {
            return PIN_INVALID_LENGTH;
        }
        if (!p1.equals(p2)) {
            return PIN_MISMATCH;
        }
        int len1 = p1.length();
        if (len1 < MIN_PIN_LENGTH || len1 > MAX_PIN_LENGTH) {
            return PIN_INVALID_LENGTH;
        }
        return NO_ERROR;
    }
    private View.OnClickListener mClicked = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == mOldPin) {
                mNewPin1.requestFocus();
            } else if (v == mNewPin1) {
                mNewPin2.requestFocus();
            } else if (v == mNewPin2) {
                mButton.requestFocus();
            } else if (v == mButton) {
                IccCard iccCardInterface = mPhone.getIccCard();
                if (iccCardInterface != null) {
                    String oldPin = mOldPin.getText().toString();
                    String newPin1 = mNewPin1.getText().toString();
                    String newPin2 = mNewPin2.getText().toString();
                    int error = validateNewPin(newPin1, newPin2);
                    switch (error) {
                        case PIN_INVALID_LENGTH:
                        case PIN_MISMATCH:
                            mNewPin1.getText().clear();
                            mNewPin2.getText().clear();
                            mMismatchError.setVisibility(View.VISIBLE);
                            Resources r = getResources();
                            CharSequence text;
                            if (error == PIN_MISMATCH) {
                                text = r.getString(R.string.mismatchPin);
                            } else {
                                text = r.getString(R.string.invalidPin);
                            }
                            mMismatchError.setText(text);
                            break;
                        default:
                            Message callBack = Message.obtain(mHandler,
                                    EVENT_PIN_CHANGED);
                            if (DBG) log("change pin attempt: old=" + oldPin +
                                    ", newPin=" + newPin1);
                            reset();
                            if (mChangePin2) {
                                iccCardInterface.changeIccFdnPassword(oldPin,
                                        newPin1, callBack);
                            } else {
                                iccCardInterface.changeIccLockPassword(oldPin,
                                        newPin1, callBack);
                            }
                    }
                }
            } else if (v == mPUKCode) {
                mPUKSubmit.requestFocus();
            } else if (v == mPUKSubmit) {
                mPhone.getIccCard().supplyPuk2(mPUKCode.getText().toString(), 
                        mNewPin1.getText().toString(), 
                        Message.obtain(mHandler, EVENT_PIN_CHANGED));
            }
        }
    };
    private void handleResult(AsyncResult ar) {
        if (ar.exception == null) {
            if (DBG) log("handleResult: success!");
            if (mState == EntryState.ES_PUK) {
                mScrollView.setVisibility(View.VISIBLE);
                mIccPUKPanel.setVisibility(View.GONE);
            }            
            showConfirmation();
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 3000);
        } else if (ar.exception instanceof CommandException
            ) {
            if (mState == EntryState.ES_PIN) {
                if (DBG) log("handleResult: pin failed!");
                mOldPin.getText().clear();
                mBadPinError.setVisibility(View.VISIBLE);
                CommandException ce = (CommandException) ar.exception;
                if (ce.getCommandError() == CommandException.Error.SIM_PUK2) {
                    if (DBG) log("handleResult: puk requested!");
                    mState = EntryState.ES_PUK;
                    displayPUKAlert();
                    mScrollView.setVisibility(View.GONE);
                    mIccPUKPanel.setVisibility(View.VISIBLE);
                    mPUKCode.requestFocus();
                }
            } else if (mState == EntryState.ES_PUK) {
                if (DBG) log("handleResult: puk2 failed!");
                displayPUKAlert();
                mPUKCode.getText().clear();
                mPUKCode.requestFocus();
            }
        }
    }
    private AlertDialog mPUKAlert;
    private void displayPUKAlert () {
        if (mPUKAlert == null) {
            mPUKAlert = new AlertDialog.Builder(this)
            .setMessage (R.string.puk_requested)
            .setCancelable(false)
            .show();
        } else {
            mPUKAlert.show();
        }
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mPUKAlert.dismiss();
            }
        }, 3000);
    }
    private void showConfirmation() {
        int id = mChangePin2 ? R.string.pin2_changed : R.string.pin_changed;
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }
    private void log(String msg) {
        String prefix = mChangePin2 ? "[ChgPin2]" : "[ChgPin]";
        Log.d(LOG_TAG, prefix + msg);
    }
}
