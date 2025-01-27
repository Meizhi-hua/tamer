public final class SyncResult implements Parcelable {
    public final boolean syncAlreadyInProgress;
    public boolean tooManyDeletions;
    public boolean tooManyRetries;
    public boolean databaseError;
    public boolean fullSyncRequested;
    public boolean partialSyncUnavailable;
    public boolean moreRecordsToGet;
    public long delayUntil;
    public final SyncStats stats;
    public static final SyncResult ALREADY_IN_PROGRESS;
    static {
        ALREADY_IN_PROGRESS = new SyncResult(true);
    }
    public SyncResult() {
        this(false);
    }
    private SyncResult(boolean syncAlreadyInProgress) {
        this.syncAlreadyInProgress = syncAlreadyInProgress;
        this.tooManyDeletions = false;
        this.tooManyRetries = false;
        this.fullSyncRequested = false;
        this.partialSyncUnavailable = false;
        this.moreRecordsToGet = false;
        this.delayUntil = 0;
        this.stats = new SyncStats();
    }
    private SyncResult(Parcel parcel) {
        syncAlreadyInProgress = parcel.readInt() != 0;
        tooManyDeletions = parcel.readInt() != 0;
        tooManyRetries = parcel.readInt() != 0;
        databaseError = parcel.readInt() != 0;
        fullSyncRequested = parcel.readInt() != 0;
        partialSyncUnavailable = parcel.readInt() != 0;
        moreRecordsToGet = parcel.readInt() != 0;
        delayUntil = parcel.readLong();
        stats = new SyncStats(parcel);
    }
    public boolean hasHardError() {
        return stats.numParseExceptions > 0
                || stats.numConflictDetectedExceptions > 0
                || stats.numAuthExceptions > 0
                || tooManyDeletions
                || tooManyRetries
                || databaseError;
    }
    public boolean hasSoftError() {
        return syncAlreadyInProgress || stats.numIoExceptions > 0;
    }
    public boolean hasError() {
        return hasSoftError() || hasHardError();
    }
    public boolean madeSomeProgress() {
        return ((stats.numDeletes > 0) && !tooManyDeletions)
                || stats.numInserts > 0
                || stats.numUpdates > 0;
    }
    public void clear() {
        if (syncAlreadyInProgress) {
            throw new UnsupportedOperationException(
                    "you are not allowed to clear the ALREADY_IN_PROGRESS SyncStats");
        }
        tooManyDeletions = false;
        tooManyRetries = false;
        databaseError = false;
        fullSyncRequested = false;
        partialSyncUnavailable = false;
        moreRecordsToGet = false;
        delayUntil = 0;
        stats.clear();
    }
    public static final Creator<SyncResult> CREATOR = new Creator<SyncResult>() {
        public SyncResult createFromParcel(Parcel in) {
            return new SyncResult(in);
        }
        public SyncResult[] newArray(int size) {
            return new SyncResult[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(syncAlreadyInProgress ? 1 : 0);
        parcel.writeInt(tooManyDeletions ? 1 : 0);
        parcel.writeInt(tooManyRetries ? 1 : 0);
        parcel.writeInt(databaseError ? 1 : 0);
        parcel.writeInt(fullSyncRequested ? 1 : 0);
        parcel.writeInt(partialSyncUnavailable ? 1 : 0);
        parcel.writeInt(moreRecordsToGet ? 1 : 0);
        parcel.writeLong(delayUntil);
        stats.writeToParcel(parcel, flags);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SyncResult:");
        if (syncAlreadyInProgress) {
            sb.append(" syncAlreadyInProgress: ").append(syncAlreadyInProgress);
        }
        if (tooManyDeletions) sb.append(" tooManyDeletions: ").append(tooManyDeletions);
        if (tooManyRetries) sb.append(" tooManyRetries: ").append(tooManyRetries);
        if (databaseError) sb.append(" databaseError: ").append(databaseError);
        if (fullSyncRequested) sb.append(" fullSyncRequested: ").append(fullSyncRequested);
        if (partialSyncUnavailable) {
            sb.append(" partialSyncUnavailable: ").append(partialSyncUnavailable);
        }
        if (moreRecordsToGet) sb.append(" moreRecordsToGet: ").append(moreRecordsToGet);
        if (delayUntil > 0) sb.append(" delayUntil: ").append(delayUntil);
        sb.append(stats);
        return sb.toString();
    }
    public String toDebugString() {
        StringBuffer sb = new StringBuffer();
        if (fullSyncRequested) {
            sb.append("f1");
        }
        if (partialSyncUnavailable) {
            sb.append("r1");
        }
        if (hasHardError()) {
            sb.append("X1");
        }
        if (stats.numParseExceptions > 0) {
            sb.append("e").append(stats.numParseExceptions);
        }
        if (stats.numConflictDetectedExceptions > 0) {
            sb.append("c").append(stats.numConflictDetectedExceptions);
        }
        if (stats.numAuthExceptions > 0) {
            sb.append("a").append(stats.numAuthExceptions);
        }
        if (tooManyDeletions) {
            sb.append("D1");
        }
        if (tooManyRetries) {
            sb.append("R1");
        }
        if (databaseError) {
            sb.append("b1");
        }
        if (hasSoftError()) {
            sb.append("x1");
        }
        if (syncAlreadyInProgress) {
            sb.append("l1");
        }
        if (stats.numIoExceptions > 0) {
            sb.append("I").append(stats.numIoExceptions);
        }
        return sb.toString();
    }
}
