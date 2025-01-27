class VMConnection {
    private VirtualMachine vm;
    private Process process = null;
    private int outputCompleteCount = 0;
    private final Connector connector;
    private final Map connectorArgs;
    private final int traceFlags;
    static public String getDebuggeeVMOptions() {
        BufferedReader reader;
        final String filename = "@debuggeeVMOptions";
        String srcDir = System.getProperty("test.src");
        if (srcDir == null) {
          srcDir = System.getProperty("user.dir");
        }
        srcDir = srcDir + File.separator;
        File myDir = new File(srcDir);
        File myFile = new File(myDir, filename);
        if (!myFile.canRead()) {
            try {
                myFile = new File(myDir.getCanonicalFile().getParent(),
                                  filename);
                if (!myFile.canRead()) {
                    return "";
                }
            } catch (IOException ee) {
                System.out.println("-- Error 1 trying to access file " +
                                   myFile.getPath() + ": " + ee);
                return "";
            }
        }
        String wholePath = myFile.getPath();
        try {
            reader = new BufferedReader(new FileReader(myFile));
        } catch (FileNotFoundException ee) {
            System.out.println("-- Error 2 trying to access file " +
                               wholePath + ": " + ee);
            return "";
        }
        String line;
        String retVal = "";
        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException ee) {
                System.out.println("-- Error reading options from file " +
                                   wholePath + ": " + ee);
                break;
            }
            if (line == null) {
                System.out.println("-- No debuggee VM options found in file " +
                                   wholePath);
                break;
            }
            line = line.trim();
            if (line.length() != 0 && !line.startsWith("#")) {
                System.out.println("-- Added debuggeeVM options from file " +
                                   wholePath + ": " + line);
                retVal = line;
                break;
            }
        }
        try {
            reader.close();
        } catch (IOException ee) {
        }
        return retVal;
    }
    private Connector findConnector(String name) {
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
    private Map parseConnectorArgs(Connector connector, String argString) {
        StringTokenizer tokenizer = new StringTokenizer(argString, ",");
        Map arguments = connector.defaultArguments();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int index = token.indexOf('=');
            if (index == -1) {
                throw new IllegalArgumentException("Illegal connector argument: " +
                                                   token);
            }
            String name = token.substring(0, index);
            String value = token.substring(index + 1);
            Connector.Argument argument = (Connector.Argument)arguments.get(name);
            if (argument == null) {
                throw new IllegalArgumentException("Argument " + name +
                                               "is not defined for connector: " +
                                               connector.name());
            }
            argument.setValue(value);
        }
        return arguments;
    }
    VMConnection(String connectSpec, int traceFlags) {
        String nameString;
        String argString;
        int index = connectSpec.indexOf(':');
        if (index == -1) {
            nameString = connectSpec;
            argString = "";
        } else {
            nameString = connectSpec.substring(0, index);
            argString = connectSpec.substring(index + 1);
        }
        connector = findConnector(nameString);
        if (connector == null) {
            throw new IllegalArgumentException("No connector named: " +
                                               nameString);
        }
        connectorArgs = parseConnectorArgs(connector, argString);
        this.traceFlags = traceFlags;
    }
    synchronized VirtualMachine open() {
        if (connector instanceof LaunchingConnector) {
            vm = launchTarget();
        } else if (connector instanceof AttachingConnector) {
            vm = attachTarget();
        } else if (connector instanceof ListeningConnector) {
            vm = listenTarget();
        } else {
            throw new InternalError("Invalid connect type");
        }
        vm.setDebugTraceMode(traceFlags);
        System.out.println("JVM version:" + vm.version());
        System.out.println("JDI version: " + Bootstrap.virtualMachineManager().majorInterfaceVersion() +
                           "." + Bootstrap.virtualMachineManager().minorInterfaceVersion());
        System.out.println("JVM description: " + vm.description());
        return vm;
    }
    boolean setConnectorArg(String name, String value) {
        if (vm != null) {
            return false;
        }
        Connector.Argument argument = (Connector.Argument)connectorArgs.get(name);
        if (argument == null) {
            return false;
        }
        argument.setValue(value);
        return true;
    }
    String connectorArg(String name) {
        Connector.Argument argument = (Connector.Argument)connectorArgs.get(name);
        if (argument == null) {
            return "";
        }
        return argument.value();
    }
    public synchronized VirtualMachine vm() {
        if (vm == null) {
            throw new InternalError("VM not connected");
        } else {
            return vm;
        }
    }
    boolean isOpen() {
        return (vm != null);
    }
    boolean isLaunch() {
        return (connector instanceof LaunchingConnector);
    }
    Connector connector() {
        return connector;
    }
    boolean isListen() {
        return (connector instanceof ListeningConnector);
    }
    boolean isAttach() {
        return (connector instanceof AttachingConnector);
    }
    private synchronized void notifyOutputComplete() {
        outputCompleteCount++;
        notifyAll();
    }
    private synchronized void waitOutputComplete() {
        if (process != null) {
            while (outputCompleteCount < 2) {
                try {wait();} catch (InterruptedException e) {}
            }
        }
    }
    public void disposeVM() {
        try {
            if (vm != null) {
                vm.dispose();
                vm = null;
            }
        } finally {
            if (process != null) {
                process.destroy();
                process = null;
            }
            waitOutputComplete();
        }
    }
    private void dumpStream(InputStream stream) throws IOException {
        PrintStream outStream = System.out;
        BufferedReader in =
            new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = in.readLine()) != null) {
            outStream.println(line);
        }
    }
    private void displayRemoteOutput(final InputStream stream) {
        Thread thr = new Thread("output reader") {
            public void run() {
                try {
                    dumpStream(stream);
                } catch (IOException ex) {
                    System.err.println("IOException reading output of child java interpreter:"
                                       + ex.getMessage());
                } finally {
                    notifyOutputComplete();
                }
            }
        };
        thr.setPriority(Thread.MAX_PRIORITY-1);
        thr.start();
    }
    private void dumpFailedLaunchInfo(Process process) {
        try {
            dumpStream(process.getErrorStream());
            dumpStream(process.getInputStream());
        } catch (IOException e) {
            System.err.println("Unable to display process output: " +
                               e.getMessage());
        }
    }
    private VirtualMachine launchTarget() {
        LaunchingConnector launcher = (LaunchingConnector)connector;
        try {
            VirtualMachine vm = launcher.launch(connectorArgs);
            process = vm.process();
            displayRemoteOutput(process.getErrorStream());
            displayRemoteOutput(process.getInputStream());
            return vm;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("\n Unable to launch target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            System.err.println("\n Internal debugger error.");
        } catch (VMStartException vmse) {
            System.err.println(vmse.getMessage() + "\n");
            dumpFailedLaunchInfo(vmse.process());
            System.err.println("\n Target VM failed to initialize.");
        }
        return null; 
    }
    private VirtualMachine attachTarget() {
        AttachingConnector attacher = (AttachingConnector)connector;
        try {
            return attacher.attach(connectorArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("\n Unable to attach to target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            System.err.println("\n Internal debugger error.");
        }
        return null; 
    }
    private VirtualMachine listenTarget() {
        ListeningConnector listener = (ListeningConnector)connector;
        try {
            String retAddress = listener.startListening(connectorArgs);
            System.out.println("Listening at address: " + retAddress);
            vm = listener.accept(connectorArgs);
            listener.stopListening(connectorArgs);
            return vm;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("\n Unable to attach to target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            System.err.println("\n Internal debugger error.");
        }
        return null; 
    }
}
