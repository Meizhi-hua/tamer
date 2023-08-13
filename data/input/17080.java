public class NonJMXPrincipalsTest {
    private static class OtherPrincipal implements Principal, Serializable {
        private String name;
        public OtherPrincipal(String name) {
            if (name == null)
                throw new NullPointerException("illegal null input");
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public String toString() {
            return("OtherPrincipal:  " + name);
        }
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (this == o)
                return true;
            if (!(o instanceof OtherPrincipal))
                return false;
            OtherPrincipal that = (OtherPrincipal)o;
            return (this.getName().equals(that.getName()));
        }
        public int hashCode() {
            return name.hashCode();
        }
    }
    private static class OtherPrincipalAuthenticator
        implements JMXAuthenticator {
        public Subject authenticate(Object credentials) {
            final String[] aCredentials = (String[]) credentials;
            final String username = (String) aCredentials[0];
            final Subject subject = new Subject();
            subject.getPrincipals().add(new JMXPrincipal("dummy"));
            subject.getPrincipals().add(new OtherPrincipal(username));
            return subject;
        }
    }
    private static class NoPrincipalAuthenticator
        implements JMXAuthenticator {
        public Subject authenticate(Object credentials) {
            return new Subject();
        }
    }
    public static void runTest(JMXAuthenticator authenticator)
        throws Exception {
        System.out.println("Create the MBean server");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        ObjectName mbeanName = new ObjectName("MBeans:type=SimpleStandard");
        System.out.println("Create SimpleStandard MBean...");
        mbs.createMBean("SimpleStandard", mbeanName, null, null);
        System.out.println(">>> Initialize the server's environment map");
        HashMap sEnv = new HashMap();
        sEnv.put("jmx.remote.authenticator", authenticator);
        sEnv.put("jmx.remote.x.access.file",
                 System.getProperty("test.src") +
                 File.separator +
                 "access.properties");
        System.out.println("Create an RMI connector server");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
        JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, sEnv, mbs);
        System.out.println("Start the RMI connector server");
        cs.start();
        System.out.println("RMI connector server successfully started");
        System.out.println("Waiting for incoming connections...");
        String[] adminCreds = new String[] { "admin" , "adminPassword" };
        System.out.println(">>> Initialize the client environment map for" +
                           " user [" + adminCreds[0] + "] with " +
                           "password [" + adminCreds[1] + "]");
        HashMap adminEnv = new HashMap();
        adminEnv.put("jmx.remote.credentials", adminCreds);
        System.out.println("Create an RMI connector client and " +
                           "connect it to the RMI connector server");
        JMXConnector adminConnector =
            JMXConnectorFactory.connect(cs.getAddress(), adminEnv);
        System.out.println("Get an MBeanServerConnection");
        MBeanServerConnection adminConnection =
            adminConnector.getMBeanServerConnection();
        SimpleStandardMBean adminProxy = (SimpleStandardMBean)
            MBeanServerInvocationHandler.newProxyInstance(
                                                 adminConnection,
                                                 mbeanName,
                                                 SimpleStandardMBean.class,
                                                 false);
        System.out.println("State = " + adminProxy.getState());
        adminProxy.setState("changed state");
        System.out.println("State = " + adminProxy.getState());
        System.out.println("Invoke reset() in SimpleStandard MBean...");
        adminProxy.reset();
        System.out.println("Close the admin connection to the server");
        adminConnector.close();
        String[] userCreds = new String[] { "user" , "userPassword" };
        System.out.println(">>> Initialize the client environment map for" +
                           " user [" + userCreds[0] + "] with " +
                           "password [" + userCreds[1] + "]");
        HashMap userEnv = new HashMap();
        userEnv.put("jmx.remote.credentials", userCreds);
        System.out.println("Create an RMI connector client and " +
                           "connect it to the RMI connector server");
        JMXConnector userConnector =
            JMXConnectorFactory.connect(cs.getAddress(), userEnv);
        System.out.println("Get an MBeanServerConnection");
        MBeanServerConnection userConnection =
            userConnector.getMBeanServerConnection();
        SimpleStandardMBean userProxy = (SimpleStandardMBean)
            MBeanServerInvocationHandler.newProxyInstance(
                                                 userConnection,
                                                 mbeanName,
                                                 SimpleStandardMBean.class,
                                                 false);
        System.out.println("State = " + userProxy.getState());
        try {
            userProxy.setState("changed state");
        } catch (SecurityException e) {
            System.out.println("Got expected security exception: " + e);
        } catch (Exception e) {
            System.out.println("Got unexpected exception: " + e);
            e.printStackTrace(System.out);
        }
        System.out.println("State = " + userProxy.getState());
        try {
            System.out.println("Invoke reset() in SimpleStandard MBean...");
            userProxy.reset();
        } catch (SecurityException e) {
            System.out.println("Got expected security exception: " + e);
        } catch (Exception e) {
            System.out.println("Got unexpected exception: " + e);
            e.printStackTrace(System.out);
        }
        System.out.println("Close the user connection to the server");
        userConnector.close();
        System.out.println(">>> Stop the connector server");
        cs.stop();
    }
    public static void main(String[] args) {
        int errorCount = 0;
        System.out.println("\n>>> Run NoPrincipalAuthenticator test...");
        try {
            NonJMXPrincipalsTest.runTest(new NoPrincipalAuthenticator());
            System.out.println("Did not get expected SecurityException");
            errorCount++;
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                System.out.println("Got expected exception: " + e);
            } else {
                System.out.println("Got unexpected exception: " + e);
                errorCount++;
            }
            e.printStackTrace(System.out);
        }
        System.out.println("\n>>> Run OtherPrincipalAuthenticator test...");
        try {
            NonJMXPrincipalsTest.runTest(new OtherPrincipalAuthenticator());
        } catch (Exception e) {
            errorCount++;
            System.out.println("Got unexpected exception: " + e);
            e.printStackTrace(System.out);
        }
        if (errorCount > 0) {
            System.out.println("\nTEST FAILED! Error count = " + errorCount);
            System.exit(1);
        }
        System.out.println("\nTEST PASSED!");
        System.out.println("\nBye! Bye!");
    }
}
