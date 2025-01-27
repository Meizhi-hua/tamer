@TestTargetClass(WindowManager.LayoutParams.class)
public class WindowManager_LayoutParamsTest extends AndroidTestCase {
    private static final int WINDOW_WIDTH = 320;
    private static final int WINDOW_HEIGHT = 480;
    private static final int XPOS = 10;
    private static final int YPOS = 15;
    private static final String PACKAGE_NAME = "android.content";
    private static final String TITLE = "params title";
    private static final String PARAMS_TITLE = "params title";
    private static final float HORIZONTAL_MARGIN = 1.0f;
    private static final float VERTICAL_MARGIN = 3.0f;
    private static final float ALPHA = 1.0f;
    private static final float DIM_AMOUNT = 1.0f;
    private static final float HORIZONTAL_WEIGHT = 1.0f;
    private static final float MARGIN = 1.0f;
    private static final float VERTICAL_WEIGHT = 1.0f;
    private static final int WINDOW_ANIMATIONS = 6;
    private WindowManager.LayoutParams mLayoutParams;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s)",
            method = "WindowManager.LayoutParams",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WindowManager.LayoutParams",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WindowManager.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WindowManager.LayoutParams",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WindowManager.LayoutParams",
            args = {int.class, int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WindowManager.LayoutParams",
            args = {int.class, int.class, int.class, int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WindowManager.LayoutParams",
            args = {Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WindowManager.mayUseInputMethod",
            args = {int.class}
        )
    })
    public void testConstructor() {
        new WindowManager.LayoutParams();
        new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION);
        new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER);
        new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER, PixelFormat.JPEG);
        new WindowManager.LayoutParams(WINDOW_WIDTH, WINDOW_HEIGHT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER, PixelFormat.JPEG);
        new WindowManager.LayoutParams(WINDOW_WIDTH, WINDOW_HEIGHT, XPOS, YPOS,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER, PixelFormat.JPEG);
        IBinder binder = new Binder();
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.token = binder;
        mLayoutParams.packageName = PACKAGE_NAME;
        mLayoutParams.setTitle(TITLE);
        Parcel parcel = Parcel.obtain();
        mLayoutParams.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        new WindowManager.LayoutParams(parcel);
        assertTrue(WindowManager.LayoutParams.mayUseInputMethod(0));
        assertTrue(WindowManager.LayoutParams.mayUseInputMethod(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM));
        assertFalse(WindowManager.LayoutParams
                .mayUseInputMethod(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE));
        assertFalse(WindowManager.LayoutParams
                .mayUseInputMethod(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "copyFrom",
        args = {WindowManager.LayoutParams.class}
    )
    public void testCopyFrom() {
        mLayoutParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_BASE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER);
        assertEquals(WindowManager.LayoutParams.TYPE_CHANGED
                | WindowManager.LayoutParams.FLAGS_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(WindowManager.LayoutParams.TYPE_BASE_APPLICATION, mLayoutParams.type);
        assertEquals(WindowManager.LayoutParams.FLAG_DITHER, mLayoutParams.flags);
        mLayoutParams = new WindowManager.LayoutParams();
        params = new WindowManager.LayoutParams(WINDOW_WIDTH, WINDOW_HEIGHT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER, PixelFormat.JPEG);
        assertEquals(WindowManager.LayoutParams.LAYOUT_CHANGED
                | WindowManager.LayoutParams.FLAGS_CHANGED
                | WindowManager.LayoutParams.FORMAT_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(WINDOW_WIDTH, mLayoutParams.width);
        assertEquals(WINDOW_HEIGHT, mLayoutParams.height);
        assertEquals(WindowManager.LayoutParams.FLAG_DITHER, mLayoutParams.flags);
        assertEquals(PixelFormat.JPEG, mLayoutParams.format);
        params = new WindowManager.LayoutParams();
        params.setTitle(PARAMS_TITLE);
        params.alpha = ALPHA - 0.5f;
        params.windowAnimations = WINDOW_ANIMATIONS;
        params.memoryType = WindowManager.LayoutParams.MEMORY_TYPE_HARDWARE;
        params.dimAmount = DIM_AMOUNT - 1.0f;
        mLayoutParams = new WindowManager.LayoutParams();
        assertEquals(WindowManager.LayoutParams.TITLE_CHANGED
                | WindowManager.LayoutParams.ALPHA_CHANGED
                | WindowManager.LayoutParams.MEMORY_TYPE_CHANGED
                | WindowManager.LayoutParams.ANIMATION_CHANGED
                | WindowManager.LayoutParams.DIM_AMOUNT_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(params.getTitle(), mLayoutParams.getTitle());
        assertEquals(params.alpha, mLayoutParams.alpha);
        assertEquals(params.memoryType, mLayoutParams.memoryType);
        assertEquals(params.dimAmount, mLayoutParams.dimAmount);
        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP;
        mLayoutParams = new WindowManager.LayoutParams();
        assertEquals(WindowManager.LayoutParams.LAYOUT_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(params.gravity, mLayoutParams.gravity);
        params = new WindowManager.LayoutParams();
        params.horizontalMargin = HORIZONTAL_MARGIN;
        mLayoutParams = new WindowManager.LayoutParams();
        assertEquals(WindowManager.LayoutParams.LAYOUT_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(params.horizontalMargin, mLayoutParams.horizontalMargin);
        params = new WindowManager.LayoutParams();
        params.horizontalWeight = HORIZONTAL_WEIGHT;
        mLayoutParams = new WindowManager.LayoutParams();
        assertEquals(WindowManager.LayoutParams.LAYOUT_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(params.horizontalWeight, mLayoutParams.horizontalWeight);
        params = new WindowManager.LayoutParams();
        params.verticalMargin = MARGIN;
        mLayoutParams = new WindowManager.LayoutParams();
        assertEquals(WindowManager.LayoutParams.LAYOUT_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(params.verticalMargin, mLayoutParams.verticalMargin);
        params = new WindowManager.LayoutParams();
        params.verticalWeight = VERTICAL_WEIGHT;
        mLayoutParams = new WindowManager.LayoutParams();
        assertEquals(WindowManager.LayoutParams.LAYOUT_CHANGED,
                mLayoutParams.copyFrom(params));
        assertEquals(params.verticalWeight, mLayoutParams.verticalWeight);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        mLayoutParams = new WindowManager.LayoutParams();
        assertEquals(0, mLayoutParams.describeContents());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {CharSequence.class}
        )
    })
    public void testAccessTitle() {
        String title = "";
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.setTitle(null);
        assertEquals(title, mLayoutParams.getTitle());
        title = "Android Test Title";
        mLayoutParams.setTitle(title);
        assertEquals(title, mLayoutParams.getTitle());
        SpannedString spannedTitle = new SpannedString(title);
        mLayoutParams.setTitle(spannedTitle);
        assertEquals(spannedTitle, mLayoutParams.getTitle());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        mLayoutParams = new WindowManager.LayoutParams();
        assertNotNull(mLayoutParams.toString());
        mLayoutParams = new WindowManager.LayoutParams(WINDOW_WIDTH, WINDOW_HEIGHT, XPOS, YPOS,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER, PixelFormat.JPEG);
        assertNotNull(mLayoutParams.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        IBinder binder = new Binder();
        mLayoutParams = new WindowManager.LayoutParams(WINDOW_WIDTH, WINDOW_HEIGHT, XPOS, YPOS,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER, PixelFormat.JPEG);
        mLayoutParams.memoryType = WindowManager.LayoutParams.MEMORY_TYPE_HARDWARE;
        mLayoutParams.gravity = Gravity.TOP;
        mLayoutParams.horizontalMargin = HORIZONTAL_MARGIN;
        mLayoutParams.verticalMargin = VERTICAL_MARGIN;
        mLayoutParams.windowAnimations = WINDOW_ANIMATIONS;
        mLayoutParams.token = binder;
        mLayoutParams.packageName = PACKAGE_NAME;
        mLayoutParams.setTitle(PARAMS_TITLE);
        Parcel parcel = Parcel.obtain();
        mLayoutParams.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        WindowManager.LayoutParams out =
            WindowManager.LayoutParams.CREATOR.createFromParcel(parcel);
        assertEquals(0, out.copyFrom(mLayoutParams));
        try {
            mLayoutParams.writeToParcel(null, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "debug",
        args = {String.class}
    )
    public void testDebug() {
    }
}
