@TestTargetClass(Canvas.class)
public class CanvasTest extends InstrumentationTestCase {
    private final static int PAINT_COLOR = 0xff00ff00;
    private final static int BITMAP_WIDTH = 10;
    private final static int BITMAP_HEIGHT = 28;
    private final static int FLOAT_ARRAY_LEN = 9;
    private final Rect mRect = new Rect(0, 0, 10, 31);
    private final RectF mRectF = new RectF(0, 0, 10, 31);
    private final float[] values1 = {
            1, 2, 3, 4, 5, 6, 7, 8, 9
    };
    private final float[] values2 = {
            9, 8, 7, 6, 5, 4, 3, 2, 1
    };
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mImmutableBitmap;
    private Bitmap mMutableBitmap;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPaint = new Paint();
        mPaint.setColor(PAINT_COLOR);
        final Resources res = getInstrumentation().getTargetContext().getResources();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inScaled = false; 
        mImmutableBitmap = BitmapFactory.decodeResource(res, R.drawable.start, opt);
        assertFalse(mImmutableBitmap.isMutable());
        mMutableBitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        mCanvas = new Canvas(mMutableBitmap);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Canvas",
        args = {}
    )
    public void testCanvas1() {
        final Canvas c = new Canvas();
        assertNull(c.getGL());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Canvas",
        args = {android.graphics.Bitmap.class}
    )
    @ToBeFixed(bug="1839977", explanation="These two abnormal case will crash the process")
    public void testCanvas2() {
        try {
            new Canvas(mImmutableBitmap);
            fail("should throw out IllegalStateException when creating Canvas with an ImmutableBitmap");
        } catch (IllegalStateException e) {
        }
        mMutableBitmap.recycle();
        try {
            new Canvas(mMutableBitmap);
            fail("should throw out RuntimeException when creating Canvas with a"
                     + " MutableBitmap which is recycled");
        } catch (RuntimeException e) {
        }
        mMutableBitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        new Canvas(mMutableBitmap);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Canvas",
            args = {javax.microedition.khronos.opengles.GL.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGL",
            args = {}
        )
    })
    public void testCanvas3() {
        Canvas c = new Canvas();
        assertNull(c.getGL());
        final MyGL myGL = new MyGL();
        c = new Canvas(myGL);
        assertTrue(myGL.equals(c.getGL()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "freeGlCaches",
        args = {}
    )
    public void testFreeGlCaches() {
        Canvas.freeGlCaches();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setBitmap",
        args = {android.graphics.Bitmap.class}
    )
    public void testSetBitmap() {
        try {
            mCanvas.setBitmap(mImmutableBitmap);
            fail("should throw out IllegalStateException when setting an "
                    + "ImmutableBitmap to a Canvas");
        } catch (IllegalStateException e) {
        }
        final Canvas c = new Canvas(new MyGL());
        try {
            c.setBitmap(mMutableBitmap);
            fail("should throw out RuntimeException when setting MutableBitmap to Canvas "
                    + "when the Canvas is created with GL");
        } catch (RuntimeException e) {
        }
        mMutableBitmap.recycle();
        try {
            mCanvas.setBitmap(mMutableBitmap);
            fail("should throw out RuntimeException when setting Bitmap which is recycled"
                          + " to a Canvas");
        } catch (RuntimeException e) {
        }
        mMutableBitmap = Bitmap.createBitmap(BITMAP_WIDTH, 31, Config.ARGB_8888);
        mCanvas.setBitmap(mMutableBitmap);
        assertEquals(BITMAP_WIDTH, mCanvas.getWidth());
        assertEquals(31, mCanvas.getHeight());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setViewport",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHeight",
            args = {}
        )
    })
    public void testSetViewport() {
        assertEquals(BITMAP_WIDTH, mCanvas.getWidth());
        assertEquals(BITMAP_HEIGHT, mCanvas.getHeight());
        mCanvas.setViewport(BITMAP_HEIGHT, BITMAP_WIDTH);
        assertEquals(BITMAP_WIDTH, mCanvas.getWidth());
        assertEquals(BITMAP_HEIGHT, mCanvas.getHeight());
        mCanvas = new Canvas(new MyGL());
        mCanvas.setViewport(BITMAP_HEIGHT, BITMAP_WIDTH);
        assertEquals(BITMAP_HEIGHT, mCanvas.getWidth());
        assertEquals(BITMAP_WIDTH, mCanvas.getHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isOpaque",
        args = {}
    )
    public void testIsOpaque() {
        assertFalse(mCanvas.isOpaque());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "restore",
        args = {}
    )
    public void testRestore() {
        try {
            mCanvas.restore();
            fail("should throw out IllegalStateException because cannot restore Canvas"
                            + " before save");
        } catch (IllegalStateException e) {
        }
        mCanvas.save();
        mCanvas.restore();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "save",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restore",
            args = {}
        )
    })
    public void testSave1() {
        final Matrix m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.save();
        final Matrix m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        final float[] values3 = new float[FLOAT_ARRAY_LEN];
        final Matrix m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        final float[] values4 = new float[FLOAT_ARRAY_LEN];
        final Matrix m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "save",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restore",
            args = {}
        )
    })
    public void testSave2() {
        Matrix m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.save(Canvas.MATRIX_SAVE_FLAG);
        Matrix m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        float[] values3 = new float[FLOAT_ARRAY_LEN];
        Matrix m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        float[] values4 = new float[FLOAT_ARRAY_LEN];
        Matrix m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.save(Canvas.CLIP_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.save(Canvas.ALL_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveLayer",
            args = {android.graphics.RectF.class, android.graphics.Paint.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restore",
            args = {}
        )
    })
    public void testSaveLayer1() {
        final Paint p = new Paint();
        final RectF rF = new RectF(0, 10, 31, 0);
        Matrix m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayer(rF, p, Canvas.MATRIX_SAVE_FLAG);
        Matrix m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        float[] values3 = new float[FLOAT_ARRAY_LEN];
        Matrix m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        float[] values4 = new float[FLOAT_ARRAY_LEN];
        Matrix m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayer(rF, p, Canvas.CLIP_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayer(rF, p, Canvas.ALL_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveLayer",
            args = {float.class, float.class, float.class, float.class,
                    android.graphics.Paint.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restore",
            args = {}
        )
    })
    public void testSaveLayer2() {
        final Paint p = new Paint();
        Matrix m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayer(10, 0, 0, 31, p, Canvas.MATRIX_SAVE_FLAG);
        Matrix m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        float[] values3 = new float[FLOAT_ARRAY_LEN];
        Matrix m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        float[] values4 = new float[FLOAT_ARRAY_LEN];
        Matrix m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayer(10, 0, 0, 31, p, Canvas.CLIP_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayer(10, 0, 0, 31, p, Canvas.ALL_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveLayerAlpha",
            args = {android.graphics.RectF.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restore",
            args = {}
        )
    })
    public void testSaveLayerAlpha1() {
        final RectF rF = new RectF(0, 10, 31, 0);
        Matrix m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayerAlpha(rF, 0xff, Canvas.MATRIX_SAVE_FLAG);
        Matrix m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        float[] values3 = new float[FLOAT_ARRAY_LEN];
        Matrix m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        float[] values4 = new float[FLOAT_ARRAY_LEN];
        Matrix m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayerAlpha(rF, 0xff, Canvas.CLIP_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayerAlpha(rF, 0xff, Canvas.ALL_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveLayerAlpha",
            args = {float.class, float.class, float.class, float.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restore",
            args = {}
        )
    })
    public void testSaveLayerAlpha2() {
        Matrix m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayerAlpha(0, 10, 31, 0, 0xff, Canvas.MATRIX_SAVE_FLAG);
        Matrix m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        float[] values3 = new float[FLOAT_ARRAY_LEN];
        Matrix m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        float[] values4 = new float[FLOAT_ARRAY_LEN];
        Matrix m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayerAlpha(0, 10, 31, 0, 0xff, Canvas.CLIP_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values4[i]);
        }
        m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        mCanvas.saveLayerAlpha(0, 10, 31, 0, 0xff, Canvas.ALL_SAVE_FLAG);
        m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        values3 = new float[FLOAT_ARRAY_LEN];
        m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restore();
        values4 = new float[FLOAT_ARRAY_LEN];
        m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSaveCount",
        args = {}
    )
    public void testGetSaveCount() {
        assertEquals(1, mCanvas.getSaveCount());
        mCanvas.save();
        assertEquals(2, mCanvas.getSaveCount());
        mCanvas.save();
        assertEquals(3, mCanvas.getSaveCount());
        mCanvas.saveLayer(new RectF(), new Paint(), Canvas.ALL_SAVE_FLAG);
        assertEquals(4, mCanvas.getSaveCount());
        mCanvas.saveLayerAlpha(new RectF(), 0, Canvas.ALL_SAVE_FLAG);
        assertEquals(5, mCanvas.getSaveCount());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "restoreToCount",
        args = {int.class}
    )
    public void testRestoreToCount() {
        try {
            mCanvas.restoreToCount(0);
            fail("should throw out IllegalArgumentException because saveCount is less than 1");
        } catch (IllegalArgumentException e) {
        }
        final Matrix m1 = new Matrix();
        m1.setValues(values1);
        mCanvas.setMatrix(m1);
        final int count = mCanvas.save();
        assertTrue(count > 0);
        final Matrix m2 = new Matrix();
        m2.setValues(values2);
        mCanvas.setMatrix(m2);
        final float[] values3 = new float[FLOAT_ARRAY_LEN];
        final Matrix m3 = mCanvas.getMatrix();
        m3.getValues(values3);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values2[i], values3[i]);
        }
        mCanvas.restoreToCount(count);
        final float[] values4 = new float[FLOAT_ARRAY_LEN];
        final Matrix m4 = mCanvas.getMatrix();
        m4.getValues(values4);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(values1[i], values4[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMatrix",
            args = {android.graphics.Matrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMatrix",
            args = {android.graphics.Matrix.class}
        )
    })
    public void testGetMatrix1() {
        final float[] f1 = {
                1, 2, 3, 4, 5, 6, 7, 8, 9
        };
        final Matrix m1 = new Matrix();
        m1.setValues(f1);
        mCanvas.setMatrix(m1);
        final Matrix m2 = new Matrix(m1);
        mCanvas.getMatrix(m2);
        assertTrue(m1.equals(m2));
        final float[] f2 = new float[FLOAT_ARRAY_LEN];
        m2.getValues(f2);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(f1[i], f2[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMatrix",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMatrix",
            args = {android.graphics.Matrix.class}
        )
    })
    public void testGetMatrix2() {
        final float[] f1 = {
                1, 2, 3, 4, 5, 6, 7, 8, 9
        };
        final Matrix m1 = new Matrix();
        m1.setValues(f1);
        mCanvas.setMatrix(m1);
        final Matrix m2 = mCanvas.getMatrix();
        assertTrue(m1.equals(m2));
        final float[] f2 = new float[FLOAT_ARRAY_LEN];
        m2.getValues(f2);
        for (int i = 0; i < FLOAT_ARRAY_LEN; i++) {
            assertEquals(f1[i], f2[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "translate",
        args = {float.class, float.class}
    )
    public void testTranslate() {
        preCompare();
        mCanvas.translate(0.10f, 0.28f);
        final float[] values = new float[FLOAT_ARRAY_LEN];
        mCanvas.getMatrix().getValues(values);
        assertEquals(1.0f, values[0]);
        assertEquals(0.0f, values[1]);
        assertEquals(0.1f, values[2]);
        assertEquals(0.0f, values[3]);
        assertEquals(1.0f, values[4]);
        assertEquals(0.28f, values[5]);
        assertEquals(0.0f, values[6]);
        assertEquals(0.0f, values[7]);
        assertEquals(1.0f, values[8]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "scale",
        args = {float.class, float.class}
    )
    public void testScale1() {
        preCompare();
        mCanvas.scale(0.5f, 0.5f);
        final float[] values = new float[FLOAT_ARRAY_LEN];
        mCanvas.getMatrix().getValues(values);
        assertEquals(0.5f, values[0]);
        assertEquals(0.0f, values[1]);
        assertEquals(0.0f, values[2]);
        assertEquals(0.0f, values[3]);
        assertEquals(0.5f, values[4]);
        assertEquals(0.0f, values[5]);
        assertEquals(0.0f, values[6]);
        assertEquals(0.0f, values[7]);
        assertEquals(1.0f, values[8]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "scale",
        args = {float.class, float.class, float.class, float.class}
    )
    public void testScale2() {
        preCompare();
        mCanvas.scale(3.0f, 3.0f, 1.0f, 1.0f);
        final float[] values = new float[FLOAT_ARRAY_LEN];
        mCanvas.getMatrix().getValues(values);
        assertEquals(3.0f, values[0]);
        assertEquals(0.0f, values[1]);
        assertEquals(-2.0f, values[2]);
        assertEquals(0.0f, values[3]);
        assertEquals(3.0f, values[4]);
        assertEquals(-2.0f, values[5]);
        assertEquals(0.0f, values[6]);
        assertEquals(0.0f, values[7]);
        assertEquals(1.0f, values[8]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "rotate",
        args = {float.class}
    )
    public void testRotate1() {
        preCompare();
        mCanvas.rotate(90);
        final float[] values = new float[FLOAT_ARRAY_LEN];
        mCanvas.getMatrix().getValues(values);
        assertEquals(0.0f, values[0]);
        assertEquals(-1.0f, values[1]);
        assertEquals(0.0f, values[2]);
        assertEquals(1.0f, values[3]);
        assertEquals(0.0f, values[4]);
        assertEquals(0.0f, values[5]);
        assertEquals(0.0f, values[6]);
        assertEquals(0.0f, values[7]);
        assertEquals(1.0f, values[8]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "rotate",
        args = {float.class, float.class, float.class}
    )
    public void testRotate2() {
        preCompare();
        mCanvas.rotate(30, 1.0f, 0.0f);
        final float[] values = new float[FLOAT_ARRAY_LEN];
        mCanvas.getMatrix().getValues(values);
        assertEquals(0.8660254f, values[0]);
        assertEquals(-0.5f, values[1]);
        assertEquals(0.13397461f, values[2]);
        assertEquals(0.5f, values[3]);
        assertEquals(0.8660254f, values[4]);
        assertEquals(-0.5f, values[5]);
        assertEquals(0.0f, values[6]);
        assertEquals(0.0f, values[7]);
        assertEquals(1.0f, values[8]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "skew",
        args = {float.class, float.class}
    )
    public void testSkew() {
        preCompare();
        mCanvas.skew(1.0f, 3.0f);
        final float[] values = new float[FLOAT_ARRAY_LEN];
        mCanvas.getMatrix().getValues(values);
        assertEquals(1.0f, values[0]);
        assertEquals(1.0f, values[1]);
        assertEquals(0.0f, values[2]);
        assertEquals(3.0f, values[3]);
        assertEquals(1.0f, values[4]);
        assertEquals(0.0f, values[5]);
        assertEquals(0.0f, values[6]);
        assertEquals(0.0f, values[7]);
        assertEquals(1.0f, values[8]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "concat",
        args = {android.graphics.Matrix.class}
    )
    public void testConcat() {
        preCompare();
        final Matrix m = new Matrix();
        final float[] values = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        m.setValues(values);
        mCanvas.concat(m);
        mCanvas.getMatrix().getValues(values);
        assertEquals(0.0f, values[0]);
        assertEquals(1.0f, values[1]);
        assertEquals(2.0f, values[2]);
        assertEquals(3.0f, values[3]);
        assertEquals(4.0f, values[4]);
        assertEquals(5.0f, values[5]);
        assertEquals(6.0f, values[6]);
        assertEquals(7.0f, values[7]);
        assertEquals(8.0f, values[8]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRect",
        args = {android.graphics.RectF.class, android.graphics.Region.Op.class}
    )
    public void testClipRect1() {
        assertFalse(mCanvas.clipRect(mRectF, Op.DIFFERENCE));
        assertFalse(mCanvas.clipRect(mRectF, Op.INTERSECT));
        assertTrue(mCanvas.clipRect(mRectF, Op.REPLACE));
        assertFalse(mCanvas.clipRect(mRectF, Op.REVERSE_DIFFERENCE));
        assertTrue(mCanvas.clipRect(mRectF, Op.UNION));
        assertFalse(mCanvas.clipRect(mRectF, Op.XOR));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRect",
        args = {android.graphics.Rect.class, android.graphics.Region.Op.class}
    )
    public void testClipRect2() {
        assertFalse(mCanvas.clipRect(mRect, Op.DIFFERENCE));
        assertFalse(mCanvas.clipRect(mRect, Op.INTERSECT));
        assertTrue(mCanvas.clipRect(mRect, Op.REPLACE));
        assertFalse(mCanvas.clipRect(mRect, Op.REVERSE_DIFFERENCE));
        assertTrue(mCanvas.clipRect(mRect, Op.UNION));
        assertFalse(mCanvas.clipRect(mRect, Op.XOR));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRect",
        args = {android.graphics.RectF.class}
    )
    public void testClipRect3() {
        assertTrue(mCanvas.clipRect(mRectF));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRect",
        args = {android.graphics.Rect.class}
    )
    public void testClipRect4() {
        assertTrue(mCanvas.clipRect(mRect));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRect",
        args = {float.class, float.class, float.class, float.class,
                android.graphics.Region.Op.class}
    )
    public void testClipRect5() {
        assertFalse(mCanvas.clipRect(0, 0, 10, 31, Op.DIFFERENCE));
        assertFalse(mCanvas.clipRect(0, 0, 10, 31, Op.INTERSECT));
        assertTrue(mCanvas.clipRect(0, 0, 10, 31, Op.REPLACE));
        assertFalse(mCanvas.clipRect(0, 0, 10, 31, Op.REVERSE_DIFFERENCE));
        assertTrue(mCanvas.clipRect(0, 0, 10, 31, Op.UNION));
        assertFalse(mCanvas.clipRect(0, 0, 10, 31, Op.XOR));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRect",
        args = {float.class, float.class, float.class, float.class}
    )
    public void testClipRect6() {
        assertTrue(mCanvas.clipRect(0, 0, 10, 31));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRect",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testClipRect7() {
        assertTrue(mCanvas.clipRect(0, 0, 10, 31));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipPath",
        args = {android.graphics.Path.class}
    )
    public void testClipPath1() {
        final Path p = new Path();
        p.addRect(mRectF, Direction.CCW);
        assertTrue(mCanvas.clipPath(p));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipPath",
        args = {android.graphics.Path.class, android.graphics.Region.Op.class}
    )
    public void testClipPath2() {
        final Path p = new Path();
        p.addRect(mRectF, Direction.CCW);
        assertFalse(mCanvas.clipPath(p, Op.DIFFERENCE));
        assertFalse(mCanvas.clipPath(p, Op.INTERSECT));
        assertTrue(mCanvas.clipPath(p, Op.REPLACE));
        assertFalse(mCanvas.clipPath(p, Op.REVERSE_DIFFERENCE));
        assertTrue(mCanvas.clipPath(p, Op.UNION));
        assertFalse(mCanvas.clipPath(p, Op.XOR));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRegion",
        args = {android.graphics.Region.class}
    )
    public void testClipRegion1() {
        assertFalse(mCanvas.clipRegion(new Region(0, 10, 29, 0)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clipRegion",
        args = {android.graphics.Region.class, android.graphics.Region.Op.class}
    )
    public void testClipRegion2() {
        final Region r = new Region(0, 10, 29, 0);
        assertTrue(mCanvas.clipRegion(r, Op.DIFFERENCE));
        assertFalse(mCanvas.clipRegion(r, Op.INTERSECT));
        assertFalse(mCanvas.clipRegion(r, Op.REPLACE));
        assertFalse(mCanvas.clipRegion(r, Op.REVERSE_DIFFERENCE));
        assertFalse(mCanvas.clipRegion(r, Op.UNION));
        assertFalse(mCanvas.clipRegion(r, Op.XOR));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawFilter",
            args = {android.graphics.DrawFilter.class}
        )
    })
    public void testGetDrawFilter() {
        assertNull(mCanvas.getDrawFilter());
        final DrawFilter dF = new DrawFilter();
        mCanvas.setDrawFilter(dF);
        assertTrue(dF.equals(mCanvas.getDrawFilter()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "quickReject",
        args = {android.graphics.RectF.class, android.graphics.Canvas.EdgeType.class}
    )
    public void testQuickReject1() {
        assertFalse(mCanvas.quickReject(mRectF, EdgeType.AA));
        assertFalse(mCanvas.quickReject(mRectF, EdgeType.BW));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "quickReject",
        args = {android.graphics.Path.class, android.graphics.Canvas.EdgeType.class}
    )
    public void testQuickReject2() {
        final Path p = new Path();
        p.addRect(mRectF, Direction.CCW);
        assertFalse(mCanvas.quickReject(p, EdgeType.AA));
        assertFalse(mCanvas.quickReject(p, EdgeType.BW));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "quickReject",
        args = {float.class, float.class, float.class, float.class,
                android.graphics.Canvas.EdgeType.class}
    )
    public void testQuickReject3() {
        assertFalse(mCanvas.quickReject(0, 0, 10, 31, EdgeType.AA));
        assertFalse(mCanvas.quickReject(0, 0, 10, 31, EdgeType.BW));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getClipBounds",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "1488979", explanation = "the width and height returned are error")
    public void testGetClipBounds1() {
        final Rect r = new Rect();
        assertTrue(mCanvas.getClipBounds(r));
        assertEquals(BITMAP_WIDTH, r.width());
        assertEquals(BITMAP_HEIGHT, r.height());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getClipBounds",
        args = {}
    )
    @ToBeFixed(bug = "1488979", explanation = "the width and height returned are error")
    public void testGetClipBounds2() {
        final Rect r = mCanvas.getClipBounds();
        assertEquals(BITMAP_WIDTH, r.width());
        assertEquals(BITMAP_HEIGHT, r.height());
    }
    private void checkDrewColor(int color) {
        assertEquals(color, mMutableBitmap.getPixel(0, 0));
        assertEquals(color, mMutableBitmap.getPixel(BITMAP_WIDTH / 2, BITMAP_HEIGHT / 2));
        assertEquals(color, mMutableBitmap.getPixel(BITMAP_WIDTH - 1, BITMAP_HEIGHT - 1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawRGB",
        args = {int.class, int.class, int.class}
    )
    public void testDrawRGB() {
        final int alpha = 0xff;
        final int red = 0xff;
        final int green = 0xff;
        final int blue = 0xff;
        mCanvas.drawRGB(red, green, blue);
        final int color = alpha << 24 | red << 16 | green << 8 | blue;
        checkDrewColor(color);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawARGB",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testDrawARGB() {
        final int alpha = 0xff;
        final int red = 0x22;
        final int green = 0x33;
        final int blue = 0x44;
        mCanvas.drawARGB(alpha, red, green, blue);
        final int color = alpha << 24 | red << 16 | green << 8 | blue;
        checkDrewColor(color);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawColor",
        args = {int.class}
    )
    public void testDrawColor1() {
        final int color = 0xffff0000;
        mCanvas.drawColor(color);
        checkDrewColor(color);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawColor",
        args = {int.class, android.graphics.PorterDuff.Mode.class}
    )
    public void testDrawColor2() {
        mCanvas.drawColor(0xffff0000, Mode.CLEAR);
        mCanvas.drawColor(0xffff0000, Mode.DARKEN);
        mCanvas.drawColor(0xffff0000, Mode.DST);
        mCanvas.drawColor(0xffff0000, Mode.DST_ATOP);
        mCanvas.drawColor(0xffff0000, Mode.DST_IN);
        mCanvas.drawColor(0xffff0000, Mode.DST_OUT);
        mCanvas.drawColor(0xffff0000, Mode.DST_OVER);
        mCanvas.drawColor(0xffff0000, Mode.LIGHTEN);
        mCanvas.drawColor(0xffff0000, Mode.MULTIPLY);
        mCanvas.drawColor(0xffff0000, Mode.SCREEN);
        mCanvas.drawColor(0xffff0000, Mode.SRC);
        mCanvas.drawColor(0xffff0000, Mode.SRC_ATOP);
        mCanvas.drawColor(0xffff0000, Mode.SRC_IN);
        mCanvas.drawColor(0xffff0000, Mode.SRC_OUT);
        mCanvas.drawColor(0xffff0000, Mode.SRC_OVER);
        mCanvas.drawColor(0xffff0000, Mode.XOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPaint",
        args = {android.graphics.Paint.class}
    )
    public void testDrawPaint() {
        mCanvas.drawPaint(mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPoints",
        args = {float[].class, int.class, int.class, android.graphics.Paint.class}
    )
    public void testDrawPoints1() {
        try {
            mCanvas.drawPoints(new float[] {
                    10.0f, 29.0f
            }, -1, 2, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because of invalid offset");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawPoints(new float[] {
                    10.0f, 29.0f
            }, 0, 31, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because of invalid count");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        mCanvas.drawPoints(new float[] {
                0, 0
        }, 0, 2, mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPoints",
        args = {float[].class, android.graphics.Paint.class}
    )
    public void testDrawPoints2() {
        mCanvas.drawPoints(new float[]{0, 0}, mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPoint",
        args = {float.class, float.class, android.graphics.Paint.class}
    )
    public void testDrawPoint() {
        mCanvas.drawPoint(0, 0, mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawLine",
        args = {float.class, float.class, float.class, float.class, android.graphics.Paint.class}
    )
    public void testDrawLine() {
        mCanvas.drawLine(0, 0, 10, 12, mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawLines",
        args = {float[].class, int.class, int.class, android.graphics.Paint.class}
    )
    public void testDrawLines1() {
        try {
            mCanvas.drawLines(new float[] {
                    0, 0, 10, 31
            }, 2, 4, new Paint());
            fail("should throw out ArrayIndexOutOfBoundsException because of invalid offset");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawLines(new float[] {
                    0, 0, 10, 31
            }, 0, 8, new Paint());
            fail("should throw out ArrayIndexOutOfBoundsException because of invalid count");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        mCanvas.drawLines(new float[] {
                0, 0, 10, 12
        }, 0, 4, mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawLines",
        args = {float[].class, android.graphics.Paint.class}
    )
    public void testDrawLines2() {
        mCanvas.drawLines(new float[] {
                0, 0, 10, 12
        }, mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
    }
    private void checkDrewPaint() {
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(0, 0));
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(5, 6));
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(9, 11));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawRect",
        args = {android.graphics.RectF.class, android.graphics.Paint.class}
    )
    public void testDrawRect1() {
        mCanvas.drawRect(new RectF(0, 0, 10, 12), mPaint);
        checkDrewPaint();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawRect",
        args = {android.graphics.Rect.class, android.graphics.Paint.class}
    )
    public void testDrawRect2() {
        mCanvas.drawRect(new Rect(0, 0, 10, 12), mPaint);
        checkDrewPaint();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawRect",
        args = {float.class, float.class, float.class, float.class, android.graphics.Paint.class}
    )
    public void testDrawRect3() {
        mCanvas.drawRect(0, 0, 10, 12, mPaint);
        checkDrewPaint();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawOval",
        args = {android.graphics.RectF.class, android.graphics.Paint.class}
    )
    public void testDrawOval() {
        try {
            mCanvas.drawOval(null, mPaint);
            fail("should throw out NullPointerException because oval is null");
        } catch (NullPointerException e) {
        }
        mCanvas.drawOval(new RectF(0, 0, 10, 12), mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawCircle",
        args = {float.class, float.class, float.class, android.graphics.Paint.class}
    )
    public void testDrawCircle() {
        mCanvas.drawCircle(10.0f, 10.0f, -1.0f, mPaint);
        mCanvas.drawCircle(10, 12, 3, mPaint);
        assertEquals(PAINT_COLOR, mMutableBitmap.getPixel(9, 11));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawArc",
        args = {android.graphics.RectF.class, float.class, float.class, boolean.class,
                android.graphics.Paint.class}
    )
    public void testDrawArc() {
        try {
            mCanvas.drawArc(null, 10.0f, 29.0f, true, mPaint);
            fail("should throw NullPointerException because oval is null");
        } catch (NullPointerException e) {
        }
        mCanvas.drawArc(new RectF(0, 0, 10, 12), 10, 11, false, mPaint);
        mCanvas.drawArc(new RectF(0, 0, 10, 12), 10, 11, true, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawRoundRect",
        args = {android.graphics.RectF.class, float.class, float.class,
                android.graphics.Paint.class}
    )
    public void testDrawRoundRect() {
        try {
            mCanvas.drawRoundRect(null, 10.0f, 29.0f, mPaint);
            fail("should throw out NullPointerException because RoundRect is null");
        } catch (NullPointerException e) {
        }
        mCanvas.drawRoundRect(new RectF(0, 0, 10, 12), 8, 8, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPath",
        args = {android.graphics.Path.class, android.graphics.Paint.class}
    )
    public void testDrawPath() {
        mCanvas.drawPath(new Path(), mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawBitmap",
        args = {android.graphics.Bitmap.class, float.class, float.class,
                android.graphics.Paint.class}
    )
    public void testDrawBitmap1() {
        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, 29, Config.ARGB_8888);
        b.recycle();
        try {
            mCanvas.drawBitmap(b, 10.0f, 29.0f, mPaint);
            fail("should throw out RuntimeException because bitmap has been recycled");
        } catch (RuntimeException e) {
        }
        b = Bitmap.createBitmap(BITMAP_WIDTH, 12, Config.ARGB_8888);
        mCanvas.drawBitmap(b, 10, 12, null);
        mCanvas.drawBitmap(b, 5, 12, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawBitmap",
        args = {android.graphics.Bitmap.class, android.graphics.Rect.class,
                android.graphics.RectF.class, android.graphics.Paint.class}
    )
    public void testDrawBitmap2() {
        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, 29, Config.ARGB_8888);
        b.recycle();
        try {
            mCanvas.drawBitmap(b, null, new RectF(), mPaint);
            fail("should throw out RuntimeException because bitmap has been recycled");
        } catch (RuntimeException e) {
        }
        b = Bitmap.createBitmap(BITMAP_WIDTH, 29, Config.ARGB_8888);
        mCanvas.drawBitmap(b, new Rect(), new RectF(), null);
        mCanvas.drawBitmap(b, new Rect(), new RectF(), mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawBitmap",
        args = {android.graphics.Bitmap.class, android.graphics.Rect.class,
                android.graphics.Rect.class, android.graphics.Paint.class}
    )
    public void testDrawBitmap3() {
        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, 29, Config.ARGB_8888);
        b.recycle();
        try {
            mCanvas.drawBitmap(b, null, new Rect(), mPaint);
            fail("should throw out RuntimeException because bitmap has been recycled");
        } catch (RuntimeException e) {
        }
        b = Bitmap.createBitmap(BITMAP_WIDTH, 29, Config.ARGB_8888);
        mCanvas.drawBitmap(b, new Rect(), new Rect(), null);
        mCanvas.drawBitmap(b, new Rect(), new Rect(), mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawBitmap",
        args = {int[].class, int.class, int.class, int.class, int.class, int.class,
                int.class, boolean.class, android.graphics.Paint.class}
    )
    public void testDrawBitmap4() {
        final int[] colors = new int[2008];
        try {
            mCanvas.drawBitmap(colors, 10, 10, 10, 10, -1, 10, true, null);
            fail("should throw out IllegalArgumentException because width is less than 0");
        } catch (IllegalArgumentException e) {
        }
        try {
            mCanvas.drawBitmap(colors, 10, 10, 10, 10, 10, -1, true, null);
            fail("should throw out IllegalArgumentException because height is less than 0");
        } catch (IllegalArgumentException e) {
        }
        try {
            mCanvas.drawBitmap(colors, 10, 5, 10, 10, 10, 10, true, null);
            fail("should throw out IllegalArgumentException because stride less than width and"
                            + " bigger than -width");
        } catch (IllegalArgumentException e) {
        }
        try {
            mCanvas.drawBitmap(colors, -1, 10, 10, 10, 10, 10, true, null);
            fail("should throw out ArrayIndexOutOfBoundsException because offset less than 0");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawBitmap(new int[29], 10, 29, 10, 10, 20, 10, true, null);
            fail("should throw out ArrayIndexOutOfBoundsException because sum of offset and width"
                            + " is bigger than colors' length");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        mCanvas.drawBitmap(colors, 10, 10, 10, 10, 0, 10, true, null);
        mCanvas.drawBitmap(colors, 10, 10, 10, 10, 10, 0, true, null);
        mCanvas.drawBitmap(colors, 10, 10, 10, 10, 10, 29, true, null);
        mCanvas.drawBitmap(colors, 10, 10, 10, 10, 10, 29, true, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawBitmap",
        args = {int[].class, int.class, int.class, float.class, float.class, int.class,
                int.class, boolean.class, android.graphics.Paint.class}
    )
    public void testDrawBitmap6() {
        final int[] colors = new int[2008];
        try {
            mCanvas.drawBitmap(colors, 10, 10, 10.0f, 10.0f, -1, 10, true, null);
            fail("should throw out IllegalArgumentException because width is less than 0");
        } catch (IllegalArgumentException e) {
        }
        try {
            mCanvas.drawBitmap(colors, 10, 10, 10.0f, 10.0f, 10, -1, true, null);
            fail("should throw out IllegalArgumentException because height is less than 0");
        } catch (IllegalArgumentException e) {
        }
        try {
            mCanvas.drawBitmap(colors, 10, 5, 10.0f, 10.0f, 10, 10, true, null);
            fail("should throw out IllegalArgumentException because stride is less than width "
                                + "and bigger than -width");
        } catch (IllegalArgumentException e) {
        }
        try {
            mCanvas.drawBitmap(colors, -1, 10, 10.0f, 10.0f, 10, 10, true, null);
            fail("should throw out IllegalArgumentException because offset is less than 0");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawBitmap(new int[29], 10, 29, 10.0f, 10.0f, 20, 10, true, null);
            fail("should throw out ArrayIndexOutOfBoundsException because sum of offset and width"
                            + " is bigger than colors' length");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        mCanvas.drawBitmap(colors, 10, 10, 10.0f, 10.0f, 0, 10, true, null);
        mCanvas.drawBitmap(colors, 10, 10, 10.0f, 10.0f, 10, 0, true, null);
        mCanvas.drawBitmap(colors, 10, 10, 10.0f, 10.0f, 10, 29, true, null);
        mCanvas.drawBitmap(colors, 10, 10, 10.0f, 10.0f, 10, 29, true, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawBitmap",
        args = {android.graphics.Bitmap.class, android.graphics.Matrix.class,
                android.graphics.Paint.class}
    )
    public void testDrawBitmap5() {
        final Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, 29, Config.ARGB_8888);
        mCanvas.drawBitmap(b, new Matrix(), null);
        mCanvas.drawBitmap(b, new Matrix(), mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawBitmapMesh",
        args = {android.graphics.Bitmap.class, int.class, int.class, float[].class,
                int.class, int[].class, int.class, android.graphics.Paint.class}
    )
    public void testDrawBitmapMesh() {
        final Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, 29, Config.ARGB_8888);
        try {
            mCanvas.drawBitmapMesh(b, -1, 10, null, 0, null, 0, null);
            fail("should throw out ArrayIndexOutOfBoundsException because meshWidth less than 0");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawBitmapMesh(b, 10, -1, null, 0, null, 0, null);
            fail("should throw out ArrayIndexOutOfBoundsException because meshHeight "
                                    + "is less than 0");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawBitmapMesh(b, 10, 10, null, -1, null, 0, null);
            fail("should throw out ArrayIndexOutOfBoundsException because vertOffset "
                                                + "is less than 0");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawBitmapMesh(b, 10, 10, null, 10, null, -1, null);
            fail("should throw out ArrayIndexOutOfBoundsException because colorOffset is"
                                    + " less than 0");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        mCanvas.drawBitmapMesh(b, 0, 10, null, 10, null, 10, null);
        mCanvas.drawBitmapMesh(b, 10, 0, null, 10, null, 10, null);
        try {
            mCanvas.drawBitmapMesh(b, 10, 10, new float[] {
                    10.0f, 29.0f
            }, 10, null, 10, null);
            fail("should throw out ArrayIndexOutOfBoundsException because verts' length"
                                    + " is too short");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        final float[] verts = new float[2008];
        try {
            mCanvas.drawBitmapMesh(b, 10, 10, verts, 10, new int[] {
                    10, 29
            }, 10, null);
            fail("should throw out ArrayIndexOutOfBoundsException because colors' "
                        + "length is too short");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        final int[] colors = new int[2008];
        mCanvas.drawBitmapMesh(b, 10, 10, verts, 10, colors, 10, null);
        mCanvas.drawBitmapMesh(b, 10, 10, verts, 10, colors, 10, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawVertices",
        args = {android.graphics.Canvas.VertexMode.class, int.class, float[].class,
                int.class, float[].class, int.class, int[].class, int.class, short[].class,
                int.class, int.class, android.graphics.Paint.class}
    )
    public void testDrawVertices() {
        final float[] verts = new float[10];
        final float[] texs = new float[10];
        final int[] colors = new int[10];
        final short[] indices = {
                0, 1, 2, 3, 4, 1
        };
        try {
            mCanvas.drawVertices(VertexMode.TRIANGLES, 10, verts, 8, texs, 0, colors, 0, indices,
                    0, 4, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because sum of vertOffset and"
                            + " vertexCount is bigger than verts' length");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawVertices(VertexMode.TRIANGLES, 10, verts, 0, texs, 30, colors, 0, indices,
                    0, 4, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because sum of texOffset and"
                                    + " vertexCount is bigger thatn texs' length");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawVertices(VertexMode.TRIANGLES, 10, verts, 0, texs, 0, colors, 30, indices,
                    0, 4, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because sum of colorOffset and"
                                + " vertexCount is bigger than colors' length");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawVertices(VertexMode.TRIANGLES, 10, verts, 0, texs, 0, colors, 0, indices,
                    10, 30, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because sum of indexOffset and"
                            + " indexCount is bigger than indices' length");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        mCanvas.drawVertices(VertexMode.TRIANGLES, 0, verts, 0, null, 0, colors, 0, indices, 0, 0,
                mPaint);
        mCanvas.drawVertices(VertexMode.TRIANGLE_STRIP, 0, verts, 0, null, 0, null, 0, indices, 0,
                0, mPaint);
        mCanvas.drawVertices(VertexMode.TRIANGLE_FAN, 0, verts, 0, null, 0, null, 0, null, 0, 0,
                mPaint);
        mCanvas.drawVertices(VertexMode.TRIANGLES, 10, verts, 0, texs, 0, colors, 0, indices, 0, 6,
                mPaint);
        mCanvas.drawVertices(VertexMode.TRIANGLE_STRIP, 10, verts, 0, texs, 0, colors, 0, indices,
                0, 6, mPaint);
        mCanvas.drawVertices(VertexMode.TRIANGLE_FAN, 10, verts, 0, texs, 0, colors, 0, indices, 0,
                6, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawText",
        args = {char[].class, int.class, int.class, float.class, float.class,
                android.graphics.Paint.class}
    )
    public void testDrawText1() {
        final char[] text = {
                'a', 'n', 'd', 'r', 'o', 'i', 'd'
        };
        try {
            mCanvas.drawText(text, -1, 7, 10, 10, mPaint);
            fail("should throw out IndexOutOfBoundsException because index is less than 0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawText(text, 0, -1, 10, 10, mPaint);
            fail("should throw out IndexOutOfBoundsException because count is less than 0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawText(text, 0, 10, 10, 10, mPaint);
            fail("should throw out IndexOutOfBoundsException because sum of index and count "
                                + "is bigger than text's length");
        } catch (IndexOutOfBoundsException e) {
        }
        mCanvas.drawText(text, 0, 7, 10, 10, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawText",
        args = {java.lang.String.class, float.class, float.class, android.graphics.Paint.class}
    )
    public void testDrawText2() {
        mCanvas.drawText("android", 10, 30, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawText",
        args = {java.lang.String.class, int.class, int.class, float.class, float.class,
                android.graphics.Paint.class}
    )
    public void testDrawText3() {
        final String text = "android";
        try {
            mCanvas.drawText(text, -1, 7, 10, 30, mPaint);
            fail("should throw out IndexOutOfBoundsException because start is lesss than 0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawText(text, 0, -1, 10, 30, mPaint);
            fail("should throw out IndexOutOfBoundsException because end is less than 0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawText(text, 3, 1, 10, 30, mPaint);
            fail("should throw out IndexOutOfBoundsException because start is bigger than end");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawText(text, 0, 10, 10, 30, mPaint);
            fail("should throw out IndexOutOfBoundsException because end subtracts start should"
                                + " bigger than text's length");
        } catch (IndexOutOfBoundsException e) {
        }
        mCanvas.drawText(text, 0, 7, 10, 30, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawText",
        args = {java.lang.CharSequence.class, int.class, int.class, float.class, float.class,
                android.graphics.Paint.class}
    )
    public void testDrawText4() {
        final String t1 = "android";
        mCanvas.drawText(t1, 0, 7, 10, 30, mPaint);
        final SpannedString t2 = new SpannedString(t1);
        mCanvas.drawText(t2, 0, 7, 10, 30, mPaint);
        final SpannableString t3 = new SpannableString(t2);
        mCanvas.drawText(t3, 0, 7, 10, 30, mPaint);
        final SpannableStringBuilder t4 = new SpannableStringBuilder(t1);
        mCanvas.drawText(t4, 0, 7, 10, 30, mPaint);
        final StringBuffer t5 = new StringBuffer(t1);
        mCanvas.drawText(t5, 0, 7, 10, 30, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPosText",
        args = {char[].class, int.class, int.class, float[].class, android.graphics.Paint.class}
    )
    public void testDrawPosText1() {
        final char[] text = {
                'a', 'n', 'd', 'r', 'o', 'i', 'd'
        };
        final float[] pos = new float[] {
                0.0f, 0.0f, 1.0f, 1.0f, 2.0f, 2.0f, 3.0f, 3.0f, 4.0f, 4.0f, 5.0f, 5.0f, 6.0f, 6.0f,
                7.0f, 7.0f
        };
        try {
            mCanvas.drawPosText(text, -1, 7, pos, mPaint);
            fail("should throw out IndexOutOfBoundsException because index is less than 0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawPosText(text, 1, 10, pos, mPaint);
            fail("should throw out IndexOutOfBoundsException because sum of index and count is"
                                + " bigger than text's length");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawPosText(text, 1, 10, new float[] {
                    10.0f, 30.f
            }, mPaint);
            fail("should throw out IndexOutOfBoundsException because 2 times of count is"
                                + " bigger than pos' length");
        } catch (IndexOutOfBoundsException e) {
        }
        mCanvas.drawPosText(text, 0, 7, pos, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPosText",
        args = {java.lang.String.class, float[].class, android.graphics.Paint.class}
    )
    public void testDrawPosText2() {
        final String text = "android";
        final float[] pos = new float[] {
                0.0f, 0.0f, 1.0f, 1.0f, 2.0f, 2.0f, 3.0f, 3.0f, 4.0f, 4.0f, 5.0f, 5.0f, 6.0f, 6.0f,
                7.0f, 7.0f
        };
        try {
            mCanvas.drawPosText(text, new float[] {
                    10.0f, 30.f
            }, mPaint);
            fail("should throw out IndexOutOfBoundsException because 2 times of text's length is"
                                + " bigger than pos' length");
        } catch (IndexOutOfBoundsException e) {
        }
        mCanvas.drawPosText(text, pos, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawTextOnPath",
        args = {char[].class, int.class, int.class, android.graphics.Path.class,
                float.class, float.class, android.graphics.Paint.class}
    )
    public void testDrawTextOnPath1() {
        final Path path = new Path();
        final char[] text = {
                'a', 'n', 'd', 'r', 'o', 'i', 'd'
        };
        try {
            mCanvas.drawTextOnPath(text, -1, 7, path, 10.0f, 10.0f, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because index is smaller than 0");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mCanvas.drawTextOnPath(text, 0, 10, path, 10.0f, 10.0f, mPaint);
            fail("should throw out ArrayIndexOutOfBoundsException because sum of index and"
                            + " count is bigger than text's length");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        mCanvas.drawTextOnPath(text, 0, 7, path, 10.0f, 10.0f, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawTextOnPath",
        args = {java.lang.String.class, android.graphics.Path.class, float.class,
                float.class, android.graphics.Paint.class}
    )
    public void testDrawTextOnPath2() {
        final Path path = new Path();
        String text = "";
        mCanvas.drawTextOnPath(text, path, 10.0f, 10.0f, mPaint);
        text = "android";
        mCanvas.drawTextOnPath(text, path, 10.0f, 10.0f, mPaint);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPicture",
        args = {android.graphics.Picture.class}
    )
    public void testDrawPicture1() {
        mCanvas.drawPicture(new Picture());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPicture",
        args = {android.graphics.Picture.class, android.graphics.RectF.class}
    )
    public void testDrawPicture2() {
        final RectF dst = new RectF(0, 0, 10, 31);
        final Picture p = new Picture();
        mCanvas.drawPicture(p, dst);
        p.beginRecording(10, 30);
        mCanvas.drawPicture(p, dst);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "drawPicture",
        args = {android.graphics.Picture.class, android.graphics.Rect.class}
    )
    public void testDrawPicture3() {
        final Rect dst = new Rect(0, 10, 30, 0);
        final Picture p = new Picture();
        mCanvas.drawPicture(p, dst);
        p.beginRecording(10, 30);
        mCanvas.drawPicture(p, dst);
    }
    private void preCompare() {
        final float[] values = new float[FLOAT_ARRAY_LEN];
        mCanvas.getMatrix().getValues(values);
        assertEquals(1.0f, values[0]);
        assertEquals(0.0f, values[1]);
        assertEquals(0.0f, values[2]);
        assertEquals(0.0f, values[3]);
        assertEquals(1.0f, values[4]);
        assertEquals(0.0f, values[5]);
        assertEquals(0.0f, values[6]);
        assertEquals(0.0f, values[7]);
        assertEquals(1.0f, values[8]);
    }
    private class MyGL implements GL {
    }
}
