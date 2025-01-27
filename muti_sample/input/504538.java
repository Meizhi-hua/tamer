public class TextAlign extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Paint   mPaint;
        private float   mX;
        private float[] mPos;
        private Path    mPath;
        private Paint   mPathPaint;
        private static final int DY = 30;
        private static final String TEXT_L = "Left";
        private static final String TEXT_C = "Center";
        private static final String TEXT_R = "Right";
        private static final String POSTEXT = "Positioned";
        private static final String TEXTONPATH = "Along a path";
        private static void makePath(Path p) {
            p.moveTo(10, 0);
            p.cubicTo(100, -50, 200, 50, 300, 0);
        }
        private float[] buildTextPositions(String text, float y, Paint paint) {
            float[] widths = new float[text.length()];
            int n = paint.getTextWidths(text, widths);
            float[] pos = new float[n * 2];
            float accumulatedX = 0;
            for (int i = 0; i < n; i++) {
                pos[i*2 + 0] = accumulatedX;
                pos[i*2 + 1] = y;
                accumulatedX += widths[i];
            }
            return pos;
        }
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(30);
            mPaint.setTypeface(Typeface.SERIF);
            mPos = buildTextPositions(POSTEXT, 0, mPaint);
            mPath = new Path();
            makePath(mPath);
            mPathPaint = new Paint();
            mPathPaint.setAntiAlias(true);
            mPathPaint.setColor(0x800000FF);
            mPathPaint.setStyle(Paint.Style.STROKE);
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            Paint p = mPaint;
            float x = mX;
            float y = 0;
            float[] pos = mPos;
            p.setColor(0x80FF0000);
            canvas.drawLine(x, y, x, y+DY*3, p);
            p.setColor(Color.BLACK);
            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(TEXT_L, x, y, p);
            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(TEXT_C, x, y, p);
            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(TEXT_R, x, y, p);
            canvas.translate(100, DY*2);
            p.setColor(0xBB00FF00);
            for (int i = 0; i < pos.length/2; i++) {
                canvas.drawLine(pos[i*2+0], pos[i*2+1]-DY,
                                pos[i*2+0], pos[i*2+1]+DY*2, p);
            }
            p.setColor(Color.BLACK);
            p.setTextAlign(Paint.Align.LEFT);
            canvas.drawPosText(POSTEXT, pos, p);
            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawPosText(POSTEXT, pos, p);
            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawPosText(POSTEXT, pos, p);
            canvas.translate(-100, DY*2);
            canvas.drawPath(mPath, mPathPaint);
            p.setTextAlign(Paint.Align.LEFT);
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p);
            canvas.translate(0, DY*1.5f);
            canvas.drawPath(mPath, mPathPaint);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p);
            canvas.translate(0, DY*1.5f);
            canvas.drawPath(mPath, mPathPaint);
            p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p);
        }
        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            mX = w * 0.5f;  
        }
    }
}
