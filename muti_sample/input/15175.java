public class JvmThreadInstanceEntryImpl
    implements JvmThreadInstanceEntryMBean, Serializable {
    public final static class ThreadStateMap {
        public final static class Byte0 {
            public final static byte inNative     = (byte)0x80; 
            public final static byte suspended    = (byte)0x40; 
            public final static byte newThread    = (byte)0x20; 
            public final static byte runnable     = (byte)0x10; 
            public final static byte blocked      = (byte)0x08; 
            public final static byte terminated   = (byte)0x04; 
            public final static byte waiting      = (byte)0x02; 
            public final static byte timedWaiting = (byte)0x01; 
        }
        public final static class Byte1 {
            public final static byte other        = (byte)0x80; 
            public final static byte reserved10   = (byte)0x40; 
            public final static byte reserved11   = (byte)0x20; 
            public final static byte reserved12   = (byte)0x10; 
            public final static byte reserved13   = (byte)0x08; 
            public final static byte reserved14   = (byte)0x04; 
            public final static byte reserved15   = (byte)0x02; 
            public final static byte reserved16   = (byte)0x01; 
        }
        public final static byte mask0 = (byte)0x3F;
        public final static byte mask1 = (byte)0x80;
        private static void setBit(byte[] bitmap, int index, byte state) {
            bitmap[index] = (byte) (bitmap[index] | state);
        }
        public static void setNative(byte[] bitmap) {
            setBit(bitmap,0,Byte0.inNative);
        }
        public static void setSuspended(byte[] bitmap) {
            setBit(bitmap,0,Byte0.suspended);
        }
        public static void setState(byte[] bitmap, Thread.State state) {
            switch(state) {
            case BLOCKED:
                setBit(bitmap,0,Byte0.blocked);
                return;
            case NEW:
                setBit(bitmap,0,Byte0.newThread);
                return;
            case RUNNABLE:
                setBit(bitmap,0,Byte0.runnable);
                return;
            case TERMINATED:
                setBit(bitmap,0,Byte0.terminated);
                return;
            case TIMED_WAITING:
                setBit(bitmap,0,Byte0.timedWaiting);
                return;
            case WAITING:
                setBit(bitmap,0,Byte0.waiting);
                return;
            }
        }
        public static void checkOther(byte[] bitmap) {
            if (((bitmap[0]&mask0)==(byte)0x00) &&
                ((bitmap[1]&mask1)==(byte)0x00))
                setBit(bitmap,1,Byte1.other);
        }
        public static Byte[] getState(ThreadInfo info) {
            byte[] bitmap = new byte[] {(byte)0x00, (byte)0x00};
            try {
                final Thread.State state = info.getThreadState();
                final boolean inNative  = info.isInNative();
                final boolean suspended = info.isSuspended();
                log.debug("getJvmThreadInstState",
                          "[State=" + state +
                          ",isInNative=" + inNative +
                          ",isSuspended=" + suspended + "]");
                setState(bitmap,state);
                if (inNative)  setNative(bitmap);
                if (suspended) setSuspended(bitmap);
                checkOther(bitmap);
            } catch (RuntimeException r) {
                bitmap[0]=(byte)0x00;
                bitmap[1]=Byte1.other;
                log.trace("getJvmThreadInstState",
                          "Unexpected exception: " + r);
                log.debug("getJvmThreadInstState",r);
            }
            Byte[] result = { new Byte(bitmap[0]), new Byte(bitmap[1]) };
            return result;
        }
    }
    private final ThreadInfo info;
    private final Byte[] index;
    public JvmThreadInstanceEntryImpl(ThreadInfo info,
                                      Byte[] index) {
        this.info = info;
        this.index = index;
    }
    private static String  jvmThreadInstIndexOid = null;
    public static String getJvmThreadInstIndexOid()
        throws SnmpStatusException {
        if (jvmThreadInstIndexOid == null) {
            final SnmpOidTable  table = new JVM_MANAGEMENT_MIBOidTable();
            final SnmpOidRecord record =
                table.resolveVarName("jvmThreadInstIndex");
            jvmThreadInstIndexOid = record.getOid();
        }
        return jvmThreadInstIndexOid;
    }
    public String getJvmThreadInstLockOwnerPtr() throws SnmpStatusException {
       long id = info.getLockOwnerId();
       if(id == -1)
           return new String("0.0");
       SnmpOid oid = JvmThreadInstanceTableMetaImpl.makeOid(id);
       return getJvmThreadInstIndexOid() + "." + oid.toString();
    }
    private String validDisplayStringTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validDisplayStringTC(str);
    }
    private String validJavaObjectNameTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(str);
    }
    private String validPathElementTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(str);
    }
    public String getJvmThreadInstLockName() throws SnmpStatusException {
        return validJavaObjectNameTC(info.getLockName());
    }
    public String getJvmThreadInstName() throws SnmpStatusException {
        return validJavaObjectNameTC(info.getThreadName());
    }
    public Long getJvmThreadInstCpuTimeNs() throws SnmpStatusException {
        long l = 0;
        final ThreadMXBean tmb = JvmThreadingImpl.getThreadMXBean();
        try {
            if (tmb.isThreadCpuTimeSupported()) {
                l = tmb.getThreadCpuTime(info.getThreadId());
                log.debug("getJvmThreadInstCpuTimeNs", "Cpu time ns : " + l);
                if(l == -1) l = 0;
            }
        } catch (UnsatisfiedLinkError e) {
            log.debug("getJvmThreadInstCpuTimeNs",
                      "Operation not supported: " + e);
        }
        return new Long(l);
    }
    public Long getJvmThreadInstBlockTimeMs() throws SnmpStatusException {
        long l = 0;
        final ThreadMXBean tmb = JvmThreadingImpl.getThreadMXBean();
        if (tmb.isThreadContentionMonitoringSupported()) {
            l = info.getBlockedTime();
            if(l == -1) l = 0;
        }
        return new Long(l);
    }
    public Long getJvmThreadInstBlockCount() throws SnmpStatusException {
        return new Long(info.getBlockedCount());
    }
    public Long getJvmThreadInstWaitTimeMs() throws SnmpStatusException {
        long l = 0;
        final ThreadMXBean tmb = JvmThreadingImpl.getThreadMXBean();
        if (tmb.isThreadContentionMonitoringSupported()) {
            l = info.getWaitedTime();
            if(l == -1) l = 0;
        }
        return new Long(l);
    }
    public Long getJvmThreadInstWaitCount() throws SnmpStatusException {
        return new Long(info.getWaitedCount());
    }
    public Byte[] getJvmThreadInstState()
        throws SnmpStatusException {
        return ThreadStateMap.getState(info);
    }
    public Long getJvmThreadInstId() throws SnmpStatusException {
        return new Long(info.getThreadId());
    }
    public Byte[] getJvmThreadInstIndex() throws SnmpStatusException {
        return index;
    }
    private String getJvmThreadInstStackTrace() throws SnmpStatusException {
        StackTraceElement[] stackTrace = info.getStackTrace();
        StringBuffer b = new StringBuffer();
        final int stackSize = stackTrace.length;
        log.debug("getJvmThreadInstStackTrace", "Stack size : " + stackSize);
        for(int i = 0; i < stackSize; i++) {
            log.debug("getJvmThreadInstStackTrace", "Append " +
                      stackTrace[i].toString());
            b.append(stackTrace[i].toString());
            if(i < stackSize)
                b.append("\n");
        }
        return validPathElementTC(b.toString());
    }
    static final MibLogger log =
        new MibLogger(JvmThreadInstanceEntryImpl.class);
}
