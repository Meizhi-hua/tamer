public class FloatCalls implements Benchmark {
    interface Server extends Remote {
        public float call(float val) throws RemoteException;
    }
    static class ServerImpl extends UnicastRemoteObject implements Server {
        public ServerImpl() throws RemoteException {
        }
        public float call(float val) throws RemoteException {
            return val;
        }
    }
    static class ServerFactory implements BenchServer.RemoteObjectFactory {
        public Remote create() throws RemoteException {
            return new ServerImpl();
        }
    }
    public long run(String[] args) throws Exception {
        int cycles = Integer.parseInt(args[0]);
        BenchServer bsrv = Main.getBenchServer();
        Server stub = (Server) bsrv.create(new ServerFactory());
        long start = System.currentTimeMillis();
        for (int i = 0; i < cycles; i++)
            stub.call((float) 0.0);
        long time = System.currentTimeMillis() - start;
        bsrv.unexport(stub, true);
        return time;
    }
}
