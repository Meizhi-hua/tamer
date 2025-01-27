public class CoreTestPrinter extends ResultPrinter {
    private Class<?> fLastClass;
    private int fColumn;
    private int fRunTime;
    private int fFlags;
    public CoreTestPrinter(PrintStream writer, int flags) {
        super(writer);
        fFlags = flags;
    }
    @Override
    protected void printHeader(long runTime) {
        fRunTime = (int)(runTime / 1000);
        if (fColumn != 0) {
            getWriter().println();
        }
        getWriter().println();
    }
    @Override
    protected void printFooter(TestResult result) {
        CoreTestResult coreResult = (CoreTestResult)result;
        PrintStream printer = getWriter();
        if (fColumn != 0) {
            printer.println();
        }
        printer.println();
        printer.println("Total tests   : " + coreResult.fTotalTestCount);
        printer.println("Tests run     : " + coreResult.runCount());
        printer.println("Tests ignored : " + coreResult.fIgnoredCount);
        printer.println();
        printer.println("Normal tests  : " + coreResult.fNormalTestCount);
        printer.println("Android-only  : " + coreResult.fAndroidOnlyCount);
        printer.println("Broken tests  : " + coreResult.fBrokenTestCount);
        printer.println("Known failures: " + coreResult.fKnownFailureCount);
        printer.println("Side-effects  : " + coreResult.fSideEffectCount);
        printMemory();
        int seconds = fRunTime;
        int hours = seconds / 3600;
        seconds = seconds % 3600;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        String text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        printer.println();
        printer.println("Time taken    : " + text);
        super.printFooter(result);
    }
    private void printMemory() {
        PrintStream printer = getWriter();
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        printer.println();
        printer.println("Total memory  : " + total);
        printer.println("Used memory   : " + used);
        printer.println("Free memory   : " + free);
    }
    @Override
    public void startTest(Test test) {
        TestCase caze = (TestCase)test;
        if (fLastClass == null ||
                caze.getClass().getPackage() != fLastClass.getPackage()) {
            if (fColumn != 0) {
                getWriter().println();
                fColumn = 0;
            }
            getWriter().println();
            Package pack = caze.getClass().getPackage();
            getWriter().println(pack == null ? "Default package" : 
                pack.getName());
            getWriter().println();
        }
        if ((fFlags & CoreTestSuite.VERBOSE) != 0) {
            if (caze.getClass() != fLastClass) {
                if (fColumn != 0) {
                    getWriter().println();
                    fColumn = 0;
                }
                String name = caze.getClass().getSimpleName().toString();
                printMemory();
                getWriter().println("Now executing : " + name);
                getWriter().println();
            }
        }
        getWriter().print(".");
        if (fColumn++ >= 40) {
            getWriter().println();
            fColumn= 0;
        }
        fLastClass = caze.getClass();
    }
    @Override
    public void addError(Test test, Throwable t) {
        if (t instanceof CoreTestTimeout) {
            getWriter().print("T");
        } else {
            super.addError(test, t);
        }
    }
}
