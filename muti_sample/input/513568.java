public class AccountActivity extends Activity {
    private static final String ACCOUNT_URI_KEY = "accountUri";
    static final int REQUEST_SIGN_IN = RESULT_FIRST_USER + 1;
    private static final String[] ACCOUNT_PROJECTION = {
        Imps.Account._ID,
        Imps.Account.PROVIDER,
        Imps.Account.USERNAME,
        Imps.Account.PASSWORD,
        Imps.Account.KEEP_SIGNED_IN,
    };
    private static final int ACCOUNT_PROVIDER_COLUMN = 1;
    private static final int ACCOUNT_USERNAME_COLUMN = 2;
    private static final int ACCOUNT_PASSWORD_COLUMN = 3;
    private static final int ACCOUNT_KEEP_SIGNED_IN_COLUMN = 4;
    Uri mAccountUri;
    EditText mEditName;
    EditText mEditPass;
    CheckBox mRememberPass;
    CheckBox mKeepSignIn;
    Button   mBtnSignIn;
    String mToAddress;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.account_activity);
        mEditName = (EditText)findViewById(R.id.edtName);
        mEditPass = (EditText)findViewById(R.id.edtPass);
        mRememberPass = (CheckBox)findViewById(R.id.rememberPassword);
        mKeepSignIn = (CheckBox)findViewById(R.id.keepSignIn);
        mBtnSignIn = (Button)findViewById(R.id.btnSignIn);
        mRememberPass.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                updateWidgetState();
            }
        });
        ImApp app = ImApp.getApplication(this);
        Intent i = getIntent();
        String action = i.getAction();
        mToAddress = i.getStringExtra(ImApp.EXTRA_INTENT_SEND_TO_USER);
        final String origUserName;
        final long providerId;
        final ProviderDef provider;
        if(Intent.ACTION_INSERT.equals(action)) {
            origUserName = "";
            providerId = ContentUris.parseId(i.getData());
            provider = app.getProvider(providerId);
            setTitle(getResources().getString(R.string.add_account, provider.mFullName));
        } else if(Intent.ACTION_EDIT.equals(action)) {
            ContentResolver cr = getContentResolver();
            Uri uri = i.getData();
            if ((uri == null) || !Imps.Account.CONTENT_ITEM_TYPE.equals(cr.getType(uri))) {
                Log.w(ImApp.LOG_TAG, "<AccountActivity>Bad data");
                return;
            }
            Cursor cursor = cr.query(uri, ACCOUNT_PROJECTION, null, null, null);
            if (cursor == null) {
                finish();
                return;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                finish();
                return;
            }
            setTitle(R.string.sign_in);
            providerId = cursor.getLong(ACCOUNT_PROVIDER_COLUMN);
            provider = app.getProvider(providerId);
            origUserName = cursor.getString(ACCOUNT_USERNAME_COLUMN);
            mEditName.setText(origUserName);
            mEditPass.setText(cursor.getString(ACCOUNT_PASSWORD_COLUMN));
            mRememberPass.setChecked(!cursor.isNull(ACCOUNT_PASSWORD_COLUMN));
            boolean keepSignIn = cursor.getInt(ACCOUNT_KEEP_SIGNED_IN_COLUMN) == 1;
            mKeepSignIn.setChecked(keepSignIn);
            cursor.close();
        } else {
            Log.w(ImApp.LOG_TAG, "<AccountActivity> unknown intent action " + action);
            finish();
            return;
        }
        final BrandingResources brandingRes = app.getBrandingResource(providerId);
        mKeepSignIn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CheckBox keepSignIn = (CheckBox) v;
                if ( keepSignIn.isChecked() ) {
                    String msg = brandingRes.getString(BrandingResourceIDs.STRING_TOAST_CHECK_AUTO_SIGN_IN);
                    Toast.makeText(AccountActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        mRememberPass.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CheckBox keepSignIn = (CheckBox) v;
                if ( keepSignIn.isChecked() ) {
                    String msg = brandingRes.getString(BrandingResourceIDs.STRING_TOAST_CHECK_SAVE_PASSWORD);
                    Toast.makeText(AccountActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        Drawable logo = brandingRes.getDrawable(BrandingResourceIDs.DRAWABLE_LOGO);
        getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON, logo);
        TextView labelUsername = (TextView)findViewById(R.id.label_username);
        labelUsername.setText(brandingRes.getString(BrandingResourceIDs.STRING_LABEL_USERNAME));
        mEditName.addTextChangedListener(mTextWatcher);
        mEditPass.addTextChangedListener(mTextWatcher);
        mBtnSignIn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String username = mEditName.getText().toString();
                final String pass = mEditPass.getText().toString();
                final boolean rememberPass = mRememberPass.isChecked();
                ContentResolver cr = getContentResolver();
                long accountId = ImApp.insertOrUpdateAccount(cr, providerId, username,
                        rememberPass ? pass : null);
                mAccountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId);
                if (!origUserName.equals(username) && shouldShowTermOfUse(brandingRes)) {
                    comfirmTermsOfUse(brandingRes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            signIn(rememberPass, pass);
                        }
                    });
                } else {
                    signIn(rememberPass, pass);
                }
            }
            void signIn(boolean rememberPass, String pass) {
                Intent intent = new Intent(AccountActivity.this, SigningInActivity.class);
                intent.setData(mAccountUri);
                if (!rememberPass) {
                    intent.putExtra(ImApp.EXTRA_INTENT_PASSWORD, pass);
                }
                if (mToAddress != null) {
                    intent.putExtra(ImApp.EXTRA_INTENT_SEND_TO_USER, mToAddress);
                }
                startActivityForResult(intent, REQUEST_SIGN_IN);
            }
        });
        String text = brandingRes.getString(BrandingResourceIDs.STRING_LABEL_SIGN_UP);
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        builder.setSpan(new URLSpan(provider.mSignUpUrl), 0, builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView signUp = (TextView)findViewById(R.id.signUp);
        signUp.setText(builder);
        signUp.setMovementMethod(LinkMovementMethod.getInstance());
        updateWidgetState();
    }
    void comfirmTermsOfUse(BrandingResources res, DialogInterface.OnClickListener accept) {
        SpannableString message = new SpannableString(
                res.getString(BrandingResourceIDs.STRING_TOU_MESSAGE));
        Linkify.addLinks(message, Linkify.ALL);
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(res.getString(BrandingResourceIDs.STRING_TOU_TITLE))
            .setMessage(message)
            .setPositiveButton(res.getString(BrandingResourceIDs.STRING_TOU_DECLINE), null)
            .setNegativeButton(res.getString(BrandingResourceIDs.STRING_TOU_ACCEPT), accept)
            .show();
    }
    boolean shouldShowTermOfUse(BrandingResources res) {
        return !TextUtils.isEmpty(res.getString(BrandingResourceIDs.STRING_TOU_MESSAGE));
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAccountUri = savedInstanceState.getParcelable(ACCOUNT_URI_KEY);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ACCOUNT_URI_KEY, mAccountUri);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                boolean keepSignIn = mKeepSignIn.isChecked();
                updateKeepSignedIn(keepSignIn);
                finish();
            } else {
                mKeepSignIn.setChecked(false);
                updateKeepSignedIn(false);
                mEditPass.setText("");
                ContentValues values = new ContentValues();
                values.put(Imps.Account.PASSWORD, (String) null);
                getContentResolver().update(mAccountUri, values, null, null);
            }
        }
    }
    void updateKeepSignedIn(boolean keepSignIn) {
        ContentValues values = new ContentValues();
        values.put(Imps.Account.KEEP_SIGNED_IN, keepSignIn ? 1 : 0);
        getContentResolver().update(mAccountUri, values, null, null);
    }
    void updateWidgetState() {
        boolean goodUsername = mEditName.getText().length() > 0;
        boolean goodPassword = mEditPass.getText().length() > 0;
        boolean hasNameAndPassword = goodUsername && goodPassword;
        mEditPass.setEnabled(goodUsername);
        mEditPass.setFocusable(goodUsername);
        mEditPass.setFocusableInTouchMode(goodUsername);
        boolean rememberPass = mRememberPass.isChecked();
        if (rememberPass && !hasNameAndPassword) {
            mRememberPass.setChecked(false);
            rememberPass = false;
        }
        mRememberPass.setEnabled(hasNameAndPassword);
        mRememberPass.setFocusable(hasNameAndPassword);
        if (!rememberPass) {
            mKeepSignIn.setChecked(false);
        }
        mKeepSignIn.setEnabled(rememberPass);
        mKeepSignIn.setFocusable(rememberPass);
        mBtnSignIn.setEnabled(hasNameAndPassword);
        mBtnSignIn.setFocusable(hasNameAndPassword);
    }
    private final TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int after) {
            updateWidgetState();
        }
        public void afterTextChanged(Editable s) {
        }
    };
}
