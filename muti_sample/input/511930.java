public class ListItemFocusablesCloseTest extends ActivityInstrumentationTestCase<ListItemFocusablesClose> {
    private ListView mListView;
    private int mListTop;
    private int mListBottom;
    public ListItemFocusablesCloseTest() {
        super("com.android.frameworks.coretests", ListItemFocusablesClose.class);
    }
    @Override
    protected void setUp() throws Exception{
        super.setUp();
        mListView = getActivity().getListView();
        mListTop = mListView.getListPaddingTop();
        mListBottom = mListView.getHeight() - mListView.getListPaddingBottom();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mListView);
        assertTrue(mListView.getAdapter().areAllItemsEnabled());
        assertTrue(mListView.getItemsCanFocus());
        assertEquals(0, mListView.getSelectedItemPosition());
        final LinearLayout first = (LinearLayout) mListView.getSelectedView();
        getInstrumentation().waitForIdleSync();
        assertEquals("first item should be at top of screen",
                mListView.getListPaddingTop(),
                first.getTop());
        assertTrue("first button of first list item should have focus",
                first.getChildAt(0).isFocused());
        assertTrue("item should be shorter than list for this test to make sense",
                first.getHeight() < mListView.getHeight());
        assertEquals("two items should be on screen",
                2, mListView.getChildCount());
        assertTrue("first button of second item should be on screen",
                getActivity().getChildOfItem(1, 0).getBottom() < mListBottom);
    }
    @MediumTest
    public void testChangeFocusWithinItem() {
        final LinearLayout first = (LinearLayout) mListView.getSelectedView();
        final int topOfFirstItemBefore = first.getTop();
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("focus should have moved to second button of first item",
                first.getChildAt(2).isFocused());
        assertEquals("selection should not have changed",
                0, mListView.getSelectedItemPosition());
        assertEquals("list item should not have been shifted",
                topOfFirstItemBefore, first.getTop());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue("focus should have moved back to first button of first item",
                first.getChildAt(0).isFocused());
        assertEquals("list item should not have been shifted",
                topOfFirstItemBefore, first.getTop());
    }
    @MediumTest
    public void testMoveDownToButtonInDifferentSelection() {
        final LinearLayout first = (LinearLayout) mListView.getSelectedView();
        final int topOfFirstItemBefore = first.getTop();
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selection should have moved to second item",
                1, mListView.getSelectedItemPosition());
        final LinearLayout selectedItem = (LinearLayout) mListView.getSelectedView();
        assertTrue("first button of second item should have focus",
                selectedItem.getChildAt(0).isFocused());
        assertEquals("list item should not have been shifted",
                topOfFirstItemBefore, first.getTop());
    }
    @MediumTest
    public void testMoveUpToButtonInDifferentSelection() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(1, mListView.getSelectedItemPosition());
        assertTrue("first button of second item should have focus",
                getActivity().getChildOfItem(1, 0).hasFocus());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("first list item should have selection", 0,
                mListView.getSelectedItemPosition());
        assertTrue("second button of first item should have focus",
                getActivity().getChildOfItem(0, 2).hasFocus());
    }
}
