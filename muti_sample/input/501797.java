public abstract class CertPathValidatorSpi {
    public CertPathValidatorSpi() {
    }
    public abstract CertPathValidatorResult engineValidate(CertPath certPath,
            CertPathParameters params) throws CertPathValidatorException,
            InvalidAlgorithmParameterException;
}
