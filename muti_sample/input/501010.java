@TestTargetClass(View.class)
public class View_FocusHandlingTest
        extends ActivityInstrumentationTestCase2<FocusHandlingStubActivity> {
    public View_FocusHandlingTest() {
        super("com.android.cts.stub", FocusHandlingStubActivity.class);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFocusable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasFocusable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusRightId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusDownId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusLeftId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNextFocusUpId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusRightId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusDownId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusLeftId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNextFocusUpId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "focusSearch",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestFocus",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestFocus",
            args = {int.class, Rect.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestFocusFromTouch",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFocused",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisibility",
            args = {int.class}
        )
    })
    @UiThreadTest
    public void testFocusHandling() {
        Activity activity = getActivity();
        View v1 = activity.findViewById(R.id.view1);
        View v2 = activity.findViewById(R.id.view2);
        View v3 = activity.findViewById(R.id.view3);
        View v4 = activity.findViewById(R.id.view4);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v3);
        assertNotNull(v4);
        assertFalse(v1.isFocusable());
        assertFalse(v2.isFocusable());
        assertFalse(v3.isFocusable());
        assertFalse(v4.isFocusable());
        v1.setFocusable(true);
        v2.setFocusable(true);
        v3.setFocusable(true);
        v4.setFocusable(true);
        assertTrue(v1.isFocusable());
        assertTrue(v2.isFocusable());
        assertTrue(v3.isFocusable());
        assertTrue(v4.isFocusable());
        v1.setNextFocusRightId(R.id.view2);
        v1.setNextFocusDownId(R.id.view3);
        v2.setNextFocusLeftId(R.id.view1);
        v2.setNextFocusDownId(R.id.view4);
        v3.setNextFocusRightId(R.id.view4);
        v3.setNextFocusUpId(R.id.view1);
        v4.setNextFocusLeftId(R.id.view3);
        v4.setNextFocusUpId(R.id.view2);
        assertEquals(R.id.view2, v1.getNextFocusRightId());
        assertEquals(R.id.view3, v1.getNextFocusDownId());
        assertEquals(R.id.view1, v2.getNextFocusLeftId());
        assertEquals(R.id.view4, v2.getNextFocusDownId());
        assertEquals(R.id.view1, v3.getNextFocusUpId());
        assertEquals(R.id.view4, v3.getNextFocusRightId());
        assertEquals(R.id.view2, v4.getNextFocusUpId());
        assertEquals(R.id.view3, v4.getNextFocusLeftId());
        assertSame(v2, v1.focusSearch(View.FOCUS_RIGHT));
        assertSame(v3, v1.focusSearch(View.FOCUS_DOWN));
        assertSame(v1, v2.focusSearch(View.FOCUS_LEFT));
        assertSame(v4, v2.focusSearch(View.FOCUS_DOWN));
        assertSame(v1, v3.focusSearch(View.FOCUS_UP));
        assertSame(v4, v3.focusSearch(View.FOCUS_RIGHT));
        assertSame(v2, v4.focusSearch(View.FOCUS_UP));
        assertSame(v3, v4.focusSearch(View.FOCUS_LEFT));
        assertTrue(v1.requestFocus());
        assertTrue(v1.hasFocus());
        assertFalse(v2.hasFocus());
        assertFalse(v3.hasFocus());
        assertFalse(v4.hasFocus());
        v1.setVisibility(View.INVISIBLE);
        assertFalse(v1.hasFocus());
        assertTrue(v2.hasFocus());
        assertFalse(v3.hasFocus());
        assertFalse(v4.hasFocus());
        v2.setVisibility(View.INVISIBLE);
        assertFalse(v1.hasFocus());
        assertFalse(v2.hasFocus());
        assertTrue(v3.hasFocus());
        assertFalse(v4.hasFocus());
        v3.setVisibility(View.INVISIBLE);
        assertFalse(v1.isFocused());
        assertFalse(v2.isFocused());
        assertFalse(v3.isFocused());
        assertTrue(v4.isFocused());
        v4.setVisibility(View.INVISIBLE);
        assertFalse(v1.isFocused());
        assertFalse(v2.isFocused());
        assertFalse(v3.isFocused());
        assertFalse(v4.isFocused());
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.VISIBLE);
        v3.setVisibility(View.VISIBLE);
        v4.setVisibility(View.VISIBLE);
        assertTrue(v1.isFocused());
        assertFalse(v2.isFocused());
        assertFalse(v3.isFocused());
        assertFalse(v4.isFocused());
        v2.setFocusable(false);
        v3.setFocusable(false);
        assertTrue(v1.isFocusable());
        assertFalse(v2.isFocusable());
        assertFalse(v3.isFocusable());
        assertTrue(v4.isFocusable());
        v1.setVisibility(View.INVISIBLE);
        assertFalse(v1.hasFocus());
        assertFalse(v2.hasFocus());
        assertFalse(v3.hasFocus());
        assertTrue(v4.hasFocus());
        assertTrue(v1.isFocusable());
        assertFalse(v1.hasFocusable());
        v1.setVisibility(View.VISIBLE);
        v2.setFocusable(true);
        v3.setFocusable(true);
        assertTrue(v1.hasFocusable());
        assertTrue(v2.hasFocusable());
        assertTrue(v3.hasFocusable());
        assertTrue(v4.hasFocusable());
        assertTrue(v2.requestFocus(View.FOCUS_UP));
        assertFalse(v1.hasFocus());
        assertTrue(v2.hasFocus());
        assertFalse(v3.hasFocus());
        assertFalse(v4.hasFocus());
        assertTrue(v1.requestFocus(View.FOCUS_LEFT, null));
        assertTrue(v1.hasFocus());
        assertFalse(v2.hasFocus());
        assertFalse(v3.hasFocus());
        assertFalse(v4.hasFocus());
        assertTrue(v3.requestFocus());
        assertFalse(v1.hasFocus());
        assertFalse(v2.hasFocus());
        assertTrue(v3.hasFocus());
        assertFalse(v4.hasFocus());
        assertTrue(v4.requestFocusFromTouch());
        assertFalse(v1.hasFocus());
        assertFalse(v2.hasFocus());
        assertFalse(v3.hasFocus());
        assertTrue(v4.hasFocus());
        v4.clearFocus();
        assertTrue(v1.hasFocus());
        assertFalse(v2.hasFocus());
        assertFalse(v3.hasFocus());
        assertFalse(v4.hasFocus());
        assertSame(v1, v1.findFocus());
        assertNull(v2.findFocus());
        assertNull(v3.findFocus());
        assertNull(v4.findFocus());
    }
}
