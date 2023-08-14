public class UnicodeChart extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Paint mBigCharPaint;
        private Paint mLabelPaint;
        private final char[] mChars = new char[256];
        private final float[] mPos = new float[512];
        private int mBase;
        private static final int XMUL = 20;
        private static final int YMUL = 28;
        private static final int YBASE = 18;
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);
            mBigCharPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBigCharPaint.setTextSize(15);
            mBigCharPaint.setTextAlign(Paint.Align.CENTER);
            mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLabelPaint.setTextSize(8);
            mLabelPaint.setTextAlign(Paint.Align.CENTER);
            float[] pos = mPos;
            int index = 0;
            for (int col = 0; col < 16; col++) {
                final float x = col * 20 + 10;
                for (int row = 0; row < 16; row++) {
                    pos[index++] = x;
                    pos[index++] = row * YMUL + YBASE;
                }
            }
        }
        private float computeX(int index) {
            return (index >> 4) * 20 + 10;
        }
        private float computeY(int index) {
            return (index & 0xF) * YMUL + YMUL;
        }
        private void drawChart(Canvas canvas, int base) {
            char[] chars = mChars;
            for (int i = 0; i < 256; i++) {
                int unichar = base + i;
                chars[i] = (char)unichar;
                canvas.drawText(Integer.toHexString(unichar),
                                computeX(i), computeY(i), mLabelPaint);
            }
            canvas.drawPosText(chars, 0, 256, mPos, mBigCharPaint);
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);            
            canvas.translate(0, 1);
            drawChart(canvas, mBase * 256);
        }
        @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (mBase > 0) {
                        mBase -= 1;
                        invalidate();
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mBase += 1;
                    invalidate();
                    return true;
                default:
                    break;
            }
            return super.onKeyDown(keyCode, event);
        }
    }
}