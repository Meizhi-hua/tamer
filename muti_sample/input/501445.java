@TestTargetClass(LayoutParams.class)
public class RadioGroup_LayoutParamsTest extends AndroidTestCase {
    private LayoutParams mLayoutParams;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLayoutParams = null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioGroup.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioGroup.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioGroup.LayoutParams",
            args = {int.class, int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioGroup.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioGroup.LayoutParams",
            args = {android.view.ViewGroup.MarginLayoutParams.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "RadioGroup.LayoutParams#RadioGroup.LayoutParams(ViewGroup.LayoutParams) "
            + "when ViewGroup.LayoutParams is null "
            + "and RadioGroup.LayoutParams#RadioGroup.LayoutParams(MarginLayoutParams) "
            + "when param MarginLayoutParams is null "
            + "and RadioGroup.LayoutParams#RadioGroup.LayoutParams(Context, AttributeSet) "
            + "when param Context is null")
    public void testConstructor() {
        mLayoutParams = new RadioGroup.LayoutParams(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, mLayoutParams.width);
        assertEquals(Integer.MAX_VALUE, mLayoutParams.height);
        assertEquals(0.0f, mLayoutParams.weight);
        mLayoutParams = new RadioGroup.LayoutParams(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, mLayoutParams.width);
        assertEquals(Integer.MIN_VALUE, mLayoutParams.height);
        assertEquals(0.0f, mLayoutParams.weight);
        mLayoutParams = new RadioGroup.LayoutParams(Integer.MIN_VALUE, Integer.MAX_VALUE,
                Float.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, mLayoutParams.width);
        assertEquals(Integer.MAX_VALUE, mLayoutParams.height);
        assertEquals(Float.MAX_VALUE, mLayoutParams.weight);
        mLayoutParams = new RadioGroup.LayoutParams(Integer.MIN_VALUE, Integer.MAX_VALUE,
                Float.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, mLayoutParams.width);
        assertEquals(Integer.MAX_VALUE, mLayoutParams.height);
        assertEquals(Float.MIN_VALUE, mLayoutParams.weight);
        mLayoutParams = new RadioGroup.LayoutParams(new ViewGroup.LayoutParams(40, 60));
        assertEquals(40, mLayoutParams.width);
        assertEquals(60, mLayoutParams.height);
        assertEquals(0.0f, mLayoutParams.weight);
        try {
            new RadioGroup.LayoutParams((ViewGroup.LayoutParams) null);
            fail("The constructor should throw NullPointerException when param "
                    + "ViewGroup.LayoutParams is null.");
        } catch (NullPointerException e) {
        }
        mLayoutParams = new RadioGroup.LayoutParams(new MarginLayoutParams(100, 200));
        assertEquals(100, mLayoutParams.width);
        assertEquals(200, mLayoutParams.height);
        assertEquals(0.0f, mLayoutParams.weight);
        assertEquals(0, mLayoutParams.leftMargin);
        assertEquals(0, mLayoutParams.topMargin);
        assertEquals(0, mLayoutParams.rightMargin);
        assertEquals(0, mLayoutParams.bottomMargin);
        MarginLayoutParams source = new MarginLayoutParams(10, 20);
        source.leftMargin = 1;
        source.topMargin = 2;
        source.rightMargin = 3;
        source.bottomMargin = 4;
        mLayoutParams = new RadioGroup.LayoutParams(source);
        assertEquals(10, mLayoutParams.width);
        assertEquals(20, mLayoutParams.height);
        assertEquals(0.0f, mLayoutParams.weight);
        assertEquals(1, mLayoutParams.leftMargin);
        assertEquals(2, mLayoutParams.topMargin);
        assertEquals(3, mLayoutParams.rightMargin);
        assertEquals(4, mLayoutParams.bottomMargin);
        try {
            new RadioGroup.LayoutParams((MarginLayoutParams) null);
            fail("The constructor should throw NullPointerException when param "
                    + "MarginLayoutParams is null.");
        } catch (NullPointerException e) {
        }
        mLayoutParams = new LayoutParams(getContext(), 
                getAttributeSet(com.android.cts.stub.R.layout.radiogroup_1));
        assertNotNull(mLayoutParams);
        assertEquals(0.5, mLayoutParams.weight, 0);
        assertEquals(Gravity.BOTTOM, mLayoutParams.gravity);
        assertEquals(5, mLayoutParams.leftMargin);
        assertEquals(5, mLayoutParams.topMargin);
        assertEquals(5, mLayoutParams.rightMargin);
        assertEquals(5, mLayoutParams.bottomMargin);
        assertEquals(LayoutParams.MATCH_PARENT, mLayoutParams.width);
        assertEquals(LayoutParams.MATCH_PARENT, mLayoutParams.height);
        mLayoutParams = new RadioGroup.LayoutParams(getContext(), null);
        assertEquals(RadioGroup.LayoutParams.WRAP_CONTENT, mLayoutParams.width);
        assertEquals(RadioGroup.LayoutParams.WRAP_CONTENT, mLayoutParams.height);
        try {
            new RadioGroup.LayoutParams(null, 
                    getAttributeSet(com.android.cts.stub.R.layout.radiogroup_1));
            fail("The constructor should throw NullPointerException when param Context is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setBaseAttributes",
        args = {android.content.res.TypedArray.class, int.class, int.class}
    )
    public void testSetBaseAttributes() {
        MockLayoutParams layoutParams = new MockLayoutParams(getContext(), null);
        assertEquals(LayoutParams.WRAP_CONTENT, layoutParams.width);
        assertEquals(LayoutParams.WRAP_CONTENT, layoutParams.height);
        AttributeSet attrs = getAttributeSet(com.android.cts.stub.R.layout.radiogroup_1);
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.ViewGroup_MarginLayout);
        layoutParams.setBaseAttributes(a,
                R.styleable.ViewGroup_MarginLayout_layout_width,
                R.styleable.ViewGroup_MarginLayout_layout_height);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.height);
    }
    private AttributeSet getAttributeSet(int resId) {
        XmlPullParser parser = mContext.getResources().getLayout(resId);
        assertNotNull(parser);
        int type = 0;
        try {
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
            }
        } catch (XmlPullParserException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertEquals("No RadioGroup element found", XmlPullParser.START_TAG, type);
        assertEquals("The first element is not RadioGroup", "RadioGroup", parser.getName());
        return Xml.asAttributeSet(parser);
    }
    private class MockLayoutParams extends RadioGroup.LayoutParams {
        public MockLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        @Override
        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            super.setBaseAttributes(a, widthAttr, heightAttr);
        }
    }
}
