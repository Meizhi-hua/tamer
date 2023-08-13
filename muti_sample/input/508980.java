public class ScaleAnimation extends Animation {
    private float mFromX;
    private float mToX;
    private float mFromY;
    private float mToY;
    private int mPivotXType = ABSOLUTE;
    private int mPivotYType = ABSOLUTE;
    private float mPivotXValue = 0.0f;
    private float mPivotYValue = 0.0f;
    private float mPivotX;
    private float mPivotY;
    public ScaleAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.ScaleAnimation);
        mFromX = a.getFloat(com.android.internal.R.styleable.ScaleAnimation_fromXScale, 0.0f);
        mToX = a.getFloat(com.android.internal.R.styleable.ScaleAnimation_toXScale, 0.0f);
        mFromY = a.getFloat(com.android.internal.R.styleable.ScaleAnimation_fromYScale, 0.0f);
        mToY = a.getFloat(com.android.internal.R.styleable.ScaleAnimation_toYScale, 0.0f);
        Description d = Description.parseValue(a.peekValue(
                com.android.internal.R.styleable.ScaleAnimation_pivotX));
        mPivotXType = d.type;
        mPivotXValue = d.value;
        d = Description.parseValue(a.peekValue(
            com.android.internal.R.styleable.ScaleAnimation_pivotY));
        mPivotYType = d.type;
        mPivotYValue = d.value;
        a.recycle();
    }
    public ScaleAnimation(float fromX, float toX, float fromY, float toY) {
        mFromX = fromX;
        mToX = toX;
        mFromY = fromY;
        mToY = toY;
        mPivotX = 0;
        mPivotY = 0;
    }
    public ScaleAnimation(float fromX, float toX, float fromY, float toY,
            float pivotX, float pivotY) {
        mFromX = fromX;
        mToX = toX;
        mFromY = fromY;
        mToY = toY;
        mPivotXType = ABSOLUTE;
        mPivotYType = ABSOLUTE;
        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
    }
    public ScaleAnimation(float fromX, float toX, float fromY, float toY,
            int pivotXType, float pivotXValue, int pivotYType, float pivotYValue) {
        mFromX = fromX;
        mToX = toX;
        mFromY = fromY;
        mToY = toY;
        mPivotXValue = pivotXValue;
        mPivotXType = pivotXType;
        mPivotYValue = pivotYValue;
        mPivotYType = pivotYType;
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float sx = 1.0f;
        float sy = 1.0f;
        if (mFromX != 1.0f || mToX != 1.0f) {
            sx = mFromX + ((mToX - mFromX) * interpolatedTime);
        }
        if (mFromY != 1.0f || mToY != 1.0f) {
            sy = mFromY + ((mToY - mFromY) * interpolatedTime);
        }
        if (mPivotX == 0 && mPivotY == 0) {
            t.getMatrix().setScale(sx, sy);
        } else {
            t.getMatrix().setScale(sx, sy, mPivotX, mPivotY);
        }
    }
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
        mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
    }
}
