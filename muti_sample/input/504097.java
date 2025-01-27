@TestTargetClass(AbstractMap.class) 
public class AbstractMapTest extends junit.framework.TestCase {
    static final String specialKey = "specialKey".intern();
    static final String specialValue = "specialValue".intern();
    class MyMap extends AbstractMap {
        final Set mySet = new HashSet(1);
        MyMap() {
            mySet.add(new Map.Entry() {
                public Object getKey() {
                    return specialKey;
                }
                public Object getValue() {
                    return specialValue;
                }
                public Object setValue(Object object) {
                    return null;
                }
            });
        }
        public Object put(Object key, Object value) {
            return null;
        }
        public Set entrySet() {
            return mySet;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify returned set of keys.",
        method = "keySet",
        args = {}
    )
    public void test_keySet() {
        AbstractMap map1 = new HashMap(0);
        assertSame("HashMap(0)", map1.keySet(), map1.keySet());
        AbstractMap map2 = new HashMap(10);
        assertSame("HashMap(10)", map2.keySet(), map2.keySet());
        Map map3 = Collections.EMPTY_MAP;
        assertSame("EMPTY_MAP", map3.keySet(), map3.keySet());
        AbstractMap map4 = new IdentityHashMap(1);
        assertSame("IdentityHashMap", map4.keySet(), map4.keySet());
        AbstractMap map5 = new LinkedHashMap(122);
        assertSame("LinkedHashMap", map5.keySet(), map5.keySet());
        AbstractMap map6 = new TreeMap();
        assertSame("TreeMap", map6.keySet(), map6.keySet());
        AbstractMap map7 = new WeakHashMap();
        assertSame("WeakHashMap", map7.keySet(), map7.keySet());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify UnsupportedOperationException.",
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void test_removeLjava_lang_Object() {
        Object key = new Object();
        Object value = new Object();
        AbstractMap map1 = new HashMap(0);
        map1.put("key", value);
        assertSame("HashMap(0)", map1.remove("key"), value);
        AbstractMap map4 = new IdentityHashMap(1);
        map4.put(key, value);
        assertSame("IdentityHashMap", map4.remove(key), value);
        AbstractMap map5 = new LinkedHashMap(122);
        map5.put(key, value);
        assertSame("LinkedHashMap", map5.remove(key), value);
        AbstractMap map6 = new TreeMap(new Comparator() {
            public int compare(Object object1, Object object2) {
                return 0;
            }
        });
        map6.put(key, value);
        assertSame("TreeMap", map6.remove(key), value);
        AbstractMap map7 = new WeakHashMap();
        map7.put(key, value);
        assertSame("WeakHashMap", map7.remove(key), value);
        AbstractMap aSpecialMap = new MyMap();
        aSpecialMap.put(specialKey, specialValue);
        Object valueOut = aSpecialMap.remove(specialKey);
        assertSame("MyMap", valueOut, specialValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify returned Collection.",
        method = "values",
        args = {}
    )
    public void test_values() {
        AbstractMap map1 = new HashMap(0);
        assertSame("HashMap(0)", map1.values(), map1.values());
        AbstractMap map2 = new HashMap(10);
        assertSame("HashMap(10)", map2.values(), map2.values());
        Map map3 = Collections.EMPTY_MAP;
        assertSame("EMPTY_MAP", map3.values(), map3.values());
        AbstractMap map4 = new IdentityHashMap(1);
        assertSame("IdentityHashMap", map4.values(), map4.values());
        AbstractMap map5 = new LinkedHashMap(122);
        assertSame("IdentityHashMap", map5.values(), map5.values());
        AbstractMap map6 = new TreeMap();
        assertSame("TreeMap", map6.values(), map6.values());
        AbstractMap map7 = new WeakHashMap();
        assertSame("WeakHashMap", map7.values(), map7.values());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify CloneNotSupportedException.",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        class MyMap extends AbstractMap implements Cloneable {
            private Map map = new HashMap();
            public Set entrySet() {
                return map.entrySet();
            }
            public Object put(Object key, Object value) {
                return map.put(key, value);
            }
            public Map getMap() {
                return map;
            }
            public Object clone() {
                try {
                    return super.clone();
                } catch (CloneNotSupportedException e) {
                    throw new AssertionError(e); 
                }
            }
        }
        ;
        MyMap map = new MyMap();
        map.put("one", "1");
        Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
        assertTrue("entry not added", entry.getKey() == "one"
                && entry.getValue() == "1");
        MyMap mapClone = (MyMap) map.clone();
        assertTrue("clone not shallow", map.getMap() == mapClone.getMap());
    }
    public class AMT extends AbstractMap {
        Vector values = new Vector();
        Vector keys   = new Vector();
        public Set entrySet() {
            return new AbstractSet() {
                public Iterator iterator() {
                    return new Iterator() {
                        int index = 0;
                        public boolean hasNext() {
                            return index < values.size();
                        }
                        public Object next() {
                            if (index < values.size()) {
                                Map.Entry me = new Map.Entry() {
                                    Object v = values.elementAt(index);
                                    Object k = keys.elementAt(index);
                                    public Object getKey() {
                                        return k;
                                    }
                                    public Object getValue() {
                                        return v;
                                    }
                                    public Object setValue(Object value) {
                                        return null;
                                    }
                                };
                                index++;
                                return me;
                            }
                            return null;
                        }
                        public void remove() {
                        }
                    };
                }
                public int size() {
                    return values.size();
                }
            };
        }
        public Object put(Object k, Object v) {
            keys.add(k);
            values.add(v);
            return v;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "putAll",
        args = {java.util.Map.class}
    )
    public void test_putAllLMap() {
        Hashtable ht  = new Hashtable();
        AMT amt = new AMT();
        ht.put("this", "that");
        amt.putAll(ht);
        assertEquals("Should be equal", amt, ht);
    }
    public void testEqualsWithNullValues() {
        Map<String, String> a = new HashMap<String, String>();
        a.put("a", null);
        a.put("b", null);
        Map<String, String> b = new HashMap<String, String>();
        a.put("c", "cat");
        a.put("d", "dog");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }
    public void testNullsOnViews() {
        Map<String, String> nullHostile = new Hashtable<String, String>();
        nullHostile.put("a", "apple");
        testNullsOnView(nullHostile.entrySet());
        nullHostile.put("a", "apple");
        testNullsOnView(nullHostile.keySet());
        nullHostile.put("a", "apple");
        testNullsOnView(nullHostile.values());
    }
    private void testNullsOnView(Collection<?> view) {
        try {
            assertFalse(view.contains(null));
        } catch (NullPointerException optional) {
        }
        try {
            assertFalse(view.remove(null));
        } catch (NullPointerException optional) {
        }
        Set<Object> setOfNull = Collections.singleton(null);
        assertFalse(view.equals(setOfNull));
        try {
            assertFalse(view.removeAll(setOfNull));
        } catch (NullPointerException optional) {
        }
        try {
            assertTrue(view.retainAll(setOfNull)); 
        } catch (NullPointerException optional) {
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
