@TestTargetClass(SimpleStringSplitter.class)
public class TextUtils_SimpleStringSplitterTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor",
        method = "TextUtils.SimpleStringSplitter",
        args = {char.class}
    )
    public void testConstructor() {
        new SimpleStringSplitter('|');
        new SimpleStringSplitter(Character.MAX_VALUE);
        new SimpleStringSplitter(Character.MIN_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test hasNext method",
        method = "hasNext",
        args = {}
    )
    public void testHasNext() {
        SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter('|');
        assertFalse(simpleStringSplitter.hasNext());
        simpleStringSplitter.setString("first|second");
        assertTrue(simpleStringSplitter.hasNext());
        simpleStringSplitter.next();
        assertTrue(simpleStringSplitter.hasNext());
        simpleStringSplitter.next();
        assertFalse(simpleStringSplitter.hasNext());
        simpleStringSplitter.setString("");
        assertFalse(simpleStringSplitter.hasNext());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test iterator method",
        method = "iterator",
        args = {}
    )
    public void testIterator() {
        SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter('|');
        Iterator<String> iterator = simpleStringSplitter.iterator();
        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
        simpleStringSplitter.setString("hello|world");
        iterator = simpleStringSplitter.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals("hello", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("world", iterator.next());
        assertFalse(iterator.hasNext());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test next method",
        method = "next",
        args = {}
    )
    @ToBeFixed(bug="1436930", explanation="should throw NoSuchElementException " +
            "when there are no more elements")
    public void testNext1() {
        SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(',');
        simpleStringSplitter.setString("first, second");
        assertEquals("first", simpleStringSplitter.next());
        assertEquals(" second", simpleStringSplitter.next());
        try {
            simpleStringSplitter.next();
            fail("Should throw StringIndexOutOfBoundsException!");
        } catch (StringIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test next method",
        method = "next",
        args = {}
    )
    @ToBeFixed(bug="1448860", explanation="The comments on SimpleStringSplitter are quite" +
            " specific that if the final char of the string to split is a delimiter then" +
            " no empty string should be returned for the text after the delimiter")
    public void testNext2() {
        SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(',');
        simpleStringSplitter.setString(" ,");
        assertEquals(" ", simpleStringSplitter.next());
        assertEquals("", simpleStringSplitter.next());
        simpleStringSplitter.setString(",,,");
        assertEquals("", simpleStringSplitter.next());
        assertEquals("", simpleStringSplitter.next());
        assertEquals("", simpleStringSplitter.next());
        assertEquals("", simpleStringSplitter.next());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test remove method",
        method = "remove",
        args = {}
    )
    public void testRemove() {
        SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(',');
        try {
            simpleStringSplitter.remove();
            fail("Should throw UnsupportedOperationException!");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setString method",
        method = "setString",
        args = {java.lang.String.class}
    )
    @ToBeFixed( bug = "1371108", explanation = "NullPointerException issue")
    public void testSetString() {
        SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(',');
        assertFalse(simpleStringSplitter.hasNext());
        simpleStringSplitter.setString("text1");
        assertTrue(simpleStringSplitter.hasNext());
        assertEquals("text1", simpleStringSplitter.next());
        assertFalse(simpleStringSplitter.hasNext());
        simpleStringSplitter.setString("text2");
        assertTrue(simpleStringSplitter.hasNext());
        assertEquals("text2", simpleStringSplitter.next());
        try {
            simpleStringSplitter.setString(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
}
