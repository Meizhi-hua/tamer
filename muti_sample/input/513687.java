public class EditStyledText extends EditText {
    private static final String LOG_TAG = "EditStyledText";
    private static final boolean DBG = false;
    public static final int MODE_NOTHING = 0;
    public static final int MODE_COPY = 1;
    public static final int MODE_PASTE = 2;
    public static final int MODE_SIZE = 3;
    public static final int MODE_COLOR = 4;
    public static final int MODE_SELECT = 5;
    public static final int MODE_ALIGN = 6;
    public static final int MODE_CUT = 7;
    public static final int STATE_SELECT_OFF = 0;
    public static final int STATE_SELECT_ON = 1;
    public static final int STATE_SELECTED = 2;
    public static final int STATE_SELECT_FIX = 3;
    public static final int HINT_MSG_NULL = 0;
    public static final int HINT_MSG_COPY_BUF_BLANK = 1;
    public static final int HINT_MSG_SELECT_START = 2;
    public static final int HINT_MSG_SELECT_END = 3;
    public static final int HINT_MSG_PUSH_COMPETE = 4;
    public static final int DEFAULT_BACKGROUND_COLOR = 0x00FFFFFF;
    public interface EditStyledTextNotifier {
        public void notifyHintMsg(int msgId);
        public void notifyStateChanged(int mode, int state);
    }
    private EditStyledTextNotifier mESTInterface;
    private EditorManager mManager;
    private StyledTextConverter mConverter;
    private StyledTextDialog mDialog;
    private Drawable mDefaultBackground;
    private int mBackgroundColor;
    public EditStyledText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public EditStyledText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public EditStyledText(Context context) {
        super(context);
        init();
    }
    public void setNotifier(EditStyledTextNotifier estInterface) {
        mESTInterface = estInterface;
    }
    public void setBuilder(Builder builder) {
        mDialog.setBuilder(builder);
    }
    public void setColorAlertParams(CharSequence colortitle,
            CharSequence[] colornames, CharSequence[] colorints) {
        mDialog.setColorAlertParams(colortitle, colornames, colorints);
    }
    public void setSizeAlertParams(CharSequence sizetitle,
            CharSequence[] sizenames, CharSequence[] sizedisplayints,
            CharSequence[] sizesendints) {
        mDialog.setSizeAlertParams(sizetitle, sizenames, sizedisplayints,
                sizesendints);
    }
    public void setAlignAlertParams(CharSequence aligntitle,
            CharSequence[] alignnames) {
        mDialog.setAlignAlertParams(aligntitle, alignnames);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mManager.isSoftKeyBlocked() &&
                event.getAction() == MotionEvent.ACTION_UP) {
            cancelLongPress();
        }
        final boolean superResult = super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (DBG) {
                Log.d(LOG_TAG, "--- onTouchEvent");
            }
            mManager.onCursorMoved();
        }
        return superResult;
    }
    public void onStartEdit() {
        mManager.onStartEdit();
    }
    public void onEndEdit() {
        mManager.onEndEdit();
    }
    public void onStartCopy() {
        mManager.onStartCopy();
    }
    public void onStartCut() {
        mManager.onStartCut();
    }
    public void onStartPaste() {
        mManager.onStartPaste();
    }
    public void onStartSize() {
        mManager.onStartSize();
    }
    public void onStartColor() {
        mManager.onStartColor();
    }
    public void onStartBackgroundColor() {
        mManager.onStartBackgroundColor();
    }
    public void onStartAlign() {
        mManager.onStartAlign();
    }
    public void onStartSelect() {
        mManager.onStartSelect();
    }
    public void onStartSelectAll() {
        mManager.onStartSelectAll();
    }
    public void onFixSelectedItem() {
        mManager.onFixSelectedItem();
    }
    public void onInsertImage(Uri uri) {
        mManager.onInsertImage(uri);
    }
    public void onInsertImage(int resId) {
        mManager.onInsertImage(resId);
    }
    public void onInsertHorizontalLine() {
        mManager.onInsertHorizontalLine();
    }
    public void onClearStyles() {
        mManager.onClearStyles();
    }
    public void setItemSize(int size) {
        mManager.setItemSize(size);
    }
    public void setItemColor(int color) {
        mManager.setItemColor(color);
    }
    public void setAlignment(Layout.Alignment align) {
        mManager.setAlignment(align);
    }
    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        mBackgroundColor = color;
    }
    public void setHtml(String html) {
        mConverter.SetHtml(html);
    }
    public boolean isEditting() {
        return mManager.isEditting();
    }
    public boolean isStyledText() {
        return mManager.isStyledText();
    }
    public boolean isSoftKeyBlocked() {
        return mManager.isSoftKeyBlocked();
    }
    public int getEditMode() {
        return mManager.getEditMode();
    }
    public int getSelectState() {
        return mManager.getSelectState();
    }
    @Override
    public Bundle getInputExtras(boolean create) {
        if (DBG) {
            Log.d(LOG_TAG, "---getInputExtras");
        }
        Bundle bundle = super.getInputExtras(create);
        if (bundle != null) {
            bundle = new Bundle();
        }
        bundle.putBoolean("allowEmoji", true);
        return bundle;
    }
    public String getHtml() {
        return mConverter.getHtml();
    }
    public String getHtml(ArrayList<Uri> uris) {
        mConverter.getUriArray(uris, getText());
        return mConverter.getHtml();
    }
    public int getBackgroundColor() {
        return mBackgroundColor;
    }
    public int getForeGroundColor(int pos) {
        if (DBG) {
            Log.d(LOG_TAG, "---getForeGroundColor: " + pos);
        }
        if (pos < 0 || pos > getText().length()) {
            Log.e(LOG_TAG, "---getForeGroundColor: Illigal position.");
            return DEFAULT_BACKGROUND_COLOR;
        } else {
            ForegroundColorSpan[] spans =
                getText().getSpans(pos, pos, ForegroundColorSpan.class);
            if (spans.length > 0) {
                return spans[0].getForegroundColor();
            } else {
                return DEFAULT_BACKGROUND_COLOR;
            }
        }
    }
    private void init() {
        if (DBG) {
            Log.d(LOG_TAG, "--- init");
        }
        requestFocus();
        mDefaultBackground = getBackground();
        mBackgroundColor = DEFAULT_BACKGROUND_COLOR;
        mManager = new EditorManager(this);
        mConverter = new StyledTextConverter(this);
        mDialog = new StyledTextDialog(this);
        setMovementMethod(new StyledTextArrowKeyMethod(mManager));
        mManager.blockSoftKey();
        mManager.unblockSoftKey();
    }
    private void onShowForegroundColorAlert() {
        mDialog.onShowForegroundColorAlertDialog();
    }
    private void onShowBackgroundColorAlert() {
        mDialog.onShowBackgroundColorAlertDialog();
    }
    private void onShowSizeAlert() {
        mDialog.onShowSizeAlertDialog();
    }
    private void onShowAlignAlert() {
        mDialog.onShowAlignAlertDialog();
    }
    private void setHintMessage(int msgId) {
        if (mESTInterface != null) {
            mESTInterface.notifyHintMsg(msgId);
        }
    }
    private void notifyStateChanged(int mode, int state) {
        if (mESTInterface != null) {
            mESTInterface.notifyStateChanged(mode, state);
        }
    }
    private class EditorManager {
        private boolean mEditFlag = false;
        private boolean mSoftKeyBlockFlag = false;
        private int mMode = 0;
        private int mState = 0;
        private int mCurStart = 0;
        private int mCurEnd = 0;
        private EditStyledText mEST;
        EditorManager(EditStyledText est) {
            mEST = est;
        }
        public void onStartEdit() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onStartEdit");
            }
            Log.d(LOG_TAG, "--- onstartedit:");
            handleResetEdit();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onEndEdit() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onEndEdit");
            }
            handleCancel();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartCopy() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onStartCopy");
            }
            handleCopy();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartCut() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onStartCut");
            }
            handleCut();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartPaste() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onStartPaste");
            }
            handlePaste();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartSize() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onStartSize");
            }
            handleSize();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartAlign() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onStartAlignRight");
            }
            handleAlign();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartColor() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickColor");
            }
            handleColor();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartBackgroundColor() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickColor");
            }
            mEST.onShowBackgroundColorAlert();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onStartSelect() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickSelect");
            }
            mMode = MODE_SELECT;
            if (mState == STATE_SELECT_OFF) {
                handleSelect();
            } else {
                unsetSelect();
                handleSelect();
            }
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onCursorMoved() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickView");
            }
            if (mState == STATE_SELECT_ON || mState == STATE_SELECTED) {
                handleSelect();
                mEST.notifyStateChanged(mMode, mState);
            }
        }
        public void onStartSelectAll() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickSelectAll");
            }
            handleSelectAll();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onFixSelectedItem() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickComplete");
            }
            handleComplete();
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onInsertImage(Uri uri) {
            if (DBG) {
                Log.d(LOG_TAG, "--- onInsertImage by URI: " + uri.getPath()
                        + "," + uri.toString());
            }
            insertImageSpan(new ImageSpan(mEST.getContext(), uri));
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onInsertImage(int resID) {
            if (DBG) {
                Log.d(LOG_TAG, "--- onInsertImage by resID");
            }
            insertImageSpan(new ImageSpan(mEST.getContext(), resID));
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onInsertHorizontalLine() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onInsertHorizontalLine:");
            }
            insertImageSpan(new HorizontalLineSpan(0xFF000000, mEST));
            mEST.notifyStateChanged(mMode, mState);
        }
        public void onClearStyles() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClearStyles");
            }
            Editable txt = mEST.getText();
            int len = txt.length();
            Object[] styles = txt.getSpans(0, len, Object.class);
            for (Object style : styles) {
                if (style instanceof ParagraphStyle ||
                        style instanceof QuoteSpan ||
                        style instanceof CharacterStyle) {
                    if (style instanceof ImageSpan) {
                        int start = txt.getSpanStart(style);
                        int end = txt.getSpanEnd(style);
                        txt.replace(start, end, "");
                    }
                    txt.removeSpan(style);
                }
            }
            mEST.setBackgroundDrawable(mEST.mDefaultBackground);
            mEST.mBackgroundColor = DEFAULT_BACKGROUND_COLOR;
        }
        public void setItemSize(int size) {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickSizeItem");
            }
            if (mState == STATE_SELECTED || mState == STATE_SELECT_FIX) {
                changeSizeSelectedText(size);
                handleResetEdit();
            }
        }
        public void setItemColor(int color) {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickColorItem");
            }
            if (mState == STATE_SELECTED || mState == STATE_SELECT_FIX) {
                changeColorSelectedText(color);
                handleResetEdit();
            }
        }
        public void setAlignment(Layout.Alignment align) {
            if (DBG) {
                Log.d(LOG_TAG, "--- onClickColorItem");
            }
            if (mState == STATE_SELECTED || mState == STATE_SELECT_FIX) {
                changeAlign(align);
                handleResetEdit();
            }
        }
        public boolean isEditting() {
            return mEditFlag;
        }
        public boolean isStyledText() {
            Editable txt = mEST.getText();
            int len = txt.length();
            if (txt.getSpans(0, len -1, ParagraphStyle.class).length > 0 ||
                    txt.getSpans(0, len -1, QuoteSpan.class).length > 0 ||
                    txt.getSpans(0, len -1, CharacterStyle.class).length > 0 ||
                    mEST.mBackgroundColor != DEFAULT_BACKGROUND_COLOR) {
                return true;
            }
            return false;
        }
        public boolean isSoftKeyBlocked() {
            return mSoftKeyBlockFlag;
        }
        public int getEditMode() {
            return mMode;
        }
        public int getSelectState() {
            return mState;
        }
        public int getSelectionStart() {
            return mCurStart;
        }
        public int getSelectionEnd() {
            return mCurEnd;
        }
        private void doNextHandle() {
            if (DBG) {
                Log.d(LOG_TAG, "--- doNextHandle: " + mMode + "," + mState);
            }
            switch (mMode) {
            case MODE_COPY:
                handleCopy();
                break;
            case MODE_CUT:
                handleCut();
                break;
            case MODE_PASTE:
                handlePaste();
                break;
            case MODE_SIZE:
                handleSize();
                break;
            case MODE_COLOR:
                handleColor();
                break;
            case MODE_ALIGN:
                handleAlign();
                break;
            default:
                break;
            }
        }
        private void handleCancel() {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleCancel");
            }
            mMode = MODE_NOTHING;
            mState = STATE_SELECT_OFF;
            mEditFlag = false;
            Log.d(LOG_TAG, "--- handleCancel:" + mEST.getInputType());
            unblockSoftKey();
            unsetSelect();
        }
        private void handleComplete() {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleComplete");
            }
            if (!mEditFlag) {
                return;
            }
            if (mState == STATE_SELECTED) {
                mState = STATE_SELECT_FIX;
            }
            doNextHandle();
        }
        private void handleTextViewFunc(int mode, int id) {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleTextView: " + mMode + "," + mState +
                        "," + id);
            }
            if (!mEditFlag) {
                return;
            }
            if (mMode == MODE_NOTHING || mMode == MODE_SELECT) {
                mMode = mode;
                if (mState == STATE_SELECTED) {
                    mState = STATE_SELECT_FIX;
                    handleTextViewFunc(mode, id);
                } else {
                    handleSelect();
                }
            } else if (mMode != mode) {
                handleCancel();
                mMode = mode;
                handleTextViewFunc(mode, id);
            } else if (mState == STATE_SELECT_FIX) {
                mEST.onTextContextMenuItem(id);
                handleResetEdit();
            }
        }
        private void handleCopy() {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleCopy: " + mMode + "," + mState);
            }
            handleTextViewFunc(MODE_COPY, android.R.id.copy);
        }
        private void handleCut() {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleCopy: " + mMode + "," + mState);
            }
            handleTextViewFunc(MODE_CUT, android.R.id.cut);
        }
        private void handlePaste() {
            if (DBG) {
                Log.d(LOG_TAG, "--- handlePaste");
            }
            if (!mEditFlag) {
                return;
            }
            mEST.onTextContextMenuItem(android.R.id.paste);
        }
        private void handleSetSpan(int mode) {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleSetSpan:" + mEditFlag + ","
                        + mState + ',' + mMode);
            }
            if (!mEditFlag) {
                Log.e(LOG_TAG, "--- handleSetSpan: Editing is not started.");
                return;
            }
            if (mMode == MODE_NOTHING || mMode == MODE_SELECT) {
                mMode = mode;
                if (mState == STATE_SELECTED) {
                    mState = STATE_SELECT_FIX;
                    handleSetSpan(mode);
                } else {
                    handleSelect();
                }
            } else if (mMode != mode) {
                handleCancel();
                mMode = mode;
                handleSetSpan(mode);
            } else {
                if (mState == STATE_SELECT_FIX) {
                    mEST.setHintMessage(HINT_MSG_NULL);
                    switch (mode) {
                    case MODE_COLOR:
                        mEST.onShowForegroundColorAlert();
                        break;
                    case MODE_SIZE:
                        mEST.onShowSizeAlert();
                        break;
                    case MODE_ALIGN:
                        mEST.onShowAlignAlert();
                        break;
                    default:
                        Log.e(LOG_TAG, "--- handleSetSpan: invalid mode.");
                        break;
                    }
                } else {
                    Log.d(LOG_TAG, "--- handleSetSpan: do nothing.");
                }
            }
        }
        private void handleSize() {
            handleSetSpan(MODE_SIZE);
        }
        private void handleColor() {
            handleSetSpan(MODE_COLOR);
        }
        private void handleAlign() {
            handleSetSpan(MODE_ALIGN);
        }
        private void handleSelect() {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleSelect:" + mEditFlag + "," + mState);
            }
            if (!mEditFlag) {
                return;
            }
            if (mState == STATE_SELECT_OFF) {
                if (isTextSelected()) {
                    Log.e(LOG_TAG, "Selection is off, but selected");
                }
                setSelectStartPos();
                blockSoftKey();
                mEST.setHintMessage(HINT_MSG_SELECT_END);
            } else if (mState == STATE_SELECT_ON) {
                if (isTextSelected()) {
                    Log.e(LOG_TAG, "Selection now start, but selected");
                }
                setSelectedEndPos();
                mEST.setHintMessage(HINT_MSG_PUSH_COMPETE);
                doNextHandle();
            } else if (mState == STATE_SELECTED) {
                if (!isTextSelected()) {
                    Log.e(LOG_TAG, "Selection is done, but not selected");
                }
                setSelectedEndPos();
                doNextHandle();
            }
        }
        private void handleSelectAll() {
            if (DBG) {
                Log.d(LOG_TAG, "--- handleSelectAll");
            }
            if (!mEditFlag) {
                return;
            }
            mEST.selectAll();
            mState = STATE_SELECTED;
        }
        private void handleResetEdit() {
            if (DBG) {
                Log.d(LOG_TAG, "Reset Editor");
            }
            blockSoftKey();
            handleCancel();
            mEditFlag = true;
            mEST.setHintMessage(HINT_MSG_SELECT_START);
        }
        private void setSelection() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onSelect:" + mCurStart + "," + mCurEnd);
            }
            if (mCurStart >= 0 && mCurStart <= mEST.getText().length()
                    && mCurEnd >= 0 && mCurEnd <= mEST.getText().length()) {
                if (mCurStart < mCurEnd) {
                    mEST.setSelection(mCurStart, mCurEnd);
                } else {
                    mEST.setSelection(mCurEnd, mCurStart);
                }
                mState = STATE_SELECTED;
            } else {
                Log.e(LOG_TAG,
                        "Select is on, but cursor positions are illigal.:"
                                + mEST.getText().length() + "," + mCurStart
                                + "," + mCurEnd);
            }
        }
        private void unsetSelect() {
            if (DBG) {
                Log.d(LOG_TAG, "--- offSelect");
            }
            int currpos = mEST.getSelectionStart();
            mEST.setSelection(currpos, currpos);
            mState = STATE_SELECT_OFF;
        }
        private void setSelectStartPos() {
            if (DBG) {
                Log.d(LOG_TAG, "--- setSelectStartPos");
            }
            mCurStart = mEST.getSelectionStart();
            mState = STATE_SELECT_ON;
        }
        private void setSelectedEndPos() {
            if (DBG) {
                Log.d(LOG_TAG, "--- setSelectEndPos:");
            }
            if (mEST.getSelectionStart() == mCurStart) {
                setSelectedEndPos(mEST.getSelectionEnd());
            } else {
                setSelectedEndPos(mEST.getSelectionStart());
            }
        }
        public void setSelectedEndPos(int pos) {
            if (DBG) {
                Log.d(LOG_TAG, "--- setSelectedEndPos:");
            }
            mCurEnd = pos;
            setSelection();
        }
        private boolean isTextSelected() {
            if (DBG) {
                Log.d(LOG_TAG, "--- isTextSelected:" + mCurStart + ","
                        + mCurEnd);
            }
            return (mCurStart != mCurEnd)
                    && (mState == STATE_SELECTED ||
                            mState == STATE_SELECT_FIX);
        }
        private void setStyledTextSpan(Object span, int start, int end) {
            if (DBG) {
                Log.d(LOG_TAG, "--- setStyledTextSpan:" + mMode + ","
                        + start + "," + end);
            }
            if (start < end) {
                mEST.getText().setSpan(span, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                mEST.getText().setSpan(span, end, start,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        private void changeSizeSelectedText(int size) {
            if (DBG) {
                Log.d(LOG_TAG, "--- changeSize:" + size);
            }
            setStyledTextSpan(new AbsoluteSizeSpan(size),
                mCurStart, mCurEnd);
        }
        private void changeColorSelectedText(int color) {
            if (DBG) {
                Log.d(LOG_TAG, "--- changeColor:" + color);
            }
            setStyledTextSpan(new ForegroundColorSpan(color),
                mCurStart, mCurEnd);
        }
        private void changeAlign(Layout.Alignment align) {
            if (DBG) {
                Log.d(LOG_TAG, "--- changeAlign:" + align);
            }
            setStyledTextSpan(new AlignmentSpan.Standard(align),
                    findLineStart(mEST.getText(), mCurStart),
                    findLineEnd(mEST.getText(), mCurEnd));
        }
        private int findLineStart(Editable text, int current) {
            if (DBG) {
                Log.d(LOG_TAG, "--- findLineStart: curr:" + current +
                        ", length:" + text.length());
            }
            int pos = current;
            for (; pos > 0; pos--) {
                if (text.charAt(pos - 1) == '\n') {
                    break;
                }
            }
            return pos;
        }
        private void insertImageSpan(ImageSpan span) {
            if (DBG) {
                Log.d(LOG_TAG, "--- insertImageSpan");
            }
            if (span != null) {
                Log.d(LOG_TAG, "--- insertimagespan:" + span.getDrawable().getIntrinsicHeight() + "," + span.getDrawable().getIntrinsicWidth());
                Log.d(LOG_TAG, "--- insertimagespan:" + span.getDrawable().getClass());
                int curpos = mEST.getSelectionStart();
                mEST.getText().insert(curpos, "\uFFFC");
                mEST.getText().setSpan(span, curpos, curpos + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mEST.notifyStateChanged(mMode, mState);
            } else {
                Log.e(LOG_TAG, "--- insertImageSpan: null span was inserted");
            }
        }
        private int findLineEnd(Editable text, int current) {
            if (DBG) {
                Log.d(LOG_TAG, "--- findLineEnd: curr:" + current +
                        ", length:" + text.length());
            }
            int pos = current;
            for (; pos < text.length(); pos++) {
                if (pos > 0 && text.charAt(pos - 1) == '\n') {
                    break;
                }
            }
            return pos;
        }
        private void blockSoftKey() {
            if (DBG) {
                Log.d(LOG_TAG, "--- blockSoftKey:");
            }
            InputMethodManager imm = (InputMethodManager) mEST.getContext().
            getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEST.getWindowToken(), 0);
            mEST.setOnClickListener(
                    new OnClickListener() {
                        public void onClick(View v) {
                            Log.d(LOG_TAG, "--- ontrackballclick:");
                            onFixSelectedItem();
                        }
            });
            mSoftKeyBlockFlag = true;
        }
        private void unblockSoftKey() {
            if (DBG) {
                Log.d(LOG_TAG, "--- unblockSoftKey:");
            }
            mEST.setOnClickListener(null);
            mSoftKeyBlockFlag = false;
        }
    }
    private class StyledTextConverter {
        private EditStyledText mEST;
        public StyledTextConverter(EditStyledText est) {
            mEST = est;
        }
        public String getHtml() {
            String htmlBody = Html.toHtml(mEST.getText());
            if (DBG) {
                Log.d(LOG_TAG, "--- getConvertedBody:" + htmlBody);
            }
            return htmlBody;
        }
        public void getUriArray(ArrayList<Uri> uris, Editable text) {
            uris.clear();
            if (DBG) {
                Log.d(LOG_TAG, "--- getUriArray:");
            }
            int len = text.length();
            int next;
            for (int i = 0; i < text.length(); i = next) {
                next = text.nextSpanTransition(i, len, ImageSpan.class);
                ImageSpan[] images = text.getSpans(i, next, ImageSpan.class);
                for (int j = 0; j < images.length; j++) {
                    if (DBG) {
                        Log.d(LOG_TAG, "--- getUriArray: foundArray" +
                                ((ImageSpan) images[j]).getSource());
                    }
                    uris.add(Uri.parse(
                            ((ImageSpan) images[j]).getSource()));
                }
            }
        }
        public void SetHtml (String html) {
            final Spanned spanned = Html.fromHtml(html, new Html.ImageGetter() {
                public Drawable getDrawable(String src) {
                    Log.d(LOG_TAG, "--- sethtml: src="+src);
                    if (src.startsWith("content:
                        Uri uri = Uri.parse(src);
                        try {
                            InputStream is = mEST.getContext().getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            Drawable drawable = new BitmapDrawable(
                                    getContext().getResources(), bitmap);
                            drawable.setBounds(0, 0,
                                    drawable.getIntrinsicWidth(),
                                    drawable.getIntrinsicHeight());
                            is.close();
                            return drawable;
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "--- set html: Failed to loaded content " + uri, e);
                            return null;
                        }
                    }
                    Log.d(LOG_TAG, "  unknown src="+src);
                    return null;
                }
            }, null);
            mEST.setText(spanned);
        }
    }
    private class StyledTextDialog {
        Builder mBuilder;
        CharSequence mColorTitle;
        CharSequence mSizeTitle;
        CharSequence mAlignTitle;
        CharSequence[] mColorNames;
        CharSequence[] mColorInts;
        CharSequence[] mSizeNames;
        CharSequence[] mSizeDisplayInts;
        CharSequence[] mSizeSendInts;
        CharSequence[] mAlignNames;
        EditStyledText mEST;
        public StyledTextDialog(EditStyledText est) {
            mEST = est;
        }
        public void setBuilder(Builder builder) {
            mBuilder = builder;
        }
        public void setColorAlertParams(CharSequence colortitle,
                CharSequence[] colornames, CharSequence[] colorints) {
            mColorTitle = colortitle;
            mColorNames = colornames;
            mColorInts = colorints;
        }
        public void setSizeAlertParams(CharSequence sizetitle,
                CharSequence[] sizenames, CharSequence[] sizedisplayints,
                CharSequence[] sizesendints) {
            mSizeTitle = sizetitle;
            mSizeNames = sizenames;
            mSizeDisplayInts = sizedisplayints;
            mSizeSendInts = sizesendints;
        }
        public void setAlignAlertParams(CharSequence aligntitle,
                CharSequence[] alignnames) {
            mAlignTitle = aligntitle;
            mAlignNames = alignnames;
        }
        private boolean checkColorAlertParams() {
            if (DBG) {
                Log.d(LOG_TAG, "--- checkParams");
            }
            if (mBuilder == null) {
                Log.e(LOG_TAG, "--- builder is null.");
                return false;
            } else if (mColorTitle == null || mColorNames == null
                    || mColorInts == null) {
                Log.e(LOG_TAG, "--- color alert params are null.");
                return false;
            } else if (mColorNames.length != mColorInts.length) {
                Log.e(LOG_TAG, "--- the length of color alert params are "
                        + "different.");
                return false;
            }
            return true;
        }
        private boolean checkSizeAlertParams() {
            if (DBG) {
                Log.d(LOG_TAG, "--- checkParams");
            }
            if (mBuilder == null) {
                Log.e(LOG_TAG, "--- builder is null.");
                return false;
            } else if (mSizeTitle == null || mSizeNames == null
                    || mSizeDisplayInts == null || mSizeSendInts == null) {
                Log.e(LOG_TAG, "--- size alert params are null.");
                return false;
            } else if (mSizeNames.length != mSizeDisplayInts.length
                    && mSizeSendInts.length != mSizeDisplayInts.length) {
                Log.e(LOG_TAG, "--- the length of size alert params are "
                        + "different.");
                return false;
            }
            return true;
        }
        private boolean checkAlignAlertParams() {
            if (DBG) {
                Log.d(LOG_TAG, "--- checkAlignAlertParams");
            }
            if (mBuilder == null) {
                Log.e(LOG_TAG, "--- builder is null.");
                return false;
            } else if (mAlignTitle == null) {
                Log.e(LOG_TAG, "--- align alert params are null.");
                return false;
            }
            return true;
        }
        private void onShowForegroundColorAlertDialog() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onShowForegroundColorAlertDialog");
            }
            if (!checkColorAlertParams()) {
                return;
            }
            mBuilder.setTitle(mColorTitle);
            mBuilder.setIcon(0);
            mBuilder.
            setItems(mColorNames,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("EETVM", "mBuilder.onclick:" + which);
                    int color = Integer.parseInt(
                            (String) mColorInts[which], 16) - 0x01000000;
                    mEST.setItemColor(color);
                }
            });
            mBuilder.show();
        }
        private void onShowBackgroundColorAlertDialog() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onShowBackgroundColorAlertDialog");
            }
            if (!checkColorAlertParams()) {
                return;
            }
            mBuilder.setTitle(mColorTitle);
            mBuilder.setIcon(0);
            mBuilder.
            setItems(mColorNames,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("EETVM", "mBuilder.onclick:" + which);
                    int color = Integer.parseInt(
                            (String) mColorInts[which], 16) - 0x01000000;
                    mEST.setBackgroundColor(color);
                }
            });
            mBuilder.show();
        }
        private void onShowSizeAlertDialog() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onShowSizeAlertDialog");
            }
            if (!checkSizeAlertParams()) {
                return;
            }
            mBuilder.setTitle(mSizeTitle);
            mBuilder.setIcon(0);
            mBuilder.
            setItems(mSizeNames,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(LOG_TAG, "mBuilder.onclick:" + which);
                    int size = Integer
                    .parseInt((String) mSizeDisplayInts[which]);
                    mEST.setItemSize(size);
                }
            });
            mBuilder.show();
        }
        private void onShowAlignAlertDialog() {
            if (DBG) {
                Log.d(LOG_TAG, "--- onShowAlignAlertDialog");
            }
            if (!checkAlignAlertParams()) {
                return;
            }
            mBuilder.setTitle(mAlignTitle);
            mBuilder.setIcon(0);
            mBuilder.
            setItems(mAlignNames,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(LOG_TAG, "mBuilder.onclick:" + which);
                    Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
                    switch (which) {
                    case 0:
                        align = Layout.Alignment.ALIGN_NORMAL;
                        break;
                    case 1:
                        align = Layout.Alignment.ALIGN_CENTER;
                        break;
                    case 2:
                        align = Layout.Alignment.ALIGN_OPPOSITE;
                        break;
                    default:
                        break;
                    }
                    mEST.setAlignment(align);
                }
            });
            mBuilder.show();
        }
    }
    private class StyledTextArrowKeyMethod extends ArrowKeyMovementMethod {
        EditorManager mManager;
        StyledTextArrowKeyMethod(EditorManager manager) {
            super();
            mManager = manager;
        }
        @Override
        public boolean onKeyDown(TextView widget, Spannable buffer,
                int keyCode, KeyEvent event) {
            if (!mManager.isSoftKeyBlocked()) {
                return super.onKeyDown(widget, buffer, keyCode, event);
            }
            if (executeDown(widget, buffer, keyCode)) {
                return true;
            }
            return false;
        }
        private int getEndPos(TextView widget) {
            int end;
            if (widget.getSelectionStart() == mManager.getSelectionStart()) {
                end = widget.getSelectionEnd();
            } else {
                end = widget.getSelectionStart();
            }
            return end;
        }
        private boolean up(TextView widget, Spannable buffer) {
            if (DBG) {
                Log.d(LOG_TAG, "--- up:");
            }
            Layout layout = widget.getLayout();
            int end = getEndPos(widget);
            int line = layout.getLineForOffset(end);
            if (line > 0) {
                int to;
                if (layout.getParagraphDirection(line) ==
                    layout.getParagraphDirection(line - 1)) {
                    float h = layout.getPrimaryHorizontal(end);
                    to = layout.getOffsetForHorizontal(line - 1, h);
                } else {
                    to = layout.getLineStart(line - 1);
                }
                mManager.setSelectedEndPos(to);
                mManager.onCursorMoved();
                return true;
            }
            return false;
        }
        private boolean down(TextView widget, Spannable buffer) {
            if (DBG) {
                Log.d(LOG_TAG, "--- down:");
            }
            Layout layout = widget.getLayout();
            int end = getEndPos(widget);
            int line = layout.getLineForOffset(end);
            if (line < layout.getLineCount() - 1) {
                int to;
                if (layout.getParagraphDirection(line) ==
                    layout.getParagraphDirection(line + 1)) {
                    float h = layout.getPrimaryHorizontal(end);
                    to = layout.getOffsetForHorizontal(line + 1, h);
                } else {
                    to = layout.getLineStart(line + 1);
                }
                mManager.setSelectedEndPos(to);
                mManager.onCursorMoved();
                return true;
            }
            return false;
        }
        private boolean left(TextView widget, Spannable buffer) {
            if (DBG) {
                Log.d(LOG_TAG, "--- left:");
            }
            Layout layout = widget.getLayout();
            int to = layout.getOffsetToLeftOf(getEndPos(widget));
            mManager.setSelectedEndPos(to);
            mManager.onCursorMoved();
            return true;
        }
        private boolean right(TextView widget, Spannable buffer) {
            if (DBG) {
                Log.d(LOG_TAG, "--- right:");
            }
            Layout layout = widget.getLayout();
            int to = layout.getOffsetToRightOf(getEndPos(widget));
            mManager.setSelectedEndPos(to);
            mManager.onCursorMoved();
            return true;
        }
        private boolean executeDown(TextView widget, Spannable buffer,
                int keyCode) {
            if (DBG) {
                Log.d(LOG_TAG, "--- executeDown: " + keyCode);
            }
            boolean handled = false;
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                handled |= up(widget, buffer);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                handled |= down(widget, buffer);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handled |= left(widget, buffer);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handled |= right(widget, buffer);
                break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    mManager.onFixSelectedItem();
                    handled = true;
                    break;
            }
            return handled;
        }
    }
    public class HorizontalLineSpan extends ImageSpan {
        public HorizontalLineSpan(int color, View view) {
            super(new HorizontalLineDrawable(color, view));
        }
    }
    public class HorizontalLineDrawable extends ShapeDrawable {
        private View mView;
        public HorizontalLineDrawable(int color, View view) {
            super(new RectShape());
            mView = view;
            renewColor(color);
            renewBounds(view);
        }
        @Override
        public void draw(Canvas canvas) {
            if (DBG) {
                Log.d(LOG_TAG, "--- draw:");
            }
            renewColor();
            renewBounds(mView);
            super.draw(canvas);
        }
        private void renewBounds(View view) {
            if (DBG) {
                int width = mView.getBackground().getBounds().width();
                int height = mView.getBackground().getBounds().height();
                Log.d(LOG_TAG, "--- renewBounds:" + width + "," + height);
                Log.d(LOG_TAG, "--- renewBounds:" + mView.getClass());
            }
            int width = mView.getWidth();
            if (width > 20) {
                width -= 20;
            }
            setBounds(0, 0, width, 2);
        }
        private void renewColor(int color) {
            if (DBG) {
                Log.d(LOG_TAG, "--- renewColor:" + color);
            }
            getPaint().setColor(color);
        }
        private void renewColor() {
            if (DBG) {
                Log.d(LOG_TAG, "--- renewColor:");
            }
            if (mView instanceof View) {
                ImageSpan parent = getParentSpan();
                Editable text = ((EditStyledText)mView).getText();
                int start = text.getSpanStart(parent);
                ForegroundColorSpan[] spans = text.getSpans(start, start, ForegroundColorSpan.class);
                if (spans.length > 0) {
                    renewColor(spans[spans.length - 1].getForegroundColor());
                }
            }
        }
        private ImageSpan getParentSpan() {
            if (DBG) {
                Log.d(LOG_TAG, "--- getParentSpan:");
            }
            if (mView instanceof EditStyledText) {
                Editable text = ((EditStyledText)mView).getText();
                ImageSpan[] images = text.getSpans(0, text.length(), ImageSpan.class);
                if (images.length > 0) {
                    for (ImageSpan image: images) {
                        if (image.getDrawable() == this) {
                            return image;
                        }
                    }
                }
            }
            Log.e(LOG_TAG, "---renewBounds: Couldn't find");
            return null;
        }
    }
}
