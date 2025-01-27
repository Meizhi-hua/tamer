public class PerfStatCollector implements TestListener {
    public boolean listAll = false;
    public boolean listBad = false;
    public long thresholdDuration = 3600 * 1000; 
    public boolean twoLines = true;
    public boolean bigMarking = true;
    private static boolean havePreciseTime =
        VMDebug.threadCpuTimeNanos() != -1;
    public class Item {
        Test test;
        long startTime, duration;
        int res;
        public boolean existsInStore;
        public int id;
        public int bestRes;
        public long lastBestAt;
        public int lastRes;
        public long lastDuration;
        public int statCount;
        public double statAvgDuration;
        public long statMinDuration;
        public long statMaxDuration;
        int adhocRelevance;
        public int histRelevance;
        public boolean isTransition;
        boolean printed = false;
        void update(boolean rBad, long rthDurat) {
            if (rBad && (res != 0)) {
                adhocRelevance = 2;
            }
            else if (duration >= rthDurat) {
                adhocRelevance = 1;
            }
            else {
                adhocRelevance = 0;
            }
            StatsStore.use1(this);
        }
        void print1(PrintStream out, boolean bigMarking) {
            switch (histRelevance) {
            case -4:
                if (bigMarking) {
                    out.println();
                    out.println("*** *** *** *** *** ATTENTION *** *** *** *** ***");
                    out.println("*** *** *** *** *** ATTENTION *** *** *** *** ***");
                    out.println("*** *** *** *** *** ATTENTION *** *** *** *** ***");
                    out.println("Test ran SUCCESSFULLY once, but NOT this time!!!!");
                    out.println("*** *** *** *** *** ATTENTION *** *** *** *** ***");
                    out.println("*** *** *** *** *** ATTENTION *** *** *** *** ***");
                    out.println("*** *** *** *** *** ATTENTION *** *** *** *** ***");
                }
                out.print("-4 VBAD"); break;
            case 4: out.print(" 4 Good"); break;
            case 3: out.print(" 3 good"); break;
            case -2: out.print("-2 SLOW"); break;
            case 2: out.print(" 2 Fast"); break;
            case 1: out.print(" 1 fast"); break;
            case -3:
                if (res == -2) out.print("-3 FAIL");
                else out.print("-3 ERR ");
                break;
            default:
                if (res == 0) out.print("       ");
                else if (res == -2) out.print("   fail");
                else out.print("   err ");
            }
            if (isTransition) out.print("! ");
            else out.print("  ");
            out.print(test.toString());
            out.format(": %d# %d(%d) [%d..%d] %.1f ms",
                    statCount, duration, lastDuration,
                    statMinDuration, statMaxDuration, statAvgDuration);
            out.println();
            printed = true;
        }
        void print2(PrintStream out, boolean bigMarking) {
            out.format("%5d. ", id);
            out.println(test.toString());
            out.print("       ");
            switch (histRelevance) {
                case -4: out.print("FAIL"); break;
                case 4: out.print("PASS"); break;
                case 3: out.print("PASS"); break;
                case -2: out.print("SLOW"); break;
                case 2: out.print("FAST"); break;
                case 1: out.print("PASS"); break;
                case -3:
                    if (res == -2) out.print("FAIL");
                    else out.print("ERR ");
                    break;
                default:
                    if (res == 0) out.print("PASS");
                    else if (res == -2) out.print("FAIL");
                    else out.print("XCPT");
            }
            out.format(" %d ms (min %d ms, max %d ms, avg %#.1f ms, %d runs)",
                    duration,
                    statMinDuration, statMaxDuration, statAvgDuration,
                    statCount);
            out.println();
            printed = true;
        }
        void print(PrintStream out, boolean bigMarking) {
            if (twoLines) print2(out, bigMarking);
            else print1(out, bigMarking);
        }
        boolean checkPrint(PrintStream out) {
            if (printed) return false;
            print(out, false);
            return true;
        }
    }
    ArrayList<Item> items;
    Item current;
    PrintStream fWriter;
    int fColumn= 0;
    public PerfStatCollector(PrintStream writer)  {
        fWriter= writer;
        items = new ArrayList();
    }
    synchronized void digest() {
        int totalCnt = 0;
        int adhocRelevantCnt = 0;
        int histRelevantCnt = 0;
        long evalStartTime = System.currentTimeMillis();
        PrintStream out = fWriter;
        out.println("Failure and Performance Statistics:");
        Iterator<Item> r = items.iterator();
        while (r.hasNext()) {
            Item item = r.next();
            item.update(listBad, thresholdDuration);
            if (item.histRelevance != 0) {
                item.print(out, bigMarking);
                histRelevantCnt++;
            }
            if (item.adhocRelevance != 0) {
                if (item.checkPrint(out))
                    adhocRelevantCnt++;
            }
            if (listAll) item.checkPrint(out);
            totalCnt++;
        }
        long evalDuration = System.currentTimeMillis() - evalStartTime;
        out.println();
        out.print(totalCnt); out.println(" tests run totally.");
        out.print(histRelevantCnt);
                out.println(" tests listed due to historical relevance.");
        out.println();
        out.print("Time used in Statistics Acquisition: ");
        out.print(evalDuration);
        out.print("ms");
        out.println();
    }
    public PrintStream getWriter() {
        return fWriter;
    }
    public void addError(Test test, Throwable t) {
        current.res = -1;
    }
    public void addFailure(Test test, AssertionFailedError t) {
        current.res = -2;
    }
    public void startTest(Test test) {
        System.gc();
        current = new Item();
        current.test = test;
        current.startTime = currentTimeMillis();
        items.add(current);
    }
    public void endTest(Test test) {
        current.duration = currentTimeMillis() - current.startTime;
    }
    private long currentTimeMillis() {
        if (havePreciseTime) {
            return VMDebug.threadCpuTimeNanos() / 1000;
        } else {
            return System.currentTimeMillis();
        }
    }
}
