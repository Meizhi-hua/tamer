final class CompletedFuture<V> implements Future<V> {
    private final V result;
    private final Throwable exc;
    private CompletedFuture(V result, Throwable exc) {
        this.result = result;
        this.exc = exc;
    }
    static <V> CompletedFuture<V> withResult(V result) {
        return new CompletedFuture<V>(result, null);
    }
    static <V> CompletedFuture<V> withFailure(Throwable exc) {
        if (!(exc instanceof IOException) && !(exc instanceof SecurityException))
            exc = new IOException(exc);
        return new CompletedFuture<V>(null, exc);
    }
    static <V> CompletedFuture<V> withResult(V result, Throwable exc) {
        if (exc == null) {
            return withResult(result);
        } else {
            return withFailure(exc);
        }
    }
    @Override
    public V get() throws ExecutionException {
        if (exc != null)
            throw new ExecutionException(exc);
        return result;
    }
    @Override
    public V get(long timeout, TimeUnit unit) throws ExecutionException {
        if (unit == null)
            throw new NullPointerException();
        if (exc != null)
            throw new ExecutionException(exc);
        return result;
    }
    @Override
    public boolean isCancelled() {
        return false;
    }
    @Override
    public boolean isDone() {
        return true;
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
}
