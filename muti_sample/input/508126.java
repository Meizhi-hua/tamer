public class CopyOnWriteArraySetTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(CopyOnWriteArraySetTest.class);
    }
    static CopyOnWriteArraySet populatedSet(int n){
        CopyOnWriteArraySet a = new CopyOnWriteArraySet();
        assertTrue(a.isEmpty());
        for (int i = 0; i < n; ++i) 
            a.add(new Integer(i));
        assertFalse(a.isEmpty());
        assertEquals(n, a.size());
        return a;
    }
    public void testConstructor() {
        CopyOnWriteArraySet a = new CopyOnWriteArraySet();
        assertTrue(a.isEmpty());
    }
    public void testConstructor3() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE-1; ++i)
            ints[i] = new Integer(i);
        CopyOnWriteArraySet a = new CopyOnWriteArraySet(Arrays.asList(ints));
        for (int i = 0; i < SIZE; ++i) 
            assertTrue(a.contains(ints[i]));
    }
    public void testAddAll() {
        CopyOnWriteArraySet full = populatedSet(3);
        Vector v = new Vector();
        v.add(three);
        v.add(four);
        v.add(five);
        full.addAll(v);
        assertEquals(6, full.size());
    }
    public void testAddAll2() {
        CopyOnWriteArraySet full = populatedSet(3);
        Vector v = new Vector();
        v.add(three);
        v.add(four);
        v.add(one); 
        full.addAll(v);
        assertEquals(5, full.size());
    }
    public void testAdd2() {
        CopyOnWriteArraySet full = populatedSet(3);
        full.add(one);
        assertEquals(3, full.size());
    }
    public void testAdd3() {
        CopyOnWriteArraySet full = populatedSet(3);
        full.add(three);
        assertTrue(full.contains(three));
    }
    public void testClear() {
        CopyOnWriteArraySet full = populatedSet(3);
        full.clear();
        assertEquals(0, full.size());
    }
    public void testContains() {
        CopyOnWriteArraySet full = populatedSet(3);
        assertTrue(full.contains(one));
        assertFalse(full.contains(five));
    }
    public void testEquals() {
        CopyOnWriteArraySet a = populatedSet(3);
        CopyOnWriteArraySet b = populatedSet(3);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        a.add(m1);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        b.add(m1);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }
    public void testContainsAll() {
        CopyOnWriteArraySet full = populatedSet(3);
        Vector v = new Vector();
        v.add(one);
        v.add(two);
        assertTrue(full.containsAll(v));
        v.add(six);
        assertFalse(full.containsAll(v));
    }
    public void testIsEmpty() {
        CopyOnWriteArraySet empty = new CopyOnWriteArraySet();
        CopyOnWriteArraySet full = populatedSet(3);
        assertTrue(empty.isEmpty());
        assertFalse(full.isEmpty());
    }
    public void testIterator() {
        CopyOnWriteArraySet full = populatedSet(3);
        Iterator i = full.iterator();
        int j;
        for(j = 0; i.hasNext(); j++)
            assertEquals(j, ((Integer)i.next()).intValue());
        assertEquals(3, j);
    }
    public void testIteratorRemove () {
        CopyOnWriteArraySet full = populatedSet(3);
        Iterator it = full.iterator();
        it.next();
        try {
            it.remove();
            shouldThrow();
        }
        catch (UnsupportedOperationException success) {}
    }
    public void testToString() {
        CopyOnWriteArraySet full = populatedSet(3);
        String s = full.toString();
        for (int i = 0; i < 3; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }        
    public void testRemoveAll() {
        CopyOnWriteArraySet full = populatedSet(3);
        Vector v = new Vector();
        v.add(one);
        v.add(two);
        full.removeAll(v);
        assertEquals(1, full.size());
    }
    public void testRemove() {
        CopyOnWriteArraySet full = populatedSet(3);
        full.remove(one);
        assertFalse(full.contains(one));
        assertEquals(2, full.size());
    }
    public void testSize() {
        CopyOnWriteArraySet empty = new CopyOnWriteArraySet();
        CopyOnWriteArraySet full = populatedSet(3);
        assertEquals(3, full.size());
        assertEquals(0, empty.size());
    }
    public void testToArray() {
        CopyOnWriteArraySet full = populatedSet(3);
        Object[] o = full.toArray();
        assertEquals(3, o.length);
        assertEquals(0, ((Integer)o[0]).intValue());
        assertEquals(1, ((Integer)o[1]).intValue());
        assertEquals(2, ((Integer)o[2]).intValue());
    }
    public void testToArray2() {
        CopyOnWriteArraySet full = populatedSet(3);
        Integer[] i = new Integer[3];
        i = (Integer[])full.toArray(i);
        assertEquals(3, i.length);
        assertEquals(0, i[0].intValue());
        assertEquals(1, i[1].intValue());
        assertEquals(2, i[2].intValue());
    }
    public void testToArray_ArrayStoreException() {
        try {
            CopyOnWriteArraySet c = new CopyOnWriteArraySet();
            c.add("zfasdfsdf");
            c.add("asdadasd");
            c.toArray(new Long[5]);
            shouldThrow();
        } catch(ArrayStoreException e){}
    }
    public void testSerialization() {
        CopyOnWriteArraySet q = populatedSet(SIZE);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            CopyOnWriteArraySet r = (CopyOnWriteArraySet)in.readObject();
            assertEquals(q.size(), r.size());
            assertTrue(q.equals(r));
            assertTrue(r.equals(q));
        } catch(Exception e){
            unexpectedException();
        }
    }
}
