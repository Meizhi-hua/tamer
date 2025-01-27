@TestTargetClass(ScanResult.class)
public class ScanResultTest extends AndroidTestCase {
    private static class MySync {
        int expectedState = STATE_NULL;
    }
    private WifiManager mWifiManager;
    private WifiLock mWifiLock;
    private static MySync mMySync;
    private static final int STATE_NULL = 0;
    private static final int STATE_WIFI_CHANGING = 1;
    private static final int STATE_WIFI_CHANGED = 2;
    private static final String TAG = "WifiInfoTest";
    private static final int TIMEOUT_MSEC = 6000;
    private static final int WAIT_MSEC = 60;
    private static final int DURATION = 10000;
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                synchronized (mMySync) {
                    mMySync.expectedState = STATE_WIFI_CHANGED;
                    mMySync.notify();
                }
            }
        }
    };
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMySync = new MySync();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mWifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        assertNotNull(mWifiManager);
        mWifiLock = mWifiManager.createWifiLock(TAG);
        mWifiLock.acquire();
        if (!mWifiManager.isWifiEnabled())
            setWifiEnabled(true);
        Thread.sleep(DURATION);
        assertTrue(mWifiManager.isWifiEnabled());
        mMySync.expectedState = STATE_NULL;
    }
    @Override
    protected void tearDown() throws Exception {
        mWifiLock.release();
        mContext.unregisterReceiver(mReceiver);
        if (!mWifiManager.isWifiEnabled())
            setWifiEnabled(true);
        Thread.sleep(DURATION);
        super.tearDown();
    }
    private void setWifiEnabled(boolean enable) throws Exception {
        synchronized (mMySync) {
            mMySync.expectedState = STATE_WIFI_CHANGING;
            assertTrue(mWifiManager.setWifiEnabled(enable));
            long timeout = System.currentTimeMillis() + TIMEOUT_MSEC;
            while (System.currentTimeMillis() < timeout
                    && mMySync.expectedState == STATE_WIFI_CHANGING)
                mMySync.wait(WAIT_MSEC);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testScanResultProperties() {
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult mScanResult = scanResults.get(i);
            assertNotNull(mScanResult.toString());
        }
    }
}
