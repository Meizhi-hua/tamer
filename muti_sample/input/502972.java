@TestTargetClass(TabWidget.class)
public class TabWidgetTest extends ActivityInstrumentationTestCase2<TabHostStubActivity> {
    private Activity mActivity;
    public TabWidgetTest() {
        super("com.android.cts.stub", TabHostStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TabWidget",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TabWidget",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TabWidget",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new TabWidget(mActivity);
        new TabWidget(mActivity, null);
        new TabWidget(mActivity, null, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "childDrawableStateChanged",
        args = {android.view.View.class}
    )
    public void testChildDrawableStateChanged() {
        MockTabWidget mockTabWidget = new MockTabWidget(mActivity);
        TextView tv0 = new TextView(mActivity);
        TextView tv1 = new TextView(mActivity);
        mockTabWidget.addView(tv0);
        mockTabWidget.addView(tv1);
        mockTabWidget.setCurrentTab(1);
        mockTabWidget.reset();
        mockTabWidget.childDrawableStateChanged(tv0);
        assertFalse(mockTabWidget.hasCalledInvalidate());
        mockTabWidget.reset();
        mockTabWidget.childDrawableStateChanged(tv1);
        assertTrue(mockTabWidget.hasCalledInvalidate());
        mockTabWidget.reset();
        mockTabWidget.childDrawableStateChanged(null);
        assertFalse(mockTabWidget.hasCalledInvalidate());
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "dispatchDraw",
        args = {android.graphics.Canvas.class}
    )
    public void testDispatchDraw() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCurrentTab",
        args = {int.class}
    )
    @UiThreadTest
    public void testSetCurrentTab() {
        TabHostStubActivity activity = getActivity();
        TabWidget tabWidget = activity.getTabWidget();
        tabWidget.addView(new TextView(mActivity));
        assertTrue(tabWidget.getChildAt(0).isSelected());
        assertFalse(tabWidget.getChildAt(1).isSelected());
        assertTrue(tabWidget.getChildAt(0).isFocused());
        assertFalse(tabWidget.getChildAt(1).isFocused());
        tabWidget.setCurrentTab(1);
        assertFalse(tabWidget.getChildAt(0).isSelected());
        assertTrue(tabWidget.getChildAt(1).isSelected());
        assertTrue(tabWidget.getChildAt(0).isFocused());
        assertFalse(tabWidget.getChildAt(1).isFocused());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "focusCurrentTab",
        args = {int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for focusCurrentTab() is incomplete." +
            " not clear what is supposed to happen if a wrong index passed to this method.")
    @UiThreadTest
    public void testFocusCurrentTab() {
        TabHostStubActivity activity = getActivity();
        TabWidget tabWidget = activity.getTabWidget();
        tabWidget.addView(new TextView(mActivity));
        assertTrue(tabWidget.getChildAt(0).isSelected());
        assertFalse(tabWidget.getChildAt(1).isSelected());
        assertEquals(tabWidget.getChildAt(0), tabWidget.getFocusedChild());
        assertTrue(tabWidget.getChildAt(0).isFocused());
        assertFalse(tabWidget.getChildAt(1).isFocused());
        tabWidget.focusCurrentTab(1);
        assertFalse(tabWidget.getChildAt(0).isSelected());
        assertTrue(tabWidget.getChildAt(1).isSelected());
        assertEquals(tabWidget.getChildAt(1), tabWidget.getFocusedChild());
        assertFalse(tabWidget.getChildAt(0).isFocused());
        assertTrue(tabWidget.getChildAt(1).isFocused());
        tabWidget.focusCurrentTab(0);
        assertTrue(tabWidget.getChildAt(0).isSelected());
        assertFalse(tabWidget.getChildAt(1).isSelected());
        assertEquals(tabWidget.getChildAt(0), tabWidget.getFocusedChild());
        assertTrue(tabWidget.getChildAt(0).isFocused());
        assertFalse(tabWidget.getChildAt(1).isFocused());
        try {
            tabWidget.focusCurrentTab(-1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            tabWidget.focusCurrentTab(tabWidget.getChildCount() + 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setEnabled",
        args = {boolean.class}
    )
    public void testSetEnabled() {
        TabWidget tabWidget = new TabWidget(mActivity);
        tabWidget.addView(new TextView(mActivity));
        tabWidget.addView(new TextView(mActivity));
        assertTrue(tabWidget.isEnabled());
        assertTrue(tabWidget.getChildAt(0).isEnabled());
        assertTrue(tabWidget.getChildAt(1).isEnabled());
        tabWidget.setEnabled(false);
        assertFalse(tabWidget.isEnabled());
        assertFalse(tabWidget.getChildAt(0).isEnabled());
        assertFalse(tabWidget.getChildAt(1).isEnabled());
        tabWidget.setEnabled(true);
        assertTrue(tabWidget.isEnabled());
        assertTrue(tabWidget.getChildAt(0).isEnabled());
        assertTrue(tabWidget.getChildAt(1).isEnabled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addView",
        args = {android.view.View.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for addView() is incomplete." +
            "1. not clear what is supposed to happen if child is null." +
            "2. not clear what is supposed to happen if child is a kind of AdapterView." +
            "3. In javadoc, it says that the default parameters are set on the child " +
            "   if no layout parameters are already set on the child, but the default width " +
            "   and weight is not equals what returned from generateDefaultLayoutParams().")
    public void testAddView() {
        MockTabWidget mockTabWidget = new MockTabWidget(mActivity);
        View view1 = new TextView(mActivity);
        mockTabWidget.addView(view1);
        assertSame(view1, mockTabWidget.getChildAt(0));
        LayoutParams defaultLayoutParam = mockTabWidget.generateDefaultLayoutParams();
        if (mockTabWidget.getOrientation() == LinearLayout.VERTICAL) {
            assertEquals(defaultLayoutParam.height, LayoutParams.WRAP_CONTENT);
            assertEquals(defaultLayoutParam.width, LayoutParams.MATCH_PARENT);
        } else if (mockTabWidget.getOrientation() == LinearLayout.HORIZONTAL) {
            assertEquals(defaultLayoutParam.height, LayoutParams.WRAP_CONTENT);
            assertEquals(defaultLayoutParam.width, LayoutParams.WRAP_CONTENT);
        } else {
            assertNull(defaultLayoutParam);
        }
        View view2 = new RelativeLayout(mActivity);
        mockTabWidget.addView(view2);
        assertSame(view2, mockTabWidget.getChildAt(1));
        try {
            mockTabWidget.addView(new ListView(mActivity));
            fail("did not throw RuntimeException when adding invalid view");
        } catch (RuntimeException e) {
        }
        try {
            mockTabWidget.addView(null);
            fail("did not throw NullPointerException when child is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onFocusChange",
        args = {android.view.View.class, boolean.class}
    )
    public void testOnFocusChange() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onSizeChanged",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testOnSizeChanged() {
    }
    private class MockTabWidget extends TabWidget {
        private boolean mCalledInvalidate = false;
        public MockTabWidget(Context context) {
            super(context);
        }
        @Override
        protected LayoutParams generateDefaultLayoutParams() {
            return super.generateDefaultLayoutParams();
        }
        @Override
        public void invalidate() {
            super.invalidate();
            mCalledInvalidate = true;
        }
        public boolean hasCalledInvalidate() {
            return mCalledInvalidate;
        }
        public void reset() {
            mCalledInvalidate = false;
        }
    }
}
