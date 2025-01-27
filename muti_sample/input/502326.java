public class ApnPreference extends Preference implements
        CompoundButton.OnCheckedChangeListener, OnClickListener {
    final static String TAG = "ApnPreference";
    public ApnPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public ApnPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ApnPreference(Context context) {
        super(context);
        init();
    }
    private static String mSelectedKey = null;
    private static CompoundButton mCurrentChecked = null;
    private boolean mProtectFromCheckedChange = false;
    private boolean mSelectable = true;
    @Override
    public View getView(View convertView, ViewGroup parent) {
        View view = super.getView(convertView, parent);
        View widget = view.findViewById(R.id.apn_radiobutton);
        if ((widget != null) && widget instanceof RadioButton) {
            RadioButton rb = (RadioButton) widget;
            if (mSelectable) {
                rb.setOnCheckedChangeListener(this);
                boolean isChecked = getKey().equals(mSelectedKey);
                if (isChecked) {
                    mCurrentChecked = rb;
                    mSelectedKey = getKey();
                }
                mProtectFromCheckedChange = true;
                rb.setChecked(isChecked);
                mProtectFromCheckedChange = false;
            } else {
                rb.setVisibility(View.GONE);
            }
        }
        View textLayout = view.findViewById(R.id.text_layout);
        if ((textLayout != null) && textLayout instanceof RelativeLayout) {
            textLayout.setOnClickListener(this);
        }
        return view;
    }
    private void init() {
        setLayoutResource(R.layout.apn_preference_layout);
    }
    public boolean isChecked() {
        return getKey().equals(mSelectedKey);
    }
    public void setChecked() {
        mSelectedKey = getKey();
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "ID: " + getKey() + " :" + isChecked);
        if (mProtectFromCheckedChange) {
            return;
        }
        if (isChecked) {
            if (mCurrentChecked != null) {
                mCurrentChecked.setChecked(false);
            }
            mCurrentChecked = buttonView;
            mSelectedKey = getKey();
            callChangeListener(mSelectedKey);
        } else {
            mCurrentChecked = null;
            mSelectedKey = null;
        }
    }
    public void onClick(android.view.View v) {
        if ((v != null) && (R.id.text_layout == v.getId())) {
            Context context = getContext();
            if (context != null) {
                int pos = Integer.parseInt(getKey());
                Uri url = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, pos);
                context.startActivity(new Intent(Intent.ACTION_EDIT, url));
            }
        }
    }
    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }
    public boolean getSelectable() {
        return mSelectable;
    }
}
