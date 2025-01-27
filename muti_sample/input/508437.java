public class RotatePane extends GLView {
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    private int mOrientation = 0;
    private GLView mChild;
    @Override
    protected void onLayout(
            boolean change, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        switch (mOrientation) {
            case UP:
            case DOWN:
                mChild.layout(0, 0, width, height);
                break;
            case LEFT:
            case RIGHT:
                mChild.layout(0, 0, height, width);
                break;
        }
    }
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        GLView c = mChild;
        switch(mOrientation) {
            case UP:
            case DOWN:
                c.measure(widthSpec, heightSpec);
                setMeasuredSize(c.getMeasuredWidth(), c.getMeasuredHeight());
                break;
            case LEFT:
            case RIGHT:
                mChild.measure(heightSpec, widthSpec);
                setMeasuredSize(c.getMeasuredHeight(), c.getMeasuredWidth());
        }
    }
    @Override
    protected void render(GLRootView view, GL11 gl) {
        if (mOrientation == UP) {
            mChild.render(view, gl);
            return;
        }
        view.pushTransform();
        Matrix matrix = view.getTransformation().getMatrix();
        float width = getWidth();
        float height = getHeight();
        switch (mOrientation) {
            case DOWN:
                matrix.preRotate(180, width / 2, height / 2);
                break;
            case LEFT:
                matrix.preRotate(270, height / 2, height / 2);
                break;
            case RIGHT:
                matrix.preRotate(90, width / 2, width / 2);
                break;
        }
        mChild.render(view, gl);
        view.popTransform();
    }
    @Override
    protected boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float width = getWidth();
        float height = getHeight();
        switch (mOrientation) {
            case DOWN: event.setLocation(width - x, height - y); break;
            case LEFT: event.setLocation(height - y, x); break;
            case RIGHT: event.setLocation(y, width - x); break;
        }
        boolean result = mChild.dispatchTouchEvent(event);
        event.setLocation(x, y);
        return result;
    }
    public void setOrientation(int orientation) {
        if (mOrientation == orientation) return;
        mOrientation = orientation;
        requestLayout();
    }
    public void setContent(GLView view) {
        if (mChild == view) return;
        if (mChild != null) super.clearComponents();
        mChild = view;
        if (view != null) super.addComponent(view);
        requestLayout();
    }
    @Override
    public void addComponent(GLView view) {
        throw new UnsupportedOperationException("use setContent(GLView)");
    }
    @Override
    public void clearComponents() {
        throw new UnsupportedOperationException("use setContent(null)");
    }
}
