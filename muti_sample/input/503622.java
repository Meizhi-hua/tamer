public class PowerTestActivity extends Activity {
    public static final String LOGTAG = "PowerTestActivity";
    public static final String PARAM_URL = "URL";
    public static final String PARAM_TIMEOUT = "Timeout";
    public static final int RESULT_TIMEOUT = 0xDEAD;
    public static final int MSG_TIMEOUT = 0xC001;
    public static final int MSG_NAVIGATE = 0xC002;
    public static final String MSG_NAV_URL = "url";
    public static final String MSG_NAV_LOGTIME = "logtime";
    private WebView webView;
    private SimpleWebViewClient webViewClient;
    private SimpleChromeClient chromeClient;
    private Handler handler;
    private boolean timeoutFlag;
    private boolean logTime;
    private boolean pageDone;
    private Object pageDoneLock;
    private int pageStartCount;
    private int manualDelay;
    private long startTime;
    private long pageLoadTime;
    private PageDoneRunner pageDoneRunner = new PageDoneRunner();
    public PowerTestActivity() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOGTAG, "onCreate, inst=" + Integer.toHexString(hashCode()));
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        setContentView(contentView);
        setTitle("Idle");
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        webViewClient = new SimpleWebViewClient();
        chromeClient = new SimpleChromeClient();
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(chromeClient);
        contentView.addView(webView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0.0f));
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_TIMEOUT:
                        handleTimeout();
                        return;
                    case MSG_NAVIGATE:
                        manualDelay = msg.arg2;
                        navigate(msg.getData().getString(MSG_NAV_URL), msg.arg1);
                        logTime = msg.getData().getBoolean(MSG_NAV_LOGTIME);
                        return;
                }
            }
        };
        pageDoneLock = new Object();
    }
    public void reset() {
        synchronized (pageDoneLock) {
            pageDone = false;
        }
        timeoutFlag = false;
        pageStartCount = 0;
        chromeClient.resetJsTimeout();
    }
    private void navigate(String url, int timeout) {
        if(url == null) {
            Log.v(LOGTAG, "URL is null, cancelling...");
            finish();
        }
        webView.stopLoading();
        if(logTime) {
            webView.clearCache(true);
        }
        startTime = System.currentTimeMillis();
        Log.v(LOGTAG, "Navigating to URL: " + url);
        webView.loadUrl(url);
        if(timeout != 0) {
            handler.sendMessageDelayed(handler.obtainMessage(MSG_TIMEOUT),
                    timeout);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOGTAG, "onDestroy, inst=" + Integer.toHexString(hashCode()));
        webView.clearCache(true);
        webView.destroy();
    }
    private boolean isPageDone() {
        synchronized (pageDoneLock) {
            return pageDone;
        }
    }
    private void setPageDone(boolean pageDone) {
        synchronized (pageDoneLock) {
            this.pageDone = pageDone;
            pageDoneLock.notifyAll();
        }
    }
    private void handleTimeout() {
        int progress = webView.getProgress();
        webView.stopLoading();
        Log.v(LOGTAG, "Page timeout triggered, progress = " + progress);
        timeoutFlag = true;
        handler.postDelayed(pageDoneRunner, manualDelay);
    }
    public boolean waitUntilDone() {
        validateNotAppThread();
        synchronized (pageDoneLock) {
            while(!isPageDone()) {
                try {
                    pageDoneLock.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        return timeoutFlag;
    }
    public Handler getHandler() {
        return handler;
    }
    private final void validateNotAppThread() {
        if (ActivityThread.currentActivityThread() != null) {
            throw new RuntimeException(
                "This method can not be called from the main application thread");
        }
    }
    public long getPageLoadTime() {
        return pageLoadTime;
    }
    public boolean getPageError() {
        return webViewClient.getPageErrorFlag();
    }
    class SimpleWebViewClient extends WebViewClient {
        private boolean pageErrorFlag = false;
        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            pageErrorFlag = true;
            Log.v(LOGTAG, "WebCore error: code=" + errorCode
                    + ", description=" + description
                    + ", url=" + failingUrl);
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            pageStartCount++;
            Log.v(LOGTAG, "onPageStarted: " + url);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.v(LOGTAG, "onPageFinished: " + url);
            if(!timeoutFlag)
                handler.postDelayed(new WebViewStatusChecker(), 500);
        }
        public boolean getPageErrorFlag() {
            return pageErrorFlag;
        }
    }
    class SimpleChromeClient extends WebChromeClient {
        private int timeoutCounter = 0;
        public void resetJsTimeout() {
            timeoutCounter = 0;
        }
        @Override
        public void onReceivedTitle(WebView view, String title) {
            PowerTestActivity.this.setTitle(title);
        }
    }
    class WebViewStatusChecker implements Runnable {
        private int initialStartCount;
        public WebViewStatusChecker() {
            initialStartCount = pageStartCount;
        }
        public void run() {
            if (initialStartCount == pageStartCount && !isPageDone()) {
                handler.removeMessages(MSG_TIMEOUT);
                webView.stopLoading();
                handler.postDelayed(pageDoneRunner, manualDelay);
            }
        }
    }
    class PageDoneRunner implements Runnable {
        public void run() {
            Log.v(LOGTAG, "Finishing URL: " + webView.getUrl());
            pageLoadTime = System.currentTimeMillis() - startTime;
            setPageDone(true);
        }
    }
}