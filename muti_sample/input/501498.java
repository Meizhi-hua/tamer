@TestTargetClass(NumberKeyListener.class)
public class NumberKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private MockNumberKeyListener mNumberKeyListener;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public NumberKeyListenerTest(){
        super("com.android.cts.stub", KeyListenerStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mTextView = (TextView) mActivity.findViewById(R.id.keylistener_textview);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "paramters dest, dstart, dend are never read in the function",
        method = "filter",
        args = {CharSequence.class, int.class, int.class, Spanned.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add NPE description in javadoc.")
    public void testFilter() {
        mNumberKeyListener = new MockNumberKeyListener();
        String source = "Android test";
        SpannableString dest = new SpannableString("012345");
        assertEquals("", mNumberKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length()).toString());
        source = "12345";
        dest = new SpannableString("012345");
        assertNull(mNumberKeyListener.filter(source, 0, source.length(), dest, 0, dest.length()));
        source = "";
        dest = new SpannableString("012345");
        assertNull(mNumberKeyListener.filter(source, 0, source.length(), dest, 0, dest.length()));
        source = "12345 Android";
        dest = new SpannableString("012345 Android-test");
        assertEquals("12345", mNumberKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length()).toString());
        Object what = new Object();
        Spannable spannableSource = new SpannableString("12345 Android");
        spannableSource.setSpan(what, 0, spannableSource.length(), Spanned.SPAN_POINT_POINT);
        Spanned filtered = (Spanned) mNumberKeyListener.filter(spannableSource,
                0, spannableSource.length(), dest, 0, dest.length());
        assertEquals("12345", filtered.toString());
        assertEquals(Spanned.SPAN_POINT_POINT, filtered.getSpanFlags(what));
        assertEquals(0, filtered.getSpanStart(what));
        assertEquals("12345".length(), filtered.getSpanEnd(what));
        try {
            mNumberKeyListener.filter(null, 0, 1, dest, 0, dest.length());
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "lookup",
        args = {android.view.KeyEvent.class, android.text.Spannable.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testLookup() {
        mNumberKeyListener = new MockNumberKeyListener();
        KeyEvent event1 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0);
        SpannableString str = new SpannableString("012345");
        assertEquals('0', mNumberKeyListener.lookup(event1, str));
        KeyEvent event2 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A);
        str = new SpannableString("ABCD");
        assertEquals('\0', mNumberKeyListener.lookup(event2, str));
        try {
            mNumberKeyListener.lookup(null, str);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ok",
        args = {char[].class, char.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testOk() {
        mNumberKeyListener = new MockNumberKeyListener();
        assertTrue(mNumberKeyListener.callOk(mNumberKeyListener.getAcceptedChars(), '3'));
        assertFalse(mNumberKeyListener.callOk(mNumberKeyListener.getAcceptedChars(), 'e'));
        try {
            mNumberKeyListener.callOk(null, 'm');
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey() {
        final CharSequence text = "123456";
        final MockNumberKeyListener numberKeyListener = new MockNumberKeyListener();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(text, BufferType.EDITABLE);
                mTextView.setKeyListener(numberKeyListener);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("123456", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_0);
        assertEquals("0123456", mTextView.getText().toString());
        int keyCode = TextMethodUtils.getUnacceptedKeyCode(MockNumberKeyListener.CHARACTERS);
        if (-1 != keyCode) {
            sendKeys(keyCode);
            assertEquals("0123456", mTextView.getText().toString());
        }
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_0);
        assertEquals("0123456", mTextView.getText().toString());
    }
    private static class MockNumberKeyListener extends NumberKeyListener {
        static final char[] CHARACTERS = new char[] {'0', '1', '2',
                '3', '4', '5', '6', '7', '8', '9'};
        @Override
        protected char[] getAcceptedChars() {
            return CHARACTERS;
        }
        @Override
        protected int lookup(KeyEvent event, Spannable content) {
            return super.lookup(event, content);
        }
        public boolean callOk(char[] accept, char c) {
            return NumberKeyListener.ok(accept, c);
        }
        public int getInputType() {
            return 0;
        }
    }
}
