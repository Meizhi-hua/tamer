public class ProviderPerfActivity extends Activity {
    private static final String TAG = "ProviderPerfActivity";
    private static final Uri SYSTEM_SETTINGS_URI = Uri.parse("content:
    private static final Uri CROSS_PROC_PROVIDER_URI = Uri.parse("content:
    private static final Uri IN_PROC_PROVIDER_URI = Uri.parse("content:
    private final Handler mHandler = new Handler();
    private final static int LOOP_TIME_MILLIS = 2000;
    private final static long LOOP_TIME_NANOS = (long) LOOP_TIME_MILLIS * 1000000L;
    private IService mServiceStub = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceStub = IService.Stub.asInterface(service);
            Log.v(TAG, "Service bound");
        }
        public void onServiceDisconnected(ComponentName name) {
            mServiceStub = null;
            Log.v(TAG, "Service unbound");
        };
    };
    private ContentResolver cr;
    private int mIterations = 100;
    private String mTraceName = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cr = getContentResolver();
        setButtonAction(R.id.file_read_button, new Runnable() {
                public void run() {
                    final float avgTime = fileReadLoop();
                    endAsyncOp(R.id.file_read_button, R.id.file_read_text, avgTime);
                }});
        setButtonAction(R.id.file_write_button, new Runnable() {
                public void run() {
                    final float avgTime = fileWriteLoop();
                    endAsyncOp(R.id.file_write_button, R.id.file_write_text, avgTime);
                }});
        setButtonAction(R.id.settings_read_button, new Runnable() {
                public void run() {
                    final float avgTime = settingsProviderLoop(MODE_READ, 0);
                    endAsyncOp(R.id.settings_read_button, R.id.settings_read_text, avgTime);
                }});
        setButtonAction(R.id.settings_sleep_button, new Runnable() {
                public void run() {
                    final float avgTime = settingsProviderLoop(MODE_READ, 100);
                    endAsyncOp(R.id.settings_sleep_button, R.id.settings_sleep_text, avgTime);
                }});
        setButtonAction(R.id.settings_write_button, new Runnable() {
                public void run() {
                    final float avgTime = settingsProviderLoop(MODE_WRITE, 0);
                    endAsyncOp(R.id.settings_write_button, R.id.settings_write_text, avgTime);
                }});
        setButtonAction(R.id.settings_writedup_button, new Runnable() {
                public void run() {
                    final float avgTime = settingsProviderLoop(MODE_WRITE_DUP, 0);
                    endAsyncOp(R.id.settings_writedup_button, R.id.settings_writedup_text, avgTime);
                }});
        setButtonAction(R.id.dummy_lookup_button, new Runnable() {
                public void run() {
                    final float avgTime = noOpProviderLoop(CROSS_PROC_PROVIDER_URI);
                    endAsyncOp(R.id.dummy_lookup_button, R.id.dummy_lookup_text, avgTime);
                }});
        setButtonAction(R.id.dummy_local_lookup_button, new Runnable() {
                public void run() {
                    final float avgTime = noOpProviderLoop(IN_PROC_PROVIDER_URI);
                    endAsyncOp(R.id.dummy_local_lookup_button,
                               R.id.dummy_local_lookup_text, avgTime);
                }});
        setButtonAction(R.id.localsocket_button, new Runnable() {
                public void run() {
                    final float avgTime = localSocketLoop();
                    endAsyncOp(R.id.localsocket_button, R.id.localsocket_text, avgTime);
                }});
        setButtonAction(R.id.service_button, new Runnable() {
                public void run() {
                    final float avgTime = serviceLoop(null);
                    endAsyncOp(R.id.service_button, R.id.service_text, avgTime);
                }});
        setButtonAction(R.id.service2_button, new Runnable() {
                public void run() {
                    final float avgTime = serviceLoop("xyzzy");
                    endAsyncOp(R.id.service2_button, R.id.service2_text, avgTime);
                }});
        setButtonAction(R.id.ping_media_button, new Runnable() {
                public void run() {
                    final float avgTime = pingServiceLoop("media.player");
                    endAsyncOp(R.id.ping_media_button, R.id.ping_media_text, avgTime);
                }});
        setButtonAction(R.id.ping_activity_button, new Runnable() {
                public void run() {
                    final float avgTime = pingServiceLoop("activity");
                    endAsyncOp(R.id.ping_activity_button, R.id.ping_activity_text, avgTime);
                }});
        setButtonAction(R.id.proc_button, new Runnable() {
                public void run() {
                    final float avgTime = procLoop();
                    endAsyncOp(R.id.proc_button, R.id.proc_text, avgTime);
                }});
        setButtonAction(R.id.call_button, new Runnable() {
                public void run() {
                    final float avgTime = callLoop("ringtone");
                    endAsyncOp(R.id.call_button, R.id.call_text, avgTime);
                }});
        setButtonAction(R.id.call2_button, new Runnable() {
                public void run() {
                    final float avgTime = callLoop("XXXXXXXX");  
                    endAsyncOp(R.id.call2_button, R.id.call2_text, avgTime);
                }});
        setButtonAction(R.id.obtain_button, new Runnable() {
                public void run() {
                    final float avgTime = parcelLoop(true);
                    endAsyncOp(R.id.obtain_button, R.id.obtain_text, avgTime);
                }});
        setButtonAction(R.id.recycle_button, new Runnable() {
                public void run() {
                    final float avgTime = parcelLoop(false);
                    endAsyncOp(R.id.recycle_button, R.id.recycle_text, avgTime);
                }});
    }
    @Override public void onResume() {
        super.onResume();
        bindService(new Intent(this, MiscService.class),
                    serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override public void onPause() {
        super.onPause();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }
    private void setButtonAction(int button_id, final Runnable r) {
        final Button button = (Button) findViewById(button_id);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button.requestFocus();
                button.setEnabled(false);
                TextView tvIter = (TextView) findViewById(R.id.iterations_edit);
                try {
                    mIterations = Integer.parseInt(tvIter.getText().toString());
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid iteration count", e);
                    if (tvIter != null) tvIter.setText(Integer.toString(mIterations));
                }
                TextView tvTrace = (TextView) findViewById(R.id.trace_edit);
                String name = tvTrace.getText().toString();
                if (name != null && name.length() > 0) {
                    mTraceName = name;
                    Debug.startMethodTracing(name);
                }
                new Thread(r).start();
            }
        });
    }
    private void endAsyncOp(final int button_id, final int text_id, final float avgTime) {
        mHandler.post(new Runnable() {
            public void run() {
                Debug.stopMethodTracing();
                findViewById(button_id).setEnabled(true);
                setTextTime(text_id, avgTime);
            }
        });
    }
    private void setTextTime(int id, float avgTime) {
        TextView tv = (TextView) findViewById(id);
        if (tv == null) return;
        String text = tv.getText().toString();
        text = text.substring(0, text.indexOf(':') + 1) + "\n" + avgTime + " ms avg";
        tv.setText(text);
    }
    private float fileReadLoop() {
        RandomAccessFile raf = null;
        File filename = getFileStreamPath("test.dat");
        try {
            long sumNanos = 0;
            byte[] buf = new byte[512];
            raf = new RandomAccessFile(filename, "rw");
            raf.write(buf);
            raf.close();
            raf = null;
            raf = new RandomAccessFile(filename, "r");
            for (int i = 0; i < mIterations; i++) {
                long lastTime = System.nanoTime();
                raf.seek(0);
                raf.read(buf);
                sumNanos += System.nanoTime() - lastTime;
            }
            return (float) sumNanos / Math.max(1.0f, (float) mIterations) / 1000000.0f;
        } catch (IOException e) {
            Log.e(TAG, "File read failed", e);
            return 0;
        } finally {
            try { if (raf != null) raf.close(); } catch (IOException e) {}
        }
    }
    private float fileWriteLoop() {
        RandomAccessFile raf = null;
        File filename = getFileStreamPath("test.dat");
        try {
            long sumNanos = 0;
            byte[] buf = new byte[512];
            for (int i = 0; i < mIterations; i++) {
                for (int j = 0; j < buf.length; j++) buf[j] = (byte) (i + j);
                long lastTime = System.nanoTime();
                raf = new RandomAccessFile(filename, "rw");
                raf.write(buf);
                raf.close();
                raf = null;
                sumNanos += System.nanoTime() - lastTime;
            }
            return (float) sumNanos / Math.max(1.0f, (float) mIterations) / 1000000.0f;
        } catch (IOException e) {
            Log.e(TAG, "File read failed", e);
            return 0;
        } finally {
            try { if (raf != null) raf.close(); } catch (IOException e) {}
        }
    }
    private float noOpProviderLoop(Uri uri) {
        long sumNanos = 0;
        int failures = 0;
        int total = 0;
        for (int i = 0; i < mIterations; i++) {
            long duration = doNoOpLookup(uri);
            if (duration < 0) {
                failures++;
            } else {
                total++;
                sumNanos += duration;
            }
        }
        float averageMillis = (float) sumNanos /
            (float) (total != 0 ? total : 1) /
            1000000.0f;
        Log.v(TAG, "dummy loop: fails=" + failures + "; total=" + total + "; goodavg ms=" + averageMillis);
        return averageMillis;
    }
    private float callLoop(String key) {
        IContentProvider cp = cr.acquireProvider(SYSTEM_SETTINGS_URI.getAuthority());
        long sumNanos = 0;
        int total = 0;
        try {
            for (int i = 0; i < mIterations; i++) {
                long lastTime = System.nanoTime();
                Bundle b = cp.call("GET_system", key, null);
                long nowTime = System.nanoTime();
                total++;
                sumNanos += (nowTime - lastTime);
            }
        } catch (RemoteException e) {
            return -999.0f;
        }
        float averageMillis = (float) sumNanos /
            (float) (total != 0 ? total : 1) /
            1000000.0f;
        Log.v(TAG, "call loop: avg_ms=" + averageMillis + "; calls=" + total);
        return averageMillis;
    }
    private float procLoop() {
        long sumNanos = 0;
        int total = 0;
        File f = new File("/proc/self/cmdline");
        byte[] buf = new byte[100];
        String value = null;
        try {
            for (int i = 0; i < mIterations; i++) {
                long lastTime = System.nanoTime();
                FileInputStream is = new FileInputStream(f);
                int readBytes = is.read(buf, 0, 100);
                is.close();
                long nowTime = System.nanoTime();
                total++;
                sumNanos += (nowTime - lastTime);
                lastTime = nowTime;
            }
        } catch (IOException e) {
            return -999.0f;
        }
        float averageMillis = (float) sumNanos /
            (float) (total != 0 ? total : 1) /
            1000000.0f;
        Log.v(TAG, "proc loop: total: " + total + "; avg_ms=" + averageMillis + "; value=" + value);
        return averageMillis;
    }
    private static final String[] IGNORED_COLUMN = {"ignored"};
    private long doNoOpLookup(Uri uri) {
        Cursor c = null;
        try {
            long startTime = System.nanoTime();
            c = cr.query(uri,
                         IGNORED_COLUMN,  
                         "name=?",
                         IGNORED_COLUMN,  
                         null );
            if (c == null) {
                Log.w(TAG, "cursor null");
                return -1;
            }
            String value = c.moveToNext() ? c.getString(0) : null;
            long duration = System.nanoTime() - startTime;
            return duration;
        } catch (SQLException e) {
            Log.w(TAG, "sqlite exception: " + e);
            return -1;
        } finally {
            if (c != null) c.close();
        }
    }
    private float serviceLoop(String value) {
        if (mServiceStub == null) {
            Log.v(TAG, "No service stub.");
            return -999;
        }
        String dummy = null;
        try {
            if (mTraceName != null) mServiceStub.startTracing(mTraceName + ".service");
            long sumNanos = 0;
            for (int i = 0; i < mIterations; i++) {
                long lastTime = System.nanoTime();
                if (value == null) {
                    mServiceStub.pingVoid();
                } else {
                    value = mServiceStub.pingString(value);
                }
                sumNanos += System.nanoTime() - lastTime;
            }
            if (mTraceName != null) mServiceStub.stopTracing();
            return (float) sumNanos / Math.max(1.0f, (float) mIterations) / 1000000.0f;
        } catch (RemoteException e) {
            Log.e(TAG, "Binder call failed", e);
            return -999;
        }
    }
    private float pingServiceLoop(String service) {
        IBinder binder = ServiceManager.getService(service);
        if (binder == null) {
            Log.e(TAG, "Service missing: " + service);
            return -1.0f;
        }
        long sumNanos = 0;
        for (int i = 0; i < mIterations; i++) {
            long lastTime = System.nanoTime();
            if (!binder.pingBinder()) {
                Log.e(TAG, "Error pinging service: " + service);
                return -1.0f;
            }
            sumNanos += System.nanoTime() - lastTime;
        }
        return (float) sumNanos / Math.max(1.0f, (float) mIterations) / 1000000.0f;
    }
    private float localSocketLoop() {
        LocalSocket socket = null;
        try {
            socket = new LocalSocket();
            Log.v(TAG, "Connecting to socket...");
            socket.connect(new LocalSocketAddress(MiscService.SOCKET_NAME));
            Log.v(TAG, "Connected to socket.");
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            int count = 0;
            long sumNanos = 0;
            for (int i = 0; i < mIterations; i++) {
                long beforeTime = System.nanoTime();
                int expectByte = count & 0xff;
                os.write(expectByte);
                int gotBackByte = is.read();
                long afterTime = System.nanoTime();
                sumNanos += (afterTime - beforeTime);
                if (gotBackByte != expectByte) {
                    Log.w(TAG, "Got wrong byte back.  Got: " + gotBackByte
                          + "; wanted=" + expectByte);
                    return -999.00f;
                }
                count++;
            }
            return count == 0 ? 0.0f : ((float) sumNanos / (float) count / 1000000.0f);
        } catch (IOException e) {
            Log.v(TAG, "error in localSocketLoop: " + e);
            return -1.0f;
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
    private float parcelLoop(boolean obtain) {
        long sumNanos = 0;
        for (int i = 0; i < mIterations; i++) {
            if (obtain) {
                long lastTime = System.nanoTime();
                Parcel p = Parcel.obtain();
                sumNanos += System.nanoTime() - lastTime;
                p.recycle();
            } else {
                Parcel p = Parcel.obtain();
                long lastTime = System.nanoTime();
                p.recycle();
                sumNanos += System.nanoTime() - lastTime;
            }
        }
        return (float) sumNanos / Math.max(1.0f, (float) mIterations) / 1000000.0f;
    }
    private static final int MODE_READ = 0;
    private static final int MODE_WRITE = 1;
    private static final int MODE_WRITE_DUP = 2;
    private float settingsProviderLoop(int mode, long innerSleep) {
        long sumMillis = 0;
        int total = 0;
        for (int i = 0; i < mIterations; i++) {
            long duration = mode == MODE_READ ? settingsRead(innerSleep) : settingsWrite(mode);
            if (duration < 0) {
                return -999.0f;
            }
            total++;
            sumMillis += duration;
        }
        float averageMillis = ((float) sumMillis / (float) (total != 0 ? total : 1));
        Log.v(TAG, "settings provider; mode=" + mode + "; total=" + total +
              "; goodavg_ms=" + averageMillis);
        return averageMillis;
    }
    private long settingsRead(long innerSleep) {
        Cursor c = null;
        try {
            long startTime = SystemClock.uptimeMillis();
            c = cr.query(SYSTEM_SETTINGS_URI,
                         new String[]{"value"},
                         "name=?",
                         new String[]{"airplane_mode_on"},
                         null );
            if (c == null) {
                Log.w(TAG, "cursor null");
                return -1;
            }
            String value = c.moveToNext() ? c.getString(0) : null;
            long duration = SystemClock.uptimeMillis() - startTime;
            if (innerSleep > 0) {
                try {
                    Thread.sleep(innerSleep);
                } catch (InterruptedException e) {}
            }
            return duration;
        } catch (SQLException e) {
            Log.w(TAG, "sqlite exception: " + e);
            return -1;
        } finally {
            if (c != null) c.close();
        }
    }
    private long settingsWrite(int mode) {
        Cursor c = null;
        long startTime = SystemClock.uptimeMillis();
        try {
            ContentValues values = new ContentValues();
            values.put("name", "dummy_for_testing");
            values.put("value", (mode == MODE_WRITE ? (""+startTime) : "foo"));
            Uri uri = cr.insert(SYSTEM_SETTINGS_URI, values);
            Log.v(TAG, "inserted uri: " + uri);
        } catch (SQLException e) {
            Log.w(TAG, "sqliteexception during write: " + e);
            return -1;
        }
        long duration = SystemClock.uptimeMillis() - startTime;
        return duration;
    }
}
