public class SurfaceViewStubActivity extends Activity {
    private MockSurfaceView mSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSurfaceView = new MockSurfaceView(this);
        setContentView(mSurfaceView);
    }
    public MockSurfaceView getSurfaceView() {
        return mSurfaceView;
    }
    public class MockSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private static final int FIX_WIDTH = 240;
        private static final int FIX_HEIGHT = 240;
        private static final int BITMAP_WIDTH = 100;
        private static final int BITMAP_HEIGHT = 100;
        private static final int RECT_LEFT = 20;
        private static final int RECT_TOP = 100;
        private static final int RECT_RIGHT = 200;
        private static final int RECT_BOTTOM = 200;
        private SurfaceHolder mHolder;
        private MockCanvas mCanvas;
        private Thread mSurfaceViewThread;
        private boolean mIsDraw;
        private boolean mIsAttachedToWindow;
        private boolean mIsDetachedFromWindow;
        private boolean mIsOnMeasure;
        private boolean mIsOnScrollChanged;
        private boolean mIsOnSizeChanged;
        private boolean mIsOnWindowVisibilityChanged;
        private boolean mIsDispatchDraw;
        private boolean mIsSurfaceChanged;
        private int mWidthInOnMeasure;
        private int mHeightInOnMeasure;
        private int mOldLOnScrollChanged;
        private int mOldTOnScrollChanged;
        private int mOldWOnSizeChanged;
        private int mOldHOnSizeChanged;
        private int mVisibilityOnWindowVisibilityChanged;
        public MockSurfaceView(Context context) {
            super(context);
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setFixedSize(FIX_WIDTH, FIX_HEIGHT);
        }
        @Override
        public void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            mVisibilityOnWindowVisibilityChanged = visibility;
            mIsOnWindowVisibilityChanged = true;
        }
        public int getVInOnWindowVisibilityChanged() {
            return mVisibilityOnWindowVisibilityChanged;
        }
        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            mIsDraw = true;
        }
        @Override
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            mIsAttachedToWindow = true;
        }
        @Override
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            mIsDetachedFromWindow = true;
        }
        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mWidthInOnMeasure = getDefaultSize(FIX_WIDTH, widthMeasureSpec);
            mHeightInOnMeasure = getDefaultSize(FIX_HEIGHT, heightMeasureSpec);
            mIsOnMeasure = true;
        }
        public int getWidthInOnMeasure() {
            return mWidthInOnMeasure;
        }
        public int getHeightInOnMeasure() {
            return mHeightInOnMeasure;
        }
        @Override
        public void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            mOldLOnScrollChanged = oldl;
            mOldTOnScrollChanged = oldt;
            mIsOnScrollChanged = true;
        }
        public int getOldHorizontal() {
            return mOldLOnScrollChanged;
        }
        public int getOldVertical() {
            return mOldTOnScrollChanged;
        }
        @Override
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mOldWOnSizeChanged = oldw;
            mOldHOnSizeChanged = oldh;
            mIsOnSizeChanged = true;
        }
        public int getOldWidth() {
            return mOldWOnSizeChanged;
        }
        public int getOldHeight() {
            return mOldHOnSizeChanged;
        }
        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            mIsDispatchDraw = true;
        }
        public void setFormat(int format) {
            getHolder().setFormat(format);
        }
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceViewThread = new Thread() {
                @Override
                public void run() {
                    mCanvas = new MockCanvas(Bitmap.createBitmap( BITMAP_WIDTH,
                                                                  BITMAP_HEIGHT,
                                                                  Bitmap.Config.ARGB_8888));
                    draw(mCanvas);
                    Canvas canvas = mHolder.lockCanvas();
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setColor(Color.BLUE);
                    canvas.drawRect(RECT_LEFT, RECT_TOP, RECT_RIGHT, RECT_BOTTOM, paint);
                    mHolder.unlockCanvasAndPost(canvas);
                }
            };
            mSurfaceViewThread.start();
        }
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceViewThread = null;
        }
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mIsSurfaceChanged = true;
        }
        public boolean isDraw() {
            return mIsDraw;
        }
        public boolean isOnAttachedToWindow() {
            return mIsAttachedToWindow;
        }
        public boolean isDetachedFromWindow() {
            return mIsDetachedFromWindow;
        }
        public boolean isOnMeasureCalled() {
            return mIsOnMeasure;
        }
        public boolean isOnScrollChanged() {
            return mIsOnScrollChanged;
        }
        public boolean isOnSizeChangedCalled() {
            return mIsOnSizeChanged;
        }
        public void resetOnSizeChangedFlag(boolean b) {
            mIsOnSizeChanged = b;
        }
        public boolean isOnWindowVisibilityChanged() {
            return mIsOnWindowVisibilityChanged;
        }
        public boolean isDispatchDraw() {
            return mIsDispatchDraw;
        }
        public boolean isDrawColor() {
            if (mCanvas != null) {
                return mCanvas.isDrawColor();
            } else {
                return false;
            }
        }
        public boolean isSurfaceChanged() {
            return mIsSurfaceChanged;
        }
        public void setDrawColor(boolean isDrawColor) {
            if (mCanvas != null) {
                mCanvas.setDrawColor(isDrawColor);
            }
        }
    }
    class MockCanvas extends Canvas {
        private boolean mIsDrawColor;
        public MockCanvas(Bitmap bitmap) {
            super(bitmap);
        }
        @Override
        public void drawColor(int color, Mode mode) {
            super.drawColor(color, mode);
            mIsDrawColor = true;
        }
        public boolean isDrawColor() {
            return mIsDrawColor;
        }
        public void setDrawColor(boolean isDrawColor) {
            this.mIsDrawColor = isDrawColor;
        }
    }
}
