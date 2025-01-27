class EventRequestManagerImpl extends MirrorImpl
                                       implements EventRequestManager
{
    List[] requestLists;
    private static int methodExitEventCmd = 0;
    static int JDWPtoJDISuspendPolicy(byte jdwpPolicy) {
        switch(jdwpPolicy) {
            case JDWP.SuspendPolicy.ALL:
                return EventRequest.SUSPEND_ALL;
            case JDWP.SuspendPolicy.EVENT_THREAD:
                return EventRequest.SUSPEND_EVENT_THREAD;
        case JDWP.SuspendPolicy.NONE:
                return EventRequest.SUSPEND_NONE;
            default:
                throw new IllegalArgumentException("Illegal policy constant: " + jdwpPolicy);
        }
    }
    static byte JDItoJDWPSuspendPolicy(int jdiPolicy) {
        switch(jdiPolicy) {
            case EventRequest.SUSPEND_ALL:
                return JDWP.SuspendPolicy.ALL;
            case EventRequest.SUSPEND_EVENT_THREAD:
                return JDWP.SuspendPolicy.EVENT_THREAD;
            case EventRequest.SUSPEND_NONE:
                return JDWP.SuspendPolicy.NONE;
            default:
                throw new IllegalArgumentException("Illegal policy constant: " + jdiPolicy);
        }
    }
    public boolean equals(Object obj) {
        return this == obj;
    }
    public int hashCode() {
        return System.identityHashCode(this);
    }
    abstract class EventRequestImpl extends MirrorImpl implements EventRequest {
        int id;
        List filters = new ArrayList();
        boolean isEnabled = false;
        boolean deleted = false;
        byte suspendPolicy = JDWP.SuspendPolicy.ALL;
        private Map<Object, Object> clientProperties = null;
        EventRequestImpl() {
            super(EventRequestManagerImpl.this.vm);
        }
        public boolean equals(Object obj) {
            return this == obj;
        }
        public int hashCode() {
            return System.identityHashCode(this);
        }
        abstract int eventCmd();
        InvalidRequestStateException invalidState() {
            return new InvalidRequestStateException(toString());
        }
        String state() {
            return deleted? " (deleted)" :
                (isEnabled()? " (enabled)" : " (disabled)");
        }
        List requestList() {
            return EventRequestManagerImpl.this.requestList(eventCmd());
        }
        void delete() {
            if (!deleted) {
                requestList().remove(this);
                disable(); 
                deleted = true;
            }
        }
        public boolean isEnabled() {
            return isEnabled;
        }
        public void enable() {
            setEnabled(true);
        }
        public void disable() {
            setEnabled(false);
        }
        public synchronized void setEnabled(boolean val) {
            if (deleted) {
                throw invalidState();
            } else {
                if (val != isEnabled) {
                    if (isEnabled) {
                        clear();
                    } else {
                        set();
                    }
                }
            }
        }
        public synchronized void addCountFilter(int count) {
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            if (count < 1) {
                throw new IllegalArgumentException("count is less than one");
            }
            filters.add(JDWP.EventRequest.Set.Modifier.Count.create(count));
        }
        public void setSuspendPolicy(int policy) {
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            suspendPolicy = JDItoJDWPSuspendPolicy(policy);
        }
        public int suspendPolicy() {
            return JDWPtoJDISuspendPolicy(suspendPolicy);
        }
        synchronized void set() {
            JDWP.EventRequest.Set.Modifier[] mods =
                (JDWP.EventRequest.Set.Modifier[])
                filters.toArray(
                    new JDWP.EventRequest.Set.Modifier[filters.size()]);
            try {
                id = JDWP.EventRequest.Set.process(vm, (byte)eventCmd(),
                                                   suspendPolicy, mods).requestID;
            } catch (JDWPException exc) {
                throw exc.toJDIException();
            }
            isEnabled = true;
        }
        synchronized void clear() {
            try {
                JDWP.EventRequest.Clear.process(vm, (byte)eventCmd(), id);
            } catch (JDWPException exc) {
                throw exc.toJDIException();
            }
            isEnabled = false;
        }
        private Map<Object, Object> getProperties() {
            if (clientProperties == null) {
                clientProperties = new HashMap<Object, Object>(2);
            }
            return clientProperties;
        }
        public final Object getProperty(Object key) {
            if (clientProperties == null) {
                return null;
            } else {
                return getProperties().get(key);
            }
        }
        public final void putProperty(Object key, Object value) {
            if (value != null) {
                getProperties().put(key, value);
            } else {
                getProperties().remove(key);
            }
        }
    }
    abstract class ThreadVisibleEventRequestImpl extends EventRequestImpl {
        public synchronized void addThreadFilter(ThreadReference thread) {
            validateMirror(thread);
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            filters.add(JDWP.EventRequest.Set.Modifier.ThreadOnly
                                      .create((ThreadReferenceImpl)thread));
        }
    }
    abstract class ClassVisibleEventRequestImpl
                                  extends ThreadVisibleEventRequestImpl {
        public synchronized void addClassFilter(ReferenceType clazz) {
            validateMirror(clazz);
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            filters.add(JDWP.EventRequest.Set.Modifier.ClassOnly
                                      .create((ReferenceTypeImpl)clazz));
        }
        public synchronized void addClassFilter(String classPattern) {
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            if (classPattern == null) {
                throw new NullPointerException();
            }
            filters.add(JDWP.EventRequest.Set.Modifier.ClassMatch
                                      .create(classPattern));
        }
        public synchronized void addClassExclusionFilter(String classPattern) {
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            if (classPattern == null) {
                throw new NullPointerException();
            }
            filters.add(JDWP.EventRequest.Set.Modifier.ClassExclude
                                      .create(classPattern));
        }
        public synchronized void addInstanceFilter(ObjectReference instance) {
            validateMirror(instance);
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            if (!vm.canUseInstanceFilters()) {
                throw new UnsupportedOperationException(
                     "target does not support instance filters");
            }
            filters.add(JDWP.EventRequest.Set.Modifier.InstanceOnly
                                      .create((ObjectReferenceImpl)instance));
        }
    }
    class BreakpointRequestImpl extends ClassVisibleEventRequestImpl
                                     implements BreakpointRequest {
        private final Location location;
        BreakpointRequestImpl(Location location) {
            this.location = location;
            filters.add(0,JDWP.EventRequest.Set.Modifier.LocationOnly
                                                 .create(location));
            requestList().add(this);
        }
        public Location location() {
            return location;
        }
        int eventCmd() {
            return JDWP.EventKind.BREAKPOINT;
        }
        public String toString() {
            return "breakpoint request " + location() + state();
        }
    }
    class ClassPrepareRequestImpl extends ClassVisibleEventRequestImpl
                                     implements ClassPrepareRequest {
        ClassPrepareRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.CLASS_PREPARE;
        }
        public synchronized void addSourceNameFilter(String sourceNamePattern) {
            if (isEnabled() || deleted) {
                throw invalidState();
            }
            if (!vm.canUseSourceNameFilters()) {
                throw new UnsupportedOperationException(
                     "target does not support source name filters");
            }
            if (sourceNamePattern == null) {
                throw new NullPointerException();
            }
            filters.add(JDWP.EventRequest.Set.Modifier.SourceNameMatch
                                      .create(sourceNamePattern));
        }
        public String toString() {
            return "class prepare request " + state();
        }
    }
    class ClassUnloadRequestImpl extends ClassVisibleEventRequestImpl
                                     implements ClassUnloadRequest {
        ClassUnloadRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.CLASS_UNLOAD;
        }
        public String toString() {
            return "class unload request " + state();
        }
    }
    class ExceptionRequestImpl extends ClassVisibleEventRequestImpl
                                      implements ExceptionRequest {
        ReferenceType exception = null;
        boolean caught = true;
        boolean uncaught = true;
        ExceptionRequestImpl(ReferenceType refType,
                          boolean notifyCaught, boolean notifyUncaught) {
            exception = refType;
            caught = notifyCaught;
            uncaught = notifyUncaught;
            {
                ReferenceTypeImpl exc;
                if (exception == null) {
                    exc = new ClassTypeImpl(vm, 0);
                } else {
                    exc = (ReferenceTypeImpl)exception;
                }
                filters.add(JDWP.EventRequest.Set.Modifier.ExceptionOnly.
                            create(exc, caught, uncaught));
            }
            requestList().add(this);
        }
        public ReferenceType exception() {
            return exception;
        }
        public boolean notifyCaught() {
            return caught;
        }
        public boolean notifyUncaught() {
            return uncaught;
        }
        int eventCmd() {
            return JDWP.EventKind.EXCEPTION;
        }
        public String toString() {
            return "exception request " + exception() + state();
        }
    }
    class MethodEntryRequestImpl extends ClassVisibleEventRequestImpl
                                      implements MethodEntryRequest {
        MethodEntryRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.METHOD_ENTRY;
        }
        public String toString() {
            return "method entry request " + state();
        }
    }
    class MethodExitRequestImpl extends ClassVisibleEventRequestImpl
                                      implements MethodExitRequest {
        MethodExitRequestImpl() {
            if (methodExitEventCmd == 0) {
                if (vm.canGetMethodReturnValues()) {
                    methodExitEventCmd = JDWP.EventKind.METHOD_EXIT_WITH_RETURN_VALUE;
                } else {
                    methodExitEventCmd = JDWP.EventKind.METHOD_EXIT;
                }
            }
            requestList().add(this);
        }
        int eventCmd() {
            return EventRequestManagerImpl.methodExitEventCmd;
        }
        public String toString() {
            return "method exit request " + state();
        }
    }
    class MonitorContendedEnterRequestImpl extends ClassVisibleEventRequestImpl
                                      implements MonitorContendedEnterRequest {
        MonitorContendedEnterRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.MONITOR_CONTENDED_ENTER;
        }
        public String toString() {
            return "monitor contended enter request " + state();
        }
    }
    class MonitorContendedEnteredRequestImpl extends ClassVisibleEventRequestImpl
                                      implements MonitorContendedEnteredRequest {
        MonitorContendedEnteredRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.MONITOR_CONTENDED_ENTERED;
        }
        public String toString() {
            return "monitor contended entered request " + state();
        }
    }
    class MonitorWaitRequestImpl extends ClassVisibleEventRequestImpl
                                 implements MonitorWaitRequest {
        MonitorWaitRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.MONITOR_WAIT;
        }
        public String toString() {
            return "monitor wait request " + state();
        }
    }
    class MonitorWaitedRequestImpl extends ClassVisibleEventRequestImpl
                                 implements MonitorWaitedRequest {
        MonitorWaitedRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.MONITOR_WAITED;
        }
        public String toString() {
            return "monitor waited request " + state();
        }
    }
    class StepRequestImpl extends ClassVisibleEventRequestImpl
                                      implements StepRequest {
        ThreadReferenceImpl thread;
        int size;
        int depth;
        StepRequestImpl(ThreadReference thread, int size, int depth) {
            this.thread = (ThreadReferenceImpl)thread;
            this.size = size;
            this.depth = depth;
            int jdwpSize;
            switch (size) {
                case STEP_MIN:
                    jdwpSize = JDWP.StepSize.MIN;
                    break;
                case STEP_LINE:
                    jdwpSize = JDWP.StepSize.LINE;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid step size");
            }
            int jdwpDepth;
            switch (depth) {
                case STEP_INTO:
                    jdwpDepth = JDWP.StepDepth.INTO;
                    break;
                case STEP_OVER:
                    jdwpDepth = JDWP.StepDepth.OVER;
                    break;
                case STEP_OUT:
                    jdwpDepth = JDWP.StepDepth.OUT;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid step depth");
            }
            List requests = stepRequests();
            Iterator iter = requests.iterator();
            while (iter.hasNext()) {
                StepRequest request = (StepRequest)iter.next();
                if ((request != this) &&
                        request.isEnabled() &&
                        request.thread().equals(thread)) {
                    throw new DuplicateRequestException(
                        "Only one step request allowed per thread");
                }
            }
            filters.add(JDWP.EventRequest.Set.Modifier.Step.
                        create(this.thread, jdwpSize, jdwpDepth));
            requestList().add(this);
        }
        public int depth() {
            return depth;
        }
        public int size() {
            return size;
        }
        public ThreadReference thread() {
            return thread;
        }
        int eventCmd() {
            return JDWP.EventKind.SINGLE_STEP;
        }
        public String toString() {
            return "step request " + thread() + state();
        }
    }
    class ThreadDeathRequestImpl extends ThreadVisibleEventRequestImpl
                                      implements ThreadDeathRequest {
        ThreadDeathRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.THREAD_DEATH;
        }
        public String toString() {
            return "thread death request " + state();
        }
    }
    class ThreadStartRequestImpl extends ThreadVisibleEventRequestImpl
                                      implements ThreadStartRequest {
        ThreadStartRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.THREAD_START;
        }
        public String toString() {
            return "thread start request " + state();
        }
    }
    abstract class WatchpointRequestImpl extends ClassVisibleEventRequestImpl
                                      implements WatchpointRequest {
        final Field field;
        WatchpointRequestImpl(Field field) {
            this.field = field;
            filters.add(0,
                   JDWP.EventRequest.Set.Modifier.FieldOnly.create(
                    (ReferenceTypeImpl)field.declaringType(),
                    ((FieldImpl)field).ref()));
        }
        public Field field() {
            return field;
        }
    }
    class AccessWatchpointRequestImpl extends WatchpointRequestImpl
                                  implements AccessWatchpointRequest {
        AccessWatchpointRequestImpl(Field field) {
            super(field);
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.FIELD_ACCESS;
        }
        public String toString() {
            return "access watchpoint request " + field + state();
        }
    }
    class ModificationWatchpointRequestImpl extends WatchpointRequestImpl
                                  implements ModificationWatchpointRequest {
        ModificationWatchpointRequestImpl(Field field) {
            super(field);
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.FIELD_MODIFICATION;
        }
        public String toString() {
            return "modification watchpoint request " + field + state();
        }
    }
    class VMDeathRequestImpl extends EventRequestImpl
                                        implements VMDeathRequest {
        VMDeathRequestImpl() {
            requestList().add(this);
        }
        int eventCmd() {
            return JDWP.EventKind.VM_DEATH;
        }
        public String toString() {
            return "VM death request " + state();
        }
    }
    EventRequestManagerImpl(VirtualMachine vm) {
        super(vm);
        java.lang.reflect.Field[] ekinds =
            JDWP.EventKind.class.getDeclaredFields();
        int highest = 0;
        for (int i = 0; i < ekinds.length; ++i) {
            int val;
            try {
                val = ekinds[i].getInt(null);
            } catch (IllegalAccessException exc) {
                throw new RuntimeException("Got: " + exc);
            }
            if (val > highest) {
                highest = val;
            }
        }
        requestLists = new List[highest+1];
        for (int i=0; i <= highest; i++) {
            requestLists[i] = new ArrayList();
        }
    }
    public ClassPrepareRequest createClassPrepareRequest() {
        return new ClassPrepareRequestImpl();
    }
    public ClassUnloadRequest createClassUnloadRequest() {
        return new ClassUnloadRequestImpl();
    }
    public ExceptionRequest createExceptionRequest(ReferenceType refType,
                                                   boolean notifyCaught,
                                                   boolean notifyUncaught) {
        validateMirrorOrNull(refType);
        return new ExceptionRequestImpl(refType, notifyCaught, notifyUncaught);
    }
    public StepRequest createStepRequest(ThreadReference thread,
                                         int size, int depth) {
        validateMirror(thread);
        return new StepRequestImpl(thread, size, depth);
    }
    public ThreadDeathRequest createThreadDeathRequest() {
        return new ThreadDeathRequestImpl();
    }
    public ThreadStartRequest createThreadStartRequest() {
        return new ThreadStartRequestImpl();
    }
    public MethodEntryRequest createMethodEntryRequest() {
        return new MethodEntryRequestImpl();
    }
    public MethodExitRequest createMethodExitRequest() {
        return new MethodExitRequestImpl();
    }
    public MonitorContendedEnterRequest createMonitorContendedEnterRequest() {
        if (!vm.canRequestMonitorEvents()) {
            throw new UnsupportedOperationException(
          "target VM does not support requesting Monitor events");
        }
        return new MonitorContendedEnterRequestImpl();
    }
    public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest() {
        if (!vm.canRequestMonitorEvents()) {
            throw new UnsupportedOperationException(
          "target VM does not support requesting Monitor events");
        }
        return new MonitorContendedEnteredRequestImpl();
    }
    public MonitorWaitRequest createMonitorWaitRequest() {
        if (!vm.canRequestMonitorEvents()) {
            throw new UnsupportedOperationException(
          "target VM does not support requesting Monitor events");
        }
        return new MonitorWaitRequestImpl();
    }
    public MonitorWaitedRequest createMonitorWaitedRequest() {
        if (!vm.canRequestMonitorEvents()) {
            throw new UnsupportedOperationException(
          "target VM does not support requesting Monitor events");
        }
        return new MonitorWaitedRequestImpl();
    }
    public BreakpointRequest createBreakpointRequest(Location location) {
        validateMirror(location);
        if (location.codeIndex() == -1) {
            throw new NativeMethodException("Cannot set breakpoints on native methods");
        }
        return new BreakpointRequestImpl(location);
    }
    public AccessWatchpointRequest
                              createAccessWatchpointRequest(Field field) {
        validateMirror(field);
        if (!vm.canWatchFieldAccess()) {
            throw new UnsupportedOperationException(
          "target VM does not support access watchpoints");
        }
        return new AccessWatchpointRequestImpl(field);
    }
    public ModificationWatchpointRequest
                        createModificationWatchpointRequest(Field field) {
        validateMirror(field);
        if (!vm.canWatchFieldModification()) {
            throw new UnsupportedOperationException(
          "target VM does not support modification watchpoints");
        }
        return new ModificationWatchpointRequestImpl(field);
    }
    public VMDeathRequest createVMDeathRequest() {
        if (!vm.canRequestVMDeathEvent()) {
            throw new UnsupportedOperationException(
          "target VM does not support requesting VM death events");
        }
        return new VMDeathRequestImpl();
    }
    public void deleteEventRequest(EventRequest eventRequest) {
        validateMirror(eventRequest);
        ((EventRequestImpl)eventRequest).delete();
    }
    public void deleteEventRequests(List<? extends EventRequest> eventRequests) {
        validateMirrors(eventRequests);
        Iterator iter = (new ArrayList(eventRequests)).iterator();
        while (iter.hasNext()) {
            ((EventRequestImpl)iter.next()).delete();
        }
    }
    public void deleteAllBreakpoints() {
        requestList(JDWP.EventKind.BREAKPOINT).clear();
        try {
            JDWP.EventRequest.ClearAllBreakpoints.process(vm);
        } catch (JDWPException exc) {
            throw exc.toJDIException();
        }
    }
    public List<StepRequest> stepRequests() {
        return unmodifiableRequestList(JDWP.EventKind.SINGLE_STEP);
    }
    public List<ClassPrepareRequest> classPrepareRequests() {
        return unmodifiableRequestList(JDWP.EventKind.CLASS_PREPARE);
    }
    public List<ClassUnloadRequest> classUnloadRequests() {
        return unmodifiableRequestList(JDWP.EventKind.CLASS_UNLOAD);
    }
    public List<ThreadStartRequest> threadStartRequests() {
        return unmodifiableRequestList(JDWP.EventKind.THREAD_START);
    }
    public List<ThreadDeathRequest> threadDeathRequests() {
        return unmodifiableRequestList(JDWP.EventKind.THREAD_DEATH);
    }
    public List<ExceptionRequest> exceptionRequests() {
        return unmodifiableRequestList(JDWP.EventKind.EXCEPTION);
    }
    public List<BreakpointRequest> breakpointRequests() {
        return unmodifiableRequestList(JDWP.EventKind.BREAKPOINT);
    }
    public List<AccessWatchpointRequest> accessWatchpointRequests() {
        return unmodifiableRequestList(JDWP.EventKind.FIELD_ACCESS);
    }
    public List<ModificationWatchpointRequest> modificationWatchpointRequests() {
        return unmodifiableRequestList(JDWP.EventKind.FIELD_MODIFICATION);
    }
    public List<MethodEntryRequest> methodEntryRequests() {
        return unmodifiableRequestList(JDWP.EventKind.METHOD_ENTRY);
    }
    public List<MethodExitRequest> methodExitRequests() {
        return unmodifiableRequestList(
                               EventRequestManagerImpl.methodExitEventCmd);
    }
    public List<MonitorContendedEnterRequest> monitorContendedEnterRequests() {
        return unmodifiableRequestList(JDWP.EventKind.MONITOR_CONTENDED_ENTER);
    }
    public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests() {
        return unmodifiableRequestList(JDWP.EventKind.MONITOR_CONTENDED_ENTERED);
    }
    public List<MonitorWaitRequest> monitorWaitRequests() {
        return unmodifiableRequestList(JDWP.EventKind.MONITOR_WAIT);
    }
    public List<MonitorWaitedRequest> monitorWaitedRequests() {
        return unmodifiableRequestList(JDWP.EventKind.MONITOR_WAITED);
    }
    public List<VMDeathRequest> vmDeathRequests() {
        return unmodifiableRequestList(JDWP.EventKind.VM_DEATH);
    }
    List unmodifiableRequestList(int eventCmd) {
        return Collections.unmodifiableList(requestList(eventCmd));
    }
    EventRequest request(int eventCmd, int requestId) {
        List rl = requestList(eventCmd);
        for (int i = rl.size() - 1; i >= 0; i--) {
            EventRequestImpl er = (EventRequestImpl)rl.get(i);
            if (er.id == requestId) {
                return er;
            }
        }
        return null;
    }
    List<? extends EventRequest>  requestList(int eventCmd) {
        return requestLists[eventCmd];
    }
}
