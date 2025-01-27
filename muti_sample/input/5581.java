class SourceChannelImpl
    extends Pipe.SourceChannel
    implements SelChImpl
{
    private static NativeDispatcher nd;
    FileDescriptor fd;
    int fdVal;
    private volatile long thread = 0;
    private final Object lock = new Object();
    private final Object stateLock = new Object();
    private static final int ST_UNINITIALIZED = -1;
    private static final int ST_INUSE = 0;
    private static final int ST_KILLED = 1;
    private volatile int state = ST_UNINITIALIZED;
    public FileDescriptor getFD() {
        return fd;
    }
    public int getFDVal() {
        return fdVal;
    }
    SourceChannelImpl(SelectorProvider sp, FileDescriptor fd) {
        super(sp);
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_INUSE;
    }
    protected void implCloseSelectableChannel() throws IOException {
        synchronized (stateLock) {
            nd.preClose(fd);
            long th = thread;
            if (th != 0)
                NativeThread.signal(th);
            if (!isRegistered())
                kill();
        }
    }
    public void kill() throws IOException {
        synchronized (stateLock) {
            if (state == ST_KILLED)
                return;
            if (state == ST_UNINITIALIZED) {
                state = ST_KILLED;
                return;
            }
            assert !isOpen() && !isRegistered();
            nd.close(fd);
            state = ST_KILLED;
        }
    }
    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(fd, block);
    }
    public boolean translateReadyOps(int ops, int initialOps,
                                     SelectionKeyImpl sk) {
        int intOps = sk.nioInterestOps(); 
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((ops & PollArrayWrapper.POLLNVAL) != 0)
            throw new Error("POLLNVAL detected");
        if ((ops & (PollArrayWrapper.POLLERR
                    | PollArrayWrapper.POLLHUP)) != 0) {
            newOps = intOps;
            sk.nioReadyOps(newOps);
            return (newOps & ~oldOps) != 0;
        }
        if (((ops & PollArrayWrapper.POLLIN) != 0) &&
            ((intOps & SelectionKey.OP_READ) != 0))
            newOps |= SelectionKey.OP_READ;
        sk.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }
    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }
    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }
    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        if (ops == SelectionKey.OP_READ)
            ops = PollArrayWrapper.POLLIN;
        sk.selector.putEventOps(sk, ops);
    }
    private void ensureOpen() throws IOException {
        if (!isOpen())
            throw new ClosedChannelException();
    }
    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();
        synchronized (lock) {
            int n = 0;
            try {
                begin();
                if (!isOpen())
                    return 0;
                thread = NativeThread.current();
                do {
                    n = IOUtil.read(fd, dst, -1, nd, lock);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                return IOStatus.normalize(n);
            } finally {
                thread = 0;
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                assert IOStatus.check(n);
            }
        }
    }
    public long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException
    {
        if ((offset < 0) || (length < 0) || (offset > dsts.length - length))
           throw new IndexOutOfBoundsException();
        return read(Util.subsequence(dsts, offset, length));
    }
    public long read(ByteBuffer[] dsts) throws IOException {
        if (dsts == null)
            throw new NullPointerException();
        ensureOpen();
        synchronized (lock) {
            long n = 0;
            try {
                begin();
                if (!isOpen())
                    return 0;
                thread = NativeThread.current();
                do {
                    n = IOUtil.read(fd, dsts, nd);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                return IOStatus.normalize(n);
            } finally {
                thread = 0;
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                assert IOStatus.check(n);
            }
        }
    }
    static {
        Util.load();
        nd = new FileDispatcherImpl();
    }
}
