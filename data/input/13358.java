public class AttributeListTypeSafeTest {
    private static String failure;
    public static void main(String[] args) throws Exception {
        for (Op op : Op.values()) {
            AttributeList alist = new AttributeList();
            alist.add(new Attribute("foo", "bar"));
            doOp(alist, op);
            String what = "asList() after calling " + op + " with non-Attribute";
            try {
                List<Attribute> lista = alist.asList();
                fail(what + ": succeeded but should not have");
            } catch (IllegalArgumentException e) {
                System.out.println("OK: " + what + ": got IllegalArgumentException");
            }
        }
        for (Op op : Op.values()) {
            AttributeList alist = new AttributeList();
            List<Attribute> lista = alist.asList();
            lista.add(new Attribute("foo", "bar"));
            String what = op + " with non-Attribute after calling asList()";
            try {
                doOp(alist, op);
                fail(what + ": succeeded but should not have");
            } catch (IllegalArgumentException e) {
                System.out.println("OK: " + what + ": got IllegalArgumentException");
            }
        }
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static enum Op {
        ADD("add(Object)"), ADD_AT("add(int, Object)"),
        ADD_ALL("add(Collection)"), ADD_ALL_AT("add(int, Collection)"),
        SET("set(int, Object)");
        private Op(String what) {
            this.what = what;
        }
        @Override
        public String toString() {
            return what;
        }
        private final String what;
    }
    private static void doOp(AttributeList alist, Op op) {
        Object x = "oops";
        switch (op) {
            case ADD: alist.add(x); break;
            case ADD_AT: alist.add(0, x); break;
            case ADD_ALL: alist.add(Collections.singleton(x)); break;
            case ADD_ALL_AT: alist.add(0, Collections.singleton(x)); break;
            case SET: alist.set(0, x); break;
            default: throw new AssertionError("Case not covered");
        }
    }
    private static void fail(String why) {
        System.out.println("FAIL: " + why);
        failure = why;
    }
}
