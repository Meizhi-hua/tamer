class StackOverflowCaughtTarg {
    public static void main(String[] args) {
        try {
            throw new StackOverflowError();
        } catch (Throwable exc) {
        }
    }
}
class StackOverflowUncaughtTarg {
    public static void main(String[] args) {
        thrower();
    }
    static void thrower()  {
        throw new StackOverflowError();
    }
}
class StackOverflowIndirectTarg {
    public static void main(String[] args) {
        try {
            thrower();
        } catch (Throwable exc) {
            System.out.println("Got exception: " + exc);
        }
    }
    static void thrower()  {
        throw new StackOverflowError();
    }
}
public class ExceptionEvents extends TestScaffold {
    static int failureCount = 0;
    static StringBuffer tooManySummary = new StringBuffer();
    static StringBuffer notSentSummary = new StringBuffer();
    static StringBuffer unexpectedSummary = new StringBuffer();
    static int globalSuspendPolicy = -1;
    static String[] flags;
    final String target;
    final String exceptionName;
    final boolean caught;
    final boolean uncaught;
    final int suspendPolicy;
    int eventCount = 0;
    ExceptionRequest request;
    ExceptionEvents(String target,
                    String exceptionName,
                    boolean caught, boolean uncaught,
                    int suspendPolicy) {
        super(flags);
        this.target = target;
        this.exceptionName = exceptionName;
        this.caught = caught;
        this.uncaught = uncaught;
        this.suspendPolicy = suspendPolicy;
    }
    static void everything() throws Exception {
        goNeither("StackOverflowCaughtTarg", "java.lang.Exception");
        goCaught("StackOverflowCaughtTarg", null);
        goCaught("StackOverflowCaughtTarg", "java.lang.Error");
        goCaught("StackOverflowCaughtTarg", "java.lang.StackOverflowError");
        goNeither("StackOverflowCaughtTarg", "java.lang.NullPointerException");
        goNeither("StackOverflowUncaughtTarg", "java.lang.Exception");
        goUncaught("StackOverflowUncaughtTarg", null);
        goUncaught("StackOverflowUncaughtTarg", "java.lang.Error");
        goUncaught("StackOverflowUncaughtTarg", "java.lang.StackOverflowError");
        goNeither("StackOverflowUncaughtTarg", "java.lang.NullPointerException");
    }
    static void usage() throws Exception {
        System.err.println("Use either with no arguments or");
        System.err.println("  c|u|n a|t|n <TargetClass> <Exception>|null");
        System.err.println("or for verbose folk");
        System.err.println("  caught|uncaught|neither all|thread|none <TargetClass> <Exception>|null");
        throw new IllegalArgumentException("see usage");
    }
    public static void main(String[] args) throws Exception {
        StringBuffer testName = new StringBuffer("ExceptionEvents(");
        List flagList = new ArrayList();
        List argList = new ArrayList();
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                flagList.add(arg);
            } else {
                argList.add(arg);
            }
        }
        flags = (String[])flagList.toArray(new String[0]);
        args = (String[])argList.toArray(new String[0]);
        if (args.length == 0) {
            everything();
            testName.append("Full Test");
        } else if (args.length == 4) {
            switch (args[1].toLowerCase().charAt(0)) {
            case 'a':
                globalSuspendPolicy = EventRequest.SUSPEND_ALL;
                break;
            case 't':
                globalSuspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
                break;
            case 'n':
                globalSuspendPolicy = EventRequest.SUSPEND_NONE;
                break;
            default:
                usage();
            }
            String excString = args[3];
            if (excString.equals("null")) {
                excString = null;
            }
            switch (args[0].toLowerCase().charAt(0)) {
            case 'c':
                goCaught(args[2], excString);
                break;
            case 'u':
                goUncaught(args[2], excString);
                break;
            case 'n':
                goNeither(args[2], excString);
                break;
            default:
                usage();
            }
            testName.append(args[0]);
            testName.append(" ");
            testName.append(args[1]);
            testName.append(" ");
            testName.append(args[2]);
        } else {
            usage();
        }
        testName.append(")");
        summarize(testName.toString());
    }
    static void summarize(String testName) throws Exception {
        if (tooManySummary.length() > 0) {
            System.out.println("\nSummary of tests with too many events:\n" +
                               tooManySummary.toString());
        }
        if (notSentSummary.length() > 0) {
            System.out.println("\nSummary of tests with expected events not sent:\n" +
                               notSentSummary.toString());
        }
        if (unexpectedSummary.length() > 0) {
            System.out.println("\nSummary of tests with events when none expected:\n" +
                               unexpectedSummary.toString());
        }
        if (failureCount > 0) {
            throw new Exception(testName + " FAILED " +
                                failureCount + " tests!");
        } else {
            System.out.println("\n" + testName + " test PASSED");
       }
    }
    static void goCaught(String target,
                         String exceptionName) throws Exception {
        goSuspendPolicy(target, true, exceptionName,
                        true, true);
        goSuspendPolicy(target, true, exceptionName,
                        true, false);
        goSuspendPolicy(target, false, exceptionName,
                        false, true);
        goSuspendPolicy(target, false, exceptionName,
                        false, false);
    }
    static void goUncaught(String target,
                           String exceptionName) throws Exception {
        goSuspendPolicy(target, true, exceptionName,
                        true, true);
        goSuspendPolicy(target, true, exceptionName,
                        false, true);
        goSuspendPolicy(target, false, exceptionName,
                        true, false);
        goSuspendPolicy(target, false, exceptionName,
                        false, false);
    }
    static void goNeither(String target,
                           String exceptionName) throws Exception {
        goSuspendPolicy(target, false, exceptionName,
                        true, true);
        goSuspendPolicy(target, false, exceptionName,
                        false, true);
        goSuspendPolicy(target, false, exceptionName,
                        true, false);
        goSuspendPolicy(target, false, exceptionName,
                        false, false);
    }
    static void goSuspendPolicy(String target,
                   boolean expectedEvent,
                   String exceptionName,
                   boolean caught, boolean uncaught) throws Exception {
        if (globalSuspendPolicy != -1) {
            go(target, expectedEvent, exceptionName,
               caught, uncaught, globalSuspendPolicy);
        } else {
            go(target, expectedEvent, exceptionName,
               caught, uncaught, EventRequest.SUSPEND_ALL);
            go(target, expectedEvent, exceptionName,
               caught, uncaught, EventRequest.SUSPEND_EVENT_THREAD);
            go(target, expectedEvent, exceptionName,
               caught, uncaught, EventRequest.SUSPEND_NONE);
        }
    }
    static void go(String target,
                   boolean expectedEvent,
                   String exceptionName,
                   boolean caught, boolean uncaught,
                   int suspendPolicy) throws Exception {
        String description = target + " with " +
                           exceptionName +
                           "/caught=" + caught +
                           "/uncaught=" + uncaught +
                           " suspend=";
        switch (suspendPolicy) {
        case EventRequest.SUSPEND_ALL:
            description += "All";
            break;
        case EventRequest.SUSPEND_EVENT_THREAD:
            description += "Thread";
            break;
        case EventRequest.SUSPEND_NONE:
            description += "None";
            break;
        default:
            throw new Exception("Test failure: bad suspend policy - " +
                                suspendPolicy);
        }
        description += "\n";
        System.out.print("\nTesting " + description);
        ExceptionEvents aRun = new ExceptionEvents(
                             target,
                             exceptionName, caught, uncaught,
                             suspendPolicy);
        aRun.startTests();
        aRun.analyse(expectedEvent, description);
    }
    void analyse(boolean expectedEvent, String description) {
        if (expectedEvent) {
            if (eventCount == 1) {
                println("pass: got expected event");
            } else if (eventCount > 1) {
                failure("FAILURE: expected only one event got: " +
                                   eventCount);
                tooManySummary.append(description);
                ++failureCount;
            } else {
                failure("FAILURE: expected event not sent");
                notSentSummary.append(description);
                ++failureCount;
            }
        } else {
            if (eventCount > 0) {
                failure("FAILURE: unexpected event sent");
                unexpectedSummary.append(description);
                ++failureCount;
            } else {
                println("pass: as expected no event sent");
            }
        }
    }
    public void exceptionThrown(ExceptionEvent event) {
        if (event.request() == request) {
            try {
                System.out.print("ExceptionEvent: ");
                System.out.print("" + event.exception().referenceType().name());
                Location loc = event.location();
                System.out.print(" @ " + loc.method().name());
                System.out.print(":" + loc.lineNumber());
            } catch (VMDisconnectedException exc) {
                System.out.print("Oops - " + exc.toString());
            }
            System.out.println();
            eventCount++;
        } else {
            System.out.print("alien exception: ");
            try {
                println(event.toString());
            } catch (VMDisconnectedException exc) {
                println("Oops - " + exc.toString());
            }
        }
    }
    protected void createDefaultExceptionRequest() {
    }
    protected void runTests() throws Exception {
        startToMain(target);
        ReferenceType exceptionClass;
        if (exceptionName == null) {
            exceptionClass = null;
        } else {
            exceptionClass = findReferenceType(exceptionName);
            if (exceptionName == null) {
                throw new Exception("test failure - cannot find: " +
                                    exceptionName);
            }
        }
        request = eventRequestManager().createExceptionRequest(exceptionClass,
                                                               caught, uncaught);
        request.addClassExclusionFilter("java.*");
        request.addClassExclusionFilter("sun.*");
        request.addClassExclusionFilter("com.sun.*");
        request.setSuspendPolicy(suspendPolicy);
        request.enable();
        listenUntilVMDisconnect();
    }
}
