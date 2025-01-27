public class InputConnectionWrapper implements InputConnection {
    private static final int MAX_WAIT_TIME_MILLIS = 2000;
    private final IInputContext mIInputContext;
    static class InputContextCallback extends IInputContextCallback.Stub {
        private static final String TAG = "InputConnectionWrapper.ICC";
        public int mSeq;
        public boolean mHaveValue;
        public CharSequence mTextBeforeCursor;
        public CharSequence mTextAfterCursor;
        public ExtractedText mExtractedText;
        public int mCursorCapsMode;
        private static InputContextCallback sInstance = new InputContextCallback();
        private static int sSequenceNumber = 1;
        private static InputContextCallback getInstance() {
            synchronized (InputContextCallback.class) {
                InputContextCallback callback;
                if (sInstance != null) {
                    callback = sInstance;
                    sInstance = null;
                    callback.mHaveValue = false;
                } else {
                    callback = new InputContextCallback();
                }
                callback.mSeq = sSequenceNumber++;
                return callback;
            }
        }
        private void dispose() {
            synchronized (InputContextCallback.class) {
                if (sInstance == null) {
                    mTextAfterCursor = null;
                    mTextBeforeCursor = null;
                    mExtractedText = null;
                    sInstance = this;
                }
            }
        }
        public void setTextBeforeCursor(CharSequence textBeforeCursor, int seq) {
            synchronized (this) {
                if (seq == mSeq) {
                    mTextBeforeCursor = textBeforeCursor;
                    mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + mSeq
                            + ") in setTextBeforeCursor, ignoring.");
                }
            }
        }
        public void setTextAfterCursor(CharSequence textAfterCursor, int seq) {
            synchronized (this) {
                if (seq == mSeq) {
                    mTextAfterCursor = textAfterCursor;
                    mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + mSeq
                            + ") in setTextAfterCursor, ignoring.");
                }
            }
        }
        public void setCursorCapsMode(int capsMode, int seq) {
            synchronized (this) {
                if (seq == mSeq) {
                    mCursorCapsMode = capsMode; 
                    mHaveValue = true;  
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + mSeq
                            + ") in setCursorCapsMode, ignoring.");
                }
            }
        }
        public void setExtractedText(ExtractedText extractedText, int seq) {
            synchronized (this) {
                if (seq == mSeq) {
                    mExtractedText = extractedText;
                    mHaveValue = true;
                    notifyAll();
                } else {
                    Log.i(TAG, "Got out-of-sequence callback " + seq + " (expected " + mSeq
                            + ") in setExtractedText, ignoring.");
                }
            }
        }
        void waitForResultLocked() {
            long startTime = SystemClock.uptimeMillis();
            long endTime = startTime + MAX_WAIT_TIME_MILLIS;
            while (!mHaveValue) {
                long remainingTime = endTime - SystemClock.uptimeMillis();
                if (remainingTime <= 0) {
                    Log.w(TAG, "Timed out waiting on IInputContextCallback");
                    return;
                }
                try {
                    wait(remainingTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }
    public InputConnectionWrapper(IInputContext inputContext) {
        mIInputContext = inputContext;
    }
    public CharSequence getTextAfterCursor(int length, int flags) {
        CharSequence value = null;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            mIInputContext.getTextAfterCursor(length, flags, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mTextAfterCursor;
                }
            }
            callback.dispose();
        } catch (RemoteException e) {
            return null;
        }
        return value;
    }
    public CharSequence getTextBeforeCursor(int length, int flags) {
        CharSequence value = null;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            mIInputContext.getTextBeforeCursor(length, flags, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mTextBeforeCursor;
                }
            }
            callback.dispose();
        } catch (RemoteException e) {
            return null;
        }
        return value;
    }
    public int getCursorCapsMode(int reqModes) {
        int value = 0;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            mIInputContext.getCursorCapsMode(reqModes, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mCursorCapsMode;
                }
            }
            callback.dispose();
        } catch (RemoteException e) {
            return 0;
        }
        return value;
    }
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        ExtractedText value = null;
        try {
            InputContextCallback callback = InputContextCallback.getInstance();
            mIInputContext.getExtractedText(request, flags, callback.mSeq, callback);
            synchronized (callback) {
                callback.waitForResultLocked();
                if (callback.mHaveValue) {
                    value = callback.mExtractedText;
                }
            }
            callback.dispose();
        } catch (RemoteException e) {
            return null;
        }
        return value;
    }
    public boolean commitText(CharSequence text, int newCursorPosition) {
        try {
            mIInputContext.commitText(text, newCursorPosition);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean commitCompletion(CompletionInfo text) {
        try {
            mIInputContext.commitCompletion(text);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean setSelection(int start, int end) {
        try {
            mIInputContext.setSelection(start, end);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean performEditorAction(int actionCode) {
        try {
            mIInputContext.performEditorAction(actionCode);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean performContextMenuAction(int id) {
        try {
            mIInputContext.performContextMenuAction(id);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        try {
            mIInputContext.setComposingText(text, newCursorPosition);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean finishComposingText() {
        try {
            mIInputContext.finishComposingText();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean beginBatchEdit() {
        try {
            mIInputContext.beginBatchEdit();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean endBatchEdit() {
        try {
            mIInputContext.endBatchEdit();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean sendKeyEvent(KeyEvent event) {
        try {
            mIInputContext.sendKeyEvent(event);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean clearMetaKeyStates(int states) {
        try {
            mIInputContext.clearMetaKeyStates(states);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean deleteSurroundingText(int leftLength, int rightLength) {
        try {
            mIInputContext.deleteSurroundingText(leftLength, rightLength);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean reportFullscreenMode(boolean enabled) {
        try {
            mIInputContext.reportFullscreenMode(enabled);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean performPrivateCommand(String action, Bundle data) {
        try {
            mIInputContext.performPrivateCommand(action, data);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
}
