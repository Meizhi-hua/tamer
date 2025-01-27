public class CacheTable {
    class Entry {
        java.lang.Object key;
        int val;
        Entry next;  
        Entry rnext; 
        public Entry(java.lang.Object k, int v) {
            key = k;
            val = v;
            next = null;
            rnext = null;
        }
    }
    private boolean noReverseMap;
    static final int INITIAL_SIZE = 16;
    static final int MAX_SIZE = 1 << 30;
    int size;
    int entryCount;
    private Entry [] map;
    private Entry [] rmap;
    private ORB orb;
    private ORBUtilSystemException wrapper;
    private CacheTable() {}
    public  CacheTable(ORB orb, boolean u) {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get(orb,
            CORBALogDomains.RPC_ENCODING);
        noReverseMap = u;
        size = INITIAL_SIZE;
        entryCount = 0;
        initTables();
    }
    private void initTables() {
        map = new Entry[size];
        rmap = noReverseMap ? null : new Entry[size];
    }
    private void grow() {
        if (size == MAX_SIZE)
                return;
        Entry [] oldMap = map;
        int oldSize = size;
        size <<= 1;
        initTables();
        for (int i = 0; i < oldSize; i++) {
            for (Entry e = oldMap[i]; e != null; e = e.next)
                put_table(e.key, e.val);
        }
    }
    private int moduloTableSize(int h) {
        h += ~(h << 9);
        h ^=  (h >>> 14);
        h +=  (h << 4);
        h ^=  (h >>> 10);
        return h & (size - 1);
    }
    private int hash(java.lang.Object key) {
        return moduloTableSize(System.identityHashCode(key));
    }
    private int hash(int val) {
        return moduloTableSize(val);
    }
    public final void put(java.lang.Object key, int val) {
        if (put_table(key, val)) {
            entryCount++;
            if (entryCount > size * 3 / 4)
                grow();
        }
    }
    private boolean put_table(java.lang.Object key, int val) {
        int index = hash(key);
        for (Entry e = map[index]; e != null; e = e.next) {
            if (e.key == key) {
                if (e.val != val) {
                    throw wrapper.duplicateIndirectionOffset();
                }
                return false;
            }
        }
        Entry newEntry = new Entry(key, val);
        newEntry.next = map[index];
        map[index] = newEntry;
        if (!noReverseMap) {
            int rindex = hash(val);
            newEntry.rnext = rmap[rindex];
            rmap[rindex] = newEntry;
        }
        return true;
    }
    public final boolean containsKey(java.lang.Object key) {
        return (getVal(key) != -1);
    }
    public final int getVal(java.lang.Object key) {
        int index = hash(key);
        for (Entry e = map[index]; e != null; e = e.next) {
            if (e.key == key)
                return e.val;
        }
        return -1;
    }
    public final boolean containsVal(int val) {
        return (getKey(val) != null);
    }
    public final boolean containsOrderedVal(int val) {
        return containsVal(val);
    }
    public final java.lang.Object getKey(int val) {
        int index = hash(val);
        for (Entry e = rmap[index]; e != null; e = e.rnext) {
            if (e.val == val)
                return e.key;
        }
        return null;
    }
    public void done() {
        map = null;
        rmap = null;
    }
}
