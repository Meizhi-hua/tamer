@TestTargetClass(IntentFilter.class)
public class IntentFilterTest extends AndroidTestCase {
    private IntentFilter mIntentFilter;
    private static final String ACTION = "testAction";
    private static final String CATEGORY = "testCategory";
    private static final String DATA_TYPE = "vnd.android.cursor.dir/person";
    private static final String DATA_SCHEME = "testDataSchemes.";
    private static final String HOST = "testHost";
    private static final int PORT = 80;
    private static final String DATA_PATH = "testDataPath";
    private static final Uri URI = People.CONTENT_URI;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIntentFilter = new IntentFilter();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentFilter",
            args = {android.content.IntentFilter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentFilter",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentFilter",
            args = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testConstructor() throws MalformedMimeTypeException {
        IntentFilter filter = new IntentFilter();
        verifyContent(filter, null, null);
        filter = new IntentFilter(ACTION);
        verifyContent(filter, ACTION, null);
        final IntentFilter actionTypeFilter = new IntentFilter(ACTION, DATA_TYPE);
        verifyContent(actionTypeFilter, ACTION, DATA_TYPE);
        filter = new IntentFilter(actionTypeFilter);
        verifyContent(filter, ACTION, DATA_TYPE);
        final String dataType = "testdataType";
        try {
            new IntentFilter(ACTION, dataType);
            fail("Should throw MalformedMimeTypeException ");
        } catch (MalformedMimeTypeException e) {
        }
    }
    private void verifyContent(IntentFilter filter, String action, String dataType) {
        if (action != null) {
            assertEquals(1, filter.countActions());
            assertEquals(action, filter.getAction(0));
        } else {
            assertEquals(0, filter.countActions());
        }
        if (dataType != null) {
            assertEquals(1, filter.countDataTypes());
            assertEquals(dataType, filter.getDataType(0));
        } else {
            assertEquals(0, filter.countDataTypes());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "categoriesIterator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addCategory",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCategory",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasCategory",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "countCategories",
            args = {}
        )
    })
    public void testCategories() {
        for (int i = 0; i < 10; i++) {
            mIntentFilter.addCategory(CATEGORY + i);
        }
        assertEquals(10, mIntentFilter.countCategories());
        Iterator<String> iter = mIntentFilter.categoriesIterator();
        String actual = null;
        int i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(CATEGORY + i, actual);
            assertEquals(CATEGORY + i, mIntentFilter.getCategory(i));
            assertTrue(mIntentFilter.hasCategory(CATEGORY + i));
            assertFalse(mIntentFilter.hasCategory(CATEGORY + i + 10));
            i++;
        }
        IntentFilter filter = new Match(null, new String[] { "category1" }, null, null, null, null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null,
                        new String[] { "category1" }, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_CATEGORY, null,
                        new String[] { "category2" }, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_CATEGORY, null, new String[] {
                        "category1", "category2" }, null, null), });
        filter = new Match(null, new String[] { "category1", "category2" }, null, null, null, null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null,
                        new String[] { "category1" }, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null,
                        new String[] { "category2" }, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null, new String[] {
                        "category1", "category2" }, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_CATEGORY, null,
                        new String[] { "category3" }, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_CATEGORY, null, new String[] {
                        "category1", "category2", "category3" }, null, null), });
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "match",
            args = {android.content.ContentResolver.class, android.content.Intent.class,
                    boolean.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "match",
            args = {java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    android.net.Uri.class, java.util.Set.class, java.lang.String.class}
        )
    })
    public void testMimeTypes() throws Exception {
        IntentFilter filter = new Match(null, null, new String[] { "which1/what1" }, null, null,
                null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_TYPE, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which1/what1",
                        null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which1*", null),
                new MatchCondition(IntentFilter.NO_MATCH_TYPE, null, null, "which2/what2", null),
                new MatchCondition(IntentFilter.NO_MATCH_TYPE, null, null, "which2*", null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which2/what2",
                        null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which2*", null),
                new MatchCondition(IntentFilter.NO_MATCH_TYPE, null, null, "which2/what2", null),
                new MatchCondition(IntentFilter.NO_MATCH_TYPE, null, null, "which2*" }, null, null, null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_TYPE, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which1/what1",
                        null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which1*", null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which2/what2",
                        null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which2/*", null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which1/what2",
                        null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_TYPE, null, null, "which3/what3",
                        null), });
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPriority",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPriority",
            args = {}
        )
    })
    public void testAccessPriority() {
        final int expected = 1;
        mIntentFilter.setPriority(expected);
        assertEquals(expected, mIntentFilter.getPriority());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "schemesIterator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDataScheme",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addDataScheme",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "countDataSchemes",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasDataScheme",
            args = {java.lang.String.class}
        )
    })
    public void testDataSchemes() {
        for (int i = 0; i < 10; i++) {
            mIntentFilter.addDataScheme(DATA_SCHEME + i);
        }
        assertEquals(10, mIntentFilter.countDataSchemes());
        final Iterator<String> iter = mIntentFilter.schemesIterator();
        String actual = null;
        int i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(DATA_SCHEME + i, actual);
            assertEquals(DATA_SCHEME + i, mIntentFilter.getDataScheme(i));
            assertTrue(mIntentFilter.hasDataScheme(DATA_SCHEME + i));
            assertFalse(mIntentFilter.hasDataScheme(DATA_SCHEME + i + 10));
            i++;
        }
        IntentFilter filter = new Match(null, null, null, new String[] { "scheme1" }, null, null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_SCHEME, null, null, null,
                        "scheme1:foo"),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme2:foo"), });
        filter = new Match(null, null, null, new String[] { "scheme1", "scheme2" }, null, null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_SCHEME, null, null, null,
                        "scheme1:foo"),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_SCHEME, null, null, null,
                        "scheme2:foo"),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme3:foo"), });
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "create",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreate() {
        IntentFilter filter = IntentFilter.create(ACTION, DATA_TYPE);
        assertNotNull(filter);
        verifyContent(filter, ACTION, DATA_TYPE);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "authoritiesIterator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "countDataAuthorities",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasDataAuthority",
            args = {android.net.Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addDataAuthority",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDataAuthority",
            args = {int.class}
        )
    })
    public void testAuthorities() {
        for (int i = 0; i < 10; i++) {
            mIntentFilter.addDataAuthority(HOST + i, String.valueOf(PORT + i));
        }
        assertEquals(10, mIntentFilter.countDataAuthorities());
        final Iterator<AuthorityEntry> iter = mIntentFilter.authoritiesIterator();
        AuthorityEntry actual = null;
        int i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(HOST + i, actual.getHost());
            assertEquals(PORT + i, actual.getPort());
            AuthorityEntry ae = new AuthorityEntry(HOST + i, String.valueOf(PORT + i));
            assertEquals(ae.getHost(), mIntentFilter.getDataAuthority(i).getHost());
            assertEquals(ae.getPort(), mIntentFilter.getDataAuthority(i).getPort());
            Uri uri = Uri.parse("http:
            assertTrue(mIntentFilter.hasDataAuthority(uri));
            Uri uri2 = Uri.parse("http:
            assertFalse(mIntentFilter.hasDataAuthority(uri2));
            i++;
        }
        IntentFilter filter = new Match(null, null, null, new String[] { "scheme1" },
                new String[] { "authority1" }, new String[] { null });
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme1:foo"),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_HOST, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_HOST, null, null, null,
                        "scheme1:
        filter = new Match(null, null, null, new String[] { "scheme1" },
                new String[] { "authority1" }, new String[] { "100" });
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme1:foo"),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PORT, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
        filter = new Match(null, null, null, new String[] { "scheme1" },
                new String[] { "authority1", "authority2" }, new String[] { "100", null });
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme1:foo"),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_HOST, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PORT, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasDataType",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addDataType",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDataType",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "typesIterator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "countDataTypes",
            args = {}
        )
    })
    public void testDataTypes() throws MalformedMimeTypeException {
        for (int i = 0; i < 10; i++) {
            mIntentFilter.addDataType(DATA_TYPE + i);
        }
        assertEquals(10, mIntentFilter.countDataTypes());
        final Iterator<String> iter = mIntentFilter.typesIterator();
        String actual = null;
        int i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(DATA_TYPE + i, actual);
            assertEquals(DATA_TYPE + i, mIntentFilter.getDataType(i));
            assertTrue(mIntentFilter.hasDataType(DATA_TYPE + i));
            assertFalse(mIntentFilter.hasDataType(DATA_TYPE + i + 10));
            i++;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "matchData",
        args = {java.lang.String.class, java.lang.String.class, android.net.Uri.class}
    )
    public void testMatchData() throws MalformedMimeTypeException {
        int expected = IntentFilter.MATCH_CATEGORY_EMPTY + IntentFilter.MATCH_ADJUSTMENT_NORMAL;
        assertEquals(expected, mIntentFilter.matchData(null, null, null));
        assertEquals(expected, mIntentFilter.matchData(null, DATA_SCHEME, null));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchData(null, DATA_SCHEME, URI));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchData(DATA_TYPE, DATA_SCHEME,
                URI));
        mIntentFilter.addDataScheme(DATA_SCHEME);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchData(DATA_TYPE,
                "mDataSchemestest", URI));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchData(DATA_TYPE, "", URI));
        expected = IntentFilter.MATCH_CATEGORY_SCHEME + IntentFilter.MATCH_ADJUSTMENT_NORMAL;
        assertEquals(expected, mIntentFilter.matchData(null, DATA_SCHEME, URI));
        assertEquals(IntentFilter.NO_MATCH_TYPE, mIntentFilter.matchData(DATA_TYPE, DATA_SCHEME,
                URI));
        mIntentFilter.addDataType(DATA_TYPE);
        assertEquals(IntentFilter.MATCH_CATEGORY_TYPE + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.matchData(DATA_TYPE, DATA_SCHEME, URI));
        mIntentFilter.addDataAuthority(HOST, String.valueOf(PORT));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchData(null, DATA_SCHEME, URI));
        final Uri uri = Uri.parse("http:
        mIntentFilter.addDataPath(DATA_PATH, PatternMatcher.PATTERN_LITERAL);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchData(null, DATA_SCHEME, uri));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "countActions",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addAction",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "actionsIterator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasAction",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "matchAction",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAction",
            args = {int.class}
        )
    })
    public void testActions() {
        for (int i = 0; i < 10; i++) {
            mIntentFilter.addAction(ACTION + i);
        }
        assertEquals(10, mIntentFilter.countActions());
        final Iterator<String> iter = mIntentFilter.actionsIterator();
        String actual = null;
        int i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(ACTION + i, actual);
            assertEquals(ACTION + i, mIntentFilter.getAction(i));
            assertTrue(mIntentFilter.hasAction(ACTION + i));
            assertFalse(mIntentFilter.hasAction(ACTION + i + 10));
            assertTrue(mIntentFilter.matchAction(ACTION + i));
            assertFalse(mIntentFilter.matchAction(ACTION + i + 10));
            i++;
        }
        IntentFilter filter = new Match(new String[] { "action1" }, null, null, null, null, null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, "action1", null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_ACTION, "action2", null, null, null), });
        filter = new Match(new String[] { "action1", "action2" }, null, null, null, null, null);
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, "action1", null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_EMPTY, "action2", null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_ACTION, "action3", null, null, null), });
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToXml",
            args = {org.xmlpull.v1.XmlSerializer.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFromXml",
            args = {org.xmlpull.v1.XmlPullParser.class}
        )
    })
    public void testWriteToXml() throws IllegalArgumentException, IllegalStateException,
            IOException, MalformedMimeTypeException, XmlPullParserException {
        XmlSerializer xml;
        ByteArrayOutputStream out;
        xml = new FastXmlSerializer();
        out = new ByteArrayOutputStream();
        xml.setOutput(out, "utf-8");
        mIntentFilter.addAction(ACTION);
        mIntentFilter.addCategory(CATEGORY);
        mIntentFilter.addDataAuthority(HOST, String.valueOf(PORT));
        mIntentFilter.addDataPath(DATA_PATH, 1);
        mIntentFilter.addDataScheme(DATA_SCHEME);
        mIntentFilter.addDataType(DATA_TYPE);
        mIntentFilter.writeToXml(xml);
        xml.flush();
        final KXmlParser parser = new KXmlParser();
        final InputStream in = new ByteArrayInputStream(out.toByteArray());
        parser.setInput(in, "utf-8");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.readFromXml(parser);
        assertEquals(ACTION, intentFilter.getAction(0));
        assertEquals(CATEGORY, intentFilter.getCategory(0));
        assertEquals(DATA_TYPE, intentFilter.getDataType(0));
        assertEquals(DATA_SCHEME, intentFilter.getDataScheme(0));
        assertEquals(DATA_PATH, intentFilter.getDataPath(0).getPath());
        assertEquals(HOST, intentFilter.getDataAuthority(0).getHost());
        assertEquals(PORT, intentFilter.getDataAuthority(0).getPort());
        out.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "matchCategories",
        args = {java.util.Set.class}
    )
    public void testMatchCategories() {
        assertNull(mIntentFilter.matchCategories(null));
        Set<String> cat = new HashSet<String>();
        assertNull(mIntentFilter.matchCategories(cat));
        final String expected = "mytest";
        cat.add(expected);
        assertEquals(expected, mIntentFilter.matchCategories(cat));
        cat = new HashSet<String>();
        cat.add(CATEGORY);
        mIntentFilter.addCategory(CATEGORY);
        assertNull(mIntentFilter.matchCategories(cat));
        cat.add(expected);
        assertEquals(expected, mIntentFilter.matchCategories(cat));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "matchDataAuthority",
        args = {android.net.Uri.class}
    )
    public void testMatchDataAuthority() {
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchDataAuthority(null));
        mIntentFilter.addDataAuthority(HOST, String.valueOf(PORT));
        final Uri uri = Uri.parse("http:
        assertEquals(IntentFilter.MATCH_CATEGORY_PORT, mIntentFilter.matchDataAuthority(uri));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, mIntentFilter.describeContents());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFromXml",
            args = {org.xmlpull.v1.XmlPullParser.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDataScheme",
            args = {int.class}
        )
    })
    public void testReadFromXml() throws NameNotFoundException, XmlPullParserException, IOException {
        XmlPullParser parser = null;
        ActivityInfo ai = null;
        final ComponentName mComponentName = new ComponentName(mContext, MockActivity.class);
        final PackageManager pm = mContext.getPackageManager();
        ai = pm.getActivityInfo(mComponentName, PackageManager.GET_META_DATA);
        parser = ai.loadXmlMetaData(pm, "android.app.intent.filter");
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && type != XmlPullParser.START_TAG) {
        }
        final String nodeName = parser.getName();
        if (!"intent-filter".equals(nodeName)) {
            throw new RuntimeException();
        }
        mIntentFilter.readFromXml(parser);
        assertEquals("testAction", mIntentFilter.getAction(0));
        assertEquals("testCategory", mIntentFilter.getCategory(0));
        assertEquals("vnd.android.cursor.dir/person", mIntentFilter.getDataType(0));
        assertEquals("testScheme", mIntentFilter.getDataScheme(0));
        assertEquals("testHost", mIntentFilter.getDataAuthority(0).getHost());
        assertEquals(80, mIntentFilter.getDataAuthority(0).getPort());
        assertEquals("test", mIntentFilter.getDataPath(0).getPath());
        assertEquals("test", mIntentFilter.getDataPath(1).getPath());
        assertEquals("test", mIntentFilter.getDataPath(2).getPath());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pathsIterator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addDataPath",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasDataPath",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDataPath",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addDataPath",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "countDataPaths",
            args = {}
        )
    })
    public void testDataPaths() {
        for (int i = 0; i < 10; i++) {
            mIntentFilter.addDataPath(DATA_PATH + i, PatternMatcher.PATTERN_PREFIX);
        }
        assertEquals(10, mIntentFilter.countDataPaths());
        Iterator<PatternMatcher> iter = mIntentFilter.pathsIterator();
        PatternMatcher actual = null;
        int i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(DATA_PATH + i, actual.getPath());
            assertEquals(PatternMatcher.PATTERN_PREFIX, actual.getType());
            PatternMatcher p = new PatternMatcher(DATA_PATH + i, PatternMatcher.PATTERN_PREFIX);
            assertEquals(p.getPath(), mIntentFilter.getDataPath(i).getPath());
            assertEquals(p.getType(), mIntentFilter.getDataPath(i).getType());
            assertTrue(mIntentFilter.hasDataPath(DATA_PATH + i));
            assertTrue(mIntentFilter.hasDataPath(DATA_PATH + i + 10));
            i++;
        }
        mIntentFilter = new IntentFilter();
        i = 0;
        for (i = 0; i < 10; i++) {
            mIntentFilter.addDataPath(DATA_PATH + i, PatternMatcher.PATTERN_LITERAL);
        }
        assertEquals(10, mIntentFilter.countDataPaths());
        iter = mIntentFilter.pathsIterator();
        i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(DATA_PATH + i, actual.getPath());
            assertEquals(PatternMatcher.PATTERN_LITERAL, actual.getType());
            PatternMatcher p = new PatternMatcher(DATA_PATH + i, PatternMatcher.PATTERN_LITERAL);
            assertEquals(p.getPath(), mIntentFilter.getDataPath(i).getPath());
            assertEquals(p.getType(), mIntentFilter.getDataPath(i).getType());
            assertTrue(mIntentFilter.hasDataPath(DATA_PATH + i));
            assertFalse(mIntentFilter.hasDataPath(DATA_PATH + i + 10));
            i++;
        }
        mIntentFilter = new IntentFilter();
        i = 0;
        for (i = 0; i < 10; i++) {
            mIntentFilter.addDataPath(DATA_PATH + i, PatternMatcher.PATTERN_SIMPLE_GLOB);
        }
        assertEquals(10, mIntentFilter.countDataPaths());
        iter = mIntentFilter.pathsIterator();
        i = 0;
        while (iter.hasNext()) {
            actual = iter.next();
            assertEquals(DATA_PATH + i, actual.getPath());
            assertEquals(PatternMatcher.PATTERN_SIMPLE_GLOB, actual.getType());
            PatternMatcher p = new PatternMatcher(DATA_PATH + i,
                    PatternMatcher.PATTERN_SIMPLE_GLOB);
            assertEquals(p.getPath(), mIntentFilter.getDataPath(i).getPath());
            assertEquals(p.getType(), mIntentFilter.getDataPath(i).getType());
            assertTrue(mIntentFilter.hasDataPath(DATA_PATH + i));
            assertFalse(mIntentFilter.hasDataPath(DATA_PATH + i + 10));
            i++;
        }
        IntentFilter filter = new Match(null, null, null, new String[] { "scheme1" },
                new String[] { "authority1" }, new String[] { null });
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme1:foo"),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_HOST, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_HOST, null, null, null,
                        "scheme1:
        filter = new Match(null, null, null, new String[] { "scheme1" },
                new String[] { "authority1" }, new String[] { "100" });
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme1:foo"),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PORT, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
        filter = new Match(null, null, null, new String[] { "scheme1" },
                new String[] { "authority1", "authority2" }, new String[] { "100", null });
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, "scheme1:foo"),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_HOST, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PORT, null, null, null,
                        "scheme1:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme1:
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "match",
        args = {android.content.ContentResolver.class, android.content.Intent.class, boolean.class,
                java.lang.String.class}
    )
    public void testMatchWithIntent() throws MalformedMimeTypeException {
        final ContentResolver resolver = mContext.getContentResolver();
        Intent intent = new Intent(ACTION);
        assertEquals(IntentFilter.NO_MATCH_ACTION, mIntentFilter.match(resolver, intent, true,
                null));
        mIntentFilter.addAction(ACTION);
        assertEquals(IntentFilter.MATCH_CATEGORY_EMPTY + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.match(resolver, intent, true, null));
        final Uri uri = Uri.parse(DATA_SCHEME + ":
        intent.setData(uri);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(resolver, intent, true, null));
        mIntentFilter.addDataAuthority(HOST, String.valueOf(PORT));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(resolver, intent, true, null));
        intent.setType(DATA_TYPE);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(resolver, intent, true, null));
        mIntentFilter.addDataType(DATA_TYPE);
        assertEquals(IntentFilter.MATCH_CATEGORY_TYPE + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.match(resolver, intent, true, null));
        assertEquals(IntentFilter.MATCH_CATEGORY_TYPE + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.match(resolver, intent, false, null));
        intent.addCategory(CATEGORY);
        assertEquals(IntentFilter.NO_MATCH_CATEGORY, mIntentFilter.match(resolver, intent, true,
                null));
        mIntentFilter.addCategory(CATEGORY);
        assertEquals(IntentFilter.MATCH_CATEGORY_TYPE + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.match(resolver, intent, true, null));
        intent.setDataAndType(uri, DATA_TYPE);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(resolver, intent, true, null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "match",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class,
                android.net.Uri.class, java.util.Set.class, java.lang.String.class}
    )
    public void testMatchWithIntentData() throws MalformedMimeTypeException {
        Set<String> cat = new HashSet<String>();
        assertEquals(IntentFilter.NO_MATCH_ACTION, mIntentFilter.match(ACTION, null, null, null,
                null, null));
        mIntentFilter.addAction(ACTION);
        assertEquals(IntentFilter.MATCH_CATEGORY_EMPTY + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.match(ACTION, null, null, null, null, null));
        assertEquals(IntentFilter.MATCH_CATEGORY_EMPTY + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.match(ACTION, null, DATA_SCHEME, null, null, null));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.matchData(null, DATA_SCHEME, URI));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, null, null));
        mIntentFilter.addDataScheme(DATA_SCHEME);
        assertEquals(IntentFilter.NO_MATCH_TYPE, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, null, null));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE, "", URI,
                null, null));
        mIntentFilter.addDataType(DATA_TYPE);
        assertEquals(IntentFilter.MATCH_CATEGORY_TYPE + IntentFilter.MATCH_ADJUSTMENT_NORMAL,
                mIntentFilter.match(ACTION, DATA_TYPE, DATA_SCHEME, URI, null, null));
        assertEquals(IntentFilter.NO_MATCH_TYPE, mIntentFilter.match(ACTION, null, DATA_SCHEME,
                URI, null, null));
        assertEquals(IntentFilter.NO_MATCH_TYPE, mIntentFilter.match(ACTION, null, DATA_SCHEME,
                URI, cat, null));
        cat.add(CATEGORY);
        assertEquals(IntentFilter.NO_MATCH_CATEGORY, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, cat, null));
        cat = new HashSet<String>();
        mIntentFilter.addDataAuthority(HOST, String.valueOf(PORT));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, null, DATA_SCHEME,
                URI, null, null));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, null, null));
        final Uri uri = Uri.parse(DATA_SCHEME + ":
        mIntentFilter.addDataPath(DATA_PATH, PatternMatcher.PATTERN_LITERAL);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, uri, null, null));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, null, null));
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, cat, null));
        cat.add(CATEGORY);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, cat, null));
        mIntentFilter.addCategory(CATEGORY);
        assertEquals(IntentFilter.NO_MATCH_DATA, mIntentFilter.match(ACTION, DATA_TYPE,
                DATA_SCHEME, URI, cat, null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws MalformedMimeTypeException {
        mIntentFilter.addAction(ACTION);
        mIntentFilter.addCategory(CATEGORY);
        mIntentFilter.addDataAuthority(HOST, String.valueOf(PORT));
        mIntentFilter.addDataPath(DATA_PATH, 1);
        mIntentFilter.addDataScheme(DATA_SCHEME);
        mIntentFilter.addDataType(DATA_TYPE);
        Parcel parcel = Parcel.obtain();
        mIntentFilter.writeToParcel(parcel, 1);
        parcel.setDataPosition(0);
        IntentFilter target = IntentFilter.CREATOR.createFromParcel(parcel);
        assertEquals(mIntentFilter.getAction(0), target.getAction(0));
        assertEquals(mIntentFilter.getCategory(0), target.getCategory(0));
        assertEquals(mIntentFilter.getDataAuthority(0).getHost(),
                target.getDataAuthority(0).getHost());
        assertEquals(mIntentFilter.getDataAuthority(0).getPort(),
                target.getDataAuthority(0).getPort());
        assertEquals(mIntentFilter.getDataPath(0).getPath(), target.getDataPath(0).getPath());
        assertEquals(mIntentFilter.getDataScheme(0), target.getDataScheme(0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addDataType",
        args = {java.lang.String.class}
    )
    public void testAddDataType() throws MalformedMimeTypeException {
        try {
            mIntentFilter.addDataType("test");
            fail("should throw MalformedMimeTypeException");
        } catch (MalformedMimeTypeException e) {
        }
        mIntentFilter.addDataType(DATA_TYPE);
        assertEquals(DATA_TYPE, mIntentFilter.getDataType(0));
    }
    private static class Match extends IntentFilter {
        Match(String[] actions, String[] categories, String[] mimeTypes, String[] schemes,
                String[] authorities, String[] ports) {
            if (actions != null) {
                for (int i = 0; i < actions.length; i++) {
                    addAction(actions[i]);
                }
            }
            if (categories != null) {
                for (int i = 0; i < categories.length; i++) {
                    addCategory(categories[i]);
                }
            }
            if (mimeTypes != null) {
                for (int i = 0; i < mimeTypes.length; i++) {
                    try {
                        addDataType(mimeTypes[i]);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        throw new RuntimeException("Bad mime type", e);
                    }
                }
            }
            if (schemes != null) {
                for (int i = 0; i < schemes.length; i++) {
                    addDataScheme(schemes[i]);
                }
            }
            if (authorities != null) {
                for (int i = 0; i < authorities.length; i++) {
                    addDataAuthority(authorities[i], ports != null ? ports[i] : null);
                }
            }
        }
        Match(String[] actions, String[] categories, String[] mimeTypes, String[] schemes,
                String[] authorities, String[] ports, String[] paths, int[] pathTypes) {
            this(actions, categories, mimeTypes, schemes, authorities, ports);
            if (paths != null) {
                for (int i = 0; i < paths.length; i++) {
                    addDataPath(paths[i], pathTypes[i]);
                }
            }
        }
    }
    private static class MatchCondition {
        public final int result;
        public final String action;
        public final String mimeType;
        public final Uri data;
        public final String[] categories;
        public MatchCondition(int _result, String _action, String[] _categories, String _mimeType,
                String _data) {
            result = _result;
            action = _action;
            mimeType = _mimeType;
            data = _data != null ? Uri.parse(_data) : null;
            categories = _categories;
        }
    }
    private static void checkMatches(IntentFilter filter, MatchCondition[] results) {
        for (int i = 0; i < results.length; i++) {
            MatchCondition mc = results[i];
            HashSet<String> categories = null;
            if (mc.categories != null) {
                for (int j = 0; j < mc.categories.length; j++) {
                    if (categories == null) {
                        categories = new HashSet<String>();
                    }
                    categories.add(mc.categories[j]);
                }
            }
            int result = filter.match(mc.action, mc.mimeType, mc.data != null ? mc.data.getScheme()
                    : null, mc.data, categories, "test");
            if ((result & IntentFilter.MATCH_CATEGORY_MASK) !=
                    (mc.result & IntentFilter.MATCH_CATEGORY_MASK)) {
                StringBuilder msg = new StringBuilder();
                msg.append("Error matching against IntentFilter:\n");
                filter.dump(new StringBuilderPrinter(msg), "    ");
                msg.append("Match action: ");
                msg.append(mc.action);
                msg.append("\nMatch mimeType: ");
                msg.append(mc.mimeType);
                msg.append("\nMatch data: ");
                msg.append(mc.data);
                msg.append("\nMatch categories: ");
                if (mc.categories != null) {
                    for (int j = 0; j < mc.categories.length; j++) {
                        if (j > 0)
                            msg.append(", ");
                        msg.append(mc.categories[j]);
                    }
                }
                msg.append("\nExpected result: 0x");
                msg.append(Integer.toHexString(mc.result));
                msg.append(", got result: 0x");
                msg.append(Integer.toHexString(result));
                throw new RuntimeException(msg.toString());
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addDataPath",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "countDataPaths",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDataPath",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasDataPath",
            args = {java.lang.String.class}
        )
    })
    public void testPaths() throws Exception {
        IntentFilter filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null,
                new String[]{"/literal1", "/2literal"},
                new int[]{PATTERN_LITERAL, PATTERN_LITERAL});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null,
                new String[]{"/literal1", "/2literal"}, new int[]{PATTERN_PREFIX, PATTERN_PREFIX});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/.*"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{".*"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/a1*b"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/a1*"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/a\\.*b"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/a.*b"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/a.*"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/a.\\*b"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
        filter = new Match(null, null, null,
                new String[]{"scheme"}, new String[]{"authority"}, null, new String[]{"/a.\\*"},
                new int[]{PATTERN_SIMPLE_GLOB});
        checkMatches(filter, new MatchCondition[] {
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null, null),
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.MATCH_CATEGORY_PATH, null, null, null,
                        "scheme:
                new MatchCondition(IntentFilter.NO_MATCH_DATA, null, null, null,
                        "scheme:
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dump",
        args = {Printer.class, String.class}
    )
    @ToBeFixed(explanation = "new IntentFilter().dump(...) does not print anything at all")
    public void testDump() throws MalformedMimeTypeException {
        TestPrinter printer = new TestPrinter();
        String prefix = "TestIntentFilter";
        IntentFilter filter = new IntentFilter(ACTION, DATA_TYPE);
        filter.dump(printer, prefix);
        assertTrue(printer.isPrintlnCalled);
    }
    private class TestPrinter implements Printer {
        public boolean isPrintlnCalled;
        public void println(String x) {
            isPrintlnCalled = true;
        }
    }
}
