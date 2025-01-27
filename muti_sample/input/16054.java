public class AtomicLongArray implements java.io.Serializable {
    private static final long serialVersionUID = -2308431214976778248L;
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int base = unsafe.arrayBaseOffset(long[].class);
    private static final int shift;
    private final long[] array;
    static {
        int scale = unsafe.arrayIndexScale(long[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }
    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);
        return byteOffset(i);
    }
    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }
    public AtomicLongArray(int length) {
        array = new long[length];
    }
    public AtomicLongArray(long[] array) {
        this.array = array.clone();
    }
    public final int length() {
        return array.length;
    }
    public final long get(int i) {
        return getRaw(checkedByteOffset(i));
    }
    private long getRaw(long offset) {
        return unsafe.getLongVolatile(array, offset);
    }
    public final void set(int i, long newValue) {
        unsafe.putLongVolatile(array, checkedByteOffset(i), newValue);
    }
    public final void lazySet(int i, long newValue) {
        unsafe.putOrderedLong(array, checkedByteOffset(i), newValue);
    }
    public final long getAndSet(int i, long newValue) {
        long offset = checkedByteOffset(i);
        while (true) {
            long current = getRaw(offset);
            if (compareAndSetRaw(offset, current, newValue))
                return current;
        }
    }
    public final boolean compareAndSet(int i, long expect, long update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }
    private boolean compareAndSetRaw(long offset, long expect, long update) {
        return unsafe.compareAndSwapLong(array, offset, expect, update);
    }
    public final boolean weakCompareAndSet(int i, long expect, long update) {
        return compareAndSet(i, expect, update);
    }
    public final long getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }
    public final long getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }
    public final long getAndAdd(int i, long delta) {
        long offset = checkedByteOffset(i);
        while (true) {
            long current = getRaw(offset);
            if (compareAndSetRaw(offset, current, current + delta))
                return current;
        }
    }
    public final long incrementAndGet(int i) {
        return addAndGet(i, 1);
    }
    public final long decrementAndGet(int i) {
        return addAndGet(i, -1);
    }
    public long addAndGet(int i, long delta) {
        long offset = checkedByteOffset(i);
        while (true) {
            long current = getRaw(offset);
            long next = current + delta;
            if (compareAndSetRaw(offset, current, next))
                return next;
        }
    }
    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }
}
