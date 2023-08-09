public class ReplayState implements IMocksControlState, Serializable {
    private static final long serialVersionUID = 6314142602251047572L;
    private final IMocksBehavior behavior;
    private final ReentrantLock lock = new ReentrantLock();
    public ReplayState(IMocksBehavior behavior) {
        this.behavior = behavior;
    }
    public Object invoke(Invocation invocation) throws Throwable {
        behavior.checkThreadSafety();
        if (behavior.isThreadSafe()) {
            lock.lock();
            try {
                return invokeInner(invocation);
            }
            finally {
                lock.unlock();
            }
        }
        return invokeInner(invocation);
    }
    private Object invokeInner(Invocation invocation) throws Throwable {
        LastControl.pushCurrentInvocation(invocation);
        try {
            Result result = behavior.addActual(invocation);                
            try {
                return result.answer();
            } catch (Throwable t) {
                if (result.shouldFillInStackTrace()) {
                    throw new ThrowableWrapper(t);
                }
                throw t;
            }
        } finally {
            LastControl.popCurrentInvocation();
        }
    }
    public void verify() {
        behavior.verify();
    }
    public void replay() {
        throwWrappedIllegalStateException();
    }
    public void callback(Runnable runnable) {
        throwWrappedIllegalStateException();
    }
    public void checkOrder(boolean value) {
        throwWrappedIllegalStateException();
    }
    public void makeThreadSafe(boolean threadSafe) {
        throwWrappedIllegalStateException();
    }
    public void checkIsUsedInOneThread(boolean shouldBeUsedInOneThread) {
        throwWrappedIllegalStateException();
    }
    public void andReturn(Object value) {
        throwWrappedIllegalStateException();
    }
    public void andThrow(Throwable throwable) {
        throwWrappedIllegalStateException();
    }
    public void andAnswer(IAnswer<?> answer) {
        throwWrappedIllegalStateException();
    }
    public void andDelegateTo(Object answer) {
        throwWrappedIllegalStateException();
    }
    public void andStubReturn(Object value) {
        throwWrappedIllegalStateException();
    }
    public void andStubThrow(Throwable throwable) {
        throwWrappedIllegalStateException();
    }
    public void andStubAnswer(IAnswer<?> answer) {
        throwWrappedIllegalStateException();
    }
    public void andStubDelegateTo(Object delegateTo) {
        throwWrappedIllegalStateException();
    }
    public void asStub() {
        throwWrappedIllegalStateException();
    }
    public void times(Range range) {
        throwWrappedIllegalStateException();
    }
    @SuppressWarnings("deprecation")
    public void setMatcher(Method method, org.easymock.ArgumentsMatcher matcher) {
        throwWrappedIllegalStateException();
    }
    @SuppressWarnings("deprecation")
    public void setDefaultMatcher(org.easymock.ArgumentsMatcher matcher) {
        throwWrappedIllegalStateException();
    }
    public void setDefaultReturnValue(Object value) {
        throwWrappedIllegalStateException();
    }
    public void setDefaultThrowable(Throwable throwable) {
        throwWrappedIllegalStateException();
    }
    public void setDefaultVoidCallable() {
        throwWrappedIllegalStateException();
    }
    private void throwWrappedIllegalStateException() {
        throw new RuntimeExceptionWrapper(new IllegalStateException(
                "This method must not be called in replay state."));
    }
    public void assertRecordState() {
        throwWrappedIllegalStateException();
    }
}
