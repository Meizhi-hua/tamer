public class BigEditTextActivityNonScrollableResize extends Activity {
    private View mRootView;
    private View mDefaultFocusedView;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mRootView = new LinearLayout(this);
        ((LinearLayout) mRootView).setOrientation(LinearLayout.VERTICAL);
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        View view = getLayoutInflater().inflate(
                R.layout.full_screen_edit_text, ((LinearLayout) mRootView), false);
        ((LinearLayout) mRootView).addView(view);
        mDefaultFocusedView = view.findViewById(R.id.data);
        setContentView(mRootView);
    }
    public View getRootView() {
        return mRootView;
    }
    public View getDefaultFocusedView() {
        return mDefaultFocusedView;
    }
}
