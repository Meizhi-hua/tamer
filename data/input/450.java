public class ThreadMXBeanProxy {
    private static MBeanServer server =
        ManagementFactory.getPlatformMBeanServer();
    private static ThreadMXBean mbean;
    static Mutex mutex = new Mutex();
    static Object lock = new Object();
    static MyThread thread = new MyThread();
    public static void main(String[] argv) throws Exception {
        mbean = newPlatformMXBeanProxy(server,
                                       THREAD_MXBEAN_NAME,
                                       ThreadMXBean.class);
        if (!mbean.isSynchronizerUsageSupported()) {
            System.out.println("Monitoring of synchronizer usage not supported");
            return;
        }
        thread.setDaemon(true);
        thread.start();
        while (!(mutex.isLocked() && mutex.getLockOwner() == thread)) {
           try {
               Thread.sleep(100);
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
        }
        long[] ids = new long[] { thread.getId() };
        ThreadInfo[] infos = getThreadMXBean().getThreadInfo(ids, true, true);
        if (infos.length != 1) {
            throw new RuntimeException("Returned ThreadInfo[] of length=" +
                infos.length + ". Expected to be 1.");
        }
        thread.checkThreadInfo(infos[0]);
        infos = mbean.getThreadInfo(ids, true, true);
        if (infos.length != 1) {
            throw new RuntimeException("Returned ThreadInfo[] of length=" +
                infos.length + ". Expected to be 1.");
        }
        thread.checkThreadInfo(infos[0]);
        boolean found = false;
        infos = mbean.dumpAllThreads(true, true);
        for (ThreadInfo ti : infos) {
            if (ti.getThreadId() == thread.getId()) {
                thread.checkThreadInfo(ti);
                found = true;
            }
        }
        if (!found) {
            throw new RuntimeException("No ThreadInfo found for MyThread");
        }
        System.out.println("Test passed");
    }
    static class MyThread extends Thread {
        public MyThread() {
            super("MyThread");
        }
        public void run() {
            synchronized (lock) {
                mutex.lock();
                Object o = new Object();
                synchronized(o) {
                    try {
                        o.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        int OWNED_MONITORS = 1;
        int OWNED_SYNCS = 1;
        void checkThreadInfo(ThreadInfo info) {
            if (!getName().equals(info.getThreadName())) {
                throw new RuntimeException("Name: " + info.getThreadName() +
                    " not matched. Expected: " + getName());
            }
            MonitorInfo[] monitors = info.getLockedMonitors();
            if (monitors.length != OWNED_MONITORS) {
                throw new RuntimeException("Number of locked monitors = " +
                    monitors.length +
                    " not matched. Expected: " + OWNED_MONITORS);
            }
            MonitorInfo m = monitors[0];
            StackTraceElement ste = m.getLockedStackFrame();
            int depth = m.getLockedStackDepth();
            StackTraceElement[] stacktrace = info.getStackTrace();
            if (!ste.equals(stacktrace[depth])) {
                System.out.println("LockedStackFrame:- " + ste);
                System.out.println("StackTrace at " + depth + " :-" +
                    stacktrace[depth]);
                throw new RuntimeException("LockedStackFrame does not match " +
                    "stack frame in ThreadInfo.getStackTrace");
           }
           String className = lock.getClass().getName();
           int hcode = System.identityHashCode(lock);
           if (!className.equals(m.getClassName()) ||
                   hcode != m.getIdentityHashCode() ||
                   !m.getLockedStackFrame().getMethodName().equals("run")) {
                System.out.println(info);
                throw new RuntimeException("MonitorInfo " + m +
                   " doesn't match.");
            }
            LockInfo[] syncs = info.getLockedSynchronizers();
            if (syncs.length != OWNED_SYNCS) {
                throw new RuntimeException("Number of locked syncs = " +
                        syncs.length + " not matched. Expected: " + OWNED_SYNCS);
            }
            AbstractOwnableSynchronizer s = mutex.getSync();
            String lockName = s.getClass().getName();
            hcode = System.identityHashCode(s);
            if (!lockName.equals(syncs[0].getClassName())) {
                throw new RuntimeException("LockInfo : " + syncs[0] +
                    " class name not matched. Expected: " + lockName);
            }
            if (hcode != syncs[0].getIdentityHashCode()) {
                throw new RuntimeException("LockInfo: " + syncs[0] +
                    " IdentityHashCode not matched. Expected: " + hcode);
            }
        }
    }
    static class Mutex implements Lock, java.io.Serializable {
        class Sync extends AbstractQueuedSynchronizer {
            protected boolean isHeldExclusively() {
                return getState() == 1;
            }
            public boolean tryAcquire(int acquires) {
                assert acquires == 1; 
                if (compareAndSetState(0, 1)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }
            protected boolean tryRelease(int releases) {
                assert releases == 1; 
                if (getState() == 0) throw new IllegalMonitorStateException();
                setExclusiveOwnerThread(null);
                setState(0);
                return true;
            }
            Condition newCondition() { return new ConditionObject(); }
            private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
                s.defaultReadObject();
                setState(0); 
            }
            protected Thread getLockOwner() {
                return getExclusiveOwnerThread();
            }
        }
        private final Sync sync = new Sync();
        public void lock()                { sync.acquire(1); }
        public boolean tryLock()          { return sync.tryAcquire(1); }
        public void unlock()              { sync.release(1); }
        public Condition newCondition()   { return sync.newCondition(); }
        public boolean isLocked()         { return sync.isHeldExclusively(); }
        public boolean hasQueuedThreads() { return sync.hasQueuedThreads(); }
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }
        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }
        public Thread getLockOwner()     { return sync.getLockOwner(); }
        public AbstractOwnableSynchronizer getSync() { return sync; }
    }
}
