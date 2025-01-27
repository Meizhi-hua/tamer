public class HashtableTest extends PerformanceTestBase {
    public static final int ITERATIONS = 1000;
    public Hashtable<String, Integer> sTable;
    public String[] sKeys;
    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        sTable = new Hashtable();
        sKeys = new String[ITERATIONS];
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            sKeys[i] = Integer.toString(i, 16);
            sTable.put(sKeys[i], i);
        }
    }
    @Override
    public int startPerformance(PerformanceTestCase.Intermediates intermediates) {
        intermediates.setInternalIterations(ITERATIONS);
        return 0;
    }
    @SuppressWarnings("unchecked")
    public void testHashtablePut() {
        Hashtable hTable = new Hashtable();
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
            hTable.put(i, i);
        }
    }
    public void testHashtableGet() {
        int value;
        String[] keys = sKeys;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
            value = hTable.get(keys[i]);
        }
    }
    public void testHashtablekeyset() {
        Set keyset;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
            keyset = hTable.keySet();
        }
    }
    public void testHashtableEntrySet() {
        Set keyset;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
            keyset = hTable.entrySet();
        }
    }
    public void testHashtableSize() {
        int len;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
            len = hTable.size();
        }
    }
    public void testHashtableContainsValue() {
        boolean flag;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
            flag = hTable.containsValue(i);
        }
    }
    @SuppressWarnings("unchecked")
    public void testHashtableRemove() {
        Hashtable<String, Integer> hTable = new Hashtable(sTable);
        String[] keys = sKeys;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
            hTable.remove(keys[i]);
        }
    }
    public void testHashtableContains() {
        Hashtable<String, Integer> hTable = sTable;
        boolean flag;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
            flag = hTable.contains(i);
        }
    }
    public void testHashtableContainsKey() {
        Hashtable<String, Integer> hTable = sTable;
        boolean flag;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
            flag = hTable.containsKey(i);
        }
    }
    public void testHashtableIsEmpty() {
        Hashtable<String, Integer> hTable = sTable;
        boolean flag;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
            flag = hTable.isEmpty();
        }
    }
    public void testHashtableKeys() {
        Hashtable<String, Integer> hTable = sTable;
        Enumeration<String> keys;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
            keys = hTable.keys();
        }
    }
    public void testHashtableElements() {
        Hashtable<String, Integer> hTable = sTable;
        Enumeration<Integer> elements;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
            elements = hTable.elements();
        }
    }
    public void testHashtableHashCode() {
        int index;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
            index = hTable.hashCode();
        }
    }
    public void testHashtableEquals() {
        boolean flag;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
            flag = hTable.equals(hTable);
        }
    }
    public void testHashtableToString() {
        String str;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
            str = hTable.toString();
        }
    }
    @SuppressWarnings("unchecked")
    public void testHashtablePutAll() {
        Hashtable<String, Integer> hTable = new Hashtable();
        Hashtable<String, Integer> hTable1 = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
            hTable.putAll(hTable1);
        }
    }
    public void testHashtableClone() {
        Hashtable hashTable;
        Hashtable<String, Integer> hTable = sTable;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
            hashTable = (Hashtable) hTable.clone();
        }
    }
}
