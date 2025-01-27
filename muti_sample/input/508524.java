public class GridStackFromBottomTest extends ActivityInstrumentationTestCase<GridStackFromBottom> {
    private GridStackFromBottom mActivity;
    private GridView mGridView;
    public GridStackFromBottomTest() {
        super("com.android.frameworks.coretests", GridStackFromBottom.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mGridView = getActivity().getGridView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mGridView);
        assertEquals(mGridView.getAdapter().getCount() - 1, mGridView.getSelectedItemPosition());
    }
}
