public class NotificationEmissionTest {
    public class CustomJMXAuthenticator implements JMXAuthenticator {
        public Subject authenticate(Object credentials) {
            String role = ((String[]) credentials)[0];
            echo("Create principal with name = " + role);
            return new Subject(true,
                               Collections.singleton(new JMXPrincipal(role)),
                               Collections.EMPTY_SET,
                               Collections.EMPTY_SET);
        }
    }
    public interface NBMBean {
        public void emitNotification(int seqnum, ObjectName name);
    }
    public static class NB
        extends NotificationBroadcasterSupport
        implements NBMBean {
        public void emitNotification(int seqnum, ObjectName name) {
            if (name == null) {
                sendNotification(new Notification("nb", this, seqnum));
            } else {
                sendNotification(new Notification("nb", name, seqnum));
            }
        }
    }
    public class Listener implements NotificationListener {
        public List<Notification> notifs = new ArrayList<Notification>();
        public void handleNotification(Notification n, Object h) {
            echo("handleNotification:");
            echo("\tNotification = " + n);
            echo("\tNotification.SeqNum = " + n.getSequenceNumber());
            echo("\tHandback = " + h);
            notifs.add(n);
        }
    }
    public int checkNotifs(int size,
                           List<Notification> received,
                           List<ObjectName> expected) {
        if (received.size() != size) {
            echo("Error: expecting " + size + " notifications, got " +
                    received.size());
            return 1;
        } else {
            for (Notification n : received) {
                echo("Received notification: " + n);
                if (!n.getType().equals("nb")) {
                    echo("Notification type must be \"nb\"");
                    return 1;
                }
                ObjectName o = (ObjectName) n.getSource();
                int index = (int) n.getSequenceNumber();
                ObjectName nb = expected.get(index);
                if (!o.equals(nb)) {
                    echo("Notification source must be " + nb);
                    return 1;
                }
            }
        }
        return 0;
    }
    public int runTest(int testcase) throws Exception {
        echo("\n=-=-= Running testcase " + testcase + " =-=-=");
        switch (testcase) {
            case 1:
                return testNotificationEmissionProperty();
            case 2:
                return testNotificationEmissionPositive(false);
            case 3:
                return testNotificationEmissionNegative(false);
            case 4:
                return testNotificationEmissionPositive(true);
            case 5:
                return testNotificationEmissionNegative(true);
            default:
                echo("Invalid testcase");
                return 1;
        }
    }
    public int testNotificationEmissionProperty(boolean exception,
                                                Object propValue)
        throws Exception {
        try {
            testNotificationEmission(propValue);
            if (exception) {
                echo("Did not get expected exception for value: " + propValue);
                return 1;
            } else {
                echo("Property has been correctly set to value: " + propValue);
            }
        } catch (Exception e) {
            if (exception) {
                echo("Got expected exception for value: " + propValue);
                echo("Exception: " + e);
            } else {
                echo("Got unexpected exception for value: " + propValue);
                echo("Exception: " + e);
                return 1;
            }
        }
        return 0;
    }
    public int testNotificationEmissionProperty() throws Exception {
        int error = 0;
        error += testNotificationEmissionProperty(true, new Boolean(false));
        error += testNotificationEmissionProperty(true, new Boolean(true));
        error += testNotificationEmissionProperty(true, "dummy");
        error += testNotificationEmissionProperty(false, "false");
        error += testNotificationEmissionProperty(false, "true");
        error += testNotificationEmissionProperty(false, "FALSE");
        error += testNotificationEmissionProperty(false, "TRUE");
        return error;
    }
    public int testNotificationEmissionPositive(boolean prop) throws Exception {
        return testNotificationEmission(prop, "true", true, true);
    }
    public int testNotificationEmissionNegative(boolean prop) throws Exception {
        return testNotificationEmission(prop, "true", true, false);
    }
    public int testNotificationEmission(Object propValue) throws Exception {
        return testNotificationEmission(true, propValue, false, true);
    }
    public int testNotificationEmission(boolean prop,
                                        Object propValue,
                                        boolean sm,
                                        boolean policyPositive)
        throws Exception {
        JMXConnectorServer server = null;
        JMXConnector client = null;
        String policyFile =
            System.getProperty("test.src") + File.separator +
            (policyPositive ? "policy.positive" : "policy.negative");
        echo("\nSetting policy file " + policyFile);
        System.setProperty("java.security.policy", policyFile);
        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            final Map<String,Object> env = new HashMap<String,Object>();
            env.put("jmx.remote.authenticator", new CustomJMXAuthenticator());
            if (prop)
                env.put("jmx.remote.x.check.notification.emission", propValue);
            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
            server = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                     env,
                                                                     mbs);
            server.start();
            final Map<String,Object> cenv = new HashMap<String,Object>();
            String[] credentials = new String[] { "role" , "password" };
            cenv.put("jmx.remote.credentials", credentials);
            client = JMXConnectorFactory.connect(server.getAddress(), cenv);
            final MBeanServerConnection mbsc =
                client.getMBeanServerConnection();
            ObjectName nb1 = ObjectName.getInstance("domain:type=NB,name=1");
            ObjectName nb2 = ObjectName.getInstance("domain:type=NB,name=2");
            ObjectName nb3 = ObjectName.getInstance("domain:type=NB,name=3");
            mbsc.createMBean(NB.class.getName(), nb1);
            mbsc.createMBean(NB.class.getName(), nb2);
            mbsc.createMBean(NB.class.getName(), nb3);
            Listener li = new Listener();
            mbsc.addNotificationListener(nb1, li, null, null);
            mbsc.addNotificationListener(nb2, li, null, null);
            if (sm) {
                echo("Setting SM");
                System.setSecurityManager(new SecurityManager());
            }
            mbsc.invoke(nb1, "emitNotification",
                new Object[] {0, null},
                new String[] {"int", "javax.management.ObjectName"});
            mbsc.invoke(nb2, "emitNotification",
                new Object[] {1, null},
                new String[] {"int", "javax.management.ObjectName"});
            mbsc.invoke(nb2, "emitNotification",
                new Object[] {2, nb3},
                new String[] {"int", "javax.management.ObjectName"});
            int expectedNotifs =
                    (prop && sm && !policyPositive) ? 2 : 3;
            long deadline = System.currentTimeMillis() + 2000;
            while (li.notifs.size() < expectedNotifs &&
                    System.currentTimeMillis() < deadline)
                Thread.sleep(1);
            mbsc.removeNotificationListener(nb1, li);
            mbsc.removeNotificationListener(nb2, li);
            int result = 0;
            List<ObjectName> sources = new ArrayList<ObjectName>();
            sources.add(nb1);
            sources.add(nb2);
            sources.add(nb3);
            result = checkNotifs(expectedNotifs, li.notifs, sources);
            if (result > 0) {
                echo("...SecurityManager=" + sm + "; policy=" + policyPositive);
                return result;
            }
        } finally {
            if (client != null)
                client.close();
            if (server != null)
                server.stop();
            if (mbs != null)
                MBeanServerFactory.releaseMBeanServer(mbs);
        }
        return 0;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main(String[] args) throws Exception {
        echo("\n--- Check the emission of notifications " +
             "when a Security Manager is installed");
        NotificationEmissionTest net = new NotificationEmissionTest();
        int error = 0;
        error += net.runTest(Integer.parseInt(args[0]));
        if (error > 0) {
            final String msg = "\nTest FAILED! Got " + error + " error(s)";
            echo(msg);
            throw new IllegalArgumentException(msg);
        } else {
            echo("\nTest PASSED!");
        }
    }
}
