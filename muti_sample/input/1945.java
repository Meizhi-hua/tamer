public class SctpNet {
    static final String osName = AccessController.doPrivileged(
                    new GetPropertyAction("os.name"));
    private static boolean IPv4MappedAddresses() {
        if ("SunOS".equals(osName)) {
            return true;
        } 
        return false;
    }
    static boolean throwAlreadyBoundException() throws IOException {
        throw new AlreadyBoundException();
    }
    static void listen(int fd, int backlog) throws IOException {
        listen0(fd, backlog);
    }
    static int connect(int fd, InetAddress remote, int remotePort)
            throws IOException {
        return connect0(fd, remote, remotePort);
    }
    static void close(int fd) throws IOException {
        close0(fd);
    }
    static void preClose(int fd) throws IOException {
        preClose0(fd);
    }
    static FileDescriptor socket(boolean oneToOne) throws IOException {
        int nativefd = socket0(oneToOne);
        return IOUtil.newFD(nativefd);
    }
    static void bindx(int fd, InetAddress[] addrs, int port, boolean add)
            throws IOException {
        bindx(fd, addrs, port, addrs.length, add,
                IPv4MappedAddresses());
    }
    static Set<SocketAddress> getLocalAddresses(int fd)
            throws IOException {
        HashSet<SocketAddress> set = null;
        SocketAddress[] saa = getLocalAddresses0(fd);
        if (saa != null) {
            set = new HashSet<SocketAddress>(saa.length);
            for (SocketAddress sa : saa)
                set.add(sa);
        }
        return set;
    }
    static Set<SocketAddress> getRemoteAddresses(int fd, int assocId)
            throws IOException {
        HashSet<SocketAddress> set = null;
        SocketAddress[] saa = getRemoteAddresses0(fd, assocId);
        if (saa != null) {
            set = new HashSet<SocketAddress>(saa.length);
            for (SocketAddress sa : saa)
                set.add(sa);
        }
        return set;
    }
    static void setSocketOption(int fd,
                                SctpSocketOption name,
                                Object value,
                                int assocId)
            throws IOException {
        if (value == null)
            throw new IllegalArgumentException("Invalid option value");
        Class<?> type = name.type();
        if (!type.isInstance(value))
            throw new IllegalArgumentException("Invalid option value");
        if (name.equals(SCTP_INIT_MAXSTREAMS)) {
            InitMaxStreams maxStreamValue = (InitMaxStreams)value;
            SctpNet.setInitMsgOption0(fd,
                 maxStreamValue.maxInStreams(), maxStreamValue.maxOutStreams());
        } else if (name.equals(SCTP_PRIMARY_ADDR) ||
                   name.equals(SCTP_SET_PEER_PRIMARY_ADDR)) {
            SocketAddress addr  = (SocketAddress) value;
            if (addr == null)
                throw new IllegalArgumentException("Invalid option value");
            Net.checkAddress(addr);
            InetSocketAddress netAddr = (InetSocketAddress)addr;
            if (name.equals(SCTP_PRIMARY_ADDR)) {
                setPrimAddrOption0(fd,
                                   assocId,
                                   netAddr.getAddress(),
                                   netAddr.getPort());
            } else {
                setPeerPrimAddrOption0(fd,
                                       assocId,
                                       netAddr.getAddress(),
                                       netAddr.getPort(),
                                       IPv4MappedAddresses());
            }
        } else if (name.equals(SCTP_DISABLE_FRAGMENTS) ||
            name.equals(SCTP_EXPLICIT_COMPLETE) ||
            name.equals(SCTP_FRAGMENT_INTERLEAVE) ||
            name.equals(SCTP_NODELAY) ||
            name.equals(SO_SNDBUF) ||
            name.equals(SO_RCVBUF) ||
            name.equals(SO_LINGER)) {
            setIntOption(fd, name, value);
        } else {
            throw new AssertionError("Unknown socket option");
        }
    }
    static Object getSocketOption(int fd, SctpSocketOption name, int assocId)
             throws IOException {
         if (name.equals(SCTP_SET_PEER_PRIMARY_ADDR)) {
            throw new IllegalArgumentException(
                    "SCTP_SET_PEER_PRIMARY_ADDR cannot be retrieved");
        } else if (name.equals(SCTP_INIT_MAXSTREAMS)) {
            int[] values = new int[2];
            SctpNet.getInitMsgOption0(fd, values);
            return InitMaxStreams.create(values[0], values[1]);
        } else if (name.equals(SCTP_PRIMARY_ADDR)) {
            return getPrimAddrOption0(fd, assocId);
        } else if (name.equals(SCTP_DISABLE_FRAGMENTS) ||
            name.equals(SCTP_EXPLICIT_COMPLETE) ||
            name.equals(SCTP_FRAGMENT_INTERLEAVE) ||
            name.equals(SCTP_NODELAY) ||
            name.equals(SO_SNDBUF) ||
            name.equals(SO_RCVBUF) ||
            name.equals(SO_LINGER)) {
            return getIntOption(fd, name);
        } else {
            throw new AssertionError("Unknown socket option");
        }
    }
    static void setIntOption(int fd, SctpSocketOption name, Object value)
            throws IOException {
        if (value == null)
            throw new IllegalArgumentException("Invalid option value");
        Class<?> type = name.type();
        if (type != Integer.class && type != Boolean.class)
            throw new AssertionError("Should not reach here");
        if (name == SO_RCVBUF ||
            name == SO_SNDBUF)
        {
            int i = ((Integer)value).intValue();
            if (i < 0)
                throw new IllegalArgumentException(
                        "Invalid send/receive buffer size");
        } else if (name == SO_LINGER) {
            int i = ((Integer)value).intValue();
            if (i < 0)
                value = Integer.valueOf(-1);
            if (i > 65535)
                value = Integer.valueOf(65535);
        } else if (name.equals(SCTP_FRAGMENT_INTERLEAVE)) {
            int i = ((Integer)value).intValue();
            if (i < 0 || i > 2)
                throw new IllegalArgumentException(
                        "Invalid value for SCTP_FRAGMENT_INTERLEAVE");
        }
        int arg;
        if (type == Integer.class) {
            arg = ((Integer)value).intValue();
        } else {
            boolean b = ((Boolean)value).booleanValue();
            arg = (b) ? 1 : 0;
        }
        setIntOption0(fd, ((SctpStdSocketOption)name).constValue(), arg);
    }
    static Object getIntOption(int fd, SctpSocketOption name)
            throws IOException {
        Class<?> type = name.type();
        if (type != Integer.class && type != Boolean.class)
            throw new AssertionError("Should not reach here");
        if (!(name instanceof SctpStdSocketOption))
            throw new AssertionError("Should not reach here");
        int value = getIntOption0(fd,
                ((SctpStdSocketOption)name).constValue());
        if (type == Integer.class) {
            return Integer.valueOf(value);
        } else {
            return (value == 0) ? Boolean.FALSE : Boolean.TRUE;
        }
    }
    static void shutdown(int fd, int assocId)
            throws IOException {
        shutdown0(fd, assocId);
    }
    static FileDescriptor branch(int fd, int assocId) throws IOException {
        int nativefd = branch0(fd, assocId);
        return IOUtil.newFD(nativefd);
    }
    static native int socket0(boolean oneToOne) throws IOException;
    static native void listen0(int fd, int backlog) throws IOException;
    static native int connect0(int fd, InetAddress remote, int remotePort)
        throws IOException;
    static native void close0(int fd) throws IOException;
    static native void preClose0(int fd) throws IOException;
    static native void bindx(int fd, InetAddress[] addrs, int port, int length,
            boolean add, boolean preferIPv6) throws IOException;
    static native int getIntOption0(int fd, int opt) throws IOException;
    static native void setIntOption0(int fd, int opt, int arg)
        throws IOException;
    static native SocketAddress[] getLocalAddresses0(int fd) throws IOException;
    static native SocketAddress[] getRemoteAddresses0(int fd, int assocId)
            throws IOException;
    static native int branch0(int fd, int assocId) throws IOException;
    static native void setPrimAddrOption0(int fd, int assocId, InetAddress ia,
            int port) throws IOException;
    static native void setPeerPrimAddrOption0(int fd, int assocId,
            InetAddress ia, int port, boolean preferIPv6) throws IOException;
    static native SocketAddress getPrimAddrOption0(int fd, int assocId)
            throws IOException;
    static native void getInitMsgOption0(int fd, int[] retVals) throws IOException;
    static native void setInitMsgOption0(int fd, int arg1, int arg2)
            throws IOException;
    static native void shutdown0(int fd, int assocId);
    static native void init();
    static {
        init();
    }
}
