public class TestMapBackedSet2 extends AbstractTestSet {
    public TestMapBackedSet2(String testName) {
        super(testName);
    }
    public static Test suite() {
        return new TestSuite(TestMapBackedSet2.class);
    }
    public static void main(String args[]) {
        String[] testCaseName = { TestMapBackedSet2.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
    public Set makeEmptySet() {
        return MapBackedSet.decorate(new LinkedMap());
    }
    protected Set setupSet() {
        Set set = makeEmptySet();
        for (int i = 0; i < 10; i++) {
            set.add(Integer.toString(i));
        }
        return set;
    }
    public void testOrdering() {
        Set set = setupSet();
        Iterator it = set.iterator();
        for (int i = 0; i < 10; i++) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
        for (int i = 0; i < 10; i += 2) {
            assertTrue("Must be able to remove int", set.remove(Integer.toString(i)));
        }
        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong after remove ", Integer.toString(i), it.next());
        }
        for (int i = 0; i < 10; i++) {
            set.add(Integer.toString(i));
        }
        assertEquals("Size of set is wrong!", 10, set.size());
        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
        for (int i = 0; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
    }
    public void testCanonicalEmptyCollectionExists() {
    }
    public void testCanonicalFullCollectionExists() {
    }
}
