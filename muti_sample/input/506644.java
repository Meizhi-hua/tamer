@TestTargetClass(AbstractSelectableChannel.class)
public class AbstractSelectableChannelTest extends TestCase {
    private MockSelectableChannel testChannel;
    protected void setUp() throws Exception {
        super.setUp();
        testChannel = new MockSelectableChannel(SelectorProvider.provider());
    }
    protected void tearDown() throws Exception {
        if (testChannel.isOpen()) {
            testChannel.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AbstractSelectableChannel",
        args = {SelectorProvider.class}
    )
    public void test_Constructor_LSelectorProvider() throws Exception {
        assertSame(SelectorProvider.provider(), testChannel.provider());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implCloseChannel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implCloseSelectableChannel",
            args = {}
        )
    })
    public void test_implClose() throws IOException {
        testChannel.isImplCloseSelectableChannelCalled = false;
        testChannel.implCloseSelectableChannelCount = 0;
        testChannel.close();
        assertFalse(testChannel.isOpen());
        assertTrue(testChannel.isImplCloseSelectableChannelCalled);
        assertEquals(1, testChannel.implCloseSelectableChannelCount);
        testChannel = new MockSelectableChannel(SelectorProvider.provider());
        testChannel.isImplCloseSelectableChannelCalled = false;
        testChannel.implCloseSelectableChannelCount = 0;
        testChannel.close();
        testChannel.close();
        assertFalse(testChannel.isOpen());
        assertTrue(testChannel.isImplCloseSelectableChannelCalled);
        assertEquals(1, testChannel.implCloseSelectableChannelCount);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "provider",
        args = {}
    )
    public void test_provider() {
        SelectorProvider provider = testChannel.provider();
        assertSame(SelectorProvider.provider(), provider);
        testChannel = new MockSelectableChannel(null);
        provider = testChannel.provider();
        assertNull(provider);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isBlocking",
        args = {}
    )
    public void test_isBlocking() throws IOException {
        assertTrue(testChannel.isBlocking());
        testChannel.configureBlocking(false);
        assertFalse(testChannel.isBlocking());
        testChannel.configureBlocking(true);
        assertTrue(testChannel.isBlocking());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "blockingLock",
        args = {}
    )
    public void test_blockingLock() {
        Object gotObj = testChannel.blockingLock();
        assertNotNull(gotObj);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "register",
        args = {java.nio.channels.Selector.class, int.class, java.lang.Object.class}
    )
    public void test_register_LSelectorILObject() throws IOException {
        assertFalse(testChannel.isRegistered());
        Selector acceptSelector1 = SelectorProvider.provider().openSelector();
        Selector acceptSelector2 = new MockAbstractSelector(SelectorProvider
                .provider());
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey acceptKey = sc.register(acceptSelector1,
                SelectionKey.OP_READ, null);
        assertNotNull(acceptKey);
        assertTrue(acceptKey.isValid());
        assertSame(sc, acceptKey.channel());
        acceptKey = sc.register(acceptSelector2, SelectionKey.OP_READ, null);
        assertTrue(((MockAbstractSelector)acceptSelector2).isRegisterCalled);
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, 0);
        selector.close();
        channel.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "register",
        args = {java.nio.channels.Selector.class, int.class, java.lang.Object.class}
    )
    public void test_register_LSelectorILObject_IllegalArgument()
            throws IOException {
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        assertTrue(acceptSelector.isOpen());
        MockSelectableChannel msc = new MockSelectableChannel(SelectorProvider
                .provider());
        msc.configureBlocking(false);
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        acceptSelector.close();
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            msc.register(acceptSelector, 0, null);
            fail("Should throw IllegalSelectorException");
        } catch (IllegalSelectorException e) {
        }
        acceptSelector = SelectorProvider.provider().openSelector();
        msc.configureBlocking(true);
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        acceptSelector.close();
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        Object argObj = new Object();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        try {
            sc.register(null, SelectionKey.OP_READ, argObj);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        msc.close();
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        SelectorProvider prov1 = MockSelectorProvider.provider();
        SelectorProvider prov2 = MockSelectorProvider.provider();
        Selector sel = prov2.openSelector();
        sc = prov1.openSocketChannel();
        sc.configureBlocking(false);
        try {
            sc.register(sel, SelectionKey.OP_READ, null);
        } catch (IllegalSelectorException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keyFor",
        args = {java.nio.channels.Selector.class}
    )
    public void test_keyfor_LSelector() throws Exception {
        SocketChannel sc = SocketChannel.open();
        Object argObj = new Object();
        sc.configureBlocking(false);
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        Selector acceptSelectorOther = SelectorProvider.provider()
                .openSelector();
        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, argObj);
        assertEquals(sc.keyFor(acceptSelector), acceptKey);
        SelectionKey acceptKeyObjNull = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        assertSame(sc.keyFor(acceptSelector), acceptKeyObjNull);
        assertSame(acceptKeyObjNull, acceptKey);
        SelectionKey acceptKeyOther = sc.register(acceptSelectorOther,
                SelectionKey.OP_READ, null);
        assertSame(sc.keyFor(acceptSelectorOther), acceptKeyOther);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "configureBlocking",
        args = {boolean.class}
    )
    public void test_configureBlocking_Z_IllegalBlockingMode() throws Exception {
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        assertEquals(sc.keyFor(acceptSelector), acceptKey);
        SelectableChannel getChannel = sc.configureBlocking(false);
        assertEquals(getChannel, sc);
        try {
            sc.configureBlocking(true);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "configureBlocking",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implConfigureBlocking",
            args = {boolean.class}
        )
    })   
    public void test_configureBlocking_Z() throws Exception {
        testChannel = new MockSelectableChannel(SelectorProvider
                .provider());
        testChannel.configureBlocking(true);
        assertFalse(testChannel.implConfigureBlockingCalled);
        testChannel.configureBlocking(false);
        assertTrue(testChannel.implConfigureBlockingCalled);
        AbstractSelectableChannel channel = 
                SelectorProvider.provider().openDatagramChannel();
        channel.configureBlocking(false);
        channel.register(SelectorProvider.provider().openSelector(),
                SelectionKey.OP_READ);
        try {
            channel.configureBlocking(true);
            fail("Should have thrown IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        testChannel.close();
        try {
            testChannel.configureBlocking(false);
            fail("Should have thrown ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    private class MockSelectableChannel extends AbstractSelectableChannel {
        private boolean isImplCloseSelectableChannelCalled = false;
        private int implCloseSelectableChannelCount = 0;
        private boolean implConfigureBlockingCalled = false;
        public MockSelectableChannel(SelectorProvider arg0) {
            super(arg0);
        }
        protected void implCloseSelectableChannel() throws IOException {
            isImplCloseSelectableChannelCalled = true;
            ++implCloseSelectableChannelCount;
        }
        protected void implConfigureBlocking(boolean arg0) throws IOException {
            implConfigureBlockingCalled = true;
        }
        public int validOps() {
            return SelectionKey.OP_ACCEPT;
        }
    }
}