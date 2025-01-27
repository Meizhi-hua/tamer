public class SSLContextSpiImpl extends MySSLContextSpi {
    private boolean init = false;
    public void engineInit(KeyManager[] km, TrustManager[] tm,
            SecureRandom sr) throws KeyManagementException {
        if (sr == null) {
            throw new KeyManagementException(
                    "secureRandom is null");
        }
        init = true;
    }
    public SSLSocketFactory engineGetSocketFactory() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }   
        return null;
    }
    public SSLServerSocketFactory engineGetServerSocketFactory() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return null;
    }
    public SSLSessionContext engineGetServerSessionContext() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return null;
    }
    public SSLSessionContext engineGetClientSessionContext() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return null;
    }
    public SSLEngine engineCreateSSLEngine(String host, int port) {
        int max = 65535;
        if (port < 0 || port > max) {
            throw new IllegalArgumentException("Illegal port");
        }
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return new tmpSSLEngine(host, port);
    }
    public SSLEngine engineCreateSSLEngine() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return new tmpSSLEngine();
    }
}
