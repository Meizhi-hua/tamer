public class Interrupt {
    static void checkInterrupted0(Iterable<Fun> fs, Executor ex) {
        for (Fun f : fs) {
            try {
                ex.execute(new Runnable() {
                        final Thread thisThread = Thread.currentThread();
                        public void run() { thisThread.interrupt(); }});
                f.f();
                fail("Expected InterruptedException not thrown");
            } catch (InterruptedException e) {
                check(! Thread.interrupted());
            } catch (Throwable t) { unexpected(t); }
        }
    }
    static void checkInterrupted(Iterable<Fun> fs) {
        final Executor immediateExecutor = new Executor() {
                public void execute(Runnable r) {
                    r.run(); }};
        final ScheduledThreadPoolExecutor stpe
            = new ScheduledThreadPoolExecutor(1);
        final Executor delayedExecutor = new Executor() {
                public void execute(Runnable r) {
                    stpe.schedule(r, 20, MILLISECONDS); }};
        checkInterrupted0(fs, immediateExecutor);
        checkInterrupted0(fs, delayedExecutor);
        stpe.shutdown();
    }
    static void testQueue(final BlockingQueue<Object> q) {
        try {
            final BlockingDeque<Object> deq =
                (q instanceof BlockingDeque<?>) ?
                (BlockingDeque<Object>) q : null;
            q.clear();
            List<Fun> fs = new ArrayList<Fun>();
            fs.add(new Fun() { void f() throws Throwable
                    { q.take(); }});
            fs.add(new Fun() { void f() throws Throwable
                    { q.poll(60, SECONDS); }});
            if (deq != null) {
                fs.add(new Fun() { void f() throws Throwable
                        { deq.takeFirst(); }});
                fs.add(new Fun() { void f() throws Throwable
                        { deq.takeLast(); }});
                fs.add(new Fun() { void f() throws Throwable
                        { deq.pollFirst(7, SECONDS); }});
                fs.add(new Fun() { void f() throws Throwable
                        { deq.pollLast(7, SECONDS); }});
            }
            checkInterrupted(fs);
            while (q.remainingCapacity() > 0)
                try { q.put(1); }
                catch (Throwable t) { unexpected(t); }
            fs.clear();
            fs.add(new Fun() { void f() throws Throwable
                    { q.put(1); }});
            fs.add(new Fun() { void f() throws Throwable
                    { q.offer(1, 7, SECONDS); }});
            if (deq != null) {
                fs.add(new Fun() { void f() throws Throwable
                        { deq.putFirst(1); }});
                fs.add(new Fun() { void f() throws Throwable
                        { deq.putLast(1); }});
                fs.add(new Fun() { void f() throws Throwable
                        { deq.offerFirst(1, 7, SECONDS); }});
                fs.add(new Fun() { void f() throws Throwable
                        { deq.offerLast(1, 7, SECONDS); }});
            }
            checkInterrupted(fs);
        } catch (Throwable t) {
          System.out.printf("Failed: %s%n", q.getClass().getSimpleName());
          unexpected(t);
        }
    }
    private static void realMain(final String[] args) throws Throwable {
        testQueue(new SynchronousQueue<Object>());
        testQueue(new ArrayBlockingQueue<Object>(1,false));
        testQueue(new ArrayBlockingQueue<Object>(1,true));
        testQueue(new LinkedBlockingQueue<Object>(1));
        testQueue(new LinkedBlockingDeque<Object>(1));
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
    private abstract static class Fun {abstract void f() throws Throwable;}
}
