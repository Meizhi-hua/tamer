public abstract class SecureRandomSpi implements Serializable {
    private static final long serialVersionUID = -2991854161009191830L;
    protected abstract void engineSetSeed(byte[] seed);
    protected abstract void engineNextBytes(byte[] bytes);
    protected abstract byte[] engineGenerateSeed(int numBytes);
}
