public class PowerUsageSummary extends PreferenceActivity implements Runnable {
    private static final boolean DEBUG = false;
    private static final String TAG = "PowerUsageSummary";
    private static final int MENU_STATS_TYPE = Menu.FIRST;
    private static final int MENU_STATS_REFRESH = Menu.FIRST + 1;
    IBatteryStats mBatteryInfo;
    BatteryStatsImpl mStats;
    private List<BatterySipper> mUsageList = new ArrayList<BatterySipper>();
    private PreferenceGroup mAppListGroup;
    private int mStatsType = BatteryStats.STATS_UNPLUGGED;
    private static final int MIN_POWER_THRESHOLD = 5;
    private static final int MAX_ITEMS_TO_LIST = 10;
    private long mStatsPeriod = 0;
    private double mMaxPower = 1;
    private double mTotalPower;
    private PowerProfile mPowerProfile;
    private HashMap<String,UidToDetail> mUidCache = new HashMap<String,UidToDetail>();
    private ArrayList<BatterySipper> mRequestQueue = new ArrayList<BatterySipper>();
    private Thread mRequestThread;
    private boolean mAbort;
    static class UidToDetail {
        String name;
        String packageName;
        Drawable icon;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.power_usage_summary);
        mBatteryInfo = IBatteryStats.Stub.asInterface(
                ServiceManager.getService("batteryinfo"));
        mAppListGroup = (PreferenceGroup) findPreference("app_list");
        mPowerProfile = new PowerProfile(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mAbort = false;
        refreshStats();
    }
    @Override
    protected void onPause() {
        synchronized (mRequestQueue) {
            mAbort = true;
        }
        mHandler.removeMessages(MSG_UPDATE_NAME_ICON);
        super.onPause();
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        PowerGaugePreference pgp = (PowerGaugePreference) preference;
        BatterySipper sipper = pgp.getInfo();
        Intent intent = new Intent(this, PowerUsageDetail.class);
        intent.putExtra(PowerUsageDetail.EXTRA_TITLE, sipper.name);
        intent.putExtra(PowerUsageDetail.EXTRA_PERCENT, (int)
                Math.ceil(sipper.getSortValue() * 100 / mTotalPower));
        intent.putExtra(PowerUsageDetail.EXTRA_GAUGE, (int)
                Math.ceil(sipper.getSortValue() * 100 / mMaxPower));
        intent.putExtra(PowerUsageDetail.EXTRA_USAGE_DURATION, mStatsPeriod);
        intent.putExtra(PowerUsageDetail.EXTRA_ICON_PACKAGE, sipper.defaultPackageName);
        intent.putExtra(PowerUsageDetail.EXTRA_ICON_ID, sipper.iconId);
        intent.putExtra(PowerUsageDetail.EXTRA_NO_COVERAGE, sipper.noCoveragePercent);
        if (sipper.uidObj != null) {
            intent.putExtra(PowerUsageDetail.EXTRA_UID, sipper.uidObj.getUid());
        }
        intent.putExtra(PowerUsageDetail.EXTRA_DRAIN_TYPE, sipper.drainType);
        int[] types;
        double[] values;
        switch (sipper.drainType) {
            case APP:
            {
                Uid uid = sipper.uidObj;
                types = new int[] {
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_gps,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_recv,
                    R.string.usage_type_audio,
                    R.string.usage_type_video,
                };
                values = new double[] {
                    sipper.cpuTime,
                    sipper.cpuFgTime,
                    sipper.gpsTime,
                    uid != null? uid.getTcpBytesSent(mStatsType) : 0,
                    uid != null? uid.getTcpBytesReceived(mStatsType) : 0,
                    0,
                    0
                };
                Writer result = new StringWriter();
                PrintWriter printWriter = new PrintWriter(result);
                mStats.dumpLocked(printWriter, "", mStatsType, uid.getUid());
                intent.putExtra(PowerUsageDetail.EXTRA_REPORT_DETAILS, result.toString());
                result = new StringWriter();
                printWriter = new PrintWriter(result);
                mStats.dumpCheckinLocked(printWriter, mStatsType, uid.getUid());
                intent.putExtra(PowerUsageDetail.EXTRA_REPORT_CHECKIN_DETAILS, result.toString());
            }
            break;
            case CELL:
            {
                types = new int[] {
                    R.string.usage_type_on_time,
                    R.string.usage_type_no_coverage
                };
                values = new double[] {
                    sipper.usageTime,
                    sipper.noCoveragePercent
                };
            }
            break;
            default:
            {
                types = new int[] {
                    R.string.usage_type_on_time
                };
                values = new double[] {
                    sipper.usageTime
                };
            }
        }
        intent.putExtra(PowerUsageDetail.EXTRA_DETAIL_TYPES, types);
        intent.putExtra(PowerUsageDetail.EXTRA_DETAIL_VALUES, values);
        startActivity(intent);
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) {
            menu.add(0, MENU_STATS_TYPE, 0, R.string.menu_stats_total)
                    .setIcon(com.android.internal.R.drawable.ic_menu_info_details)
                    .setAlphabeticShortcut('t');
        }
        menu.add(0, MENU_STATS_REFRESH, 0, R.string.menu_stats_refresh)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setAlphabeticShortcut('r');
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (DEBUG) {
            menu.findItem(MENU_STATS_TYPE).setTitle(mStatsType == BatteryStats.STATS_TOTAL
                    ? R.string.menu_stats_unplugged
                    : R.string.menu_stats_total);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STATS_TYPE:
                if (mStatsType == BatteryStats.STATS_TOTAL) {
                    mStatsType = BatteryStats.STATS_UNPLUGGED;
                } else {
                    mStatsType = BatteryStats.STATS_TOTAL;
                }
                refreshStats();
                return true;
            case MENU_STATS_REFRESH:
                mStats = null;
                refreshStats();
                return true;
            default:
                return false;
        }
    }
    private void refreshStats() {
        if (mStats == null) {
            load();
        }
        mMaxPower = 0;
        mTotalPower = 0;
        mAppListGroup.removeAll();
        mUsageList.clear();
        processAppUsage();
        processMiscUsage();
        mAppListGroup.setOrderingAsAdded(false);
        Collections.sort(mUsageList);
        for (BatterySipper sipper : mUsageList) {
            if (sipper.getSortValue() < MIN_POWER_THRESHOLD) continue;
            final double percentOfTotal =  ((sipper.getSortValue() / mTotalPower) * 100);
            if (percentOfTotal < 1) continue;
            PowerGaugePreference pref = new PowerGaugePreference(this, sipper.getIcon(), sipper);
            double percentOfMax = (sipper.getSortValue() * 100) / mMaxPower;
            sipper.percent = percentOfTotal;
            pref.setTitle(sipper.name);
            pref.setPercent(percentOfTotal);
            pref.setOrder(Integer.MAX_VALUE - (int) sipper.getSortValue()); 
            pref.setGaugeValue(percentOfMax);
            if (sipper.uidObj != null) {
                pref.setKey(Integer.toString(sipper.uidObj.getUid()));
            }
            mAppListGroup.addPreference(pref);
            if (mAppListGroup.getPreferenceCount() > MAX_ITEMS_TO_LIST) break;
        }
        if (DEBUG) setTitle("Battery total uAh = " + ((mTotalPower * 1000) / 3600));
        synchronized (mRequestQueue) {
            if (!mRequestQueue.isEmpty()) {
                if (mRequestThread == null) {
                    mRequestThread = new Thread(this, "BatteryUsage Icon Loader");
                    mRequestThread.setPriority(Thread.MIN_PRIORITY);
                    mRequestThread.start();
                }
                mRequestQueue.notify();
            }
        }
    }
    private void updateStatsPeriod(long duration) {
        String durationString = Utils.formatElapsedTime(this, duration / 1000);
        String label = getString(mStats.isOnBattery()
                ? R.string.battery_stats_duration
                : R.string.battery_stats_last_duration, durationString);
        setTitle(label);
    }
    private void processAppUsage() {
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        final int which = mStatsType;
        final int speedSteps = mPowerProfile.getNumSpeedSteps();
        final double[] powerCpuNormal = new double[speedSteps];
        final long[] cpuSpeedStepTimes = new long[speedSteps];
        for (int p = 0; p < speedSteps; p++) {
            powerCpuNormal[p] = mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p);
        }
        final double averageCostPerByte = getAverageDataCost();
        long uSecTime = mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, which);
        mStatsPeriod = uSecTime;
        updateStatsPeriod(uSecTime);
        SparseArray<? extends Uid> uidStats = mStats.getUidStats();
        final int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            Uid u = uidStats.valueAt(iu);
            double power = 0;
            double highestDrain = 0;
            String packageWithHighestDrain = null;
            Map<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
            long cpuTime = 0;
            long cpuFgTime = 0;
            long gpsTime = 0;
            if (processStats.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent
                        : processStats.entrySet()) {
                    if (DEBUG) Log.i(TAG, "Process name = " + ent.getKey());
                    Uid.Proc ps = ent.getValue();
                    final long userTime = ps.getUserTime(which);
                    final long systemTime = ps.getSystemTime(which);
                    final long foregroundTime = ps.getForegroundTime(which);
                    cpuFgTime += foregroundTime * 10; 
                    final long tmpCpuTime = (userTime + systemTime) * 10; 
                    int totalTimeAtSpeeds = 0;
                    for (int step = 0; step < speedSteps; step++) {
                        cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(step, which);
                        totalTimeAtSpeeds += cpuSpeedStepTimes[step];
                    }
                    if (totalTimeAtSpeeds == 0) totalTimeAtSpeeds = 1;
                    double processPower = 0;
                    for (int step = 0; step < speedSteps; step++) {
                        double ratio = (double) cpuSpeedStepTimes[step] / totalTimeAtSpeeds;
                        processPower += ratio * tmpCpuTime * powerCpuNormal[step];
                    }
                    cpuTime += tmpCpuTime;
                    power += processPower;
                    if (highestDrain < processPower) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    }
                }
                if (DEBUG) Log.i(TAG, "Max drain of " + highestDrain 
                        + " by " + packageWithHighestDrain);
            }
            if (cpuFgTime > cpuTime) {
                if (DEBUG && cpuFgTime > cpuTime + 10000) {
                    Log.i(TAG, "WARNING! Cputime is more than 10 seconds behind Foreground time");
                }
                cpuTime = cpuFgTime; 
            }
            power /= 1000;
            power += (u.getTcpBytesReceived(mStatsType) + u.getTcpBytesSent(mStatsType))
                    * averageCostPerByte;
            Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats = u.getSensorStats();
            for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry
                    : sensorStats.entrySet()) {
                Uid.Sensor sensor = sensorEntry.getValue();
                int sensorType = sensor.getHandle();
                BatteryStats.Timer timer = sensor.getSensorTime();
                long sensorTime = timer.getTotalTimeLocked(uSecTime, which) / 1000;
                double multiplier = 0;
                switch (sensorType) {
                    case Uid.Sensor.GPS:
                        multiplier = mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_ON);
                        gpsTime = sensorTime;
                        break;
                    default:
                        android.hardware.Sensor sensorData =
                                sensorManager.getDefaultSensor(sensorType);
                        if (sensorData != null) {
                            multiplier = sensorData.getPower();
                            if (DEBUG) {
                                Log.i(TAG, "Got sensor " + sensorData.getName() + " with power = "
                                        + multiplier);
                            }
                        }
                }
                power += (multiplier * sensorTime) / 1000;
            }
            if (power != 0) {
                BatterySipper app = new BatterySipper(packageWithHighestDrain, DrainType.APP, 0, u,
                        new double[] {power});
                app.cpuTime = cpuTime;
                app.gpsTime = gpsTime;
                app.cpuFgTime = cpuFgTime;
                mUsageList.add(app);
            }
            if (power > mMaxPower) mMaxPower = power;
            mTotalPower += power;
            if (DEBUG) Log.i(TAG, "Added power = " + power);
        }
    }
    private void addPhoneUsage(long uSecNow) {
        long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
        double phoneOnPower = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
                * phoneOnTimeMs / 1000;
        addEntry(getString(R.string.power_phone), DrainType.PHONE, phoneOnTimeMs,
                R.drawable.ic_settings_voice_calls, phoneOnPower);
    }
    private void addScreenUsage(long uSecNow) {
        double power = 0;
        long screenOnTimeMs = mStats.getScreenOnTime(uSecNow, mStatsType) / 1000;
        power += screenOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
        final double screenFullPower =
                mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
        for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            double screenBinPower = screenFullPower * (i + 0.5f)
                    / BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
            long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow, mStatsType) / 1000;
            power += screenBinPower * brightnessTime;
            if (DEBUG) {
                Log.i(TAG, "Screen bin power = " + (int) screenBinPower + ", time = "
                        + brightnessTime);
            }
        }
        power /= 1000; 
        addEntry(getString(R.string.power_screen), DrainType.SCREEN, screenOnTimeMs,
                R.drawable.ic_settings_display, power);
    }
    private void addRadioUsage(long uSecNow) {
        double power = 0;
        final int BINS = BatteryStats.NUM_SIGNAL_STRENGTH_BINS;
        long signalTimeMs = 0;
        for (int i = 0; i < BINS; i++) {
            long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow, mStatsType) / 1000;
            power += strengthTimeMs / 1000
                    * mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i);
            signalTimeMs += strengthTimeMs;
        }
        long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow, mStatsType) / 1000;
        power += scanningTimeMs / 1000 * mPowerProfile.getAveragePower(
                PowerProfile.POWER_RADIO_SCANNING);
        BatterySipper bs =
                addEntry(getString(R.string.power_cell), DrainType.CELL, signalTimeMs,
                R.drawable.ic_settings_cell_standby, power);
        if (signalTimeMs != 0) {
            bs.noCoveragePercent = mStats.getPhoneSignalStrengthTime(0, uSecNow, mStatsType)
                    / 1000 * 100.0 / signalTimeMs;
        }
    }
    private void addWiFiUsage(long uSecNow) {
        long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
        long runningTimeMs = mStats.getWifiRunningTime(uSecNow, mStatsType) / 1000;
        double wifiPower = (onTimeMs * 0 
                * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)
            + runningTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
        addEntry(getString(R.string.power_wifi), DrainType.WIFI, runningTimeMs,
                R.drawable.ic_settings_wifi, wifiPower);
    }
    private void addIdleUsage(long uSecNow) {
        long idleTimeMs = (uSecNow - mStats.getScreenOnTime(uSecNow, mStatsType)) / 1000;
        double idlePower = (idleTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE))
                / 1000;
        addEntry(getString(R.string.power_idle), DrainType.IDLE, idleTimeMs,
                R.drawable.ic_settings_phone_idle, idlePower);
    }
    private void addBluetoothUsage(long uSecNow) {
        long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
        double btPower = btOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)
                / 1000;
        int btPingCount = mStats.getBluetoothPingCount();
        btPower += (btPingCount
                * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 1000;
        addEntry(getString(R.string.power_bluetooth), DrainType.BLUETOOTH, btOnTimeMs,
                R.drawable.ic_settings_bluetooth, btPower);
    }
    private double getAverageDataCost() {
        final long WIFI_BPS = 1000000; 
        final long MOBILE_BPS = 200000; 
        final double WIFI_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE)
                / 3600;
        final double MOBILE_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
                / 3600;
        final long mobileData = mStats.getMobileTcpBytesReceived(mStatsType) +
                mStats.getMobileTcpBytesSent(mStatsType);
        final long wifiData = mStats.getTotalTcpBytesReceived(mStatsType) +
                mStats.getTotalTcpBytesSent(mStatsType) - mobileData;
        final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
        final long mobileBps = radioDataUptimeMs != 0
                ? mobileData * 8 * 1000 / radioDataUptimeMs
                : MOBILE_BPS;
        double mobileCostPerByte = MOBILE_POWER / (mobileBps / 8);
        double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);
        if (wifiData + mobileData != 0) {
            return (mobileCostPerByte * mobileData + wifiCostPerByte * wifiData)
                    / (mobileData + wifiData);
        } else {
            return 0;
        }
    }
    private void processMiscUsage() {
        final int which = mStatsType;
        long uSecTime = SystemClock.elapsedRealtime() * 1000;
        final long uSecNow = mStats.computeBatteryRealtime(uSecTime, which);
        final long timeSinceUnplugged = uSecNow;
        if (DEBUG) {
            Log.i(TAG, "Uptime since last unplugged = " + (timeSinceUnplugged / 1000));
        }
        addPhoneUsage(uSecNow);
        addScreenUsage(uSecNow);
        addWiFiUsage(uSecNow);
        addBluetoothUsage(uSecNow);
        addIdleUsage(uSecNow); 
        addRadioUsage(uSecNow);
    }
    private BatterySipper addEntry(String label, DrainType drainType, long time, int iconId,
            double power) {
        if (power > mMaxPower) mMaxPower = power;
        mTotalPower += power;
        BatterySipper bs = new BatterySipper(label, drainType, iconId, null, new double[] {power});
        bs.usageTime = time;
        bs.iconId = iconId;
        mUsageList.add(bs);
        return bs;
    }
    private void load() {
        try {
            byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
                    .createFromParcel(parcel);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }
    }
    class BatterySipper implements Comparable<BatterySipper> {
        String name;
        Drawable icon;
        int iconId; 
        Uid uidObj;
        double value;
        double[] values;
        DrainType drainType;
        long usageTime;
        long cpuTime;
        long gpsTime;
        long cpuFgTime;
        double percent;
        double noCoveragePercent;
        String defaultPackageName;
        BatterySipper(String label, DrainType drainType, int iconId, Uid uid, double[] values) {
            this.values = values;
            name = label;
            this.drainType = drainType;
            if (iconId > 0) {
                icon = getResources().getDrawable(iconId);
            }
            if (values != null) value = values[0];
            if ((label == null || iconId == 0) && uid != null) {
                getQuickNameIconForUid(uid);
            }
            uidObj = uid;
        }
        double getSortValue() {
            return value;
        }
        double[] getValues() {
            return values;
        }
        Drawable getIcon() {
            return icon;
        }
        public int compareTo(BatterySipper other) {
            return (int) (other.getSortValue() - getSortValue());
        }
        void getQuickNameIconForUid(Uid uidObj) {
            final int uid = uidObj.getUid();
            final String uidString = Integer.toString(uid);
            if (mUidCache.containsKey(uidString)) {
                UidToDetail utd = mUidCache.get(uidString);
                defaultPackageName = utd.packageName;
                name = utd.name;
                icon = utd.icon;
                return;
            }
            PackageManager pm = getPackageManager();
            final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
            String[] packages = pm.getPackagesForUid(uid);
            icon = pm.getDefaultActivityIcon();
            if (packages == null) {
                if (uid == 0) {
                    name = getResources().getString(R.string.process_kernel_label);
                } else if ("mediaserver".equals(name)) {
                    name = getResources().getString(R.string.process_mediaserver_label);
                }
                iconId = R.drawable.ic_power_system;
                icon = getResources().getDrawable(iconId);
                return;
            } else {
            }
            synchronized (mRequestQueue) {
                mRequestQueue.add(this);
            }
        }
        void getNameIcon() {
            PackageManager pm = getPackageManager();
            final int uid = uidObj.getUid();
            final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
            String[] packages = pm.getPackagesForUid(uid);
            if (packages == null) {
                name = Integer.toString(uid);
                return;
            }
            String[] packageLabels = new String[packages.length];
            System.arraycopy(packages, 0, packageLabels, 0, packages.length);
            int preferredIndex = -1;
            for (int i = 0; i < packageLabels.length; i++) {
                if (packageLabels[i].equals(name)) preferredIndex = i;
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(packageLabels[i], 0);
                    CharSequence label = ai.loadLabel(pm);
                    if (label != null) {
                        packageLabels[i] = label.toString();
                    }
                    if (ai.icon != 0) {
                        defaultPackageName = packages[i];
                        icon = ai.loadIcon(pm);
                        break;
                    }
                } catch (NameNotFoundException e) {
                }
            }
            if (icon == null) icon = defaultActivityIcon;
            if (packageLabels.length == 1) {
                name = packageLabels[0];
            } else {
                for (String pkgName : packages) {
                    try {
                        final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                        if (pi.sharedUserLabel != 0) {
                            final CharSequence nm = pm.getText(pkgName,
                                    pi.sharedUserLabel, pi.applicationInfo);
                            if (nm != null) {
                                name = nm.toString();
                                if (pi.applicationInfo.icon != 0) {
                                    defaultPackageName = pkgName;
                                    icon = pi.applicationInfo.loadIcon(pm);
                                }
                                break;
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }
            }
            final String uidString = Integer.toString(uidObj.getUid());
            UidToDetail utd = new UidToDetail();
            utd.name = name;
            utd.icon = icon;
            utd.packageName = defaultPackageName;
            mUidCache.put(uidString, utd);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_NAME_ICON, this));
        }
    }
    public void run() {
        while (true) {
            BatterySipper bs;
            synchronized (mRequestQueue) {
                if (mRequestQueue.isEmpty() || mAbort) {
                    mRequestThread = null;
                    return;
                }
                bs = mRequestQueue.remove(0);
            }
            bs.getNameIcon();
        }
    }
    private static final int MSG_UPDATE_NAME_ICON = 1;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_NAME_ICON:
                    BatterySipper bs = (BatterySipper) msg.obj;
                    PowerGaugePreference pgp = 
                            (PowerGaugePreference) findPreference(
                                    Integer.toString(bs.uidObj.getUid()));
                    if (pgp != null) {
                        pgp.setIcon(bs.icon);
                        pgp.setPercent(bs.percent);
                        pgp.setTitle(bs.name);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
