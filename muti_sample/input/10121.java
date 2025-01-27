public final class RenderCache {
    final class Entry {
        private SurfaceType src;
        private CompositeType comp;
        private SurfaceType dst;
        private Object value;
        public Entry(SurfaceType src,
                     CompositeType comp,
                     SurfaceType dst,
                     Object value)
        {
            this.src = src;
            this.comp = comp;
            this.dst = dst;
            this.value = value;
        }
        public boolean matches(SurfaceType src,
                               CompositeType comp,
                               SurfaceType dst)
        {
            return ((this.src == src) &&
                    (this.comp == comp) &&
                    (this.dst == dst));
        }
        public Object getValue() {
            return value;
        }
    }
    private Entry entries[];
    public RenderCache(int size) {
        entries = new Entry[size];
    }
    public synchronized Object get(SurfaceType src,
                      CompositeType comp,
                      SurfaceType dst)
    {
        int max = entries.length - 1;
        for (int i = max; i >= 0; i--) {
            Entry e = entries[i];
            if (e == null) {
                break;
            }
            if (e.matches(src, comp, dst)) {
                if (i < max - 4) {
                    System.arraycopy(entries, i+1, entries, i, max - i);
                    entries[max] = e;
                }
                return e.getValue();
            }
        }
        return null;
    }
    public synchronized void put(SurfaceType src,
                    CompositeType comp,
                    SurfaceType dst,
                    Object value)
    {
        Entry e = new Entry(src, comp, dst, value);
        int num = entries.length;
        System.arraycopy(entries, 1, entries, 0, num - 1);
        entries[num - 1] = e;
    }
}
