abstract class AbstractLauncher extends ConnectorImpl implements LaunchingConnector {
    abstract public VirtualMachine
        launch(Map<String,? extends Connector.Argument> arguments)
                                 throws IOException,
                                        IllegalConnectorArgumentsException,
                                        VMStartException;
    abstract public String name();
    abstract public String description();
    ThreadGroup grp;
    AbstractLauncher() {
        super();
        grp = Thread.currentThread().getThreadGroup();
        ThreadGroup parent = null;
        while ((parent = grp.getParent()) != null) {
            grp = parent;
        }
    }
    String[] tokenizeCommand(String command, char quote) {
        String quoteStr = String.valueOf(quote); 
        StringTokenizer tokenizer = new StringTokenizer(command,
                                                        quote + " \t\r\n\f",
                                                        true);
        String quoted = null;
        String pending = null;
        List<String> tokenList = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (quoted != null) {
                if (token.equals(quoteStr)) {
                    tokenList.add(quoted);
                    quoted = null;
                } else {
                    quoted += token;
                }
            } else if (pending != null) {
                if (token.equals(quoteStr)) {
                    quoted = pending;
                } else if ((token.length() == 1) &&
                           Character.isWhitespace(token.charAt(0))) {
                    tokenList.add(pending);
                } else {
                    throw new InternalException("Unexpected token: " + token);
                }
                pending = null;
            } else {
                if (token.equals(quoteStr)) {
                    quoted = "";
                } else if ((token.length() == 1) &&
                           Character.isWhitespace(token.charAt(0))) {
                } else {
                    pending = token;
                }
            }
        }
        if (pending != null) {
            tokenList.add(pending);
        }
        if (quoted != null) {
            tokenList.add(quoted);
        }
        String[] tokenArray = new String[tokenList.size()];
        for (int i = 0; i < tokenList.size(); i++) {
            tokenArray[i] = tokenList.get(i);
        }
        return tokenArray;
    }
    protected VirtualMachine launch(String[] commandArray, String address,
                                    TransportService.ListenKey listenKey,
                                    TransportService ts)
                                    throws IOException, VMStartException {
        Helper helper = new Helper(commandArray, address, listenKey, ts);
        helper.launchAndAccept();
        VirtualMachineManager manager =
            Bootstrap.virtualMachineManager();
        return manager.createVirtualMachine(helper.connection(),
                                            helper.process());
    }
    private class Helper {
        private final String address;
        private TransportService.ListenKey listenKey;
        private TransportService ts;
        private final String[] commandArray;
        private Process process = null;
        private Connection connection = null;
        private IOException acceptException = null;
        private boolean exited = false;
        Helper(String[] commandArray, String address, TransportService.ListenKey listenKey,
            TransportService ts) {
            this.commandArray = commandArray;
            this.address = address;
            this.listenKey = listenKey;
            this.ts = ts;
        }
        String commandString() {
            String str = "";
            for (int i = 0; i < commandArray.length; i++) {
                if (i > 0) {
                    str += " ";
                }
                str += commandArray[i];
            }
            return str;
        }
        synchronized void launchAndAccept() throws
                                IOException, VMStartException {
            process = Runtime.getRuntime().exec(commandArray);
            Thread acceptingThread = acceptConnection();
            Thread monitoringThread = monitorTarget();
            try {
                while ((connection == null) &&
                       (acceptException == null) &&
                       !exited) {
                    wait();
                }
                if (exited) {
                    throw new VMStartException(
                        "VM initialization failed for: " + commandString(), process);
                }
                if (acceptException != null) {
                    throw acceptException;
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException("Interrupted during accept");
            } finally {
                acceptingThread.interrupt();
                monitoringThread.interrupt();
            }
        }
        Process process() {
            return process;
        }
        Connection connection() {
            return connection;
        }
        synchronized void notifyOfExit() {
            exited = true;
            notify();
        }
        synchronized void notifyOfConnection(Connection connection) {
            this.connection = connection;
            notify();
        }
        synchronized void notifyOfAcceptException(IOException acceptException) {
            this.acceptException = acceptException;
            notify();
        }
        Thread monitorTarget() {
            Thread thread = new Thread(grp,
                                       "launched target monitor") {
                public void run() {
                    try {
                        process.waitFor();
                        notifyOfExit();
                    } catch (InterruptedException e) {
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
            return thread;
        }
        Thread acceptConnection() {
            Thread thread = new Thread(grp,
                                       "connection acceptor") {
                public void run() {
                    try {
                        Connection connection = ts.accept(listenKey, 0, 0);
                        notifyOfConnection(connection);
                    } catch (InterruptedIOException e) {
                    } catch (IOException e) {
                        notifyOfAcceptException(e);
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
            return thread;
        }
    }
}
