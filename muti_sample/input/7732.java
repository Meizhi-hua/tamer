public class CancelledProducerConsumerLoops {
    static final int CAPACITY =      100;
    static final long TIMEOUT = 100;
    static final ExecutorService pool = Executors.newCachedThreadPool();
    static boolean print = false;
    public static void main(String[] args) throws Exception {
        int maxPairs = 8;
        int iters = 1000000;
        if (args.length > 0)
            maxPairs = Integer.parseInt(args[0]);
        print = true;
        for (int i = 1; i <= maxPairs; i += (i+1) >>> 1) {
            System.out.println("Pairs:" + i);
            try {
                oneTest(i, iters);
            }
            catch (BrokenBarrierException bb) {
            }
            Thread.sleep(100);
        }
        pool.shutdown();
        if (! pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
            throw new Error();
   }
    static void oneRun(BlockingQueue<Integer> q, int npairs, int iters) throws Exception {
        if (print)
            System.out.printf("%-18s", q.getClass().getSimpleName());
        LoopHelpers.BarrierTimer timer = new LoopHelpers.BarrierTimer();
        CyclicBarrier barrier = new CyclicBarrier(npairs * 2 + 1, timer);
        Future<?>[] prods = new Future<?>[npairs];
        Future<?>[] cons  = new Future<?>[npairs];
        for (int i = 0; i < npairs; ++i) {
            prods[i] = pool.submit(new Producer(q, barrier, iters));
            cons[i] = pool.submit(new Consumer(q, barrier, iters));
        }
        barrier.await();
        Thread.sleep(TIMEOUT);
        boolean tooLate = false;
        for (int i = 1; i < npairs; ++i) {
            if (!prods[i].cancel(true))
                tooLate = true;
            if (!cons[i].cancel(true))
                tooLate = true;
        }
        Object p0 = prods[0].get();
        Object c0 = cons[0].get();
        if (!tooLate) {
            for (int i = 1; i < npairs; ++i) {
                if (!prods[i].isDone() || !prods[i].isCancelled())
                    throw new Error("Only one producer thread should complete");
                if (!cons[i].isDone() || !cons[i].isCancelled())
                    throw new Error("Only one consumer thread should complete");
            }
        }
        else
            System.out.print("(cancelled too late) ");
        long endTime = System.nanoTime();
        long time = endTime - timer.startTime;
        if (print) {
            double secs = (double)(time) / 1000000000.0;
            System.out.println("\t " + secs + "s run time");
        }
    }
    static void oneTest(int pairs, int iters) throws Exception {
        oneRun(new ArrayBlockingQueue<Integer>(CAPACITY), pairs, iters);
        oneRun(new LinkedBlockingQueue<Integer>(CAPACITY), pairs, iters);
        oneRun(new LinkedBlockingDeque<Integer>(CAPACITY), pairs, iters);
        oneRun(new SynchronousQueue<Integer>(), pairs, iters / 8);
    }
    abstract static class Stage implements Callable<Integer> {
        final BlockingQueue<Integer> queue;
        final CyclicBarrier barrier;
        final int iters;
        Stage(BlockingQueue<Integer> q, CyclicBarrier b, int iters) {
            queue = q;
            barrier = b;
            this.iters = iters;
        }
    }
    static class Producer extends Stage {
        Producer(BlockingQueue<Integer> q, CyclicBarrier b, int iters) {
            super(q, b, iters);
        }
        public Integer call() throws Exception {
            barrier.await();
            int s = 0;
            int l = 4321;
            for (int i = 0; i < iters; ++i) {
                l = LoopHelpers.compute1(l);
                s += LoopHelpers.compute2(l);
                if (!queue.offer(new Integer(l), 1, TimeUnit.SECONDS))
                    break;
            }
            return new Integer(s);
        }
    }
    static class Consumer extends Stage {
        Consumer(BlockingQueue<Integer> q, CyclicBarrier b, int iters) {
            super(q, b, iters);
        }
        public Integer call() throws Exception {
            barrier.await();
            int l = 0;
            int s = 0;
            for (int i = 0; i < iters; ++i) {
                Integer x = queue.poll(1, TimeUnit.SECONDS);
                if (x == null)
                    break;
                l = LoopHelpers.compute1(x.intValue());
                s += l;
            }
            return new Integer(s);
        }
    }
}
