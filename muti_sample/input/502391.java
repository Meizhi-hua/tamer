public class _BitIntSet extends TestCase {
    public void test_basic() {
        BitIntSet set = new BitIntSet(32);
        assertEquals(0, set.elements());
        set.add(0);
        set.add(1);
        set.add(31);
        assertTrue(set.has(0));
        assertTrue(set.has(1));
        assertTrue(set.has(31));
        assertEquals(3, set.elements());
        assertFalse(set.has(2));
        assertFalse(set.has(7));
        assertFalse(set.has(30));
    }
    public void test_iterator() {
        BitIntSet set = new BitIntSet(32);
        set.add(0);
        set.add(0);
        set.add(1);
        set.add(1);
        set.add(31);
        set.add(31);
        IntIterator iter = set.iterator();
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 0);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 1);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 31);
        assertFalse(iter.hasNext());
        try {
            iter.next();
            fail();
        } catch (NoSuchElementException ex) {
        }
    }
    public void test_remove() {
        BitIntSet set = new BitIntSet(32);
        set.add(0);
        set.add(1);
        set.add(31);
        assertTrue(set.has(0));
        assertTrue(set.has(1));
        assertTrue(set.has(31));
        assertFalse(set.has(2));
        assertFalse(set.has(7));
        assertFalse(set.has(30));
        set.remove(0);
        assertFalse(set.has(0));
        assertTrue(set.has(1));
        assertTrue(set.has(31));
    }
    public void test_expand() {
        BitIntSet set = new BitIntSet(32);
        int[] values = {0, 1, 31, 32, 128};
        for (int i = 0; i < values.length; i++) {
            set.add(values[i]);
        }
        IntIterator iter = set.iterator();
        for (int i = 0; i < values.length; i++) {
            assertTrue(iter.hasNext());
            assertEquals(values[i], iter.next());
        }
        assertFalse(iter.hasNext());
    }
    public void test_merge() {
        BitIntSet setA = new BitIntSet(32);
        int[] valuesA = {0, 1, 31};
        for (int i = 0; i < valuesA.length; i++) {
            setA.add(valuesA[i]);
        }
        BitIntSet setB = new BitIntSet(32);
        int[] valuesB = {0, 5, 6, 8, 31};
        for (int i = 0; i < valuesB.length; i++) {
            setB.add(valuesB[i]);
        }
        setA.merge(setB);
        for (int i = 0; i < valuesA.length; i++) {
            assertTrue(setA.has(valuesA[i]));
        }
        for (int i = 0; i < valuesB.length; i++) {
            assertTrue(setA.has(valuesB[i]));
        }
    }
    public void test_mergeWithListIntSet() {
        BitIntSet setA = new BitIntSet(32);
        int[] valuesA = {0, 1, 31};
        for (int i = 0; i < valuesA.length; i++) {
            setA.add(valuesA[i]);
        }
        ListIntSet setB = new ListIntSet();
        int[] valuesB = {0, 5, 6, 8, 31};
        for (int i = 0; i < valuesB.length; i++) {
            setB.add(valuesB[i]);
        }
        setA.merge(setB);
        for (int i = 0; i < valuesA.length; i++) {
            assertTrue(setA.has(valuesA[i]));
        }
        for (int i = 0; i < valuesB.length; i++) {
            assertTrue(setA.has(valuesB[i]));
        }
    }
    public void test_mergeAndExpand() {
        BitIntSet setA = new BitIntSet(32);
        int[] valuesA = {0, 1, 31};
        for (int i = 0; i < valuesA.length; i++) {
            setA.add(valuesA[i]);
        }
        BitIntSet setB = new BitIntSet(32);
        int[] valuesB = {0, 5, 6, 32, 127};
        for (int i = 0; i < valuesB.length; i++) {
            setB.add(valuesB[i]);
        }
        setA.merge(setB);
        for (int i = 0; i < valuesA.length; i++) {
            assertTrue(setA.has(valuesA[i]));
        }
        for (int i = 0; i < valuesB.length; i++) {
            assertTrue(setA.has(valuesB[i]));
        }
    }
    public void test_toString() {
        BitIntSet set = new BitIntSet(32);
        assertEquals(set.toString(), "{}");
        set.add(1);
        assertEquals(set.toString(), "{1}");
        set.add(2);
        assertEquals(set.toString(), "{1, 2}");
    }
}
