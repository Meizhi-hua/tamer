public class ReverseOrder2 {
    final static int N = 100;
    static void realMain(String[] args) throws Throwable {
        check(Collections.reverseOrder()
              == Collections.reverseOrder(null));
        check(Collections.reverseOrder()
              == reincarnate(Collections.reverseOrder()));
        check(Collections.reverseOrder(Collections.reverseOrder(cmp))
              == cmp);
        equal(Collections.reverseOrder(cmp),
              Collections.reverseOrder(cmp));
        equal(Collections.reverseOrder(cmp).hashCode(),
              Collections.reverseOrder(cmp).hashCode());
        check(Collections.reverseOrder(cmp).hashCode() !=
              cmp.hashCode());
        test(new ArrayList<String>());
        test(new LinkedList<String>());
        test2(new ArrayList<Integer>());
        test2(new LinkedList<Integer>());
    }
    static void test(List<String> list) {
        for (int i = 0; i < N; i++)
            list.add(String.valueOf(i));
        Collections.shuffle(list);
        Collections.sort(list, Collections.reverseOrder(cmp));
        equal(list, golden);
    }
    private static Comparator<String> cmp = new Comparator<String> () {
        public int compare(String s1, String s2) {
            int i1 = Integer.parseInt(s1);
            int i2 = Integer.parseInt(s2);
            return (i1 < i2 ? Integer.MIN_VALUE : (i1 == i2 ? 0 : 1));
        }
    };
    private final static List<String> golden = new ArrayList<String>(N);
    static {
        for (int i = N-1; i >= 0; i--)
            golden.add(String.valueOf(i));
    }
    static void test2(List<Integer> list) {
        for (int i = 0; i < N; i++)
            list.add(i);
        Collections.shuffle(list);
        Collections.sort(list, Collections.reverseOrder(null));
        equal(list, golden2);
    }
    private final static List<Integer> golden2 = new ArrayList<Integer>(N);
    static {
        for (int i = N-1; i >= 0; i--)
            golden2.add(i);
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
    static byte[] serializedForm(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {throw new RuntimeException(e);}}
    static Object readObject(byte[] bytes)
        throws IOException, ClassNotFoundException {
        InputStream is = new ByteArrayInputStream(bytes);
        return new ObjectInputStream(is).readObject();}
    @SuppressWarnings("unchecked")
    static <T> T reincarnate(T obj) {
        try {return (T) readObject(serializedForm(obj));}
        catch (Exception e) {throw new RuntimeException(e);}}
}
