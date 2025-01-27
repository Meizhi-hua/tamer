public class TransformTestActivity extends Activity {
    public TransformTestActivity() {
        super();
        init(false);
    }
    public TransformTestActivity(boolean noCompat) {
        super();
        init(noCompat);
    }
    public void init(boolean noCompat) {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.act_title);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        TransformView view = new TransformView(getApplicationContext());
        Drawable drawable = getResources().getDrawable(R.drawable.logo);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicWidth());
        view.setDrawable(drawable);
        root.addView(view);
        setContentView(root);
    }
    private class TransformView extends View {
        private Drawable mDrawable;
        private float mPosX;
        private float mPosY;
        private float mScale = 1.f;
        private Matrix mMatrix;
        private ScaleGestureDetector mDetector;
        private float mLastX;
        private float mLastY;
        private class Listener implements ScaleGestureDetector.OnScaleGestureListener {
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = detector.getScaleFactor();
                Log.d("ttest", "Scale: " + scale);
                if (mScale * scale > 0.1f) {
                    if (mScale * scale < 10.f) {
                        mScale *= scale;
                    } else {
                        mScale = 10.f;
                    }
                } else {
                    mScale = 0.1f;
                }
                Log.d("ttest", "mScale: " + mScale + " mPos: (" + mPosX + ", " + mPosY + ")");
                float sizeX = mDrawable.getIntrinsicWidth()/2;
                float sizeY = mDrawable.getIntrinsicHeight()/2;
                float centerX = detector.getFocusX();
                float centerY = detector.getFocusY();
                float diffX = centerX - mPosX;
                float diffY = centerY - mPosY;
                diffX = diffX*scale - diffX;
                diffY = diffY*scale - diffY;
                mPosX -= diffX;
                mPosY -= diffY;
                mMatrix.reset();
                mMatrix.postTranslate(-sizeX, -sizeY);
                mMatrix.postScale(mScale, mScale);
                mMatrix.postTranslate(mPosX, mPosY);
                invalidate();
                return true;
            }
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }
            public void onScaleEnd(ScaleGestureDetector detector) {
                mLastX = detector.getFocusX();
                mLastY = detector.getFocusY();
            }            
        }
        public TransformView(Context context) {
            super(context);
            mMatrix = new Matrix();
            mDetector = new ScaleGestureDetector(context, new Listener());
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            mPosX = metrics.widthPixels/2;
            mPosY = metrics.heightPixels/2;
        }
        public void setDrawable(Drawable d) {
            mDrawable = d;
            float sizeX = mDrawable.getIntrinsicWidth()/2;
            float sizeY = mDrawable.getIntrinsicHeight()/2;
            mMatrix.reset();
            mMatrix.postTranslate(-sizeX, -sizeY);
            mMatrix.postScale(mScale, mScale);
            mMatrix.postTranslate(mPosX, mPosY);
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            mDetector.onTouchEvent(event);
            if (!mDetector.isInProgress()) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastX = event.getX();
                        mLastY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        final float x = event.getX();
                        final float y = event.getY();
                        mPosX += x - mLastX;
                        mPosY += y - mLastY;
                        mLastX = x;
                        mLastY = y;
                        float sizeX = mDrawable.getIntrinsicWidth()/2;
                        float sizeY = mDrawable.getIntrinsicHeight()/2;
                        mMatrix.reset();
                        mMatrix.postTranslate(-sizeX, -sizeY);
                        mMatrix.postScale(mScale, mScale);
                        mMatrix.postTranslate(mPosX, mPosY);
                        invalidate();
                        break;
                }
            }
            return true;
        }
        @Override
        public void onDraw(Canvas canvas) {
            int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.concat(mMatrix);
            mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }
}
