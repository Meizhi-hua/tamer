public class ZoomIndicator extends AbstractIndicator {
    private static final DecimalFormat sZoomFormat = new DecimalFormat("#.#x");
    private static final float FONT_SIZE = 18;
    private static final int FONT_COLOR = 0xA8FFFFFF;
    protected static final String TAG = "ZoomIndicator";
    private final float mFontSize;
    private ZoomController mZoomController;
    private LinearLayout mPopupContent;
    private ZoomListener mZoomListener;
    private int mZoomIndex = 0;
    private int mDrawIndex = -1;
    private float mZoomRatios[];
    private StringTexture mTitle;
    public ZoomIndicator(Context context) {
        super(context);
        mFontSize = GLRootView.dpToPixel(context, FONT_SIZE);
    }
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int maxWidth = 0;
        int maxHeight = 0;
        int n = mZoomRatios == null ? 0: mZoomRatios.length;
        for (int i = 0; i < n; ++i) {
            float value = mZoomRatios[i];
            Texture tex = StringTexture.newInstance(
                    sZoomFormat.format(value), mFontSize, FONT_COLOR);
            if (maxWidth < tex.getWidth()) maxWidth = tex.getWidth();
            if (maxHeight < tex.getHeight()) maxHeight = tex.getHeight();
        }
        new MeasureHelper(this)
                .setPreferredContentSize(maxWidth, maxHeight)
                .measure(widthSpec, heightSpec);
    }
    @Override
    protected Texture getIcon() {
        if (mDrawIndex != mZoomIndex) {
            mDrawIndex = mZoomIndex;
            if (mTitle != null) mTitle.deleteFromGL();
            float value = mZoomRatios == null ? 0 : mZoomRatios[mZoomIndex];
            mTitle = StringTexture.newInstance(
                    sZoomFormat.format(value), mFontSize, FONT_COLOR);
        }
        return mTitle;
    }
    @Override
    public GLView getPopupContent() {
        if (mZoomController == null) {
            Context context = getGLRootView().getContext();
            mZoomController = new ZoomController(context);
            mZoomController.setAvailableZoomRatios(mZoomRatios);
            mZoomController.setPaddings(15, 6, 15, 6);
            mPopupContent = new LinearLayout();
            GLOptionHeader header = new GLOptionHeader(context,
                    context.getString(R.string.zoom_control_title));
            header.setBackground(new NinePatchTexture(
                    context, R.drawable.optionheader_background));
            header.setPaddings(6, 3, 6, 3);
            mPopupContent.addComponent(header);
            mPopupContent.addComponent(mZoomController);
            mZoomController.setZoomListener(new MyZoomListener());
            mZoomController.setZoomIndex(mZoomIndex);
        }
        return mPopupContent;
    }
    @Override
    public void overrideSettings(String key, String settings) {
    }
    @Override
    public void reloadPreferences() {
    }
    public void setZoomRatios(float[] ratios) {
        mZoomRatios = ratios;
        requestLayout();
    }
    private class MyZoomListener implements ZoomController.ZoomListener {
        public void onZoomChanged(int index, float value, boolean isMoving) {
            if (mZoomListener != null) {
                mZoomListener.onZoomChanged(index, value, isMoving);
            }
            if (mZoomIndex != index) onZoomIndexChanged(index);
        }
    }
    private void onZoomIndexChanged(int index) {
        if (mZoomIndex == index) return;
        mZoomIndex = index;
        invalidate();
    }
    public void setZoomListener(ZoomListener listener) {
        mZoomListener = listener;
    }
    public void setZoomIndex(int index) {
        if (mZoomIndex == index) return;
        if (mZoomController != null) {
            mZoomController.setZoomIndex(index);
        } else {
            onZoomIndexChanged(index);
        }
    }
}
