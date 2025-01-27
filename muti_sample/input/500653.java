public class SyncStatusInfo implements Parcelable {
    static final int VERSION = 2;
    public final int authorityId;
    public long totalElapsedTime;
    public int numSyncs;
    public int numSourcePoll;
    public int numSourceServer;
    public int numSourceLocal;
    public int numSourceUser;
    public int numSourcePeriodic;
    public long lastSuccessTime;
    public int lastSuccessSource;
    public long lastFailureTime;
    public int lastFailureSource;
    public String lastFailureMesg;
    public long initialFailureTime;
    public boolean pending;
    public boolean initialize;
    public ArrayList<Long> periodicSyncTimes;
    private static final String TAG = "Sync";
    SyncStatusInfo(int authorityId) {
        this.authorityId = authorityId;
    }
    public int getLastFailureMesgAsInt(int def) {
        try {
            if (lastFailureMesg != null) {
                return Integer.parseInt(lastFailureMesg);
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, "error parsing lastFailureMesg of " + lastFailureMesg, e);
        }
        return def;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(VERSION);
        parcel.writeInt(authorityId);
        parcel.writeLong(totalElapsedTime);
        parcel.writeInt(numSyncs);
        parcel.writeInt(numSourcePoll);
        parcel.writeInt(numSourceServer);
        parcel.writeInt(numSourceLocal);
        parcel.writeInt(numSourceUser);
        parcel.writeLong(lastSuccessTime);
        parcel.writeInt(lastSuccessSource);
        parcel.writeLong(lastFailureTime);
        parcel.writeInt(lastFailureSource);
        parcel.writeString(lastFailureMesg);
        parcel.writeLong(initialFailureTime);
        parcel.writeInt(pending ? 1 : 0);
        parcel.writeInt(initialize ? 1 : 0);
        if (periodicSyncTimes != null) {
            parcel.writeInt(periodicSyncTimes.size());
            for (long periodicSyncTime : periodicSyncTimes) {
                parcel.writeLong(periodicSyncTime);
            }
        } else {
            parcel.writeInt(-1);
        }
    }
    SyncStatusInfo(Parcel parcel) {
        int version = parcel.readInt();
        if (version != VERSION && version != 1) {
            Log.w("SyncStatusInfo", "Unknown version: " + version);
        }
        authorityId = parcel.readInt();
        totalElapsedTime = parcel.readLong();
        numSyncs = parcel.readInt();
        numSourcePoll = parcel.readInt();
        numSourceServer = parcel.readInt();
        numSourceLocal = parcel.readInt();
        numSourceUser = parcel.readInt();
        lastSuccessTime = parcel.readLong();
        lastSuccessSource = parcel.readInt();
        lastFailureTime = parcel.readLong();
        lastFailureSource = parcel.readInt();
        lastFailureMesg = parcel.readString();
        initialFailureTime = parcel.readLong();
        pending = parcel.readInt() != 0;
        initialize = parcel.readInt() != 0;
        if (version == 1) {
            periodicSyncTimes = null;
        } else {
            int N = parcel.readInt();
            if (N < 0) {
                periodicSyncTimes = null;
            } else {
                periodicSyncTimes = new ArrayList<Long>();
                for (int i=0; i<N; i++) {
                    periodicSyncTimes.add(parcel.readLong());
                }
            }
        }
    }
    public void setPeriodicSyncTime(int index, long when) {
        ensurePeriodicSyncTimeSize(index);
        periodicSyncTimes.set(index, when);
    }
    private void ensurePeriodicSyncTimeSize(int index) {
        if (periodicSyncTimes == null) {
            periodicSyncTimes = new ArrayList<Long>(0);
        }
        final int requiredSize = index + 1;
        if (periodicSyncTimes.size() < requiredSize) {
            for (int i = periodicSyncTimes.size(); i < requiredSize; i++) {
                periodicSyncTimes.add((long) 0);
            }
        }
    }
    public long getPeriodicSyncTime(int index) {
        if (periodicSyncTimes == null || periodicSyncTimes.size() < (index + 1)) {
            return 0;
        }
        return periodicSyncTimes.get(index);
    }
    public void removePeriodicSyncTime(int index) {
        ensurePeriodicSyncTimeSize(index);
        periodicSyncTimes.remove(index);
    }
    public static final Creator<SyncStatusInfo> CREATOR = new Creator<SyncStatusInfo>() {
        public SyncStatusInfo createFromParcel(Parcel in) {
            return new SyncStatusInfo(in);
        }
        public SyncStatusInfo[] newArray(int size) {
            return new SyncStatusInfo[size];
        }
    };
}