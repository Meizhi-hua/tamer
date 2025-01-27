public class EditFdnContactScreen extends Activity {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private static final boolean DBG = false;
    private static final int MENU_IMPORT = 1;
    private static final int MENU_DELETE = 2;
    private static final String INTENT_EXTRA_NAME = "name";
    private static final String INTENT_EXTRA_NUMBER = "number";
    private static final int PIN2_REQUEST_CODE = 100;
    private String mName;
    private String mNumber;
    private String mPin2;
    private boolean mAddContact;
    private QueryHandler mQueryHandler;
    private EditText mNameField;
    private EditText mNumberField;
    private LinearLayout mPinFieldContainer;
    private Button mButton;
    private Handler mHandler = new Handler();
    private static final int CONTACTS_PICKER_CODE = 200;
    private static final String NUM_PROJECTION[] = {PeopleColumns.DISPLAY_NAME,
        PhonesColumns.NUMBER};
    private static final Intent CONTACT_IMPORT_INTENT;
    static {
        CONTACT_IMPORT_INTENT = new Intent(Intent.ACTION_GET_CONTENT);
        CONTACT_IMPORT_INTENT.setType(android.provider.Contacts.Phones.CONTENT_ITEM_TYPE);
    }
    private boolean mDataBusy;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        resolveIntent();
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.edit_fdn_contact_screen);
        setupView();
        setTitle(mAddContact ?
                R.string.add_fdn_contact : R.string.edit_fdn_contact);
        mDataBusy = false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (DBG) log("onActivityResult request:" + requestCode + " result:" + resultCode);
        switch (requestCode) {
            case PIN2_REQUEST_CODE:
                Bundle extras = (intent != null) ? intent.getExtras() : null;
                if (extras != null) {
                    mPin2 = extras.getString("pin2");
                    if (mAddContact) {
                        addContact();
                    } else {
                        updateContact();
                    }
                } else if (resultCode != RESULT_OK) {
                    if (DBG) log("onActivityResult: cancelled.");
                    finish();
                }
                break;
            case CONTACTS_PICKER_CODE:
                if (resultCode != RESULT_OK) {
                    if (DBG) log("onActivityResult: cancelled.");
                    return;
                }
                Cursor cursor = getContentResolver().query(intent.getData(),
                        NUM_PROJECTION, null, null, null);
                if ((cursor == null) || (!cursor.moveToFirst())) {
                    Log.w(LOG_TAG,"onActivityResult: bad contact data, no results found.");
                    return;
                }
                mNameField.setText(cursor.getString(0));
                mNumberField.setText(cursor.getString(1));
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Resources r = getResources();
        menu.add(0, MENU_IMPORT, 0, r.getString(R.string.importToFDNfromContacts))
                .setIcon(R.drawable.ic_menu_contact);
        menu.add(0, MENU_DELETE, 0, r.getString(R.string.menu_delete))
                .setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        return mDataBusy ? false : result;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_IMPORT:
                startActivityForResult(CONTACT_IMPORT_INTENT, CONTACTS_PICKER_CODE);
                return true;
            case MENU_DELETE:
                deleteSelected();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void resolveIntent() {
        Intent intent = getIntent();
        mName =  intent.getStringExtra(INTENT_EXTRA_NAME);
        mNumber =  intent.getStringExtra(INTENT_EXTRA_NUMBER);
        if (TextUtils.isEmpty(mName) && TextUtils.isEmpty(mNumber)) {
            mAddContact = true;
        }
    }
    private void setupView() {
        mNameField = (EditText) findViewById(R.id.fdn_name);
        if (mNameField != null) {
            mNameField.setOnFocusChangeListener(mOnFocusChangeHandler);
            mNameField.setOnClickListener(mClicked);
        }
        mNumberField = (EditText) findViewById(R.id.fdn_number);
        if (mNumberField != null) {
            mNumberField.setKeyListener(DialerKeyListener.getInstance());
            mNumberField.setOnFocusChangeListener(mOnFocusChangeHandler);
            mNumberField.setOnClickListener(mClicked);
        }
        if (!mAddContact) {
            if (mNameField != null) {
                mNameField.setText(mName);
            }
            if (mNumberField != null) {
                mNumberField.setText(mNumber);
            }
        }
        mButton = (Button) findViewById(R.id.button);
        if (mButton != null) {
            mButton.setOnClickListener(mClicked);
        }
        mPinFieldContainer = (LinearLayout) findViewById(R.id.pinc);
    }
    private String getNameFromTextField() {
        return mNameField.getText().toString();
    }
    private String getNumberFromTextField() {
        return mNumberField.getText().toString();
    }
    private Uri getContentURI() {
        return Uri.parse("content:
    }
     private boolean isValidNumber(String number) {
         return (number.length() <= 20);
     }
    private void addContact() {
        if (DBG) log("addContact");
        if (!isValidNumber(getNumberFromTextField())) {
            handleResult(false, true);
            return;
        }
        Uri uri = getContentURI();
        ContentValues bundle = new ContentValues(3);
        bundle.put("tag", getNameFromTextField());
        bundle.put("number", getNumberFromTextField());
        bundle.put("pin2", mPin2);
        mQueryHandler = new QueryHandler(getContentResolver());
        mQueryHandler.startInsert(0, null, uri, bundle);
        displayProgress(true);
        showStatus(getResources().getText(R.string.adding_fdn_contact));
    }
    private void updateContact() {
        if (DBG) log("updateContact");
        if (!isValidNumber(getNumberFromTextField())) {
            handleResult(false, true);
            return;
        }
        Uri uri = getContentURI();
        ContentValues bundle = new ContentValues();
        bundle.put("tag", mName);
        bundle.put("number", mNumber);
        bundle.put("newTag", getNameFromTextField());
        bundle.put("newNumber", getNumberFromTextField());
        bundle.put("pin2", mPin2);
        mQueryHandler = new QueryHandler(getContentResolver());
        mQueryHandler.startUpdate(0, null, uri, bundle, null, null);
        displayProgress(true);
        showStatus(getResources().getText(R.string.updating_fdn_contact));
    }
    private void deleteSelected() {
        if (!mAddContact) {
            Intent intent = new Intent();
            intent.setClass(this, DeleteFdnContactScreen.class);
            intent.putExtra(INTENT_EXTRA_NAME, mName);
            intent.putExtra(INTENT_EXTRA_NUMBER, mNumber);
            startActivity(intent);
        }
        finish();
    }
    private void authenticatePin2() {
        Intent intent = new Intent();
        intent.setClass(this, GetPin2Screen.class);
        startActivityForResult(intent, PIN2_REQUEST_CODE);
    }
    private void displayProgress(boolean flag) {
        mDataBusy = flag;
        getWindow().setFeatureInt(
                Window.FEATURE_INDETERMINATE_PROGRESS,
                mDataBusy ? PROGRESS_VISIBILITY_ON : PROGRESS_VISIBILITY_OFF);
        mButton.setClickable(!mDataBusy);
    }
    private void showStatus(CharSequence statusMsg) {
        if (statusMsg != null) {
            Toast.makeText(this, statusMsg, Toast.LENGTH_SHORT)
            .show();
        }
    }
    private void handleResult(boolean success, boolean invalidNumber) {
        if (success) {
            if (DBG) log("handleResult: success!");
            showStatus(getResources().getText(mAddContact ?
                    R.string.fdn_contact_added : R.string.fdn_contact_updated));
        } else {
            if (DBG) log("handleResult: failed!");
            if (invalidNumber)
                showStatus(getResources().getText(R.string.fdn_invalid_number));
            else
                showStatus(getResources().getText(R.string.pin2_invalid));
        }
        mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 2000);
    }
    private View.OnClickListener mClicked = new View.OnClickListener() {
        public void onClick(View v) {
            if (mPinFieldContainer.getVisibility() != View.VISIBLE) {
                return;
            }
            if (v == mNameField) {
                mNumberField.requestFocus();
            } else if (v == mNumberField) {
                mButton.requestFocus();
            } else if (v == mButton) {
                if (!mDataBusy) {
                    authenticatePin2();
                }
            }
        }
    };
    View.OnFocusChangeListener mOnFocusChangeHandler =
            new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                TextView textView = (TextView) v;
                Selection.selectAll((Spannable) textView.getText());
            }
        }
    };
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
        }
        @Override
        protected void onInsertComplete(int token, Object cookie,
                                        Uri uri) {
            if (DBG) log("onInsertComplete");
            displayProgress(false);
            handleResult(uri != null, false);
        }
        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            if (DBG) log("onUpdateComplete");
            displayProgress(false);
            handleResult(result > 0, false);
        }
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
        }
    }
    private void log(String msg) {
        Log.d(LOG_TAG, "[EditFdnContact] " + msg);
    }
}
