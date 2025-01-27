public class IteratorWeakConsistency {
    void test(String[] args) throws Throwable {
        test(new LinkedBlockingQueue());
        test(new LinkedBlockingQueue(20));
        test(new LinkedBlockingDeque());
        test(new LinkedBlockingDeque(20));
        test(new ConcurrentLinkedDeque());
        test(new ConcurrentLinkedQueue());
        test(new LinkedTransferQueue());
        test(new ArrayBlockingQueue(20));
    }
    void test(Queue q) {
        try {
            for (int i = 0; i < 10; i++)
                q.add(i);
            Iterator it = q.iterator();
            q.poll();
            q.poll();
            q.poll();
            q.remove(7);
            List list = new ArrayList();
            while (it.hasNext())
                list.add(it.next());
            equal(list, Arrays.asList(0, 3, 4, 5, 6, 8, 9));
            check(! list.contains(null));
            System.out.printf("%s: %s%n",
                              q.getClass().getSimpleName(),
                              list);
        } catch (Throwable t) { unexpected(t); }
        try {
            q.clear();
            q.add(1);
            q.add(2);
            q.add(3);
            q.add(4);
            Iterator it = q.iterator();
            it.next();
            q.remove(2);
            q.remove(1);
            q.remove(3);
            boolean found4 = false;
            while (it.hasNext()) {
                found4 |= it.next().equals(4);
            }
            check(found4);
        } catch (Throwable t) { unexpected(t); }
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
        new IteratorWeakConsistency().instanceMain(args);}
    public void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
