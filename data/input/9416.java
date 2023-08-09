public class B6644726 {
    public static void main(String[] args) throws Exception {
        testCookieStore();
    }
    private static void testCookieStore() throws Exception {
        CookieManager cm = new CookieManager();
        CookieStore cs = cm.getCookieStore();
        URI uri = new URI("http:
        URI suri = new URI("https:
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        ArrayList<String> lst = new ArrayList<String>();
        lst.add("myCookie1=foo");
        lst.add("myCookie2=bar; path=/dir; expires=Tue, 19 Aug 2025 16:00:00 GMT");
        lst.add("myCookie3=test; path=/dir; expires=Tue Aug 19 2025 16:00:00 GMT-0100");
        lst.add("myCookie4=test; domain=.sun.com; path=/dir/foo");
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("Set-Cookie", lst);
        cm.put(uri, map);
        map.clear();
        lst.clear();
        lst.add("myCookie5=test; secure");
        map.put("Set-Cookie", lst);
        cm.put(suri, map);
        List<HttpCookie> cookies = cs.getCookies();
        if (cookies.size() != 5) {
            fail("Should have 5 cookies. Got only "+ cookies.size() + ", expires probably didn't parse correctly");
        }
        for (HttpCookie c : cookies) {
            if (c.getName().equals("myCookie1")) {
                if (!"/dir/foo/".equals(c.getPath())) {
                    fail("Default path for myCookie1 is " + c.getPath());
                }
            }
        }
        HashMap<String, List<String>> emptyMap = new HashMap<String, List<String>>();
        Map<String, List<String>>m = cm.get(new URI("http:
                emptyMap);
        List<String> clst = m.get("Cookie");
        if (clst.size() != 1) {
            fail("We should have only 1 cookie, not " + clst.size());
        } else {
            if (!clst.get(0).startsWith("myCookie4")) {
                fail("The cookie should be myCookie4, not " + clst.get(0));
            }
        }
        m = cm.get(suri, emptyMap);
        clst = m.get("Cookie");
        if (clst.size() != 5) {
            fail("Cookies didn't cross from http to https. Got only " + clst.size());
        }
        m = cm.get(uri, emptyMap);
        clst = m.get("Cookie");
        if (clst.size() != 4) {
            fail("We should have gotten only 4 cookies over http (non secure), got " +
                    clst.size());
        }
        if (isIn(clst, "myCookie5=")) {
            fail("Got the secure cookie over a non secure link");
        }
        uri = new URI("http:
        lst.clear();
        lst.add("myCookie6=foo");
        map.clear();
        map.put("Set-Cookie", lst);
        cm.put(uri, map);
        uri = new URI("http:
        m = cm.get(uri, emptyMap);
        clst = m.get("Cookie");
        if (clst.size() != 1) {
            fail("Missing a cookie when using an empty path");
        }
        uri = new URI("http:
        lst.clear();
        lst.add("myCookie7=foo");
        map.clear();
        map.put("Set-Cookie", lst);
        cm.put(uri, map);
        uri = new URI("http:
        m = cm.get(uri, emptyMap);
        clst = m.get("Cookie");
        if (!isIn(clst, "myCookie7=")) {
            fail("Missing a cookie when using an empty path");
        }
        lst.clear();
        lst.add("myCookie8=porttest; port");
        lst.add("myCookie9=porttest; port=\"80,8000\"");
        lst.add("myCookie10=porttest; port=\"8000\"");
        map.clear();
        map.put("Set-Cookie", lst);
        uri = new URI("http:
        cm.put(uri, map);
        cookies = cs.getCookies();
        for (HttpCookie c : cookies) {
            if (c.getName().equals("myCookie10")) {
                fail("A cookie with an invalid port list was accepted");
            }
        }
        uri = new URI("http:
        m = cm.get(uri, emptyMap);
        clst = m.get("Cookie");
        if (!isIn(clst, "myCookie8=") || !isIn(clst, "myCookie9=")) {
            fail("Missing a cookie on port 80");
        }
        uri = new URI("http:
        m = cm.get(uri, emptyMap);
        clst = m.get("Cookie");
        if (!isIn(clst, "myCookie9=")) {
            fail("Missing a cookie on port 80");
        }
        if (isIn(clst, "myCookie8=")) {
            fail("A cookie with an invalid port list was returned");
        }
        lst.clear();
        map.clear();
        cm.getCookieStore().removeAll();
        lst.add("myCookie11=httpOnlyTest; httpOnly");
        map.put("Set-Cookie", lst);
        uri = new URI("http:
        cm.put(uri, map);
        m = cm.get(uri, emptyMap);
        clst = m.get("Cookie");
        if (!isIn(clst, "myCookie11=")) {
            fail("Missing cookie with httpOnly flag");
        }
        uri = new URI("javascript:
        m = cm.get(uri, emptyMap);
        clst = m.get("Cookie");
        if (isIn(clst, "myCookie11=")) {
            fail("Should get the cookie with httpOnly when scheme is javascript:");
        }
    }
    private static boolean isIn(List<String> lst, String cookie) {
        if (lst == null || lst.isEmpty()) {
            return false;
        }
        for (String s : lst) {
            if (s.startsWith(cookie))
                return true;
        }
        return false;
    }
    private static void fail(String msg) throws Exception {
        throw new RuntimeException(msg);
    }
}
