public class ValidatorException extends CertificateException {
    private static final long serialVersionUID = -2836879718282292155L;
    public final static Object T_NO_TRUST_ANCHOR =
        "No trusted certificate found";
    public final static Object T_EE_EXTENSIONS =
        "End entity certificate extension check failed";
    public final static Object T_CA_EXTENSIONS =
        "CA certificate extension check failed";
    public final static Object T_CERT_EXPIRED =
        "Certificate expired";
    public final static Object T_SIGNATURE_ERROR =
        "Certificate signature validation failed";
    public final static Object T_NAME_CHAINING =
        "Certificate chaining error";
    public final static Object T_ALGORITHM_DISABLED =
        "Certificate signature algorithm disabled";
    private Object type;
    private X509Certificate cert;
    public ValidatorException(String msg) {
        super(msg);
    }
    public ValidatorException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }
    public ValidatorException(Object type) {
        this(type, null);
    }
    public ValidatorException(Object type, X509Certificate cert) {
        super((String)type);
        this.type = type;
        this.cert = cert;
    }
    public ValidatorException(Object type, X509Certificate cert,
            Throwable cause) {
        this(type, cert);
        initCause(cause);
    }
    public ValidatorException(String msg, Object type, X509Certificate cert) {
        super(msg);
        this.type = type;
        this.cert = cert;
    }
    public ValidatorException(String msg, Object type, X509Certificate cert,
            Throwable cause) {
        this(msg, type, cert);
        initCause(cause);
    }
    public Object getErrorType() {
        return type;
    }
    public X509Certificate getErrorCertificate() {
        return cert;
    }
}
