public class Result implements IAnswer<Object>, Serializable {
    private static final long serialVersionUID = 5476251941213917681L;
    private final IAnswer<?> value;
    private final boolean shouldFillInStackTrace;
    private Result(IAnswer<?> value, boolean shouldFillInStackTrace) {
        this.value = value;
        this.shouldFillInStackTrace = shouldFillInStackTrace;
    }
    public static Result createThrowResult(final Throwable throwable) {
        class ThrowingAnswer implements IAnswer<Object>, Serializable {
            private static final long serialVersionUID = -332797751209289222L;
            public Object answer() throws Throwable {
                throw throwable;
            }
            @Override
            public String toString() {
                return "Answer throwing " + throwable;
            }
        }
        return new Result(new ThrowingAnswer(), true);
    }
    public static Result createReturnResult(final Object value) {
        class ReturningAnswer implements IAnswer<Object>, Serializable {
            private static final long serialVersionUID = 6973893913593916866L;
            public Object answer() throws Throwable {
                return value;
            }
            @Override
            public String toString() {
                return "Answer returning " + value;
            }
        }
        return new Result(new ReturningAnswer(), true);
    }
    public static Result createDelegatingResult(final Object value) {
        class DelegatingAnswer implements IAnswer<Object>, Serializable {
            private static final long serialVersionUID = -5699326678580460103L;
            public Object answer() throws Throwable {
                Invocation invocation = LastControl.getCurrentInvocation();
                try {
                    return invocation.getMethod().invoke(value,
                            invocation.getArguments());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Delegation to object ["
                            + String.valueOf(value)
                            + "] is not implementing the mocked method ["
                            + invocation.getMethod() + "]", e);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
            @Override
            public String toString() {
                return "Delegated to " + value;
            }
        }
        return new Result(new DelegatingAnswer(), false);
    }
    public static Result createAnswerResult(IAnswer<?> answer) {
        return new Result(answer, false);
    }
    public Object answer() throws Throwable {
        return value.answer();
    }
    public boolean shouldFillInStackTrace() {
        return shouldFillInStackTrace;
    }
    @Override
    public String toString() {
        return value.toString();
    }
}
