public class Jdk14Logger implements Log, Serializable {
    protected static final Level dummyLevel = Level.FINE;
    public Jdk14Logger(String name) {
        this.name = name;
        logger = getLogger();
    }
    protected transient Logger logger = null;
    protected String name = null;
    private void log( Level level, String msg, Throwable ex ) {
        Logger logger = getLogger();
        if (logger.isLoggable(level)) {
            Throwable dummyException=new Throwable();
            StackTraceElement locations[]=dummyException.getStackTrace();
            String cname="unknown";
            String method="unknown";
            if( locations!=null && locations.length >2 ) {
                StackTraceElement caller=locations[2];
                cname=caller.getClassName();
                method=caller.getMethodName();
            }
            if( ex==null ) {
                logger.logp( level, cname, method, msg );
            } else {
                logger.logp( level, cname, method, msg, ex );
            }
        }
    }
    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }
    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }
    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }
    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }
    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }
    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(name);
        }
        return (logger);
    }
    public void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }
    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }
    public boolean isDebugEnabled() {
        return (getLogger().isLoggable(Level.FINE));
    }
    public boolean isErrorEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }
    public boolean isFatalEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }
    public boolean isInfoEnabled() {
        return (getLogger().isLoggable(Level.INFO));
    }
    public boolean isTraceEnabled() {
        return (getLogger().isLoggable(Level.FINEST));
    }
    public boolean isWarnEnabled() {
        return (getLogger().isLoggable(Level.WARNING));
    }
    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }
    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }
    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }
    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }
}
