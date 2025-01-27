@TestTargetClass(PasswordTransformationMethod.class)
public class PasswordTransformationMethodTest extends
        ActivityInstrumentationTestCase2<StubActivity> {
    private static final int EDIT_TXT_ID = 1;
    private static final String TEST_CONTENT = "test content";
    private static final String TEST_CONTENT_TRANSFORMED =
        "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
    private int mPasswordPrefBackUp;
    private boolean isPasswordPrefSaved;
    private StubActivity mActicity;
    private MockPasswordTransformationMethod mMethod;
    private EditText mEditText;
    private CharSequence mTransformedText;
    public PasswordTransformationMethodTest() {
        super("com.android.cts.stub", StubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActicity = getActivity();
        mMethod = new MockPasswordTransformationMethod();
        try {
            runTestOnUiThread(new Runnable() {
                public void run() {
                    EditText editText = new EditText(mActicity);
                    editText.setId(EDIT_TXT_ID);
                    editText.setTransformationMethod(mMethod);
                    Button button = new Button(mActicity);
                    LinearLayout layout = new LinearLayout(mActicity);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.addView(editText, new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT));
                    layout.addView(button, new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT));
                    mActicity.setContentView(layout);
                    editText.requestFocus();
                }
            });
        } catch (Throwable e) {
            fail("Exception thrown is UI thread:" + e.getMessage());
        }
        getInstrumentation().waitForIdleSync();
        mEditText = (EditText) getActivity().findViewById(EDIT_TXT_ID);
        assertTrue(mEditText.isFocused());
        savePasswordPref();
        switchShowPassword(true);
    }
    @Override
    protected void tearDown() throws Exception {
        resumePasswordPref();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PasswordTransformationMethod",
        args = {}
    )
    public void testConstructor() {
        new PasswordTransformationMethod();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "beforeTextChanged",
            args = {CharSequence.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTextChanged",
            args = {CharSequence.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "afterTextChanged",
            args = {Editable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTransformation",
            args = {CharSequence.class, View.class}
        )
    })
    public void testTextChangedCallBacks() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mTransformedText = mMethod.getTransformation(mEditText.getText(), mEditText);
            }
        });
        mMethod.reset();
        sendKeys("H E 2*L O");
        assertTrue(mMethod.hasCalledBeforeTextChanged());
        assertTrue(mMethod.hasCalledOnTextChanged());
        assertTrue(mMethod.hasCalledAfterTextChanged());
        mMethod.reset();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mEditText.append(" ");
            }
        });
        assertEquals("\u2022\u2022\u2022\u2022\u2022 ", mTransformedText.toString());
        assertTrue(mMethod.hasCalledBeforeTextChanged());
        assertTrue(mMethod.hasCalledOnTextChanged());
        assertTrue(mMethod.hasCalledAfterTextChanged());
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return mTransformedText.toString()
                        .equals("\u2022\u2022\u2022\u2022\u2022\u2022");
            }
        }.run();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTransformation",
        args = {CharSequence.class, View.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should check whether the source passed in is null,"
            + "if null source is passed in, exception will be thrown when toString() is called")
    public void testGetTransformation() {
        PasswordTransformationMethod method = new PasswordTransformationMethod();
        assertEquals(TEST_CONTENT_TRANSFORMED,
                method.getTransformation(TEST_CONTENT, null).toString());
        CharSequence transformed = method.getTransformation(null, mEditText);
        assertNotNull(transformed);
        try {
            transformed.toString();
            fail("Should throw NullPointerException if the source is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link PasswordTransformationMethod#getInstance()}.",
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        PasswordTransformationMethod method0 = PasswordTransformationMethod.getInstance();
        assertNotNull(method0);
        PasswordTransformationMethod method1 = PasswordTransformationMethod.getInstance();
        assertNotNull(method1);
        assertSame(method0, method1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onFocusChanged",
        args = {View.class, CharSequence.class, boolean.class, int.class, Rect.class}
    )
    public void testOnFocusChanged() {
        mMethod.reset();
        assertTrue(mEditText.isFocused());
        sendKeys("DPAD_DOWN");
        assertFalse(mEditText.isFocused());
        assertTrue(mMethod.hasCalledOnFocusChanged());
        mMethod.reset();
        assertFalse(mEditText.isFocused());
        sendKeys("DPAD_UP");
        assertTrue(mEditText.isFocused());
        assertTrue(mMethod.hasCalledOnFocusChanged());
    }
    private void savePasswordPref() {
        try {
            mPasswordPrefBackUp = System.getInt(mActicity.getContentResolver(),
                    System.TEXT_SHOW_PASSWORD);
            isPasswordPrefSaved = true;
        } catch (SettingNotFoundException e) {
            isPasswordPrefSaved = false;
        }
    }
    private void resumePasswordPref() {
        if (isPasswordPrefSaved) {
            System.putInt(mActicity.getContentResolver(), System.TEXT_SHOW_PASSWORD,
                    mPasswordPrefBackUp);
        }
    }
    private void switchShowPassword(boolean on) {
        System.putInt(mActicity.getContentResolver(), System.TEXT_SHOW_PASSWORD,
                on ? 1 : 0);
    }
    private static class MockPasswordTransformationMethod extends PasswordTransformationMethod {
        private boolean mHasCalledBeforeTextChanged;
        private boolean mHasCalledOnTextChanged;
        private boolean mHasCalledAfterTextChanged;
        private boolean mHasCalledOnFocusChanged;
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            mHasCalledAfterTextChanged = true;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            super.beforeTextChanged(s, start, count, after);
            mHasCalledBeforeTextChanged = true;
        }
        @Override
        public void onFocusChanged(View view, CharSequence sourceText, boolean focused,
                int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(view, sourceText, focused, direction, previouslyFocusedRect);
            mHasCalledOnFocusChanged = true;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
            mHasCalledOnTextChanged = true;
        }
        public boolean hasCalledBeforeTextChanged() {
            return mHasCalledBeforeTextChanged;
        }
        public boolean hasCalledOnTextChanged() {
            return mHasCalledOnTextChanged;
        }
        public boolean hasCalledAfterTextChanged() {
            return mHasCalledAfterTextChanged;
        }
        public boolean hasCalledOnFocusChanged() {
            return mHasCalledOnFocusChanged;
        }
        public void reset() {
            mHasCalledBeforeTextChanged = false;
            mHasCalledOnTextChanged = false;
            mHasCalledAfterTextChanged = false;
            mHasCalledOnFocusChanged = false;
        }
    }
}
