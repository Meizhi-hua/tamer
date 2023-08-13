public class ForwardServer {
    private static final String LOGTAG = "ForwardServer";
    private int remotePort;
    private int remoteAddress;
    private int localPort;
    private ServerSocket serverSocket;
    private boolean started;
    private Set<Forwarder> forwarders;
    public ForwardServer(int localPort, int remoteAddress, int remotePort) {
        this.localPort = localPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        started = false;
        forwarders = new HashSet<Forwarder>();
    }
    public synchronized void start() throws IOException {
        if(!started) {
            serverSocket = new ServerSocket(localPort);
            Thread serverThread = new Thread(new ServerRunner(serverSocket));
            serverThread.setName(LOGTAG);
            serverThread.start();
            started = true;
        }
    }
    public synchronized void stop() {
        if(started) {
            synchronized (forwarders) {
                for(Forwarder forwarder : forwarders)
                    forwarder.stop();
                forwarders.clear();
            }
            try {
                serverSocket.close();
            } catch (IOException ioe) {
                Log.v(LOGTAG, "exception while closing", ioe);
            } finally {
                started = false;
            }
        }
    }
    public synchronized boolean isRunning() {
        return started;
    }
    private class ServerRunner implements Runnable {
        private ServerSocket socket;
        public ServerRunner(ServerSocket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                while (true) {
                    Socket localSocket = socket.accept();
                    Socket remoteSocket = AdbUtils.getForwardedSocket(remoteAddress, remotePort);
                    if(remoteSocket == null) {
                        try {
                            localSocket.close();
                        } catch (IOException ioe) {
                            Log.w(LOGTAG, "error while closing socket", ioe);
                        } finally {
                            Log.w(LOGTAG, "failed to start forwarding from " + localSocket);
                        }
                    } else {
                        Forwarder forwarder = new Forwarder(localSocket, remoteSocket,
                                ForwardServer.this);
                        forwarder.start();
                    }
                }
            } catch (IOException ioe) {
                return;
            }
        }
    }
    public void register(Forwarder forwarder) {
        synchronized (forwarders) {
            if(!forwarders.contains(forwarder)) {
                forwarders.add(forwarder);
            }
        }
    }
    public void unregister(Forwarder recyclable) {
        synchronized (forwarders) {
            if(forwarders.contains(recyclable)) {
                recyclable.stop();
                forwarders.remove(recyclable);
            }
        }
    }
}