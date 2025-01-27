@TestTargetClass(ContentUris.class)
public class ContentUrisTest extends AndroidTestCase {
    private static final String AUTHORITY = "ctstest";
    private static final String PATH1 = "testPath1";
    private static final String PATH2 = "testPath2";
    private static final int CODE1 = 1;
    private static final int CODE2 = 2;
    private Uri uri1 = Uri.parse("content:
    private Uri uri2 = Uri.parse("content:
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of ContentUris.",
        method = "ContentUris",
        args = {}
    )
    public void testConstructor() {
        new ContentUris();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test parseId(Uri contentUri).",
        method = "parseId",
        args = {android.net.Uri.class}
    )
    public void testParseId() {
        Uri result = ContentUris.withAppendedId(uri1, CODE1);
        assertEquals(CODE1, ContentUris.parseId(result));
        result = ContentUris.withAppendedId(uri2, CODE2);
        assertEquals(CODE2, ContentUris.parseId(result));
        assertEquals(-1, ContentUris.parseId(Uri.parse("")));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test parseId(Uri contentUri).",
        method = "parseId",
        args = {android.net.Uri.class}
    )
    @ToBeFixed(bug = "", explanation =
        "There should not be a NullPointerException thrown out," +
        "and should be an UnsupportedOperationException thrown out.")
    public void testParseIdFailure() {
        try {
            ContentUris.parseId(uri1);
            fail("There should be a NumberFormatException thrown out.");
        } catch (NumberFormatException e) {
        }
        Uri uri = Uri.fromParts("abc", "123", null);
        ContentUris.parseId(uri);
        try {
            ContentUris.parseId(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test withAppendedId(Uri contentUri, long id).",
        method = "withAppendedId",
        args = {android.net.Uri.class, long.class}
    )
    public void testWithAppendedId() {
        String expected = "content:
        Uri actually;
        assertNotNull(actually = ContentUris.withAppendedId(uri1, CODE1));
        assertEquals(expected, actually.toString());
        expected = "content:
        assertNotNull(actually = ContentUris.withAppendedId(uri2, CODE2));
        assertEquals(expected, actually.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test withAppendedId(Uri contentUri, long id).",
        method = "withAppendedId",
        args = {android.net.Uri.class, long.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "Unexpected NullPointerException")
    public void testWithAppendedIdFailure() {
        try {
            ContentUris.withAppendedId(null, -1);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test appendId(Builder builder, long id).",
        method = "appendId",
        args = {android.net.Uri.Builder.class, long.class}
    )
    public void testAppendId() {
        String expected = "content:
        Builder actually;
        Builder b = uri1.buildUpon();
        assertNotNull(actually = ContentUris.appendId(b, CODE1));
        assertEquals(expected, actually.toString());
        expected = "content:
        b = uri2.buildUpon();
        assertNotNull(actually = ContentUris.appendId(b, CODE2));
        assertEquals(expected, actually.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test appendId(Builder builder, long id).",
        method = "appendId",
        args = {android.net.Uri.Builder.class, long.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "Unexpected NullPointerException")
    public void testAppendIdFailure() {
        try {
            ContentUris.appendId(null, -1);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
    }
}
