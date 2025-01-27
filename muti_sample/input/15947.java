public class InterfaceHash {
    private static final int PORT = 2020;
    private static final String NAME = "WMM";
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4472769");
        System.err.println(
            "\n=== verifying that J2SE registry's skeleton uses" +
            "\ncorrect interface hash and operation numbers:");
        Registry testImpl = LocateRegistry.createRegistry(PORT);
        System.err.println("created test registry on port " + PORT);
        RemoteRef ref = new UnicastRef(
            new LiveRef(new ObjID(ObjID.REGISTRY_ID),
                        new TCPEndpoint("", PORT), false));
        Registry referenceStub = new ReferenceRegistryStub(ref);
        System.err.println("created reference registry stub: " +
                           referenceStub);
        referenceStub.bind(NAME, referenceStub);
        System.err.println("bound name \"" + NAME + "\" in registry");
        String[] list = referenceStub.list();
        System.err.println("list of registry contents: " +
                           Arrays.asList(list));
        if (list.length != 1 || !list[0].equals(NAME)) {
            throw new RuntimeException(
                "TEST FAILED: unexpected list contents");
        }
        Registry result = (Registry) referenceStub.lookup(NAME);
        System.err.println("lookup of name \"" + NAME + "\" returned: " +
                           result);
        if (!result.equals(referenceStub)) {
            throw new RuntimeException(
                "TEST FAILED: unexpected lookup result");
        }
        referenceStub.rebind(NAME, referenceStub);
        referenceStub.unbind(NAME);
        System.err.println("unbound name \"" + NAME + "\"");
        list = referenceStub.list();
        System.err.println("list of registry contents: " +
                           Arrays.asList(list));
        if (list.length != 0) {
            throw new RuntimeException("TEST FAILED: list not empty");
        }
        System.err.println("\n=== verifying that J2SE registry's stub uses" +
                           "correct interface hash:");
        class FakeRemoteRef implements RemoteRef {
            long hash;
            int opnum;
            public RemoteCall newCall(RemoteObject obj, Operation[] op,
                                      int opnum, long hash)
            {
                this.hash = hash;
                this.opnum = opnum;
                throw new UnsupportedOperationException();
            }
            public void invoke(RemoteCall call) { }
            public void done(RemoteCall call) { }
            public Object invoke(Remote obj, Method method,
                                 Object[] args, long hash)
            {
                throw new UnsupportedOperationException();
            }
            public String getRefClass(java.io.ObjectOutput out) {
                return "FakeRemoteRef";
            }
            public int remoteHashCode() { return 1013; }
            public boolean remoteEquals(RemoteRef obj) { return false; }
            public String remoteToString() { return "FakeRemoteRef"; }
            public void writeExternal(java.io.ObjectOutput out) { }
            public void readExternal(java.io.ObjectInput in) { }
        }
        FakeRemoteRef f = new FakeRemoteRef();
        Registry testRegistry = LocateRegistry.getRegistry(PORT);
        System.err.println("created original test registry stub: " +
                           testRegistry);
        Class stubClass = testRegistry.getClass();
        System.err.println("test registry stub class: " + stubClass);
        Constructor cons = stubClass.getConstructor(
            new Class[] { RemoteRef.class });
        Registry testStub = (Registry) cons.newInstance(
            new Object[] { f });
        System.err.println("created new instrumented test registry stub: " +
                           testStub);
        System.err.println("invoking bind:");
        try {
            testStub.bind(NAME, referenceStub);
        } catch (UnsupportedOperationException e) {
        }
        System.err.println("hash == " + f.hash + ", opnum == " + f.opnum);
        if (f.hash != 4905912898345647071L) {
            throw new RuntimeException("TEST FAILED: wrong interface hash");
        } else if (f.opnum != 0) {
            throw new RuntimeException("TEST FAILED: wrong operation number");
        }
        System.err.println("invoking list:");
        try {
            testStub.list();
        } catch (UnsupportedOperationException e) {
        }
        System.err.println("hash == " + f.hash + ", opnum == " + f.opnum);
        if (f.hash != 4905912898345647071L) {
            throw new RuntimeException("TEST FAILED: wrong interface hash");
        } else if (f.opnum != 1) {
            throw new RuntimeException("TEST FAILED: wrong operation number");
        }
        System.err.println("invoking lookup:");
        try {
            testStub.lookup(NAME);
        } catch (UnsupportedOperationException e) {
        }
        System.err.println("hash == " + f.hash + ", opnum == " + f.opnum);
        if (f.hash != 4905912898345647071L) {
            throw new RuntimeException("TEST FAILED: wrong interface hash");
        } else if (f.opnum != 2) {
            throw new RuntimeException("TEST FAILED: wrong operation number");
        }
        System.err.println("invoking rebind:");
        try {
            testStub.rebind(NAME, referenceStub);
        } catch (UnsupportedOperationException e) {
        }
        System.err.println("hash == " + f.hash + ", opnum == " + f.opnum);
        if (f.hash != 4905912898345647071L) {
            throw new RuntimeException("TEST FAILED: wrong interface hash");
        } else if (f.opnum != 3) {
            throw new RuntimeException("TEST FAILED: wrong operation number");
        }
        System.err.println("invoking unbind:");
        try {
            testStub.unbind(NAME);
        } catch (UnsupportedOperationException e) {
        }
        System.err.println("hash == " + f.hash + ", opnum == " + f.opnum);
        if (f.hash != 4905912898345647071L) {
            throw new RuntimeException("TEST FAILED: wrong interface hash");
        } else if (f.opnum != 4) {
            throw new RuntimeException("TEST FAILED: wrong operation number");
        }
        System.err.println("TEST PASSED");
    }
}
