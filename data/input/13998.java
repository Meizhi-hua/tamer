public class SwapMicroBenchmark {
    abstract static class Job {
        private final String name;
        public Job(String name) { this.name = name; }
        public String name() { return name; }
        public abstract void work() throws Throwable;
    }
    private static void collectAllGarbage() {
        final java.util.concurrent.CountDownLatch drained
            = new java.util.concurrent.CountDownLatch(1);
        try {
            System.gc();        
            new Object() { protected void finalize() {
                drained.countDown(); }};
            System.gc();        
            drained.await();    
            System.gc();        
        } catch (InterruptedException e) { throw new Error(e); }
    }
    private static long[] time0(Job ... jobs) throws Throwable {
        final long warmupNanos = 10L * 1000L * 1000L * 1000L;
        long[] nanoss = new long[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            collectAllGarbage();
            long t0 = System.nanoTime();
            long t;
            int j = 0;
            do { jobs[i].work(); j++; }
            while ((t = System.nanoTime() - t0) < warmupNanos);
            nanoss[i] = t/j;
        }
        return nanoss;
    }
    private static void time(Job ... jobs) throws Throwable {
        long[] warmup = time0(jobs); 
        long[] nanoss = time0(jobs); 
        long[] milliss = new long[jobs.length];
        double[] ratios = new double[jobs.length];
        final String nameHeader   = "Method";
        final String millisHeader = "Millis";
        final String ratioHeader  = "Ratio";
        int nameWidth   = nameHeader.length();
        int millisWidth = millisHeader.length();
        int ratioWidth  = ratioHeader.length();
        for (int i = 0; i < jobs.length; i++) {
            nameWidth = Math.max(nameWidth, jobs[i].name().length());
            milliss[i] = nanoss[i]/(1000L * 1000L);
            millisWidth = Math.max(millisWidth,
                                   String.format("%d", milliss[i]).length());
            ratios[i] = (double) nanoss[i] / (double) nanoss[0];
            ratioWidth = Math.max(ratioWidth,
                                  String.format("%.3f", ratios[i]).length());
        }
        String format = String.format("%%-%ds %%%dd %%%d.3f%%n",
                                      nameWidth, millisWidth, ratioWidth);
        String headerFormat = String.format("%%-%ds %%%ds %%%ds%%n",
                                            nameWidth, millisWidth, ratioWidth);
        System.out.printf(headerFormat, "Method", "Millis", "Ratio");
        for (int i = 0; i < jobs.length; i++)
            System.out.printf(format, jobs[i].name(), milliss[i], ratios[i]);
    }
    private static String keywordValue(String[] args, String keyword) {
        for (String arg : args)
            if (arg.startsWith(keyword))
                return arg.substring(keyword.length() + 1);
        return null;
    }
    private static int intArg(String[] args, String keyword, int defaultValue) {
        String val = keywordValue(args, keyword);
        return val == null ? defaultValue : Integer.parseInt(val);
    }
    private static Pattern patternArg(String[] args, String keyword) {
        String val = keywordValue(args, keyword);
        return val == null ? null : Pattern.compile(val);
    }
    private static Job[] filter(Pattern filter, Job[] jobs) {
        if (filter == null) return jobs;
        Job[] newJobs = new Job[jobs.length];
        int n = 0;
        for (Job job : jobs)
            if (filter.matcher(job.name()).find())
                newJobs[n++] = job;
        Job[] ret = new Job[n];
        System.arraycopy(newJobs, 0, ret, 0, n);
        return ret;
    }
    private static void deoptimize(int sum) {
        if (sum == 42)
            System.out.println("the answer");
    }
    public static void main(String[] args) throws Throwable {
        final int iterations = intArg(args, "iterations", 10000);
        final int size       = intArg(args, "size", 1024);
        final Pattern filter = patternArg(args, "filter");
        final Random rnd = new Random();
        final ByteBuffer b = ByteBuffer.allocateDirect(8*size);
        for (int i = 0; i < b.limit(); i++)
            b.put(i, (byte) rnd.nextInt());
        Job[] jobs = {
            new Job("swap char BIG_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.BIG_ENDIAN);
                    CharBuffer x = b.asCharBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}},
            new Job("swap char LITTLE_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.LITTLE_ENDIAN);
                    CharBuffer x = b.asCharBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}},
            new Job("swap short BIG_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.BIG_ENDIAN);
                    ShortBuffer x = b.asShortBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}},
            new Job("swap short LITTLE_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.LITTLE_ENDIAN);
                    ShortBuffer x = b.asShortBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}},
            new Job("swap int BIG_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.BIG_ENDIAN);
                    IntBuffer x = b.asIntBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}},
            new Job("swap int LITTLE_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.LITTLE_ENDIAN);
                    IntBuffer x = b.asIntBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}},
            new Job("swap long BIG_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.BIG_ENDIAN);
                    LongBuffer x = b.asLongBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}},
            new Job("swap long LITTLE_ENDIAN") {
                public void work() throws Throwable {
                    b.order(ByteOrder.LITTLE_ENDIAN);
                    LongBuffer x = b.asLongBuffer();
                    for (int i = 0; i < iterations; i++) {
                        int sum = 0;
                        for (int j = 0, end = x.limit(); j < end; j++)
                            sum += x.get(j);
                        deoptimize(sum);}}}
        };
        time(filter(filter, jobs));
    }
}
