public class Cache {
    private static final long HASH_MASK = 0xFFFFFFFFFFFF0000L;
    private static final long PREFIX_HASH_MASK = 0xFFFFFFFF00000000L;
    private static final int  INDEX_MASK = 0x00FFFF;
    private final int cache_size;
    private final int prefix_size;
    private final long[] hashes;
    private final byte[][] encodings;
    private final Object[] cache;
    private final long[] hashes_idx;
    private int last_cached = 0;
    private boolean cache_is_full = false;
    public Cache(int pref_size, int size) {
        cache_size = size;
        prefix_size = pref_size;
        hashes = new long[cache_size];
        hashes_idx = new long[cache_size];
        encodings = new byte[cache_size][];
        cache = new Object[cache_size];
    }
    public Cache(int pref_size) {
        this(pref_size, 9);
    }
    public Cache() {
        this(28, 9);
    }
    public long getHash(byte[] arr) {
        long hash = 0;
        for (int i=1; i<prefix_size; i++) {
            hash += (arr[i] & 0xFF);
        } 
        hash = hash << 32;
        return hash;
    }
    public boolean contains(long prefix_hash) {
        int idx = -1*Arrays.binarySearch(hashes_idx, prefix_hash)-1;
        if (idx == cache_size) {
            return false;
        } else {
            return (hashes_idx[idx] & PREFIX_HASH_MASK) == prefix_hash;
        }
    }
    public Object get(long hash, byte[] encoding) {
        hash |= getSuffHash(encoding);
        int idx = -1*Arrays.binarySearch(hashes_idx, hash)-1;
        if (idx == cache_size) {
            return null;
        }
        while ((hashes_idx[idx] & HASH_MASK) == hash) {
            int i = (int) (hashes_idx[idx] & INDEX_MASK) - 1;
            if (Arrays.equals(encoding, encodings[i])) {
                return cache[i];
            }
            idx++;
            if (idx == cache_size) {
                return null;
            }
        }
        return null;
    }
    public void put(long hash, byte[] encoding, Object object) {
        if (last_cached == cache_size) {
            last_cached = 0;
            cache_is_full = true;
        }
        int index = last_cached++;
        hash |= getSuffHash(encoding);
        if (cache_is_full) {
            long idx_hash = (hashes[index] | (index+1));
            int idx = Arrays.binarySearch(hashes_idx, idx_hash);
            if (idx < 0) {
                System.out.println("WARNING! "+idx); 
                idx = -(idx + 1);
            }
            long new_hash_idx = (hash | (index + 1));
            int new_idx = Arrays.binarySearch(hashes_idx, new_hash_idx);
            if (new_idx >= 0) {
                if (idx != new_idx) {
                    System.out.println("WARNING: "); 
                    System.out.println(">> idx: "+idx+" new_idx: "+new_idx); 
                }
            } else {
                new_idx = -(new_idx + 1);
                if (new_idx > idx) {
                    System.arraycopy(hashes_idx, idx+1, hashes_idx, idx,
                            new_idx - idx - 1);
                    hashes_idx[new_idx-1] = new_hash_idx;
                } else if (idx > new_idx) {
                    System.arraycopy(hashes_idx, new_idx, hashes_idx, new_idx+1,
                            idx - new_idx);
                    hashes_idx[new_idx] = new_hash_idx;
                } else { 
                    hashes_idx[new_idx] = new_hash_idx;
                }
            }
        } else {
            long idx_hash = (hash | (index + 1));
            int idx = Arrays.binarySearch(hashes_idx, idx_hash);
            if (idx < 0) {
                idx = -(idx + 1);
            }
            idx = idx - 1;
            if (idx != cache_size - index - 1) {
                System.arraycopy(hashes_idx, cache_size - index,
                        hashes_idx, cache_size - index - 1,
                        idx - (cache_size - index) + 1);
            }
            hashes_idx[idx] = idx_hash;
        }
        hashes[index] = hash;
        encodings[index] = encoding;
        cache[index] = object;
    }
    private long getSuffHash(byte[] arr) {
        long hash_addon = 0;
        for (int i=arr.length-1; i>arr.length - prefix_size; i--) {
            hash_addon += (arr[i] & 0xFF);
        }
        return hash_addon << 16;
    }
}
