class LoopHelpers {
    public static int compute1(int x) {
        int lo = 16807 * (x & 0xFFFF);
        int hi = 16807 * (x >>> 16);
        lo += (hi & 0x7FFF) << 16;
        if ((lo & 0x80000000) != 0) {
            lo &= 0x7fffffff;
            ++lo;
        }
        lo += hi >>> 15;
        if (lo == 0 || (lo & 0x80000000) != 0) {
            lo &= 0x7fffffff;
            ++lo;
        }
        return lo;
    }
    public static int compute2(int x) {
        int loops = (x >>> 4) & 7;
        while (loops-- > 0) {
            x = (x * 2147483647) % 16807;
        }
        return x;
    }
    public static class SimpleRandom {
        private static final long multiplier = 0x5DEECE66DL;
        private static final long addend = 0xBL;
        private static final long mask = (1L << 48) - 1;
        static final AtomicLong seq = new AtomicLong(1);
        private long seed = System.nanoTime() + seq.getAndIncrement();
        public void setSeed(long s) {
            seed = s;
        }
        public int next() {
            long nextseed = (seed * multiplier + addend) & mask;
            seed = nextseed;
            return ((int)(nextseed >>> 17)) & 0x7FFFFFFF;
        }
    }
    public static class BarrierTimer implements Runnable {
        public volatile long startTime;
        public volatile long endTime;
        public void run() {
            long t = System.nanoTime();
            if (startTime == 0)
                startTime = t;
            else
                endTime = t;
        }
        public void clear() {
            startTime = 0;
            endTime = 0;
        }
        public long getTime() {
            return endTime - startTime;
        }
    }
    public static String rightJustify(long n) {
        String field = "         ";
        String num = Long.toString(n);
        if (num.length() >= field.length())
            return num;
        StringBuffer b = new StringBuffer(field);
        b.replace(b.length()-num.length(), b.length(), num);
        return b.toString();
    }
}