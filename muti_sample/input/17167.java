public class NullCipher extends Cipher {
    public NullCipher() {
        super(new NullCipherSpi(), null);
    }
}
