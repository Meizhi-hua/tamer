public class MenuLayoutLandscapeTest extends ActivityInstrumentationTestCase<MenuLayoutLandscape> {
    private static final String LONG_TITLE = "Really really really really really really really really really really long title";
    private static final String SHORT_TITLE = "Item";
    private MenuLayout mActivity;
    public MenuLayoutLandscapeTest() {
        super("com.android.frameworks.coretests", MenuLayoutLandscape.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    public void testPreconditions() {
        assertNotNull(mActivity);
    }
    private void toggleMenu() {
        getInstrumentation().waitForIdleSync();
        KeyUtils.tapMenuKey(this);
        getInstrumentation().waitForIdleSync();
    }
    private void assertLayout(Integer... expectedLayout) {
        toggleMenu();
        IconMenuView iconMenuView = ((IconMenuView) mActivity.getMenuView(MenuBuilder.TYPE_ICON));
        int[] layout = iconMenuView.getLayout();
        int layoutNumRows = iconMenuView.getLayoutNumRows(); 
        int expectedRows = expectedLayout.length;
        assertEquals("Row mismatch", expectedRows, layoutNumRows);
        for (int row = 0; row < expectedRows; row++) {
            assertEquals("Col mismatch on row " + row, expectedLayout[row].intValue(),
                    layout[row]);
        }
    }
    public void test1ShortItem() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(1)
                .setItemTitle(0, SHORT_TITLE));
        assertLayout(1);
    }
    public void test1LongItem() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(1)
                .setItemTitle(0, LONG_TITLE));
        assertLayout(1);
    }
    public void test2LongItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(2)
                .setItemTitle(0, LONG_TITLE)
                .setItemTitle(1, LONG_TITLE));
        assertLayout(1, 1);
    }
    public void test2ShortItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(2)
                .setItemTitle(0, SHORT_TITLE)
                .setItemTitle(1, SHORT_TITLE));
        assertLayout(2);
    }
    public void test3ShortItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(3)
                .setItemTitle(0, SHORT_TITLE)
                .setItemTitle(1, SHORT_TITLE)
                .setItemTitle(2, SHORT_TITLE));
        assertLayout(3);
    }
    public void test3VarietyItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(3)
                .setItemTitle(0, SHORT_TITLE)
                .setItemTitle(1, LONG_TITLE)
                .setItemTitle(2, SHORT_TITLE));
        assertLayout(1, 2);
    }
    public void test3VarietyItems2() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(3)
                .setItemTitle(0, LONG_TITLE)
                .setItemTitle(1, SHORT_TITLE)
                .setItemTitle(2, SHORT_TITLE));
        assertLayout(1, 2);
    }
    public void test4LongItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(4)
                .setItemTitle(0, LONG_TITLE)
                .setItemTitle(1, LONG_TITLE)
                .setItemTitle(2, LONG_TITLE)
                .setItemTitle(3, LONG_TITLE));
        assertLayout(2, 2);
    }
    public void test4ShortItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(4)
                .setItemTitle(0, SHORT_TITLE)
                .setItemTitle(1, SHORT_TITLE)
                .setItemTitle(2, SHORT_TITLE)
                .setItemTitle(3, SHORT_TITLE));
        assertLayout(4);
    }
    public void test4VarietyItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(4)
                .setItemTitle(0, LONG_TITLE)
                .setItemTitle(1, SHORT_TITLE)
                .setItemTitle(2, SHORT_TITLE)
                .setItemTitle(3, SHORT_TITLE));
        assertLayout(2, 2);
    }
    public void test5ShortItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(5)
                .setItemTitle(0, SHORT_TITLE)
                .setItemTitle(1, SHORT_TITLE)
                .setItemTitle(2, SHORT_TITLE)
                .setItemTitle(3, SHORT_TITLE)
                .setItemTitle(4, SHORT_TITLE));
        assertLayout(5);
    }
    public void test5LongItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(5)
                .setItemTitle(0, LONG_TITLE)
                .setItemTitle(1, LONG_TITLE)
                .setItemTitle(2, LONG_TITLE)
                .setItemTitle(3, LONG_TITLE)
                .setItemTitle(4, LONG_TITLE));
        assertLayout(2, 3);
    }
    public void test5VarietyItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(5)
                .setItemTitle(0, LONG_TITLE)
                .setItemTitle(1, SHORT_TITLE)
                .setItemTitle(2, LONG_TITLE)
                .setItemTitle(3, SHORT_TITLE)
                .setItemTitle(4, LONG_TITLE));
        assertLayout(2, 3);
    }
    public void test6LongItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(6)
                .setItemTitle(0, LONG_TITLE)
                .setItemTitle(1, LONG_TITLE)
                .setItemTitle(2, LONG_TITLE)
                .setItemTitle(3, LONG_TITLE)
                .setItemTitle(4, LONG_TITLE)
                .setItemTitle(5, LONG_TITLE));
        assertLayout(3, 3);
    }
    public void test6ShortItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(6)
                .setItemTitle(0, SHORT_TITLE)
                .setItemTitle(1, SHORT_TITLE)
                .setItemTitle(2, SHORT_TITLE)
                .setItemTitle(3, SHORT_TITLE)
                .setItemTitle(4, SHORT_TITLE)
                .setItemTitle(5, SHORT_TITLE));
        assertLayout(6);
    }
    public void test6VarietyItems() {
        mActivity.setParams(new MenuScenario.Params()
                .setNumItems(6)
                .setItemTitle(0, SHORT_TITLE)
                .setItemTitle(1, LONG_TITLE)
                .setItemTitle(2, SHORT_TITLE)
                .setItemTitle(3, LONG_TITLE)
                .setItemTitle(4, SHORT_TITLE)
                .setItemTitle(5, SHORT_TITLE));
        assertLayout(3, 3);
    }
}
