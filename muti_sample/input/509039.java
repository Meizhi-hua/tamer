public class CubeWallpaper2 extends WallpaperService {
    public static final String SHARED_PREFS_NAME="cube2settings";
    static class ThreeDPoint {
        float x;
        float y;
        float z;
    }
    static class ThreeDLine {
        int startPoint;
        int endPoint;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public Engine onCreateEngine() {
        return new CubeEngine();
    }
    class CubeEngine extends Engine 
        implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Handler mHandler = new Handler();
        ThreeDPoint [] mOriginalPoints;
        ThreeDPoint [] mRotatedPoints;
        ThreeDLine [] mLines;
        private final Paint mPaint = new Paint();
        private float mOffset;
        private float mTouchX = -1;
        private float mTouchY = -1;
        private long mStartTime;
        private float mCenterX;
        private float mCenterY;
        private final Runnable mDrawCube = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        private boolean mVisible;
        private SharedPreferences mPrefs;
        CubeEngine() {
            final Paint paint = mPaint;
            paint.setColor(0xffffffff);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(2);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            mStartTime = SystemClock.elapsedRealtime();
            mPrefs = CubeWallpaper2.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            String shape = prefs.getString("cube2_shape", "cube");
            readModel(shape);
        }
        private void readModel(String prefix) {
            int pid = getResources().getIdentifier(prefix + "points", "array", getPackageName());
            int lid = getResources().getIdentifier(prefix + "lines", "array", getPackageName());
            String [] p = getResources().getStringArray(pid);
            int numpoints = p.length;
            mOriginalPoints = new ThreeDPoint[numpoints];
            mRotatedPoints = new ThreeDPoint[numpoints];
            for (int i = 0; i < numpoints; i++) {
                mOriginalPoints[i] = new ThreeDPoint();
                mRotatedPoints[i] = new ThreeDPoint();
                String [] coord = p[i].split(" ");
                mOriginalPoints[i].x = Float.valueOf(coord[0]);
                mOriginalPoints[i].y = Float.valueOf(coord[1]);
                mOriginalPoints[i].z = Float.valueOf(coord[2]);
            }
            String [] l = getResources().getStringArray(lid);
            int numlines = l.length;
            mLines = new ThreeDLine[numlines];
            for (int i = 0; i < numlines; i++) {
                mLines[i] = new ThreeDLine();
                String [] idx = l[i].split(" ");
                mLines[i].startPoint = Integer.valueOf(idx[0]);
                mLines[i].endPoint = Integer.valueOf(idx[1]);
            }
        }
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawCube);
        }
        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mDrawCube);
            }
        }
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mCenterX = width/2.0f;
            mCenterY = height/2.0f;
            drawFrame();
        }
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDrawCube);
        }
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            mOffset = xOffset;
            drawFrame();
        }
        @Override
        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            } else {
                mTouchX = -1;
                mTouchY = -1;
            }
            super.onTouchEvent(event);
        }
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
            final Rect frame = holder.getSurfaceFrame();
            final int width = frame.width();
            final int height = frame.height();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    drawCube(c);
                    drawTouchPoint(c);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
            mHandler.removeCallbacks(mDrawCube);
            if (mVisible) {
                mHandler.postDelayed(mDrawCube, 1000 / 25);
            }
        }
        void drawCube(Canvas c) {
            c.save();
            c.translate(mCenterX, mCenterY);
            c.drawColor(0xff000000);
            long now = SystemClock.elapsedRealtime();
            float xrot = ((float)(now - mStartTime)) / 1000;
            float yrot = (0.5f - mOffset) * 2.0f;
            rotateAndProjectPoints(xrot, yrot);
            drawLines(c);
            c.restore();
        }
        void rotateAndProjectPoints(float xrot, float yrot) {
            int n = mOriginalPoints.length;
            for (int i = 0; i < n; i++) {
                ThreeDPoint p = mOriginalPoints[i];
                float x = p.x;
                float y = p.y;
                float z = p.z;
                float newy = (float)(Math.sin(xrot) * z + Math.cos(xrot) * y);
                float newz = (float)(Math.cos(xrot) * z - Math.sin(xrot) * y);
                float newx = (float)(Math.sin(yrot) * newz + Math.cos(yrot) * x);
                newz = (float)(Math.cos(yrot) * newz - Math.sin(yrot) * x);
                float screenX = newx / (4 - newz / 400);
                float screenY = newy / (4 - newz / 400);
                mRotatedPoints[i].x = screenX;
                mRotatedPoints[i].y = screenY;
                mRotatedPoints[i].z = 0;
            }
        }
        void drawLines(Canvas c) {
            int n = mLines.length;
            for (int i = 0; i < n; i++) {
                ThreeDLine l = mLines[i];
                ThreeDPoint start = mRotatedPoints[l.startPoint];
                ThreeDPoint end = mRotatedPoints[l.endPoint];
                c.drawLine(start.x, start.y, end.x, end.y, mPaint);
            }
        }
        void drawTouchPoint(Canvas c) {
            if (mTouchX >=0 && mTouchY >= 0) {
                c.drawCircle(mTouchX, mTouchY, 80, mPaint);
            }
        }
    }
}
