abstract public class RemoteStub extends RemoteObject {
    private static final long serialVersionUID = -1585587260594494182L;
    protected RemoteStub() {
        super();
    }
    protected RemoteStub(RemoteRef ref) {
        super(ref);
    }
    @Deprecated
    protected static void setRef(RemoteStub stub, RemoteRef ref) {
        throw new UnsupportedOperationException();
    }
}
