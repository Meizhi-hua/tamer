public class SourceShortcutRefresherTest extends AndroidTestCase {
    private final String mQuery = "foo";
    private MockNamedTaskExecutor mExecutor;
    private SourceShortcutRefresher mRefresher;
    private RefreshListener mListener;
    private Source mSource1;
    private Source mRefreshedSource;
    private String mRefreshedShortcutId;
    private SuggestionCursor mRefreshedCursor;
    @Override
    protected void setUp() throws Exception {
        mExecutor = new MockNamedTaskExecutor();
        mRefresher = new SourceShortcutRefresher(mExecutor);
        mListener = new RefreshListener();
        mSource1 = new MockRefreshSource("source1");
        mRefreshedSource = null;
        mRefreshedShortcutId = null;
        mRefreshedCursor = null;
    }
    public void testShouldRefreshTrue() {
        assertTrue(mRefresher.shouldRefresh(mSource1, "refresh_me"));
    }
    public void testShouldRefreshFalse() {
        assertFalse(mRefresher.shouldRefresh(null, "foo"));
        assertFalse(mRefresher.shouldRefresh(mSource1, null));
    }
    public void testMarkShortcutRefreshed() {
        mRefresher.markShortcutRefreshed(mSource1, "refreshed");
        assertFalse(mRefresher.shouldRefresh(mSource1, "refreshed"));
        assertTrue(mRefresher.shouldRefresh(mSource1, "not_refreshed"));
    }
    public void testRefreshNull() {
        SuggestionData shortcut1 = new SuggestionData(mSource1)
                .setShortcutId("null_refresh");
        ListSuggestionCursor shortcuts = new ListSuggestionCursor(mQuery, shortcut1);
        mRefresher.refresh(shortcuts, mListener);
        assertTrue(mExecutor.runNext());
        assertEquals(mSource1, mRefreshedSource);
        assertEquals("null_refresh", mRefreshedShortcutId);
        assertEquals(null, mRefreshedCursor);
    }
    public void testRefreshEmpty() {
        SuggestionData shortcut1 = new SuggestionData(mSource1)
                .setShortcutId("empty_refresh");
        ListSuggestionCursor shortcuts = new ListSuggestionCursor(mQuery, shortcut1);
        mRefresher.refresh(shortcuts, mListener);
        assertTrue(mExecutor.runNext());
        assertEquals(mSource1, mRefreshedSource);
        assertEquals("empty_refresh", mRefreshedShortcutId);
        assertEquals(null, mRefreshedCursor);
    }
    public void testRefreshSuccess() {
        SuggestionData shortcut1 = new SuggestionData(mSource1)
                .setShortcutId("success");
        ListSuggestionCursor shortcuts = new ListSuggestionCursor(mQuery, shortcut1);
        mRefresher.refresh(shortcuts, mListener);
        assertTrue(mExecutor.runNext());
        assertEquals(mSource1, mRefreshedSource);
        assertEquals("success", mRefreshedShortcutId);
        SuggestionCursor expected =
                SuggestionCursorUtil.slice(mSource1.getSuggestions(mQuery, 1, true), 0, 1);
        SuggestionCursorUtil.assertSameSuggestions(expected, mRefreshedCursor);
    }
    private class RefreshListener implements ShortcutRefresher.Listener {
        public void onShortcutRefreshed(Source source, String shortcutId,
                SuggestionCursor refreshed) {
            mRefreshedSource = source;
            mRefreshedShortcutId = shortcutId;
            mRefreshedCursor = refreshed;
        }
    }
    private class MockRefreshSource extends MockSource {
        public MockRefreshSource(String name) {
            super(name);
        }
        @Override
        public SuggestionCursor refreshShortcut(String shortcutId, String extraData) {
            if ("null_refresh".equals(shortcutId)) {
                return null;
            } else if ("empty_refresh".equals(shortcutId)) {
                return new ListSuggestionCursor(mQuery);
            } else {
                 SuggestionCursor suggestions = getSuggestions(mQuery, 1, true);
                 return SuggestionCursorUtil.slice(suggestions, 0, 1);
            }
        }
    }
}
