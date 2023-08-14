public class ReliabilityTest extends ActivityInstrumentationTestCase2<ReliabilityTestActivity> {
    private static final String LOGTAG = "ReliabilityTest";
    private static final String PKG_NAME = "com.android.dumprendertree";
    private static final String TEST_LIST_FILE = "/sdcard/android/reliability_tests_list.txt";
    private static final String TEST_STATUS_FILE = "/sdcard/android/reliability_running_test.txt";
    private static final String TEST_TIMEOUT_FILE = "/sdcard/android/reliability_timeout_test.txt";
    private static final String TEST_LOAD_TIME_FILE = "/sdcard/android/reliability_load_time.txt";
    private static final String TEST_DONE = "#DONE";
    static final String RELIABILITY_TEST_RUNNER_FILES[] = {
        "run_reliability_tests.py"
    };
    public ReliabilityTest() {
        super(PKG_NAME, ReliabilityTestActivity.class);
    }
    public void runReliabilityTest() throws Throwable {
        LayoutTestsAutoRunner runner = (LayoutTestsAutoRunner)getInstrumentation();
        File testListFile = new File(TEST_LIST_FILE);
        if(!testListFile.exists())
            throw new FileNotFoundException("test list file not found.");
        BufferedReader listReader = new BufferedReader(
                new FileReader(testListFile));
        String lastUrl = FsUtils.readTestStatus(TEST_STATUS_FILE);
        if(lastUrl != null && !TEST_DONE.equals(lastUrl))
            fastForward(listReader, lastUrl);
        String url = null;
        Handler handler = null;
        boolean timeoutFlag = false;
        long start, elapsed;
        Intent intent = new Intent(runner.getContext(), ReliabilityTestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ReliabilityTestActivity activity = (ReliabilityTestActivity)runner.startActivitySync(
            intent);
        while((url = listReader.readLine()) != null) {
            url = url.trim();
            if(url.length() == 0)
                continue;
            start = System.currentTimeMillis();
            Log.v(LOGTAG, "Testing URL: " + url);
            FsUtils.updateTestStatus(TEST_STATUS_FILE, url);
            activity.reset();
            handler = activity.getHandler();
            Message msg = handler.obtainMessage(
                    ReliabilityTestActivity.MSG_NAVIGATE,
                    runner.mTimeoutInMillis, runner.mDelay);
            msg.getData().putString(ReliabilityTestActivity.MSG_NAV_URL, url);
            msg.getData().putBoolean(ReliabilityTestActivity.MSG_NAV_LOGTIME,
                    runner.mLogtime);
            handler.sendMessage(msg);
            timeoutFlag = activity.waitUntilDone();
            elapsed = System.currentTimeMillis() - start;
            if(elapsed < 1000) {
                Log.w(LOGTAG, "Page load finished in " + elapsed
                        + "ms, too soon?");
            } else {
                Log.v(LOGTAG, "Page load finished in " + elapsed + "ms");
            }
            if(timeoutFlag) {
                writeTimeoutFile(url);
            }
            if(runner.mLogtime) {
                writeLoadTime(url, activity.getPageLoadTime());
            }
            System.runFinalization();
            System.gc();
            System.gc();
        }
        activity.finish();
        FsUtils.updateTestStatus(TEST_STATUS_FILE, TEST_DONE);
        listReader.close();
    }
    public void copyRunnerAssetsToCache() {
        try {
            String out_dir = getActivity().getApplicationContext()
            .getCacheDir().getPath() + "/";
            for( int i=0; i< RELIABILITY_TEST_RUNNER_FILES.length; i++) {
                InputStream in = getActivity().getAssets().open(
                        RELIABILITY_TEST_RUNNER_FILES[i]);
                OutputStream out = new FileOutputStream(
                        out_dir + RELIABILITY_TEST_RUNNER_FILES[i]);
                byte[] buf = new byte[2048];
                int len;
                while ((len = in.read(buf)) >= 0 ) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        }catch (IOException e) {
            Log.e(LOGTAG, "Cannot extract scripts for testing.", e);
        }
    }
    private void fastForward(BufferedReader testListReader, String lastUrl) {
        if(lastUrl == null)
            return;
        String line = null;
        try {
            while((line = testListReader.readLine()) != null) {
                if(lastUrl.equals(line))
                    return;
            }
        } catch (IOException ioe) {
            Log.e(LOGTAG, "Error while reading test list.", ioe);
            return;
        }
    }
    private void writeTimeoutFile(String s) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(TEST_TIMEOUT_FILE, true));
            bos.write(s.getBytes());
            bos.write('\n');
            bos.close();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot update file " + TEST_TIMEOUT_FILE, e);
        }
    }
    private void writeLoadTime(String s, long time) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(TEST_LOAD_TIME_FILE, true));
            bos.write((s + '|' + time + '\n').getBytes());
            bos.close();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot update file " + TEST_LOAD_TIME_FILE, e);
        }
    }
}