public class AtomicLongArrayTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicLongArrayTest.class);
    }
    public void testConstructor(){
        AtomicLongArray ai = new AtomicLongArray(SIZE);
        for (int i = 0; i < SIZE; ++i) 
            assertEquals(0,ai.get(i));
    }
    public void testConstructor2NPE() {
        try {
            long[] a = null;
            AtomicLongArray ai = new AtomicLongArray(a);
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testConstructor2() {
        long[] a = { 17L, 3L, -42L, 99L, -7L};
        AtomicLongArray ai = new AtomicLongArray(a);
        assertEquals(a.length, ai.length());
        for (int i = 0; i < a.length; ++i) 
            assertEquals(a[i], ai.get(i));
    }
    public void testIndexing(){
        AtomicLongArray ai = new AtomicLongArray(SIZE);
        try {
            ai.get(SIZE);
        } catch(IndexOutOfBoundsException success){
        }
        try {
            ai.get(-1);
        } catch(IndexOutOfBoundsException success){
        }
        try {
            ai.set(SIZE, 0);
        } catch(IndexOutOfBoundsException success){
        }
        try {
            ai.set(-1, 0);
        } catch(IndexOutOfBoundsException success){
        }
    }
    public void testGetSet(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(1,ai.get(i));
            ai.set(i, 2);
            assertEquals(2,ai.get(i));
            ai.set(i, -3);
            assertEquals(-3,ai.get(i));
        }
    }
    public void testCompareAndSet(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertTrue(ai.compareAndSet(i, 1,2));
            assertTrue(ai.compareAndSet(i, 2,-4));
            assertEquals(-4,ai.get(i));
            assertFalse(ai.compareAndSet(i, -5,7));
            assertFalse((7 == ai.get(i)));
            assertTrue(ai.compareAndSet(i, -4,7));
            assertEquals(7,ai.get(i));
        }
    }
    public void testCompareAndSetInMultipleThreads() {
        final AtomicLongArray a = new AtomicLongArray(1);
        a.set(0, 1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(0, 2, 3)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(a.compareAndSet(0, 1, 2));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(a.get(0), 3);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testWeakCompareAndSet(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            while(!ai.weakCompareAndSet(i, 1,2));
            while(!ai.weakCompareAndSet(i, 2,-4));
            assertEquals(-4,ai.get(i));
            while(!ai.weakCompareAndSet(i, -4,7));
            assertEquals(7,ai.get(i));
        }
    }
    public void testGetAndSet(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(1,ai.getAndSet(i,0));
            assertEquals(0,ai.getAndSet(i,-10));
            assertEquals(-10,ai.getAndSet(i,1));
        }
    }
    public void testGetAndAdd(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(1,ai.getAndAdd(i,2));
            assertEquals(3,ai.get(i));
            assertEquals(3,ai.getAndAdd(i,-4));
            assertEquals(-1,ai.get(i));
        }
    }
    public void testGetAndDecrement(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(1,ai.getAndDecrement(i));
            assertEquals(0,ai.getAndDecrement(i));
            assertEquals(-1,ai.getAndDecrement(i));
        }
    }
    public void testGetAndIncrement(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(1,ai.getAndIncrement(i));
            assertEquals(2,ai.get(i));
            ai.set(i,-2);
            assertEquals(-2,ai.getAndIncrement(i));
            assertEquals(-1,ai.getAndIncrement(i));
            assertEquals(0,ai.getAndIncrement(i));
            assertEquals(1,ai.get(i));
        }
    }
    public void testAddAndGet() {
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(3,ai.addAndGet(i,2));
            assertEquals(3,ai.get(i));
            assertEquals(-1,ai.addAndGet(i,-4));
            assertEquals(-1,ai.get(i));
        }
    }
    public void testDecrementAndGet(){
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(0,ai.decrementAndGet(i));
            assertEquals(-1,ai.decrementAndGet(i));
            assertEquals(-2,ai.decrementAndGet(i));
            assertEquals(-2,ai.get(i));
        }
    }
    public void testIncrementAndGet() {
        AtomicLongArray ai = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, 1);
            assertEquals(2,ai.incrementAndGet(i));
            assertEquals(2,ai.get(i));
            ai.set(i, -2);
            assertEquals(-1,ai.incrementAndGet(i));
            assertEquals(0,ai.incrementAndGet(i));
            assertEquals(1,ai.incrementAndGet(i));
            assertEquals(1,ai.get(i));
        }
    }
    static final long COUNTDOWN = 100000;
    class Counter implements Runnable {
        final AtomicLongArray ai;
        volatile long counts;
        Counter(AtomicLongArray a) { ai = a; }
        public void run() {
            for (;;) {
                boolean done = true;
                for (int i = 0; i < ai.length(); ++i) {
                    long v = ai.get(i);
                    threadAssertTrue(v >= 0);
                    if (v != 0) {
                        done = false;
                        if (ai.compareAndSet(i, v, v-1))
                            ++counts;
                    }
                }
                if (done)
                    break;
            }
        }
    }
    public void testCountingInMultipleThreads() {
        try {
            final AtomicLongArray ai = new AtomicLongArray(SIZE); 
            for (int i = 0; i < SIZE; ++i) 
                ai.set(i, COUNTDOWN);
            Counter c1 = new Counter(ai);
            Counter c2 = new Counter(ai);
            Thread t1 = new Thread(c1);
            Thread t2 = new Thread(c2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            assertEquals(c1.counts+c2.counts, SIZE * COUNTDOWN);
        }
        catch(InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testSerialization() {
        AtomicLongArray l = new AtomicLongArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) 
            l.set(i, -i);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            AtomicLongArray r = (AtomicLongArray) in.readObject();
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(l.get(i), r.get(i));
            }
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testToString() {
        long[] a = { 17, 3, -42, 99, -7};
        AtomicLongArray ai = new AtomicLongArray(a);
        assertEquals(Arrays.toString(a), ai.toString());
    }
}
