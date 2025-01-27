public class TimingLogger {
    private String mTag;
    private String mLabel;
    private boolean mDisabled;
    ArrayList<Long> mSplits;
    ArrayList<String> mSplitLabels;
    public TimingLogger(String tag, String label) {
        reset(tag, label);
    }
    public void reset(String tag, String label) {
        mTag = tag;
        mLabel = label;
        reset();
    }
    public void reset() {
        mDisabled = !Log.isLoggable(mTag, Log.VERBOSE);
        if (mDisabled) return;
        if (mSplits == null) {
            mSplits = new ArrayList<Long>();
            mSplitLabels = new ArrayList<String>();
        } else {
            mSplits.clear();
            mSplitLabels.clear();
        }
        addSplit(null);
    }
    public void addSplit(String splitLabel) {
        if (mDisabled) return;
        long now = SystemClock.elapsedRealtime();
        mSplits.add(now);
        mSplitLabels.add(splitLabel);
    }
    public void dumpToLog() {
        if (mDisabled) return;
        Log.d(mTag, mLabel + ": begin");
        final long first = mSplits.get(0);
        long now = first;
        for (int i = 1; i < mSplits.size(); i++) {
            now = mSplits.get(i);
            final String splitLabel = mSplitLabels.get(i);
            final long prev = mSplits.get(i - 1);
            Log.d(mTag, mLabel + ":      " + (now - prev) + " ms, " + splitLabel);
        }
        Log.d(mTag, mLabel + ": end, " + (now - first) + " ms");
    }
}
