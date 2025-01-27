@TestTargetClass(BulletSpan.class)
public class BulletSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of BulletSpan.",
            method = "BulletSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of BulletSpan.",
            method = "BulletSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of BulletSpan.",
            method = "BulletSpan",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of BulletSpan.",
            method = "BulletSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new BulletSpan();
        new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH);
        BulletSpan b = new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH, Color.RED);
        final Parcel p = Parcel.obtain();
        b.writeToParcel(p, 0);
        p.setDataPosition(0);
        new BulletSpan(p);
        p.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getLeadingMargin(boolean first). And the parameter first is never read",
        method = "getLeadingMargin",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetLeadingMargin() {
        BulletSpan bulletSpan = new BulletSpan(1);
        int leadingMargin1 = bulletSpan.getLeadingMargin(true);
        bulletSpan = new BulletSpan(4);
        int leadingMargin2 = bulletSpan.getLeadingMargin(false);
        assertTrue(leadingMargin2 > leadingMargin1);
    }
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Test drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top," +
                " int baseline, int bottom, CharSequence text, int start, int end," +
                " boolean first, Layout l). And the following parameters are never" +
                " used in this method: baseline, end, first, l.",
        method = "drawLeadingMargin",
        args = {android.graphics.Canvas.class, android.graphics.Paint.class, int.class,
                int.class, int.class, int.class, int.class, java.lang.CharSequence.class,
                int.class, int.class, boolean.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "have not found a reasonable way to test it" +
            " automatically.")
    public void testDrawLeadingMargin() {
        BulletSpan bulletSpan = new BulletSpan(10, 20);
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Spanned text = Html.fromHtml("<b>hello</b>");
        bulletSpan.drawLeadingMargin(canvas, paint, 10, 0, 10, 0, 20, text, 0, 0, true, null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top," +
                " int baseline, int bottom, CharSequence text, int start, int end," +
                " boolean first, Layout l).",
        method = "drawLeadingMargin",
        args = {android.graphics.Canvas.class, android.graphics.Paint.class, int.class,
                int.class, int.class, int.class, int.class, java.lang.CharSequence.class,
                int.class, int.class, boolean.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws NullPointerException clause" +
            " into javadoc when input null. And when try to use a String as the text," +
            " there should not be a ClassCastException")
    public void testDrawLeadingMarginFailure() {
        BulletSpan bulletSpan = new BulletSpan(10, 20);
        try {
            String text = "cts test.";
            bulletSpan.drawLeadingMargin(null, null, 0, 0, 0, 0, 0, text, 0, 0, true, null);
            fail("did not throw ClassCastException when use a String as text");
        } catch (ClassCastException e) {
        }
        try {
            bulletSpan.drawLeadingMargin(null, null, 0, 0, 0, 0, 0, null, 0, 0, false, null);
            fail("did not throw NullPointerException when text is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        BulletSpan bulletSpan = new BulletSpan();
        bulletSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        BulletSpan bulletSpan = new BulletSpan();
        bulletSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        BulletSpan bulletSpan = new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH, Color.RED);
        bulletSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        BulletSpan b = new BulletSpan(p);
        int leadingMargin1 = b.getLeadingMargin(true);
        p.recycle();
        p = Parcel.obtain();
        bulletSpan = new BulletSpan(10, Color.BLACK);
        bulletSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        b = new BulletSpan(p);
        int leadingMargin2 = b.getLeadingMargin(true);
        p.recycle();
        assertTrue(leadingMargin2 > leadingMargin1);
    }
}
