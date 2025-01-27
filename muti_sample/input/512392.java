public class GroupingListAdapterTests extends AndroidTestCase {
    static private final String[] CALL_LOG_PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.DATE,
    };
    private static final int CALLS_NUMBER_COLUMN_INDEX = 1;
    private MatrixCursor mCursor;
    private long mNextCall;
    private GroupingListAdapter mAdapter = new GroupingListAdapter(null) {
        @Override
        protected void addGroups(Cursor cursor) {
            int count = cursor.getCount();
            int groupItemCount = 1;
            cursor.moveToFirst();
            String currentValue = cursor.getString(CALLS_NUMBER_COLUMN_INDEX);
            for (int i = 1; i < count; i++) {
                cursor.moveToNext();
                String value = cursor.getString(CALLS_NUMBER_COLUMN_INDEX);
                if (TextUtils.equals(value, currentValue)) {
                    groupItemCount++;
                } else {
                    if (groupItemCount > 1) {
                        addGroup(i - groupItemCount, groupItemCount, false);
                    }
                    groupItemCount = 1;
                    currentValue = value;
                }
            }
            if (groupItemCount > 1) {
                addGroup(count - groupItemCount, groupItemCount, false);
            }
        }
        @Override
        protected void bindChildView(View view, Context context, Cursor cursor) {
        }
        @Override
        protected void bindGroupView(View view, Context context, Cursor cursor, int groupSize,
                boolean expanded) {
        }
        @Override
        protected void bindStandAloneView(View view, Context context, Cursor cursor) {
        }
        @Override
        protected View newChildView(Context context, ViewGroup parent) {
            return null;
        }
        @Override
        protected View newGroupView(Context context, ViewGroup parent) {
            return null;
        }
        @Override
        protected View newStandAloneView(Context context, ViewGroup parent) {
            return null;
        }
    };
    private void buildCursor(String... numbers) {
        mCursor = new MatrixCursor(CALL_LOG_PROJECTION);
        mNextCall = 1;
        for (String number : numbers) {
            mCursor.addRow(new Object[]{mNextCall, number, 1000 - mNextCall});
            mNextCall++;
        }
    }
    public void testGroupingWithoutGroups() {
        buildCursor("1", "2", "3");
        mAdapter.changeCursor(mCursor);
        assertEquals(3, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_STANDALONE, false, 1);
        assertPositionMetadata(2, ITEM_TYPE_STANDALONE, false, 2);
    }
    public void testGroupingWithCollapsedGroupAtTheBeginning() {
        buildCursor("1", "1", "2");
        mAdapter.changeCursor(mCursor);
        assertEquals(2, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_GROUP_HEADER, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_STANDALONE, false, 2);
    }
    public void testGroupingWithExpandedGroupAtTheBeginning() {
        buildCursor("1", "1", "2");
        mAdapter.changeCursor(mCursor);
        mAdapter.toggleGroup(0);
        assertEquals(4, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_GROUP_HEADER, true, 0);
        assertPositionMetadata(1, ITEM_TYPE_IN_GROUP, false, 0);
        assertPositionMetadata(2, ITEM_TYPE_IN_GROUP, false, 1);
        assertPositionMetadata(3, ITEM_TYPE_STANDALONE, false, 2);
    }
    public void testGroupingWithExpandCollapseCycleAtTheBeginning() {
        buildCursor("1", "1", "2");
        mAdapter.changeCursor(mCursor);
        mAdapter.toggleGroup(0);
        mAdapter.toggleGroup(0);
        assertEquals(2, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_GROUP_HEADER, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_STANDALONE, false, 2);
    }
    public void testGroupingWithCollapsedGroupInTheMiddle() {
        buildCursor("1", "2", "2", "2", "3");
        mAdapter.changeCursor(mCursor);
        assertEquals(3, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, false, 1);
        assertPositionMetadata(2, ITEM_TYPE_STANDALONE, false, 4);
    }
    public void testGroupingWithExpandedGroupInTheMiddle() {
        buildCursor("1", "2", "2", "2", "3");
        mAdapter.changeCursor(mCursor);
        mAdapter.toggleGroup(1);
        assertEquals(6, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, true, 1);
        assertPositionMetadata(2, ITEM_TYPE_IN_GROUP, false, 1);
        assertPositionMetadata(3, ITEM_TYPE_IN_GROUP, false, 2);
        assertPositionMetadata(4, ITEM_TYPE_IN_GROUP, false, 3);
        assertPositionMetadata(5, ITEM_TYPE_STANDALONE, false, 4);
    }
    public void testGroupingWithCollapsedGroupAtTheEnd() {
        buildCursor("1", "2", "3", "3", "3");
        mAdapter.changeCursor(mCursor);
        assertEquals(3, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_STANDALONE, false, 1);
        assertPositionMetadata(2, ITEM_TYPE_GROUP_HEADER, false, 2);
    }
    public void testGroupingWithExpandedGroupAtTheEnd() {
        buildCursor("1", "2", "3", "3", "3");
        mAdapter.changeCursor(mCursor);
        mAdapter.toggleGroup(2);
        assertEquals(6, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_STANDALONE, false, 1);
        assertPositionMetadata(2, ITEM_TYPE_GROUP_HEADER, true, 2);
        assertPositionMetadata(3, ITEM_TYPE_IN_GROUP, false, 2);
        assertPositionMetadata(4, ITEM_TYPE_IN_GROUP, false, 3);
        assertPositionMetadata(5, ITEM_TYPE_IN_GROUP, false, 4);
    }
    public void testGroupingWithMultipleCollapsedGroups() {
        buildCursor("1", "2", "2", "3", "4", "4", "5", "5", "6");
        mAdapter.changeCursor(mCursor);
        assertEquals(6, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, false, 1);
        assertPositionMetadata(2, ITEM_TYPE_STANDALONE, false, 3);
        assertPositionMetadata(3, ITEM_TYPE_GROUP_HEADER, false, 4);
        assertPositionMetadata(4, ITEM_TYPE_GROUP_HEADER, false, 6);
        assertPositionMetadata(5, ITEM_TYPE_STANDALONE, false, 8);
    }
    public void testGroupingWithMultipleExpandedGroups() {
        buildCursor("1", "2", "2", "3", "4", "4", "5", "5", "6");
        mAdapter.changeCursor(mCursor);
        mAdapter.toggleGroup(1);
        mAdapter.toggleGroup(6);
        assertEquals(10, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, true, 1);
        assertPositionMetadata(2, ITEM_TYPE_IN_GROUP, false, 1);
        assertPositionMetadata(3, ITEM_TYPE_IN_GROUP, false, 2);
        assertPositionMetadata(4, ITEM_TYPE_STANDALONE, false, 3);
        assertPositionMetadata(5, ITEM_TYPE_GROUP_HEADER, false, 4);
        assertPositionMetadata(6, ITEM_TYPE_GROUP_HEADER, true, 6);
        assertPositionMetadata(7, ITEM_TYPE_IN_GROUP, false, 6);
        assertPositionMetadata(8, ITEM_TYPE_IN_GROUP, false, 7);
        assertPositionMetadata(9, ITEM_TYPE_STANDALONE, false, 8);
    }
    public void testPositionCache() {
        buildCursor("1", "2", "2", "3", "4", "4", "5", "5", "6");
        mAdapter.changeCursor(mCursor);
        assertEquals(6, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, false, 1);
        assertPositionMetadata(2, ITEM_TYPE_STANDALONE, false, 3);
        assertPositionMetadata(3, ITEM_TYPE_GROUP_HEADER, false, 4);
        assertPositionMetadata(4, ITEM_TYPE_GROUP_HEADER, false, 6);
        assertPositionMetadata(5, ITEM_TYPE_STANDALONE, false, 8);
        assertEquals(6, mAdapter.getCount());
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, false, 1);
        assertPositionMetadata(2, ITEM_TYPE_STANDALONE, false, 3);
        assertPositionMetadata(3, ITEM_TYPE_GROUP_HEADER, false, 4);
        assertPositionMetadata(4, ITEM_TYPE_GROUP_HEADER, false, 6);
        assertPositionMetadata(5, ITEM_TYPE_STANDALONE, false, 8);
        mAdapter.toggleGroup(1);
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, true, 1);
        assertPositionMetadata(2, ITEM_TYPE_IN_GROUP, false, 1);
        assertPositionMetadata(3, ITEM_TYPE_IN_GROUP, false, 2);
        assertPositionMetadata(4, ITEM_TYPE_STANDALONE, false, 3);
        assertPositionMetadata(5, ITEM_TYPE_GROUP_HEADER, false, 4);
        assertPositionMetadata(6, ITEM_TYPE_GROUP_HEADER, false, 6);
        assertPositionMetadata(7, ITEM_TYPE_STANDALONE, false, 8);
        assertPositionMetadata(0, ITEM_TYPE_STANDALONE, false, 0);
        assertPositionMetadata(1, ITEM_TYPE_GROUP_HEADER, true, 1);
        assertPositionMetadata(2, ITEM_TYPE_IN_GROUP, false, 1);
        assertPositionMetadata(3, ITEM_TYPE_IN_GROUP, false, 2);
        assertPositionMetadata(4, ITEM_TYPE_STANDALONE, false, 3);
        assertPositionMetadata(5, ITEM_TYPE_GROUP_HEADER, false, 4);
        assertPositionMetadata(6, ITEM_TYPE_GROUP_HEADER, false, 6);
        assertPositionMetadata(7, ITEM_TYPE_STANDALONE, false, 8);
    }
    public void testGroupDescriptorArrayGrowth() {
        String[] numbers = new String[500];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = String.valueOf((i / 2) * 2);
        }
        buildCursor(numbers);
        mAdapter.changeCursor(mCursor);
        assertEquals(250, mAdapter.getCount());
    }
    private void assertPositionMetadata(int position, int itemType, boolean isExpanded,
            int cursorPosition) {
        GroupingListAdapter.PositionMetadata metadata = new GroupingListAdapter.PositionMetadata();
        mAdapter.obtainPositionMetadata(metadata, position);
        assertEquals(itemType, metadata.itemType);
        if (metadata.itemType == ITEM_TYPE_GROUP_HEADER) {
            assertEquals(isExpanded, metadata.isExpanded);
        }
        assertEquals(cursorPosition, metadata.cursorPosition);
    }
}
