public class Logger {
    private static final Handler emptyHandlers[] = new Handler[0];
    private static final int offValue = Level.OFF.intValue();
    private LogManager manager;
    private String name;
    private final CopyOnWriteArrayList<Handler> handlers =
        new CopyOnWriteArrayList<>();
    private String resourceBundleName;
    private volatile boolean useParentHandlers = true;
    private volatile Filter filter;
    private boolean anonymous;
    private ResourceBundle catalog;     
    private String catalogName;         
    private Locale catalogLocale;       
    private static Object treeLock = new Object();
    private volatile Logger parent;    
    private ArrayList<LogManager.LoggerWeakRef> kids;   
    private volatile Level levelObject;
    private volatile int levelValue;  
    public static final String GLOBAL_LOGGER_NAME = "global";
    public static final Logger getGlobal() {
        return global;
    }
    @Deprecated
    public static final Logger global = new Logger(GLOBAL_LOGGER_NAME);
    protected Logger(String name, String resourceBundleName) {
        this.manager = LogManager.getLogManager();
        if (resourceBundleName != null) {
            setupResourceInfo(resourceBundleName);
        }
        this.name = name;
        levelValue = Level.INFO.intValue();
    }
    private Logger(String name) {
        this.name = name;
        levelValue = Level.INFO.intValue();
    }
    void setLogManager(LogManager manager) {
        this.manager = manager;
    }
    private void checkAccess() throws SecurityException {
        if (!anonymous) {
            if (manager == null) {
                manager = LogManager.getLogManager();
            }
            manager.checkAccess();
        }
    }
    public static Logger getLogger(String name) {
        LogManager manager = LogManager.getLogManager();
        return manager.demandLogger(name);
    }
    public static Logger getLogger(String name, String resourceBundleName) {
        LogManager manager = LogManager.getLogManager();
        Logger result = manager.demandLogger(name);
        if (result.resourceBundleName == null) {
            result.setupResourceInfo(resourceBundleName);
        } else if (!result.resourceBundleName.equals(resourceBundleName)) {
            throw new IllegalArgumentException(result.resourceBundleName +
                                " != " + resourceBundleName);
        }
        return result;
    }
    public static Logger getAnonymousLogger() {
        return getAnonymousLogger(null);
    }
    public static Logger getAnonymousLogger(String resourceBundleName) {
        LogManager manager = LogManager.getLogManager();
        manager.drainLoggerRefQueueBounded();
        Logger result = new Logger(null, resourceBundleName);
        result.anonymous = true;
        Logger root = manager.getLogger("");
        result.doSetParent(root);
        return result;
    }
    public ResourceBundle getResourceBundle() {
        return findResourceBundle(getResourceBundleName());
    }
    public String getResourceBundleName() {
        return resourceBundleName;
    }
    public void setFilter(Filter newFilter) throws SecurityException {
        checkAccess();
        filter = newFilter;
    }
    public Filter getFilter() {
        return filter;
    }
    public void log(LogRecord record) {
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return;
        }
        Filter theFilter = filter;
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return;
        }
        Logger logger = this;
        while (logger != null) {
            for (Handler handler : logger.getHandlers()) {
                handler.publish(record);
            }
            if (!logger.getUseParentHandlers()) {
                break;
            }
            logger = logger.getParent();
        }
    }
    private void doLog(LogRecord lr) {
        lr.setLoggerName(name);
        String ebname = getEffectiveResourceBundleName();
        if (ebname != null) {
            lr.setResourceBundleName(ebname);
            lr.setResourceBundle(findResourceBundle(ebname));
        }
        log(lr);
    }
    public void log(Level level, String msg) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        doLog(lr);
    }
    public void log(Level level, String msg, Object param1) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        Object params[] = { param1 };
        lr.setParameters(params);
        doLog(lr);
    }
    public void log(Level level, String msg, Object params[]) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setParameters(params);
        doLog(lr);
    }
    public void log(Level level, String msg, Throwable thrown) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setThrown(thrown);
        doLog(lr);
    }
    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        doLog(lr);
    }
    public void logp(Level level, String sourceClass, String sourceMethod,
                                                String msg, Object param1) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        Object params[] = { param1 };
        lr.setParameters(params);
        doLog(lr);
    }
    public void logp(Level level, String sourceClass, String sourceMethod,
                                                String msg, Object params[]) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setParameters(params);
        doLog(lr);
    }
    public void logp(Level level, String sourceClass, String sourceMethod,
                                                        String msg, Throwable thrown) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr);
    }
    private void doLog(LogRecord lr, String rbname) {
        lr.setLoggerName(name);
        if (rbname != null) {
            lr.setResourceBundleName(rbname);
            lr.setResourceBundle(findResourceBundle(rbname));
        }
        log(lr);
    }
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                String bundleName, String msg) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        doLog(lr, bundleName);
    }
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                String bundleName, String msg, Object param1) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        Object params[] = { param1 };
        lr.setParameters(params);
        doLog(lr, bundleName);
    }
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                String bundleName, String msg, Object params[]) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setParameters(params);
        doLog(lr, bundleName);
    }
    public void logrb(Level level, String sourceClass, String sourceMethod,
                                        String bundleName, String msg, Throwable thrown) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr, bundleName);
    }
    public void entering(String sourceClass, String sourceMethod) {
        if (Level.FINER.intValue() < levelValue) {
            return;
        }
        logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
    }
    public void entering(String sourceClass, String sourceMethod, Object param1) {
        if (Level.FINER.intValue() < levelValue) {
            return;
        }
        Object params[] = { param1 };
        logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", params);
    }
    public void entering(String sourceClass, String sourceMethod, Object params[]) {
        if (Level.FINER.intValue() < levelValue) {
            return;
        }
        String msg = "ENTRY";
        if (params == null ) {
           logp(Level.FINER, sourceClass, sourceMethod, msg);
           return;
        }
        for (int i = 0; i < params.length; i++) {
            msg = msg + " {" + i + "}";
        }
        logp(Level.FINER, sourceClass, sourceMethod, msg, params);
    }
    public void exiting(String sourceClass, String sourceMethod) {
        if (Level.FINER.intValue() < levelValue) {
            return;
        }
        logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
    }
    public void exiting(String sourceClass, String sourceMethod, Object result) {
        if (Level.FINER.intValue() < levelValue) {
            return;
        }
        Object params[] = { result };
        logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
    }
    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        if (Level.FINER.intValue() < levelValue || levelValue == offValue ) {
            return;
        }
        LogRecord lr = new LogRecord(Level.FINER, "THROW");
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr);
    }
    public void severe(String msg) {
        if (Level.SEVERE.intValue() < levelValue) {
            return;
        }
        log(Level.SEVERE, msg);
    }
    public void warning(String msg) {
        if (Level.WARNING.intValue() < levelValue) {
            return;
        }
        log(Level.WARNING, msg);
    }
    public void info(String msg) {
        if (Level.INFO.intValue() < levelValue) {
            return;
        }
        log(Level.INFO, msg);
    }
    public void config(String msg) {
        if (Level.CONFIG.intValue() < levelValue) {
            return;
        }
        log(Level.CONFIG, msg);
    }
    public void fine(String msg) {
        if (Level.FINE.intValue() < levelValue) {
            return;
        }
        log(Level.FINE, msg);
    }
    public void finer(String msg) {
        if (Level.FINER.intValue() < levelValue) {
            return;
        }
        log(Level.FINER, msg);
    }
    public void finest(String msg) {
        if (Level.FINEST.intValue() < levelValue) {
            return;
        }
        log(Level.FINEST, msg);
    }
    public void setLevel(Level newLevel) throws SecurityException {
        checkAccess();
        synchronized (treeLock) {
            levelObject = newLevel;
            updateEffectiveLevel();
        }
    }
    public Level getLevel() {
        return levelObject;
    }
    public boolean isLoggable(Level level) {
        if (level.intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        return true;
    }
    public String getName() {
        return name;
    }
    public void addHandler(Handler handler) throws SecurityException {
        handler.getClass();
        checkAccess();
        handlers.add(handler);
    }
    public void removeHandler(Handler handler) throws SecurityException {
        checkAccess();
        if (handler == null) {
            return;
        }
        handlers.remove(handler);
    }
    public Handler[] getHandlers() {
        return handlers.toArray(emptyHandlers);
    }
    public void setUseParentHandlers(boolean useParentHandlers) {
        checkAccess();
        this.useParentHandlers = useParentHandlers;
    }
    public boolean getUseParentHandlers() {
        return useParentHandlers;
    }
    private synchronized ResourceBundle findResourceBundle(String name) {
        if (name == null) {
            return null;
        }
        Locale currentLocale = Locale.getDefault();
        if (catalog != null && currentLocale == catalogLocale
                                        && name == catalogName) {
            return catalog;
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        try {
            catalog = ResourceBundle.getBundle(name, currentLocale, cl);
            catalogName = name;
            catalogLocale = currentLocale;
            return catalog;
        } catch (MissingResourceException ex) {
        }
        for (int ix = 0; ; ix++) {
            Class clz = sun.reflect.Reflection.getCallerClass(ix);
            if (clz == null) {
                break;
            }
            ClassLoader cl2 = clz.getClassLoader();
            if (cl2 == null) {
                cl2 = ClassLoader.getSystemClassLoader();
            }
            if (cl == cl2) {
                continue;
            }
            cl = cl2;
            try {
                catalog = ResourceBundle.getBundle(name, currentLocale, cl);
                catalogName = name;
                catalogLocale = currentLocale;
                return catalog;
            } catch (MissingResourceException ex) {
            }
        }
        if (name.equals(catalogName)) {
            return catalog;
        }
        return null;
    }
    private synchronized void setupResourceInfo(String name) {
        if (name == null) {
            return;
        }
        ResourceBundle rb = findResourceBundle(name);
        if (rb == null) {
            throw new MissingResourceException("Can't find " + name + " bundle", name, "");
        }
        resourceBundleName = name;
    }
    public Logger getParent() {
        return parent;
    }
    public void setParent(Logger parent) {
        if (parent == null) {
            throw new NullPointerException();
        }
        manager.checkAccess();
        doSetParent(parent);
    }
    private void doSetParent(Logger newParent) {
        synchronized (treeLock) {
            LogManager.LoggerWeakRef ref = null;
            if (parent != null) {
                for (Iterator<LogManager.LoggerWeakRef> iter = parent.kids.iterator(); iter.hasNext(); ) {
                    ref = iter.next();
                    Logger kid =  ref.get();
                    if (kid == this) {
                        iter.remove();
                        break;
                    } else {
                        ref = null;
                    }
                }
            }
            parent = newParent;
            if (parent.kids == null) {
                parent.kids = new ArrayList<>(2);
            }
            if (ref == null) {
                ref = manager.new LoggerWeakRef(this);
            }
            ref.setParentRef(new WeakReference<Logger>(parent));
            parent.kids.add(ref);
            updateEffectiveLevel();
        }
    }
    final void removeChildLogger(LogManager.LoggerWeakRef child) {
        synchronized (treeLock) {
            for (Iterator<LogManager.LoggerWeakRef> iter = kids.iterator(); iter.hasNext(); ) {
                LogManager.LoggerWeakRef ref = iter.next();
                if (ref == child) {
                    iter.remove();
                    return;
                }
            }
        }
    }
    private void updateEffectiveLevel() {
        int newLevelValue;
        if (levelObject != null) {
            newLevelValue = levelObject.intValue();
        } else {
            if (parent != null) {
                newLevelValue = parent.levelValue;
            } else {
                newLevelValue = Level.INFO.intValue();
            }
        }
        if (levelValue == newLevelValue) {
            return;
        }
        levelValue = newLevelValue;
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                LogManager.LoggerWeakRef ref = kids.get(i);
                Logger kid =  ref.get();
                if (kid != null) {
                    kid.updateEffectiveLevel();
                }
            }
        }
    }
    private String getEffectiveResourceBundleName() {
        Logger target = this;
        while (target != null) {
            String rbn = target.getResourceBundleName();
            if (rbn != null) {
                return rbn;
            }
            target = target.getParent();
        }
        return null;
    }
}
