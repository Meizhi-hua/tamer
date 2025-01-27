public class ClassLogger {
    private static final boolean ok;
    private final String className;
    private final Logger logger;
    static {
        boolean loaded = false;
        try {
            Class<?> c = java.util.logging.Logger.class;
            loaded = true;
        } catch (Error e) {
        }
        ok = loaded;
    }
    public ClassLogger(String subsystem, String className) {
        if (ok)
            logger = Logger.getLogger(subsystem);
        else
            logger = null;
        this.className = className;
    }
    public final boolean traceOn() {
        return finerOn();
    }
    public final boolean debugOn() {
        return finestOn();
    }
    public final boolean warningOn() {
        return ok && logger.isLoggable(java.util.logging.Level.WARNING);
    }
    public final boolean infoOn() {
        return ok && logger.isLoggable(java.util.logging.Level.INFO);
    }
    public final boolean configOn() {
        return ok && logger.isLoggable(java.util.logging.Level.CONFIG);
    }
    public final boolean fineOn() {
        return ok && logger.isLoggable(java.util.logging.Level.FINE);
    }
    public final boolean finerOn() {
        return ok && logger.isLoggable(java.util.logging.Level.FINER);
    }
    public final boolean finestOn() {
        return ok && logger.isLoggable(java.util.logging.Level.FINEST);
    }
    public final void debug(String func, String msg) {
        finest(func,msg);
    }
    public final void debug(String func, Throwable t) {
        finest(func,t);
    }
    public final void debug(String func, String msg, Throwable t) {
        finest(func,msg,t);
    }
    public final void trace(String func, String msg) {
        finer(func,msg);
    }
    public final void trace(String func, Throwable t) {
        finer(func,t);
    }
    public final void trace(String func, String msg, Throwable t) {
        finer(func,msg,t);
    }
    public final void error(String func, String msg) {
        severe(func,msg);
    }
    public final void error(String func, Throwable t) {
        severe(func,t);
    }
    public final void error(String func, String msg, Throwable t) {
        severe(func,msg,t);
    }
    public final void finest(String func, String msg) {
        if (ok)
            logger.logp(java.util.logging.Level.FINEST, className, func, msg);
    }
    public final void finest(String func, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.FINEST, className, func,
                        t.toString(), t);
    }
    public final void finest(String func, String msg, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.FINEST, className, func, msg,
                        t);
    }
    public final void finer(String func, String msg) {
        if (ok)
            logger.logp(java.util.logging.Level.FINER, className, func, msg);
    }
    public final void finer(String func, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.FINER, className, func,
                        t.toString(), t);
    }
    public final void finer(String func, String msg, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.FINER, className, func, msg,t);
    }
    public final void fine(String func, String msg) {
        if (ok)
            logger.logp(java.util.logging.Level.FINE, className, func, msg);
    }
    public final void fine(String func, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.FINE, className, func,
                        t.toString(), t);
    }
    public final void fine(String func, String msg, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.FINE, className, func, msg,
                        t);
    }
    public final void config(String func, String msg) {
        if (ok)
            logger.logp(java.util.logging.Level.CONFIG, className, func, msg);
    }
    public final void config(String func, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.CONFIG, className, func,
                        t.toString(), t);
    }
    public final void config(String func, String msg, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.CONFIG, className, func, msg,
                        t);
    }
    public final void info(String func, String msg) {
        if (ok)
            logger.logp(java.util.logging.Level.INFO, className, func, msg);
    }
    public final void info(String func, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.INFO, className, func,
                        t.toString(), t);
    }
    public final void info(String func, String msg, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.INFO, className, func, msg,
                        t);
    }
    public final void warning(String func, String msg) {
        if (ok)
            logger.logp(java.util.logging.Level.WARNING, className, func, msg);
    }
    public final void warning(String func, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.WARNING, className, func,
                        t.toString(), t);
    }
    public final void warning(String func, String msg, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.WARNING, className, func, msg,
                        t);
    }
    public final void severe(String func, String msg) {
        if (ok)
            logger.logp(java.util.logging.Level.SEVERE, className, func, msg);
    }
    public final void severe(String func, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.SEVERE, className, func,
                        t.toString(), t);
    }
    public final void severe(String func, String msg, Throwable t) {
        if (ok)
            logger.logp(java.util.logging.Level.SEVERE, className, func, msg,
                        t);
    }
}
