public class CharSequencesTest extends TestCase {
    @SmallTest
    public void testCharSequences() {
        String s = "Crazy Bob";
        byte[] bytes = s.getBytes();
        String copy = toString(forAsciiBytes(bytes));
        assertTrue(s.equals(copy));
        copy = toString(forAsciiBytes(bytes, 0, s.length()));
        assertTrue(s.equals(copy));
        String crazy = toString(forAsciiBytes(bytes, 0, 5));
        assertTrue("Crazy".equals(crazy));
        String a = toString(forAsciiBytes(bytes, 0, 3).subSequence(2, 3));
        assertTrue("a".equals(a));
        String empty = toString(forAsciiBytes(bytes, 0, 3).subSequence(3, 3));
        assertTrue("".equals(empty));
        assertTrue(CharSequences.equals("bob", "bob"));
        assertFalse(CharSequences.equals("b", "bob"));
        assertFalse(CharSequences.equals("", "bob"));
    }
    static String toString(CharSequence charSequence) {
        return new StringBuilder().append(charSequence).toString();
    }
}
