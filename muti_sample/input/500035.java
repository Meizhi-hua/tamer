@TestTargetClass(ListIterator.class)
public class ListIteratorTest extends TestCase {
    ListIterator<Integer> l = null;
    static Object[] objArray;
    {
        objArray = new Object[100];
        for (int i = 0; i < objArray.length; i++)
            objArray[i] = new Integer(i);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hasNext",
        args = {}
    )
    public void testHasNext() {
        for (int i = 0; i < objArray.length; i++) {
            assertTrue(l.hasNext());
            l.next();
        }
        assertFalse(l.hasNext());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "next",
        args = {}
    )
    public void testNext() {
        for (int i = 0; i < objArray.length; i++) {
            assertTrue(objArray[i].equals(l.next()));
        }
        try {
            l.next();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
        }
    }
    class Mock_ListIterator implements ListIterator {
        public void add(Object o) {
            if(((String) o).equals("Wrong element")) throw new IllegalArgumentException();
            if(o.getClass() == Double.class) throw new ClassCastException();
            throw new UnsupportedOperationException();
        }
        public boolean hasNext() {
            return false;
        }
        public boolean hasPrevious() {
            return false;
        }
        public Object next() {
            return null;
        }
        public int nextIndex() {
            return 0;
        }
        public Object previous() {
            return null;
        }
        public int previousIndex() {
            return 0;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
        public void set(Object o) {
            if(((String) o).equals("Wrong element")) throw new IllegalArgumentException();
            if(o.getClass() == Double.class) throw new ClassCastException();
            throw new UnsupportedOperationException();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {}
    )
    public void testRemove() {
        try {
            l.remove();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        for (int i = 0; i < objArray.length; i++) {
            l.next();
            l.remove();
            assertFalse(l.hasPrevious());
        }
        try {
            l.remove();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        Mock_ListIterator ml = new Mock_ListIterator();
        try {
            ml.remove();
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hasPrevious",
        args = {}
    )
    public void testHasPrevious() {
        assertFalse(l.hasPrevious());
        for (int i = 0; i < objArray.length; i++) {
            l.next();
            assertTrue(l.hasPrevious());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "previous",
        args = {}
    )
    public void testPrevious() {
        try {
            l.previous();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
        }
        while(l.hasNext()) {
            l.next();
        }
        for (int i = objArray.length - 1; i > -1 ; i--) {
            assertTrue(objArray[i].equals(l.previous()));
        }
        try {
            l.previous();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "nextIndex",
        args = {}
    )
    public void testNextIndex() {
        for (int i = 0; i < objArray.length; i++) {
            assertTrue(objArray[i].equals(l.nextIndex()));
            l.next();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "previousIndex",
        args = {}
    )
    public void testPreviousIndex() {
        for (int i = 0; i < objArray.length; i++) {
            assertTrue(objArray[i].equals(l.previousIndex() + 1));
            l.next();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "set",
        args = {java.lang.Object.class}
    )
    public void testSet() {
        try {
            l.set(new Integer(1));
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        for (int i = 0; i < objArray.length; i++) {
            l.next();
            l.set((Integer)objArray[objArray.length - i - 1]);
        }
        l.remove();
        try {
            l.set(new Integer(1));
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        Mock_ListIterator ml = new Mock_ListIterator();
        ml.next();
        try {
            ml.set("Wrong element");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            ml.set(new Double("3.14"));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
        try {
            ml.set("");
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "add",
        args = {java.lang.Object.class}
    )
    public void testAdd() {
        l.add(new Integer(33));
        Mock_ListIterator ml = new Mock_ListIterator();
        ml.next();
        try {
            ml.add("Wrong element");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            ml.add(new Double("3.14"));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
        try {
            ml.add("");
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
        LinkedList ll = new LinkedList();
        for (int i = 0; i < objArray.length; i++) {
            ll.add(objArray[i]);
        }
        l = ll.listIterator();
    }
}
