public class AnticipateOvershootInterpolator implements Interpolator {
    private final float mTension;
    public AnticipateOvershootInterpolator() {
        mTension = 2.0f * 1.5f;
    }
    public AnticipateOvershootInterpolator(float tension) {
        mTension = tension * 1.5f;
    }
    public AnticipateOvershootInterpolator(float tension, float extraTension) {
        mTension = tension * extraTension;
    }
    public AnticipateOvershootInterpolator(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, AnticipateOvershootInterpolator);
        mTension = a.getFloat(AnticipateOvershootInterpolator_tension, 2.0f) *
                a.getFloat(AnticipateOvershootInterpolator_extraTension, 1.5f);
        a.recycle();
    }
    private static float a(float t, float s) {
        return t * t * ((s + 1) * t - s);
    }
    private static float o(float t, float s) {
        return t * t * ((s + 1) * t + s);
    }
    public float getInterpolation(float t) {
        if (t < 0.5f) return 0.5f * a(t * 2.0f, mTension);
        else return 0.5f * (o(t * 2.0f - 2.0f, mTension) + 2.0f);
    }
}
