public class SocketOptionTests {
    final String osName = AccessController.doPrivileged(
                    new GetPropertyAction("os.name"));
    <T> void checkOption(SctpChannel sc, SctpSocketOption<T> name,
            T expectedValue) throws IOException {
        T value = sc.getOption(name);
        check(value.equals(expectedValue), name + ": value (" + value +
                ") not as expected (" + expectedValue + ")");
       }
    <T> void optionalSupport(SctpChannel sc, SctpSocketOption<T> name,
            T value) {
        try {
            sc.setOption(name, value);
            checkOption(sc, name, value);
        } catch (IOException e) {
            out.println(name + " not supported. " + e);
        }
    }
    void test(String[] args) {
        if (!Util.isSCTPSupported()) {
            out.println("SCTP protocol is not supported");
            out.println("Test cannot be run");
            return;
        }
        try {
            SctpChannel sc = SctpChannel.open();
            Set<SctpSocketOption<?>> options = sc.supportedOptions();
            List<? extends SctpSocketOption<?>> expected = Arrays.<SctpSocketOption<?>>asList(
                    SCTP_DISABLE_FRAGMENTS, SCTP_EXPLICIT_COMPLETE,
                    SCTP_FRAGMENT_INTERLEAVE, SCTP_INIT_MAXSTREAMS,
                    SCTP_NODELAY, SCTP_PRIMARY_ADDR, SCTP_SET_PEER_PRIMARY_ADDR,
                    SO_SNDBUF, SO_RCVBUF, SO_LINGER);
            for (SctpSocketOption opt: expected) {
                if (!options.contains(opt))
                    fail(opt.name() + " should be supported");
            }
            InitMaxStreams streams = InitMaxStreams.create(1024, 1024);
            sc.setOption(SCTP_INIT_MAXSTREAMS, streams);
            checkOption(sc, SCTP_INIT_MAXSTREAMS, streams);
            streams = sc.getOption(SCTP_INIT_MAXSTREAMS);
            check(streams.maxInStreams() == 1024, "Max in streams: value: "
                    + streams.maxInStreams() + ", expected 1024 ");
            check(streams.maxOutStreams() == 1024, "Max out streams: value: "
                    + streams.maxOutStreams() + ", expected 1024 ");
            optionalSupport(sc, SCTP_DISABLE_FRAGMENTS, true);
            optionalSupport(sc, SCTP_EXPLICIT_COMPLETE, true);
            optionalSupport(sc, SCTP_FRAGMENT_INTERLEAVE, 1);
            sc.setOption(SCTP_NODELAY, true);
            checkOption(sc, SCTP_NODELAY, true);
            sc.setOption(SO_SNDBUF, 16*1024);
            checkOption(sc, SO_SNDBUF, 16*1024);
            sc.setOption(SO_RCVBUF, 16*1024);
            checkOption(sc, SO_RCVBUF, 16*1024);
            checkOption(sc, SO_LINGER, -1);  
            sc.setOption(SO_LINGER, 2000);
            checkOption(sc, SO_LINGER, 2000);
            sctpPrimaryAddr();
            try {
                sc.setOption(null, "value");
                fail("NullPointerException not thrown for setOption");
            } catch (NullPointerException unused) {
                pass();
            }
            try {
               sc.getOption(null);
               fail("NullPointerException not thrown for getOption");
            } catch (NullPointerException unused) {
               pass();
            }
            sc.close();
            try {
               sc.setOption(SCTP_INIT_MAXSTREAMS, streams);
               fail("ClosedChannelException not thrown");
            } catch (ClosedChannelException unused) {
                pass();
            }
        } catch (IOException ioe) {
            unexpected(ioe);
        }
    }
    void sctpPrimaryAddr() throws IOException {
        SocketAddress addrToSet = null;;
        System.out.println("TESTING SCTP_PRIMARY_ADDR");
        SctpChannel sc = SctpChannel.open();
        SctpServerChannel ssc = SctpServerChannel.open().bind(null);
        Set<SocketAddress> addrs = ssc.getAllLocalAddresses();
        if (addrs.isEmpty())
            debug("addrs should not be empty");
        debug("Listening on " + addrs);
        InetSocketAddress serverAddr = (InetSocketAddress) addrs.iterator().next();
        debug("connecting to " + serverAddr);
        sc.connect(serverAddr);
        SctpChannel peerChannel = ssc.accept();
        ssc.close();
        Set<SocketAddress> peerAddrs = peerChannel.getAllLocalAddresses();
        debug("Peer local Addresses: ");
        for (Iterator<SocketAddress> it = peerAddrs.iterator(); it.hasNext(); ) {
            InetSocketAddress addr = (InetSocketAddress)it.next();
            debug("\t" + addr);
            addrToSet = addr;   
        }
        if ("SunOS".equals(osName)) {
            return;
        } else { 
            SocketAddress primaryAddr = sc.getOption(SCTP_PRIMARY_ADDR);
            System.out.println("SCTP_PRIMARY_ADDR returned: " + primaryAddr);
            boolean found = false;
            addrToSet = primaryAddr; 
            for (Iterator<SocketAddress> it = peerAddrs.iterator(); it.hasNext(); ) {
                InetSocketAddress addr = (InetSocketAddress)it.next();
                if (addr.equals(primaryAddr)) {
                    found = true;
                }
                addrToSet = addr;
            }
            check(found, "SCTP_PRIMARY_ADDR returned bogus address!");
            System.out.println("SCTP_PRIMARY_ADDR try set to: " + addrToSet);
            sc.setOption(SCTP_PRIMARY_ADDR, addrToSet);
            System.out.println("SCTP_PRIMARY_ADDR set to: " + addrToSet);
            primaryAddr = sc.getOption(SCTP_PRIMARY_ADDR);
            System.out.println("SCTP_PRIMARY_ADDR returned: " + primaryAddr);
            check(addrToSet.equals(primaryAddr),"SCTP_PRIMARY_ADDR not set correctly");
        }
    }
    boolean debug = true;
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    void check(boolean cond, String failMessage) {if (cond) pass(); else fail(failMessage);}
    void debug(String message) {if(debug) { System.out.println(message); }  }
    public static void main(String[] args) throws Throwable {
        Class<?> k = new Object(){}.getClass().getEnclosingClass();
        try {k.getMethod("instanceMain",String[].class)
                .invoke( k.newInstance(), (Object) args);}
        catch (Throwable e) {throw e.getCause();}}
    public void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
