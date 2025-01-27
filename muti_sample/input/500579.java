@TestTargetClass(ViewAnimator.class)
public class ViewAnimatorTest extends
        ActivityInstrumentationTestCase2<ViewAnimatorStubActivity> {
    private ViewAnimator mViewAnimator;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private AttributeSet mAttributeSet;
    public ViewAnimatorTest() {
        super("com.android.cts.stub", ViewAnimatorStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        XmlPullParser parser = mActivity.getResources().getXml(R.layout.viewanimator_layout);
        mAttributeSet = Xml.asAttributeSet(parser);
        mViewAnimator = new ViewAnimator(mActivity, mAttributeSet);
        assertNotNull(mActivity);
        assertNotNull(mInstrumentation);
        assertNotNull(mViewAnimator);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ViewAnimator",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ViewAnimator",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new ViewAnimator(mActivity);
        new ViewAnimator(mActivity, mAttributeSet);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setInAnimation",
            args = {android.view.animation.Animation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setInAnimation",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInAnimation",
            args = {}
        )
    })
    public void testAccessInAnimation() {
        AnimationSet expected = new AnimationSet(mActivity, mAttributeSet);
        assertNull(mViewAnimator.getInAnimation());
        mViewAnimator.setInAnimation(expected);
        assertSame(expected, mViewAnimator.getInAnimation());
        mViewAnimator.setInAnimation(null);
        assertNull(mViewAnimator.getInAnimation());
        mViewAnimator.setInAnimation(mActivity, R.anim.anim_alpha);
        Animation animation = mViewAnimator.getInAnimation();
        assertTrue(animation.getInterpolator() instanceof AccelerateInterpolator);
        assertEquals(500, animation.getDuration());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "showNext",
        args = {}
    )
    @UiThreadTest
    public void testShowNext() {
        final View v1 = mActivity.findViewById(R.id.ok);
        final View v2 = mActivity.findViewById(R.id.cancel);
        final View v3 = mActivity.findViewById(R.id.label);
        final View v4 = mActivity.findViewById(R.id.entry);
        final RelativeLayout parent = (RelativeLayout) v1.getParent();
        parent.removeView(v1);
        parent.removeView(v2);
        parent.removeView(v3);
        parent.removeView(v4);
        assertEquals(0, mViewAnimator.getChildCount());
        mViewAnimator.addView(v1);
        mViewAnimator.addView(v2);
        mViewAnimator.addView(v3);
        mViewAnimator.addView(v4);
        assertEquals(4, mViewAnimator.getChildCount());
        int current = 0;
        mViewAnimator.setDisplayedChild(current);
        assertEquals(current, mViewAnimator.getDisplayedChild());
        mViewAnimator.showNext();
        assertEquals(1, mViewAnimator.getDisplayedChild());
        mViewAnimator.showNext();
        assertEquals(2, mViewAnimator.getDisplayedChild());
        mViewAnimator.showNext();
        assertEquals(3, mViewAnimator.getDisplayedChild());
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setAnimateFirstView",
        args = {boolean.class}
    )
    public void testSetAnimateFirstView() {
        mViewAnimator.setAnimateFirstView(true);
        mViewAnimator.setAnimateFirstView(false);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDisplayedChild",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDisplayedChild",
            args = {}
        )
    })
    @UiThreadTest
    public void testAccessDisplayedChild() {
        final View v1 = mActivity.findViewById(R.id.ok);
        final View v2 = mActivity.findViewById(R.id.cancel);
        final RelativeLayout parent = (RelativeLayout) v1.getParent();
        parent.removeView(v1);
        parent.removeView(v2);
        assertEquals(0, mViewAnimator.getChildCount());
        mViewAnimator.addView(v1);
        assertEquals(1, mViewAnimator.getChildCount());
        mViewAnimator.addView(v2);
        assertEquals(2, mViewAnimator.getChildCount());
        mViewAnimator.setDisplayedChild(0);
        assertEquals(0, mViewAnimator.getDisplayedChild());
        mViewAnimator.setDisplayedChild(-1);
        assertEquals(1, mViewAnimator.getDisplayedChild());
        mViewAnimator.setDisplayedChild(2);
        assertEquals(0, mViewAnimator.getDisplayedChild());
        mViewAnimator.setDisplayedChild(1);
        assertEquals(1, mViewAnimator.getDisplayedChild());
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDisplayedChild",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDisplayedChild",
            args = {}
        )
    })
    @UiThreadTest
    public void testAccessDisplayedChildBoundary() {
        final View v1 = mActivity.findViewById(R.id.ok);
        final View v2 = mActivity.findViewById(R.id.cancel);
        final RelativeLayout parent = (RelativeLayout) v1.getParent();
        parent.removeView(v1);
        parent.removeView(v2);
        assertEquals(0, mViewAnimator.getChildCount());
        mViewAnimator.addView(v1);
        assertEquals(1, mViewAnimator.getChildCount());
        mViewAnimator.addView(v2);
        assertEquals(2, mViewAnimator.getChildCount());
        int index = -1;
        mViewAnimator.setDisplayedChild(index);
        assertEquals(1, mViewAnimator.getDisplayedChild());
        index = 2;
        mViewAnimator.setDisplayedChild(index);
        assertEquals(0, mViewAnimator.getDisplayedChild());
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBaseline",
        args = {}
    )
    @UiThreadTest
    public void testGetBaseline() {
        final View v1 = mActivity.findViewById(R.id.ok);
        final View v2 = mActivity.findViewById(R.id.cancel);
        final RelativeLayout parent = (RelativeLayout) v1.getParent();
        parent.removeView(v1);
        parent.removeView(v2);
        assertEquals(0, mViewAnimator.getChildCount());
        mViewAnimator.addView(v1);
        mViewAnimator.addView(v2);
        assertEquals(2, mViewAnimator.getChildCount());
        int expected = v1.getBaseline();
        mViewAnimator.setDisplayedChild(0);
        assertEquals(expected, mViewAnimator.getBaseline());
        expected = v2.getBaseline();
        mViewAnimator.setDisplayedChild(1);
        assertEquals(expected, mViewAnimator.getBaseline());
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "showPrevious",
        args = {}
    )
    @UiThreadTest
    public void testShowPrevious() {
        final View v1 = mActivity.findViewById(R.id.ok);
        final View v2 = mActivity.findViewById(R.id.cancel);
        final View v3 = mActivity.findViewById(R.id.label);
        final View v4 = mActivity.findViewById(R.id.entry);
        final RelativeLayout parent = (RelativeLayout) v1.getParent();
        parent.removeView(v1);
        parent.removeView(v2);
        parent.removeView(v3);
        parent.removeView(v4);
        assertEquals(0, mViewAnimator.getChildCount());
        mViewAnimator.addView(v1);
        mViewAnimator.addView(v2);
        mViewAnimator.addView(v3);
        mViewAnimator.addView(v4);
        assertEquals(4, mViewAnimator.getChildCount());
        int current = 3;
        mViewAnimator.setDisplayedChild(current);
        assertEquals(current, mViewAnimator.getDisplayedChild());
        mViewAnimator.showPrevious();
        assertEquals(2, mViewAnimator.getDisplayedChild());
        mViewAnimator.showPrevious();
        assertEquals(1, mViewAnimator.getDisplayedChild());
        mViewAnimator.showPrevious();
        assertEquals(0, mViewAnimator.getDisplayedChild());
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCurrentView",
        args = {}
    )
    @UiThreadTest
    public void testGetCurrentView() {
        final View v = mActivity.findViewById(R.id.label);
        final RelativeLayout parent = (RelativeLayout) v.getParent();
        parent.removeView(v);
        assertEquals(0, mViewAnimator.getChildCount());
        mViewAnimator.addView(v);
        assertEquals(1, mViewAnimator.getChildCount());
        int current = 0;
        mViewAnimator.setDisplayedChild(current);
        assertSame(v, mViewAnimator.getCurrentView());
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addView",
            args = {android.view.View.class, int.class, android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeAllViews",
            args = {}
        )
    })
    @UiThreadTest
    public void testAddView() {
        final View v1 = mActivity.findViewById(R.id.ok);
        final View v2 = mActivity.findViewById(R.id.cancel);
        final RelativeLayout parent = (RelativeLayout) v1.getParent();
        parent.removeView(v1);
        parent.removeView(v2);
        assertEquals(0, mViewAnimator.getChildCount());
        LayoutParams p =
            new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mViewAnimator.addView(v1, 0, p);
        assertEquals(1, mViewAnimator.getChildCount());
        assertEquals(0, mViewAnimator.indexOfChild(v1));
        mViewAnimator.addView(v2, 1, p);
        assertEquals(2, mViewAnimator.getChildCount());
        assertEquals(1, mViewAnimator.indexOfChild(v2));
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOutAnimation",
            args = {android.view.animation.Animation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOutAnimation",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOutAnimation",
            args = {}
        )
    })
    public void testAccessOutAnimation() {
        AnimationSet expected = new AnimationSet(mActivity, mAttributeSet);
        assertNull(mViewAnimator.getOutAnimation());
        mViewAnimator.setOutAnimation(expected);
        assertSame(expected, mViewAnimator.getOutAnimation());
        mViewAnimator.setOutAnimation(null);
        assertNull(mViewAnimator.getOutAnimation());
        mViewAnimator.setOutAnimation(mActivity, R.anim.anim_alpha);
        Animation animation = mViewAnimator.getOutAnimation();
        assertTrue(animation.getInterpolator() instanceof AccelerateInterpolator);
        assertEquals(500, animation.getDuration());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeView",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeViewAt",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeViewInLayout",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeViews",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeViewsInLayout",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeAllViews",
            args = {}
        )
    })
    @UiThreadTest
    public void testRemoveViews() {
        final View v1 = mActivity.findViewById(R.id.ok);
        final View v2 = mActivity.findViewById(R.id.cancel);
        final View v3 = mActivity.findViewById(R.id.label);
        final View v4 = mActivity.findViewById(R.id.entry);
        final RelativeLayout parent = (RelativeLayout) v1.getParent();
        parent.removeView(v1);
        parent.removeView(v2);
        parent.removeView(v3);
        parent.removeView(v4);
        assertEquals(0, mViewAnimator.getChildCount());
        mViewAnimator.addView(v1);
        mViewAnimator.addView(v2);
        mViewAnimator.addView(v3);
        mViewAnimator.addView(v4);
        assertEquals(4, mViewAnimator.getChildCount());
        mViewAnimator.removeViewAt(3);
        assertEquals(3, mViewAnimator.getChildCount());
        assertSame(v1, mViewAnimator.getChildAt(0));
        assertSame(v2, mViewAnimator.getChildAt(1));
        assertSame(v3, mViewAnimator.getChildAt(2));
        mViewAnimator.removeView(v3);
        assertEquals(2, mViewAnimator.getChildCount());
        assertSame(v1, mViewAnimator.getChildAt(0));
        assertSame(v2, mViewAnimator.getChildAt(1));
        mViewAnimator.removeViewInLayout(v2);
        assertEquals(1, mViewAnimator.getChildCount());
        assertSame(v1, mViewAnimator.getChildAt(0));
        mViewAnimator.addView(v2);
        mViewAnimator.addView(v3);
        mViewAnimator.addView(v4);
        assertEquals(4, mViewAnimator.getChildCount());
        mViewAnimator.removeViews(0, 2);
        assertEquals(2, mViewAnimator.getChildCount());
        assertSame(v3, mViewAnimator.getChildAt(0));
        assertSame(v4, mViewAnimator.getChildAt(1));
        mViewAnimator.removeViewsInLayout(1, 1);
        assertEquals(1, mViewAnimator.getChildCount());
        assertSame(v3, mViewAnimator.getChildAt(0));
        mViewAnimator.removeAllViews();
        assertEquals(0, mViewAnimator.getChildCount());
    }
}
