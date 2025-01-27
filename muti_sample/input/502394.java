class ColorButton extends Button implements OnClickListener {
    int CLICK_FEEDBACK_COLOR;
    static final int CLICK_FEEDBACK_INTERVAL = 10;
    static final int CLICK_FEEDBACK_DURATION = 350;
    float mTextX;
    float mTextY;
    long mAnimStart;
    OnClickListener mListener;
    Paint mFeedbackPaint;
    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Calculator calc = (Calculator) context;
        init(calc);
        mListener = calc.mListener;
        setOnClickListener(this);
    }
    public void onClick(View view) {
        mListener.onClick(this);
    }
    private void init(Calculator calc) {
        Resources res = getResources();
        CLICK_FEEDBACK_COLOR = res.getColor(R.color.magic_flame);
        mFeedbackPaint = new Paint();
        mFeedbackPaint.setStyle(Style.STROKE);
        mFeedbackPaint.setStrokeWidth(2);
        getPaint().setColor(res.getColor(R.color.button_text));
        mAnimStart = -1;
        calc.adjustFontSize(this);
    }
    @Override 
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        measureText();
    }
    private void measureText() {
        Paint paint = getPaint();
        mTextX = (getWidth() - paint.measureText(getText().toString())) / 2;
        mTextY = (getHeight() - paint.ascent() - paint.descent()) / 2;
    }
    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        measureText();
    }
    private void drawMagicFlame(int duration, Canvas canvas) {
        int alpha = 255 - 255 * duration / CLICK_FEEDBACK_DURATION;
        int color = CLICK_FEEDBACK_COLOR | (alpha << 24);
        mFeedbackPaint.setColor(color);
        canvas.drawRect(1, 1, getWidth() - 1, getHeight() - 1, mFeedbackPaint);
    }
    @Override
    public void onDraw(Canvas canvas) {
        if (mAnimStart != -1) {
            int animDuration = (int) (System.currentTimeMillis() - mAnimStart);
            if (animDuration >= CLICK_FEEDBACK_DURATION) {
                mAnimStart = -1;
            } else {
                drawMagicFlame(animDuration, canvas);
                postInvalidateDelayed(CLICK_FEEDBACK_INTERVAL);
            }
        } else if (isPressed()) {
            drawMagicFlame(0, canvas);
        }
        CharSequence text = getText();
        canvas.drawText(text, 0, text.length(), mTextX, mTextY, getPaint());
    }
    public void animateClickFeedback() {
        mAnimStart = System.currentTimeMillis();
        invalidate();        
    } 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                animateClickFeedback();
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
                invalidate();
                break;
        }
        return result;
    }
}
