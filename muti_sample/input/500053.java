public final class KeyPair implements Serializable {
    private static final long serialVersionUID = -7565189502268009837L;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
    public PrivateKey getPrivate() {
        return privateKey;
    }
    public PublicKey getPublic() {
        return publicKey;
    }
}
