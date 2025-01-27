@TestTargetClass(AutoCompleteTextView.class)
public class AutoCompleteTextViewTest extends
        ActivityInstrumentationTestCase2<AutoCompleteStubActivity> {
    public AutoCompleteTextViewTest() {
        super("com.android.cts.stub", AutoCompleteStubActivity.class);
    }
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private AutoCompleteTextView mAutoCompleteTextView;
    ArrayAdapter<String> mAdapter;
    private final String[] WORDS = new String[] { "testOne", "testTwo", "testThree", "testFour" };
    boolean isOnFilterComplete = false;
    final String STRING_TEST = "To be tested";
    final String STRING_VALIDATED = "String Validated";
    final String STRING_CHECK = "To be checked";
    final String STRING_APPEND = "and be appended";
    Validator mValidator = new Validator() {
        public CharSequence fixText(CharSequence invalidText) {
            return STRING_VALIDATED;
        }
        public boolean isValid(CharSequence text) {
            return false;
        }
    };
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mAutoCompleteTextView = (AutoCompleteTextView) mActivity
                .findViewById(R.id.autocompletetv_edit);
        mAdapter = new ArrayAdapter<String>(mActivity,
                android.R.layout.simple_dropdown_item_1line, WORDS);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AutoCompleteTextView",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AutoCompleteTextView",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AutoCompleteTextView",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        XmlPullParser parser;
        new AutoCompleteTextView(mActivity);
        parser = mActivity.getResources().getXml(R.layout.simple_dropdown_item_1line);
        AttributeSet attributeSet = Xml.asAttributeSet(parser);
        new AutoCompleteTextView(mActivity, attributeSet);
        new AutoCompleteTextView(mActivity, null);
        parser = mActivity.getResources().getXml(R.layout.framelayout_layout);
        attributeSet = Xml.asAttributeSet(parser);
        new AutoCompleteTextView(mActivity, attributeSet, 0);
        new AutoCompleteTextView(mActivity, null, 0);
        try {
            new AutoCompleteTextView(null, attributeSet, 0);
            fail("should throw NullPointerException");
        } catch (Exception e) {
        }
        new AutoCompleteTextView(mActivity, attributeSet, -1);
        new AutoCompleteTextView(mActivity, null, -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "enoughToFilter",
        args = {}
    )
    public void testEnoughToFilter() throws Throwable {
        mAutoCompleteTextView.setThreshold(3);
        assertEquals(3, mAutoCompleteTextView.getThreshold());
        runTestOnUiThread(new Runnable() {
            public void run() {
                String testString = "TryToTest";
                mAutoCompleteTextView.setText(testString);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(mAutoCompleteTextView.enoughToFilter());
        runTestOnUiThread(new Runnable() {
            public void run() {
                String testString = "No";
                mAutoCompleteTextView.setText(testString);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertFalse(mAutoCompleteTextView.enoughToFilter());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAdapter",
            args = {android.widget.ListAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAdapter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFilter",
            args = {}
        )
    })
    public void testAccessAdapter() {
        MockAutoCompleteTextView autoCompleteTextView = new MockAutoCompleteTextView(mActivity);
        autoCompleteTextView.setThreshold(4);
        ArrayAdapter<String> adapter = null;
        autoCompleteTextView.setAdapter(adapter);
        assertNull(autoCompleteTextView.getAdapter());
        assertNull(autoCompleteTextView.getFilter());
        Filter filter = mAdapter.getFilter();
        assertNotNull(filter);
        autoCompleteTextView.setAdapter(mAdapter);
        assertSame(mAdapter, autoCompleteTextView.getAdapter());
        assertSame(filter, autoCompleteTextView.getFilter());
        autoCompleteTextView.setAdapter(adapter);
        assertNull(autoCompleteTextView.getAdapter());
        assertNull(autoCompleteTextView.getFilter());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnItemClickListener",
            args = {android.widget.AdapterView.OnItemClickListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getItemClickListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOnItemClickListener",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    public void testAccessItemClickListener() {
        final MockOnItemClickListener testOnItemClickListener = new MockOnItemClickListener();
        mAutoCompleteTextView.setOnItemClickListener(null);
        assertNull(mAutoCompleteTextView.getItemClickListener());
        assertNull(mAutoCompleteTextView.getOnItemClickListener());
        assertNotNull(testOnItemClickListener);
        mAutoCompleteTextView.setOnItemClickListener(testOnItemClickListener);
        assertSame(testOnItemClickListener, mAutoCompleteTextView.getItemClickListener());
        assertSame(testOnItemClickListener, mAutoCompleteTextView.getOnItemClickListener());
        mAutoCompleteTextView.setOnItemClickListener(null);
        assertNull(mAutoCompleteTextView.getItemClickListener());
        assertNull(mAutoCompleteTextView.getOnItemClickListener());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnItemSelectedListener",
            args = {android.widget.AdapterView.OnItemSelectedListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getItemSelectedListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOnItemSelectedListener",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    public void testAccessItemSelectedListener() {
        MockOnItemSelectedListener testOnItemSelectedListener = new MockOnItemSelectedListener();
        mAutoCompleteTextView.setOnItemSelectedListener(null);
        assertNull(mAutoCompleteTextView.getItemSelectedListener());
        assertNull(mAutoCompleteTextView.getOnItemSelectedListener());
        assertNotNull(testOnItemSelectedListener);
        mAutoCompleteTextView.setOnItemSelectedListener(testOnItemSelectedListener);
        assertSame(testOnItemSelectedListener, mAutoCompleteTextView.getItemSelectedListener());
        assertSame(testOnItemSelectedListener, mAutoCompleteTextView.getOnItemSelectedListener());
        mAutoCompleteTextView.setOnItemSelectedListener(null);
        assertNull(mAutoCompleteTextView.getItemSelectedListener());
        assertNull(mAutoCompleteTextView.getOnItemSelectedListener());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "convertSelectionToString",
        args = {java.lang.Object.class}
    )
    public void testConvertSelectionToString() {
        MockAutoCompleteTextView autoCompleteTextView = new MockAutoCompleteTextView(mActivity);
        autoCompleteTextView.setThreshold(4);
        autoCompleteTextView.setAdapter(mAdapter);
        assertNotNull(autoCompleteTextView.getAdapter());
        assertEquals("", autoCompleteTextView.convertSelectionToString(null));
        assertEquals(STRING_TEST, autoCompleteTextView.convertSelectionToString(STRING_TEST));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onTextChanged",
        args = {java.lang.CharSequence.class, int.class, int.class, int.class}
    )
    public void testOnTextChanged() {
        MockAutoCompleteTextView autoCompleteTextView = new MockAutoCompleteTextView(mActivity);
        assertFalse(autoCompleteTextView.isOnTextChanged());
        assertEquals("", autoCompleteTextView.getLastChangeText());
        assertEquals("", autoCompleteTextView.getText().toString());
        assertEquals(0, autoCompleteTextView.getStart());
        assertEquals(0, autoCompleteTextView.getBefore());
        assertEquals(0, autoCompleteTextView.getAfter());
        autoCompleteTextView.setText(STRING_TEST);
        assertEquals(STRING_TEST, autoCompleteTextView.getText().toString());
        assertTrue(autoCompleteTextView.isOnTextChanged());
        assertEquals(STRING_TEST, autoCompleteTextView.getLastChangeText());
        assertEquals(0, autoCompleteTextView.getStart());
        assertEquals(0, autoCompleteTextView.getBefore());
        assertEquals(STRING_TEST.length(), autoCompleteTextView.getAfter());
        autoCompleteTextView.resetStatus();
        autoCompleteTextView.setText(STRING_CHECK);
        assertEquals(STRING_CHECK, autoCompleteTextView.getText().toString());
        assertEquals(STRING_CHECK, autoCompleteTextView.getLastChangeText());
        assertEquals(0, autoCompleteTextView.getStart());
        assertEquals(STRING_TEST.length(), autoCompleteTextView.getBefore());
        assertEquals(STRING_CHECK.length(), autoCompleteTextView.getAfter());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onFocusChanged",
            args = {boolean.class, int.class, android.graphics.Rect.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismissDropDown",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "showDropDown",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPopupShowing",
            args = {}
        )
    })
    @UiThreadTest
    public void testPopupWindow() throws XmlPullParserException, IOException {
        assertFalse(mAutoCompleteTextView.isPopupShowing());
        mAutoCompleteTextView.showDropDown();
        assertTrue(mAutoCompleteTextView.isPopupShowing());
        mAutoCompleteTextView.dismissDropDown();
        assertFalse(mAutoCompleteTextView.isPopupShowing());
        mAutoCompleteTextView.showDropDown();
        assertTrue(mAutoCompleteTextView.isPopupShowing());
        final MockValidator validator = new MockValidator();
        mAutoCompleteTextView.setValidator(validator);
        mAutoCompleteTextView.requestFocus();
        mAutoCompleteTextView.showDropDown();
        assertTrue(mAutoCompleteTextView.isPopupShowing());
        mAutoCompleteTextView.setText(STRING_TEST);
        assertEquals(STRING_TEST, mAutoCompleteTextView.getText().toString());
        mAutoCompleteTextView.clearFocus();
        assertFalse(mAutoCompleteTextView.isPopupShowing());
        assertEquals(STRING_VALIDATED, mAutoCompleteTextView.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "replaceText",
        args = {java.lang.CharSequence.class}
    )
    public void testReplaceText() {
        MockAutoCompleteTextView autoCompleteTextView = new MockAutoCompleteTextView(mActivity);
        assertEquals("", autoCompleteTextView.getText().toString());
        assertFalse(autoCompleteTextView.isOnTextChanged());
        autoCompleteTextView.replaceText("Text");
        assertEquals("Text", autoCompleteTextView.getText().toString());
        assertTrue(autoCompleteTextView.isOnTextChanged());
        autoCompleteTextView.resetStatus();
        assertFalse(autoCompleteTextView.isOnTextChanged());
        autoCompleteTextView.replaceText("Another");
        assertEquals("Another", autoCompleteTextView.getText().toString());
        assertTrue(autoCompleteTextView.isOnTextChanged());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setFrame",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testSetFrame() {
        MockAutoCompleteTextView autoCompleteTextView = new MockAutoCompleteTextView(mActivity);
        assertTrue(autoCompleteTextView.setFrame(0, 1, 2, 3));
        assertEquals(0, autoCompleteTextView.getLeft());
        assertEquals(1, autoCompleteTextView.getTop());
        assertEquals(2, autoCompleteTextView.getRight());
        assertEquals(3, autoCompleteTextView.getBottom());
        assertFalse(autoCompleteTextView.setFrame(0, 1, 2, 3));
        assertEquals(0, autoCompleteTextView.getLeft());
        assertEquals(1, autoCompleteTextView.getTop());
        assertEquals(2, autoCompleteTextView.getRight());
        assertEquals(3, autoCompleteTextView.getBottom());
        assertTrue(autoCompleteTextView.setFrame(2, 3, 4, 5));
        assertEquals(2, autoCompleteTextView.getLeft());
        assertEquals(3, autoCompleteTextView.getTop());
        assertEquals(4, autoCompleteTextView.getRight());
        assertEquals(5, autoCompleteTextView.getBottom());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getThreshold",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setThreshold",
            args = {int.class}
        )
    })
    public void testGetThreshold() {
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) mActivity
                .findViewById(R.id.autocompletetv_edit);
        assertNotNull(autoCompleteTextView);
        assertEquals(1, autoCompleteTextView.getThreshold());
        autoCompleteTextView.setThreshold(3);
        assertEquals(3, autoCompleteTextView.getThreshold());
        autoCompleteTextView.setThreshold(-5);
        assertEquals(1, autoCompleteTextView.getThreshold());
        autoCompleteTextView.setThreshold(0);
        assertEquals(1, autoCompleteTextView.getThreshold());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getValidator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setValidator",
            args = {android.widget.AutoCompleteTextView.Validator.class}
        )
    })
    public void testAccessValidater() {
        final MockValidator validator = new MockValidator();
        assertNull(mAutoCompleteTextView.getValidator());
        mAutoCompleteTextView.setValidator(validator);
        assertSame(validator, mAutoCompleteTextView.getValidator());
        mAutoCompleteTextView.setValidator(null);
        assertNull(mAutoCompleteTextView.getValidator());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onFilterComplete",
        args = {int.class}
    )
    public void testOnFilterComplete() throws Throwable {
        mAutoCompleteTextView.setThreshold(4);
        inflatePopup();
        assertTrue(mAutoCompleteTextView.isPopupShowing());
        String testString = "tes";
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setAdapter(mAdapter);
                mAutoCompleteTextView.setText("");
                mAutoCompleteTextView.requestFocus();
            }
        });
        mInstrumentation.sendStringSync(testString);
        assertFalse(mAutoCompleteTextView.isPopupShowing());
        inflatePopup();
        assertTrue(mAutoCompleteTextView.isPopupShowing());
        testString = "that";
        mInstrumentation.sendStringSync(testString);
        assertFalse(mAutoCompleteTextView.isPopupShowing());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setFocusable(true);
                mAutoCompleteTextView.requestFocus();
                mAutoCompleteTextView.setText("");
            }
        });
        mInstrumentation.sendStringSync("test");
        assertTrue(mAutoCompleteTextView.hasFocus());
        assertTrue(mAutoCompleteTextView.hasWindowFocus());
        Thread.sleep(200);
        assertTrue(mAutoCompleteTextView.isPopupShowing());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performFiltering",
            args = {java.lang.CharSequence.class, int.class}
        )
    })
    @ToBeFixed(bug = "", explanation = "mAutoCompleteTextView.isPopupShowing() should be false")
    public void testPerformFiltering() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setAdapter(mAdapter);
                mAutoCompleteTextView.setValidator(mValidator);
                mAutoCompleteTextView.setAdapter(mAdapter);
            }
        });
        inflatePopup();
        assertTrue(mAutoCompleteTextView.isPopupShowing());
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        assertFalse(mAutoCompleteTextView.isPopupShowing());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.dismissDropDown();
                mAutoCompleteTextView.setText(STRING_TEST);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(STRING_TEST, mAutoCompleteTextView.getText().toString());
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(STRING_VALIDATED, mAutoCompleteTextView.getText().toString());
        final MockAdapter<String> adapter = new MockAdapter<String>(mActivity,
                android.R.layout.simple_dropdown_item_1line, WORDS);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setAdapter(adapter);
                mAutoCompleteTextView.requestFocus();
                mAutoCompleteTextView.setText("");
            }
        });
        mInstrumentation.waitForIdleSync();
        MockFilter filter = (MockFilter) adapter.getFilter();
        assertNull(filter.getResult());
        mInstrumentation.sendStringSync(STRING_TEST);
        Thread.sleep(100);
        assertEquals(STRING_TEST, filter.getResult());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performCompletion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPerformingCompletion",
            args = {}
        )
    })
    public void testPerformCompletion() throws Throwable {
        final MockOnItemClickListener listener = new MockOnItemClickListener();
        assertFalse(mAutoCompleteTextView.isPerformingCompletion());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setOnItemClickListener(listener);
                mAutoCompleteTextView.setAdapter(mAdapter);
                mAutoCompleteTextView.requestFocus();
                mAutoCompleteTextView.showDropDown();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertFalse(mAutoCompleteTextView.isPerformingCompletion());
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        mInstrumentation.waitForIdleSync();
        assertTrue(listener.isOnItemClicked());
        assertEquals(WORDS[0], mAutoCompleteTextView.getText().toString());
        listener.clearItemClickedStatus();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.showDropDown();
            }
        });
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(listener.isOnItemClicked());
        assertEquals(WORDS[0], mAutoCompleteTextView.getText().toString());
        assertFalse(mAutoCompleteTextView.isPerformingCompletion());
        listener.clearItemClickedStatus();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.showDropDown();
            }
        });
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_0);
        assertFalse(listener.isOnItemClicked());
        assertNotSame("", mAutoCompleteTextView.getText().toString());
        assertFalse(mAutoCompleteTextView.isPerformingCompletion());
        listener.clearItemClickedStatus();
        runTestOnUiThread(new Runnable() {
            public void run() {
               mAutoCompleteTextView.dismissDropDown();
            }
        });
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        assertFalse(listener.isOnItemClicked());
        assertNotSame("", mAutoCompleteTextView.getText().toString());
        assertFalse(mAutoCompleteTextView.isPerformingCompletion());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "performValidation",
        args = {}
    )
    @UiThreadTest
    public void testPerformValidation() {
        final CharSequence text = "this";
        mAutoCompleteTextView.setValidator(mValidator);
        mAutoCompleteTextView.setAdapter((ArrayAdapter<String>) null);
        mAutoCompleteTextView.setText(text);
        mAutoCompleteTextView.performValidation();
        assertEquals(STRING_VALIDATED, mAutoCompleteTextView.getText().toString());
        mAutoCompleteTextView.setValidator(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCompletionHint",
        args = {java.lang.CharSequence.class}
    )
    @ToBeFixed( bug = "1400249", explanation = "only setter no getter")
    public void testSetCompletionHint() {
        mAutoCompleteTextView.setCompletionHint("TEST HINT");
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onAttachedToWindow",
        args = {}
    )
    public void testOnAttachedToWindow() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onCommitCompletion",
        args = {android.view.inputmethod.CompletionInfo.class}
    )
    public void testOnCommitCompletion() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onDetachedFromWindow",
        args = {}
    )
    public void testOnDetachedFromWindow() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onKeyPreIme",
        args = {int.class, android.view.KeyEvent.class}
    )
    public void testOnKeyPreIme() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setListSelection",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getListSelection",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearListSelection",
            args = {}
        )
    })
    @ToBeFixed(bug = "", explanation = "When clearListSelection, getListSelection should " +
            "return ListView.INVALID_POSITION, but not.")
    public void testAccessListSelection() throws Throwable {
        final MockOnItemClickListener listener = new MockOnItemClickListener();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setOnItemClickListener(listener);
                mAutoCompleteTextView.setAdapter(mAdapter);
                mAutoCompleteTextView.requestFocus();
                mAutoCompleteTextView.showDropDown();
            }
        });
        mInstrumentation.waitForIdleSync();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setListSelection(1);
                assertEquals(1, mAutoCompleteTextView.getListSelection());
                mAutoCompleteTextView.setListSelection(2);
                assertEquals(2, mAutoCompleteTextView.getListSelection());
                mAutoCompleteTextView.clearListSelection();
                assertEquals(2, mAutoCompleteTextView.getListSelection());
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDropDownAnchor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDropDownAnchor",
            args = {}
        )
    })
    public void testAccessDropDownAnchor() {
        mAutoCompleteTextView.setDropDownAnchor(View.NO_ID);
        assertEquals(View.NO_ID, mAutoCompleteTextView.getDropDownAnchor());
        mAutoCompleteTextView.setDropDownAnchor(0x5555);
        assertEquals(0x5555, mAutoCompleteTextView.getDropDownAnchor());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDropDownWidth",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDropDownWidth",
            args = {}
        )
    })
    public void testAccessDropDownWidth() {
        mAutoCompleteTextView.setDropDownWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, mAutoCompleteTextView.getDropDownWidth());
        mAutoCompleteTextView.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, mAutoCompleteTextView.getDropDownWidth());
    }
    private static class MockOnItemClickListener implements AdapterView.OnItemClickListener {
        private boolean mOnItemClickedFlag = false;
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mOnItemClickedFlag = true;
            return;
        }
        public boolean isOnItemClicked() {
            return mOnItemClickedFlag;
        }
        public void clearItemClickedStatus() {
            mOnItemClickedFlag = false;
        }
    }
    private static class MockOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            return;
        }
        public void onNothingSelected(AdapterView<?> parent) {
            return;
        }
    }
    private class MockValidator implements AutoCompleteTextView.Validator {
        public CharSequence fixText(CharSequence invalidText) {
            return STRING_VALIDATED;
        }
        public boolean isValid(CharSequence text) {
            if (text == STRING_TEST) {
                return true;
            }
            return false;
        }
    }
    private static class MockAutoCompleteTextView extends AutoCompleteTextView {
        private boolean mOnTextChangedFlag = false;
        private boolean mOnFilterCompleteFlag = false;
        private String lastChangeText = "";
        private int mStart = 0;
        private int mBefore = 0;
        private int mAfter = 0;
        public void resetStatus() {
            mOnTextChangedFlag = false;
            mOnFilterCompleteFlag = false;
            mStart = 0;
            mBefore = 0;
            mAfter = 0;
        }
        public MockAutoCompleteTextView(Context context) {
            super(context);
            resetStatus();
        }
        public MockAutoCompleteTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        protected MockAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
        @Override
        protected CharSequence convertSelectionToString(Object selectedItem) {
            return super.convertSelectionToString(selectedItem);
        }
        @Override
        protected Filter getFilter() {
            return super.getFilter();
        }
        @Override
        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
        @Override
        protected void onTextChanged(CharSequence text, int start, int before, int after) {
            super.onTextChanged(text, start, before, after);
            mOnTextChangedFlag = true;
            lastChangeText = text.toString();
            mStart = start;
            mBefore = before;
            mAfter = after;
        }
        @Override
        protected void performFiltering(CharSequence text, int keyCode) {
            super.performFiltering(text, keyCode);
        }
        @Override
        protected void replaceText(CharSequence text) {
            super.replaceText(text);
        }
        @Override
        protected boolean setFrame(int l, int t, int r, int b) {
            return super.setFrame(l, t, r, b);
        }
        @Override
        public void onFilterComplete(int count) {
            super.onFilterComplete(count);
            mOnFilterCompleteFlag = true;
        }
        protected boolean isOnTextChanged() {
            return mOnTextChangedFlag;
        }
        protected String getLastChangeText() {
            return lastChangeText;
        }
        protected boolean isOnFilterComplete() {
            return mOnFilterCompleteFlag;
        }
        protected int getStart() {
            return mStart;
        }
        protected int getBefore() {
            return mBefore;
        }
        protected int getAfter() {
            return mAfter;
        }
    }
    private void inflatePopup() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAutoCompleteTextView.setText("");
                mAutoCompleteTextView.setFocusable(true);
                mAutoCompleteTextView.requestFocus();
                mAutoCompleteTextView.showDropDown();
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    private static class MockFilter extends Filter {
        private String mFilterResult;
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                mFilterResult = constraint.toString();
            }
            return null;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
        }
        public String getResult() {
            return mFilterResult;
        }
    }
    private static class MockAdapter<T> extends ArrayAdapter<T> implements Filterable {
        private MockFilter mFilter;
        public MockAdapter(Context context, int textViewResourceId, T[] objects) {
            super(context, textViewResourceId, objects);
        }
        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new MockFilter();
            }
            return mFilter;
        }
    }
}
