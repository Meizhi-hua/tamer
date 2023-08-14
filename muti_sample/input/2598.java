public class SSLContext {
    private Provider provider;
    private SSLContextSpi contextSpi;
    private String protocol;
    protected SSLContext(SSLContextSpi contextSpi, Provider provider,
        String protocol) {
        this.contextSpi = contextSpi;
        this.provider = provider;
        this.protocol = protocol;
    }
    public static SSLContext getInstance(String protocol)
        throws NoSuchAlgorithmException
    {
        try {
            Object[] objs = SSLSecurity.getImpl(protocol, "SSLContext",
                                                (String) null);
            return new SSLContext((SSLContextSpi)objs[0], (Provider)objs[1],
                protocol);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(protocol + " not found");
        }
    }
    public static SSLContext getInstance(String protocol, String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = SSLSecurity.getImpl(protocol, "SSLContext",
                                            provider);
        return new SSLContext((SSLContextSpi)objs[0], (Provider)objs[1],
            protocol);
    }
    public static SSLContext getInstance(String protocol, Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider == null)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = SSLSecurity.getImpl(protocol, "SSLContext",
                                            provider);
        return new SSLContext((SSLContextSpi)objs[0], (Provider)objs[1],
            protocol);
    }
    public final String getProtocol() {
        return this.protocol;
    }
    public final Provider getProvider() {
        return this.provider;
    }
    public final void init(KeyManager[] km, TrustManager[] tm,
                                SecureRandom random)
        throws KeyManagementException {
        contextSpi.engineInit(km, tm, random);
    }
    public final SSLSocketFactory getSocketFactory() {
        return contextSpi.engineGetSocketFactory();
    }
    public final SSLServerSocketFactory getServerSocketFactory() {
        return contextSpi.engineGetServerSocketFactory();
    }
}