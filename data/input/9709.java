public class ConnectionTest {
    public static void main(String[] args) {
        boolean ok = true;
        String[] protocols = {"rmi", "iiop", "jmxmp"};
        if (args.length > 0)
            protocols = args;
        for (int i = 0; i < protocols.length; i++) {
            final String proto = protocols[i];
            System.out.println("Testing for protocol " + proto);
            try {
                ok &= test(proto);
            } catch (Exception e) {
                System.err.println("Unexpected exception: " + e);
                e.printStackTrace();
                ok = false;
            }
        }
        if (ok)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
    private static boolean test(String proto) throws Exception {
        ObjectName serverName = ObjectName.getInstance("d:type=server");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        JMXAuthenticator authenticator = new BogusAuthenticator();
        Map env = Collections.singletonMap("jmx.remote.authenticator",
                                           authenticator);
        JMXServiceURL url = new JMXServiceURL("service:jmx:" + proto + ":
        JMXConnectorServer server;
        try {
            server =
                JMXConnectorServerFactory.newJMXConnectorServer(url, env,
                                                                null);
        } catch (MalformedURLException e) {
            System.out.println("Protocol " + proto +
                               " not supported, ignoring");
            return true;
        }
        System.out.println("Created connector server");
        mbs.registerMBean(server, serverName);
        System.out.println("Registered connector server in MBean server");
        mbs.addNotificationListener(serverName, logListener, null, null);
        mbs.invoke(serverName, "start", null, null);
        System.out.println("Started connector server");
        JMXServiceURL address =
            (JMXServiceURL) mbs.getAttribute(serverName, "Address");
        System.out.println("Retrieved address: " + address);
        if (address.getHost().length() == 0) {
            System.out.println("Generated address has empty hostname");
            return false;
        }
        JMXConnector client = JMXConnectorFactory.connect(address);
        System.out.println("Client connected");
        String clientConnId = client.getConnectionId();
        System.out.println("Got connection ID on client: " + clientConnId);
        boolean ok = checkConnectionId(proto, clientConnId);
        if (!ok)
            return false;
        System.out.println("Connection ID is OK");
        Notification notif = waitForNotification(1000);
        System.out.println("Server got notification: " + notif);
        ok = mustBeConnectionNotification(notif, clientConnId,
                                         JMXConnectionNotification.OPENED);
        if (!ok)
            return false;
        client.close();
        System.out.println("Closed client");
        notif = waitForNotification(1000);
        System.out.println("Got notification: " + notif);
        ok = mustBeConnectionNotification(notif, clientConnId,
                                          JMXConnectionNotification.CLOSED);
        if (!ok)
            return false;
        client = JMXConnectorFactory.connect(address);
        System.out.println("Second client connected");
        String clientConnId2 = client.getConnectionId();
        if (clientConnId.equals(clientConnId2)) {
            System.out.println("Same connection ID for two connections: " +
                               clientConnId2);
            return false;
        }
        System.out.println("Second client connection ID is different");
        notif = waitForNotification(1);
        ok = mustBeConnectionNotification(notif, clientConnId2,
                                          JMXConnectionNotification.OPENED);
        if (!ok)
            return false;
        MBeanServerConnection mbsc = client.getMBeanServerConnection();
        Map attrs = (Map) mbsc.getAttribute(serverName, "Attributes");
        System.out.println("Server attributes received by client: " + attrs);
        server.stop();
        System.out.println("Server stopped");
        notif = waitForNotification(1000);
        System.out.println("Server got connection-closed notification: " +
                           notif);
        ok = mustBeConnectionNotification(notif, clientConnId2,
                                          JMXConnectionNotification.CLOSED);
        if (!ok)
            return false;
        try {
            mbsc.getDefaultDomain();
            System.out.println("Connection still working but should not be");
            return false;
        } catch (IOException e) {
            System.out.println("Connection correctly got exception: " + e);
        }
        try {
            client = JMXConnectorFactory.connect(address);
            System.out.println("Connector server still working but should " +
                               "not be");
            return false;
        } catch (IOException e) {
            System.out.println("New connection correctly got exception: " + e);
        }
        return true;
    }
    private static boolean
        mustBeConnectionNotification(Notification notif,
                                     String requiredConnId,
                                     String requiredType) {
        if (!(notif instanceof JMXConnectionNotification)) {
            System.out.println("Should have been a " +
                               "JMXConnectionNotification: " +
                               notif.getClass());
            return false;
        }
        JMXConnectionNotification cnotif = (JMXConnectionNotification) notif;
        if (!cnotif.getType().equals(requiredType)) {
            System.out.println("Wrong type notif: is \"" + cnotif.getType() +
                               "\", should be \"" + requiredType + "\"");
            return false;
        }
        if (!cnotif.getConnectionId().equals(requiredConnId)) {
            System.out.println("Wrong connection id: is \"" +
                               cnotif.getConnectionId() + "\", should be \"" +
                               requiredConnId);
            return false;
        }
        return true;
    }
    private static boolean checkConnectionId(String proto, String clientConnId)
            throws Exception {
        StringTokenizer tok = new StringTokenizer(clientConnId, " ", true);
        String s;
        s = tok.nextToken();
        if (!s.startsWith(proto + ":")) {
            System.out.println("Expected \"" + proto + ":\", found \"" + s +
                               "\"");
            return false;
        }
        s = tok.nextToken();
        if (!s.equals(" ")) {
            System.out.println("Expected \" \", found \"" + s + "\"");
            return false;
        }
        s = tok.nextToken();
        StringTokenizer tok2 = new StringTokenizer(s, ";", true);
        Set principalNames = new HashSet();
        String s2;
        s2 = tok2.nextToken();
        if (s2.equals(";")) {
            System.out.println("In identity \"" + s +
                               "\", expected name, found \";\"");
            return false;
        }
        principalNames.add(s2);
        s2 = tok2.nextToken();
        if (!s2.equals(";"))
            throw new Exception("Can't happen");
        s2 = tok2.nextToken();
        if (s2.equals(";")) {
            System.out.println("In identity \"" + s +
                               "\", expected name, found \";\"");
            return false;
        }
        principalNames.add(s2);
        if (tok2.hasMoreTokens()) {
            System.out.println("In identity \"" + s + "\", too many tokens");
            return false;
        }
        if (principalNames.size() != bogusPrincipals.size()) {
            System.out.println("Wrong number of principal names: " +
                               principalNames.size() + " != " +
                               bogusPrincipals.size());
            return false;
        }
        for (Iterator it = bogusPrincipals.iterator(); it.hasNext(); ) {
            Principal p = (Principal) it.next();
            if (!principalNames.contains(p.getName())) {
                System.out.println("Principal names don't contain \"" +
                                   p.getName() + "\"");
                return false;
            }
        }
        s = tok.nextToken();
        if (!s.equals(" ")) {
            System.out.println("Expected \" \", found \"" + s + "\"");
            return false;
        }
        return true;
    }
    private static Notification waitForNotification(long timeout)
            throws InterruptedException {
        synchronized (log) {
            if (log.isEmpty()) {
                long remainingTime = timeout;
                final long startTime = System.currentTimeMillis();
                while (log.isEmpty() && remainingTime >0) {
                    log.wait(remainingTime);
                    remainingTime = timeout - (System.currentTimeMillis() - startTime);
                }
                if (log.isEmpty()) {
                    throw new InterruptedException("Timed out waiting for " +
                                                   "notification!");
                }
            }
            return (Notification) log.remove(0);
        }
    }
    private static class LogListener implements NotificationListener {
        LogListener(List log) {
            this.log = log;
        }
        public void handleNotification(Notification n, Object h) {
            synchronized (log) {
                log.add(n);
                log.notifyAll();
            }
        }
        private final List log;
    }
    private static List log = new LinkedList();
    private static NotificationListener logListener = new LogListener(log);
    private static class BogusAuthenticator implements JMXAuthenticator {
        public Subject authenticate(Object credentials) {
            Subject subject =
                new Subject(true, bogusPrincipals,
                            Collections.EMPTY_SET, Collections.EMPTY_SET);
            System.out.println("Authenticator returns: " + subject);
            return subject;
        }
    }
    private static final Set bogusPrincipals = new HashSet();
    static {
        bogusPrincipals.add(new JMXPrincipal("foo"));
        bogusPrincipals.add(new JMXPrincipal("bar"));
    }
}
