public class WindowManagerPermissionTests extends TestCase {
    IWindowManager mWm;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWm = IWindowManager.Stub.asInterface(
                ServiceManager.getService("window"));
    }
    @SmallTest
	public void testMANAGE_APP_TOKENS() {
        try {
            mWm.pauseKeyDispatching(null);
            fail("IWindowManager.pauseKeyDispatching did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.resumeKeyDispatching(null);
            fail("IWindowManager.resumeKeyDispatching did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setEventDispatching(true);
            fail("IWindowManager.setEventDispatching did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.addWindowToken(null, 0);
            fail("IWindowManager.addWindowToken did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.removeWindowToken(null);
            fail("IWindowManager.removeWindowToken did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.addAppToken(0, null, 0, 0, false);
            fail("IWindowManager.addAppToken did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setAppGroupId(null, 0);
            fail("IWindowManager.setAppGroupId did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.updateOrientationFromAppTokens(new Configuration(), null);
            fail("IWindowManager.updateOrientationFromAppTokens did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setAppOrientation(null, 0);
            mWm.addWindowToken(null, 0);
            fail("IWindowManager.setAppOrientation did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setFocusedApp(null, false);
            fail("IWindowManager.setFocusedApp did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.prepareAppTransition(0);
            fail("IWindowManager.prepareAppTransition did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.executeAppTransition();
            fail("IWindowManager.executeAppTransition did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setAppStartingWindow(null, "foo", 0, null, 0, 0, null, false);
            fail("IWindowManager.setAppStartingWindow did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setAppWillBeHidden(null);
            fail("IWindowManager.setAppWillBeHidden did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setAppVisibility(null, false);
            fail("IWindowManager.setAppVisibility did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.startAppFreezingScreen(null, 0);
            fail("IWindowManager.startAppFreezingScreen did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.stopAppFreezingScreen(null, false);
            fail("IWindowManager.stopAppFreezingScreen did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.removeAppToken(null);
            fail("IWindowManager.removeAppToken did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.moveAppToken(0, null);
            fail("IWindowManager.moveAppToken did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.moveAppTokensToTop(null);
            fail("IWindowManager.moveAppTokensToTop did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.moveAppTokensToBottom(null);
            fail("IWindowManager.moveAppTokensToBottom did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
	}    
    @SmallTest
    public void testINJECT_EVENTS() {
        try {
            mWm.injectKeyEvent(new KeyEvent(0, 0), false);
            fail("IWindowManager.injectKeyEvent did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.injectPointerEvent(MotionEvent.obtain(0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0), false);
            fail("IWindowManager.injectPointerEvent did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.injectTrackballEvent(MotionEvent.obtain(0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0), false);
            fail("IWindowManager.injectTrackballEvent did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testDISABLE_KEYGUARD() {
        Binder token = new Binder();
        try {
            mWm.disableKeyguard(token, "foo");
            fail("IWindowManager.disableKeyguard did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.reenableKeyguard(token);
            fail("IWindowManager.reenableKeyguard did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.exitKeyguardSecurely(null);
            fail("IWindowManager.exitKeyguardSecurely did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSET_ANIMATION_SCALE() {
        try {
            mWm.setAnimationScale(0, 1);
            fail("IWindowManager.setAnimationScale did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.setAnimationScales(new float[1]);
            fail("IWindowManager.setAnimationScales did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testREAD_INPUT_STATE() {
        try {
            mWm.getSwitchState(0);
            fail("IWindowManager.getSwitchState did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.getSwitchStateForDevice(0, 0);
            fail("IWindowManager.getSwitchStateForDevice did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.getScancodeState(0);
            fail("IWindowManager.getScancodeState did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.getScancodeStateForDevice(0, 0);
            fail("IWindowManager.getScancodeStateForDevice did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.getKeycodeState(0);
            fail("IWindowManager.getKeycodeState did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
        try {
            mWm.getKeycodeStateForDevice(0, 0);
            fail("IWindowManager.getKeycodeStateForDevice did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
    @SmallTest
    public void testSET_ORIENTATION() {
        try {
            mWm.setRotation(0, true, 0);
            mWm.getSwitchState(0);
            fail("IWindowManager.setRotation did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        } catch (RemoteException e) {
            fail("Unexpected remote exception");
        }
    }
}
