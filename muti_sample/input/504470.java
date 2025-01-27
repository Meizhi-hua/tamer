@TestTargetClass(android.webkit.CookieManager.class)
public class CookieManagerTest extends
        ActivityInstrumentationTestCase2<CookieSyncManagerStubActivity> {
    private static final int TEST_DELAY = 5000;
    private WebView mWebView;
    private CookieManager mCookieManager;
    public CookieManagerTest() {
        super("com.android.cts.stub", CookieSyncManagerStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
        mCookieManager = CookieManager.getInstance();
        assertNotNull(mCookieManager);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        CookieManager c1 = CookieManager.getInstance();
        CookieManager c2 = CookieManager.getInstance();
        assertSame(c1, c2);
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "clone",
        notes = "clone() is protected and CookieManager cannot be subclassed",
        args = {}
    )
    public void testClone() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAcceptCookie",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "acceptCookie",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCookie",
            args = {String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCookie",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeAllCookie",
            args = {}
        )
    })
    public void testAcceptCookie() throws Exception {
        mCookieManager.setAcceptCookie(false);
        assertFalse(mCookieManager.acceptCookie());
        assertFalse(mCookieManager.hasCookies());
        CtsTestServer server = new CtsTestServer(getActivity(), false);
        String url = server.getCookieUrl("conquest.html");
        loadUrl(url);
        assertEquals(null, mWebView.getTitle()); 
        Thread.sleep(TEST_DELAY);
        assertNull(mCookieManager.getCookie(url));
        mCookieManager.setAcceptCookie(true);
        assertTrue(mCookieManager.acceptCookie());
        url = server.getCookieUrl("war.html");
        loadUrl(url);
        assertEquals(null, mWebView.getTitle()); 
        waitForCookie(url);
        String cookie = mCookieManager.getCookie(url);
        assertNotNull(cookie);
        final Pattern pat = Pattern.compile("count=(\\d+)");
        Matcher m = pat.matcher(cookie);
        assertTrue(m.matches());
        assertEquals("0", m.group(1));
        url = server.getCookieUrl("famine.html");
        loadUrl(url);
        assertEquals("count=0", mWebView.getTitle()); 
        waitForCookie(url);
        cookie = mCookieManager.getCookie(url);
        assertNotNull(cookie);
        m = pat.matcher(cookie);
        assertTrue(m.matches());
        assertEquals("1", m.group(1)); 
        url = server.getCookieUrl("death.html");
        mCookieManager.setCookie(url, "count=41");
        loadUrl(url);
        assertEquals("count=41", mWebView.getTitle()); 
        waitForCookie(url);
        cookie = mCookieManager.getCookie(url);
        assertNotNull(cookie);
        m = pat.matcher(cookie);
        assertTrue(m.matches());
        assertEquals("42", m.group(1)); 
        mCookieManager.removeAllCookie();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasCookies",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeAllCookie",
            args = {}
        )
    })
    @ToBeFixed(explanation = "CookieManager.hasCookies() should also count cookies in RAM cache")
    public void testCookieManager() {
        mCookieManager.setAcceptCookie(true);
        assertTrue(mCookieManager.acceptCookie());
        assertFalse(mCookieManager.hasCookies());
        String url = "http:
        String cookie = "name=test";
        mCookieManager.setCookie(url, cookie);
        assertEquals(cookie, mCookieManager.getCookie(url));
        CookieSyncManager.getInstance().sync();
        new DelayedCheck(TEST_DELAY) {
            @Override
            protected boolean check() {
                return mCookieManager.hasCookies();
            }
        }.run();
        mCookieManager.removeAllCookie();
        new DelayedCheck(TEST_DELAY) {
            @Override
            protected boolean check() {
                return !mCookieManager.hasCookies();
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeSessionCookie",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeExpiredCookie",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    public void testRemoveCookies() throws InterruptedException {
        mCookieManager.setAcceptCookie(true);
        assertTrue(mCookieManager.acceptCookie());
        assertFalse(mCookieManager.hasCookies());
        final String url = "http:
        final String cookie1 = "cookie1=peter";
        final String cookie2 = "cookie2=sue";
        final String cookie3 = "cookie3=marc";
        mCookieManager.setCookie(url, cookie1); 
        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 600);
        String value2 = cookie2 + "; expires=" + date.toGMTString();
        mCookieManager.setCookie(url, value2); 
        long expiration = 3000;
        date = new Date();
        date.setTime(date.getTime() + expiration);
        String value3 = cookie3 + "; expires=" + date.toGMTString();
        mCookieManager.setCookie(url, value3); 
        String allCookies = mCookieManager.getCookie(url);
        assertTrue(allCookies.contains(cookie1));
        assertTrue(allCookies.contains(cookie2));
        assertTrue(allCookies.contains(cookie3));
        mCookieManager.removeSessionCookie();
        new DelayedCheck(TEST_DELAY) {
            protected boolean check() {
                String c = mCookieManager.getCookie(url);
                return !c.contains(cookie1) && c.contains(cookie2) && c.contains(cookie3);
            }
        }.run();
        Thread.sleep(expiration + 1000); 
        mCookieManager.removeExpiredCookie();
        new DelayedCheck(TEST_DELAY) {
            protected boolean check() {
                String c = mCookieManager.getCookie(url);
                return !c.contains(cookie1) && c.contains(cookie2) && !c.contains(cookie3);
            }
        }.run();
        mCookieManager.removeAllCookie();
        new DelayedCheck(TEST_DELAY) {
            protected boolean check() {
                return mCookieManager.getCookie(url) == null;
            }
        }.run();
    }
    private void loadUrl(String url) {
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_DELAY) {
            protected boolean check() {
                return mWebView.getProgress() == 100;
            }
        }.run();
    }
    private void waitForCookie(final String url) {
        new DelayedCheck(TEST_DELAY) {
            protected boolean check() {
                return mCookieManager.getCookie(url) != null;
            }
        }.run();
    }
}
