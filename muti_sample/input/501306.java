public class UtilTest extends TestCase {
    private static final String ASCII_ALPHABET_LC = "abcdefghijklmnopqrstuvwxyz";
    private static final String ASCII_ALPHABET_UC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final byte[] ASCII_ALPHABET_LC_BYTES;
    private static final byte[] ASCII_ALPHABET_UC_BYTES;
    static {
        ASCII_ALPHABET_LC_BYTES = new byte[ASCII_ALPHABET_LC.length()];
        for (int i = 0; i < ASCII_ALPHABET_LC_BYTES.length; i++) {
            final char c = ASCII_ALPHABET_LC.charAt(i);
            final byte b = (byte) c;
            assert ((char) b) == c;
            ASCII_ALPHABET_LC_BYTES[i] = b;
        }
        ASCII_ALPHABET_UC_BYTES = new byte[ASCII_ALPHABET_UC.length()];
        for (int i = 0; i < ASCII_ALPHABET_UC_BYTES.length; i++) {
            final char c = ASCII_ALPHABET_UC.charAt(i);
            final byte b = (byte) c;
            assert ((char) b) == c;
            ASCII_ALPHABET_UC_BYTES[i] = b;
        }
    }
    public void testasciiEndsWithIgnoreCase() {
        final String s1 = ASCII_ALPHABET_LC;
        final String s2 = ASCII_ALPHABET_UC;
        assertTrue(Util.asciiEndsWithIgnoreCase(s1, s2));
        assertTrue(Util.asciiEndsWithIgnoreCase(s2, s1));
        assertTrue(Util.asciiEndsWithIgnoreCase(s1, "wxyz"));
    }
    public void testasciiEqualsIgnoreCase() {
        final String s1 = ASCII_ALPHABET_LC;
        final String s2 = ASCII_ALPHABET_UC;
        assertTrue(Util.asciiEqualsIgnoreCase(s1, s2));
        assertTrue(Util.asciiEqualsIgnoreCase(s2, s1));
    }
    public void testEqualsIgnoreCaseByteArrayByteArray() {
        assertTrue(Util.asciiEqualsIgnoreCase(ASCII_ALPHABET_LC_BYTES,
                ASCII_ALPHABET_LC_BYTES));
        assertTrue(Util.asciiEqualsIgnoreCase(ASCII_ALPHABET_LC_BYTES,
                ASCII_ALPHABET_UC_BYTES));
        assertTrue(Util.asciiEqualsIgnoreCase(ASCII_ALPHABET_UC_BYTES,
                ASCII_ALPHABET_UC_BYTES));
    }
}
