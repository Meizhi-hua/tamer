abstract class ImageViewTouchBase extends ImageView {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageViewTouchBase";
    protected Matrix mBaseMatrix = new Matrix();
    protected Matrix mSuppMatrix = new Matrix();
    private final Matrix mDisplayMatrix = new Matrix();
    private final float[] mMatrixValues = new float[9];
    final protected RotateBitmap mBitmapDisplayed = new RotateBitmap(null);
    int mThisWidth = -1, mThisHeight = -1;
    float mMaxZoom;
    public interface Recycler {
        public void recycle(Bitmap b);
    }
    public void setRecycler(Recycler r) {
        mRecycler = r;
    }
    private Recycler mRecycler;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mThisWidth = right - left;
        mThisHeight = bottom - top;
        Runnable r = mOnLayoutRunnable;
        if (r != null) {
            mOnLayoutRunnable = null;
            r.run();
        }
        if (mBitmapDisplayed.getBitmap() != null) {
            getProperBaseMatrix(mBitmapDisplayed, mBaseMatrix);
            setImageMatrix(getImageViewMatrix());
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getScale() > 1.0f) {
            zoomTo(1.0f);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    protected Handler mHandler = new Handler();
    protected int mLastXTouchPos;
    protected int mLastYTouchPos;
    @Override
    public void setImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap, 0);
    }
    private void setImageBitmap(Bitmap bitmap, int rotation) {
        super.setImageBitmap(bitmap);
        Drawable d = getDrawable();
        if (d != null) {
            d.setDither(true);
        }
        Bitmap old = mBitmapDisplayed.getBitmap();
        mBitmapDisplayed.setBitmap(bitmap);
        mBitmapDisplayed.setRotation(rotation);
        if (old != null && old != bitmap && mRecycler != null) {
            mRecycler.recycle(old);
        }
    }
    public void clear() {
        setImageBitmapResetBase(null, true);
    }
    private Runnable mOnLayoutRunnable = null;
    public void setImageBitmapResetBase(final Bitmap bitmap, final boolean resetSupp) {
        setImageRotateBitmapResetBase(new RotateBitmap(bitmap), resetSupp);
    }
    public void setImageRotateBitmapResetBase(final RotateBitmap bitmap, final boolean resetSupp) {
        final int viewWidth = getWidth();
        if (viewWidth <= 0) {
            mOnLayoutRunnable = new Runnable() {
                public void run() {
                    setImageRotateBitmapResetBase(bitmap, resetSupp);
                }
            };
            return;
        }
        if (bitmap.getBitmap() != null) {
            getProperBaseMatrix(bitmap, mBaseMatrix);
            setImageBitmap(bitmap.getBitmap(), bitmap.getRotation());
        } else {
            mBaseMatrix.reset();
            setImageBitmap(null);
        }
        if (resetSupp) {
            mSuppMatrix.reset();
        }
        setImageMatrix(getImageViewMatrix());
        mMaxZoom = maxZoom();
    }
    protected void center(boolean horizontal, boolean vertical) {
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }
        Matrix m = getImageViewMatrix();
        RectF rect = new RectF(0, 0, mBitmapDisplayed.getBitmap().getWidth(), mBitmapDisplayed.getBitmap().getHeight());
        m.mapRect(rect);
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0, deltaY = 0;
        if (vertical) {
            int viewHeight = getHeight();
            if (height < viewHeight) {
                deltaY = (viewHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < viewHeight) {
                deltaY = getHeight() - rect.bottom;
            }
        }
        if (horizontal) {
            int viewWidth = getWidth();
            if (width < viewWidth) {
                deltaX = (viewWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < viewWidth) {
                deltaX = viewWidth - rect.right;
            }
        }
        postTranslate(deltaX, deltaY);
        setImageMatrix(getImageViewMatrix());
    }
    public ImageViewTouchBase(Context context) {
        super(context);
        init();
    }
    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        setScaleType(ImageView.ScaleType.MATRIX);
    }
    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }
    protected float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }
    protected float getScale() {
        return getScale(mSuppMatrix);
    }
    private void getProperBaseMatrix(RotateBitmap bitmap, Matrix matrix) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        matrix.reset();
        float widthScale = Math.min(viewWidth / w, 2.0f);
        float heightScale = Math.min(viewHeight / h, 2.0f);
        float scale = Math.min(widthScale, heightScale);
        matrix.postConcat(bitmap.getRotateMatrix());
        matrix.postScale(scale, scale);
        matrix.postTranslate((viewWidth - w * scale) / 2F, (viewHeight - h * scale) / 2F);
    }
    protected Matrix getImageViewMatrix() {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSuppMatrix);
        return mDisplayMatrix;
    }
    static final float SCALE_RATE = 1.25F;
    protected float maxZoom() {
        if (mBitmapDisplayed.getBitmap() == null) {
            return 1F;
        }
        float fw = (float) mBitmapDisplayed.getWidth() / (float) mThisWidth;
        float fh = (float) mBitmapDisplayed.getHeight() / (float) mThisHeight;
        float max = Math.max(fw, fh) * 4;
        return max;
    }
    protected void zoomTo(float scale, float centerX, float centerY) {
        if (scale > mMaxZoom) {
            scale = mMaxZoom;
        }
        float oldScale = getScale();
        float deltaScale = scale / oldScale;
        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }
    protected void zoomTo(final float scale, final float centerX, final float centerY, final float durationMs) {
        final float incrementPerMs = (scale - getScale()) / durationMs;
        final float oldScale = getScale();
        final long startTime = System.currentTimeMillis();
        mHandler.post(new Runnable() {
            public void run() {
                long now = System.currentTimeMillis();
                float currentMs = Math.min(durationMs, now - startTime);
                float target = oldScale + (incrementPerMs * currentMs);
                zoomTo(target, centerX, centerY);
                if (currentMs < durationMs) {
                    mHandler.post(this);
                }
            }
        });
    }
    protected void zoomTo(float scale) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;
        zoomTo(scale, cx, cy);
    }
    protected void zoomIn() {
        zoomIn(SCALE_RATE);
    }
    protected void zoomOut() {
        zoomOut(SCALE_RATE);
    }
    protected void zoomIn(float rate) {
        if (getScale() >= mMaxZoom) {
            return; 
        }
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;
        mSuppMatrix.postScale(rate, rate, cx, cy);
        setImageMatrix(getImageViewMatrix());
    }
    protected void zoomOut(float rate) {
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;
        Matrix tmp = new Matrix(mSuppMatrix);
        tmp.postScale(1F / rate, 1F / rate, cx, cy);
        if (getScale(tmp) < 1F) {
            mSuppMatrix.setScale(1F, 1F, cx, cy);
        } else {
            mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
        }
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }
    protected void postTranslate(float dx, float dy) {
        mSuppMatrix.postTranslate(dx, dy);
    }
    protected void panBy(float dx, float dy) {
        postTranslate(dx, dy);
        setImageMatrix(getImageViewMatrix());
    }
}
