public final class SimpleReentrantLockLoops {
    static final ExecutorService pool = Executors.newCachedThreadPool();
    static final LoopHelpers.SimpleRandom rng = new LoopHelpers.SimpleRandom();
    static boolean print = false;
    static int iters = 1000000;
    public static void main(String[] args) throws Exception {
        int maxThreads = 5;
        if (args.length > 0)
            maxThreads = Integer.parseInt(args[0]);
        print = true;
        int reps = 2;
        for (int i = 1; i <= maxThreads; i += (i+1) >>> 1) {
            int n = reps;
            if (reps > 1) --reps;
            while (n-- > 0) {
                System.out.print("Threads: " + i);
                new ReentrantLockLoop(i).test();
                Thread.sleep(100);
            }
        }
        pool.shutdown();
        if (! pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
            throw new Error();
    }
    static final class ReentrantLockLoop implements Runnable {
        private int v = rng.next();
        private volatile int result = 17;
        private final ReentrantLock lock = new ReentrantLock();
        private final LoopHelpers.BarrierTimer timer = new LoopHelpers.BarrierTimer();
        private final CyclicBarrier barrier;
        private final int nthreads;
        ReentrantLockLoop(int nthreads) {
            this.nthreads = nthreads;
            barrier = new CyclicBarrier(nthreads+1, timer);
        }
        final void test() throws Exception {
            for (int i = 0; i < nthreads; ++i)
                pool.execute(this);
            barrier.await();
            barrier.await();
            if (print) {
                long time = timer.getTime();
                long tpi = time / ((long)iters * nthreads);
                System.out.print("\t" + LoopHelpers.rightJustify(tpi) + " ns per lock");
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
                int n = iters;
                do {
                    lock.lock();
                    try {
                        if ((n & 255) == 0)
                            v = x = LoopHelpers.compute2(LoopHelpers.compute1(v));
                        else
                            v = x += ~(v - n);
                    }
                    finally {
                        lock.unlock();
                    }
                    if ((~n & 255) == 0) {
                        sum += LoopHelpers.compute1(LoopHelpers.compute2(x));
                    }
                    else
                        sum += sum ^ x;
                } while (n-- > 0);
                barrier.await();
                result += sum;
            }
            catch (Exception ie) {
                return;
            }
        }
    }
}
