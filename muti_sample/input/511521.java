public class LandingPage extends ListActivity implements View.OnCreateContextMenuListener {
    private static final String TAG = ImApp.LOG_TAG;
    private static final int ID_SIGN_IN = Menu.FIRST + 1;
    private static final int ID_SIGN_OUT = Menu.FIRST + 2;
    private static final int ID_EDIT_ACCOUNT = Menu.FIRST + 3;
    private static final int ID_REMOVE_ACCOUNT = Menu.FIRST + 4;
    private static final int ID_SIGN_OUT_ALL = Menu.FIRST + 5;
    private static final int ID_ADD_ACCOUNT = Menu.FIRST + 6;
    private static final int ID_VIEW_CONTACT_LIST = Menu.FIRST + 7;
    private static final int ID_SETTINGS = Menu.FIRST + 8;
    private ProviderAdapter mAdapter;
    private Cursor mProviderCursor;
    private ImApp mApp;
    private SimpleAlertHandler mHandler;
    private static final String[] PROVIDER_PROJECTION = {
            Imps.Provider._ID,
            Imps.Provider.NAME,
            Imps.Provider.FULLNAME,
            Imps.Provider.CATEGORY,
            Imps.Provider.ACTIVE_ACCOUNT_ID,
            Imps.Provider.ACTIVE_ACCOUNT_USERNAME,
            Imps.Provider.ACTIVE_ACCOUNT_PW,
            Imps.Provider.ACTIVE_ACCOUNT_LOCKED,
            Imps.Provider.ACTIVE_ACCOUNT_KEEP_SIGNED_IN,
            Imps.Provider.ACCOUNT_PRESENCE_STATUS,
            Imps.Provider.ACCOUNT_CONNECTION_STATUS,
    };
    static final int PROVIDER_ID_COLUMN = 0;
    static final int PROVIDER_NAME_COLUMN = 1;
    static final int PROVIDER_FULLNAME_COLUMN = 2;
    static final int PROVIDER_CATEGORY_COLUMN = 3;
    static final int ACTIVE_ACCOUNT_ID_COLUMN = 4;
    static final int ACTIVE_ACCOUNT_USERNAME_COLUMN = 5;
    static final int ACTIVE_ACCOUNT_PW_COLUMN = 6;
    static final int ACTIVE_ACCOUNT_LOCKED = 7;
    static final int ACTIVE_ACCOUNT_KEEP_SIGNED_IN = 8;
    static final int ACCOUNT_PRESENCE_STATUS = 9;
    static final int ACCOUNT_CONNECTION_STATUS = 10;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTitle(R.string.landing_page_title);
        mApp = ImApp.getApplication(this);
        mHandler = new MyHandler(this);
        ImPluginHelper.getInstance(this).loadAvaiablePlugins();
        mProviderCursor = managedQuery(Imps.Provider.CONTENT_URI_WITH_ACCOUNT,
                PROVIDER_PROJECTION,
                Imps.Provider.CATEGORY + "=?" ,
                new String[]{ ImApp.IMPS_CATEGORY } ,
                Imps.Provider.DEFAULT_SORT_ORDER);
        mAdapter = new ProviderAdapter(this, mProviderCursor);
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
    }
    @Override
    protected void onPause() {
        mHandler.unregisterForBroadcastEvents();
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mHandler.registerForBroadcastEvents();
    }
    private void signIn(long accountId) {
        if (accountId == 0) {
            Log.w(TAG, "signIn: account id is 0, bail");
            return;
        }
        boolean isAccountEditible = mProviderCursor.getInt(ACTIVE_ACCOUNT_LOCKED) == 0;
        if (isAccountEditible && mProviderCursor.isNull(ACTIVE_ACCOUNT_PW_COLUMN)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) log("no pw for account " + accountId);
            Intent intent = getEditAccountIntent();
            startActivity(intent);
            return;
        }
        Intent intent = new Intent(this, SigningInActivity.class);
        intent.setData(ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId));
        startActivity(intent);
    }
    boolean isSigningIn(Cursor cursor) {
        int connectionStatus = cursor.getInt(ACCOUNT_CONNECTION_STATUS);
        return connectionStatus == Imps.ConnectionStatus.CONNECTING;
    }
    private boolean isSignedIn(Cursor cursor) {
        int connectionStatus = cursor.getInt(ACCOUNT_CONNECTION_STATUS);
        return connectionStatus == Imps.ConnectionStatus.ONLINE;
    }
    private boolean allAccountsSignedOut() {
        if(!mProviderCursor.moveToFirst()) {
            return false;
        }
        do {
            if (isSignedIn(mProviderCursor)) {
                return false;
            }
        } while (mProviderCursor.moveToNext()) ;
        return true;
    }
    private void signoutAll() {
        DialogInterface.OnClickListener confirmListener
                = new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton) {
                do {
                    long accountId = mProviderCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN);
                    signOut(accountId);
                } while (mProviderCursor.moveToNext()) ;
            }
        };
        new AlertDialog.Builder(this)
            .setTitle(R.string.confirm)
            .setMessage(R.string.signout_all_confirm_message)
            .setPositiveButton(R.string.yes, confirmListener) 
            .setNegativeButton(R.string.no, null)
            .setCancelable(true)
            .show();
    }
    private void signOut(long accountId) {
        if (accountId == 0) {
            Log.w(TAG, "signOut: account id is 0, bail");
            return;
        }
        try {
            IImConnection conn = mApp.getConnectionByAccount(accountId);
            if (conn != null) {
                conn.logout();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "signOut failed", ex);
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(ID_SIGN_OUT_ALL).setVisible(!allAccountsSignedOut());
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ID_SIGN_OUT_ALL, 0, R.string.menu_sign_out_all)
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_SIGN_OUT_ALL:
                signoutAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
        Cursor providerCursor = (Cursor) getListAdapter().getItem(info.position);
        menu.setHeaderTitle(providerCursor.getString(PROVIDER_FULLNAME_COLUMN));
        if (providerCursor.isNull(ACTIVE_ACCOUNT_ID_COLUMN)) {
            menu.add(0, ID_ADD_ACCOUNT, 0, R.string.menu_add_account);
            return;
        }
        long providerId = providerCursor.getLong(PROVIDER_ID_COLUMN);
        boolean isLoggingIn = isSigningIn(providerCursor);
        boolean isLoggedIn = isSignedIn(providerCursor);
        BrandingResources brandingRes = mApp.getBrandingResource(providerId);
        if (!isLoggedIn) {
            menu.add(0, ID_SIGN_IN, 0, R.string.sign_in).setIcon(com.android.internal.R.drawable.ic_menu_login);
        } else {
            menu.add(0, ID_VIEW_CONTACT_LIST, 0,
                    brandingRes.getString(BrandingResourceIDs.STRING_MENU_CONTACT_LIST));
            menu.add(0, ID_SIGN_OUT, 0, R.string.menu_sign_out)
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        }
        boolean isAccountEditible = providerCursor.getInt(ACTIVE_ACCOUNT_LOCKED) == 0;
        if (isAccountEditible && !isLoggingIn && !isLoggedIn) {
            menu.add(0, ID_EDIT_ACCOUNT, 0, R.string.menu_edit_account)
                .setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, ID_REMOVE_ACCOUNT, 0, R.string.menu_remove_account)
                .setIcon(android.R.drawable.ic_menu_delete);
        }
        menu.add(0, ID_SETTINGS, 0, R.string.menu_settings);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
        long providerId = info.id;
        Cursor providerCursor = (Cursor) getListAdapter().getItem(info.position);
        long accountId = providerCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN);
        switch (item.getItemId()) {
            case ID_EDIT_ACCOUNT:
            {
                startActivity(getEditAccountIntent());
                return true;
            }
            case ID_REMOVE_ACCOUNT:
            {
                Uri accountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId);
                getContentResolver().delete(accountUri, null, null);
                providerCursor.requery();
                return true;
            }
            case ID_VIEW_CONTACT_LIST:
            {
                Intent intent = getViewContactsIntent();
                startActivity(intent);
                return true;
            }
            case ID_ADD_ACCOUNT:
            {
                startActivity(getCreateAccountIntent());
                return true;
            }
            case ID_SIGN_IN:
            {
                signIn(accountId);
                return true;
            }
            case ID_SIGN_OUT:
            {
                signOut(accountId);
                return true;
            }
            case ID_SETTINGS:
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Imps.ProviderSettings.CONTENT_URI);
                intent.addCategory(getProviderCategory(providerCursor));
                intent.putExtra("providerId", providerId);
                startActivity(intent);
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = null;
        mProviderCursor.moveToPosition(position);
        if (mProviderCursor.isNull(ACTIVE_ACCOUNT_ID_COLUMN)) {
            intent = getCreateAccountIntent();
        } else {
            int state = mProviderCursor.getInt(ACCOUNT_CONNECTION_STATUS);
            long accountId = mProviderCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN);
            if (state == Imps.ConnectionStatus.OFFLINE) {
                boolean isKeepSignedIn = mProviderCursor.getInt(ACTIVE_ACCOUNT_KEEP_SIGNED_IN) != 0;
                boolean isAccountEditible = mProviderCursor.getInt(ACTIVE_ACCOUNT_LOCKED) == 0;
                if (isKeepSignedIn) {
                    signIn(accountId);
                } else if(isAccountEditible) {
                    intent = getEditAccountIntent();
                }
            } else if (state == Imps.ConnectionStatus.CONNECTING) {
                signIn(accountId);
            } else {
                intent = getViewContactsIntent();
            }
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    Intent getCreateAccountIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_INSERT);
        long providerId = mProviderCursor.getLong(PROVIDER_ID_COLUMN);
        intent.setData(ContentUris.withAppendedId(Imps.Provider.CONTENT_URI, providerId));
        intent.addCategory(getProviderCategory(mProviderCursor));
        return intent;
    }
    Intent getEditAccountIntent() {
        Intent intent = new Intent(Intent.ACTION_EDIT,
                ContentUris.withAppendedId(Imps.Account.CONTENT_URI,
                        mProviderCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN)));
        intent.addCategory(getProviderCategory(mProviderCursor));
        return intent;
    }
    Intent getViewContactsIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Imps.Contacts.CONTENT_URI);
        intent.addCategory(getProviderCategory(mProviderCursor));
        intent.putExtra("accountId", mProviderCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN));
        return intent;
    }
    private String getProviderCategory(Cursor cursor) {
        return cursor.getString(PROVIDER_CATEGORY_COLUMN);
    }
    static void log(String msg) {
        Log.d(TAG, "[LandingPage]" + msg);
    }
    private class ProviderListItemFactory implements LayoutInflater.Factory {
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            if (name != null && name.equals(ProviderListItem.class.getName())) {
                return new ProviderListItem(context, LandingPage.this);
            }
            return null;
        }
    }
    private final class ProviderAdapter extends CursorAdapter {
        private LayoutInflater mInflater;
        public ProviderAdapter(Context context, Cursor c) {
            super(context, c);
            mInflater = LayoutInflater.from(context).cloneInContext(context);
            mInflater.setFactory(new ProviderListItemFactory());
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ProviderListItem view = (ProviderListItem) mInflater.inflate(
                    R.layout.account_view, parent, false);
            view.init(cursor);
            return view;
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((ProviderListItem) view).bindView(cursor);
        }
    }
    private final static class MyHandler extends SimpleAlertHandler {
        public MyHandler(Activity activity) {
            super(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ImApp.EVENT_CONNECTION_DISCONNECTED) {
                promptDisconnectedEvent(msg);
            }
            super.handleMessage(msg);
        }
    }
}
