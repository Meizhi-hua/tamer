public class IndicatorBar extends GLView {
    public static final int INDEX_NONE = -1;
    private NinePatchTexture mBackground;
    private NinePatchTexture mHighlight;
    private int mSelectedIndex = INDEX_NONE;
    private OnItemSelectedListener mSelectedListener;
    private boolean mActivated = false;
    private boolean mSelectionChanged = false;
    private class Background extends GLView {
        @Override
        protected void render(GLRootView root, GL11 gl) {
            mBackground.setSize(getWidth(), getHeight());
            mBackground.draw(root, 0, 0);
            if (mActivated && mSelectedIndex != INDEX_NONE
                    && mHighlight != null) {
                Rect bounds = IndicatorBar.this.getComponent(
                        mSelectedIndex + 1).mBounds;
                mHighlight.setSize(bounds.width(), bounds.height());
                mHighlight.draw(root, bounds.left, bounds.top);
            }
        }
    }
    public interface OnItemSelectedListener {
        public void onItemSelected(GLView view, int position);
        public void onNothingSelected();
    }
    public IndicatorBar() {
        GLView background = new Background();
        background.setVisibility(GLView.INVISIBLE);
        addComponent(background);
    }
    public void overrideSettings(String key, String value) {
        for (int i = 1, n = getComponentCount(); i < n; ++i) {
            AbstractIndicator indicator = (AbstractIndicator) getComponent(i);
            indicator.overrideSettings(key, value);
        }
    }
    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        mSelectedListener = l;
    }
    public void setBackground(NinePatchTexture background) {
        if (mBackground == background) return;
        mBackground = background;
        if (background != null) {
            setPaddings(background.getPaddings());
        } else {
            setPaddings(0, 0, 0, 0);
        }
        invalidate();
    }
    public void setHighlight(NinePatchTexture highlight) {
        if (mHighlight == highlight) return;
        mHighlight = highlight;
        invalidate();
    }
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = 0;
        int height = 0;
        for (int i = 1, n = getComponentCount(); i < n; ++i) {
            GLView component = getComponent(i);
            component.measure(
                    MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            width = Math.max(width, component.getMeasuredWidth());
            height += component.getMeasuredHeight();
        }
        new MeasureHelper(this)
                .setPreferredContentSize(width, height)
                .measure(widthSpec, heightSpec);
    }
    @Override
    protected void onLayout(
            boolean changed, int left, int top, int right, int bottom) {
        getComponent(0).layout(0, 0, right - left, bottom - top);
        int count = getComponentCount();
        Rect p = mPaddings;
        int cBottom = bottom - top - p.bottom;
        int cRight = right - left - p.right;
        int yoffset = mPaddings.top;
        int xoffset = mPaddings.left;
        for (int i = 1; i < count; ++i) {
            int cHeight = (cBottom - yoffset) / (count - i);
            int nextYoffset = yoffset + cHeight;
            getComponent(i).layout(xoffset, yoffset, cRight, nextYoffset);
            yoffset = nextYoffset;
        }
    }
    private void setSelectedItem(GLView view, int index) {
        if (index == mSelectedIndex) return;
        mSelectionChanged = true;
        mSelectedIndex = index;
        if (mSelectedListener != null) {
            if (index == INDEX_NONE) {
                mSelectedListener.onNothingSelected();
            } else {
                mSelectedListener.onItemSelected(view, index);
            }
        }
        invalidate();
    }
    public void setSelectedIndex(int index) {
        if (index == mSelectedIndex) return;
        setSelectedItem(index == INDEX_NONE ? null :getComponent(index), index);
    }
    public void setActivated(boolean activated) {
        if (activated == mActivated) return;
        mActivated = activated;
        if (activated) {
            GLView background = getComponent(0);
            background.setVisibility(GLView.VISIBLE);
            AlphaAnimation anim = new AlphaAnimation(0, 1);
            anim.setDuration(200);
            background.startAnimation(anim);
        } else {
            GLView background = getComponent(0);
            background.setVisibility(GLView.INVISIBLE);
            AlphaAnimation anim = new AlphaAnimation(1, 0);
            anim.setDuration(200);
            background.startAnimation(anim);
        }
    }
    public boolean isActivated() {
        return mActivated;
    }
    @Override
    protected boolean dispatchTouchEvent(MotionEvent event) {
        return onTouch(event);
    }
    @Override @SuppressWarnings("fallthrough")
    protected boolean onTouch(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mSelectionChanged = false;
                setActivated(true);
            case MotionEvent.ACTION_MOVE:
                for (int i = 1, n = getComponentCount(); i < n; ++i) {
                    GLView component = getComponent(i);
                    if (y <= component.mBounds.bottom) {
                        setSelectedItem(component, i - 1);
                        return true;
                    }
                }
                setSelectedItem(null, INDEX_NONE);
                break;
            case MotionEvent.ACTION_UP:
                if (mSelectionChanged == false) {
                    setSelectedItem(null, INDEX_NONE);
                }
        }
        return true;
    }
    public void reloadPreferences() {
        for (int i = 1, n = getComponentCount(); i < n; ++i) {
            ((AbstractIndicator) getComponent(i)).reloadPreferences();
        }
    }
    public void setOrientation(int orientation) {
        for (int i = 1, n = getComponentCount(); i < n; ++i) {
            ((AbstractIndicator) getComponent(i)).setOrientation(orientation);
        }
    }
    public int getSelectedIndex() {
        return mSelectedIndex;
    }
}
