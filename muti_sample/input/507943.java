@TestTargetClass(Binder.class)
public class BinderTest extends ActivityTestsBase {
    private static final String DESCRIPTOR_GOOGLE = "google";
    private static final String DESCRIPTOR_ANDROID = "android";
    private static final int STATE_START_1 = 0;
    private static final int STATE_START_2 = 1;
    private static final int STATE_UNBIND = 2;
    private static final int STATE_DESTROY = 3;
    private static final int STATE_REBIND = 4;
    private static final int STATE_UNBIND_ONLY = 5;
    private static final int DELAY_MSEC = 5000;
    private MockBinder mBinder;
    private Binder mStartReceiver;
    private int mStartState;
    private Intent mService;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mService = new Intent(LocalService.SERVICE_LOCAL);
        mBinder = new MockBinder();
        mStartReceiver = new Binder() {
            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                             throws RemoteException {
                switch (code) {
                    case LocalService.STARTED_CODE:
                        data.enforceInterface(LocalService.SERVICE_LOCAL);
                        int count = data.readInt();
                        switch (mStartState) {
                            case STATE_START_1:
                                if (count == 1) {
                                    finishGood();
                                } else {
                                    finishBad("onStart() again on an object when it "
                                            + "should have been the first time");
                                }
                                break;
                            case STATE_START_2:
                                if (count == 2) {
                                    finishGood();
                                } else {
                                    finishBad("onStart() the first time on an object when it "
                                            + "should have been the second time");
                                }
                                break;
                            default:
                                finishBad("onStart() was called when not expected (state="
                                        + mStartState + ")");
                        }
                        return true;
                    case LocalService.DESTROYED_CODE:
                        data.enforceInterface(LocalService.SERVICE_LOCAL);
                        if (mStartState == STATE_DESTROY) {
                            finishGood();
                        } else {
                            finishBad("onDestroy() was called when not expected (state="
                                    + mStartState + ")");
                        }
                        return true;
                    case LocalService.UNBIND_CODE:
                        data.enforceInterface(LocalService.SERVICE_LOCAL);
                        switch (mStartState) {
                            case STATE_UNBIND:
                                mStartState = STATE_DESTROY;
                                break;
                            case STATE_UNBIND_ONLY:
                                finishGood();
                                break;
                            default:
                                finishBad("onUnbind() was called when not expected (state="
                                        + mStartState + ")");
                        }
                        return true;
                    case LocalService.REBIND_CODE:
                        data.enforceInterface(LocalService.SERVICE_LOCAL);
                        if (mStartState == STATE_REBIND) {
                            finishGood();
                        } else {
                            finishBad("onRebind() was called when not expected (state="
                                    + mStartState + ")");
                        }
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
        };
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mContext.stopService(mService);
    }
    public class MockServiceConnection implements ServiceConnection {
        private final boolean mIsDisconnect;
        private final boolean mSetReporter;
        private boolean mIsMonitorEnable;
        private int mCount;
        public MockServiceConnection(final boolean isDisconnect, final boolean setReporter) {
            mIsDisconnect = isDisconnect;
            mSetReporter = setReporter;
            mIsMonitorEnable = !setReporter;
        }
        void setMonitor(boolean v) {
            mIsMonitorEnable = v;
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mSetReporter) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(LocalService.SERVICE_LOCAL);
                data.writeStrongBinder(mStartReceiver);
                try {
                    service.transact(LocalService.SET_REPORTER_CODE, data, null, 0);
                } catch (RemoteException e) {
                    finishBad("DeadObjectException when sending reporting object");
                }
                data.recycle();
            }
            if (mIsMonitorEnable) {
                mCount++;
                if (mStartState == STATE_START_1) {
                    if (mCount == 1) {
                        finishGood();
                    } else {
                        finishBad("onServiceConnected() again on an object when it "
                                + "should have been the first time");
                    }
                } else if (mStartState == STATE_START_2) {
                    if (mCount == 2) {
                        finishGood();
                    } else {
                        finishBad("onServiceConnected() the first time on an object "
                                + "when it should have been the second time");
                    }
                } else {
                    finishBad("onServiceConnected() called unexpectedly");
                }
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            if (mIsMonitorEnable) {
                if (mStartState == STATE_DESTROY) {
                    if (mIsDisconnect) {
                        finishGood();
                    } else {
                        finishBad("onServiceDisconnected() when it shouldn't have been");
                    }
                } else {
                    finishBad("onServiceDisconnected() called unexpectedly");
                }
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test transact",
            method = "transact",
            args = {int.class, android.os.Parcel.class, android.os.Parcel.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test onTransact",
            method = "onTransact",
            args = {int.class, android.os.Parcel.class, android.os.Parcel.class, int.class}
        )
    })
    public void testTransact() {
        MockServiceConnection conn1 = new MockServiceConnection(true, false);
        MockServiceConnection conn2 = new MockServiceConnection(false, false);
        boolean success = false;
        try {
            mStartState = STATE_START_1;
            getContext().bindService(mService, conn1, 0);
            getContext().startService(mService);
            waitForResultOrThrow(DELAY_MSEC, "existing connection to receive service");
            getContext().bindService(mService, conn2, 0);
            waitForResultOrThrow(DELAY_MSEC, "new connection to receive service");
            getContext().unbindService(conn2);
            success = true;
        } finally {
            if (!success) {
                try {
                getContext().stopService(mService);
                getContext().unbindService(conn1);
                getContext().unbindService(conn2);
                } catch (SecurityException e) {
                    fail(e.getMessage());
                }
            }
        }
        mStartState = STATE_DESTROY;
        getContext().stopService(mService);
        waitForResultOrThrow(DELAY_MSEC, "the existing connection to lose service");
        getContext().unbindService(conn1);
        conn1 = new MockServiceConnection(true, true);
        success = false;
        try {
            conn1.setMonitor(true);
            mStartState = STATE_START_1;
            getContext().bindService(mService, conn1, 0);
            getContext().startService(mService);
            waitForResultOrThrow(DELAY_MSEC, "the existing connection to receive service");
            success = true;
        } finally {
            if (!success) {
                try {
                    getContext().stopService(mService);
                    getContext().unbindService(conn1);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        }
        conn1.setMonitor(false);
        mStartState = STATE_UNBIND;
        getContext().stopService(mService);
        waitForResultOrThrow(DELAY_MSEC, "the existing connection to lose service");
        getContext().unbindService(conn1);
        conn1 = new MockServiceConnection(true, true);
        success = false;
        try {
            conn1.setMonitor(true);
            mStartState = STATE_START_1;
            getContext().bindService(mService, conn1, 0);
            getContext().startService(mService);
            waitForResultOrThrow(DELAY_MSEC, "existing connection to receive service");
            success = true;
        } finally {
            if (!success) {
                try {
                    getContext().stopService(mService);
                    getContext().unbindService(conn1);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        }
        conn1.setMonitor(false);
        mStartState = STATE_UNBIND_ONLY;
        getContext().unbindService(conn1);
        waitForResultOrThrow(DELAY_MSEC, "existing connection to unbind service");
        mStartState = STATE_REBIND;
        getContext().bindService(mService, conn1, 0);
        waitForResultOrThrow(DELAY_MSEC, "existing connection to rebind service");
        mStartState = STATE_UNBIND;
        getContext().stopService(mService);
        waitForResultOrThrow(DELAY_MSEC, "existing connection to lose service");
        getContext().unbindService(conn1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingPid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingUid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dump",
            args = {java.io.FileDescriptor.class, java.io.PrintWriter.class,
                    java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dump",
            args = {java.io.FileDescriptor.class, java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBinderAlive",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "linkToDeath",
            args = {android.os.IBinder.DeathRecipient.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unlinkToDeath",
            args = {android.os.IBinder.DeathRecipient.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pingBinder",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test constructor",
            method = "Binder",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        )
    })
    public void testSimpleMethods() {
        new Binder();
        assertEquals(Process.myPid(), Binder.getCallingPid());
        assertEquals(Process.myUid(), Binder.getCallingUid());
        final String[] dumpArgs = new String[]{"one", "two", "three"};
        mBinder.dump(new FileDescriptor(),
                new PrintWriter(new ByteArrayOutputStream()),
                dumpArgs);
        mBinder.dump(new FileDescriptor(), dumpArgs);
        assertTrue(mBinder.isBinderAlive());
        mBinder.linkToDeath(new MockDeathRecipient(), 0);
        assertTrue(mBinder.unlinkToDeath(new MockDeathRecipient(), 0));
        assertTrue(mBinder.pingBinder());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "flushPendingCommands",
        args = {}
    )
    @ToBeFixed(bug = "1393825", explanation = "it is native function "
        + "and we cannot get the state change")
    public void testFlushPendingCommands() {
        Binder.flushPendingCommands();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "joinThreadPool won't be return until the current process is exiting."
            + "so not suitable to test it in unit test",
        method = "joinThreadPool",
        args = {}
    )
    @ToBeFixed(bug = "1709683", explanation = "it always throws UnsatisfiedLinkError "
        + "when calling method: joinThreadPool")
    public void testJoinThreadPool() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearCallingIdentity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restoreCallingIdentity",
            args = {long.class}
        )
    })
    public void testClearCallingIdentity() {
        long token = Binder.clearCallingIdentity();
        assertTrue(token > 0);
        Binder.restoreCallingIdentity(token);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "attachInterface",
            args = {android.os.IInterface.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInterfaceDescriptor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "queryLocalInterface",
            args = {java.lang.String.class}
        )
    })
    public void testInterfaceRelatedMethods() {
        assertNull(mBinder.getInterfaceDescriptor());
        mBinder.attachInterface(new MockIInterface(), DESCRIPTOR_GOOGLE);
        assertEquals(DESCRIPTOR_GOOGLE, mBinder.getInterfaceDescriptor());
        mBinder.attachInterface(new MockIInterface(), DESCRIPTOR_ANDROID);
        assertNull(mBinder.queryLocalInterface(DESCRIPTOR_GOOGLE));
        mBinder.attachInterface(new MockIInterface(), DESCRIPTOR_GOOGLE);
        assertNotNull(mBinder.queryLocalInterface(DESCRIPTOR_GOOGLE));
    }
    private static class MockDeathRecipient implements IBinder.DeathRecipient {
         public void binderDied() {
         }
    }
    private static class MockIInterface implements IInterface {
        public IBinder asBinder() {
            return new Binder();
        }
    }
    private static class MockBinder extends Binder {
        @Override
        public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
            super.dump(fd, fout, args);
        }
    }
}
