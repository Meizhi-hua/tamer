@TestTargetClass(Collection.class)
public class Support_UnmodifiableCollectionTest extends TestCase {
    Collection<Integer> col;
    public Support_UnmodifiableCollectionTest(String p1) {
        super(p1);
    }
    public Support_UnmodifiableCollectionTest(String p1, Collection<Integer> c) {
        super(p1);
        col = c;
    }
    @Override
    public void runTest() {
        assertTrue("UnmodifiableCollectionTest - should contain 0", col
                .contains(new Integer(0)));
        assertTrue("UnmodifiableCollectionTest - should contain 50", col
                .contains(new Integer(50)));
        assertTrue("UnmodifiableCollectionTest - should not contain 100", !col
                .contains(new Integer(100)));
        HashSet<Integer> hs = new HashSet<Integer>();
        hs.add(new Integer(0));
        hs.add(new Integer(25));
        hs.add(new Integer(99));
        assertTrue(
                "UnmodifiableCollectionTest - should contain set of 0, 25, and 99",
                col.containsAll(hs));
        hs.add(new Integer(100));
        assertTrue(
                "UnmodifiableCollectionTest - should not contain set of 0, 25, 99 and 100",
                !col.containsAll(hs));
        assertTrue("UnmodifiableCollectionTest - should not be empty", !col
                .isEmpty());
        Iterator<Integer> it = col.iterator();
        SortedSet<Integer> ss = new TreeSet<Integer>();
        while (it.hasNext()) {
            ss.add(it.next());
        }
        it = ss.iterator();
        for (int counter = 0; it.hasNext(); counter++) {
            int nextValue = it.next().intValue();
            assertTrue(
                    "UnmodifiableCollectionTest - Iterator returned wrong value.  Wanted: "
                            + counter + " got: " + nextValue,
                    nextValue == counter);
        }
        assertTrue(
                "UnmodifiableCollectionTest - returned wrong size.  Wanted 100, got: "
                        + col.size(), col.size() == 100);
        Object[] objArray;
        objArray = col.toArray();
        for (int counter = 0; it.hasNext(); counter++) {
            assertTrue(
                    "UnmodifiableCollectionTest - toArray returned incorrect array",
                    objArray[counter] == it.next());
        }
        objArray = new Object[100];
        col.toArray(objArray);
        for (int counter = 0; it.hasNext(); counter++) {
            assertTrue(
                    "UnmodifiableCollectionTest - toArray(Object) filled array incorrectly",
                    objArray[counter] == it.next());
        }
    }
}
