@TestTargetClass(IntentFilter.AuthorityEntry.class)
public class IntentFilter_AuthorityEntryTest extends AndroidTestCase {
    private AuthorityEntry mAuthorityEntry;
    private final String mHost = "testHost";
    private final String mWildHost = "*" + mHost;
    private final int mPort = 80;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAuthorityEntry = new AuthorityEntry(mHost, String.valueOf(mPort));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "IntentFilter.AuthorityEntry",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor() {
        mAuthorityEntry = new AuthorityEntry(mHost, String.valueOf(mPort));
        assertNotNull(mAuthorityEntry);
        assertEquals(mHost, mAuthorityEntry.getHost());
        assertEquals(mPort, mAuthorityEntry.getPort());
        mAuthorityEntry = new AuthorityEntry(mWildHost, String.valueOf(mPort));
        assertNotNull(mAuthorityEntry);
        assertEquals(mWildHost, mAuthorityEntry.getHost());
        assertEquals(Integer.valueOf(mPort).intValue(), mAuthorityEntry.getPort());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPort",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHost",
            args = {}
        )
    })
    public void testAuthorityEntryProperties() {
        assertEquals(Integer.valueOf(mPort).intValue(), mAuthorityEntry.getPort());
        assertEquals(mHost, mAuthorityEntry.getHost());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "match",
        args = {android.net.Uri.class}
    )
    public void testMatch() {
        Uri uri = Uri.parse("testUri");
        assertEquals(IntentFilter.NO_MATCH_DATA, mAuthorityEntry.match(uri));
        uri = Uri.parse("content:
        assertEquals(IntentFilter.NO_MATCH_DATA, mAuthorityEntry.match(uri));
        uri = Uri.parse("test");
        mAuthorityEntry = new IntentFilter.AuthorityEntry(mWildHost, String.valueOf(-1));
        assertEquals(IntentFilter.NO_MATCH_DATA, mAuthorityEntry.match(uri));
        uri = Uri.parse("http:
        mAuthorityEntry = new IntentFilter.AuthorityEntry(mHost, String.valueOf(-1));
        assertEquals(IntentFilter.MATCH_CATEGORY_HOST, mAuthorityEntry.match(uri));
        uri = Uri.parse("http:
        mAuthorityEntry = new AuthorityEntry(mHost, String.valueOf(-1));
        assertEquals(IntentFilter.MATCH_CATEGORY_HOST, mAuthorityEntry.match(uri));
        uri = Uri.parse("http:
        mAuthorityEntry = new AuthorityEntry(mHost, String.valueOf(mPort));
        assertEquals(IntentFilter.MATCH_CATEGORY_PORT, mAuthorityEntry.match(uri));
        uri = Uri.parse("http:
        mAuthorityEntry = new AuthorityEntry(mHost, String.valueOf(-1));
        assertEquals(IntentFilter.MATCH_CATEGORY_HOST, mAuthorityEntry.match(uri));
    }
}
