public class ScheduledTickleService {
    private static final int concurrency = 2;
    public static final CountDownLatch done = new CountDownLatch(concurrency);
    public static void realMain(String... args) throws InterruptedException {
        ScheduledExecutorService tickleService =
            new ScheduledThreadPoolExecutor(concurrency) {
                protected <V> RunnableScheduledFuture<V>
                    decorateTask(Runnable runnable,
                                 RunnableScheduledFuture<V> task) {
                    final ScheduledThreadPoolExecutor exec = this;
                    return new CustomRunnableScheduledFuture<V>(task) {
                        public boolean cancel(boolean b) {
                            return (task().cancel(b)
                                    && exec.remove(this));}};}};
        for (int i = 0; i < concurrency; i++)
            new ScheduledTickle(i, tickleService)
                .setUpdateInterval(25, MILLISECONDS);
        done.await();
        tickleService.shutdown();
        pass();
    }
    static class ScheduledTickle implements Runnable {
        public volatile int failures = 0;
        private final ScheduledExecutorService service;
        private ScheduledFuture ticket = null;
        private int numTickled = 0;
        private final String name;
        public ScheduledTickle(int i, ScheduledExecutorService service) {
            super();
            this.name = "Tickler-"+i;
            this.service = service;
        }
        public synchronized void setUpdateInterval(long interval,
                                                   TimeUnit unit) {
            if (ticket != null) {
                ticket.cancel(false);
                ticket = null;
            }
            if (interval > 0 && ! service.isShutdown()) {
                ticket = service.scheduleAtFixedRate(this, interval,
                                                     interval, unit);
            }
        }
        public synchronized void run() {
            try {
                check(numTickled < 6);
                numTickled++;
                System.out.println(name + ": Run " + numTickled);
                if (numTickled == 3) {
                    System.out.println(name + ": slower please!");
                    this.setUpdateInterval(100, MILLISECONDS);
                }
                else if (numTickled == 5) {
                    System.out.println(name + ": OK that's enough.");
                    this.setUpdateInterval(0, MILLISECONDS);
                    ScheduledTickleService.done.countDown();
                }
            } catch (Throwable t) { unexpected(t); }
        }
    }
    static class CustomRunnableScheduledFuture<V>
        implements RunnableScheduledFuture<V> {
        private RunnableScheduledFuture<V> task;
        public CustomRunnableScheduledFuture(RunnableScheduledFuture<V> task) {
            super();
            this.task = task;
        }
        public RunnableScheduledFuture<V> task() { return task; }
        public boolean isPeriodic()         { return task.isPeriodic(); }
        public boolean isCancelled()        { return task.isCancelled(); }
        public boolean isDone()             { return task.isDone(); }
        public boolean cancel(boolean b)    { return task.cancel(b); }
        public long getDelay(TimeUnit unit) { return task.getDelay(unit); }
        public void run()                   {        task.run(); }
        public V get()
            throws InterruptedException, ExecutionException {
            return task.get();
        }
        public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return task.get(timeout, unit);
        }
        public int compareTo(Delayed other) {
            if (this == other)
                return 0;
            else if (other instanceof CustomRunnableScheduledFuture)
                return task.compareTo(((CustomRunnableScheduledFuture)other).task());
            else
                return task.compareTo(other);
        }
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
}
