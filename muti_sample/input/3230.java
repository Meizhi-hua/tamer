public final class LockOncePerThreadLoops {
    static final ExecutorService pool = Executors.newCachedThreadPool();
    static final LoopHelpers.SimpleRandom rng = new LoopHelpers.SimpleRandom();
    static boolean print = false;
    static int nlocks = 50000;
    static int nthreads = 100;
    static int replications = 5;
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            replications = Integer.parseInt(args[0]);
        if (args.length > 1)
            nlocks = Integer.parseInt(args[1]);
        print = true;
        for (int i = 0; i < replications; ++i) {
            System.out.print("Iteration: " + i);
            new ReentrantLockLoop().test();
            Thread.sleep(100);
        }
        pool.shutdown();
        if (! pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
            throw new Error();
    }
    static final class ReentrantLockLoop implements Runnable {
        private int v = rng.next();
        private volatile int result = 17;
        final ReentrantLock[]locks = new ReentrantLock[nlocks];
        private final ReentrantLock lock = new ReentrantLock();
        private final LoopHelpers.BarrierTimer timer = new LoopHelpers.BarrierTimer();
        private final CyclicBarrier barrier;
        ReentrantLockLoop() {
            barrier = new CyclicBarrier(nthreads+1, timer);
            for (int i = 0; i < nlocks; ++i)
                locks[i] = new ReentrantLock();
        }
        final void test() throws Exception {
            for (int i = 0; i < nthreads; ++i)
                pool.execute(this);
            barrier.await();
            barrier.await();
            if (print) {
                long time = timer.getTime();
                double secs = (double)(time) / 1000000000.0;
                System.out.println("\t " + secs + "s run time");
            }
            int r = result;
            if (r == 0) 
                System.out.println("useless result: " + r);
        }
        public final void run() {
            try {
                barrier.await();
                int sum = v;
                int x = 0;
                for (int i = 0; i < locks.length; ++i) {
                    locks[i].lock();
                    try {
                            v = x += ~(v - i);
                    }
                    finally {
                        locks[i].unlock();
                    }
                    if ((~i & 255) == 0) {
                        sum += LoopHelpers.compute1(LoopHelpers.compute2(x));
                    }
                    else
                        sum += sum ^ x;
                }
                barrier.await();
                result += sum;
            }
            catch (Exception ie) {
                return;
            }
        }
    }
}
