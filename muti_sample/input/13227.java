abstract class ConnectorImpl implements Connector {
    Map defaultArguments = new LinkedHashMap();
    static String trueString = null;
    static String falseString;
    private static List freeVMClasses; 
    private static ClassLoader myLoader;
    static final protected boolean DEBUG;
    static {
        myLoader = ConnectorImpl.class.getClassLoader();
        freeVMClasses = new ArrayList(0);
        DEBUG = System.getProperty("sun.jvm.hotspot.jdi.ConnectorImpl.DEBUG") != null;
    }
    private static synchronized void addFreeVMImplClass(Class clazz) {
        if (DEBUG) {
            System.out.println("adding free VirtualMachineImpl class");
        }
        freeVMClasses.add(new SoftReference(clazz));
    }
    private static synchronized Class getFreeVMImplClass() {
        while (!freeVMClasses.isEmpty()) {
              SoftReference ref = (SoftReference) freeVMClasses.remove(0);
              Object o = ref.get();
              if (o != null) {
                  if (DEBUG) {
                      System.out.println("re-using loaded VirtualMachineImpl");
                  }
                  return (Class) o;
              }
        }
        return null;
    }
    private static Class getVMImplClassFrom(ClassLoader cl)
                               throws ClassNotFoundException {
        return Class.forName("sun.jvm.hotspot.jdi.VirtualMachineImpl", true, cl);
    }
    protected static Class loadVirtualMachineImplClass()
                               throws ClassNotFoundException {
        Class vmImplClass = getFreeVMImplClass();
        if (vmImplClass == null) {
            ClassLoader cl = new SAJDIClassLoader(myLoader);
            vmImplClass = getVMImplClassFrom(cl);
        }
        return vmImplClass;
    }
    private static String getSAClassPathForVM(String vmVersion) {
        final String prefix = "sun.jvm.hotspot.jdi.";
        String jvmHome = System.getProperty(prefix + vmVersion);
        if (DEBUG) {
            System.out.println("looking for System property " + prefix + vmVersion);
        }
        if (jvmHome == null) {
            int index = vmVersion.indexOf('-');
            if (index != -1) {
                vmVersion = vmVersion.substring(0, index);
                if (DEBUG) {
                    System.out.println("looking for System property " + prefix + vmVersion);
                }
                jvmHome = System.getProperty(prefix + vmVersion);
            }
            if (jvmHome == null) {
                if (DEBUG) {
                    System.out.println("can't locate JDK home for " + vmVersion);
                }
                return null;
            }
        }
        if (DEBUG) {
            System.out.println("JDK home for " + vmVersion + " is " + jvmHome);
        }
        StringBuffer buf = new StringBuffer();
        buf.append(jvmHome);
        buf.append(File.separatorChar);
        buf.append("lib");
        buf.append(File.separatorChar);
        buf.append("sa-jdi.jar");
        return buf.toString();
    }
    protected static Class loadVirtualMachineImplClass(String vmVersion)
            throws ClassNotFoundException {
        if (DEBUG) {
            System.out.println("attemping to load sa-jdi.jar for version " + vmVersion);
        }
        String classPath = getSAClassPathForVM(vmVersion);
        if (classPath != null) {
            ClassLoader cl = new SAJDIClassLoader(myLoader, classPath);
            return getVMImplClassFrom(cl);
        } else {
            return null;
        }
    }
    private static boolean isVMVersionMismatch(Throwable throwable) {
        String className = throwable.getClass().getName();
        return className.equals("sun.jvm.hotspot.runtime.VMVersionMismatchException");
    }
    private static String getVMVersion(Throwable throwable)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class expClass = throwable.getClass();
        Method targetVersionMethod = expClass.getMethod("getTargetVersion", new Class[0]);
        return (String) targetVersionMethod.invoke(throwable);
    }
    protected static Class handleVMVersionMismatch(InvocationTargetException ite) {
        Throwable cause = ite.getCause();
        if (DEBUG) {
            System.out.println("checking for version mismatch...");
        }
        while (cause != null) {
            try {
                if (isVMVersionMismatch(cause)) {
                    if (DEBUG) {
                        System.out.println("Triggering cross VM version support...");
                    }
                    return loadVirtualMachineImplClass(getVMVersion(cause));
                }
            } catch (Exception exp) {
                if (DEBUG) {
                    System.out.println("failed to load VirtualMachineImpl class");
                    exp.printStackTrace();
                }
                return null;
            }
            cause = cause.getCause();
        }
        return null;
    }
    protected void checkNativeLink(SecurityManager sm, String os) {
        if (os.equals("SunOS") || os.equals("Linux")) {
            sm.checkLink("saproc");
        } else if (os.startsWith("Windows")) {
            sm.checkLink("sawindbg");
        } else {
           throw new RuntimeException(os + " is not yet supported");
        }
    }
    protected static void setVMDisposeObserver(final Object vm) {
        try {
            Method setDisposeObserverMethod = vm.getClass().getDeclaredMethod("setDisposeObserver",
                                                         new Class[] { java.util.Observer.class });
            setDisposeObserverMethod.setAccessible(true);
            setDisposeObserverMethod.invoke(vm,
                                         new Object[] {
                                             new Observer() {
                                                 public void update(Observable o, Object data) {
                                                     if (DEBUG) {
                                                         System.out.println("got VM.dispose notification");
                                                     }
                                                     addFreeVMImplClass(vm.getClass());
                                                 }
                                             }
                                         });
        } catch (Exception exp) {
            if (DEBUG) {
               System.out.println("setVMDisposeObserver() got an exception:");
               exp.printStackTrace();
            }
        }
    }
    public Map defaultArguments() {
        Map defaults = new LinkedHashMap();
        Collection values = defaultArguments.values();
        Iterator iter = values.iterator();
        while (iter.hasNext()) {
            ArgumentImpl argument = (ArgumentImpl)iter.next();
            defaults.put(argument.name(), argument.clone());
        }
        return defaults;
    }
    void addStringArgument(String name, String label, String description,
                           String defaultValue, boolean mustSpecify) {
        defaultArguments.put(name,
                             new StringArgumentImpl(name, label,
                                                    description,
                                                    defaultValue,
                                                    mustSpecify));
    }
    void addBooleanArgument(String name, String label, String description,
                            boolean defaultValue, boolean mustSpecify) {
        defaultArguments.put(name,
                             new BooleanArgumentImpl(name, label,
                                                     description,
                                                     defaultValue,
                                                     mustSpecify));
    }
    void addIntegerArgument(String name, String label, String description,
                            String defaultValue, boolean mustSpecify,
                            int min, int max) {
        defaultArguments.put(name,
                             new IntegerArgumentImpl(name, label,
                                                     description,
                                                     defaultValue,
                                                     mustSpecify,
                                                     min, max));
    }
    void addSelectedArgument(String name, String label, String description,
                             String defaultValue, boolean mustSpecify,
                             List list) {
        defaultArguments.put(name,
                             new SelectedArgumentImpl(name, label,
                                                      description,
                                                      defaultValue,
                                                      mustSpecify, list));
    }
    ArgumentImpl argument(String name, Map arguments)
                throws IllegalConnectorArgumentsException {
        ArgumentImpl argument = (ArgumentImpl)arguments.get(name);
        if (argument == null) {
            throw new IllegalConnectorArgumentsException(
                         "Argument missing", name);
        }
        String value = argument.value();
        if (value == null || value.length() == 0) {
            if (argument.mustSpecify()) {
            throw new IllegalConnectorArgumentsException(
                         "Argument unspecified", name);
            }
        } else if(!argument.isValid(value)) {
            throw new IllegalConnectorArgumentsException(
                         "Argument invalid", name);
        }
        return argument;
    }
    String getString(String key) {
        return key;
    }
    public String toString() {
        String string = name() + " (defaults: ";
        Iterator iter = defaultArguments().values().iterator();
        boolean first = true;
        while (iter.hasNext()) {
            ArgumentImpl argument = (ArgumentImpl)iter.next();
            if (!first) {
                string += ", ";
            }
            string += argument.toString();
            first = false;
        }
        return string  + ")";
    }
    abstract class ArgumentImpl implements Connector.Argument, Cloneable, Serializable {
        private String name;
        private String label;
        private String description;
        private String value;
        private boolean mustSpecify;
        ArgumentImpl(String name, String label, String description,
                     String value,
                     boolean mustSpecify) {
            this.name = name;
            this.label = label;
            this.description = description;
            this.value = value;
            this.mustSpecify = mustSpecify;
        }
        public abstract boolean isValid(String value);
        public String name() {
            return name;
        }
        public String label() {
            return label;
        }
        public String description() {
            return description;
        }
        public String value() {
            return value;
        }
        public void setValue(String value) {
            if (value == null) {
                throw new NullPointerException("Can't set null value");
            }
            this.value = value;
        }
        public boolean mustSpecify() {
            return mustSpecify;
        }
        public boolean equals(Object obj) {
            if ((obj != null) && (obj instanceof Connector.Argument)) {
                Connector.Argument other = (Connector.Argument)obj;
                return (name().equals(other.name())) &&
                       (description().equals(other.description())) &&
                       (mustSpecify() == other.mustSpecify()) &&
                       (value().equals(other.value()));
            } else {
                return false;
            }
        }
        public int hashCode() {
            return description().hashCode();
        }
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw (InternalException) new InternalException().initCause(e);
            }
        }
        public String toString() {
            return name() + "=" + value();
        }
    }
    class BooleanArgumentImpl extends ConnectorImpl.ArgumentImpl
                              implements Connector.BooleanArgument {
        BooleanArgumentImpl(String name, String label, String description,
                            boolean value,
                            boolean mustSpecify) {
            super(name, label, description, null, mustSpecify);
            if(trueString == null) {
                trueString = getString("true");
                falseString = getString("false");
            }
            setValue(value);
        }
        public void setValue(boolean value) {
            setValue(stringValueOf(value));
        }
        public boolean isValid(String value) {
            return value.equals(trueString) || value.equals(falseString);
        }
        public String stringValueOf(boolean value) {
            return value? trueString : falseString;
        }
        public boolean booleanValue() {
            return value().equals(trueString);
        }
    }
    class IntegerArgumentImpl extends ConnectorImpl.ArgumentImpl
                              implements Connector.IntegerArgument {
        private final int min;
        private final int max;
        IntegerArgumentImpl(String name, String label, String description,
                            String value,
                            boolean mustSpecify, int min, int max) {
            super(name, label, description, value, mustSpecify);
            this.min = min;
            this.max = max;
        }
        public void setValue(int value) {
            setValue(stringValueOf(value));
        }
        public boolean isValid(String value) {
            if (value == null) {
                return false;
            }
            try {
                return isValid(Integer.decode(value).intValue());
            } catch(NumberFormatException exc) {
                return false;
            }
        }
        public boolean isValid(int value) {
            return min <= value && value <= max;
        }
        public String stringValueOf(int value) {
            return ""+value;
        }
        public int intValue() {
            if (value() == null) {
                return 0;
            }
            try {
                return Integer.decode(value()).intValue();
            } catch(NumberFormatException exc) {
                return 0;
            }
        }
        public int max() {
            return max;
        }
        public int min() {
            return min;
        }
    }
    class StringArgumentImpl extends ConnectorImpl.ArgumentImpl
                              implements Connector.StringArgument {
        StringArgumentImpl(String name, String label, String description,
                           String value,
                           boolean mustSpecify) {
            super(name, label, description, value, mustSpecify);
        }
        public boolean isValid(String value) {
            return true;
        }
    }
    class SelectedArgumentImpl extends ConnectorImpl.ArgumentImpl
                              implements Connector.SelectedArgument {
        private final List choices;
        SelectedArgumentImpl(String name, String label, String description,
                             String value,
                             boolean mustSpecify, List choices) {
            super(name, label, description, value, mustSpecify);
            this.choices = Collections.unmodifiableList(
                                           new ArrayList(choices));
        }
        public List choices() {
            return choices;
        }
        public boolean isValid(String value) {
            return choices.contains(value);
        }
    }
}
