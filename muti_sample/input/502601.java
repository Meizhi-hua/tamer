@TestTargetClass(android.webkit.WebChromeClient.class)
public class WebChromeClientTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long TEST_TIMEOUT = 5000L;
    private WebView mWebView;
    private CtsTestServer mWebServer;
    private WebIconDatabase mIconDb;
    public WebChromeClientTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
        mWebServer = new CtsTestServer(getActivity());
    }
    @Override
    protected void tearDown() throws Exception {
        mWebView.clearHistory();
        mWebView.clearCache(true);
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        if (mIconDb != null) {
            mIconDb.removeAllIcons();
            mIconDb.close();
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onProgressChanged",
            args = {WebView.class, int.class}
        )
    })
    public void testOnProgressChanged() throws InterruptedException {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        assertFalse(webChromeClient.hadOnProgressChanged());
        mWebView.loadUrl(TestHtmlConstants.HELLO_WORLD_URL);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnProgressChanged();
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onReceivedTitle",
            args = {WebView.class, String.class}
        )
    })
    public void testOnReceivedTitle() throws Exception {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        assertFalse(webChromeClient.hadOnReceivedTitle());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnReceivedTitle();
            }
        }.run();
        assertTrue(webChromeClient.hadOnReceivedTitle());
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, webChromeClient.getPageTitle());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onReceivedIcon",
            args = {WebView.class, Bitmap.class}
        )
    })
    public void testOnReceivedIcon() {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        WebIconDatabase mIconDb = WebIconDatabase.getInstance();
        String dbPath = getActivity().getFilesDir().toString() + "/icons";
        mIconDb.open(dbPath);
        mIconDb.removeAllIcons();
        assertFalse(webChromeClient.hadOnReceivedIcon());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnReceivedIcon();
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateWindow",
            args = {WebView.class, boolean.class, boolean.class, Message.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRequestFocus",
            args = {WebView.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCloseWindow",
            args = {WebView.class}
        )
    })
    public void testWindows() throws Exception {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        assertFalse(webChromeClient.hadOnCreateWindow());
        loadUrl(mWebServer.getAssetUrl(TestHtmlConstants.JS_WINDOW_URL));
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnCreateWindow();
            }
        }.run();
        assertFalse(webChromeClient.hadOnRequestFocus());
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnCloseWindow();
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onJsBeforeUnload",
            args = {WebView.class, String.class, String.class, JsResult.class}
        )
    })
    public void testOnJsBeforeUnload() throws Exception {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        assertFalse(webChromeClient.hadOnJsBeforeUnload());
        loadUrl(mWebServer.getAssetUrl(TestHtmlConstants.JS_UNLOAD_URL));
        loadUrl(mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL));
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnJsBeforeUnload();
            }
        }.run();
        assertEquals(webChromeClient.getMessage(), "testOnJsBeforeUnload");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onJsAlert",
            args = {WebView.class, String.class, String.class, JsResult.class}
        )
    })
    public void testOnJsAlert() throws Exception {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        assertFalse(webChromeClient.hadOnJsAlert());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.JS_ALERT_URL);
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnJsAlert();
            }
        }.run();
        assertEquals(webChromeClient.getMessage(), "testOnJsAlert");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onJsConfirm",
            args = {WebView.class, String.class, String.class, JsResult.class}
        )
    })
    public void testOnJsConfirm() throws Exception {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        assertFalse(webChromeClient.hadOnJsConfirm());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.JS_CONFIRM_URL);
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnJsConfirm();
            }
        }.run();
        assertEquals(webChromeClient.getMessage(), "testOnJsConfirm");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onJsPrompt",
            args = {WebView.class, String.class, String.class, String.class, JsPromptResult.class}
        )
    })
    public void testOnJsPrompt() throws Exception {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        assertFalse(webChromeClient.hadOnJsPrompt());
        final String promptResult = "CTS";
        webChromeClient.setPromptResult(promptResult);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.JS_PROMPT_URL);
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.hadOnJsPrompt();
            }
        }.run();
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return mWebView.getTitle().equals(promptResult);
            }
        }.run();
        assertEquals(webChromeClient.getMessage(), "testOnJsPrompt");
    }
    private void loadUrl(String url) {
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return mWebView.getProgress() == 100;
            }
        }.run();
    }
    private class MockWebChromeClient extends WebChromeClient {
        private boolean mHadOnProgressChanged;
        private boolean mHadOnReceivedTitle;
        private String mPageTitle;
        private boolean mHadOnJsAlert;
        private boolean mHadOnJsConfirm;
        private boolean mHadOnJsPrompt;
        private boolean mHadOnJsBeforeUnload;
        private String mMessage;
        private String mPromptResult;
        private boolean mHadOnCloseWindow;
        private boolean mHadOnCreateWindow;
        private boolean mHadOnRequestFocus;
        private boolean mHadOnReceivedIcon;
        public void setPromptResult(String promptResult) {
            mPromptResult = promptResult;
        }
        public boolean hadOnProgressChanged() {
            return mHadOnProgressChanged;
        }
        public boolean hadOnReceivedTitle() {
            return mHadOnReceivedTitle;
        }
        public String getPageTitle() {
            return mPageTitle;
        }
        public boolean hadOnJsAlert() {
            return mHadOnJsAlert;
        }
        public boolean hadOnJsConfirm() {
            return mHadOnJsConfirm;
        }
        public boolean hadOnJsPrompt() {
            return mHadOnJsPrompt;
        }
        public boolean hadOnJsBeforeUnload() {
            return mHadOnJsBeforeUnload;
        }
        public boolean hadOnCreateWindow() {
            return mHadOnCreateWindow;
        }
        public boolean hadOnCloseWindow() {
            return mHadOnCloseWindow;
        }
        public boolean hadOnRequestFocus() {
            return mHadOnRequestFocus;
        }
        public boolean hadOnReceivedIcon() {
            return mHadOnReceivedIcon;
        }
        public String getMessage() {
            return mMessage;
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mHadOnProgressChanged = true;
        }
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mPageTitle = title;
            mHadOnReceivedTitle = true;
        }
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            super.onJsAlert(view, url, message, result);
            mHadOnJsAlert = true;
            mMessage = message;
            result.confirm();
            return true;
        }
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            super.onJsConfirm(view, url, message, result);
            mHadOnJsConfirm = true;
            mMessage = message;
            result.confirm();
            return true;
        }
        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                String defaultValue, JsPromptResult result) {
            super.onJsPrompt(view, url, message, defaultValue, result);
            mHadOnJsPrompt = true;
            mMessage = message;
            result.confirm(mPromptResult);
            return true;
        }
        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            super.onJsBeforeUnload(view, url, message, result);
            mHadOnJsBeforeUnload = true;
            mMessage = message;
            result.confirm();
            return true;
        }
        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
            mHadOnCloseWindow = true;
        }
        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture,
                Message resultMsg) {
            WebView childView = new WebView(getActivity());
            final WebSettings settings = childView.getSettings();
            settings.setJavaScriptEnabled(true);
            childView.setWebChromeClient(this);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            mHadOnCreateWindow = true;
            return true;
        }
        @Override
        public void onRequestFocus(WebView view) {
            mHadOnRequestFocus = true;
        }
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            mHadOnReceivedIcon = true;
        }
    }
}
