@TestTargetClass(Rect.class)
public class RectTest extends AndroidTestCase {
    private Rect mRect;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRect = null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Rect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Rect",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Rect",
            args = {android.graphics.Rect.class}
        )
    })
    public void testConstructor() {
        mRect = null;
        mRect = new Rect();
        mRect = null;
        mRect = new Rect(10, 10, 20, 20);
        mRect = null;
        Rect rect = new Rect(10, 10, 20, 20);
        mRect = new Rect(rect);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testSet1() {
        mRect = new Rect();
        mRect.set(1, 2, 3, 4);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.top);
        assertEquals(3, mRect.right);
        assertEquals(4, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {android.graphics.Rect.class}
    )
    public void testSet2() {
        Rect rect = new Rect(1, 2, 3, 4);
        mRect = new Rect();
        mRect.set(rect);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.top);
        assertEquals(3, mRect.right);
        assertEquals(4, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersects",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testIntersects1() {
        mRect = new Rect(0, 0, 10, 10);
        assertTrue(mRect.intersects(5, 5, 15, 15));
        assertEquals(0, mRect.left);
        assertEquals(0, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
        mRect = new Rect(0, 0, 10, 10);
        assertFalse(mRect.intersects(15, 15, 25, 25));
        assertEquals(0, mRect.left);
        assertEquals(0, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersects",
        args = {android.graphics.Rect.class, android.graphics.Rect.class}
    )
    public void testIntersects2() {
        Rect rect1;
        Rect rect2;
        rect1 = new Rect(0, 0, 10, 10);
        rect2 = new Rect(5, 5, 15, 15);
        assertTrue(Rect.intersects(rect1, rect2));
        rect1 = new Rect(0, 0, 10, 10);
        rect2 = new Rect(15, 15, 25, 25);
        assertFalse(Rect.intersects(rect1, rect2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "height",
        args = {}
    )
    public void testHeight() {
        mRect = new Rect(6, 6, 10, 10);
        assertEquals(4, mRect.height());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offsetTo",
        args = {int.class, int.class}
    )
    public void testOffsetTo() {
        mRect = new Rect(5, 5, 10, 10);
        mRect.offsetTo(1, 1);
        assertEquals(1, mRect.left);
        assertEquals(1, mRect.top);
        assertEquals(6, mRect.right);
        assertEquals(6, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setIntersect",
        args = {android.graphics.Rect.class, android.graphics.Rect.class}
    )
    public void testSetIntersect() {
        Rect rect1 = new Rect(0, 0, 10, 10);
        Rect rect2 = new Rect(5, 5, 15, 15);
        mRect = new Rect();
        assertTrue(mRect.setIntersect(rect1, rect2));
        assertEquals(5, mRect.left);
        assertEquals(5, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
        mRect = new Rect(0, 0, 15, 15);
        assertTrue(mRect.setIntersect(rect1, rect2));
        assertEquals(5, mRect.left);
        assertEquals(5, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "union",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testUnion1() {
        mRect = new Rect(0, 0, 1, 1);
        mRect.union(1, 1, 2, 2);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(1, 1, 0, 0);
        mRect.union(1, 1, 2, 2);
        assertEquals(1, mRect.top);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(0, 0, 1, 1);
        mRect.union(2, 2, 1, 1);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(1, mRect.right);
        assertEquals(1, mRect.bottom);
        mRect = new Rect();
        mRect.union(1, 1, 2, 2);
        assertEquals(1, mRect.top);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(0, 0, 1, 1);
        mRect.union(2, 2, 2, 2);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(1, mRect.right);
        assertEquals(1, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "union",
        args = {android.graphics.Rect.class}
    )
    public void testUnion2() {
        Rect rect;
        mRect = new Rect(0, 0, 1, 1);
        rect = new Rect(1, 1, 2, 2);
        mRect.union(rect);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(1, 1, 0, 0);
        rect = new Rect(1, 1, 2, 2);
        mRect.union(rect);
        assertEquals(1, mRect.top);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(0, 0, 1, 1);
        rect = new Rect(2, 2, 1, 1);
        mRect.union(rect);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(1, mRect.right);
        assertEquals(1, mRect.bottom);
        mRect = new Rect();
        rect = new Rect(1, 1, 2, 2);
        mRect.union(rect);
        assertEquals(1, mRect.top);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(0, 0, 1, 1);
        rect = new Rect(2, 2, 2, 2);
        mRect.union(rect);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(1, mRect.right);
        assertEquals(1, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "union",
        args = {int.class, int.class}
    )
    public void testUnion3() {
        mRect = new Rect(0, 0, 1, 1);
        mRect.union(2, 2);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(1, 1, 2, 2);
        mRect.union(0, 0);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect(1, 1, 2, 2);
        mRect.union(1, 1);
        assertEquals(1, mRect.top);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
        mRect = new Rect();
        mRect.union(2, 2);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.left);
        assertEquals(2, mRect.right);
        assertEquals(2, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "contains",
        args = {int.class, int.class}
    )
    public void testContains1() {
        mRect = new Rect(1, 1, 20, 20);
        assertFalse(mRect.contains(0, 0));
        assertTrue(mRect.contains(1, 1));
        assertTrue(mRect.contains(19, 19));
        assertFalse(mRect.contains(20, 20));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "contains",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testContains2() {
        mRect = new Rect(1, 1, 20, 20);
        assertTrue(mRect.contains(1, 1, 20, 20));
        assertTrue(mRect.contains(2, 2, 19, 19));
        assertFalse(mRect.contains(21, 21, 22, 22));
        assertFalse(mRect.contains(0, 0, 19, 19));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "contains",
        args = {android.graphics.Rect.class}
    )
    public void testContains3() {
        Rect rect;
        mRect = new Rect(1, 1, 20, 20);
        rect = new Rect(1, 1, 20, 20);
        assertTrue(mRect.contains(rect));
        rect = new Rect(2, 2, 19, 19);
        assertTrue(mRect.contains(rect));
        rect = new Rect(21, 21, 22, 22);
        assertFalse(mRect.contains(rect));
        rect = new Rect(0, 0, 19, 19);
        assertFalse(mRect.contains(rect));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "width",
        args = {}
    )
    public void testWidth() {
        mRect = new Rect(6, 6, 10, 10);
        assertEquals(4, mRect.width());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isEmpty",
        args = {}
    )
    public void testIsEmpty() {
        mRect = new Rect();
        assertTrue(mRect.isEmpty());
        mRect = new Rect(1, 1, 1, 1);
        assertTrue(mRect.isEmpty());
        mRect = new Rect(0, 1, 2, 1);
        assertTrue(mRect.isEmpty());
        mRect = new Rect(1, 1, 20, 20);
        assertFalse(mRect.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersect",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testIntersect1() {
        mRect = new Rect(0, 0, 10, 10);
        assertTrue(mRect.intersect(5, 5, 15, 15));
        assertEquals(5, mRect.left);
        assertEquals(5, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
        mRect = new Rect(0, 0, 10, 10);
        assertFalse(mRect.intersect(15, 15, 25, 25));
        assertEquals(0, mRect.left);
        assertEquals(0, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersect",
        args = {android.graphics.Rect.class}
    )
    public void testIntersect2() {
        Rect rect;
        mRect = new Rect(0, 0, 10, 10);
        rect= new Rect(5, 5, 15, 15);
        assertTrue(mRect.intersect(rect));
        assertEquals(5, mRect.left);
        assertEquals(5, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
        mRect = new Rect(0, 0, 10, 10);
        rect= new Rect(15, 15, 25, 25);
        assertFalse(mRect.intersect(rect));
        assertEquals(0, mRect.left);
        assertEquals(0, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "centerY",
        args = {}
    )
    public void testCenterY() {
        mRect = new Rect(10, 10, 20, 20);
        assertEquals(15, mRect.centerY());
        mRect = new Rect(10, 11, 20, 20);
        assertEquals(15, mRect.centerY());
        mRect = new Rect(10, 12, 20, 20);
        assertEquals(16, mRect.centerY());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toShortString",
            args = {}
        )
    })
    public void testToString() {
        mRect = new Rect();
        assertNotNull(mRect.toString());
        assertNotNull(mRect.toShortString());
        mRect = new Rect(1, 2, 3, 4);
        assertNotNull(mRect.toString());
        assertNotNull(mRect.toShortString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "sort",
        args = {}
    )
    public void testSort() {
        mRect = new Rect(10, 10, 5, 5);
        assertEquals(10, mRect.left);
        assertEquals(10, mRect.top);
        assertEquals(5, mRect.right);
        assertEquals(5, mRect.bottom);
        mRect.sort();
        assertEquals(5, mRect.left);
        assertEquals(5, mRect.top);
        assertEquals(10, mRect.right);
        assertEquals(10, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "centerX",
        args = {}
    )
    public void testCenterX() {
        mRect = new Rect(10, 10, 20, 20);
        assertEquals(15, mRect.centerX());
        mRect = new Rect(11, 10, 20, 20);
        assertEquals(15, mRect.centerX());
        mRect = new Rect(12, 10, 20, 20);
        assertEquals(16, mRect.centerX());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        mRect = new Rect(1, 2, 3, 4);
        Rect rect = new Rect(1, 2, 3, 4);
        assertTrue(mRect.equals(rect));
        rect = new Rect(2, 2, 3, 4);
        assertFalse(mRect.equals(rect));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offset",
        args = {int.class, int.class}
    )
    public void testOffset() {
        mRect = new Rect(5, 5, 10, 10);
        mRect.offset(1, 1);
        assertEquals(6, mRect.left);
        assertEquals(6, mRect.top);
        assertEquals(11, mRect.right);
        assertEquals(11, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inset",
        args = {int.class, int.class}
    )
    public void testInset() {
        mRect = new Rect(5, 5, 10, 10);
        mRect.inset(1, 1);
        assertEquals(6, mRect.left);
        assertEquals(6, mRect.top);
        assertEquals(9, mRect.right);
        assertEquals(9, mRect.bottom);
        mRect = new Rect(5, 5, 10, 10);
        mRect.inset(-1, -1);
        assertEquals(4, mRect.left);
        assertEquals(4, mRect.top);
        assertEquals(11, mRect.right);
        assertEquals(11, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setEmpty",
        args = {}
    )
    public void testSetEmpty() {
        mRect = new Rect(1, 2, 3, 4);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.top);
        assertEquals(3, mRect.right);
        assertEquals(4, mRect.bottom);
        mRect.setEmpty();
        assertEquals(0, mRect.left);
        assertEquals(0, mRect.top);
        assertEquals(0, mRect.right);
        assertEquals(0, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "exactCenterX",
        args = {}
    )
    public void testExactCenterX() {
        mRect = new Rect(11, 10, 20, 20);
        assertEquals(15.5f, mRect.exactCenterX());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "exactCenterY",
        args = {}
    )
    public void testExactCenterY() {
        mRect = new Rect(10, 11, 20, 20);
        assertEquals(15.5f, mRect.exactCenterY());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFromParcel",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        )
    })
    public void testAccessParcel() {
        Rect rect;
        Parcel p = Parcel.obtain();
        rect = new Rect(1, 2, 3, 4);
        rect.writeToParcel(p, 0);
        p.setDataPosition(0);
        mRect = new Rect();
        mRect.readFromParcel(p);
        assertEquals(1, mRect.left);
        assertEquals(2, mRect.top);
        assertEquals(3, mRect.right);
        assertEquals(4, mRect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        mRect = new Rect();
        assertEquals(0, mRect.describeContents());
    }
}
