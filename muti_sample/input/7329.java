public class PeerEvent extends InvocationEvent {
    public static final long PRIORITY_EVENT = 0x01;
    public static final long ULTIMATE_PRIORITY_EVENT = 0x02;
    public static final long LOW_PRIORITY_EVENT = 0x04;
    private long flags;
    public PeerEvent(Object source, Runnable runnable, long flags) {
        this(source, runnable, null, false, flags);
    }
    public PeerEvent(Object source, Runnable runnable, Object notifier,
                      boolean catchExceptions, long flags) {
        super(source, runnable, notifier, catchExceptions);
        this.flags = flags;
    }
    public long getFlags() {
        return flags;
    }
    public PeerEvent coalesceEvents(PeerEvent newEvent) {
        return null;
    }
}
