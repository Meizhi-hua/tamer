class VMManagementImpl implements VMManagement {
    private static String version;
    private static boolean compTimeMonitoringSupport;
    private static boolean threadContentionMonitoringSupport;
    private static boolean currentThreadCpuTimeSupport;
    private static boolean otherThreadCpuTimeSupport;
    private static boolean bootClassPathSupport;
    private static boolean objectMonitorUsageSupport;
    private static boolean synchronizerUsageSupport;
    private static boolean threadAllocatedMemorySupport;
    private static boolean gcNotificationSupport;
    static {
        version = getVersion0();
        if (version == null) {
            throw new AssertionError("Invalid Management Version");
        }
        initOptionalSupportFields();
    }
    private native static String getVersion0();
    private native static void initOptionalSupportFields();
    public boolean isCompilationTimeMonitoringSupported() {
        return compTimeMonitoringSupport;
    }
    public boolean isThreadContentionMonitoringSupported() {
        return threadContentionMonitoringSupport;
    }
    public boolean isCurrentThreadCpuTimeSupported() {
        return currentThreadCpuTimeSupport;
    }
    public boolean isOtherThreadCpuTimeSupported() {
        return otherThreadCpuTimeSupport;
    }
    public boolean isBootClassPathSupported() {
        return bootClassPathSupport;
    }
    public boolean isObjectMonitorUsageSupported() {
        return objectMonitorUsageSupport;
    }
    public boolean isSynchronizerUsageSupported() {
        return synchronizerUsageSupport;
    }
    public boolean isThreadAllocatedMemorySupported() {
        return threadAllocatedMemorySupport;
    }
    public boolean isGcNotificationSupported() {
        return gcNotificationSupport;
    }
    public native boolean isThreadContentionMonitoringEnabled();
    public native boolean isThreadCpuTimeEnabled();
    public native boolean isThreadAllocatedMemoryEnabled();
    public int    getLoadedClassCount() {
        long count = getTotalClassCount() - getUnloadedClassCount();
        return (int) count;
    }
    public native long getTotalClassCount();
    public native long getUnloadedClassCount();
    public native boolean getVerboseClass();
    public native boolean getVerboseGC();
    public String   getManagementVersion() {
        return version;
    }
    public String getVmId() {
        int pid = getProcessId();
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return pid + "@" + hostname;
    }
    private native int getProcessId();
    public String   getVmName() {
        return System.getProperty("java.vm.name");
    }
    public String   getVmVendor() {
        return System.getProperty("java.vm.vendor");
    }
    public String   getVmVersion() {
        return System.getProperty("java.vm.version");
    }
    public String   getVmSpecName()  {
        return System.getProperty("java.vm.specification.name");
    }
    public String   getVmSpecVendor() {
        return System.getProperty("java.vm.specification.vendor");
    }
    public String   getVmSpecVersion() {
        return System.getProperty("java.vm.specification.version");
    }
    public String   getClassPath() {
        return System.getProperty("java.class.path");
    }
    public String   getLibraryPath()  {
        return System.getProperty("java.library.path");
    }
    public String   getBootClassPath( ) {
        PrivilegedAction<String> pa
            = new GetPropertyAction("sun.boot.class.path");
        String result =  AccessController.doPrivileged(pa);
        return result;
    }
    private List<String> vmArgs = null;
    public synchronized List<String> getVmArguments() {
        if (vmArgs == null) {
            String[] args = getVmArguments0();
            List<String> l = ((args != null && args.length != 0) ? Arrays.asList(args) :
                                        Collections.<String>emptyList());
            vmArgs = Collections.unmodifiableList(l);
        }
        return vmArgs;
    }
    public native String[] getVmArguments0();
    public native long getStartupTime();
    public native int getAvailableProcessors();
    public String   getCompilerName() {
        String name =  AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("sun.management.compiler");
                }
            });
        return name;
    }
    public native long getTotalCompileTime();
    public native long getTotalThreadCount();
    public native int  getLiveThreadCount();
    public native int  getPeakThreadCount();
    public native int  getDaemonThreadCount();
    public String getOsName() {
        return System.getProperty("os.name");
    }
    public String getOsArch() {
        return System.getProperty("os.arch");
    }
    public String getOsVersion() {
        return System.getProperty("os.version");
    }
    public native long getSafepointCount();
    public native long getTotalSafepointTime();
    public native long getSafepointSyncTime();
    public native long getTotalApplicationNonStoppedTime();
    public native long getLoadedClassSize();
    public native long getUnloadedClassSize();
    public native long getClassLoadingTime();
    public native long getMethodDataSize();
    public native long getInitializedClassCount();
    public native long getClassInitializationTime();
    public native long getClassVerificationTime();
    private PerfInstrumentation perfInstr = null;
    private boolean noPerfData = false;
    private synchronized PerfInstrumentation getPerfInstrumentation() {
        if (noPerfData || perfInstr != null) {
             return perfInstr;
        }
        Perf perf =  AccessController.doPrivileged(new Perf.GetPerfAction());
        try {
            ByteBuffer bb = perf.attach(0, "r");
            if (bb.capacity() == 0) {
                noPerfData = true;
                return null;
            }
            perfInstr = new PerfInstrumentation(bb);
        } catch (IllegalArgumentException e) {
            noPerfData = true;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return perfInstr;
    }
    public List<Counter> getInternalCounters(String pattern) {
        PerfInstrumentation perf = getPerfInstrumentation();
        if (perf != null) {
            return perf.findByPattern(pattern);
        } else {
            return Collections.emptyList();
        }
    }
}
