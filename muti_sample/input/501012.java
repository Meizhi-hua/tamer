@TestTargetClass(SurfaceView.class)
public class SurfaceViewTest extends ActivityInstrumentationTestCase2<SurfaceViewStubActivity> {
    private static final long WAIT_TIME = 1000;
    private Context mContext;
    private Instrumentation mInstrumentation;
    private MockSurfaceView mMockSurfaceView;
    public SurfaceViewTest() {
        super("com.android.cts.stub", SurfaceViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mContext = mInstrumentation.getContext();
        mMockSurfaceView = getActivity().getSurfaceView();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link SurfaceView}",
            method = "SurfaceView",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link SurfaceView}",
            method = "SurfaceView",
            args = {Context.class, AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link SurfaceView}",
            method = "SurfaceView",
            args = {Context.class, AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new SurfaceView(mContext);
        new SurfaceView(mContext, null);
        new SurfaceView(mContext, null, 0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: draw",
            method = "draw",
            args = {Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: dispatchDraw",
            method = "dispatchDraw",
            args = {Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: gatherTransparentRegion",
            method = "gatherTransparentRegion",
            args = {Region.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: getHolder",
            method = "getHolder",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onAttachedToWindow",
            method = "onAttachedToWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onMeasure",
            method = "onMeasure",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onWindowVisibilityChanged",
            method = "onWindowVisibilityChanged",
            args = {int.class}
        )
    })
    public void testSurfaceView() {
        final int left = 40;
        final int top = 30;
        final int right = 320;
        final int bottom = 240;
        assertTrue(mMockSurfaceView.isDraw());
        assertTrue(mMockSurfaceView.isOnAttachedToWindow());
        assertTrue(mMockSurfaceView.isDispatchDraw());
        assertTrue(mMockSurfaceView.isDrawColor());
        assertTrue(mMockSurfaceView.isSurfaceChanged());
        assertTrue(mMockSurfaceView.isOnWindowVisibilityChanged());
        int expectedVisibility = mMockSurfaceView.getVisibility();
        int actualVisibility = mMockSurfaceView.getVInOnWindowVisibilityChanged();
        assertEquals(expectedVisibility, actualVisibility);
        assertTrue(mMockSurfaceView.isOnMeasureCalled());
        int expectedWidth = mMockSurfaceView.getMeasuredWidth();
        int expectedHeight = mMockSurfaceView.getMeasuredHeight();
        int actualWidth = mMockSurfaceView.getWidthInOnMeasure();
        int actualHeight = mMockSurfaceView.getHeightInOnMeasure();
        assertEquals(expectedWidth, actualWidth);
        assertEquals(expectedHeight, actualHeight);
        Region region = new Region();
        region.set(left, top, right, bottom);
        assertTrue(mMockSurfaceView.gatherTransparentRegion(region));
        mMockSurfaceView.setFormat(PixelFormat.TRANSPARENT);
        assertFalse(mMockSurfaceView.gatherTransparentRegion(region));
        SurfaceHolder actual = mMockSurfaceView.getHolder();
        assertNotNull(actual);
        assertTrue(actual instanceof SurfaceHolder);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: onSizeChanged",
        method = "onSizeChanged",
        args = {int.class, int.class, int.class, int.class}
    )
    @UiThreadTest
    public void testOnSizeChanged() {
        final int left = 40;
        final int top = 30;
        final int right = 320;
        final int bottom = 240;
        int beforeLayoutWidth = mMockSurfaceView.getWidth();
        int beforeLayoutHeight = mMockSurfaceView.getHeight();
        mMockSurfaceView.resetOnSizeChangedFlag(false);
        assertFalse(mMockSurfaceView.isOnSizeChangedCalled());
        mMockSurfaceView.layout(left, top, right, bottom);
        assertTrue(mMockSurfaceView.isOnSizeChangedCalled());
        assertEquals(beforeLayoutWidth, mMockSurfaceView.getOldWidth());
        assertEquals(beforeLayoutHeight, mMockSurfaceView.getOldHeight());
        assertEquals(right - left, mMockSurfaceView.getWidth());
        assertEquals(bottom - top, mMockSurfaceView.getHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: onScrollChanged",
        method = "onScrollChanged",
        args = {int.class, int.class, int.class, int.class}
    )
    @UiThreadTest
    public void testOnScrollChanged() {
        final int scrollToX = 200;
        final int scrollToY = 200;
        int oldHorizontal = mMockSurfaceView.getScrollX();
        int oldVertical = mMockSurfaceView.getScrollY();
        assertFalse(mMockSurfaceView.isOnScrollChanged());
        mMockSurfaceView.scrollTo(scrollToX, scrollToY);
        assertTrue(mMockSurfaceView.isOnScrollChanged());
        assertEquals(oldHorizontal, mMockSurfaceView.getOldHorizontal());
        assertEquals(oldVertical, mMockSurfaceView.getOldVertical());
        assertEquals(scrollToX, mMockSurfaceView.getScrollX());
        assertEquals(scrollToY, mMockSurfaceView.getScrollY());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test method: onDetachedFromWindow",
        method = "onDetachedFromWindow",
        args = {}
    )
    public void testOnDetachedFromWindow() {
        MockSurfaceView mockSurfaceView = getActivity().getSurfaceView();
        assertFalse(mockSurfaceView.isDetachedFromWindow());
        assertTrue(mockSurfaceView.isShown());
        sendKeys(KeyEvent.KEYCODE_BACK);
        sleep(WAIT_TIME);
        assertTrue(mockSurfaceView.isDetachedFromWindow());
        assertFalse(mockSurfaceView.isShown());
    }
    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            fail("error occurs when wait for an action: " + e.toString());
        }
    }
}
