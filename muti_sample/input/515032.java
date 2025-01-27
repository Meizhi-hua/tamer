public class AddContactActivity extends Activity {
    private static final String[] CONTACT_LIST_PROJECTION = {
        Imps.ContactList._ID,
        Imps.ContactList.NAME,
    };
    private static final int CONTACT_LIST_NAME_COLUMN = 1;
    private MultiAutoCompleteTextView mAddressList;
    private Spinner mListSpinner;
    Button mInviteButton;
    ImApp mApp;
    SimpleAlertHandler mHandler;
    private long mProviderId;
    private long mAccountId;
    private String mDefaultDomain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = ImApp.getApplication(this);
        mHandler = new SimpleAlertHandler(this);
        resolveIntent(getIntent());
        setContentView(R.layout.add_contact_activity);
        BrandingResources brandingRes = mApp.getBrandingResource(mProviderId);
        setTitle(brandingRes.getString(BrandingResourceIDs.STRING_ADD_CONTACT_TITLE));
        TextView label = (TextView) findViewById(R.id.input_contact_label);
        label.setText(brandingRes.getString(BrandingResourceIDs.STRING_LABEL_INPUT_CONTACT));
        mAddressList = (MultiAutoCompleteTextView) findViewById(R.id.email);
        mAddressList.setAdapter(new EmailAddressAdapter(this));
        mAddressList.setTokenizer(new Rfc822Tokenizer());
        mAddressList.addTextChangedListener(mTextWatcher);
        mListSpinner = (Spinner) findViewById(R.id.choose_list);
        Cursor c = queryContactLists();
        int initSelection = searchInitListPos(c, getIntent().getStringExtra(
                ImServiceConstants.EXTRA_INTENT_LIST_NAME));
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                c,
                new String[] {Imps.ContactList.NAME},
                new int[] {android.R.id.text1});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mListSpinner.setAdapter(adapter);
        mListSpinner.setSelection(initSelection);
        mInviteButton = (Button) findViewById(R.id.invite);
        mInviteButton.setText(brandingRes.getString(
                BrandingResourceIDs.STRING_BUTTON_ADD_CONTACT));
        mInviteButton.setOnClickListener(mButtonHandler);
        mInviteButton.setEnabled(false);
    }
    private Cursor queryContactLists() {
        Uri uri = Imps.ContactList.CONTENT_URI;
        uri = ContentUris.withAppendedId(uri, mProviderId);
        uri = ContentUris.withAppendedId(uri, mAccountId);
        Cursor c = managedQuery(uri, CONTACT_LIST_PROJECTION, null, null);
        return c;
    }
    private int searchInitListPos(Cursor c, String listName) {
        if (TextUtils.isEmpty(listName)) {
            return 0;
        }
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            if (listName.equals(c.getString(CONTACT_LIST_NAME_COLUMN))) {
                return c.getPosition();
            }
        }
        return 0;
    }
    private void resolveIntent(Intent intent) {
        mProviderId = intent.getLongExtra(
                ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, -1);
        mAccountId = intent.getLongExtra(
                ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, -1);
        mDefaultDomain = Imps.ProviderSettings.getStringValue(getContentResolver(),
                mProviderId, ImpsConfigNames.DEFAULT_DOMAIN);
    }
    void inviteBuddies() {
        Rfc822Token[] recipients = Rfc822Tokenizer.tokenize(mAddressList.getText());
        try {
            IImConnection conn = mApp.getConnection(mProviderId);
            IContactList list = getContactList(conn);
            if (list == null) {
                Log.e(ImApp.LOG_TAG, "<AddContactActivity> can't find given contact list:"
                        + getSelectedListName());
                finish();
            } else {
                boolean fail = false;
                for (Rfc822Token recipient : recipients) {
                    String username = recipient.getAddress();
                    if (mDefaultDomain != null && username.indexOf('@') == -1) {
                        username = username + "@" + mDefaultDomain;
                    }
                    if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)){
                        log("addContact:" + username);
                    }
                    int res = list.addContact(username);
                    if (res != ImErrorInfo.NO_ERROR) {
                        fail = true;
                        mHandler.showAlert(R.string.error,
                                ErrorResUtils.getErrorRes(getResources(), res, username));
                    }
                }
                if (!fail) {
                    finish();
                }
            }
        } catch (RemoteException ex) {
            Log.e(ImApp.LOG_TAG, "<AddContactActivity> inviteBuddies: caught " + ex);
        }
    }
    private IContactList getContactList(IImConnection conn) {
        if (conn == null) {
            return null;
        }
        try {
            IContactListManager contactListMgr = conn.getContactListManager();
            String listName = getSelectedListName();
            if (!TextUtils.isEmpty(listName)) {
                return contactListMgr.getContactList(listName);
            } else {
                List<IBinder> lists = contactListMgr.getContactLists();
                for (IBinder binder : lists) {
                    IContactList list = IContactList.Stub.asInterface(binder);
                    if (list.isDefault()) {
                        return list;
                    }
                }
                if (!lists.isEmpty()) {
                    return IContactList.Stub.asInterface(lists.get(0));
                }
                return null;
            }
        } catch (RemoteException e) {
            return null;
        }
    }
    private String getSelectedListName() {
        Cursor c = (Cursor) mListSpinner.getSelectedItem();
        return (c == null) ? null : c.getString(CONTACT_LIST_NAME_COLUMN);
    }
    private View.OnClickListener mButtonHandler = new View.OnClickListener() {
        public void onClick(View v) {
            mApp.callWhenServiceConnected(mHandler, new Runnable() {
                public void run() {
                    inviteBuddies();
                }
            });
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            mInviteButton.setEnabled(s.length() != 0);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };
    private static void log(String msg) {
        Log.d(ImApp.LOG_TAG, "<AddContactActivity> " + msg);
    }
    static class EmailAddressAdapter extends ResourceCursorAdapter {
        public static final int DATA_INDEX = 1;
        private static final String SORT_ORDER = "people.name, contact_methods.data";
        private ContentResolver mContentResolver;
        private static final String[] PROJECTION = {
            ContactMethods._ID,     
            ContactMethods.DATA     
        };
        public EmailAddressAdapter(Context context) {
            super(context, android.R.layout.simple_dropdown_item_1line, null);
            mContentResolver = context.getContentResolver();
        }
        @Override
        public final String convertToString(Cursor cursor) {
            return cursor.getString(DATA_INDEX);
        }
        @Override
        public final void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(cursor.getString(DATA_INDEX));
        }
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            String where = null;
            if (constraint != null) {
                String filter = DatabaseUtils.sqlEscapeString(constraint.toString() + '%');
                StringBuilder s = new StringBuilder();
                s.append("(people.name LIKE ");
                s.append(filter);
                s.append(") OR (contact_methods.data LIKE ");
                s.append(filter);
                s.append(")");
                where = s.toString();
            }
            return mContentResolver.query(CONTENT_EMAIL_URI, PROJECTION, where, null, SORT_ORDER);
        }
    }
}
