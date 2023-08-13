public class BasicIndicator extends AbstractIndicator {
    private final ResourceTexture mIcon[];
    private final IconListPreference mPreference;
    protected int mIndex;
    private GLListView mPopupContent;
    private PreferenceAdapter mModel;
    private String mOverride;
    public BasicIndicator(Context context,
            PreferenceGroup group, IconListPreference preference) {
        super(context);
        mPreference = preference;
        mIcon = new ResourceTexture[preference.getLargeIconIds().length];
        mIndex = preference.findIndexOfValue(preference.getValue());
    }
    private void updateContent(String override, boolean reloadValue) {
        if (!reloadValue && Util.equals(mOverride, override)) return;
        IconListPreference pref = mPreference;
        mOverride = override;
        int index = pref.findIndexOfValue(
                override == null ? pref.getValue() : override);
        if (mIndex != index) {
            mIndex = index;
            invalidate();
        }
    }
    @Override
    public void overrideSettings(String key, String settings) {
        IconListPreference pref = mPreference;
        if (!pref.getKey().equals(key)) return;
        updateContent(settings, false);
    }
    @Override
    public void reloadPreferences() {
        if (mModel != null) mModel.reload();
        updateContent(null, true);
    }
    @Override
    public GLView getPopupContent() {
        if (mPopupContent == null) {
            Context context = getGLRootView().getContext();
            mPopupContent = new GLListView(context);
            mPopupContent.setHighLight(new NinePatchTexture(
                    context, R.drawable.optionitem_highlight));
            mPopupContent.setScroller(new NinePatchTexture(
                    context, R.drawable.scrollbar_handle_vertical));
            mModel = new PreferenceAdapter(context, mPreference);
            mPopupContent.setOnItemSelectedListener(new MyListener(mModel));
            mPopupContent.setDataModel(mModel);
        }
        mModel.overrideSettings(mOverride);
        return mPopupContent;
    }
    protected void onPreferenceChanged(int newIndex) {
        if (newIndex == mIndex) return;
        mIndex = newIndex;
        invalidate();
    }
    private class MyListener implements OnItemSelectedListener {
        private final PreferenceAdapter mAdapter;
        public MyListener(PreferenceAdapter adapter) {
            mAdapter = adapter;
        }
        public void onItemSelected(GLView view, int position) {
            mAdapter.onItemSelected(view, position);
            onPreferenceChanged(position - 1);
        }
    }
    @Override
    protected ResourceTexture getIcon() {
        int index = mIndex;
        if (mIcon[index] == null) {
            Context context = getGLRootView().getContext();
            mIcon[index] = new ResourceTexture(
                    context, mPreference.getLargeIconIds()[index]);
        }
        return mIcon[index];
    }
}
