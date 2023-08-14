public class GpsIndicator extends BasicIndicator {
    private static final int GPS_ON_INDEX = 1;
    private ResourceTexture mNoSignalIcon;
    private boolean mHasSignal = false;
    public GpsIndicator(Context context,
            PreferenceGroup group, IconListPreference preference) {
        super(context, group, preference);
    }
    @Override
    protected ResourceTexture getIcon() {
        if (mIndex == GPS_ON_INDEX && !mHasSignal) {
            if (mNoSignalIcon == null) {
                Context context = getGLRootView().getContext();
                mNoSignalIcon = new ResourceTexture(
                        context, R.drawable.ic_viewfinder_gps_no_signal);
            }
            return mNoSignalIcon;
        }
        return super.getIcon();
    }
    public void setHasSignal(boolean hasSignal) {
        if (mHasSignal == hasSignal) return;
        mHasSignal = hasSignal;
        invalidate();
    }
    @Override
    protected void onPreferenceChanged(int newIndex) {
        mHasSignal = false;
        super.onPreferenceChanged(newIndex);
    }
}
