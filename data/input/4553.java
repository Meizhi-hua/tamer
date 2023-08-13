public class MyOwnSynchronizer {
    static ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
    static Mutex mutex = new Mutex();
    static MyThread thread = new MyThread();
    public static void main(String[] argv) throws Exception {
        if (!mbean.isSynchronizerUsageSupported()) {
            System.out.println("Monitoring of synchronizer usage not supported");
            return;
        }
        thread.setDaemon(true);
        thread.start();
        while (!mutex.isLocked()) {
           try {
               Thread.sleep(100);
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
        }
        ThreadDump.threadDump();
        ThreadInfo[] tinfos = mbean.dumpAllThreads(false, true);
        for (ThreadInfo ti : tinfos) {
           MonitorInfo[] monitors = ti.getLockedMonitors();
           if (monitors.length != 0) {
               throw new RuntimeException("Name: " + ti.getThreadName() +
                   " has non-empty locked monitors = " + monitors.length);
           }
           LockInfo[] syncs = ti.getLockedSynchronizers();
           if (ti.getThreadId() == thread.getId()) {
               thread.checkLockedSyncs(ti, syncs);
           }
        }
        tinfos = mbean.getThreadInfo(new long[] {thread.getId()}, false, true);
        if (tinfos.length != 1) {
            throw new RuntimeException("getThreadInfo() returns " +
                tinfos.length + " ThreadInfo objects. Expected 0.");
        }
        ThreadInfo ti = tinfos[0];
        if (ti.getLockedMonitors().length != 0) {
            throw new RuntimeException("Name: " + ti.getThreadName() +
               " has non-empty locked monitors = " +
               ti.getLockedMonitors().length);
        }
        thread.checkLockedSyncs(ti, ti.getLockedSynchronizers());
        System.out.println("Test passed");
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
        public AbstractOwnableSynchronizer getSync() { return sync; }
    }
    static class MyThread extends Thread {
        public MyThread() {
            super("MyThread");
        }
        public void run() {
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
        int OWNED_SYNCS = 1;
        void checkLockedSyncs(ThreadInfo info, LockInfo[] syncs) {
            if (!getName().equals(info.getThreadName())) {
                throw new RuntimeException("Name: " + info.getThreadName() +
                    " not matched. Expected: " + getName());
            }
            if (syncs.length != OWNED_SYNCS) {
                throw new RuntimeException("Number of locked syncs = " +
                    syncs.length +
                    " not matched. Expected: " + OWNED_SYNCS);
            }
            AbstractOwnableSynchronizer s = mutex.getSync();
            String lockName = s.getClass().getName();
            int hcode = System.identityHashCode(s);
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
}
