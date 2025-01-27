public class BluetoothA2dpService extends IBluetoothA2dp.Stub {
    private static final String TAG = "BluetoothA2dpService";
    private static final boolean DBG = true;
    public static final String BLUETOOTH_A2DP_SERVICE = "bluetooth_a2dp";
    private static final String BLUETOOTH_ADMIN_PERM = android.Manifest.permission.BLUETOOTH_ADMIN;
    private static final String BLUETOOTH_PERM = android.Manifest.permission.BLUETOOTH;
    private static final String BLUETOOTH_ENABLED = "bluetooth_enabled";
    private static final int MESSAGE_CONNECT_TO = 1;
    private static final String PROPERTY_STATE = "State";
    private static final String SINK_STATE_DISCONNECTED = "disconnected";
    private static final String SINK_STATE_CONNECTING = "connecting";
    private static final String SINK_STATE_CONNECTED = "connected";
    private static final String SINK_STATE_PLAYING = "playing";
    private static int mSinkCount;
    private final Context mContext;
    private final IntentFilter mIntentFilter;
    private HashMap<BluetoothDevice, Integer> mAudioDevices;
    private final AudioManager mAudioManager;
    private final BluetoothService mBluetoothService;
    private final BluetoothAdapter mAdapter;
    private int   mTargetA2dpState;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                               BluetoothAdapter.ERROR);
                switch (state) {
                case BluetoothAdapter.STATE_ON:
                    onBluetoothEnable();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    onBluetoothDisable();
                    break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                                   BluetoothDevice.ERROR);
                switch(bondState) {
                case BluetoothDevice.BOND_BONDED:
                    if (getSinkPriority(device) == BluetoothA2dp.PRIORITY_UNDEFINED) {
                        setSinkPriority(device, BluetoothA2dp.PRIORITY_ON);
                    }
                    break;
                case BluetoothDevice.BOND_NONE:
                    setSinkPriority(device, BluetoothA2dp.PRIORITY_UNDEFINED);
                    break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                if (getSinkPriority(device) == BluetoothA2dp.PRIORITY_AUTO_CONNECT &&
                        isSinkDevice(device)) {
                    Message msg = Message.obtain(mHandler, MESSAGE_CONNECT_TO, device);
                    mHandler.sendMessageDelayed(msg, 6000);
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                synchronized (this) {
                    if (mAudioDevices.containsKey(device)) {
                        int state = mAudioDevices.get(device);
                        handleSinkStateChange(device, state, BluetoothA2dp.STATE_DISCONNECTED);
                    }
                }
            } else if (action.equals(AudioManager.VOLUME_CHANGED_ACTION)) {
                int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
                if (streamType == AudioManager.STREAM_MUSIC) {
                    BluetoothDevice sinks[] = getConnectedSinks();
                    if (sinks.length != 0 && isPhoneDocked(sinks[0])) {
                        String address = sinks[0].getAddress();
                        int newVolLevel =
                          intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, 0);
                        int oldVolLevel =
                          intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, 0);
                        String path = mBluetoothService.getObjectPathFromAddress(address);
                        if (newVolLevel > oldVolLevel) {
                            avrcpVolumeUpNative(path);
                        } else if (newVolLevel < oldVolLevel) {
                            avrcpVolumeDownNative(path);
                        }
                    }
                }
            }
        }
    };
    private boolean isPhoneDocked(BluetoothDevice device) {
        Intent i = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_DOCK_EVENT));
        if (i != null) {
            int state = i.getIntExtra(Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DOCK_STATE_UNDOCKED);
            if (state != Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                BluetoothDevice dockDevice = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (dockDevice != null && device.equals(dockDevice)) {
                    return true;
                }
            }
        }
        return false;
    }
    public BluetoothA2dpService(Context context, BluetoothService bluetoothService) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mBluetoothService = bluetoothService;
        if (mBluetoothService == null) {
            throw new RuntimeException("Platform does not support Bluetooth");
        }
        if (!initNative()) {
            throw new RuntimeException("Could not init BluetoothA2dpService");
        }
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mIntentFilter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mAudioDevices = new HashMap<BluetoothDevice, Integer>();
        if (mBluetoothService.isEnabled())
            onBluetoothEnable();
        mTargetA2dpState = -1;
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            cleanupNative();
        } finally {
            super.finalize();
        }
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_CONNECT_TO:
                BluetoothDevice device = (BluetoothDevice) msg.obj;
                if (mBluetoothService.isEnabled() &&
                        getSinkPriority(device) == BluetoothA2dp.PRIORITY_AUTO_CONNECT &&
                        lookupSinksMatchingStates(new int[] {
                            BluetoothA2dp.STATE_CONNECTING,
                            BluetoothA2dp.STATE_CONNECTED,
                            BluetoothA2dp.STATE_PLAYING,
                            BluetoothA2dp.STATE_DISCONNECTING}).size() == 0) {
                    log("Auto-connecting A2DP to sink " + device);
                    connectSink(device);
                }
                break;
            }
        }
    };
    private int convertBluezSinkStringtoState(String value) {
        if (value.equalsIgnoreCase("disconnected"))
            return BluetoothA2dp.STATE_DISCONNECTED;
        if (value.equalsIgnoreCase("connecting"))
            return BluetoothA2dp.STATE_CONNECTING;
        if (value.equalsIgnoreCase("connected"))
            return BluetoothA2dp.STATE_CONNECTED;
        if (value.equalsIgnoreCase("playing"))
            return BluetoothA2dp.STATE_PLAYING;
        return -1;
    }
    private boolean isSinkDevice(BluetoothDevice device) {
        ParcelUuid[] uuids = mBluetoothService.getRemoteUuids(device.getAddress());
        if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.AudioSink)) {
            return true;
        }
        return false;
    }
    private synchronized boolean addAudioSink (BluetoothDevice device) {
        String path = mBluetoothService.getObjectPathFromAddress(device.getAddress());
        String propValues[] = (String []) getSinkPropertiesNative(path);
        if (propValues == null) {
            Log.e(TAG, "Error while getting AudioSink properties for device: " + device);
            return false;
        }
        Integer state = null;
        for (int i = 0; i < propValues.length; i+=2) {
            if (propValues[i].equals(PROPERTY_STATE)) {
                state = new Integer(convertBluezSinkStringtoState(propValues[i+1]));
                break;
            }
        }
        mAudioDevices.put(device, state);
        handleSinkStateChange(device, BluetoothA2dp.STATE_DISCONNECTED, state);
        return true;
    }
    private synchronized void onBluetoothEnable() {
        String devices = mBluetoothService.getProperty("Devices");
        mSinkCount = 0;
        if (devices != null) {
            String [] paths = devices.split(",");
            for (String path: paths) {
                String address = mBluetoothService.getAddressFromObjectPath(path);
                BluetoothDevice device = mAdapter.getRemoteDevice(address);
                ParcelUuid[] remoteUuids = mBluetoothService.getRemoteUuids(address);
                if (remoteUuids != null)
                    if (BluetoothUuid.containsAnyUuid(remoteUuids,
                            new ParcelUuid[] {BluetoothUuid.AudioSink,
                                                BluetoothUuid.AdvAudioDist})) {
                        addAudioSink(device);
                    }
                }
        }
        mAudioManager.setParameters(BLUETOOTH_ENABLED+"=true");
        mAudioManager.setParameters("A2dpSuspended=false");
    }
    private synchronized void onBluetoothDisable() {
        if (!mAudioDevices.isEmpty()) {
            BluetoothDevice[] devices = new BluetoothDevice[mAudioDevices.size()];
            devices = mAudioDevices.keySet().toArray(devices);
            for (BluetoothDevice device : devices) {
                int state = getSinkState(device);
                switch (state) {
                    case BluetoothA2dp.STATE_CONNECTING:
                    case BluetoothA2dp.STATE_CONNECTED:
                    case BluetoothA2dp.STATE_PLAYING:
                        disconnectSinkNative(mBluetoothService.getObjectPathFromAddress(
                                device.getAddress()));
                        handleSinkStateChange(device, state, BluetoothA2dp.STATE_DISCONNECTED);
                        break;
                    case BluetoothA2dp.STATE_DISCONNECTING:
                        handleSinkStateChange(device, BluetoothA2dp.STATE_DISCONNECTING,
                                              BluetoothA2dp.STATE_DISCONNECTED);
                        break;
                }
            }
            mAudioDevices.clear();
        }
        mAudioManager.setParameters(BLUETOOTH_ENABLED + "=false");
    }
    public synchronized boolean connectSink(BluetoothDevice device) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (DBG) log("connectSink(" + device + ")");
        if (!mBluetoothService.isEnabled()) return false;
        if (lookupSinksMatchingStates(new int[] {
                BluetoothA2dp.STATE_CONNECTING,
                BluetoothA2dp.STATE_CONNECTED,
                BluetoothA2dp.STATE_PLAYING,
                BluetoothA2dp.STATE_DISCONNECTING}).size() != 0) {
            return false;
        }
        if (mAudioDevices.get(device) == null && !addAudioSink(device))
            return false;
        int state = mAudioDevices.get(device);
        switch (state) {
        case BluetoothA2dp.STATE_CONNECTED:
        case BluetoothA2dp.STATE_PLAYING:
        case BluetoothA2dp.STATE_DISCONNECTING:
            return false;
        case BluetoothA2dp.STATE_CONNECTING:
            return true;
        }
        String path = mBluetoothService.getObjectPathFromAddress(device.getAddress());
        if (path == null)
            return false;
        handleSinkStateChange(device, state, BluetoothA2dp.STATE_CONNECTING);
        if (!connectSinkNative(path)) {
            handleSinkStateChange(device, mAudioDevices.get(device), state);
            return false;
        }
        return true;
    }
    public synchronized boolean disconnectSink(BluetoothDevice device) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (DBG) log("disconnectSink(" + device + ")");
        String path = mBluetoothService.getObjectPathFromAddress(device.getAddress());
        if (path == null) {
            return false;
        }
        int state = getSinkState(device);
        switch (state) {
        case BluetoothA2dp.STATE_DISCONNECTED:
            return false;
        case BluetoothA2dp.STATE_DISCONNECTING:
            return true;
        }
        handleSinkStateChange(device, state, BluetoothA2dp.STATE_DISCONNECTING);
        if (!disconnectSinkNative(path)) {
            handleSinkStateChange(device, mAudioDevices.get(device), state);
            return false;
        }
        return true;
    }
    public synchronized boolean suspendSink(BluetoothDevice device) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                            "Need BLUETOOTH_ADMIN permission");
        if (DBG) log("suspendSink(" + device + "), mTargetA2dpState: "+mTargetA2dpState);
        if (device == null || mAudioDevices == null) {
            return false;
        }
        String path = mBluetoothService.getObjectPathFromAddress(device.getAddress());
        Integer state = mAudioDevices.get(device);
        if (path == null || state == null) {
            return false;
        }
        mTargetA2dpState = BluetoothA2dp.STATE_CONNECTED;
        return checkSinkSuspendState(state.intValue());
    }
    public synchronized boolean resumeSink(BluetoothDevice device) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                            "Need BLUETOOTH_ADMIN permission");
        if (DBG) log("resumeSink(" + device + "), mTargetA2dpState: "+mTargetA2dpState);
        if (device == null || mAudioDevices == null) {
            return false;
        }
        String path = mBluetoothService.getObjectPathFromAddress(device.getAddress());
        Integer state = mAudioDevices.get(device);
        if (path == null || state == null) {
            return false;
        }
        mTargetA2dpState = BluetoothA2dp.STATE_PLAYING;
        return checkSinkSuspendState(state.intValue());
    }
    public synchronized BluetoothDevice[] getConnectedSinks() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Set<BluetoothDevice> sinks = lookupSinksMatchingStates(
                new int[] {BluetoothA2dp.STATE_CONNECTED, BluetoothA2dp.STATE_PLAYING});
        return sinks.toArray(new BluetoothDevice[sinks.size()]);
    }
    public synchronized BluetoothDevice[] getNonDisconnectedSinks() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Set<BluetoothDevice> sinks = lookupSinksMatchingStates(
                new int[] {BluetoothA2dp.STATE_CONNECTED,
                           BluetoothA2dp.STATE_PLAYING,
                           BluetoothA2dp.STATE_CONNECTING,
                           BluetoothA2dp.STATE_DISCONNECTING});
        return sinks.toArray(new BluetoothDevice[sinks.size()]);
    }
    public synchronized int getSinkState(BluetoothDevice device) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Integer state = mAudioDevices.get(device);
        if (state == null)
            return BluetoothA2dp.STATE_DISCONNECTED;
        return state;
    }
    public synchronized int getSinkPriority(BluetoothDevice device) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.getBluetoothA2dpSinkPriorityKey(device.getAddress()),
                BluetoothA2dp.PRIORITY_UNDEFINED);
    }
    public synchronized boolean setSinkPriority(BluetoothDevice device, int priority) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return false;
        }
        return Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.getBluetoothA2dpSinkPriorityKey(device.getAddress()), priority);
    }
    private synchronized void onSinkPropertyChanged(String path, String []propValues) {
        if (!mBluetoothService.isEnabled()) {
            return;
        }
        String name = propValues[0];
        String address = mBluetoothService.getAddressFromObjectPath(path);
        if (address == null) {
            Log.e(TAG, "onSinkPropertyChanged: Address of the remote device in null");
            return;
        }
        BluetoothDevice device = mAdapter.getRemoteDevice(address);
        if (name.equals(PROPERTY_STATE)) {
            int state = convertBluezSinkStringtoState(propValues[1]);
            if (mAudioDevices.get(device) == null) {
                addAudioSink(device);
            } else {
                int prevState = mAudioDevices.get(device);
                handleSinkStateChange(device, prevState, state);
            }
        }
    }
    private void handleSinkStateChange(BluetoothDevice device, int prevState, int state) {
        if (state != prevState) {
            if (state == BluetoothA2dp.STATE_DISCONNECTED ||
                    state == BluetoothA2dp.STATE_DISCONNECTING) {
                mSinkCount--;
            } else if (state == BluetoothA2dp.STATE_CONNECTED) {
                mSinkCount ++;
            }
            mAudioDevices.put(device, state);
            checkSinkSuspendState(state);
            mTargetA2dpState = -1;
            if (getSinkPriority(device) > BluetoothA2dp.PRIORITY_OFF &&
                    state == BluetoothA2dp.STATE_CONNECTING ||
                    state == BluetoothA2dp.STATE_CONNECTED) {
                setSinkPriority(device, BluetoothA2dp.PRIORITY_AUTO_CONNECT);
            }
            Intent intent = new Intent(BluetoothA2dp.ACTION_SINK_STATE_CHANGED);
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
            intent.putExtra(BluetoothA2dp.EXTRA_PREVIOUS_SINK_STATE, prevState);
            intent.putExtra(BluetoothA2dp.EXTRA_SINK_STATE, state);
            mContext.sendBroadcast(intent, BLUETOOTH_PERM);
            if (DBG) log("A2DP state : device: " + device + " State:" + prevState + "->" + state);
        }
    }
    private synchronized Set<BluetoothDevice> lookupSinksMatchingStates(int[] states) {
        Set<BluetoothDevice> sinks = new HashSet<BluetoothDevice>();
        if (mAudioDevices.isEmpty()) {
            return sinks;
        }
        for (BluetoothDevice device: mAudioDevices.keySet()) {
            int sinkState = getSinkState(device);
            for (int state : states) {
                if (state == sinkState) {
                    sinks.add(device);
                    break;
                }
            }
        }
        return sinks;
    }
    private boolean checkSinkSuspendState(int state) {
        boolean result = true;
        if (state != mTargetA2dpState) {
            if (state == BluetoothA2dp.STATE_PLAYING &&
                mTargetA2dpState == BluetoothA2dp.STATE_CONNECTED) {
                mAudioManager.setParameters("A2dpSuspended=true");
            } else if (state == BluetoothA2dp.STATE_CONNECTED &&
                mTargetA2dpState == BluetoothA2dp.STATE_PLAYING) {
                mAudioManager.setParameters("A2dpSuspended=false");
            } else {
                result = false;
            }
        }
        return result;
    }
    private void onConnectSinkResult(String deviceObjectPath, boolean result) {
        if (!result) {
            if (deviceObjectPath != null) {
                String address = mBluetoothService.getAddressFromObjectPath(deviceObjectPath);
                BluetoothDevice device = mAdapter.getRemoteDevice(address);
                int state = getSinkState(device);
                handleSinkStateChange(device, state, BluetoothA2dp.STATE_DISCONNECTED);
            }
        }
    }
    @Override
    protected synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mAudioDevices.isEmpty()) return;
        pw.println("Cached audio devices:");
        for (BluetoothDevice device : mAudioDevices.keySet()) {
            int state = mAudioDevices.get(device);
            pw.println(device + " " + BluetoothA2dp.stateToString(state));
        }
    }
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
    private native boolean initNative();
    private native void cleanupNative();
    private synchronized native boolean connectSinkNative(String path);
    private synchronized native boolean disconnectSinkNative(String path);
    private synchronized native boolean suspendSinkNative(String path);
    private synchronized native boolean resumeSinkNative(String path);
    private synchronized native Object []getSinkPropertiesNative(String path);
    private synchronized native boolean avrcpVolumeUpNative(String path);
    private synchronized native boolean avrcpVolumeDownNative(String path);
}
