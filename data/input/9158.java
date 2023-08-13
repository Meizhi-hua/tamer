public class MonitorDeadlock {
    private final int EXPECTED_THREADS = 3;
    private Barrier go = new Barrier(1);
    private Barrier barr = new Barrier(EXPECTED_THREADS);
    private Object a = new Object();
    private Object b = new Object();
    private Object c = new Object();
    private Thread[] dThreads = new Thread[EXPECTED_THREADS];
    public MonitorDeadlock() {
        dThreads[0] = new DeadlockingThread("MThread-1", a, b);
        dThreads[1] = new DeadlockingThread("MThread-2", b, c);
        dThreads[2] = new DeadlockingThread("MThread-3", c, a);
        for (int i = 0; i < EXPECTED_THREADS; i++) {
            dThreads[i].setDaemon(true);
            dThreads[i].start();
        }
    }
    void goDeadlock() {
        barr.await();
        barr.set(EXPECTED_THREADS);
        while (go.getWaiterCount() != EXPECTED_THREADS) {
            synchronized(this) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                }
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        go.signal();
    }
    void waitUntilDeadlock() {
        barr.await();
        for (int i=0; i < 100; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            boolean retry = false;
            for (Thread t: dThreads) {
                if (t.getState() == Thread.State.RUNNABLE) {
                    retry = true;
                    break;
                }
            }
            if (!retry) {
                break;
            }
        }
    }
    private class DeadlockingThread extends Thread {
        private final Object lock1;
        private final Object lock2;
        DeadlockingThread(String name, Object lock1, Object lock2) {
            super(name);
            this.lock1 = lock1;
            this.lock2 = lock2;
        }
        public void run() {
            f();
        }
        private void f() {
            synchronized (lock1) {
                barr.signal();
                go.await();
                g();
            }
        }
        private void g() {
            barr.signal();
            synchronized (lock2) {
                throw new RuntimeException("should not reach here.");
            }
        }
    }
    void checkResult(long[] threads) {
        if (threads.length != EXPECTED_THREADS) {
            throw new RuntimeException("Expected to have " +
                EXPECTED_THREADS + " to be in the deadlock list");
        }
        boolean[] found = new boolean[EXPECTED_THREADS];
        for (int i = 0; i < threads.length; i++) {
            for (int j = 0; j < dThreads.length; j++) {
                if (dThreads[j].getId() == threads[i]) {
                    found[j] = true;
                }
            }
        }
        boolean ok = true;
        for (int j = 0; j < found.length; j++) {
            ok = ok && found[j];
        }
        if (!ok) {
            System.out.print("Returned result is [");
            for (int j = 0; j < threads.length; j++) {
                System.out.print(threads[j] + " ");
            }
            System.out.println("]");
            System.out.print("Expected result is [");
            for (int j = 0; j < threads.length; j++) {
                System.out.print(dThreads[j] + " ");
            }
            System.out.println("]");
            throw new RuntimeException("Unexpected result returned " +
                " by findMonitorDeadlockedThreads method.");
        }
    }
}
