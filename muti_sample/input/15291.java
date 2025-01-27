class SocksSocketImpl extends PlainSocketImpl implements SocksConsts {
    private String server = null;
    private int serverPort = DEFAULT_PORT;
    private InetSocketAddress external_address;
    private boolean useV4 = false;
    private Socket cmdsock = null;
    private InputStream cmdIn = null;
    private OutputStream cmdOut = null;
    private boolean applicationSetProxy;  
    SocksSocketImpl() {
    }
    SocksSocketImpl(String server, int port) {
        this.server = server;
        this.serverPort = (port == -1 ? DEFAULT_PORT : port);
    }
    SocksSocketImpl(Proxy proxy) {
        SocketAddress a = proxy.address();
        if (a instanceof InetSocketAddress) {
            InetSocketAddress ad = (InetSocketAddress) a;
            server = ad.getHostString();
            serverPort = ad.getPort();
        }
    }
    void setV4() {
        useV4 = true;
    }
    private synchronized void privilegedConnect(final String host,
                                              final int port,
                                              final int timeout)
         throws IOException
    {
        try {
            AccessController.doPrivileged(
                new java.security.PrivilegedExceptionAction<Void>() {
                    public Void run() throws IOException {
                              superConnectServer(host, port, timeout);
                              cmdIn = getInputStream();
                              cmdOut = getOutputStream();
                              return null;
                          }
                      });
        } catch (java.security.PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }
    private void superConnectServer(String host, int port,
                                    int timeout) throws IOException {
        super.connect(new InetSocketAddress(host, port), timeout);
    }
    private static int remainingMillis(long deadlineMillis) throws IOException {
        if (deadlineMillis == 0L)
            return 0;
        final long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining > 0)
            return (int) remaining;
        throw new SocketTimeoutException();
    }
    private int readSocksReply(InputStream in, byte[] data) throws IOException {
        return readSocksReply(in, data, 0L);
    }
    private int readSocksReply(InputStream in, byte[] data, long deadlineMillis) throws IOException {
        int len = data.length;
        int received = 0;
        for (int attempts = 0; received < len && attempts < 3; attempts++) {
            int count;
            try {
                count = ((SocketInputStream)in).read(data, received, len - received, remainingMillis(deadlineMillis));
            } catch (SocketTimeoutException e) {
                throw new SocketTimeoutException("Connect timed out");
            }
            if (count < 0)
                throw new SocketException("Malformed reply from SOCKS server");
            received += count;
        }
        return received;
    }
    private boolean authenticate(byte method, InputStream in,
                                 BufferedOutputStream out) throws IOException {
        return authenticate(method, in, out, 0L);
    }
    private boolean authenticate(byte method, InputStream in,
                                 BufferedOutputStream out,
                                 long deadlineMillis) throws IOException {
        if (method == NO_AUTH)
            return true;
        if (method == USER_PASSW) {
            String userName;
            String password = null;
            final InetAddress addr = InetAddress.getByName(server);
            PasswordAuthentication pw =
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<PasswordAuthentication>() {
                        public PasswordAuthentication run() {
                                return Authenticator.requestPasswordAuthentication(
                                       server, addr, serverPort, "SOCKS5", "SOCKS authentication", null);
                            }
                        });
            if (pw != null) {
                userName = pw.getUserName();
                password = new String(pw.getPassword());
            } else {
                userName = java.security.AccessController.doPrivileged(
                        new sun.security.action.GetPropertyAction("user.name"));
            }
            if (userName == null)
                return false;
            out.write(1);
            out.write(userName.length());
            try {
                out.write(userName.getBytes("ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException uee) {
                assert false;
            }
            if (password != null) {
                out.write(password.length());
                try {
                    out.write(password.getBytes("ISO-8859-1"));
                } catch (java.io.UnsupportedEncodingException uee) {
                    assert false;
                }
            } else
                out.write(0);
            out.flush();
            byte[] data = new byte[2];
            int i = readSocksReply(in, data, deadlineMillis);
            if (i != 2 || data[1] != 0) {
                out.close();
                in.close();
                return false;
            }
            return true;
        }
        return false;
    }
    private void connectV4(InputStream in, OutputStream out,
                           InetSocketAddress endpoint,
                           long deadlineMillis) throws IOException {
        if (!(endpoint.getAddress() instanceof Inet4Address)) {
            throw new SocketException("SOCKS V4 requires IPv4 only addresses");
        }
        out.write(PROTO_VERS4);
        out.write(CONNECT);
        out.write((endpoint.getPort() >> 8) & 0xff);
        out.write((endpoint.getPort() >> 0) & 0xff);
        out.write(endpoint.getAddress().getAddress());
        String userName = getUserName();
        try {
            out.write(userName.getBytes("ISO-8859-1"));
        } catch (java.io.UnsupportedEncodingException uee) {
            assert false;
        }
        out.write(0);
        out.flush();
        byte[] data = new byte[8];
        int n = readSocksReply(in, data, deadlineMillis);
        if (n != 8)
            throw new SocketException("Reply from SOCKS server has bad length: " + n);
        if (data[0] != 0 && data[0] != 4)
            throw new SocketException("Reply from SOCKS server has bad version");
        SocketException ex = null;
        switch (data[1]) {
        case 90:
            external_address = endpoint;
            break;
        case 91:
            ex = new SocketException("SOCKS request rejected");
            break;
        case 92:
            ex = new SocketException("SOCKS server couldn't reach destination");
            break;
        case 93:
            ex = new SocketException("SOCKS authentication failed");
            break;
        default:
            ex = new SocketException("Reply from SOCKS server contains bad status");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
    }
    @Override
    protected void connect(SocketAddress endpoint, int timeout) throws IOException {
        final long deadlineMillis;
        if (timeout == 0) {
            deadlineMillis = 0L;
        } else {
            long finish = System.currentTimeMillis() + timeout;
            deadlineMillis = finish < 0 ? Long.MAX_VALUE : finish;
        }
        SecurityManager security = System.getSecurityManager();
        if (endpoint == null || !(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (security != null) {
            if (epoint.isUnresolved())
                security.checkConnect(epoint.getHostName(),
                                      epoint.getPort());
            else
                security.checkConnect(epoint.getAddress().getHostAddress(),
                                      epoint.getPort());
        }
        if (server == null) {
            ProxySelector sel = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<ProxySelector>() {
                    public ProxySelector run() {
                            return ProxySelector.getDefault();
                        }
                    });
            if (sel == null) {
                super.connect(epoint, remainingMillis(deadlineMillis));
                return;
            }
            URI uri;
            String host = epoint.getHostString();
            if (epoint.getAddress() instanceof Inet6Address &&
                (!host.startsWith("[")) && (host.indexOf(":") >= 0)) {
                host = "[" + host + "]";
            }
            try {
                uri = new URI("socket:
            } catch (URISyntaxException e) {
                assert false : e;
                uri = null;
            }
            Proxy p = null;
            IOException savedExc = null;
            java.util.Iterator<Proxy> iProxy = null;
            iProxy = sel.select(uri).iterator();
            if (iProxy == null || !(iProxy.hasNext())) {
                super.connect(epoint, remainingMillis(deadlineMillis));
                return;
            }
            while (iProxy.hasNext()) {
                p = iProxy.next();
                if (p == null || p == Proxy.NO_PROXY) {
                    super.connect(epoint, remainingMillis(deadlineMillis));
                    return;
                }
                if (p.type() != Proxy.Type.SOCKS)
                    throw new SocketException("Unknown proxy type : " + p.type());
                if (!(p.address() instanceof InetSocketAddress))
                    throw new SocketException("Unknow address type for proxy: " + p);
                server = ((InetSocketAddress) p.address()).getHostString();
                serverPort = ((InetSocketAddress) p.address()).getPort();
                if (p instanceof SocksProxy) {
                    if (((SocksProxy)p).protocolVersion() == 4) {
                        useV4 = true;
                    }
                }
                try {
                    privilegedConnect(server, serverPort, remainingMillis(deadlineMillis));
                    break;
                } catch (IOException e) {
                    sel.connectFailed(uri,p.address(),e);
                    server = null;
                    serverPort = -1;
                    savedExc = e;
                }
            }
            if (server == null) {
                throw new SocketException("Can't connect to SOCKS proxy:"
                                          + savedExc.getMessage());
            }
        } else {
            try {
                privilegedConnect(server, serverPort, remainingMillis(deadlineMillis));
            } catch (IOException e) {
                throw new SocketException(e.getMessage());
            }
        }
        BufferedOutputStream out = new BufferedOutputStream(cmdOut, 512);
        InputStream in = cmdIn;
        if (useV4) {
            if (epoint.isUnresolved())
                throw new UnknownHostException(epoint.toString());
            connectV4(in, out, epoint, deadlineMillis);
            return;
        }
        out.write(PROTO_VERS);
        out.write(2);
        out.write(NO_AUTH);
        out.write(USER_PASSW);
        out.flush();
        byte[] data = new byte[2];
        int i = readSocksReply(in, data, deadlineMillis);
        if (i != 2 || ((int)data[0]) != PROTO_VERS) {
            if (epoint.isUnresolved())
                throw new UnknownHostException(epoint.toString());
            connectV4(in, out, epoint, deadlineMillis);
            return;
        }
        if (((int)data[1]) == NO_METHODS)
            throw new SocketException("SOCKS : No acceptable methods");
        if (!authenticate(data[1], in, out, deadlineMillis)) {
            throw new SocketException("SOCKS : authentication failed");
        }
        out.write(PROTO_VERS);
        out.write(CONNECT);
        out.write(0);
        if (epoint.isUnresolved()) {
            out.write(DOMAIN_NAME);
            out.write(epoint.getHostName().length());
            try {
                out.write(epoint.getHostName().getBytes("ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException uee) {
                assert false;
            }
            out.write((epoint.getPort() >> 8) & 0xff);
            out.write((epoint.getPort() >> 0) & 0xff);
        } else if (epoint.getAddress() instanceof Inet6Address) {
            out.write(IPV6);
            out.write(epoint.getAddress().getAddress());
            out.write((epoint.getPort() >> 8) & 0xff);
            out.write((epoint.getPort() >> 0) & 0xff);
        } else {
            out.write(IPV4);
            out.write(epoint.getAddress().getAddress());
            out.write((epoint.getPort() >> 8) & 0xff);
            out.write((epoint.getPort() >> 0) & 0xff);
        }
        out.flush();
        data = new byte[4];
        i = readSocksReply(in, data, deadlineMillis);
        if (i != 4)
            throw new SocketException("Reply from SOCKS server has bad length");
        SocketException ex = null;
        int len;
        byte[] addr;
        switch (data[1]) {
        case REQUEST_OK:
            switch(data[3]) {
            case IPV4:
                addr = new byte[4];
                i = readSocksReply(in, addr, deadlineMillis);
                if (i != 4)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                break;
            case DOMAIN_NAME:
                len = data[1];
                byte[] host = new byte[len];
                i = readSocksReply(in, host, deadlineMillis);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                break;
            case IPV6:
                len = data[1];
                addr = new byte[len];
                i = readSocksReply(in, addr, deadlineMillis);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data, deadlineMillis);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                break;
            default:
                ex = new SocketException("Reply from SOCKS server contains wrong code");
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS server general failure");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: Connection not allowed by ruleset");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: Network unreachable");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: Host unreachable");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: Connection refused");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL expired");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: Command not supported");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: address type not supported");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
        external_address = epoint;
    }
    private void bindV4(InputStream in, OutputStream out,
                        InetAddress baddr,
                        int lport) throws IOException {
        if (!(baddr instanceof Inet4Address)) {
            throw new SocketException("SOCKS V4 requires IPv4 only addresses");
        }
        super.bind(baddr, lport);
        byte[] addr1 = baddr.getAddress();
        InetAddress naddr = baddr;
        if (naddr.isAnyLocalAddress()) {
            naddr = cmdsock.getLocalAddress();
            addr1 = naddr.getAddress();
        }
        out.write(PROTO_VERS4);
        out.write(BIND);
        out.write((super.getLocalPort() >> 8) & 0xff);
        out.write((super.getLocalPort() >> 0) & 0xff);
        out.write(addr1);
        String userName = getUserName();
        try {
            out.write(userName.getBytes("ISO-8859-1"));
        } catch (java.io.UnsupportedEncodingException uee) {
            assert false;
        }
        out.write(0);
        out.flush();
        byte[] data = new byte[8];
        int n = readSocksReply(in, data);
        if (n != 8)
            throw new SocketException("Reply from SOCKS server has bad length: " + n);
        if (data[0] != 0 && data[0] != 4)
            throw new SocketException("Reply from SOCKS server has bad version");
        SocketException ex = null;
        switch (data[1]) {
        case 90:
            external_address = new InetSocketAddress(baddr, lport);
            break;
        case 91:
            ex = new SocketException("SOCKS request rejected");
            break;
        case 92:
            ex = new SocketException("SOCKS server couldn't reach destination");
            break;
        case 93:
            ex = new SocketException("SOCKS authentication failed");
            break;
        default:
            ex = new SocketException("Reply from SOCKS server contains bad status");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
    }
    protected synchronized void socksBind(InetSocketAddress saddr) throws IOException {
        if (socket != null) {
            return;
        }
        if (server == null) {
            ProxySelector sel = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<ProxySelector>() {
                    public ProxySelector run() {
                            return ProxySelector.getDefault();
                        }
                    });
            if (sel == null) {
                return;
            }
            URI uri;
            String host = saddr.getHostString();
            if (saddr.getAddress() instanceof Inet6Address &&
                (!host.startsWith("[")) && (host.indexOf(":") >= 0)) {
                host = "[" + host + "]";
            }
            try {
                uri = new URI("serversocket:
            } catch (URISyntaxException e) {
                assert false : e;
                uri = null;
            }
            Proxy p = null;
            Exception savedExc = null;
            java.util.Iterator<Proxy> iProxy = null;
            iProxy = sel.select(uri).iterator();
            if (iProxy == null || !(iProxy.hasNext())) {
                return;
            }
            while (iProxy.hasNext()) {
                p = iProxy.next();
                if (p == null || p == Proxy.NO_PROXY) {
                    return;
                }
                if (p.type() != Proxy.Type.SOCKS)
                    throw new SocketException("Unknown proxy type : " + p.type());
                if (!(p.address() instanceof InetSocketAddress))
                    throw new SocketException("Unknow address type for proxy: " + p);
                server = ((InetSocketAddress) p.address()).getHostString();
                serverPort = ((InetSocketAddress) p.address()).getPort();
                if (p instanceof SocksProxy) {
                    if (((SocksProxy)p).protocolVersion() == 4) {
                        useV4 = true;
                    }
                }
                try {
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Void>() {
                            public Void run() throws Exception {
                                cmdsock = new Socket(new PlainSocketImpl());
                                cmdsock.connect(new InetSocketAddress(server, serverPort));
                                cmdIn = cmdsock.getInputStream();
                                cmdOut = cmdsock.getOutputStream();
                                return null;
                            }
                        });
                } catch (Exception e) {
                    sel.connectFailed(uri,p.address(),new SocketException(e.getMessage()));
                    server = null;
                    serverPort = -1;
                    cmdsock = null;
                    savedExc = e;
                }
            }
            if (server == null || cmdsock == null) {
                throw new SocketException("Can't connect to SOCKS proxy:"
                                          + savedExc.getMessage());
            }
        } else {
            try {
                AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws Exception {
                            cmdsock = new Socket(new PlainSocketImpl());
                            cmdsock.connect(new InetSocketAddress(server, serverPort));
                            cmdIn = cmdsock.getInputStream();
                            cmdOut = cmdsock.getOutputStream();
                            return null;
                        }
                    });
            } catch (Exception e) {
                throw new SocketException(e.getMessage());
            }
        }
        BufferedOutputStream out = new BufferedOutputStream(cmdOut, 512);
        InputStream in = cmdIn;
        if (useV4) {
            bindV4(in, out, saddr.getAddress(), saddr.getPort());
            return;
        }
        out.write(PROTO_VERS);
        out.write(2);
        out.write(NO_AUTH);
        out.write(USER_PASSW);
        out.flush();
        byte[] data = new byte[2];
        int i = readSocksReply(in, data);
        if (i != 2 || ((int)data[0]) != PROTO_VERS) {
            bindV4(in, out, saddr.getAddress(), saddr.getPort());
            return;
        }
        if (((int)data[1]) == NO_METHODS)
            throw new SocketException("SOCKS : No acceptable methods");
        if (!authenticate(data[1], in, out)) {
            throw new SocketException("SOCKS : authentication failed");
        }
        out.write(PROTO_VERS);
        out.write(BIND);
        out.write(0);
        int lport = saddr.getPort();
        if (saddr.isUnresolved()) {
            out.write(DOMAIN_NAME);
            out.write(saddr.getHostName().length());
            try {
                out.write(saddr.getHostName().getBytes("ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException uee) {
                assert false;
            }
            out.write((lport >> 8) & 0xff);
            out.write((lport >> 0) & 0xff);
        } else if (saddr.getAddress() instanceof Inet4Address) {
            byte[] addr1 = saddr.getAddress().getAddress();
            out.write(IPV4);
            out.write(addr1);
            out.write((lport >> 8) & 0xff);
            out.write((lport >> 0) & 0xff);
            out.flush();
        } else if (saddr.getAddress() instanceof Inet6Address) {
            byte[] addr1 = saddr.getAddress().getAddress();
            out.write(IPV6);
            out.write(addr1);
            out.write((lport >> 8) & 0xff);
            out.write((lport >> 0) & 0xff);
            out.flush();
        } else {
            cmdsock.close();
            throw new SocketException("unsupported address type : " + saddr);
        }
        data = new byte[4];
        i = readSocksReply(in, data);
        SocketException ex = null;
        int len, nport;
        byte[] addr;
        switch (data[1]) {
        case REQUEST_OK:
            switch(data[3]) {
            case IPV4:
                addr = new byte[4];
                i = readSocksReply(in, addr);
                if (i != 4)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                external_address =
                    new InetSocketAddress(new Inet4Address("", addr) , nport);
                break;
            case DOMAIN_NAME:
                len = data[1];
                byte[] host = new byte[len];
                i = readSocksReply(in, host);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                external_address = new InetSocketAddress(new String(host), nport);
                break;
            case IPV6:
                len = data[1];
                addr = new byte[len];
                i = readSocksReply(in, addr);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                external_address =
                    new InetSocketAddress(new Inet6Address("", addr), nport);
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS server general failure");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: Bind not allowed by ruleset");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: Network unreachable");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: Host unreachable");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: Connection refused");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL expired");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: Command not supported");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: address type not supported");
            break;
        }
        if (ex != null) {
            in.close();
            out.close();
            cmdsock.close();
            cmdsock = null;
            throw ex;
        }
        cmdIn = in;
        cmdOut = out;
    }
    protected void acceptFrom(SocketImpl s, InetSocketAddress saddr) throws IOException {
        if (cmdsock == null) {
            return;
        }
        InputStream in = cmdIn;
        socksBind(saddr);
        in.read();
        int i = in.read();
        in.read();
        SocketException ex = null;
        int nport;
        byte[] addr;
        InetSocketAddress real_end = null;
        switch (i) {
        case REQUEST_OK:
            i = in.read();
            switch(i) {
            case IPV4:
                addr = new byte[4];
                readSocksReply(in, addr);
                nport = in.read() << 8;
                nport += in.read();
                real_end =
                    new InetSocketAddress(new Inet4Address("", addr) , nport);
                break;
            case DOMAIN_NAME:
                int len = in.read();
                addr = new byte[len];
                readSocksReply(in, addr);
                nport = in.read() << 8;
                nport += in.read();
                real_end = new InetSocketAddress(new String(addr), nport);
                break;
            case IPV6:
                addr = new byte[16];
                readSocksReply(in, addr);
                nport = in.read() << 8;
                nport += in.read();
                real_end =
                    new InetSocketAddress(new Inet6Address("", addr), nport);
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS server general failure");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: Accept not allowed by ruleset");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: Network unreachable");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: Host unreachable");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: Connection refused");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL expired");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: Command not supported");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: address type not supported");
            break;
        }
        if (ex != null) {
            cmdIn.close();
            cmdOut.close();
            cmdsock.close();
            cmdsock = null;
            throw ex;
        }
        if (s instanceof SocksSocketImpl) {
            ((SocksSocketImpl)s).external_address = real_end;
        }
        if (s instanceof PlainSocketImpl) {
            PlainSocketImpl psi = (PlainSocketImpl) s;
            psi.setInputStream((SocketInputStream) in);
            psi.setFileDescriptor(cmdsock.getImpl().getFileDescriptor());
            psi.setAddress(cmdsock.getImpl().getInetAddress());
            psi.setPort(cmdsock.getImpl().getPort());
            psi.setLocalPort(cmdsock.getImpl().getLocalPort());
        } else {
            s.fd = cmdsock.getImpl().fd;
            s.address = cmdsock.getImpl().address;
            s.port = cmdsock.getImpl().port;
            s.localport = cmdsock.getImpl().localport;
        }
        cmdsock = null;
    }
    @Override
    protected InetAddress getInetAddress() {
        if (external_address != null)
            return external_address.getAddress();
        else
            return super.getInetAddress();
    }
    @Override
    protected int getPort() {
        if (external_address != null)
            return external_address.getPort();
        else
            return super.getPort();
    }
    @Override
    protected int getLocalPort() {
        if (socket != null)
            return super.getLocalPort();
        if (external_address != null)
            return external_address.getPort();
        else
            return super.getLocalPort();
    }
    @Override
    protected void close() throws IOException {
        if (cmdsock != null)
            cmdsock.close();
        cmdsock = null;
        super.close();
    }
    private String getUserName() {
        String userName = "";
        if (applicationSetProxy) {
            try {
                userName = System.getProperty("user.name");
            } catch (SecurityException se) {  }
        } else {
            userName = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("user.name"));
        }
        return userName;
    }
}
