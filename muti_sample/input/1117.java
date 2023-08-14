public class RunToExit {
    static int error_seen = 0;
    static volatile boolean ready = false;
    static class IOHandler implements Runnable {
        private String              name;
        private BufferedInputStream in;
        private StringBuffer        buffer;
        IOHandler(String name, InputStream in) {
            this.name = name;
            this.in = new BufferedInputStream(in);
            this.buffer = new StringBuffer();
        }
        static void handle(String name, InputStream in) {
            IOHandler handler = new IOHandler(name, in);
            Thread thr = new Thread(handler);
            thr.setDaemon(true);
            thr.start();
        }
        public void run() {
            try {
                byte b[] = new byte[100];
                for (;;) {
                    int n = in.read(b, 0, 100);
                    ready = true;
                    if (n < 0) {
                        break;
                    }
                    buffer.append(new String(b, 0, n));
                }
            } catch (IOException ioe) { }
            String str = buffer.toString();
            if ( str.contains("ERROR:") ) {
                error_seen++;
            }
            System.out.println(name + ": " + str);
        }
    }
    private static Connector findConnector(String name) {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector)iter.next();
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        return null;
    }
    private static Process launch(String address, String class_name) throws IOException {
        String exe =   System.getProperty("java.home")
                     + File.separator + "bin" + File.separator;
        String arch = System.getProperty("os.arch");
        String osname = System.getProperty("os.name");
        if (osname.equals("SunOS") && arch.equals("sparcv9")) {
            exe += "sparcv9/java";
        } else if (osname.equals("SunOS") && arch.equals("amd64")) {
            exe += "amd64/java";
        } else {
            exe += "java";
        }
        String cmd = exe + " " + VMConnection.getDebuggeeVMOptions() +
            " -agentlib:jdwp=transport=dt_socket" +
            ",server=y" + ",suspend=y" + ",address=" + address +
            " " + class_name;
        System.out.println("Starting: " + cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        IOHandler.handle("Input Stream", p.getInputStream());
        IOHandler.handle("Error Stream", p.getErrorStream());
        return p;
    }
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();
        String address = String.valueOf(port);
        Process process = launch(address, "Exit0");
        while (!ready) {
            try {
                Thread.sleep(1000);
            } catch(Exception ee) {
                throw ee;
            }
        }
        AttachingConnector conn = (AttachingConnector)findConnector("com.sun.jdi.SocketAttach");
        Map conn_args = conn.defaultArguments();
        Connector.IntegerArgument port_arg =
            (Connector.IntegerArgument)conn_args.get("port");
        port_arg.setValue(port);
        VirtualMachine vm = conn.attach(conn_args);
        EventSet evtSet = vm.eventQueue().remove();
        for (Event event: evtSet) {
            if (event instanceof VMStartEvent) {
                break;
            }
            throw new RuntimeException("Test failed - debuggee did not start properly");
        }
        vm.eventRequestManager().deleteAllBreakpoints();
        vm.resume();
        int exitCode = process.waitFor();
        if (exitCode == 0 && error_seen == 0) {
            System.out.println("Test passed - server debuggee cleanly terminated");
        } else {
            throw new RuntimeException("Test failed - server debuggee generated an error when it terminated");
        }
    }
}