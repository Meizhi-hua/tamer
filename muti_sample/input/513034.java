public class ListOfThinItemsTest extends ActivityInstrumentationTestCase<ListOfThinItems> {
    private ListView mListView;
    public ListOfThinItemsTest() {
        super("com.android.frameworks.coretests", ListOfThinItems.class);
    }
    @Override
    protected void setUp() throws Exception{
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mListView);
        assertTrue("need item height less than fading edge length",
                mListView.getSelectedView().getHeight() < mListView.getVerticalFadingEdgeLength());
        assertTrue("need items off screen",
                mListView.getChildCount() < mListView.getAdapter().getCount());
    }
    @LargeTest
    public void testScrollToBottom() {
        final int numItems = mListView.getAdapter().getCount();
        final int listBottom = mListView.getHeight() - mListView.getListPaddingBottom();
        for (int i = 0; i < numItems; i++) {
            assertEquals("wrong selection at position " + i,
                    i, mListView.getSelectedItemPosition());
            final int bottomFadingEdge = listBottom - mListView.getVerticalFadingEdgeLength();
            final View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
            final int lastVisiblePosition = lastChild.getId();
            int bottomThreshold = (lastVisiblePosition < mListView.getAdapter().getCount() - 1) ?
                    bottomFadingEdge : listBottom;
            String prefix = "after " + i + " down presses, ";
            assertTrue(prefix + "selected item is below bottom threshold (fading edge or bottom as " +
                    "appropriate)",
                    mListView.getSelectedView().getBottom() <= bottomThreshold);
            assertTrue(prefix + "first item in list must be at very top or just above",
                    mListView.getChildAt(0).getTop() <= 0);
            assertTrue(prefix + "last item in list should be at very bottom or just below",
                    lastChild.getBottom() >= listBottom);
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
    }
    @LargeTest
    public void testScrollToTop() {
        final int numItems = mListView.getAdapter().getCount();
        for (int i = 0; i < numItems - 1; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("should have moved to last position",
                numItems - 1, mListView.getSelectedItemPosition());
        int listTop = mListView.getListPaddingTop();
        final int listBottom = mListView.getHeight() - mListView.getListPaddingBottom();
        for (int i = 0; i < numItems; i++) {
            int expectedPostion = numItems - (i + 1);
            assertEquals("wrong selection at position " + expectedPostion,
                    expectedPostion, mListView.getSelectedItemPosition());
            final int topFadingEdge = listTop + mListView.getVerticalFadingEdgeLength();
            final View firstChild = mListView.getChildAt(0);
            final View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
            final int firstVisiblePosition = firstChild.getId();
            int topThreshold = (firstVisiblePosition > 0) ?
                    topFadingEdge : listTop;
            String prefix = "after " + i + " up presses, ";
            assertTrue(prefix + "selected item is above top threshold (fading edge or top as " +
                    "appropriate)",
                    mListView.getSelectedView().getTop() >= topThreshold);
            assertTrue(prefix + "first item in list must be at very top or just above",
                    firstChild.getTop() <= 0);
            assertTrue(prefix + "last item in list should be at very bottom or just below",
                    lastChild.getBottom() >= listBottom);
            sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        }
    }
}
