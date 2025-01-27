final class X509TrustManagerImpl extends X509ExtendedTrustManager
        implements X509TrustManager {
    private final String validatorType;
    private final Collection<X509Certificate> trustedCerts;
    private final PKIXBuilderParameters pkixParams;
    private volatile Validator clientValidator, serverValidator;
    private static final Debug debug = Debug.getInstance("ssl");
    X509TrustManagerImpl(String validatorType, KeyStore ks)
            throws KeyStoreException {
        this.validatorType = validatorType;
        this.pkixParams = null;
        if (ks == null) {
            trustedCerts = Collections.<X509Certificate>emptySet();
        } else {
            trustedCerts = KeyStores.getTrustedCerts(ks);
        }
        showTrustedCerts();
    }
    X509TrustManagerImpl(String validatorType, PKIXBuilderParameters params) {
        this.validatorType = validatorType;
        this.pkixParams = params;
        Validator v = getValidator(Validator.VAR_TLS_SERVER);
        trustedCerts = v.getTrustedCertificates();
        serverValidator = v;
        showTrustedCerts();
    }
    @Override
    public void checkClientTrusted(X509Certificate chain[], String authType)
            throws CertificateException {
        checkTrusted(chain, authType, (Socket)null, true);
    }
    @Override
    public void checkServerTrusted(X509Certificate chain[], String authType)
            throws CertificateException {
        checkTrusted(chain, authType, (Socket)null, false);
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] certsArray = new X509Certificate[trustedCerts.size()];
        trustedCerts.toArray(certsArray);
        return certsArray;
    }
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType,
                Socket socket) throws CertificateException {
        checkTrusted(chain, authType, socket, true);
    }
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType,
            Socket socket) throws CertificateException {
        checkTrusted(chain, authType, socket, false);
    }
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType,
            SSLEngine engine) throws CertificateException {
        checkTrusted(chain, authType, engine, true);
    }
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType,
            SSLEngine engine) throws CertificateException {
        checkTrusted(chain, authType, engine, false);
    }
    private Validator checkTrustedInit(X509Certificate[] chain,
                                        String authType, boolean isClient) {
        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException(
                "null or zero-length certificate chain");
        }
        if (authType == null || authType.length() == 0) {
            throw new IllegalArgumentException(
                "null or zero-length authentication type");
        }
        Validator v = null;
        if (isClient) {
            v = clientValidator;
            if (v == null) {
                synchronized (this) {
                    v = clientValidator;
                    if (v == null) {
                        v = getValidator(Validator.VAR_TLS_CLIENT);
                        clientValidator = v;
                    }
                }
            }
        } else {
            v = serverValidator;
            if (v == null) {
                synchronized (this) {
                    v = serverValidator;
                    if (v == null) {
                        v = getValidator(Validator.VAR_TLS_SERVER);
                        serverValidator = v;
                    }
                }
            }
        }
        return v;
    }
    private void checkTrusted(X509Certificate[] chain, String authType,
                Socket socket, boolean isClient) throws CertificateException {
        Validator v = checkTrustedInit(chain, authType, isClient);
        AlgorithmConstraints constraints = null;
        if ((socket != null) && socket.isConnected() &&
                                        (socket instanceof SSLSocket)) {
            SSLSocket sslSocket = (SSLSocket)socket;
            SSLSession session = sslSocket.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            String identityAlg = sslSocket.getSSLParameters().
                                        getEndpointIdentificationAlgorithm();
            if (identityAlg != null && identityAlg.length() != 0) {
                String hostname = session.getPeerHost();
                checkIdentity(hostname, chain[0], identityAlg);
            }
            ProtocolVersion protocolVersion =
                ProtocolVersion.valueOf(session.getProtocol());
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                if (session instanceof ExtendedSSLSession) {
                    ExtendedSSLSession extSession =
                                    (ExtendedSSLSession)session;
                    String[] localSupportedSignAlgs =
                            extSession.getLocalSupportedSignatureAlgorithms();
                    constraints = new SSLAlgorithmConstraints(
                                    sslSocket, localSupportedSignAlgs, false);
                } else {
                    constraints =
                            new SSLAlgorithmConstraints(sslSocket, false);
                }
            } else {
                constraints = new SSLAlgorithmConstraints(sslSocket, false);
            }
        }
        X509Certificate[] trustedChain = null;
        if (isClient) {
            trustedChain = validate(v, chain, constraints, null);
        } else {
            trustedChain = validate(v, chain, constraints, authType);
        }
        if (debug != null && Debug.isOn("trustmanager")) {
            System.out.println("Found trusted certificate:");
            System.out.println(trustedChain[trustedChain.length - 1]);
        }
    }
    private void checkTrusted(X509Certificate[] chain, String authType,
            SSLEngine engine, boolean isClient) throws CertificateException {
        Validator v = checkTrustedInit(chain, authType, isClient);
        AlgorithmConstraints constraints = null;
        if (engine != null) {
            SSLSession session = engine.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            String identityAlg = engine.getSSLParameters().
                                        getEndpointIdentificationAlgorithm();
            if (identityAlg != null && identityAlg.length() != 0) {
                String hostname = session.getPeerHost();
                checkIdentity(hostname, chain[0], identityAlg);
            }
            ProtocolVersion protocolVersion =
                ProtocolVersion.valueOf(session.getProtocol());
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                if (session instanceof ExtendedSSLSession) {
                    ExtendedSSLSession extSession =
                                    (ExtendedSSLSession)session;
                    String[] localSupportedSignAlgs =
                            extSession.getLocalSupportedSignatureAlgorithms();
                    constraints = new SSLAlgorithmConstraints(
                                    engine, localSupportedSignAlgs, false);
                } else {
                    constraints =
                            new SSLAlgorithmConstraints(engine, false);
                }
            } else {
                constraints = new SSLAlgorithmConstraints(engine, false);
            }
        }
        X509Certificate[] trustedChain = null;
        if (isClient) {
            trustedChain = validate(v, chain, constraints, null);
        } else {
            trustedChain = validate(v, chain, constraints, authType);
        }
        if (debug != null && Debug.isOn("trustmanager")) {
            System.out.println("Found trusted certificate:");
            System.out.println(trustedChain[trustedChain.length - 1]);
        }
    }
    private void showTrustedCerts() {
        if (debug != null && Debug.isOn("trustmanager")) {
            for (X509Certificate cert : trustedCerts) {
                System.out.println("adding as trusted cert:");
                System.out.println("  Subject: "
                                        + cert.getSubjectX500Principal());
                System.out.println("  Issuer:  "
                                        + cert.getIssuerX500Principal());
                System.out.println("  Algorithm: "
                                        + cert.getPublicKey().getAlgorithm()
                                        + "; Serial number: 0x"
                                        + cert.getSerialNumber().toString(16));
                System.out.println("  Valid from "
                                        + cert.getNotBefore() + " until "
                                        + cert.getNotAfter());
                System.out.println();
            }
        }
    }
    private Validator getValidator(String variant) {
        Validator v;
        if (pkixParams == null) {
            v = Validator.getInstance(validatorType, variant, trustedCerts);
        } else {
            v = Validator.getInstance(validatorType, variant, pkixParams);
        }
        return v;
    }
    private static X509Certificate[] validate(Validator v,
            X509Certificate[] chain, AlgorithmConstraints constraints,
            String authType) throws CertificateException {
        Object o = JsseJce.beginFipsProvider();
        try {
            return v.validate(chain, null, constraints, authType);
        } finally {
            JsseJce.endFipsProvider(o);
        }
    }
    static void checkIdentity(String hostname, X509Certificate cert,
            String algorithm) throws CertificateException {
        if (algorithm != null && algorithm.length() != 0) {
            if ((hostname != null) && hostname.startsWith("[") &&
                    hostname.endsWith("]")) {
                hostname = hostname.substring(1, hostname.length() - 1);
            }
            if (algorithm.equalsIgnoreCase("HTTPS")) {
                HostnameChecker.getInstance(HostnameChecker.TYPE_TLS).match(
                        hostname, cert);
            } else if (algorithm.equalsIgnoreCase("LDAP") ||
                    algorithm.equalsIgnoreCase("LDAPS")) {
                HostnameChecker.getInstance(HostnameChecker.TYPE_LDAP).match(
                        hostname, cert);
            } else {
                throw new CertificateException(
                        "Unknown identification algorithm: " + algorithm);
            }
        }
    }
}
