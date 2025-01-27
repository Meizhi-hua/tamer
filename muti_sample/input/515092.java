@TestTargetClass(android.graphics.drawable.InsetDrawable.class)
public class InsetDrawableTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "InsetDrawable",
            args = {android.graphics.drawable.Drawable.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "InsetDrawable",
            args = {android.graphics.drawable.Drawable.class, int.class, int.class, int.class,
                    int.class}
        )
    })
    public void testConstructor() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        new InsetDrawable(d, 1);
        new InsetDrawable(d, 1, 1, 1, 1);
        new InsetDrawable(null, -1);
        new InsetDrawable(null, -1, -1, -1, -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                android.util.AttributeSet.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "no getter can not be tested," +
            " and there should not be a NullPointerException thrown out.")
    public void testInflate() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        Resources r = mContext.getResources();
        XmlPullParser parser = r.getXml(R.layout.framelayout_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        try {
            insetDrawable.inflate(r, parser, attrs);
            fail("There should be a XmlPullParserException thrown out.");
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
            fail("There should not be an IOException thrown out.");
        }
        try {
            insetDrawable.inflate(null, null, null);
            fail("There should be a NullPointerException thrown out.");
        } catch (XmlPullParserException e) {
            fail("There should not be a XmlPullParserException thrown out.");
        } catch (IOException e) {
            fail("There should not be an IOException thrown out.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidateDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "it's hard to do unit test, should be tested by" +
            " functional test.")
    public void testInvalidateDrawable() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        insetDrawable.invalidateDrawable(d);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "scheduleDrawable",
        args = {android.graphics.drawable.Drawable.class, java.lang.Runnable.class, long.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "it's hard to do unit test, should be tested by" +
            " functional test.")
    public void testScheduleDrawable() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        Runnable runnable = new Runnable() {
            public void run() {
            }
        };
        insetDrawable.scheduleDrawable(d, runnable, 10);
        insetDrawable.scheduleDrawable(null, null, -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "unscheduleDrawable",
        args = {android.graphics.drawable.Drawable.class, java.lang.Runnable.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "it's hard to do unit test, should be tested by" +
            " functional test.")
    public void testUnscheduleDrawable() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        Runnable runnable = new Runnable() {
            public void run() {
            }
        };
        insetDrawable.unscheduleDrawable(d, runnable);
        insetDrawable.unscheduleDrawable(null, null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "it's hard to do unit test, should be tested by" +
            " functional test, and there should not be an NullPointerException thrown out.")
    public void testDraw() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        Canvas c = new Canvas();
        insetDrawable.draw(c);
        try {
            insetDrawable.draw(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getChangingConfigurations",
        args = {}
    )
    public void testGetChangingConfigurations() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        insetDrawable.setChangingConfigurations(11);
        assertEquals(11, insetDrawable.getChangingConfigurations());
        insetDrawable.setChangingConfigurations(-21);
        assertEquals(-21, insetDrawable.getChangingConfigurations());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPadding",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "1371108", explanation = "There should not be a" +
            " NullPointerException thrown out.")
    public void testGetPadding() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 1, 2, 3, 4);
        Rect r = new Rect();
        assertEquals(0, r.left);
        assertEquals(0, r.top);
        assertEquals(0, r.right);
        assertEquals(0, r.bottom);
        assertTrue(insetDrawable.getPadding(r));
        assertEquals(1, r.left);
        assertEquals(2, r.top);
        assertEquals(3, r.right);
        assertEquals(4, r.bottom);
        insetDrawable = new InsetDrawable(d, 0);
        r = new Rect();
        assertEquals(0, r.left);
        assertEquals(0, r.top);
        assertEquals(0, r.right);
        assertEquals(0, r.bottom);
        assertFalse(insetDrawable.getPadding(r));
        assertEquals(0, r.left);
        assertEquals(0, r.top);
        assertEquals(0, r.right);
        assertEquals(0, r.bottom);
        try {
            insetDrawable.getPadding(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setVisible",
        args = {boolean.class, boolean.class}
    )
    public void testSetVisible() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        assertFalse(insetDrawable.setVisible(true, true)); 
        assertTrue(insetDrawable.setVisible(false, true)); 
        assertFalse(insetDrawable.setVisible(false, true)); 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAlpha",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "no getter can not be tested")
    public void testSetAlpha() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        insetDrawable.setAlpha(1);
        insetDrawable.setAlpha(-1);
        insetDrawable.setAlpha(0);
        insetDrawable.setAlpha(Integer.MAX_VALUE);
        insetDrawable.setAlpha(Integer.MIN_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setColorFilter",
        args = {android.graphics.ColorFilter.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "no getter can not be tested")
    public void testSetColorFilter() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        ColorFilter cf = new ColorFilter();
        insetDrawable.setColorFilter(cf);
        insetDrawable.setColorFilter(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOpacity",
        args = {}
    )
    @BrokenTest(value="bug 2397630 - needs investigation")
    public void testGetOpacity() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        assertEquals(PixelFormat.OPAQUE, insetDrawable.getOpacity());
        d = mContext.getResources().getDrawable(R.drawable.testimage);
        insetDrawable = new InsetDrawable(d, 0);
        assertEquals(PixelFormat.OPAQUE, insetDrawable.getOpacity());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isStateful",
        args = {}
    )
    public void testIsStateful() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        assertFalse(insetDrawable.isStateful());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onStateChange",
        args = {int[].class}
    )
    @ToBeFixed(bug = "", explanation = "The onStateChange will always return false.")
    public void testOnStateChange() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        MockInsetDrawable insetDrawable = new MockInsetDrawable(d, 10);
        Rect bounds = d.getBounds();
        assertEquals(0, bounds.left);
        assertEquals(0, bounds.top);
        assertEquals(0, bounds.right);
        assertEquals(0, bounds.bottom);
        int[] state = new int[] {1, 2, 3};
        assertFalse(insetDrawable.onStateChange(state));
        assertEquals(10, bounds.left);
        assertEquals(10, bounds.top);
        assertEquals(-10, bounds.right);
        assertEquals(-10, bounds.bottom);
        insetDrawable.onStateChange(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onBoundsChange",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "1371108", explanation = "There should not be a" +
            " NullPointerException thrown out.")
    public void testOnBoundsChange() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        MockInsetDrawable insetDrawable = new MockInsetDrawable(d, 5);
        Rect bounds = d.getBounds();
        assertEquals(0, bounds.left);
        assertEquals(0, bounds.top);
        assertEquals(0, bounds.right);
        assertEquals(0, bounds.bottom);
        Rect r = new Rect();
        insetDrawable.onBoundsChange(r);
        assertEquals(5, bounds.left);
        assertEquals(5, bounds.top);
        assertEquals(-5, bounds.right);
        assertEquals(-5, bounds.bottom);
        try {
            insetDrawable.onBoundsChange(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIntrinsicWidth",
        args = {}
    )
    public void testGetIntrinsicWidth() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        int expected = d.getIntrinsicWidth(); 
        assertEquals(expected, insetDrawable.getIntrinsicWidth());
        d = mContext.getResources().getDrawable(R.drawable.scenery);
        insetDrawable = new InsetDrawable(d, 0);
        expected = d.getIntrinsicWidth(); 
        assertEquals(expected, insetDrawable.getIntrinsicWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIntrinsicHeight",
        args = {}
    )
    public void testGetIntrinsicHeight() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        int expected = d.getIntrinsicHeight(); 
        assertEquals(expected, insetDrawable.getIntrinsicHeight());
        d = mContext.getResources().getDrawable(R.drawable.scenery);
        insetDrawable = new InsetDrawable(d, 0);
        expected = d.getIntrinsicHeight(); 
        assertEquals(expected, insetDrawable.getIntrinsicHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getConstantState",
        args = {}
    )
    @ToBeFixed(bug = "", explanation = "can not assert the inner fields, becuase the class" +
            " InsetState is package protected.")
    public void testGetConstantState() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        InsetDrawable insetDrawable = new InsetDrawable(d, 0);
        ConstantState constantState = insetDrawable.getConstantState();
        assertNotNull(constantState);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "mutate",
        args = {}
    )
    public void testMutate() {
        Resources resources = mContext.getResources();
        InsetDrawable d1 = (InsetDrawable) resources.getDrawable(R.drawable.insetdrawable);
        d1.setAlpha(100);
        d1.mutate();
        d1.setAlpha(200);
    }
    private class MockInsetDrawable extends InsetDrawable {
        public MockInsetDrawable(Drawable drawable, int inset) {
            super(drawable, inset);
        }
        protected boolean onStateChange(int[] state) {
            return super.onStateChange(state);
        }
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
        }
    }
}
