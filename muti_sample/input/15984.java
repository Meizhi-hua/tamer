public class ThreadCpuTimeArray {
    private static com.sun.management.ThreadMXBean mbean =
        (com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
    private static boolean testFailed = false;
    private static boolean done = false;
    private static Object obj = new Object();
    private static final int NUM_THREADS = 10;
    private static Thread[] threads = new Thread[NUM_THREADS];
    private static final int DELTA = 100;
    public static void main(String[] argv)
        throws Exception {
        if (!mbean.isThreadCpuTimeSupported()) {
            return;
        }
        if (mbean.isThreadCpuTimeEnabled()) {
            mbean.setThreadCpuTimeEnabled(false);
        }
        if (mbean.isThreadCpuTimeEnabled()) {
            throw new RuntimeException("ThreadCpuTime is expected to be disabled");
        }
        long[] ids = new long[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new MyThread("MyThread-" + i);
            threads[i].start();
            ids[i] = threads[i].getId();
        }
        waitUntilThreadBlocked();
        long times[] = mbean.getThreadCpuTime(ids);
        long userTimes[] = mbean.getThreadUserTime(ids);
        if (times == null) {
            throw new RuntimeException("Null ThreadCpuTime array returned");
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            long t = times[i];
            if (t != -1) {
                throw new RuntimeException(
                    "Invalid ThreadCpuTime returned for thread " +
                    threads[i].getName() + " = " +  t + " expected = -1");
            }
            long ut = userTimes[i];
            if (ut != -1) {
                throw new RuntimeException(
                    "Invalid ThreadUserTime returned for thread " +
                    threads[i].getName() + " = " +  ut + " expected = -1");
            }
        }
        if (!mbean.isThreadCpuTimeEnabled()) {
            mbean.setThreadCpuTimeEnabled(true);
        }
        if (!mbean.isThreadCpuTimeEnabled()) {
            throw new RuntimeException("ThreadCpuTime is expected to be enabled");
        }
        times = mbean.getThreadCpuTime(ids);
        userTimes = mbean.getThreadUserTime(ids);
        goSleep(200);
        for (int i = 0; i < NUM_THREADS; i++) {
            long t = times[i];
            if (t < 0) {
                throw new RuntimeException(
                    "Invalid CPU time returned for thread " +
                    threads[i].getName() + " = " + t);
            }
            long ut = userTimes[i];
            if (ut < 0) {
                throw new RuntimeException(
                    "Invalid user time returned for thread " +
                    threads[i].getName() + " = " + ut);
            }
        }
        long[] times1 = mbean.getThreadCpuTime(ids);
        long[] userTimes1 = mbean.getThreadUserTime(ids);
        for (int i = 0; i < NUM_THREADS; i++) {
            long newTime = times1[i];
            long newUserTime = userTimes1[i];
            if (times[i] > newTime) {
                throw new RuntimeException("TEST FAILED: " +
                    threads[i].getName() +
                    " previous CPU time = " + times[i] +
                    " > current CPU time = " + newTime);
            }
            if ((times[i] + DELTA) < newTime) {
                throw new RuntimeException("TEST FAILED: " +
                    threads[i].getName() +
                    " CPU time = " + newTime +
                    " previous CPU time " + times[i] +
                    " out of expected range");
            }
            System.out.println(threads[i].getName() +
                " Previous Cpu Time = " + times[i] +
                " Current CPU time = " + newTime);
            System.out.println(threads[i].getName() +
                " Previous User Time = " + userTimes[i] +
                " Current User time = " + newUserTime);
        }
        try {
            times = mbean.getThreadCpuTime(null);
        } catch (NullPointerException e) {
            System.out.println(
                "Caught expected NullPointerException: " + e.getMessage());
        }
        try {
            ids[0] = 0;
            times = mbean.getThreadCpuTime(ids);
        } catch (IllegalArgumentException e) {
            System.out.println(
                "Caught expected IllegalArgumentException: " + e.getMessage());
        }
        synchronized (obj) {
            done = true;
            obj.notifyAll();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.out.println("Unexpected exception is thrown.");
                e.printStackTrace(System.out);
                testFailed = true;
                break;
            }
        }
        if (testFailed) {
            throw new RuntimeException("TEST FAILED");
        }
        System.out.println("Test passed");
    }
    private static void goSleep(long ms) throws Exception {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.out.println("Unexpected exception is thrown.");
            throw e;
        }
    }
    private static void waitUntilThreadBlocked()
        throws Exception {
        int count = 0;
        while (count != NUM_THREADS) {
            goSleep(100);
            count = 0;
            for (int i = 0; i < NUM_THREADS; i++) {
                ThreadInfo info = mbean.getThreadInfo(threads[i].getId());
                if (info.getThreadState() == Thread.State.WAITING) {
                    count++;
                }
            }
        }
    }
    public static void doit() {
        double sum = 0;
        for (int i = 0; i < 5000; i++) {
           double r = Math.random();
           double x = Math.pow(3, r);
           sum += x - r;
        }
        System.out.println(Thread.currentThread().getName() +
                           " sum = " + sum);
    }
    static class MyThread extends Thread {
        public MyThread(String name) {
            super(name);
        }
        public void run() {
            ThreadCpuTimeArray.doit();
            synchronized (obj) {
                while (!done) {
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        System.out.println("Unexpected exception is thrown.");
                        e.printStackTrace(System.out);
                        testFailed = true;
                        break;
                    }
                }
            }
            ThreadCpuTimeArray.doit();
        }
    }
}
