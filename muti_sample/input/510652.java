public class Forwarder {
    private ForwardServer server;
    private Socket from, to;
    private static final String LOGTAG = "Forwarder";
    public Forwarder (Socket from, Socket to, ForwardServer server) {
        this.server = server;
        this.from = from;
        this.to = to;
        server.register(this);
    }
    public void start() {
        Thread outgoing = new Thread(new SocketPipe(from, to));
        Thread incoming = new Thread(new SocketPipe(to, from));
        outgoing.setName(LOGTAG);
        incoming.setName(LOGTAG);
        outgoing.start();
        incoming.start();
    }
    public void stop() {
        shutdown(from);
        shutdown(to);
    }
    private void shutdown(Socket socket) {
        try {
            socket.shutdownInput();
        } catch (IOException e) {
            Log.v(LOGTAG, "Socket#shutdownInput", e);
        }
        try {
            socket.shutdownOutput();
        } catch (IOException e) {
            Log.v(LOGTAG, "Socket#shutdownOutput", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            Log.v(LOGTAG, "Socket#close", e);
        }
    }
    private class SocketPipe implements Runnable {
        private Socket in, out;
        public SocketPipe(Socket in, Socket out) {
            this.in = in;
            this.out = out;
        }
        public void run() {
            try {
                int length;
                InputStream is = in.getInputStream();
                OutputStream os = out.getOutputStream();
                byte[] buffer = new byte[4096];
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } catch (IOException ioe) {
            } finally {
                server.unregister(Forwarder.this);
            }
        }
        @Override
        public String toString() {
            return "SocketPipe{" + in + "=>" + out  + "}";
        }
    }
}
