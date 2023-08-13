public class CameraHeadUpDisplay extends HeadUpDisplay {
    protected static final String TAG = "CamcoderHeadUpDisplay";
    private OtherSettingsIndicator mOtherSettings;
    private GpsIndicator mGpsIndicator;
    private ZoomIndicator mZoomIndicator;
    public CameraHeadUpDisplay(Context context) {
        super(context);
    }
    @Override
    protected void initializeIndicatorBar(
            Context context, PreferenceGroup group) {
        super.initializeIndicatorBar(context, group);
        ListPreference prefs[] = getListPreferences(group,
                CameraSettings.KEY_FOCUS_MODE,
                CameraSettings.KEY_EXPOSURE,
                CameraSettings.KEY_SCENE_MODE,
                CameraSettings.KEY_PICTURE_SIZE,
                CameraSettings.KEY_JPEG_QUALITY,
                CameraSettings.KEY_COLOR_EFFECT);
        mOtherSettings = new OtherSettingsIndicator(context, prefs);
        mOtherSettings.setOnRestorePreferencesClickedRunner(new Runnable() {
            public void run() {
                if (mListener != null) {
                    mListener.onRestorePreferencesClicked();
                }
            }
        });
        mIndicatorBar.addComponent(mOtherSettings);
        mGpsIndicator = new GpsIndicator(
                context, group, (IconListPreference)
                group.findPreference(CameraSettings.KEY_RECORD_LOCATION));
        mIndicatorBar.addComponent(mGpsIndicator);
        addIndicator(context, group, CameraSettings.KEY_WHITE_BALANCE);
        addIndicator(context, group, CameraSettings.KEY_FLASH_MODE);
        mZoomIndicator = new ZoomIndicator(context);
        mIndicatorBar.addComponent(mZoomIndicator);
    }
    public void setZoomListener(ZoomController.ZoomListener listener) {
        mZoomIndicator.setZoomListener(listener);
    }
    public void setZoomIndex(int index) {
        mZoomIndicator.setZoomIndex(index);
    }
    public void setGpsHasSignal(final boolean hasSignal) {
        GLRootView root = getGLRootView();
        if (root != null) {
            root.queueEvent(new Runnable() {
                public void run() {
                    mGpsIndicator.setHasSignal(hasSignal);
                }
            });
        } else {
            mGpsIndicator.setHasSignal(hasSignal);
        }
    }
    public void setZoomRatios(float[] zoomRatios) {
        mZoomIndicator.setZoomRatios(zoomRatios);
    }
}
