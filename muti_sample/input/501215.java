class ObjectFactory {
    private static final String DEFAULT_PROPERTIES_FILENAME =
                                                     "xalan.properties";
    private static final String SERVICES_PATH = "META-INF/services/";
    private static final boolean DEBUG = false;
    private static Properties fXalanProperties = null;
    private static long fLastModified = -1;
    static Object createObject(String factoryId, String fallbackClassName)
        throws ConfigurationError {
        return createObject(factoryId, null, fallbackClassName);
    } 
    static Object createObject(String factoryId, 
                                      String propertiesFilename,
                                      String fallbackClassName)
        throws ConfigurationError
    {
        Class factoryClass = lookUpFactoryClass(factoryId,
                                                propertiesFilename,
                                                fallbackClassName);
        if (factoryClass == null) {
            throw new ConfigurationError(
                "Provider for " + factoryId + " cannot be found", null);
        }
        try{
            Object instance = factoryClass.newInstance();
            debugPrintln("created new instance of factory " + factoryId);
            return instance;
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider for factory " + factoryId
                    + " could not be instantiated: " + x, x);
        }
    } 
    static Class lookUpFactoryClass(String factoryId) 
        throws ConfigurationError
    {
        return lookUpFactoryClass(factoryId, null, null);
    } 
    static Class lookUpFactoryClass(String factoryId,
                                           String propertiesFilename,
                                           String fallbackClassName)
        throws ConfigurationError
    {
        String factoryClassName = lookUpFactoryClassName(factoryId,
                                                         propertiesFilename,
                                                         fallbackClassName);
        ClassLoader cl = findClassLoader();
        if (factoryClassName == null) {
            factoryClassName = fallbackClassName;
        }
        try{
            Class providerClass = findProviderClass(factoryClassName,
                                                    cl,
                                                    true);
            debugPrintln("created new instance of " + providerClass +
                   " using ClassLoader: " + cl);
            return providerClass;
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError(
                "Provider " + factoryClassName + " not found", x);
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider "+factoryClassName+" could not be instantiated: "+x,
                x);
        }
    } 
    static String lookUpFactoryClassName(String factoryId,
                                                String propertiesFilename,
                                                String fallbackClassName)
    {
        SecuritySupport ss = SecuritySupport.getInstance();
        try {
            String systemProp = ss.getSystemProperty(factoryId);
            if (systemProp != null) {
                debugPrintln("found system property, value=" + systemProp);
                return systemProp;
            }
        } catch (SecurityException se) {
        }
        String factoryClassName = null;
        if (propertiesFilename == null) {
            File propertiesFile = null;
            boolean propertiesFileExists = false;
            try {
                String javah = ss.getSystemProperty("java.home");
                propertiesFilename = javah + File.separator +
                    "lib" + File.separator + DEFAULT_PROPERTIES_FILENAME;
                propertiesFile = new File(propertiesFilename);
                propertiesFileExists = ss.getFileExists(propertiesFile);
            } catch (SecurityException e) {
                fLastModified = -1;
                fXalanProperties = null;
            }
            synchronized (ObjectFactory.class) {
                boolean loadProperties = false;
                FileInputStream fis = null;
                try {
                    if(fLastModified >= 0) {
                        if(propertiesFileExists &&
                                (fLastModified < (fLastModified = ss.getLastModified(propertiesFile)))) {
                            loadProperties = true;
                        } else {
                            if(!propertiesFileExists) {
                                fLastModified = -1;
                                fXalanProperties = null;
                            } 
                        }
                    } else {
                        if(propertiesFileExists) {
                            loadProperties = true;
                            fLastModified = ss.getLastModified(propertiesFile);
                        } 
                    }
                    if(loadProperties) {
                        fXalanProperties = new Properties();
                        fis = ss.getFileInputStream(propertiesFile);
                        fXalanProperties.load(fis);
                    }
	        } catch (Exception x) {
	            fXalanProperties = null;
	            fLastModified = -1;
	        }
                finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        }
                        catch (IOException exc) {}
                    }
                }	            
            }
            if(fXalanProperties != null) {
                factoryClassName = fXalanProperties.getProperty(factoryId);
            }
        } else {
            FileInputStream fis = null;
            try {
                fis = ss.getFileInputStream(new File(propertiesFilename));
                Properties props = new Properties();
                props.load(fis);
                factoryClassName = props.getProperty(factoryId);
            } catch (Exception x) {
            }
            finally {
                if (fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException exc) {}
                }
            }               
        }
        if (factoryClassName != null) {
            debugPrintln("found in " + propertiesFilename + ", value="
                          + factoryClassName);
            return factoryClassName;
        }
        return findJarServiceProviderName(factoryId);
    } 
    private static void debugPrintln(String msg) {
        if (DEBUG) {
            System.err.println("JAXP: " + msg);
        }
    } 
    static ClassLoader findClassLoader()
        throws ConfigurationError
    { 
        SecuritySupport ss = SecuritySupport.getInstance();
        ClassLoader context = ss.getContextClassLoader();
        ClassLoader system = ss.getSystemClassLoader();
        ClassLoader chain = system;
        while (true) {
            if (context == chain) {
                ClassLoader current = ObjectFactory.class.getClassLoader();
                chain = system;
                while (true) {
                    if (current == chain) {
                        return system;
                    }
                    if (chain == null) {
                        break;
                    }
                    chain = ss.getParentClassLoader(chain);
                }
                return current;
            }
            if (chain == null) {
                break;
            }
            chain = ss.getParentClassLoader(chain);
        };
        return context;
    } 
    static Object newInstance(String className, ClassLoader cl,
                                      boolean doFallback)
        throws ConfigurationError
    {
        try{
            Class providerClass = findProviderClass(className, cl, doFallback);
            Object instance = providerClass.newInstance();
            debugPrintln("created new instance of " + providerClass +
                   " using ClassLoader: " + cl);
            return instance;
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError(
                "Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider " + className + " could not be instantiated: " + x,
                x);
        }
    }
    static Class findProviderClass(String className, ClassLoader cl,
                                           boolean doFallback)
        throws ClassNotFoundException, ConfigurationError
    {   
        SecurityManager security = System.getSecurityManager();
        try{
                if (security != null){
                    final int lastDot = className.lastIndexOf(".");
                    String packageName = className;
                    if (lastDot != -1) packageName = className.substring(0, lastDot);
                    security.checkPackageAccess(packageName);
                 }   
        }catch(SecurityException e){
            throw e;
        }
        Class providerClass;
        if (cl == null) {
            providerClass = Class.forName(className);
        } else {
            try {
                providerClass = cl.loadClass(className);
            } catch (ClassNotFoundException x) {
                if (doFallback) {
                    ClassLoader current = ObjectFactory.class.getClassLoader();
                    if (current == null) {
                        providerClass = Class.forName(className);
                    } else if (cl != current) {
                        cl = current;
                        providerClass = cl.loadClass(className);
                    } else {
                        throw x;
                    }
                } else {
                    throw x;
                }
            }
        }
        return providerClass;
    }
    private static String findJarServiceProviderName(String factoryId)
    {
        SecuritySupport ss = SecuritySupport.getInstance();
        String serviceId = SERVICES_PATH + factoryId;
        InputStream is = null;
        ClassLoader cl = findClassLoader();
        is = ss.getResourceAsStream(cl, serviceId);
        if (is == null) {
            ClassLoader current = ObjectFactory.class.getClassLoader();
            if (cl != current) {
                cl = current;
                is = ss.getResourceAsStream(cl, serviceId);
            }
        }
        if (is == null) {
            return null;
        }
        debugPrintln("found jar resource=" + serviceId +
               " using ClassLoader: " + cl);
        BufferedReader rd;
        try {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(is));
        }
        String factoryClassName = null;
        try {
            factoryClassName = rd.readLine();
        } catch (IOException x) {
            return null;
        }
        finally {
            try {
                rd.close();
            }
            catch (IOException exc) {}
        }          
        if (factoryClassName != null &&
            ! "".equals(factoryClassName)) {
            debugPrintln("found in resource, value="
                   + factoryClassName);
            return factoryClassName;
        }
        return null;
    }
    static class ConfigurationError 
        extends Error {
                static final long serialVersionUID = 2276082712114762609L;
        private Exception exception;
        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        } 
        Exception getException() {
            return exception;
        } 
    } 
} 
