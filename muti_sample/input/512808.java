public class ExecutorCompletionService<V> implements CompletionService<V> {
    private final Executor executor;
    private final AbstractExecutorService aes;
    private final BlockingQueue<Future<V>> completionQueue;
    private class QueueingFuture extends FutureTask<Void> {
        QueueingFuture(FutureTask<V> task) {
            super(task, null);
            this.task = task;
        }
        protected void done() { completionQueue.add(task); }
        private final Future<V> task;
    }
    private FutureTask<V> newTaskFor(Callable<V> task) {
        return new FutureTask<V>(task);
    }
    private FutureTask<V> newTaskFor(Runnable task, V result) {
        return new FutureTask<V>(task, result);
    }
    public ExecutorCompletionService(Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ?
            (AbstractExecutorService) executor : null;
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }
    public ExecutorCompletionService(Executor executor,
                                     BlockingQueue<Future<V>> completionQueue) {
        if (executor == null || completionQueue == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ?
            (AbstractExecutorService) executor : null;
        this.completionQueue = completionQueue;
    }
    public Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        FutureTask<V> f = newTaskFor(task);
        executor.execute(new QueueingFuture(f));
        return f;
    }
    public Future<V> submit(Runnable task, V result) {
        if (task == null) throw new NullPointerException();
        FutureTask<V> f = newTaskFor(task, result);
        executor.execute(new QueueingFuture(f));
        return f;
    }
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }
    public Future<V> poll() {
        return completionQueue.poll();
    }
    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }
}
