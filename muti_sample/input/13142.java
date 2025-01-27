public class MonitorInfoCompositeData extends LazyCompositeData {
    private final MonitorInfo lock;
    private MonitorInfoCompositeData(MonitorInfo mi) {
        this.lock = mi;
    }
    public MonitorInfo getMonitorInfo() {
        return lock;
    }
    public static CompositeData toCompositeData(MonitorInfo mi) {
        MonitorInfoCompositeData micd = new MonitorInfoCompositeData(mi);
        return micd.getCompositeData();
    }
    protected CompositeData getCompositeData() {
        int len = monitorInfoItemNames.length;
        Object[] values = new Object[len];
        CompositeData li = LockDataConverter.toLockInfoCompositeData(lock);
        for (int i = 0; i < len; i++) {
            String item = monitorInfoItemNames[i];
            if (item.equals(LOCKED_STACK_FRAME)) {
                StackTraceElement ste = lock.getLockedStackFrame();
                values[i] = (ste != null ? StackTraceElementCompositeData.
                                               toCompositeData(ste)
                                         : null);
            } else if (item.equals(LOCKED_STACK_DEPTH)) {
                values[i] = new Integer(lock.getLockedStackDepth());
            } else {
                values[i] = li.get(item);
            }
        }
        try {
            return new CompositeDataSupport(monitorInfoCompositeType,
                                            monitorInfoItemNames,
                                            values);
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }
    private static final CompositeType monitorInfoCompositeType;
    private static final String[] monitorInfoItemNames;
    static {
        try {
            monitorInfoCompositeType = (CompositeType)
                MappedMXBeanType.toOpenType(MonitorInfo.class);
            Set<String> s = monitorInfoCompositeType.keySet();
            monitorInfoItemNames = (String[]) s.toArray(new String[0]);
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }
    static CompositeType getMonitorInfoCompositeType() {
        return monitorInfoCompositeType;
    }
    private static final String CLASS_NAME         = "className";
    private static final String IDENTITY_HASH_CODE = "identityHashCode";
    private static final String LOCKED_STACK_FRAME = "lockedStackFrame";
    private static final String LOCKED_STACK_DEPTH = "lockedStackDepth";
    public static String getClassName(CompositeData cd) {
        return getString(cd, CLASS_NAME);
    }
    public static int getIdentityHashCode(CompositeData cd) {
        return getInt(cd, IDENTITY_HASH_CODE);
    }
    public static StackTraceElement getLockedStackFrame(CompositeData cd) {
        CompositeData ste = (CompositeData) cd.get(LOCKED_STACK_FRAME);
        if (ste != null) {
            return StackTraceElementCompositeData.from(ste);
        } else {
            return null;
        }
    }
    public static int getLockedStackDepth(CompositeData cd) {
        return getInt(cd, LOCKED_STACK_DEPTH);
    }
    public static void validateCompositeData(CompositeData cd) {
        if (cd == null) {
            throw new NullPointerException("Null CompositeData");
        }
        if (!isTypeMatched(monitorInfoCompositeType, cd.getCompositeType())) {
            throw new IllegalArgumentException(
                "Unexpected composite type for MonitorInfo");
        }
    }
    private static final long serialVersionUID = -5825215591822908529L;
}
