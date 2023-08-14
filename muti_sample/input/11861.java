class SharedMemoryTransportService extends TransportService {
    private ResourceBundle messages = null;
    static class SharedMemoryListenKey extends ListenKey {
        long id;
        String name;
        SharedMemoryListenKey(long id, String name) {
            this.id = id;
            this.name = name;
        }
        long id() {
            return id;
        }
        void setId(long id) {
            this.id = id;
        }
        public String address() {
            return name;
        }
        public String toString() {
            return address();
        }
    }
    SharedMemoryTransportService() {
        System.loadLibrary("dt_shmem");
        initialize();
    }
    public String name() {
        return "SharedMemory";
    }
    public String defaultAddress() {
        return "javadebug";
    }
    public String description() {
        synchronized (this) {
            if (messages == null) {
                messages = ResourceBundle.getBundle("com.sun.tools.jdi.resources.jdi");
            }
        }
        return messages.getString("memory_transportservice.description");
    }
    public Capabilities capabilities() {
        return new SharedMemoryTransportServiceCapabilities();
    }
    private native void initialize();
    private native long startListening0(String address) throws IOException;
    private native long attach0(String address, long attachTimeout) throws IOException;
    private native void stopListening0(long id) throws IOException;
    private native long accept0(long id, long acceptTimeout) throws IOException;
    private native String name(long id) throws IOException;
    public Connection attach(String address, long attachTimeout, long handshakeTimeout) throws IOException {
        if (address == null) {
            throw new NullPointerException("address is null");
        }
        long id = attach0(address, attachTimeout);
        SharedMemoryConnection conn = new SharedMemoryConnection(id);
        conn.handshake(handshakeTimeout);
        return conn;
    }
    public TransportService.ListenKey startListening(String address) throws IOException {
        if (address == null || address.length() == 0) {
            address = defaultAddress();
        }
        long id = startListening0(address);
        return new SharedMemoryListenKey(id, name(id));
    }
    public ListenKey startListening() throws IOException {
        return startListening(null);
    }
    public void stopListening(ListenKey listener) throws IOException {
        if (!(listener instanceof SharedMemoryListenKey)) {
            throw new IllegalArgumentException("Invalid listener");
        }
        long id;
        SharedMemoryListenKey key = (SharedMemoryListenKey)listener;
        synchronized (key) {
            id = key.id();
            if (id == 0) {
                throw new IllegalArgumentException("Invalid listener");
            }
            key.setId(0);
        }
        stopListening0(id);
    }
    public Connection accept(ListenKey listener, long acceptTimeout, long handshakeTimeout) throws IOException {
        if (!(listener instanceof SharedMemoryListenKey)) {
            throw new IllegalArgumentException("Invalid listener");
        }
        long transportId;
        SharedMemoryListenKey key = (SharedMemoryListenKey)listener;
        synchronized (key) {
            transportId = key.id();
            if (transportId == 0) {
                throw new IllegalArgumentException("Invalid listener");
            }
        }
        long connectId = accept0(transportId, acceptTimeout);
        SharedMemoryConnection conn = new SharedMemoryConnection(connectId);
        conn.handshake(handshakeTimeout);
        return conn;
    }
}
class SharedMemoryConnection extends Connection {
    private long id;
    private Object receiveLock = new Object();
    private Object sendLock = new Object();
    private Object closeLock = new Object();
    private boolean closed = false;
    private native byte receiveByte0(long id) throws IOException;
    private native void sendByte0(long id, byte b) throws IOException;
    private native void close0(long id);
    private native byte[] receivePacket0(long id)throws IOException;
    private native void sendPacket0(long id, byte b[]) throws IOException;
    void handshake(long handshakeTimeout) throws IOException {
        byte[] hello = "JDWP-Handshake".getBytes("UTF-8");
        for (int i=0; i<hello.length; i++) {
            sendByte0(id, hello[i]);
        }
        for (int i=0; i<hello.length; i++) {
            byte b = receiveByte0(id);
            if (b != hello[i]) {
                throw new IOException("handshake failed - unrecognized message from target VM");
            }
        }
    }
    SharedMemoryConnection(long id) throws IOException {
        this.id = id;
    }
    public void close() {
        synchronized (closeLock) {
            if (!closed) {
                close0(id);
                closed = true;
            }
        }
    }
    public boolean isOpen() {
        synchronized (closeLock) {
            return !closed;
        }
    }
    public byte[] readPacket() throws IOException {
        if (!isOpen()) {
            throw new ClosedConnectionException("Connection closed");
        }
        byte b[];
        try {
            synchronized (receiveLock) {
                b  = receivePacket0(id);
            }
        } catch (IOException ioe) {
            if (!isOpen()) {
                throw new ClosedConnectionException("Connection closed");
            } else {
                throw ioe;
            }
        }
        return b;
    }
    public void writePacket(byte b[]) throws IOException {
        if (!isOpen()) {
            throw new ClosedConnectionException("Connection closed");
        }
        if (b.length < 11) {
            throw new IllegalArgumentException("packet is insufficient size");
        }
        int b0 = b[0] & 0xff;
        int b1 = b[1] & 0xff;
        int b2 = b[2] & 0xff;
        int b3 = b[3] & 0xff;
        int len = ((b0 << 24) | (b1 << 16) | (b2 << 8) | (b3 << 0));
        if (len < 11) {
            throw new IllegalArgumentException("packet is insufficient size");
        }
        if (len > b.length) {
            throw new IllegalArgumentException("length mis-match");
        }
        try {
            synchronized(sendLock) {
                sendPacket0(id, b);
            }
        } catch (IOException ioe) {
            if (!isOpen()) {
               throw new ClosedConnectionException("Connection closed");
            } else {
               throw ioe;
            }
        }
    }
}
class SharedMemoryTransportServiceCapabilities extends TransportService.Capabilities {
    public boolean supportsMultipleConnections() {
        return false;
    }
    public boolean supportsAttachTimeout() {
        return true;
    }
    public boolean supportsAcceptTimeout() {
        return true;
    }
    public boolean supportsHandshakeTimeout() {
        return false;
    }
}