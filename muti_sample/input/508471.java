@TestTargetClass(ColorMatrix.class)
public class ColorMatrixTest extends AndroidTestCase {
    private ColorMatrix mColorMatrix;
    private final float[] mSrc = new float[]{
        0, 1, 2, 3, 4,
        5, 6, 7, 8, 9,
        10, 11, 12, 13, 14,
        15, 16, 17, 18, 19
    };
    private static final float TOLERANCE = 0.0000001f;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mColorMatrix = new ColorMatrix(mSrc);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ColorMatrix",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ColorMatrix",
            args = {float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ColorMatrix",
            args = {android.graphics.ColorMatrix.class}
        )
    })
    public void testColorMatrix(){
        new ColorMatrix();
        ColorMatrix cM1 = new ColorMatrix(mSrc);
        float[] fA1 = cM1.getArray();
        assertTrue(mSrc.length == fA1.length);
        int len = mSrc.length;
        for(int i = 0; i < len; i++){
            assertEquals(mSrc[i], fA1[i]);
        }
        ColorMatrix cM2 = new ColorMatrix(cM1);
        float[] fA2 = cM2.getArray();
        assertTrue(fA1.length == fA2.length);
        len = fA1.length;
        for(int i = 0; i < len; i++){
            assertEquals(fA1[i], fA2[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testReset(){
        float[] ret = mColorMatrix.getArray();
        preCompare(ret);
        mColorMatrix.reset();
        ret = mColorMatrix.getArray();
        assertEquals(20, ret.length);
        for(int i = 0; i <= 19; i++){
            if(0 == i % 6){
                assertEquals(1.0f, ret[i]);
                continue;
            }
            assertEquals(0.0f, ret[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "set",
            args = {float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSet1(){
        float[] ret = mColorMatrix.getArray();
        preCompare(ret);
        float[] fArray = new float[]{
            19, 18, 17, 16, 15,
            14, 13, 12, 11, 10,
            9, 8, 7, 6, 5,
            4, 3, 2, 1, 0
        };
        mColorMatrix.set(fArray);
        ret = mColorMatrix.getArray();
        assertEquals(20, ret.length);
        for(int i = 19; i >= 0; i--){
            assertEquals((float) i, ret[19 - i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "set",
            args = {android.graphics.ColorMatrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSet2(){
        float[] ret = mColorMatrix.getArray();
        preCompare(ret);
        float[] fArray = new float[]{
            19, 18, 17, 16, 15,
            14, 13, 12, 11, 10,
            9, 8, 7, 6, 5,
            4, 3, 2, 1, 0
        };
        mColorMatrix.set(new ColorMatrix(fArray));
        ret = mColorMatrix.getArray();
        assertEquals(20, ret.length);
        for(int i = 19; i >= 0; i--){
            assertEquals((float) i, ret[19 - i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRotate",
            args = {int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSetRotate(){
        try{
            mColorMatrix.setRotate(4, 90);
            fail("shouldn't come to here");
        }catch(RuntimeException e){
        }
        mColorMatrix.setRotate(0, 180);
        float[] ret = mColorMatrix.getArray();
        assertEquals(-1.0f, ret[6], TOLERANCE);
        assertEquals(-1.0f, ret[12], TOLERANCE);
        assertEquals(0, ret[7], TOLERANCE);
        assertEquals(0, ret[11], TOLERANCE);
        mColorMatrix.setRotate(1, 180);
        assertEquals(-1.0f, ret[0], TOLERANCE);
        assertEquals(-1.0f, ret[12], TOLERANCE);
        assertEquals(0, ret[2], TOLERANCE);
        assertEquals(0, ret[10], TOLERANCE);
        mColorMatrix.setRotate(2, 180);
        assertEquals(-1.0f, ret[0], TOLERANCE);
        assertEquals(-1.0f, ret[6], TOLERANCE);
        assertEquals(0, ret[1], TOLERANCE);
        assertEquals(0, ret[5], TOLERANCE);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSaturation",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSetSaturation(){
        mColorMatrix.setSaturation(0.5f);
        float[] ret = mColorMatrix.getArray();
        assertEquals(0.6065f, ret[0]);
        assertEquals(0.3575f, ret[1]);
        assertEquals(0.036f, ret[2]);
        assertEquals(0.1065f, ret[5]);
        assertEquals(0.85749996f, ret[6]);
        assertEquals(0.036f, ret[7]);
        assertEquals(0.1065f, ret[10]);
        assertEquals(0.3575f, ret[11]);
        assertEquals(0.536f, ret[12]);
        assertEquals(0.0f, ret[3]);
        assertEquals(0.0f, ret[4]);
        assertEquals(0.0f, ret[8]);
        assertEquals(0.0f, ret[9]);
        assertEquals(0.0f, ret[13]);
        assertEquals(0.0f, ret[14]);
        assertEquals(0.0f, ret[15]);
        assertEquals(0.0f, ret[16]);
        assertEquals(0.0f, ret[17]);
        assertEquals(1.0f, ret[18]);
        assertEquals(0.0f, ret[19]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setScale",
            args = {float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSetScale(){
        float[] ret = mColorMatrix.getArray();
        preCompare(ret);
        mColorMatrix.setScale(2, 3, 4, 5);
        ret = mColorMatrix.getArray();
        assertEquals(20, ret.length);
        assertEquals(2.0f, ret[0]);
        assertEquals(3.0f, ret[6]);
        assertEquals(4.0f, ret[12]);
        assertEquals(5.0f, ret[18]);
        for(int i = 1; i <= 19; i++){
            if(0 == i % 6){
                continue;
            }
            assertEquals(0.0f, ret[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRGB2YUV",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSetRGB2YUV(){
        mColorMatrix.setRGB2YUV();
        float[] ret = mColorMatrix.getArray();
        assertEquals(0.299f, ret[0]);
        assertEquals(0.587f, ret[1]);
        assertEquals(0.114f, ret[2]);
        assertEquals(-0.16874f, ret[5]);
        assertEquals(-0.33126f, ret[6]);
        assertEquals(0.5f, ret[7]);
        assertEquals(0.5f, ret[10]);
        assertEquals(-0.41869f, ret[11]);
        assertEquals(-0.08131f, ret[12]);
        assertEquals(0.0f, ret[3]);
        assertEquals(0.0f, ret[4]);
        assertEquals(0.0f, ret[8]);
        assertEquals(0.0f, ret[9]);
        assertEquals(0.0f, ret[13]);
        assertEquals(0.0f, ret[14]);
        assertEquals(0.0f, ret[15]);
        assertEquals(0.0f, ret[16]);
        assertEquals(0.0f, ret[17]);
        assertEquals(1.0f, ret[18]);
        assertEquals(0.0f, ret[19]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setYUV2RGB",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSetYUV2RGB(){
        mColorMatrix.setYUV2RGB();
        float[] ret = mColorMatrix.getArray();
        assertEquals(1.402f, ret[2]);
        assertEquals(1.0f, ret[5]);
        assertEquals(-0.34414f, ret[6]);
        assertEquals(-0.71414f, ret[7]);
        assertEquals(1.0f, ret[10]);
        assertEquals(1.772f, ret[11]);
        assertEquals(0.0f, ret[12]);
        assertEquals(1.0f, ret[0]);
        assertEquals(0.0f, ret[1]);
        assertEquals(0.0f, ret[3]);
        assertEquals(0.0f, ret[4]);
        assertEquals(0.0f, ret[8]);
        assertEquals(0.0f, ret[9]);
        assertEquals(0.0f, ret[13]);
        assertEquals(0.0f, ret[14]);
        assertEquals(0.0f, ret[15]);
        assertEquals(0.0f, ret[16]);
        assertEquals(0.0f, ret[17]);
        assertEquals(1.0f, ret[18]);
        assertEquals(0.0f, ret[19]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "postConcat",
            args = {android.graphics.ColorMatrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testPostConcat(){
        mColorMatrix.postConcat(new ColorMatrix());
        float[] ret = mColorMatrix.getArray();
        for(int i = 0; i < 20; i++){
            assertEquals((float) i, ret[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "preConcat",
            args = {android.graphics.ColorMatrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testPreConcat(){
        mColorMatrix.preConcat(new ColorMatrix());
        float[] ret = mColorMatrix.getArray();
        for(int i = 0; i < 20; i++){
            assertEquals((float) i, ret[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setConcat",
            args = {android.graphics.ColorMatrix.class, android.graphics.ColorMatrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getArray",
            args = {}
        )
    })
    public void testSetConcat(){
        float[] floatA = new float[]{
            0, 1, 2, 3, 4,
            5, 6, 7, 8, 9,
            9, 8, 7, 6, 5,
            4, 3, 2, 1, 0,
        };
        float[] floatB = new float[]{
            1, 1, 1, 1, 1,
            1, 1, 1, 1, 1,
            1, 1, 1, 1, 1,
            1, 1, 1, 1, 1,
        };
        mColorMatrix.setConcat(new ColorMatrix(floatA), new ColorMatrix(floatB));
        float[] ret = mColorMatrix.getArray();
        assertEquals(6.0f, ret[0]);
        assertEquals(6.0f, ret[1]);
        assertEquals(6.0f, ret[2]);
        assertEquals(6.0f, ret[3]);
        assertEquals(10.0f, ret[4]);
        assertEquals(26.0f, ret[5]);
        assertEquals(26.0f, ret[6]);
        assertEquals(26.0f, ret[7]);
        assertEquals(26.0f, ret[8]);
        assertEquals(35.0f, ret[9]);
        assertEquals(30.0f, ret[10]);
        assertEquals(30.0f, ret[11]);
        assertEquals(30.0f, ret[12]);
        assertEquals(30.0f, ret[13]);
        assertEquals(35.0f, ret[14]);
        assertEquals(10.0f, ret[15]);
        assertEquals(10.0f, ret[16]);
        assertEquals(10.0f, ret[17]);
        assertEquals(10.0f, ret[18]);
        assertEquals(10.0f, ret[19]);
    }
    private void preCompare(float[] ret){
        assertEquals(20, ret.length);
        for(int i = 0; i < 20; i++){
            assertEquals((float) i, ret[i]);
        }
    }
}
