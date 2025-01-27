@TestTargetClass(android.view.animation.RotateAnimation.class)
public class RotateAnimationTest
        extends ActivityInstrumentationTestCase2<AnimationTestStubActivity> {
    private Activity mActivity;
    private static final long DURATION = 1000;
    private static final float ROTATE_DELTA = 0.001f;
    private static final float FROM_DEGREE = 0.0f;
    private static final float TO_DEGREE = 90.0f;
    public RotateAnimationTest() {
        super("com.android.cts.stub", AnimationTestStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RotateAnimation",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RotateAnimation",
            args = {float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RotateAnimation",
            args = {float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RotateAnimation",
            args = {float.class, float.class, int.class, float.class, int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RotateAnimation",
            args = {float.class, float.class}
        )
    })
    public void testConstructors() {
        new RotateAnimation(mActivity, null);
        final XmlResourceParser parser = mActivity.getResources().getAnimation(
                R.anim.anim_rotate);
        final AttributeSet attr = Xml.asAttributeSet(parser);
        assertNotNull(attr);
        new RotateAnimation(mActivity, attr);
        new RotateAnimation(0.6f, 0.6f);
        new RotateAnimation(-0.6f, -0.6f);
        new RotateAnimation(0.6f, 0.6f, 0.6f, 0.6f);
        new RotateAnimation(-0.6f, -0.6f, -0.6f, -0.6f);
        new RotateAnimation(0.6f, 0.6f, Animation.ABSOLUTE, 0.6f, Animation.ABSOLUTE, 0.6f);
        new RotateAnimation(-0.6f, -0.6f, Animation.ABSOLUTE, -0.6f, Animation.ABSOLUTE, -0.6f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "applyTransformation",
            args = {float.class, android.view.animation.Transformation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "initialize",
            args = {int.class, int.class, int.class, int.class}
        )
    })
    public void testRotateAgainstOrigin(){
        final View animWindowParent = mActivity.findViewById(R.id.anim_window_parent);
        final View animWindow = mActivity.findViewById(R.id.anim_window);
        Transformation transformation = new Transformation();
        MyRotateAnimation rotateAnimation = new MyRotateAnimation(FROM_DEGREE, TO_DEGREE);
        rotateAnimation.setDuration(DURATION);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        assertFalse(rotateAnimation.isInitialized());
        rotateAnimation.initialize(animWindow.getWidth(), animWindow.getHeight(),
                animWindowParent.getWidth(), animWindowParent.getHeight());
        assertTrue(rotateAnimation.isInitialized());
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, rotateAnimation);
        final long startTime = rotateAnimation.getStartTime();
        Matrix expectedMatrix = new Matrix();
        expectedMatrix.setRotate(FROM_DEGREE);
        rotateAnimation.getTransformation(startTime, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        transformation.clear();
        rotateAnimation.applyTransformation(0.0f, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        expectedMatrix.reset();
        expectedMatrix.setRotate((FROM_DEGREE + TO_DEGREE) / 2);
        rotateAnimation.getTransformation(startTime + DURATION / 2, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        transformation.clear();
        rotateAnimation.applyTransformation(0.5f, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        expectedMatrix.reset();
        expectedMatrix.setRotate(TO_DEGREE);
        rotateAnimation.getTransformation(startTime + DURATION, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        rotateAnimation.applyTransformation(1.0f, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
    }
    private void assertMatrixEquals(Matrix expectedMatrix, Matrix actualMatrix) {
        final float[] expectedMatrixValues = new float[9];
        final float[] actualMatrixValues = new float[9];
        expectedMatrix.getValues(expectedMatrixValues);
        actualMatrix.getValues(actualMatrixValues);
        for (int i = 0; i < 9; i++) {
            assertEquals(expectedMatrixValues[i], actualMatrixValues[i], ROTATE_DELTA);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "applyTransformation",
            args = {float.class, android.view.animation.Transformation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "initialize",
            args = {int.class, int.class, int.class, int.class}
        )
    })
    public void testRotateAgainstPoint(){
        final View animWindowParent = mActivity.findViewById(R.id.anim_window_parent);
        final View animWindow = mActivity.findViewById(R.id.anim_window);
        Transformation transformation = new Transformation();
        final float pivotX = 0.2f;
        final float pivotY = 0.2f;
        final float actualPivotX = pivotX * animWindowParent.getWidth();
        final float actualPivotY = pivotY * animWindow.getHeight();
        MyRotateAnimation rotateAnimation = new MyRotateAnimation(FROM_DEGREE, TO_DEGREE,
                    Animation.RELATIVE_TO_PARENT, pivotX, Animation.RELATIVE_TO_SELF, pivotY);
        rotateAnimation.setDuration(DURATION);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        assertFalse(rotateAnimation.isInitialized());
        rotateAnimation.initialize(animWindow.getWidth(), animWindow.getHeight(),
                animWindowParent.getWidth(), animWindowParent.getHeight());
        assertTrue(rotateAnimation.isInitialized());
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, rotateAnimation);
        final long startTime = rotateAnimation.getStartTime();
        Matrix expectedMatrix = new Matrix();
        expectedMatrix.setRotate(FROM_DEGREE, actualPivotX, actualPivotY);
        rotateAnimation.getTransformation(startTime, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        transformation.clear();
        rotateAnimation.applyTransformation(0.0f, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        expectedMatrix.reset();
        expectedMatrix.setRotate((FROM_DEGREE + TO_DEGREE) / 2, actualPivotX, actualPivotY);
        rotateAnimation.getTransformation(startTime + DURATION / 2, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        transformation.clear();
        rotateAnimation.applyTransformation(0.5f, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        expectedMatrix.reset();
        expectedMatrix.setRotate(TO_DEGREE, actualPivotX, actualPivotY);
        rotateAnimation.getTransformation(startTime + DURATION, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
        transformation.clear();
        rotateAnimation.applyTransformation(1.0f, transformation);
        assertMatrixEquals(expectedMatrix, transformation.getMatrix());
    }
    private static class MyRotateAnimation extends RotateAnimation {
        public MyRotateAnimation(float fromDegrees, float toDegrees) {
            super(fromDegrees, toDegrees);
        }
        public MyRotateAnimation(float fromDegrees, float toDegrees, float pivotX, float pivotY) {
            super(fromDegrees, toDegrees, pivotX, pivotY);
        }
        public MyRotateAnimation(float fromDegrees, float toDegrees, int pivotXType,
                float pivotX, int pivotYType, float pivotY) {
            super(fromDegrees, toDegrees, pivotXType, pivotX, pivotYType, pivotY);
        }
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
        }
    }
}
