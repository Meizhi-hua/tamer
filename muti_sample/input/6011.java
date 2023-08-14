public class GetPlatformMXBeans {
    private static MBeanServer platformMBeanServer =
            getPlatformMBeanServer();
    public static void main(String[] argv) throws Exception {
        checkPlatformMXBean(getClassLoadingMXBean(),
                            ClassLoadingMXBean.class,
                            CLASS_LOADING_MXBEAN_NAME);
        checkPlatformMXBean(getCompilationMXBean(),
                            CompilationMXBean.class,
                            COMPILATION_MXBEAN_NAME);
        checkPlatformMXBean(getMemoryMXBean(),
                            MemoryMXBean.class,
                            MEMORY_MXBEAN_NAME);
        checkPlatformMXBean(getOperatingSystemMXBean(),
                            OperatingSystemMXBean.class,
                            OPERATING_SYSTEM_MXBEAN_NAME);
        checkPlatformMXBean(getRuntimeMXBean(),
                            RuntimeMXBean.class,
                            RUNTIME_MXBEAN_NAME);
        checkPlatformMXBean(getThreadMXBean(),
                            ThreadMXBean.class,
                            THREAD_MXBEAN_NAME);
        checkGarbageCollectorMXBeans(getGarbageCollectorMXBeans());
        checkMemoryManagerMXBeans(getMemoryManagerMXBeans());
        checkMemoryPoolMXBeans(getMemoryPoolMXBeans());
        checkInvalidPlatformMXBean();
    }
    private static <T extends PlatformManagedObject>
            void checkPlatformMXBean(T obj, Class<T> mxbeanInterface,
                                     String mxbeanName)
        throws Exception
    {
        PlatformManagedObject mxbean = getPlatformMXBean(mxbeanInterface);
        if (obj != mxbean) {
            throw new RuntimeException("Singleton MXBean returned not matched");
        }
        int numElements = obj == null ? 0 : 1;
        List<? extends PlatformManagedObject> mxbeans =
            getPlatformMXBeans(mxbeanInterface);
        if (mxbeans.size() != numElements) {
            throw new RuntimeException("Unmatched number of platform MXBeans "
                + mxbeans.size() + ". Expected = " + numElements);
        }
        if (obj != null) {
            if (obj != mxbeans.get(0)) {
                throw new RuntimeException("The list returned by getPlatformMXBeans"
                    + " not matched");
            }
            ObjectName on = new ObjectName(mxbeanName);
            if (!on.equals(mxbean.getObjectName())) {
                throw new RuntimeException("Unmatched ObjectName " +
                    mxbean.getObjectName() + " Expected = " + on);
            }
            checkRemotePlatformMXBean(obj, platformMBeanServer,
                                      mxbeanInterface, mxbeanName);
        }
    }
    private static <T extends PlatformManagedObject>
            void checkRemotePlatformMXBean(T obj,
                                           MBeanServerConnection mbs,
                                           Class<T> mxbeanInterface,
                                           String mxbeanName)
        throws Exception
    {
        PlatformManagedObject mxbean = getPlatformMXBean(mbs, mxbeanInterface);
        if ((obj == null && mxbean != null) || (obj != null && mxbean == null)) {
            throw new RuntimeException("Singleton MXBean returned not matched");
        }
        int numElements = obj == null ? 0 : 1;
        List<? extends PlatformManagedObject> mxbeans =
            getPlatformMXBeans(mbs, mxbeanInterface);
        if (mxbeans.size() != numElements) {
            throw new RuntimeException("Unmatched number of platform MXBeans "
                + mxbeans.size() + ". Expected = " + numElements);
        }
        ObjectName on = new ObjectName(mxbeanName);
        if (!on.equals(mxbean.getObjectName())) {
            throw new RuntimeException("Unmatched ObjectName " +
                mxbean.getObjectName() + " Expected = " + on);
        }
    }
    private static void checkMemoryManagerMXBeans(List<MemoryManagerMXBean> objs)
        throws Exception
    {
        checkPlatformMXBeans(objs, MemoryManagerMXBean.class);
        for (MemoryManagerMXBean mxbean : objs) {
            String domainAndType;
            if (mxbean instanceof GarbageCollectorMXBean) {
                domainAndType = GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE;
            } else {
                domainAndType = MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE;
            }
            ObjectName on = new ObjectName(domainAndType +
                                           ",name=" + mxbean.getName());
            if (!on.equals(mxbean.getObjectName())) {
                throw new RuntimeException("Unmatched ObjectName " +
                    mxbean.getObjectName() + " Expected = " + on);
            }
        }
    }
    private static void checkMemoryPoolMXBeans(List<MemoryPoolMXBean> objs)
        throws Exception
    {
        checkPlatformMXBeans(objs, MemoryPoolMXBean.class);
        for (MemoryPoolMXBean mxbean : objs) {
            ObjectName on = new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE +
                                           ",name=" + mxbean.getName());
            if (!on.equals(mxbean.getObjectName())) {
                throw new RuntimeException("Unmatched ObjectName " +
                    mxbean.getObjectName() + " Expected = " + on);
            }
        }
    }
    private static void checkGarbageCollectorMXBeans(List<GarbageCollectorMXBean> objs)
        throws Exception
    {
        checkPlatformMXBeans(objs, GarbageCollectorMXBean.class);
        for (GarbageCollectorMXBean mxbean : objs) {
            ObjectName on = new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE +
                                           ",name=" + mxbean.getName());
            if (!on.equals(mxbean.getObjectName())) {
                throw new RuntimeException("Unmatched ObjectName " +
                    mxbean.getObjectName() + " Expected = " + on);
            }
        }
    }
    private static <T extends PlatformManagedObject>
        void checkPlatformMXBeans(List<T> objs, Class<T> mxbeanInterface)
            throws Exception
    {
        try {
            getPlatformMXBean(mxbeanInterface);
            throw new RuntimeException(mxbeanInterface + ": not a singleton MXBean");
        } catch (IllegalArgumentException e) {
        }
        List<? extends PlatformManagedObject> mxbeans =
            getPlatformMXBeans(mxbeanInterface);
        if (objs.size() != mxbeans.size()) {
            throw new RuntimeException("Unmatched number of platform MXBeans "
                + mxbeans.size() + ". Expected = " + objs.size());
        }
        List<T> list = new ArrayList<T>(objs);
        for (PlatformManagedObject pmo : mxbeans) {
            if (list.contains(pmo)) {
                list.remove(pmo);
            } else {
                throw new RuntimeException(pmo +
                    " not in the platform MXBean list");
            }
        }
        if (!list.isEmpty()) {
            throw new RuntimeException("The list returned by getPlatformMXBeans"
                + " not matched");
        }
        mxbeans = getPlatformMXBeans(platformMBeanServer, mxbeanInterface);
        if (objs.size() != mxbeans.size()) {
            throw new RuntimeException("Unmatched number of platform MXBeans "
                + mxbeans.size() + ". Expected = " + objs.size());
        }
    }
    interface FakeMXBean extends PlatformManagedObject {};
    private static void checkInvalidPlatformMXBean() throws IOException {
        try {
            getPlatformMXBean(FakeMXBean.class);
            throw new RuntimeException("Expect IllegalArgumentException but not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            getPlatformMXBeans(FakeMXBean.class);
            throw new RuntimeException("Expect IllegalArgumentException but not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            getPlatformMXBean(platformMBeanServer, FakeMXBean.class);
            throw new RuntimeException("Expect IllegalArgumentException but not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            getPlatformMXBeans(platformMBeanServer, FakeMXBean.class);
            throw new RuntimeException("Expect IllegalArgumentException but not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
}
