public class MessengerTest extends AndroidTestCase {
    private Messenger mServiceMessenger;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (MessengerTest.this) {
                mServiceMessenger = new Messenger(service);
                MessengerTest.this.notifyAll();
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
        }
    };
    private class TestThread extends TestHandlerThread {
        private Handler mTestHandler;
        private Messenger mTestMessenger;
        public void go() {
            synchronized (MessengerTest.this) {
                mTestHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        TestThread.this.handleMessage(msg);
                    }
                };
                mTestMessenger = new Messenger(mTestHandler);
                TestThread.this.executeTest();
            }
        }
        public void executeTest() {
            Message msg = Message.obtain();
            msg.arg1 = 100;
            msg.arg2 = 1000;
            msg.replyTo = mTestMessenger;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
            }
        }
        public void handleMessage(Message msg) {
            if (msg.arg1 != 100) {
                failure(new RuntimeException(
                        "Message.arg1 is not 100: " + msg.arg1));
                return;
            }
            if (msg.arg2 != 1000) {
                failure(new RuntimeException(
                        "Message.arg2 is not 1000: " + msg.arg2));
                return;
            }
            if (!mTestMessenger.equals(msg.replyTo)) {
                failure(new RuntimeException(
                        "Message.replyTo is not me: " + msg.replyTo));
                return;
            }
            success();
        }
    };
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getContext().bindService(new Intent(mContext, MessengerService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        synchronized (this) {
            while (mServiceMessenger == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().unbindService(mConnection);
    }
    @MediumTest
    public void testSend() {
        (new TestThread()).doTest(1000);
    }
}
