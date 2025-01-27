public class Tethering extends INetworkManagementEventObserver.Stub {
    private Context mContext;
    private final String TAG = "Tethering";
    private boolean mBooted = false;
    private boolean mDeferedUsbConnection = false;
    private String[] mTetherableUsbRegexs;
    private String[] mTetherableWifiRegexs;
    private String[] mUpstreamIfaceRegexs;
    private Looper mLooper;
    private HandlerThread mThread;
    private HashMap<String, TetherInterfaceSM> mIfaces; 
    private BroadcastReceiver mStateReceiver;
    private static final String USB_NEAR_IFACE_ADDR      = "192.168.42.129";
    private static final String USB_NETMASK              = "255.255.255.0";
    private String[] mDhcpRange;
    private static final String DHCP_DEFAULT_RANGE1_START = "192.168.42.2";
    private static final String DHCP_DEFAULT_RANGE1_STOP  = "192.168.42.254";
    private static final String DHCP_DEFAULT_RANGE2_START = "192.168.43.2";
    private static final String DHCP_DEFAULT_RANGE2_STOP  = "192.168.43.254";
    private String[] mDnsServers;
    private static final String DNS_DEFAULT_SERVER1 = "8.8.8.8";
    private static final String DNS_DEFAULT_SERVER2 = "4.2.2.2";
    private boolean mDunRequired;  
    private HierarchicalStateMachine mTetherMasterSM;
    private Notification mTetheredNotification;
    private boolean mUsbMassStorageOff;  
    private boolean mUsbConnected;       
    public Tethering(Context context, Looper looper) {
        Log.d(TAG, "Tethering starting");
        mContext = context;
        mLooper = looper;
        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
        INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
        try {
            service.registerObserver(this);
        } catch (RemoteException e) {
            Log.e(TAG, "Error registering observer :" + e);
        }
        mIfaces = new HashMap<String, TetherInterfaceSM>();
        mThread = new HandlerThread("Tethering");
        mThread.start();
        mLooper = mThread.getLooper();
        mTetherMasterSM = new TetherMasterSM("TetherMaster", mLooper);
        mTetherMasterSM.start();
        mStateReceiver = new StateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(mStateReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        mContext.registerReceiver(mStateReceiver, filter);
        mUsbMassStorageOff = !Environment.MEDIA_SHARED.equals(
                Environment.getExternalStorageState());
        mDhcpRange = context.getResources().getStringArray(
                com.android.internal.R.array.config_tether_dhcp_range);
        if ((mDhcpRange.length == 0) || (mDhcpRange.length % 2 ==1)) {
            mDhcpRange = new String[4];
            mDhcpRange[0] = DHCP_DEFAULT_RANGE1_START;
            mDhcpRange[1] = DHCP_DEFAULT_RANGE1_STOP;
            mDhcpRange[2] = DHCP_DEFAULT_RANGE2_START;
            mDhcpRange[3] = DHCP_DEFAULT_RANGE2_STOP;
        }
        mDunRequired = false; 
        mTetherableUsbRegexs = context.getResources().getStringArray(
                com.android.internal.R.array.config_tether_usb_regexs);
        mTetherableWifiRegexs = context.getResources().getStringArray(
                com.android.internal.R.array.config_tether_wifi_regexs);
        mUpstreamIfaceRegexs = context.getResources().getStringArray(
                com.android.internal.R.array.config_tether_upstream_regexs);
        mDnsServers = new String[2];
        mDnsServers[0] = DNS_DEFAULT_SERVER1;
        mDnsServers[1] = DNS_DEFAULT_SERVER2;
    }
    public void interfaceLinkStatusChanged(String iface, boolean link) {
        Log.d(TAG, "interfaceLinkStatusChanged " + iface + ", " + link);
        boolean found = false;
        boolean usb = false;
        if (isWifi(iface)) {
            found = true;
        } else if (isUsb(iface)) {
            found = true;
            usb = true;
        }
        if (found == false) return;
        synchronized (mIfaces) {
            TetherInterfaceSM sm = mIfaces.get(iface);
            if (link) {
                if (sm == null) {
                    sm = new TetherInterfaceSM(iface, mLooper, usb);
                    mIfaces.put(iface, sm);
                    sm.start();
                }
            } else {
                if (sm != null) {
                    sm.sendMessage(TetherInterfaceSM.CMD_INTERFACE_DOWN);
                    mIfaces.remove(iface);
                }
            }
        }
    }
    private boolean isUsb(String iface) {
        for (String regex : mTetherableUsbRegexs) {
            if (iface.matches(regex)) return true;
        }
        return false;
    }
    public boolean isWifi(String iface) {
        for (String regex : mTetherableWifiRegexs) {
            if (iface.matches(regex)) return true;
        }
        return false;
    }
    public void interfaceAdded(String iface) {
        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
        INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
        boolean found = false;
        boolean usb = false;
        if (isWifi(iface)) {
            found = true;
        }
        if (isUsb(iface)) {
            found = true;
            usb = true;
        }
        if (found == false) {
            Log.d(TAG, iface + " is not a tetherable iface, ignoring");
            return;
        }
        synchronized (mIfaces) {
            TetherInterfaceSM sm = mIfaces.get(iface);
            if (sm != null) {
                Log.e(TAG, "active iface (" + iface + ") reported as added, ignoring");
                return;
            }
            sm = new TetherInterfaceSM(iface, mLooper, usb);
            mIfaces.put(iface, sm);
            sm.start();
        }
        Log.d(TAG, "interfaceAdded :" + iface);
    }
    public void interfaceRemoved(String iface) {
        synchronized (mIfaces) {
            TetherInterfaceSM sm = mIfaces.get(iface);
            if (sm == null) {
                Log.e(TAG, "attempting to remove unknown iface (" + iface + "), ignoring");
                return;
            }
            sm.sendMessage(TetherInterfaceSM.CMD_INTERFACE_DOWN);
            mIfaces.remove(iface);
        }
    }
    public int tether(String iface) {
        Log.d(TAG, "Tethering " + iface);
        TetherInterfaceSM sm = null;
        synchronized (mIfaces) {
            sm = mIfaces.get(iface);
        }
        if (sm == null) {
            Log.e(TAG, "Tried to Tether an unknown iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
        }
        if (!sm.isAvailable() && !sm.isErrored()) {
            Log.e(TAG, "Tried to Tether an unavailable iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNAVAIL_IFACE;
        }
        sm.sendMessage(TetherInterfaceSM.CMD_TETHER_REQUESTED);
        return ConnectivityManager.TETHER_ERROR_NO_ERROR;
    }
    public int untether(String iface) {
        Log.d(TAG, "Untethering " + iface);
        TetherInterfaceSM sm = null;
        synchronized (mIfaces) {
            sm = mIfaces.get(iface);
        }
        if (sm == null) {
            Log.e(TAG, "Tried to Untether an unknown iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
        }
        if (sm.isErrored()) {
            Log.e(TAG, "Tried to Untethered an errored iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNAVAIL_IFACE;
        }
        sm.sendMessage(TetherInterfaceSM.CMD_TETHER_UNREQUESTED);
        return ConnectivityManager.TETHER_ERROR_NO_ERROR;
    }
    public int getLastTetherError(String iface) {
        TetherInterfaceSM sm = null;
        synchronized (mIfaces) {
            sm = mIfaces.get(iface);
        }
        if (sm == null) {
            Log.e(TAG, "Tried to getLastTetherError on an unknown iface :" + iface + ", ignoring");
            return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
        }
        return sm.getLastError();
    }
    private void sendTetherStateChangedBroadcast() {
        IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
        IConnectivityManager service = IConnectivityManager.Stub.asInterface(b);
        try {
            if (!service.isTetheringSupported()) return;
        } catch (RemoteException e) {
            return;
        }
        ArrayList<String> availableList = new ArrayList<String>();
        ArrayList<String> activeList = new ArrayList<String>();
        ArrayList<String> erroredList = new ArrayList<String>();
        boolean wifiTethered = false;
        boolean usbTethered = false;
        synchronized (mIfaces) {
            Set ifaces = mIfaces.keySet();
            for (Object iface : ifaces) {
                TetherInterfaceSM sm = mIfaces.get(iface);
                if (sm != null) {
                    if(sm.isErrored()) {
                        erroredList.add((String)iface);
                    } else if (sm.isAvailable()) {
                        availableList.add((String)iface);
                    } else if (sm.isTethered()) {
                        if (isUsb((String)iface)) {
                            usbTethered = true;
                        } else if (isWifi((String)iface)) {
                            wifiTethered = true;
                        }
                        activeList.add((String)iface);
                    }
                }
            }
        }
        Intent broadcast = new Intent(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_AVAILABLE_TETHER,
                availableList);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER, activeList);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ERRORED_TETHER,
                erroredList);
        mContext.sendStickyBroadcast(broadcast);
        Log.d(TAG, "sendTetherStateChangedBroadcast " + availableList.size() + ", " +
                activeList.size() + ", " + erroredList.size());
        if (usbTethered) {
            if (wifiTethered) {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_general);
            } else {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_usb);
            }
        } else if (wifiTethered) {
            showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_wifi);
        } else {
            clearTetheredNotification();
        }
    }
    private void showTetheredNotification(int icon) {
        NotificationManager notificationManager =
                (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        if (mTetheredNotification != null) {
            if (mTetheredNotification.icon == icon) {
                return;
            }
            notificationManager.cancel(mTetheredNotification.icon);
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
        Resources r = Resources.getSystem();
        CharSequence title = r.getText(com.android.internal.R.string.tethered_notification_title);
        CharSequence message = r.getText(com.android.internal.R.string.
                tethered_notification_message);
        if(mTetheredNotification == null) {
            mTetheredNotification = new Notification();
            mTetheredNotification.when = 0;
        }
        mTetheredNotification.icon = icon;
        mTetheredNotification.defaults &= ~Notification.DEFAULT_SOUND;
        mTetheredNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mTetheredNotification.tickerText = title;
        mTetheredNotification.setLatestEventInfo(mContext, title, message, pi);
        notificationManager.notify(mTetheredNotification.icon, mTetheredNotification);
    }
    private void clearTetheredNotification() {
        NotificationManager notificationManager =
            (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && mTetheredNotification != null) {
            notificationManager.cancel(mTetheredNotification.icon);
            mTetheredNotification = null;
        }
    }
    private void updateUsbStatus() {
        boolean enable = mUsbConnected && mUsbMassStorageOff;
        if (mBooted) {
            enableUsbIfaces(enable);
        }
    }
    private class StateReceiver extends BroadcastReceiver {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                mUsbConnected = (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                        == BatteryManager.BATTERY_PLUGGED_USB);
                Tethering.this.updateUsbStatus();
            } else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {
                mUsbMassStorageOff = false;
                updateUsbStatus();
            }
            else if (action.equals(Intent.ACTION_MEDIA_UNSHARED)) {
                mUsbMassStorageOff = true;
                updateUsbStatus();
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(TAG, "Tethering got CONNECTIVITY_ACTION");
                mTetherMasterSM.sendMessage(TetherMasterSM.CMD_UPSTREAM_CHANGED);
            } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                mBooted = true;
                updateUsbStatus();
            }
        }
    }
    private void enableUsbIfaces(boolean enable) {
        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
        INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
        String[] ifaces = new String[0];
        try {
            ifaces = service.listInterfaces();
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces :" + e);
            return;
        }
        for (String iface : ifaces) {
            if (isUsb(iface)) {
                if (enable) {
                    interfaceAdded(iface);
                } else {
                    interfaceRemoved(iface);
                }
            }
        }
    }
    private boolean enableUsbRndis(boolean enabled) {
        Log.d(TAG, "enableUsbRndis(" + enabled + ")");
        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
        try {
            if (enabled) {
                synchronized (this) {
                    if (!service.isUsbRNDISStarted()) {
                        service.startUsbRNDIS();
                    }
                }
            } else {
                if (service.isUsbRNDISStarted()) {
                    service.stopUsbRNDIS();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling usb RNDIS :" + e);
            return false;
        }
        return true;
    }
    private boolean configureUsbIface(boolean enabled) {
        Log.d(TAG, "configureUsbIface(" + enabled + ")");
        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
        INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
        String[] ifaces = new String[0];
        try {
            ifaces = service.listInterfaces();
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces :" + e);
            return false;
        }
        for (String iface : ifaces) {
            if (isUsb(iface)) {
                InterfaceConfiguration ifcg = null;
                try {
                    ifcg = service.getInterfaceConfig(iface);
                    if (ifcg != null) {
                        String[] addr = USB_NEAR_IFACE_ADDR.split("\\.");
                        ifcg.ipAddr = (Integer.parseInt(addr[0]) << 24) +
                                (Integer.parseInt(addr[1]) << 16) +
                                (Integer.parseInt(addr[2]) << 8) +
                                (Integer.parseInt(addr[3]));
                        addr = USB_NETMASK.split("\\.");
                        ifcg.netmask = (Integer.parseInt(addr[0]) << 24) +
                                (Integer.parseInt(addr[1]) << 16) +
                                (Integer.parseInt(addr[2]) << 8) +
                                (Integer.parseInt(addr[3]));
                        if (enabled) {
                            ifcg.interfaceFlags = ifcg.interfaceFlags.replace("down", "up");
                        } else {
                            ifcg.interfaceFlags = ifcg.interfaceFlags.replace("up", "down");
                        }
                        ifcg.interfaceFlags = ifcg.interfaceFlags.replace("running", "");
                        ifcg.interfaceFlags = ifcg.interfaceFlags.replace("  "," ");
                        service.setInterfaceConfig(iface, ifcg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error configuring interface " + iface + ", :" + e);
                    return false;
                }
            }
        }
        return true;
    }
    public String[] getTetherableUsbRegexs() {
        return mTetherableUsbRegexs;
    }
    public String[] getTetherableWifiRegexs() {
        return mTetherableWifiRegexs;
    }
    public String[] getUpstreamIfaceRegexs() {
        return mUpstreamIfaceRegexs;
    }
    public boolean isDunRequired() {
        boolean defaultVal = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_tether_dun_required);
        boolean result = (Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.TETHER_DUN_REQUIRED, (defaultVal ? 1 : 0)) == 1);
        return result;
    }
    public String[] getTetheredIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mIfaces) {
            Set keys = mIfaces.keySet();
            for (Object key : keys) {
                TetherInterfaceSM sm = mIfaces.get(key);
                if (sm.isTethered()) {
                    list.add((String)key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i=0; i < list.size(); i++) {
            retVal[i] = list.get(i);
        }
        return retVal;
    }
    public String[] getTetherableIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mIfaces) {
            Set keys = mIfaces.keySet();
            for (Object key : keys) {
                TetherInterfaceSM sm = mIfaces.get(key);
                if (sm.isAvailable()) {
                    list.add((String)key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i=0; i < list.size(); i++) {
            retVal[i] = list.get(i);
        }
        return retVal;
    }
    public String[] getErroredIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mIfaces) {
            Set keys = mIfaces.keySet();
            for (Object key : keys) {
                TetherInterfaceSM sm = mIfaces.get(key);
                if (sm.isErrored()) {
                    list.add((String)key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i= 0; i< list.size(); i++) {
            retVal[i] = list.get(i);
        }
        return retVal;
    }
    class TetherInterfaceSM extends HierarchicalStateMachine {
        static final int CMD_TETHER_MODE_DEAD            =  1;
        static final int CMD_TETHER_REQUESTED            =  2;
        static final int CMD_TETHER_UNREQUESTED          =  3;
        static final int CMD_INTERFACE_DOWN              =  4;
        static final int CMD_INTERFACE_UP                =  5;
        static final int CMD_CELL_DUN_ERROR              =  6;
        static final int CMD_IP_FORWARDING_ENABLE_ERROR  =  7;
        static final int CMD_IP_FORWARDING_DISABLE_ERROR =  8;
        static final int CMD_START_TETHERING_ERROR       =  9;
        static final int CMD_STOP_TETHERING_ERROR        = 10;
        static final int CMD_SET_DNS_FORWARDERS_ERROR    = 11;
        static final int CMD_TETHER_CONNECTION_CHANGED   = 12;
        private HierarchicalState mDefaultState;
        private HierarchicalState mInitialState;
        private HierarchicalState mStartingState;
        private HierarchicalState mTetheredState;
        private HierarchicalState mUnavailableState;
        private boolean mAvailable;
        private boolean mTethered;
        int mLastError;
        String mIfaceName;
        String mMyUpstreamIfaceName;  
        boolean mUsb;
        TetherInterfaceSM(String name, Looper looper, boolean usb) {
            super(name, looper);
            mIfaceName = name;
            mUsb = usb;
            setLastError(ConnectivityManager.TETHER_ERROR_NO_ERROR);
            mInitialState = new InitialState();
            addState(mInitialState);
            mStartingState = new StartingState();
            addState(mStartingState);
            mTetheredState = new TetheredState();
            addState(mTetheredState);
            mUnavailableState = new UnavailableState();
            addState(mUnavailableState);
            setInitialState(mInitialState);
        }
        public String toString() {
            String res = new String();
            res += mIfaceName + " - ";
            HierarchicalState current = getCurrentState();
            if (current == mInitialState) res += "InitialState";
            if (current == mStartingState) res += "StartingState";
            if (current == mTetheredState) res += "TetheredState";
            if (current == mUnavailableState) res += "UnavailableState";
            if (mAvailable) res += " - Available";
            if (mTethered) res += " - Tethered";
            res += " - lastError =" + mLastError;
            return res;
        }
        public synchronized int getLastError() {
            return mLastError;
        }
        private synchronized void setLastError(int error) {
            mLastError = error;
            if (isErrored()) {
                if (mUsb) {
                    Tethering.this.configureUsbIface(false);
                }
            }
        }
        public synchronized boolean isAvailable() {
            return mAvailable;
        }
        private synchronized void setAvailable(boolean available) {
            mAvailable = available;
        }
        public synchronized boolean isTethered() {
            return mTethered;
        }
        private synchronized void setTethered(boolean tethered) {
            mTethered = tethered;
        }
        public synchronized boolean isErrored() {
            return (mLastError != ConnectivityManager.TETHER_ERROR_NO_ERROR);
        }
        class InitialState extends HierarchicalState {
            @Override
            public void enter() {
                setAvailable(true);
                setTethered(false);
                sendTetherStateChangedBroadcast();
            }
            @Override
            public boolean processMessage(Message message) {
                Log.d(TAG, "InitialState.processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_REQUESTED:
                        setLastError(ConnectivityManager.TETHER_ERROR_NO_ERROR);
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_REQUESTED,
                                TetherInterfaceSM.this);
                        transitionTo(mStartingState);
                        break;
                    case CMD_INTERFACE_DOWN:
                        transitionTo(mUnavailableState);
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }
        class StartingState extends HierarchicalState {
            @Override
            public void enter() {
                setAvailable(false);
                if (mUsb) {
                    if (!Tethering.this.configureUsbIface(true)) {
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        setLastError(ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);
                        transitionTo(mInitialState);
                        return;
                    }
                }
                sendTetherStateChangedBroadcast();
                transitionTo(mTetheredState);
            }
            @Override
            public boolean processMessage(Message message) {
                Log.d(TAG, "StartingState.processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_UNREQUESTED:
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        if (mUsb) {
                            if (!Tethering.this.configureUsbIface(false)) {
                                setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);
                                break;
                            }
                        }
                        transitionTo(mInitialState);
                        break;
                    case CMD_CELL_DUN_ERROR:
                    case CMD_IP_FORWARDING_ENABLE_ERROR:
                    case CMD_IP_FORWARDING_DISABLE_ERROR:
                    case CMD_START_TETHERING_ERROR:
                    case CMD_STOP_TETHERING_ERROR:
                    case CMD_SET_DNS_FORWARDERS_ERROR:
                        setLastErrorAndTransitionToInitialState(
                                ConnectivityManager.TETHER_ERROR_MASTER_ERROR);
                        break;
                    case CMD_INTERFACE_DOWN:
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        transitionTo(mUnavailableState);
                        break;
                    default:
                        retValue = false;
                }
                return retValue;
            }
        }
        class TetheredState extends HierarchicalState {
            @Override
            public void enter() {
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service =
                        INetworkManagementService.Stub.asInterface(b);
                try {
                    service.tetherInterface(mIfaceName);
                } catch (Exception e) {
                    setLastError(ConnectivityManager.TETHER_ERROR_TETHER_IFACE_ERROR);
                    transitionTo(mInitialState);
                    return;
                }
                if (mUsb) Tethering.this.enableUsbRndis(true);
                Log.d(TAG, "Tethered " + mIfaceName);
                setAvailable(false);
                setTethered(true);
                sendTetherStateChangedBroadcast();
            }
            @Override
            public void exit() {
                if (mUsb) Tethering.this.enableUsbRndis(false);
            }
            @Override
            public boolean processMessage(Message message) {
                Log.d(TAG, "TetheredState.processMessage what=" + message.what);
                boolean retValue = true;
                boolean error = false;
                switch (message.what) {
                    case CMD_TETHER_UNREQUESTED:
                    case CMD_INTERFACE_DOWN:
                        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                        INetworkManagementService service =
                                INetworkManagementService.Stub.asInterface(b);
                        if (mMyUpstreamIfaceName != null) {
                            try {
                                service.disableNat(mIfaceName, mMyUpstreamIfaceName);
                                mMyUpstreamIfaceName = null;
                            } catch (Exception e) {
                                try {
                                    service.untetherInterface(mIfaceName);
                                } catch (Exception ee) {}
                                setLastErrorAndTransitionToInitialState(
                                        ConnectivityManager.TETHER_ERROR_DISABLE_NAT_ERROR);
                                break;
                            }
                        }
                        try {
                            service.untetherInterface(mIfaceName);
                        } catch (Exception e) {
                            setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_UNTETHER_IFACE_ERROR);
                            break;
                        }
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED,
                                TetherInterfaceSM.this);
                        if (message.what == CMD_TETHER_UNREQUESTED) {
                            if (mUsb) {
                                if (!Tethering.this.configureUsbIface(false)) {
                                    setLastError(
                                            ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);
                                }
                            }
                            transitionTo(mInitialState);
                        } else if (message.what == CMD_INTERFACE_DOWN) {
                            transitionTo(mUnavailableState);
                        }
                        Log.d(TAG, "Untethered " + mIfaceName);
                        break;
                    case CMD_TETHER_CONNECTION_CHANGED:
                        String newUpstreamIfaceName = (String)(message.obj);
                        b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                        service = INetworkManagementService.Stub.asInterface(b);
                        if (mMyUpstreamIfaceName != null) {
                            try {
                                service.disableNat(mIfaceName, mMyUpstreamIfaceName);
                                mMyUpstreamIfaceName = null;
                            } catch (Exception e) {
                                try {
                                    service.untetherInterface(mIfaceName);
                                } catch (Exception ee) {}
                                setLastErrorAndTransitionToInitialState(
                                        ConnectivityManager.TETHER_ERROR_DISABLE_NAT_ERROR);
                                break;
                            }
                        }
                        if (newUpstreamIfaceName != null) {
                            try {
                                service.enableNat(mIfaceName, newUpstreamIfaceName);
                            } catch (Exception e) {
                                try {
                                    service.untetherInterface(mIfaceName);
                                } catch (Exception ee) {}
                                setLastError(ConnectivityManager.TETHER_ERROR_ENABLE_NAT_ERROR);
                                transitionTo(mInitialState);
                                return true;
                            }
                        }
                        mMyUpstreamIfaceName = newUpstreamIfaceName;
                        break;
                    case CMD_CELL_DUN_ERROR:
                    case CMD_IP_FORWARDING_ENABLE_ERROR:
                    case CMD_IP_FORWARDING_DISABLE_ERROR:
                    case CMD_START_TETHERING_ERROR:
                    case CMD_STOP_TETHERING_ERROR:
                    case CMD_SET_DNS_FORWARDERS_ERROR:
                        error = true;
                    case CMD_TETHER_MODE_DEAD:
                        b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                        service = INetworkManagementService.Stub.asInterface(b);
                        if (mMyUpstreamIfaceName != null) {
                            try {
                                service.disableNat(mIfaceName, mMyUpstreamIfaceName);
                                mMyUpstreamIfaceName = null;
                            } catch (Exception e) {
                                try {
                                    service.untetherInterface(mIfaceName);
                                } catch (Exception ee) {}
                                setLastErrorAndTransitionToInitialState(
                                        ConnectivityManager.TETHER_ERROR_DISABLE_NAT_ERROR);
                                break;
                            }
                        }
                        try {
                            service.untetherInterface(mIfaceName);
                        } catch (Exception e) {
                            setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_UNTETHER_IFACE_ERROR);
                            break;
                        }
                        if (error) {
                            setLastErrorAndTransitionToInitialState(
                                    ConnectivityManager.TETHER_ERROR_MASTER_ERROR);
                            break;
                        }
                        Log.d(TAG, "Tether lost upstream connection " + mIfaceName);
                        sendTetherStateChangedBroadcast();
                        if (mUsb) {
                            if (!Tethering.this.configureUsbIface(false)) {
                                setLastError(ConnectivityManager.TETHER_ERROR_IFACE_CFG_ERROR);
                            }
                        }
                        transitionTo(mInitialState);
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }
        class UnavailableState extends HierarchicalState {
            @Override
            public void enter() {
                setAvailable(false);
                setLastError(ConnectivityManager.TETHER_ERROR_NO_ERROR);
                setTethered(false);
                sendTetherStateChangedBroadcast();
            }
            @Override
            public boolean processMessage(Message message) {
                boolean retValue = true;
                switch (message.what) {
                    case CMD_INTERFACE_UP:
                        transitionTo(mInitialState);
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }
        void setLastErrorAndTransitionToInitialState(int error) {
            setLastError(error);
            transitionTo(mInitialState);
        }
    }
    class TetherMasterSM extends HierarchicalStateMachine {
        static final int CMD_TETHER_MODE_REQUESTED   = 1;
        static final int CMD_TETHER_MODE_UNREQUESTED = 2;
        static final int CMD_UPSTREAM_CHANGED        = 3;
        static final int CMD_CELL_CONNECTION_RENEW   = 4;
        static final int CMD_RETRY_UPSTREAM          = 5;
        private int mSequenceNumber;
        private HierarchicalState mInitialState;
        private HierarchicalState mTetherModeAliveState;
        private HierarchicalState mSetIpForwardingEnabledErrorState;
        private HierarchicalState mSetIpForwardingDisabledErrorState;
        private HierarchicalState mStartTetheringErrorState;
        private HierarchicalState mStopTetheringErrorState;
        private HierarchicalState mSetDnsForwardersErrorState;
        private ArrayList mNotifyList;
        private boolean mConnectionRequested = false;
        private String mUpstreamIfaceName = null;
        private static final int UPSTREAM_SETTLE_TIME_MS     = 10000;
        private static final int CELL_CONNECTION_RENEW_MS    = 40000;
        TetherMasterSM(String name, Looper looper) {
            super(name, looper);
            mInitialState = new InitialState();
            addState(mInitialState);
            mTetherModeAliveState = new TetherModeAliveState();
            addState(mTetherModeAliveState);
            mSetIpForwardingEnabledErrorState = new SetIpForwardingEnabledErrorState();
            addState(mSetIpForwardingEnabledErrorState);
            mSetIpForwardingDisabledErrorState = new SetIpForwardingDisabledErrorState();
            addState(mSetIpForwardingDisabledErrorState);
            mStartTetheringErrorState = new StartTetheringErrorState();
            addState(mStartTetheringErrorState);
            mStopTetheringErrorState = new StopTetheringErrorState();
            addState(mStopTetheringErrorState);
            mSetDnsForwardersErrorState = new SetDnsForwardersErrorState();
            addState(mSetDnsForwardersErrorState);
            mNotifyList = new ArrayList();
            setInitialState(mInitialState);
        }
        class TetherMasterUtilState extends HierarchicalState {
            protected final static boolean TRY_TO_SETUP_MOBILE_CONNECTION = true;
            protected final static boolean WAIT_FOR_NETWORK_TO_SETTLE     = false;
            @Override
            public boolean processMessage(Message m) {
                return false;
            }
            protected int turnOnMobileConnection() {
                IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
                IConnectivityManager service = IConnectivityManager.Stub.asInterface(b);
                int retValue = Phone.APN_REQUEST_FAILED;
                try {
                    retValue = service.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE,
                            (mDunRequired ? Phone.FEATURE_ENABLE_DUN : Phone.FEATURE_ENABLE_HIPRI),
                            new Binder());
                } catch (Exception e) {
                }
                switch (retValue) {
                case Phone.APN_ALREADY_ACTIVE:
                case Phone.APN_REQUEST_STARTED:
                    sendMessageDelayed(CMD_CELL_CONNECTION_RENEW, CELL_CONNECTION_RENEW_MS);
                    mConnectionRequested = true;
                    break;
                case Phone.APN_REQUEST_FAILED:
                default:
                    mConnectionRequested = false;
                    break;
                }
                return retValue;
            }
            protected boolean turnOffMobileConnection() {
                if (mConnectionRequested) {
                    IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
                    IConnectivityManager service =
                            IConnectivityManager.Stub.asInterface(b);
                    try {
                        service.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE,
                                (mDunRequired? Phone.FEATURE_ENABLE_DUN :
                                             Phone.FEATURE_ENABLE_HIPRI));
                    } catch (Exception e) {
                        return false;
                    }
                    mConnectionRequested = false;
                }
                return true;
            }
            protected boolean turnOnMasterTetherSettings() {
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service =
                        INetworkManagementService.Stub.asInterface(b);
                try {
                    service.setIpForwardingEnabled(true);
                } catch (Exception e) {
                    transitionTo(mSetIpForwardingEnabledErrorState);
                    return false;
                }
                try {
                    service.startTethering(mDhcpRange);
                } catch (Exception e) {
                    transitionTo(mStartTetheringErrorState);
                    return false;
                }
                try {
                    service.setDnsForwarders(mDnsServers);
                } catch (Exception e) {
                    transitionTo(mSetDnsForwardersErrorState);
                    return false;
                }
                return true;
            }
            protected boolean turnOffMasterTetherSettings() {
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service =
                        INetworkManagementService.Stub.asInterface(b);
                try {
                    service.stopTethering();
                } catch (Exception e) {
                    transitionTo(mStopTetheringErrorState);
                    return false;
                }
                try {
                    service.setIpForwardingEnabled(false);
                } catch (Exception e) {
                    transitionTo(mSetIpForwardingDisabledErrorState);
                    return false;
                }
                transitionTo(mInitialState);
                return true;
            }
            protected String findActiveUpstreamIface() {
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
                String[] ifaces = new String[0];
                try {
                    ifaces = service.listInterfaces();
                } catch (Exception e) {
                    Log.e(TAG, "Error listing Interfaces :" + e);
                    return null;
                }
                for (String iface : ifaces) {
                    for (String regex : mUpstreamIfaceRegexs) {
                        if (iface.matches(regex)) {
                            InterfaceConfiguration ifcg = null;
                            try {
                                ifcg = service.getInterfaceConfig(iface);
                            } catch (Exception e) {
                                Log.e(TAG, "Error getting iface config :" + e);
                                continue;
                            }
                            if (ifcg.interfaceFlags.contains("up")) {
                                return iface;
                            }
                        }
                    }
                }
                return null;
            }
            protected void chooseUpstreamType(boolean tryCell) {
                String iface = findActiveUpstreamIface();
                IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
                IConnectivityManager cm = IConnectivityManager.Stub.asInterface(b);
                mConnectionRequested = false;
                Log.d(TAG, "chooseUpstreamType(" + tryCell + "),  dunRequired ="
                        + mDunRequired + ", iface=" + iface);
                if (iface != null) {
                    try {
                        if (mDunRequired) {
                            NetworkInfo info = cm.getNetworkInfo(
                                    ConnectivityManager.TYPE_MOBILE_DUN);
                            if (info.isConnected()) {
                                Log.d(TAG, "setting dun ifacename =" + iface);
                                turnOnMobileConnection();
                            } else {
                                info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                                if (info.isConnected()) {
                                    iface = null; 
                                }
                            }
                        } else {
                            Log.d(TAG, "checking if hipri brought us this connection");
                            NetworkInfo info = cm.getNetworkInfo(
                                    ConnectivityManager.TYPE_MOBILE_HIPRI);
                            if (info.isConnected()) {
                                Log.d(TAG, "yes - hipri in use");
                                turnOnMobileConnection();
                            }
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException calling ConnectivityManager " + e);
                        iface = null;
                    }
                }
                if (iface == null ) {
                    if (tryCell == TRY_TO_SETUP_MOBILE_CONNECTION) {
                        turnOnMobileConnection();
                    }
                    sendMessageDelayed(CMD_RETRY_UPSTREAM, UPSTREAM_SETTLE_TIME_MS);
                }
                notifyTetheredOfNewUpstreamIface(iface);
            }
            protected void notifyTetheredOfNewUpstreamIface(String ifaceName) {
                Log.d(TAG, "notifying tethered with iface =" + ifaceName);
                mUpstreamIfaceName = ifaceName;
                for (Object o : mNotifyList) {
                    TetherInterfaceSM sm = (TetherInterfaceSM)o;
                    sm.sendMessage(TetherInterfaceSM.CMD_TETHER_CONNECTION_CHANGED,
                            ifaceName);
                }
            }
        }
        class InitialState extends TetherMasterUtilState {
            @Override
            public void enter() {
                mConnectionRequested = false;
            }
            @Override
            public boolean processMessage(Message message) {
                Log.d(TAG, "MasterInitialState.processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        mDunRequired = isDunRequired();
                        TetherInterfaceSM who = (TetherInterfaceSM)message.obj;
                        Log.d(TAG, "Tether Mode requested by " + who.toString());
                        mNotifyList.add(who);
                        transitionTo(mTetherModeAliveState);
                        break;
                    case CMD_TETHER_MODE_UNREQUESTED:
                        who = (TetherInterfaceSM)message.obj;
                        Log.d(TAG, "Tether Mode unrequested by " + who.toString());
                        int index = mNotifyList.indexOf(who);
                        if (index != -1) {
                            mNotifyList.remove(who);
                        }
                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }
        class TetherModeAliveState extends TetherMasterUtilState {
            boolean mTryCell = WAIT_FOR_NETWORK_TO_SETTLE;
            @Override
            public void enter() {
                mTryCell = WAIT_FOR_NETWORK_TO_SETTLE;  
                chooseUpstreamType(mTryCell);
                mTryCell = !mTryCell;
                turnOnMasterTetherSettings(); 
            }
            @Override
            public void exit() {
                turnOffMobileConnection();
                notifyTetheredOfNewUpstreamIface(null);
            }
            @Override
            public boolean processMessage(Message message) {
                Log.d(TAG, "TetherModeAliveState.processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        TetherInterfaceSM who = (TetherInterfaceSM)message.obj;
                        mNotifyList.add(who);
                        who.sendMessage(TetherInterfaceSM.CMD_TETHER_CONNECTION_CHANGED,
                                mUpstreamIfaceName);
                        break;
                    case CMD_TETHER_MODE_UNREQUESTED:
                        who = (TetherInterfaceSM)message.obj;
                        int index = mNotifyList.indexOf(who);
                        if (index != -1) {
                            mNotifyList.remove(index);
                            if (mNotifyList.isEmpty()) {
                                turnOffMasterTetherSettings(); 
                            }
                        }
                        break;
                    case CMD_UPSTREAM_CHANGED:
                        mTryCell = WAIT_FOR_NETWORK_TO_SETTLE;
                        chooseUpstreamType(mTryCell);
                        mTryCell = !mTryCell;
                        break;
                    case CMD_CELL_CONNECTION_RENEW:
                        if (mConnectionRequested) {
                            Log.d(TAG, "renewing mobile connection - requeuing for another " +
                                    CELL_CONNECTION_RENEW_MS + "ms");
                            turnOnMobileConnection();
                        }
                        break;
                   case CMD_RETRY_UPSTREAM:
                       chooseUpstreamType(mTryCell);
                       mTryCell = !mTryCell;
                       break;
                   default:
                       retValue = false;
                       break;
                }
                return retValue;
            }
        }
        class ErrorState extends HierarchicalState {
            int mErrorNotification;
            @Override
            public boolean processMessage(Message message) {
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        TetherInterfaceSM who = (TetherInterfaceSM)message.obj;
                        who.sendMessage(mErrorNotification);
                        break;
                    default:
                       retValue = false;
                }
                return retValue;
            }
            void notify(int msgType) {
                mErrorNotification = msgType;
                for (Object o : mNotifyList) {
                    TetherInterfaceSM sm = (TetherInterfaceSM)o;
                    sm.sendMessage(msgType);
                }
            }
        }
        class SetIpForwardingEnabledErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "Error in setIpForwardingEnabled");
                notify(TetherInterfaceSM.CMD_IP_FORWARDING_ENABLE_ERROR);
            }
        }
        class SetIpForwardingDisabledErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "Error in setIpForwardingDisabled");
                notify(TetherInterfaceSM.CMD_IP_FORWARDING_DISABLE_ERROR);
            }
        }
        class StartTetheringErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "Error in startTethering");
                notify(TetherInterfaceSM.CMD_START_TETHERING_ERROR);
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service =
                        INetworkManagementService.Stub.asInterface(b);
                try {
                    service.setIpForwardingEnabled(false);
                } catch (Exception e) {}
            }
        }
        class StopTetheringErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "Error in stopTethering");
                notify(TetherInterfaceSM.CMD_STOP_TETHERING_ERROR);
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service =
                         INetworkManagementService.Stub.asInterface(b);
                try {
                    service.setIpForwardingEnabled(false);
                } catch (Exception e) {}
            }
        }
        class SetDnsForwardersErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "Error in setDnsForwarders");
                notify(TetherInterfaceSM.CMD_SET_DNS_FORWARDERS_ERROR);
                IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
                INetworkManagementService service =
                        INetworkManagementService.Stub.asInterface(b);
                try {
                    service.stopTethering();
                } catch (Exception e) {}
                try {
                    service.setIpForwardingEnabled(false);
                } catch (Exception e) {}
            }
        }
    }
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(
                android.Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump ConnectivityService.Tether " +
                    "from from pid=" + Binder.getCallingPid() + ", uid=" +
                    Binder.getCallingUid());
                    return;
        }
        pw.println();
        pw.println("Tether state:");
        synchronized (mIfaces) {
            for (Object o : mIfaces.values()) {
                pw.println(" "+o.toString());
            }
        }
        pw.println();
        return;
    }
}
