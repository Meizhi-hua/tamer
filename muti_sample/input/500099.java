public class LayoutManager {
    private static final String TAG = "LayoutManager";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private final Context mContext;
    private LayoutParameters mLayoutParams;
    private static LayoutManager sInstance;
    private LayoutManager(Context context) {
        mContext = context;
        initLayoutParameters(context.getResources().getConfiguration());
    }
    private void initLayoutParameters(Configuration configuration) {
        mLayoutParams = getLayoutParameters(
                configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                ? LayoutParameters.HVGA_PORTRAIT
                : LayoutParameters.HVGA_LANDSCAPE);
        if (LOCAL_LOGV) {
            Log.v(TAG, "LayoutParameters: " + mLayoutParams.getTypeDescription()
                    + ": " + mLayoutParams.getWidth() + "x" + mLayoutParams.getHeight());
        }
    }
    private static LayoutParameters getLayoutParameters(int displayType) {
        switch (displayType) {
            case LayoutParameters.HVGA_LANDSCAPE:
                return new HVGALayoutParameters(LayoutParameters.HVGA_LANDSCAPE);
            case LayoutParameters.HVGA_PORTRAIT:
                return new HVGALayoutParameters(LayoutParameters.HVGA_PORTRAIT);
        }
        throw new IllegalArgumentException(
                "Unsupported display type: " + displayType);
    }
    public static void init(Context context) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "DefaultLayoutManager.init()");
        }
        if (sInstance != null) {
            Log.w(TAG, "Already initialized.");
        }
        sInstance = new LayoutManager(context);
    }
    public static LayoutManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Uninitialized.");
        }
        return sInstance;
    }
    public void onConfigurationChanged(Configuration newConfig) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "-> LayoutManager.onConfigurationChanged().");
        }
        initLayoutParameters(newConfig);
    }
    public int getLayoutType() {
        return mLayoutParams.getType();
    }
    public int getLayoutWidth() {
        return mLayoutParams.getWidth();
    }
    public int getLayoutHeight() {
        return mLayoutParams.getHeight();
    }
    public LayoutParameters getLayoutParameters() {
        return mLayoutParams;
    }
}
