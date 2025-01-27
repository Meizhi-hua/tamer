@TestTargetClass(WifiManager.class)
public class WifiManagerTest extends AndroidTestCase {
    private static class MySync {
        int expectedState = STATE_NULL;
    }
    private WifiManager mWifiManager;
    private WifiLock mWifiLock;
    private static MySync mMySync;
    private List<ScanResult> mScanResult = null;
    private static final int MIN_RSSI = -100;
    private static final int MAX_RSSI = -55;
    private static final int STATE_NULL = 0;
    private static final int STATE_WIFI_CHANGING = 1;
    private static final int STATE_WIFI_CHANGED = 2;
    private static final int STATE_SCANING = 3;
    private static final int STATE_SCAN_RESULTS_AVAILABLE = 4;
    private static final String TAG = "WifiManagerTest";
    private static final String SSID1 = "\"WifiManagerTest\"";
    private static final String SSID2 = "\"WifiManagerTestModified\"";
    private static final int TIMEOUT_MSEC = 6000;
    private static final int WAIT_MSEC = 60;
    private static final int DURATION = 10000;
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                synchronized (mMySync) {
                    if (mWifiManager.getScanResults() != null) {
                        mScanResult = mWifiManager.getScanResults();
                        mMySync.expectedState = STATE_SCAN_RESULTS_AVAILABLE;
                        mScanResult = mWifiManager.getScanResults();
                        mMySync.notify();
                    }
                }
            } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
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
    private void startScan() throws Exception {
        synchronized (mMySync) {
            mMySync.expectedState = STATE_SCANING;
            assertTrue(mWifiManager.startScan());
            long timeout = System.currentTimeMillis() + TIMEOUT_MSEC;
            while (System.currentTimeMillis() < timeout && mMySync.expectedState == STATE_SCANING)
                mMySync.wait(WAIT_MSEC);
        }
    }
    private boolean existSSID(String ssid) {
        for (final WifiConfiguration w : mWifiManager.getConfiguredNetworks()) {
            if (w.SSID.equals(ssid))
                return true;
        }
        return false;
    }
    private int findConfiguredNetworks(String SSID, List<WifiConfiguration> networks) {
        for (final WifiConfiguration w : networks) {
            if (w.SSID.equals(SSID))
                return networks.indexOf(w);
        }
        return -1;
    }
    private void assertDisableOthers(WifiConfiguration wifiConfiguration, boolean disableOthers) {
        for (WifiConfiguration w : mWifiManager.getConfiguredNetworks()) {
            if ((!w.SSID.equals(wifiConfiguration.SSID)) && w.status != Status.CURRENT) {
                if (disableOthers)
                    assertEquals(Status.DISABLED, w.status);
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isWifiEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWifiEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startScan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getScanResults",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pingSupplicant",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reassociate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reconnect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "disconnect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createWifiLock",
            args = {int.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createWifiLock",
            args = {String.class}
        )
    })
    public void testWifiManagerActions() throws Exception {
        assertTrue(mWifiManager.reconnect());
        assertTrue(mWifiManager.reassociate());
        assertTrue(mWifiManager.disconnect());
        assertTrue(mWifiManager.pingSupplicant());
        startScan();
        setWifiEnabled(false);
        Thread.sleep(DURATION);
        assertFalse(mWifiManager.pingSupplicant());
        final String TAG = "Test";
        assertNotNull(mWifiManager.createWifiLock(TAG));
        assertNotNull(mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isWifiEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWifiState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWifiEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getConnectionInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDhcpInfo",
            args = {}
        )
    })
    public void testWifiManagerProperties() throws Exception {
        setWifiEnabled(true);
        assertTrue(mWifiManager.isWifiEnabled());
        assertNotNull(mWifiManager.getDhcpInfo());
        assertEquals(WifiManager.WIFI_STATE_ENABLED, mWifiManager.getWifiState());
        mWifiManager.getConnectionInfo();
        setWifiEnabled(false);
        assertFalse(mWifiManager.isWifiEnabled());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isWifiEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWifiEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getConfiguredNetworks",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addNetwork",
            args = {android.net.wifi.WifiConfiguration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "updateNetwork",
            args = {android.net.wifi.WifiConfiguration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeNetwork",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "enableNetwork",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "disableNetwork",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveConfiguration",
            args = {}
        )
    })
    public void testWifiManagerNetWork() throws Exception {
        WifiConfiguration wifiConfiguration;
        final int notExist = -1;
        List<WifiConfiguration> wifiConfiguredNetworks = mWifiManager.getConfiguredNetworks();
        int pos = findConfiguredNetworks(SSID1, wifiConfiguredNetworks);
        if (notExist != pos) {
            wifiConfiguration = wifiConfiguredNetworks.get(pos);
            mWifiManager.removeNetwork(wifiConfiguration.networkId);
        }
        pos = findConfiguredNetworks(SSID1, wifiConfiguredNetworks);
        assertEquals(notExist, pos);
        final int size = wifiConfiguredNetworks.size();
        wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = SSID1;
        int netId = mWifiManager.addNetwork(wifiConfiguration);
        assertTrue(existSSID(SSID1));
        wifiConfiguredNetworks = mWifiManager.getConfiguredNetworks();
        assertEquals(size + 1, wifiConfiguredNetworks.size());
        pos = findConfiguredNetworks(SSID1, wifiConfiguredNetworks);
        assertTrue(notExist != pos);
        boolean disableOthers = false;
        assertTrue(mWifiManager.enableNetwork(netId, disableOthers));
        wifiConfiguration = mWifiManager.getConfiguredNetworks().get(pos);
        assertDisableOthers(wifiConfiguration, disableOthers);
        assertEquals(Status.ENABLED, wifiConfiguration.status);
        disableOthers = true;
        assertTrue(mWifiManager.enableNetwork(netId, disableOthers));
        wifiConfiguration = mWifiManager.getConfiguredNetworks().get(pos);
        assertDisableOthers(wifiConfiguration, disableOthers);
        assertTrue(mWifiManager.disableNetwork(netId));
        wifiConfiguration = mWifiManager.getConfiguredNetworks().get(pos);
        assertEquals(Status.DISABLED, wifiConfiguration.status);
        wifiConfiguration = wifiConfiguredNetworks.get(pos);
        wifiConfiguration.SSID = SSID2;
        netId = mWifiManager.updateNetwork(wifiConfiguration);
        assertFalse(existSSID(SSID1));
        assertTrue(existSSID(SSID2));
        assertTrue(mWifiManager.removeNetwork(netId));
        assertFalse(mWifiManager.removeNetwork(notExist));
        assertFalse(existSSID(SSID1));
        assertFalse(existSSID(SSID2));
        assertTrue(mWifiManager.saveConfiguration());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "compareSignalLevel",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "calculateSignalLevel",
            args = {int.class, int.class}
        )
    })
    public void testSignal() {
        final int numLevels = 9;
        int expectLevel = 0;
        assertEquals(expectLevel, WifiManager.calculateSignalLevel(MIN_RSSI, numLevels));
        assertEquals(numLevels - 1, WifiManager.calculateSignalLevel(MAX_RSSI, numLevels));
        expectLevel = 4;
        assertEquals(expectLevel, WifiManager.calculateSignalLevel((MIN_RSSI + MAX_RSSI) / 2,
                numLevels));
        int rssiA = 4;
        int rssiB = 5;
        assertTrue(WifiManager.compareSignalLevel(rssiA, rssiB) < 0);
        rssiB = 4;
        assertTrue(WifiManager.compareSignalLevel(rssiA, rssiB) == 0);
        rssiA = 5;
        rssiB = 4;
        assertTrue(WifiManager.compareSignalLevel(rssiA, rssiB) > 0);
    }
}
