public abstract class CallTracker extends Handler {
    private static final boolean DBG_POLL = false;
    static final int POLL_DELAY_MSEC = 250;
    protected int pendingOperations;
    protected boolean needsPoll;
    protected Message lastRelevantPoll;
    public CommandsInterface cm;
    protected static final int EVENT_POLL_CALLS_RESULT             = 1;
    protected static final int EVENT_CALL_STATE_CHANGE             = 2;
    protected static final int EVENT_REPOLL_AFTER_DELAY            = 3;
    protected static final int EVENT_OPERATION_COMPLETE            = 4;
    protected static final int EVENT_GET_LAST_CALL_FAIL_CAUSE      = 5;
    protected static final int EVENT_SWITCH_RESULT                 = 8;
    protected static final int EVENT_RADIO_AVAILABLE               = 9;
    protected static final int EVENT_RADIO_NOT_AVAILABLE           = 10;
    protected static final int EVENT_CONFERENCE_RESULT             = 11;
    protected static final int EVENT_SEPARATE_RESULT               = 12;
    protected static final int EVENT_ECT_RESULT                    = 13;
    protected static final int EVENT_EXIT_ECM_RESPONSE_CDMA        = 14;
    protected static final int EVENT_CALL_WAITING_INFO_CDMA        = 15;
    protected static final int EVENT_THREE_WAY_DIAL_L2_RESULT_CDMA = 16;
    protected void pollCallsWhenSafe() {
        needsPoll = true;
        if (checkNoOperationsPending()) {
            lastRelevantPoll = obtainMessage(EVENT_POLL_CALLS_RESULT);
            cm.getCurrentCalls(lastRelevantPoll);
        }
    }
    protected void
    pollCallsAfterDelay() {
        Message msg = obtainMessage();
        msg.what = EVENT_REPOLL_AFTER_DELAY;
        sendMessageDelayed(msg, POLL_DELAY_MSEC);
    }
    protected boolean
    isCommandExceptionRadioNotAvailable(Throwable e) {
        return e != null && e instanceof CommandException
                && ((CommandException)e).getCommandError()
                        == CommandException.Error.RADIO_NOT_AVAILABLE;
    }
    protected abstract void handlePollCalls(AsyncResult ar);
    protected void handleRadioAvailable() {
        pollCallsWhenSafe();
    }
    protected Message
    obtainNoPollCompleteMessage(int what) {
        pendingOperations++;
        lastRelevantPoll = null;
        return obtainMessage(what);
    }
    private boolean
    checkNoOperationsPending() {
        if (DBG_POLL) log("checkNoOperationsPending: pendingOperations=" +
                pendingOperations);
        return pendingOperations == 0;
    }
    public abstract void handleMessage (Message msg);
    protected abstract void log(String msg);
}
