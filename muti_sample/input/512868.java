@TestTargetClass(EnumMap.class) 
public class EnumMapTest extends TestCase {
    enum Size {
        Small, Middle, Big {};
    }
    enum Color {
        Red, Green, Blue {};
    }
    enum Empty {
    }
    private static class MockEntry<K, V> implements Map.Entry<K, V> {
        private K key;
        private V value;
        public MockEntry(K key, V value) {
            this.key   = key;
            this.value = value;
        }
        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }
        public K getKey() {
            return key;
        }
        public V getValue() {
            return value;
        }
        public V setValue(V object) {
            V oldValue = value;
            value = object;
            return oldValue;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EnumMap",
        args = {java.lang.Class.class}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_ConstructorLjava_lang_Class() {
        try {
            new EnumMap((Class) null);
            fail("Expected NullPointerException"); 
        } catch (NullPointerException e) {
        }
        try {
            new EnumMap(Size.Big.getClass());
            fail("Expected NullPointerException"); 
        } catch (NullPointerException e) {
        }
        try {
            new EnumMap(Integer.class);
            fail("Expected NullPointerException"); 
        } catch (NullPointerException e) {
        }
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        assertNull("Return non-null for non mapped key", enumColorMap.put( 
                Color.Green, 2));
        assertEquals("Get returned incorrect value for given key", 2, 
                enumColorMap.get(Color.Green));
        EnumMap enumEmptyMap = new EnumMap<Empty, Double>(Empty.class);
        try {
            enumEmptyMap.put(Color.Red, 2);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        EnumMap enumSizeMap = new EnumMap(Size.class);
        assertNull("Return non-null for non mapped key", enumSizeMap.put( 
                Size.Big, 2));
        assertEquals("Get returned incorrect value for given key", 2, 
                enumSizeMap.get(Size.Big));
        try {
            enumSizeMap.put(Color.Red, 2);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        enumSizeMap = new EnumMap(Size.Middle.getClass());
        assertNull("Return non-null for non mapped key", enumSizeMap.put( 
                Size.Small, 1));
        assertEquals("Get returned incorrect value for given key", 1, 
                enumSizeMap.get(Size.Small));
        try {
            enumSizeMap.put(Color.Red, 2);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EnumMap",
        args = {java.util.EnumMap.class}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_ConstructorLjava_util_EnumMap() {
        EnumMap enumMap;
        EnumMap enumColorMap = null;
        try {
            enumMap = new EnumMap(enumColorMap);
            fail("Expected NullPointerException"); 
        } catch (NullPointerException e) {
        }
        enumColorMap = new EnumMap<Color, Double>(Color.class);
        Double double1 = new Double(1);
        enumColorMap.put(Color.Green, 2);
        enumColorMap.put(Color.Blue, double1);
        enumMap = new EnumMap(enumColorMap);
        assertEquals("Constructor fails", 2, enumMap.get(Color.Green)); 
        assertSame("Constructor fails", double1, enumMap.get(Color.Blue)); 
        assertNull("Constructor fails", enumMap.get(Color.Red)); 
        enumMap.put(Color.Red, 1);
        assertEquals("Wrong value", 1, enumMap.get(Color.Red)); 
        try {
            enumMap.put(Size.Middle, 2);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EnumMap",
        args = {java.util.Map.class}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_ConstructorLjava_util_Map() {
        EnumMap enumMap;
        Map enumColorMap = null;
        try {
            enumMap = new EnumMap(enumColorMap);
            fail("Expected NullPointerException"); 
        } catch (NullPointerException e) {
        }
        enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumMap      = new EnumMap(enumColorMap);
        enumColorMap.put(Color.Blue, 3);
        enumMap      = new EnumMap(enumColorMap);
        HashMap hashColorMap = null;
        try {
            enumMap = new EnumMap(hashColorMap);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        hashColorMap = new HashMap();
        try {
            enumMap = new EnumMap(hashColorMap);
            fail("Expected IllegalArgumentException"); 
        } catch (IllegalArgumentException e) {
        }
        hashColorMap.put(Color.Green, 2);
        enumMap = new EnumMap(hashColorMap);
        assertEquals("Constructor fails", 2, enumMap.get(Color.Green)); 
        assertNull("Constructor fails", enumMap.get(Color.Red)); 
        enumMap.put(Color.Red, 1);
        assertEquals("Wrong value", 1, enumMap.get(Color.Red)); 
        hashColorMap.put(Size.Big, 3);
        try {
            enumMap = new EnumMap(hashColorMap);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        hashColorMap = new HashMap();
        hashColorMap.put(new Integer(1), 1);
        try {
            enumMap = new EnumMap(hashColorMap);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_clear() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Small, 1);
        enumSizeMap.clear();
        assertNull("Failed to clear all elements", enumSizeMap.get(Size.Small)); 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsKey",
        args = {java.lang.Object.class}
    )
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_containsKeyLjava_lang_Object() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        assertFalse("Returned true for uncontained key", enumSizeMap 
                .containsKey(Size.Small));
        enumSizeMap.put(Size.Small, 1);
        assertTrue("Returned false for contained key", enumSizeMap 
                .containsKey(Size.Small));
        enumSizeMap.put(Size.Big, null);
        assertTrue("Returned false for contained key", enumSizeMap 
                .containsKey(Size.Big));
        assertFalse("Returned true for uncontained key", enumSizeMap 
                .containsKey(Color.Red));
        assertFalse("Returned true for uncontained key", enumSizeMap 
                .containsKey(new Integer("3"))); 
        assertFalse("Returned true for uncontained key", enumSizeMap 
                .containsKey(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_clone() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        Integer integer = new Integer("3"); 
        enumSizeMap.put(Size.Small, integer);
        EnumMap enumSizeMapClone = enumSizeMap.clone();
        assertNotSame("Should not be same", enumSizeMap, enumSizeMapClone); 
        assertEquals("Clone answered unequal EnumMap", enumSizeMap, 
                enumSizeMapClone);
        assertSame("Should be same", enumSizeMap.get(Size.Small), 
                enumSizeMapClone.get(Size.Small));
        assertSame("Clone is not shallow clone", integer, enumSizeMapClone 
                .get(Size.Small));
        enumSizeMap.remove(Size.Small);
        assertSame("Clone is not shallow clone", integer, enumSizeMapClone 
                .get(Size.Small));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsValue",
        args = {java.lang.Object.class}
    )
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_containsValueLjava_lang_Object() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        Double double1 = new Double(3);
        Double double2 = new Double(3);
        assertFalse("Returned true for uncontained value", enumSizeMap 
                .containsValue(double1));
        enumSizeMap.put(Size.Middle, 2);
        enumSizeMap.put(Size.Small, double1);
        assertTrue("Returned false for contained value", enumSizeMap 
                .containsValue(double1));
        assertTrue("Returned false for contained value", enumSizeMap 
                .containsValue(double2));
        assertTrue("Returned false for contained value", enumSizeMap 
                .containsValue(2));
        assertFalse("Returned true for uncontained value", enumSizeMap 
                .containsValue(1));
        assertFalse("Returned true for uncontained value", enumSizeMap 
                .containsValue(null));
        enumSizeMap.put(Size.Big, null);
        assertTrue("Returned false for contained value", enumSizeMap 
                .containsValue(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entrySet",
        args = {}
    )
    @AndroidOnly("Map.Entry is indirectly modified on RI when Iterator.next() is invoked")
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_entrySet() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        MockEntry mockEntry = new MockEntry(Size.Middle, 1);
        Set set = enumSizeMap.entrySet();
        Set set1 = enumSizeMap.entrySet();
        assertSame("Should be same", set1, set); 
        try {
            set.add(mockEntry);
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
        assertTrue("Returned false for contained object", set
                .contains(mockEntry));
        mockEntry = new MockEntry(Size.Middle, null);
        assertFalse("Returned true for uncontained object", set 
                .contains(mockEntry));
        assertFalse("Returned true for uncontained object", set 
                .contains(Size.Small));
        mockEntry = new MockEntry(new Integer(1), 1);
        assertFalse("Returned true for uncontained object", set 
                .contains(mockEntry));
        assertFalse("Returned true for uncontained object", set 
                .contains(new Integer(1)));
        mockEntry = new MockEntry(Size.Big, null);
        assertTrue("Returned false for contained object", set
                .contains(mockEntry));
        assertTrue("Returned false when the object can be removed", set 
                .remove(mockEntry));
        assertFalse("Returned true for uncontained object", set 
                .contains(mockEntry));
        assertFalse("Returned true when the object can not be removed", set 
                .remove(mockEntry));
        mockEntry = new MockEntry(new Integer(1), 1);
        assertFalse("Returned true when the object can not be removed", set 
                .remove(mockEntry));
        assertFalse("Returned true when the object can not be removed", set 
                .remove(new Integer(1)));
        enumSizeMap.put(Size.Big, 3);
        mockEntry = new MockEntry(Size.Big, 3);
        assertTrue("Returned false for contained object", set
                .contains(mockEntry));
        enumSizeMap.remove(Size.Big);
        assertFalse("Returned true for uncontained object", set 
                .contains(mockEntry));
        assertEquals("Wrong size", 1, set.size()); 
        set.clear();
        assertEquals("Wrong size", 0, set.size()); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.entrySet();
        Collection c = new ArrayList();
        c.add(new MockEntry(Size.Middle, 1));
        assertTrue("Return wrong value", set.containsAll(c)); 
        assertTrue("Remove does not success", set.removeAll(c)); 
        enumSizeMap.put(Size.Middle, 1);
        c.add(new MockEntry(Size.Big, 3));
        assertTrue("Remove does not success", set.removeAll(c)); 
        assertFalse("Should return false", set.removeAll(c)); 
        assertEquals("Wrong size", 1, set.size()); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.entrySet();
        c = new ArrayList();
        c.add(new MockEntry(Size.Middle, 1));
        c.add(new MockEntry(Size.Big, 3));
        assertTrue("Retain does not success", set.retainAll(c)); 
        assertEquals("Wrong size", 1, set.size()); 
        assertFalse("Should return false", set.retainAll(c)); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.entrySet();
        Object[] array = set.toArray();
        assertEquals("Wrong length", 2, array.length); 
        Map.Entry entry = (Map.Entry) array[0];
        assertEquals("Wrong key", Size.Middle, entry.getKey()); 
        assertEquals("Wrong value", 1, entry.getValue()); 
        Object[] array1 = new Object[10];
        array1 = set.toArray();
        assertEquals("Wrong length", 2, array1.length); 
        entry = (Map.Entry) array[0];
        assertEquals("Wrong key", Size.Middle, entry.getKey()); 
        assertEquals("Wrong value", 1, entry.getValue()); 
        array1 = new Object[10];
        array1 = set.toArray(array1);
        assertEquals("Wrong length", 10, array1.length); 
        entry = (Map.Entry) array[1];
        assertEquals("Wrong key", Size.Big, entry.getKey()); 
        assertNull("Should be null", array1[2]); 
        set = enumSizeMap.entrySet();
        Integer integer = new Integer("1"); 
        assertFalse("Returned true when the object can not be removed", set 
                .remove(integer));
        assertTrue("Returned false when the object can be removed", set 
                .remove(entry));
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.entrySet();
        Iterator iter = set.iterator();
        entry = (Map.Entry) iter.next();
        assertTrue("Returned false for contained object", set.contains(entry)); 
        mockEntry = new MockEntry(Size.Middle, 2);
        assertFalse("Returned true for uncontained object", set 
                .contains(mockEntry));
        mockEntry = new MockEntry(new Integer(2), 2);
        assertFalse("Returned true for uncontained object", set 
                .contains(mockEntry));
        entry = (Map.Entry) iter.next();
        assertTrue("Returned false for contained object", set.contains(entry)); 
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.remove(Size.Big);
        mockEntry = new MockEntry(Size.Big, null);
        assertEquals("Wrong size", 1, set.size()); 
        assertFalse("Returned true for uncontained object", set.contains(mockEntry)); 
        enumSizeMap.put(Size.Big, 2);
        mockEntry = new MockEntry(Size.Big, 2);
        assertTrue("Returned false for contained object", set 
                .contains(mockEntry));
        iter.remove();
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        try {
            entry.setValue(2);
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        try {
            set.contains(entry);
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.entrySet();
        iter = set.iterator();
        entry = (Map.Entry) iter.next();
        assertEquals("Wrong key", Size.Middle, entry.getKey()); 
        assertTrue("Returned false for contained object", set.contains(entry)); 
        enumSizeMap.put(Size.Middle, 3);
        assertTrue("Returned false for contained object", set.contains(entry)); 
        entry.setValue(2);
        assertTrue("Returned false for contained object", set.contains(entry)); 
        assertFalse("Returned true for uncontained object", set 
                .remove(new Integer(1)));
        iter.next();
        assertEquals("Wrong key", Size.Middle, entry.getKey()); 
        set.clear();
        assertEquals("Wrong size", 0, set.size()); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.entrySet();
        iter = set.iterator();
        mockEntry = new MockEntry(Size.Middle, 1);
        assertFalse("Wrong result", entry.equals(mockEntry)); 
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        entry = (Map.Entry) iter.next();
        assertEquals("Wrong key", Size.Middle, entry.getKey()); 
        assertTrue("Should return true", entry.equals(mockEntry)); 
        assertEquals("Should be equal", mockEntry.hashCode(), entry.hashCode()); 
        mockEntry = new MockEntry(Size.Big, 1);
        assertFalse("Wrong result", entry.equals(mockEntry)); 
        entry = (Map.Entry) iter.next();
        assertFalse("Wrong result", entry.equals(mockEntry)); 
        assertEquals("Wrong key", Size.Big, entry.getKey()); 
        iter.remove();
        assertFalse("Wrong result", entry.equals(mockEntry)); 
        assertEquals("Wrong size", 1, set.size()); 
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        try {
            iter.next();
            fail("Should throw NoSuchElementException"); 
        } catch (NoSuchElementException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_equalsLjava_lang_Object() {
        EnumMap enumMap = new EnumMap(Size.class);
        enumMap.put(Size.Small, 1);
        EnumMap enumSizeMap = new EnumMap(Size.class);
        assertFalse("Returned true for unequal EnumMap", enumSizeMap 
                .equals(enumMap));
        enumSizeMap.put(Size.Small, 1);
        assertTrue("Returned false for equal EnumMap", enumSizeMap 
                .equals(enumMap));
        enumSizeMap.put(Size.Big, null);
        assertFalse("Returned true for unequal EnumMap", enumSizeMap 
                .equals(enumMap));
        enumMap.put(Size.Middle, null);
        assertFalse("Returned true for unequal EnumMap", enumSizeMap 
                .equals(enumMap));
        enumMap.remove(Size.Middle);
        enumMap.put(Size.Big, 3);
        assertFalse("Returned true for unequal EnumMap", enumSizeMap 
                .equals(enumMap));
        enumMap.put(Size.Big, null);
        assertTrue("Returned false for equal EnumMap", enumSizeMap 
                .equals(enumMap));
        HashMap hashMap = new HashMap();
        hashMap.put(Size.Small, 1);
        assertFalse("Returned true for unequal EnumMap", hashMap 
                .equals(enumMap));
        hashMap.put(Size.Big, null);
        assertTrue("Returned false for equal EnumMap", enumMap.equals(hashMap)); 
        assertFalse("Should return false", enumSizeMap 
                .equals(new Integer(1)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_keySet() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 2);
        enumSizeMap.put(Size.Big, null);
        Set set = enumSizeMap.keySet();
        Set set1 = enumSizeMap.keySet();
        assertSame("Should be same", set1, set); 
        try {
            set.add(Size.Big);
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
        assertTrue("Returned false for contained object", set
                .contains(Size.Middle));
        assertTrue("Returned false for contained object", set
                .contains(Size.Big));
        assertFalse("Returned true for uncontained object", set 
                .contains(Size.Small));
        assertFalse("Returned true for uncontained object", set 
                .contains(new Integer(1)));
        assertTrue("Returned false when the object can be removed", set 
                .remove(Size.Big));
        assertFalse("Returned true for uncontained object", set 
                .contains(Size.Big));
        assertFalse("Returned true when the object can not be removed", set 
                .remove(Size.Big));
        assertFalse("Returned true when the object can not be removed", set 
                .remove(new Integer(1)));
        enumSizeMap.put(Size.Big, 3);
        assertTrue("Returned false for contained object", set
                .contains(Size.Big));
        enumSizeMap.remove(Size.Big);
        assertFalse("Returned true for uncontained object", set 
                .contains(Size.Big));
        assertEquals("Wrong size", 1, set.size()); 
        set.clear();
        assertEquals("Wrong size", 0, set.size()); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.keySet();
        Collection c = new ArrayList();
        c.add(Size.Big);
        assertTrue("Should return true", set.containsAll(c)); 
        c.add(Size.Small);
        assertFalse("Should return false", set.containsAll(c)); 
        assertTrue("Should return true", set.removeAll(c)); 
        assertEquals("Wrong size", 1, set.size()); 
        assertFalse("Should return false", set.removeAll(c)); 
        assertEquals("Wrong size", 1, set.size()); 
        try {
            set.addAll(c);
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
        enumSizeMap.put(Size.Big, null);
        assertEquals("Wrong size", 2, set.size()); 
        assertTrue("Should return true", set.retainAll(c)); 
        assertEquals("Wrong size", 1, set.size()); 
        assertFalse("Should return false", set.retainAll(c)); 
        assertEquals(1, set.size());
        Object[] array = set.toArray();
        assertEquals("Wrong length", 1, array.length); 
        assertEquals("Wrong key", Size.Big, array[0]); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.keySet();
        c = new ArrayList();
        c.add(Color.Blue);
        assertFalse("Should return false", set.remove(c)); 
        assertEquals("Wrong size", 2, set.size()); 
        assertTrue("Should return true", set.retainAll(c)); 
        assertEquals("Wrong size", 0, set.size()); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.keySet();
        Iterator iter = set.iterator();
        Enum enumKey = (Enum) iter.next();
        assertTrue("Returned false for contained object", set.contains(enumKey)); 
        enumKey = (Enum) iter.next();
        assertTrue("Returned false for contained object", set.contains(enumKey)); 
        enumSizeMap.remove(Size.Big);
        assertFalse("Returned true for uncontained object", set 
                .contains(enumKey));
        iter.remove();
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        assertFalse("Returned true for uncontained object", set 
                .contains(enumKey));
        iter = set.iterator();
        enumKey = (Enum) iter.next();
        assertTrue("Returned false for contained object", set.contains(enumKey)); 
        enumSizeMap.put(Size.Middle, 3);
        assertTrue("Returned false for contained object", set.contains(enumKey)); 
        enumSizeMap = new EnumMap(Size.class);
        enumSizeMap.put(Size.Middle, 1);
        enumSizeMap.put(Size.Big, null);
        set = enumSizeMap.keySet();
        iter = set.iterator();
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        enumKey = (Enum) iter.next();
        assertEquals("Wrong key", Size.Middle, enumKey); 
        assertSame("Wrong key", Size.Middle, enumKey); 
        assertFalse("Returned true for unequal object", iter.equals(enumKey)); 
        iter.remove();
        assertFalse("Returned true for uncontained object", set 
                .contains(enumKey));
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        assertEquals("Wrong size", 1, set.size()); 
        enumKey = (Enum) iter.next();
        assertEquals("Wrong key", Size.Big, enumKey); 
        iter.remove();
        try {
            iter.next();
            fail("Should throw NoSuchElementException"); 
        } catch (NoSuchElementException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {java.lang.Object.class}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_getLjava_lang_Object() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        assertNull("Get returned non-null for non mapped key", enumSizeMap 
                .get(Size.Big));
        enumSizeMap.put(Size.Big, 1);
        assertEquals("Get returned incorrect value for given key", 1, 
                enumSizeMap.get(Size.Big));
        assertNull("Get returned non-null for non mapped key", enumSizeMap 
                .get(Size.Small));
        assertNull("Get returned non-null for non existent key", enumSizeMap 
                .get(Color.Red));
        assertNull("Get returned non-null for non existent key", enumSizeMap 
                .get(new Integer(1)));
        assertNull("Get returned non-null for non existent key", enumSizeMap 
                .get(null));
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        assertNull("Get returned non-null for non mapped key", enumColorMap 
                .get(Color.Green));
        enumColorMap.put(Color.Green, 2);
        assertEquals("Get returned incorrect value for given key", 2, 
                enumColorMap.get(Color.Green));
        assertNull("Get returned non-null for non mapped key", enumColorMap 
                .get(Color.Blue));
        enumColorMap.put(Color.Green, new Double(4));
        assertEquals("Get returned incorrect value for given key", 
                new Double(4), enumColorMap.get(Color.Green));
        enumColorMap.put(Color.Green, new Integer("3"));
        assertEquals("Get returned incorrect value for given key", new Integer( 
                "3"), enumColorMap.get(Color.Green));
        enumColorMap.put(Color.Green, null);
        assertNull("Can not handle null value", enumColorMap.get(Color.Green)); 
        Float f = new Float("3.4");
        enumColorMap.put(Color.Green, f);
        assertSame("Get returned incorrect value for given key", f, 
                enumColorMap.get(Color.Green));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "put",
        args = {Enum.class, java.lang.Object.class}
    )
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        try {
            enumSizeMap.put(Color.Red, 2);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        assertNull("Return non-null for non mapped key", enumSizeMap.put( 
                Size.Small, 1));
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        try {
            enumColorMap.put(Size.Big, 2);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        try {
            enumColorMap.put(null, 2);
            fail("Expected NullPointerException"); 
        } catch (NullPointerException e) {
        }
        assertNull("Return non-null for non mapped key", enumColorMap.put( 
                Color.Green, 2));
        assertEquals("Return wrong value", 2, enumColorMap.put(Color.Green, 
                new Double(4)));
        assertEquals("Return wrong value", new Double(4), enumColorMap.put( 
                Color.Green, new Integer("3")));
        assertEquals("Return wrong value", new Integer("3"), enumColorMap.put( 
                Color.Green, null));
        Float f = new Float("3.4");
        assertNull("Return non-null for non mapped key", enumColorMap.put( 
                Color.Green, f));
        assertNull("Return non-null for non mapped key", enumColorMap.put( 
                Color.Blue, 2));
        assertEquals("Return wrong value", 2, enumColorMap.put(Color.Blue, 
                new Double(4)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "putAll",
        args = {java.util.Map.class}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_putAllLjava_util_Map() {
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumColorMap.put(Color.Green, 2);
        EnumMap enumSizeMap = new EnumMap(Size.class);
        enumColorMap.putAll(enumSizeMap);
        enumSizeMap.put(Size.Big, 1);
        try {
            enumColorMap.putAll(enumSizeMap);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        EnumMap enumColorMap1 = new EnumMap<Color, Double>(Color.class);
        enumColorMap1.put(Color.Blue, 3);
        enumColorMap.putAll(enumColorMap1);
        assertEquals("Get returned incorrect value for given key", 3, 
                enumColorMap.get(Color.Blue));
        assertEquals("Wrong Size", 2, enumColorMap.size()); 
        enumColorMap = new EnumMap<Color, Double>(Color.class);
        HashMap hashColorMap = null;
        try {
            enumColorMap.putAll(hashColorMap);
            fail("Expected NullPointerException"); 
        } catch (NullPointerException e) {
        }
        hashColorMap = new HashMap();
        enumColorMap.putAll(hashColorMap);
        hashColorMap.put(Color.Green, 2);
        enumColorMap.putAll(hashColorMap);
        assertEquals("Get returned incorrect value for given key", 2, 
                enumColorMap.get(Color.Green));
        assertNull("Get returned non-null for non mapped key", enumColorMap 
                .get(Color.Red));
        hashColorMap.put(Color.Red, new Integer(1));
        enumColorMap.putAll(hashColorMap);
        assertEquals("Get returned incorrect value for given key", new Integer(
                2), enumColorMap.get(Color.Green));
        hashColorMap.put(Size.Big, 3);
        try {
            enumColorMap.putAll(hashColorMap);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        hashColorMap = new HashMap();
        hashColorMap.put(new Integer(1), 1);
        try {
            enumColorMap.putAll(hashColorMap);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {java.lang.Object.class}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_removeLjava_lang_Object() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        assertNull("Remove of non-mapped key returned non-null", enumSizeMap 
                .remove(Size.Big));
        enumSizeMap.put(Size.Big, 3);
        enumSizeMap.put(Size.Middle, 2);
        assertNull("Get returned non-null for non mapped key", enumSizeMap 
                .get(Size.Small));
        assertEquals("Remove returned incorrect value", 3, enumSizeMap 
                .remove(Size.Big));
        assertNull("Get returned non-null for non mapped key", enumSizeMap 
                .get(Size.Big));
        assertNull("Remove of non-mapped key returned non-null", enumSizeMap 
                .remove(Size.Big));
        assertNull("Remove of non-existent key returned non-null", enumSizeMap 
                .remove(Color.Red));
        assertNull("Remove of non-existent key returned non-null", enumSizeMap 
                .remove(new Double(4)));
        assertNull("Remove of non-existent key returned non-null", enumSizeMap 
                .remove(null));
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        assertNull("Get returned non-null for non mapped key", enumColorMap 
                .get(Color.Green));
        enumColorMap.put(Color.Green, new Double(4));
        assertEquals("Remove returned incorrect value", new Double(4), 
                enumColorMap.remove(Color.Green));
        assertNull("Get returned non-null for non mapped key", enumColorMap 
                .get(Color.Green));
        enumColorMap.put(Color.Green, null);
        assertNull("Can not handle null value", enumColorMap 
                .remove(Color.Green));
        assertNull("Get returned non-null for non mapped key", enumColorMap 
                .get(Color.Green));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void test_size() {
        EnumMap enumSizeMap = new EnumMap(Size.class);
        assertEquals("Wrong size", 0, enumSizeMap.size()); 
        enumSizeMap.put(Size.Small, 1);
        assertEquals("Wrong size", 1, enumSizeMap.size()); 
        enumSizeMap.put(Size.Small, 0);
        assertEquals("Wrong size", 1, enumSizeMap.size()); 
        try {
            enumSizeMap.put(Color.Red, 2);
            fail("Expected ClassCastException"); 
        } catch (ClassCastException e) {
        }
        assertEquals("Wrong size", 1, enumSizeMap.size()); 
        enumSizeMap.put(Size.Middle, null);
        assertEquals("Wrong size", 2, enumSizeMap.size()); 
        enumSizeMap.remove(Size.Big);
        assertEquals("Wrong size", 2, enumSizeMap.size()); 
        enumSizeMap.remove(Size.Middle);
        assertEquals("Wrong size", 1, enumSizeMap.size()); 
        enumSizeMap.remove(Color.Green);
        assertEquals("Wrong size", 1, enumSizeMap.size()); 
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumColorMap.put(Color.Green, 2);
        assertEquals("Wrong size", 1, enumColorMap.size()); 
        enumColorMap.remove(Color.Green);
        assertEquals("Wrong size", 0, enumColorMap.size()); 
        EnumMap enumEmptyMap = new EnumMap<Empty, Double>(Empty.class);
        assertEquals("Wrong size", 0, enumEmptyMap.size()); 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_values() {
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumColorMap.put(Color.Red, 1);
        enumColorMap.put(Color.Blue, null);
        Collection collection = enumColorMap.values();
        Collection collection1 = enumColorMap.values();
        assertSame("Should be same", collection1, collection); 
        try {
            collection.add(new Integer(1));
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
        assertTrue("Returned false for contained object", collection
                .contains(1));
        assertTrue("Returned false for contained object", collection
                .contains(null));
        assertFalse("Returned true for uncontained object", collection 
                .contains(2));
        assertTrue("Returned false when the object can be removed", collection 
                .remove(null));
        assertFalse("Returned true for uncontained object", collection 
                .contains(null));
        assertFalse("Returned true when the object can not be removed", 
                collection.remove(null));
        enumColorMap.put(Color.Blue, 3);
        assertTrue("Returned false for contained object", collection
                .contains(3));
        enumColorMap.remove(Color.Blue);
        assertFalse("Returned true for uncontained object", collection
                .contains(3));
        assertEquals("Wrong size", 1, collection.size()); 
        collection.clear();
        assertEquals("Wrong size", 0, collection.size()); 
        enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumColorMap.put(Color.Red, 1);
        enumColorMap.put(Color.Blue, null);
        collection = enumColorMap.values();
        Collection c = new ArrayList();
        c.add(new Integer(1));
        assertTrue("Should return true", collection.containsAll(c)); 
        c.add(new Double(3.4));
        assertFalse("Should return false", collection.containsAll(c)); 
        assertTrue("Should return true", collection.removeAll(c)); 
        assertEquals("Wrong size", 1, collection.size()); 
        assertFalse("Should return false", collection.removeAll(c)); 
        assertEquals("Wrong size", 1, collection.size()); 
        try {
            collection.addAll(c);
            fail("Should throw UnsupportedOperationException"); 
        } catch (UnsupportedOperationException e) {
        }
        enumColorMap.put(Color.Red, 1);
        assertEquals("Wrong size", 2, collection.size()); 
        assertTrue("Should return true", collection.retainAll(c)); 
        assertEquals("Wrong size", 1, collection.size()); 
        assertFalse("Should return false", collection.retainAll(c)); 
        assertEquals(1, collection.size());
        Object[] array = collection.toArray();
        assertEquals("Wrong length", 1, array.length); 
        assertEquals("Wrong key", 1, array[0]); 
        enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumColorMap.put(Color.Red, 1);
        enumColorMap.put(Color.Blue, null);
        collection = enumColorMap.values();
        assertEquals("Wrong size", 2, collection.size()); 
        assertFalse("Returned true when the object can not be removed", 
                collection.remove(new Integer("10"))); 
        Iterator iter = enumColorMap.values().iterator();
        Object value = iter.next();
        assertTrue("Returned false for contained object", collection 
                .contains(value));
        value = iter.next();
        assertTrue("Returned false for contained object", collection 
                .contains(value));
        enumColorMap.put(Color.Green, 1);
        enumColorMap.remove(Color.Blue);
        assertFalse("Returned true for uncontained object", collection 
                .contains(value));
        iter.remove();
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        assertFalse("Returned true for uncontained object", collection 
                .contains(value));
        iter = enumColorMap.values().iterator();
        value = iter.next();
        assertTrue("Returned false for contained object", collection 
                .contains(value));
        enumColorMap.put(Color.Green, 3);
        assertTrue("Returned false for contained object", collection 
                .contains(value));
        assertTrue("Returned false for contained object", collection 
                .remove(new Integer("1"))); 
        assertEquals("Wrong size", 1, collection.size()); 
        collection.clear();
        assertEquals("Wrong size", 0, collection.size()); 
        enumColorMap = new EnumMap<Color, Double>(Color.class);
        Integer integer1 = new Integer(1);
        enumColorMap.put(Color.Green, integer1);
        enumColorMap.put(Color.Blue, null);
        collection = enumColorMap.values();
        iter = enumColorMap.values().iterator();
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        value = iter.next();
        assertEquals("Wrong value", integer1, value); 
        assertSame("Wrong value", integer1, value); 
        assertFalse("Returned true for unequal object", iter.equals(value)); 
        iter.remove();
        assertFalse("Returned true for unequal object", iter.equals(value)); 
        try {
            iter.remove();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        assertEquals("Wrong size", 1, collection.size()); 
        value = iter.next();
        assertFalse("Returned true for unequal object", iter.equals(value)); 
        iter.remove();
        try {
            iter.next();
            fail("Should throw NoSuchElementException"); 
        } catch (NoSuchElementException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )    
    @SuppressWarnings({ "unchecked", "boxing" })
    public void testSerializationSelf() throws Exception {
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumColorMap.put(Color.Blue, 3);
        SerializationTest.verifySelf(enumColorMap);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )
    @SuppressWarnings({ "unchecked", "boxing" })
    public void testSerializationCompatibility() throws Exception {
        EnumMap enumColorMap = new EnumMap<Color, Double>(Color.class);
        enumColorMap.put(Color.Red, 1);
        enumColorMap.put(Color.Blue, 3);
        SerializationTest.verifyGolden(this, enumColorMap);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
    }
}
