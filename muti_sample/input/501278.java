public class KeyUtils {
    public static void tapMenuKey(ActivityInstrumentationTestCase test) {
        final Instrumentation inst = test.getInstrumentation();
        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
    }
    public static void chordMenuKey(ActivityInstrumentationTestCase test, char shortcutKey) {
        final Instrumentation inst = test.getInstrumentation();
        final KeyEvent pushMenuKey = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
        final KeyCharacterMap keyCharMap = KeyCharacterMap.load(pushMenuKey.getDeviceId());
        final KeyEvent shortcutKeyEvent = keyCharMap.getEvents(new char[] { shortcutKey })[0];
        final int shortcutKeyCode = shortcutKeyEvent.getKeyCode();
        inst.sendKeySync(pushMenuKey);
        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, shortcutKeyCode));
        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, shortcutKeyCode));
        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
    }
    public static void longClick(ActivityInstrumentationTestCase test) {
        final Instrumentation inst = test.getInstrumentation();
        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER));
        try {
            Thread.sleep((long)(ViewConfiguration.getLongPressTimeout() * 1.5f));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER));
    }
}
