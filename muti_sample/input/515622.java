@TestTargetClass(android.webkit.URLUtil.class)
public class URLUtilTest extends AndroidTestCase {
    private final String VALID_HTTP_URL = "http:
    private final String VALID_HTTPS_URL = "https:
    private final String VALID_ASSET_URL = "file:
    private final String VALID_PROXY_URL = "file:
    private final String VALID_JAVASCRIPT_URL = "javascript:wave()";
    private final String VALID_CONTENT_URL = "content:
    private final String VALID_DATA_URL = "data:
    private final String VALID_ABOUT_URL = "about:
    private final String VALID_FILE_URL = "file:
    private final String VALID_FTP_URL = "ftp:
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isAssetUrl",
            args = {String.class}
        )
    })
    public void testIsAssetUrl() {
        assertFalse(URLUtil.isAssetUrl(null));
        assertFalse(URLUtil.isAssetUrl(VALID_HTTP_URL));
        assertTrue(URLUtil.isAssetUrl(VALID_ASSET_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isAboutUrl",
            args = {String.class}
        )
    })
    public void testIsAboutUrl() {
        assertFalse(URLUtil.isAboutUrl(null));
        assertFalse(URLUtil.isAboutUrl(VALID_DATA_URL));
        assertTrue(URLUtil.isAboutUrl(VALID_ABOUT_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isContentUrl",
            args = {String.class}
        )
    })
    public void testIsContentUrl() {
        assertFalse(URLUtil.isContentUrl(null));
        assertFalse(URLUtil.isContentUrl(VALID_DATA_URL));
        assertTrue(URLUtil.isContentUrl(VALID_CONTENT_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isCookielessProxyUrl",
            args = {String.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void testIsCookielessProxyUrl() {
        assertFalse(URLUtil.isCookielessProxyUrl(null));
        assertFalse(URLUtil.isCookielessProxyUrl(VALID_HTTP_URL));
        assertTrue(URLUtil.isCookielessProxyUrl(VALID_PROXY_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isDataUrl",
            args = {String.class}
        )
    })
    public void testIsDataUrl() {
        assertFalse(URLUtil.isDataUrl(null));
        assertFalse(URLUtil.isDataUrl(VALID_CONTENT_URL));
        assertTrue(URLUtil.isDataUrl(VALID_DATA_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFileUrl",
            args = {String.class}
        )
    })
    public void testIsFileUrl() {
        assertFalse(URLUtil.isFileUrl(null));
        assertFalse(URLUtil.isFileUrl(VALID_CONTENT_URL));
        assertFalse(URLUtil.isFileUrl(VALID_ASSET_URL));
        assertFalse(URLUtil.isFileUrl(VALID_PROXY_URL));
        assertTrue(URLUtil.isFileUrl(VALID_FILE_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHttpsUrl",
            args = {String.class}
        )
    })
    public void testIsHttpsUrl() {
        assertFalse(URLUtil.isHttpsUrl(null));
        assertFalse(URLUtil.isHttpsUrl(VALID_HTTP_URL));
        assertTrue(URLUtil.isHttpsUrl(VALID_HTTPS_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHttpUrl",
            args = {String.class}
        )
    })
    public void testIsHttpUrl() {
        assertFalse(URLUtil.isHttpUrl(null));
        assertFalse(URLUtil.isHttpUrl(VALID_FTP_URL));
        assertTrue(URLUtil.isHttpUrl(VALID_HTTP_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isJavaScriptUrl",
            args = {String.class}
        )
    })
    public void testIsJavaScriptUrl() {
        assertFalse(URLUtil.isJavaScriptUrl(null));
        assertFalse(URLUtil.isJavaScriptUrl(VALID_FTP_URL));
        assertTrue(URLUtil.isJavaScriptUrl(VALID_JAVASCRIPT_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isNetworkUrl",
            args = {String.class}
        )
    })
    public void testIsNetworkUrl() {
        assertFalse(URLUtil.isNetworkUrl(null));
        assertFalse(URLUtil.isNetworkUrl(""));
        assertFalse(URLUtil.isNetworkUrl(VALID_FTP_URL));
        assertTrue(URLUtil.isNetworkUrl(VALID_HTTP_URL));
        assertTrue(URLUtil.isNetworkUrl(VALID_HTTPS_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isValidUrl",
            args = {String.class}
        )
    })
    public void testIsValidUrl() {
        assertFalse(URLUtil.isValidUrl(null));
        assertFalse(URLUtil.isValidUrl(""));
        assertFalse(URLUtil.isValidUrl("Error URL"));
        assertTrue(URLUtil.isValidUrl(VALID_ASSET_URL));
        assertTrue(URLUtil.isValidUrl(VALID_FILE_URL));
        assertTrue(URLUtil.isValidUrl(VALID_ABOUT_URL));
        assertTrue(URLUtil.isValidUrl(VALID_HTTP_URL));
        assertTrue(URLUtil.isValidUrl(VALID_HTTPS_URL));
        assertTrue(URLUtil.isValidUrl(VALID_JAVASCRIPT_URL));
        assertTrue(URLUtil.isValidUrl(VALID_CONTENT_URL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "composeSearchUrl",
            args = {String.class, String.class, String.class}
        )
    })
    public void testComposeSearchUrl() {
        assertNull(URLUtil.composeSearchUrl("", "template", "no such holder"));
        String expected = "http:
        assertEquals(expected, URLUtil.composeSearchUrl("story", "http:
        expected = "file:
        assertEquals(expected, URLUtil.composeSearchUrl("query", "file:
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "decode",
            args = {byte[].class}
        )
    })
    public void testDecode() {
        byte[] url = new byte[0];
        byte[] result = URLUtil.decode(url);
        assertEquals(0, result.length);
        url = new byte[] { 'w', 'w', 'w', '.', 'n', 'a', 'm', 'e', '.', 'c', 'o', 'm', '/',
                '%', '2', '0', '%', '4', '5', '/' };
        result = URLUtil.decode(url);
        byte[] expected = new byte[] { 'w', 'w', 'w', '.', 'n', 'a', 'm', 'e', '.', 'c', 'o', 'm',
                '/', ' ', 'E', '/' };
        MoreAsserts.assertEquals(expected, result);
        url = new byte[] { 'w', 'w', 'w', '.', 'n', 'a', 'm', 'e', '.', 'c', 'o', 'm', '/',
                '%', '2', '0', '%', '4' };
        try {
            result = URLUtil.decode(url);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "guessFileName",
            args = {String.class, String.class, String.class}
        )
    })
    public void testGuessFileName() {
        String url = "ftp:
        assertEquals("test.jpeg", URLUtil.guessFileName(url, null, "image/jpeg"));
        assertEquals("test.bin", URLUtil.guessFileName(url, null, "application/octet-stream"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "guessUrl",
            args = {String.class}
        )
    })
    public void testGuessUrl() {
        assertEquals(VALID_FILE_URL, URLUtil.guessUrl(VALID_FILE_URL));
        assertEquals(VALID_ABOUT_URL, URLUtil.guessUrl(VALID_ABOUT_URL));
        assertEquals(VALID_DATA_URL, URLUtil.guessUrl(VALID_DATA_URL));
        assertEquals(VALID_JAVASCRIPT_URL, URLUtil.guessUrl(VALID_JAVASCRIPT_URL));
        String url = "domainName";
        assertEquals("http:
        try {
            URLUtil.guessUrl(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stripAnchor",
            args = {String.class}
        )
    })
    public void testStripAnchor() {
        assertEquals(VALID_HTTP_URL, URLUtil.stripAnchor(VALID_HTTP_URL));
        String url = "http:
        String expected = "http:
        assertEquals(expected, URLUtil.stripAnchor(url));
    }
}
