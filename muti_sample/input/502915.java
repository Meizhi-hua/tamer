public class GrantCredentialsPermissionActivity extends Activity implements View.OnClickListener {
    public static final String EXTRAS_ACCOUNT = "account";
    public static final String EXTRAS_AUTH_TOKEN_LABEL = "authTokenLabel";
    public static final String EXTRAS_AUTH_TOKEN_TYPE = "authTokenType";
    public static final String EXTRAS_RESPONSE = "response";
    public static final String EXTRAS_ACCOUNT_TYPE_LABEL = "accountTypeLabel";
    public static final String EXTRAS_PACKAGES = "application";
    public static final String EXTRAS_REQUESTING_UID = "uid";
    private Account mAccount;
    private String mAuthTokenType;
    private int mUid;
    private Bundle mResultBundle = null;
    protected LayoutInflater mInflater;
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grant_credentials_permission);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Bundle extras = getIntent().getExtras();
        mAccount = extras.getParcelable(EXTRAS_ACCOUNT);
        mAuthTokenType = extras.getString(EXTRAS_AUTH_TOKEN_TYPE);
        if (mAccount == null || mAuthTokenType == null) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        mUid = extras.getInt(EXTRAS_REQUESTING_UID);
        final String accountTypeLabel = extras.getString(EXTRAS_ACCOUNT_TYPE_LABEL);
        final String[] packages = extras.getStringArray(EXTRAS_PACKAGES);
        final String authTokenLabel = extras.getString(EXTRAS_AUTH_TOKEN_LABEL);
        findViewById(R.id.allow_button).setOnClickListener(this);
        findViewById(R.id.deny_button).setOnClickListener(this);
        LinearLayout packagesListView = (LinearLayout) findViewById(R.id.packages_list);
        final PackageManager pm = getPackageManager();
        for (String pkg : packages) {
            String packageLabel;
            try {
                packageLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                packageLabel = pkg;
            }
            packagesListView.addView(newPackageView(packageLabel));
        }
        ((TextView) findViewById(R.id.account_name)).setText(mAccount.name);
        ((TextView) findViewById(R.id.account_type)).setText(accountTypeLabel);
        TextView authTokenTypeView = (TextView) findViewById(R.id.authtoken_type);
        if (TextUtils.isEmpty(authTokenLabel)) {
            authTokenTypeView.setVisibility(View.GONE);
        } else {
            authTokenTypeView.setText(authTokenLabel);
        }
    }
    private View newPackageView(String packageLabel) {
        View view = mInflater.inflate(R.layout.permissions_package_list_item, null);
        ((TextView) view.findViewById(R.id.package_label)).setText(packageLabel);
        return view;
    }
    public void onClick(View v) {
        final AccountManagerService accountManagerService = AccountManagerService.getSingleton();
        switch (v.getId()) {
            case R.id.allow_button:
                accountManagerService.grantAppPermission(mAccount, mAuthTokenType, mUid);
                Intent result = new Intent();
                result.putExtra("retry", true);
                setResult(RESULT_OK, result);
                setAccountAuthenticatorResult(result.getExtras());
                break;
            case R.id.deny_button:
                accountManagerService.revokeAppPermission(mAccount, mAuthTokenType, mUid);
                setResult(RESULT_CANCELED);
                break;
        }
        finish();
    }
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }
    public void finish() {
        Intent intent = getIntent();
        AccountAuthenticatorResponse response = intent.getParcelableExtra(EXTRAS_RESPONSE);
        if (response != null) {
            if (mResultBundle != null) {
                response.onResult(mResultBundle);
            } else {
                response.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
        }
        super.finish();
    }
}
