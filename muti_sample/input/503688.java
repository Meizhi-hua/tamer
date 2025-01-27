class IdleCache {
    class Entry {
        HttpHost mHost;
        Connection mConnection;
        long mTimeout;
    };
    private final static int IDLE_CACHE_MAX = 8;
    private final static int EMPTY_CHECK_MAX = 5;
    private final static int TIMEOUT = 6 * 1000;
    private final static int CHECK_INTERVAL = 2 * 1000;
    private Entry[] mEntries = new Entry[IDLE_CACHE_MAX];
    private int mCount = 0;
    private IdleReaper mThread = null;
    private int mCached = 0;
    private int mReused = 0;
    IdleCache() {
        for (int i = 0; i < IDLE_CACHE_MAX; i++) {
            mEntries[i] = new Entry();
        }
    }
    synchronized boolean cacheConnection(
            HttpHost host, Connection connection) {
        boolean ret = false;
        if (HttpLog.LOGV) {
            HttpLog.v("IdleCache size " + mCount + " host "  + host);
        }
        if (mCount < IDLE_CACHE_MAX) {
            long time = SystemClock.uptimeMillis();
            for (int i = 0; i < IDLE_CACHE_MAX; i++) {
                Entry entry = mEntries[i];
                if (entry.mHost == null) {
                    entry.mHost = host;
                    entry.mConnection = connection;
                    entry.mTimeout = time + TIMEOUT;
                    mCount++;
                    if (HttpLog.LOGV) mCached++;
                    ret = true;
                    if (mThread == null) {
                        mThread = new IdleReaper();
                        mThread.start();
                    }
                    break;
                }
            }
        }
        return ret;
    }
    synchronized Connection getConnection(HttpHost host) {
        Connection ret = null;
        if (mCount > 0) {
            for (int i = 0; i < IDLE_CACHE_MAX; i++) {
                Entry entry = mEntries[i];
                HttpHost eHost = entry.mHost;
                if (eHost != null && eHost.equals(host)) {
                    ret = entry.mConnection;
                    entry.mHost = null;
                    entry.mConnection = null;
                    mCount--;
                    if (HttpLog.LOGV) mReused++;
                    break;
                }
            }
        }
        return ret;
    }
    synchronized void clear() {
        for (int i = 0; mCount > 0 && i < IDLE_CACHE_MAX; i++) {
            Entry entry = mEntries[i];
            if (entry.mHost != null) {
                entry.mHost = null;
                entry.mConnection.closeConnection();
                entry.mConnection = null;
                mCount--;
            }
        }
    }
    private synchronized void clearIdle() {
        if (mCount > 0) {
            long time = SystemClock.uptimeMillis();
            for (int i = 0; i < IDLE_CACHE_MAX; i++) {
                Entry entry = mEntries[i];
                if (entry.mHost != null && time > entry.mTimeout) {
                    entry.mHost = null;
                    entry.mConnection.closeConnection();
                    entry.mConnection = null;
                    mCount--;
                }
            }
        }
    }
    private class IdleReaper extends Thread {
        public void run() {
            int check = 0;
            setName("IdleReaper");
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
            synchronized (IdleCache.this) {
                while (check < EMPTY_CHECK_MAX) {
                    try {
                        IdleCache.this.wait(CHECK_INTERVAL);
                    } catch (InterruptedException ex) {
                    }
                    if (mCount == 0) {
                        check++;
                    } else {
                        check = 0;
                        clearIdle();
                    }
                }
                mThread = null;
            }
            if (HttpLog.LOGV) {
                HttpLog.v("IdleCache IdleReaper shutdown: cached " + mCached +
                          " reused " + mReused);
                mCached = 0;
                mReused = 0;
            }
        }
    }
}
