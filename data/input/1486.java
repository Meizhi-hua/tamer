public class FloatDoubleOrder {
    void test(String[] args) throws Throwable {
        double[] unsortedDbl = new double[] {1.0d, 3.7d, Double.NaN, -2.0d,
           Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0d, -0.0d};
        double[] sortedDbl = new double[] {Double.NEGATIVE_INFINITY, -2.0d,
           -0.0d, 0.0d, 1.0d, 3.7d, Double.POSITIVE_INFINITY, Double.NaN};
        List list = new ArrayList();
        for (int i=0; i<unsortedDbl.length; i++)
            list.add(new Double(unsortedDbl[i]));
        Collections.sort(list);
        List sortedList = new ArrayList();
        for (int i=0; i<sortedDbl.length; i++)
            sortedList.add(new Double(sortedDbl[i]));
        check(list.equals(sortedList));
        Arrays.sort(unsortedDbl);
        check(Arrays.equals(unsortedDbl, sortedDbl));
        double negNan = Double.longBitsToDouble(0xfff8000000000000L);
        for (int i = 0; i < sortedDbl.length; i++) {
            equal(Arrays.binarySearch(sortedDbl, sortedDbl[i]), i);
            if (Double.isNaN(sortedDbl[i]))
                equal(Arrays.binarySearch(sortedDbl, negNan), i);
        }
        float[] unsortedFlt = new float[] {1.0f, 3.7f, Float.NaN, -2.0f,
           Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.0f, -0.0f};
        float[] sortedFlt = new float[] {Float.NEGATIVE_INFINITY, -2.0f,
           -0.0f, 0.0f, 1.0f, 3.7f, Float.POSITIVE_INFINITY, Float.NaN};
        list.clear();
        for (int i=0; i<unsortedFlt.length; i++)
            list.add(new Float(unsortedFlt[i]));
        Collections.sort(list);
        sortedList.clear();
        for (int i=0; i<sortedFlt.length; i++)
            sortedList.add(new Float(sortedFlt[i]));
        check(list.equals(sortedList));
        Arrays.sort(unsortedFlt);
        check(Arrays.equals(unsortedFlt, sortedFlt));
        float negNaN = Float.intBitsToFloat(0xFfc00000);
        for (int i = 0; i < sortedDbl.length; i++) {
            equal(Arrays.binarySearch(sortedFlt, sortedFlt[i]), i);
            if (Float.isNaN(sortedFlt[i]))
                equal(Arrays.binarySearch(sortedFlt, negNaN), i);
        }
        double[] da = {-0.0d, -0.0d, 0.0d, -0.0d};
        Arrays.sort(da, 1, 4);
        check(Arrays.equals(da, new double[] {-0.0d, -0.0d, -0.0d, 0.0d}));
        float[] fa = {-0.0f, -0.0f, 0.0f, -0.0f};
        Arrays.sort(fa, 1, 4);
        check(Arrays.equals(fa, new float[] {-0.0f, -0.0f, -0.0f, 0.0f}));
    }
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        new FloatDoubleOrder().instanceMain(args);}
    void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
