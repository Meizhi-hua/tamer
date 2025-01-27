class DualStackPlainSocketImpl extends AbstractPlainSocketImpl
{
    static JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
    public DualStackPlainSocketImpl() {}
    public DualStackPlainSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }
    void socketCreate(boolean stream) throws IOException {
        if (fd == null)
            throw new SocketException("Socket closed");
        int newfd = socket0(stream, false );
        fdAccess.set(fd, newfd);
    }
    void socketConnect(InetAddress address, int port, int timeout)
        throws IOException {
        int nativefd = checkAndReturnNativeFD();
        if (address == null)
            throw new NullPointerException("inet address argument is null.");
        int connectResult;
        if (timeout <= 0) {
            connectResult = connect0(nativefd, address, port);
        } else {
            configureBlocking(nativefd, false);
            try {
                connectResult = connect0(nativefd, address, port);
                if (connectResult == WOULDBLOCK) {
                    waitForConnect(nativefd, timeout);
                }
            } finally {
                configureBlocking(nativefd, true);
            }
        }
        if (localport == 0)
            localport = localPort0(nativefd);
    }
    void socketBind(InetAddress address, int port) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        if (address == null)
            throw new NullPointerException("inet address argument is null.");
        bind0(nativefd, address, port);
        if (port == 0) {
            localport = localPort0(nativefd);
        } else {
            localport = port;
        }
        this.address = address;
    }
    void socketListen(int backlog) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        listen0(nativefd, backlog);
    }
    void socketAccept(SocketImpl s) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        if (s == null)
            throw new NullPointerException("socket is null");
        int newfd = -1;
        InetSocketAddress[] isaa = new InetSocketAddress[1];
        if (timeout <= 0) {
            newfd = accept0(nativefd, isaa);
        } else {
            configureBlocking(nativefd, false);
            try {
                waitForNewConnection(nativefd, timeout);
                newfd = accept0(nativefd, isaa);
                if (newfd != -1) {
                    configureBlocking(newfd, true);
                }
            } finally {
                configureBlocking(nativefd, true);
            }
        }
        fdAccess.set(s.fd, newfd);
        InetSocketAddress isa = isaa[0];
        s.port = isa.getPort();
        s.address = isa.getAddress();
        s.localport = localport;
    }
    int socketAvailable() throws IOException {
        int nativefd = checkAndReturnNativeFD();
        return available0(nativefd);
    }
    void socketClose0(boolean useDeferredClose) throws IOException {
        if (fd == null)
            throw new SocketException("Socket closed");
        if (!fd.valid())
            return;
        close0(fdAccess.get(fd));
        fdAccess.set(fd, -1);
    }
    void socketShutdown(int howto) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        shutdown0(nativefd, howto);
    }
    void socketSetOption(int opt, boolean on, Object value)
        throws SocketException {
        int nativefd = checkAndReturnNativeFD();
        if (opt == SO_TIMEOUT) {  
            return;
        }
        int optionValue = 0;
        switch(opt) {
            case TCP_NODELAY :
            case SO_OOBINLINE :
            case SO_KEEPALIVE :
            case SO_REUSEADDR :
                optionValue = on ? 1 : 0;
                break;
            case SO_SNDBUF :
            case SO_RCVBUF :
            case IP_TOS :
                optionValue = ((Integer)value).intValue();
                break;
            case SO_LINGER :
                if (on) {
                    optionValue =  ((Integer)value).intValue();
                } else {
                    optionValue = -1;
                }
                break;
            default :
                throw new SocketException("Option not supported");
        }
        setIntOption(nativefd, opt, optionValue);
    }
    int socketGetOption(int opt, Object iaContainerObj) throws SocketException {
        int nativefd = checkAndReturnNativeFD();
        if (opt == SO_BINDADDR) {
            localAddress(nativefd, (InetAddressContainer)iaContainerObj);
            return 0;  
        }
        int value = getIntOption(nativefd, opt);
        switch (opt) {
            case TCP_NODELAY :
            case SO_OOBINLINE :
            case SO_KEEPALIVE :
            case SO_REUSEADDR :
                return (value == 0) ? -1 : 1;
        }
        return value;
    }
    void socketSendUrgentData(int data) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        sendOOB(nativefd, data);
    }
    private int checkAndReturnNativeFD() throws SocketException {
        if (fd == null || !fd.valid())
            throw new SocketException("Socket closed");
        return fdAccess.get(fd);
    }
    static final int WOULDBLOCK = -2;       
    static {
        initIDs();
    }
    static native void initIDs();
    static native int socket0(boolean stream, boolean v6Only) throws IOException;
    static native void bind0(int fd, InetAddress localAddress, int localport)
        throws IOException;
    static native int connect0(int fd, InetAddress remote, int remotePort)
        throws IOException;
    static native void waitForConnect(int fd, int timeout) throws IOException;
    static native int localPort0(int fd) throws IOException;
    static native void localAddress(int fd, InetAddressContainer in) throws SocketException;
    static native void listen0(int fd, int backlog) throws IOException;
    static native int accept0(int fd, InetSocketAddress[] isaa) throws IOException;
    static native void waitForNewConnection(int fd, int timeout) throws IOException;
    static native int available0(int fd) throws IOException;
    static native void close0(int fd) throws IOException;
    static native void shutdown0(int fd, int howto) throws IOException;
    static native void setIntOption(int fd, int cmd, int optionValue) throws SocketException;
    static native int getIntOption(int fd, int cmd) throws SocketException;
    static native void sendOOB(int fd, int data) throws IOException;
    static native void configureBlocking(int fd, boolean blocking) throws IOException;
}
