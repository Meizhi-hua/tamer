public abstract class AbstractIndicator extends GLView {
    private static final int DEFAULT_PADDING = 3;
    private int mOrientation = 0;
    abstract protected Texture getIcon();
    public AbstractIndicator(Context context) {
        int padding = GLRootView.dpToPixel(context, DEFAULT_PADDING);
        setPaddings(padding, 0, padding, 0);
    }
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        Texture icon = getIcon();
        new MeasureHelper(this)
               .setPreferredContentSize(icon.getWidth(), icon.getHeight())
               .measure(widthSpec, heightSpec);
    }
    @Override
    protected void render(GLRootView root, GL11 gl) {
        Texture icon = getIcon();
        if (icon != null) {
            Rect p = mPaddings;
            int width = getWidth() - p.left - p.right;
            int height = getHeight() - p.top - p.bottom;
            if (mOrientation != 0) {
                Transformation trans = root.pushTransform();
                Matrix matrix = trans.getMatrix();
                matrix.preTranslate(p.left + width / 2, p.top + height / 2);
                matrix.preRotate(-mOrientation);
                icon.draw(root, -icon.getWidth() / 2, -icon.getHeight() / 2);
                root.popTransform();
            } else {
                icon.draw(root,
                        p.left + (width - icon.getWidth()) / 2,
                        p.top + (height - icon.getHeight()) / 2);
            }
        }
    }
    public void setOrientation(int orientation) {
        if (orientation % 90 != 0) throw new IllegalArgumentException();
        orientation = orientation % 360;
        if (orientation < 0) orientation += 360;
        if (mOrientation == orientation) return;
        mOrientation = orientation;
        if (getGLRootView() != null) {
            AlphaAnimation anim = new AlphaAnimation(0.2f, 1);
            anim.setDuration(200);
            startAnimation(anim);
        }
    }
    abstract public GLView getPopupContent();
    abstract public void overrideSettings(String key, String settings);
    abstract public void reloadPreferences();
}
