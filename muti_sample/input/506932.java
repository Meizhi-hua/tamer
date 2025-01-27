@TestTargetClass(LevelListDrawable.class)
public class LevelListDrawableTest extends InstrumentationTestCase {
    private MockLevelListDrawable mLevelListDrawable;
    private Resources mResources;
    private DrawableContainerState mDrawableContainerState;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLevelListDrawable = new MockLevelListDrawable();
        mDrawableContainerState = (DrawableContainerState) mLevelListDrawable.getConstantState();
        mResources = getInstrumentation().getTargetContext().getResources();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "LevelListDrawable",
        args = {}
    )
    public void testLevelListDrawable() {
        new LevelListDrawable();
        assertNotNull(new LevelListDrawable().getConstantState());
        assertTrue(new MockLevelListDrawable().hasCalledOnLevelChanged());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addLevel",
        args = {int.class, int.class, android.graphics.drawable.Drawable.class}
    )
    public void testAddLevel() {
        assertEquals(0, mDrawableContainerState.getChildCount());
        mLevelListDrawable.reset();
        mLevelListDrawable.addLevel(0, 0, null);
        assertEquals(0, mDrawableContainerState.getChildCount());
        assertFalse(mLevelListDrawable.hasCalledOnLevelChanged());
        mLevelListDrawable.reset();
        mLevelListDrawable.addLevel(Integer.MAX_VALUE, Integer.MIN_VALUE, new MockDrawable());
        assertEquals(1, mDrawableContainerState.getChildCount());
        assertTrue(mLevelListDrawable.hasCalledOnLevelChanged());
        mLevelListDrawable.reset();
        mLevelListDrawable.addLevel(Integer.MIN_VALUE, Integer.MAX_VALUE, new MockDrawable());
        assertEquals(2, mDrawableContainerState.getChildCount());
        assertTrue(mLevelListDrawable.hasCalledOnLevelChanged());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onLevelChange",
        args = {int.class}
    )
    public void testOnLevelChange() {
        mLevelListDrawable.addLevel(0, 0, new MockDrawable());
        mLevelListDrawable.addLevel(0, 0, new MockDrawable());
        mLevelListDrawable.addLevel(0, 10, new MockDrawable());
        mLevelListDrawable.reset();
        mLevelListDrawable.setLevel(mLevelListDrawable.getLevel());
        assertFalse(mLevelListDrawable.hasCalledOnLevelChanged());
        mLevelListDrawable.reset();
        mLevelListDrawable.setLevel(mLevelListDrawable.getLevel() - 1);
        assertTrue(mLevelListDrawable.hasCalledOnLevelChanged());
        assertTrue(mLevelListDrawable.onLevelChange(10));
        assertSame(mLevelListDrawable.getCurrent(), mDrawableContainerState.getChildren()[2]);
        assertFalse(mLevelListDrawable.onLevelChange(5));
        assertSame(mLevelListDrawable.getCurrent(), mDrawableContainerState.getChildren()[2]);
        assertTrue(mLevelListDrawable.onLevelChange(0));
        assertSame(mLevelListDrawable.getCurrent(), mDrawableContainerState.getChildren()[0]);
        assertTrue(mLevelListDrawable.onLevelChange(100));
        assertNull(mLevelListDrawable.getCurrent());
        assertFalse(mLevelListDrawable.onLevelChange(101));
        assertNull(mLevelListDrawable.getCurrent());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                android.util.AttributeSet.class}
    )
    public void testInflate() throws XmlPullParserException, IOException {
        XmlResourceParser parser = getResourceParser(R.xml.level_list_correct);
        mLevelListDrawable.reset();
        mLevelListDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
        assertTrue(mLevelListDrawable.hasCalledOnLevelChanged());
        assertEquals(2, mDrawableContainerState.getChildCount());
        mLevelListDrawable.setLevel(200);
        assertSame(mLevelListDrawable.getCurrent(), mDrawableContainerState.getChildren()[0]);
        mLevelListDrawable.setLevel(201);
        assertSame(mLevelListDrawable.getCurrent(), mDrawableContainerState.getChildren()[1]);
        mLevelListDrawable.setLevel(0);
        assertNull(mLevelListDrawable.getCurrent());
        mLevelListDrawable.reset();
        parser = getResourceParser(R.xml.level_list_missing_item_minlevel_maxlevel);
        mLevelListDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
        assertTrue(mLevelListDrawable.hasCalledOnLevelChanged());
        assertEquals(3, mDrawableContainerState.getChildCount());
        assertSame(mLevelListDrawable.getCurrent(), mDrawableContainerState.getChildren()[2]);
        mLevelListDrawable.setLevel(1);
        assertNull(mLevelListDrawable.getCurrent());
        parser = getResourceParser(R.xml.level_list_missing_item_drawable);
        try {
            mLevelListDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
            fail("Should throw XmlPullParserException if drawable of item is missing");
        } catch (XmlPullParserException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                android.util.AttributeSet.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "LevelListDrawable#inflate(Resources, XmlPullParser, AttributeSet) when param r,"
            + "parser or attrs is out of bounds")
    public void testInflateWithNullParameters() throws XmlPullParserException, IOException{
        XmlResourceParser parser = getResourceParser(R.xml.level_list_correct);
        try {
            mLevelListDrawable.inflate(null, parser, Xml.asAttributeSet(parser));
            fail("Should throw XmlPullParserException if resource is null");
        } catch (NullPointerException e) {
        }
        try {
            mLevelListDrawable.inflate(mResources, null, Xml.asAttributeSet(parser));
            fail("Should throw XmlPullParserException if parser is null");
        } catch (NullPointerException e) {
        }
        try {
            mLevelListDrawable.inflate(mResources, parser, null);
            fail("Should throw XmlPullParserException if AttributeSet is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "mutate",
        args = {}
    )
    public void testMutate() throws InterruptedException {
        Resources resources = getInstrumentation().getTargetContext().getResources();
        LevelListDrawable d1 =
            (LevelListDrawable) resources.getDrawable(R.drawable.levellistdrawable);
        LevelListDrawable d2 =
            (LevelListDrawable) resources.getDrawable(R.drawable.levellistdrawable);
        LevelListDrawable d3 =
            (LevelListDrawable) resources.getDrawable(R.drawable.levellistdrawable);
        d1.addLevel(100, 200, resources.getDrawable(R.drawable.testimage));
        assertEquals(3, ((DrawableContainerState) d1.getConstantState()).getChildCount());
        assertEquals(2, ((DrawableContainerState) d2.getConstantState()).getChildCount());
        assertEquals(2, ((DrawableContainerState) d3.getConstantState()).getChildCount());
        d1.mutate();
    }
    private XmlResourceParser getResourceParser(int resId) throws XmlPullParserException,
            IOException {
        XmlResourceParser parser = getInstrumentation().getTargetContext().getResources().getXml(
                resId);
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
        }
        return parser;
    }
    private class MockLevelListDrawable extends LevelListDrawable {
        private boolean mHasCalledOnLevelChanged;
        public boolean hasCalledOnLevelChanged() {
            return mHasCalledOnLevelChanged;
        }
        public void reset() {
            mHasCalledOnLevelChanged = false;
        }
        @Override
        protected boolean onLevelChange(int level) {
            boolean result = super.onLevelChange(level);
            mHasCalledOnLevelChanged = true;
            return result;
        }
    }
    private class MockDrawable extends Drawable {
        @Override
        public void draw(Canvas canvas) {
        }
        @Override
        public int getOpacity() {
            return 0;
        }
        @Override
        public void setAlpha(int alpha) {
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
        }
    }
}
