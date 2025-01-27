class CountFilterTarg {
    void thisOne() {
    }
    void butNotThisOne() {
    }
    void norThisOne() {
    }
    void butThisOne() {
    }
    public static void main(String[] args){
        CountFilterTarg cft = new CountFilterTarg();
        System.out.println("Hi! Hi! Hello...");
        cft.thisOne();
        cft.butNotThisOne();
        cft.norThisOne();
        cft.butThisOne();
        System.out.println("Goodbye from CountFilterTarg!");
    }
}
public class CountFilterTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    EventRequestManager erm;
    Map whereMap = new HashMap();
    CountFilterTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new CountFilterTest(args).startTests();
    }
    public void breakpointReached(BreakpointEvent event) {
        println("Got BreakpointEvent");
        locatableEvent(event, event.location());
    }
    public void methodEntered(MethodEntryEvent event) {
        println("Got MethodEntryEvent");
        locatableEvent(event, event.location());
    }
    public void methodExited(MethodExitEvent event) {
        println("Got MethodExitEvent");
        locatableEvent(event, event.location());
    }
    public void locatableEvent(Event event, Location loc) {
        String got = loc.method().name();
        String expected = (String)whereMap.get(event.request());
        if (!got.equals(expected)) {
            failure("FAIL: expected event in " + expected +
                    " but it occurred in " + got);
        }
    }
    BreakpointRequest breakpointAtMethod(String methodName)
                                           throws Exception {
        List meths = targetClass.methodsByName(methodName);
        if (meths.size() != 1) {
            throw new Exception("test error: should be one " +
                                methodName);
        }
        Method meth = (Method)meths.get(0);
        return erm.createBreakpointRequest(meth.location());
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("CountFilterTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        erm = vm().eventRequestManager();
        ThreadReference otherThread = null;
        for (Iterator it = vm().allThreads().iterator();
                       it.hasNext(); ) {
            ThreadReference tr = (ThreadReference)it.next();
            if (!tr.equals(mainThread)) {
                otherThread = tr;
                break;
            }
        }
        if (otherThread == null) {
            throw new Exception("test error: couldn't find " +
                                "other thread");
        }
        MethodEntryRequest meRequest =
            erm.createMethodEntryRequest();
        meRequest.addClassFilter("CountFilterTarg");
        meRequest.addCountFilter(5);  
        meRequest.enable();
        whereMap.put(meRequest, "butThisOne");
        MethodExitRequest mxRequest =
            erm.createMethodExitRequest();
        mxRequest.addCountFilter(2);
        mxRequest.addClassFilter("borp");
        mxRequest.enable();
        whereMap.put(mxRequest, "nowhere (from method exit)");
        BreakpointRequest thisOneRequest =
            breakpointAtMethod("thisOne");
        thisOneRequest.addCountFilter(1);
        thisOneRequest.addThreadFilter(mainThread);
        thisOneRequest.enable();
        whereMap.put(thisOneRequest, "thisOne");
        BreakpointRequest butNotThisOneRequest =
            breakpointAtMethod("butNotThisOne");
        butNotThisOneRequest.addCountFilter(1);
        butNotThisOneRequest.addThreadFilter(otherThread);
        butNotThisOneRequest.enable();
        whereMap.put(butNotThisOneRequest,
                     "nowhere (post filter)");
        BreakpointRequest norThisOneRequest =
            breakpointAtMethod("norThisOne");
        norThisOneRequest.addThreadFilter(otherThread);
        norThisOneRequest.addCountFilter(1);
        norThisOneRequest.enable();
        whereMap.put(norThisOneRequest,
                     "nowhere (pre filter)");
        BreakpointRequest butThisOneRequest =
            breakpointAtMethod("butThisOne");
        butThisOneRequest.addThreadFilter(mainThread);
        butThisOneRequest.addCountFilter(1);
        butThisOneRequest.enable();
        whereMap.put(butThisOneRequest, "butThisOne");
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("CountFilterTest: passed");
        } else {
            throw new Exception("CountFilterTest: failed");
        }
    }
}
