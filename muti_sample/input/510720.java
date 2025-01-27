public class FrameLayoutGravityTest extends ActivityInstrumentationTestCase<FrameLayoutGravity> {
    private View mLeftView;
    private View mRightView;
    private View mCenterHorizontalView;
    private View mLeftCenterVerticalView;
    private View mRighCenterVerticalView;
    private View mCenterView;
    private View mLeftBottomView;
    private View mRightBottomView;
    private View mCenterHorizontalBottomView;
    private View mParent;
    public FrameLayoutGravityTest() {
        super("com.android.frameworks.coretests", FrameLayoutGravity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mParent = activity.findViewById(R.id.parent);
        mLeftView = activity.findViewById(R.id.left);
        mRightView = activity.findViewById(R.id.right);
        mCenterHorizontalView = activity.findViewById(R.id.center_horizontal);
        mLeftCenterVerticalView = activity.findViewById(R.id.left_center_vertical);
        mRighCenterVerticalView = activity.findViewById(R.id.right_center_vertical);
        mCenterView = activity.findViewById(R.id.center);
        mLeftBottomView = activity.findViewById(R.id.left_bottom);
        mRightBottomView = activity.findViewById(R.id.right_bottom);
        mCenterHorizontalBottomView = activity.findViewById(R.id.center_horizontal_bottom);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mParent);
        assertNotNull(mLeftView);
        assertNotNull(mRightView);
        assertNotNull(mCenterHorizontalView);
        assertNotNull(mLeftCenterVerticalView);
        assertNotNull(mRighCenterVerticalView);
        assertNotNull(mCenterView);
        assertNotNull(mLeftBottomView);
        assertNotNull(mRightBottomView);
        assertNotNull(mCenterHorizontalBottomView);
    }
    @MediumTest
    public void testLeftTopAligned() throws Exception {
        ViewAsserts.assertLeftAligned(mParent, mLeftView);
        ViewAsserts.assertTopAligned(mParent, mLeftView);
    }
    @MediumTest
    public void testRightTopAligned() throws Exception {
        ViewAsserts.assertRightAligned(mParent, mRightView);
        ViewAsserts.assertTopAligned(mParent, mRightView);
    }
    @MediumTest
    public void testCenterHorizontalTopAligned() throws Exception {
        ViewAsserts.assertHorizontalCenterAligned(mParent, mCenterHorizontalView);
        ViewAsserts.assertTopAligned(mParent, mCenterHorizontalView);
    }
    @MediumTest
    public void testLeftCenterVerticalAligned() throws Exception {
        ViewAsserts.assertLeftAligned(mParent, mLeftCenterVerticalView);
        ViewAsserts.assertVerticalCenterAligned(mParent, mLeftCenterVerticalView);
    }
    @MediumTest
    public void testRightCenterVerticalAligned() throws Exception {
        ViewAsserts.assertRightAligned(mParent, mRighCenterVerticalView);
        ViewAsserts.assertVerticalCenterAligned(mParent, mRighCenterVerticalView);
    }
    @MediumTest
    public void testCenterAligned() throws Exception {
        ViewAsserts.assertHorizontalCenterAligned(mParent, mCenterView);
        ViewAsserts.assertVerticalCenterAligned(mParent, mCenterView);
    }
    @MediumTest
    public void testLeftBottomAligned() throws Exception {
        ViewAsserts.assertLeftAligned(mParent, mLeftBottomView);
        ViewAsserts.assertBottomAligned(mParent, mLeftBottomView);
    }
    @MediumTest
    public void testRightBottomAligned() throws Exception {
        ViewAsserts.assertRightAligned(mParent, mRightBottomView);
        ViewAsserts.assertBottomAligned(mParent, mRightBottomView);
    }
    @MediumTest
    public void testCenterHorizontalBottomAligned() throws Exception {
        ViewAsserts.assertHorizontalCenterAligned(mParent, mCenterHorizontalBottomView);
        ViewAsserts.assertBottomAligned(mParent, mCenterHorizontalBottomView);
    }
}
