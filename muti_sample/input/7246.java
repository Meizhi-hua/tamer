public class JvmstatCountersTest {
    public static void checkAddress(String address) throws IOException {
        System.out.println("Address = " + address);
        JMXServiceURL url = new JMXServiceURL(address);
        JMXConnector jmxc = JMXConnectorFactory.connect(url);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        System.out.println("MBean Count = " + mbsc.getMBeanCount());
    }
    public static void checkKey(Map<String, String> data, int index,
            String key, String expectedValue) throws Exception {
        String counter = "sun.management.JMXConnectorServer." + index + "." + key;
        if (!data.containsKey(counter)) {
            System.out.println("Test FAILED! Missing counter " + counter);
            throw new IllegalArgumentException("Test case failed");
        }
        String value = data.get(counter);
        if (key.equals("remoteAddress")) {
            checkAddress(value);
        } else if (!expectedValue.equals(value)) {
            System.out.println("Test FAILED! Invalid counter " +
                    counter + "=" + value);
            throw new IllegalArgumentException("Test case failed");
        }
        System.out.println("OK: " + counter + "=" + value);
    }
    public static void main(String args[]) throws Exception {
        String localAddress = ConnectorAddressLink.importFrom(0);
        Map<String, String> remoteData = ConnectorAddressLink.importRemoteFrom(0);
        final int testCase = Integer.parseInt(args[0]);
        switch (testCase) {
            case 1:
                if (localAddress == null && remoteData.isEmpty()) {
                    System.out.println("Test PASSED! The OOTB management " +
                            "agent didn't publish any jvmstat counter.");
                } else {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent unexpectedly published jvmstat counters.");
                    throw new IllegalArgumentException("Test case 1 failed");
                }
                break;
            case 2:
                if (localAddress == null) {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent didn't publish the local connector.");
                    throw new IllegalArgumentException("Test case 2 failed");
                }
                checkAddress(localAddress);
                if (!remoteData.isEmpty()) {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent shouldn't publish the remote connector.");
                    throw new IllegalArgumentException("Test case 2 failed");
                }
                System.out.println("Test PASSED! The OOTB management " +
                        "agent only publishes the local connector through " +
                        "a jvmstat counter.");
                break;
            case 3:
                if (localAddress == null) {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent didn't publish the local connector.");
                    throw new IllegalArgumentException("Test case 3 failed");
                }
                checkAddress(localAddress);
                if (remoteData.isEmpty()) {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent didnn't publish the remote connector.");
                    throw new IllegalArgumentException("Test case 3 failed");
                }
                for (String key : remoteData.keySet()) {
                    if (!key.startsWith("sun.management.JMXConnectorServer.0.")) {
                        System.out.println("Test FAILED! The OOTB management " +
                                "agent shouldn't publish anything which isn't " +
                                "related to the remote connector.");
                        throw new IllegalArgumentException("Test case 3 failed");
                    }
                }
                checkKey(remoteData, 0, "remoteAddress", null);
                checkKey(remoteData, 0, "authenticate", "false");
                checkKey(remoteData, 0, "ssl", "false");
                checkKey(remoteData, 0, "sslRegistry", "false");
                checkKey(remoteData, 0, "sslNeedClientAuth", "false");
                System.out.println("Test PASSED! The OOTB management " +
                        "agent publishes both the local and remote " +
                        "connector info through jvmstat counters.");
                break;
            case 4:
                if (localAddress != null || !remoteData.isEmpty()) {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent unexpectedly published jvmstat counters.");
                    throw new IllegalArgumentException("Test case 4 failed");
                }
                RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
                String name = rt.getName();
                System.out.println("name = " + name);
                String vmid = name.substring(0, name.indexOf("@"));
                System.out.println("vmid = " + vmid);
                VirtualMachine vm = VirtualMachine.attach(vmid);
                String agent = vm.getSystemProperties().getProperty("java.home") +
                        File.separator + "lib" + File.separator + "management-agent.jar";
                vm.loadAgent(agent, "com.sun.management.jmxremote.port=0,com.sun.management.jmxremote.authenticate=false,com.sun.management.jmxremote.ssl=false");
                vm.detach();
                String localAddress2 = ConnectorAddressLink.importFrom(0);
                if (localAddress2 == null) {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent didn't publish the local connector.");
                    throw new IllegalArgumentException("Test case 4 failed");
                }
                checkAddress(localAddress2);
                Map<String, String> remoteData2 = ConnectorAddressLink.importRemoteFrom(0);
                if (remoteData2.isEmpty()) {
                    System.out.println("Test FAILED! The OOTB management " +
                            "agent didnn't publish the remote connector.");
                    throw new IllegalArgumentException("Test case 4 failed");
                }
                for (String key : remoteData2.keySet()) {
                    if (!key.startsWith("sun.management.JMXConnectorServer.0.")) {
                        System.out.println("Test FAILED! The OOTB management " +
                                "agent shouldn't publish anything which isn't " +
                                "related to the remote connector.");
                        throw new IllegalArgumentException("Test case 4 failed");
                    }
                }
                checkKey(remoteData2, 0, "remoteAddress", null);
                checkKey(remoteData2, 0, "authenticate", "false");
                checkKey(remoteData2, 0, "ssl", "false");
                checkKey(remoteData2, 0, "sslRegistry", "false");
                checkKey(remoteData2, 0, "sslNeedClientAuth", "false");
                System.out.println("Test PASSED! The OOTB management agent " +
                        "publishes both the local and remote connector " +
                        "info through jvmstat counters when the agent is " +
                        "loaded through the Attach API.");
        }
        System.out.println("Bye! Bye!");
    }
}
