@TestTargetClass(SocketImplFactory.class)
public class SocketImplFactoryTest extends TestCase {
    SocketImplFactory oldFactory = null;
    Field factoryField = null;
    boolean isTestable = false;
    boolean iSocketImplCalled = false;
    boolean isCreateSocketImpl = false;    
    @TestTargets ({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "createSocketImpl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies positive case, and SocketException.",
            clazz = Socket.class,
            method = "setSocketImplFactory",
            args = {java.net.SocketImplFactory.class}
        )        
    })
    public void test_createSocketImpl() throws IOException {
        MockSocketImplFactory factory = new MockSocketImplFactory();
        if(isTestable) {
            assertFalse(isCreateSocketImpl);
            Socket.setSocketImplFactory(factory);
            try {
                Socket ds = new Socket();
                assertTrue(isCreateSocketImpl);
                assertTrue(iSocketImplCalled);
            } catch (Exception e) {
                fail("Exception during test : " + e.getMessage());
            }
            try {
                Socket.setSocketImplFactory(factory);
                fail("SocketException was not thrown.");                
            } catch(SocketException se) {
            }
            try {
                Socket.setSocketImplFactory(null);
                fail("SocketException was not thrown.");                
            } catch(SocketException se) {
            }
        } else {
            SocketImpl si = factory.createSocketImpl();
            try {
               assertNull(si.getOption(0));
            } catch (SocketException e) {
                fail("SocketException was thrown.");
            }
        }
    }
    public void setUp() {
        Field [] fields = Socket.class.getDeclaredFields();
        int counter = 0;
        for (Field field : fields) {
            if (SocketImplFactory.class.equals(field.getType())) {
                counter++;
                factoryField = field;
            }
        } 
        if(counter == 1) {
            isTestable = true;
            factoryField.setAccessible(true);
            try {
                oldFactory = (SocketImplFactory) factoryField.get(null);
            } catch (IllegalArgumentException e) {
                fail("IllegalArgumentException was thrown during setUp: " 
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                fail("IllegalAccessException was thrown during setUp: "
                        + e.getMessage());
            }        
        }        
    }
    public void tearDown() {
        if(isTestable) {
            try {
                factoryField.set(null, oldFactory);
            } catch (IllegalArgumentException e) {
                fail("IllegalArgumentException was thrown during tearDown: " 
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                fail("IllegalAccessException was thrown during tearDown: "
                        + e.getMessage());
            }
        }        
    }
    class MockSocketImplFactory implements SocketImplFactory {
        public SocketImpl createSocketImpl() {
            isCreateSocketImpl = true;
            return new MockSocketImpl();
        }
    }
    class MockSocketImpl extends SocketImpl {
        public MockSocketImpl() {
            super();
            iSocketImplCalled = true;
        }
        @Override
        protected void accept(SocketImpl arg0) throws IOException {
        }
        @Override
        protected int available() throws IOException {
            return 0;
        }
        @Override
        protected void bind(InetAddress arg0, int arg1) throws IOException {
        }
        @Override
        protected void close() throws IOException {
        }
        @Override
        protected void connect(String arg0, int arg1) throws IOException {
        }
        @Override
        protected void connect(InetAddress arg0, int arg1) throws IOException {
        }
        @Override
        protected void connect(SocketAddress arg0, int arg1) throws IOException {
        }
        @Override
        protected void create(boolean arg0) throws IOException {
        }
        @Override
        protected InputStream getInputStream() throws IOException {
            return null;
        }
        @Override
        protected OutputStream getOutputStream() throws IOException {
            return null;
        }
        @Override
        protected void listen(int arg0) throws IOException {
        }
        @Override
        protected void sendUrgentData(int arg0) throws IOException {
        }
        public Object getOption(int arg0) throws SocketException {
            return null;
        }
        public void setOption(int arg0, Object arg1) throws SocketException {
        }
    }
}
