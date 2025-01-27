public final class SystemClock {
    private SystemClock() {
    }
    public static void sleep(long ms)
    {
        long start = uptimeMillis();
        long duration = ms;
        boolean interrupted = false;
        do {
            try {
                Thread.sleep(duration);
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
            duration = start + ms - uptimeMillis();
        } while (duration > 0);
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
    native public static boolean setCurrentTimeMillis(long millis);
    native public static long uptimeMillis();
    native public static long elapsedRealtime();
    public static native long currentThreadTimeMillis();
}
