class ServerImpl implements TimeSource {
    private String protocol;
    private boolean https;
    private Executor executor;
    private HttpsConfigurator httpsConfig;
    private SSLContext sslContext;
    private ContextList contexts;
    private InetSocketAddress address;
    private ServerSocketChannel schan;
    private Selector selector;
    private SelectionKey listenerKey;
    private Set<HttpConnection> idleConnections;
    private Set<HttpConnection> allConnections;
    private Set<HttpConnection> reqConnections;
    private Set<HttpConnection> rspConnections;
    private List<Event> events;
    private Object lolock = new Object();
    private volatile boolean finished = false;
    private volatile boolean terminating = false;
    private boolean bound = false;
    private boolean started = false;
    private volatile long time;  
    private volatile long subticks = 0;
    private volatile long ticks; 
    private HttpServer wrapper;
    final static int CLOCK_TICK = ServerConfig.getClockTick();
    final static long IDLE_INTERVAL = ServerConfig.getIdleInterval();
    final static int MAX_IDLE_CONNECTIONS = ServerConfig.getMaxIdleConnections();
    final static long TIMER_MILLIS = ServerConfig.getTimerMillis ();
    final static long MAX_REQ_TIME=getTimeMillis(ServerConfig.getMaxReqTime());
    final static long MAX_RSP_TIME=getTimeMillis(ServerConfig.getMaxRspTime());
    final static boolean timer1Enabled = MAX_REQ_TIME != -1 || MAX_RSP_TIME != -1;
    private Timer timer, timer1;
    private Logger logger;
    ServerImpl (
        HttpServer wrapper, String protocol, InetSocketAddress addr, int backlog
    ) throws IOException {
        this.protocol = protocol;
        this.wrapper = wrapper;
        this.logger = Logger.getLogger ("com.sun.net.httpserver");
        ServerConfig.checkLegacyProperties (logger);
        https = protocol.equalsIgnoreCase ("https");
        this.address = addr;
        contexts = new ContextList();
        schan = ServerSocketChannel.open();
        if (addr != null) {
            ServerSocket socket = schan.socket();
            socket.bind (addr, backlog);
            bound = true;
        }
        selector = Selector.open ();
        schan.configureBlocking (false);
        listenerKey = schan.register (selector, SelectionKey.OP_ACCEPT);
        dispatcher = new Dispatcher();
        idleConnections = Collections.synchronizedSet (new HashSet<HttpConnection>());
        allConnections = Collections.synchronizedSet (new HashSet<HttpConnection>());
        reqConnections = Collections.synchronizedSet (new HashSet<HttpConnection>());
        rspConnections = Collections.synchronizedSet (new HashSet<HttpConnection>());
        time = System.currentTimeMillis();
        timer = new Timer ("server-timer", true);
        timer.schedule (new ServerTimerTask(), CLOCK_TICK, CLOCK_TICK);
        if (timer1Enabled) {
            timer1 = new Timer ("server-timer1", true);
            timer1.schedule (new ServerTimerTask1(),TIMER_MILLIS,TIMER_MILLIS);
            logger.config ("HttpServer timer1 enabled period in ms:  "+TIMER_MILLIS);
            logger.config ("MAX_REQ_TIME:  "+MAX_REQ_TIME);
            logger.config ("MAX_RSP_TIME:  "+MAX_RSP_TIME);
        }
        events = new LinkedList<Event>();
        logger.config ("HttpServer created "+protocol+" "+ addr);
    }
    public void bind (InetSocketAddress addr, int backlog) throws IOException {
        if (bound) {
            throw new BindException ("HttpServer already bound");
        }
        if (addr == null) {
            throw new NullPointerException ("null address");
        }
        ServerSocket socket = schan.socket();
        socket.bind (addr, backlog);
        bound = true;
    }
    public void start () {
        if (!bound || started || finished) {
            throw new IllegalStateException ("server in wrong state");
        }
        if (executor == null) {
            executor = new DefaultExecutor();
        }
        Thread t = new Thread (dispatcher);
        started = true;
        t.start();
    }
    public void setExecutor (Executor executor) {
        if (started) {
            throw new IllegalStateException ("server already started");
        }
        this.executor = executor;
    }
    private static class DefaultExecutor implements Executor {
        public void execute (Runnable task) {
            task.run();
        }
    }
    public Executor getExecutor () {
        return executor;
    }
    public void setHttpsConfigurator (HttpsConfigurator config) {
        if (config == null) {
            throw new NullPointerException ("null HttpsConfigurator");
        }
        if (started) {
            throw new IllegalStateException ("server already started");
        }
        this.httpsConfig = config;
        sslContext = config.getSSLContext();
    }
    public HttpsConfigurator getHttpsConfigurator () {
        return httpsConfig;
    }
    public void stop (int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException ("negative delay parameter");
        }
        terminating = true;
        try { schan.close(); } catch (IOException e) {}
        selector.wakeup();
        long latest = System.currentTimeMillis() + delay * 1000;
        while (System.currentTimeMillis() < latest) {
            delay();
            if (finished) {
                break;
            }
        }
        finished = true;
        selector.wakeup();
        synchronized (allConnections) {
            for (HttpConnection c : allConnections) {
                c.close();
            }
        }
        allConnections.clear();
        idleConnections.clear();
        timer.cancel();
        if (timer1Enabled) {
            timer1.cancel();
        }
    }
    Dispatcher dispatcher;
    public synchronized HttpContextImpl createContext (String path, HttpHandler handler) {
        if (handler == null || path == null) {
            throw new NullPointerException ("null handler, or path parameter");
        }
        HttpContextImpl context = new HttpContextImpl (protocol, path, handler, this);
        contexts.add (context);
        logger.config ("context created: " + path);
        return context;
    }
    public synchronized HttpContextImpl createContext (String path) {
        if (path == null) {
            throw new NullPointerException ("null path parameter");
        }
        HttpContextImpl context = new HttpContextImpl (protocol, path, null, this);
        contexts.add (context);
        logger.config ("context created: " + path);
        return context;
    }
    public synchronized void removeContext (String path) throws IllegalArgumentException {
        if (path == null) {
            throw new NullPointerException ("null path parameter");
        }
        contexts.remove (protocol, path);
        logger.config ("context removed: " + path);
    }
    public synchronized void removeContext (HttpContext context) throws IllegalArgumentException {
        if (!(context instanceof HttpContextImpl)) {
            throw new IllegalArgumentException ("wrong HttpContext type");
        }
        contexts.remove ((HttpContextImpl)context);
        logger.config ("context removed: " + context.getPath());
    }
    public InetSocketAddress getAddress() {
        return (InetSocketAddress)schan.socket().getLocalSocketAddress();
    }
    Selector getSelector () {
        return selector;
    }
    void addEvent (Event r) {
        synchronized (lolock) {
            events.add (r);
            selector.wakeup();
        }
    }
    class Dispatcher implements Runnable {
        private void handleEvent (Event r) {
            ExchangeImpl t = r.exchange;
            HttpConnection c = t.getConnection();
            try {
                if (r instanceof WriteFinishedEvent) {
                    int exchanges = endExchange();
                    if (terminating && exchanges == 0) {
                        finished = true;
                    }
                    responseCompleted (c);
                    LeftOverInputStream is = t.getOriginalInputStream();
                    if (!is.isEOF()) {
                        t.close = true;
                    }
                    if (t.close || idleConnections.size() >= MAX_IDLE_CONNECTIONS) {
                        c.close();
                        allConnections.remove (c);
                    } else {
                        if (is.isDataBuffered()) {
                            requestStarted (c);
                            handle (c.getChannel(), c);
                        } else {
                            connsToRegister.add (c);
                        }
                    }
                }
            } catch (IOException e) {
                logger.log (
                    Level.FINER, "Dispatcher (1)", e
                );
                c.close();
            }
        }
        final LinkedList<HttpConnection> connsToRegister =
                new LinkedList<HttpConnection>();
        void reRegister (HttpConnection c) {
            try {
                SocketChannel chan = c.getChannel();
                chan.configureBlocking (false);
                SelectionKey key = chan.register (selector, SelectionKey.OP_READ);
                key.attach (c);
                c.selectionKey = key;
                c.time = getTime() + IDLE_INTERVAL;
                idleConnections.add (c);
            } catch (IOException e) {
                dprint(e);
                logger.log(Level.FINER, "Dispatcher(8)", e);
                c.close();
            }
        }
        public void run() {
            while (!finished) {
                try {
                    ListIterator<HttpConnection> li =
                        connsToRegister.listIterator();
                    for (HttpConnection c : connsToRegister) {
                        reRegister(c);
                    }
                    connsToRegister.clear();
                    List<Event> list = null;
                    selector.select(1000);
                    synchronized (lolock) {
                        if (events.size() > 0) {
                            list = events;
                            events = new LinkedList<Event>();
                        }
                    }
                    if (list != null) {
                        for (Event r: list) {
                            handleEvent (r);
                        }
                    }
                    Set<SelectionKey> selected = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selected.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove ();
                        if (key.equals (listenerKey)) {
                            if (terminating) {
                                continue;
                            }
                            SocketChannel chan = schan.accept();
                            if (chan == null) {
                                continue; 
                            }
                            chan.configureBlocking (false);
                            SelectionKey newkey = chan.register (selector, SelectionKey.OP_READ);
                            HttpConnection c = new HttpConnection ();
                            c.selectionKey = newkey;
                            c.setChannel (chan);
                            newkey.attach (c);
                            requestStarted (c);
                            allConnections.add (c);
                        } else {
                            try {
                                if (key.isReadable()) {
                                    boolean closed;
                                    SocketChannel chan = (SocketChannel)key.channel();
                                    HttpConnection conn = (HttpConnection)key.attachment();
                                    key.cancel();
                                    chan.configureBlocking (true);
                                    if (idleConnections.remove(conn)) {
                                        requestStarted (conn);
                                    }
                                    handle (chan, conn);
                                } else {
                                    assert false;
                                }
                            } catch (CancelledKeyException e) {
                                handleException(key, null);
                            } catch (IOException e) {
                                handleException(key, e);
                            }
                        }
                    }
                    selector.selectNow();
                } catch (IOException e) {
                    logger.log (Level.FINER, "Dispatcher (4)", e);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log (Level.FINER, "Dispatcher (7)", e);
                }
            }
        }
        private void handleException (SelectionKey key, Exception e) {
            HttpConnection conn = (HttpConnection)key.attachment();
            if (e != null) {
                logger.log (Level.FINER, "Dispatcher (2)", e);
            }
            closeConnection(conn);
        }
        public void handle (SocketChannel chan, HttpConnection conn)
        throws IOException
        {
            try {
                Exchange t = new Exchange (chan, protocol, conn);
                executor.execute (t);
            } catch (HttpError e1) {
                logger.log (Level.FINER, "Dispatcher (4)", e1);
                closeConnection(conn);
            } catch (IOException e) {
                logger.log (Level.FINER, "Dispatcher (5)", e);
                closeConnection(conn);
            }
        }
    }
    static boolean debug = ServerConfig.debugEnabled ();
    static synchronized void dprint (String s) {
        if (debug) {
            System.out.println (s);
        }
    }
    static synchronized void dprint (Exception e) {
        if (debug) {
            System.out.println (e);
            e.printStackTrace();
        }
    }
    Logger getLogger () {
        return logger;
    }
    private void closeConnection(HttpConnection conn) {
        conn.close();
        allConnections.remove(conn);
        switch (conn.getState()) {
        case REQUEST:
            reqConnections.remove(conn);
            break;
        case RESPONSE:
            rspConnections.remove(conn);
            break;
        case IDLE:
            idleConnections.remove(conn);
            break;
        }
        assert !reqConnections.remove(conn);
        assert !rspConnections.remove(conn);
        assert !idleConnections.remove(conn);
    }
    class Exchange implements Runnable {
        SocketChannel chan;
        HttpConnection connection;
        HttpContextImpl context;
        InputStream rawin;
        OutputStream rawout;
        String protocol;
        ExchangeImpl tx;
        HttpContextImpl ctx;
        boolean rejected = false;
        Exchange (SocketChannel chan, String protocol, HttpConnection conn) throws IOException {
            this.chan = chan;
            this.connection = conn;
            this.protocol = protocol;
        }
        public void run () {
            context = connection.getHttpContext();
            boolean newconnection;
            SSLEngine engine = null;
            String requestLine = null;
            SSLStreams sslStreams = null;
            try {
                if (context != null ) {
                    this.rawin = connection.getInputStream();
                    this.rawout = connection.getRawOutputStream();
                    newconnection = false;
                } else {
                    newconnection = true;
                    if (https) {
                        if (sslContext == null) {
                            logger.warning ("SSL connection received. No https contxt created");
                            throw new HttpError ("No SSL context established");
                        }
                        sslStreams = new SSLStreams (ServerImpl.this, sslContext, chan);
                        rawin = sslStreams.getInputStream();
                        rawout = sslStreams.getOutputStream();
                        engine = sslStreams.getSSLEngine();
                        connection.sslStreams = sslStreams;
                    } else {
                        rawin = new BufferedInputStream(
                            new Request.ReadStream (
                                ServerImpl.this, chan
                        ));
                        rawout = new Request.WriteStream (
                            ServerImpl.this, chan
                        );
                    }
                    connection.raw = rawin;
                    connection.rawout = rawout;
                }
                Request req = new Request (rawin, rawout);
                requestLine = req.requestLine();
                if (requestLine == null) {
                    closeConnection(connection);
                    return;
                }
                int space = requestLine.indexOf (' ');
                if (space == -1) {
                    reject (Code.HTTP_BAD_REQUEST,
                            requestLine, "Bad request line");
                    return;
                }
                String method = requestLine.substring (0, space);
                int start = space+1;
                space = requestLine.indexOf(' ', start);
                if (space == -1) {
                    reject (Code.HTTP_BAD_REQUEST,
                            requestLine, "Bad request line");
                    return;
                }
                String uriStr = requestLine.substring (start, space);
                URI uri = new URI (uriStr);
                start = space+1;
                String version = requestLine.substring (start);
                Headers headers = req.headers();
                String s = headers.getFirst ("Transfer-encoding");
                long clen = 0L;
                if (s !=null && s.equalsIgnoreCase ("chunked")) {
                    clen = -1L;
                } else {
                    s = headers.getFirst ("Content-Length");
                    if (s != null) {
                        clen = Long.parseLong(s);
                    }
                    if (clen == 0) {
                        requestCompleted (connection);
                    }
                }
                ctx = contexts.findContext (protocol, uri.getPath());
                if (ctx == null) {
                    reject (Code.HTTP_NOT_FOUND,
                            requestLine, "No context found for request");
                    return;
                }
                connection.setContext (ctx);
                if (ctx.getHandler() == null) {
                    reject (Code.HTTP_INTERNAL_ERROR,
                            requestLine, "No handler for context");
                    return;
                }
                tx = new ExchangeImpl (
                    method, uri, req, clen, connection
                );
                String chdr = headers.getFirst("Connection");
                Headers rheaders = tx.getResponseHeaders();
                if (chdr != null && chdr.equalsIgnoreCase ("close")) {
                    tx.close = true;
                }
                if (version.equalsIgnoreCase ("http/1.0")) {
                    tx.http10 = true;
                    if (chdr == null) {
                        tx.close = true;
                        rheaders.set ("Connection", "close");
                    } else if (chdr.equalsIgnoreCase ("keep-alive")) {
                        rheaders.set ("Connection", "keep-alive");
                        int idle=(int)ServerConfig.getIdleInterval()/1000;
                        int max=(int)ServerConfig.getMaxIdleConnections();
                        String val = "timeout="+idle+", max="+max;
                        rheaders.set ("Keep-Alive", val);
                    }
                }
                if (newconnection) {
                    connection.setParameters (
                        rawin, rawout, chan, engine, sslStreams,
                        sslContext, protocol, ctx, rawin
                    );
                }
                String exp = headers.getFirst("Expect");
                if (exp != null && exp.equalsIgnoreCase ("100-continue")) {
                    logReply (100, requestLine, null);
                    sendReply (
                        Code.HTTP_CONTINUE, false, null
                    );
                }
                List<Filter> sf = ctx.getSystemFilters();
                List<Filter> uf = ctx.getFilters();
                Filter.Chain sc = new Filter.Chain(sf, ctx.getHandler());
                Filter.Chain uc = new Filter.Chain(uf, new LinkHandler (sc));
                tx.getRequestBody();
                tx.getResponseBody();
                if (https) {
                    uc.doFilter (new HttpsExchangeImpl (tx));
                } else {
                    uc.doFilter (new HttpExchangeImpl (tx));
                }
            } catch (IOException e1) {
                logger.log (Level.FINER, "ServerImpl.Exchange (1)", e1);
                closeConnection(connection);
            } catch (NumberFormatException e3) {
                reject (Code.HTTP_BAD_REQUEST,
                        requestLine, "NumberFormatException thrown");
            } catch (URISyntaxException e) {
                reject (Code.HTTP_BAD_REQUEST,
                        requestLine, "URISyntaxException thrown");
            } catch (Exception e4) {
                logger.log (Level.FINER, "ServerImpl.Exchange (2)", e4);
                closeConnection(connection);
            }
        }
        class LinkHandler implements HttpHandler {
            Filter.Chain nextChain;
            LinkHandler (Filter.Chain nextChain) {
                this.nextChain = nextChain;
            }
            public void handle (HttpExchange exchange) throws IOException {
                nextChain.doFilter (exchange);
            }
        }
        void reject (int code, String requestStr, String message) {
            rejected = true;
            logReply (code, requestStr, message);
            sendReply (
                code, false, "<h1>"+code+Code.msg(code)+"</h1>"+message
            );
            closeConnection(connection);
        }
        void sendReply (
            int code, boolean closeNow, String text)
        {
            try {
                StringBuilder builder = new StringBuilder (512);
                builder.append ("HTTP/1.1 ")
                    .append (code).append (Code.msg(code)).append ("\r\n");
                if (text != null && text.length() != 0) {
                    builder.append ("Content-Length: ")
                        .append (text.length()).append ("\r\n")
                        .append ("Content-Type: text/html\r\n");
                } else {
                    builder.append ("Content-Length: 0\r\n");
                    text = "";
                }
                if (closeNow) {
                    builder.append ("Connection: close\r\n");
                }
                builder.append ("\r\n").append (text);
                String s = builder.toString();
                byte[] b = s.getBytes("ISO8859_1");
                rawout.write (b);
                rawout.flush();
                if (closeNow) {
                    closeConnection(connection);
                }
            } catch (IOException e) {
                logger.log (Level.FINER, "ServerImpl.sendReply", e);
                closeConnection(connection);
            }
        }
    }
    void logReply (int code, String requestStr, String text) {
        if (!logger.isLoggable(Level.FINE)) {
            return;
        }
        if (text == null) {
            text = "";
        }
        String r;
        if (requestStr.length() > 80) {
           r = requestStr.substring (0, 80) + "<TRUNCATED>";
        } else {
           r = requestStr;
        }
        String message = r + " [" + code + " " +
                    Code.msg(code) + "] ("+text+")";
        logger.fine (message);
    }
    long getTicks() {
        return ticks;
    }
    public long getTime() {
        return time;
    }
    void delay () {
        Thread.yield();
        try {
            Thread.sleep (200);
        } catch (InterruptedException e) {}
    }
    private int exchangeCount = 0;
    synchronized void startExchange () {
        exchangeCount ++;
    }
    synchronized int endExchange () {
        exchangeCount --;
        assert exchangeCount >= 0;
        return exchangeCount;
    }
    HttpServer getWrapper () {
        return wrapper;
    }
    void requestStarted (HttpConnection c) {
        c.creationTime = getTime();
        c.setState (State.REQUEST);
        reqConnections.add (c);
    }
    void requestCompleted (HttpConnection c) {
        assert c.getState() == State.REQUEST;
        reqConnections.remove (c);
        c.rspStartedTime = getTime();
        rspConnections.add (c);
        c.setState (State.RESPONSE);
    }
    void responseCompleted (HttpConnection c) {
        assert c.getState() == State.RESPONSE;
        rspConnections.remove (c);
        c.setState (State.IDLE);
    }
    class ServerTimerTask extends TimerTask {
        public void run () {
            LinkedList<HttpConnection> toClose = new LinkedList<HttpConnection>();
            time = System.currentTimeMillis();
            ticks ++;
            synchronized (idleConnections) {
                for (HttpConnection c : idleConnections) {
                    if (c.time <= time) {
                        toClose.add (c);
                    }
                }
                for (HttpConnection c : toClose) {
                    idleConnections.remove (c);
                    allConnections.remove (c);
                    c.close();
                }
            }
        }
    }
    class ServerTimerTask1 extends TimerTask {
        public void run () {
            LinkedList<HttpConnection> toClose = new LinkedList<HttpConnection>();
            time = System.currentTimeMillis();
            synchronized (reqConnections) {
                if (MAX_REQ_TIME != -1) {
                    for (HttpConnection c : reqConnections) {
                        if (c.creationTime + TIMER_MILLIS + MAX_REQ_TIME <= time) {
                            toClose.add (c);
                        }
                    }
                    for (HttpConnection c : toClose) {
                        logger.log (Level.FINE, "closing: no request: " + c);
                        reqConnections.remove (c);
                        allConnections.remove (c);
                        c.close();
                    }
                }
            }
            toClose = new LinkedList<HttpConnection>();
            synchronized (rspConnections) {
                if (MAX_RSP_TIME != -1) {
                    for (HttpConnection c : rspConnections) {
                        if (c.rspStartedTime + TIMER_MILLIS +MAX_RSP_TIME <= time) {
                            toClose.add (c);
                        }
                    }
                    for (HttpConnection c : toClose) {
                        logger.log (Level.FINE, "closing: no response: " + c);
                        rspConnections.remove (c);
                        allConnections.remove (c);
                        c.close();
                    }
                }
            }
        }
    }
    void logStackTrace (String s) {
        logger.finest (s);
        StringBuilder b = new StringBuilder ();
        StackTraceElement[] e = Thread.currentThread().getStackTrace();
        for (int i=0; i<e.length; i++) {
            b.append (e[i].toString()).append("\n");
        }
        logger.finest (b.toString());
    }
    static long getTimeMillis(long secs) {
        if (secs == -1) {
            return -1;
        } else {
            return secs * 1000;
        }
    }
}
