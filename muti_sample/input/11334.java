public class Custom {
    static volatile int passed = 0, failed = 0;
    static void pass() { passed++; }
    static void fail() { failed++; Thread.dumpStack(); }
    static void unexpected(Throwable t) { failed++; t.printStackTrace(); }
    static void check(boolean cond) { if (cond) pass(); else fail(); }
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else {System.out.println(x + " not equal to " + y); fail(); }}
    private static class CustomTask<V> extends FutureTask<V> {
        public static final AtomicInteger births = new AtomicInteger(0);
        CustomTask(Callable<V> c) { super(c); births.getAndIncrement(); }
        CustomTask(Runnable r, V v) { super(r, v); births.getAndIncrement(); }
    }
    private static class CustomTPE extends ThreadPoolExecutor {
        CustomTPE() {
            super(threadCount, threadCount,
                  30, TimeUnit.MILLISECONDS,
                  new ArrayBlockingQueue<Runnable>(2*threadCount));
        }
        protected <V> RunnableFuture<V> newTaskFor(Callable<V> c) {
            return new CustomTask<V>(c);
        }
        protected <V> RunnableFuture<V> newTaskFor(Runnable r, V v) {
            return new CustomTask<V>(r, v);
        }
    }
    private static class CustomSTPE extends ScheduledThreadPoolExecutor {
        public static final AtomicInteger decorations = new AtomicInteger(0);
        CustomSTPE() {
            super(threadCount);
        }
        protected <V> RunnableScheduledFuture<V> decorateTask(
            Runnable r, RunnableScheduledFuture<V> task) {
            decorations.getAndIncrement();
            return task;
        }
        protected <V> RunnableScheduledFuture<V> decorateTask(
            Callable<V> c, RunnableScheduledFuture<V> task) {
            decorations.getAndIncrement();
            return task;
        }
    }
    static int countExecutorThreads() {
        Thread[] threads = new Thread[Thread.activeCount()+100];
        Thread.enumerate(threads);
        int count = 0;
        for (Thread t : threads)
            if (t != null && t.getName().matches("pool-[0-9]+-thread-[0-9]+"))
                count++;
        return count;
    }
    private static final int threadCount = 10;
    public static void main(String[] args) throws Throwable {
        CustomTPE tpe = new CustomTPE();
        equal(tpe.getCorePoolSize(), threadCount);
        equal(countExecutorThreads(), 0);
        for (int i = 0; i < threadCount; i++)
            tpe.submit(new Runnable() { public void run() {}});
        equal(countExecutorThreads(), threadCount);
        equal(CustomTask.births.get(), threadCount);
        tpe.shutdown();
        tpe.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        Thread.sleep(10);
        equal(countExecutorThreads(), 0);
        CustomSTPE stpe = new CustomSTPE();
        for (int i = 0; i < threadCount; i++)
            stpe.submit(new Runnable() { public void run() {}});
        equal(CustomSTPE.decorations.get(), threadCount);
        equal(countExecutorThreads(), threadCount);
        stpe.shutdown();
        stpe.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        Thread.sleep(10);
        equal(countExecutorThreads(), 0);
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Exception("Some tests failed");
    }
}
