public class BitmapMesh extends GraphicsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    private static class SampleView extends View {
        private static final int WIDTH = 20;
        private static final int HEIGHT = 20;
        private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);
        private final Bitmap mBitmap;
        private final float[] mVerts = new float[COUNT*2];
        private final float[] mOrig = new float[COUNT*2];
        private final Matrix mMatrix = new Matrix();
        private final Matrix mInverse = new Matrix();
        private static void setXY(float[] array, int index, float x, float y) {
            array[index*2 + 0] = x;
            array[index*2 + 1] = y;
        }
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            mBitmap = BitmapFactory.decodeResource(getResources(),
                                                     R.drawable.beach);
            float w = mBitmap.getWidth();
            float h = mBitmap.getHeight();
            int index = 0;
            for (int y = 0; y <= HEIGHT; y++) {
                float fy = h * y / HEIGHT;
                for (int x = 0; x <= WIDTH; x++) {
                    float fx = w * x / WIDTH;                    
                    setXY(mVerts, index, fx, fy);
                    setXY(mOrig, index, fx, fy);
                    index += 1;
                }
            }
            mMatrix.setTranslate(10, 10);
            mMatrix.invert(mInverse);
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);
            canvas.concat(mMatrix);
            canvas.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, mVerts, 0,
                                  null, 0, null);
        }
        private void warp(float cx, float cy) {
            final float K = 10000;
            float[] src = mOrig;
            float[] dst = mVerts;
            for (int i = 0; i < COUNT*2; i += 2) {
                float x = src[i+0];
                float y = src[i+1];
                float dx = cx - x;
                float dy = cy - y;
                float dd = dx*dx + dy*dy;
                float d = FloatMath.sqrt(dd);
                float pull = K / (dd + 0.000001f);
                pull /= (d + 0.000001f);
                if (pull >= 1) {
                    dst[i+0] = cx;
                    dst[i+1] = cy;
                } else {
                    dst[i+0] = x + dx * pull;
                    dst[i+1] = y + dy * pull;
                }
            }
        }
        private int mLastWarpX = -9999; 
        private int mLastWarpY;
        @Override public boolean onTouchEvent(MotionEvent event) {
            float[] pt = { event.getX(), event.getY() };
            mInverse.mapPoints(pt);
            int x = (int)pt[0];
            int y = (int)pt[1];
            if (mLastWarpX != x || mLastWarpY != y) {
                mLastWarpX = x;
                mLastWarpY = y;
                warp(pt[0], pt[1]);
                invalidate();
            }
            return true;
        }
    }
}
