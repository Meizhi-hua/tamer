public class LogFactoryImpl extends LogFactory {
    private static final String LOGGING_IMPL_LOG4J_LOGGER = "org.apache.commons.logging.impl.Log4JLogger";
    private static final String LOGGING_IMPL_JDK14_LOGGER = "org.apache.commons.logging.impl.Jdk14Logger";
    private static final String LOGGING_IMPL_LUMBERJACK_LOGGER = "org.apache.commons.logging.impl.Jdk13LumberjackLogger";
    private static final String LOGGING_IMPL_SIMPLE_LOGGER = "org.apache.commons.logging.impl.SimpleLog";
    private static final String PKG_IMPL="org.apache.commons.logging.impl.";
    private static final int PKG_LEN = PKG_IMPL.length();
    public LogFactoryImpl() {
        super();
        initDiagnostics();  
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Instance created.");
        }
    }
    public static final String LOG_PROPERTY =
        "org.apache.commons.logging.Log";
    protected static final String LOG_PROPERTY_OLD =
        "org.apache.commons.logging.log";
    public static final String ALLOW_FLAWED_CONTEXT_PROPERTY = 
        "org.apache.commons.logging.Log.allowFlawedContext";
    public static final String ALLOW_FLAWED_DISCOVERY_PROPERTY = 
        "org.apache.commons.logging.Log.allowFlawedDiscovery";
    public static final String ALLOW_FLAWED_HIERARCHY_PROPERTY = 
        "org.apache.commons.logging.Log.allowFlawedHierarchy";
    private static final String[] classesToDiscover = {
            LOGGING_IMPL_LOG4J_LOGGER,
            "org.apache.commons.logging.impl.Jdk14Logger",
            "org.apache.commons.logging.impl.Jdk13LumberjackLogger",
            "org.apache.commons.logging.impl.SimpleLog"
    };
    private boolean useTCCL = true;
    private String diagnosticPrefix;
    protected Hashtable attributes = new Hashtable();
    protected Hashtable instances = new Hashtable();
    private String logClassName;
    protected Constructor logConstructor = null;
    protected Class logConstructorSignature[] =
    { java.lang.String.class };
    protected Method logMethod = null;
    protected Class logMethodSignature[] =
    { LogFactory.class };
    private boolean allowFlawedContext;
    private boolean allowFlawedDiscovery;
    private boolean allowFlawedHierarchy;
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }
    public String[] getAttributeNames() {
        Vector names = new Vector();
        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement((String) keys.nextElement());
        }
        String results[] = new String[names.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = (String) names.elementAt(i);
        }
        return (results);
    }
    public Log getInstance(Class clazz) throws LogConfigurationException {
        return (getInstance(clazz.getName()));
    }
    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log) instances.get(name);
        if (instance == null) {
            instance = newInstance(name);
            instances.put(name, instance);
        }
        return (instance);
    }
    public void release() {
        logDiagnostic("Releasing all known loggers");
        instances.clear();
    }
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    public void setAttribute(String name, Object value) {
        if (logConstructor != null) {
            logDiagnostic("setAttribute: call too late; configuration already performed.");
        }
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
        if (name.equals(TCCL_KEY)) {
            useTCCL = Boolean.valueOf(value.toString()).booleanValue();
        }
    }
    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return LogFactory.getContextClassLoader();
    }
    protected static boolean isDiagnosticsEnabled() {
        return LogFactory.isDiagnosticsEnabled();
    }
    protected static ClassLoader getClassLoader(Class clazz) {
        return LogFactory.getClassLoader(clazz);
    }
    private void initDiagnostics() {
        Class clazz = this.getClass();
        ClassLoader classLoader = getClassLoader(clazz);
        String classLoaderName;
        try {
            if (classLoader == null) {
                classLoaderName = "BOOTLOADER";
            } else {
                classLoaderName = objectId(classLoader);
            }
        } catch(SecurityException e) {
            classLoaderName = "UNKNOWN";
        }
        diagnosticPrefix = "[LogFactoryImpl@" + System.identityHashCode(this) + " from " + classLoaderName + "] ";
    }
    protected void logDiagnostic(String msg) {
        if (isDiagnosticsEnabled()) {
            logRawDiagnostic(diagnosticPrefix + msg);
        }
    }
    protected String getLogClassName() {
        if (logClassName == null) {
            discoverLogImplementation(getClass().getName());
        }
        return logClassName;
    }
    protected Constructor getLogConstructor()
        throws LogConfigurationException {
        if (logConstructor == null) {
            discoverLogImplementation(getClass().getName());
        }
        return logConstructor;
    }
    protected boolean isJdk13LumberjackAvailable() {
        return isLogLibraryAvailable(
                "Jdk13Lumberjack",
                "org.apache.commons.logging.impl.Jdk13LumberjackLogger");
    }
    protected boolean isJdk14Available() {
        return isLogLibraryAvailable(
                "Jdk14",
                "org.apache.commons.logging.impl.Jdk14Logger");
    }
    protected boolean isLog4JAvailable() {
        return isLogLibraryAvailable(
                "Log4J",
                LOGGING_IMPL_LOG4J_LOGGER);
    }
    protected Log newInstance(String name) throws LogConfigurationException {
        Log instance = null;
        try {
            if (logConstructor == null) {
                instance = discoverLogImplementation(name);
            }
            else {
                Object params[] = { name };
                instance = (Log) logConstructor.newInstance(params);
            }
            if (logMethod != null) {
                Object params[] = { this };
                logMethod.invoke(instance, params);
            }
            return (instance);
        } catch (LogConfigurationException lce) {
            throw (LogConfigurationException) lce;
        } catch (InvocationTargetException e) {
            Throwable c = e.getTargetException();
            if (c != null) {
                throw new LogConfigurationException(c);
            } else {
                throw new LogConfigurationException(e);
            }
        } catch (Throwable t) {
            throw new LogConfigurationException(t);
        }
    }
    private boolean isLogLibraryAvailable(String name, String classname) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Checking for '" + name + "'.");
        }
        try {
            Log log = createLogFromClass(
                        classname, 
                        this.getClass().getName(), 
                        false);
            if (log == null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("Did not find '" + name + "'.");
                }
                return false;
            } else {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("Found '" + name + "'.");
                }
                return true;
            }
        } catch(LogConfigurationException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Logging system '" + name + "' is available but not useable.");
            }
            return false;
        }
    }
    private String getConfigurationValue(String property) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] Trying to get configuration for item " + property);
        }
        Object valueObj =  getAttribute(property);
        if (valueObj != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] Found LogFactory attribute [" + valueObj + "] for " + property);
            }
            return valueObj.toString();
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] No LogFactory attribute found for " + property);
        }
        try {
            String value = System.getProperty(property);
            if (value != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[ENV] Found system property [" + value + "] for " + property);
                }
                return value;
            }
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] No system property found for property " + property);
            }
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] Security prevented reading system property " + property);
            }
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] No configuration defined for item " + property);
        }
        return null;
    }
    private boolean getBooleanConfiguration(String key, boolean dflt) {
        String val = getConfigurationValue(key);
        if (val == null)
            return dflt;
        return Boolean.valueOf(val).booleanValue();
    }
    private void initConfiguration() {
        allowFlawedContext = getBooleanConfiguration(ALLOW_FLAWED_CONTEXT_PROPERTY, true);
        allowFlawedDiscovery = getBooleanConfiguration(ALLOW_FLAWED_DISCOVERY_PROPERTY, true);
        allowFlawedHierarchy = getBooleanConfiguration(ALLOW_FLAWED_HIERARCHY_PROPERTY, true);
    }
    private Log discoverLogImplementation(String logCategory)
    throws LogConfigurationException
    {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Discovering a Log implementation...");
        }
        initConfiguration();
        Log result = null;
        String specifiedLogClassName = findUserSpecifiedLogClassName();
        if (specifiedLogClassName != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Attempting to load user-specified log class '" + 
                    specifiedLogClassName + "'...");
            }
            result = createLogFromClass(specifiedLogClassName,
                                        logCategory,
                                        true);
            if (result == null) {
                StringBuffer messageBuffer =  new StringBuffer("User-specified log class '");
                messageBuffer.append(specifiedLogClassName);
                messageBuffer.append("' cannot be found or is not useable.");
                if (specifiedLogClassName != null) {
                    informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_LOG4J_LOGGER);
                    informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_JDK14_LOGGER);
                    informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_LUMBERJACK_LOGGER);
                    informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_SIMPLE_LOGGER);
                }
                throw new LogConfigurationException(messageBuffer.toString());
            }
            return result;
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic(
                "No user-specified Log implementation; performing discovery" +
            	" using the standard supported logging implementations...");
        }
        for(int i=0; (i<classesToDiscover.length) && (result == null); ++i) {
            result = createLogFromClass(classesToDiscover[i], logCategory, true);
        }
        if (result == null) {
            throw new LogConfigurationException
                        ("No suitable Log implementation");
        }
        return result;        
    }
    private void informUponSimilarName(final StringBuffer messageBuffer, final String name, 
            final String candidate) {
        if (name.equals(candidate)) {
            return;
        }
        if (name.regionMatches(true, 0, candidate, 0, PKG_LEN + 5)) {
            messageBuffer.append(" Did you mean '");
            messageBuffer.append(candidate);
            messageBuffer.append("'?");
        }
    }
    private String findUserSpecifiedLogClassName()
    {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Trying to get log class from attribute '" + LOG_PROPERTY + "'");
        }
        String specifiedClass = (String) getAttribute(LOG_PROPERTY);
        if (specifiedClass == null) { 
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from attribute '" + 
                              LOG_PROPERTY_OLD + "'");
            }
            specifiedClass = (String) getAttribute(LOG_PROPERTY_OLD);
        }
        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property '" + 
                          LOG_PROPERTY + "'");
            }
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("No access allowed to system property '" + 
                        LOG_PROPERTY + "' - " + e.getMessage());
                }
            }
        }
        if (specifiedClass == null) { 
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property '" + 
                          LOG_PROPERTY_OLD + "'");
            }
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY_OLD);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("No access allowed to system property '" + 
                        LOG_PROPERTY_OLD + "' - " + e.getMessage());
                }
            }
        }
        if (specifiedClass != null) {
            specifiedClass = specifiedClass.trim();
        }
        return specifiedClass;
    }
    private Log createLogFromClass(String logAdapterClassName,
                                   String logCategory,
                                   boolean affectState) 
            throws LogConfigurationException {       
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Attempting to instantiate '" + logAdapterClassName + "'");
        }
        Object[] params = { logCategory };
        Log logAdapter = null;
        Constructor constructor = null;
        Class logAdapterClass = null;
        ClassLoader currentCL = getBaseClassLoader();
        for(;;) {
            logDiagnostic(
                    "Trying to load '"
                    + logAdapterClassName
                    + "' from classloader "
                    + objectId(currentCL));
            try {
                if (isDiagnosticsEnabled()) {
                    URL url;
                    String resourceName = logAdapterClassName.replace('.', '/') + ".class";
                    if (currentCL != null) {
                        url = currentCL.getResource(resourceName );
                    } else {
                        url = ClassLoader.getSystemResource(resourceName + ".class");
                    }
                    if (url == null) {
                        logDiagnostic("Class '" + logAdapterClassName + "' [" + resourceName + "] cannot be found.");
                    } else {
                        logDiagnostic("Class '" + logAdapterClassName + "' was found at '" + url + "'");
                    }
                }
                Class c = null;
                try {
                    c = Class.forName(logAdapterClassName, true, currentCL);
                } catch (ClassNotFoundException originalClassNotFoundException) {
                    String msg = "" + originalClassNotFoundException.getMessage();
                    logDiagnostic(
                        "The log adapter '"
                        + logAdapterClassName
                        + "' is not available via classloader " 
                        + objectId(currentCL)
                        + ": "
                        + msg.trim());
                    try {
                        c = Class.forName(logAdapterClassName);
                    } catch (ClassNotFoundException secondaryClassNotFoundException) {
                        msg = "" + secondaryClassNotFoundException.getMessage();
                        logDiagnostic(
                            "The log adapter '"
                            + logAdapterClassName
                            + "' is not available via the LogFactoryImpl class classloader: "
                            + msg.trim());
                        break;
                    }
                }
                constructor = c.getConstructor(logConstructorSignature);
                Object o = constructor.newInstance(params);
                if (o instanceof Log) {
                    logAdapterClass = c;
                    logAdapter = (Log) o;
                    break;
                }
                handleFlawedHierarchy(currentCL, c);
            } catch (NoClassDefFoundError e) {
                String msg = "" + e.getMessage();
                logDiagnostic(
                    "The log adapter '"
                    + logAdapterClassName
                    + "' is missing dependencies when loaded via classloader "
                    + objectId(currentCL)
                    + ": "
                    + msg.trim());
                break;
            } catch (ExceptionInInitializerError e) {
                String msg = "" + e.getMessage();
                logDiagnostic(
                    "The log adapter '"
                    + logAdapterClassName
                    + "' is unable to initialize itself when loaded via classloader "
                    + objectId(currentCL)
                    + ": "
                    + msg.trim());
                break;
            } catch(LogConfigurationException e) {
                throw e;
            } catch(Throwable t) {
                handleFlawedDiscovery(logAdapterClassName, currentCL, t);
            }
            if (currentCL == null) {
                break;
            }
            currentCL = currentCL.getParent();
        }
        if ((logAdapter != null) && affectState) {
            this.logClassName   = logAdapterClassName;
            this.logConstructor = constructor;
            try {
                this.logMethod = logAdapterClass.getMethod("setLogFactory",
                                               logMethodSignature);
                logDiagnostic("Found method setLogFactory(LogFactory) in '" 
                              + logAdapterClassName + "'");
            } catch (Throwable t) {
                this.logMethod = null;
                logDiagnostic(
                    "[INFO] '" + logAdapterClassName 
                    + "' from classloader " + objectId(currentCL)
                    + " does not declare optional method "
                    + "setLogFactory(LogFactory)");
            }
            logDiagnostic(
                "Log adapter '" + logAdapterClassName 
                + "' from classloader " + objectId(logAdapterClass.getClassLoader())
                + " has been selected for use.");
        }
        return logAdapter;
    }
    private ClassLoader getBaseClassLoader() throws LogConfigurationException {
        ClassLoader thisClassLoader = getClassLoader(LogFactoryImpl.class);
        if (useTCCL == false) {
            return thisClassLoader;
        }
        ClassLoader contextClassLoader = getContextClassLoader();
        ClassLoader baseClassLoader = getLowestClassLoader(
                contextClassLoader, thisClassLoader);
        if (baseClassLoader == null) {
           if (allowFlawedContext) {   
              if (isDiagnosticsEnabled()) {
                   logDiagnostic(
                           "[WARNING] the context classloader is not part of a"
                           + " parent-child relationship with the classloader that"
                           + " loaded LogFactoryImpl.");
              }
              return contextClassLoader;
           }
           else {
            throw new LogConfigurationException(
                "Bad classloader hierarchy; LogFactoryImpl was loaded via"
                + " a classloader that is not related to the current context"
                + " classloader.");
           }           
        }
        if (baseClassLoader != contextClassLoader) {
            if (allowFlawedContext) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(
                            "Warning: the context classloader is an ancestor of the"
                            + " classloader that loaded LogFactoryImpl; it should be"
                            + " the same or a descendant. The application using"
                            + " commons-logging should ensure the context classloader"
                            + " is used correctly.");
                }
            } else {
                throw new LogConfigurationException(
                        "Bad classloader hierarchy; LogFactoryImpl was loaded via"
                        + " a classloader that is not related to the current context"
                        + " classloader."); 
            }
        }
        return baseClassLoader;
    }
    private ClassLoader getLowestClassLoader(ClassLoader c1, ClassLoader c2) {
        if (c1 == null)
            return c2;
        if (c2 == null)
            return c1;
        ClassLoader current;
        current = c1;
        while (current != null) {
            if (current == c2)
                return c1;
            current = current.getParent();
        }
        current = c2;
        while (current != null) {
            if (current == c1)
                return c2;
            current = current.getParent();
        }
        return null;
    }
    private void handleFlawedDiscovery(String logAdapterClassName,
                                       ClassLoader classLoader,
                                       Throwable discoveryFlaw) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Could not instantiate Log '"
                      + logAdapterClassName + "' -- "
                      + discoveryFlaw.getClass().getName() + ": "
                      + discoveryFlaw.getLocalizedMessage());       
        }
        if (!allowFlawedDiscovery) {
            throw new LogConfigurationException(discoveryFlaw);
        }
    }
    private void handleFlawedHierarchy(ClassLoader badClassLoader, Class badClass)
    throws LogConfigurationException {
        boolean implementsLog = false;
        String logInterfaceName = Log.class.getName();
        Class interfaces[] = badClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (logInterfaceName.equals(interfaces[i].getName())) {
                implementsLog = true;
                break;
            }
        }
        if (implementsLog) {
            if (isDiagnosticsEnabled()) {
                try {
                    ClassLoader logInterfaceClassLoader = getClassLoader(Log.class);
                    logDiagnostic(
                        "Class '" + badClass.getName()
                        + "' was found in classloader " 
                        + objectId(badClassLoader)
                        + ". It is bound to a Log interface which is not"
                        + " the one loaded from classloader "
                        + objectId(logInterfaceClassLoader));
                } catch (Throwable t) {
                    logDiagnostic(
                        "Error while trying to output diagnostics about"
                        + " bad class '" + badClass + "'");
                }
            }
            if (!allowFlawedHierarchy) {
                StringBuffer msg = new StringBuffer();
                msg.append("Terminating logging for this context ");
                msg.append("due to bad log hierarchy. ");
                msg.append("You have more than one version of '");
                msg.append(Log.class.getName());
                msg.append("' visible.");
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(msg.toString());
                } 
                throw new LogConfigurationException(msg.toString());
            }
            if (isDiagnosticsEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Warning: bad log hierarchy. ");
                msg.append("You have more than one version of '");
                msg.append(Log.class.getName());
                msg.append("' visible.");
                logDiagnostic(msg.toString());
            }
        } else {
            if (!allowFlawedDiscovery) {
                StringBuffer msg = new StringBuffer();
                msg.append("Terminating logging for this context. ");
                msg.append("Log class '");
                msg.append(badClass.getName());
                msg.append("' does not implement the Log interface.");
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(msg.toString());
                }
                throw new LogConfigurationException(msg.toString());
            }
            if (isDiagnosticsEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append("[WARNING] Log class '");
                msg.append(badClass.getName());
                msg.append("' does not implement the Log interface.");
                logDiagnostic(msg.toString());
            }
        }
    }
}
