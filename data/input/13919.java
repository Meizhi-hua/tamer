public class NoServerTimeoutTest {
    public static void main(String[] args) throws Exception {
        boolean ok = true;
        for (String proto : new String[] {"rmi", "iiop", "jmxmp"}) {
            JMXServiceURL url = new JMXServiceURL(proto, null, 0);
            try {
                MBeanServer mbs = MBeanServerFactory.newMBeanServer();
                JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                null,
                                                                mbs);
            } catch (MalformedURLException e) {
                System.out.println();
                System.out.println("Ignoring protocol: " + proto);
                continue;
            }
            try {
                ok &= test(url);
            } catch (Exception e) {
                System.out.println("TEST FAILED WITH EXCEPTION:");
                e.printStackTrace();
                ok = false;
            }
        }
        System.out.println();
        if (ok)
            System.out.println("Test passed");
        else
            throw new Exception("Test failed");
    }
    private static enum Test {
        NO_ENV("No Map for connector server"),
        EMPTY_ENV("Empty Map for connector server"),
        PLAIN_TIMEOUT("Map with two-minute timeout as int"),
        PLAIN_STRING_TIMEOUT("Map with two-minute timeout as string"),
        INFINITE_TIMEOUT("Map with Long.MAX_VALUE timeout as long"),
        INFINITE_STRING_TIMEOUT("Map with Long.MAX_VALUE timeout as string");
        Test(String description) {
            this.description = description;
        }
        public String toString() {
            return description;
        }
        private final String description;
    }
    private static final Set<Test> expectThread =
        EnumSet.copyOf(Arrays.asList(Test.NO_ENV,
                                     Test.EMPTY_ENV,
                                     Test.PLAIN_TIMEOUT,
                                     Test.PLAIN_STRING_TIMEOUT));
    private static boolean test(JMXServiceURL url) throws Exception {
        System.out.println();
        System.out.println("Test: " + url);
        boolean ok = true;
        for (Test test : Test.values())
            ok &= test(url, test);
        return ok;
    }
    private static final String TIMEOUT =
        "jmx.remote.x.server.connection.timeout";
    private static boolean test(JMXServiceURL url, Test test)
            throws Exception {
        System.out.println("* " + test);
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        Map<String, Object> env = new HashMap<String, Object>();
        switch (test) {
        case NO_ENV: env = null; break;
        case EMPTY_ENV: break;
        case PLAIN_TIMEOUT: env.put(TIMEOUT, 120 * 1000L); break;
        case PLAIN_STRING_TIMEOUT: env.put(TIMEOUT, (120 * 1000L) + ""); break;
        case INFINITE_TIMEOUT: env.put(TIMEOUT, Long.MAX_VALUE); break;
        case INFINITE_STRING_TIMEOUT: env.put(TIMEOUT, "" + Long.MAX_VALUE);
            break;
        default: throw new AssertionError();
        }
        for (int i = 0; i < 10 && countTimeoutThreads() > 0; i++)
            Thread.sleep(500);
        if (countTimeoutThreads() > 0) {
            System.out.println("TIMEOUT THREAD(S) WOULD NOT GO AWAY");
            return false;
        }
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        JMXConnector cc = JMXConnectorFactory.connect(addr);
        MBeanServerConnection mbsc = cc.getMBeanServerConnection();
        mbsc.getDefaultDomain();
        int expectTimeoutThreads = expectThread.contains(test) ? 1 : 0;
        int timeoutThreads = countTimeoutThreads();
        boolean ok = (expectTimeoutThreads == timeoutThreads);
        if (!ok) {
            System.out.println("TEST FAILS: Expected timeout threads: " +
                               expectTimeoutThreads +
                               "; actual timeout threads: " + timeoutThreads);
            ok = false;
        }
        cc.close();
        cs.stop();
        return ok;
    }
    private static int countTimeoutThreads() {
        ThreadMXBean mb = ManagementFactory.getThreadMXBean();
        int count = 0;
        long[] ids = mb.getAllThreadIds();
        for (ThreadInfo ti : mb.getThreadInfo(ids)) {
            if (ti != null &&
                ti.getThreadName().startsWith("JMX server connection timeout"))
                count++;
        }
        return count;
    }
}
