public class RateController {
    private static final String TAG = "RateController";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final int RATE_LIMIT = 100;
    private static final long ONE_HOUR = 1000 * 60 * 60;
    private static final int NO_ANSWER  = 0;
    private static final int ANSWER_YES = 1;
    private static final int ANSWER_NO  = 2;
    public static final int ANSWER_TIMEOUT = 20000;
    public static final String RATE_LIMIT_SURPASSED_ACTION =
        "com.android.mms.RATE_LIMIT_SURPASSED";
    public static final String RATE_LIMIT_CONFIRMED_ACTION =
        "com.android.mms.RATE_LIMIT_CONFIRMED";
    private static RateController sInstance;
    private static boolean sMutexLock;
    private final Context mContext;
    private int mAnswer;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Intent received: " + intent);
            }
            if (RATE_LIMIT_CONFIRMED_ACTION.equals(intent.getAction())) {
                synchronized (this) {
                    mAnswer = intent.getBooleanExtra("answer", false)
                                            ? ANSWER_YES : ANSWER_NO;
                    notifyAll();
                }
            }
        }
    };
    private RateController(Context context) {
        mContext = context;
    }
    public static void init(Context context) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "RateController.init()");
        }
        if (sInstance != null) {
            Log.w(TAG, "Already initialized.");
        }
        sInstance = new RateController(context);
    }
    public static RateController getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Uninitialized.");
        }
        return sInstance;
    }
    public final void update() {
        ContentValues values = new ContentValues(1);
        values.put(Rate.SENT_TIME, System.currentTimeMillis());
        SqliteWrapper.insert(mContext, mContext.getContentResolver(),
                             Rate.CONTENT_URI, values);
    }
    public final boolean isLimitSurpassed() {
        long oneHourAgo = System.currentTimeMillis() - ONE_HOUR;
        Cursor c = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                Rate.CONTENT_URI, new String[] { "COUNT(*) AS rate" },
                Rate.SENT_TIME + ">" + oneHourAgo, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(0) >= RATE_LIMIT;
                }
            } finally {
                c.close();
            }
        }
        return false;
    }
    synchronized public boolean isAllowedByUser() {
        while (sMutexLock) {
            try {
                wait();
            } catch (InterruptedException _) {
            }
        }
        sMutexLock = true;
        mContext.registerReceiver(mBroadcastReceiver,
                new IntentFilter(RATE_LIMIT_CONFIRMED_ACTION));
        mAnswer = NO_ANSWER;
        try {
            Intent intent = new Intent(RATE_LIMIT_SURPASSED_ACTION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return waitForAnswer() == ANSWER_YES;
        } finally {
            mContext.unregisterReceiver(mBroadcastReceiver);
            sMutexLock = false;
            notifyAll();
        }
    }
    synchronized private int waitForAnswer() {
        for (int t = 0; (mAnswer == NO_ANSWER) && (t < ANSWER_TIMEOUT); t += 1000) {
            try {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "Waiting for answer..." + t / 1000);
                }
                wait(1000L);
            } catch (InterruptedException _) {
            }
        }
        return mAnswer;
    }
}
