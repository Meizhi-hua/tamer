public class Touch {
    private Touch() { }
    public static void scrollTo(TextView widget, Layout layout, int x, int y) {
        int padding = widget.getTotalPaddingTop() +
                      widget.getTotalPaddingBottom();
        int top = layout.getLineForVertical(y);
        int bottom = layout.getLineForVertical(y + widget.getHeight() -
                                               padding);
        int left = Integer.MAX_VALUE;
        int right = 0;
        Alignment a = null;
        for (int i = top; i <= bottom; i++) {
            left = (int) Math.min(left, layout.getLineLeft(i));
            right = (int) Math.max(right, layout.getLineRight(i));
            if (a == null) {
                a = layout.getParagraphAlignment(i);
            }
        }
        padding = widget.getTotalPaddingLeft() + widget.getTotalPaddingRight();
        int width = widget.getWidth();
        int diff = 0;
        if (right - left < width - padding) {
            if (a == Alignment.ALIGN_CENTER) {
                diff = (width - padding - (right - left)) / 2;
            } else if (a == Alignment.ALIGN_OPPOSITE) {
                diff = width - padding - (right - left);
            }
        }
        x = Math.min(x, right - (width - padding) - diff);
        x = Math.max(x, left - diff);
        widget.scrollTo(x, y);
    }
    public static int getMaxScrollX(TextView widget, Layout layout, int y) {
        int top = layout.getLineForVertical(y);
        int bottom = layout.getLineForVertical(y + widget.getHeight()
                - widget.getTotalPaddingTop() -widget.getTotalPaddingBottom());
        int left = Integer.MAX_VALUE;
        int right = 0;
        for (int i = top; i <= bottom; i++) {
            left = (int) Math.min(left, layout.getLineLeft(i));
            right = (int) Math.max(right, layout.getLineRight(i));
        }
        return right - left - widget.getWidth() - widget.getTotalPaddingLeft()
                - widget.getTotalPaddingRight();
    }
    public static boolean onTouchEvent(TextView widget, Spannable buffer,
                                       MotionEvent event) {
        DragState[] ds;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            ds = buffer.getSpans(0, buffer.length(), DragState.class);
            for (int i = 0; i < ds.length; i++) {
                buffer.removeSpan(ds[i]);
            }
            buffer.setSpan(new DragState(event.getX(), event.getY(),
                            widget.getScrollX(), widget.getScrollY()),
                    0, 0, Spannable.SPAN_MARK_MARK);
            return true;
        case MotionEvent.ACTION_UP:
            ds = buffer.getSpans(0, buffer.length(), DragState.class);
            for (int i = 0; i < ds.length; i++) {
                buffer.removeSpan(ds[i]);
            }
            if (ds.length > 0 && ds[0].mUsed) {
                return true;
            } else {
                return false;
            }
        case MotionEvent.ACTION_MOVE:
            ds = buffer.getSpans(0, buffer.length(), DragState.class);
            if (ds.length > 0) {
                if (ds[0].mFarEnough == false) {
                    int slop = ViewConfiguration.get(widget.getContext()).getScaledTouchSlop();
                    if (Math.abs(event.getX() - ds[0].mX) >= slop ||
                        Math.abs(event.getY() - ds[0].mY) >= slop) {
                        ds[0].mFarEnough = true;
                    }
                }
                if (ds[0].mFarEnough) {
                    ds[0].mUsed = true;
                    boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                                   KeyEvent.META_SHIFT_ON) == 1) ||
                                   (MetaKeyKeyListener.getMetaState(buffer,
                                    MetaKeyKeyListener.META_SELECTING) != 0);
                    float dx;
                    float dy;
                    if (cap) {
                        dx = event.getX() - ds[0].mX;
                        dy = event.getY() - ds[0].mY;
                    } else {
                        dx = ds[0].mX - event.getX();
                        dy = ds[0].mY - event.getY();
                    }
                    ds[0].mX = event.getX();
                    ds[0].mY = event.getY();
                    int nx = widget.getScrollX() + (int) dx;
                    int ny = widget.getScrollY() + (int) dy;
                    int padding = widget.getTotalPaddingTop() +
                                  widget.getTotalPaddingBottom();
                    Layout layout = widget.getLayout();
                    ny = Math.min(ny, layout.getHeight() - (widget.getHeight() -
                                                            padding));
                    ny = Math.max(ny, 0);
                    int oldX = widget.getScrollX();
                    int oldY = widget.getScrollY();
                    scrollTo(widget, layout, nx, ny);
                    if (oldX != widget.getScrollX()
                            || oldY != widget.getScrollY()) {
                        widget.cancelLongPress();
                    }
                    return true;
                }
            }
        }
        return false;
    }
    public static int getInitialScrollX(TextView widget, Spannable buffer) {
        DragState[] ds = buffer.getSpans(0, buffer.length(), DragState.class);
        return ds.length > 0 ? ds[0].mScrollX : -1;
    }
    public static int getInitialScrollY(TextView widget, Spannable buffer) {
        DragState[] ds = buffer.getSpans(0, buffer.length(), DragState.class);
        return ds.length > 0 ? ds[0].mScrollY : -1;
    }
    private static class DragState implements NoCopySpan {
        public float mX;
        public float mY;
        public int mScrollX;
        public int mScrollY;
        public boolean mFarEnough;
        public boolean mUsed;
        public DragState(float x, float y, int scrollX, int scrollY) {
            mX = x;
            mY = y;
            mScrollX = scrollX;
            mScrollY = scrollY;
        }
    }
}
