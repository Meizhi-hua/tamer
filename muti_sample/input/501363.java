public class AndroidGraphicsTests extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void testMatrix() {
        Matrix m1 = new Matrix();
        assertTrue(m1.isIdentity());
        m1.setValues(new float[] { 1,0,0, 0,1,0, 0,0,1 });
        assertTrue(m1.isIdentity());
        Matrix m2 = new Matrix(m1);
        float[] v1 = new float[9];
        float[] v2 = new float[9];
        m1.getValues(v1);
        m2.getValues(v2);
        for (int i = 0 ; i < 9; i++) {
            assertEquals(v1[i], v2[i]);
        }
    }
    public void testPaint() {
        _Original_Paint o = new _Original_Paint();
        assertNotNull(o);
        Paint p = new Paint();
        assertNotNull(p);
    }
    public void textTextPaint() {
        TextPaint p = new TextPaint();
        assertNotNull(p);
    }
}
