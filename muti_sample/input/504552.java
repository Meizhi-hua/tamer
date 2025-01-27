public class PatternsTest extends TestCase {
    @SmallTest
    public void testTldPattern() throws Exception {
        boolean t;
        t = Patterns.TOP_LEVEL_DOMAIN.matcher("com").matches();
        assertTrue("Missed valid TLD", t);
        t = Patterns.TOP_LEVEL_DOMAIN.matcher("me").matches();
        assertTrue("Missed valid TLD", t);
        t = Patterns.TOP_LEVEL_DOMAIN.matcher("xn--0zwm56d").matches();
        assertTrue("Missed valid TLD", t);
        t = Patterns.TOP_LEVEL_DOMAIN.matcher("mem").matches();
        assertFalse("Matched invalid TLD!", t);
        t = Patterns.TOP_LEVEL_DOMAIN.matcher("xn").matches();
        assertFalse("Matched invalid TLD!", t);
        t = Patterns.TOP_LEVEL_DOMAIN.matcher("xer").matches();
        assertFalse("Matched invalid TLD!", t);
    }
    @SmallTest
    public void testUrlPattern() throws Exception {
        boolean t;
        t = Patterns.WEB_URL.matcher("http:
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("http:
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("google.me").matches();
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("http:
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("xn--fsqu00a.xn--0zwm56d").matches();
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("http:
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("\uD604\uAE08\uC601\uC218\uC99D.kr").matches();
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("http:
            "top-five-moments-from-eric-schmidt\u2019s-talk-in-abu-dhabi/").matches();
        assertTrue("Valid URL", t);
        t = Patterns.WEB_URL.matcher("ftp:
        assertFalse("Matched invalid protocol", t);
        t = Patterns.WEB_URL.matcher("http:
        assertTrue("Didn't match valid URL with port", t);
        t = Patterns.WEB_URL.matcher("http:
        assertTrue("Didn't match valid URL with port and query args", t);
        t = Patterns.WEB_URL.matcher("http:
        assertTrue("Didn't match valid URL with ~", t);
    }
    @SmallTest
    public void testIpPattern() throws Exception {
        boolean t;
        t = Patterns.IP_ADDRESS.matcher("172.29.86.3").matches();
        assertTrue("Valid IP", t);
        t = Patterns.IP_ADDRESS.matcher("1234.4321.9.9").matches();
        assertFalse("Invalid IP", t);
    }
    @SmallTest
    public void testDomainPattern() throws Exception {
        boolean t;
        t = Patterns.DOMAIN_NAME.matcher("mail.example.com").matches();
        assertTrue("Valid domain", t);
        t = Patterns.WEB_URL.matcher("google.me").matches();
        assertTrue("Valid domain", t);
        t = Patterns.DOMAIN_NAME.matcher("\uD604\uAE08\uC601\uC218\uC99D.kr").matches();
        assertTrue("Valid domain", t);
        t = Patterns.DOMAIN_NAME.matcher("__+&42.xer").matches();
        assertFalse("Invalid domain", t);
    }
    @SmallTest
    public void testPhonePattern() throws Exception {
        boolean t;
        t = Patterns.PHONE.matcher("(919) 555-1212").matches();
        assertTrue("Valid phone", t);
        t = Patterns.PHONE.matcher("2334 9323/54321").matches();
        assertFalse("Invalid phone", t);
        String[] tests = {
                "Me: 16505551212 this\n",
                "Me: 6505551212 this\n",
                "Me: 5551212 this\n",
                "Me: 1-650-555-1212 this\n",
                "Me: (650) 555-1212 this\n",
                "Me: +1 (650) 555-1212 this\n",
                "Me: +1-650-555-1212 this\n",
                "Me: 650-555-1212 this\n",
                "Me: 555-1212 this\n",
                "Me: 1.650.555.1212 this\n",
                "Me: (650) 555.1212 this\n",
                "Me: +1 (650) 555.1212 this\n",
                "Me: +1.650.555.1212 this\n",
                "Me: 650.555.1212 this\n",
                "Me: 555.1212 this\n",
                "Me: 1 650 555 1212 this\n",
                "Me: (650) 555 1212 this\n",
                "Me: +1 (650) 555 1212 this\n",
                "Me: +1 650 555 1212 this\n",
                "Me: 650 555 1212 this\n",
                "Me: 555 1212 this\n",
        };
        for (String test : tests) {
            Matcher m = Patterns.PHONE.matcher(test);
            assertTrue("Valid phone " + test, m.find());
        }
    }
}
