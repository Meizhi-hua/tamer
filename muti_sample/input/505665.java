public class BitIntSet implements IntSet {
    int[] bits;
    public BitIntSet(int max) {
        bits = Bits.makeBitSet(max);
    }
    public void add(int value) {
        ensureCapacity(value);
        Bits.set(bits, value, true);
    }
    private void ensureCapacity(int value) {
        if (value >= Bits.getMax(bits)) {
            int[] newBits = Bits.makeBitSet(
                    Math.max(value + 1, 2 * Bits.getMax(bits)));
            System.arraycopy(bits, 0, newBits, 0, bits.length);
            bits = newBits;
        }
    }
    public void remove(int value) {
        if (value < Bits.getMax(bits)) {
            Bits.set(bits, value, false);
        }
    }
    public boolean has(int value) {
        return (value < Bits.getMax(bits)) && Bits.get(bits, value);    
    }
    public void merge(IntSet other) {
        if (other instanceof BitIntSet) {
            BitIntSet o = (BitIntSet) other;
            ensureCapacity(Bits.getMax(o.bits) + 1);
            Bits.or(bits, o.bits);
        } else if (other instanceof ListIntSet) {
            ListIntSet o = (ListIntSet) other;
            int sz = o.ints.size();
            if (sz > 0) {
                ensureCapacity(o.ints.get(sz - 1));
            }
            for (int i = 0; i < o.ints.size(); i++) {
                Bits.set(bits, o.ints.get(i), true);
            }
        } else {
            IntIterator iter = other.iterator();
            while (iter.hasNext()) {
                add(iter.next());
            }
        }
    }
    public int elements() {
        return Bits.bitCount(bits);        
    }
    public IntIterator iterator() {
        return new IntIterator() {
            private int idx = Bits.findFirst(bits, 0);
            public boolean hasNext() {
                return idx >= 0;
            }
            public int next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int ret = idx;
                idx = Bits.findFirst(bits, idx+1);
                return ret;
            }
        };
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (int i = Bits.findFirst(bits, 0)
                ; i >= 0
                ; i = Bits.findFirst(bits, i + 1)) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(i);
        }
        sb.append('}');
        return sb.toString();
    }
}
