public class RestoreSettingsItem extends GLView {
    private static final int FONT_COLOR = Color.WHITE;
    private static final float FONT_SIZE = 18;
    private static final int LEFT_PADDING = 20;
    private static final int RIGHT_PADDING = 4;
    private static final int TOP_PADDING = 2;
    private static final int BOTTOM_PADDING = 2;
    private static int sLeftPadding = -1;
    private static int sRightPadding;
    private static int sTopPadding;
    private static int sBottomPadding;
    private static float sFontSize;
    private final StringTexture mText;
    private static void initializeStaticVariables(Context context) {
        if (sLeftPadding >= 0) return;
        sLeftPadding = dpToPixel(context, LEFT_PADDING);
        sRightPadding = dpToPixel(context, RIGHT_PADDING);
        sTopPadding = dpToPixel(context, TOP_PADDING);
        sBottomPadding = dpToPixel(context, BOTTOM_PADDING);
        sFontSize = dpToPixel(context, FONT_SIZE);
    }
    public RestoreSettingsItem(Context context, String title) {
        initializeStaticVariables(context);
        mText = StringTexture.newInstance(title, sFontSize, FONT_COLOR);
        setPaddings(sLeftPadding, sTopPadding, sRightPadding, sBottomPadding);
    }
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        new MeasureHelper(this)
                .setPreferredContentSize(mText.getWidth(), mText.getHeight())
                .measure(widthSpec, heightSpec);
    }
    @Override
    protected void render(GLRootView root, GL11 gl) {
        Rect p = mPaddings;
        int height = getHeight() - p.top - p.bottom;
        StringTexture title = mText;
        title.draw(root, p.left, p.top + (height - title.getHeight()) / 2);
    }
}
