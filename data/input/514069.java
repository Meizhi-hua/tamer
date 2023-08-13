public class GLRootView extends GLSurfaceView
        implements GLSurfaceView.Renderer {
    private static final String TAG = "GLRootView";
    private final boolean ENABLE_FPS_TEST = false;
    private int mFrameCount = 0;
    private long mFrameCountingStart = 0;
    private static final int VERTEX_BUFFER_SIZE = 8;
    private static final int FLAG_INITIALIZED = 1;
    private static final int FLAG_NEED_LAYOUT = 2;
    private static float sPixelDensity = -1f;
    private GL11 mGL;
    private GLView mContentView;
    private DisplayMetrics mDisplayMetrics;
    private final ArrayList<Animation> mAnimations = new ArrayList<Animation>();
    private final Stack<Transformation> mFreeTransform =
            new Stack<Transformation>();
    private final Transformation mTransformation = new Transformation();
    private final Stack<Transformation> mTransformStack =
            new Stack<Transformation>();
    private float mLastAlpha = mTransformation.getAlpha();
    private final float mMatrixValues[] = new float[16];
    private final float mCoordBuffer[] = new float[8];
    private final float mPointBuffer[] = new float[4];
    private ByteBuffer mVertexBuffer;
    private ByteBuffer mTexCoordBuffer;
    private int mFlags = FLAG_NEED_LAYOUT;
    private long mAnimationTime;
    private Thread mGLThread;
    private boolean mIsQueueActive = true;
    private int mFirstWidth;
    private int mFirstHeight;
    private final Looper mLooper;
    public GLRootView(Context context) {
        this(context, null);
    }
    public GLRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
        mLooper = Looper.getMainLooper();
    }
    void registerLaunchedAnimation(Animation animation) {
        mAnimations.add(animation);
    }
    public long currentAnimationTimeMillis() {
        return mAnimationTime;
    }
    public synchronized static float dpToPixel(Context context, float dp) {
        if (sPixelDensity < 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager()
                    .getDefaultDisplay().getMetrics(metrics);
            sPixelDensity =  metrics.density;
        }
        return sPixelDensity * dp;
    }
    public static int dpToPixel(Context context, int dp) {
        return (int)(dpToPixel(context, (float) dp) + .5f);
    }
    public Transformation obtainTransformation() {
        if (!mFreeTransform.isEmpty()) {
            Transformation t = mFreeTransform.pop();
            t.clear();
            return t;
        }
        return new Transformation();
    }
    public void freeTransformation(Transformation freeTransformation) {
        mFreeTransform.push(freeTransformation);
    }
    public Transformation getTransformation() {
        return mTransformation;
    }
    public Transformation pushTransform() {
        Transformation trans = obtainTransformation();
        trans.set(mTransformation);
        mTransformStack.push(trans);
        return mTransformation;
    }
    public void popTransform() {
        Transformation trans = mTransformStack.pop();
        mTransformation.set(trans);
        freeTransformation(trans);
    }
    public void runInGLThread(Runnable runnable) {
        if (Thread.currentThread() == mGLThread) {
            runnable.run();
        } else {
            queueEvent(runnable);
        }
    }
    private void initialize() {
        mFlags |= FLAG_INITIALIZED;
        setEGLConfigChooser(8, 8, 8, 8, 0, 4);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        setRenderer(this);
        mVertexBuffer = ByteBuffer
                .allocateDirect(VERTEX_BUFFER_SIZE * Float.SIZE / Byte.SIZE)
                .order(ByteOrder.nativeOrder());
        mVertexBuffer.asFloatBuffer()
                .put(new float[] {0, 0, 1, 0, 0, 1, 1, 1})
                .position(0);
        mTexCoordBuffer = ByteBuffer
                .allocateDirect(VERTEX_BUFFER_SIZE * Float.SIZE / Byte.SIZE)
                .order(ByteOrder.nativeOrder());
    }
    public void setContentPane(GLView content) {
        mContentView = content;
        content.onAttachToRoot(this);
        content.onAddToParent(null);
        requestLayoutContentPane();
    }
    public GLView getContentPane() {
        return mContentView;
    }
    void handleLowMemory() {
    }
    public synchronized void requestLayoutContentPane() {
        if (mContentView == null || (mFlags & FLAG_NEED_LAYOUT) != 0) return;
        if ((mFlags & FLAG_INITIALIZED) == 0) return;
        mFlags |= FLAG_NEED_LAYOUT;
        requestRender();
    }
    private synchronized void layoutContentPane() {
        mFlags &= ~FLAG_NEED_LAYOUT;
        int width = getWidth();
        int height = getHeight();
        Log.v(TAG, "layout content pane " + width + "x" + height);
        if (mContentView != null && width != 0 && height != 0) {
            mContentView.layout(0, 0, width, height);
        }
    }
    @Override
    protected void onLayout(
            boolean changed, int left, int top, int right, int bottom) {
        if (changed) requestLayoutContentPane();
    }
    public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
        GL11 gl = (GL11) gl1;
        if (mGL != null) {
            Log.i(TAG, "GLObject has changed from " + mGL + " to " + gl);
        }
        mGL = gl;
        if (!ENABLE_FPS_TEST) {
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        } else {
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        }
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
        mGLThread = Thread.currentThread();
        gl.glDisable(GL11.GL_LIGHTING);
        gl.glEnable(GL11.GL_BLEND);
        gl.glEnable(GL11.GL_SCISSOR_TEST);
        gl.glEnable(GL11.GL_STENCIL_TEST);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnable(GL11.GL_TEXTURE_2D);
        gl.glTexEnvf(GL11.GL_TEXTURE_ENV,
                GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
        gl.glClearColor(0f, 0f, 0f, 0f);
        gl.glClearStencil(0);
        gl.glVertexPointer(2, GL11.GL_FLOAT, 0, mVertexBuffer);
        gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, mTexCoordBuffer);
    }
    public void onSurfaceChanged(GL10 gl1, int width, int height) {
        Log.v(TAG, "onSurfaceChanged: " + width + "x" + height
                + ", gl10: " + gl1.toString());
        GL11 gl = (GL11) gl1;
        mGL = gl;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluOrtho2D(gl, 0, width, 0, height);
        Matrix matrix = mTransformation.getMatrix();
        matrix.reset();
        matrix.preTranslate(0, getHeight());
        matrix.preScale(1, -1);
    }
    private void setAlphaValue(float alpha) {
        if (mLastAlpha == alpha) return;
        GL11 gl = mGL;
        mLastAlpha = alpha;
        if (alpha >= 0.95f) {
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV,
                    GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
        } else {
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV,
                    GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            gl.glColor4f(alpha, alpha, alpha, alpha);
        }
    }
    public void drawRect(int x, int y, int width, int height) {
        float matrix[] = mMatrixValues;
        mTransformation.getMatrix().getValues(matrix);
        drawRect(x, y, width, height, matrix, mTransformation.getAlpha());
    }
    private void drawRect(
            int x, int y, int width, int height, float matrix[], float alpha) {
        GL11 gl = mGL;
        gl.glPushMatrix();
        setAlphaValue(alpha);
        gl.glMultMatrixf(toGLMatrix(matrix), 0);
        gl.glTranslatef(x, y, 0);
        gl.glScalef(width, height, 1);
        gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        gl.glPopMatrix();
    }
    public void drawRect(int x, int y, int width, int height, float alpha) {
        float matrix[] = mMatrixValues;
        mTransformation.getMatrix().getValues(matrix);
        drawRect(x, y, width, height, matrix, alpha);
    }
    private float[] mapPoints(Matrix matrix, int x1, int y1, int x2, int y2) {
        float[] point = mPointBuffer;
        point[0] = x1; point[1] = y1; point[2] = x2; point[3] = y2;
        matrix.mapPoints(point);
        return point;
    }
    public void clipRect(int x, int y, int width, int height) {
        float point[] = mapPoints(
                mTransformation.getMatrix(), x, y + height, x + width, y);
        if (point[0] > point[2]) {
            x = (int) point[2];
            width = (int) point[0] - x;
        } else {
            x = (int) point[0];
            width = (int) point[2] - x;
        }
        if (point[1] > point[3]) {
            y = (int) point[3];
            height = (int) point[1] - y;
        } else {
            y = (int) point[1];
            height = (int) point[3] - y;
        }
        mGL.glScissor(x, y, width, height);
    }
    public void clearClip() {
        mGL.glScissor(0, 0, getWidth(), getHeight());
    }
    private static float[] toGLMatrix(float v[]) {
        v[15] = v[8]; v[13] = v[5]; v[5] = v[4]; v[4] = v[1];
        v[12] = v[2]; v[1] = v[3]; v[3] = v[6];
        v[2] = v[6] = v[8] = v[9] = 0;
        v[10] = 1;
        return v;
    }
    public void drawTexture(
            Texture texture, int x, int y, int width, int height, float alpha) {
        if (!texture.bind(this, mGL)) {
            throw new RuntimeException("cannot bind" + texture.toString());
        }
        if (width <= 0 || height <= 0) return ;
        Matrix matrix = mTransformation.getMatrix();
        matrix.getValues(mMatrixValues);
        if (isMatrixRotatedOrFlipped(mMatrixValues)) {
            texture.getTextureCoords(mCoordBuffer, 0);
            mTexCoordBuffer.asFloatBuffer().put(mCoordBuffer).position(0);
            drawRect(x, y, width, height, mMatrixValues, alpha);
        } else {
            float points[] = mapPoints(matrix, x, y + height, x + width, y);
            x = (int) points[0];
            y = (int) points[1];
            width = (int) points[2] - x;
            height = (int) points[3] - y;
            if (width > 0 && height > 0) {
                setAlphaValue(alpha);
                ((GL11Ext) mGL).glDrawTexiOES(x, y, 0, width, height);
            }
        }
    }
    private static boolean isMatrixRotatedOrFlipped(float matrix[]) {
        return matrix[Matrix.MSKEW_X] != 0 || matrix[Matrix.MSKEW_Y] != 0
                || matrix[Matrix.MSCALE_X] < 0 || matrix[Matrix.MSCALE_Y] > 0;
    }
    public void drawTexture(
            Texture texture, int x, int y, int width, int height) {
        drawTexture(texture, x, y, width, height, mTransformation.getAlpha());
    }
    public void onDrawFrame(GL10 gl) {
        if (ENABLE_FPS_TEST) {
            long now = System.nanoTime();
            if (mFrameCountingStart == 0) {
                mFrameCountingStart = now;
            } else if ((now - mFrameCountingStart) > 1000000000) {
                Log.v(TAG, "fps: " + (double) mFrameCount
                        * 1000000000 / (now - mFrameCountingStart));
                mFrameCountingStart = now;
                mFrameCount = 0;
            }
            ++mFrameCount;
        }
        if ((mFlags & FLAG_NEED_LAYOUT) != 0) layoutContentPane();
        clearClip();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_STENCIL_BUFFER_BIT);
        gl.glEnable(GL11.GL_BLEND);
        gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        mAnimationTime = SystemClock.uptimeMillis();
        if (mContentView != null) {
            mContentView.render(GLRootView.this, (GL11) gl);
        }
        long now = SystemClock.uptimeMillis();
        for (Animation animation : mAnimations) {
            animation.setStartTime(now);
        }
        mAnimations.clear();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mIsQueueActive) return false;
        FutureTask<Boolean> task = new FutureTask<Boolean>(
                new TouchEventHandler(event));
        queueEventOrThrowException(task);
        try {
            return task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private class TouchEventHandler implements Callable<Boolean> {
        private final MotionEvent mEvent;
        public TouchEventHandler(MotionEvent event) {
            mEvent = event;
        }
        public Boolean call() throws Exception {
            if (mContentView == null) return false;
            return mContentView.dispatchTouchEvent(mEvent);
        }
    }
    public DisplayMetrics getDisplayMetrics() {
        if (mDisplayMetrics == null) {
            mDisplayMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager()
                    .getDefaultDisplay().getMetrics(mDisplayMetrics);
        }
        return mDisplayMetrics;
    }
    public void copyTexture2D(
            RawTexture texture, int x, int y, int width, int height)
            throws GLOutOfMemoryException {
        Matrix matrix = mTransformation.getMatrix();
        matrix.getValues(mMatrixValues);
        if (isMatrixRotatedOrFlipped(mMatrixValues)) {
            throw new IllegalArgumentException("cannot support rotated matrix");
        }
        float points[] = mapPoints(matrix, x, y + height, x + width, y);
        x = (int) points[0];
        y = (int) points[1];
        width = (int) points[2] - x;
        height = (int) points[3] - y;
        GL11 gl = mGL;
        int newWidth = Util.nextPowerOf2(width);
        int newHeight = Util.nextPowerOf2(height);
        int glError = GL11.GL_NO_ERROR;
        gl.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
        int[] cropRect = {0,  0, width, height};
        gl.glTexParameteriv(GL11.GL_TEXTURE_2D,
                GL11Ext.GL_TEXTURE_CROP_RECT_OES, cropRect, 0);
        gl.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        gl.glTexParameterf(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        gl.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0,
                GL11.GL_RGBA, x, y, newWidth, newHeight, 0);
        glError = gl.glGetError();
        if (glError == GL11.GL_OUT_OF_MEMORY) {
            throw new GLOutOfMemoryException();
        }
        if (glError != GL11.GL_NO_ERROR) {
            throw new RuntimeException(
                    "Texture copy fail, glError " + glError);
        }
        texture.setSize(width, height);
        texture.setTexCoordSize(
                (float) width / newWidth, (float) height / newHeight);
    }
    public synchronized void queueEventOrThrowException(Runnable runnable) {
        if (!mIsQueueActive) {
            throw new IllegalStateException("GLThread has exit");
        }
        super.queueEvent(runnable);
    }
    @Override
    protected void onDetachedFromWindow() {
        final ConditionVariable var = new ConditionVariable();
        synchronized (this) {
            mIsQueueActive = false;
            queueEvent(new Runnable() {
                public void run() {
                    var.open();
                }
            });
        }
        var.block();
        super.onDetachedFromWindow();
    }
    protected Looper getTimerLooper() {
        return mLooper;
    }
}
