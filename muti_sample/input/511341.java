public class Pictures extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private Picture mPicture;
        private Drawable mDrawable;
        static void drawSomething(Canvas canvas) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(0x88FF0000);
            canvas.drawCircle(50, 50, 40, p);
            p.setColor(Color.GREEN);
            p.setTextSize(30);
            canvas.drawText("Pictures", 60, 60, p);
        }
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);
            mPicture = new Picture();
            drawSomething(mPicture.beginRecording(200, 100));
            mPicture.endRecording();
            mDrawable = new PictureDrawable(mPicture);
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            canvas.drawPicture(mPicture);
            canvas.drawPicture(mPicture, new RectF(0, 100, getWidth(), 200));
            mDrawable.setBounds(0, 200, getWidth(), 300);
            mDrawable.draw(canvas);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            mPicture.writeToStream(os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            canvas.translate(0, 300);
            canvas.drawPicture(Picture.createFromStream(is));
        }
    }
}
